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

import io.github.brevilo.jolm.model.GroupMessage;
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
class GroupSessionTest {
  private final String PLAINTEXT = "TEST";
  private String cipher;

  private OutboundGroupSession aliceOutboundSession;
  private InboundGroupSession bobInboundSession;

  @BeforeAll
  void setUp() throws Exception {
    aliceOutboundSession = new OutboundGroupSession();
  }

  @AfterAll
  void tearDown() throws Exception {
    aliceOutboundSession.clear();

    if (bobInboundSession != null) {
      bobInboundSession.clear();
    }
  }

  @Test
  void testOutboundSessionId() throws Exception {
    String sessionId = aliceOutboundSession.sessionId();
    assertNotNull(sessionId);
    assertFalse(sessionId.isEmpty());
  }

  @Test
  @Order(1)
  void testOutboundMessageIndex() throws Exception {
    assertEquals(0, aliceOutboundSession.messageIndex());
  }

  @Test
  void testOutboundSessionKey() throws Exception {
    String sessionKey = aliceOutboundSession.sessionKey();
    assertNotNull(sessionKey);
    assertFalse(sessionKey.isEmpty());
  }

  @Test
  @Order(2)
  void testOutboundEncrypt() throws Exception {
    cipher = aliceOutboundSession.encrypt(PLAINTEXT);
    assertNotNull(cipher);
    assertFalse(cipher.isEmpty());
    assertEquals(1, aliceOutboundSession.messageIndex());
  }

  @Test
  @Order(3)
  void testOutboundSerialization() throws Exception {
    final String key = "SECRET";

    String serialized = aliceOutboundSession.pickle(key);
    assertNotNull(serialized);
    assertFalse(serialized.isEmpty());

    OutboundGroupSession deserialized = OutboundGroupSession.unpickle(key, serialized);
    assertEquals(aliceOutboundSession.sessionId(), deserialized.sessionId());
    assertEquals(aliceOutboundSession.messageIndex(), deserialized.messageIndex());
    assertEquals(aliceOutboundSession.sessionKey(), deserialized.sessionKey());

    deserialized.clear();
  }

  @Test
  @Order(1)
  void testInboundSessionCreate() throws Exception {
    bobInboundSession = new InboundGroupSession(aliceOutboundSession.sessionKey());
    assertNotNull(bobInboundSession);
  }

  @Test
  @Order(2)
  void testInboundSessionId() throws Exception {
    String sessionId = bobInboundSession.sessionId();
    assertNotNull(sessionId);
    assertFalse(sessionId.isEmpty());

    assertEquals(aliceOutboundSession.sessionId(), sessionId);
  }

  @Test
  @Order(3)
  void testInboundFirstKnownIndex() throws Exception {
    assertEquals(0, bobInboundSession.firstKnownIndex());
  }

  @Test
  @Order(3)
  void testInboundDecrypt() throws Exception {
    GroupMessage message = bobInboundSession.decrypt(cipher);
    assertNotNull(message);

    String plainText = message.getMessage();
    assertNotNull(plainText);
    assertFalse(plainText.isEmpty());

    assertEquals(PLAINTEXT, plainText);
    assertEquals(0, message.getIndex());
  }

  @Test
  @Order(4)
  void testInboundIsVerified() throws Exception {
    assertTrue(bobInboundSession.isVerified());
  }

  @Test
  @Order(4)
  void testInboundSerialization() throws Exception {
    final String key = "SECRET";

    String serialized = bobInboundSession.pickle(key);
    assertNotNull(serialized);
    assertFalse(serialized.isEmpty());

    InboundGroupSession deserialized = InboundGroupSession.unpickle(key, serialized);
    assertEquals(bobInboundSession.sessionId(), deserialized.sessionId());
    assertEquals(bobInboundSession.firstKnownIndex(), deserialized.firstKnownIndex());
    assertEquals(bobInboundSession.isVerified(), deserialized.isVerified());

    deserialized.clear();
  }

  @Test
  @Order(5)
  void testInboundExportImport() throws Exception {
    // check original
    assertTrue(bobInboundSession.isVerified());

    GroupMessage message = bobInboundSession.decrypt(cipher);
    String plainText = message.getMessage();
    assertEquals(PLAINTEXT, plainText);
    assertEquals(0, message.getIndex());

    // export session
    String sessionKey = bobInboundSession.export(0);
    assertNotNull(sessionKey);
    assertFalse(sessionKey.isEmpty());

    // clear and re-import session
    bobInboundSession.clear();
    bobInboundSession.importer(sessionKey);

    // check imported session
    assertFalse(bobInboundSession.isVerified());

    message = bobInboundSession.decrypt(cipher);
    plainText = message.getMessage();
    assertEquals(PLAINTEXT, plainText);
    assertEquals(0, message.getIndex());

    assertTrue(bobInboundSession.isVerified());
  }
}
