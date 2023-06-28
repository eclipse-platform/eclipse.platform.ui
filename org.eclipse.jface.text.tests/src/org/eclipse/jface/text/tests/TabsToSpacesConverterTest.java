/*******************************************************************************
 * Copyright (c) 2019 Mateusz Matela and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mateusz Matela - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TabsToSpacesConverter;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

public class TabsToSpacesConverterTest {

	private void doTest(String input, String output, int keyCode, int tabWidth) {
		Shell shell= new Shell();
		TextViewer textViewer= new TextViewer(shell, SWT.NONE);

		TabsToSpacesConverter tabToSpacesConverter= new TabsToSpacesConverter();
		tabToSpacesConverter.setLineTracker(new DefaultLineTracker());
		tabToSpacesConverter.setNumberOfSpacesPerTab(tabWidth);
		tabToSpacesConverter.setDeleteSpacesAsTab(true);
		textViewer.setTabsToSpacesConverter(tabToSpacesConverter);

		int selectionFrom= input.indexOf('|');
		int selectionTo= input.indexOf('|', selectionFrom + 1) - 1;
		Document document= new Document(input.replace("|", ""));
		textViewer.setDocument(document);
		textViewer.setSelectedRange(selectionFrom, selectionTo - selectionFrom);

		TextViewerTest.postKeyEvent(textViewer.getTextWidget(), keyCode, SWT.NONE, SWT.KeyDown);

		assertEquals(output, document.get());
	}

	@Test
	public void testDelete1() {
		doTest("||        ABC", "    ABC", SWT.DEL, 4);
	}

	@Test
	public void testDelete2() {
		doTest("    ||    ABC", "    ABC", SWT.DEL, 4);
	}

	@Test
	public void testDelete3() {
		doTest("     ||   ABC", "     ABC", SWT.DEL, 4);
	}

	@Test
	public void testDelete4() {
		doTest("       || ABC", "       ABC", SWT.DEL, 4);
	}

	@Test
	public void testDeleteRange1() {
		doTest("   | |    ABC", "       ABC", SWT.DEL, 4);
	}

	@Test
	public void testDeleteRange2() {
		doTest("    | |   ABC", "       ABC", SWT.DEL, 4);
	}

	@Test
	public void testDeleteInside1() {
		doTest("    ABCD||    EFG", "    ABCDEFG", SWT.DEL, 4);
	}

	@Test
	public void testDeleteInside2() {
		doTest("    ABCD  ||    EFG", "    ABCD    EFG", SWT.DEL, 4);
	}

	@Test
	public void testDeleteInside3() {
		doTest("    ABCD||  EFG", "    ABCDEFG", SWT.DEL, 4);
	}

	@Test
	public void testDeleteLargeWidth1() {
		doTest("  ||          ABC", "    ABC", SWT.DEL, 10);
	}

	@Test
	public void testDeleteLargeWidth2() {
		doTest("        ||    ABC", "          ABC", SWT.DEL, 10);
	}

	@Test
	public void testDeleteSmallWidth() {
		doTest("  ||          ABC", "          ABC", SWT.DEL, 2);
	}

	@Test
	public void testBackspace1() {
		doTest("    ||    ABC", "    ABC", SWT.BS, 4);
	}

	@Test
	public void testBackspace2() {
		doTest("        ||ABC", "    ABC", SWT.BS, 4);
	}

	@Test
	public void testBackspace3() {
		doTest("   ||     ABC", "     ABC", SWT.BS, 4);
	}

	@Test
	public void testBackspace4() {
		doTest("      ||  ABC", "      ABC", SWT.BS, 4);
	}

	@Test
	public void testBackspaceRange1() {
		doTest("   | |    ABC", "       ABC", SWT.BS, 4);
	}

	@Test
	public void testBackspaceRange2() {
		doTest("    | |   ABC", "       ABC", SWT.BS, 4);
	}

	@Test
	public void testBackspaceInside1() {
		doTest("    ABCD    ||EFG", "    ABCDEFG", SWT.BS, 4);
	}

	@Test
	public void testBackspaceInside2() {
		doTest("    ABCD      ||EFG", "    ABCD    EFG", SWT.BS, 4);
	}

	@Test
	public void testBackspaceInside3() {
		doTest("    ABCDEF  ||G", "    ABCDEFG", SWT.BS, 4);
	}

	@Test
	public void testBackspaceLargeWidth1() {
		doTest("            ||  ABC", "            ABC", SWT.BS, 10);
	}

	@Test
	public void testBackspaceLargeWidth2() {
		doTest("          ||    ABC", "    ABC", SWT.BS, 10);
	}

	@Test
	public void testBackspaceSmallWidth() {
		doTest("            ||  ABC", "            ABC", SWT.BS, 2);
	}

	@Test
	public void testDeleteAfterCollapsedRegion() throws BadLocationException {
		Shell shell= new Shell();
		ProjectionViewer textViewer= new ProjectionViewer(shell, null, null, false, SWT.NONE);

		TabsToSpacesConverter tabToSpacesConverter= new TabsToSpacesConverter();
		tabToSpacesConverter.setLineTracker(new DefaultLineTracker());
		tabToSpacesConverter.setNumberOfSpacesPerTab(4);
		tabToSpacesConverter.setDeleteSpacesAsTab(true);
		textViewer.setTabsToSpacesConverter(tabToSpacesConverter);

		Document document= new Document("    COLLAPSED!!!\n    REGION!!!\n    VISIBLE\n    REGION");
		int caretPosition= document.get().indexOf("VISIBLE") - 4;
		textViewer.setDocument(document, new ProjectionAnnotationModel());
		textViewer.enableProjection();
		textViewer.setSelectedRange(caretPosition, 0);

		ProjectionAnnotation annotation= new ProjectionAnnotation(true);
		textViewer.getProjectionAnnotationModel().addAnnotation(annotation, new Position(0, document.getLineOffset(2)));
		textViewer.doOperation(ProjectionViewer.COLLAPSE_ALL);

		TextViewerTest.postKeyEvent(textViewer.getTextWidget(), SWT.DEL, SWT.NONE, SWT.KeyDown);

		assertEquals("    COLLAPSED!!!\n    REGION!!!\nVISIBLE\n    REGION", document.get());
	}
}
