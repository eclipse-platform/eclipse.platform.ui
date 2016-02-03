/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer <anton.leherbauer@windriver.com> - [projection] "Backspace" key deleting something else - http://bugs.eclipse.org/301023
 *******************************************************************************/
package org.eclipse.text.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISlaveDocumentManager;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.projection.Fragment;
import org.eclipse.jface.text.projection.Segment;

public class ProjectionDocumentTest {

	static private class ProjectionDocument extends org.eclipse.jface.text.projection.ProjectionDocument {

		public boolean isUpdating= false;

		public ProjectionDocument(IDocument masterDocument) {
			super(masterDocument);
		}

		/*
		 * @see org.eclipse.jface.text.projection.ProjectionDocument#getSegments()
		 */
		public Position[] getSegments2() {
			return super.getSegments();
		}

		/*
		 * @see org.eclipse.jface.text.projection.ProjectionDocument#getFragments()
		 */
		public Position[] getFragments2() {
			return super.getFragments();
		}

		/*
		 * @see org.eclipse.jface.text.projection.ProjectionDocument#adaptProjectionToMasterChange(org.eclipse.jface.text.DocumentEvent)
		 */
		public boolean adaptProjectionToMasterChange2(DocumentEvent masterEvent) throws BadLocationException {
			return super.adaptProjectionToMasterChange(masterEvent);
		}

		@Override
		protected boolean isUpdating() {
			return super.isUpdating() || isUpdating;
		}
	}

	static private class ProjectionDocumentManager extends org.eclipse.jface.text.projection.ProjectionDocumentManager {
		@Override
		protected org.eclipse.jface.text.projection.ProjectionDocument createProjectionDocument(IDocument master) {
			return new ProjectionDocument(master);
		}
	}

	static private boolean LINES= true;


	private ProjectionDocument fSlaveDocument;
	private IDocument fMasterDocument;
	private ISlaveDocumentManager fSlaveDocumentManager;


	private String getOriginalMasterContents() {
		return LINES ?
			"1111111111111111111\n" +
			"2222222222222222222\n" +
			"3333333333333333333\n" +
			"4444444444444444444\n" +
			"5555555555555555555\n" +
			"6666666666666666666\n" +
			"7777777777777777777\n" +
			"8888888888888888888\n" +
			"9999999999999999999\n"
		:
			"11111111111111111111" +
			"22222222222222222222" +
			"33333333333333333333" +
			"44444444444444444444" +
			"55555555555555555555" +
			"66666666666666666666" +
			"77777777777777777777" +
			"88888888888888888888" +
			"99999999999999999999";
	}

	@Before
	public void setUp() {
		fMasterDocument= new Document();
		fMasterDocument.set(getOriginalMasterContents());
		fSlaveDocumentManager= new ProjectionDocumentManager();
		fSlaveDocument= (ProjectionDocument) fSlaveDocumentManager.createSlaveDocument(fMasterDocument);
	}

	@After
	public void tearDown () {
		fSlaveDocumentManager.freeSlaveDocument(fSlaveDocument);
		fSlaveDocument= null;
		fSlaveDocumentManager= null;
	}

