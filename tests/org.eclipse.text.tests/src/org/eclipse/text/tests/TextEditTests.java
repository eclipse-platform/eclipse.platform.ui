/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
package org.eclipse.text.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.After;
import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.text.edits.CopySourceEdit;
import org.eclipse.text.edits.CopyTargetEdit;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.ISourceModifier;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MoveSourceEdit;
import org.eclipse.text.edits.MoveTargetEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.RangeMarker;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditCopier;
import org.eclipse.text.edits.UndoEdit;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

public class TextEditTests {

	private IDocument fDocument;
	private MultiTextEdit fRoot;

	@Before
	public void setUp() {
		fDocument= new Document("0123456789");
		fRoot= new MultiTextEdit();
	}

	@After
	public void tearDown() {
		fRoot= null;
		fRoot= null;
	}

	@Test
	public void testCovers1() throws Exception {
		InsertEdit insert= new InsertEdit(1, "");
		DeleteEdit delete= new DeleteEdit(2, 2);
		Assert.assertFalse(insert.covers(delete));
	}

	@Test
	public void testCovers2() throws Exception {
		MultiTextEdit multi= new MultiTextEdit(0,0);
		MultiTextEdit child= new MultiTextEdit(0,0);
		Assert.assertTrue(multi.covers(child));
	}

	@Test
	public void testOverlap1() throws Exception {
		// [ [ ] ]
		fRoot.addChild(new ReplaceEdit(0, 2, "01"));
		boolean exception= false;
		try {
			fRoot.addChild(new ReplaceEdit(1, 2, "12"));
		} catch (MalformedTreeException e) {
			exception= true;
		}
		assertTrue(exception);
	}

	@Test
	public void testOverlap2() throws Exception {
		// [[ ] ]
		fRoot.addChild(new ReplaceEdit(0, 2, "01"));
		boolean exception= false;
		try {
			fRoot.addChild(new ReplaceEdit(0, 1, "0"));
		} catch (MalformedTreeException e) {
			exception= true;
		}
		assertTrue(exception);
	}

	@Test
	public void testOverlap3() throws Exception {
		// [ [ ]]
		fRoot.addChild(new ReplaceEdit(0, 2, "01"));
		boolean exception= false;
		try {
			fRoot.addChild(new ReplaceEdit(1, 1, "1"));
		} catch (MalformedTreeException e) {
			exception= true;
		}
		assertTrue(exception);
	}

	@Test
	public void testOverlap4() throws Exception {
		// [ [ ] ]
		fRoot.addChild(new ReplaceEdit(0, 3, "012"));
		boolean exception= false;
		try {
			fRoot.addChild(new ReplaceEdit(1, 1, "1"));
		} catch (MalformedTreeException e) {
			exception= true;
		}
		assertTrue(exception);
	}

	@Test
	public void testOverlap5() throws Exception {
		// [ []  ]
		fRoot.addChild(new ReplaceEdit(0, 3, "012"));
		boolean exception= false;
		try {
			fRoot.addChild(new InsertEdit(1, "xx"));
		} catch (MalformedTreeException e) {
			exception= true;
		}
		assertTrue(exception);
	}

	@Test
	public void testOverlap6() throws Exception {
		// [  [] ]
		fRoot.addChild(new ReplaceEdit(0, 3, "012"));
		boolean exception= false;
		try {
			fRoot.addChild(new InsertEdit(2, "xx"));
		} catch (MalformedTreeException e) {
			exception= true;
		}
		assertTrue(exception);
	}

	@Test
	public void testOverlap7() throws Exception {
		MoveSourceEdit source= new MoveSourceEdit(2, 5);
		MoveTargetEdit target= new MoveTargetEdit(3, source);
		fRoot.addChild(source);
		boolean exception= false;
		try {
			fRoot.addChild(target);
		} catch (MalformedTreeException e) {
			exception= true;
		}
		assertTrue(exception);
	}

	@Test
	public void testOverlap8() throws Exception {
		MoveSourceEdit source= new MoveSourceEdit(2, 5);
		MoveTargetEdit target= new MoveTargetEdit(6, source);
		fRoot.addChild(source);
		boolean exception= false;
		try {
			fRoot.addChild(target);
		} catch (MalformedTreeException e) {
			exception= true;
		}
		assertTrue(exception);
	}

	@Test
	public void testOverlap9() throws Exception {
		MoveSourceEdit s1= new MoveSourceEdit(3, 1);
		MoveTargetEdit t1= new MoveTargetEdit(7, s1);
		MoveSourceEdit s2= new MoveSourceEdit(2, 3);
		MoveTargetEdit t2= new MoveTargetEdit(8, s2);
		fRoot.addChild(s1);
		fRoot.addChild(t1);
		boolean exception= false;
		try {
			fRoot.addChild(s2);
			fRoot.addChild(t2);
		} catch (MalformedTreeException e) {
			exception= true;
		}
		assertTrue(exception);
	}

	@Test
	public void testUndefinedMultiEdit1() throws Exception {
		MultiTextEdit m1= new MultiTextEdit();
		m1.addChild(new InsertEdit(0,""));
		fRoot.addChild(m1);

		MultiTextEdit m2= new MultiTextEdit();
		m2.addChild(new InsertEdit(2, ""));
		fRoot.addChild(m2);
	}

	@Test
	public void testUndefinedMultiEdit2() throws Exception {
		MultiTextEdit m1= new MultiTextEdit();
		MultiTextEdit m2= new MultiTextEdit();
		assertTrue(m1.covers(m2));
		assertTrue(m2.covers(m1));
	}

	@Test
	public void testUndefinedMultiEdit3() throws Exception {
		MultiTextEdit m2= new MultiTextEdit();
		Assert.assertEquals(0, m2.getOffset());
		Assert.assertEquals(0, m2.getLength());
		m2.addChild(new DeleteEdit(1,3));
		Assert.assertEquals(1, m2.getOffset());
		Assert.assertEquals(3, m2.getLength());
	}

	@Test
	public void testUndefinedMultiEdit4() throws Exception {
		MultiTextEdit m2= new MultiTextEdit();
		m2.addChild(new DeleteEdit(1,3));
		m2.addChild(new DeleteEdit(4, 2));
		Assert.assertEquals(1, m2.getOffset());
		Assert.assertEquals(5, m2.getLength());
	}

	@Test
	public void testUndefinedMultiEdit5() throws Exception {
		MultiTextEdit m2= new MultiTextEdit();
		m2.addChild(new DeleteEdit(4, 2));
		m2.addChild(new DeleteEdit(1,3));
		Assert.assertEquals(1, m2.getOffset());
		Assert.assertEquals(5, m2.getLength());
	}

