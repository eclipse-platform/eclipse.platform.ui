/*******************************************************************************
 * Copyright (c) 2021, 2025 Red Hat Inc. and others
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import org.eclipse.test.Screenshots;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.text.tests.Accessor;

import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;

import org.eclipse.ui.genericeditor.tests.contributions.AlrightyHoverProvider;
import org.eclipse.ui.tests.harness.util.DisplayHelper;

/**
 * @since 1.2
 */
@EnabledOnOs(value = OS.LINUX, disabledReason = "This test currently always fail on Windows and MacOS (bug 505842), skipping")
public class ShowInformationTest extends AbstratGenericEditorTest {

	@Test
	public void testInformationControl(TestInfo info) throws Exception {
		Shell shell= getHoverShell(info, triggerCompletionAndRetrieveInformationControlManager(), true);
		assertNotNull(findControl(shell, StyledText.class, AlrightyHoverProvider.LABEL));
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
		boolean[] result = {false};
		Shell shell= control[0].getShell();
		DisplayHelper.waitForCondition(control[0].getShell().getDisplay(), 2000, () -> result[0] = shell.isVisible());
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

	private Object getShowInformationData(AbstractInformationControlManager manager) {
		return new Accessor(manager, AbstractInformationControlManager.class).get("fInformation");
	}

	private AbstractInformationControlManager triggerCompletionAndRetrieveInformationControlManager() {
		final int caretLocation= 2;
		this.editor.selectAndReveal(caretLocation, 0);
		final StyledText editorTextWidget= (StyledText) this.editor.getAdapter(Control.class);
		DisplayHelper.waitForCondition(editorTextWidget.getDisplay(), 3000,
				() -> editorTextWidget.isFocusControl() && editorTextWidget.getSelection().x == caretLocation);
		// sending event to trigger hover computation
		editorTextWidget.getShell().forceActive();
		editorTextWidget.getShell().setActive();
		editorTextWidget.getShell().setFocus();
		editorTextWidget.getShell().getDisplay().wake();

		ITextViewer viewer= editor.getAdapter(ITextViewer.class);

		ITextOperationTarget textOperationTarget = (ITextOperationTarget)viewer;
		assertTrue(textOperationTarget.canDoOperation(ISourceViewer.INFORMATION));
		textOperationTarget.doOperation(ISourceViewer.INFORMATION);
		
		AbstractInformationControlManager informationControlManager= (AbstractInformationControlManager) new Accessor(viewer, SourceViewer.class).get("fInformationPresenter");
		// retrieving hover content
		DisplayHelper.waitForCondition(editorTextWidget.getDisplay(), 6000,
				() -> getShowInformationData(informationControlManager) != null);
		return informationControlManager;
	}
}
