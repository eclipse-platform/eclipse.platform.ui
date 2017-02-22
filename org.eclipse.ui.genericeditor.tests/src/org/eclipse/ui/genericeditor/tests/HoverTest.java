/*******************************************************************************
 * Copyright (c) 2016, 2017 Red Hat Inc. and others
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.core.resources.IMarker;

import org.eclipse.text.tests.Accessor;

import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.tests.util.DisplayHelper;

import org.eclipse.ui.genericeditor.tests.contributions.MagicHoverProvider;
import org.eclipse.ui.genericeditor.tests.contributions.MarkerResolutionGenerator;

import org.eclipse.ui.workbench.texteditor.tests.ScreenshotTest;

import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * @since 1.0
 */
public class HoverTest extends AbstratGenericEditorTest {

	@Rule
	public TestName testName = new TestName();

	@Test
	public void testHover() throws Exception {
		Shell shell = getHoverShell(triggerCompletionAndRetrieveInformationControlManager());
		assertNotNull(findControl(shell, StyledText.class, MagicHoverProvider.LABEL));
	}

	@Test
	public void testProblemHover() throws Exception {
		String problemMessage = "Huston...";
		IMarker marker = null;
		try {
			marker = this.file.createMarker(IMarker.PROBLEM);
			marker.setAttribute(IMarker.LINE_NUMBER, 1);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			marker.setAttribute(IMarker.CHAR_START, 0);
			marker.setAttribute(IMarker.CHAR_END, 5);
			marker.setAttribute(IMarker.MESSAGE, problemMessage);
			marker.setAttribute(MarkerResolutionGenerator.FIXME, true);
			AbstractInformationControlManager manager = triggerCompletionAndRetrieveInformationControlManager();
			assertEquals(Collections.singletonList(marker), getHoverData(manager));
			// check dialog content
			Shell shell= getHoverShell(manager);
			assertNotNull(findControl(shell, Label.class, marker.getAttribute(IMarker.MESSAGE, "NONE")));
			Link link = findControl(shell, Link.class, MarkerResolutionGenerator.FIXME);
			assertNotNull(link);
			Event event = new Event();
			event.widget = link;
			event.display = link.getDisplay();
			event.doit = true;
			event.type = SWT.Selection;
			link.notifyListeners(SWT.Selection, event);
			assertFalse(marker.exists());
		} finally {
			if (marker != null && marker.exists()) {
				marker.delete();
			}
		}
	}

	private Shell getHoverShell(AbstractInformationControlManager manager) {
		AbstractInformationControl[] control = { null };
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				control[0] = (AbstractInformationControl)new Accessor(manager, AbstractInformationControlManager.class).get("fInformationControl");
				return control[0] != null;
			}
		}.waitForCondition(this.editor.getSite().getShell().getDisplay(), 5000);
		if (control[0] == null) {
			ScreenshotTest.takeScreenshot(getClass(), testName.getMethodName(), System.out);
			fail();
		}
		Shell shell = (Shell)new Accessor(control[0], AbstractInformationControl.class).get("fShell");
		assertTrue(shell.isVisible());
		return shell;
	}

	private <T extends Control> T findControl(Control control, Class<T> controlType, String label) {
		if (control.getClass() == controlType) {
			T res = (T)control;
			if (label == null) {
				return res;
			}
			String controlLabel = null;
			if (control instanceof Label) {
				controlLabel = ((Label)control).getText();
			} else if (control instanceof Link) {
				controlLabel = ((Link) control).getText();
			} else if (control instanceof Text) {
				controlLabel = ((Text) control).getText();
			} else if (control instanceof StyledText) {
				controlLabel = ((StyledText) control).getText();
			}
			if (controlLabel != null && controlLabel.contains(label)) {
				return res;
			}
		} else if (control instanceof Composite) {
			for (Control child : ((Composite) control).getChildren()) {
				T res = findControl(child, controlType, label);
				if (res != null) {
					return res;
				}
			}
		}
		return null;
	}

	private Object getHoverData(AbstractInformationControlManager manager) throws Exception {
		Object hoverData = new Accessor(manager, AbstractInformationControlManager.class).get("fInformation");
		return hoverData;
	}

	private AbstractInformationControlManager triggerCompletionAndRetrieveInformationControlManager() {
		this.editor.selectAndReveal(2, 0);
		final StyledText editorTextWidget = (StyledText) this.editor.getAdapter(Control.class);
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return editorTextWidget.isFocusControl() && editorTextWidget.getSelection().x == 2;
			}
		}.waitForCondition(editorTextWidget.getDisplay(), 1000);
		// sending event to trigger hover computation
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
		editorTextWidget.getDisplay().setCursorLocation(editorTextWidget.toDisplay(hoverEvent.x, hoverEvent.y));
		editorTextWidget.notifyListeners(SWT.MouseHover, hoverEvent);
		// Events need to be processed for hover listener to work correctly
		DisplayHelper.sleep(editorTextWidget.getDisplay(), 1000);
		// retrieving hover content
		ITextViewer viewer = (ITextViewer)new Accessor(editor, AbstractTextEditor.class).invoke("getSourceViewer", new Object[0]);
		AbstractInformationControlManager textHoverManager = (AbstractInformationControlManager)new Accessor(viewer, TextViewer.class).get("fTextHoverManager");
		return textHoverManager;
	}

}
