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
import io.github.brevilo.jolm.jna.OlmOutboundGroupSession;

/** Class to represent an outbound Megolm session. */
public class OutboundGroupSession {

  // backing store
  private final OlmOutboundGroupSession instance;

  /**
   * Creates a new outbound Megolm session initialized with random data.
   *
   * @throws OlmException <code>NOT_ENOUGH_RANDOM</code> if there weren't enough random bytes
   */
  public OutboundGroupSession() throws OlmException {
    // initialize backing store
    instance =
        Utils.initialize(
            OlmLibrary::olm_outbound_group_session, OlmLibrary::olm_outbound_group_session_size);

    // generate randomness and create session
    NativeSize randomLength = OlmLibrary.olm_init_outbound_group_session_random_length(instance);
    Memory randomBuffer = Utils.randomBuffer(randomLength);

    // call olm
    NativeSize result =
        OlmLibrary.olm_init_outbound_group_session(instance, randomBuffer, randomLength);

    // clear the random buffer
    randomBuffer.clear();

    checkOlmResult(result);
  }

  /** Clears the memory used to back this group session. */
  public void clear() {
    OlmLibrary.olm_clear_outbound_group_session(instance);
  }

  /**
   * Get a base64-encoded identifier for this session.
   *
   * @return session identifier
   * @throws OlmException <code>OUTPUT_BUFFER_TOO_SMALL</code> if the ID buffer was too small
   */
  public String sessionId() throws OlmException {
    // prepare output buffer
    NativeSize idLength = OlmLibrary.olm_outbound_group_session_id_length(instance);
    Memory id = new Memory(idLength.longValue());

    // call olm
    NativeSize result = OlmLibrary.olm_outbound_group_session_id(instance, id, idLength);
    checkOlmResult(result);

    return Utils.fromNative(id);
  }

  /**
   * Get the current message index for this session.
   *
   * @return current message index
   */
  public int messageIndex() {
    return OlmLibrary.olm_outbound_group_session_message_index(instance);
  }

  /**
   * Get the base64-encoded current ratchet key for this session.
   *
   * <p>Each message is sent with a different ratchet key. This function returns the ratchet key
   * that will be used for the next message.
   *
   * @return ratchet key for the message in this session
   * @throws OlmException <code>OUTPUT_BUFFER_TOO_SMALL</code> if the key buffer was too small
   */
  public String sessionKey() throws OlmException {
    NativeSize keyLength = OlmLibrary.olm_outbound_group_session_key_length(instance);
    Memory key = new Memory(keyLength.longValue());

    // call olm
    NativeSize result = OlmLibrary.olm_outbound_group_session_key(instance, key, keyLength);

    // clear the key buffer
    String sessionKey = Utils.fromNative(key);
    key.clear();

    checkOlmResult(result);

    return sessionKey;
  }

  /**
   * Encrypt some plain-text.
   *
   * @param plainText text to be encrypted
   * @return encrypted text
   * @throws OlmException <code>OUTPUT_BUFFER_TOO_SMALL</code> if the output buffer was too small
   */
  public String encrypt(String plainText) throws OlmException {
    // get native plain text
    Memory plainTextBuffer = Utils.toNative(plainText);

    // prepare output buffer
    NativeSize messageLength =
        OlmLibrary.olm_group_encrypt_message_length(instance, new NativeSize(plainTextBuffer));
    Memory messageBuffer = new Memory(messageLength.longValue());

    // call olm
    NativeSize result =
        OlmLibrary.olm_group_encrypt(
            instance,
            plainTextBuffer,
            new NativeSize(plainTextBuffer),
            messageBuffer,
            messageLength);

    // clear the plaintext buffer
    plainTextBuffer.clear();

    checkOlmResult(result);

    return Utils.fromNative(messageBuffer);
  }

  /**
   * Stores an outbound group session as a base64 string. Encrypts the session using the supplied
   * key.
   *
   * @param key key used to encrypt the serialized session data
   * @return serialized session
   * @throws RuntimeException <code>OUTPUT_BUFFER_TOO_SMALL</code> if the pickle output buffer was
   *     too small
   */
  public String pickle(String key) throws RuntimeException {
    return Utils.pickle(
        instance,
        key,
        OlmLibrary::olm_pickle_outbound_group_session_length,
        OlmLibrary::olm_pickle_outbound_group_session,
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
    OutboundGroupSession.checkOlmResult(instance, result);
  }

  // ================= static methods =================

  /**
   * Check the latest olm function call for errors.
   *
   * @param session outbound group session to check
   * @param result result returned by the olm function call to check
   * @throws OlmException thrown if an error occurred (incl. error details)
   */
  private static void checkOlmResult(OlmOutboundGroupSession session, NativeSize result)
      throws OlmException {

    if (result.equalTo(OlmLibrary.olm_error())) {
      throw new OlmException(OlmLibrary.olm_outbound_group_session_last_error(session));
    }
  }

  /**
   * Loads an outbound group session from a pickled base64 string. Decrypts the session using the
   * supplied key.
   *
   * @param key key used to encrypt the serialized session data
   * @param pickle serialized session data
   * @return new initialized session instance
   * @throws OlmException <code>NOT_ENOUGH_RANDOM</code> if there weren't enough random bytes to
   *     create the new session
   * @throws RuntimeException <code>BAD_ACCOUNT_KEY</code> if the key doesn't match the one used to
   *     encrypt the session; <code>INVALID_BASE64</code> if the base64 couldn't be decoded.
   */
  public static OutboundGroupSession unpickle(String key, String pickle)
      throws OlmException, RuntimeException {

    // create new instance
    OutboundGroupSession session = new OutboundGroupSession();

    // populate instance from persisted data
    Utils.unpickle(
        session.instance,
        key,
        pickle,
        OlmLibrary::olm_unpickle_outbound_group_session,
        (a, r) -> {
          try {
            checkOlmResult(a, r);
          } catch (OlmException e) {
            throw new RuntimeException(e);
          }
        });

    return session;
  }
}
