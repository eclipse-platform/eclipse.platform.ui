/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.preferences;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.jface.dialogs.Dialog;

/**
 * Insert the type's description here.
 * @see PreferencePage
 */
public class MainPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {
	private static final String KEY_DESCRIPTION =
		"MainPreferencePage.description";
	private static final String PREFIX = UpdateUI.getPluginId();
	public static final String P_HISTORY_SIZE = PREFIX + ".historySize";
	public static final String P_CHECK_SIGNATURE = PREFIX + ".checkSignature";
	public static final String P_BROWSER = PREFIX + ".browser";
	public static final String EMBEDDED_VALUE = "embedded";
	private static final String SYSTEM_VALUE = "system";
	private static final String KEY_CHECK_SIGNATURE =
		"MainPreferencePage.checkSignature";
	private static final String KEY_HISTORY_SIZE =
		"MainPreferencePage.historySize";
	private static final String KEY_TOPIC_COLOR =
		"MainPreferencePage.topicColor";
	private static final String KEY_BROWSER_CHOICE =
		"MainPreferencePage.browserChoice";
	private static final String KEY_BROWSER_CHOICE_EMBEDDED =
		"MainPreferencePage.browserChoice.embedded";
	private static final String KEY_BROWSER_CHOICE_SYSTEM =
		"MainPreferencePage.browserChoice.system";

	public static final String P_UPDATE_VERSIONS = PREFIX + ".updateVersions";
	private static final String KEY_UPDATE_VERSIONS =
		"MainPreferencePage.updateVersions";
	private static final String KEY_UPDATE_VERSIONS_EQUIVALENT =
		"MainPreferencePage.updateVersions.equivalent";
	private static final String KEY_UPDATE_VERSIONS_COMPATIBLE =
		"MainPreferencePage.updateVersions.compatible";
	public static final String EQUIVALENT_VALUE = "equivalent";
	public static final String COMPATIBLE_VALUE = "compatible";

	private BooleanFieldEditor checkSignatureEditor;
	private Label httpProxyHostLabel;
	private Label httpProxyPortLabel;
	private Text httpProxyHostText;
	private Text httpProxyPortText;
	private Button enableHttpProxy;
	private static final String KEY_ENABLE_HTTP_PROXY =
		"MainPreferencePage.enableHttpProxy";
	private static final String KEY_HTTP_PROXY_SERVER =
		"MainPreferencePage.httpProxyHost";
	private static final String KEY_HTTP_PROXY_PORT =
		"MainPreferencePage.httpProxyPort";

	/**
	 * The constructor.
	 */
	public MainPreferencePage() {
		super(GRID);
		setPreferenceStore(UpdateUI.getDefault().getPreferenceStore());
		setDescription(UpdateUI.getString(KEY_DESCRIPTION));
	}

	/**
	 * Insert the method's description here.
	 * @see PreferencePage#init
	 */
	public void init(IWorkbench workbench) {
	}
	public void createFieldEditors() {
		WorkbenchHelp.setHelp(
			getFieldEditorParent(),
			"org.eclipse.update.ui.MainPreferencePage");
		IntegerFieldEditor maxLevel =
			new IntegerFieldEditor(
				P_HISTORY_SIZE,
				UpdateUI.getString(KEY_HISTORY_SIZE),
				getFieldEditorParent());
		maxLevel.setValidRange(1, Integer.MAX_VALUE);
		addField(maxLevel);
		checkSignatureEditor =
			new BooleanFieldEditor(
				P_CHECK_SIGNATURE,
				UpdateUI.getString(KEY_CHECK_SIGNATURE),
				getFieldEditorParent());
		addField(checkSignatureEditor);
		if ("win32".equals(SWT.getPlatform())) {
			RadioGroupFieldEditor browser =
				new RadioGroupFieldEditor(
					P_BROWSER,
					UpdateUI.getString(KEY_BROWSER_CHOICE),
					1,
					new String[][] {
						{
							UpdateUI.getString(KEY_BROWSER_CHOICE_EMBEDDED),
							EMBEDDED_VALUE },
						{
					UpdateUI.getString(KEY_BROWSER_CHOICE_SYSTEM),
						SYSTEM_VALUE }
			}, getFieldEditorParent(), true);
			addField(browser);
		}
		createSpacer(getFieldEditorParent(), 2);

		RadioGroupFieldEditor updateVersions =
			new RadioGroupFieldEditor(
				P_UPDATE_VERSIONS,
				UpdateUI.getString(KEY_UPDATE_VERSIONS),
				1,
				new String[][] {
					{
						UpdateUI.getString(KEY_UPDATE_VERSIONS_EQUIVALENT),
						EQUIVALENT_VALUE },
					{
				UpdateUI.getString(KEY_UPDATE_VERSIONS_COMPATIBLE),
					COMPATIBLE_VALUE }
		}, getFieldEditorParent(), true);
		addField(updateVersions);

		createSpacer(getFieldEditorParent(), 2);

		ColorFieldEditor topicColor =
			new ColorFieldEditor(
				UpdateColors.P_TOPIC_COLOR,
				UpdateUI.getString(KEY_TOPIC_COLOR),
				getFieldEditorParent());
		addField(topicColor);

		createSpacer(getFieldEditorParent(), 2);
		createHttpProxy(getFieldEditorParent(), 2);

	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		Dialog.applyDialogFont(getControl());
	}

