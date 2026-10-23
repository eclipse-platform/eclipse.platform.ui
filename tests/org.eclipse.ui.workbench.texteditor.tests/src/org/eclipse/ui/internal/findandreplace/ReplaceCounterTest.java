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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ReplaceCounterTest {

	// -----------------------------------------------------------------------
	// containsCounter
	// -----------------------------------------------------------------------

	@Test
	public void containsCounter_returnsTrue_whenPlaceholderPresent() {
		assertTrue(ReplaceCounter.containsCounter("item_#{1,1,3}"));
	}

	@Test
	public void containsCounter_returnsFalse_whenNoPlaceholder() {
		assertFalse(ReplaceCounter.containsCounter("plain text"));
	}

	@Test
	public void containsCounter_returnsFalse_forEmptyString() {
		assertFalse(ReplaceCounter.containsCounter(""));
	}

	@Test
	public void containsCounter_returnsFalse_forIncompleteSyntax() {
		assertFalse(ReplaceCounter.containsCounter("#{1,1}"));   // missing pad
		assertFalse(ReplaceCounter.containsCounter("{1,1,3}"));  // missing #
	}

	// -----------------------------------------------------------------------
	// parse
	// -----------------------------------------------------------------------

	@Test
	public void parse_returnsNull_whenNoPlaceholder() {
		assertNull(ReplaceCounter.parse("no counter here"));
	}

	@Test
	public void parse_returnsCounter_withCorrectParameters() {
		ReplaceCounter counter = ReplaceCounter.parse("#{1,1,3}");
		assertNotNull(counter);
		// Verify behaviour rather than private state:
		assertEquals("001", counter.next());
		assertEquals("002", counter.next());
	}

	@Test
	public void parse_handlesNegativeStep() {
		ReplaceCounter counter = ReplaceCounter.parse("#{100,-1,0}");
		assertNotNull(counter);
		assertEquals("100", counter.next());
		assertEquals("99", counter.next());
	}

	@Test
	public void parse_handlesNegativeStart() {
		ReplaceCounter counter = ReplaceCounter.parse("#{-5,5,0}");
		assertNotNull(counter);
		assertEquals("-5", counter.next());
		assertEquals("0", counter.next());
		assertEquals("5", counter.next());
	}

	// -----------------------------------------------------------------------
	// next / padding
	// -----------------------------------------------------------------------

	@Test
	public void next_producesZeroPaddedValues() {
		ReplaceCounter counter = new ReplaceCounter(1, 1, 3);
		assertEquals("001", counter.next());
		assertEquals("002", counter.next());
		assertEquals("003", counter.next());
	}

	@Test
	public void next_producesUnpaddedValues_whenPadZero() {
		ReplaceCounter counter = new ReplaceCounter(10, 5, 0);
		assertEquals("10", counter.next());
		assertEquals("15", counter.next());
		assertEquals("20", counter.next());
	}

	@Test
	public void next_descending() {
		ReplaceCounter counter = new ReplaceCounter(5, -1, 0);
		assertEquals("5", counter.next());
		assertEquals("4", counter.next());
		assertEquals("3", counter.next());
	}

	@Test
	public void next_largeStep() {
		ReplaceCounter counter = new ReplaceCounter(0, 100, 0);
		assertEquals("0", counter.next());
		assertEquals("100", counter.next());
	}

	// -----------------------------------------------------------------------
	// reset
	// -----------------------------------------------------------------------

	@Test
	public void reset_restoresStartValue() {
		ReplaceCounter counter = new ReplaceCounter(1, 1, 2);
		counter.next();
		counter.next();
		counter.reset();
		assertEquals("01", counter.next());
	}

	// -----------------------------------------------------------------------
	// expand
	// -----------------------------------------------------------------------

	@Test
	public void expand_replacesSinglePlaceholder() {
		ReplaceCounter counter = new ReplaceCounter(1, 1, 3);
		assertEquals("item_001_end", counter.expand("item_#{1,1,3}_end"));
		assertEquals("item_002_end", counter.expand("item_#{1,1,3}_end"));
	}

	@Test
	public void expand_returnsTemplateUnchanged_whenNoPlaceholder() {
		ReplaceCounter counter = new ReplaceCounter(1, 1, 0);
		String plain = "no placeholder here";
		assertEquals(plain, counter.expand(plain));
	}

	@Test
	public void expand_doesNotConsumeTick_whenNoPlaceholder() {
		ReplaceCounter counter = new ReplaceCounter(1, 1, 0);
		counter.expand("no placeholder");
		// counter must not have moved
		assertEquals("1", counter.next());
	}
}
