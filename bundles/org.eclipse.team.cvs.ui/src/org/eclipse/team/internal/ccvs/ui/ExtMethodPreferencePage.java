package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

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
		intro.setText("These variables define the external connection program to use with the 'ext' connection method.\nThese values should be the same as the 'ext' CVS command-line environment variable settings.");
		GridData data = new GridData();
		data.horizontalSpan = 3;
		data.horizontalAlignment = GridData.FILL;
		intro.setLayoutData(data);
		
		new Label(composite, SWT.NULL); new Label(composite, SWT.NULL); new Label(composite, SWT.NULL); // spacer
		
		new Label(composite, SWT.LEFT).setText("CVS_RSH:");
		cvsRsh = new Text(composite, SWT.BORDER);
		cvsRsh.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		final Button b = new Button(composite, SWT.NONE);
		b.setText("Browse...");
		b.setLayoutData(new GridData());
		b.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent (Event event) {
				FileDialog d = new FileDialog(getShell());
				d.setText("Select a program or script");
				String file = d.open();
				if(file!=null) {
					setCvsRshText(file);
				}
			}			
		});
		
		
		Label l = new Label(composite, SWT.LEFT | SWT.BOLD);
		l.setText("Note:");
		l.setFont(JFaceResources.getBannerFont());
		
		l = new Label(composite, SWT.LEFT);
		l.setText("The RSH command must fit the following calling pattern:\n<CVS_RSH> -l <USERNAME> <HOST> <CVS_SERVER>\nThis program will be called to connect to the remote CVS server.");
		data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		l.setLayoutData(data);
		
		new Label(composite, SWT.NULL); new Label(composite, SWT.NULL); new Label(composite, SWT.NULL); // spacer
		
		new Label(composite, SWT.LEFT).setText("CVS_SERVER:");
		cvsServer = new Text(composite, SWT.BORDER);
		data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		cvsServer.setLayoutData(data);

		l = new Label(composite, SWT.LEFT | SWT.BOLD);
		l.setText("Note:");
		l.setFont(JFaceResources.getBannerFont());
		
		l = new Label(composite, SWT.LEFT);
		l.setText("This is the name of the remote CVS server program.\nChange this setting only if the remote CVS server name\nis different than the default.");
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
