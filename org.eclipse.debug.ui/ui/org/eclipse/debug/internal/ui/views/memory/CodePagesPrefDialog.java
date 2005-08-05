/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory;

import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;


/**
 * Dialog to allow user to change code page preference
 * @since 3.1
 */
public class CodePagesPrefDialog extends Dialog {

	private Text fAsciiCodePage;
	private Text fEbcdicCodePage;
	
	/**
	 * @param parentShell
	 */
	public CodePagesPrefDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parentShell, DebugUIPlugin.getUniqueIdentifier() + ".CodePagesPrefDialog_context"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
	
		getShell().setText(DebugUIMessages.CodePagesPrefDialog_1); 
		setShellStyle(SWT.RESIZE);
		
		Composite canvas = new Composite(parent, SWT.NONE);
		canvas.setLayout(new GridLayout(2, false));
		GridData spec2= new GridData();
		spec2.grabExcessVerticalSpace= true;
		spec2.grabExcessHorizontalSpace= true;
		spec2.horizontalAlignment= GridData.FILL;
		spec2.verticalAlignment= GridData.CENTER;
		canvas.setLayoutData(spec2);

		Label textLabel = new Label(canvas, SWT.WRAP);
		textLabel.setText(DebugUIMessages.CodePagesPrefDialog_2); 
		GridData textLayout = new GridData();
		textLayout.widthHint = 280;
		textLayout.horizontalSpan = 2;
		textLabel.setLayoutData(textLayout);
		
		fAsciiCodePage = new Text(canvas, SWT.BORDER);
		GridData asciispec= new GridData();
		asciispec.grabExcessVerticalSpace= false;
		asciispec.grabExcessHorizontalSpace= true;
		asciispec.horizontalAlignment= GridData.FILL;
		asciispec.verticalAlignment= GridData.BEGINNING;
		asciispec.horizontalSpan = 1;
		fAsciiCodePage.setLayoutData(asciispec);
		
		String codepage = DebugUITools.getPreferenceStore().getString(IDebugUIConstants.PREF_DEFAULT_ASCII_CODE_PAGE);
		if (codepage == null || codepage.length() == 0)
			codepage = IDebugPreferenceConstants.DEFAULT_ASCII_CP; 
		fAsciiCodePage.setText(codepage);
		
		Label ebcdicLabel = new Label(canvas, SWT.WRAP);
		ebcdicLabel.setText(DebugUIMessages.CodePagesPrefDialog_4); 
		GridData ebcdicLayout = new GridData();
		ebcdicLayout.widthHint = 280;
		ebcdicLayout.horizontalSpan = 2;
		ebcdicLabel.setLayoutData(ebcdicLayout);
		
		fEbcdicCodePage = new Text(canvas, SWT.BORDER);
		GridData ebcdicspec= new GridData();
		ebcdicspec.grabExcessVerticalSpace= false;
		ebcdicspec.grabExcessHorizontalSpace= true;
		ebcdicspec.horizontalAlignment= GridData.FILL;
		ebcdicspec.verticalAlignment= GridData.BEGINNING;
		ebcdicspec.horizontalSpan = 1;
		fAsciiCodePage.setLayoutData(ebcdicspec);		
		fEbcdicCodePage.setLayoutData(asciispec);

		codepage = DebugUITools.getPreferenceStore().getString(IDebugUIConstants.PREF_DEFAULT_EBCDIC_CODE_PAGE);
		fEbcdicCodePage.setText(codepage);
		
		if (codepage == null || codepage.length() == 0)
			codepage = IDebugPreferenceConstants.DEFAULT_EBCDIC_CP; //	
		
		return canvas;
	}
	protected void okPressed() {

		// check that the codepages are supported
		String asciiCodePage = fAsciiCodePage.getText();
		asciiCodePage = asciiCodePage.trim();
		try {
			new String(new byte[]{1}, asciiCodePage);
		} catch (UnsupportedEncodingException e) {
			Shell shell = DebugUIPlugin.getShell();
			if (shell != null)
			{
				IStatus status = DebugUIPlugin.newErrorStatus(DebugUIMessages.CodePagesPrefDialog_0, e); 
				ErrorDialog.openError(shell, DebugUIMessages.CodePagesPrefDialog_6,  DebugUIMessages.CodePagesPrefDialog_7, status);		 // 
			}
			return;
		}
		
		String ebcdicCodePage = fEbcdicCodePage.getText();
		ebcdicCodePage = ebcdicCodePage.trim();
		try {
			new String(new byte[]{1}, ebcdicCodePage);
		} catch (UnsupportedEncodingException e) {
			Shell shell = DebugUIPlugin.getShell();
			if (shell != null)
			{
				IStatus status = DebugUIPlugin.newErrorStatus(DebugUIMessages.CodePagesPrefDialog_0, e); 
				ErrorDialog.openError(shell, DebugUIMessages.CodePagesPrefDialog_8,  DebugUIMessages.CodePagesPrefDialog_9, status);		 // 
			}
			return;
		}
		
		IPreferenceStore store = DebugUITools.getPreferenceStore();
		store.setValue(IDebugUIConstants.PREF_DEFAULT_ASCII_CODE_PAGE, asciiCodePage);
		store.setValue(IDebugUIConstants.PREF_DEFAULT_EBCDIC_CODE_PAGE, ebcdicCodePage);
		
		super.okPressed();
	}
	
	/**
	 * @param shell
	 */
	
	protected void createButtonsForButtonBar(Composite parent) {
		Button defaultButton = createButton(parent, 3, DebugUIMessages.CodePagesPrefDialog_13, false); 
		defaultButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				String defaultASCII = IDebugPreferenceConstants.DEFAULT_ASCII_CP;
				fAsciiCodePage.setText(defaultASCII);
				String defaulgEBCDIC = IDebugPreferenceConstants.DEFAULT_EBCDIC_CP;
				fEbcdicCodePage.setText(defaulgEBCDIC);
			}});
		super.createButtonsForButtonBar(parent);
	}
}
