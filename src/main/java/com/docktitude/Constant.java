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

public interface Constant {

	String DOCKTITUDE = "docktitude";
	String DOCKERFILE = "Dockerfile";
	String USAGE = "docktitude [--help] <command> [<args>]\n\nCommands:";

	String VERSION = "version";
	String DEFAULT_IMG_TAG = "latest";

	String MAINTAINER = "MAINTAINER";
	String PARENT_PATTERN = "FROM ";

	String SCRIPT_TAG = "#@";
	String BEGIN_TEMPLATE_SCRIPT = "[--DOCKTITUDE-SCRIPT";
	String END_TEMPLATE_SCRIPT = "DOCKTITUDE-SCRIPT--]";

	String SECTION = "------------------------------";

	String CTX = "CONTEXT";
	String CTX_IMG_TAG = "IMAGE TAG";
	String CTX_FILE = "+++ %s%n";
	String CTX_SCRIPT = "+++ %s [ SHELL SCRIPT ]%n";
	String CTX_PATH = "[%s]%n";
	String CTX_REPORT = "Docker contexts: %s";

	String FMT2 = "%s%s";
	String FMT21 = "%s %s";
	String FMT22 = "\"%s\" %s";
	String FMT3 = "%s%s%s";

	String EMPTY = "";
	String SPACE = " ";
	String SLASH = "/";
	String COLON = ":";
	String DASH = "-";
	String UNDERSCORE = "_";
	String DOT = ".";
	String PLUS = "+";
	String STAR = "*";
	String QMK = "?";

	String TAR_EXTENSION = ".tar";

	String USELESS_IMAGE = "<none>:<none>";
	String IMG_HISTORY_ERROR = "Err";

	interface Msg {
		String NO_CTX = "!!! NO DOCKER CONTEXT FOUND IN THE CURRENT DIRECTORY !!!";
		String DUPLICATED_CTX = "!!! DUPLICATED DOCKER CONTEXTS FOUND !!!";

		String NO_DOCKER_DAEMON = "Cannot connect to the Docker daemon";
		String NO_PRIVILEGE = "An error occurred. Are you root? Use sudo to run a command with root privileges.";

		String UPGRADE_REQUIRED = "parent image updated [%s] | Upgrade required";
		String PARENT_NOT_FOUND = "<<parent image>> not found !";
		String IMAGE_NOT_FOUND = "image not available | Build OK ?";

		String NOTHING_TO_REPORT = "nothing to upgrade";

		String NO_IMG = "!!! NO DOCKER IMAGE FOUND !!!";
		String PROMPT_IMG = "Image to save [1-9]*: ";
		String PROMPT_NO_ENTRY = "No entry for: %s";

		String GENERIC_ERROR = "Unable to execute this command ... An error occurred.";
		String NULL = "NULL";
	}

	interface Tree {
		int TAB = 4;
		String BRANCH0 = "\u2502";
		String BRANCH1 = "\u2514\u2500\u2500";
		String BRANCH2 = "\u251C\u2500\u2500";
	}

	interface Table {
		String BORDER1 = "+%s+";
		String BORDER2 = "+%s+%s+";
		String COLS1 = "| %s |";
		String COLS2 = "| %s | %s |";
	}
}
