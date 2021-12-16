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
import io.github.brevilo.jolm.jna.OlmSession;
import io.github.brevilo.jolm.model.Message;

/** Class to represent an Olm session. */
public class Session {

  // backing store
  public final OlmSession instance;

  /** Private constructor. Use static create methods. */
  private Session() {
    // initialize backing store
    instance = Utils.initialize(OlmLibrary::olm_session, OlmLibrary::olm_session_size);
  }

  /** Clears the memory used to back this session. */
  public void clear() {
    OlmLibrary.olm_clear_session(instance);
  }

  /**
   * Get the identifier for this session. Will be the same for both ends of the conversation.
   *
   * @return session identifier
   * @throws OlmException <code>OUTPUT_BUFFER_TOO_SMALL</code> if the ID buffer is too small
   */
  public String sessionId() throws OlmException {
    // prepare output buffer
    NativeSize idLength = OlmLibrary.olm_session_id_length(instance);
    Memory id = new Memory(idLength.longValue());

    // call olm
    NativeSize result = OlmLibrary.olm_session_id(instance, id, idLength);
    checkOlmResult(result);

    return Utils.fromNative(id);
  }

  /**
   * Check if the session received a message of type <code>MESSAGE</code>.
   *
   * @return true if the session received a <code>MESSAGE</code> message
   */
  public boolean hasReceivedMessage() {
    return OlmLibrary.olm_session_has_received_message(instance) != 0;
  }

  /**
   * Returns a description of the internal state of this session (debugging and logging purposes).
   * If the buffer is not large enough to hold the entire string, it will be truncated and will end
   * with "...". A buffer length of 600 will be enough to hold any output. The required minimum
   * buffer length is 23.
   *
   * @param size maximum size allowed for the returned description
   * @return session description
   */
  public String describe(long size) {
    // prepare output buffer
    NativeSize bufferSize = new NativeSize(size);
    Memory buffer = new Memory(size);

    // call olm
    OlmLibrary.olm_session_describe(instance, buffer, bufferSize);

    // return result
    return Utils.fromNative(buffer);
  }

  /**
   * Checks if the <code>PRE_KEY</code> message is for this in-bound session. This can happen if
   * multiple messages are sent to this account before this account sends a message in reply.
   *
   * @param oneTimeKeyMessage <code>PRE_KEY</code> message
   * @return true if the <code>PRE_KEY</code> message matches
   * @throws OlmException <code>INVALID_BASE64</code> if the base64 couldn't be decoded; <code>
   *     BAD_MESSAGE_VERSION</code> if the message was for an unsupported protocol version; <code>
   *     BAD_MESSAGE_FORMAT</code> if the message couldn't be decoded
   */
  public boolean matchesInboundSession(String oneTimeKeyMessage) throws OlmException {
    // get native message
    Memory messageBuffer = Utils.toNative(oneTimeKeyMessage);

    // call olm
    NativeSize result =
        OlmLibrary.olm_matches_inbound_session(
            instance, messageBuffer, new NativeSize(messageBuffer));

    checkOlmResult(result);

    return result.longValue() == 1;
  }

  /**
   * Checks if the <code>PRE_KEY</code> message is for this in-bound session based on the sender
   * identity key.<br>
   * This API may be used to process a "m.room.encrypted" event when type = 1 (<code>PRE_KEY</code>
   * ).
   *
   * @param theirIdentityKey identity key of the sender
   * @param oneTimeKeyMessage <code>PRE_KEY</code> message
   * @return true if the <code>PRE_KEY</code> message matches
   * @throws OlmException <code>INVALID_BASE64</code> if the base64 couldn't be decoded; <code>
   *     BAD_MESSAGE_VERSION</code> if the message was for an unsupported protocol version; <code>
   *     BAD_MESSAGE_FORMAT</code> if the message couldn't be decoded
   */
  public boolean matchesInboundSessionFrom(String theirIdentityKey, String oneTimeKeyMessage)
      throws OlmException {

    // get native values
    Memory keyBuffer = Utils.toNative(theirIdentityKey);
    Memory messageBuffer = Utils.toNative(oneTimeKeyMessage);

    // call olm
    NativeSize result =
        OlmLibrary.olm_matches_inbound_session_from(
            instance,
            keyBuffer,
            new NativeSize(keyBuffer),
            messageBuffer,
            new NativeSize(messageBuffer));

    checkOlmResult(result);

    return result.longValue() == 1;
  }

