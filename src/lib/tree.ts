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
import { Callback, Indexed, StringKeyMap } from "./common";
import { TreeDataHolder } from "./tree/dataholder";
import { TreeBuilder } from "./tree/builder";

import fs = require("fs");
import path = require("path");

// #############################################################################

export interface Context extends Indexed {
    name: string;
    paths: string[];
    tag?: string;
    parent?: string;
}

// #############################################################################

export function print(): void {
    const f = (t: TreeDataHolder<Context>) => {
        const builder: TreeBuilder<Context> = new TreeBuilder(t);
        builder.build();
    };
    generateTreeData(f);
}

export function findContexts(callback: Callback<StringKeyMap<Context>>): void {
    checkSearchDepth();

    const currentWorkingDir: string = util.getCurrentWorkingDir();
    const contextsByName: StringKeyMap<Context> = {};

    const read = (paths: string[]): void => {
        let curCtx: string;
        let fullpath: string;

        for (let p of paths) {
            curCtx = path.basename(p);
            fullpath = path.join(currentWorkingDir, p);

            if (contextsByName[curCtx] != null) {
                contextsByName[curCtx].paths.push(fullpath);
            }
            else {
                const curTag: string = computeTag(currentWorkingDir, fullpath);
                contextsByName[curCtx] = {
                    name: curCtx,
                    paths: [fullpath],
                    tag: curTag,
                    index: curTag
                };
            }
        }

        const names: string[] = Object.keys(contextsByName);
        if (names.length === 0) {
            util.println(constant.NO_CTX);
            process.exit(0);
        }
        for (let ctx of names) {
            if (contextsByName[ctx].paths.length > 1) {
                util.println(`${constant.DUPLICATED_CTX}${constant.NL}${constant.ARROW} ${ctx}`);
                util.printArray(contextsByName[ctx].paths, constant.COLLAPSE + constant.SPACE);
                process.exit(1);
            }
        }
        return callback(contextsByName);
    };

    util.runAsync("find . -type f -name 'Dockerfile' | awk -F '.' '{idx=index($0,\".\"); print substr($0,idx+1)}' | awk -F 'Dockerfile' '{print $1}'", read);
}

export function generateTreeData(callback: Callback<TreeDataHolder<Context>>): void {
    const nodesByParentId: StringKeyMap<Context[]> = {};
    const roots: Context[] = [];

    const computeHierarchy = (contextsByName: StringKeyMap<Context>) => {
        for (let name of Object.keys(contextsByName)) {
            contextsByName[name].parent = getParentImage(name, contextsByName[name].paths[0]);
        }
        return computeNodes(contextsByName);
    };

    const computeNodes = (contextsByName: StringKeyMap<Context>) => {
        let curParentId: string;
        for (let name of Object.keys(contextsByName)) {
            curParentId = contextsByName[name].parent;
            if (nodesByParentId[curParentId] != null) {
                nodesByParentId[curParentId].push(contextsByName[name]);
            }
            else {
                nodesByParentId[curParentId] = [contextsByName[name]];
            }
        }
        return callback(new TreeDataHolder(nodesByParentId));
    };

    findContexts(computeHierarchy);
}

export function checkSearchDepth(): void {
    const output: string = util.runSyncString("find . -mindepth 2 -maxdepth 2 -type f -name 'Dockerfile' | wc -l");
    if (output === constant.ZERO) {
        util.println(constant.NO_CTX);
        process.exit(0);
    }
}

function computeTag(currentWorkingDir: string, dirpath: string): string {
    const f = (s: string): string => {
        return s.replace(constant.DASH, constant.COLON).replace(constant.UNDERSCORE, constant.DASH);
    };
    const parentDir: string = path.dirname(dirpath);
    if (currentWorkingDir === parentDir) {
        return f(path.basename(dirpath));
    }
    return f(path.join(parentDir.replace(currentWorkingDir + path.sep, constant.EMPTY), path.basename(dirpath)));
}

function getParentImage(ctx: string, dirpath: string): string {
    let parent: string = constant.QMK;
    fs.readFileSync(path.join(dirpath, constant.DOCKERFILE), constant.ENCODING_UTF8).split(constant.NL).some((line: string) => {
        if (line.indexOf(constant.PARENT_PATTERN) >= 0) {
            const index: number = line.indexOf(constant.PARENT_PATTERN) + constant.PARENT_PATTERN.length;
            parent = line.substr(index);
            return true;
        }
        return false;
    });
    return parent;
}

// #############################################################################