	@Test
	public void testUndefinedMultiEdit6() throws Exception {
		DeleteEdit d1= new DeleteEdit(1,3);
		MultiTextEdit m2= new MultiTextEdit();
		assertTrue(d1.covers(m2));
	}

	@Test
	public void testUnconnected1() throws Exception {
		MoveSourceEdit s1= new MoveSourceEdit(3, 1);
		boolean exception= false;
		try {
			fRoot.addChild(s1);
			fRoot.apply(fDocument);
		} catch (MalformedTreeException e) {
			exception= true;
		}
		assertTrue(exception);
	}

	@Test
	public void testBufferLength() throws Exception {
		MultiTextEdit edit= new MultiTextEdit(0, fDocument.getLength() + 1);
		boolean exception= false;
		try {
			fRoot.addChild(edit);
			fRoot.apply(fDocument);
		} catch (MalformedTreeException e) {
			exception= true;
			assertTrue(exception);
		}
	}

	@Test
	public void testCopy1() throws Exception {
		MultiTextEdit root= new MultiTextEdit();
		TextEdit e1= new InsertEdit(2, "yy");
		TextEdit e2= new ReplaceEdit(2, 3, "3456");
		root.addChild(e1);
		root.addChild(e2);
		List<TextEdit> org= flatten(root);
		TextEditCopier copier= new TextEditCopier(root);
		List<TextEdit> copy= flatten(copier.perform());
		compare(org, copy);
	}

	@Test
	public void testCopy2() throws Exception {
		MultiTextEdit root= new MultiTextEdit();
		CopySourceEdit s1= new CopySourceEdit(5, 2);
		CopyTargetEdit t1= new CopyTargetEdit(8, s1);
		CopySourceEdit s2= new CopySourceEdit(5, 2);
		CopyTargetEdit t2= new CopyTargetEdit(2, s2);
		s1.addChild(s2);
		root.addChild(s1);
		root.addChild(t1);
		root.addChild(t2);
		List<TextEdit> org= flatten(root);
		TextEditCopier copier= new TextEditCopier(root);
		List<TextEdit> copy= flatten(copier.perform());
		compare(org, copy);
	}

	private List<TextEdit> flatten(TextEdit edit) {
		List<TextEdit> result= new ArrayList<>();
		flatten(result, edit);
		return result;
	}

	private static void flatten(List<TextEdit> result, TextEdit edit) {
		result.add(edit);
		TextEdit[] children= edit.getChildren();
		for (TextEdit c : children) {
			flatten(result, c);
		}
	}

	private static void compare(List<TextEdit> org, List<TextEdit> copy) {
		assertTrue("Same length", org.size() == copy.size());
		for (TextEdit edit : copy) {
			assertTrue("Original is part of copy list", !org.contains(edit));
			if (edit instanceof MoveSourceEdit) {
				MoveSourceEdit source= (MoveSourceEdit)edit;
				assertTrue("Target edit isn't a copy", copy.contains(source.getTargetEdit()));
				assertTrue("Traget edit is a original", !org.contains(source.getTargetEdit()));
			} else if (edit instanceof MoveTargetEdit) {
				MoveTargetEdit target= (MoveTargetEdit)edit;
				assertTrue("Source edit isn't a copy", copy.contains(target.getSourceEdit()));
				assertTrue("Source edit is a original", !org.contains(target.getSourceEdit()));
			} else if (edit instanceof CopySourceEdit) {
				CopySourceEdit source= (CopySourceEdit)edit;
				assertTrue("Target edit isn't a copy", copy.contains(source.getTargetEdit()));
				assertTrue("Traget edit is a original", !org.contains(source.getTargetEdit()));
			} else if (edit instanceof CopyTargetEdit) {
				CopyTargetEdit target= (CopyTargetEdit)edit;
				assertTrue("Source edit isn't a copy", copy.contains(target.getSourceEdit()));
				assertTrue("Source edit is a original", !org.contains(target.getSourceEdit()));
			}
		}
	}

	@Test
	public void testInsert1() throws Exception {
		// [][  ]
		TextEdit e1= new InsertEdit(2, "yy");
		TextEdit e2= new ReplaceEdit(2, 3, "3456");
		fRoot.addChild(e1);
		fRoot.addChild(e2);
		UndoEdit undo= fRoot.apply(fDocument);
		assertEquals(fRoot, 2, 6);
		assertEquals(e1, 2, 2);
		assertEquals(e2, 4, 4);
		Assert.assertEquals("Buffer content", "01yy345656789", fDocument.get());
		doUndoRedo(undo, "01yy345656789");
	}

	@Test
	public void testInsert2() throws Exception {
		// [][]
		TextEdit e1= new InsertEdit(2, "yy");
		TextEdit e2= new InsertEdit(2, "xx");
		fRoot.addChild(e1);
		fRoot.addChild(e2);
		UndoEdit undo= fRoot.apply(fDocument);
		assertEquals(fRoot, 2, 4);
		assertEquals(e1, 2, 2);
		assertEquals(e2, 4, 2);
		Assert.assertEquals("Buffer content", "01yyxx23456789", fDocument.get());
		doUndoRedo(undo, "01yyxx23456789");
	}

	@Test
	public void testInsert3() throws Exception {
		// [  ][][  ]
		TextEdit e1= new ReplaceEdit(0, 2, "011");
		TextEdit e2= new InsertEdit(2, "xx");
		TextEdit e3= new ReplaceEdit(2, 2, "2");
		fRoot.addChild(e1);
		fRoot.addChild(e2);
		fRoot.addChild(e3);
		UndoEdit undo= fRoot.apply(fDocument);
		assertEquals(fRoot, 0, 6);
		assertEquals(e1, 0, 3);
		assertEquals(e2, 3, 2);
		assertEquals(e3, 5, 1);
		Assert.assertEquals("Buffer content", "011xx2456789", fDocument.get());
		doUndoRedo(undo, "011xx2456789");
	}

	@Test
	public void testInsert4() throws Exception {
		TextEdit e1= new InsertEdit(0, "xx");
		fRoot.addChild(e1);
		UndoEdit undo= fRoot.apply(fDocument);
		Assert.assertEquals("Buffer length", 12, fDocument.getLength());
		assertEquals(fRoot, 0, 2);
		assertEquals(e1, 0, 2);
		Assert.assertEquals("Buffer content", "xx0123456789", fDocument.get());
		doUndoRedo(undo, "xx0123456789");
	}

	@Test
	public void testInsert5() throws Exception {
		TextEdit e1= new InsertEdit(10, "xx");
		fRoot.addChild(e1);
		UndoEdit undo= fRoot.apply(fDocument);
		Assert.assertEquals("Buffer length", 12, fDocument.getLength());
		assertEquals(fRoot, 10, 2);
		assertEquals(e1, 10, 2);
		Assert.assertEquals("Buffer content", "0123456789xx", fDocument.get());
		doUndoRedo(undo, "0123456789xx");
	}

