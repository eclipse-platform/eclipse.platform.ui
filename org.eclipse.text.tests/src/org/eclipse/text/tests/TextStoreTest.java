/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ConfigurableLineTracker;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextStore;

/**
 * Correctness tests for {@link ITextStore} implementations.
 *
 * @since 3.3
 */
public abstract class TextStoreTest {

	private ITextStore fTextStore;
	private ILineTracker fTracker;

	@Before
	public void setUp() {

		fTextStore= createTextStore();
		fTracker= createTracker();
		set("x\nx\nx\nx\nx\n");
	}

	protected ILineTracker createTracker() {
		return new ConfigurableLineTracker(new String[]{"\n"});
	}

	protected final void replace(int offset, int length, String text) throws BadLocationException {
		fTextStore.replace(offset, length, text);
		fTracker.replace(offset, length, text);
	}

	protected final void set(String text) {
		fTextStore.set(text);
		fTracker.set(text);
	}

	abstract protected ITextStore createTextStore();

	@After
	public void tearDown() {
		fTextStore= null;
		fTracker= null;
	}
	
	@Test
	public void testGet1() throws Exception {
		set("xxxxx");

		String[] expected= {"xyxxxx", "xyxyxxx", "xyxyxyxx", "xyxyxyxyx", "xyxyxyxyxy"};

		for (int i= 1; i < 5; i++) {
			replace(2 * i - 1, 0, "y");
			String txt= fTextStore.get(0, fTextStore.getLength());
			assertEquals(expected[i - 1], txt);
		}

	}
	
	@Test
	public void testGet2() throws Exception {
		set("xxxxx");

		String[] expected= {"yxxxxx", "yxyxxxx", "yxyxyxxx", "yxyxyxyxx", "yxyxyxyxyx"};

		for (int i= 1; i < 5; i++) {
			replace(2 * (i - 1), 0, "y");
			String txt= fTextStore.get(0, fTextStore.getLength());
			assertEquals(expected[i - 1], txt);
		}

	}
	
	@Test
	public void testEditScript1() throws Exception {
		replace(0, fTextStore.getLength(), "x");
		assertTextStoreContents("x");

		replace(1, 0, "y");
		assertTextStoreContents("xy");

		replace(2, 0, "z");
		assertTextStoreContents("xyz");

		replace(3, 0, "\n");
		assertTextStoreContents("xyz\n");

		replace(4, 0, "x");
		assertTextStoreContents("xyz\nx");
	}

	private void assertTextStoreContents(String expected) {
		assertEquals(expected, fTextStore.get(0, fTextStore.getLength()));
		for (int i= 0; i < fTextStore.getLength(); i++)
			assertEquals(expected.charAt(i), fTextStore.get(i));

		try {
			fTextStore.get(fTextStore.getLength());
			fail();
		} catch (IndexOutOfBoundsException e) {
		}
	}
	
	@Test
	public void testEmptyLines() throws Exception {

		replace(0, 10, null);
		assertTextStoreContents("");

		replace(0, 0, "\n\n\n\n\n");
		assertTextStoreContents("\n\n\n\n\n");
	}
	
	@Test
	public void testInsert1() throws Exception {

		replace(3, 0, "yyyy");
		assertTextStoreContents("x\nxyyyy\nx\nx\nx\n");

		replace(9, 0, "y\n");
		assertTextStoreContents("x\nxyyyy\nxy\n\nx\nx\n");

		replace(11, 0, "y\n");
		assertTextStoreContents("x\nxyyyy\nxy\ny\n\nx\nx\n");

		replace(13, 0, "y");
		assertTextStoreContents("x\nxyyyy\nxy\ny\ny\nx\nx\n");

		replace(11, 5, "y\nxyz");
		assertTextStoreContents("x\nxyyyy\nxy\ny\nxyz\nx\n");
	}
	
	@Test
	public void testInsert2() throws Exception {
		replace(3, 0, "yyyy");
		assertTextStoreContents("x\nxyyyy\nx\nx\nx\n");

		replace(9, 0, "y\ny\ny");
		assertTextStoreContents("x\nxyyyy\nxy\ny\ny\nx\nx\n");
	}
	
	@Test
	public void testLinesNumbers() throws Exception {
		replace(0, 10, "\na\nbb\nccc\ndddd\neeeee\n");
		assertTextStoreContents("\na\nbb\nccc\ndddd\neeeee\n");

		int offset= 0;
		for (int i= 0; i < 5; i++) {
			for (int j= 0; j < i; j++) {
				int no= fTracker.getLineNumberOfOffset(offset + j);
				assertTrue("invalid line number " + no + " reported instead of " + i, no == i);
			}
			offset+= (i + 1);
		}
	}
	
