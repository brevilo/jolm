/*
 * Copyright 2021 Oliver Behnke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.brevilo.jolm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import io.github.brevilo.jolm.jna.NativeSize;
import io.github.brevilo.jolm.jna.OlmObject;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/** Internal helper methods to reduce code clutter. */
public class Utils {

  /**
   * Generic initializer providing a backing store for olm objects.
   *
   * @param <T> olm object type
   * @param function reference to olm object initializer function
   * @param getSize reference to olm object sizing function
   * @return instance of the allocated olm object
   * @throws RuntimeException olm object's size function returned an error
   * @throws OutOfMemoryError backing store could not be allocated
   */
  public static <T extends OlmObject> T initialize(
      Function<Pointer, T> function, Callable<NativeSize> getSize)
      throws RuntimeException, OutOfMemoryError {

    // allocate required buffer
    NativeSize size;
    try {
      size = getSize.call();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    Memory buffer = new Memory(size.longValue());

    // create instance within buffer
    T instance = function.apply(buffer);
    if (instance == null) {
      throw new OutOfMemoryError();
    }

    // keep backing store (prevent premature GCing)
    instance.setBackingStore(buffer);

    return instance;
  }

  /**
   * Converts a UTF-8 string into a raw memory buffer.
   *
   * @param content string to be converted
   * @return raw memory buffer containing the converted string
   * @throws RuntimeException UTF-8 is unsupported
   */
  public static Memory toNative(String content) throws RuntimeException {
    try {
      byte[] contentBytes;
      contentBytes = content.getBytes(Constant.UTF8);
      int contentLength = contentBytes.length;
      Memory contentBuffer = new Memory(contentLength);
      contentBuffer.write(0, contentBytes, 0, contentLength);
      return contentBuffer;
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Converts a raw memory buffer into a UTF-8 string.
   *
   * @param buffer memory buffer to convert
   * @return trimmed string representation of the memory buffer
   * @throws RuntimeException UTF-8 is unsupported
   */
  public static String fromNative(Memory buffer) {
    // shorten to actual string length
    NativeSize size = new NativeSize(buffer.size());
    Long stringEnd = new Long(buffer.indexOf(0, (byte) 0));
    int length = stringEnd.intValue() < size.intValue() ? stringEnd.intValue() : size.intValue();

    try {
      // return trimmed string
      return new String(buffer.getByteArray(0, length), Constant.UTF8).trim();
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Allocates a memory buffer and fills it with random data.
   *
   * @param size buffer size in bytes
   * @return random data buffer
   */
  public static Memory randomBuffer(NativeSize size) {
    SecureRandom rng = new SecureRandom();
    Memory buffer = new Memory(size.longValue());

    // prepare buffer chunking (n integer blocks + remaining bytes)
    long intChunks = size.longValue() / Integer.BYTES;
    int remainder = (int) (size.longValue() % Integer.BYTES);
    long remainderOffset = intChunks * Integer.BYTES;

    // fill buffer with random ints
    for (long chunk = 0; chunk < intChunks; chunk++) {
      buffer.setInt(chunk * Integer.BYTES, rng.nextInt());
    }

    // fill buffer's remaining bytes
    byte[] bytes = new byte[remainder];
    rng.nextBytes(bytes);
    for (int i = 0; i < remainder; i++) {
      buffer.setByte(remainderOffset + i, bytes[i]);
    }

    return buffer;
  }

  /**
   * Functional consumer interface used by {@link Utils#pickle(PointerByReference, String, Function,
   * OlmPickler, Consumer)} and {@link Utils#unpickle(PointerByReference, String, String,
   * OlmPickler, BiConsumer)}.
   *
   * @param <T> olm object type
   * @param <K> key type
   * @param <KL> key length type
   * @param <P> pickled content (encrypted/serialized) type
   * @param <PL> length of pickled content type
   */
  @FunctionalInterface
  interface OlmPickler<T, K, L, P, S> {
    public NativeSize apply(T olmInstance, K key, L keyLength, P pickled, S pickledSize);
  }

  /**
   * Generic helper method to pickle (serialize and encrypt) olm objects.
   *
   * @param <T> olm object type
   * @param instance olm object
   * @param key encryption key
   * @param pickleLength method reference to type-specific olm_pickle_TYPE_length()
   * @param pickle method reference to type-specific olm_pickle_TYPE()
   * @param checkOlmResult method reference to the type-specific error checker
   * @return encrypted serialized instance
   */
  public static <T extends PointerByReference> String pickle(
      T instance,
      String key,
      Function<T, NativeSize> pickleLength,
      OlmPickler<T, Pointer, NativeSize, Pointer, NativeSize> pickle,
      Consumer<NativeSize> checkOlmResult) {

    // prepare key
    Memory keyBuffer = Utils.toNative(key);

    // allocate required output buffer
    NativeSize pickledLength = pickleLength.apply(instance);
    Memory pickled = new Memory(pickledLength.longValue());

    // call olm
    NativeSize result =
        pickle.apply(instance, keyBuffer, new NativeSize(keyBuffer), pickled, pickledLength);

    checkOlmResult.accept(result);

    // return pickled olm instance
    return Utils.fromNative(pickled);
  }

  /**
   * Generic helper method to unpickle (decrypt and deserialize) olm objects.
   *
   * @param <T> olm object type
   * @param instance olm object
   * @param key decryption key
   * @param pickle encrypted serialized instance
   * @param unpickle method reference to type-specific olm_unpickle_TYPE()
   * @param checkOlmResult method reference to the type-specific error checker
   */
  public static <T extends PointerByReference> void unpickle(
      T instance,
      String key,
      String pickle,
      OlmPickler<T, Pointer, NativeSize, Pointer, NativeSize> unpickle,
      BiConsumer<T, NativeSize> checkOlmResult) {

    // prepare key and pickle data
    Memory keyBuffer = Utils.toNative(key);
    Memory pickledBuffer = Utils.toNative(pickle);

    // call olm
    NativeSize result =
        unpickle.apply(
            instance,
            keyBuffer,
            new NativeSize(keyBuffer),
            pickledBuffer,
            new NativeSize(pickledBuffer));

    checkOlmResult.accept(instance, result);
  }

  /**
   * Takes a JSON fragment and signs it.
   *
   * @see <a href="https://matrix.org/docs/spec/appendices#signing-json">Matrix: Signing JSON</a>
   * @param account account instance to be used for signing
   * @param json JSON fragment to sign
   * @param userId user entity signing the fragment
   * @param keyAlgorithm algorithm used for signing
   * @param deviceId device ID for which to sign (used as key identifier)
   * @return original JSON fragment, augmented by a new signature
   * @throws OlmException <code>OUTPUT_BUFFER_TOO_SMALL</code> if the signature buffer was too small
   * @throws JsonProcessingException (de)serialization error
   */
  public static String signJson(
      Account account, String json, String userId, String keyAlgorithm, String deviceId)
      throws OlmException, JsonProcessingException {

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);

    // parse JSON string
    ObjectNode node = (ObjectNode) objectMapper.readTree(json);

    // strip nodes not to be signed
    JsonNode signaturesNode = node.remove(Constant.JSON_SIGNATURES);
    final JsonNode unsignedNode = node.remove(Constant.JSON_UNSIGNED);

    // sign canonical JSON
    String signature = account.sign(objectMapper.writeValueAsString(node));

    // add signature node
    if (signaturesNode == null || signaturesNode.isNull()) {
      signaturesNode = objectMapper.createObjectNode();
    }
    ObjectNode keyNode = objectMapper.createObjectNode();
    keyNode.put(String.join(":", keyAlgorithm, deviceId), signature);
    ((ObjectNode) signaturesNode).set(userId, keyNode);
    node.set("signatures", signaturesNode);

    // add back stripped unsigned node
    if (unsignedNode != null) {
      node.set("unsigned", unsignedNode);
    }

    return objectMapper.writeValueAsString(node);
  }

  /** Exception representing error messages returned by olm function calls. */
  public static class OlmException extends Exception {
    private static final long serialVersionUID = 1L;

    public OlmException(String message) {
      super(message);
    }
  }
}