	@Test
	public void testInsertReplace1() throws Exception {
		TextEdit e1= new ReplaceEdit(2, 1, "y");
		TextEdit e2= new InsertEdit(2, "xx");
		fRoot.addChild(e1);
		fRoot.addChild(e2);
		UndoEdit undo= fRoot.apply(fDocument);
		assertEquals(fRoot, 2, 3);
		assertEquals(e1, 4, 1);
		assertEquals(e2, 2, 2);
		Assert.assertEquals("Buffer content", "01xxy3456789", fDocument.get());
		doUndoRedo(undo, "01xxy3456789");
	}

	@Test
	public void testDelete1() throws Exception {
		TextEdit e1= new DeleteEdit(3, 1);
		fRoot.addChild(e1);
		UndoEdit undo= fRoot.apply(fDocument);
		assertEquals(fRoot, 3, 0);
		assertEquals(e1, 3, 0);
		Assert.assertEquals("Buffer content", "012456789", fDocument.get());
		doUndoRedo(undo, "012456789");
	}

	@Test
	public void testDelete2() throws Exception {
		TextEdit e1= new DeleteEdit(4, 1);
		TextEdit e2= new DeleteEdit(3, 1);
		TextEdit e3= new DeleteEdit(5, 1);
		fRoot.addChild(e1);
		fRoot.addChild(e2);
		fRoot.addChild(e3);
		UndoEdit undo= fRoot.apply(fDocument);
		assertEquals(fRoot, 3, 0);
		assertEquals(e1, 3, 0);
		assertEquals(e2, 3, 0);
		assertEquals(e3, 3, 0);
		Assert.assertEquals("Buffer content", "0126789", fDocument.get());
		doUndoRedo(undo, "0126789");
	}

	@Test
	public void testDelete3() throws Exception {
		TextEdit e1= new InsertEdit(3, "x");
		TextEdit e2= new DeleteEdit(3, 1);
		fRoot.addChild(e1);
		fRoot.addChild(e2);
		UndoEdit undo= fRoot.apply(fDocument);
		assertEquals(fRoot, 3, 1);
		assertEquals(e1, 3, 1);
		assertEquals(e2, 4, 0);
		Assert.assertEquals("Buffer content", "012x456789", fDocument.get());
		doUndoRedo(undo, "012x456789");
	}

	@Test
	public void testDeleteWithChildren() throws Exception {
		TextEdit e1= new DeleteEdit(2, 6);
		MultiTextEdit e2= new MultiTextEdit(3, 3);
		e1.addChild(e2);
		TextEdit e3= new ReplaceEdit(3, 1, "xx");
		TextEdit e4= new ReplaceEdit(5, 1, "yy");
		e2.addChild(e3);
		e2.addChild(e4);
		fRoot.addChild(e1);
		UndoEdit undo= fRoot.apply(fDocument);
		Assert.assertEquals("Buffer content", "0189", fDocument.get());
		assertEquals(fRoot, 2, 0);
		assertEquals(e1, 2, 0);
		assertTrue(e2.isDeleted());
		assertTrue(e3.isDeleted());
		assertTrue(e4.isDeleted());
		doUndoRedo(undo, "0189");
	}

	@Test
	public void testTreeUpdate1() throws Exception {
		MultiTextEdit m1= new MultiTextEdit();
		TextEdit e1= new InsertEdit(2, "aa");
		TextEdit e2= new InsertEdit(4, "bb");
		m1.addChild(e1);
		m1.addChild(e2);
		MultiTextEdit m2= new MultiTextEdit();
		TextEdit e3= new InsertEdit(6, "cc");
		TextEdit e4= new InsertEdit(8, "dd");
		m2.addChild(e3);
		m2.addChild(e4);
		fRoot.addChild(m1);
		fRoot.addChild(m2);
		assertEquals(m1, 2, 2);
		assertEquals(m2, 6, 2);
		UndoEdit undo= fRoot.apply(fDocument);
		Assert.assertEquals("Buffer content", "01aa23bb45cc67dd89", fDocument.get());
		assertEquals(e1, 2, 2);
		assertEquals(e2, 6, 2);
		assertEquals(e3, 10, 2);
		assertEquals(e4, 14, 2);
		assertEquals(m1, 2, 6);
		assertEquals(m2, 10, 6);
		assertEquals(fRoot, 2, 14);
		doUndoRedo(undo, "01aa23bb45cc67dd89");
	}

	@Test
	public void testMove1() throws Exception {
		MoveSourceEdit s1= new MoveSourceEdit(2, 2);
		MoveTargetEdit t1= new MoveTargetEdit(5, s1);
		fRoot.addChild(s1);
		fRoot.addChild(t1);
		UndoEdit undo= fRoot.apply(fDocument);
		Assert.assertEquals("Buffer content", "0142356789", fDocument.get());
		assertEquals(s1, 2, 0);
		assertEquals(t1, 3, 2);
		doUndoRedo(undo, "0142356789");
	}

	@Test
	public void testMove2() throws Exception {
		MoveSourceEdit s1= new MoveSourceEdit(5, 2);
		MoveTargetEdit t1= new MoveTargetEdit(2, s1);
		fRoot.addChild(s1);
		fRoot.addChild(t1);
		UndoEdit undo= fRoot.apply(fDocument);
		Assert.assertEquals("Buffer content", "0156234789", fDocument.get());
		assertEquals(s1, 7, 0);
		assertEquals(t1, 2, 2);
		doUndoRedo(undo, "0156234789");
	}

	@Test
	public void testMove3() throws Exception {
		MoveSourceEdit s1= new MoveSourceEdit(2, 2);
		MoveTargetEdit t1= new MoveTargetEdit(7, s1);
		TextEdit e2= new ReplaceEdit(4, 1, "x");
		fRoot.addChild(s1);
		fRoot.addChild(t1);
		fRoot.addChild(e2);
		UndoEdit undo= fRoot.apply(fDocument);
		Assert.assertEquals("Buffer content", "01x5623789", fDocument.get());
		assertEquals(s1, 2, 0);
		assertEquals(t1, 5, 2);
		assertEquals(e2, 2, 1);
		doUndoRedo(undo, "01x5623789");
	}

