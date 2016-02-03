/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.tests.revisions;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.graphics.RGB;

import org.eclipse.jface.internal.text.revisions.ChangeRegion;
import org.eclipse.jface.internal.text.revisions.Hunk;
import org.eclipse.jface.internal.text.revisions.Range;

import org.eclipse.jface.text.revisions.Revision;
import org.eclipse.jface.text.source.LineRange;

/**
 *
 * @since 3.2
 */
public class ChangeRegionTest {

	public static final class TestRevision extends Revision {
		@Override
		public Object getHoverInfo() {
			return null;
		}

		@Override
		public RGB getColor() {
			return null;
		}

		@Override
		public String getId() {
			return null;
		}

		@Override
		public Date getDate() {
			return null;
		}
	}

	private Revision fRevision;

	@Before
	public void setUp() throws Exception {
		fRevision= new TestRevision();
	}

	@Test
	public void testCreation() throws Exception {
		try {
			new ChangeRegion(fRevision, null);
			fail();
		} catch (Exception e) {
		}

		try {
			new ChangeRegion(null, new LineRange(12, 3));
			fail();
		} catch (Exception e) {
		}

		try {
			new ChangeRegion(null, null);
			fail();
		} catch (Exception e) {
		}

		ChangeRegion r= new ChangeRegion(fRevision, new LineRange(12, 3));
		assertEquals(fRevision, r.getRevision());
		RangeUtil.assertEqualRange(new LineRange(12, 3), r.getAdjustedRanges().get(0));
		RangeUtil.assertEqualRange(new LineRange(12, 3), r.getAdjustedCoverage());
	}

