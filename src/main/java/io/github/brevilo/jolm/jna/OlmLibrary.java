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

package io.github.brevilo.jolm.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/** Main JNA mapping class for libolm. */
public class OlmLibrary implements Library {
  public static final String JNA_LIBRARY_NAME = "olm";
  public static final NativeLibrary JNA_NATIVE_LIB =
      NativeLibrary.getInstance(OlmLibrary.JNA_LIBRARY_NAME);

  static {
    Native.register(OlmLibrary.class, OlmLibrary.JNA_NATIVE_LIB);
  }

  // olm/error.h
  public static interface OlmErrorCode {
    public static final int OLM_SUCCESS = 0;
    public static final int OLM_NOT_ENOUGH_RANDOM = 1;
    public static final int OLM_OUTPUT_BUFFER_TOO_SMALL = 2;
    public static final int OLM_BAD_MESSAGE_VERSION = 3;
    public static final int OLM_BAD_MESSAGE_FORMAT = 4;
    public static final int OLM_BAD_MESSAGE_MAC = 5;
    public static final int OLM_BAD_MESSAGE_KEY_ID = 6;
    public static final int OLM_INVALID_BASE64 = 7;
    public static final int OLM_BAD_ACCOUNT_KEY = 8;
    public static final int OLM_UNKNOWN_PICKLE_VERSION = 9;
    public static final int OLM_CORRUPTED_PICKLE = 10;
    public static final int OLM_BAD_SESSION_KEY = 11;
    public static final int OLM_UNKNOWN_MESSAGE_INDEX = 12;
    public static final int OLM_BAD_LEGACY_ACCOUNT_PICKLE = 13;
    public static final int OLM_BAD_SIGNATURE = 14;
    public static final int OLM_INPUT_BUFFER_TOO_SMALL = 15;
    public static final int OLM_SAS_THEIR_KEY_NOT_SET = 16;
    public static final int OLM_PICKLE_EXTRA_DATA = 17;
  }
  ;

  public static native String _olm_error_to_string(int error);

  // olm/olm.h
  public static final long OLM_MESSAGE_TYPE_PRE_KEY = 0;
  public static final long OLM_MESSAGE_TYPE_MESSAGE = 1;

  // olm/inbound_group_session.h
  public static native NativeSize olm_inbound_group_session_size();

  public static native OlmInboundGroupSession olm_inbound_group_session(Pointer memory);

  public static native String olm_inbound_group_session_last_error(PointerByReference session);

  public static native int olm_inbound_group_session_last_error_code(PointerByReference session);

  public static native NativeSize olm_clear_inbound_group_session(PointerByReference session);

  public static native NativeSize olm_pickle_inbound_group_session_length(
      PointerByReference session);

  public static native NativeSize olm_pickle_inbound_group_session(
      PointerByReference session,
      Pointer key,
      NativeSize key_length,
      Pointer pickled,
      NativeSize pickled_length);

  public static native NativeSize olm_unpickle_inbound_group_session(
      PointerByReference session,
      Pointer key,
      NativeSize key_length,
      Pointer pickled,
      NativeSize pickled_length);

  public static native NativeSize olm_init_inbound_group_session(
      PointerByReference session, byte session_key[], NativeSize session_key_length);

  public static native NativeSize olm_init_inbound_group_session(
      PointerByReference session, Pointer session_key, NativeSize session_key_length);

  public static native NativeSize olm_import_inbound_group_session(
      PointerByReference session, byte session_key[], NativeSize session_key_length);

  public static native NativeSize olm_import_inbound_group_session(
      PointerByReference session, Pointer session_key, NativeSize session_key_length);

  public static native NativeSize olm_group_decrypt_max_plaintext_length(
      PointerByReference session, ByteBuffer message, NativeSize message_length);

  public static native NativeSize olm_group_decrypt_max_plaintext_length(
      PointerByReference session, Pointer message, NativeSize message_length);

  public static native NativeSize olm_group_decrypt(
      PointerByReference session,
      ByteBuffer message,
      NativeSize message_length,
      ByteBuffer plaintext,
      NativeSize max_plaintext_length,
      IntBuffer message_index);

