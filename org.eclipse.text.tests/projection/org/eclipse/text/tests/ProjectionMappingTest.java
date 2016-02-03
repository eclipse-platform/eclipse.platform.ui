/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.projection.Fragment;
import org.eclipse.jface.text.projection.ProjectionMapping;
import org.eclipse.jface.text.projection.Segment;

/**
 * @since 3.0
 */
public class ProjectionMappingTest {

	private IDocument fMasterDocument;
	private IDocument fSlaveDocument;
	private String fFragmentsCategory;
	private String fSegmentsCategory;
	private ProjectionMapping fProjectionMapping;


	private String getOriginalMasterContent() {
		return
			"1111111111111111111\n" +
			"2222222222222222222\n" +
			"3333333333333333333\n" +
			"4444444444444444444\n" +
			"5555555555555555555\n" +
			"6666666666666666666\n" +
			"7777777777777777777\n" +
			"8888888888888888888\n" +
			"99999999999999999999";
	}

	private String getOriginalSlaveContent() {
		StringBuffer buffer= new StringBuffer(getOriginalMasterContent());
		buffer.delete(80, 180);
		buffer.delete(40, 60);
		buffer.delete(0, 20);
		return buffer.toString();
	}

	private String getLineWrappingSlaveContent() {
		StringBuffer buffer= new StringBuffer(getOriginalMasterContent());
		buffer.delete(80, 180);
		buffer.delete(50, 70); // ...333444...
		buffer.delete(10, 30); // ...111222...
		return buffer.toString(); // "1111111111222222222\n3333333333444444444\n"
	}

	private void addProjection(int fragmentOffset, int segmentOffset, int length) {
		Fragment fragment= new Fragment(fragmentOffset, length);
		Segment segment= new Segment(segmentOffset, length);
		fragment.segment= segment;
		segment.fragment= fragment;
		try {
			fMasterDocument.addPosition(fFragmentsCategory, fragment);
			fSlaveDocument.addPosition(fSegmentsCategory, segment);
		} catch (BadLocationException e) {
			assertTrue(false);
		} catch (BadPositionCategoryException e) {
			assertTrue(false);
		}
	}

	private void createStandardProjection() {
		fMasterDocument.set(getOriginalMasterContent());
		fSlaveDocument.set(getOriginalSlaveContent());
		addProjection(20, 0, 20);
		addProjection(60, 20, 20);
	}

	private void createIdenticalProjection() {
		fMasterDocument.set(getOriginalMasterContent());
		fSlaveDocument.set(getOriginalMasterContent());
		addProjection(0, 0, fMasterDocument.getLength());
	}

	private void createLineWrappingProjection() {
		fMasterDocument.set(getOriginalMasterContent());
		fSlaveDocument.set(getLineWrappingSlaveContent());
		addProjection(0, 0, 10);
		addProjection(30, 10, 20);
		addProjection(70, 30, 10);
	}

	@Before
	public void setUp() {
		fMasterDocument= new Document();
		fSlaveDocument= new Document();
		fFragmentsCategory= "_fragments" + fSlaveDocument.hashCode();
		fSegmentsCategory= "_segments" + fMasterDocument.hashCode();
		fMasterDocument.addPositionCategory(fFragmentsCategory);
		fSlaveDocument.addPositionCategory(fSegmentsCategory);
		fProjectionMapping= new ProjectionMapping(fMasterDocument, fFragmentsCategory, fSlaveDocument, fSegmentsCategory);
	}


	@After
	public void tearDown() {
		fMasterDocument= null;
		fSlaveDocument= null;
		fFragmentsCategory= null;
		fSegmentsCategory= null;
		fProjectionMapping= null;
	}

	@Test
	public void test1() {
		// test getCoverage

		createStandardProjection();
		IRegion coverage= fProjectionMapping.getCoverage();
		assertTrue(coverage.getOffset() == 20);
		assertTrue(coverage.getLength() == 60);
	}