	@Test
	public void testMove4() throws Exception {
		MoveSourceEdit s1= new MoveSourceEdit(7, 2);
		MoveTargetEdit t1= new MoveTargetEdit(2, s1);
		TextEdit e2= new ReplaceEdit(5, 1, "x");
		fRoot.addChild(s1);
		fRoot.addChild(t1);
		fRoot.addChild(e2);
		UndoEdit undo= fRoot.apply(fDocument);
		Assert.assertEquals("Buffer content", "0178234x69", fDocument.get());
		assertEquals(s1, 9, 0);
		assertEquals(t1, 2, 2);
		assertEquals(e2, 7, 1);
		doUndoRedo(undo, "0178234x69");
	}

	@Test
	public void testMove5() throws Exception {
		// Move onto itself
		MoveSourceEdit s1= new MoveSourceEdit(2, 1);
		MoveTargetEdit t1= new MoveTargetEdit(3, s1);
		TextEdit e2= new ReplaceEdit(2, 1, "x");
		s1.addChild(e2);
		fRoot.addChild(s1);
		fRoot.addChild(t1);
		UndoEdit undo= fRoot.apply(fDocument);
		assertEquals(s1, 2, 0);
		assertEquals(t1, 2, 1);
		assertEquals(e2, 2, 1);
		Assert.assertEquals("Buffer content", "01x3456789", fDocument.get());
		doUndoRedo(undo, "01x3456789");
	}

	@Test
	public void testMove6() throws Exception {
		// Move onto itself
		MoveSourceEdit s1= new MoveSourceEdit(2, 1);
		MoveTargetEdit t1= new MoveTargetEdit(2, s1);
		TextEdit e2= new ReplaceEdit(2, 1, "x");
		s1.addChild(e2);
		fRoot.addChild(s1);
		fRoot.addChild(t1);
		UndoEdit undo= fRoot.apply(fDocument);
		assertEquals(s1, 3, 0);
		assertEquals(t1, 2, 1);
		assertEquals(e2, 2, 1);
		Assert.assertEquals("Buffer content", "01x3456789", fDocument.get());
		doUndoRedo(undo,"01x3456789");
	}

	@Test
	public void testMove7() throws Exception {
		MoveSourceEdit s1= new MoveSourceEdit(2, 3);
		MoveTargetEdit t1= new MoveTargetEdit(7, s1);
		TextEdit e2= new ReplaceEdit(3, 1, "x");
		s1.addChild(e2);
		fRoot.addChild(s1);
		fRoot.addChild(t1);
		UndoEdit undo= fRoot.apply(fDocument);
		Assert.assertEquals("Buffer content", "01562x4789", fDocument.get());
		assertEquals(s1, 2, 0);
		assertEquals(t1, 4, 3);
		assertEquals(e2, 5, 1);
		doUndoRedo(undo, "01562x4789");
	}

	@Test
	public void testMove8() throws Exception {
		MoveSourceEdit s1= new MoveSourceEdit(5, 3);
		MoveTargetEdit t1= new MoveTargetEdit(1, s1);
		TextEdit e2= new ReplaceEdit(6, 1, "x");
		s1.addChild(e2);
		fRoot.addChild(s1);
		fRoot.addChild(t1);
		UndoEdit undo= fRoot.apply(fDocument);
		Assert.assertEquals("Buffer content", "05x7123489", fDocument.get());
		assertEquals(s1, 8, 0);
		assertEquals(t1, 1, 3);
		assertEquals(e2, 2, 1);
		doUndoRedo(undo, "05x7123489");
	}

	@Test
	public void testMove9() throws Exception {
		MoveSourceEdit s1= new MoveSourceEdit(1, 3);
		MoveTargetEdit t1= new MoveTargetEdit(5, s1);

		MoveSourceEdit s2= new MoveSourceEdit(1, 1);
		MoveTargetEdit t2= new MoveTargetEdit(3, s2);
		s1.addChild(s2);
		s1.addChild(t2);

		fRoot.addChild(s1);
		fRoot.addChild(t1);
		UndoEdit undo= fRoot.apply(fDocument);
		assertEquals(s1, 1, 0);
		assertEquals(t1, 2, 3);

		assertEquals(s2, 2, 0);
		assertEquals(t2, 3, 1);
		Assert.assertEquals("Buffer content", "0421356789", fDocument.get());
		doUndoRedo(undo, "0421356789");
	}

	@Test
	public void testMove10() throws Exception {
		MoveSourceEdit s1= new MoveSourceEdit(2, 2);
		MoveTargetEdit t1= new MoveTargetEdit(8, s1);
		MoveSourceEdit s2= new MoveSourceEdit(5, 2);
		MoveTargetEdit t2= new MoveTargetEdit(1, s2);

		fRoot.addChild(s1);
		fRoot.addChild(t1);
		fRoot.addChild(s2);
		fRoot.addChild(t2);

		UndoEdit undo= fRoot.apply(fDocument);
		assertEquals(s1, 4, 0);
		assertEquals(t1, 6, 2);
		assertEquals(s2, 5, 0);
		assertEquals(t2, 1, 2);
		Assert.assertEquals("Buffer content", "0561472389", fDocument.get());
		doUndoRedo(undo, "0561472389");
	}

	@Test
	public void testMoveWithRangeMarker() throws Exception {
		MoveSourceEdit s1= new MoveSourceEdit(2, 2);
		MoveTargetEdit t1= new MoveTargetEdit(5, s1);

		RangeMarker marker= new RangeMarker(2, 2);
		s1.addChild(marker);

		fRoot.addChild(s1);
		fRoot.addChild(t1);

		UndoEdit undo= fRoot.apply(fDocument);
		Assert.assertEquals("Buffer content", "0142356789", fDocument.get());
		assertEquals(s1, 2, 0);
		assertEquals(t1, 3, 2);
		assertEquals(marker, 3, 2);
		doUndoRedo(undo, "0142356789");
	}

	@Test
	public void testMoveWithTargetDelete() throws Exception {
		MoveSourceEdit s1= new MoveSourceEdit(2, 3);
		MoveTargetEdit t1= new MoveTargetEdit(7, s1);
		TextEdit e2= new DeleteEdit(6, 2);
		e2.addChild(t1);
		fRoot.addChild(s1);
		fRoot.addChild(e2);
		UndoEdit undo= fRoot.apply(fDocument);
		Assert.assertEquals("Buffer content", "01589", fDocument.get());
		assertEquals(s1, 2, 0);
		assertTrue(t1.isDeleted());
		assertEquals(e2, 3, 0);
		doUndoRedo(undo, "01589");
	}

