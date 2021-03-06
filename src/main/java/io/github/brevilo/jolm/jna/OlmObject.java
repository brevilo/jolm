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

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/** Internal backing store for all libolm objects. */
public abstract class OlmObject extends PointerByReference {
  private Memory backingStore;

  public OlmObject() {
    super();
  }

  public OlmObject(Pointer address) {
    super(address);
  }

  public Memory getBackingStore() {
    return backingStore;
  }

  public void setBackingStore(Memory backingStore) {
    this.backingStore = backingStore;
  }
}