	@Test
	public void test2() {
		// test toOriginOffset

		createStandardProjection();
		try {
			assertEquals(20, fProjectionMapping.toOriginOffset(0));
			assertEquals(25, fProjectionMapping.toOriginOffset(5));
			assertEquals(60, fProjectionMapping.toOriginOffset(20));
			assertEquals(65, fProjectionMapping.toOriginOffset(25));
			assertEquals(80, fProjectionMapping.toOriginOffset(40));
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		try {
			fProjectionMapping.toOriginOffset(41);
			assertTrue(false);
		} catch (BadLocationException e) {
		}
	}

	@Test
	public void test3a() {
		// test toOriginRegion
		// image region inside segment

		createStandardProjection();
		try {
			IRegion origin= fProjectionMapping.toOriginRegion(new Region(5, 10));
			assertEquals(new Region(25, 10), origin);
			origin= fProjectionMapping.toOriginRegion(new Region(25, 10));
			assertEquals(new Region(65, 10), origin);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test3b() {
		// test toOriginRegion
		// image region is segment

		createStandardProjection();
		try {
			IRegion origin= fProjectionMapping.toOriginRegion(new Region(0, 20));
			assertEquals(new Region(20, 20), origin);
			origin= fProjectionMapping.toOriginRegion(new Region(20, 20));
			assertEquals(new Region(60, 20), origin);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test3c() {
		// test toOriginRegion
		// image region overlapping segments
		createStandardProjection();
		try {
			IRegion origin= fProjectionMapping.toOriginRegion(new Region(10, 20));
			assertEquals(new Region(30, 40), origin);
			origin= fProjectionMapping.toOriginRegion(new Region(0, 40));
			assertEquals(new Region(20, 60), origin);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test3d() {
		// test toOriginRegion
		// test null projection

		try {
			IRegion origin= fProjectionMapping.toOriginRegion(new Region(0, 0));
			assertEquals(new Region(0, fMasterDocument.getLength()), origin);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		try {
			fProjectionMapping.toOriginRegion(new Region(0, 2));
			assertTrue(false);
		} catch (BadLocationException e) {
		}

		try {
			fProjectionMapping.toOriginRegion(new Region(2, 2));
			assertTrue(false);
		} catch (BadLocationException e) {
		}
	}

	@Test
	public void test3e() {
		// test toOriginRegion
		// identical projection

		createIdenticalProjection();
		try {
			IRegion origin= fProjectionMapping.toOriginRegion(new Region(0, 0));
			assertEquals(new Region(0, 0), origin);
			origin= fProjectionMapping.toOriginRegion(new Region(20, 40));
			assertEquals(new Region(20, 40), origin);
			origin= fProjectionMapping.toOriginRegion(new Region(fMasterDocument.getLength(), 0));
			assertEquals(new Region(fMasterDocument.getLength(), 0), origin);
			origin= fProjectionMapping.toOriginRegion(new Region(0, fMasterDocument.getLength()));
			assertEquals(new Region(0, fMasterDocument.getLength()), origin);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test3f() {
		// test toOriginRegion
		// test empty slave document

		fMasterDocument.set("abc\n");
		fSlaveDocument.set("");
		addProjection(4, 0, 0);

		try {
			IRegion origin= fProjectionMapping.toOriginRegion(new Region(0, 0));
			assertEquals(new Region(4, 0), origin); // fails, origin is (0, 4)
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test4() {
		// test toOriginLines

		createLineWrappingProjection();
		assertEquals(3, fSlaveDocument.getNumberOfLines());

		try {
			IRegion lines= fProjectionMapping.toOriginLines(0);
			assertEquals(new Region(0,2), lines);
			lines= fProjectionMapping.toOriginLines(1);
			assertEquals(new Region(2, 2), lines);
			lines= fProjectionMapping.toOriginLines(2);
			assertEquals(new Region(4, 1), lines);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test5a() {
		// test toOriginLine
		// test projection with no wrapped line

		createStandardProjection();
		assertEquals(3, fSlaveDocument.getNumberOfLines());

		try {
			assertEquals(1, fProjectionMapping.toOriginLine(0));
			assertEquals(3, fProjectionMapping.toOriginLine(1));
			assertEquals(4, fProjectionMapping.toOriginLine(2));
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test5b() {
		// test toOriginLine
		// test line wrapping projection

		createLineWrappingProjection();

		try {
			assertEquals(-1, fProjectionMapping.toOriginLine(0));
			assertEquals(-1, fProjectionMapping.toOriginLine(1));
			assertEquals(4, fProjectionMapping.toOriginLine(2));
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test6() {
		// test toImageOffset

		createStandardProjection();

		try {
			// test begin of slave document
			assertEquals(0, fProjectionMapping.toImageOffset(20));
			// test end of slave document
			assertEquals(39, fProjectionMapping.toImageOffset(79));
			assertEquals(40, fProjectionMapping.toImageOffset(80));
			// test begin of fragment
			assertEquals(20, fProjectionMapping.toImageOffset(60));
			// test end of fragment which is not the last fragment
			assertEquals(19, fProjectionMapping.toImageOffset(39));
			assertEquals(-1, fProjectionMapping.toImageOffset(40));
			// test middle of fragment
			assertEquals(10, fProjectionMapping.toImageOffset(30));
			// test in between two fragments
			assertEquals(-1, fProjectionMapping.toImageOffset(45));
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	private IRegion computeImageRegion(IRegion region, boolean exact) throws BadLocationException {
		if (exact)
			return fProjectionMapping.toExactImageRegion(region);
		return fProjectionMapping.toImageRegion(region);
	}

	private void commonSubSection_toImageRegion(boolean exact) {

		try {
			// test a region contained by a fragment
			IRegion imageRegion= computeImageRegion(new Region(25, 10), exact);
			assertEquals(new Region(5, 10), imageRegion);
			// test region of length 0
			imageRegion= computeImageRegion(new Region(25, 0), exact);
			assertEquals(new Region(5, 0), imageRegion);
			// test a complete fragment
			imageRegion= computeImageRegion(new Region(20, 20), exact);
			assertEquals(new Region(0, 20), imageRegion);
			// test a region spanning multiple fragments incompletely
			imageRegion= computeImageRegion(new Region(25, 50), exact);
			assertEquals(new Region(5, 30), imageRegion);
			// test a region spanning multiple fragments completely
			imageRegion= computeImageRegion(new Region(20, 60), exact);
			assertEquals(new Region(0, 40), imageRegion);
			// test a region non overlapping with a fragment
			imageRegion= computeImageRegion(new Region(45, 10), exact);
			assertEquals(null, imageRegion);
			// test a zero-length region at the end of the last fragment
			imageRegion= computeImageRegion(new Region(80, 0), exact);
			assertEquals(new Region(40, 0), imageRegion);
			// test a region at the end of the last fragment
			imageRegion= computeImageRegion(new Region(80, 10), exact);
			assertEquals(null, imageRegion);

		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test7() {
		// test toExactImageRegion

		createStandardProjection();
		commonSubSection_toImageRegion(true);

		try {
			// test a region surrounded by two fragments
			IRegion imageRegion= fProjectionMapping.toExactImageRegion(new Region(40, 20));
			assertEquals(null, imageRegion);
			// test a region starting in a fragment and ending outside a fragment
			imageRegion= fProjectionMapping.toExactImageRegion(new Region(25, 30));
			assertEquals(null, imageRegion);
			// test a region starting outside a fragment and ending inside a fragment
			imageRegion= fProjectionMapping.toExactImageRegion(new Region(45, 30));
			assertEquals(null, imageRegion);
			// test a region starting outside a fragment and ending outside a fragment (covering one)
			imageRegion= fProjectionMapping.toExactImageRegion(new Region(45, 50));
			assertEquals(null, imageRegion);
			// test a region starting outside a fragment and ending outside a fragment (covering two)
			imageRegion= fProjectionMapping.toExactImageRegion(new Region(15, 70));
			assertEquals(null, imageRegion);

		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test8() {
		// test toImageRegion

		createStandardProjection();
		commonSubSection_toImageRegion(false);

		try {
			// test a region surrounded by two fragments
			IRegion imageRegion= fProjectionMapping.toImageRegion(new Region(40, 20));
			assertEquals(null, imageRegion);
			// test a region starting in a fragment and ending outside a fragment
			imageRegion= fProjectionMapping.toImageRegion(new Region(25, 30));
			assertEquals(new Region(5, 15), imageRegion);
			// test a region starting outside a fragment and ending inside a fragment
			imageRegion= fProjectionMapping.toImageRegion(new Region(45, 30));
			assertEquals(new Region(20, 15), imageRegion);
			// test a region starting outside a fragment and ending outside a fragment (covering one)
			imageRegion= fProjectionMapping.toImageRegion(new Region(45, 50));
			assertEquals(new Region(20, 20), imageRegion);
			// test a region starting outside a fragment and ending outside a fragment (covering two)
			imageRegion= fProjectionMapping.toImageRegion(new Region(15, 70));
			assertEquals(new Region(0, 40), imageRegion);

		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test8b() {
		// test toImageRegion

		createStandardProjection();

		try {
			// test a region contained by a fragment
			IRegion imageRegion= fProjectionMapping.toClosestImageRegion(new Region(25, 10));
			assertEquals(new Region(5, 10), imageRegion);
			// test region of length 0
			imageRegion= fProjectionMapping.toClosestImageRegion(new Region(25, 0));
			assertEquals(new Region(5, 0), imageRegion);
			// test a complete fragment
			imageRegion= fProjectionMapping.toClosestImageRegion(new Region(20, 20));
			assertEquals(new Region(0, 20), imageRegion);
			// test a region spanning multiple fragments incompletely
			imageRegion= fProjectionMapping.toClosestImageRegion(new Region(25, 50));
			assertEquals(new Region(5, 30), imageRegion);
			// test a region spanning multiple fragments completely
			imageRegion= fProjectionMapping.toClosestImageRegion(new Region(20, 60));
			assertEquals(new Region(0, 40), imageRegion);
			// test a region non overlapping with a fragment
			imageRegion= fProjectionMapping.toClosestImageRegion(new Region(45, 10));
			assertEquals(new Region(20, 0), imageRegion);
			// test a zero-length region at the end of the last fragment
			imageRegion= fProjectionMapping.toClosestImageRegion(new Region(80, 0));
			assertEquals(new Region(40, 0), imageRegion);
			// test a region at the end of the last fragment
			imageRegion= fProjectionMapping.toClosestImageRegion(new Region(80, 10));
			assertEquals(new Region(40, 0), imageRegion);
			// test a region before the first fragment
			imageRegion= fProjectionMapping.toClosestImageRegion(new Region(10, 5));
			assertEquals(new Region(0, 0), imageRegion);
			// test a region surrounded by two fragments
			imageRegion= fProjectionMapping.toClosestImageRegion(new Region(40, 20));
			assertEquals(new Region(20, 0), imageRegion);
			// test a region starting in a fragment and ending outside a fragment
			imageRegion= fProjectionMapping.toClosestImageRegion(new Region(25, 30));
			assertEquals(new Region(5, 15), imageRegion);
			// test a region starting outside a fragment and ending inside a fragment
			imageRegion= fProjectionMapping.toClosestImageRegion(new Region(45, 30));
			assertEquals(new Region(20, 15), imageRegion);
			// test a region starting outside a fragment and ending outside a fragment (covering one)
			imageRegion= fProjectionMapping.toClosestImageRegion(new Region(45, 50));
			assertEquals(new Region(20, 20), imageRegion);
			// test a region starting outside a fragment and ending outside a fragment (covering two)
			imageRegion= fProjectionMapping.toClosestImageRegion(new Region(15, 70));
			assertEquals(new Region(0, 40), imageRegion);

		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test9a() {
		// test toImageLine
		// test standard line wrapping projection

		createLineWrappingProjection();
		try {
			assertEquals( 0, fProjectionMapping.toImageLine(0));
			assertEquals( 0, fProjectionMapping.toImageLine(1));
			assertEquals( 1, fProjectionMapping.toImageLine(2));
			assertEquals( 1, fProjectionMapping.toImageLine(3));
			assertEquals( 2, fProjectionMapping.toImageLine(4));
			assertEquals(-1, fProjectionMapping.toImageLine(5));
			assertEquals(-1, fProjectionMapping.toImageLine(6));
			assertEquals(-1, fProjectionMapping.toImageLine(7));
			assertEquals(-1, fProjectionMapping.toImageLine(8));
			assertEquals(-1, fProjectionMapping.toImageLine(9));
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test9b() {
		// test toImageLine
		// test non-line wrapping, well distributed projection of empty lines

		fMasterDocument.set("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
		fSlaveDocument.set("\n\n\n\n\n\n");
		addProjection(3, 0, 3);
		addProjection(9, 3, 3);

		assertEquals(16, fMasterDocument.getNumberOfLines());
		assertEquals(7, fSlaveDocument.getNumberOfLines());

		try {
			assertEquals(-1, fProjectionMapping.toImageLine(0));
			assertEquals(-1, fProjectionMapping.toImageLine(1));
			assertEquals(-1, fProjectionMapping.toImageLine(2));
			assertEquals( 0, fProjectionMapping.toImageLine(3));
			assertEquals( 1, fProjectionMapping.toImageLine(4));
			assertEquals( 2, fProjectionMapping.toImageLine(5));
			assertEquals(-1, fProjectionMapping.toImageLine(6));
			assertEquals(-1, fProjectionMapping.toImageLine(7));
			assertEquals(-1, fProjectionMapping.toImageLine(8));
			assertEquals( 3, fProjectionMapping.toImageLine(9));
			assertEquals( 4, fProjectionMapping.toImageLine(10));
			assertEquals( 5, fProjectionMapping.toImageLine(11));
			assertEquals( 6, fProjectionMapping.toImageLine(12));
			assertEquals(-1, fProjectionMapping.toImageLine(13));
			assertEquals(-1, fProjectionMapping.toImageLine(14));
			assertEquals(-1, fProjectionMapping.toImageLine(15));
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test10a() {
		// test toClosestImageLine
		// test standard line wrapping projection

		createLineWrappingProjection();
		try {
			assertEquals(0, fProjectionMapping.toClosestImageLine(0));
			assertEquals(0, fProjectionMapping.toClosestImageLine(1));
			assertEquals(1, fProjectionMapping.toClosestImageLine(2));
			assertEquals(1, fProjectionMapping.toClosestImageLine(3));
			assertEquals(2, fProjectionMapping.toClosestImageLine(4));
			assertEquals(2, fProjectionMapping.toClosestImageLine(5));
			assertEquals(2, fProjectionMapping.toClosestImageLine(6));
			assertEquals(2, fProjectionMapping.toClosestImageLine(7));
			assertEquals(2, fProjectionMapping.toClosestImageLine(8));
			assertEquals(2, fProjectionMapping.toClosestImageLine(9));
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test10b() {
		// test toClosestImageLine
		// test empty projection

		try {
			assertEquals(-1, fProjectionMapping.toClosestImageLine(0));
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test10c() {
		// test toClosestImageLine
		// test non-line wrapping, well distributed projection of empty lines

		fMasterDocument.set("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
//		fSlaveDocument.set("       \n\n\n      \n\n\n      ");
		fSlaveDocument.set("\n\n\n\n\n\n");
		addProjection(3, 0, 3);
		addProjection(9, 3, 3);

		assertEquals(16, fMasterDocument.getNumberOfLines());
		assertEquals(7, fSlaveDocument.getNumberOfLines());

		try {
			assertEquals(0, fProjectionMapping.toClosestImageLine(0));
			assertEquals(0, fProjectionMapping.toClosestImageLine(1));
			assertEquals(0, fProjectionMapping.toClosestImageLine(2));
			assertEquals(0, fProjectionMapping.toClosestImageLine(3));
			assertEquals(1, fProjectionMapping.toClosestImageLine(4));
			assertEquals(2, fProjectionMapping.toClosestImageLine(5));
			assertEquals(2, fProjectionMapping.toClosestImageLine(6));
			assertEquals(2, fProjectionMapping.toClosestImageLine(7));
			assertEquals(3, fProjectionMapping.toClosestImageLine(8));
			assertEquals(3, fProjectionMapping.toClosestImageLine(9));
			assertEquals(4, fProjectionMapping.toClosestImageLine(10));
			assertEquals(5, fProjectionMapping.toClosestImageLine(11));
			assertEquals(6, fProjectionMapping.toClosestImageLine(12));
			assertEquals(6, fProjectionMapping.toClosestImageLine(13));
			assertEquals(6, fProjectionMapping.toClosestImageLine(14));
			assertEquals(6, fProjectionMapping.toClosestImageLine(15));
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	private void assertRegions(IRegion[] expected, IRegion[] actual) {
		assertTrue("invalid number of regions", expected.length == actual.length);
		for (int i= 0; i < expected.length; i++)
			assertEquals(expected[i], actual[i]);
	}

	@Test
	public void test11a() {
		// test toExactOriginRegions
		// test the whole slave document

		createStandardProjection();

		try {
			IRegion[] actual= fProjectionMapping.toExactOriginRegions(new Region(0, fSlaveDocument.getLength()));
			IRegion[] expected= new IRegion[] {
				new Region(20, 20),
				new Region(60, 20)
			};
			assertRegions(expected, actual);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test11b() {
		// test toExactOriginRegions
		// test a region completely comprised by a segment

		createStandardProjection();

		try {
			IRegion[] actual= fProjectionMapping.toExactOriginRegions(new Region(5, 10));
			IRegion[] expected= new IRegion[] {
				new Region(25, 10)
			};
			assertRegions(expected, actual);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test11c() {
		// test toExactOriginRegions
		// test a region completely comprised by a segment at the beginning of a segment

		createStandardProjection();

		try {
			IRegion[] actual= fProjectionMapping.toExactOriginRegions(new Region(0, 10));
			IRegion[] expected= new IRegion[] {
				new Region(20, 10)
			};
			assertRegions(expected, actual);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test11d() {
		// test toExactOriginRegions
		// test a region completely comprised by a segment at the end of a segment

		createStandardProjection();

		try {
			IRegion[] actual= fProjectionMapping.toExactOriginRegions(new Region(10, 10));
			IRegion[] expected= new IRegion[] {
				new Region(30, 10)
			};
			assertRegions(expected, actual);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test11e() {
		// test toExactOriginRegions
		// test a complete segment

		createStandardProjection();

		try {
			IRegion[] actual= fProjectionMapping.toExactOriginRegions(new Region(0, 20));
			IRegion[] expected= new IRegion[] {
				new Region(20, 20)
			};
			assertRegions(expected, actual);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test11f() {
		// test toExactOriginRegions
		// test zero-length regions

		createStandardProjection();

		try {
			IRegion[] actual= fProjectionMapping.toExactOriginRegions(new Region(0, 0));
			IRegion[] expected= new IRegion[] {
				new Region(20, 0)
			};
			assertRegions(expected, actual);

			actual= fProjectionMapping.toExactOriginRegions(new Region(20, 0));
			expected= new IRegion[] {
				new Region(60, 0)
			};
			assertRegions(expected, actual);

			actual= fProjectionMapping.toExactOriginRegions(new Region(40, 0));
			expected= new IRegion[] {
				new Region(80, 0)
			};
			assertRegions(expected, actual);

		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test11g() {
		// test toExactOriginRegions
		// test a region starting in the middle of a segment and ending in the middle of another fragment

		createStandardProjection();

		try {
			IRegion[] actual= fProjectionMapping.toExactOriginRegions(new Region(10, 20));
			IRegion[] expected= new IRegion[] {
				new Region(30, 10),
				new Region(60, 10)
			};
			assertRegions(expected, actual);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test11h() {
		// test toExactOriginRegions
		// test a region completely comprised by a segment at the end of a segment, not the first segment

		createStandardProjection();

		try {
			IRegion[] actual= fProjectionMapping.toExactOriginRegions(new Region(30, 10));
			IRegion[] expected= new IRegion[] {
				new Region(70, 10)
			};
			assertRegions(expected, actual);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test11i() {
		// test toExactOriginRegions
		// test a single region in the identical projection

		createIdenticalProjection();

		try {
			IRegion[] actual= fProjectionMapping.toExactOriginRegions(new Region(30, 10));
			IRegion[] expected= new IRegion[] {
				new Region(30, 10)
			};
			assertRegions(expected, actual);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

	}

	@Test
	public void test12a() {
		// test toExactImageRegions
		// test the whole master document

		createStandardProjection();

		try {
			IRegion[] actual= fProjectionMapping.toExactImageRegions(new Region(0, fMasterDocument.getLength()));
			IRegion[] expected= new IRegion[] {
				new Region(0, 20),
				new Region(20, 20)
			};
			assertRegions(expected, actual);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test12b() {
		// test toExactImageRegions
		// test a region completely comprised by a fragment

		createStandardProjection();

		try {
			IRegion[] actual= fProjectionMapping.toExactImageRegions(new Region(25, 10));
			IRegion[] expected= new IRegion[] {
				new Region(5, 10)
			};
			assertRegions(expected, actual);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test12c() {
		// test toExactImageRegions
		// test a region completely comprised by a fragment at the beginning of a fragment

		createStandardProjection();

		try {
			IRegion[] actual= fProjectionMapping.toExactImageRegions(new Region(20, 10));
			IRegion[] expected= new IRegion[] {
				new Region(0, 10)
			};
			assertRegions(expected, actual);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test12d() {
		// test toExactImageRegions
		// test a region completely comprised by a fragment at the end of a fragment

		createStandardProjection();

		try {
			IRegion[] actual= fProjectionMapping.toExactImageRegions(new Region(30, 10));
			IRegion[] expected= new IRegion[] {
				new Region(10, 10)
			};
			assertRegions(expected, actual);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test12e() {
		// test toExactImageRegions
		// test a complete fragment

		createStandardProjection();

		try {
			IRegion[] actual= fProjectionMapping.toExactImageRegions(new Region(20, 20));
			IRegion[] expected= new IRegion[] {
				new Region(0, 20)
			};
			assertRegions(expected, actual);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test12f() {
		// test toExactImageRegions
		// test zero-length regions

		createStandardProjection();

		try {
			IRegion[] actual= fProjectionMapping.toExactImageRegions(new Region(20, 0));
			IRegion[] expected= new IRegion[] {
				new Region(0, 0)
			};
			assertRegions(expected, actual);

			actual= fProjectionMapping.toExactImageRegions(new Region(60, 0));
			expected= new IRegion[] {
				new Region(20, 0)
			};
			assertRegions(expected, actual);

			actual= fProjectionMapping.toExactImageRegions(new Region(80, 0));
			expected= new IRegion[] {
				new Region(40, 0)
			};
			assertRegions(expected, actual);

		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test12g() {
		// test toExactImageRegions
		// test a region starting in the middle of a fragment and ending in the middle of another fragment

		createStandardProjection();

		try {
			IRegion[] actual= fProjectionMapping.toExactImageRegions(new Region(30, 40));
			IRegion[] expected= new IRegion[] {
				new Region(10, 10),
				new Region(20, 10)
			};
			assertRegions(expected, actual);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test12h() {
		// test toExactImageRegions
		// test a region completely comprised by a fragment at the end of a fragment, not the first fragment

		createStandardProjection();

		try {
			IRegion[] actual= fProjectionMapping.toExactImageRegions(new Region(70, 10));
			IRegion[] expected= new IRegion[] {
				new Region(30, 10)
			};
			assertRegions(expected, actual);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test12i() {
		// test toExactImageRegions
		// test a single region in the identical projection

		createIdenticalProjection();

		try {
			IRegion[] actual= fProjectionMapping.toExactImageRegions(new Region(30, 10));
			IRegion[] expected= new IRegion[] {
				new Region(30, 10)
			};
			assertRegions(expected, actual);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

	}

	@Test
	public void test13a() {
		// test getImageLength
		// empty projection
		assertEquals(0, fProjectionMapping.getImageLength());
	}

	@Test
	public void test13b() {
		// test getImageLength
		// identical projection
		createIdenticalProjection();
		assertEquals(fSlaveDocument.getLength(), fProjectionMapping.getImageLength());
	}

	@Test
	public void test13c() {
		// test getImageLength
		// standard projection
		createStandardProjection();
		assertEquals(fSlaveDocument.getLength(), fProjectionMapping.getImageLength());
	}

	@Test
	public void test13d() {
		// test getImageLength
		// line wrapping projection
		createLineWrappingProjection();
		assertEquals(fSlaveDocument.getLength(), fProjectionMapping.getImageLength());
	}
}