	@Test
	public void testHunkAfter() throws Exception {
		ChangeRegion r= new ChangeRegion(fRevision, new LineRange(12, 3));
		List<Range> before= RangeUtil.deepClone(r.getAdjustedRanges());
		r.adjustTo(new Hunk(16, 3, 2));
		RangeUtil.assertEqualRanges(before, r.getAdjustedRanges());
		RangeUtil.assertEqualRange(before.get(0), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 3));
		before= RangeUtil.deepClone(r.getAdjustedRanges());
		r.adjustTo(new Hunk(16, -33, 2));
		RangeUtil.assertEqualRanges(before, r.getAdjustedRanges());
		RangeUtil.assertEqualRange(before.get(0), r.getAdjustedCoverage());
	}

	@Test
	public void testHunkRightAfter() throws Exception {
		ChangeRegion r= new ChangeRegion(fRevision, new LineRange(12, 3));
		List<Range> before= RangeUtil.deepClone(r.getAdjustedRanges());
		r.adjustTo(new Hunk(15, 3, 2));
		RangeUtil.assertEqualRanges(before, r.getAdjustedRanges());
		RangeUtil.assertEqualRange(before.get(0), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 3));
		before= RangeUtil.deepClone(r.getAdjustedRanges());
		r.adjustTo(new Hunk(15, -3, 2));
		RangeUtil.assertEqualRanges(before, r.getAdjustedRanges());
		RangeUtil.assertEqualRange(before.get(0), r.getAdjustedCoverage());
	}

	@Test
	public void testHunkBefore() throws Exception {
		ChangeRegion r;

		r= new ChangeRegion(fRevision, new LineRange(12, 3));
		r.adjustTo(new Hunk(5, 3, 2));
		RangeUtil.assertEqualSingleRange(new LineRange(15, 3), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(15, 3), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 3));
		r.adjustTo(new Hunk(5, 3, 0));
		RangeUtil.assertEqualSingleRange(new LineRange(15, 3), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(15, 3), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 3));
		r.adjustTo(new Hunk(5, -3, 0));
		RangeUtil.assertEqualSingleRange(new LineRange(9, 3), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(9, 3), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 3));
		r.adjustTo(new Hunk(5, 3, 3));
		RangeUtil.assertEqualSingleRange(new LineRange(15, 3), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(15, 3), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 3));
		r.adjustTo(new Hunk(5, -3, 2));
		RangeUtil.assertEqualSingleRange(new LineRange(9, 3), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(9, 3), r.getAdjustedCoverage());
	}

	@Test
	public void testHunkRightBefore() throws Exception {
		ChangeRegion r;

		r= new ChangeRegion(fRevision, new LineRange(12, 3));
		r.adjustTo(new Hunk(10, 0 , 2));
		RangeUtil.assertEqualSingleRange(new LineRange(12, 3), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(12, 3), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 3));
		r.adjustTo(new Hunk(10, 2 , 2));
		RangeUtil.assertEqualSingleRange(new LineRange(14, 3), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(14, 3), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 3));
		r.adjustTo(new Hunk(9, -3, 0));
		RangeUtil.assertEqualSingleRange(new LineRange(9, 3), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(9, 3), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 3));
		r.adjustTo(new Hunk(9, -1, 2));
		RangeUtil.assertEqualSingleRange(new LineRange(11, 3), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(11, 3), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 3));
		r.adjustTo(new Hunk(9, 3, 3));
		RangeUtil.assertEqualSingleRange(new LineRange(15, 3), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(15, 3), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 3));
		r.adjustTo(new Hunk(12, 3, 0));
		RangeUtil.assertEqualSingleRange(new LineRange(15, 3), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(15, 3), r.getAdjustedCoverage());
	}

	@Test
	public void testHunkAtStart() throws Exception {
		ChangeRegion r;

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(12, 0 , 2));
		RangeUtil.assertEqualSingleRange(new LineRange(14, 5), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(14, 5), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(12, -2, 0));
		RangeUtil.assertEqualSingleRange(new LineRange(12, 5), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(12, 5), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(12, -2, 1));
		RangeUtil.assertEqualSingleRange(new LineRange(13, 4), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(13, 4), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(12, 3, 0));
		RangeUtil.assertEqualSingleRange(new LineRange(15, 7), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(15, 7), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(12, 3, 2));
		RangeUtil.assertEqualSingleRange(new LineRange(17, 5), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(17, 5), r.getAdjustedCoverage());
	}

	@Test
	public void testHunkAtEnd() throws Exception {
		ChangeRegion r;

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(17, 0, 2));
		RangeUtil.assertEqualSingleRange(new LineRange(12, 5), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(12, 5), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(17, 2, 2));
		RangeUtil.assertEqualSingleRange(new LineRange(12, 5), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(12, 5), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(15, -2, 2));
		RangeUtil.assertEqualSingleRange(new LineRange(12, 3), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(12, 3), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(17, -2, 0));
		RangeUtil.assertEqualSingleRange(new LineRange(12, 5), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(12, 5), r.getAdjustedCoverage());
	}
	
	@Test
	public void testHunkOverStart() throws Exception {
		ChangeRegion r;

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(11, 0 , 2));
		RangeUtil.assertEqualSingleRange(new LineRange(13, 6), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(13, 6), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(11, 2, 2));
		RangeUtil.assertEqualSingleRange(new LineRange(15, 6), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(15, 6), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(11, -2, 2));
		RangeUtil.assertEqualSingleRange(new LineRange(13, 4), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(13, 4), r.getAdjustedCoverage());
	}

	@Test
	public void testHunkOverEnd() throws Exception {
		ChangeRegion r;

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(18, 0 , 2));
		RangeUtil.assertEqualSingleRange(new LineRange(12, 6), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(12, 6), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(18, -2, 0));
		RangeUtil.assertEqualSingleRange(new LineRange(12, 6), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(12, 6), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(18, -2, 2));
		RangeUtil.assertEqualSingleRange(new LineRange(12, 6), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(12, 6), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(17, -2, 1));
		RangeUtil.assertEqualSingleRange(new LineRange(12, 5), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(12, 5), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(17, -2, 3));
		RangeUtil.assertEqualSingleRange(new LineRange(12, 5), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(12, 5), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(17, 2, 3));
		RangeUtil.assertEqualSingleRange(new LineRange(12, 5), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(12, 5), r.getAdjustedCoverage());
	}

	@Test
	public void testHunkCovering() throws Exception {
		ChangeRegion r;

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(12, 0 , 7));
		assertTrue(r.getAdjustedRanges().isEmpty());
		RangeUtil.assertEqualRange(new LineRange(12, 0), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(11, 0 , 8));
		assertTrue(r.getAdjustedRanges().isEmpty());
		RangeUtil.assertEqualRange(new LineRange(12, 0), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(11, 0 , 9));
		assertTrue(r.getAdjustedRanges().isEmpty());
		RangeUtil.assertEqualRange(new LineRange(12, 0), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(12, 0 , 9));
		assertTrue(r.getAdjustedRanges().isEmpty());
		RangeUtil.assertEqualRange(new LineRange(12, 0), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(12, -7, 0));
		assertTrue(r.getAdjustedRanges().isEmpty());
		RangeUtil.assertEqualRange(new LineRange(12, 0), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(12, -8, 0));
		assertTrue(r.getAdjustedRanges().isEmpty());
		RangeUtil.assertEqualRange(new LineRange(12, 0), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(11, -8, 0));
		assertTrue(r.getAdjustedRanges().isEmpty());
		RangeUtil.assertEqualRange(new LineRange(12, 0), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(11, -9, 0));
		assertTrue(r.getAdjustedRanges().isEmpty());
		RangeUtil.assertEqualRange(new LineRange(12, 0), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(8, -9, 6));
		assertTrue(r.getAdjustedRanges().isEmpty());
		RangeUtil.assertEqualRange(new LineRange(12, 0), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(8, -4, 8));
		assertTrue(r.getAdjustedRanges().isEmpty());
		RangeUtil.assertEqualRange(new LineRange(12, 0), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(8, -3, 9));
		assertTrue(r.getAdjustedRanges().isEmpty());
		RangeUtil.assertEqualRange(new LineRange(12, 0), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(8, -12, 5));
		assertTrue(r.getAdjustedRanges().isEmpty());
		RangeUtil.assertEqualRange(new LineRange(12, 0), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(8, -12, 20));
		assertTrue(r.getAdjustedRanges().isEmpty());
		RangeUtil.assertEqualRange(new LineRange(12, 0), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(8, 12, 20));
		assertTrue(r.getAdjustedRanges().isEmpty());
		RangeUtil.assertEqualRange(new LineRange(12, 0), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(8, -3, 8));
		assertTrue(r.getAdjustedRanges().isEmpty());
		RangeUtil.assertEqualRange(new LineRange(12, 0), r.getAdjustedCoverage());
	}

	@Test
	public void testHunkInBetween() throws Exception {
		// TODO require merging of adjacent ranges?
		ChangeRegion r;

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(15, -2, 0));
		RangeUtil.assertEqualRanges(new LineRange(12, 3), new LineRange(15, 2), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(12, 5), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(15, 2, 0));
		RangeUtil.assertEqualRanges(new LineRange(12, 3), new LineRange(17, 4), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(12, 9), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(15, 0, 2));
		RangeUtil.assertEqualRanges(new LineRange(12, 3), new LineRange(17, 2), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(12, 7), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(15, -1, 2));
		RangeUtil.assertEqualRanges(new LineRange(12, 3), new LineRange(17, 1), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(12, 6), r.getAdjustedCoverage());

		r= new ChangeRegion(fRevision, new LineRange(12, 7));
		r.adjustTo(new Hunk(15, 2, 2));
		RangeUtil.assertEqualRanges(new LineRange(12, 3), new LineRange(19, 2), r.getAdjustedRanges());
		RangeUtil.assertEqualRange(new LineRange(12, 9), r.getAdjustedCoverage());
	}
}
