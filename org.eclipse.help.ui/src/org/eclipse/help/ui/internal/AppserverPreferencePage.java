/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.help.internal.appserver.AppserverPlugin;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.widgets.FormText;

/**
 * Preference page for Tomcat network interface and port.
 */
public class AppserverPreferencePage extends PreferencePage
		implements
			IWorkbenchPreferencePage {
	protected Text textServerAddr;
	protected Text textServerPort;
	/**
	 * Creates preference page controls on demand.
	 * 
	 * @param parent
	 *            the parent for the preference page
	 */
	protected Control createContents(Composite parent) {
		Font font = parent.getFont();

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IHelpUIConstants.PREF_PAGE_APPSERVER);

		final Composite mainComposite = new Composite(parent, SWT.NULL);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		final GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		mainComposite.setLayout(layout);

		final Label descLabel = new Label(mainComposite, SWT.WRAP);
		descLabel.setText(Messages.AppserverPreferencePage_description); //$NON-NLS-1$
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.widthHint = 100;
		data.heightHint = 20;
		descLabel.setLayoutData(data);
		descLabel.setFont(font);

		// Spacer
		Label label = new Label(mainComposite, SWT.NONE);
		data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		label.setFont(font);

		label = new Label(mainComposite, SWT.NONE);
		label.setFont(font);
		label.setText(Messages.AppserverPreferencePage_hostDescription); //$NON-NLS-1$

		textServerAddr = new Text(mainComposite, SWT.SINGLE | SWT.BORDER);
		//text.addListener(SWT.Modify, this);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = convertWidthInCharsToPixels(8);
		textServerAddr.setLayoutData(data);
		textServerAddr.setFont(font);

		label = new Label(mainComposite, SWT.NONE);
		label.setFont(font);
		label.setText(Messages.AppserverPreferencePage_portDescription); //$NON-NLS-1$

		textServerPort = new Text(mainComposite, SWT.SINGLE | SWT.BORDER);
		textServerPort.setTextLimit(5);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = convertWidthInCharsToPixels(8);
		textServerPort.setLayoutData(data);
		textServerPort.setFont(font);

		// Validation of port field
		textServerPort.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (textServerPort.getText().length() == 0) {
					AppserverPreferencePage.this.setValid(true);
					setErrorMessage(null);
					return;
				}
				try {
					int num = Integer.valueOf(textServerPort.getText())
							.intValue();
					if (0 <= num && num <= 0xFFFF) {
						// port is valid
						AppserverPreferencePage.this.setValid(true);
						setErrorMessage(null);
						return;
					}

					// port is invalid
				} catch (NumberFormatException nfe) {
				}
				AppserverPreferencePage.this.setValid(false);
				setErrorMessage(Messages.AppserverPreferencePage_invalidPort); //$NON-NLS-1$
			}
		});

		// Spacer
		label = new Label(mainComposite, SWT.NONE);
		data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		label.setFont(font);
	
		FormText ftext = new FormText(mainComposite, SWT.WRAP);
		ftext.setText(Messages.AppserverPreferencePage_requireRestart, true, false);
		data = new GridData(GridData.GRAB_HORIZONTAL);
		ftext.setHyperlinkSettings(new HyperlinkSettings(mainComposite.getDisplay()));
		data.horizontalSpan = 2;
		data.widthHint = 100;
		data.heightHint = 20;
		ftext.setLayoutData(data);
		ftext.setFont(font);
		final GridData fdata = data;
		mainComposite.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				GridData ddata = (GridData)descLabel.getLayoutData();
				int width = mainComposite.getSize().x-layout.marginWidth*2;
				fdata.widthHint = width;
				fdata.heightHint = SWT.DEFAULT;
				ddata.widthHint = width;
				ddata.heightHint = SWT.DEFAULT;
				mainComposite.layout();
			}
		});

		Preferences pref = AppserverPlugin.getDefault().getPluginPreferences();
		textServerAddr.setText(pref.getString(AppserverPlugin.HOST_KEY));
		textServerPort.setText(pref.getString(AppserverPlugin.PORT_KEY));

		return mainComposite;
	}
	/**
	 * @see IWorkbenchPreferencePage
	 */
	public void init(IWorkbench workbench) {
	}
	/**
	 * Performs special processing when this page's Defaults button has been
	 * pressed.
	 * <p>
	 * This is a framework hook method for sublcasses to do special things when
	 * the Defaults button has been pressed. Subclasses may override, but should
	 * call <code>super.performDefaults</code>.
	 * </p>
	 */
	protected void performDefaults() {
		Preferences pref = AppserverPlugin.getDefault().getPluginPreferences();
		textServerAddr.setText(pref.getDefaultString(AppserverPlugin.HOST_KEY));
		textServerPort.setText(pref.getDefaultString(AppserverPlugin.PORT_KEY));
		super.performDefaults();
	}
	/**
	 * @see IPreferencePage
	 */
	public boolean performOk() {
		Preferences pref = AppserverPlugin.getDefault().getPluginPreferences();
		pref.setValue(AppserverPlugin.HOST_KEY, textServerAddr.getText());
		pref.setValue(AppserverPlugin.PORT_KEY, textServerPort.getText());
		AppserverPlugin.getDefault().savePluginPreferences();
		return true;
	}

}
