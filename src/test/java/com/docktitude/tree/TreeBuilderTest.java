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
package com.docktitude.tree;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class TreeBuilderTest {

	private static final String EXPECTED_TREE =
			".\n" +
					"├── alpine*\n" +
					"│   └── transmission\n" +
					"└── debian:latest*\n" +
					"    └── base/debian:build\n" +
					"        └── base/debian\n" +
					"            ├── base/debian:jdk7\n" +
					"            │   └── base/debian:jdk7-ui\n" +
					"            │       └── gatling\n" +
					"            ├── base/debian:jdk8\n" +
					"            │   ├── activemq\n" +
					"            │   └── base/debian:jdk8-ui\n" +
					"            │       ├── base/debian:jdk8-scm\n" +
					"            │       │   ├── idea\n" +
					"            │       │   └── netbeans\n" +
					"            │       └── libreoffice\n" +
					"            ├── chrome\n" +
					"            ├── clamav\n" +
					"            ├── grafana\n" +
					"            ├── influxdb\n" +
					"            └── telegraf\n";

	@Test
	public void testBuild() throws Exception {
		final Map<String, List<String>> m = new HashMap<>();
		m.put("base/debian:jdk7", Collections.singletonList("base/debian:jdk7-ui"));
		m.put("base/debian:jdk8", Arrays.asList("activemq", "base/debian:jdk8-ui"));
		m.put("base/debian:jdk8-scm", Arrays.asList("idea", "netbeans"));
		m.put("alpine", Collections.singletonList("transmission"));
		m.put("base/debian", Arrays.asList("base/debian:jdk7", "base/debian:jdk8", "chrome",
				"clamav", "grafana", "influxdb", "telegraf"));
		m.put("base/debian:jdk7-ui", Collections.singletonList("gatling"));
		m.put("base/debian:build", Collections.singletonList("base/debian"));
		m.put("debian:latest", Collections.singletonList("base/debian:build"));
		m.put("base/debian:jdk8-ui", Arrays.asList("base/debian:jdk8-scm", "libreoffice"));

		final TreeDataHolder<String> treeDataHolder = new TreeDataHolder<>(Arrays.asList("alpine", "debian:latest"), m);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final TreeBuilder tb = new TreeBuilder(treeDataHolder, new PrintStream(baos));
		tb.build();

		assertEquals(EXPECTED_TREE, baos.toString());
	}
}