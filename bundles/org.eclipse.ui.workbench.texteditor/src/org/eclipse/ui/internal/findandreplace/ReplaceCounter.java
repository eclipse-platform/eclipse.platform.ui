/*******************************************************************************
 * Copyright (c) 2026 Contributors to Eclipse Foundation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Contributors to Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.findandreplace;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides an auto-incrementing counter that can be embedded in Find/Replace
 * replace strings using the placeholder syntax {@code #{start,step,pad}}.
 *
 * <p>
 * Syntax: {@code #{start,step,pad}} where
 * <ul>
 * <li>{@code start} &mdash; initial value (integer, may be negative)</li>
 * <li>{@code step} &mdash; increment per replacement (integer, may be negative
 * for descending sequences)</li>
 * <li>{@code pad} &mdash; minimum number of digits, zero-padded (0 = no
 * padding)</li>
 * </ul>
 *
 * <p>
 * Examples:
 * <ul>
 * <li>{@code #{1,1,3}} &mdash; produces {@code 001}, {@code 002},
 * {@code 003}, &hellip;</li>
 * <li>{@code #{10,5,0}} &mdash; produces {@code 10}, {@code 15}, {@code 20},
 * &hellip;</li>
 * <li>{@code #{100,-1,0}} &mdash; produces {@code 100}, {@code 99},
 * {@code 98}, &hellip;</li>
 * </ul>
 *
 * <p>
 * The placeholder may be combined with literal text and regex back-references,
 * for example {@code $1_#{1,1,2}} produces {@code Match1_01},
 * {@code Match1_02}, &hellip;
 *
 * @since 3.21
 */
public class ReplaceCounter {

	/**
	 * Pattern matching the counter placeholder {@code #{start,step,pad}}.
	 * Captures: group 1 = start, group 2 = step, group 3 = pad.
	 */
	static final Pattern COUNTER_PATTERN = Pattern.compile("#\\{(-?\\d+),(-?\\d+),(\\d+)\\}"); //$NON-NLS-1$

	private final int start;
	private final int step;
	private final int pad;
	private int current;

	/**
	 * Creates a counter with the given parameters.
	 *
	 * @param start initial counter value
	 * @param step  increment applied after each {@link #next()} call
	 * @param pad   minimum digit width (0 = no padding)
	 */
	public ReplaceCounter(int start, int step, int pad) {
		this.start = start;
		this.step = step;
		this.pad = pad;
		this.current = start;
	}

	/** Resets the counter to the initial {@code start} value. */
	public void reset() {
		current = start;
	}

	/**
	 * Returns the current counter value as a (possibly zero-padded) string, then
	 * advances the counter by {@code step}.
	 *
	 * @return formatted counter value
	 */
	public String next() {
		String value;
		if (pad > 0) {
			value = String.format("%0" + pad + "d", current); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			value = Integer.toString(current);
		}
		current += step;
		return value;
	}

	/**
	 * Replaces the first counter placeholder in {@code template} with the
	 * {@link #next()} value.
	 *
	 * @param template the replace string possibly containing {@code #{...}}
	 * @return the template with the placeholder replaced by the next counter value
	 */
	public String expand(String template) {
		Matcher matcher = COUNTER_PATTERN.matcher(template);
		if (matcher.find()) {
			return matcher.replaceFirst(Matcher.quoteReplacement(next()));
		}
		return template;
	}

	/**
	 * Returns {@code true} if the given string contains a counter placeholder.
	 *
	 * @param replaceString string to test
	 * @return {@code true} if a placeholder is present
	 */
	public static boolean containsCounter(String replaceString) {
		return COUNTER_PATTERN.matcher(replaceString).find();
	}

	/**
	 * Parses the first counter placeholder found in {@code replaceString} and
	 * returns a new {@link ReplaceCounter} with the corresponding parameters.
	 * Returns {@code null} if no placeholder is present.
	 *
	 * @param replaceString string to parse
	 * @return a new counter, or {@code null}
	 */
	public static ReplaceCounter parse(String replaceString) {
		Matcher matcher = COUNTER_PATTERN.matcher(replaceString);
		if (matcher.find()) {
			int start = Integer.parseInt(matcher.group(1));
			int step = Integer.parseInt(matcher.group(2));
			int pad = Math.min(Integer.parseInt(matcher.group(3)), 20);
			return new ReplaceCounter(start, step, pad);
		}
		return null;
	}
}
