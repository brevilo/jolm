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

/**
 * Model class representing an encrypted message. <code>mac</code> is a Message Authentication Code
 * to ensure that the data is received and decrypted properly. <code>ephemeral</code> is the public
 * part of the ephemeral key used (together with the recipient's key) to generate a symmetric
 * encryption key.
 */
public class PkMessage {

  private String cipherText;
  private String mac;
  private String ephemeral;

  /**
   * Creates a new PkMessage.
   *
   * @param cipherText cipher text to set
   * @param mac MAC to set
   * @param ephemeral ephemeral key to set
   */
  public PkMessage(String cipherText, String mac, String ephemeral) {
    this.setCipherText(cipherText);
    this.setMac(mac);
    this.setEphemeral(ephemeral);
  }

  /**
   * Gets the cipher text.
   *
   * @return cipher text
   */
  public String getCipherText() {
    return cipherText;
  }

  /**
   * Sets the cipher text.
   *
   * @param cipherText cipher text
   */
  public void setCipherText(String cipherText) {
    this.cipherText = cipherText;
  }

  /**
   * Gets the MAC.
   *
   * @return MAC
   */
  public String getMac() {
    return mac;
  }

  /**
   * Sets the MAC.
   *
   * @param mac MAC
   */
  public void setMac(String mac) {
    this.mac = mac;
  }

  /**
   * Gets the ephemeral key.
   *
   * @return ephemeral key
   */
  public String getEphemeral() {
    return ephemeral;
  }

  /**
   * Sets the ephemeral key.
   *
   * @param ephemeral ephemeral key
   */
  public void setEphemeral(String ephemeral) {
    this.ephemeral = ephemeral;
  }
}
