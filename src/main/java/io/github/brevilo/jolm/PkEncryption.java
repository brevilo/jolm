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
import io.github.brevilo.jolm.jna.OlmPkEncryption;
import io.github.brevilo.jolm.model.PkMessage;

/** Class to represent an Olm encryption object. */
public class PkEncryption {

  /** Encryption object backing store. */
  public final OlmPkEncryption instance =
      Utils.initialize(OlmLibrary::olm_pk_encryption, OlmLibrary::olm_pk_encryption_size);

  /**
   * Create a new encryption object for the given recipient key. The recepient's public key will be
   * provided by the corresponding decryption object ({@link PkDecryption#publicKey()}).
   *
   * @param recipientKey the public key of the recipient
   * @throws OlmException unspecified
   */
  public PkEncryption(String recipientKey) throws OlmException {
    // get native key
    Memory keyBuffer = Utils.toNative(recipientKey);

    // call olm
    NativeSize result =
        OlmLibrary.olm_pk_encryption_set_recipient_key(
            instance, keyBuffer, new NativeSize(keyBuffer));

    checkOlmResult(result);
  }

  /** Clears the memory used to back this encryption object. */
  public void clear() {
    OlmLibrary.olm_clear_pk_encryption(instance);
  }

  /**
   * Encrypt a plaintext for the recipient set via {@link #PkEncryption(String)}. Returns a @see
   * PkMessage instance whose details should be sent to the recipient.
   *
   * @param plainText plaintext to be encrypted
   * @return encrypted message
   * @throws OlmException <code>OLM_INPUT_BUFFER_TOO_SMALL</code> if there weren't enough random
   *     bytes; <code>OUTPUT_BUFFER_TOO_SMALL</code> if the ciphertext, MAC, or ephemeral key
   *     buffers were too small
   */
  public PkMessage encrypt(String plainText) throws OlmException {
    // get native text
    Memory plainTextBuffer = Utils.toNative(plainText);

    // get required buffer sizes
    NativeSize cipherTextLength =
        OlmLibrary.olm_pk_ciphertext_length(instance, new NativeSize(plainTextBuffer));
    NativeSize macLength = OlmLibrary.olm_pk_mac_length(instance);
    NativeSize ephemeralLength = OlmLibrary.olm_pk_key_length();
    NativeSize randomLength = OlmLibrary.olm_pk_encrypt_random_length(instance);

    // prepare required buffers
    Memory cipherTextBuffer = new Memory(cipherTextLength.longValue());
    Memory macBuffer = new Memory(macLength.longValue());
    Memory ephemeralBuffer = new Memory(ephemeralLength.longValue());
    Memory randomBuffer = Utils.randomBuffer(randomLength);

    // call olm
    NativeSize result =
        OlmLibrary.olm_pk_encrypt(
            instance,
            plainTextBuffer,
            new NativeSize(plainTextBuffer),
            cipherTextBuffer,
            cipherTextLength,
            macBuffer,
            macLength,
            ephemeralBuffer,
            ephemeralLength,
            randomBuffer,
            randomLength);

    checkOlmResult(result);

    return new PkMessage(
        Utils.fromNative(cipherTextBuffer),
        Utils.fromNative(macBuffer),
        Utils.fromNative(ephemeralBuffer));
  }

  /**
   * Check the latest olm function call for errors.
   *
   * @param result result returned by the olm function call to check
   * @throws OlmException thrown if an error occurred (incl. error details)
   */
  private void checkOlmResult(NativeSize result) throws OlmException {
    if (result.equalTo(OlmLibrary.olm_error())) {
      throw new OlmException(OlmLibrary.olm_pk_encryption_last_error(instance));
    }
  }
}