	@Test
	public void testMoveUpWithSourceDelete() throws Exception {
		MoveSourceEdit s1= new MoveSourceEdit(5, 2);
		MoveTargetEdit t1= new MoveTargetEdit(2, s1);

		TextEdit d1= new DeleteEdit(5, 2);
		d1.addChild(s1);

		RangeMarker marker= new RangeMarker(5, 2);
		s1.addChild(marker);

		fRoot.addChild(d1);
		fRoot.addChild(t1);

		UndoEdit undo= fRoot.apply(fDocument);
		Assert.assertEquals("Buffer content", "0156234789", fDocument.get());
		assertEquals(t1, 2, 2);
		assertEquals(marker, 2, 2);
		assertTrue(s1.isDeleted());
		assertEquals(d1, 7, 0);
		doUndoRedo(undo, "0156234789");
	}

	@Test
	public void testMoveDown() throws Exception {
		MoveSourceEdit s1= new MoveSourceEdit(2, 2);
		TextEdit i1= new InsertEdit(5, "x");
		MoveTargetEdit t1= new MoveTargetEdit(7, s1);
		TextEdit d1= new DeleteEdit(9, 1);

		RangeMarker m1= new RangeMarker(2, 2);
		s1.addChild(m1);

		fRoot.addChild(s1);
		fRoot.addChild(i1);
		fRoot.addChild(t1);
		fRoot.addChild(d1);

		UndoEdit undo= fRoot.apply(fDocument);
		Assert.assertEquals("Buffer content", "014x562378", fDocument.get());
		assertEquals(s1, 2, 0);
		assertEquals(i1, 3, 1);
		assertEquals(t1, 6, 2);
		assertEquals(m1, 6, 2);
		assertEquals(d1, 10, 0);
		doUndoRedo(undo, "014x562378");
	}

	@Test
	public void testMoveUp() throws Exception {
		MoveSourceEdit s1= new MoveSourceEdit(7, 2);
		MoveTargetEdit t1= new MoveTargetEdit(2, s1);
		TextEdit i1= new InsertEdit(5, "x");
		TextEdit d1= new DeleteEdit(9, 1);

		RangeMarker m1= new RangeMarker(7, 2);
		s1.addChild(m1);

		fRoot.addChild(s1);
		fRoot.addChild(i1);
		fRoot.addChild(t1);
		fRoot.addChild(d1);

		UndoEdit undo= fRoot.apply(fDocument);
		Assert.assertEquals("Buffer content", "0178234x56", fDocument.get());
		assertEquals(s1, 10, 0);
		assertEquals(i1, 7, 1);
		assertEquals(t1, 2, 2);
		assertEquals(m1, 2, 2);
		assertEquals(d1, 10, 0);
		doUndoRedo(undo, "0178234x56");
	}

	@Test
	public void testMoveDownWithSourceDelete() throws Exception {
		MoveSourceEdit s1= new MoveSourceEdit(2, 2);
		MoveTargetEdit t1= new MoveTargetEdit(7, s1);

		TextEdit d1= new DeleteEdit(2, 2);
		d1.addChild(s1);

		RangeMarker m1= new RangeMarker(2, 2);
		s1.addChild(m1);

		fRoot.addChild(t1);
		fRoot.addChild(d1);

		UndoEdit undo= fRoot.apply(fDocument);
		Assert.assertEquals("Buffer content", "0145623789", fDocument.get());
		assertEquals(d1, 2, 0);
		assertTrue(s1.isDeleted());
		assertEquals(t1, 5, 2);
		assertEquals(m1, 5, 2);
		doUndoRedo(undo, "0145623789");
	}

	@Test
	public void testMoveUpWithInnerMark() throws Exception {
		MoveSourceEdit s1= new MoveSourceEdit(7, 2);
		MoveTargetEdit t1= new MoveTargetEdit(2, s1);
		TextEdit m= new ReplaceEdit(4, 1, "yy");
		fRoot.addChild(t1);
		fRoot.addChild(m);
		fRoot.addChild(s1);
		UndoEdit undo= fRoot.apply(fDocument);
		Assert.assertEquals("Buffer content", "017823yy569", fDocument.get());
		assertEquals(s1, 10, 0);
		assertEquals(t1, 2, 2);
		assertEquals(m, 6, 2);
		doUndoRedo(undo, "017823yy569");
	}

	@Test
	public void testMoveDownWithInnerMark() throws Exception {
		MoveSourceEdit s1= new MoveSourceEdit(2, 2);
		MoveTargetEdit t1= new MoveTargetEdit(7, s1);
		TextEdit m= new ReplaceEdit(4, 1, "yy");
		fRoot.addChild(t1);
		fRoot.addChild(m);
		fRoot.addChild(s1);
		UndoEdit undo= fRoot.apply(fDocument);
		Assert.assertEquals("Buffer content", "01yy5623789", fDocument.get());
		assertEquals(s1, 2, 0);
		assertEquals(t1, 6, 2);
		assertEquals(m, 2, 2);
		doUndoRedo(undo, "01yy5623789");
	}

	@Test
	public void testMoveUpWithParentMark() throws Exception {
		RangeMarker m= new RangeMarker(2, 6);
		MoveSourceEdit s1= new MoveSourceEdit(4, 2);
		MoveTargetEdit t1= new MoveTargetEdit(3, s1);
		m.addChild(s1);
		m.addChild(t1);
		fRoot.addChild(m);
		UndoEdit undo= fRoot.apply(fDocument);
		Assert.assertEquals("Buffer content", "0124536789", fDocument.get());
		assertEquals(m, 2, 6);
		assertEquals(t1, 3, 2);
		assertEquals(s1, 6, 0);
		doUndoRedo(undo, "0124536789");
	}

	@Test
	public void testMoveDownWithParentMark() throws Exception {
		RangeMarker m= new RangeMarker(2, 6);
		MoveSourceEdit s1= new MoveSourceEdit(2, 2);
		MoveTargetEdit t1= new MoveTargetEdit(5, s1);
		m.addChild(s1);
		m.addChild(t1);
		fRoot.addChild(m);
		UndoEdit undo= fRoot.apply(fDocument);
		Assert.assertEquals("Buffer content", "0142356789", fDocument.get());
		assertEquals(m, 2, 6);
		assertEquals(t1, 3, 2);
		assertEquals(s1, 2, 0);
		doUndoRedo(undo, "0142356789");
	}

	@Test
	public void testNestedMoveSource() throws Exception {
		MoveSourceEdit s1= new MoveSourceEdit(1, 5);
		MoveSourceEdit s2= new MoveSourceEdit(2, 3);
		MoveSourceEdit s3= new MoveSourceEdit(3, 1);
		s1.addChild(s2);
		s2.addChild(s3);
		MoveTargetEdit t1= new MoveTargetEdit(9, s1);
		MoveTargetEdit t2= new MoveTargetEdit(8, s2);
		MoveTargetEdit t3= new MoveTargetEdit(7, s3);
		fRoot.addChild(s1);
		fRoot.addChild(t1);
		fRoot.addChild(t2);
		fRoot.addChild(t3);
		UndoEdit undo= fRoot.apply(fDocument);
		assertEquals(s1, 1, 0);
		assertEquals(s2, 8, 0);
		assertEquals(s3, 5, 0);
		assertEquals(t1, 7, 2);
		assertEquals(t2, 4, 2);
		assertEquals(t3, 2, 1);
		String result= "0637248159";
		Assert.assertEquals("Buffer content", result, fDocument.get());
		doUndoRedo(undo, result);
	}

