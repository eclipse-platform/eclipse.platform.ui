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

package org.eclipse.search.core.tests;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search2.internal.ui.text.PositionTracker;
import org.eclipse.ui.ide.IDE;

/**
 */
public class LineConversionTest extends TestCase {
	private IFile fFile;
	
	private static final String LINE_TWO= "This is the second line\n";
	private static final String LINE_ONE= "This is the first line\n";
	private static final String LINE_THREE= "This is the third line";

	protected void setUp() throws Exception {
		IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject("Test");
		project.create(null);
		project.open(null);
		fFile= project.getFile("/test.txt");
		fFile.create(new ByteArrayInputStream(getFileContents().getBytes()), true, null);
		super.setUp();
	}
	
	protected void tearDown() throws Exception {
		SearchPlugin.getActivePage().closeAllEditors(false);
		fFile.getProject().delete(true, true, null);
		super.tearDown();
	}

	private String getFileContents() {
		return LINE_ONE+LINE_TWO+LINE_THREE;
	}
	
	public void testConvertToCharacter() throws Exception {
		IDE.openEditor(SearchPlugin.getActivePage(), fFile);
		ITextFileBuffer fb= FileBuffers.getTextFileBufferManager().getTextFileBuffer(fFile.getLocation());
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
	
	public void testBogusLines() throws Exception {
		IDE.openEditor(SearchPlugin.getActivePage(), fFile);
		ITextFileBuffer fb= FileBuffers.getTextFileBufferManager().getTextFileBuffer(fFile.getLocation());
		IDocument doc= fb.getDocument();

		Position p1= new Position(2, 67);
		try {
			Position p2= PositionTracker.convertToCharacterPosition(p1, doc);
			assertTrue("shouldn't happen", false);
		} catch (BadLocationException e) {
		}
	}

	public void atestLineOffsets() throws Exception {
		IDE.openEditor(SearchPlugin.getActivePage(), fFile);
		ITextFileBuffer fb= FileBuffers.getTextFileBufferManager().getTextFileBuffer(fFile.getLocation());
		IDocument doc= fb.getDocument();

		int offset= doc.getLineOffset(3);
		int line= doc.getLineOfOffset(offset);
		assertEquals(3, line);
	}
}