/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.preferences;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/*
 * The page to configure the code formatter options.
 */
public class AntCodeFormatterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private OverlayPreferenceStore fOverlayStore;
	
	private Map fCheckBoxes= new HashMap();
	private SelectionListener fCheckBoxListener= new SelectionListener() {
		public void widgetDefaultSelected(SelectionEvent e) {
		}
		public void widgetSelected(SelectionEvent e) {
			Button button= (Button) e.widget;
			fOverlayStore.setValue((String) fCheckBoxes.get(button), button.getSelection());
		}
	};
	
	private Map fTextFields= new HashMap();
	private ModifyListener fTextFieldListener= new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			Text text= (Text) e.widget;
			fOverlayStore.setValue((String) fTextFields.get(text), text.getText());
		}
	};

	private Map fNumberFields= new HashMap();
	private ModifyListener fNumberFieldListener= new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			numberFieldChanged((Text) e.widget);
		}
	};
	
	public AntCodeFormatterPreferencePage() {
		setPreferenceStore(AntUIPlugin.getDefault().getPreferenceStore());
		fOverlayStore= createOverlayStore();
	}
	
	private OverlayPreferenceStore createOverlayStore() {
		List overlayKeys= new ArrayList();
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AntEditorPreferenceConstants.FORMATTER_WRAP_LONG));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AntEditorPreferenceConstants.FORMATTER_ALIGN));				
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AntEditorPreferenceConstants.FORMATTER_MAX_LINE_LENGTH));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AntEditorPreferenceConstants.FORMATTER_TAB_CHAR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AntEditorPreferenceConstants.FORMATTER_TAB_SIZE));
		
		OverlayPreferenceStore.OverlayKey[] keys= new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return new OverlayPreferenceStore(getPreferenceStore(), keys);
	}
	
	private Button addCheckBox(Composite parent, String labelText, String key, int indentation) {		
		Button checkBox= new Button(parent, SWT.CHECK);
		checkBox.setText(labelText);
		checkBox.setFont(parent.getFont());
		
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= indentation;
		gd.horizontalSpan= 2;
		checkBox.setLayoutData(gd);
		checkBox.addSelectionListener(fCheckBoxListener);
		
		fCheckBoxes.put(checkBox, key);
		
		return checkBox;
	}
	
	/*
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		//TODO set help
		//WorkbenchHelp.setHelp(getControl(), "ANT_FORMATTER_PREFERENCE_PAGE"); //$NON-NLS-1$
	}

	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);
		fOverlayStore.load();
		fOverlayStore.start();
		int numColumns= 2;
		Composite result= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth= 0;
		layout.verticalSpacing= convertVerticalDLUsToPixels(10);
		layout.horizontalSpacing= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		result.setLayout(layout);
		
		Group indentationGroup= createGroup(numColumns, result, AntPreferencesMessages.getString("AntCodeFormatterPreferencePage.0")); //$NON-NLS-1$
		
		String labelText= AntPreferencesMessages.getString("AntCodeFormatterPreferencePage.1"); //$NON-NLS-1$
		String[] errorMessages= new String[]{AntPreferencesMessages.getString("AntCodeFormatterPreferencePage.2"), AntPreferencesMessages.getString("AntCodeFormatterPreferencePage.3")}; //$NON-NLS-1$ //$NON-NLS-2$
		addTextField(indentationGroup, labelText, AntEditorPreferenceConstants.FORMATTER_TAB_SIZE, 3, 0, errorMessages);
		
		labelText= AntPreferencesMessages.getString("AntCodeFormatterPreferencePage.4"); //$NON-NLS-1$
		addCheckBox(indentationGroup, labelText, AntEditorPreferenceConstants.FORMATTER_TAB_CHAR, 1);
		
		Group wrappingGroup= createGroup(numColumns, result, AntPreferencesMessages.getString("AntCodeFormatterPreferencePage.6")); //$NON-NLS-1$
		labelText= AntPreferencesMessages.getString("AntCodeFormatterPreferencePage.7"); //$NON-NLS-1$
		errorMessages= new String[]{AntPreferencesMessages.getString("AntCodeFormatterPreferencePage.8"), AntPreferencesMessages.getString("AntCodeFormatterPreferencePage.9")}; //$NON-NLS-1$ //$NON-NLS-2$
		addTextField(wrappingGroup, labelText, AntEditorPreferenceConstants.FORMATTER_MAX_LINE_LENGTH, 3, 0, errorMessages);
		labelText= AntPreferencesMessages.getString("AntCodeFormatterPreferencePage.10"); //$NON-NLS-1$
		addCheckBox(wrappingGroup, labelText, AntEditorPreferenceConstants.FORMATTER_WRAP_LONG, 1);
		labelText= AntPreferencesMessages.getString("AntCodeFormatterPreferencePage.5"); //$NON-NLS-1$
		addCheckBox(wrappingGroup, labelText, AntEditorPreferenceConstants.FORMATTER_ALIGN, 1);
		
		initializeFields();
		
		applyDialogFont(result);
	
		return result;
	}
	
	private Control addTextField(Composite composite, String labelText, String key, int textLimit, int indentation, String[] errorMessages) {
		Font font= composite.getFont();
		
		Label label= new Label(composite, SWT.NONE);
		label.setText(labelText);
		label.setFont(font);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= indentation;
		label.setLayoutData(gd);
		
		Text textControl= new Text(composite, SWT.BORDER | SWT.SINGLE);
		textControl.setFont(font);		
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint= convertWidthInCharsToPixels(textLimit + 1);
		textControl.setLayoutData(gd);
		textControl.setTextLimit(textLimit);
		fTextFields.put(textControl, key);
		if (errorMessages != null) {
			fNumberFields.put(textControl, errorMessages);
			textControl.addModifyListener(fNumberFieldListener);
		} else {
			textControl.addModifyListener(fTextFieldListener);
		}
			
		return textControl;
	}
	
 
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	/**
	 * Convenience method to create a GridData.
	 */
	private static GridData createGridData(int numColumns, int style, int widthHint) {
		final GridData gd= new GridData(style);
		gd.horizontalSpan= numColumns;
		gd.widthHint= widthHint;
		return gd;		
	}
	
	/**
	 * Convenience method to create a group
	 */
	private Group createGroup(int numColumns, Composite parent, String text ) {
		final Group group= new Group(parent, SWT.NONE);
		group.setLayoutData(createGridData(numColumns, GridData.FILL_HORIZONTAL, 0));
		
		final GridLayout layout= new GridLayout(numColumns, false);
		layout.verticalSpacing=  convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.marginHeight= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		
		group.setLayout(layout);//createGridLayout(numColumns, true));
		group.setText(text);
		return group;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		
		fOverlayStore.loadDefaults();

		initializeFields();

		super.performDefaults();
	}
	
	private void initializeFields() {
		
		Iterator iter= fCheckBoxes.keySet().iterator();
		while (iter.hasNext()) {
			Button b= (Button) iter.next();
			String key= (String) fCheckBoxes.get(b);
			b.setSelection(fOverlayStore.getBoolean(key));
		}
		
		iter= fTextFields.keySet().iterator();
		while (iter.hasNext()) {
			Text t= (Text) iter.next();
			String key= (String) fTextFields.get(t);
			t.setText(fOverlayStore.getString(key));
		}		
	}
	
	private void numberFieldChanged(Text textControl) {
		String number= textControl.getText();
		IStatus status= validatePositiveNumber(number, (String[])fNumberFields.get(textControl));
		if (!status.matches(IStatus.ERROR)) {
			fOverlayStore.setValue((String) fTextFields.get(textControl), number);
		}
		updateStatus(status);
	}
	
	private IStatus validatePositiveNumber(String number, String[] errorMessages) {
		StatusInfo status= new StatusInfo();
		if (number.length() == 0) {
			status.setError(errorMessages[0]);
		} else {
			try {
				int value= Integer.parseInt(number);
				if (value < 0)
					status.setError(MessageFormat.format(errorMessages[1], new String[]{number})); //$NON-NLS-1$
			} catch (NumberFormatException e) {
				status.setError(MessageFormat.format(errorMessages[1], new String[]{number})); //$NON-NLS-1$
			}
		}
		return status;
	}
	
	private void updateStatus(IStatus status) {
		if (!status.matches(IStatus.ERROR)) {
			Set keys= fNumberFields.keySet();
			for (Iterator iter = keys.iterator(); iter.hasNext();) {
				Text text = (Text) iter.next();
				IStatus s= validatePositiveNumber(text.getText(), (String[])fNumberFields.get(text));
				status= s.getSeverity() > status.getSeverity() ? s : status;
			}
		}	
		setValid(!status.matches(IStatus.ERROR));
		applyToStatusLine(this, status);
	}
	
	/*
	 * Applies the status to the status line of a dialog page.
	 */
	private void applyToStatusLine(DialogPage page, IStatus status) {
		String message= status.getMessage();
		switch (status.getSeverity()) {
			case IStatus.OK:
				page.setMessage(message, IMessageProvider.NONE);
				page.setErrorMessage(null);
				break;
			case IStatus.WARNING:
				page.setMessage(message, IMessageProvider.WARNING);
				page.setErrorMessage(null);
				break;				
			case IStatus.INFO:
				page.setMessage(message, IMessageProvider.INFORMATION);
				page.setErrorMessage(null);
				break;			
			default:
				if (message.length() == 0) {
					message= null;
				}
				page.setMessage(null);
				page.setErrorMessage(message);
				break;		
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		fOverlayStore.propagate();
		AntUIPlugin.getDefault().savePluginPreferences();
		return true;
	}
}