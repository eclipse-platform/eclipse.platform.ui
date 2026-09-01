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
import org.osgi.framework.Bundle;

/**
 * Media query model for {@code @media} rules.
 *
 * <p>
 * The media types {@code all} and {@code screen} match, plus vendor features
 * for the operating system, the windowing system, the operating system version
 * and the installed bundles. Unknown types, features and values evaluate to
 * false, as Media Queries requires.
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

	/** Feature naming a bundle that has to be installed. */
	public static final String FEATURE_BUNDLE = "-eclipse-bundle"; //$NON-NLS-1$

	/** Feature naming a bundle and its version, e.g. {@code "org.eclipse.ui 3.2"}. */
	public static final String FEATURE_BUNDLE_VERSION = "-eclipse-bundle-version"; //$NON-NLS-1$

	/** Lower bound on a bundle's version, inclusive. */
	public static final String FEATURE_MIN_BUNDLE_VERSION = "-eclipse-min-bundle-version"; //$NON-NLS-1$

	/** Upper bound on a bundle's version, inclusive. */
	public static final String FEATURE_MAX_BUNDLE_VERSION = "-eclipse-max-bundle-version"; //$NON-NLS-1$

	private static final List<String> BUNDLE_FEATURES = List.of(FEATURE_BUNDLE, FEATURE_BUNDLE_VERSION,
			FEATURE_MIN_BUNDLE_VERSION, FEATURE_MAX_BUNDLE_VERSION);

	private static final Pattern NON_DIGITS = Pattern.compile("[^0-9]+"); //$NON-NLS-1$

	private static final Pattern WHITESPACE = Pattern.compile("\\s+"); //$NON-NLS-1$

	private static final List<String> SUPPORTED_TYPES = List.of("all", "screen"); //$NON-NLS-1$ //$NON-NLS-2$

	private Media() {
		// constants and nested types only
	}

	/** Looks a bundle's version up by symbolic name. */
	@FunctionalInterface
	public interface BundleVersions {

		/** The version of that bundle, or {@code null} when it is not installed. */
		String versionOf(String symbolicName);
	}

	/**
	 * The environment a query is evaluated against. The installed bundles can
	 * change while the workbench runs, so a bundle query answers for the moment
	 * the rules were indexed.
	 */
	public record Context(String os, String ws, String osVersion, BundleVersions bundles) {

		private static final BundleVersions NONE_INSTALLED = symbolicName -> null;

		public Context(String os, String ws) {
			this(os, ws, null, NONE_INSTALLED);
		}

		public Context(String os, String ws, String osVersion) {
			this(os, ws, osVersion, NONE_INSTALLED);
		}

		/** The platform this workbench runs on. */
		public static Context current() {
			return new Context(Platform.getOS(), Platform.getWS(), System.getProperty("os.version"), //$NON-NLS-1$
					Context::installedVersion);
		}

		private static String installedVersion(String symbolicName) {
			Bundle bundle = Platform.getBundle(symbolicName);
			return bundle == null ? null : bundle.getVersion().toString();
		}
	}

	/**
	 * One {@code (name: value)} expression. A {@code null} value is the boolean
	 * form {@code (name)}, true whenever the feature has a value at all.
	 */
	public record Feature(String name, String value) {

		boolean matches(Context context) {
			String feature = name.toLowerCase();
			if (BUNDLE_FEATURES.contains(feature)) {
				return matchesBundle(feature, context);
			}
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

		/**
		 * A bundle feature names its subject in its value, as
		 * {@code "<symbolic name> <version>"}: features are evaluated one by one
		 * and cannot refer to each other.
		 */
		private boolean matchesBundle(String feature, Context context) {
			if (value == null) {
				return false; // the boolean form names no bundle
			}
			String[] parts = WHITESPACE.split(value.trim(), 2);
			String installed = context.bundles().versionOf(parts[0]);
			if (installed == null) {
				return false;
			}
			if (feature.equals(FEATURE_BUNDLE)) {
				return true;
			}
			if (parts.length < 2) {
				return false; // a bound needs a version to compare against
			}
			return switch (feature) {
			case FEATURE_BUNDLE_VERSION -> compareVersions(installed, parts[1]) == 0;
			case FEATURE_MIN_BUNDLE_VERSION -> compareVersions(installed, parts[1]) >= 0;
			default -> compareVersions(installed, parts[1]) <= 0;
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
