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

import util = require("../util");
import { Callback, Indexed, StringKeyMap } from "../common";

// #############################################################################

export class TreeDataHolder<T extends Indexed> {
    constructor(public roots: T[], private nodesByParent: StringKeyMap<T[]>) {
        TreeDataHolder.sort(roots, nodesByParent);
    }

    isEmpty(): boolean {
        return ((this.roots != null) ? (this.roots.length === 0) : true);
    }

    getNodes(parent: T): T[] {
        if (this.nodesByParent[parent.index] != null) {
            return this.nodesByParent[parent.index];
        }
        return null;
    }

    getParent(node: T): Indexed {
        for (let parent of Object.keys(this.nodesByParent)) {
            if (util.contains(node, this.nodesByParent[parent])) {
                return { index: parent };
            }
        }
        return null;
    }

    walk(func: Callback<T>): void {
        TreeDataHolder.walk(this.roots, this.nodesByParent, func);
    }

    private static walk<T extends Indexed>(rootElements: T[], nodesByParent: StringKeyMap<T[]>, func: Callback<T>): void {
        rootElements.forEach(e => {
            func(e);
            const children: T[] = nodesByParent[e.index];
            if (children != null) {
                TreeDataHolder.walk(children, nodesByParent, func);
            }
        });
    }

    private static sort<T extends Indexed>(array: T[], map: StringKeyMap<T[]>): void {
        array.sort(TreeDataHolder.compare);
        for (let k of Object.keys(map)) {
            map[k].sort(TreeDataHolder.compare);
        }
    }

    private static compare<T extends Indexed>(t1: T, t2: T): number {
        if (t1.index > t2.index) {
            return 1;
        }
        if (t1.index < t2.index) {
            return -1;
        }
        return 0;
    }
}

// #############################################################################