package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ExtMethodPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	Text cvsRsh;
	Text cvsServer;	

	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData());
		
		Label intro = new Label(composite, SWT.LEFT);
		intro.setText(Policy.bind("ExtMethodPreferencePage_message")); //$NON-NLS-1$
		GridData data = new GridData();
		data.horizontalSpan = 3;
		data.horizontalAlignment = GridData.FILL;
		intro.setLayoutData(data);
		
		new Label(composite, SWT.NULL); new Label(composite, SWT.NULL); new Label(composite, SWT.NULL); // spacer
		
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
		
		
		Label l = new Label(composite, SWT.LEFT | SWT.BOLD);
		l.setText(Policy.bind("ExtMethodPreferencePage_Note__5")); //$NON-NLS-1$
		l.setFont(JFaceResources.getBannerFont());
		
		l = new Label(composite, SWT.LEFT);
		l.setText(Policy.bind("ExtMethodPreferencePage_note_CVS_RSH")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		l.setLayoutData(data);
		
		new Label(composite, SWT.NULL); new Label(composite, SWT.NULL); new Label(composite, SWT.NULL); // spacer
		
		new Label(composite, SWT.LEFT).setText(Policy.bind("ExtMethodPreferencePage_CVS_SERVER__7")); //$NON-NLS-1$
		cvsServer = new Text(composite, SWT.BORDER);
		data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		cvsServer.setLayoutData(data);

		l = new Label(composite, SWT.LEFT | SWT.BOLD);
		l.setText(Policy.bind("ExtMethodPreferencePage_Note__8")); //$NON-NLS-1$
		l.setFont(JFaceResources.getBannerFont());
		
		l = new Label(composite, SWT.LEFT);
		l.setText(Policy.bind("ExtMethodPreferencePage_NoteForCVS_SERVER")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		l.setLayoutData(data);

		
		initializeDefaults();
				
		return composite;
	}
	
	protected void initializeDefaults() {
		IPreferenceStore store = getPreferenceStore();
		cvsRsh.setText(store.getString(ICVSUIConstants.PREF_CVS_RSH));
		cvsServer.setText(store.getString(ICVSUIConstants.PREF_CVS_SERVER));
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
		store.setValue(ICVSUIConstants.PREF_CVS_RSH, cvsRsh.getText());
		store.setValue(ICVSUIConstants.PREF_CVS_SERVER, cvsServer.getText());
		CVSProviderPlugin.getPlugin().setCvsRshCommand(cvsRsh.getText());
		CVSProviderPlugin.getPlugin().setCvsServer(cvsServer.getText());
		return super.performOk();
	}
	/*
	 * @see PreferencePage#doGetPreferenceStore()
	 */
	protected IPreferenceStore doGetPreferenceStore() {
		return CVSUIPlugin.getPlugin().getPreferenceStore();
	}
}
