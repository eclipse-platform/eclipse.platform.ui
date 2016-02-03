/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.tests.revisions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

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
	public void testIllegalOperations() throws Exception {

		try {
			Range.copy(null);
			fail();
		} catch (NullPointerException e) {
		}

		try {
			Range.createRelative(0, 0);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
		}

		try {
			Range.createRelative(0, -1);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
		}

		try {
			Range.createRelative(-1, 0);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
		}

		try {
			Range.createRelative(-1, -1);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
		}

		try {
			Range.createAbsolute(0, 0);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
		}

		try {
			Range.createAbsolute(0, -1);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
		}

		try {
			Range.createAbsolute(-1, 0);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
		}

		try {
			Range.createAbsolute(-1, 12);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
		}

		try {
			Range.createAbsolute(10, 10);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
		}

		try {
			Range.createAbsolute(12, 10);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
		}

		try {
			Range.createAbsolute(12, -3);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
		}

		Range r= Range.createRelative(5, 10);

		try {
			r.moveBy(-6);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
			assertEquals(5, r.start());
			assertEquals(10, r.length());
			assertConsistency(r);
		}

		r.moveBy(-4);
		try {
			r.moveBy(-2);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
			r.moveBy(4);
			assertEquals(5, r.start());
			assertEquals(10, r.length());
			assertConsistency(r);
		}

		try {
			r.resizeBy(-11);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
			assertEquals(5, r.start());
			assertEquals(10, r.length());
			assertConsistency(r);
		}

		try {
			r.resizeAndMoveBy(-6);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
			assertEquals(5, r.start());
			assertEquals(10, r.length());
			assertConsistency(r);
		}

		try {
			r.resizeAndMoveBy(10);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
			assertEquals(5, r.start());
			assertEquals(10, r.length());
			assertConsistency(r);
		}

		try {
			r.resizeAndMoveBy(11);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
			assertEquals(5, r.start());
			assertEquals(10, r.length());
			assertConsistency(r);
		}

		try {
			r.resizeAndMoveBy(20);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
			assertEquals(5, r.start());
			assertEquals(10, r.length());
			assertConsistency(r);
		}

		try {
			r.setLength(0);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
			assertEquals(5, r.start());
			assertEquals(10, r.length());
			assertConsistency(r);
		}

		try {
			r.setLength(-1);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
			assertEquals(5, r.start());
			assertEquals(10, r.length());
			assertConsistency(r);
		}

		try {
			r.moveTo(-1);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
			assertEquals(5, r.start());
			assertEquals(10, r.length());
			assertConsistency(r);
		}

		try {
			r.setEnd(5);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
			assertEquals(5, r.start());
			assertEquals(10, r.length());
			assertConsistency(r);
		}

		try {
			r.setEnd(3);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
			assertEquals(5, r.start());
			assertEquals(10, r.length());
			assertConsistency(r);
		}

		try {
			r.setEnd(-5);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
			assertEquals(5, r.start());
			assertEquals(10, r.length());
			assertConsistency(r);
		}

		try {
			r.setStart(18);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
			assertEquals(5, r.start());
			assertEquals(10, r.length());
			assertConsistency(r);
		}

		try {
			r.moveEndTo(9);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
			assertEquals(5, r.start());
			assertEquals(10, r.length());
			assertConsistency(r);
		}

		try {
			r.setLengthAndMove(16);
			fail();
		} catch (LineIndexOutOfBoundsException e) {
			assertEquals(5, r.start());
			assertEquals(10, r.length());
			assertConsistency(r);
		}
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
