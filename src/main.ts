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

import constant = require("./lib/constant");
import util = require("./lib/util");
import tree = require("./lib/tree");
import board = require("./lib/board");
import { TreeDataHolder } from "./lib/tree/dataholder";
import { Command, Usage } from "./lib/command";
import { BiConsumer, Callback, Indexed, NumericKeyMap, StringKeyMap } from "./lib/common";

import fs = require("fs");
import path = require("path");
import readline = require("readline");

// #############################################################################

function parseArgs(): void {
    const args: string[] = process.argv.slice(constant.ARGS_PROCESS_START_INDEX);
    const usage: Usage = buildUsage();

    if (args.length === 0 || usage.contains(args[0]) || util.containsAny(args[0], [constant.HELP_OPTS, constant.VERSION_OPTS])) {
        const index: number = util.contains(args[0], constant.VERSION_OPTS) ? -1 : Command[args[0]];
        switch (index) {
            case Command.build: { build(checkArg(args)); break; }
            case Command.clean: { clean(args.length > 1 && args[1] === "-v"); break; }
            case Command.config: { printConfig(); break; }
            case Command.export: { exportContexts(); break; }
            case Command.info: { printInfo(); break; }
            case Command.op: { changeMaintainer(checkArg(args, true)); break; }
            case Command.play: { runScript(checkArg(args)); break; }
            case Command.print: { printContext(checkArg(args)); break; }
            case Command.script: { printScript(checkArg(args)); break; }
            case Command.snapshot: { snapshot(); break; }
            case Command.status: { printStatus(); break; }
            case Command.tree: { tree.print(); break; }
            case Command.update: { update(); break; }
            case Command.upgrade: { upgrade(); break; }
            case Command.version: { printVersion(); break; }
            case -1: { printVersion(); break; }
            default: usage.display();
        }
    }
    else {
        util.fmt(constant.SEE_HELP, util.quote(args[0]));
    }
}

function printStatus(): void {
    const output: string = util.runSyncString("docker images | awk -F ' ' '{print $1\":\"$2\"+\"$3}' | awk 'NR != 1' | sort | uniq -u | grep -v '<none>:<none>'");

    const idsByImageTag: StringKeyMap<string> = {};
    output.split(constant.NL).forEach((value, index, array) => {
        const [tag, id]: string[] = value.split(constant.PLUS);
        idsByImageTag[tag] = id;
    });

    const f = (t: TreeDataHolder<tree.Context>) => {
        const statusByImageTag: StringKeyMap<Indexed> = {};

        t.walk((ctx: tree.Context) => {
            if (!util.contains(ctx, t.roots)) {
                let layersIds: string[] = [];
                if (idsByImageTag[normalize(ctx.index)] != null) {
                    layersIds = util.runSyncString(`docker history -q ${ctx.index}`).split(constant.NL);
                }
                if ((layersIds.length > 1) && (!util.containsString(constant.IMG_HISTORY_ERROR, layersIds[0]))) {
                    const parent: Indexed = t.getParent(ctx);
                    if (parent != null) {
                        const parentId: string = idsByImageTag[normalize(parent.index)];
                        if (!util.contains(parentId, layersIds)) {
                            statusByImageTag[ctx.index] = { index: util.fmtString(constant.UPGRADE_REQUIRED, parent.index) };
                        }
                    }
                    else {
                        statusByImageTag[ctx.index] = { index: constant.PARENT_NOT_FOUND };
                    }
                }
                else {
                    statusByImageTag[ctx.index] = { index: constant.IMAGE_NOT_FOUND };
                }
            }
        });
        if (Object.keys(statusByImageTag).length > 0) {
            board.print2(statusByImageTag);
        }
        else {
            util.println(constant.NOTHING_TO_REPORT);
        }
    };
    tree.generateTreeData(f);
}

function printScript(ctx: string): void {
    const f = (line: string): void => {
        if (line.indexOf(constant.EOF) !== 0) {
            util.println(line);
        }
    };
    useContext(ctx, useScript(ctx, f));
}

function runScript(ctx: string): void {
    const lines: string[] = [];

    const f = (line: string): void => {
        if (!util.isEmpty(line)) {
            const endOfScript: boolean = (line.indexOf(constant.EOF) === 0);
            if ((line.indexOf(constant.SECTION) !== 0) && (line.indexOf(constant.ALINEA) !== 0) && !endOfScript && (line.indexOf("#!/") !== 0)) {
                lines.push(line);
            }
            if (endOfScript) {
                util.runSync(lines.join(constant.NL), true);
            }
        }
    };
    useContext(ctx, useScript(ctx, f));
}

