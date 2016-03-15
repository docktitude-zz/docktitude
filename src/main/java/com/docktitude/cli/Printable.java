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

import java.io.PrintStream;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

public interface Printable {

	int DEFAULT_COL_SIZE = 25;
	int DEFAULT_COL_PADDING = 2;

	static <T, U> void printMap(final Map<T, U> map) {
		printMap(map, null, null);
	}

	static <T, U> void printMap(final Map<T, U> map, final String kTitle, final String vTitle) {
		printMap(map, kTitle, vTitle, System.out);
	}

	static <T, U> void printMap(final Map<T, U> map, final String kTitle, final String vTitle, final PrintStream ps) {
		if ((null == map) || map.isEmpty()) return;

		final Function<Collection<?>, Integer> maxFunc = c -> c.stream()
				.map(t -> t.toString().length())
				.max(Integer::compare)
				.orElse(DEFAULT_COL_SIZE);

		final Integer[] kMax = {maxFunc.apply(map.keySet())};
		final Integer[] vMax = {maxFunc.apply(map.values())};

		final boolean hasTitles = (kTitle != null) && (vTitle != null);
		if (hasTitles) {
			if (kTitle.length() > kMax[0]) {
				kMax[0] = kTitle.length();
			}
			if (vTitle.length() > vMax[0]) {
				vMax[0] = vTitle.length();
			}
		}

		final String border = String.format(Constant.Table.BORDER2,
				Printable.repeat(Constant.DASH, kMax[0] + DEFAULT_COL_PADDING),
				Printable.repeat(Constant.DASH, vMax[0] + DEFAULT_COL_PADDING));

		final BiConsumer<String, String> fillColsFunc = (k, v) -> print(String.format(Constant.Table.COLS2,
				(k + Printable.repeat(Constant.SPACE, kMax[0] - k.length())),
				(v + Printable.repeat(Constant.SPACE, vMax[0] - v.length()))), ps);

		Comparator<Map.Entry<T, U>> comparator;
		if (isNumeric(map.keySet().stream().findFirst().get())) {
			comparator = (o1, o2) -> Integer.valueOf(o1.getKey().toString())
					.compareTo(Integer.valueOf(o2.getKey().toString()));
		}
		else {
			comparator = (o1, o2) -> o1.getKey().toString().compareTo(o2.getKey().toString());
		}

		if (hasTitles) {
			print(border, ps);
			fillColsFunc.accept(kTitle, vTitle);
		}
		print(border, ps);
		map.entrySet().stream()
				.sorted(comparator)
				.forEach(e -> fillColsFunc.accept(e.getKey().toString(), e.getValue().toString()));
		print(border, ps);
	}

	static void printColl(final Collection<String> c) {
		printColl(c, null);
	}

	static void printColl(final Collection<String> c, final String legend) {
		printColl(c, legend, System.out);
	}

	static void printColl(final Collection<String> c, final String legend, final PrintStream ps) {
		if ((null == c) || c.isEmpty()) return;

		final Integer[] max = {c.stream().map(String::length).max(Integer::compare).orElse(DEFAULT_COL_SIZE)};

		boolean hasLegend = (legend != null);
		if (hasLegend && (legend.length() > max[0])) {
			max[0] = legend.length();
		}

		final String border = String.format(Constant.Table.BORDER1,
				Printable.repeat(Constant.DASH, max[0] + DEFAULT_COL_PADDING));

		final Consumer<String> fillColFunc = s -> print(String.format(Constant.Table.COLS1,
				(s + Printable.repeat(Constant.SPACE, max[0] - s.length()))), ps);

		print(border, ps);
		c.stream().sorted().forEach(fillColFunc::accept);
		print(border, ps);

		if (hasLegend) {
			fillColFunc.accept(legend);
			print(border, ps);
		}
	}

	static void printf(final String s, final Object... args) {
		System.out.printf(s, args);
	}

	static void print(final String s) {
		print(s, System.out);
	}

	static void print(final String s, final PrintStream printStream) {
		printStream.println(s);
	}

	static String repeat(final String s, final int repeat) {
		if (repeat <= 0) return Constant.EMPTY;
		final StringBuilder builder = new StringBuilder();
		IntStream.rangeClosed(1, repeat).forEach(i -> builder.append(s));
		return builder.toString();
	}

	static boolean isNumeric(final Object o) {
		final Integer i = convert(String.valueOf(o));
		return (i != null);
	}

	static Integer convert(final String s) {
		try {
			return Integer.parseInt(s);
		}
		catch (Exception e) {
			// Exception ignored
		}
		return null;
	}

	static String sanitize(final String text) {
		if (text.contains(Constant.TAR_EXTENSION)) {
			final String[] frags = text.split(Constant.SPACE);
			final List<String> l = new ArrayList<>();
			for (String s : frags) {
				if (s.contains(Constant.TAR_EXTENSION)) {
					l.add(s.replaceAll(Constant.SLASH, Constant.DASH)
							.replaceAll(Constant.COLON, Constant.DASH));
				}
				else {
					l.add(s);
				}
			}
			return String.join(Constant.SPACE, l);
		}
		return text;
	}

}
