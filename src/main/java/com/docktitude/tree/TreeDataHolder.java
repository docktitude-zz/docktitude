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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public final class TreeDataHolder<T> {

	private final List<T> roots;
	private final Map<T, List<T>> nodesByParent;
	private final Map<T, T> additionalInfoByNode;

	public TreeDataHolder(final List<T> roots, final Map<T, List<T>> nodesByParent) {
		this(roots, nodesByParent, null);
	}

	public TreeDataHolder(
			final List<T> roots,
			final Map<T, List<T>> nodesByParent,
			final Map<T, T> additionalInfoByNode) {

		this.roots = roots;
		this.nodesByParent = nodesByParent;
		this.additionalInfoByNode = additionalInfoByNode;
	}

	private static <T> void walk(
			final List<T> rootElements,
			final Map<T, List<T>> nodesByParent,
			final Map<T, T> additionalInfoByNode,
			final BiConsumer<T, T> func) {

		for (T t : rootElements) {
			func.accept(t, additionalInfoByNode.get(t));
			final List<T> children = nodesByParent.get(t);
			if (children != null) {
				walk(children, nodesByParent, additionalInfoByNode, func);
			}
		}
	}

	public boolean isEmpty() {
		return ((roots != null) && roots.isEmpty());
	}

	public void walk(final BiConsumer<T, T> func) {
		walk(roots, nodesByParent, additionalInfoByNode, func);
	}

	public List<T> getRoots() {
		return roots;
	}

	public List<T> getNodes(T parent) {
		if (nodesByParent.containsKey(parent)) {
			return nodesByParent.get(parent);
		}
		return null;
	}

	public Optional<T> getParent(T node) {
		return nodesByParent.keySet().stream()
				.filter(k -> nodesByParent.get(k).contains(node))
				.findFirst();
	}
}