function useScript(ctx: string, callback: Callback<string>): BiConsumer<string, StringKeyMap<tree.Context>> {
    return (ctx: string, contextsByName: StringKeyMap<tree.Context>): void => {
        const filepath: string = path.join(contextsByName[ctx].paths[0], constant.DOCKERFILE);
        const fileContent: string[] = fs.readFileSync(filepath, constant.ENCODING_UTF8).split(constant.NL).filter((value, index, array) => {
            return (value.indexOf(constant.SCRIPT_TAG) === 0);
        });

        if ((fileContent.length > 2) &&
            (util.containsString(constant.BEGIN_TEMPLATE_SCRIPT, fileContent[0])) &&
            (util.containsString(constant.END_TEMPLATE_SCRIPT, fileContent[fileContent.length - 1]))) {

            callback(constant.SECTION);
            callback(`${constant.ALINEA} ${ctx} ${constant.SCRIPT_NAME_SUFFIX}`);
            callback(constant.SECTION);

            for (let i: number = 1; i < fileContent.length - 1; i++) {
                if (fileContent[i].indexOf(constant.SCRIPT_TAG + constant.SPACE) === 0) {
                    callback(fileContent[i].replace(constant.SCRIPT_TAG + constant.SPACE, constant.EMPTY));
                }
                else {
                    callback(fileContent[i].replace(constant.SCRIPT_TAG, constant.EMPTY));
                }
            }
            callback(constant.SECTION);
            callback(constant.EOF);
        }
        else {
            callback(constant.NO_SCRIPT_DEFINED);
        }
    };
}

function snapshot(): void {
    const output: string = util.runSyncString("docker images | awk -F ' ' '{print $1\":\"$2}' | awk 'NR != 1' | sort | uniq -u | grep -v '<none>:<none>'");
    const imagesByIndex: NumericKeyMap<Indexed> = {};
    output.split(constant.NL).forEach((value, index, array) => {
        imagesByIndex[index + 1] = { index: value };
    });
    board.print2(imagesByIndex);

    const rl: readline.ReadLine = readline.createInterface(process.stdin, process.stdout);
    rl.question(constant.PROMPT_IMG, (answer: string) => {
        if (imagesByIndex[Number(answer)] != null) {
            let img: string = imagesByIndex[Number(answer)].index;
            util.runSync(`docker save ${img} > ${img.replace(constant.SLASH, constant.DASH).replace(constant.COLON, constant.DASH)}.tar`);
        }
        else {
            util.fmt(constant.NO_ENTRY_FOUND, answer);
        }
        rl.close();
    });
}

function changeMaintainer(maintainer: string): void {
    if (maintainer != null) {
        tree.checkSearchDepth();
        util.runSync(`find . -type f -name 'Dockerfile' -exec sed -i 's/MAINTAINER.*/MAINTAINER ${maintainer}/' {} \\;`);
    }
    else {
        util.println(constant.MISSING_ARGUMENT);
    }
}

function exportContexts(): void {
    tree.findContexts((contextsByName: StringKeyMap<tree.Context>) => {
        // TODO Consider yml files as non binary
        util.runSync("find . -size -3000k -exec file {} \\; | grep text | cut -d: -f1 | tar -cJ -f export.tar.xz -T -");
    });
}

function printInfo(): void {
    const f = (t: TreeDataHolder<tree.Context>) => {
        const total: number = t.getNbNodes();

        const stats: StringKeyMap<number> = {};
        const statsAsString: StringKeyMap<Indexed> = {};

        for (let e of t.roots) {
            stats[e.index] = 0;
        }
        for (let id of Object.keys(t.nodesByParentId)) {
            stats[t.getRoot({ index: id }).index] += t.nodesByParentId[id].length;
        }
        for (let n of Object.keys(stats)) {
            statsAsString[n] = {
                index: util.fmtString(`%s ${constant.PERCENT}  (${stats[n]})`, ((stats[n] * 100) / total).toPrecision(4))
            };
        }
        board.print2(statsAsString, constant.INFO_COL1, `${constant.INFO_COL2} (${total})`);
    };
    tree.generateTreeData(f);
}

function printConfig(): void {
    tree.findContexts((contextsByName: StringKeyMap<tree.Context>) => {
        board.print2(contextsByName, constant.CTX, constant.CTX_IMG_TAG);
    });
}

function build(ctx: string): void {
    const f = (ctx: string, contextsByName: StringKeyMap<tree.Context>): void => {
        util.runSync(`docker build -t "${contextsByName[ctx].tag}" ${contextsByName[ctx].paths[0]}`, true);
    };
    useContext(ctx, f);
}

function readDockerfile(ctx: string, dirpath: string): void {
    const filepath: string = path.join(dirpath, constant.DOCKERFILE);

    const stream: fs.ReadStream = fs.createReadStream(filepath, {
        "encoding": constant.ENCODING_UTF8,
        "autoClose": true
    });

    util.println(constant.SECTION);
    util.println(`${constant.ALINEA} ${ctx}`);
    util.println(`[${filepath}]`);
    util.println(constant.SECTION);

    stream.on("data", (chunck: string): void => {
        util.print(chunck);
    });
    stream.on("end", () => {
        util.println(constant.SECTION);
    });
}

