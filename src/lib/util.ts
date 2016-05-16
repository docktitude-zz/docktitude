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
import { Callback, Indexed, Map } from "./common";

// #############################################################################

const spawn = require("child_process").spawn;
const execSync = require("child_process").execSync;

// #############################################################################

export function print(t: any): void {
    process.stdout.write(t);
}

export function println(...params: any[]): void {
    console.log(params.join(constant.EMPTY));
}

export function printArray(array: string[], prefix: string = constant.EMPTY): void {
    for (let s of array) {
        console.log(`${prefix}${s}`);
    }
}

export function printPadLeft(s: string, n: number): void {
    println(repeat(constant.SPACE, n), s);
}

export function printPadRight(s: string, n: number): void {
    println(s, repeat(constant.SPACE, n));
}

export function fmt(s: string, ...params: string[]): void {
    console.log(s, params);
}

export function fmtString(s: string, param: string): string {
    return s.replace(constant.STRING_FMT, param);
}

export function padLeft(s: string, n: number): string {
    return repeat(constant.SPACE, n) + s;
}

export function padRight(s: string, n: number): string {
    return s + repeat(constant.SPACE, n);
}

export function repeat(s: string, n: number): string {
    if (n <= 0) {
        return constant.EMPTY;
    }
    return Array(n + 1).join(s);
}

export function isEmpty(s: string): boolean {
    return ((s != null) ? s.trim().length === 0 : true);
}

export function containsString(searchStr: string, str: string): boolean {
    return (str.indexOf(searchStr) >= 0);
}

export function contains<T>(t: T, array: T[]): boolean {
    return containsAny(t, [array]);
}

export function containsAny<T>(t: T, arrays: T[][]): boolean {
    let b: boolean = false;
    arrays.some(array => {
        if (array.indexOf(t) >= 0) {
            b = true;
        }
        return b;
    });
    return b;
}

export function quote(s: string): string {
    return `'${s}'`;
}

export function isNumeric(s: string): boolean {
    if ((s != null) && (s.trim().length > 0)) {
        return !isNaN(parseFloat(s));
    }
    return false;
}

export function stringValues<T extends Indexed>(map: Map<T[]>): string[] {
    const array: string[] = [];
    let curNodes: T[];
    for (let k of Object.keys(map)) {
        curNodes = map[k];
        for (let e of curNodes) {
            array.push(e.index);
        }
    }
    return array;
}

export function getCurrentWorkingDir(): string {
    return runSyncString("pwd");
}

export function runSync(command: string, echo: boolean = false, exitOnError: boolean = true): void {
    if (echo) {
        println(`${constant.NL}$> ${command}`);
    }
    try {
        execSync(command, { stdio: [0, 1, 2] });
    }
    catch (e) {
        if (exitOnError) {
            process.exit(1);
        }
    }
}

export function runSyncString(command: string, exitOnError: boolean = true): string {
    let output: string;
    try {
        output = execSync(command).toString().trim();
    }
    catch (e) {
        if (exitOnError) {
            process.exit(1);
        }
    }
    return output;
}

export function runAsync(command: string, ...callbacks: Callback<string[]>[]): void {
    const p = spawn("bash", ["-c", command]);

    const lines: string[] = [];
    p.stdout.on("data", (data: any) => {
        const frags: string[] = data.toString().split(constant.NL);
        for (let s of frags) {
            if (s.trim().length > 0) {
                lines.push(s);
            }
        }
    });

    handleError(p);

    p.on("close", (code: any) => {
        callbacks.forEach(f => f(lines));
    });
}

export function apply<T>(...callbacks: Callback<T>[]): void {
    callbacks.forEach(c => c.apply(null));
}

function handleError(t: any): void {
    t.stderr.on("data", (data: any) => {
        println(`${constant.DOCKTITUDE}: ${data}`);
        process.exit(1);
    });
}

// #############################################################################