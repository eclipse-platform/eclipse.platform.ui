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

import java.nio.charset.Charset;
import java.util.Set;
import java.util.SortedMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.LabelProvider;
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
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.internal.help.WorkbenchHelpSystem;


/**
 * Dialog to allow user to change code page preference
 * @since 3.1
 */
public class CodePagesPrefDialog extends Dialog {

	private Text fAsciiCodePage;
	private Text fEbcdicCodePage;
	
	private static CharSetInfo[] fCodePages;
	
	private class CharSetInfo
	{
		String fCharSetName;
		String fDisplayName;
		
		public CharSetInfo(String charSetName, String displayName) {

			fCharSetName = charSetName;
			fDisplayName = displayName;
		}
		
		public String toString() {
			return fDisplayName;
		}
	}
	
	/**
	 * @param parentShell
	 */
	public CodePagesPrefDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		WorkbenchHelpSystem.getInstance().setHelp(parentShell, DebugUIPlugin.getUniqueIdentifier() + ".CodePagesPrefDialog_context"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
	
		getShell().setText(DebugUIMessages.getString("CodePagesPrefDialog.1")); //$NON-NLS-1$
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
		textLabel.setText(DebugUIMessages.getString("CodePagesPrefDialog.2")); //$NON-NLS-1$
		GridData textLayout = new GridData();
		textLayout.widthHint = 280;
		textLayout.horizontalSpan = 2;
		textLabel.setLayoutData(textLayout);
		
		fAsciiCodePage = new Text(canvas, SWT.READ_ONLY | SWT.BORDER);
		GridData asciispec= new GridData();
		asciispec.grabExcessVerticalSpace= false;
		asciispec.grabExcessHorizontalSpace= true;
		asciispec.horizontalAlignment= GridData.FILL;
		asciispec.verticalAlignment= GridData.BEGINNING;
		asciispec.horizontalSpan = 1;
		fAsciiCodePage.setLayoutData(asciispec);
		
		String codepage = DebugUITools.getPreferenceStore().getString(IDebugUIConstants.PREF_DEFAULT_ASCII_CODE_PAGE);
		if (codepage == null || codepage.length() == 0)
			codepage = "CP1252"; //$NON-NLS-1$
		fAsciiCodePage.setText(codepage);
		
		Button asciiButton = new Button(canvas, SWT.PUSH);
		asciiButton.setText(DebugUIMessages.getString("CodePagesPrefDialog.3")); //$NON-NLS-1$
		asciiButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				Shell shell = DebugUIPlugin.getShell();
				if (shell != null)
					openCodePageSelectionDialog(shell, fAsciiCodePage.getText(), fAsciiCodePage);
			}});		
		Label ebcdicLabel = new Label(canvas, SWT.WRAP);
		ebcdicLabel.setText(DebugUIMessages.getString("CodePagesPrefDialog.4")); //$NON-NLS-1$
		GridData ebcdicLayout = new GridData();
		ebcdicLayout.widthHint = 280;
		ebcdicLayout.horizontalSpan = 2;
		ebcdicLabel.setLayoutData(ebcdicLayout);
		
		fEbcdicCodePage = new Text(canvas, SWT.READ_ONLY | SWT.BORDER);
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
			codepage = "CP1047"; //$NON-NLS-1$
		
		Button ebcdicButon = new Button(canvas, SWT.PUSH);
		ebcdicButon.setText(DebugUIMessages.getString("CodePagesPrefDialog.5")); //$NON-NLS-1$
		
		ebcdicButon.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				Shell shell = DebugUIPlugin.getShell();
				if (shell != null)
					openCodePageSelectionDialog(shell, fEbcdicCodePage.getText(), fEbcdicCodePage);
			}});	
		
		return canvas;
	}
	protected void okPressed() {

		// check that the codepages are supported
		String asciiCodePage = fAsciiCodePage.getText();
		if (!Charset.isSupported(asciiCodePage))
		{
			Shell shell = DebugUIPlugin.getShell();
			if (shell != null)
			{
				IStatus status = DebugUIPlugin.newErrorStatus(DebugUIMessages.getString("CodePagesPrefDialog.0"), null); //$NON-NLS-1$
				ErrorDialog.openError(shell, DebugUIMessages.getString("CodePagesPrefDialog.6"),  DebugUIMessages.getString("CodePagesPrefDialog.7"), status);		 //$NON-NLS-1$ //$NON-NLS-2$
			}
			return;
		}
		
		String ebcdicCodePage = fEbcdicCodePage.getText();
		if (!Charset.isSupported(ebcdicCodePage))
		{
			Shell shell = DebugUIPlugin.getShell();
			if (shell != null)
			{
				IStatus status = DebugUIPlugin.newErrorStatus(DebugUIMessages.getString("CodePagesPrefDialog.0"), null); //$NON-NLS-1$
				ErrorDialog.openError(shell, DebugUIMessages.getString("CodePagesPrefDialog.8"), DebugUIMessages.getString("CodePagesPrefDialog.9"), status); //$NON-NLS-1$ //$NON-NLS-2$
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
	private void openCodePageSelectionDialog(Shell shell, String initialSelection, Text text) {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new LabelProvider(){});	
		dialog.setTitle(DebugUIMessages.getString("CodePagesPrefDialog.10")); //$NON-NLS-1$
		dialog.setMessage(DebugUIMessages.getString("CodePagesPrefDialog.11")); //$NON-NLS-1$
		dialog.setMultipleSelection(false);

		WorkbenchHelpSystem.getInstance().setHelp(shell, DebugUIPlugin.getUniqueIdentifier() + ".SelectCodepageDialog_context"); //$NON-NLS-1$
		
		if (fCodePages == null)
		{
			SortedMap map = Charset.availableCharsets();
			Set keys = map.keySet();
			Object[] charSetKeys = keys.toArray();
			
			fCodePages = new CharSetInfo[charSetKeys.length];
			
			for (int i=0; i<fCodePages.length; i++)
			{
				fCodePages[i] = new CharSetInfo((String)charSetKeys[i], Charset.forName((String)charSetKeys[i]).displayName());
			}
		}
		dialog.setElements(fCodePages);
		
		// find initial selection
		CharSetInfo selected = null;
		for (int i=0; i<fCodePages.length; i++){
			if (fCodePages[i].fCharSetName.equals(initialSelection))
			{
				selected = fCodePages[i];
				break;
			}
		}
		if (selected != null)
			dialog.setFilter(selected.fCharSetName);
		
		dialog.open();
		Object selection = dialog.getFirstResult();
		if (selection instanceof CharSetInfo)
			text.setText(((CharSetInfo)selection).fCharSetName);
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		Button defaultButton = createButton(parent, 3, DebugUIMessages.getString("CodePagesPrefDialog.13"), false); //$NON-NLS-1$
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
