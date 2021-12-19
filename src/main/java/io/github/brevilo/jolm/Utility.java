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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.jna.Memory;
import io.github.brevilo.jolm.Utils.OlmException;
import io.github.brevilo.jolm.jna.NativeSize;
import io.github.brevilo.jolm.jna.OlmLibrary;
import io.github.brevilo.jolm.jna.OlmUtility;

/** Class to provide libolm utility functions. */
public class Utility {

  // backing store
  private final OlmUtility instance;

  private final JsonMapper jsonMapper;

  // initializer
  {
    jsonMapper = JsonMapper.builder().build();
  }

  /** Creates a new Utility object. */
  public Utility() {
    // initialize backing store
    instance = Utils.initialize(OlmLibrary::olm_utility, OlmLibrary::olm_utility_size);
  }

  /** Clears the memory used to back this utility. */
  public void clear() {
    OlmLibrary.olm_clear_utility(instance);
  }

  /**
   * Verify an ed25519 signature.
   *
   * @param key signing key to use for verification
   * @param message message to be verified
   * @param signature signature to use for verification
   * @throws OlmException <code>INVALID_BASE64</code> if the key was too small; <code>
   *     BAD_MESSAGE_MAC</code> if the signature was invalid
   * @throws JsonProcessingException (de)serialization error
   */
  public void verifyEd25519(String key, String message, String signature)
      throws OlmException, JsonProcessingException {

    ObjectNode node = (ObjectNode) jsonMapper.readTree(message);

    // strip nodes not to be verified
    node.remove(Constant.JSON_SIGNATURES);
    node.remove(Constant.JSON_UNSIGNED);

    // get content
    Memory keyBuffer = Utils.toNative(key);
    Memory messageBuffer = Utils.toNative(Utils.canonicalizeJson(node));
    Memory signatureBuffer = Utils.toNative(signature);

    // call olm
    NativeSize result =
        OlmLibrary.olm_ed25519_verify(
            instance,
            keyBuffer,
            new NativeSize(keyBuffer),
            messageBuffer,
            new NativeSize(messageBuffer),
            signatureBuffer,
            new NativeSize(signatureBuffer));

    // clear the input buffer
    messageBuffer.clear();

    checkOlmResult(result);
  }

  /**
   * Calculates the SHA-256 hash of the input and encodes it as base64.
   *
   * @param input input to be hashed
   * @return base64-encoded SHA-256 hash value
   * @throws OlmException <code>OUTPUT_BUFFER_TOO_SMALL</code> if the hash buffer was too small
   */
  public String sha256(String input) throws OlmException {
    // get native input
    Memory inputBuffer = Utils.toNative(input);

    // prepare output buffer
    NativeSize outputLength = OlmLibrary.olm_sha256_length(instance);
    Memory outputBuffer = new Memory(outputLength.longValue());

    // call olm
    NativeSize result =
        OlmLibrary.olm_sha256(
            instance, inputBuffer, new NativeSize(inputBuffer), outputBuffer, outputLength);

    // clear the input buffer
    inputBuffer.clear();

    checkOlmResult(result);

    return Utils.fromNative(outputBuffer);
  }

  /**
   * Check the latest olm function call for errors.
   *
   * @param result result returned by the olm function call to check
   * @throws OlmException thrown if an error occurred (incl. error details)
   */
  private void checkOlmResult(NativeSize result) throws OlmException {
    if (result.equalTo(OlmLibrary.olm_error())) {
      throw new OlmException(OlmLibrary.olm_utility_last_error(instance));
    }
  }
}
