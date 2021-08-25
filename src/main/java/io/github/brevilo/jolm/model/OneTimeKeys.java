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

package io.github.brevilo.jolm.model;

import java.util.Map;

/**
 * Model class representing one time keys.
 *
 * <p>Single property <code>curve25519</code> which is itself a map from a key id to a
 * base64-encoded Curve25519 one time key. For example (as JSON):
 *
 * <pre>
 * {
 *     curve25519: {
 *         "AAAAAA": "wo76WcYtb0Vk/pBOdmduiGJ0wIEjW4IBMbbQn7aSnTo",
 *         "AAAAAB": "LRvjo46L1X2vx69sS9QNFD29HWulxrmW11Up5AfAjgU"
 *     }
 * }
 * </pre>
 */
public class OneTimeKeys {
  private Map<String, String> curve25519;

  /**
   * Gets the one time key map.
   *
   * @return one time key map
   */
  public Map<String, String> getCurve25519() {
    return curve25519;
  }

  /**
   * Sets the one time key map.
   *
   * @param curve25519 one time key map
   */
  public void setCurve25519(Map<String, String> curve25519) {
    this.curve25519 = curve25519;
  }
}