	@Test
	public void testOffsets() throws Exception {
		for (int i= 0; i < 5; i++) {
			IRegion line= fTracker.getLineInformation(i);
			int pos= line.getOffset() + line.getLength();
			int offset= (2 * i) + 1;
			assertTrue("invalid line end offset " + pos + " for line " + i + " should be " + offset, offset == pos);
		}

		for (int i= 0; i < 5; i++) {
			int pos= fTracker.getLineOffset(i);
			int offset= 2 * i;
			assertTrue("invalid line start offset " + pos + " for line " + i + " should be " + offset, pos == offset);
		}

		for (int i= 0; i < 10; i++) {
			int line= fTracker.getLineNumberOfOffset(i);
			double l= Math.floor(i / 2);
			assertTrue("invalid line number " + line + " for position " + i + " should be " + l, l == line);
		}
	}
	
	@Test
	public void testRemove() throws Exception {
		replace(3, 1, null);
		assertTextStoreContents("x\nxx\nx\nx\n");

		replace(6, 1, null);
		assertTextStoreContents("x\nxx\nxx\n");

		replace(3, 5, null);
		assertTextStoreContents("x\nx");

		replace(0, 3, null);
		assertTextStoreContents("");
	}
	
	@Test
	public void testReplace() throws Exception {
		replace(0, fTextStore.getLength(), "\tx\n\tx\n\tx\n\tx\n\tx\n");
		assertTextStoreContents("\tx\n\tx\n\tx\n\tx\n\tx\n");
	}
	
	@Test
	public void testReplace2() throws Exception {
		replace(0, fTextStore.getLength(), "x");
		assertTextStoreContents("x");

		replace(0, fTextStore.getLength(), "x\nx\nx\n");
		assertTextStoreContents("x\nx\nx\n");
	}
	
	@Test
	public void testReplace3() throws Exception {
		replace(1, 1, "\n");
		assertTextStoreContents("x\nx\nx\nx\nx\n");
	}
	
	@Test
	public void testReplace4() throws Exception {
		int lines= fTracker.getNumberOfLines();
		IRegion previous= fTracker.getLineInformation(0);
		for (int i= 1; i < lines; i++) {
			int lastLineEnd= previous.getOffset() + previous.getLength();
			int lineStart= fTracker.getLineInformation(i).getOffset();
			replace(lastLineEnd, lineStart - lastLineEnd, "\n");
			assertTextStoreContents("x\nx\nx\nx\nx\n");
			previous= fTracker.getLineInformation(i);
		}
	}
	
	@Test
	public void testShiftLeft() throws Exception {
		replace(0, fTextStore.getLength(), "\tx\n\tx\n\tx\n\tx\n\tx\n");
		assertTextStoreContents("\tx\n\tx\n\tx\n\tx\n\tx\n");

		for (int i= 0; i < 5; i++) {
			int pos= fTracker.getLineOffset(i);
			replace(pos, 1, null);
		}

		assertTextStoreContents("x\nx\nx\nx\nx\n");
	}
	
	@Test
	public void testShiftRight() throws Exception {
		for (int i= 0; i < 5; i++) {
			int pos= fTracker.getLineOffset(i);
			replace(pos, 0, "\t");
		}

		assertTextStoreContents("\tx\n\tx\n\tx\n\tx\n\tx\n");
	}
	
	@Test
	public void testDeleteEmptyLine() throws Exception {
		set("x\nx\n\nx\n\n");
		assertTextStoreContents("x\nx\n\nx\n\n");

		String[] expected= {
				"",
				"x\n",
				"x\nx\n",
				"x\nx\n\n",
				"x\nx\n\nx\n",
				"x\nx\n\nx\n\n",
		};

		for (int line= fTracker.getNumberOfLines() - 1; line >= 0; line--) {
			int offset= fTracker.getLineOffset(line);
			int length= fTracker.getLineLength(line);
			replace(offset, length, null);
			assertTextStoreContents(expected[line]);
		}

	}
	
