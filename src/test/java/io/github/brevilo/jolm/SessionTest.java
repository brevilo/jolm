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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.github.brevilo.jolm.model.Message;
import io.github.brevilo.jolm.model.OneTimeKeys;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
class SessionTest {
  Account aliceAccount;
  Session aliceSession;

  Account bobAccount;
  Session bobSession;

  @BeforeAll
  void setUp() throws Exception {
    aliceAccount = new Account();
    bobAccount = new Account();
  }

  @AfterAll
  void tearDown() throws Exception {
    aliceAccount.clear();
    aliceSession.clear();

    bobAccount.clear();
    bobSession.clear();
  }

  @Test
  @Order(1)
  void testAliceToBob() throws Exception {
    final String message = "5-BY-5!";

    // generate identity keys
    String aliceIdentityKey = aliceAccount.identityKeys().getCurve25519();
    assertNotNull(aliceIdentityKey);
    assertFalse(aliceIdentityKey.isEmpty());

    String bobIdentityKey = bobAccount.identityKeys().getCurve25519();
    assertNotNull(bobIdentityKey);
    assertFalse(bobIdentityKey.isEmpty());

    // generate bob's one-time key
    bobAccount.generateOneTimeKeys(1);
    OneTimeKeys bobOneTimeKeys = bobAccount.oneTimeKeys();
    String bobOneTimeKey = (String) bobOneTimeKeys.getCurve25519().values().toArray()[0];
    assertNotNull(bobOneTimeKey);
    assertFalse(bobOneTimeKey.isEmpty());

    // create alice's outbound olm Session (to bob)
    aliceSession = Session.createOutboundSession(aliceAccount, bobIdentityKey, bobOneTimeKey);

    // encrypt PRE_KEY message for bob
    Message encryptedMessage = aliceSession.encrypt(message);

    // create bob's inbound olm test session (from alice)
    Session bobTestSession =
        Session.createInboundSession(bobAccount, encryptedMessage.getCipherText());
    assertNotNull(bobTestSession.sessionId());
    bobTestSession.clear();

    // create bob's verified session
    bobSession =
        Session.createInboundSessionFrom(
            bobAccount, aliceIdentityKey, encryptedMessage.getCipherText());

    // ensure this isn't a MESSAGE message (but a PRE_KEY message)
    assertFalse(bobSession.hasReceivedMessage());

    // verify an incoming PRE_KEY message
    if (bobSession.matchesInboundSession(encryptedMessage.getCipherText())) {

      if (bobSession.matchesInboundSessionFrom(
          aliceIdentityKey, encryptedMessage.getCipherText())) {

        // decrypt message from alice
        String decryptedMessage = bobSession.decrypt(encryptedMessage);
        assertNotNull(decryptedMessage);
        assertFalse(decryptedMessage.isEmpty());

        // compare message content
        assertEquals(message, decryptedMessage);
      } else {
        fail("PRE_KEY message doesn't match expected identity key!");
      }
    } else {
      fail("PRE_KEY message doesn't match inbound session!");
    }

    // remove used one-time key
    bobAccount.removeOneTimeKeys(bobSession);
  }

  @Test
  @Order(2)
  void testSessionId() throws Exception {
    String aliceSessionId = aliceSession.sessionId();
    String bobSessionId = bobSession.sessionId();

    assertNotNull(aliceSessionId);
    assertFalse(aliceSessionId.isEmpty());
    assertNotNull(bobSessionId);
    assertFalse(bobSessionId.isEmpty());
    assertEquals(aliceSessionId, bobSessionId);
  }

  @Test
  @Order(2)
  void testDescribe() throws Exception {
    final long LENGTH = 128;
    String description = aliceSession.describe(LENGTH);

    assertNotNull(description);
    assertTrue(description.length() <= LENGTH);
  }

  @Test
  @Order(3)
  void testSerialization() throws Exception {
    final String key = "SECRET";

    String serialized = aliceSession.pickle(key);
    assertNotNull(serialized);
    assertFalse(serialized.isEmpty());

    Session deserialized = Session.unpickle(key, serialized);
    assertEquals(aliceSession.sessionId(), deserialized.sessionId());

    deserialized.clear();
  }
}