  /**
   * Encrypt a message using the session.
   *
   * @param plainText plain text message
   * @return encrypted message
   * @throws OlmException <code>NOT_ENOUGH_RANDOM</code> if there weren't enough random bytes;
   *     <code>OUTPUT_BUFFER_TOO_SMALL</code> if the message buffer is too small
   */
  public Message encrypt(String plainText) throws OlmException {
    // determine message type
    NativeSize messageType = OlmLibrary.olm_encrypt_message_type(instance);

    // get native plain text
    Memory plainTextBuffer = Utils.toNative(plainText);

    // generate randomness (if needed)
    NativeSize randomLength = OlmLibrary.olm_encrypt_random_length(instance);
    Memory randomBuffer = randomLength.longValue() > 0 ? Utils.randomBuffer(randomLength) : null;

    // prepare output buffer
    NativeSize messageLength =
        OlmLibrary.olm_encrypt_message_length(instance, new NativeSize(plainTextBuffer));
    Memory messageBuffer = new Memory(messageLength.longValue());

    // call olm
    NativeSize result =
        OlmLibrary.olm_encrypt(
            instance,
            plainTextBuffer,
            new NativeSize(plainTextBuffer),
            randomBuffer,
            randomLength,
            messageBuffer,
            messageLength);

    // clear the plaintext and random buffers
    plainTextBuffer.clear();
    if (randomBuffer != null) {
      randomBuffer.clear();
    }

    checkOlmResult(result);

    return Message.get(Utils.fromNative(messageBuffer), messageType.longValue());
  }

  /**
   * Decrypt a message using the session.
   *
   * @param message to decrypt
   * @return decrypted message
   * @throws OlmException <code>INVALID_BASE64</code> if the base64 couldn't be decoded; <code>
   *     BAD_MESSAGE_VERSION</code> if the message is for an unsupported version of the protocol;
   *     <code>BAD_MESSAGE_FORMAT</code> if the message couldn't be decoded; <code>BAD_MESSAGE_MAC
   *     </code> if the MAC on the message was invalid; <code>OUTPUT_BUFFER_TOO_SMALL</code> if the
   *     plain-text buffer is too small
   */
  public String decrypt(Message message) throws OlmException {
    // get native message
    // (add a separate message buffer as olm_decrypt_max_plaintext_length() destroys it!)
    Memory messageBuffer = Utils.toNative(message.getCipherText());
    Memory messageBufferCopy = Utils.toNative(message.getCipherText());

    // determine output buffer length
    NativeSize maxPlainTextLength =
        OlmLibrary.olm_decrypt_max_plaintext_length(
            instance,
            new NativeSize(message.type()),
            messageBufferCopy,
            new NativeSize(messageBufferCopy));

    checkOlmResult(maxPlainTextLength);

    // prepare output buffer
    Memory plainTextBuffer = new Memory(maxPlainTextLength.longValue());

    // call olm
    NativeSize plainTextLength =
        OlmLibrary.olm_decrypt(
            instance,
            new NativeSize(message.type()),
            messageBuffer,
            new NativeSize(messageBuffer),
            plainTextBuffer,
            maxPlainTextLength);

    // clear the plaintext buffer
    String plainText = Utils.fromNative(plainTextBuffer);
    plainTextBuffer.clear();

    checkOlmResult(plainTextLength);

    return plainText;
  }

  /**
   * Stores a session as a base64 string. Encrypts the session using the supplied key.
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
        OlmLibrary::olm_pickle_session_length,
        OlmLibrary::olm_pickle_session,
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
    Session.checkOlmResult(instance, result);
  }

  // ================= static methods =================

  /**
   * Check the latest olm function call for errors.
   *
   * @param instance session to check
   * @param result result returned by the olm function call to check
   * @throws OlmException thrown if an error occurred (incl. error details)
   */
  private static void checkOlmResult(OlmSession instance, NativeSize result) throws OlmException {
    if (result.equalTo(OlmLibrary.olm_error())) {
      throw new OlmException(OlmLibrary.olm_session_last_error(instance));
    }
  }

