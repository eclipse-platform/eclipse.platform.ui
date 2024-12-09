/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

package org.eclipse.search.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.ByteArrayInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.tests.SearchTestUtil;

import org.eclipse.search2.internal.ui.text.PositionTracker;

public class LineConversionTest {
	private IFile fFile;

	private static final String LINE_TWO= "This is the second line\n";

	private static final String LINE_ONE= "This is the first line\n";

	private static final String LINE_THREE= "This is the third line";

	@Before
	public void setUp() throws Exception {
		IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject("Test");
		project.create(null);
		project.open(null);
		fFile= project.getFile("/test.txt");
		fFile.create(new ByteArrayInputStream(getFileContents().getBytes()), true, null);
	}

	@After
	public void tearDown() throws Exception {
		SearchPlugin.getActivePage().closeAllEditors(false);
		fFile.getProject().delete(true, true, null);
	}

	private String getFileContents() {
		return LINE_ONE + LINE_TWO + LINE_THREE;
	}

	@Test
	public void testConvertToCharacter() throws Exception {
		SearchTestUtil.openTextEditor(SearchPlugin.getActivePage(), fFile);
		ITextFileBuffer fb= FileBuffers.getTextFileBufferManager().getTextFileBuffer(fFile.getFullPath(), LocationKind.IFILE);
		IDocument doc= fb.getDocument();

		Position p1= new Position(2, 1);
		Position p2= PositionTracker.convertToCharacterPosition(p1, doc);
		//assertEquals(LINE_THREE, doc.get(p2.getOffset(), p2.getLength()));
		assertEquals(p1, PositionTracker.convertToLinePosition(p2, doc));

		p1= new Position(0, 1);
		p2= PositionTracker.convertToCharacterPosition(p1, doc);
		assertEquals(LINE_ONE, doc.get(p2.getOffset(), p2.getLength()));
		assertEquals(p1, PositionTracker.convertToLinePosition(p2, doc));

		p1= new Position(1, 1);
		p2= PositionTracker.convertToCharacterPosition(p1, doc);
		assertEquals(LINE_TWO, doc.get(p2.getOffset(), p2.getLength()));
		assertEquals(p1, PositionTracker.convertToLinePosition(p2, doc));

		p1= new Position(0, 0);
		p2= PositionTracker.convertToCharacterPosition(p1, doc);
		assertEquals("", doc.get(p2.getOffset(), p2.getLength()));
		assertEquals(p1, PositionTracker.convertToLinePosition(p2, doc));
	}

	@Test
	public void testBogusLines() throws Exception {
		SearchTestUtil.openTextEditor(SearchPlugin.getActivePage(), fFile);
		ITextFileBuffer fb= FileBuffers.getTextFileBufferManager().getTextFileBuffer(fFile.getFullPath(), LocationKind.IFILE);
		IDocument doc= fb.getDocument();

		Position p1= new Position(2, 67);
		assertThrows(BadLocationException.class, () -> PositionTracker.convertToCharacterPosition(p1, doc));
	}

	public void atestLineOffsets() throws Exception {
		SearchTestUtil.openTextEditor(SearchPlugin.getActivePage(), fFile);
		ITextFileBuffer fb= FileBuffers.getTextFileBufferManager().getTextFileBuffer(fFile.getFullPath(), LocationKind.IFILE);
		IDocument doc= fb.getDocument();

		int offset= doc.getLineOffset(3);
		int line= doc.getLineOfOffset(offset);
		assertEquals(3, line);
	}
}
