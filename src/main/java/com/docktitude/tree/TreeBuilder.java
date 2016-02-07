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
package com.docktitude.tree;

import com.docktitude.Constant;
import com.docktitude.cli.Printable;

import java.io.PrintStream;
import java.util.*;
import java.util.function.Consumer;

public class TreeBuilder {

	private final TreeDataHolder<String> treeDataHolder;
	private final Consumer<List<String>> lineConsumer;
	private final Map<String, String> decorations;

	public TreeBuilder(final TreeDataHolder<String> treeDataHolder) {
		this(treeDataHolder, System.out);
	}

	public TreeBuilder(final TreeDataHolder<String> treeDataHolder, final PrintStream printStream) {
		this.treeDataHolder = treeDataHolder;
		this.lineConsumer = l -> printStream.println(String.join(Constant.EMPTY, l));
		this.decorations = new HashMap<>();
	}

	public void build() {
		if (!treeDataHolder.isEmpty()) {
			final List<String> line = Collections.singletonList(Constant.DOT);
			lineConsumer.accept(line);

			final List<String> roots = treeDataHolder.getRoots();
			for (String node : roots) {
				decorations.put(node, Constant.STAR);
			}
			appendTreeNodes(roots, 0, new int[]{1}, line);
		}
	}

	private void appendTreeNodes(
			final List<String> nodes,
			final int level,
			final int[] index,
			final List<String> prevLine) {

		final int nbNodes = nodes.size();

		List<String> line;
		for (String node : nodes) {
			line = new ArrayList<>();

			for (int i = 0; i < level; i++) {
				line.add(Printable.repeat(Constant.SPACE, Constant.Tree.TAB));
			}
			line.add(((nbNodes > 1) && (index[0] != nbNodes)) ? Constant.Tree.BRANCH2 : Constant.Tree.BRANCH1);
			line.add(String.format(Constant.FMT3, Constant.SPACE, node, getDecoration(node)));

			if (level > 0) {
				int lineNbElements = line.size();
				int cur = 0;
				for (String s : prevLine) {
					if ((s.contains(Constant.Tree.BRANCH2) || s.contains(Constant.Tree.BRANCH0))
							&& (cur < lineNbElements) && line.get(cur).trim().isEmpty()) {
						line.remove(cur);
						line.add(cur, String.format(
								Constant.FMT2,
								Constant.Tree.BRANCH0,
								Printable.repeat(Constant.SPACE, Constant.Tree.TAB - 1)));
					}
					cur++;
				}
			}
			lineConsumer.accept(line);

			index[0] = index[0] + 1;

			final List<String> subNodes = treeDataHolder.getNodes(node);
			if (subNodes != null) {
				appendTreeNodes(subNodes, level + 1, new int[]{1}, line);
			}
		}
	}

	private String getDecoration(final String node) {
		return Optional.ofNullable(decorations.get(node)).orElse(Constant.EMPTY);
	}
}