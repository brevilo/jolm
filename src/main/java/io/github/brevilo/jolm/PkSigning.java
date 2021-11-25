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
import io.github.brevilo.jolm.jna.OlmPkSigning;
import java.security.SecureRandom;

/** Class to represent an Olm signing object. */
public class PkSigning {

  // backing store
  public final OlmPkSigning instance;

  private String publicKey;

  /**
   * Creates a new signing object and initializes a new key pair with a random seed.
   *
   * @throws OlmException <code>INPUT_BUFFER_TOO_SMALL</code> if the seed buffer is too small;
   *     <code>OUTPUT_BUFFER_TOO_SMALL</code> if the public key buffer is too small
   */
  public PkSigning() throws OlmException {
    this(generateSeed());
  }

  /**
   * Creates a new signing object and initializes a new key pair based on the given random seed. Get
   * the required seed length or a generated seed via {@link #seedLength()} and {@link
   * #generateSeed()}.
   *
   * @param seed seed to initialize the random number generator with
   * @throws OlmException <code>INPUT_BUFFER_TOO_SMALL</code> if the seed buffer is too small;
   *     <code>OUTPUT_BUFFER_TOO_SMALL</code> if the public key buffer is too small
   */
  public PkSigning(byte[] seed) throws OlmException {
    // initialize backing store
    instance = Utils.initialize(OlmLibrary::olm_pk_signing, OlmLibrary::olm_pk_signing_size);

    // get native seed
    Memory seedBuffer = new Memory(seed.length);
    seedBuffer.write(0, seed, 0, seed.length);

    // prepare output buffer
    NativeSize publicKeyLength = OlmLibrary.olm_pk_signing_public_key_length();
    Memory publicKeyBuffer = new Memory(publicKeyLength.longValue());

    // call olm
    NativeSize result =
        OlmLibrary.olm_pk_signing_key_from_seed(
            instance, publicKeyBuffer, publicKeyLength, seedBuffer, new NativeSize(seedBuffer));

    checkOlmResult(result);

    // store associated public key
    this.publicKey = Utils.fromNative(publicKeyBuffer);
  }

  /** Clears the memory used to back this signing object. */
  public void clear() {
    OlmLibrary.olm_clear_pk_signing(instance);
  }

  /**
   * Gets the public key. Can be used to check the signature of a messsage that has been signed by
   * this object.
   *
   * @return public key
   */
  public String publicKey() {
    return publicKey;
  }

  /**
   * Sign a message using this object.
   *
   * @param message message to sign
   * @return signature
   * @throws OlmException <code>OUTPUT_BUFFER_TOO_SMALL</code> if the output buffer was too small
   */
  public String sign(String message) throws OlmException {
    // get native message
    Memory messageBuffer = Utils.toNative(message);

    // prepare output buffer
    NativeSize signatureLength = OlmLibrary.olm_pk_signature_length();
    Memory signatureBuffer = new Memory(signatureLength.longValue());

    // call olm
    NativeSize result =
        OlmLibrary.olm_pk_sign(
            instance,
            messageBuffer,
            new NativeSize(messageBuffer),
            signatureBuffer,
            signatureLength);

    checkOlmResult(result);

    return Utils.fromNative(signatureBuffer);
  }

  /**
   * Check the latest olm function call for errors.
   *
   * @param result result returned by the olm function call to check
   * @throws OlmException thrown if an error occurred (incl. error details)
   */
  private void checkOlmResult(NativeSize result) throws OlmException {
    if (result.equalTo(OlmLibrary.olm_error())) {
      throw new OlmException(OlmLibrary.olm_pk_signing_last_error(instance));
    }
  }

  // ================= static methods =================

  /**
   * Get the required random seed length.
   *
   * @return required random seed length
   */
  public static int seedLength() {
    return OlmLibrary.olm_pk_signing_seed_length().intValue();
  }

  /**
   * Generate a random seed that can be used to initialize a signing object.
   *
   * @return random seed of the required length
   */
  public static byte[] generateSeed() {
    SecureRandom random = new SecureRandom();
    return random.generateSeed(seedLength());
  }
}
