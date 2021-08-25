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

import java.security.MessageDigest;
import java.util.Base64;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class UtilityTest {

  private Utility utility;

  @BeforeAll
  void setUp() throws Exception {
    utility = new Utility();
  }

  @AfterAll
  void tearDown() throws Exception {
    utility.clear();
  }

  @Test
  void testSha256() throws Exception {
    final String testString = "TEST";
    final String testHash = utility.sha256(testString);

    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] trueBytes = digest.digest(testString.getBytes("UTF-8"));
    String trueHash = Base64.getEncoder().withoutPadding().encodeToString(trueBytes);

    if (!testHash.equals(trueHash)) {
      fail(String.format("SHA-256 hash mismatch: %s != %s", testHash, trueHash));
    }
  }
}
