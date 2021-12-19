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
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.sun.jna.Memory;
import io.github.brevilo.jolm.Utils.OlmException;
import io.github.brevilo.jolm.jna.NativeSize;
import io.github.brevilo.jolm.jna.OlmAccount;
import io.github.brevilo.jolm.jna.OlmLibrary;
import io.github.brevilo.jolm.model.IdentityKeys;
import io.github.brevilo.jolm.model.OneTimeKeys;

/** Class to represent an Olm account. */
public class Account {

  // backing store
  public final OlmAccount instance;

  private final JsonMapper jsonMapper;

  // initializer
  {
    jsonMapper = JsonMapper.builder().build();
  }

  /**
   * Creates a new account initialized with random data.
   *
   * @throws OlmException <code>NOT_ENOUGH_RANDOM</code> if there weren't enough random bytes
   */
  public Account() throws OlmException {
    // initialize account backing store
    instance = Utils.initialize(OlmLibrary::olm_account, OlmLibrary::olm_account_size);

    // generate randomness and create account
    NativeSize randomLength = OlmLibrary.olm_create_account_random_length(instance);
    Memory randomBuffer = Utils.randomBuffer(randomLength);

    // call olm
    NativeSize result = OlmLibrary.olm_create_account(instance, randomBuffer, randomLength);

    // clear the random buffer
    randomBuffer.clear();

    checkOlmResult(result);
  }

  /** Clears the memory used to back this account. */
  public void clear() {
    OlmLibrary.olm_clear_account(instance);
  }

  /**
   * Gets the public parts of the identity keys for the account.
   *
   * @return identity keys
   * @throws OlmException <code>OUTPUT_BUFFER_TOO_SMALL</code> if the keys buffer was too small
   * @throws JsonProcessingException deserialization error
   */
  public IdentityKeys identityKeys() throws OlmException, JsonProcessingException {
    // prepare output buffer
    NativeSize identityKeysLength = OlmLibrary.olm_account_identity_keys_length(instance);
    Memory identityKeys = new Memory(identityKeysLength.longValue());

    // call olm
    NativeSize result =
        OlmLibrary.olm_account_identity_keys(instance, identityKeys, identityKeysLength);

    checkOlmResult(result);

    return jsonMapper.readValue(Utils.fromNative(identityKeys), IdentityKeys.class);
  }

  /**
   * The largest number of one time keys this account can store.
   *
   * @return maximum number of one time keys
   */
  public long maxNumberOfOneTimeKeys() {
    return OlmLibrary.olm_account_max_number_of_one_time_keys(instance).longValue();
  }

  /**
   * Generates a number of new one time keys. If the total number of keys stored by this account
   * exceeds {@link #maxNumberOfOneTimeKeys()} then the old keys are discarded.
   *
   * @param numberOfKeys number of new one time keys to be generated
   * @throws OlmException <code>NOT_ENOUGH_RANDOM</code> if the number of random bytes is too small
   */
  public void generateOneTimeKeys(long numberOfKeys) throws OlmException {
    // generate randomness
    NativeSize randomLength =
        OlmLibrary.olm_account_generate_one_time_keys_random_length(
            instance, new NativeSize(numberOfKeys));
    Memory randomBuffer = Utils.randomBuffer(randomLength);

    // call olm
    NativeSize result =
        OlmLibrary.olm_account_generate_one_time_keys(
            instance, new NativeSize(numberOfKeys), randomBuffer, randomLength);

    // clear the random buffer
    randomBuffer.clear();

    checkOlmResult(result);
  }

  /**
   * Gets the public parts of the unpublished one time keys for the account.
   *
   * @return one time keys
   * @throws OlmException <code>OUTPUT_BUFFER_TOO_SMALL</code> if the keys buffer was too small
   * @throws JsonProcessingException deserialization error
   */
  public OneTimeKeys oneTimeKeys() throws OlmException, JsonProcessingException {
    // prepare output buffer
    NativeSize keysLength = OlmLibrary.olm_account_one_time_keys_length(instance);
    Memory oneTimeKeys = new Memory(keysLength.longValue());

    // call olm
    NativeSize result = OlmLibrary.olm_account_one_time_keys(instance, oneTimeKeys, keysLength);
    checkOlmResult(result);

    return jsonMapper.readValue(Utils.fromNative(oneTimeKeys), OneTimeKeys.class);
  }

  /**
   * Removes the account's one time keys that the given session used.
   *
   * @param session session of which the one time keys should be removed
   * @throws OlmException <code>BAD_MESSAGE_KEY_ID</code> if the account doesn't have any matching
   *     one time keys
   */
  public void removeOneTimeKeys(Session session) throws OlmException {
    // call olm
    NativeSize result = OlmLibrary.olm_remove_one_time_keys(instance, session.instance);
    checkOlmResult(result);
  }

