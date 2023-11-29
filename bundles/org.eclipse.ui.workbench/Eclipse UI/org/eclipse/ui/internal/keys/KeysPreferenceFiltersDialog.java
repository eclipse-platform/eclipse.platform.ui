/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal.keys;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.preferences.ViewSettingsDialog;

/**
 * Creates a dialog box for applying filter selection of When combo box in
 * NewKeysPreferencePage
 *
 * @since 3.3
 */
public class KeysPreferenceFiltersDialog extends ViewSettingsDialog {

	private Button actionSetFilterCheckBox;
	private Button internalFilterCheckBox;
	private Button uncategorizedFilterCheckBox;

	private boolean filterActionSet;
	private boolean filterInternal;
	private boolean filterUncategorized;
	private boolean filterShowUnboundCommands;

	void setFilterActionSet(boolean b) {
		filterActionSet = b;
	}

	void setFilterInternal(boolean b) {
		filterInternal = b;
	}

	void setFilterUncategorized(boolean b) {
		filterUncategorized = b;
	}

	boolean getFilterActionSet() {
		return filterActionSet;
	}

	boolean getFilterInternal() {
		return filterInternal;
	}

	boolean getFilterUncategorized() {
		return filterUncategorized;
	}

	public KeysPreferenceFiltersDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void performDefaults() {
		actionSetFilterCheckBox.setSelection(true);
		internalFilterCheckBox.setSelection(true);
		uncategorizedFilterCheckBox.setSelection(true);
		super.performDefaults();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite topComposite = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout(1, false);
		topComposite.setLayout(layout);
		topComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));
		actionSetFilterCheckBox = new Button(topComposite, SWT.CHECK);
		actionSetFilterCheckBox.setText(NewKeysPreferenceMessages.ActionSetFilterCheckBox_Text);
		internalFilterCheckBox = new Button(topComposite, SWT.CHECK);
		internalFilterCheckBox.setText(NewKeysPreferenceMessages.InternalFilterCheckBox_Text);
		uncategorizedFilterCheckBox = new Button(topComposite, SWT.CHECK);
		uncategorizedFilterCheckBox.setText(NewKeysPreferenceMessages.UncategorizedFilterCheckBox_Text);

		actionSetFilterCheckBox.setSelection(filterActionSet);
		internalFilterCheckBox.setSelection(filterInternal);
		uncategorizedFilterCheckBox.setSelection(filterUncategorized);
		applyDialogFont(topComposite);

		return topComposite;
	}

	@Override
	protected void okPressed() {
		filterActionSet = actionSetFilterCheckBox.getSelection();
		filterInternal = internalFilterCheckBox.getSelection();
		filterUncategorized = uncategorizedFilterCheckBox.getSelection();
		super.okPressed();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(NewKeysPreferenceMessages.KeysPreferenceFilterDialog_Title);
	}

	boolean getFilterShowUnboundCommands() {
		return filterShowUnboundCommands;
	}

	void setFilterUnboundCommands(boolean filterUnboundCommands) {
		this.filterShowUnboundCommands = filterUnboundCommands;
	}

}