  public static native NativeSize olm_group_decrypt(
      PointerByReference session,
      Pointer message,
      NativeSize message_length,
      Pointer plaintext,
      NativeSize max_plaintext_length,
      IntByReference message_index);

  public static native NativeSize olm_inbound_group_session_id_length(PointerByReference session);

  public static native NativeSize olm_inbound_group_session_id(
      PointerByReference session, ByteBuffer id, NativeSize id_length);

  public static native NativeSize olm_inbound_group_session_id(
      PointerByReference session, Pointer id, NativeSize id_length);

  public static native int olm_inbound_group_session_first_known_index(PointerByReference session);

  public static native int olm_inbound_group_session_is_verified(PointerByReference session);

  public static native NativeSize olm_export_inbound_group_session_length(
      PointerByReference session);

  public static native NativeSize olm_export_inbound_group_session(
      PointerByReference session, ByteBuffer key, NativeSize key_length, int message_index);

  public static native NativeSize olm_export_inbound_group_session(
      PointerByReference session, Pointer key, NativeSize key_length, int message_index);

  // olm/outbound_group_session.h
  public static native NativeSize olm_outbound_group_session_size();

  public static native OlmOutboundGroupSession olm_outbound_group_session(Pointer memory);

  public static native String olm_outbound_group_session_last_error(PointerByReference session);

  public static native int olm_outbound_group_session_last_error_code(PointerByReference session);

  public static native NativeSize olm_clear_outbound_group_session(PointerByReference session);

  public static native NativeSize olm_pickle_outbound_group_session_length(
      PointerByReference session);

  public static native NativeSize olm_pickle_outbound_group_session(
      PointerByReference session,
      Pointer key,
      NativeSize key_length,
      Pointer pickled,
      NativeSize pickled_length);

  public static native NativeSize olm_unpickle_outbound_group_session(
      PointerByReference session,
      Pointer key,
      NativeSize key_length,
      Pointer pickled,
      NativeSize pickled_length);

  public static native NativeSize olm_init_outbound_group_session_random_length(
      PointerByReference session);

  public static native NativeSize olm_init_outbound_group_session(
      PointerByReference session, ByteBuffer random, NativeSize random_length);

  public static native NativeSize olm_init_outbound_group_session(
      PointerByReference session, Pointer random, NativeSize random_length);

  public static native NativeSize olm_group_encrypt_message_length(
      PointerByReference session, NativeSize plaintext_length);

  public static native NativeSize olm_group_encrypt(
      PointerByReference session,
      byte plaintext[],
      NativeSize plaintext_length,
      ByteBuffer message,
      NativeSize message_length);

  public static native NativeSize olm_group_encrypt(
      PointerByReference session,
      Pointer plaintext,
      NativeSize plaintext_length,
      Pointer message,
      NativeSize message_length);

  public static native NativeSize olm_outbound_group_session_id_length(PointerByReference session);

  public static native NativeSize olm_outbound_group_session_id(
      PointerByReference session, ByteBuffer id, NativeSize id_length);

  public static native NativeSize olm_outbound_group_session_id(
      PointerByReference session, Pointer id, NativeSize id_length);

  public static native int olm_outbound_group_session_message_index(PointerByReference session);

  public static native NativeSize olm_outbound_group_session_key_length(PointerByReference session);

  public static native NativeSize olm_outbound_group_session_key(
      PointerByReference session, ByteBuffer key, NativeSize key_length);

  public static native NativeSize olm_outbound_group_session_key(
      PointerByReference session, Pointer key, NativeSize key_length);

  // olm/olm.h
  public static native void olm_get_library_version(
      ByteBuffer major, ByteBuffer minor, ByteBuffer patch);

  public static native NativeSize olm_account_size();

  public static native NativeSize olm_session_size();

  public static native NativeSize olm_utility_size();

  public static native OlmAccount olm_account(Pointer memory);

  public static native OlmSession olm_session(Pointer memory);

  public static native OlmUtility olm_utility(Pointer memory);

  public static native NativeSize olm_error();

  public static native String olm_account_last_error(PointerByReference account);

  public static native int olm_account_last_error_code(PointerByReference account);

  public static native String olm_session_last_error(PointerByReference session);

