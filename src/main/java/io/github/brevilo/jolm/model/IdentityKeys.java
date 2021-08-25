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

/** Model class representing identity keys. */
public class IdentityKeys {
  private String curve25519;
  private String ed25519;

  public String getCurve25519() {
    return curve25519;
  }

  public void setCurve25519(String curve25519) {
    this.curve25519 = curve25519;
  }

  public String getEd25519() {
    return ed25519;
  }

  public void setEd25519(String ed25519) {
    this.ed25519 = ed25519;
  }
}
