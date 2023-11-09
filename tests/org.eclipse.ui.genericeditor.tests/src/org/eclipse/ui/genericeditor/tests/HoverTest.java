/*******************************************************************************
 * Copyright (c) 2016, 2017 Red Hat Inc. and others
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Map;

import org.junit.Assume;
import org.junit.Before;
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

import org.eclipse.core.runtime.Platform;

import org.eclipse.core.resources.IMarker;

import org.eclipse.text.tests.Accessor;

import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.tests.util.DisplayHelper;

import org.eclipse.ui.genericeditor.tests.contributions.AlrightyHoverProvider;
import org.eclipse.ui.genericeditor.tests.contributions.EnabledPropertyTester;
import org.eclipse.ui.genericeditor.tests.contributions.HelloHoverProvider;
import org.eclipse.ui.genericeditor.tests.contributions.MarkerResolutionGenerator;
import org.eclipse.ui.genericeditor.tests.contributions.WorldHoverProvider;

import org.eclipse.ui.workbench.texteditor.tests.ScreenshotTest;

import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * @since 1.0
 */
public class HoverTest extends AbstratGenericEditorTest {

	@Rule
	public TestName testName= new TestName();

	@Before
	public void skipOnNonLinux() {
		Assume.assumeFalse("This test currently always fail on Windows (bug 505842), skipping", Platform.OS_WIN32.equals(Platform.getOS()));
		Assume.assumeFalse("This test currently always fail on macOS (bug 505842), skipping", Platform.OS_MACOSX.equals(Platform.getOS()));
	}

	@Test
	public void testSingleHover() throws Exception {
		Shell shell= getHoverShell(triggerCompletionAndRetrieveInformationControlManager(), true);
		assertNotNull(findControl(shell, StyledText.class, AlrightyHoverProvider.LABEL));
		assertNull(findControl(shell, StyledText.class, HelloHoverProvider.LABEL));
		assertNull(findControl(shell, StyledText.class, WorldHoverProvider.LABEL));
	}

	@Test
	public void testEnabledWhenHover() throws Exception {
		cleanFileAndEditor();
		EnabledPropertyTester.setEnabled(true);
		createAndOpenFile("enabledWhen.txt", "bar 'bar'");
		Shell shell= getHoverShell(triggerCompletionAndRetrieveInformationControlManager(), true);
		assertNotNull(findControl(shell, StyledText.class, AlrightyHoverProvider.LABEL));
		assertNull(findControl(shell, StyledText.class, WorldHoverProvider.LABEL));

		cleanFileAndEditor();
		EnabledPropertyTester.setEnabled(false);
		createAndOpenFile("enabledWhen.txt", "bar 'bar'");
		shell= getHoverShell(triggerCompletionAndRetrieveInformationControlManager(), true);
		assertNull(findControl(shell, StyledText.class, AlrightyHoverProvider.LABEL));
		assertNotNull(findControl(shell, StyledText.class, WorldHoverProvider.LABEL));
	}

	/**
	 * @throws Exception ex
	 * @since 1.1
	 */
	@Test
	public void testMultipleHover() throws Exception {
		cleanFileAndEditor();
		createAndOpenFile("bar.txt", "Hi");
		Shell shell= getHoverShell(triggerCompletionAndRetrieveInformationControlManager(), true);
		assertNull(findControl(shell, StyledText.class, AlrightyHoverProvider.LABEL));
		assertNotNull(findControl(shell, StyledText.class, WorldHoverProvider.LABEL));
		assertNotNull(findControl(shell, StyledText.class, HelloHoverProvider.LABEL));
	}

	@Test
	public void testProblemHover() throws Exception {
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
			assertTrue(""+hoverData, hoverData instanceof Map);
			assertTrue(((Map<?, ?>) hoverData).containsValue(Collections.singletonList(marker)));
			assertTrue(((Map<?, ?>) hoverData).containsValue(AlrightyHoverProvider.LABEL));
			assertFalse(((Map<?, ?>) hoverData).containsValue(HelloHoverProvider.LABEL));
			// check dialog content
			Shell shell= getHoverShell(manager, true);
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
			new DisplayHelper() {
				@Override
				protected boolean condition() {
					return !m.exists();
				}
			}.waitForCondition(event.display, 1000);
			assertFalse(marker.exists());
		} finally {
			if (marker != null && marker.exists()) {
				marker.delete();
			}
		}
	}

	private Shell getHoverShell(AbstractInformationControlManager manager, boolean failOnError) {
		AbstractInformationControl[] control= { null };
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				control[0]= (AbstractInformationControl) new Accessor(manager, AbstractInformationControlManager.class).get("fInformationControl");
				return control[0] != null;
			}
		}.waitForCondition(this.editor.getSite().getShell().getDisplay(), 5000);
		if (control[0] == null) {
			if (failOnError) {
				ScreenshotTest.takeScreenshot(getClass(), testName.getMethodName(), System.out);
				fail();
			} else {
				return null;
			}
		}
		Shell shell= (Shell) new Accessor(control[0], AbstractInformationControl.class).get("fShell");
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return shell.isVisible();
			}
		}.waitForCondition(this.editor.getSite().getShell().getDisplay(), 2000);
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
			if (control instanceof Label) {
				controlLabel= ((Label) control).getText();
			} else if (control instanceof Link) {
				controlLabel= ((Link) control).getText();
			} else if (control instanceof Text) {
				controlLabel= ((Text) control).getText();
			} else if (control instanceof StyledText) {
				controlLabel= ((StyledText) control).getText();
			}
			if (controlLabel != null && controlLabel.contains(label)) {
				return res;
			}
		} else if (control instanceof Composite) {
			for (Control child : ((Composite) control).getChildren()) {
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
		final int caretLocation= 2;
		this.editor.selectAndReveal(caretLocation, 0);
		final StyledText editorTextWidget= (StyledText) this.editor.getAdapter(Control.class);
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return editorTextWidget.isFocusControl() && editorTextWidget.getSelection().x == caretLocation;
			}
		}.waitForCondition(editorTextWidget.getDisplay(), 3000);
		// sending event to trigger hover computation
		editorTextWidget.getShell().forceActive();
		editorTextWidget.getShell().setActive();
		editorTextWidget.getShell().setFocus();
		editorTextWidget.getShell().getDisplay().wake();
		Event hoverEvent= new Event();
		hoverEvent.widget= editorTextWidget;
		hoverEvent.type= SWT.MouseHover;
		hoverEvent.x= editorTextWidget.getClientArea().x + 5;
		hoverEvent.y= editorTextWidget.getClientArea().y + 5;
		hoverEvent.display= editorTextWidget.getDisplay();
		hoverEvent.doit= true;
		editorTextWidget.getDisplay().setCursorLocation(editorTextWidget.toDisplay(hoverEvent.x, hoverEvent.y));
		editorTextWidget.notifyListeners(SWT.MouseHover, hoverEvent);
		ITextViewer viewer= (ITextViewer) new Accessor(editor, AbstractTextEditor.class).invoke("getSourceViewer", new Object[0]);
		AbstractInformationControlManager textHoverManager= (AbstractInformationControlManager) new Accessor(viewer, TextViewer.class).get("fTextHoverManager");
		// retrieving hover content
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return getHoverData(textHoverManager) != null;
			}
		}.waitForCondition(hoverEvent.display, 6000);
		return textHoverManager;
	}
}
