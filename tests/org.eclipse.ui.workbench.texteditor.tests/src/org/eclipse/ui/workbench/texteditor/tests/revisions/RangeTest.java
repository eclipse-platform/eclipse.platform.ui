/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.tests.revisions;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.eclipse.jface.internal.text.revisions.LineIndexOutOfBoundsException;
import org.eclipse.jface.internal.text.revisions.Range;

import org.eclipse.jface.text.source.LineRange;

public class RangeTest {

	@Test
	public void testLegalOperations() {
		Range r= Range.createRelative(0, 1);
		assertEquals(0, r.start());
		assertEquals(1, r.length());
		assertConsistency(r);

		r= Range.createAbsolute(5, 6);
		assertEquals(5, r.start());
		assertEquals(1, r.length());
		assertConsistency(r);

		r= Range.copy(new LineRange(5, 1));
		assertEquals(5, r.start());
		assertEquals(1, r.length());
		assertConsistency(r);

		r= Range.createRelative(5, 1);
		assertEquals(5, r.start());
		assertEquals(1, r.length());
		assertConsistency(r);

		r.moveBy(10);
		assertEquals(15, r.start());
		assertEquals(1, r.length());
		assertConsistency(r);

		r.moveBy(-8);
		assertEquals(7, r.start());
		assertEquals(1, r.length());
		assertConsistency(r);

		r.moveTo(12);
		assertEquals(12, r.start());
		assertEquals(1, r.length());
		assertConsistency(r);

		r.resizeBy(4);
		assertEquals(12, r.start());
		assertEquals(5, r.length());
		assertConsistency(r);

		r.resizeAndMoveBy(3);
		assertEquals(15, r.start());
		assertEquals(2, r.length());
		assertConsistency(r);

		r.resizeAndMoveBy(-3);
		assertEquals(12, r.start());
		assertEquals(5, r.length());
		assertConsistency(r);

		r.setLength(3);
		assertEquals(12, r.start());
		assertEquals(3, r.length());
		assertConsistency(r);

		r.resizeBy(13);
		assertEquals(12, r.start());
		assertEquals(16, r.length());
		assertConsistency(r);

		r.resizeBy(-4);
		assertEquals(12, r.start());
		assertEquals(12, r.length());
		assertConsistency(r);

		r.setEnd(18);
		assertEquals(12, r.start());
		assertEquals(6, r.length());
		assertConsistency(r);

		r.moveEndTo(13);
		assertEquals(7, r.start());
		assertEquals(6, r.length());
		assertConsistency(r);

		r.setLengthAndMove(3);
		assertEquals(10, r.start());
		assertEquals(3, r.length());
		assertConsistency(r);

		r.setStart(7);
		assertEquals(7, r.start());
		assertEquals(6, r.length());
		assertConsistency(r);
	}

	@Test
	public void testSplit() throws Exception {
		Range r= Range.createRelative(12, 18);
		Range second= r.split(8);
		assertEquals(12, r.start());
		assertEquals(8, r.length());
		assertConsistency(r);

		assertEquals(20, second.start());
		assertEquals(10, second.length());
		assertConsistency(second);
	}