	@Test
	public void testNestedMoveSourceWithInsert() throws Exception {
		MoveSourceEdit s1= new MoveSourceEdit(1, 5);
		MoveSourceEdit s2= new MoveSourceEdit(2, 3);
		MoveSourceEdit s3= new MoveSourceEdit(3, 1);
		InsertEdit i1= new InsertEdit(4, "x");
		s1.addChild(s2);
		s2.addChild(s3);
		s3.addChild(i1);
		MoveTargetEdit t1= new MoveTargetEdit(9, s1);
		MoveTargetEdit t2= new MoveTargetEdit(8, s2);
		MoveTargetEdit t3= new MoveTargetEdit(7, s3);
		fRoot.addChild(s1);
		fRoot.addChild(t1);
		fRoot.addChild(t2);
		fRoot.addChild(t3);
		UndoEdit undo= fRoot.apply(fDocument);
		assertEquals(s1, 1, 0);
		assertEquals(s2, 9, 0);
		assertEquals(s3, 6, 0);
		assertEquals(i1, 3, 1);
		assertEquals(t1, 8, 2);
		assertEquals(t2, 5, 2);
		assertEquals(t3, 2, 2);
		String result= "063x7248159";
		Assert.assertEquals("Buffer content", result, fDocument.get());
		doUndoRedo(undo, result);
	}

	@Test
	public void testNestedMoveTarget() throws Exception {
		MoveSourceEdit s1= new MoveSourceEdit(1, 2);
		MoveSourceEdit s2= new MoveSourceEdit(5, 3);
		MoveTargetEdit t1= new MoveTargetEdit(6, s1);
		MoveTargetEdit t2= new MoveTargetEdit(9, s2);
		s2.addChild(t1);
		fRoot.addChild(s1);
		fRoot.addChild(s2);
		fRoot.addChild(t2);
		UndoEdit undo= fRoot.apply(fDocument);
		assertEquals(s1, 1, 0);
		assertEquals(s2, 3, 0);
		assertEquals(t1, 5, 2);
		assertEquals(t2, 4, 5);
		String result= "0348512679";
		Assert.assertEquals("Buffer content", result, fDocument.get());
		doUndoRedo(undo, result);
	}

	@Test
	public void testCopyDown() throws Exception {
		CopySourceEdit s1= new CopySourceEdit(2, 3);
		CopyTargetEdit t1= new CopyTargetEdit(8, s1);

		fRoot.addChild(s1);
		fRoot.addChild(t1);
		UndoEdit undo= fRoot.apply(fDocument);
		assertEquals(s1, 2, 3);
		assertEquals(t1, 8, 3);
		String result= "0123456723489";
		Assert.assertEquals("Buffer content", result, fDocument.get());
		doUndoRedo(undo, result);
	}

	@Test
	public void testCopyUp() throws Exception {
		CopySourceEdit s1= new CopySourceEdit(7, 2);
		CopyTargetEdit t1= new CopyTargetEdit(3, s1);

		fRoot.addChild(s1);
		fRoot.addChild(t1);
		UndoEdit undo= fRoot.apply(fDocument);
		assertEquals(s1, 9, 2);
		assertEquals(t1, 3, 2);
		String result= "012783456789";
		Assert.assertEquals("Buffer content", result, fDocument.get());
		doUndoRedo(undo, result);
	}

	@Test
	public void testDoubleCopy() throws Exception {
		CopySourceEdit s1= new CopySourceEdit(5, 2);
		CopyTargetEdit t1= new CopyTargetEdit(8, s1);
		CopySourceEdit s2= new CopySourceEdit(5, 2);
		CopyTargetEdit t2= new CopyTargetEdit(2, s2);
		s1.addChild(s2);

		fRoot.addChild(s1);
		fRoot.addChild(t1);
		fRoot.addChild(t2);
		UndoEdit undo= fRoot.apply(fDocument);
		assertEquals(s1, 7, 2);
		assertEquals(t1, 10, 2);
		assertEquals(s2, 7, 2);
		assertEquals(t2, 2, 2);
		String result= "01562345675689";
		Assert.assertEquals("Buffer content", result, fDocument.get());
		doUndoRedo(undo, result);
	}

	@Test
	public void testNestedCopySource() throws Exception {
		CopySourceEdit s1= new CopySourceEdit(1, 5);
		CopySourceEdit s2= new CopySourceEdit(2, 3);
		CopySourceEdit s3= new CopySourceEdit(3, 1);
		s1.addChild(s2);
		s2.addChild(s3);
		CopyTargetEdit t1= new CopyTargetEdit(9, s1);
		CopyTargetEdit t2= new CopyTargetEdit(8, s2);
		CopyTargetEdit t3= new CopyTargetEdit(7, s3);
		fRoot.addChild(s1);
		fRoot.addChild(t1);
		fRoot.addChild(t2);
		fRoot.addChild(t3);
		UndoEdit undo= fRoot.apply(fDocument);
		assertEquals(s1, 1, 5);
		assertEquals(s2, 2, 3);
		assertEquals(s3, 3, 1);
		assertEquals(t1, 13, 5);
		assertEquals(t2, 9, 3);
		assertEquals(t3, 7, 1);
		String result= "0123456372348123459";
		Assert.assertEquals("Buffer content", result, fDocument.get());
		doUndoRedo(undo, result);
	}

	@Test
	public void testNestedCopySourceWithInsert() throws Exception {
		CopySourceEdit s1= new CopySourceEdit(1, 5);
		CopySourceEdit s2= new CopySourceEdit(2, 3);
		CopySourceEdit s3= new CopySourceEdit(3, 1);
		InsertEdit i1= new InsertEdit(4, "x");
		s1.addChild(s2);
		s2.addChild(s3);
		s3.addChild(i1);
		CopyTargetEdit t1= new CopyTargetEdit(9, s1);
		CopyTargetEdit t2= new CopyTargetEdit(8, s2);
		CopyTargetEdit t3= new CopyTargetEdit(7, s3);
		fRoot.addChild(s1);
		fRoot.addChild(t1);
		fRoot.addChild(t2);
		fRoot.addChild(t3);
		UndoEdit undo= fRoot.apply(fDocument);
		assertEquals(s1, 1, 6);
		assertEquals(s2, 2, 4);
		assertEquals(s3, 3, 2);
		assertEquals(i1, 4, 1);
		assertEquals(t1, 16, 6);
		assertEquals(t2, 11, 4);
		assertEquals(t3, 8, 2);
		String result= "0123x4563x723x48123x459";
		Assert.assertEquals("Buffer content", result, fDocument.get());
		doUndoRedo(undo, result);
	}