	protected void createSpacer(Composite composite, int columnSpan) {
		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData(gd);
	}
	protected void createHttpProxy(Composite composite, int columnSpan) {
		Group group = new Group(composite, SWT.NONE);
		group.setText(UpdateUI.getString("MainPreferencePage.proxyGroup"));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		gd.horizontalAlignment = GridData.FILL;
		group.setLayoutData(gd);
		group.setFont(composite.getFont());

		enableHttpProxy = new Button(group, SWT.CHECK);
		enableHttpProxy.setText(UpdateUI.getString(KEY_ENABLE_HTTP_PROXY));
		gd = new GridData();
		gd.horizontalSpan = 2;
		enableHttpProxy.setLayoutData(gd);

		httpProxyHostLabel = new Label(group, SWT.NONE);
		httpProxyHostLabel.setText(UpdateUI.getString(KEY_HTTP_PROXY_SERVER));

		httpProxyHostText = new Text(group, SWT.SINGLE | SWT.BORDER);
		httpProxyHostText.setFont(group.getFont());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		httpProxyHostText.setLayoutData(gd);

		httpProxyPortLabel = new Label(group, SWT.NONE);
		httpProxyPortLabel.setText(UpdateUI.getString(KEY_HTTP_PROXY_PORT));

		httpProxyPortText = new Text(group, SWT.SINGLE | SWT.BORDER);
		httpProxyPortText.setFont(group.getFont());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		httpProxyPortText.setLayoutData(gd);

		performDefaults();

		enableHttpProxy.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				boolean enable = enableHttpProxy.getSelection();
				httpProxyPortLabel.setEnabled(enable);
				httpProxyHostLabel.setEnabled(enable);
				httpProxyPortText.setEnabled(enable);
				httpProxyHostText.setEnabled(enable);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	}
	private int getHistorySize() {
		IPreferenceStore store = UpdateUI.getDefault().getPreferenceStore();
		return store.getInt(P_HISTORY_SIZE);
	}

	public static boolean getCheckDigitalSignature() {
		IPreferenceStore store = UpdateUI.getDefault().getPreferenceStore();
		return store.getBoolean(P_CHECK_SIGNATURE);
	}

	public static boolean getUseEmbeddedBrowser() {
		IPreferenceStore store = UpdateUI.getDefault().getPreferenceStore();
		return store.getString(P_BROWSER).equals(EMBEDDED_VALUE);
	}

	public static String getUpdateVersionsMode() {
		IPreferenceStore store = UpdateUI.getDefault().getPreferenceStore();
		return store.getString(P_UPDATE_VERSIONS);
	}

	public boolean performOk() {
		boolean result = super.performOk();
		if (result) {
			BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
				public void run() {
					try {
						SiteManager.getLocalSite().setMaximumHistoryCount(
							getHistorySize());
						SiteManager.setHttpProxyInfo(
							enableHttpProxy.getSelection(),
							httpProxyHostText.getText(),
							httpProxyPortText.getText());
					} catch (CoreException e) {
						UpdateUI.logException(e);
					}
				}
			});
		}
		UpdateUI.getDefault().savePluginPreferences();
		return result;
	}
	public void performApply() {
		super.performApply();
		BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
			public void run() {
				SiteManager.setHttpProxyInfo(
					enableHttpProxy.getSelection(),
					httpProxyHostText.getText(),
					httpProxyPortText.getText());
			}
		});
	}
	public void performDefaults() {
		super.performDefaults();

		enableHttpProxy.setSelection(SiteManager.isHttpProxyEnable());
		String serverValue = SiteManager.getHttpProxyServer();
		if (serverValue != null)
			httpProxyHostText.setText(serverValue);
		String portValue = SiteManager.getHttpProxyPort();
		if (portValue != null)
			httpProxyPortText.setText(portValue);

		httpProxyPortLabel.setEnabled(enableHttpProxy.getSelection());
		httpProxyHostLabel.setEnabled(enableHttpProxy.getSelection());
		httpProxyPortText.setEnabled(enableHttpProxy.getSelection());
		httpProxyHostText.setEnabled(enableHttpProxy.getSelection());
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getSource().equals(checkSignatureEditor)) {
			if (event.getNewValue().equals(Boolean.FALSE)) {
				warnSignatureCheck(getShell());
			}
		}
	}	

	private void warnSignatureCheck(Shell shell) {
		MessageDialog.openWarning(
			shell,
			UpdateUI.getString("MainPreferencePage.digitalSignature.title"),
			UpdateUI.getString("MainPreferencePage.digitalSignature.message"));
	}
}