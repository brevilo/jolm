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
import com.sun.jna.ptr.IntByReference;
import io.github.brevilo.jolm.Utils.OlmException;
import io.github.brevilo.jolm.jna.NativeSize;
import io.github.brevilo.jolm.jna.OlmInboundGroupSession;
import io.github.brevilo.jolm.jna.OlmLibrary;
import io.github.brevilo.jolm.model.GroupMessage;

/** Class to represent an inbound Megolm session. */
public class InboundGroupSession {

  // backing store
  private final OlmInboundGroupSession instance;

  /**
   * Creates a new inbound Megolm session. The session key must be imported by calling {@link
   * #importer(String)} before using the session (e.g. when processing a <code>m.forwarded_room_key
   * </code> event).
   */
  public InboundGroupSession() {
    // initialize backing store
    instance =
        Utils.initialize(
            OlmLibrary::olm_inbound_group_session, OlmLibrary::olm_inbound_group_session_size);
  }

  /**
   * Creates a new inbound Megolm session using the provided session key. The key can be exported by
   * {@link OutboundGroupSession#sessionKey()}.
   *
   * @param sessionKey session key to generate the session for
   * @throws OlmException <code>OLM_INVALID_BASE64</code> if the sessionKey is not valid base64;
   *     <code>OLM_BAD_SESSION_KEY</code> if the sessionKey is invalid
   */
  public InboundGroupSession(String sessionKey) throws OlmException {
    // call private constructor
    this();

    // get native key
    Memory sessionKeyBuffer = Utils.toNative(sessionKey);

    // call olm
    NativeSize result =
        OlmLibrary.olm_init_inbound_group_session(
            instance, sessionKeyBuffer, new NativeSize(sessionKeyBuffer));

    checkOlmResult(result);
  }

  /** Clears the memory used to back this group session. */
  public void clear() {
    OlmLibrary.olm_clear_inbound_group_session(instance);
  }

  /**
   * Get a base64-encoded identifier for this session.
   *
   * @return session identifier
   * @throws OlmException <code>OUTPUT_BUFFER_TOO_SMALL</code> if the ID buffer was too small
   */
  public String sessionId() throws OlmException {
    // prepare output buffer
    NativeSize idLength = OlmLibrary.olm_inbound_group_session_id_length(instance);
    Memory id = new Memory(idLength.longValue());

    // call olm
    NativeSize result = OlmLibrary.olm_inbound_group_session_id(instance, id, idLength);
    checkOlmResult(result);

    return Utils.fromNative(id);
  }

  /**
   * Get the first message index we know how to decrypt.
   *
   * @return index of the first message ready for decryption
   */
  public long firstKnownIndex() {
    return OlmLibrary.olm_inbound_group_session_first_known_index(instance);
  }

  /**
   * Check if the session has been verified as a valid session. A session is verified either because
   * the original session share was signed, or because we have subsequently successfully decrypted a
   * message. (This is mainly intended for the unit tests, currently)
   *
   * @return true if session is verified
   */
  public boolean isVerified() {
    return OlmLibrary.olm_inbound_group_session_is_verified(instance) != 0;
  }

  /**
   * Export the base64-encoded ratchet key for this session, at the given index, in a format which
   * can be used by {@link #importer(String)}.
   *
   * @param messageIndex index at which to export the session
   * @return session key at the given index
   * @throws OlmException <code>OUTPUT_BUFFER_TOO_SMALL</code> if the buffer was too small; <code>
   *     OLM_UNKNOWN_MESSAGE_INDEX</code> if we do not have a session key corresponding to the given
   *     message index (ie, it was sent before the session key was shared with us)
   */
  public String export(long messageIndex) throws OlmException {
    // prepare output buffer
    NativeSize sessionKeyLength = OlmLibrary.olm_export_inbound_group_session_length(instance);
    Memory sessionKeyBuffer = new Memory(sessionKeyLength.longValue());

    // call olm
    NativeSize result =
        OlmLibrary.olm_export_inbound_group_session(
            instance, sessionKeyBuffer, sessionKeyLength, (int) messageIndex);

    checkOlmResult(result);

    return Utils.fromNative(sessionKeyBuffer);
  }