	@Test
	public void testNestedCopyTarget() throws Exception {
		CopySourceEdit s1= new CopySourceEdit(1, 2);
		CopySourceEdit s2= new CopySourceEdit(5, 3);
		CopyTargetEdit t1= new CopyTargetEdit(6, s1);
		CopyTargetEdit t2= new CopyTargetEdit(9, s2);
		s2.addChild(t1);
		fRoot.addChild(s1);
		fRoot.addChild(s2);
		fRoot.addChild(t2);
		UndoEdit undo= fRoot.apply(fDocument);
		assertEquals(s1, 1, 2);
		assertEquals(s2, 5, 5);
		assertEquals(t1, 6, 2);
		assertEquals(t2, 11, 5);
		String result= "01234512678512679";
		Assert.assertEquals("Buffer content", result, fDocument.get());
		doUndoRedo(undo, result);
	}

	@Test
	public void testSwap1() throws Exception {
		IDocument document= new Document("foo(1, 2), 3");

		MultiTextEdit root= new MultiTextEdit();
		CopySourceEdit innerRoot= new CopySourceEdit(0, 9);

		TextEdit e1= new ReplaceEdit(0, 9, "");
		e1.addChild(innerRoot);
		CopyTargetEdit t1= new CopyTargetEdit(11, innerRoot);

		TextEdit e2= new ReplaceEdit(11, 1, "");
		CopySourceEdit s2= new CopySourceEdit(11, 1);
		e2.addChild(s2);
		CopyTargetEdit t2= new CopyTargetEdit(0, s2);

		root.addChild(e1);
		root.addChild(t2);
		root.addChild(e2);
		root.addChild(t1);

		root.apply(document);

		String result= "3, foo(1, 2)";
		Assert.assertEquals("Buffer content", result, document.get());
	}

	@Test
	public void testSwap2() throws Exception {
		IDocument document= new Document("foo(1, 2), 3");

		MultiTextEdit root= new MultiTextEdit();
		TextEdit e1= new ReplaceEdit(4, 1, "");
		CopySourceEdit s1= new CopySourceEdit(4, 1);
		e1.addChild(s1);
		CopyTargetEdit t1= new CopyTargetEdit(7, s1);

		TextEdit e2= new ReplaceEdit(7, 1, "");
		CopySourceEdit s2= new CopySourceEdit(7, 1);
		e2.addChild(s2);
		CopyTargetEdit t2= new CopyTargetEdit(4, s2);

		root.addChild(e1);
		root.addChild(t2);
		root.addChild(e2);
		root.addChild(t1);

		root.apply(document);

		String result= "foo(2, 1), 3";
		Assert.assertEquals("Buffer content", result, document.get());
	}

	@Test
	public void testSwap2InSwap1() throws Exception {
		IDocument document= new Document("foo(1, 2), 3");

		CopySourceEdit innerRoot= new CopySourceEdit(0, 9);
		{
			TextEdit e1= new ReplaceEdit(4, 1, "");
			CopySourceEdit s1= new CopySourceEdit(4, 1);
			e1.addChild(s1);
			CopyTargetEdit t1= new CopyTargetEdit(7, s1);

			TextEdit e2= new ReplaceEdit(7, 1, "");
			CopySourceEdit s2= new CopySourceEdit(7, 1);
			e2.addChild(s2);
			CopyTargetEdit t2= new CopyTargetEdit(4, s2);

			innerRoot.addChild(e1);
			innerRoot.addChild(t2);
			innerRoot.addChild(e2);
			innerRoot.addChild(t1);
		}
		MultiTextEdit root= new MultiTextEdit();
		TextEdit e1= new ReplaceEdit(0, 9, "");
		e1.addChild(innerRoot);
		CopyTargetEdit t1= new CopyTargetEdit(11, innerRoot);

		TextEdit e2= new ReplaceEdit(11, 1, "");
		CopySourceEdit s2= new CopySourceEdit(11, 1);
		e2.addChild(s2);
		CopyTargetEdit t2= new CopyTargetEdit(0, s2);

		root.addChild(e1);
		root.addChild(t2);
		root.addChild(e2);
		root.addChild(t1);

		root.apply(document);

		String result= "3, foo(2, 1)";
		Assert.assertEquals("Buffer content", result, document.get());
	}

	@Test
	public void testMoveTree1() {
		TextEdit root= new MultiTextEdit();
		TextEdit e1= new ReplaceEdit(0, 1, "");
		root.addChild(e1);
		TextEdit e2= new ReplaceEdit(2, 2, "");
		root.addChild(e2);
		root.moveTree(3);
		Assert.assertEquals(3, root.getOffset());
		Assert.assertEquals(4, root.getLength());
		Assert.assertEquals(3, e1.getOffset());
		Assert.assertEquals(1, e1.getLength());
		Assert.assertEquals(5, e2.getOffset());
		Assert.assertEquals(2, e2.getLength());
	}

	@Test
	public void testMoveTree2() {
		TextEdit root= new MultiTextEdit();
		TextEdit e1= new ReplaceEdit(3, 1, "");
		root.addChild(e1);
		TextEdit e2= new ReplaceEdit(5, 2, "");
		root.addChild(e2);
		root.moveTree(-3);
		Assert.assertEquals(0, root.getOffset());
		Assert.assertEquals(4, root.getLength());
		Assert.assertEquals(0, e1.getOffset());
		Assert.assertEquals(1, e1.getLength());
		Assert.assertEquals(2, e2.getOffset());
		Assert.assertEquals(2, e2.getLength());
	}

	@Test
	public void testMoveTree3() {
		boolean exception= false;
		try {
			TextEdit root= new ReplaceEdit(0, 1, "");
			root.moveTree(-1);
		} catch (Exception e) {
			exception= true;
		}
		assertTrue(exception);
	}

	@Test
	public void testMoveTree4() {
		boolean exception= false;
		try {
			TextEdit root= new MultiTextEdit();
			TextEdit e1= new ReplaceEdit(0, 1, "");
			root.addChild(e1);
			e1.moveTree(1);
		} catch (Exception e) {
			exception= true;
		}
		assertTrue(exception);
	}

