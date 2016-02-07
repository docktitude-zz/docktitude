/*
 * Copyright 2015-2016 Docktitude
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
package com.docktitude.cli;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class PrintableTest {

	private static final String EXPECTED_MAP =
			"+-----------------+--------------------+\n" +
					"| key             | value              |\n" +
					"+-----------------+--------------------+\n" +
					"| activemq        | activemq           |\n" +
					"| debian-jdk6     | debian:jdk6        |\n" +
					"| debian-jdk7     | debian:jdk7        |\n" +
					"| debian-jdk7-ui  | debian:jdk7-ui     |\n" +
					"| debian-jdk8     | debian:jdk8        |\n" +
					"| debian-jdk8-scm | debian:jdk8-scm    |\n" +
					"| debian-jdk8-ui  | debian:jdk8-ui     |\n" +
					"| debian-local    | debian:local       |\n" +
					"| demo            | demo               |\n" +
					"| eclipse         | eclipse            |\n" +
					"| gatling         | gatling            |\n" +
					"| grafana         | grafana            |\n" +
					"| idea            | idea               |\n" +
					"| influxdb        | influxdb           |\n" +
					"| jmxtrans        | jmxtrans           |\n" +
					"| libreoffice     | libreoffice        |\n" +
					"| mailcatcher     | common/mailcatcher |\n" +
					"| metrics-jdk6    | metrics:jdk6       |\n" +
					"| netbeans        | netbeans           |\n" +
					"| petclinic       | spring/petclinic   |\n" +
					"| telegraf        | telegraf           |\n" +
					"+-----------------+--------------------+\n";

	private static final String EXPECTED_LIST =
			"+-----------------+\n" +
					"| activemq        |\n" +
					"| debian-jdk6     |\n" +
					"| debian-jdk7     |\n" +
					"| debian-jdk7-ui  |\n" +
					"| debian-jdk8     |\n" +
					"| debian-jdk8-scm |\n" +
					"| debian-jdk8-ui  |\n" +
					"| debian-local    |\n" +
					"| gatling         |\n" +
					"| grafana         |\n" +
					"| idea            |\n" +
					"| influxdb        |\n" +
					"| jmxtrans        |\n" +
					"| libreoffice     |\n" +
					"| mailcatcher     |\n" +
					"| metrics-jdk6    |\n" +
					"| netbeans        |\n" +
					"| petclinic       |\n" +
					"| telegraf        |\n" +
					"+-----------------+\n";

	@Test
	public void testPrintMap() throws Exception {
		final Map<String, String> m = new HashMap<>();
		m.put("activemq", "activemq");
		m.put("debian-jdk6", "debian:jdk6");
		m.put("debian-jdk7", "debian:jdk7");
		m.put("debian-jdk7-ui", "debian:jdk7-ui");
		m.put("debian-jdk8", "debian:jdk8");
		m.put("debian-jdk8-scm", "debian:jdk8-scm");
		m.put("debian-jdk8-ui", "debian:jdk8-ui");
		m.put("debian-local", "debian:local");
		m.put("demo", "demo");
		m.put("eclipse", "eclipse");
		m.put("gatling", "gatling");
		m.put("grafana", "grafana");
		m.put("idea", "idea");
		m.put("influxdb", "influxdb");
		m.put("jmxtrans", "jmxtrans");
		m.put("libreoffice", "libreoffice");
		m.put("mailcatcher", "common/mailcatcher");
		m.put("metrics-jdk6", "metrics:jdk6");
		m.put("netbeans", "netbeans");
		m.put("petclinic", "spring/petclinic");
		m.put("telegraf", "telegraf");

		Printable.printMap(null);
		Printable.printMap(m);

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Printable.printMap(m, "key", "value", new PrintStream(baos));
		assertEquals(EXPECTED_MAP, baos.toString());
	}

	@Test
	public void testPrintColl() throws Exception {
		final List<String> l = new ArrayList<>();
		l.add("activemq");
		l.add("debian-jdk6");
		l.add("debian-jdk7");
		l.add("debian-jdk7-ui");
		l.add("debian-jdk8");
		l.add("debian-jdk8-scm");
		l.add("debian-jdk8-ui");
		l.add("debian-local");
		l.add("gatling");
		l.add("grafana");
		l.add("idea");
		l.add("influxdb");
		l.add("jmxtrans");
		l.add("libreoffice");
		l.add("mailcatcher");
		l.add("metrics-jdk6");
		l.add("netbeans");
		l.add("petclinic");
		l.add("telegraf");

		Printable.printColl(null);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Printable.printColl(l, null, new PrintStream(baos));
		assertEquals(EXPECTED_LIST, baos.toString());
	}

	@Test
	public void testIsNumeric() throws Exception {
		assertFalse(Printable.isNumeric(null));
		assertFalse(Printable.isNumeric("test"));
		assertTrue(Printable.isNumeric("1"));
	}

	@Test
	public void testConvert() throws Exception {
		assertNull(Printable.convert(null));
		assertNull(Printable.convert("test"));
		assertEquals(new Integer(1), Printable.convert("1"));
	}

	@Test
	public void testSanitize() throws Exception {
		final String test = "docker save debian:latest > debian:latest.tar";
		assertEquals("docker save debian:latest > debian-latest.tar", Printable.sanitize(test));

		final String testWithSlash = "docker save common/test:latest > common/test:latest.tar";
		assertEquals("docker save common/test:latest > common-test-latest.tar", Printable.sanitize(testWithSlash));

		final String testWithMultiSlash = "docker save foo/bar/test:latest > foo/bar/test:latest.tar";
		assertEquals("docker save foo/bar/test:latest > foo-bar-test-latest.tar", Printable.sanitize(testWithMultiSlash));
	}
}