  /**
   * Import an inbound group session, from a previous export via {@link #export(long)}.
   *
   * @param sessionKey session key to start the new session from
   * @throws OlmException <code>OLM_INVALID_BASE64</code> if the session key is not valid base64;
   *     <code>OLM_BAD_SESSION_KEY</code> if the session key is invalid
   */
  public void importer(String sessionKey) throws OlmException {
    // get native key
    Memory sessionKeyBuffer = Utils.toNative(sessionKey);

    // call olm
    NativeSize result =
        OlmLibrary.olm_import_inbound_group_session(
            instance, sessionKeyBuffer, new NativeSize(sessionKeyBuffer));

    checkOlmResult(result);
  }

  /**
   * Decrypt a message.
   *
   * @param message message to be decrypted
   * @return decrypted message
   * @throws OlmException <code>OLM_OUTPUT_BUFFER_TOO_SMALL</code> if the plain-text buffer is too
   *     small; <code>OLM_INVALID_BASE64</code> if the message is not valid base64; <code>
   *     OLM_BAD_MESSAGE_VERSION</code> if the message was encrypted with an unsupported version of
   *     the protocol; <code>OLM_BAD_MESSAGE_FORMAT</code> if the message headers could not be
   *     decoded; <code>OLM_BAD_MESSAGE_MAC</code> if the message could not be verified; <code>
   *     OLM_UNKNOWN_MESSAGE_INDEX</code> if we do not have a session key corresponding to the
   *     message's index (ie, it was sent before the session key was shared with us)
   */
  public GroupMessage decrypt(String message) throws OlmException {
    // get native message
    // (add a separate message buffer as olm_group_decrypt_max_plaintext_length() destroys it!)
    Memory messageBuffer = Utils.toNative(message);
    Memory messageBufferCopy = Utils.toNative(message);

    // prepare output buffer and index reference
    NativeSize maxPlainTextLength =
        OlmLibrary.olm_group_decrypt_max_plaintext_length(
            instance, messageBufferCopy, new NativeSize(messageBufferCopy));

    checkOlmResult(maxPlainTextLength);

    Memory plainTextBuffer = new Memory(maxPlainTextLength.longValue());
    IntByReference messageIndex = new IntByReference();

    // call olm
    NativeSize plainTextLength =
        OlmLibrary.olm_group_decrypt(
            instance,
            messageBuffer,
            new NativeSize(messageBuffer),
            plainTextBuffer,
            maxPlainTextLength,
            messageIndex);

    checkOlmResult(plainTextLength);

    return new GroupMessage(Utils.fromNative(plainTextBuffer), messageIndex.getValue());
  }

  /**
   * Stores an inbound group session as a base64 string. Encrypts the session using the supplied
   * key.
   *
   * @param key key used to encrypt the serialized session data
   * @return serialized session
   * @throws RuntimeException <code>OUTPUT_BUFFER_TOO_SMALL</code> if the pickle output buffer was
   *     too small
   */
  public String pickle(String key) {
    return Utils.pickle(
        instance,
        key,
        OlmLibrary::olm_pickle_inbound_group_session_length,
        OlmLibrary::olm_pickle_inbound_group_session,
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
    InboundGroupSession.checkOlmResult(instance, result);
  }

  // ================= static methods =================

  /**
   * Check the latest olm function call for errors.
   *
   * @param session inbound group session to check
   * @param result result returned by the olm function call to check
   * @throws OlmException thrown if an error occurred (incl. error details)
   */
  private static void checkOlmResult(OlmInboundGroupSession session, NativeSize result)
      throws OlmException {

    if (result.equalTo(OlmLibrary.olm_error())) {
      throw new OlmException(OlmLibrary.olm_inbound_group_session_last_error(session));
    }
  }

  /**
   * Loads an inbound group session from a pickled base64 string. Decrypts the session using the
   * supplied key.
   *
   * @param key key used to encrypt the serialized session data
   * @param pickle serialized session data
   * @return new initialized session instance
   * @throws RuntimeException <code>BAD_ACCOUNT_KEY</code> if the key doesn't match the one used to
   *     encrypt the session; <code>INVALID_BASE64</code> if the base64 couldn't be decoded.
   */
  public static InboundGroupSession unpickle(String key, String pickle) throws RuntimeException {
    // create new instance
    InboundGroupSession session = new InboundGroupSession();

    // populate instance from persisted data
    Utils.unpickle(
        session.instance,
        key,
        pickle,
        OlmLibrary::olm_unpickle_inbound_group_session,
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