  /**
   * Creates a new out-bound session for sending messages to a given identity key and one time key.
   *
   * @param account account associated with new session
   * @param theirIdentityKey the identity key of the recipient
   * @param theirOneTimeKey the one time key of the recipient
   * @return the new olm session
   * @throws OlmException <code>INVALID_BASE64</code> if the keys couldn't be decoded as base64;
   *     <code>NOT_ENOUGH_RANDOM</code> if there weren't enough random bytes
   */
  public static Session createOutboundSession(
      Account account, String theirIdentityKey, String theirOneTimeKey) throws OlmException {

    // create new instance
    Session session = new Session();

    // get native message
    Memory identityKeyBuffer = Utils.toNative(theirIdentityKey);
    Memory oneTimeKeyBuffer = Utils.toNative(theirOneTimeKey);

    // generate randomness
    NativeSize randomLength =
        OlmLibrary.olm_create_outbound_session_random_length(session.instance);
    Memory randomBuffer = Utils.randomBuffer(randomLength);

    // call olm
    NativeSize result =
        OlmLibrary.olm_create_outbound_session(
            session.instance,
            account.instance,
            identityKeyBuffer,
            new NativeSize(identityKeyBuffer),
            oneTimeKeyBuffer,
            new NativeSize(oneTimeKeyBuffer),
            randomBuffer,
            randomLength);

    // clear the random buffer
    randomBuffer.clear();

    checkOlmResult(session.instance, result);

    return session;
  }

  /**
   * Create a new in-bound session for sending/receiving messages from an incoming <code>PRE_KEY
   * </code> message.
   *
   * @param account account associated with new session
   * @param oneTimeKeyMessage <code>PRE_KEY</code> message
   * @return the new olm session
   * @throws OlmException <code>INVALID_BASE64</code> if the base64 couldn't be decoded; <code>
   *     BAD_MESSAGE_VERSION</code> if the message was for an unsupported protocol version; <code>
   *     BAD_MESSAGE_FORMAT</code> if the message couldn't be decoded; <code>BAD_MESSAGE_KEY_ID
   *     </code> if the message refers to an unknown one time key
   */
  public static Session createInboundSession(Account account, String oneTimeKeyMessage)
      throws OlmException {

    // create new instance
    Session session = new Session();

    // get native message
    Memory oneTimeKeyMessageBuffer = Utils.toNative(oneTimeKeyMessage);

    // call olm
    NativeSize result =
        OlmLibrary.olm_create_inbound_session(
            session.instance,
            account.instance,
            oneTimeKeyMessageBuffer,
            new NativeSize(oneTimeKeyMessageBuffer));

    // clear the message buffer
    oneTimeKeyMessageBuffer.clear();

    checkOlmResult(session.instance, result);

    return session;
  }

  /**
   * Same as {@link #createInboundSession(Account, String)}, but ensures that the identity key in
   * the <code>PRE_KEY</code> message matches the expected identity key.
   *
   * @param account account associated with new session
   * @param theirIdentityKey expected identity key of the sender
   * @param oneTimeKeyMessage <code>PRE_KEY</code> message
   * @return the new olm session
   * @throws OlmException <code>INVALID_BASE64</code> if the base64 couldn't be decoded; <code>
   *     BAD_MESSAGE_VERSION</code> if the message was for an unsupported protocol version; <code>
   *     BAD_MESSAGE_FORMAT</code> if the message couldn't be decoded; <code>BAD_MESSAGE_KEY_ID
   *     </code> if the message refers to an unknown one time key
   */
  public static Session createInboundSessionFrom(
      Account account, String theirIdentityKey, String oneTimeKeyMessage) throws OlmException {

    // create new instance
    Session session = new Session();

    // get native message
    Memory identityKeyBuffer = Utils.toNative(theirIdentityKey);
    Memory oneTimeKeyMessageBuffer = Utils.toNative(oneTimeKeyMessage);

    // call olm
    NativeSize result =
        OlmLibrary.olm_create_inbound_session_from(
            session.instance,
            account.instance,
            identityKeyBuffer,
            new NativeSize(identityKeyBuffer),
            oneTimeKeyMessageBuffer,
            new NativeSize(oneTimeKeyMessageBuffer));

    // clear the message buffer
    oneTimeKeyMessageBuffer.clear();

    checkOlmResult(session.instance, result);

    return session;
  }

  /**
   * Loads a session from a pickled base64 string. Decrypts the session using the supplied key.
   *
   * @param key key used to encrypt the serialized session data
   * @param pickle serialized session data
   * @return new initialized session instance
   * @throws OlmException <code>BAD_ACCOUNT_KEY</code> if the key doesn't match the one used to
   *     encrypt the session; <code>INVALID_BASE64</code> if the base64 couldn't be decoded
   */
  public static Session unpickle(String key, String pickle) throws Exception {
    // create new instance
    Session session = new Session();

    // populate instance from persisted data
    Utils.unpickle(
        session.instance,
        key,
        pickle,
        OlmLibrary::olm_unpickle_session,
        (s, r) -> {
          try {
            checkOlmResult(s, r);
          } catch (OlmException e) {
            throw new RuntimeException(e);
          }
        });

    return session;
  }
}
