/*******************************************************************************
 * Copyright (c) 2026 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.impl.dom;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Platform;

/**
 * Media query model for {@code @media} rules.
 *
 * <p>
 * The media types {@code all} and {@code screen} match, plus vendor features
 * for the operating system, the windowing system and the operating system
 * version. Unknown types, features and values evaluate to false, as Media
 * Queries requires.
 * </p>
 */
public final class Media {

	/** Feature naming the operating system, e.g. {@code linux}. */
	public static final String FEATURE_OS = "-eclipse-os"; //$NON-NLS-1$

	/** Feature naming the windowing system, e.g. {@code gtk}. */
	public static final String FEATURE_WS = "-eclipse-ws"; //$NON-NLS-1$

	/** Feature naming the operating system version, e.g. {@code "10.15"}. */
	public static final String FEATURE_OS_VERSION = "-eclipse-os-version"; //$NON-NLS-1$

	/** Lower bound on the operating system version, inclusive. */
	public static final String FEATURE_MIN_OS_VERSION = "-eclipse-min-os-version"; //$NON-NLS-1$

	/** Upper bound on the operating system version, inclusive. */
	public static final String FEATURE_MAX_OS_VERSION = "-eclipse-max-os-version"; //$NON-NLS-1$

	private static final Pattern NON_DIGITS = Pattern.compile("[^0-9]+"); //$NON-NLS-1$

	private static final List<String> SUPPORTED_TYPES = List.of("all", "screen"); //$NON-NLS-1$ //$NON-NLS-2$

	private Media() {
		// constants and nested types only
	}

	/** The environment a query is evaluated against. */
	public record Context(String os, String ws, String osVersion) {

		public Context(String os, String ws) {
			this(os, ws, null);
		}

		/** The platform this workbench runs on. */
		public static Context current() {
			return new Context(Platform.getOS(), Platform.getWS(), System.getProperty("os.version")); //$NON-NLS-1$
		}
	}

	/**
	 * One {@code (name: value)} expression. A {@code null} value is the boolean
	 * form {@code (name)}, true whenever the feature has a value at all.
	 */
	public record Feature(String name, String value) {

		boolean matches(Context context) {
			String feature = name.toLowerCase();
			String actual = switch (feature) {
			case FEATURE_OS -> context.os();
			case FEATURE_WS -> context.ws();
			case FEATURE_OS_VERSION, FEATURE_MIN_OS_VERSION, FEATURE_MAX_OS_VERSION -> context.osVersion();
			default -> null;
			};
			if (actual == null || actual.isEmpty()) {
				return false;
			}
			if (value == null) {
				return true;
			}
			return switch (feature) {
			case FEATURE_OS_VERSION -> compareVersions(actual, value) == 0;
			case FEATURE_MIN_OS_VERSION -> compareVersions(actual, value) >= 0;
			case FEATURE_MAX_OS_VERSION -> compareVersions(actual, value) <= 0;
			default -> actual.equalsIgnoreCase(value);
			};
		}

		String text() {
			return value == null ? "(" + name + ")" : "(" + name + ": " + value + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
	}

	/**
	 * A single media query: an optional {@code not}, a media type and the
	 * features joined to it with {@code and}.
	 */
	public record Query(boolean negated, String type, List<Feature> features) {

		/** {@code not all}, what a malformed query evaluates to. */
		public static final Query NEVER = new Query(true, "all", List.of()); //$NON-NLS-1$

		public Query {
			features = List.copyOf(features);
		}

		public boolean matches(Context context) {
			boolean matched = SUPPORTED_TYPES.contains(type.toLowerCase())
					&& features.stream().allMatch(feature -> feature.matches(context));
			return negated != matched;
		}

		public String text() {
			String expressions = features.stream().map(Feature::text).collect(Collectors.joining(" and ")); //$NON-NLS-1$
			String prefix = negated ? "not " + type : type; //$NON-NLS-1$
			return expressions.isEmpty() ? prefix : prefix + " and " + expressions; //$NON-NLS-1$
		}

		@Override
		public String toString() {
			return text();
		}
	}

	/**
	 * Compares versions over the segments the value gives, so its precision is
	 * the granularity: {@code "10.15"} covers all of 10.15.x. Anything that is
	 * not a digit separates segments, dropping a {@code -33-generic} tail.
	 */
	private static int compareVersions(String actual, String required) {
		long[] left = segments(actual);
		long[] right = segments(required);
		for (int i = 0; i < right.length; i++) {
			int difference = Long.compare(i < left.length ? left[i] : 0, right[i]);
			if (difference != 0) {
				return difference;
			}
		}
		return 0;
	}

	private static long[] segments(String version) {
		return NON_DIGITS.splitAsStream(version).filter(part -> !part.isEmpty())
				.mapToLong(part -> part.length() > 18 ? Long.MAX_VALUE : Long.parseLong(part)).toArray();
	}

	/**
	 * Whether a comma separated query list applies. Any query matching is
	 * enough, and an empty list matches everything.
	 */
	public static boolean matches(List<Query> queries, Context context) {
		return queries.isEmpty() || queries.stream().anyMatch(query -> query.matches(context));
	}
}
