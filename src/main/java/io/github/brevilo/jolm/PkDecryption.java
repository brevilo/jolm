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

import com.sun.jna.Memory;
import io.github.brevilo.jolm.Utils.OlmException;
import io.github.brevilo.jolm.jna.NativeSize;
import io.github.brevilo.jolm.jna.OlmLibrary;
import io.github.brevilo.jolm.jna.OlmPkDecryption;
import io.github.brevilo.jolm.model.PkMessage;

/** Class to represent an Olm decryption object. */
public class PkDecryption {

  // backing store
  public final OlmPkDecryption instance;

  private String publicKey;

  /**
   * Creates a new decryption object and initializes a new (random) key pair.
   *
   * @throws OlmException <code>OLM_INPUT_BUFFER_TOO_SMALL</code> if the private key was not long
   *     enough; <code>OUTPUT_BUFFER_TOO_SMALL</code> if the public key buffer was too small
   */
  public PkDecryption() throws OlmException {
    // initialize backing store
    instance = Utils.initialize(OlmLibrary::olm_pk_decryption, OlmLibrary::olm_pk_decryption_size);

    // generate random private key
    NativeSize privateKeyLength = OlmLibrary.olm_pk_private_key_length();
    Memory privateKeyBuffer = Utils.randomBuffer(privateKeyLength);

    // prepare output buffer
    NativeSize publicKeyLength = OlmLibrary.olm_pk_key_length();
    Memory publicKeyBuffer = new Memory(publicKeyLength.longValue());

    // call olm
    NativeSize result =
        OlmLibrary.olm_pk_key_from_private(
            instance, publicKeyBuffer, publicKeyLength, privateKeyBuffer, privateKeyLength);

    // clear the random private key buffer
    privateKeyBuffer.clear();

    checkOlmResult(result);

    // store associated public key
    this.publicKey = Utils.fromNative(publicKeyBuffer);
  }

  /** Clears the memory used to back this decryption object. */
  public void clear() {
    OlmLibrary.olm_clear_pk_decryption(instance);
  }

  /**
   * Gets the public key. To be used with the corresponding encryption object in {@link
   * PkEncryption#PkEncryption(String)}.
   *
   * @return public key
   */
  public String publicKey() {
    return publicKey;
  }

  /**
   * Get the private key for a decryption object.
   *
   * @return private key
   * @throws OlmException <code>OUTPUT_BUFFER_TOO_SMALL</code> if the private key buffer is too
   *     small
   */
  public String privateKey() throws OlmException {
    // prepare output buffer
    NativeSize privateKeyLength = OlmLibrary.olm_pk_private_key_length();
    Memory privateKeyBuffer = new Memory(privateKeyLength.longValue());

    // call olm
    NativeSize result =
        OlmLibrary.olm_pk_get_private_key(instance, privateKeyBuffer, privateKeyLength);

    // clear the private key buffer
    String privateKey = Utils.fromNative(privateKeyBuffer);
    privateKeyBuffer.clear();

    checkOlmResult(result);

    return privateKey;
  }

  /**
   * Decrypt a ciphertext.
   *
   * @param message encrypted message to decrypt
   * @return decrypted message
   * @throws OlmException <code>OUTPUT_BUFFER_TOO_SMALL</code> if the plaintext buffer is too small
   */
  public String decrypt(PkMessage message) throws OlmException {
    // get native message content
    Memory cipherTextBuffer = Utils.toNative(message.getCipherText());
    Memory macBuffer = Utils.toNative(message.getMac());
    Memory ephemeralBuffer = Utils.toNative(message.getEphemeral());

    // prepare output buffer
    NativeSize plainTextLength =
        OlmLibrary.olm_pk_max_plaintext_length(instance, new NativeSize(cipherTextBuffer));
    Memory plainTextBuffer = new Memory(plainTextLength.longValue());

    // call olm
    NativeSize result =
        OlmLibrary.olm_pk_decrypt(
            instance,
            ephemeralBuffer,
            new NativeSize(ephemeralBuffer),
            macBuffer,
            new NativeSize(macBuffer),
            cipherTextBuffer,
            new NativeSize(cipherTextBuffer),
            plainTextBuffer,
            plainTextLength);

    // clear the plaintext buffer
    String plainText = Utils.fromNative(plainTextBuffer);
    plainTextBuffer.clear();

    checkOlmResult(result);

    return plainText;
  }

  /**
   * Stores an decryption object as a base64 string. Encrypts the decryption object using the
   * supplied key.
   *
   * @param key key used to encrypt the serialized decryption object data
   * @return serialized decryption object.
   * @throws RuntimeException <code>OUTPUT_BUFFER_TOO_SMALL</code> if the pickle output buffer was
   *     too small
   */
  public String pickle(String key) throws RuntimeException {
    return Utils.pickle(
        instance,
        key,
        OlmLibrary::olm_pickle_pk_decryption_length,
        OlmLibrary::olm_pickle_pk_decryption,
        r -> {
          try {
            checkOlmResult(r);
          } catch (OlmException e) {
            throw new RuntimeException(e);
          }
        });
  }

  /**
   * Check the latest olm function call for errors.
   *
   * @param result result returned by the olm function call to check
   * @throws OlmException thrown if an error occurred (incl. error details)
   */
  private void checkOlmResult(NativeSize result) throws OlmException {
    PkDecryption.checkOlmResult(instance, result);
  }

  // ================= static methods =================

  /**
   * Check the latest olm function call for errors.
   *
   * @param instance decryption object to check
   * @param result result returned by the olm function call to check
   * @throws OlmException thrown if an error occurred (incl. error details)
   */
  private static void checkOlmResult(OlmPkDecryption instance, NativeSize result)
      throws OlmException {

    if (result.equalTo(OlmLibrary.olm_error())) {
      throw new OlmException(OlmLibrary.olm_pk_decryption_last_error(instance));
    }
  }

  /**
   * Loads a decryption object from a pickled base64 string. The associated public key will be
   * written to the publicKey buffer. Decrypts the object using the supplied key.
   *
   * @param key key used to encrypt the serialized decryption object data
   * @param pickle serialized decryption object data
   * @return new initialized decryption object instance
   * @throws OlmException <code>OLM_INPUT_BUFFER_TOO_SMALL</code> if the private key was not long
   *     enough to create the new session; <code>OUTPUT_BUFFER_TOO_SMALL</code> if the public key
   *     buffer was too small to create the new session; <code>BAD_ACCOUNT_KEY</code> if the key
   *     doesn't match the one used to encrypt the session; <code>INVALID_BASE64</code> if the
   *     base64 couldn't be decoded.
   */
  public static PkDecryption unpickle(String key, String pickle) throws OlmException {
    // create new instance
    PkDecryption decryption = new PkDecryption();

    // populate instance from persisted data
    // prepare keys and pickle data
    Memory keyBuffer = Utils.toNative(key);
    Memory pickledBuffer = Utils.toNative(pickle);
    NativeSize publicKeyLength = OlmLibrary.olm_pk_key_length();
    Memory publicKeyBuffer = new Memory(publicKeyLength.longValue());

    // call olm
    NativeSize result =
        OlmLibrary.olm_unpickle_pk_decryption(
            decryption.instance,
            keyBuffer,
            new NativeSize(keyBuffer),
            pickledBuffer,
            new NativeSize(pickledBuffer),
            publicKeyBuffer,
            publicKeyLength);

    PkDecryption.checkOlmResult(decryption.instance, result);

    decryption.publicKey = Utils.fromNative(publicKeyBuffer);

    return decryption;
  }
}
