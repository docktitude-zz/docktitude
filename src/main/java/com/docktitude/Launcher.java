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
package com.docktitude;

import com.docktitude.cli.CustomParser;
import com.docktitude.cli.Engine;
import com.docktitude.cli.Printable;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.util.Arrays;
import java.util.function.Consumer;

public final class Launcher {

	private static final int HELP_WIDTH = 80;
	private static final int OPTION_PREFIX_WIDTH = 2;

	private static final String OPTION_ARG_V = "-v";

	private Launcher() {
	}

	public static void main(String[] args) {
		final CommandLineParser parser = new CustomParser();
		try {
			if (cleanWithArgs(args)) return;

			final Options options = OPTION.build();
			final CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption(OPTION.config.toString())) {
				Engine.printConfig();
			}
			else if (cmd.hasOption(OPTION.build.toString())) {
				final String ctx = cmd.getOptionValue(OPTION.build.toString());
				Engine.build(ctx);
			}
			else if (cmd.hasOption(OPTION.status.toString())) {
				Engine.printStatus();
			}
			else if (cmd.hasOption(OPTION.update.toString())) {
				Engine.update();
			}
			else if (cmd.hasOption(OPTION.upgrade.toString())) {
				Engine.upgrade();
			}
			else if (cmd.hasOption(OPTION.info.toString())) {
				Engine.printInfo();
			}
			else if (cmd.hasOption(OPTION.version.toString())) {
				Engine.printVersion();
			}
			else if (cmd.hasOption(OPTION.clean.toString())) {
				Engine.clean();
			}
			else if (cmd.hasOption(OPTION.print.toString())) {
				final String ctx = cmd.getOptionValue(OPTION.print.toString());
				Engine.printContext(ctx);
			}
			else if (cmd.hasOption(OPTION.script.toString())) {
				final String ctx = cmd.getOptionValue(OPTION.script.toString());
				Engine.printScript(ctx);
			}
			else if (cmd.hasOption(OPTION.snapshot.toString())) {
				Engine.snapshot();
			}
			else if (cmd.hasOption(OPTION.tree.toString())) {
				Engine.printTree();
			}
			else if (cmd.hasOption(OPTION.export.toString())) {
				Engine.export();
			}
			else if (cmd.hasOption(OPTION.op.toString())) {
				final String[] nameFragments = cmd.getOptionValues(OPTION.op.toString());
				Engine.changeMaintainer(nameFragments);
			}
			else {
				final HelpFormatter helpFormatter = new HelpFormatter();
				helpFormatter.setOptPrefix(Printable.repeat(Constant.SPACE, OPTION_PREFIX_WIDTH));
				helpFormatter.setWidth(HELP_WIDTH);
				helpFormatter.printHelp(Constant.USAGE, options);
			}
		}
		catch (Exception e) {
			final String message = e.getMessage();
			System.err.println(Constant.Msg.NULL.equalsIgnoreCase(message) ? Constant.Msg.GENERIC_ERROR : message);
		}
	}

	private static boolean cleanWithArgs(String[] args) throws Exception {
		if (args.length > 1 && args[0].equals(OPTION.clean.toString())) {
			if (OPTION_ARG_V.equalsIgnoreCase(args[1])) {
				Engine.clean(true);
				return true;
			}
		}
		return false;
	}

	private enum OPTION {

		build("Build context Docker image", "context"),
		clean("Remove exited Docker containers and useless images\nUse -v to remove the associated volumes"),
		config("List auto-configured Docker images building tags"),
		export("Export all contexts except binaries to a tar archive"),
		info("Show information relating to the Dockerfile files"),
		op("Change maintainer information in the Dockerfile files", "name", 2),
		print("Show context Dockerfile", "context"),
		script("Show shell script for defined docktitude script tags", "context"),
		snapshot("Display Docker images and save the selected one (.tar)"),
		status("Show local Docker images update status"),
		tree("List Docker images in a tree-like format"),
		update("Update external Docker images"),
		upgrade("Build cascade local Docker images"),
		version("Show version information");

		private final String desc;
		private final String arg;
		private final int occ;

		OPTION(final String desc) {
			this(desc, null, 0);
		}

		OPTION(final String desc, final String arg) {
			this(desc, arg, 1);
		}

		OPTION(final String desc, final String arg, final int occ) {
			this.desc = desc;
			this.arg = arg;
			this.occ = occ;
		}

		static Options build() {
			final Options options = new Options();

			final Consumer<OPTION> action = e -> {
				options.addOption(e.name(), e.desc);
				if (e.arg != null) {
					options.getOption(e.name()).setArgs(e.occ);
					options.getOption(e.name()).setArgName(e.arg);
					if (e.occ > 1) {
						options.getOption(e.name()).setOptionalArg(true);
					}
				}
			};
			Arrays.asList(OPTION.values()).stream().forEach(action);

			return options;
		}

		@Override
		public String toString() {
			return name();
		}
	}
}