function printContext(ctx: string): void {
    const f = (ctx: string, contextsByName: StringKeyMap<tree.Context>): void => {
        readDockerfile(ctx, contextsByName[ctx].paths[0]);
    };
    useContext(ctx, f);
}

function useContext(ctx: string, func: BiConsumer<string, StringKeyMap<tree.Context>>): void {
    if (ctx != null) {
        tree.findContexts((contextsByName: StringKeyMap<tree.Context>) => {
            if (contextsByName[ctx] != null) {
                func(ctx, contextsByName);
            }
            else {
                util.println(constant.CTX_NOT_FOUND);
            }
        });
    }
    else {
        util.println(constant.MISSING_ARGUMENT);
    }
}

function upgrade(): void {
    const f = (t: TreeDataHolder<tree.Context>) => {
        t.walk((ctx: tree.Context) => {
            if ((ctx.paths != null) && (ctx.paths[0] != null)) {
                util.runSync(`docker build -t "${ctx.tag}" ${ctx.paths[0]}`, true);
            }
        });
    };
    tree.generateTreeData(f);
}

function update(): void {
    let registryImages: string[];

    const dumpImages = (images: string[]) => {
        registryImages = images.slice();
    };

    const updateImages = (contextsByName: StringKeyMap<tree.Context>) => {
        const localTags: string[] = [];

        for (let name of Object.keys(contextsByName)) {
            localTags.push(normalize(contextsByName[name].tag));
        }

        const filteredImages: string[] = registryImages.filter((value, index, array) => {
            return !util.contains(value, localTags);
        });

        for (let img of filteredImages) {
            util.runSync(`docker pull ${img}`, false, false);
        }
    };

    const findContexts = () => {
        tree.findContexts(updateImages);
    };

    util.runAsync("docker images | awk -F ' ' '{print $1\":\"$2}' | awk 'NR != 1' | sort | uniq -u | grep -v \"<none>:<none>\"",
        dumpImages,
        findContexts
    );
}

function normalize(imageTag: string): string {
    return (util.containsString(constant.COLON, imageTag) ? imageTag : `${imageTag}${constant.COLON}${constant.DEFAULT_TAG}`);
}

function clean(removeVolumes: boolean): void {
    const rm = (exitedContainers: string[]) => {
        if (exitedContainers.length > 0) {
            if (removeVolumes) {
                util.runSync("docker rm -v $(docker ps -a | grep 'Exited' | awk -F ' ' '{print $1}')");
            }
            else {
                util.runSync("docker rm $(docker ps -a | grep 'Exited' | awk -F ' ' '{print $1}')");
            }
        }
        util.apply(findGhostImages);
    };

    const findGhostImages = () => {
        util.runAsync("echo $(docker images | grep '<none>' | awk -F ' ' '{print $3}') | awk /./", rmi);
    };

    const rmi = (ghostImages: string[]) => {
        if (ghostImages.length > 0) {
            util.runSync("docker rmi $(docker images | grep '<none>' | awk -F ' ' '{print $3}')");
        }
    };

    util.runAsync("echo $(docker ps -a | grep 'Exited' | awk -F ' ' '{print $1}') | awk /./", rm);
}

function printVersion(): void {
    util.println(`${constant.DOCKTITUDE} ${constant.VERSION} ${process.env.npm_package_version}${constant.EMPTY} (installed node version: ${process.version.substring(1)})`);
}

function checkNodeVersion(): void {
    if (util.getNodeVersion() < constant.NODE_SUPPORTED_INI_VERSION) {
        util.fmt(constant.NODE_UNSUPPORTED_VERSION, process.version.substring(1));
    }
}

function checkArg(args: string[], joined: boolean = false): string {
    if (args.length > 1) {
        if (joined) {
            return args.slice(1).join(constant.SPACE);
        }
        return args[1];
    }
    return null;
}

function buildUsage(): Usage {
    const u = new Usage("usage: docktitude [help] <command> [<args>]\n\nCommands:");
    u.add(Command.build, "Build context Docker image", "context");
    u.add(Command.clean, "Remove exited Docker containers and useless images\nUse -v to remove the associated volumes");
    u.add(Command.config, "List auto-configured Docker images building tags");
    u.add(Command.export, "Export all contexts except binaries to a tar archive");
    u.add(Command.info, "Show information relating to the Dockerfile files");
    u.add(Command.op, "Change maintainer information in the Dockerfile files", "name");
    u.add(Command.play, "Run shell script for defined docktitude script tags", "context");
    u.add(Command.print, "Show context Dockerfile", "context");
    u.add(Command.script, "Show shell script for defined docktitude script tags", "context");
    u.add(Command.snapshot, "Display Docker images and save the selected one (.tar)");
    u.add(Command.status, "Show local Docker images update status");
    u.add(Command.tree, "List Docker images in a tree-like format");
    u.add(Command.update, "Update external Docker images");
    u.add(Command.upgrade, "Build cascade local Docker images");
    u.add(Command.version, "Show version information");
    return u;
}

// #############################################################################

checkNodeVersion();
parseArgs();

// #############################################################################