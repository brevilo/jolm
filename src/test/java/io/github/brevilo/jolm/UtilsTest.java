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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class UtilsTest {

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
