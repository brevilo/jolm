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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class UtilsTest {

  @Test
  void testSignJson() throws Exception {
    final ObjectMapper objectMapper = new ObjectMapper();
    final Account account = new Account();
    final String jsonShort =
        "{\"name\":\"example.org\",\"signing_keys\":{\"ed25519:1\":\"XSl0kuyvrXNj6A+7/tkrB9sxSbRi08Of5uRhxOqZtEQ\"}}";
    final String jsonFull =
        "{\n"
            + "   \"name\": \"example.org\",\n"
            + "   \"signing_keys\": {\n"
            + "     \"ed25519:1\": \"XSl0kuyvrXNj6A+7/tkrB9sxSbRi08Of5uRhxOqZtEQ\"\n"
            + "   },\n"
            + "   \"unsigned\": {\n"
            + "      \"age_ts\": 922834800000\n"
            + "   },\n"
            + "   \"signatures\": {\n"
            + "      \"example.org\": {\n"
            + "         \"ed25519:1\": \"s76RUgajp8w172am0zQb/iPTHsRnb4SkrzGoeCOSFfcBY2V/1c8QfrmdXHpvnc2jK5BD1WiJIxiMW95fMjK7Bw\"\n"
            + "      }\n"
            + "   }\n"
            + "}";

    // sign JSON
    String signedJson =
        Utils.signJson(account, jsonFull, "USERID", Constant.KEY_ED25519, "DEVICEID");

    // verify original content
    JsonNode node = objectMapper.readTree(signedJson);
    assertEquals("example.org", node.at("/name").asText());
    assertEquals(
        "XSl0kuyvrXNj6A+7/tkrB9sxSbRi08Of5uRhxOqZtEQ", node.at("/signing_keys/ed25519:1").asText());
    assertEquals("922834800000", node.at("/unsigned/age_ts").asText());
    assertEquals(
        "s76RUgajp8w172am0zQb/iPTHsRnb4SkrzGoeCOSFfcBY2V/1c8QfrmdXHpvnc2jK5BD1WiJIxiMW95fMjK7Bw",
        node.at("/signatures/example.org/ed25519:1").asText());

    // verify new signature format
    assertFalse(node.at("/signatures/USERID/ed25519:DEVICEID").isMissingNode());
    assertEquals(
        node.at("/signatures/example.org/ed25519:1").asText().length(),
        node.at("/signatures/USERID/ed25519:DEVICEID").asText().length());

    // verify new signature content
    assertEquals(account.sign(jsonShort), node.at("/signatures/USERID/ed25519:DEVICEID").asText());

    account.clear();
  }

  @Test
  void testCanonicalizeJson() throws Exception {
    assertEquals("{}", Utils.canonicalizeJson("{}"));

    assertEquals(
        "{\"one\":1,\"two\":\"Two\"}",
        Utils.canonicalizeJson("{\n" + "    \"one\": 1,\n" + "    \"two\": \"Two\"\n" + "}"));

    assertEquals(
        "{\"a\":\"1\",\"b\":\"2\"}",
        Utils.canonicalizeJson("{\n" + "    \"b\": \"2\",\n" + "    \"a\": \"1\"\n" + "}"));

    assertEquals("{\"a\":\"1\",\"b\":\"2\"}", Utils.canonicalizeJson("{\"b\":\"2\",\"a\":\"1\"}"));

    assertEquals(
        "{\"auth\":{\"mxid\":\"@john.doe:example.com\",\"profile\":{\"display_name\":\"John Doe\",\"three_pids\":[{\"address\":\"john.doe@example.org\",\"medium\":\"email\"},{\"address\":\"123456789\",\"medium\":\"msisdn\"}]},\"success\":true}}",
        Utils.canonicalizeJson(
            "{\n"
                + "    \"auth\": {\n"
                + "        \"success\": true,\n"
                + "        \"mxid\": \"@john.doe:example.com\",\n"
                + "        \"profile\": {\n"
                + "            \"display_name\": \"John Doe\",\n"
                + "            \"three_pids\": [\n"
                + "                {\n"
                + "                    \"medium\": \"email\",\n"
                + "                    \"address\": \"john.doe@example.org\"\n"
                + "                },\n"
                + "                {\n"
                + "                    \"medium\": \"msisdn\",\n"
                + "                    \"address\": \"123456789\"\n"
                + "                }\n"
                + "            ]\n"
                + "        }\n"
                + "    }\n"
                + "}"));

    assertEquals("{\"a\":\"日本語\"}", Utils.canonicalizeJson("{\n" + "    \"a\": \"日本語\"\n" + "}"));

    assertEquals(
        "{\"日\":1,\"本\":2}",
        Utils.canonicalizeJson("{\n" + "    \"本\": 2,\n" + "    \"日\": 1\n" + "}"));

    assertEquals("{\"a\":\"日\"}", Utils.canonicalizeJson("{\n" + "    \"a\": \"\\u65E5\"\n" + "}"));

    assertEquals("{\"a\":null}", Utils.canonicalizeJson("{\n" + "    \"a\": null\n" + "}"));
  }
}
