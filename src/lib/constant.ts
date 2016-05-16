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

// *****************************************************************************

export const EMPTY: string = "";
export const SPACE: string = " ";
export const DASH: string = "-";
export const DOT: string = ".";
export const COLON: string = ":";
export const UNDERSCORE: string = "_";
export const QMK: string = "?";
export const SLASH: string = "/";
export const NL: string = "\n";

export const PLUS: string = "+";
export const PERCENT: string = "%";
export const ARROW: string = "=>";
export const COLLAPSE: string = "[-]";
export const ZERO: string = "0";

export const STRING_FMT: string = "%s";
export const ENCODING_UTF8: string = "utf8";
export const EOF: string = "EOF";

export const DOCKTITUDE: string = "docktitude";
export const VERSION: string = "version";

export const SEE_HELP: string = `${DOCKTITUDE}: %s is not a ${DOCKTITUDE} command. See '${DOCKTITUDE} help'.`;
export const HELP_OPTS: string[] = ["help", "--help", "-h"];
export const VERSION_OPTS: string[] = ["--version", "-v"];

export const ALINEA: string = "+++";
export const CTX: string = "CONTEXT";
export const CTX_IMG_TAG: string = "IMAGE TAG";
export const INFO_COL1: string = "BASE IMAGE";
export const INFO_COL2: string = "DISTRIBUTION";

export const PROMPT_IMG: string = "Image to save (choose a number)> ";

export const DOCKERFILE: string = "Dockerfile";
export const DEFAULT_TAG: string = "latest";
export const PARENT_PATTERN: string = "FROM ";
export const GHOST_IMAGE: string = "<none>:<none>";
export const IMG_HISTORY_ERROR: string = "Err";

export const SCRIPT_NAME_SUFFIX: string = "[ SHELL SCRIPT ]";
export const SCRIPT_TAG: string = "#@";
export const BEGIN_TEMPLATE_SCRIPT: string = "[--DOCKTITUDE-SCRIPT";
export const END_TEMPLATE_SCRIPT: string = "DOCKTITUDE-SCRIPT--]";

export const SECTION: string = "------------------------------";

export const UPGRADE_REQUIRED: string = "parent image updated [%s] | Upgrade required";
export const PARENT_NOT_FOUND: string = "<<parent image>> not found !";
export const IMAGE_NOT_FOUND: string = "image not available | Build OK ?";
export const NOTHING_TO_REPORT: string = "nothing to upgrade";

export const MISSING_ARGUMENT: string = "Missing argument !";
export const CTX_NOT_FOUND: string = `Docker context not found ! See '${DOCKTITUDE} info'.`;
export const DUPLICATED_CTX: string = "Duplicated docker context found !";
export const NO_CTX: string = "No Docker context found in the current directory !";
export const NO_ENTRY_FOUND: string = "No entry found for '%s' !";
export const NO_SCRIPT_DEFINED: string = `No ${DOCKTITUDE} script tags defined !`;
export const NO_PERM: string = `${DOCKTITUDE}: Are you root?`;

export const DEFAULT_COL_PADDING: number = 2;
export const ARGS_PROCESS_START_INDEX: number = 2;