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

/// <reference path="../../typings/mocha/mocha.d.ts" />
/// <reference path="../../typings/chai/chai.d.ts" />

import constant = require("../lib/constant");
import util = require("../lib/util");

import { Callback, StringKeyMap } from "../lib/common";
import { TreeDataHolder } from "../lib/tree/dataholder";
import { TreeBuilder } from "../lib/tree/builder";

// #############################################################################

interface Indexed {
    index: string;
    parent?: string;
}

// #############################################################################

const assert = require("chai").assert;

const EXPECTED_TREE1: string =
    `.
├── alpine*
│   └── transmission
└── debian:latest*
    └── base/debian:build
        └── base/debian
            ├── base/debian:jdk7
            │   └── base/debian:jdk7-ui
            │       └── gatling
            ├── base/debian:jdk8
            │   ├── activemq
            │   └── base/debian:jdk8-ui
            │       ├── base/debian:jdk8-scm
            │       │   ├── idea
            │       │   └── netbeans
            │       └── libreoffice
            ├── chrome
            ├── clamav
            ├── grafana
            ├── influxdb
            └── telegraf`;

// #############################################################################

const nodesByParentId: StringKeyMap<string[]> = {};
nodesByParentId["base/debian:jdk7"] = ["base/debian:jdk7-ui"];
nodesByParentId["base/debian:jdk8"] = ["activemq", "base/debian:jdk8-ui"];
nodesByParentId["base/debian:jdk8-scm"] = ["idea", "netbeans"];
nodesByParentId["alpine"] = ["transmission"];
nodesByParentId["base/debian"] = ["base/debian:jdk7", "base/debian:jdk8", "chrome", "clamav", "grafana", "influxdb", "telegraf"];
nodesByParentId["base/debian:jdk7-ui"] = ["gatling"];
nodesByParentId["base/debian:build"] = ["base/debian"];
nodesByParentId["debian:latest"] = ["base/debian:build"];
nodesByParentId["base/debian:jdk8-ui"] = ["base/debian:jdk8-scm", "libreoffice"];

const treeDataHolder: TreeDataHolder<Indexed> = new TreeDataHolder(indexMap(nodesByParentId));

const content: string[] = [];
const lineConsumer: Callback<string[]> = (line: string[]) => {
    content.push(line.join(constant.EMPTY));
};
const treeBuilder: TreeBuilder<Indexed> = new TreeBuilder(treeDataHolder, lineConsumer);

describe("TreeDataHolder", () => {
    describe("constructor", () => {

        it("should have 2 roots", () => {
            assert.equal(treeDataHolder.roots.length, 2);
        });

        it("should return the first root", () => {
            assert.equal(treeDataHolder.roots[0].index, "alpine");
        });

        it("should return the second root", () => {
            assert.equal(treeDataHolder.roots[1].index, "debian:latest");
        });
    });

    describe("#isEmpty()", () => {
        it("should not be empty", () => {
            assert.isFalse(treeDataHolder.isEmpty());
        });
    });

    describe("#getNodes(..)", () => {
        it("should be null", () => {
            assert.isNull(treeDataHolder.getNodes(index("test")));
        });

        it("should return a node", () => {
            assert.deepEqual(treeDataHolder.getNodes(index("alpine")), [index("transmission", "alpine")]);
        });
    });

    describe("#getParent(..)", () => {
        it("should be null", () => {
            assert.isNull(treeDataHolder.getParent(index("test")));
        });

        it("should return a parent", () => {
            assert.deepEqual(treeDataHolder.getParent(index("clamav", "base/debian")), { index: "base/debian" });
        });
    });
});

describe("TreeBuilder", () => {
    describe("#build()", () => {
        treeBuilder.build();
        it("should build a tree", () => {
            assert.equal(content.join(constant.NL), EXPECTED_TREE1);
        });
    });
});

// #############################################################################

function indexMap(map: StringKeyMap<string[]>): StringKeyMap<Indexed[]> {
    const newMap: StringKeyMap<Indexed[]> = {};
    for (let k of Object.keys(map)) {
        newMap[k] = indexArray(k, map[k]);
    }
    return newMap;
}

function indexArray(key: string, array: string[]): Indexed[] {
    const newArray: Indexed[] = [];
    for (let i: number = 0; i < array.length; i++) {
        newArray[i] = index(array[i], key);
    }
    return newArray;
}

function index(name: string, parent?: string): Indexed {
    return {
        index: name,
        parent: parent,
    };
}

// #############################################################################