	@Test
	public void testComparator() throws Exception {
		DeleteEdit d1= new DeleteEdit(1,3);
		Accessor accessor= new Accessor(d1, TextEdit.class);
		@SuppressWarnings("unchecked")
		Comparator<TextEdit> comparator= (Comparator<TextEdit>)accessor.get("INSERTION_COMPARATOR");

		TextEdit edit1= new InsertEdit(1, "test");
		TextEdit edit2= new InsertEdit(1, "test");
		TextEdit edit3= new InsertEdit(57, "test3");

		assertTrue(edit1.equals(edit1));
		Assert.assertEquals(0, comparator.compare(edit1, edit1));
		Assert.assertEquals(0, comparator.compare(edit1, edit2));
		Assert.assertEquals(0, comparator.compare(edit2, edit1));
		assertTrue(comparator.compare(edit1, edit3) == -comparator.compare(edit3, edit1));

	}

	@Test
	public void testSourceTransformationIncludes() throws Exception {
		MoveSourceEdit ms= new MoveSourceEdit(2, 4);
		MoveTargetEdit mt= new MoveTargetEdit(9, ms);
		fRoot.addChild(ms);
		fRoot.addChild(mt);
		RangeMarker r1= new RangeMarker(3,2);
		ms.addChild(r1);
		ms.setSourceModifier(new ISourceModifier() {
			@Override
			public ISourceModifier copy() {
				return this;
			}
			@Override
			public ReplaceEdit[] getModifications(String source) {
				return new ReplaceEdit[] { new ReplaceEdit(1,1,"aa") };
			}
		});
		fRoot.apply(fDocument);
		String result= "016782aa459";
		Assert.assertEquals("Buffer content", result, fDocument.get());
		assertEquals(r1, 6, 3);
	}

	// Regression test for 108672 [quick fix] Exception when applying Convert to enhanced for loop
	@Test
	public void testSourceTransformationMultipleCovers() throws Exception {
		MoveSourceEdit ms= new MoveSourceEdit(2, 4);
		MoveTargetEdit mt= new MoveTargetEdit(9, ms);
		fRoot.addChild(ms);
		fRoot.addChild(mt);
		RangeMarker r1= new RangeMarker(3,0);
		ms.addChild(r1);
		RangeMarker r2= new RangeMarker(3,0);
		ms.addChild(r2);
		RangeMarker r3= new RangeMarker(4,2);
		ms.addChild(r3);
		ms.setSourceModifier(new ISourceModifier() {
			@Override
			public ISourceModifier copy() {
				return this;
			}
			@Override
			public ReplaceEdit[] getModifications(String source) {
				return new ReplaceEdit[] { new ReplaceEdit(0,2,"aa") };
			}
		});
		fRoot.apply(fDocument);
		String result= "01678aa459";
		Assert.assertEquals("Buffer content", result, fDocument.get());
		assertTrue(r1.isDeleted());
		assertTrue(r2.isDeleted());
		assertEquals(r3, 7, 2);
	}

	@Test
	public void testSourceTransformationSplit1() throws Exception {
		MoveSourceEdit ms= new MoveSourceEdit(2, 4);
		MoveTargetEdit mt= new MoveTargetEdit(9, ms);
		fRoot.addChild(ms);
		fRoot.addChild(mt);
		RangeMarker r1= new RangeMarker(3,2);
		ms.addChild(r1);
		ms.setSourceModifier(new ISourceModifier() {
			@Override
			public ISourceModifier copy() {
				return this;
			}
			@Override
			public ReplaceEdit[] getModifications(String source) {
				return new ReplaceEdit[] { new ReplaceEdit(0,2,"aa") };
			}
		});
		fRoot.apply(fDocument);
		String result= "01678aa459";
		Assert.assertEquals("Buffer content", result, fDocument.get());
		assertEquals(r1, 7, 1);
	}

	@Test
	public void testSourceTransformationSplit2() throws Exception {
		MoveSourceEdit ms= new MoveSourceEdit(2, 4);
		MoveTargetEdit mt= new MoveTargetEdit(9, ms);
		fRoot.addChild(ms);
		fRoot.addChild(mt);
		RangeMarker r1= new RangeMarker(3,2);
		ms.addChild(r1);
		ms.setSourceModifier(new ISourceModifier() {
			@Override
			public ISourceModifier copy() {
				return this;
			}
			@Override
			public ReplaceEdit[] getModifications(String source) {
				return new ReplaceEdit[] { new ReplaceEdit(2,2,"aa") };
			}
		});
		fRoot.apply(fDocument);
		String result= "0167823aa9";
		Assert.assertEquals("Buffer content", result, fDocument.get());
		assertEquals(r1, 6, 3);
	}

	@Test
	public void testIntersect() throws Exception {
		IRegion result= MoveSourceEdit.intersect(new RangeMarker(0,1), new RangeMarker(2,1));
		assertNull(result);
		result= MoveSourceEdit.intersect(new RangeMarker(2,1), new RangeMarker(0,1));
		assertNull(result);
		result= MoveSourceEdit.intersect(new RangeMarker(0,1), new RangeMarker(1,1));
		assertNull(result);
		result= MoveSourceEdit.intersect(new RangeMarker(1,1), new RangeMarker(0,1));
		assertNull(result);
		result= MoveSourceEdit.intersect(new RangeMarker(0,2), new RangeMarker(1,2));
		assertNotNull(result);
		assertEquals(result, 1, 1);
		result= MoveSourceEdit.intersect(new RangeMarker(1,2), new RangeMarker(0,2));
		assertNotNull(result);
		assertEquals(result, 1, 1);
		result= MoveSourceEdit.intersect(new RangeMarker(1,2), new RangeMarker(2,2));
		assertNotNull(result);
		assertEquals(result, 2, 1);
		result= MoveSourceEdit.intersect(new RangeMarker(2,2), new RangeMarker(1,2));
		assertNotNull(result);
		assertEquals(result, 2, 1);
	}

	private void doUndoRedo(UndoEdit undo, String redoResult) throws Exception {
		UndoEdit redo= undo.apply(fDocument);
		assertBufferContent();
		undo= redo.apply(fDocument);
		Assert.assertEquals("Buffer content redo", redoResult, fDocument.get());
		undo.apply(fDocument);
		assertBufferContent();
	}

	private void assertEquals(TextEdit edit, int offset, int length) {
		Assert.assertEquals("Offset", offset, edit.getOffset());
		Assert.assertEquals("Length", length, edit.getLength());
	}

	private void assertEquals(IRegion region, int offset, int length) {
		Assert.assertEquals("Offset", offset, region.getOffset());
		Assert.assertEquals("Length", length, region.getLength());
	}

	private void assertBufferContent() {
		Assert.assertEquals("Buffer content restored", "0123456789", fDocument.get());
	}
}

