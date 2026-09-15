/*******************************************************************************
 * Copyright (c) 2026 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.LinkedHashMap;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * Asks whether to include the projects nested below or referenced by the
 * selection, with a toggle that stores the answers in preferences.
 */
final class RelatedProjectsDialog extends MessageDialogWithToggle {

	/** The answer, each flag only meaningful when its check box was shown. */
	record Answer(boolean includeNested, boolean includeReferenced) {
	}

	private final IPreferenceStore store;
	private final String nestedKey;
	private final String referencedKey;
	private Button nestedButton;
	private Button referencedButton;
	private Answer answer;

	private RelatedProjectsDialog(Shell parent, String title, String message, String actionLabel,
			IPreferenceStore store, String nestedKey, String referencedKey) {
		super(parent, title, null, message, QUESTION, buttons(actionLabel), 0, null, false);
		setShellStyle(getShellStyle() | SWT.SHEET);
		this.store = store;
		this.nestedKey = nestedKey;
		this.referencedKey = referencedKey;
	}

	/** The map fixes the ids, a plain label array gets internal ids instead of OK_ID. */
	private static LinkedHashMap<String, Integer> buttons(String actionLabel) {
		LinkedHashMap<String, Integer> buttons = new LinkedHashMap<>();
		buttons.put(actionLabel, Integer.valueOf(IDialogConstants.OK_ID));
		buttons.put(IDialogConstants.CANCEL_LABEL, Integer.valueOf(IDialogConstants.CANCEL_ID));
		return buttons;
	}

	/**
	 * Opens the dialog with a check box for each non-null preference key and
	 * returns the answer, or <code>null</code> if the user cancelled. A checked
	 * toggle stores each answer under its key as {@link #ALWAYS} or
	 * {@link #NEVER}.
	 */
	static Answer open(Shell parent, String title, String message, String actionLabel, IPreferenceStore store,
			String nestedKey, String referencedKey) {
		RelatedProjectsDialog dialog = new RelatedProjectsDialog(parent, title, message, actionLabel, store,
				nestedKey, referencedKey);
		if (dialog.open() != IDialogConstants.OK_ID) {
			return null;
		}
		return dialog.answer;
	}

	@Override
	protected Control createCustomArea(Composite parent) {
		Composite area = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(area);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(area);
		if (nestedKey != null) {
			nestedButton = createCheckBox(area, IDEWorkbenchMessages.RelatedProjectsDialog_includeNested);
		}
		if (referencedKey != null) {
			referencedButton = createCheckBox(area, IDEWorkbenchMessages.RelatedProjectsDialog_includeReferenced);
		}
		return area;
	}

	private static Button createCheckBox(Composite parent, String text) {
		Button button = new Button(parent, SWT.CHECK | SWT.LEFT);
		button.setText(text);
		button.setFont(parent.getFont());
		return button;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		answer = new Answer(isChecked(nestedButton), isChecked(referencedButton));
		if (buttonId == IDialogConstants.OK_ID && getToggleState()) {
			remember(nestedKey, answer.includeNested());
			remember(referencedKey, answer.includeReferenced());
		}
		super.buttonPressed(buttonId);
	}

	private static boolean isChecked(Button button) {
		return button != null && button.getSelection();
	}

	private void remember(String key, boolean include) {
		if (key != null) {
			store.setValue(key, include ? ALWAYS : NEVER);
		}
	}
}