	@Test
	public void testIllegalOperations() {

		assertThrows(NullPointerException.class, () -> Range.copy(null));

		assertThrows(LineIndexOutOfBoundsException.class, () -> Range.createRelative(0, 0));

		assertThrows(LineIndexOutOfBoundsException.class, () -> Range.createRelative(0, -1));

		assertThrows(LineIndexOutOfBoundsException.class, () -> Range.createRelative(-1, 0));

		assertThrows(LineIndexOutOfBoundsException.class, () -> Range.createRelative(-1, -1));

		assertThrows(LineIndexOutOfBoundsException.class, () -> Range.createAbsolute(0, 0));

		assertThrows(LineIndexOutOfBoundsException.class, () -> Range.createAbsolute(0, -1));

		assertThrows(LineIndexOutOfBoundsException.class, () -> Range.createAbsolute(-1, 0));

		assertThrows(LineIndexOutOfBoundsException.class, () -> Range.createAbsolute(-1, 12));

		assertThrows(LineIndexOutOfBoundsException.class, () -> Range.createAbsolute(10, 10));

		assertThrows(LineIndexOutOfBoundsException.class, () -> Range.createAbsolute(12, 10));

		assertThrows(LineIndexOutOfBoundsException.class, () -> Range.createAbsolute(12, -3));

		Range r= Range.createRelative(5, 10);

		assertThrows(LineIndexOutOfBoundsException.class, () -> r.moveBy(-6));
		assertEquals(5, r.start());
		assertEquals(10, r.length());
		assertConsistency(r);

		r.moveBy(-4);
		assertThrows(LineIndexOutOfBoundsException.class, () -> r.moveBy(-2));
		r.moveBy(4);
		assertEquals(5, r.start());
		assertEquals(10, r.length());
		assertConsistency(r);

		assertThrows(LineIndexOutOfBoundsException.class, () -> r.resizeBy(-11));
		assertEquals(5, r.start());
		assertEquals(10, r.length());
		assertConsistency(r);

		assertThrows(LineIndexOutOfBoundsException.class, () -> r.resizeAndMoveBy(-6));
		assertEquals(5, r.start());
		assertEquals(10, r.length());
		assertConsistency(r);

		assertThrows(LineIndexOutOfBoundsException.class, () -> r.resizeAndMoveBy(10));
		assertEquals(5, r.start());
		assertEquals(10, r.length());
		assertConsistency(r);

		assertThrows(LineIndexOutOfBoundsException.class, () -> r.resizeAndMoveBy(11));
		assertEquals(5, r.start());
		assertEquals(10, r.length());
		assertConsistency(r);

		assertThrows(LineIndexOutOfBoundsException.class, () -> r.resizeAndMoveBy(20));
		assertEquals(5, r.start());
		assertEquals(10, r.length());
		assertConsistency(r);

		assertThrows(LineIndexOutOfBoundsException.class, () -> r.setLength(0));
		assertEquals(5, r.start());
		assertEquals(10, r.length());
		assertConsistency(r);

		assertThrows(LineIndexOutOfBoundsException.class, () -> r.setLength(-1));
		assertEquals(5, r.start());
		assertEquals(10, r.length());
		assertConsistency(r);

		assertThrows(LineIndexOutOfBoundsException.class, () -> r.moveTo(-1));
		assertEquals(5, r.start());
		assertEquals(10, r.length());
		assertConsistency(r);

		assertThrows(LineIndexOutOfBoundsException.class, () -> r.setEnd(5));
		assertEquals(5, r.start());
		assertEquals(10, r.length());
		assertConsistency(r);

		assertThrows(LineIndexOutOfBoundsException.class, () -> r.setEnd(3));
		assertEquals(5, r.start());
		assertEquals(10, r.length());
		assertConsistency(r);

		assertThrows(LineIndexOutOfBoundsException.class, () -> r.setEnd(-5));
		assertEquals(5, r.start());
		assertEquals(10, r.length());
		assertConsistency(r);

		assertThrows(LineIndexOutOfBoundsException.class, () -> r.setStart(18));
		assertEquals(5, r.start());
		assertEquals(10, r.length());
		assertConsistency(r);

		assertThrows(LineIndexOutOfBoundsException.class, () -> r.moveEndTo(9));
		assertEquals(5, r.start());
		assertEquals(10, r.length());
		assertConsistency(r);

		assertThrows(LineIndexOutOfBoundsException.class, () -> r.setLengthAndMove(16));
		assertEquals(5, r.start());
		assertEquals(10, r.length());
		assertConsistency(r);
	}

	@Test
	public void testIllegalSplit() throws Exception {
		Range r= Range.createRelative(12, 18);

		try {
			r.split(-1);
		} catch (LineIndexOutOfBoundsException e) {
			assertEquals(12, r.start());
			assertEquals(18, r.length());
			assertConsistency(r);
		}

		try {
			r.split(0);
		} catch (LineIndexOutOfBoundsException e) {
			assertEquals(12, r.start());
			assertEquals(18, r.length());
			assertConsistency(r);
		}

		try {
			r.split(18);
		} catch (LineIndexOutOfBoundsException e) {
			assertEquals(12, r.start());
			assertEquals(18, r.length());
			assertConsistency(r);
		}

		try {
			r.split(20);
		} catch (LineIndexOutOfBoundsException e) {
			assertEquals(12, r.start());
			assertEquals(18, r.length());
			assertConsistency(r);
		}
	}

	private static void assertConsistency(Range r) {
		assertEquals(r, r);
		assertTrue(r.equalRange(Range.copy(r)));
		assertTrue(r.equalRange(Range.createRelative(r.start(), r.length())));
		assertTrue(r.equalRange(Range.createAbsolute(r.start(), r.end())));
		assertEquals(r.getStartLine(), r.start());
		assertEquals(r.getNumberOfLines(), r.length());
		assertEquals(r.start() + r.length(), r.end());
	}

}