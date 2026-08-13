/*******************************************************************************
 * Copyright (c) 2016, 2025 Red Hat Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mickael Istria, Sopot Cela (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import org.eclipse.test.Screenshots;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
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

import org.eclipse.ui.genericeditor.tests.contributions.AlrightyHoverProvider;
import org.eclipse.ui.genericeditor.tests.contributions.EnabledPropertyTester;
import org.eclipse.ui.genericeditor.tests.contributions.HelloHoverProvider;
import org.eclipse.ui.genericeditor.tests.contributions.MarkerResolutionGenerator;
import org.eclipse.ui.genericeditor.tests.contributions.WorldHoverProvider;
import org.eclipse.ui.tests.harness.util.DisplayHelper;

/**
 * @since 1.0
 */
@EnabledOnOs(value = OS.LINUX, disabledReason = "This test currently always fail on Windows and MacOS (bug 505842), skipping")
public class HoverTest extends AbstratGenericEditorTest {

	private static final int MAXIMUM_HOVER_RETRY_COUNT = 5;

	private static final int FOCUS_RETRY_COUNT = 3;

	private static final int CARET_LOCATION = 2;

	@Test
	public void testSingleHover(TestInfo info) throws Exception {
		Shell shell= getHoverShell(info, triggerCompletionAndRetrieveInformationControlManager(), true);
		assertNotNull(findControl(shell, StyledText.class, AlrightyHoverProvider.LABEL));
		assertNull(findControl(shell, StyledText.class, HelloHoverProvider.LABEL));
		assertNull(findControl(shell, StyledText.class, WorldHoverProvider.LABEL));
	}

	@Test
	public void testEnabledWhenHover(TestInfo info) throws Exception {
		cleanFileAndEditor();
		EnabledPropertyTester.setEnabled(true);
		createAndOpenFile("enabledWhen.txt", "bar 'bar'");
		Shell shell= getHoverShell(info, triggerCompletionAndRetrieveInformationControlManager(), true);
		assertNotNull(findControl(shell, StyledText.class, AlrightyHoverProvider.LABEL));
		assertNull(findControl(shell, StyledText.class, WorldHoverProvider.LABEL));

		// Capture the first shell and its display to wait for disposal
		Shell firstShell = shell;
		Display display = firstShell.getDisplay();
		cleanFileAndEditor();
		// Wait for the hover shell to be disposed after editor cleanup
		DisplayHelper.waitForCondition(display, 3000, () -> firstShell.isDisposed());

		EnabledPropertyTester.setEnabled(false);
		createAndOpenFile("enabledWhen.txt", "bar 'bar'");
		shell= getHoverShell(info, triggerCompletionAndRetrieveInformationControlManager(), true);
		assertNull(findControl(shell, StyledText.class, AlrightyHoverProvider.LABEL));
		assertNotNull(findControl(shell, StyledText.class, WorldHoverProvider.LABEL));
	}

	/**
	 * @throws Exception ex
	 * @since 1.1
	 */
	@Test
	public void testMultipleHover(TestInfo info) throws Exception {
		cleanFileAndEditor();
		createAndOpenFile("bar.txt", "Hi");
		Shell shell= getHoverShell(info, triggerCompletionAndRetrieveInformationControlManager(), true);
		assertNull(findControl(shell, StyledText.class, AlrightyHoverProvider.LABEL));
		assertNotNull(findControl(shell, StyledText.class, WorldHoverProvider.LABEL));
		assertNotNull(findControl(shell, StyledText.class, HelloHoverProvider.LABEL));
	}

	@Test
	public void testProblemHover(TestInfo info) throws Exception {
		String problemMessage= "Huston...";
		IMarker marker= null;
		try {
			marker= this.file.createMarker(IMarker.PROBLEM);
			marker.setAttribute(IMarker.LINE_NUMBER, 1);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			marker.setAttribute(IMarker.CHAR_START, 0);
			marker.setAttribute(IMarker.CHAR_END, 5);
			marker.setAttribute(IMarker.MESSAGE, problemMessage);
			marker.setAttribute(MarkerResolutionGenerator.FIXME, true);
			AbstractInformationControlManager manager= triggerCompletionAndRetrieveInformationControlManager();
			Object hoverData= getHoverData(manager);
			assertTrue(hoverData instanceof Map, ""+hoverData);
			assertTrue(((Map<?, ?>) hoverData).containsValue(Collections.singletonList(marker)));
			assertTrue(((Map<?, ?>) hoverData).containsValue(AlrightyHoverProvider.LABEL));
			assertFalse(((Map<?, ?>) hoverData).containsValue(HelloHoverProvider.LABEL));
			// check dialog content
			Shell shell= getHoverShell(info, manager, true);
			assertNotNull(findControl(shell, Label.class, marker.getAttribute(IMarker.MESSAGE, "NONE")));
			assertNotNull(findControl(shell, StyledText.class, AlrightyHoverProvider.LABEL));
			assertNull(findControl(shell, StyledText.class, HelloHoverProvider.LABEL));
			// check quick-fix works
			Link link= findControl(shell, Link.class, MarkerResolutionGenerator.FIXME);
			assertNotNull(link);
			Event event= new Event();
			event.widget= link;
			event.display= link.getDisplay();
			event.doit= true;
			event.type= SWT.Selection;
			link.notifyListeners(SWT.Selection, event);
			final IMarker m= marker;
			DisplayHelper.waitForCondition(event.display, 1000, () -> !m.exists());
			assertFalse(marker.exists());
		} finally {
			if (marker != null && marker.exists()) {
				marker.delete();
			}
		}
	}

