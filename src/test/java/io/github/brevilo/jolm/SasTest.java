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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class SasTest {

  private Sas aliceSas;
  private Sas bobSas;

  @BeforeAll
  void setUp() throws Exception {
    aliceSas = new Sas();
    bobSas = new Sas();
  }

  @AfterAll
  void tearDown() throws Exception {
    aliceSas.clear();
    bobSas.clear();
  }

  @Test
  void testSas() throws Exception {
    final String sasInfo = "SAS";
    final String macInfo = "MAC";
    final String message = "HELLO!";

    // get public keys
    String aliceKey = aliceSas.publicKey();
    String bobKey = bobSas.publicKey();

    assertNotNull(aliceKey);
    assertFalse(aliceKey.isEmpty());
    assertNotNull(bobKey);
    assertFalse(bobKey.isEmpty());

    // set mutual keys
    assertFalse(aliceSas.isTheirKeySet());
    assertFalse(bobSas.isTheirKeySet());

    aliceSas.setTheirKey(bobKey);
    bobSas.setTheirKey(aliceKey);

    assertTrue(aliceSas.isTheirKeySet());
    assertTrue(bobSas.isTheirKeySet());

    // generate SAS codes
    int codeLength = 5;
    byte[] aliceSasCode = aliceSas.generateBytes(sasInfo, codeLength);
    byte[] bobSasCode = bobSas.generateBytes(sasInfo, codeLength);

    assertEquals(codeLength, aliceSasCode.length);
    assertEquals(codeLength, bobSasCode.length);
    assertArrayEquals(aliceSasCode, bobSasCode);

    // get MACs
    String aliceMac = aliceSas.calculateMac(message, macInfo);
    String bobMac = bobSas.calculateMac(message, macInfo);

    assertEquals(aliceMac, bobMac);

    // get BASE64 encoded MACs
    String aliceBase64Mac = aliceSas.calculateMacFixedBase64(message, macInfo);
    String bobBase64Mac = bobSas.calculateMacFixedBase64(message, macInfo);

    assertEquals(aliceBase64Mac, bobBase64Mac);

    // get obsolete MACs (for completeness, until removed)
    @SuppressWarnings("deprecation")
    String aliceLongKdfMac = aliceSas.calculateMacLongKdf(message, macInfo);
    @SuppressWarnings("deprecation")
    String bobLongKdfMac = bobSas.calculateMacLongKdf(message, macInfo);

    assertEquals(aliceLongKdfMac, bobLongKdfMac);
  }
}