  public static native int olm_session_last_error_code(PointerByReference session);

  public static native String olm_utility_last_error(PointerByReference utility);

  public static native int olm_utility_last_error_code(PointerByReference utility);

  public static native NativeSize olm_clear_account(PointerByReference account);

  public static native NativeSize olm_clear_session(PointerByReference session);

  public static native NativeSize olm_clear_utility(PointerByReference utility);

  public static native NativeSize olm_pickle_account_length(PointerByReference account);

  public static native NativeSize olm_pickle_session_length(PointerByReference session);

  public static native NativeSize olm_pickle_account(
      PointerByReference account,
      Pointer key,
      NativeSize key_length,
      Pointer pickled,
      NativeSize pickled_length);

  public static native NativeSize olm_pickle_session(
      PointerByReference session,
      Pointer key,
      NativeSize key_length,
      Pointer pickled,
      NativeSize pickled_length);

  public static native NativeSize olm_unpickle_account(
      PointerByReference account,
      Pointer key,
      NativeSize key_length,
      Pointer pickled,
      NativeSize pickled_length);

  public static native NativeSize olm_unpickle_session(
      PointerByReference session,
      Pointer key,
      NativeSize key_length,
      Pointer pickled,
      NativeSize pickled_length);

  public static native NativeSize olm_create_account_random_length(PointerByReference account);

  public static native NativeSize olm_create_account(
      PointerByReference account, Pointer random, NativeSize random_length);

  public static native NativeSize olm_account_identity_keys_length(PointerByReference account);

  public static native NativeSize olm_account_identity_keys(
      PointerByReference account, Pointer identity_keys, NativeSize identity_key_length);

  public static native NativeSize olm_account_signature_length(PointerByReference account);

  public static native NativeSize olm_account_sign(
      PointerByReference account,
      Pointer message,
      NativeSize message_length,
      Pointer signature,
      NativeSize signature_length);

  public static native NativeSize olm_account_one_time_keys_length(PointerByReference account);

  public static native NativeSize olm_account_one_time_keys(
      PointerByReference account, Pointer one_time_keys, NativeSize one_time_keys_length);

  public static native NativeSize olm_account_mark_keys_as_published(PointerByReference account);

  public static native NativeSize olm_account_max_number_of_one_time_keys(
      PointerByReference account);

  public static native NativeSize olm_account_generate_one_time_keys_random_length(
      PointerByReference account, NativeSize number_of_keys);

  public static native NativeSize olm_account_generate_one_time_keys(
      PointerByReference account,
      NativeSize number_of_keys,
      Pointer random,
      NativeSize random_length);

  public static native NativeSize olm_account_generate_fallback_key_random_length(
      PointerByReference account);

  public static native NativeSize olm_account_generate_fallback_key(
      PointerByReference account, Pointer random, NativeSize random_length);

  public static native NativeSize olm_account_fallback_key_length(PointerByReference account);

  public static native NativeSize olm_account_fallback_key(
      PointerByReference account, Pointer fallback_key, NativeSize fallback_key_size);

  public static native NativeSize olm_create_outbound_session_random_length(
      PointerByReference session);

  public static native NativeSize olm_create_outbound_session(
      PointerByReference session,
      PointerByReference account,
      Pointer their_identity_key,
      NativeSize their_identity_key_length,
      Pointer their_one_time_key,
      NativeSize their_one_time_key_length,
      Pointer random,
      NativeSize random_length);

  public static native NativeSize olm_create_inbound_session(
      PointerByReference session,
      PointerByReference account,
      Pointer one_time_key_message,
      NativeSize message_length);

  public static native NativeSize olm_create_inbound_session_from(
      PointerByReference session,
      PointerByReference account,
      Pointer their_identity_key,
      NativeSize their_identity_key_length,
      Pointer one_time_key_message,
      NativeSize message_length);

  public static native NativeSize olm_session_id_length(PointerByReference session);

  public static native NativeSize olm_session_id(
      PointerByReference session, Pointer id, NativeSize id_length);

  public static native int olm_session_has_received_message(PointerByReference session);

  public static native void olm_session_describe(
      PointerByReference session, ByteBuffer buf, NativeSize buflen);

