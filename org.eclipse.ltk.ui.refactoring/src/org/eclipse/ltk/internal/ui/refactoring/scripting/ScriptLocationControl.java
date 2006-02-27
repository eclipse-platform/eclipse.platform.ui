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

import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.internal.ui.refactoring.util.SWTUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;

/**
 * Control to specify the location of a refactoring script.
 * 
 * @since 3.2
 */
public class ScriptLocationControl extends Composite {

	/** The clipboard script, or <code>null</code> */
	private String fCliboardScript= null;

	/** The external browse button */
	private Button fExternalBrowseButton= null;

	/** The external location text field */
	private Text fExternalLocationField= null;

	/** The from clipboard button */
	private Button fFromClipboardButton= null;

	/** The from external location button */
	private Button fFromExternalLocationButton= null;

	/** The script location, or <code>null</code> */
	private URI fScriptLocation= null;

	/**
	 * Creates a new script location control.
	 * 
	 * @param parent
	 *            the parent control
	 */
	public ScriptLocationControl(final Composite parent) {
		super(parent, SWT.NONE);
		setLayoutData(createGridData(GridData.FILL_HORIZONTAL, 6, 0));
		setLayout(new GridLayout(3, false));
		fFromClipboardButton= new Button(this, SWT.RADIO);
		fFromClipboardButton.setText(ScriptingMessages.ScriptLocationControl_clipboard_label);
		fFromClipboardButton.setLayoutData(createGridData(GridData.HORIZONTAL_ALIGN_BEGINNING, 3, 0));
		fFromClipboardButton.addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				final boolean selection= fFromClipboardButton.getSelection();
				fExternalLocationField.setEnabled(!selection);
				fExternalBrowseButton.setEnabled(!selection);
				handleClipboardScriptChanged();
			}
		});
		fFromExternalLocationButton= new Button(this, SWT.RADIO);
		fFromExternalLocationButton.setText(ScriptingMessages.ScriptLocationControl_location_label);
		fFromExternalLocationButton.setLayoutData(createGridData(GridData.HORIZONTAL_ALIGN_BEGINNING, 1, 0));
		fFromExternalLocationButton.setSelection(true);
		fFromExternalLocationButton.addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				handleExternalLocationChanged();
			}
		});
		fExternalLocationField= new Text(this, SWT.SINGLE | SWT.BORDER);
		fExternalLocationField.setLayoutData(createGridData(GridData.FILL_HORIZONTAL, 1, 0));
		fExternalLocationField.addModifyListener(new ModifyListener() {

			public final void modifyText(final ModifyEvent event) {
				handleExternalLocationChanged();
			}
		});
		fExternalLocationField.setFocus();
		fExternalBrowseButton= new Button(this, SWT.PUSH);
		fExternalBrowseButton.setText(ScriptingMessages.ScriptLocationControl_browse_label);
		fExternalBrowseButton.setLayoutData(createGridData(GridData.HORIZONTAL_ALIGN_FILL, 1, 0));
		SWTUtil.setButtonDimensionHint(fExternalBrowseButton);
		fExternalBrowseButton.addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				handleBrowseExternalLocation();
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
	private GridData createGridData(final int flag, final int hspan, final int indent) {
		final GridData data= new GridData(flag);
		data.horizontalIndent= indent;
		data.horizontalSpan= hspan;
		return data;
	}

	/**
	 * Returns the refactoring script from the clipboard.
	 * 
	 * @return the refactoring script from the clipboard, or <code>null</code>
	 *         if the clipboard is empty or does not contain a refactoring
	 *         script
	 */
	public String getClipboardScript() {
		return fCliboardScript;
	}

	/**
	 * Returns the chosen refactoring script location.
	 * 
	 * @return the refactoring script location, or <code>null</code> if no
	 *         refactoring script has been chosen, or the refactoring script
	 *         should be taken from the clipboard
	 */
	public URI getScriptLocation() {
		return fScriptLocation;
	}

	/**
	 * Handles the browse external location event.
	 */
	private void handleBrowseExternalLocation() {
		final FileDialog file= new FileDialog(getShell(), SWT.OPEN);
		file.setText(ScriptingMessages.ScriptLocationControl_browse_caption);
		file.setFilterNames(new String[] { ScriptingMessages.ScriptLocationControl_filter_name_script, ScriptingMessages.ScriptLocationControl_filter_name_wildcard});
		file.setFilterExtensions(new String[] { ScriptingMessages.ScriptLocationControl_filter_extension_script, ScriptingMessages.ScriptLocationControl_filter_extension_wildcard});
		final String path= file.open();
		if (path != null)
			fExternalLocationField.setText(path);
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
		final String text= fExternalLocationField.getText();
		if (text != null && !"".equals(text)) //$NON-NLS-1$
			fScriptLocation= new File(text).toURI();
		else
			fScriptLocation= null;
	}

	/**
	 * Sets the refactoring script location to choose.
	 * 
	 * @param uri
	 *            the refactoring script location, or <code>null</code> if no
	 *            refactoring script has been chosen, or the refactoring script
	 *            should be taken from the clipboard
	 */
	public void setScriptLocation(final URI uri) {
		if (fExternalLocationField != null)
			fExternalLocationField.setEnabled(true);
		if (fExternalBrowseButton != null)
			fExternalBrowseButton.setEnabled(true);
		if (uri == null)
			fExternalLocationField.setText(""); //$NON-NLS-1$
		else {
			try {
				final String path= new File(uri).getCanonicalPath();
				if (path != null && !"".equals(path)) //$NON-NLS-1$
					fExternalLocationField.setText(path);
				handleExternalLocationChanged();
			} catch (IOException exception) {
				RefactoringUIPlugin.log(exception);
			}
		}
	}
}