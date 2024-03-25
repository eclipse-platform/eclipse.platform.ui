/*******************************************************************************
 * Copyright (c) 2021 Red Hat Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
// *
 * Contributors:
 *     Red Hat Inc.
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.core.runtime.Platform;

import org.eclipse.text.tests.Accessor;

import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.tests.util.DisplayHelper;

import org.eclipse.ui.genericeditor.tests.contributions.AlrightyHoverProvider;

import org.eclipse.ui.workbench.texteditor.tests.ScreenshotTest;

import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * @since 1.2
 */
public class ShowInformationTest extends AbstratGenericEditorTest {

	@Rule
	public TestName testName= new TestName();

	@Before
	public void skipOnNonLinux() {
		Assume.assumeFalse("This test currently always fail on Windows (bug 505842), skipping", Platform.OS_WIN32.equals(Platform.getOS()));
		Assume.assumeFalse("This test currently always fail on macOS (bug 505842), skipping", Platform.OS_MACOSX.equals(Platform.getOS()));
	}

	@Test
	public void testInformationControl() throws Exception {
		Shell shell= getHoverShell(triggerCompletionAndRetrieveInformationControlManager(), true);
		assertNotNull(findControl(shell, StyledText.class, AlrightyHoverProvider.LABEL));
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
		boolean[] result = {false};
		Shell shell= (Shell) new Accessor(control[0], AbstractInformationControl.class).get("fShell");
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return (result[0] = shell.isVisible());
			}
		}.waitForCondition(control[0].getShell().getDisplay(), 2000);
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

	private Object getShowInformationData(AbstractInformationControlManager manager) {
		return new Accessor(manager, AbstractInformationControlManager.class).get("fInformation");
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

		ITextViewer viewer= (ITextViewer) new Accessor(editor, AbstractTextEditor.class).invoke("getSourceViewer", new Object[0]);

		ITextOperationTarget textOperationTarget = (ITextOperationTarget)viewer;
		assertTrue(textOperationTarget.canDoOperation(ISourceViewer.INFORMATION));
		textOperationTarget.doOperation(ISourceViewer.INFORMATION);
		
		AbstractInformationControlManager informationControlManager= (AbstractInformationControlManager) new Accessor(viewer, SourceViewer.class).get("fInformationPresenter");
		// retrieving hover content
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return getShowInformationData(informationControlManager) != null;
			}
		}.waitForCondition(editorTextWidget.getDisplay(), 6000);
		return informationControlManager;
	}
}