  public static native void olm_session_describe(
      PointerByReference session, Pointer buf, NativeSize buflen);

  public static native NativeSize olm_matches_inbound_session(
      PointerByReference session, Pointer one_time_key_message, NativeSize message_length);

  public static native NativeSize olm_matches_inbound_session_from(
      PointerByReference session,
      Pointer their_identity_key,
      NativeSize their_identity_key_length,
      Pointer one_time_key_message,
      NativeSize message_length);

  public static native NativeSize olm_remove_one_time_keys(
      PointerByReference account, PointerByReference session);

  public static native NativeSize olm_encrypt_message_type(PointerByReference session);

  public static native NativeSize olm_encrypt_random_length(PointerByReference session);

  public static native NativeSize olm_encrypt_message_length(
      PointerByReference session, NativeSize plaintext_length);

  public static native NativeSize olm_encrypt(
      PointerByReference session,
      Pointer plaintext,
      NativeSize plaintext_length,
      Pointer random,
      NativeSize random_length,
      Pointer message,
      NativeSize message_length);

  public static native NativeSize olm_decrypt_max_plaintext_length(
      PointerByReference session,
      NativeSize message_type,
      Pointer message,
      NativeSize message_length);

  public static native NativeSize olm_decrypt(
      PointerByReference session,
      NativeSize message_type,
      Pointer message,
      NativeSize message_length,
      Pointer plaintext,
      NativeSize max_plaintext_length);

  public static native NativeSize olm_sha256_length(PointerByReference utility);

  public static native NativeSize olm_sha256(
      PointerByReference utility,
      Pointer input,
      NativeSize input_length,
      Pointer output,
      NativeSize output_length);

  public static native NativeSize olm_ed25519_verify(
      PointerByReference utility,
      Pointer key,
      NativeSize key_length,
      Pointer message,
      NativeSize message_length,
      Pointer signature,
      NativeSize signature_length);

  // olm/pk.h
  public static native NativeSize olm_pk_encryption_size();

  public static native OlmPkEncryption olm_pk_encryption(Pointer memory);

  public static native String olm_pk_encryption_last_error(PointerByReference encryption);

  public static native int olm_pk_encryption_last_error_code(PointerByReference encryption);

  public static native NativeSize olm_clear_pk_encryption(PointerByReference encryption);

  public static native NativeSize olm_pk_encryption_set_recipient_key(
      PointerByReference encryption, Pointer public_key, NativeSize public_key_length);

  public static native NativeSize olm_pk_ciphertext_length(
      PointerByReference encryption, NativeSize plaintext_length);

  public static native NativeSize olm_pk_mac_length(PointerByReference encryption);

  public static native NativeSize olm_pk_key_length();

  public static native NativeSize olm_pk_encrypt_random_length(PointerByReference encryption);

  public static native NativeSize olm_pk_encrypt(
      PointerByReference encryption,
      Pointer plaintext,
      NativeSize plaintext_length,
      Pointer ciphertext,
      NativeSize ciphertext_length,
      Pointer mac,
      NativeSize mac_length,
      Pointer ephemeral_key,
      NativeSize ephemeral_key_size,
      Pointer random,
      NativeSize random_length);

  public static native NativeSize olm_pk_decryption_size();

  public static native OlmPkDecryption olm_pk_decryption(Pointer memory);

  public static native String olm_pk_decryption_last_error(PointerByReference decryption);

  public static native int olm_pk_decryption_last_error_code(PointerByReference decryption);

  public static native NativeSize olm_clear_pk_decryption(PointerByReference decryption);

  public static native NativeSize olm_pk_private_key_length();

  public static native NativeSize olm_pk_generate_key_random_length();

  public static native NativeSize olm_pk_key_from_private(
      PointerByReference decryption,
      Pointer pubkey,
      NativeSize pubkey_length,
      Pointer privkey,
      NativeSize privkey_length);

  public static native NativeSize olm_pk_generate_key(
      PointerByReference decryption,
      Pointer pubkey,
      NativeSize pubkey_length,
      Pointer privkey,
      NativeSize privkey_length);

