/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.tests;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISlaveDocumentManager;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.projection.Fragment;
import org.eclipse.jface.text.projection.Segment;

public class ProjectionDocumentTest extends TestCase {
	
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
		
		/*
		 * @see org.eclipse.jface.text.projection.ProjectionDocument#isUpdating()
		 */
		protected boolean isUpdating() {
			return super.isUpdating() || isUpdating;
		}
	}
	
	static private class ProjectionDocumentManager extends org.eclipse.jface.text.projection.ProjectionDocumentManager {
		/*
		 * @see org.eclipse.jface.text.projection.ProjectionDocumentManager#createProjectionDocument(org.eclipse.jface.text.IDocument)
		 */
		protected org.eclipse.jface.text.projection.ProjectionDocument createProjectionDocument(IDocument master) {
			return new ProjectionDocument(master);
		}
	}
	
	static private boolean LINES= true;
	
	
	private ProjectionDocument fSlaveDocument;
	private IDocument fMasterDocument;
	private ISlaveDocumentManager fSlaveDocumentManager;
	
	
	public ProjectionDocumentTest(String name) {
		super(name);
	}
	
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

	protected void setUp() {
		fMasterDocument= new Document();
		fMasterDocument.set(getOriginalMasterContents());
		fSlaveDocumentManager= new ProjectionDocumentManager();
		fSlaveDocument= (ProjectionDocument) fSlaveDocumentManager.createSlaveDocument(fMasterDocument);
	}
	
	protected void tearDown () {
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
			assertFalse(segmentation.length > 1 && segmentation[i].getLength() == 0);
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
			assertFalse(segmentation.length > 1 && fragment.getLength() == 0);
			assertTrue(fragment.length == segment.length);
			if (previous != null)
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
			assertEquals(print(actual) + " != " + print(expected[i]), expected[i], actual);
		}
		
	}
	
	private void assertLineInformationConsistency(IDocument document) {
		DefaultLineTracker textTracker= new DefaultLineTracker();
		textTracker.set(document.get());
		
		int textLines= textTracker.getNumberOfLines();
		int trackerLines= document.getNumberOfLines();
		assertEquals(trackerLines, textLines);
		
		for (int i= 0; i < trackerLines; i++) {
			try {
				IRegion trackerLine= document.getLineInformation(i);
				IRegion textLine= textTracker.getLineInformation(i);
				
				assertEquals(trackerLine.getOffset(), textLine.getOffset());
				assertEquals(trackerLine.getLength(), textLine.getLength());
			
			} catch (BadLocationException e) {
				assertTrue(false);
			}
		}
	}
	
	private void assertContents(String expected, IDocument document) {
		assertWellFormedSegmentation();
		assertWellFormedFragmentation();
		assertEquals(expected, document.get());
		assertLineInformationConsistency(document);
	}
	
	private void assertSlaveContents(String expected) {
		assertContents(expected, fSlaveDocument);
	}
	
	private void assertMasterContents(String expected) {
		assertContents(expected, fMasterDocument);
	}

	public void test1() {
		// test identical projection
		createIdenticalProjection();
		assertSlaveContents(fMasterDocument.get());
	}

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
	
	public void test3() {
		// test standard projection, i.e. all odd digits
		createProjectionA();
		assertSlaveContents(getProjectionASlaveContents());
	}
	
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
	
	public void test7() {
		// test replacing the contents of the master document
		createProjectionA();
		try {
			fMasterDocument.replace(0, fMasterDocument.getLength(), "~~~~~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		assertSlaveContents("~~~~~");
		
		Position[] expected= new Position[] { new Position(0, 5) };
		assertFragmentation(expected);
	}
	
	public void test8_a() {
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

	public void test8_b() {
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

	public void test8_c() {
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

	public void test8_d() {
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

	public void test8_e() {
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

	public void test8_f() {
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

	public void test8_g() {
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

	public void test8_h() {
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

	public void test8_i() {
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

	public void test8_j() {
		// test corner case manipulation of the projected regions of the master document
		// insert at the end of a projected region of the master document
		// -> slave document unchanged as this is interpreted as "beginning of an unprojected region"
		
		test9_a();
	}

	public void test8_k() {
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
	
	public void test8_l() {
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
	
	public void test9_a() {
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
	
	public void test9_b() {
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
	
	public void test9_c() {
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
	
	public void test9_d() {
		// test corner case manipulation of the unprojected regions of the master document
		// insert at the end of an unprojected region	
		// -> slave document changed, as this is interpreted as "beginning of a projected region"
		
		test8_g();
	}
	
	public void test9_e() {
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
	
	public void test9_f() {
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
	
	/*
	 * Replace in the master document at the end offset of the slave document
	 * 
	 * [formatting] IllegalArgumentException when formatting comment code snippet in segmented mode
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=51594
	 */
	public void test9_g() {
		if (!LINES)
			return;
		
		try {
			int startOffset= fMasterDocument.getLineOffset(4);
			assertEquals(80, startOffset);
			int endOffset= fMasterDocument.getLineOffset(7);
			assertEquals(140, endOffset);
			fSlaveDocument.addMasterDocumentRange(startOffset, endOffset - startOffset);
			
			assertSlaveContents(getOriginalMasterContents().substring(80, 140));
			
			fMasterDocument.replace(endOffset, 1, "x");
			assertLineInformationConsistency(fSlaveDocument);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}
	
	public void test10_a() {
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
	
	public void test10_b() {
		// test manipulations overlapping projected and unprojected regions of the master document
		// replace range overlapping from a projected into an unprojected region
		// => replaced range will appear in slave document because of auto expansion of slave document in case of overlapping events
		
		createProjectionA();
		try {
			fMasterDocument.replace(50, 20, "~~~~~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
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
	
	public void test10_c() {
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
	
	public void test10_d() {
		// test manipulations overlapping projected and unprojected regions of the master document
		// replace range overlapping from an unprojected into a projected region
		// -> replaced range will appear in slave document because of auto expansion of slave document in case of overlapping events

		createProjectionA();
		try {
			fMasterDocument.replace(70, 20, "~~~~~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
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
	
	public void test11() {
		// test deleting an unprojected region of the master document
		
		createProjectionA();
		try {
			fMasterDocument.replace(60, 20, "");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		Position[] expected= new Position[] {
				new Position(0, 20),
				new Position(40, 40),
				new Position(100, 20),
				new Position(140, 20)
		};
		assertFragmentation(expected);
		
		assertSlaveContents(getProjectionASlaveContents());
	}
	
	public void test12() {
		// test deleting a projected region of the master document
		
		createProjectionA();
		try {
			fMasterDocument.replace(80, 20, "");
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
				new Position(0, 20),
				new Position(40, 20),
				new Position(100, 20),
				new Position(140, 20)
		};
		assertFragmentation(expected);
	}
	
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
	
	public void test14_a() {
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

	public void test14_b() {
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
	
	public void test17_a() {
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

	public void test17_b() {
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
	
	public void test17_c() {
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
	
	public void test17_d() {
		// test corner case manipulation of the segments of the slave document		
		// insert at the end of a segment of the slave document
		// interpreted as "insert at the beginning of the next segment"
		
		test17_a();
	}
	
	public void test17_e() {
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
	
	public void test17_f() {
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
	
	public void test18_a() {
		// test manipulations overlapping multiple segments of the slave document
		// delete range overlapping two neighboring segments
		
		createProjectionA();
		
		try {
			fSlaveDocument.replace(30, 20, "");
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
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

	public void test18_b() {
		// test manipulations overlapping multiple segments of the slave document
		// replace range overlapping two neighboring segments

		createProjectionA();
		
		try {
			fSlaveDocument.replace(30, 20, "~~~~~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
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
		
		Position[] expected= new Position[] {
			new Position(0, 20),
			new Position(60, 20),
			new Position(100, 20),
			new Position(140, 20)
		};
		assertFragmentation(expected);
	}
	
	public void test20_a() {
		// test adding a range to the slave document at the beginning of a segment gap
		
		createProjectionA();
		
		try {
			fSlaveDocument.addMasterDocumentRange(60, 10);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
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

	public void test20_b() {
		// test adding a range to the slave document at the end of a segment gap
		
		createProjectionA();
		
		try {
			fSlaveDocument.addMasterDocumentRange(70, 10);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
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

	public void test20_c() {
		// test adding a range to the slave document that is in the middle of a segment gap
		
		createProjectionA();
		
		try {
			fSlaveDocument.addMasterDocumentRange(65, 10);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
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
	
	public void test20_d() {
		// test adding a range to the slave document that is a segment gap
		
		createProjectionA();
		
		try {
			fSlaveDocument.addMasterDocumentRange(60, 20);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
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

	public void test20_e() {
		// test adding a range to the slave document beginning in a segment gap and ending in a segment
		
		createProjectionA();
		
		try {
			fSlaveDocument.addMasterDocumentRange(70, 20);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
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

	public void test20_f() {
		// test adding a range to the slave document beginning in a segment and ending in a segment gap
		
		createProjectionA();
		
		try {
			fSlaveDocument.addMasterDocumentRange(50, 20);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
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

	public void test20_g() {
		// test adding a range to the slave document beginning in a segment and ending in a segment
		
		createProjectionA();
		
		try {
			fSlaveDocument.addMasterDocumentRange(50, 40);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
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

	public void test20_h() {
		// test adding a range to the slave document beginning in a segment gap and ending in a segment gap
		
		createProjectionA();
		
		try {
			fSlaveDocument.addMasterDocumentRange(70, 40);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
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
	
	public void test21_a() {
		// test removing a range from the slave document at the beginning of a segment
		
		createProjectionA();
		
		try {
			fSlaveDocument.removeMasterDocumentRange(40, 10);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
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

	public void test21_b() {
		// test removing a range from the slave document at the end of a segment
		
		createProjectionA();
		
		try {
			fSlaveDocument.removeMasterDocumentRange(50, 10);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
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

	public void test21_c() {
		// test removing a range from the slave document that is in the middle of a segment
		
		createProjectionA();
		
		try {
			fSlaveDocument.removeMasterDocumentRange(85, 10);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
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
	
	public void test21_d() {
		// test removing a range from the slave document that is a segment
		
		createProjectionA();
		
		try {
			fSlaveDocument.removeMasterDocumentRange(40, 20);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
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
	
	public void test21_e() {
		// test removing a range from the slave document beginning in a segment and ending in a segment gap
		
		createProjectionA();
		
		try {
			fSlaveDocument.removeMasterDocumentRange(50, 20);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
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

	public void test21_f() {
		// test removing a range from the slave document beginning in a segment gap and ending in a segment
		
		createProjectionA();
		
		try {
			fSlaveDocument.removeMasterDocumentRange(70, 20);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
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

	public void test21_g() {
		// test removing a range from the slave document beginning in a segment gap and ending in a segment gap
		
		createProjectionA();
		
		try {
			fSlaveDocument.removeMasterDocumentRange(70, 40);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
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

	public void test21_h() {
		// test removing a range from the slave document beginning in a segment and ending in a segment
		
		createProjectionA();
		
		try {
			fSlaveDocument.removeMasterDocumentRange(50, 40);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
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

	public void test21_i() {
		// test removing a range from the slave document using identical projection
		
		createIdenticalProjection();
		
		try {
			fSlaveDocument.removeMasterDocumentRange(50, 40);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
			new Position(0, 50),
			new Position(90, 90)
		};
		assertFragmentation(expected);
		
		StringBuffer buffer= new StringBuffer(getOriginalMasterContents());
		buffer.delete(50, 90);
		assertSlaveContents(buffer.toString());
	}
	
	private void assertEquals(DocumentEvent expected, DocumentEvent received) {
		assertSame(expected.getDocument(), received.getDocument());
		assertEquals(expected.getOffset(), received.getOffset());
		assertEquals(expected.getLength(), received.getLength());
		if (expected.getText() == null || expected.getText().length() == 0)
			assertTrue(received.getText() == null || received.getText().length() == 0);
		else
			assertEquals(expected.getText(), received.getText());
	}
	
	private void assertSlaveEvents(DocumentEvent[] expected, DocumentEvent[] received) {
		if (expected == null)
			assertNull(received);
		
		assertTrue(expected.length == received.length);
		
		for (int i= 0; i < received.length; i++)
			assertEquals(received[i], expected[i]);
	}
	
	public void test22() {
		// test document events sent out by the slave document when adding segments
		
		final List receivedEvents= new ArrayList();
		
		IDocumentListener listener= new IDocumentListener() {
			public void documentAboutToBeChanged(DocumentEvent event) {}
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

	public void test23() {
		// test document events sent out by the slave document when removing segments
		
		final List receivedEvents= new ArrayList();
		
		IDocumentListener listener= new IDocumentListener() {
			public void documentAboutToBeChanged(DocumentEvent event) {}
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
	
	public void test24a() {
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
		
		Position[] expected= new Position[] {
				new Position(5, 35),
				new Position(60, 20),
				new Position(100, 20),
				new Position(140, 20)
		};
		assertFragmentation(expected);
	}
	
	public void test24b() {
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
		
		Position[] expected= new Position[] {
				new Position(20,20),
				new Position(60, 20),
				new Position(100, 20),
				new Position(140, 35)
		};
		assertFragmentation(expected);
	}
	
	public void test24c() {
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
		
		Position[] expected= new Position[] {
				new Position(20, 20),
				new Position(45, 35),
				new Position(100, 20),
				new Position(140, 20)
		};
		assertFragmentation(expected);
	}
	
	public void test24d() {
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
		
		Position[] expected= new Position[] {
				new Position(20, 20),
				new Position(60, 20),
				new Position(85, 35),
				new Position(140, 20)
		};
		assertFragmentation(expected);
	}
	
	public void test24e() {
		// test auto expand mode when manipulating the master document
		// master event start left of fragment and end inside of a fragment
		
		createProjectionB();
		fSlaveDocument.setAutoExpandMode(true);
		
		try {
			DocumentEvent event= new DocumentEvent(fMasterDocument, 50, 20, "~");
			fSlaveDocument.adaptProjectionToMasterChange2(event);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
				new Position(20, 20),
				new Position(50, 30),
				new Position(100, 20),
				new Position(140, 20)
		};
		assertFragmentation(expected);
	}
	
	public void test24f() {
		// test auto expand mode when manipulating the master document
		// master event start inside of a fragment and ends right of a fragment
		
		createProjectionB();
		fSlaveDocument.setAutoExpandMode(true);
		
		try {
			DocumentEvent event= new DocumentEvent(fMasterDocument, 70, 20, "~");
			fSlaveDocument.adaptProjectionToMasterChange2(event);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
				new Position(20, 20),
				new Position(60, 30),
				new Position(100, 20),
				new Position(140, 20)
		};
		assertFragmentation(expected);
	}
	
	public void test24g() {
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
		
		Position[] expected= new Position[] {
				new Position(20, 20),
				new Position(50, 40),
				new Position(100, 20),
				new Position(140, 20)
		};
		assertFragmentation(expected);
	}
	
	public void test25() {
		// test auto expand mode when manipulating the slave document
		
		try {
			fSlaveDocument.isUpdating= true;
			fSlaveDocument.adaptProjectionToMasterChange2(new DocumentEvent(fSlaveDocument, 0, 0, "~"));
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
				new Position(0, 0)
		};
		assertFragmentation(expected, false);
	}
	
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
	
	public void test27() {
		// test changing the projection until identical projection is reached
		
		createProjectionA();

		Position[] expected= new Position[] {
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
		
	public void test28_a() {
		// delete slave content and check fragmentation, need to keep a single fragment as anchor
		createProjectionB();
		try {
			fSlaveDocument.replace(0, fSlaveDocument.getLength(), null);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		assertSlaveContents("");
		StringBuffer buffer= new StringBuffer(getOriginalMasterContents());
		buffer.delete(20, 160);
		assertMasterContents(buffer.toString());
		
		Position[] expected= new Position[] {
			new Position(20, 0)
		};
		assertFragmentation(expected);
	}
	
	public void test28_b() {
		// test step wise version of the complete replace
		// delete whole content of slave, followed by inserting text
		
		createProjectionB();
		try {
			fSlaveDocument.replace(0, fSlaveDocument.getLength(), null);
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		try {
			fSlaveDocument.replace(0, 0, "~~~~~");
		} catch (BadLocationException e) {
			assertTrue(false);
		}
		
		Position[] expected= new Position[] {
			new Position(20, 5)
		};
		assertFragmentation(expected);
		
		assertSlaveContents("~~~~~");
		StringBuffer buffer= new StringBuffer(getOriginalMasterContents());
		buffer.replace(20, 160, "~~~~~");
		assertMasterContents(buffer.toString());		
	}
}
