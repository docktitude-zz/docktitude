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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.Arrays;
import java.util.List;

public final class CustomParser extends DefaultParser {
	private static List<String> DEFAULT_OPTS = Arrays.asList("-help", "--help");

	private final List<String> ignoredUnrecognizedOptions;

	public CustomParser() {
		this(DEFAULT_OPTS);
	}

	private CustomParser(List<String> ignoredUnrecognizedOptions) {
		this.ignoredUnrecognizedOptions = ignoredUnrecognizedOptions;
	}

	@Override
	public CommandLine parse(Options options, String[] arguments) throws ParseException {
		if ((arguments.length > 0) && ignoredUnrecognizedOptions.contains(arguments[0])) {
			return super.parse(options, new String[]{DEFAULT_OPTS.get(0)});
		}
		return super.parse(options, useSmartOptions(arguments));
	}

	@Override
	protected void handleConcatenatedOptions(String token) throws ParseException {
		if (ignoredUnrecognizedOptions.contains(token)) return;
		super.handleConcatenatedOptions(token);
	}

	private String[] useSmartOptions(String[] args) {
		if ((args.length > 0) && !args[0].startsWith(Constant.DASH)) {
			args[0] = Constant.DASH + args[0];
		}
		return args;
	}
}
