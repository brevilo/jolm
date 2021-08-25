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

/** Model class representing an Olm message. */
public abstract class Message {
  private static final long PREKEY = 0;
  private static final long NORMAL = 1;

  private String cipherText;

  private Message(String cipherText) {
    this.cipherText = cipherText;
  }

  /**
   * Creates a new message.
   *
   * @param cipherText cipher text to set
   * @param type message type to set
   * @return new Message instance
   */
  public static Message get(String cipherText, long type) {
    if (type == PREKEY) {
      return new Message.PreKey(cipherText);
    } else if (type == NORMAL) {
      return new Message.Normal(cipherText);
    } else {
      return null;
    }
  }

  /**
   * Gets the message type.
   *
   * @return message type
   */
  public Long type() {
    if (this instanceof PreKey) {
      return PREKEY;
    } else if (this instanceof Normal) {
      return NORMAL;
    } else {
      return null;
    }
  }

  /**
   * Gets the cipher text.
   *
   * @return cipher text
   */
  public String getCipherText() {
    return cipherText;
  }

  /** Model class representing an Olm <code>PRE_KEY</code> message. */
  public static class PreKey extends Message {
    public PreKey(String cipherText) {
      super(cipherText);
    }
  }

  /** Model class representing a normal Olm message. */
  public static class Normal extends Message {
    public Normal(String cipherText) {
      super(cipherText);
    }
  }
}
