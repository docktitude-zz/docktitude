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

import { Callback, Indexed, StringKeyMap } from "../lib/common";
import { TreeDataHolder } from "../lib/tree/dataholder";
import { TreeBuilder } from "../lib/tree/builder";

// #############################################################################

const expect = require("chai").expect;

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

describe("Tree Builder Unit Tests:", () => {

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

    const treeDataHolder: TreeDataHolder<Indexed> = new TreeDataHolder(util.indexMap(nodesByParentId));

    const content: string[] = [];
    const lineConsumer: Callback<string[]> = (line: string[]) => {
        content.push(line.join(constant.EMPTY));
    };
    const treeBuilder: TreeBuilder<Indexed> = new TreeBuilder(treeDataHolder, lineConsumer);

    describe("docktitude tree", () => {
        it("should be a tree of 20 nodes", (done) => {
            treeBuilder.build();
            expect(content.join(constant.NL)).to.equals(EXPECTED_TREE1);
            done();
        });
    });
});

// #############################################################################