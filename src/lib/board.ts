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

import c = require('./constant');
import util = require('./util');
import { BiConsumer, Callback, Indexed, Map } from './common';

//#############################################################################

interface MetaData {
    kMax: number;
    vMax: number;
    detailed: boolean;
    keys: string[];
}

//#############################################################################

export function print1<V extends Indexed>(map: Map<V>, legend?: string, printer: Callback<string> = util.println): void {
    const md: MetaData = computeColsWidth(map, legend);
    const border: string = `+${util.repeat(c.DASH, md.kMax + c.DEFAULT_COL_PADDING)}+`;

    const filler: Callback<string> = (k: string) => {
        printer(`| ${k + util.repeat(c.SPACE, md.kMax - k.length)} |`);
    }

    printer(border);
    for (let key of md.keys) {
        filler(key);
    }
    printer(border);
    if (md.detailed) {
        filler(legend);
        printer(border);
    }
}

export function print2<V extends Indexed>(map: Map<V>, kTitle?: string, vTitle?: string, printer: Callback<string> = util.println): void {
    const md: MetaData = computeColsWidth(map, kTitle, vTitle);
    const border: string = `+${util.repeat(c.DASH, md.kMax + c.DEFAULT_COL_PADDING)}+${util.repeat(c.DASH, md.vMax + c.DEFAULT_COL_PADDING)}+`;

    const filler: BiConsumer<string, string> = (k: string, v: string) => {
        printer(`| ${k + util.repeat(c.SPACE, md.kMax - k.length)} | ${v + util.repeat(c.SPACE, md.vMax - v.length)} |`);
    }

    if (md.detailed) {
        printer(border);
        filler(kTitle, vTitle);
    }
    printer(border);
    for (let key of md.keys) {
        filler(key, map[key].index);
    }
    printer(border);
}

function computeColsWidth<V extends Indexed>(map: Map<V>, kTitle?: string, vTitle?: string): MetaData {
    const keys: string[] = [];
    let kMax: number = 0;
    let vMax: number = 0;

    for (let key of Object.keys(map)) {
        keys.push(key);

        if (key.length > kMax) {
            kMax = key.length;
        }
        if ((map[key].index != null) && (map[key].index.length > vMax)) {
            vMax = map[key].index.length;
        }
    }

    if ((kTitle != null) && (kTitle.length > kMax)) {
        kMax = kTitle.length;
    }
    if ((vTitle != null) && (vTitle.length > vMax)) {
        vMax = vTitle.length;
    }

    return {
        kMax: kMax,
        vMax: vMax,
        detailed: (kTitle != null) || (vTitle != null),
        keys: util.isNumeric(keys[0]) ? keys : keys.sort()
    };
}