/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.scripting;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.core.runtime.Assert;

import org.eclipse.ltk.internal.ui.refactoring.RefactoringLocationControl;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.internal.ui.refactoring.util.SWTUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;

/**
 * Control to specify the location of a refactoring script.
 * 
 * @since 3.2
 */
public class RefactoringScriptLocationControl extends Composite {

	/** The clipboard dialog setting */
	protected static final String SETTING_CLIPBOARD= "org.eclipse.ltk.ui.refactoring.useClipboard"; //$NON-NLS-1$

	/** The history dialog setting */
	protected static final String SETTING_HISTORY= "org.eclipse.ltk.ui.refactoring.scriptHistory"; //$NON-NLS-1$

	/** The external browse button */
	protected Button fExternalBrowseButton= null;

	/** The location control */
	protected RefactoringLocationControl fExternalLocationControl= null;

	/** The from clipboard button */
	protected Button fFromClipboardButton= null;

	/** The from external location button */
	protected Button fFromExternalLocationButton= null;

	/** The script location, or <code>null</code> */
	protected URI fScriptLocation= null;

	/** The parent wizard */
	protected final IWizard fWizard;

	/**
	 * Creates a new refactoring script location control.
	 * 
	 * @param wizard
	 *            the parent wizard
	 * @param parent
	 *            the parent control
	 */
	public RefactoringScriptLocationControl(final IWizard wizard, final Composite parent) {
		super(parent, SWT.NONE);
		Assert.isNotNull(wizard);
		fWizard= wizard;
		setLayoutData(createGridData(GridData.FILL_HORIZONTAL, 6, 0));
		setLayout(new GridLayout(3, false));
		boolean clipboard= false;
		final IDialogSettings settings= fWizard.getDialogSettings();
		if (settings != null)
			clipboard= settings.getBoolean(SETTING_CLIPBOARD);
		fFromClipboardButton= new Button(this, SWT.RADIO);
		fFromClipboardButton.setText(ScriptingMessages.ScriptLocationControl_clipboard_label);
		fFromClipboardButton.setLayoutData(createGridData(GridData.HORIZONTAL_ALIGN_BEGINNING, 3, 0));
		fFromClipboardButton.setSelection(clipboard);
		fFromClipboardButton.addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				final boolean selection= fFromClipboardButton.getSelection();
				fExternalLocationControl.setEnabled(!selection);
				fExternalBrowseButton.setEnabled(!selection);
				handleClipboardScriptChanged();
			}
		});
		fFromExternalLocationButton= new Button(this, SWT.RADIO);
		fFromExternalLocationButton.setText(ScriptingMessages.ScriptLocationControl_location_label);
		fFromExternalLocationButton.setLayoutData(createGridData(GridData.HORIZONTAL_ALIGN_BEGINNING, 1, 0));
		fFromExternalLocationButton.setSelection(!clipboard);
		fFromExternalLocationButton.addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				handleExternalLocationChanged();
			}
		});
		fExternalLocationControl= new RefactoringLocationControl(fWizard, this, SETTING_HISTORY);
		fExternalLocationControl.setLayoutData(createGridData(GridData.FILL_HORIZONTAL, 1, 0));
		fExternalLocationControl.setEnabled(!clipboard);
		fExternalLocationControl.getControl().addModifyListener(new ModifyListener() {

			public final void modifyText(final ModifyEvent event) {
				handleExternalLocationChanged();
			}
		});
		fExternalLocationControl.getControl().addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(final SelectionEvent event) {
				handleExternalLocationChanged();
			}
		});
		if (!clipboard)
			fExternalLocationControl.setFocus();
		fExternalBrowseButton= new Button(this, SWT.PUSH);
		fExternalBrowseButton.setText(ScriptingMessages.ScriptLocationControl_browse_label);
		fExternalBrowseButton.setEnabled(!clipboard);
		fExternalBrowseButton.setLayoutData(createGridData(GridData.HORIZONTAL_ALIGN_FILL, 1, 0));
		SWTUtil.setButtonDimensionHint(fExternalBrowseButton);
		fExternalBrowseButton.addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				handleBrowseExternalLocation();
			}
		});
		addDisposeListener(new DisposeListener() {

			public final void widgetDisposed(final DisposeEvent event) {
				if (settings != null)
					settings.put(SETTING_CLIPBOARD, fFromClipboardButton.getSelection());
			}
		});
	}

	/**
	 * Creates a new grid data.
	 * 
	 * @param flag
	 *            the flags to use
	 * @param hspan
	 *            the horizontal span
	 * @param indent
	 *            the indent
	 * @return the grid data
	 */
	protected GridData createGridData(final int flag, final int hspan, final int indent) {
		final GridData data= new GridData(flag);
		data.horizontalIndent= indent;
		data.horizontalSpan= hspan;
		return data;
	}

	/**
	 * Returns the chosen refactoring script location.
	 * 
	 * @return the refactoring script location, or <code>null</code> if no
	 *         refactoring script has been chosen, or the refactoring script
	 *         should be taken from the clipboard
	 */
	public URI getRefactoringScript() {
		return fScriptLocation;
	}

	/**
	 * Handles the browse external location event.
	 */
	protected void handleBrowseExternalLocation() {
		final FileDialog file= new FileDialog(getShell(), SWT.OPEN);
		file.setText(ScriptingMessages.ScriptLocationControl_browse_caption);
		file.setFilterNames(new String[] { ScriptingMessages.ScriptLocationControl_filter_name_script, ScriptingMessages.ScriptLocationControl_filter_name_wildcard});
		file.setFilterExtensions(new String[] { ScriptingMessages.ScriptLocationControl_filter_extension_script, ScriptingMessages.ScriptLocationControl_filter_extension_wildcard});
		final String path= file.open();
		if (path != null)
			fExternalLocationControl.setText(path);
	}

	/**
	 * Handles the clipboard script changed event.
	 */
	protected void handleClipboardScriptChanged() {
		// Do nothing
	}

	/**
	 * Handles the external location changed event.
	 */
	protected void handleExternalLocationChanged() {
		final String text= fExternalLocationControl.getText();
		if (text != null && !"".equals(text)) //$NON-NLS-1$
			fScriptLocation= new File(text).toURI();
		else
			fScriptLocation= null;
	}

	/**
	 * Loads the history of this control.
	 */
	public void loadHistory() {
		fExternalLocationControl.loadHistory();
		if (fFromClipboardButton.getSelection())
			handleClipboardScriptChanged();
	}

	/**
	 * Saves the history of this control.
	 */
	public void saveHistory() {
		fExternalLocationControl.saveHistory();
	}

	/**
	 * Sets the refactoring script location to choose.
	 * 
	 * @param uri
	 *            the refactoring script location, or <code>null</code> if no
	 *            refactoring script has been chosen, or the refactoring script
	 *            should be taken from the clipboard
	 */
	public void setRefactoringScript(final URI uri) {
		if (fExternalLocationControl != null)
			fExternalLocationControl.setEnabled(true);
		if (fExternalBrowseButton != null)
			fExternalBrowseButton.setEnabled(true);
		if (uri == null)
			fExternalLocationControl.setText(""); //$NON-NLS-1$
		else {
			try {
				final String path= new File(uri).getCanonicalPath();
				if (path != null && !"".equals(path)) //$NON-NLS-1$
					fExternalLocationControl.setText(path);
				handleExternalLocationChanged();
			} catch (IOException exception) {
				RefactoringUIPlugin.log(exception);
			}
		}
	}
}