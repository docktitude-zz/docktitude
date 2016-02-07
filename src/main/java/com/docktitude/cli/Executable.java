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

import com.docktitude.Constant;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Executable {

	static void execWithPattern(final String command, final String value) throws Exception {
		execWithPattern(true, command, value);
	}

	static void execWithPattern(final boolean showCommand, final String command, final String value) throws Exception {
		final List<String> params = new ArrayList<>(Command.SUDO);
		params.add(Printable.sanitize(command.replace(Constant.QMK, value)));
		exec(showCommand, params.toArray(new String[params.size()]));
	}

	static List<String> execWithPatternAndResults(final String command, final String value) throws Exception {
		final List<String> params = new ArrayList<>(Command.SUDO);
		params.add(Printable.sanitize(command.replace(Constant.QMK, value)));
		return execWithResultsAsList(params.toArray(new String[params.size()]));
	}

	static void exec(final String... command) throws Exception {
		exec(false, command);
	}

	static void exec(final boolean showCommand, final String... command) throws Exception {
		if (showCommand) {
			Printable.print(String.format(Constant.FMT21, Command.PREFIX, String.join(Constant.SPACE, command)));
		}
		final ProcessBuilder builder = new ProcessBuilder(command);
		builder.inheritIO();
		builder.start().waitFor();
	}

	static List<String> execWithResultsAsList(final String... command) throws Exception {
		return execWithResultsAsStream(command).collect(Collectors.toList());
	}

	static Stream<Path> execWithResultsAsPathStream(final String... command) throws Exception {
		return execWithResultsAsStream(command).map(Paths::get);
	}

	static Stream<String> execWithResultsAsStream(final String... command) throws Exception {
		final ProcessBuilder builder = new ProcessBuilder(command);
		final java.lang.Process p = builder.start();
		p.waitFor();

		final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		return reader.lines();
	}
}
