package org.eclipse.help.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.help.ui.internal.util.*;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.tomcat.internal.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.*;

/**
 * Preference page for Tomcat network interface and port.
 */
public class TomcatPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage {
	private Text textServerAddr;
	private Text textServerPort;
	/**
	 * Creates preference page controls on demand.
	 *
	 * @param parent the parent for the preference page
	 */
	protected Control createContents(Composite parent) {
		WorkbenchHelp.setHelp(parent, IHelpUIConstants.PREF_PAGE_APPSERVER);

		Composite mainComposite = new Composite(parent, SWT.NULL);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		mainComposite.setLayout(layout);

		Label label = new Label(mainComposite, SWT.NONE);
		label.setText(
			WorkbenchResources.getString("TomcatPreferencePage.description"));
		GridData data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		// Spacer
		label = new Label(mainComposite, SWT.NONE);
		data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		label = new Label(mainComposite, SWT.NONE);
		label.setText(
			WorkbenchResources.getString(
				"TomcatPreferencePage.hostDescription"));
		data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		//Label labelHost = new Label(mainComposite, SWT.LEFT);
		//labelHost.setText(
		//	WorkbenchResources.getString("TomcatPreferencePage.host"));
		//data = new GridData();
		//labelHost.setLayoutData(data);

		textServerAddr = new Text(mainComposite, SWT.SINGLE | SWT.BORDER);
		//text.addListener(SWT.Modify, this);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		textServerAddr.setLayoutData(data);

		// Spacer
		label = new Label(mainComposite, SWT.NONE);
		data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		label = new Label(mainComposite, SWT.NONE);
		label.setText(
			WorkbenchResources.getString(
				"TomcatPreferencePage.portDescription"));
		data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		//Label labelPort = new Label(mainComposite, SWT.LEFT);
		//labelPort.setText(
		//	WorkbenchResources.getString("TomcatPreferencePage.port"));
		//data = new GridData();
		//labelPort.setLayoutData(data);

		textServerPort = new Text(mainComposite, SWT.SINGLE | SWT.BORDER);
		textServerPort.setTextLimit(5);
		data = new GridData();
		data.widthHint = convertWidthInCharsToPixels(8);
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		textServerPort.setLayoutData(data);

		// Spacer
		label = new Label(mainComposite, SWT.NONE);
		data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		label = new Label(mainComposite, SWT.NONE);
		label.setText(
			WorkbenchResources.getString("TomcatPreferencePage.Note"));
		label.setFont(JFaceResources.getBannerFont());
		data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		//data.horizontalSpan = 2;
		label.setLayoutData(data);

		label = new Label(mainComposite, SWT.NONE);
		label.setText(
			WorkbenchResources.getString(
				"TomcatPreferencePage.requireRestart"));
		data = new GridData();
		//data.horizontalSpan = 2;
		label.setLayoutData(data);

		Preferences pref = TomcatPlugin.getDefault().getPluginPreferences();
		textServerAddr.setText(pref.getString(TomcatPlugin.HOST_KEY));
		textServerPort.setText(pref.getString(TomcatPlugin.PORT_KEY));

		return mainComposite;
	}
	/**
	 * @see IWorkbenchPreferencePage
	 */
	public void init(IWorkbench workbench) {
	}
	/**
	 * Performs special processing when this page's Defaults button has been pressed.
	 * <p>
	 * This is a framework hook method for sublcasses to do special things when
	 * the Defaults button has been pressed.
	 * Subclasses may override, but should call <code>super.performDefaults</code>.
	 * </p>
	 */
	protected void performDefaults() {
		Preferences pref = TomcatPlugin.getDefault().getPluginPreferences();
		textServerAddr.setText(pref.getDefaultString(TomcatPlugin.HOST_KEY));
		textServerPort.setText(pref.getDefaultString(TomcatPlugin.PORT_KEY));
		super.performDefaults();
	}
	/**
	 * @see IPreferencePage
	 */
	public boolean performOk() {
		Preferences pref = TomcatPlugin.getDefault().getPluginPreferences();
		pref.setValue(TomcatPlugin.HOST_KEY, textServerAddr.getText());
		pref.setValue(TomcatPlugin.PORT_KEY, textServerPort.getText());
		TomcatPlugin.getDefault().savePluginPreferences();
		return true;
	}

}