/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria, Sopot Cela (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

import org.eclipse.core.resources.IMarker;

import org.eclipse.jface.text.AbstractHoverInformationControlManager;
import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * @since 3.11
 *
 */
public class HoverTest {

	private AbstractTextEditor editor;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		GenericEditorTestUtils.setUpBeforeClass();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		GenericEditorTestUtils.tearDownAfterClass();
	}

	@Before
	public void setUp() throws Exception {
		GenericEditorTestUtils.closeIntro();
		editor = (AbstractTextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().openEditor(new FileEditorInput(GenericEditorTestUtils.getFile()), "org.eclipse.ui.genericeditor.GenericEditor");
	}

	@After
	public void tearDown() throws Exception {
		editor.getSite().getPage().closeEditor(editor, false);
		editor= null;
	}

	@Test
	public void testHover() throws Exception {
		assertEquals("Alrighty!", getHoverData());
	}
	
	@Test
	public void testProblemHover() throws Exception {
		String problemMessage = "Huston...";
		IMarker marker = null;
		try {
			marker = GenericEditorTestUtils.getFile().createMarker(IMarker.PROBLEM);
			marker.setAttribute(IMarker.LINE_NUMBER, 1);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			marker.setAttribute(IMarker.CHAR_START, 0);
			marker.setAttribute(IMarker.CHAR_END, 5);
			marker.setAttribute(IMarker.MESSAGE, problemMessage);
			assertEquals(problemMessage, getHoverData());
		} finally {
			if (marker != null) {
				marker.delete();
			}
		}
	}

	private Object getHoverData() throws Exception {
		this.editor.selectAndReveal(2, 0);
		GenericEditorTestUtils.waitAndDispatch(1000);
		// sending event to trigger hover computation
		StyledText editorTextWidget = (StyledText) this.editor.getAdapter(Control.class);
		editorTextWidget.getShell().forceActive();
		editorTextWidget.getShell().setActive();
		editorTextWidget.getShell().setFocus();
		editorTextWidget.getShell().getDisplay().wake();
		Event hoverEvent = new Event();
		hoverEvent.widget = editorTextWidget;
		hoverEvent.type = SWT.MouseHover;
		hoverEvent.x = editorTextWidget.getClientArea().x + 5;
		hoverEvent.y = editorTextWidget.getClientArea().y + 5;
		hoverEvent.display = editorTextWidget.getDisplay();
		hoverEvent.doit = true;
		editorTextWidget.notifyListeners(SWT.MouseHover, hoverEvent);
		// Events need to be processed for hover listener to work correctly
		GenericEditorTestUtils.waitAndDispatch(1000);
		// retrieving hover content
		Method getSourceViewerMethod= AbstractTextEditor.class.getDeclaredMethod("getSourceViewer");
		getSourceViewerMethod.setAccessible(true);
		ITextViewer viewer = (ITextViewer) getSourceViewerMethod.invoke(editor);
		Field textHoverManagerField= TextViewer.class.getDeclaredField("fTextHoverManager");
		textHoverManagerField.setAccessible(true);
		AbstractHoverInformationControlManager hover = (AbstractHoverInformationControlManager) textHoverManagerField.get(viewer);
		Field informationField = AbstractInformationControlManager.class.getDeclaredField("fInformation");
		informationField.setAccessible(true);
		Object hoverData = informationField.get(hover);
		GenericEditorTestUtils.waitAndDispatch(1000);
		return hoverData;
	}

}