  /**
   * Marks the current set of one time keys and fallback key as being published.
   *
   * <p>Once marked as published, the one time keys will no longer be returned by {@link
   * #oneTimeKeys()}, and the fallback key will no longer be returned by {@link
   * #unpublishedFallbackKey()}.
   *
   * @throws OlmException unspecified
   */
  public void markKeysAsPublished() throws OlmException {
    // call olm
    NativeSize result = OlmLibrary.olm_account_mark_keys_as_published(instance);
    checkOlmResult(result);
  }

  /**
   * Generates a new fallback key. Only one previous fallback key is stored.
   *
   * @throws OlmException <code>NOT_ENOUGH_RANDOM</code> if the number of random bytes is too small
   */
  public void generateFallbackKey() throws OlmException {
    // generate randomness
    NativeSize randomLength = OlmLibrary.olm_account_generate_fallback_key_random_length(instance);
    Memory randomBuffer = new Memory(randomLength.longValue());

    // call olm
    NativeSize result =
        OlmLibrary.olm_account_generate_fallback_key(instance, randomBuffer, randomLength);

    // clear the random buffer
    randomBuffer.clear();

    checkOlmResult(result);
  }

  /**
   * Gets the fallback key for the account (if present and unpublished).
   *
   * @return fallback key
   * @throws OlmException <code>OUTPUT_BUFFER_TOO_SMALL</code> if the key buffer was too small
   * @throws JsonProcessingException deserialization error
   */
  public OneTimeKeys unpublishedFallbackKey() throws OlmException, JsonProcessingException {
    // prepare output buffer
    NativeSize keyLength = OlmLibrary.olm_account_unpublished_fallback_key_length(instance);
    Memory fallbackKey = new Memory(keyLength.longValue());

    // call olm
    NativeSize result =
        OlmLibrary.olm_account_unpublished_fallback_key(instance, fallbackKey, keyLength);
    checkOlmResult(result);

    return jsonMapper.readValue(Utils.fromNative(fallbackKey), OneTimeKeys.class);
  }

  /**
   * Forget about the old fallback key.
   *
   * <p>This should be called once you are reasonably certain that you will not receive any more
   * messages that use the old fallback key (e.g. 5 minutes after the new fallback key has been
   * published).
   */
  public void forgetFallbackKey() {
    // call olm
    OlmLibrary.olm_account_forget_old_fallback_key(instance);
  }

  /**
   * Signs a message with the ed25519 key for this account.
   *
   * @param message message to sign
   * @return message signature
   * @throws OlmException <code>OUTPUT_BUFFER_TOO_SMALL</code> if the signature buffer was too small
   */
  public String sign(String message) throws OlmException {
    // get native message
    Memory messageBuffer = Utils.toNative(message);

    // prepare output buffer
    NativeSize signatureLength = OlmLibrary.olm_account_signature_length(instance);
    Memory signatureBuffer = new Memory(signatureLength.longValue());

    // call olm
    NativeSize result =
        OlmLibrary.olm_account_sign(
            instance,
            messageBuffer,
            new NativeSize(messageBuffer),
            signatureBuffer,
            signatureLength);

    // clear the message buffer
    messageBuffer.clear();

    checkOlmResult(result);

    return Utils.fromNative(signatureBuffer);
  }

  /**
   * Stores an account as a base64 string. Encrypts the account using the supplied key.
   *
   * @param key key used to encrypt the serialized account data
   * @return serialized account
   * @throws RuntimeException <code>OUTPUT_BUFFER_TOO_SMALL</code> if the pickle output buffer was
   *     too small
   */
  public String pickle(String key) throws RuntimeException {
    return Utils.pickle(
        instance,
        key,
        OlmLibrary::olm_pickle_account_length,
        OlmLibrary::olm_pickle_account,
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
    Account.checkOlmResult(instance, result);
  }

  // ================= static methods =================

  /**
   * Check the latest olm function call for errors.
   *
   * @param instance account to check
   * @param result result returned by the olm function call to check
   * @throws OlmException thrown if an error occurred (incl. error details)
   */
  private static void checkOlmResult(OlmAccount instance, NativeSize result) throws OlmException {
    if (result.equalTo(OlmLibrary.olm_error())) {
      throw new OlmException(OlmLibrary.olm_account_last_error(instance));
    }
  }

  /**
   * Loads an account from a pickled base64 string. Decrypts the account using the supplied key.
   *
   * @param key key used to encrypt the serialized account data
   * @param pickle serialized account data
   * @return new initialized account instance
   * @throws OlmException <code>NOT_ENOUGH_RANDOM</code> if there weren't enough random bytes to
   *     create the new Account
   * @throws RuntimeException <code>BAD_ACCOUNT_KEY</code> if the key doesn't match the one used to
   *     encrypt the account; <code>INVALID_BASE64</code> if the base64 couldn't be decoded.
   */
  public static Account unpickle(String key, String pickle) throws OlmException, RuntimeException {
    // create new instance
    Account account = new Account();

    // populate instance from persisted data
    Utils.unpickle(
        account.instance,
        key,
        pickle,
        OlmLibrary::olm_unpickle_account,
        (a, r) -> {
          try {
            checkOlmResult(a, r);
          } catch (OlmException e) {
            throw new RuntimeException(e);
          }
        });

    return account;
  }
}
