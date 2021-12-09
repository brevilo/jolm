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

import io.github.brevilo.jolm.jna.OlmLibrary;

/** Provider for various Matrix constants. */
public class Constant {
  public static final byte MIN_OLM_VERSION_MAJOR = 3;
  public static final byte MIN_OLM_VERSION_MINOR = 2;
  public static final byte MIN_OLM_VERSION_PATCH = 7;

  public static final String UTF8 = "UTF-8";

  public static final String KEY_ED25519 = "ed25519";
  public static final String KEY_CURVE25519 = "curve25519";
  public static final String KEY_SIGNED_CURVE25519 = "signed_curve25519";

  public static final String ALGO_OLM = "m.olm.v1.curve25519-aes-sha2";
  public static final String ALGO_MEGOLM = "m.megolm.v1.aes-sha2";

  public static final String JSON_SIGNATURES = "signatures";
  public static final String JSON_UNSIGNED = "unsigned";

  public static final long MESSAGE_TYPE_PRE_KEY = OlmLibrary.OLM_MESSAGE_TYPE_PRE_KEY;
  public static final long MESSAGE_TYPE_MESSAGE = OlmLibrary.OLM_MESSAGE_TYPE_MESSAGE;
}
