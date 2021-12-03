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

import io.github.brevilo.jolm.model.PkMessage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class PkTest {

  private PkDecryption decryption;
  private PkSigning signing;

  @BeforeAll
  void setUp() throws Exception {
    decryption = new PkDecryption();
    signing = new PkSigning();
  }

  @AfterAll
  void tearDown() throws Exception {
    decryption.clear();
    signing.clear();
  }

  @Test
  void testDecryptionPublicKey() throws Exception {
    final String key = decryption.publicKey();
    assertNotNull(key);
    assertFalse(key.isEmpty());
  }

  @Test
  void testDecryptionPrivateKey() throws Exception {
    final String key = decryption.privateKey();
    assertNotNull(key);
    assertFalse(key.isEmpty());
  }

  @Test
  void testDecryptionSerialization() throws Exception {
    final String key = "SECRET";

    String serialized = decryption.pickle(key);
    assertNotNull(serialized);
    assertFalse(serialized.isEmpty());

    PkDecryption deserialized = PkDecryption.unpickle(key, serialized);
    assertEquals(decryption.publicKey(), deserialized.publicKey());
    assertEquals(decryption.privateKey(), deserialized.privateKey());

    deserialized.clear();
  }

  @Test
  void testEncryptionDecryption() throws Exception {
    final String plainText = "HELLO!";

    // bob gets public key and publishes it
    final String key = decryption.publicKey();
    assertNotNull(key);
    assertFalse(key.isEmpty());

    // alice uses bob's public key to encrypt
    PkEncryption encryption = new PkEncryption(key);
    PkMessage encryptedMessage = encryption.encrypt(plainText);

    // bob decrypts alice's message
    String message = decryption.decrypt(encryptedMessage);
    assertNotNull(message);
    assertFalse(message.isEmpty());
    assertEquals(plainText, message);

    encryption.clear();
  }

  @Test
  void testSigningSeededCreate() throws Exception {
    final byte[] seed = PkSigning.generateSeed();
    PkSigning base = new PkSigning(seed);
    PkSigning copy = new PkSigning(seed);

    assertEquals(base.publicKey(), copy.publicKey());

    base.clear();
    copy.clear();
  }

  @Test
  void testSigningSign() throws Exception {
    final String message = Utils.canonicalizeJson("{ \"content\" : {} }");

    final String key = signing.publicKey();
    assertNotNull(key);
    assertFalse(key.isEmpty());

    String signature = signing.sign(message);

    Utility utility = new Utility();
    utility.verifyEd25519(key, message, signature);
    utility.clear();
  }

  @Test
  void testSigningPublicKey() throws Exception {
    final String key = signing.publicKey();
    assertNotNull(key);
    assertFalse(key.isEmpty());
  }

  @Test
  void testSigningSeedLength() throws Exception {
    final int key = PkSigning.seedLength();
    assertNotNull(key);
    assertTrue(key > 0);
  }

  @Test
  void testSigningGenerateSeed() throws Exception {
    final byte[] seed = PkSigning.generateSeed();
    assertNotNull(seed);
    assertEquals(PkSigning.seedLength(), seed.length);
  }
}