	private void createIdenticalProjection() {
		int offset= 0;
		int length= fMasterDocument.getLength();
		try {
			fSlaveDocument.addMasterDocumentRange(offset, length);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	private void createProjectionA() {
		createProjectionA(fSlaveDocument);
	}

	private void createProjectionA(ProjectionDocument projection) {
		try {
			projection.addMasterDocumentRange(0, 20);
			projection.addMasterDocumentRange(40, 20);
			projection.addMasterDocumentRange(80, 20);
			projection.addMasterDocumentRange(120, 20);
			projection.addMasterDocumentRange(160, 20);
		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	private String getProjectionASlaveContents() {
		return LINES ?
			"1111111111111111111\n" +
			"3333333333333333333\n" +
			"5555555555555555555\n" +
			"7777777777777777777\n" +
			"9999999999999999999\n"
		:
			"11111111111111111111" +
			"33333333333333333333" +
			"55555555555555555555" +
			"77777777777777777777" +
			"99999999999999999999";
	}

	private void createProjectionB() {
		createProjectionB(fSlaveDocument);
	}

	private void createProjectionB(ProjectionDocument projection) {
		try {
			projection.addMasterDocumentRange(20, 20);
			projection.addMasterDocumentRange(60, 20);
			projection.addMasterDocumentRange(100, 20);
			projection.addMasterDocumentRange(140, 20);
		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	private String getProjectionBSlaveContents() {
		return LINES ?
			"2222222222222222222\n" +
			"4444444444444444444\n" +
			"6666666666666666666\n" +
			"8888888888888888888\n"
		:
			"22222222222222222222" +
			"44444444444444444444" +
			"66666666666666666666" +
			"88888888888888888888";
	}

	private String print(Position p) {
		return "[" + p.getOffset() + "," + p.getLength() + "]";
	}

	private void assertWellFormedSegmentation() {
		Position[] segmentation= fSlaveDocument.getSegments2();
		assertNotNull(segmentation);

		Position previous= null;
		for (int i= 0; i < segmentation.length; i++) {
			assertFalse(segmentation.length > 1 && (segmentation[i].getLength() == 0 && i < segmentation.length - 1));
			if (previous != null)
				assertTrue(previous.getOffset() + previous.getLength() == segmentation[i].getOffset());
			previous= segmentation[i];
		}
	}

	private void assertWellFormedFragmentation() {
		Position[] segmentation= fSlaveDocument.getSegments2();
		assertNotNull(segmentation);
		Position[] fragmention= fSlaveDocument.getFragments2();
		assertNotNull(fragmention);

		assertTrue(fragmention.length == segmentation.length);

		Position previous= null;
		for (int i= 0; i < segmentation.length; i++) {
			Segment segment= (Segment) segmentation[i];
			Fragment fragment= (Fragment) fragmention[i];
			assertTrue(fragment == segment.fragment);
			assertTrue(segment == fragment.segment);
			assertFalse(segmentation.length > 1 && (fragment.getLength() == 0 && i < segmentation.length - 1));
			assertTrue(fragment.length == segment.length);
			if (previous != null && i < segmentation.length - 1)
				assertFalse(previous.getOffset() + previous.getLength() == fragment.getOffset());
			previous= fragment;
		}
	}

	private void assertFragmentation(Position[] expected) {
		assertFragmentation(expected, true);
	}

	private void assertFragmentation(Position[] expected, boolean checkWellFormedness) {
		if (checkWellFormedness) {
			assertWellFormedSegmentation();
			assertWellFormedFragmentation();
		}

		Position[] segmentation= fSlaveDocument.getSegments2();
		assertTrue("invalid number of segments", expected.length == segmentation.length);

		for (int i= 0; i < expected.length; i++) {
			Segment segment= (Segment) segmentation[i];
			Fragment actual= segment.fragment;
			Assert.assertEquals(print(actual) + " != " + print(expected[i]), expected[i], actual);
		}

	}

	private void assertLineInformationConsistency(IDocument document) {
		DefaultLineTracker textTracker= new DefaultLineTracker();
		textTracker.set(document.get());

		int textLines= textTracker.getNumberOfLines();
		int trackerLines= document.getNumberOfLines();
		Assert.assertEquals(trackerLines, textLines);

		for (int i= 0; i < trackerLines; i++) {
			try {
				IRegion trackerLine= document.getLineInformation(i);
				IRegion textLine= textTracker.getLineInformation(i);

				Assert.assertEquals(trackerLine.getOffset(), textLine.getOffset());
				Assert.assertEquals(trackerLine.getLength(), textLine.getLength());

			} catch (BadLocationException e) {
				assertTrue(false);
			}
		}
	}

	private void assertContents(String expected, IDocument document) {
		assertWellFormedSegmentation();
		assertWellFormedFragmentation();
		Assert.assertEquals(expected, document.get());
		assertLineInformationConsistency(document);
	}

	private void assertSlaveContents(String expected) {
		assertContents(expected, fSlaveDocument);
	}

	private void assertMasterContents(String expected) {
		assertContents(expected, fMasterDocument);
	}

	@Test
	public void test1() {
		// test identical projection
		createIdenticalProjection();
		assertSlaveContents(fMasterDocument.get());
	}

	@Test
	public void test2() {
		// test complete replace the master document in case of identical projection
		createIdenticalProjection();
		try {
			fMasterDocument.replace(0,fMasterDocument.getLength(), "nothing");
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		assertSlaveContents(fMasterDocument.get());
	}

	@Test
	public void test3() {
		// test standard projection, i.e. all odd digits
		createProjectionA();
		assertSlaveContents(getProjectionASlaveContents());
	}

	@Test
	public void test4() {
		// test modifying the unprojected regions of the master document
		createProjectionA();
		try {
			fMasterDocument.replace(145, 5, "~");
			fMasterDocument.replace(105, 5, "~");
			fMasterDocument.replace(65, 5, "~");
			fMasterDocument.replace(25, 5, "~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		assertSlaveContents(getProjectionASlaveContents());
	}

	@Test
	public void test5() {
		// test modifying the projected regions of the master document
		createProjectionA();
		try {
			fMasterDocument.replace(165, 5, "~");
			fMasterDocument.replace(125, 5, "~");
			fMasterDocument.replace(85, 5, "~");
			fMasterDocument.replace(45, 5, "~");
			fMasterDocument.replace(5, 5, "~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.replace(85, 90, "~");
		buffer.replace(65, 70, "~");
		buffer.replace(45, 50, "~");
		buffer.replace(25, 30, "~");
		buffer.replace(5, 10, "~");
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test6() {
		// test replacing the contents of the projected regions of the master document
		createProjectionA();
		try {
			fMasterDocument.replace(160, 20, "~");
			fMasterDocument.replace(120, 20, "~");
			fMasterDocument.replace(80, 20, "~");
			fMasterDocument.replace(40, 20, "~");
			fMasterDocument.replace(0, 20, "~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		assertSlaveContents("~~~~~");
	}

	@Test
	public void test7() {
		// test replacing the contents of the master document
		createProjectionA();
		try {
			fMasterDocument.replace(0, fMasterDocument.getLength(), "~~~~~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		assertSlaveContents("~~~~~");

		Position[] expected= { new Position(0, 5) };
		assertFragmentation(expected);
	}

	@Test
	public void test8_1() {
		// test corner case manipulation of the projected regions of the master document
		// insert at the beginning of the document

		createProjectionA();
		try {
			fMasterDocument.replace(0, 0, "~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		assertSlaveContents("~" + getProjectionASlaveContents());
	}

	@Test
	public void test8_2() {
		// test corner case manipulation of the projected regions of the master document
		// delete at the beginning of the document

		createProjectionA();
		try {
			fMasterDocument.replace(0, 1, "");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		assertSlaveContents(getProjectionASlaveContents().substring(1));
	}

	@Test
	public void test8_3() {
		// test corner case manipulation of the projected regions of the master document
		// replace at the beginning of the document

		createProjectionA();
		try {
			fMasterDocument.replace(0, 1, "~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		assertSlaveContents("~" + getProjectionASlaveContents().substring(1));
	}

	@Test
	public void test8_4() {
		// test corner case manipulation of the projected regions of the master document
		// insert at the end of the document

		createProjectionA();
		try {
			fMasterDocument.replace(fMasterDocument.getLength(), 0, "~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		assertSlaveContents(getProjectionASlaveContents() + "~");
	}

	@Test
	public void test8_5() {
		// test corner case manipulation of the projected regions of the master document
		// delete at the end of the document

		createProjectionA();
		try {
			fMasterDocument.replace(fMasterDocument.getLength()-1, 1, "");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		String text= getProjectionASlaveContents();
		assertSlaveContents(text.substring(0, text.length()-1));
	}

	@Test
	public void test8_6() {
		// test corner case manipulation of the projected regions of the master document
		// replace at the end of the document

		createProjectionA();
		try {
			fMasterDocument.replace(fMasterDocument.getLength()-1, 1, "~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		String text= getProjectionASlaveContents();
		assertSlaveContents(text.substring(0, text.length()-1) + "~");
	}

	@Test
	public void test8_7() {
		// test corner case manipulation of the projected regions of the master document
		// insert at the beginning of a projected region of the master document

		createProjectionA();
		try {
			fMasterDocument.replace(80, 0, "~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.insert(40, '~');
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test8_8() {
		// test corner case manipulation of the projected regions of the master document
		// delete at the beginning of a projected region of the master document

		createProjectionA();
		try {
			fMasterDocument.replace(80, 1, "");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.deleteCharAt(40);
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test8_9() {
		// test corner case manipulation of the projected regions of the master document
		// replace at the beginning of a projected region of the master document

		createProjectionA();
		try {
			fMasterDocument.replace(80, 1, "~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.replace(40, 41, "~");
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test8_10() {
		// test corner case manipulation of the projected regions of the master document
		// insert at the end of a projected region of the master document
		// -> slave document unchanged as this is interpreted as "beginning of an unprojected region"

		test9_1();
	}

	@Test
	public void test8_11() {
		// test corner case manipulation of the projected regions of the master document
		// delete at the end of a projected region of the master document
		// -> slave document changed

		createProjectionA();
		try {
			fMasterDocument.replace(99, 1, "");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.deleteCharAt(59);
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test8_12() {
		// test corner case manipulation of the projected regions of the master document
		// replace at the end of a projected region of the master document
		// -> slave document changed

		createProjectionA();
		try {
			fMasterDocument.replace(99, 1, "~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.replace(59, 60, "~");
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test9_1() {
		// test corner case manipulation of the unprojected regions of the master document
		// insert at the beginning of an unprojected region
		// -> slave document unchanged

		createProjectionA();
		try {
			fMasterDocument.replace(100, 0, "~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		assertSlaveContents(getProjectionASlaveContents());
	}

	@Test
	public void test9_2() {
		// test corner case manipulation of the unprojected regions of the master document
		// delete at the beginning of an unprojected region
		// -> slave document unchanged

		createProjectionA();
		try {
			fMasterDocument.replace(100, 1, "");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		assertSlaveContents(getProjectionASlaveContents());
	}

	@Test
	public void test9_3() {
		// test corner case manipulation of the unprojected regions of the master document
		// replace at the beginning of an unprojected region
		// -> slave document unchanged

		createProjectionA();
		try {
			fMasterDocument.replace(100, 1, "~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		assertSlaveContents(getProjectionASlaveContents());
	}

	@Test
	public void test9_4() {
		// test corner case manipulation of the unprojected regions of the master document
		// insert at the end of an unprojected region
		// -> slave document changed, as this is interpreted as "beginning of a projected region"

		test8_7();
	}

	@Test
	public void test9_5() {
		// test corner case manipulation of the unprojected regions of the master document
		// delete at the end of an unprojected region
		// -> slave document unchanged

		createProjectionA();
		try {
			fMasterDocument.replace(79, 1, "");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		assertSlaveContents(getProjectionASlaveContents());
	}

	@Test
	public void test9_6() {
		// test corner case manipulation of the unprojected regions of the master document
		// replace at the end of an unprojected region
		// -> slave document unchanged

		createProjectionA();
		try {
			fMasterDocument.replace(79, 1, "~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		assertSlaveContents(getProjectionASlaveContents());
	}

	@Test
	public void test9_7() {
		// test corner case manipulation of the unprojected regions of the master document
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=51594

		if (!LINES)
			return;

		try {
			int startOffset= fMasterDocument.getLineOffset(4);
			Assert.assertEquals(80, startOffset);
			int endOffset= fMasterDocument.getLineOffset(7);
			Assert.assertEquals(140, endOffset);
			fSlaveDocument.addMasterDocumentRange(startOffset, endOffset - startOffset);

			assertSlaveContents(getOriginalMasterContents().substring(80, 140));

			fMasterDocument.replace(endOffset, 1, "x");
			assertLineInformationConsistency(fSlaveDocument);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test10_1() {
		// test manipulations overlapping projected and unprojected regions of the master document
		// delete range overlapping from a projected into an unprojected region

		createProjectionA();
		try {
			fMasterDocument.replace(50, 20, "");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.delete(30, 40);
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test10_2() {
		// test manipulations overlapping projected and unprojected regions of the master document
		// replace range overlapping from a projected into an unprojected region
		// => replaced range will appear in slave document because of auto expansion of slave document in case of overlapping events

		createProjectionA();
		try {
			fMasterDocument.replace(50, 20, "~~~~~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
				new Position(0, 20),
				new Position(40, 15),
				new Position(65, 20),
				new Position(105, 20),
				new Position(145, 20)
		};
		assertFragmentation(expected);

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.replace(30, 40, "~~~~~");
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test10_3() {
		// test manipulations overlapping projected and unprojected regions of the master document
		// delete range overlapping from an unprojected into a projected region

		createProjectionA();
		try {
			fMasterDocument.replace(70, 20, "");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.delete(40, 50);
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test10_4() {
		// test manipulations overlapping projected and unprojected regions of the master document
		// replace range overlapping from an unprojected into a projected region
		// -> replaced range will appear in slave document because of auto expansion of slave document in case of overlapping events

		createProjectionA();
		try {
			fMasterDocument.replace(70, 20, "~~~~~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
				new Position(0, 20),
				new Position(40, 20),
				new Position(70, 15),
				new Position(105, 20),
				new Position(145, 20)
		};
		assertFragmentation(expected);

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.replace(40, 50, "~~~~~");
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test11() {
		// test deleting an unprojected region of the master document

		createProjectionA();
		try {
			fMasterDocument.replace(60, 20, "");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
				new Position(0, 20),
				new Position(40, 40),
				new Position(100, 20),
				new Position(140, 20)
		};
		assertFragmentation(expected);

		assertSlaveContents(getProjectionASlaveContents());
	}

	@Test
	public void test12() {
		// test deleting a projected region of the master document

		createProjectionA();
		try {
			fMasterDocument.replace(80, 20, "");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
				new Position(0, 20),
				new Position(40, 20),
				new Position(100, 20),
				new Position(140, 20)
		};
		assertFragmentation(expected);
	}

	@Test
	public void test13() {
		// test complete replace of the contents of the slave document in identical projection

		createIdenticalProjection();

		try {
			fSlaveDocument.replace(0, fSlaveDocument.getLength(), "~~~~~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		assertSlaveContents("~~~~~");
		assertMasterContents("~~~~~");
	}

	@Test
	public void test14_1() {
		// test complete replace of the contents of the slave document in standard projection A

		createProjectionA();
		try {
			fSlaveDocument.replace(0, fSlaveDocument.getLength(), "~~~~~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		assertSlaveContents("~~~~~");
		assertMasterContents("~~~~~");
	}

	@Test
	public void test14_2() {
		// test complete replace of the contents of the slave document in standard projection B

		createProjectionB();
		try {
			fSlaveDocument.replace(0, fSlaveDocument.getLength(), "~~~~~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		assertSlaveContents("~~~~~");
		StringBuffer buffer= new StringBuffer(getOriginalMasterContents());
		buffer.replace(20, 160, "~~~~~");
		assertMasterContents(buffer.toString());
	}

	@Test
	public void test15() {
		// test modifying the segments of the slave document

		createProjectionA();
		try {
			fSlaveDocument.replace(90, 5, "~");
			fSlaveDocument.replace(70, 5, "~");
			fSlaveDocument.replace(50, 5, "~");
			fSlaveDocument.replace(30, 5, "~");
			fSlaveDocument.replace(10, 5, "~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.replace(90, 95, "~");
		buffer.replace(70, 75, "~");
		buffer.replace(50, 55, "~");
		buffer.replace(30, 35, "~");
		buffer.replace(10, 15, "~");
		assertSlaveContents(buffer.toString());

		buffer= new StringBuffer(getOriginalMasterContents());
		buffer.replace(170, 175, "~");
		buffer.replace(130, 135, "~");
		buffer.replace(90, 95, "~");
		buffer.replace(50, 55, "~");
		buffer.replace(10, 15, "~");
		assertMasterContents(buffer.toString());
	}

	@Test
	public void test16() {
		// test replacing the contents of the segments of the slave document

		createProjectionA();
		try {
			fSlaveDocument.replace(80, 20, "~");
			fSlaveDocument.replace(60, 20, "~");
			fSlaveDocument.replace(40, 20, "~");
			fSlaveDocument.replace(20, 20, "~");
			fSlaveDocument.replace(0, 20, "~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		assertSlaveContents("~~~~~");
		StringBuffer buffer= new StringBuffer(getOriginalMasterContents());
		buffer.replace(160, 180, "~");
		buffer.replace(120, 140, "~");
		buffer.replace(80, 100, "~");
		buffer.replace(40, 60, "~");
		buffer.replace(0, 20, "~");
		assertMasterContents(buffer.toString());
	}

	@Test
	public void test17_1() {
		// test corner case manipulation of the segments of the slave document
		// insert at the beginning of a segment of the slave document

		createProjectionA();
		try {
			fSlaveDocument.replace(20, 0, "~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.insert(20, '~');
		assertSlaveContents(buffer.toString());

		buffer= new StringBuffer(getOriginalMasterContents());
		buffer.insert(40, '~');
		assertMasterContents(buffer.toString());
	}

	@Test
	public void test17_2() {
		// test corner case manipulation of the segments of the slave document
		// delete at the beginning of a segment of the slave document

		createProjectionA();
		try {
			fSlaveDocument.replace(20, 1, "");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.deleteCharAt(20);
		assertSlaveContents(buffer.toString());

		buffer= new StringBuffer(getOriginalMasterContents());
		buffer.deleteCharAt(40);
		assertMasterContents(buffer.toString());
	}

	@Test
	public void test17_3() {
		// test corner case manipulation of the segments of the slave document
		// replace at the beginning of a segment of the slave document

		createProjectionA();
		try {
			fSlaveDocument.replace(20, 1, "~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.replace(20, 21, "~");
		assertSlaveContents(buffer.toString());

		buffer= new StringBuffer(getOriginalMasterContents());
		buffer.replace(40, 41, "~");
		assertMasterContents(buffer.toString());
	}

	@Test
	public void test17_4() {
		// test corner case manipulation of the segments of the slave document
		// insert at the end of a segment of the slave document
		// interpreted as "insert at the beginning of the next segment"

		test17_1();
	}

	@Test
	public void test17_5() {
		// test corner case manipulation of the segments of the slave document
		// delete at the end of a segment of the slave document

		createProjectionA();
		try {
			fSlaveDocument.replace(39, 1, "");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.deleteCharAt(39);
		assertSlaveContents(buffer.toString());

		buffer= new StringBuffer(getOriginalMasterContents());
		buffer.deleteCharAt(59);
		assertMasterContents(buffer.toString());
	}

	@Test
	public void test17_6() {
		// test corner case manipulation of the segments of the slave document
		// replace at the end of a segment of the slave document

		createProjectionA();
		try {
			fSlaveDocument.replace(39, 1, "~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.replace(39, 40, "~");
		assertSlaveContents(buffer.toString());

		buffer= new StringBuffer(getOriginalMasterContents());
		buffer.replace(59, 60, "~");
		assertMasterContents(buffer.toString());
	}

	@Test
	public void test17_7() {
		// test corner case manipulation of the segments of the slave document
		// insert at the end of last segment of the slave document - bug 301023

		createIdenticalProjection();
		String originalSlaveContent = "";
		try {
			fSlaveDocument.removeMasterDocumentRange(80, 100);
			originalSlaveContent = fSlaveDocument.get();
			fSlaveDocument.replace(80, 0, "~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		StringBuffer buffer= new StringBuffer(originalSlaveContent);
		buffer.insert(80, "~");
		assertSlaveContents(buffer.toString());

		buffer= new StringBuffer(getOriginalMasterContents());
		buffer.insert(180, "~");
		assertMasterContents(buffer.toString());
	}

	@Test
	public void test18_1() {
		// test manipulations overlapping multiple segments of the slave document
		// delete range overlapping two neighboring segments

		createProjectionA();

		try {
			fSlaveDocument.replace(30, 20, "");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
				new Position(0, 20),
				new Position(40, 20),
				new Position(80, 20),
				new Position(120, 20)
		};
		assertFragmentation(expected);

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.delete(30, 50);
		assertSlaveContents(buffer.toString());

		buffer= new StringBuffer(getOriginalMasterContents());
		buffer.delete(50, 90);
		assertMasterContents(buffer.toString());
	}

	@Test
	public void test18_2() {
		// test manipulations overlapping multiple segments of the slave document
		// replace range overlapping two neighboring segments

		createProjectionA();

		try {
			fSlaveDocument.replace(30, 20, "~~~~~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
			new Position(0, 20),
			new Position(40, 25),
			new Position(85, 20),
			new Position(125, 20)
		};
		assertFragmentation(expected);

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.replace(30, 50, "~~~~~");
		assertSlaveContents(buffer.toString());

		buffer= new StringBuffer(getOriginalMasterContents());
		buffer.replace(50, 90, "~~~~~");
		assertMasterContents(buffer.toString());
	}

	@Test
	public void test19() {
		// test deleting the contents of a segment of the slave document

		createProjectionA();

		try {
			fSlaveDocument.replace(20, 20, "");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.delete(20, 40);
		assertSlaveContents(buffer.toString());

		buffer= new StringBuffer(getOriginalMasterContents());
		buffer.delete(40, 60);
		assertMasterContents(buffer.toString());

		Position[] expected= {
			new Position(0, 20),
			new Position(60, 20),
			new Position(100, 20),
			new Position(140, 20)
		};
		assertFragmentation(expected);
	}

	@Test
	public void test20_1() {
		// test adding a range to the slave document at the beginning of a segment gap

		createProjectionA();

		try {
			fSlaveDocument.addMasterDocumentRange(60, 10);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
			new Position(0, 20),
			new Position(40, 30),
			new Position(80, 20),
			new Position(120, 20),
			new Position(160, 20)
		};
		assertFragmentation(expected);

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		String addition= getOriginalMasterContents().substring(60, 70);
		buffer.insert(40, addition);
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test20_2() {
		// test adding a range to the slave document at the end of a segment gap

		createProjectionA();

		try {
			fSlaveDocument.addMasterDocumentRange(70, 10);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
			new Position(0, 20),
			new Position(40, 20),
			new Position(70, 30),
			new Position(120, 20),
			new Position(160, 20)
		};
		assertFragmentation(expected);

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		String addition= getOriginalMasterContents().substring(70, 80);
		buffer.insert(40, addition);
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test20_3() {
		// test adding a range to the slave document that is in the middle of a segment gap

		createProjectionA();

		try {
			fSlaveDocument.addMasterDocumentRange(65, 10);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
			new Position(0, 20),
			new Position(40, 20),
			new Position(65, 10),
			new Position(80, 20),
			new Position(120, 20),
			new Position(160, 20)
		};
		assertFragmentation(expected);

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		String addition= getOriginalMasterContents().substring(65, 75);
		buffer.insert(40, addition);
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test20_4() {
		// test adding a range to the slave document that is a segment gap

		createProjectionA();

		try {
			fSlaveDocument.addMasterDocumentRange(60, 20);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
			new Position(0, 20),
			new Position(40, 60),
			new Position(120, 20),
			new Position(160, 20)
		};
		assertFragmentation(expected);

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		String addition= getOriginalMasterContents().substring(60, 80);
		buffer.insert(40, addition);
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test20_5() {
		// test adding a range to the slave document beginning in a segment gap and ending in a segment

		createProjectionA();

		try {
			fSlaveDocument.addMasterDocumentRange(70, 20);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
			new Position(0, 20),
			new Position(40, 20),
			new Position(70, 30),
			new Position(120, 20),
			new Position(160, 20)
		};
		assertFragmentation(expected);

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		String addition= getOriginalMasterContents().substring(70, 80);
		buffer.insert(40, addition);
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test20_6() {
		// test adding a range to the slave document beginning in a segment and ending in a segment gap

		createProjectionA();

		try {
			fSlaveDocument.addMasterDocumentRange(50, 20);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
			new Position(0, 20),
			new Position(40, 30),
			new Position(80, 20),
			new Position(120, 20),
			new Position(160, 20)
		};
		assertFragmentation(expected);

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		String addition= getOriginalMasterContents().substring(60, 70);
		buffer.insert(40, addition);
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test20_7() {
		// test adding a range to the slave document beginning in a segment and ending in a segment

		createProjectionA();

		try {
			fSlaveDocument.addMasterDocumentRange(50, 40);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
			new Position(0, 20),
			new Position(40, 60),
			new Position(120, 20),
			new Position(160, 20)
		};
		assertFragmentation(expected);

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		String addition= getOriginalMasterContents().substring(60, 80);
		buffer.insert(40, addition);
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test20_8() {
		// test adding a range to the slave document beginning in a segment gap and ending in a segment gap

		createProjectionA();

		try {
			fSlaveDocument.addMasterDocumentRange(70, 40);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
			new Position(0, 20),
			new Position(40, 20),
			new Position(70, 40),
			new Position(120, 20),
			new Position(160, 20)
		};
		assertFragmentation(expected);

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		String addition= getOriginalMasterContents().substring(100, 110);
		buffer.insert(60, addition);
		addition= getOriginalMasterContents().substring(70, 80);
		buffer.insert(40, addition);
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test21_1() {
		// test removing a range from the slave document at the beginning of a segment

		createProjectionA();

		try {
			fSlaveDocument.removeMasterDocumentRange(40, 10);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
			new Position(0, 20),
			new Position(50, 10),
			new Position(80, 20),
			new Position(120, 20),
			new Position(160, 20)
		};
		assertFragmentation(expected);

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.delete(20, 30);
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test21_2() {
		// test removing a range from the slave document at the end of a segment

		createProjectionA();

		try {
			fSlaveDocument.removeMasterDocumentRange(50, 10);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
			new Position(0, 20),
			new Position(40, 10),
			new Position(80, 20),
			new Position(120, 20),
			new Position(160, 20)
		};
		assertFragmentation(expected);

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.delete(30, 40);
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test21_3() {
		// test removing a range from the slave document that is in the middle of a segment

		createProjectionA();

		try {
			fSlaveDocument.removeMasterDocumentRange(85, 10);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
			new Position(0, 20),
			new Position(40, 20),
			new Position(80, 5),
			new Position(95, 5),
			new Position(120, 20),
			new Position(160, 20)
		};
		assertFragmentation(expected);

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.delete(45, 55);
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test21_4() {
		// test removing a range from the slave document that is a segment

		createProjectionA();

		try {
			fSlaveDocument.removeMasterDocumentRange(40, 20);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
			new Position(0, 20),
			new Position(80, 20),
			new Position(120, 20),
			new Position(160, 20)
		};
		assertFragmentation(expected);

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.delete(20, 40);
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test21_5() {
		// test removing a range from the slave document beginning in a segment and ending in a segment gap

		createProjectionA();

		try {
			fSlaveDocument.removeMasterDocumentRange(50, 20);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
			new Position(0, 20),
			new Position(40, 10),
			new Position(80, 20),
			new Position(120, 20),
			new Position(160, 20)
		};
		assertFragmentation(expected);

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.delete(30, 40);
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test21_6() {
		// test removing a range from the slave document beginning in a segment gap and ending in a segment

		createProjectionA();

		try {
			fSlaveDocument.removeMasterDocumentRange(70, 20);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
			new Position(0, 20),
			new Position(40, 20),
			new Position(90, 10),
			new Position(120, 20),
			new Position(160, 20)
		};
		assertFragmentation(expected);

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.delete(40, 50);
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test21_7() {
		// test removing a range from the slave document beginning in a segment gap and ending in a segment gap

		createProjectionA();

		try {
			fSlaveDocument.removeMasterDocumentRange(70, 40);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
			new Position(0, 20),
			new Position(40, 20),
			new Position(120, 20),
			new Position(160, 20)
		};
		assertFragmentation(expected);

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.delete(40, 60);
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test21_8() {
		// test removing a range from the slave document beginning in a segment and ending in a segment

		createProjectionA();

		try {
			fSlaveDocument.removeMasterDocumentRange(50, 40);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
			new Position(0, 20),
			new Position(40, 10),
			new Position(90, 10),
			new Position(120, 20),
			new Position(160, 20)
		};
		assertFragmentation(expected);

		StringBuffer buffer= new StringBuffer(getProjectionASlaveContents());
		buffer.delete(30, 50);
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test21_9() {
		// test removing a range from the slave document using identical projection

		createIdenticalProjection();

		try {
			fSlaveDocument.removeMasterDocumentRange(50, 40);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
			new Position(0, 50),
			new Position(90, 90)
		};
		assertFragmentation(expected);

		StringBuffer buffer= new StringBuffer(getOriginalMasterContents());
		buffer.delete(50, 90);
		assertSlaveContents(buffer.toString());
	}

	@Test
	public void test21_a() {
		// test removing a range from the slave document using identical projection
		// the removed range includes the end of the master document - see bug 301023

		createIdenticalProjection();

		try {
			fSlaveDocument.removeMasterDocumentRange(80, 100);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
			new Position(0, 80),
			new Position(180, 0)
		};
		assertFragmentation(expected);

		StringBuffer buffer= new StringBuffer(getOriginalMasterContents());
		buffer.delete(80, 180);
		assertSlaveContents(buffer.toString());
	}

	private void assertEquals(DocumentEvent expected, DocumentEvent received) {
		assertSame(expected.getDocument(), received.getDocument());
		Assert.assertEquals(expected.getOffset(), received.getOffset());
		Assert.assertEquals(expected.getLength(), received.getLength());
		if (expected.getText() == null || expected.getText().length() == 0)
			assertTrue(received.getText() == null || received.getText().length() == 0);
		else
			Assert.assertEquals(expected.getText(), received.getText());
	}

	private void assertSlaveEvents(DocumentEvent[] expected, DocumentEvent[] received) {
		if (expected == null)
			assertNull(received);

		assertTrue(expected.length == received.length);

		for (int i= 0; i < received.length; i++)
			assertEquals(received[i], expected[i]);
	}

	@Test
	public void test22() {
		// test document events sent out by the slave document when adding segments

		final List<DocumentEvent> receivedEvents= new ArrayList<>();

		IDocumentListener listener= new IDocumentListener() {
			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {}
			@Override
			public void documentChanged(DocumentEvent event) {
				receivedEvents.add(event);
			}
		};

		fSlaveDocument.addDocumentListener(listener);
		createProjectionA();
		DocumentEvent[] actual= new DocumentEvent[receivedEvents.size()];
		receivedEvents.toArray(actual);

		StringBuffer buffer= new StringBuffer(getOriginalMasterContents());
		DocumentEvent[] expected= new DocumentEvent[] {
			new DocumentEvent(fSlaveDocument, 0, 0, buffer.substring(0, 20)),
			new DocumentEvent(fSlaveDocument, 20, 0, buffer.substring(40, 60)),
			new DocumentEvent(fSlaveDocument, 40, 0, buffer.substring(80, 100)),
			new DocumentEvent(fSlaveDocument, 60, 0, buffer.substring(120, 140)),
			new DocumentEvent(fSlaveDocument, 80, 0, buffer.substring(160, 180))
		};
		assertSlaveEvents(expected, actual);
	}

	@Test
	public void test23() {
		// test document events sent out by the slave document when removing segments

		final List<DocumentEvent> receivedEvents= new ArrayList<>();

		IDocumentListener listener= new IDocumentListener() {
			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {}
			@Override
			public void documentChanged(DocumentEvent event) {
				receivedEvents.add(event);
			}
		};

		createProjectionA();

		fSlaveDocument.addDocumentListener(listener);
		try {
			fSlaveDocument.removeMasterDocumentRange(40, 20);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		DocumentEvent[] actual= new DocumentEvent[receivedEvents.size()];
		receivedEvents.toArray(actual);
		DocumentEvent[] expected= new DocumentEvent[] { new DocumentEvent(fSlaveDocument, 20, 20, "") };
		assertSlaveEvents(expected, actual);
	}

	@Test
	public void test24_1() {
		// test auto expand mode when manipulating the master document
		// master event completely left of slave document

		createProjectionB();
		fSlaveDocument.setAutoExpandMode(true);

		try {
			DocumentEvent event= new DocumentEvent(fMasterDocument, 5, 10, "~");
			fSlaveDocument.adaptProjectionToMasterChange2(event);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
				new Position(5, 10),
				new Position(20, 20),
				new Position(60, 20),
				new Position(100, 20),
				new Position(140, 20)
		};
		assertFragmentation(expected);
	}

	@Test
	public void test24_2() {
		// test auto expand mode when manipulating the master document
		// master event completely right of slave document

		createProjectionB();
		fSlaveDocument.setAutoExpandMode(true);

		try {
			DocumentEvent event= new DocumentEvent(fMasterDocument, 165, 10, "~");
			fSlaveDocument.adaptProjectionToMasterChange2(event);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
				new Position(20,20),
				new Position(60, 20),
				new Position(100, 20),
				new Position(140, 20),
				new Position(165, 10)
		};
		assertFragmentation(expected);
	}

	@Test
	public void test24_3() {
		// test auto expand mode when manipulating the master document
		// master event completely left of fragment

		createProjectionB();
		fSlaveDocument.setAutoExpandMode(true);

		try {
			DocumentEvent event= new DocumentEvent(fMasterDocument, 45, 10, "~");
			fSlaveDocument.adaptProjectionToMasterChange2(event);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
				new Position(20, 20),
				new Position(45, 10),
				new Position(60, 20),
				new Position(100, 20),
				new Position(140, 20)
		};
		assertFragmentation(expected);
	}

	@Test
	public void test24_4() {
		// test auto expand mode when manipulating the master document
		// master event completely right of fragment
		// -> is also left of the fragment in this setup

		createProjectionB();
		fSlaveDocument.setAutoExpandMode(true);

		try {
			DocumentEvent event= new DocumentEvent(fMasterDocument, 85, 10, "~");
			fSlaveDocument.adaptProjectionToMasterChange2(event);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
				new Position(20, 20),
				new Position(60, 20),
				new Position(85, 10),
				new Position(100, 20),
				new Position(140, 20)
		};
		assertFragmentation(expected);
	}

	@Test
	public void test24_5() {
		// test auto expand mode when manipulating the master document
		// master event starts left of fragment and ends inside of a fragment

		createProjectionB();
		fSlaveDocument.setAutoExpandMode(true);

		try {
			DocumentEvent event= new DocumentEvent(fMasterDocument, 50, 20, "~");
			fSlaveDocument.adaptProjectionToMasterChange2(event);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
				new Position(20, 20),
				new Position(50, 30),
				new Position(100, 20),
				new Position(140, 20)
		};
		assertFragmentation(expected);
	}

	@Test
	public void test24_6() {
		// test auto expand mode when manipulating the master document
		// master event starts inside of a fragment and ends right of a fragment

		createProjectionB();
		fSlaveDocument.setAutoExpandMode(true);

		try {
			DocumentEvent event= new DocumentEvent(fMasterDocument, 70, 20, "~");
			fSlaveDocument.adaptProjectionToMasterChange2(event);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
				new Position(20, 20),
				new Position(60, 30),
				new Position(100, 20),
				new Position(140, 20)
		};
		assertFragmentation(expected);
	}

	@Test
	public void test24_7() {
		// test auto expand mode when manipulating the master document
		// master event starts left of a fragment and ends right of a fragment

		createProjectionB();
		fSlaveDocument.setAutoExpandMode(true);

		try {
			DocumentEvent event= new DocumentEvent(fMasterDocument, 50, 40, "~");
			fSlaveDocument.adaptProjectionToMasterChange2(event);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
				new Position(20, 20),
				new Position(50, 40),
				new Position(100, 20),
				new Position(140, 20)
		};
		assertFragmentation(expected);
	}

	@Test
	public void test24_8() {
		// test auto expand mode when manipulating the master document
		// complete replace of master document

		createProjectionB();
		fSlaveDocument.setAutoExpandMode(true);

		try {
			DocumentEvent event= new DocumentEvent(fMasterDocument, 0, fMasterDocument.getLength(), "x" + getOriginalMasterContents() + "y");
			fSlaveDocument.adaptProjectionToMasterChange2(event);
		} catch (BadLocationException x) {
			assertTrue(false);
		}

		Position[] expected= {
				new Position(0, fMasterDocument.getLength())
		};
		assertFragmentation(expected);
	}

	@Test
	public void test25() {
		// test auto expand mode when manipulating the slave document

		try {
			fSlaveDocument.isUpdating= true;
			fSlaveDocument.adaptProjectionToMasterChange2(new DocumentEvent(fSlaveDocument, 0, 0, "~"));
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
				new Position(0, 0)
		};
		assertFragmentation(expected, false);
	}

	@Test
	public void test26() {
		// test multiple slave documents for the same master document

		createIdenticalProjection();
		ProjectionDocument slave2= (ProjectionDocument) fSlaveDocumentManager.createSlaveDocument(fMasterDocument);
		createProjectionA(slave2);
		ProjectionDocument slave3= (ProjectionDocument) fSlaveDocumentManager.createSlaveDocument(fMasterDocument);
		createProjectionB(slave3);

		assertContents(getOriginalMasterContents(), fSlaveDocument);
		assertContents(getProjectionASlaveContents(), slave2);
		assertContents(getProjectionBSlaveContents(), slave3);

		fSlaveDocumentManager.freeSlaveDocument(slave3);
		fSlaveDocumentManager.freeSlaveDocument(slave2);
	}

	@Test
	public void test27() {
		// test changing the projection until identical projection is reached

		createProjectionA();

		Position[] expected= {
			new Position(0, 20),
			new Position(40, 20),
			new Position(80, 20),
			new Position(120, 20),
			new Position(160, 20)
		};
		assertFragmentation(expected);

		try {
			fSlaveDocument.addMasterDocumentRange(20, 20);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		expected= new Position[] {
			new Position(0, 60),
			new Position(80, 20),
			new Position(120, 20),
			new Position(160, 20)
		};
		assertFragmentation(expected);

		try {
			fSlaveDocument.addMasterDocumentRange(60, 20);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		expected= new Position[] {
			new Position(0, 100),
			new Position(120, 20),
			new Position(160, 20)
		};
		assertFragmentation(expected);

		try {
			fSlaveDocument.addMasterDocumentRange(100, 20);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		expected= new Position[] {
			new Position(0, 140),
			new Position(160, 20)
		};
		assertFragmentation(expected);

		try {
			fSlaveDocument.addMasterDocumentRange(140, 20);
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		expected= new Position[] {
			new Position(0, fMasterDocument.getLength())
		};
		assertFragmentation(expected);
	}

	@Test
	public void test28_1() {
		// delete slave content and check fragmentation, need to keep a single fragment as anchor
		createProjectionB();
		try {
			fSlaveDocument.replace(0, fSlaveDocument.getLength(), "");
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		assertSlaveContents("");
		StringBuffer buffer= new StringBuffer(getOriginalMasterContents());
		buffer.delete(20, 160);
		assertMasterContents(buffer.toString());

		Position[] expected= {
			new Position(20, 0)
		};
		assertFragmentation(expected);
	}

	@Test
	public void test28_2() {
		// test step wise version of the complete replace
		// delete whole content of slave, followed by inserting text

		createProjectionB();
		try {
			fSlaveDocument.replace(0, fSlaveDocument.getLength(), "");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		try {
			fSlaveDocument.replace(0, 0, "~~~~~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= {
			new Position(20, 5)
		};
		assertFragmentation(expected);

		assertSlaveContents("~~~~~");
		StringBuffer buffer= new StringBuffer(getOriginalMasterContents());
		buffer.replace(20, 160, "~~~~~");
		assertMasterContents(buffer.toString());
	}

	@Test
	public void _test29() {
		// test computation of unprojected  master regions

		// spanning no fragment
			// left of fragment
			// left of fragment, touching border of fragment
			// right of fragment, touching border to fragment
			// right of fragment

		// spanning one fragment
			// left of fragment, reaching into fragment
			// inside fragment, touching left border
			// inside fragment
			// inside fragment, touching right border
			// right of fragment, reaching into fragment
			// being identical to a fragment
			// starting left of fragment, ending right of fragment

		// spanning multiple fragments
			// starting left of fragment
				// ending left of fragment
				// ending touching left fragment border
				// ending inside fragment
				// ending touching right fragment border
				// ending right of fragment
			// starting touching left fragment border
				// ending left of fragment
				// ending touching left fragment border
				// ending inside fragment
				// ending touching right fragment border
				// ending right of fragment
			// starting inside fragment
				// ending left of fragment
				// ending touching left fragment border
				// ending inside fragment
				// ending touching right fragment border
				// ending right of fragment
			// starting touching right fragment border
				// ending left of fragment
				// ending touching left fragment border
				// ending inside fragment
				// ending touching right fragment border
				// ending right of fragment
			// starting right of fragment
				// ending left of fragment
				// ending touching left fragment border
				// ending inside fragment
				// ending touching right fragment border
				// ending right of fragment
	}

	private String print(IRegion p) {
		return "[" + p.getOffset() + "," + p.getLength() + "]";
	}

	private void assertRegions(IRegion[] expected, IRegion[] actual) {

		if (expected == null) {
			assertNull(actual);
			return;
		}

		if (actual == null) {
			assertNull(expected);
		}

		assertTrue("invalid number of regions", expected.length == actual.length);
		for (int i= 0; i < expected.length; i++)
			Assert.assertEquals(print(actual[i]) + " != " + print(expected[i]), expected[i], actual[i]);
	}

	private void assertUnprojectedMasterRegions(IRegion[] expected, int offsetInMaster, int lengthInMaster) {
		createProjectionB();
		try {
			IRegion[] regions= fSlaveDocument.computeUnprojectedMasterRegions(offsetInMaster, lengthInMaster);
			assertRegions(expected, regions);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test29_1() {
		// test computation of unprojected  master regions
		// spanning no fragment
			// left of fragment

		IRegion[] expected= {
			new Region(45, 10)
		};
		assertUnprojectedMasterRegions(expected, 45, 10);
	}

	@Test
	public void test29_2() {
		// test computation of unprojected  master regions
		// spanning no fragment
			// left of fragment, touching border of fragment

		IRegion[] expected= {
			new Region(45, 15)
		};
		assertUnprojectedMasterRegions(expected, 45, 15);
	}

	@Test
	public void test29_3() {
		// test computation of unprojected  master regions
		// spanning no fragment
			// right of fragment, touching border to fragment

		IRegion[] expected= {
			new Region(80, 15)
		};
		assertUnprojectedMasterRegions(expected, 80, 15);
	}

	@Test
	public void test29_4() {
		// test computation of unprojected  master regions
		// spanning no fragment
			// right of fragment

		IRegion[] expected= {
			new Region(85, 10)
		};
		assertUnprojectedMasterRegions(expected, 85, 10);
	}

	@Test
	public void test29_5() {
		// test computation of unprojected  master regions
		// spanning one fragment
			// left of fragment, reaching into fragment

		IRegion[] expected= {
			new Region(50, 10)
		};
		assertUnprojectedMasterRegions(expected, 50, 20);
	}

	@Test
	public void test29_6() {
		// test computation of unprojected  master regions
		// spanning one fragment
			// inside fragment, touching left border

		assertUnprojectedMasterRegions(new IRegion[] {}, 60, 10);
	}

	@Test
	public void test29_7() {
		// test computation of unprojected  master regions
		// spanning one fragment
			// inside fragment

		assertUnprojectedMasterRegions(new IRegion[] {}, 65, 10);
	}

	@Test
	public void test29_8() {
		// test computation of unprojected  master regions
		// spanning one fragment
			// inside fragment, touching right border

		assertUnprojectedMasterRegions(new IRegion[] {}, 65, 15);
	}

	@Test
	public void test29_9() {
		// test computation of unprojected  master regions
		// spanning one fragment
			// right of fragment, reaching into fragment

		IRegion[] expected= {
			new Region(80, 10)
		};
		assertUnprojectedMasterRegions(expected, 70, 20);
	}

	@Test
	public void test29_10() {
		// test computation of unprojected  master regions
		// spanning one fragment
			// being identical to a fragment

		assertUnprojectedMasterRegions(new IRegion[] {}, 60, 20);
	}

	@Test
	public void test29_11() {
		// test computation of unprojected  master regions
		// spanning one fragment
			// starting left of fragment, ending right of fragment

		IRegion[] expected= {
			new Region(50, 10),
			new Region(80, 10)
		};
		assertUnprojectedMasterRegions(expected, 50, 40);
	}

	@Test
	public void test29_12() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting left of fragment
				// ending left of fragment

		IRegion[] expected= {
			new Region(50, 10),
			new Region(80, 20),
			new Region(120, 10)
		};
		assertUnprojectedMasterRegions(expected, 50, 80);
	}

	@Test
	public void test29_13() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting left of fragment
				// ending touching left fragment border

		IRegion[] expected= {
			new Region(50, 10),
			new Region(80, 20),
			new Region(120, 20)
		};
		assertUnprojectedMasterRegions(expected, 50, 90);
	}

	@Test
	public void test29_14() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting left of fragment
				// ending inside fragment

		IRegion[] expected= {
			new Region(50, 10),
			new Region(80, 20)
		};
		assertUnprojectedMasterRegions(expected, 50, 60);
	}

	@Test
	public void test29_15() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting left of fragment
				// ending touching right fragment border

		IRegion[] expected= {
			new Region(50, 10),
			new Region(80, 20),
			new Region(120, 20)
		};
		assertUnprojectedMasterRegions(expected, 50, 110);
	}

	@Test
	public void test29_16() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting left of fragment
				// ending right of fragment

		IRegion[] expected= {
			new Region(50, 10),
			new Region(80, 20),
			new Region(120, 10)
		};
		assertUnprojectedMasterRegions(expected, 50, 80);
	}

	@Test
	public void test29_17() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting touching left fragment border
				// ending left of fragment

		IRegion[] expected= {
			new Region(80, 20),
			new Region(120, 10)
		};
		assertUnprojectedMasterRegions(expected, 60, 70);
	}

	@Test
	public void test29_18() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting touching left fragment border
				// ending touching left fragment border

		IRegion[] expected= {
			new Region(80, 20),
			new Region(120, 20)
		};
		assertUnprojectedMasterRegions(expected, 60, 80);
	}

	@Test
	public void test29_19() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting touching left fragment border
				// ending inside fragment

		IRegion[] expected= {
			new Region(80, 20)
		};
		assertUnprojectedMasterRegions(expected, 60, 50);
	}

	@Test
	public void test29_20() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting touching left fragment border
				// ending touching right fragment border

		IRegion[] expected= {
			new Region(80, 20),
			new Region(120, 20)
		};
		assertUnprojectedMasterRegions(expected, 60, 100);
	}

	@Test
	public void test29_21() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting touching left fragment border
				// ending right of fragment

		IRegion[] expected= {
			new Region(80, 20),
			new Region(120, 10)
		};
		assertUnprojectedMasterRegions(expected, 60, 70);
	}

	@Test
	public void test29_22() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting inside fragment
				// ending left of fragment

		IRegion[] expected= {
			new Region(80, 20),
			new Region(120, 10)
		};
		assertUnprojectedMasterRegions(expected, 70, 60);
	}

	@Test
	public void test29_23() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting inside fragment
				// ending touching left fragment border

		IRegion[] expected= {
			new Region(80, 20),
			new Region(120, 20)
		};
		assertUnprojectedMasterRegions(expected, 70, 70);
	}

	@Test
	public void test29_24() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting inside fragment
				// ending inside fragment

		IRegion[] expected= {
			new Region(80, 20)
		};
		assertUnprojectedMasterRegions(expected, 70, 40);
	}

	@Test
	public void test29_25() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting inside fragment
				// ending touching right fragment border

		IRegion[] expected= {
			new Region(80, 20),
			new Region(120, 20)
		};
		assertUnprojectedMasterRegions(expected, 70, 90);
	}

	@Test
	public void test29_26() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting inside fragment
				// ending right of fragment

		IRegion[] expected= {
			new Region(80, 20),
			new Region(120, 10)
		};
		assertUnprojectedMasterRegions(expected, 70, 60);
	}

	@Test
	public void test29_27() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting touching right fragment border
				// ending left of fragment

		IRegion[] expected= {
			new Region(40, 20),
			new Region(80, 20),
			new Region(120, 10)
		};
		assertUnprojectedMasterRegions(expected, 40, 90);
	}

	@Test
	public void test29_28() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting touching right fragment border
				// ending touching left fragment border

		IRegion[] expected= {
			new Region(40, 20),
			new Region(80, 20),
			new Region(120, 20)
		};
		assertUnprojectedMasterRegions(expected, 40, 100);
	}

	@Test
	public void test29_29() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting touching right fragment border
				// ending inside fragment

		IRegion[] expected= {
			new Region(40, 20),
			new Region(80, 20)
		};
		assertUnprojectedMasterRegions(expected, 40, 70);
	}

	@Test
	public void test29_30() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting touching right fragment border
				// ending touching right fragment border

		IRegion[] expected= {
			new Region(40, 20),
			new Region(80, 20),
			new Region(120, 20)
		};
		assertUnprojectedMasterRegions(expected, 40, 120);
	}

	@Test
	public void test29_31() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting touching right fragment border
				// ending right of fragment

		IRegion[] expected= {
			new Region(40, 20),
			new Region(80, 20),
			new Region(120, 10)
		};
		assertUnprojectedMasterRegions(expected, 40, 90);
	}

	@Test
	public void test29_32() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting right of fragment
				// ending left of fragment

		IRegion[] expected= {
			new Region(50, 10),
			new Region(80, 20),
			new Region(120, 10)
		};
		assertUnprojectedMasterRegions(expected, 50, 80);
	}

	@Test
	public void test29_33() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting right of fragment
				// ending touching left fragment border

		IRegion[] expected= {
			new Region(50, 10),
			new Region(80, 20),
			new Region(120, 20)
		};
		assertUnprojectedMasterRegions(expected, 50, 90);
	}

	@Test
	public void test29_34() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting right of fragment
				// ending inside fragment

		IRegion[] expected= {
			new Region(50, 10),
			new Region(80, 20)
		};
		assertUnprojectedMasterRegions(expected, 50, 60);
	}

	@Test
	public void test29_35() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting right of fragment
				// ending touching right fragment border

		IRegion[] expected= {
			new Region(50, 10),
			new Region(80, 20),
			new Region(120, 20)
		};
		assertUnprojectedMasterRegions(expected, 50, 110);
	}

	@Test
	public void test29_36() {
		// test computation of unprojected  master regions
		// spanning multiple fragments
			// starting right of fragment
				// ending right of fragment

		IRegion[] expected= {
			new Region(50, 10),
			new Region(80, 20),
			new Region(120, 10)
		};
		assertUnprojectedMasterRegions(expected, 50, 80);
	}

	@Test
	public void test29_37() {
		// test computation of unprojected  master regions

		createProjectionB();
		try {
			IRegion[] regions= fSlaveDocument.computeUnprojectedMasterRegions(0, 180);
			IRegion[] expected= {
				new Region(0, 20),
				new Region(40, 20),
				new Region(80, 20),
				new Region(120, 20),
				new Region(160, 20)
			};
			assertRegions(expected, regions);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test29_38() {
		// test computation of unprojected  master regions
		createProjectionA();
		try {
			IRegion[] regions= fSlaveDocument.computeUnprojectedMasterRegions(0, 180);
			IRegion[] expected= {
				new Region(20, 20),
				new Region(60, 20),
				new Region(100, 20),
				new Region(140, 20)
			};
			assertRegions(expected, regions);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}
}
