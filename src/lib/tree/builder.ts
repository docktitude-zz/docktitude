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

//*****************************************************************************

import constant = require('./constant');
import util = require('../util');
import { Callback, Indexed, StringKeyMap } from '../common';
import { TreeDataHolder } from './dataholder';

//#############################################################################

export class TreeBuilder<T extends Indexed> {

    private lineConsumer: Callback<string[]>;
    private decorationsByNode: StringKeyMap<String>;

    private leftPadding: string;
    private rightPadding: string;

    constructor(private treeDataHolder: TreeDataHolder<T>, callback?: Callback<string[]>) {
        if (callback != null) {
            this.lineConsumer = callback;
        }
        else {
            this.lineConsumer = (line: string[]) => {
                util.println(line.join(constant.EMPTY));
            }
        }
        this.decorationsByNode = {};
        this.leftPadding = util.repeat(constant.SPACE, constant.TAB_SIZE);
        this.rightPadding = `${constant.BRANCH0}${util.repeat(constant.SPACE, constant.TAB_SIZE - constant.BRANCH0.length)}`;
    }

    build(): void {
        if (!this.treeDataHolder.isEmpty()) {
            const line: string[] = [constant.SEED];
            this.lineConsumer(line);
            for (let node of this.treeDataHolder.roots) {
                this.decorationsByNode[node.index] = constant.STAR;
            }
            this.appendTreeNodes(this.treeDataHolder.roots, 0, line);
        }
    }

    private appendTreeNodes(
        nodes: T[],
        level: number,
        prevLine: string[],
        index: number[] = [1]): void {

        const nbNodes: number = nodes.length;
        let line: string[];

        for (let node of nodes) {
            line = [];
            if (level > 0) {
                for (let i: number = 0; i < prevLine.length - 1; i++) {
                    if (this.containsAny([constant.BRANCH0, constant.BRANCH2], prevLine[i].trim())) {
                        line.push(this.rightPadding);
                    }
                    else {
                        line.push(this.leftPadding);
                    }
                }
            }
            line.push(((nbNodes > 1) && (index[0] !== nbNodes)) ? constant.BRANCH2 : constant.BRANCH1);
            line.push(` ${node.index}${this.getDecoration(node)}`);

            this.lineConsumer(line);
            index[0]++;

            const subNodes: T[] = this.treeDataHolder.getNodes(node);
            if (subNodes != null) {
                this.appendTreeNodes(subNodes, level + 1, line);
            }
        }
    }

    private containsAny(tokens: string[], lineElement: string): boolean {
        for (let token of tokens) {
            if (util.containsString(token, lineElement)) {
                return true;
            }
        }
        return false;
    }

    private getDecoration(node: T): string {
        if (this.decorationsByNode[node.index] != null) {
            return constant.STAR;
        }
        return constant.EMPTY;
    }
}

//#############################################################################