	private Shell getHoverShell(TestInfo info, AbstractInformationControlManager manager, boolean failOnError) {
		AbstractInformationControl[] control= { null };
		DisplayHelper.waitForCondition(this.editor.getSite().getShell().getDisplay(), 5000, () -> {
			control[0] = (AbstractInformationControl) new Accessor(manager, AbstractInformationControlManager.class)
					.get("fInformationControl");
			return control[0] != null;
		});
		if (control[0] == null) {
			if (failOnError) {
				Screenshots.takeScreenshot(getClass(), info.getDisplayName());
				fail();
			} else {
				return null;
			}
		}
		Shell shell= control[0].getShell();
		DisplayHelper.waitForCondition(this.editor.getSite().getShell().getDisplay(), 2000, () -> shell.isVisible());
		if (failOnError) {
			assertTrue(shell.isVisible());
		}
		return shell;
	}

	private <T extends Control> T findControl(Control control, Class<T> controlType, String label) {
		if (control.getClass() == controlType) {
			@SuppressWarnings("unchecked")
			T res= (T) control;
			if (label == null) {
				return res;
			}
			String controlLabel= null;
			if (control instanceof Label l) {
				controlLabel= l.getText();
			} else if (control instanceof Link link) {
				controlLabel= link.getText();
			} else if (control instanceof Text text) {
				controlLabel= text.getText();
			} else if (control instanceof StyledText styled) {
				controlLabel= styled.getText();
			}
			if (controlLabel != null && controlLabel.contains(label)) {
				return res;
			}
		} else if (control instanceof Composite comp) {
			for (Control child : comp.getChildren()) {
				T res= findControl(child, controlType, label);
				if (res != null) {
					return res;
				}
			}
		}
		return null;
	}

	private Object getHoverData(AbstractInformationControlManager manager) {
		Object hoverData= new Accessor(manager, AbstractInformationControlManager.class).get("fInformation");
		return hoverData;
	}

	private AbstractInformationControlManager triggerCompletionAndRetrieveInformationControlManager() {
		boolean foundHoverData = false;
		boolean gotFocus = false;
		int attemptNumber = 0;

		ITextViewer viewer= editor.getAdapter(ITextViewer.class);
		AbstractInformationControlManager textHoverManager= (AbstractInformationControlManager) new Accessor(viewer, TextViewer.class).get("fTextHoverManager");

		while (!foundHoverData && attemptNumber++ < MAXIMUM_HOVER_RETRY_COUNT) {
			final StyledText editorTextWidget= (StyledText) this.editor.getAdapter(Control.class);
			if (!focusEditor(editorTextWidget)) {
				// the window manager may hand activation to another shell, retry from scratch
				continue;
			}
			gotFocus = true;
			// sending event to trigger hover computation
			Event hoverEvent= new Event();
			hoverEvent.widget= editorTextWidget;
			hoverEvent.type= SWT.MouseHover;
			hoverEvent.x= editorTextWidget.getClientArea().x + 5;
			hoverEvent.y= editorTextWidget.getClientArea().y + 5;
			hoverEvent.display= editorTextWidget.getDisplay();
			hoverEvent.doit= true;
			editorTextWidget.getDisplay().setCursorLocation(editorTextWidget.toDisplay(hoverEvent.x, hoverEvent.y));
			editorTextWidget.notifyListeners(SWT.MouseHover, hoverEvent);
			// retrieving hover content
			foundHoverData = DisplayHelper.waitForCondition(hoverEvent.display, 6000,
					() -> getHoverData(textHoverManager) != null);
		}
		assertTrue(gotFocus, "editor does not have focus");
		assertTrue(foundHoverData, "hover data not found");
		return textHoverManager;
	}

	/**
	 * Places the caret and waits until the editor widget really owns the keyboard
	 * focus. Requesting focus once is not enough, activation can still be taken
	 * away right afterwards, so the request is repeated a few times.
	 */
	private boolean focusEditor(StyledText editorTextWidget) {
		Shell shell= this.editor.getSite().getShell();
		Display display= shell.getDisplay();
		for (int attempt= 0; attempt < FOCUS_RETRY_COUNT; attempt++) {
			// a retry means the previous request was not honored, so re-activate the
			// window even when SWT still considers its shell to be the active one
			if (attempt > 0 || display.getActiveShell() != shell) {
				// forceActive() alone can be a no-op on a window manager that refuses the raise
				shell.forceActive();
				shell.forceFocus();
			}
			this.editor.getSite().getPage().activate(this.editor);
			this.editor.setFocus();
			this.editor.selectAndReveal(CARET_LOCATION, 0);
			if (DisplayHelper.waitForCondition(display, 1000, () -> editorTextWidget.isFocusControl()
					&& editorTextWidget.getSelection().x == CARET_LOCATION)) {
				return true;
			}
		}
		return false;
	}
}
