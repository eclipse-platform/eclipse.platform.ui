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
package org.eclipse.team.internal.ccvs.ui;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.IConnectionMethod;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

public class ExtMethodPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	Text cvsRsh;
	Text cvsServer;	
	Text cvsRshParameters;
	private Button useExternal;
	private Button useInternal;
	private Combo methodType;
	private Control internal, external;

	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData());
		
		SelectionAdapter selectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateEnablements();
			}
		};
		
		useExternal = createRadioButton(composite, Policy.bind("ExtMethodPreferencePage.0"), 1); //$NON-NLS-1$
		useExternal.addSelectionListener(selectionListener);
		external = createExternalArea(composite);
		
		useInternal = createRadioButton(composite, Policy.bind("ExtMethodPreferencePage.1"), 1); //$NON-NLS-1$
		useInternal.addSelectionListener(selectionListener);
		internal = createInternalArea(composite);
		
		initializeDefaults();
		WorkbenchHelp.setHelp(composite, IHelpContextIds.EXT_PREFERENCE_PAGE);
		Dialog.applyDialogFont(parent);
		return composite;
	}
	
	private void updateEnablements() {
		external.setEnabled(useExternal.getSelection());
		cvsRsh.setEnabled(useExternal.getSelection());
		cvsRshParameters.setEnabled(useExternal.getSelection());
		cvsServer.setEnabled(useExternal.getSelection());
		internal.setEnabled(!useExternal.getSelection());
		methodType.setEnabled(!useExternal.getSelection());
	}

	private Control createInternalArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData());
		
		Label intro = new Label(composite, SWT.LEFT | SWT.WRAP);
		intro.setText(Policy.bind("ExtMethodPreferencePage.0")); //$NON-NLS-1$
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		data.widthHint = 300;
		intro.setLayoutData(data);
		
		createLabel(composite, Policy.bind("CVSPropertiesPage.connectionType"), 1); //$NON-NLS-1$
		methodType = createCombo(composite);
		return composite;
	}
	protected Control createExternalArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 3;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData());
		
		Label intro = new Label(composite, SWT.LEFT | SWT.WRAP);
		intro.setText(Policy.bind("ExtMethodPreferencePage_message")); //$NON-NLS-1$
		GridData data = new GridData();
		data.horizontalSpan = 3;
		data.horizontalAlignment = GridData.FILL;
		data.widthHint = 300;
		intro.setLayoutData(data);
		
		new Label(composite, SWT.LEFT).setText(Policy.bind("ExtMethodPreferencePage_CVS_RSH")); //$NON-NLS-1$
		cvsRsh = new Text(composite, SWT.BORDER);
		cvsRsh.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		final Button b = new Button(composite, SWT.NONE);
		b.setText(Policy.bind("ExtMethodPreferencePage_Browse")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, b.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		b.setLayoutData(data);
		b.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent (Event event) {
				FileDialog d = new FileDialog(getShell());
				d.setText(Policy.bind("ExtMethodPreferencePage_Details")); //$NON-NLS-1$
				String file = d.open();
				if(file!=null) {
					setCvsRshText(file);
				}
			}			
		});
		
		new Label(composite, SWT.LEFT).setText(Policy.bind("ExtMethodPreferencePage_CVS_RSH_Parameters")); //$NON-NLS-1$
		cvsRshParameters = new Text(composite, SWT.BORDER);
		data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		cvsRshParameters.setLayoutData(data);

		new Label(composite, SWT.LEFT).setText(Policy.bind("ExtMethodPreferencePage_CVS_SERVER__7")); //$NON-NLS-1$
		cvsServer = new Text(composite, SWT.BORDER);
		data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		cvsServer.setLayoutData(data);
		
		WorkbenchHelp.setHelp(cvsRsh, IHelpContextIds.EXT_PREFERENCE_RSH);
		WorkbenchHelp.setHelp(cvsRshParameters, IHelpContextIds.EXT_PREFERENCE_PARAM);
		WorkbenchHelp.setHelp(cvsServer, IHelpContextIds.EXT_PREFERENCE_SERVER);
		return composite;
	}
	
	private void initializeDefaults() {
		IPreferenceStore store = getPreferenceStore();
		cvsRsh.setText(store.getString(ICVSUIConstants.PREF_CVS_RSH));
		cvsRshParameters.setText(store.getString(ICVSUIConstants.PREF_CVS_RSH_PARAMETERS));
		cvsServer.setText(store.getString(ICVSUIConstants.PREF_CVS_SERVER));
		
		IConnectionMethod[] methods = CVSRepositoryLocation.getPluggedInConnectionMethods();
		for (int i = 0; i < methods.length; i++) {
			String name = methods[i].getName();
			if (!name.equals("ext")) { //$NON-NLS-1$
				methodType.add(name);
			}
		}
		String method = store.getString(ICVSUIConstants.PREF_EXT_CONNECTION_METHOD_PROXY);
		if (method.equals("ext")) { //$NON-NLS-1$
			methodType.select(0);
		} else {
			methodType.select(methodType.indexOf(method));
		}
		useExternal.setSelection(method.equals("ext")); //$NON-NLS-1$
		useInternal.setSelection(!method.equals("ext")); //$NON-NLS-1$
		updateEnablements();
	}
	
	/*
	 * Set CVS_RSH program
	 */
	 protected void setCvsRshText(String s) {
	 	cvsRsh.setText(s);
	 }
	
	/*
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	/*
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();
		String method;
		if (useExternal.getSelection()) {
			method = "ext"; //$NON-NLS-1$
			store.setValue(ICVSUIConstants.PREF_CVS_RSH, cvsRsh.getText());
			store.setValue(ICVSUIConstants.PREF_CVS_RSH_PARAMETERS, cvsRshParameters.getText());
			store.setValue(ICVSUIConstants.PREF_CVS_SERVER, cvsServer.getText());
			CVSProviderPlugin.getPlugin().setCvsRshCommand(cvsRsh.getText());
			CVSProviderPlugin.getPlugin().setCvsRshParameters(cvsRshParameters.getText());
			CVSProviderPlugin.getPlugin().setCvsServer(cvsServer.getText());
		} else {
			method = methodType.getText();
		}
		store.setValue(ICVSUIConstants.PREF_EXT_CONNECTION_METHOD_PROXY, method);
		CVSRepositoryLocation.setExtConnectionMethodProxy(method);
		CVSUIPlugin.getPlugin().savePluginPreferences();
		return super.performOk();
	}
	/*
	 * @see PreferencePage#doGetPreferenceStore()
	 */
	protected IPreferenceStore doGetPreferenceStore() {
		return CVSUIPlugin.getPlugin().getPreferenceStore();
	}
	
	protected Button createRadioButton(Composite parent, String label, int span) {
		Button button = new Button(parent, SWT.RADIO);
		button.setText(label);
		GridData data = new GridData();
		data.horizontalSpan = span;
		button.setLayoutData(data);
		return button;
	}
	
	protected Label createLabel(Composite parent, String text, int span) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = span;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}
	protected Combo createCombo(Composite parent) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.horizontalSpan = 1;
		combo.setLayoutData(data);
		return combo;
	}
}
