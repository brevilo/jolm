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

import io.github.brevilo.jolm.model.Message;
import io.github.brevilo.jolm.model.OneTimeKeys;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class SessionTest {
  final String message = "5-BY-5!";

  Account aliceAccount;
  Session aliceSession;

  Account bobAccount;
  Session bobSession;

  String bobIdentityKey;
  String bobOneTimeKey;

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
  void testAliceToBob() throws Exception {
    // generate bob's identity key
    bobIdentityKey = bobAccount.identityKeys().getCurve25519();

    // generate bob's one-time key
    bobAccount.generateOneTimeKeys(1);
    OneTimeKeys bobOneTimeKeys = bobAccount.oneTimeKeys();
    bobOneTimeKeys
        .getCurve25519()
        .forEach(
            (id, key) -> {
              bobOneTimeKey = key;
            });

    // create alice's outbound olm Session (to bob)
    aliceSession = Session.createOutboundSession(aliceAccount, bobIdentityKey, bobOneTimeKey);

    // encrypt message for bob
    Message encryptedMessage = aliceSession.encrypt(message);

    // create bob's inbound olm session (from alice)
    bobSession = Session.createInboundSession(bobAccount, encryptedMessage.getCipherText());

    // decrypt message from alice
    String decryptedMessage = bobSession.decrypt(encryptedMessage);

    // compare message content
    assertEquals(decryptedMessage, message);

    // remove used one-time key
    bobAccount.removeOneTimeKeys(bobSession);
  }
}
