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

import static org.junit.jupiter.api.Assertions.fail;

import io.github.brevilo.jolm.jna.OlmLibrary;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class OlmLibraryTest {

  @Test
  void testOlmVersion() throws Exception {
    ByteBuffer major = ByteBuffer.allocate(1);
    ByteBuffer minor = ByteBuffer.allocate(1);
    ByteBuffer patch = ByteBuffer.allocate(1);

    OlmLibrary.olm_get_library_version(major, minor, patch);

    if (major.get() <= Constant.MIN_OLM_VERSION_MAJOR
        && minor.get() <= Constant.MIN_OLM_VERSION_MINOR
        && patch.get() < Constant.MIN_OLM_VERSION_PATCH) {
      fail(
          String.format(
              "Minimum required libolm version: %d.%d.%d",
              Constant.MIN_OLM_VERSION_MAJOR,
              Constant.MIN_OLM_VERSION_MINOR,
              Constant.MIN_OLM_VERSION_PATCH));
    }
  }
}
