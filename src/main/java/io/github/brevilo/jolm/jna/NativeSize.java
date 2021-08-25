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

import com.sun.jna.IntegerType;
import com.sun.jna.Memory;
import com.sun.jna.Native;

/** Internal native long (size_t) representation. */
public class NativeSize extends IntegerType {

  private static final long serialVersionUID = 1L;

  public NativeSize() {
    this(0);
  }

  public NativeSize(long value) {
    super(Native.SIZE_T_SIZE, value, true);
  }

  public NativeSize(Memory buffer) {
    this(buffer.size());
  }

  public boolean equalTo(NativeSize value) {
    return this.longValue() == value.longValue();
  }
}