  public static native NativeSize olm_pickle_pk_decryption_length(PointerByReference decryption);

  public static native NativeSize olm_pickle_pk_decryption(
      PointerByReference decryption,
      Pointer key,
      NativeSize key_length,
      Pointer pickled,
      NativeSize pickled_length);

  public static native NativeSize olm_unpickle_pk_decryption(
      PointerByReference decryption,
      Pointer key,
      NativeSize key_length,
      Pointer pickled,
      NativeSize pickled_length,
      Pointer pubkey,
      NativeSize pubkey_length);

  public static native NativeSize olm_pk_max_plaintext_length(
      PointerByReference decryption, NativeSize ciphertext_length);

  public static native NativeSize olm_pk_decrypt(
      PointerByReference decryption,
      Pointer ephemeral_key,
      NativeSize ephemeral_key_length,
      Pointer mac,
      NativeSize mac_length,
      Pointer ciphertext,
      NativeSize ciphertext_length,
      Pointer plaintext,
      NativeSize max_plaintext_length);

  public static native NativeSize olm_pk_get_private_key(
      PointerByReference decryption, Pointer private_key, NativeSize private_key_length);

  public static native NativeSize olm_pk_signing_size();

  public static native OlmPkSigning olm_pk_signing(Pointer memory);

  public static native String olm_pk_signing_last_error(PointerByReference sign);

  public static native int olm_pk_signing_last_error_code(PointerByReference sign);

  public static native NativeSize olm_clear_pk_signing(PointerByReference sign);

  public static native NativeSize olm_pk_signing_key_from_seed(
      PointerByReference sign,
      Pointer pubkey,
      NativeSize pubkey_length,
      Pointer seed,
      NativeSize seed_length);

  public static native NativeSize olm_pk_signing_seed_length();

  public static native NativeSize olm_pk_signing_public_key_length();

  public static native NativeSize olm_pk_signature_length();

  public static native NativeSize olm_pk_sign(
      PointerByReference sign,
      byte message[],
      NativeSize message_length,
      ByteBuffer signature,
      NativeSize signature_length);

  public static native NativeSize olm_pk_sign(
      PointerByReference sign,
      Pointer message,
      NativeSize message_length,
      Pointer signature,
      NativeSize signature_length);

  // olm/sas.h
  public static native String olm_sas_last_error(PointerByReference sas);

  public static native int olm_sas_last_error_code(PointerByReference sas);

  public static native NativeSize olm_sas_size();

  public static native OlmSas olm_sas(Pointer memory);

  public static native NativeSize olm_clear_sas(PointerByReference sas);

  public static native NativeSize olm_create_sas_random_length(PointerByReference sas);

  public static native NativeSize olm_create_sas(
      PointerByReference sas, Pointer random, NativeSize random_length);

  public static native NativeSize olm_sas_pubkey_length(PointerByReference sas);

  public static native NativeSize olm_sas_get_pubkey(
      PointerByReference sas, Pointer pubkey, NativeSize pubkey_length);

  public static native NativeSize olm_sas_set_their_key(
      PointerByReference sas, Pointer their_key, NativeSize their_key_length);

  public static native int olm_sas_is_their_key_set(PointerByReference sas);

  public static native NativeSize olm_sas_generate_bytes(
      PointerByReference sas,
      Pointer info,
      NativeSize info_length,
      Pointer output,
      NativeSize output_length);

  public static native NativeSize olm_sas_mac_length(PointerByReference sas);

  public static native NativeSize olm_sas_calculate_mac(
      PointerByReference sas,
      Pointer input,
      NativeSize input_length,
      Pointer info,
      NativeSize info_length,
      Pointer mac,
      NativeSize mac_length);

  public static native NativeSize olm_sas_calculate_mac_fixed_base64(
      PointerByReference sas,
      Pointer input,
      NativeSize input_length,
      Pointer info,
      NativeSize info_length,
      Pointer mac,
      NativeSize mac_length);

  public static native NativeSize olm_sas_calculate_mac_long_kdf(
      PointerByReference sas,
      Pointer input,
      NativeSize input_length,
      Pointer info,
      NativeSize info_length,
      Pointer mac,
      NativeSize mac_length);
  ;
}
