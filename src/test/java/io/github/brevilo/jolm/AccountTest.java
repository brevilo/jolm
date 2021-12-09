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

import io.github.brevilo.jolm.model.IdentityKeys;
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
class AccountTest {
  final int ONETIME_KEY_COUNT = 5;

  Account account;

  @BeforeAll
  void setUp() throws Exception {
    account = new Account();
  }

  @AfterAll
  void tearDown() throws Exception {
    account.clear();
  }

  @Test
  @Order(0)
  void testCreateAccount() throws Exception {
    assertNotNull(account);
  }

  @Test
  void testIdentityKeys() throws Exception {
    IdentityKeys keys = account.identityKeys();
    assertNotNull(keys);

    assertNotNull(keys.getCurve25519());
    assertFalse(keys.getCurve25519().isEmpty());

    assertNotNull(keys.getEd25519());
    assertFalse(keys.getEd25519().isEmpty());
  }

  @Test
  void testMaxNumberOfOneTimeKeys() throws Exception {
    assertTrue(account.maxNumberOfOneTimeKeys() > 0);
  }

  @Test
  @Order(1)
  void testGenerateOneTimeKeys() throws Exception {
    account.generateOneTimeKeys(ONETIME_KEY_COUNT);
  }

  @Test
  @Order(2)
  void testOneTimeKeys() throws Exception {
    OneTimeKeys keys = account.oneTimeKeys();
    assertNotNull(keys);

    assertNotNull(keys.getCurve25519());
    assertEquals(ONETIME_KEY_COUNT, keys.getCurve25519().size());
  }

  @Test
  @Order(3)
  void testRemoveOneTimeKeys() throws Exception {
    // using identityKeys and oneTimeKeys from own account (for test brevity)
    String identityKey = account.identityKeys().getCurve25519();
    OneTimeKeys oneTimeKeys = account.oneTimeKeys();
    String oneTimeKey = (String) oneTimeKeys.getCurve25519().values().toArray()[0];

    assertFalse(identityKey.isEmpty());
    assertFalse(oneTimeKey.isEmpty());

    Session session = Session.createOutboundSession(account, identityKey, oneTimeKey);
    account.removeOneTimeKeys(session);

    oneTimeKeys = account.oneTimeKeys();
    assertEquals(ONETIME_KEY_COUNT - 1, oneTimeKeys.getCurve25519().size());

    session.clear();
  }

  @Test
  @Order(4)
  void testMarkOneTimeKeysAsPublished() throws Exception {
    assertEquals(4, account.oneTimeKeys().getCurve25519().size());
    assertEquals(1, account.unpublishedFallbackKey().getCurve25519().size());

    account.markKeysAsPublished();

    assertEquals(0, account.oneTimeKeys().getCurve25519().size());
    assertEquals(0, account.unpublishedFallbackKey().getCurve25519().size());
  }

  @Test
  void testSign() throws Exception {
    String signature = account.sign("TEST");
    assertNotNull(signature);
    assertFalse(signature.isEmpty());
  }

  @Test
  @Order(1)
  void testGenerateFallbackKey() throws Exception {
    account.generateFallbackKey();
  }

  @Test
  @Order(2)
  void testUnpublishedFallbackKey() throws Exception {
    OneTimeKeys key = account.unpublishedFallbackKey();
    assertNotNull(key);

    assertNotNull(key.getCurve25519());
    assertFalse(key.getCurve25519().isEmpty());
    assertEquals(1, key.getCurve25519().size());
  }

  @Test
  @Order(3)
  void testforgetFallbackKey() throws Exception {
    assertEquals(1, account.unpublishedFallbackKey().getCurve25519().size());

    account.forgetFallbackKey();
    assertEquals(1, account.unpublishedFallbackKey().getCurve25519().size());

    account.generateFallbackKey();
    assertEquals(1, account.unpublishedFallbackKey().getCurve25519().size());
  }

  @Test
  void testSerialization() throws Exception {
    final String key = "SECRET";
    final Account baseline = new Account();

    baseline.generateOneTimeKeys(1);
    baseline.generateFallbackKey();

    String serialized = baseline.pickle(key);
    assertNotNull(serialized);
    assertFalse(serialized.isEmpty());

    Account deserialized = Account.unpickle(key, serialized);
    assertEquals(
        baseline.identityKeys().getCurve25519(), deserialized.identityKeys().getCurve25519());
    assertEquals(baseline.identityKeys().getEd25519(), deserialized.identityKeys().getEd25519());
    assertEquals(
        baseline.oneTimeKeys().getCurve25519(), deserialized.oneTimeKeys().getCurve25519());
    assertEquals(
        baseline.unpublishedFallbackKey().getCurve25519(),
        deserialized.unpublishedFallbackKey().getCurve25519());

    deserialized.clear();
    baseline.clear();
  }
}
