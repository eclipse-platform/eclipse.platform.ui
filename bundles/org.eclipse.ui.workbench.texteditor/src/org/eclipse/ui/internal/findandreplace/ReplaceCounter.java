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
 * Provides an auto-incrementing counter that can be embedded in regular
 * expression Find/Replace replace strings using the placeholder syntax
 * {@code #{start,step,pad}}.
 *
 * <p>
 * Syntax: {@code #{start,step,pad}} where
 * <ul>
 * <li>{@code start} &mdash; initial value (integer of at most 9 digits, may be
 * negative)</li>
 * <li>{@code step} &mdash; increment per replacement (integer of at most 9
 * digits, may be negative for descending sequences)</li>
 * <li>{@code pad} &mdash; minimum number of digits, zero-padded, not counting
 * a minus sign (0 = no padding, at most {@value #MAX_PAD})</li>
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
 * {@code Match1_02}, &hellip; Only the first placeholder in a replace string is
 * expanded. Since the counter value is inserted before the replace string is
 * interpreted as a regular expression replacement, a {@code $} or {@code \}
 * directly in front of the placeholder must be escaped (e.g.
 * {@code \$#{1,1,0}}), otherwise it forms a group reference with the counter
 * value.
 */
public final class ReplaceCounter {

	/** Maximum supported value for the {@code pad} parameter. */
	static final int MAX_PAD = 20;

	/**
	 * Pattern matching the counter placeholder {@code #{start,step,pad}}.
	 * Captures: group 1 = start, group 2 = step, group 3 = pad. The number of
	 * digits is limited so that the values always fit into an {@code int}.
	 */
	static final Pattern COUNTER_PATTERN = Pattern.compile("#\\{(-?\\d{1,9}),(-?\\d{1,9}),(\\d{1,2})\\}"); //$NON-NLS-1$

	private final int start;
	private final int step;
	private final int pad;
	private long current;

	/**
	 * Creates a counter with the given parameters.
	 *
	 * @param start initial counter value
	 * @param step  increment applied after each {@link #next()} call
	 * @param pad   minimum digit width (0 = no padding), limited to
	 *              {@value #MAX_PAD}
	 */
	public ReplaceCounter(int start, int step, int pad) {
		this.start = start;
		this.step = step;
		this.pad = Math.max(0, Math.min(pad, MAX_PAD));
		this.current = start;
	}

	/** Resets the counter to the initial {@code start} value. */
	public void reset() {
		current = start;
	}

	/**
	 * Returns the current counter value as a (possibly zero-padded) string, then
	 * advances the counter by {@code step}. A minus sign is not counted as a
	 * digit, i.e. {@code -5} with a padding of 3 is formatted as {@code -005}.
	 *
	 * @return formatted counter value
	 */
	public String next() {
		long value = current;
		current += step;
		String digits = Long.toString(Math.abs(value));
		StringBuilder result = new StringBuilder(Math.max(pad, digits.length()) + 1);
		if (value < 0) {
			result.append('-');
		}
		for (int i = digits.length(); i < pad; i++) {
			result.append('0');
		}
		return result.append(digits).toString();
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
			int pad = Integer.parseInt(matcher.group(3));
			return new ReplaceCounter(start, step, pad);
		}
		return null;
	}
}
