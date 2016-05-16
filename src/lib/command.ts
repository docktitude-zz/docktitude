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

import constant = require("./constant");
import util = require("./util");

// #############################################################################

interface Option {
    command: Command;
    desc: string;
    argName?: string;
}

// #############################################################################

export enum Command {
    build, clean, config, export, info, op, play, print, script, snapshot, status, tree, update, upgrade, version
}

export class Usage {

    private options: Option[] = [];
    private maxNameLength: number = 0;
    private maxArgNameLength: number = 0;

    constructor(private usage: string) {
    }

    add(command: Command, desc: string, argName?: string): void {
        this.options.push({
            command: command,
            desc: desc,
            argName: argName
        });

        if (len(command) > this.maxNameLength) {
            this.maxNameLength = len(command);
        }
        if ((argName != null) && (argName.length > this.maxArgNameLength)) {
            this.maxArgNameLength = argName.length;
        }
    }

    display(): void {
        util.println(this.usage);

        let argName: string = constant.EMPTY;
        let padding: number = 0;
        let multiLinesDesc: string[];

        this.options.forEach(o => {
            argName = tidyArg(o.argName);
            padding = this.computePadding(o.command, argName);
            multiLinesDesc = null;
            if (o.desc.indexOf(constant.NL) > 0) {
                multiLinesDesc = o.desc.split(constant.NL);
            }
            if (multiLinesDesc == null) {
                util.println(`${util.padLeft(stringify(o.command), 3)} ${argName}${util.padLeft(o.desc, padding)}`);
            }
            else {
                const line: string = `${util.padLeft(stringify(o.command), 3)} ${util.padRight(argName, padding)}`;
                util.println(line, multiLinesDesc[0]);
                for (let n: number = 1; n < multiLinesDesc.length; n++) {
                    util.printPadLeft(multiLinesDesc[n], line.length);
                }
            }
        });
    }

    contains(command: string): boolean {
        let b: boolean = false;
        this.options.some(o => {
            if (stringify(o.command) === command) {
                b = true;
            }
            return b;
        });
        return b;
    }

    private computePadding(command: Command, argName: string): number {
        return this.maxNameLength + this.maxArgNameLength + 3 - (len(command) + argName.length);
    }
}

// #############################################################################

function stringify(c: Command): string {
    return Command[c];
}

function len(c: Command): number {
    return stringify(c).length;
}

function tidyArg(s: string): string {
    return (s != null) ? `<${s}>` : constant.EMPTY;
}

// #############################################################################