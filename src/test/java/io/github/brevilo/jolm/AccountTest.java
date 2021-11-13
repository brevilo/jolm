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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.brevilo.jolm.model.IdentityKeys;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class AccountTest {

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
  void testIdentityKeys() throws Exception {
    IdentityKeys keys = account.identityKeys();

    assertNotNull(keys.getCurve25519());
    assertFalse(keys.getCurve25519().isEmpty());

    assertNotNull(keys.getEd25519());
    assertFalse(keys.getEd25519().isEmpty());
  }
}
