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
package com.docktitude.cli;

import com.docktitude.Constant;
import com.docktitude.tree.TreeBuilder;
import com.docktitude.tree.TreeDataHolder;

import java.io.Console;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class Engine {

	private static final String VERSION = "1.1.0";

	private Engine() {
	}

	public static void build(final String ctx) throws Exception {
		final Map<String, Path> relPathsMap = readPaths();

		final Map<String, Path> fullPaths = readFullPaths(relPathsMap);
		final Map<String, String> tagsByCtx = readTags(relPathsMap);

		final Path path = fullPaths.get(ctx);
		if (path != null) {
			Executable.execWithPattern(Command.BUILD, String.format(Constant.FMT22,
					tagsByCtx.get(ctx),
					path.getParent().toString()));
		}
	}

	public static void clean() throws Exception {
		clean(false);
	}

	public static void clean(final boolean removeVolumes) throws Exception {
		final List<String> exitedContainers = Executable.execWithResultsAsList(Command.RM0);
		if (exitedContainers.size() > 0) {
			if (removeVolumes) {
				Executable.exec(Command.RMV);
			}
			else {
				Executable.exec(Command.RM);
			}
		}
		final List<String> uselessImages = Executable.execWithResultsAsList(Command.RMI0);
		if (uselessImages.size() > 0) {
			Executable.exec(Command.RMI);
		}
	}

	public static void printStatus() throws Exception {
		final Stream<String> images = Executable.execWithResultsAsStream(Command.IMAGES_IDS);
		final Map<String, String> idsByImage = images.collect(Collectors.toMap(
				s -> s.substring(0, s.indexOf(Constant.PLUS)),
				s -> s.substring(s.indexOf(Constant.PLUS) + 1)));

		final Map<String, String> reportMap = new HashMap<>();
		final TreeDataHolder<String> treeDataHolder = generateTreeData();

		final BiConsumer<String, String> func = (image, path) -> {
			try {
				if (treeDataHolder.getRoots().contains(image)) return;

				final List<String> layersIds = Executable.execWithPatternAndResults(Command.HISTORY, image);
				if ((layersIds.size() > 1) && (!layersIds.get(0).contains(Constant.IMG_HISTORY_ERROR))) {

					final Optional<String> parent = treeDataHolder.getParent(image);
					if (parent.isPresent()) {
						final String parentId = idsByImage.get(normalize(parent.get()));
						if (!layersIds.contains(parentId)) {
							reportMap.put(image, String.format(Constant.Msg.UPGRADE_REQUIRED, parent.get()));
						}
					}
					else {
						reportMap.put(image, Constant.Msg.PARENT_NOT_FOUND);
					}
				}
				else {
					reportMap.put(image, Constant.Msg.IMAGE_NOT_FOUND);
				}
			}
			catch (Exception e) {
				// Exception ignored
			}
		};

		treeDataHolder.walk(func);
		if (reportMap.isEmpty()) {
			Printable.print(Constant.Msg.NOTHING_TO_REPORT);
		}
		else {
			Printable.printMap(reportMap);
		}
	}

	private static String normalize(final String image) {
		if (!image.contains(Constant.COLON)) {
			return String.format(Constant.FMT3, image, Constant.COLON, Constant.DEFAULT_IMG_TAG);
		}
		return image;
	}

	public static void export() throws Exception {
		//TODO Consider yml files as non binary
		if (!readPaths().isEmpty()) {
			Executable.exec(Command.EXPORT);
		}
	}

	public static void snapshot() throws Exception {
		final List<String> images = Executable.execWithResultsAsList(Command.IMAGES_IDS);
		if (images.isEmpty()) {
			Printable.print(Constant.Msg.NO_IMG);
			return;
		}

		final Map<Integer, String> imagesByIndex = IntStream.rangeClosed(1, images.size())
				.mapToObj(i -> i)
				.collect(Collectors.toMap(
						Function.identity(),
						i -> {
							final String img = images.get(i - 1);
							return img.substring(0, img.indexOf(Constant.PLUS));
						}));
		Printable.printMap(imagesByIndex);

		try {
			final Console console = System.console();
			if (console != null) {
				final String imgIdxAsString = console.readLine(Constant.Msg.PROMPT_IMG);
				final Integer imgIdx = Printable.convert(imgIdxAsString);
				if ((imgIdx != null) && imagesByIndex.containsKey(imgIdx)) {
					Executable.execWithPattern(Command.SAVE, imagesByIndex.get(imgIdx));
				}
				else {
					Printable.print(String.format(Constant.Msg.PROMPT_NO_ENTRY, imgIdxAsString));
				}
			}
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void printConfig() throws Exception {
		Printable.printMap(readTags(), Constant.CTX, Constant.CTX_IMG_TAG);
	}

	public static void update() throws Exception {
		final Stream<String> images = Executable.execWithResultsAsStream(Command.IMAGES);

		final Set<String> localImages = readTags().values()
				.stream()
				.map(s -> (!s.contains(Constant.COLON)) ? String.format(Constant.FMT3,
						s,
						Constant.COLON,
						Constant.DEFAULT_IMG_TAG) : s)
				.collect(Collectors.toSet());

		images.filter(s -> !localImages.contains(s))
				.filter(s -> !s.equals(Constant.USELESS_IMAGE))
				.forEach(s -> {
					try {
						Executable.execWithPattern(Command.PULL, s);
					}
					catch (Exception e) {
						// Exception ignored
					}
				});
	}

	public static void upgrade() throws Exception {
		final BiConsumer<String, String> func = (image, path) -> {
			if (path != null) {
				try {
					Executable.execWithPattern(Command.BUILD, String.format(Constant.FMT22, image, path));
				}
				catch (Exception e) {
					// Exception ignored
				}
			}
		};
		final TreeDataHolder<String> treeDataHolder = generateTreeData();
		treeDataHolder.walk(func);
	}

	private static TreeDataHolder<String> generateTreeData() throws Exception {
		final Map<String, Path> fullPathByRelativePath = readPaths();
		final Map<String, Path> locationsByCtx = readFullPaths(fullPathByRelativePath);
		final Map<String, String> tagsByCtx = readTags(fullPathByRelativePath);

		final Map<String, List<String>> childrenByParent = locationsByCtx.keySet().parallelStream()
				.collect(Collectors.groupingBy(s -> getParentImage(s, locationsByCtx)));

		final Map<String, List<String>> taggedChildrenByParent = new HashMap<>();
		final Consumer<Map.Entry<String, List<String>>> action = e -> {
			taggedChildrenByParent.put(
					e.getKey(),
					e.getValue().stream()
							.map(tagsByCtx::get)
							.sorted()
							.collect(Collectors.toList()));
		};
		childrenByParent.entrySet().stream().forEach(action);

		final Map<String, String> locationsByTaggedChild = tagsByCtx.keySet().stream()
				.collect(Collectors.toMap(
						tagsByCtx::get,
						s -> locationsByCtx.get(s).getParent().toString()));

		final List<String> allChildren = childrenByParent.values().parallelStream()
				.flatMap(Collection::stream)
				.map(tagsByCtx::get)
				.collect(Collectors.toList());

		final List<String> roots = childrenByParent.keySet().stream()
				.filter(s -> !allChildren.contains(s))
				.sorted()
				.collect(Collectors.toList());

		return new TreeDataHolder<>(roots, taggedChildrenByParent, locationsByTaggedChild);
	}

	public static void printTree() throws Exception {
		final TreeBuilder tb = new TreeBuilder(generateTreeData());
		tb.build();
	}

	private static String getParentImage(final String ctx, final Map<String, Path> locationsByCtx) {
		if (Files.exists(locationsByCtx.get(ctx))) {
			try {
				final List<String> lines = Files.readAllLines(locationsByCtx.get(ctx));
				for (String line : lines) {
					if (line.startsWith(Constant.PARENT_PATTERN)) {
						final int beginIndex = line.indexOf(Constant.PARENT_PATTERN) + Constant.PARENT_PATTERN.length();
						return line.substring(beginIndex).trim();
					}
				}
			}
			catch (Exception e) {
				// Exception ignored
			}
		}
		return Constant.QMK;
	}

	public static void printContext(final String ctx) throws Exception {
		final Map<String, Path> locationsByCtx = readFullPaths();

		if (locationsByCtx.containsKey(ctx)) {
			if (Files.exists(locationsByCtx.get(ctx))) {
				Printable.print(Constant.SECTION);
				Printable.printf(Constant.CTX_FILE, ctx);
				final String path = locationsByCtx.get(ctx).toString();
				Printable.printf(Constant.CTX_PATH, path);
				Printable.print(Constant.SECTION);
				Files.readAllLines(Paths.get(path))
						.forEach(java.lang.System.out::println);
				Printable.print(Constant.SECTION);
			}
		}
	}

	public static void printScript(final String ctx) throws Exception {
		final Map<String, Path> locationsByCtx = readFullPaths();
		if (locationsByCtx.containsKey(ctx)) {
			if (Files.exists(locationsByCtx.get(ctx))) {

				final String path = locationsByCtx.get(ctx).toString();
				final List<String> scriptLines = Files.readAllLines(Paths.get(path)).stream()
						.filter(s -> s.startsWith(Constant.SCRIPT_TAG))
						.collect(Collectors.toList());

				if ((scriptLines.size() > 2)
						&& scriptLines.get(0).contains(Constant.BEGIN_TEMPLATE_SCRIPT)
						&& scriptLines.get(scriptLines.size() - 1).contains(Constant.END_TEMPLATE_SCRIPT)) {

					Printable.print(Constant.SECTION);
					Printable.printf(Constant.CTX_SCRIPT, ctx);
					Printable.print(Constant.SECTION);

					scriptLines.remove(scriptLines.size() - 1);
					scriptLines.stream()
							.skip(1)
							.map(s -> (s.startsWith(Constant.SCRIPT_TAG + Constant.SPACE)) ?
									s.replaceFirst(Constant.SCRIPT_TAG + Constant.SPACE, Constant.EMPTY) :
									s.replaceFirst(Constant.SCRIPT_TAG, Constant.EMPTY))
							.forEach(java.lang.System.out::println);

					Printable.print(Constant.SECTION);
				}
			}
		}
	}

	public static void changeMaintainer(String[] nameFragments) throws Exception {
		if (nameFragments != null) {
			final String maintainer = String.join(Constant.SPACE, nameFragments);
			readFullPaths().values()
					.parallelStream()
					.forEach(p -> changeMaintainer(p, maintainer));
		}
	}

	private static void changeMaintainer(final Path path, final String maintainer) {
		if (Files.exists(path)) {
			try {
				final List<String> lines = Files.readAllLines(path);
				int i = 0;
				boolean hasMaintainer = false;
				for (String line : lines) {
					if (line.startsWith(String.format(Constant.FMT2, Constant.MAINTAINER, Constant.SPACE))) {
						hasMaintainer = true;
						break;
					}
					i++;
				}
				if (hasMaintainer) {
					lines.set(i, String.join(Constant.SPACE, Constant.MAINTAINER, maintainer));
					Files.write(path, lines);
				}
			}
			catch (Exception e) {
				java.lang.System.err.println(e.getMessage());
			}
		}
	}

	public static void printVersion() throws Exception {
		Printable.print(String.join(Constant.SPACE, Constant.DOCKTITUDE, Constant.VERSION, VERSION));
	}

	public static void printInfo() throws Exception {
		Printable.printColl(readFullPaths().keySet(), String.format(Constant.CTX_REPORT, readFullPaths().size()));
	}

	private static Map<String, Path> readPaths() throws Exception {
		final Path currentPath = Paths.get(Constant.DOT);

		Stream<Path> stream;
		if ((Constant.SLASH + Constant.DOT).equals(currentPath.toAbsolutePath().toString())) {
			stream = Executable.execWithResultsAsPathStream(Command.FIND);
		}
		else {
			stream = Files.walk(currentPath)
					.filter(p -> p.getFileName().toString().equals(Constant.DOCKERFILE));
		}

		final Map<String, Path> fullPathByRelativePath = stream.collect(Collectors.toMap(
				Path::toString,
				p -> Paths.get(String.join(
						Constant.SLASH,
						p.toAbsolutePath().toString().split(
								String.format(Constant.FMT3, Constant.SLASH, Constant.DOT, Constant.SLASH))))));

		final Set<String> filteredPathNames = new HashSet<>();
		final Set<Path> duplicatedContexts = fullPathByRelativePath.values().stream()
				.filter(p -> !filteredPathNames.add(p.getParent().getFileName().toString()))
				.collect(Collectors.toSet());

		if (duplicatedContexts.size() > 0) {
			duplicatedContexts.stream().forEach(java.lang.System.out::println);
			Printable.print(Constant.SECTION);
			Printable.print(Constant.Msg.DUPLICATED_CTX);
			Printable.print(Constant.SECTION);
			System.exit(0);
		}

		if (fullPathByRelativePath.isEmpty()) {
			Printable.print(Constant.Msg.NO_CTX);
			System.exit(0);
		}

		return fullPathByRelativePath;
	}

	private static Map<String, Path> readFullPaths() throws Exception {
		return readFullPaths(readPaths());
	}

	private static Map<String, Path> readFullPaths(final Map<String, Path> relPathsMap) {
		return relPathsMap.values().stream()
				.collect(Collectors.toMap(p -> p.getParent().getFileName().toString(), p -> p));
	}

	private static Map<String, String> readTags() throws Exception {
		return readTags(readPaths());
	}

	private static Map<String, String> readTags(final Map<String, Path> relPathsMap) {
		return relPathsMap.entrySet().stream()
				.collect(Collectors.toMap(
						e -> e.getValue().getParent().getFileName().toString(),
						e -> computeTag(e.getKey())
				));
	}

	private static String computeTag(final String path) {
		final Function<Path, String> func = p -> p.getFileName().toString()
				.replaceFirst(Constant.DASH, Constant.COLON)
				.replaceAll(Constant.UNDERSCORE, Constant.DASH);

		final Path parent = Paths.get(path).getParent();

		if (Paths.get(Constant.DOT).equals(parent)) {
			return func.apply(parent.toAbsolutePath().getParent());
		}

		final String parentPath = parent.getParent().getFileName().toString();
		return String.format(Constant.FMT2,
				(!Constant.DOT.equals(parentPath)) ? String.format(Constant.FMT2,
						parentPath, Constant.SLASH) : Constant.EMPTY,
				func.apply(parent));
	}
}