	@Test
	public void testDeleteLines() throws Exception {
		String content= "";
		for (int i= 0; i < 50; i++) {
			content += "x\nx\n\nx\n\n";
			set(content);

			String expected= content;
			int lines= fTracker.getNumberOfLines();
			for (int line= 0; line < lines; line++) {
				int offset= fTracker.getLineOffset(0);
				int length= fTracker.getLineLength(0);
				replace(offset, length, null);
				expected= expected.substring(length);
				assertTextStoreContents(expected);
			}
		}
		content= "";
		for (int i= 0; i < 50; i++) {
			content += "x\nx\n\nx\n\n";
			set(content);

			String expected= content;
			int lines= fTracker.getNumberOfLines();
			for (int line= lines - 1; line >= 0; line--) {
				int offset= fTracker.getLineOffset(line);
				int length= fTracker.getLineLength(line);
				replace(offset, length, null);
				expected= expected.substring(0, expected.length() - length);
				assertTextStoreContents(expected);
			}
		}
	}
	
	@Test
	public void testDeleteLines2() throws Exception {
		String content= "";
		for (int i= 0; i < 50; i++) {
			content += "xxxxxxxxxxxxxx";
			set(content);

			String expected= content;
			int lines= fTracker.getNumberOfLines();
			for (int line= 0; line < lines; line++) {
				int offset= fTracker.getLineOffset(0);
				int length= fTracker.getLineLength(0);
				replace(offset, length, null);
				expected= expected.substring(length);
				assertTextStoreContents(expected);
			}
		}
		content= "";
		for (int i= 0; i < 50; i++) {
			content += "xxxxxxxxxxxxxx";
			set(content);

			String expected= content;
			int lines= fTracker.getNumberOfLines();
			for (int line= lines - 1; line >= 0; line--) {
				int offset= fTracker.getLineOffset(line);
				int length= fTracker.getLineLength(line);
				replace(offset, length, null);
				expected= expected.substring(0, expected.length() - length);
				assertTextStoreContents(expected);
			}
		}
	}
	
	@Test
	public void testSet() throws Exception {
		String content= "";
		for (int i= 0; i < 35; i++) {
			int[] lenghts= new int[i + 1];
			for (int j= 0; j < i + 1; j++)
				lenghts[j]= j;
			for (int j= 0; j < i; j++)
				content+= "x";

			set(content);
			assertTextStoreContents(content);

			content+= "\n";
		}
	}
	
	@Test
	public void testFunnyLastLineCompatibility() throws Exception {
		/* empty last line */
		set("x\n");
		assertTextStoreContents("x\n");
		int[] offsets= {0, 2};
		int[] lengths= {1, 0};

		assertEquals("invalid number of lines, ", lengths.length, fTracker.getNumberOfLines());
		assertEquals("invalid number of lines, ", lengths.length, fTracker.getNumberOfLines(0, fTextStore.getLength()));
		for (int i= 0; i < lengths.length; i++) {
			IRegion line= fTracker.getLineInformation(i);
			assertEquals("line: " + i, lengths[i], line.getLength());
			assertEquals("line: " + i, offsets[i], line.getOffset());
		}
		try {
			fTracker.getLineInformation(lengths.length);
			fail();
		} catch (Exception e) {
		}

		try {
			fTracker.getLineInformationOfOffset(offsets[offsets.length] + 1);
			fail();
		} catch (Exception e) {
		}

		/* phantom last line when the last line is not empty */
		set("x\nx");
		assertTextStoreContents("x\nx");
		offsets= new int[]{0, 2, 3};
		lengths= new int[]{1, 1, 0};
		assertEquals("invalid number of lines, ", lengths.length - 1 /* !!!! */, fTracker.getNumberOfLines());
		assertEquals("invalid number of lines, ", lengths.length - 1 /* !!!! */, fTracker.getNumberOfLines(0, fTextStore.getLength()));
		for (int i= 0; i < lengths.length; i++) {
			IRegion line= fTracker.getLineInformation(i);
			int len= lengths[i];
			int offset= offsets[i];
			assertEquals("length of line: " + i, len, line.getLength());
			assertEquals("offset of line: " + i, offset, line.getOffset());

			line= fTracker.getLineInformationOfOffset(offset);
			if (i == lengths.length - 1) { // phantom line cannot be queried by offset
				len= lengths[i - 1];
				offset= offsets[i - 1];
			}
			assertEquals("length of line: " + i, len, line.getLength());
			assertEquals("offset of line: " + i, offset, line.getOffset());
		}

		try {
			fTracker.getLineInformation(lengths.length);
			fail();
		} catch (Exception e) {
		}

		try {
			fTracker.getLineInformationOfOffset(offsets[offsets.length] + 1);
			fail();
		} catch (Exception e) {
		}

	}

}
