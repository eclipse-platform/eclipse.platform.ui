package org.eclipse.update.internal.ui.preferences;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.internal.ui.UpdateUIPlugin;

/**
 * Insert the type's description here.
 * @see PreferencePage
 */
public class MainPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {
	private static final String KEY_DESCRIPTION =
		"MainPreferencePage.description";
	private static final String PREFIX = UpdateUIPlugin.getPluginId();
	public static final String P_HISTORY_SIZE = PREFIX + ".historySize";
	public static final String P_BROWSER = PREFIX + ".browser";
	public static final String EMBEDDED_VALUE = "embedded";
	private static final String SYSTEM_VALUE = "system";
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
	
	
	private Text httpProxyHostText;
	private Text httpProxyPortText;
	private Label httpProxyHostLabel;
	private Label httpProxyPortLabel;
	private Button enableHttpProxy;
	private static final String KEY_ENABLE_HTTP_PROXY = "MainPreferencePage.enableHttpProxy";
	private static final String KEY_HTTP_PROXY_SERVER = "MainPreferencePage.httpProxyHost";
	private static final String KEY_HTTP_PROXY_PORT = "MainPreferencePage.httpProxyPort";
		
	/**
	 * The constructor.
	 */
	public MainPreferencePage() {
		super(GRID);
		setPreferenceStore(UpdateUIPlugin.getDefault().getPreferenceStore());
		setDescription(UpdateUIPlugin.getResourceString(KEY_DESCRIPTION));
	}

	/**
	 * Insert the method's description here.
	 * @see PreferencePage#init
	 */
	public void init(IWorkbench workbench) {
	}
	public void createFieldEditors() {
		WorkbenchHelp.setHelp(getFieldEditorParent(), "org.eclipse.update.ui.MainPreferencePage_getFieldEditorParent");
		IntegerFieldEditor maxLevel =
			new IntegerFieldEditor(
				P_HISTORY_SIZE,
				UpdateUIPlugin.getResourceString(KEY_HISTORY_SIZE),
				getFieldEditorParent());
		maxLevel.setValidRange(1, Integer.MAX_VALUE);
		addField(maxLevel);
		if ("win32".equals(SWT.getPlatform())) {
			RadioGroupFieldEditor browser =
				new RadioGroupFieldEditor(
					P_BROWSER,
					UpdateUIPlugin.getResourceString(KEY_BROWSER_CHOICE),
					1,
					new String[][] {
						{
							UpdateUIPlugin.getResourceString(
								KEY_BROWSER_CHOICE_EMBEDDED),
							EMBEDDED_VALUE },
						{
					UpdateUIPlugin.getResourceString(KEY_BROWSER_CHOICE_SYSTEM),
						SYSTEM_VALUE }
			}, getFieldEditorParent());
			addField(browser);
		}
		createSpacer(getFieldEditorParent(), 2);

		RadioGroupFieldEditor updateVersions =
			new RadioGroupFieldEditor(
				P_UPDATE_VERSIONS,
				UpdateUIPlugin.getResourceString(KEY_UPDATE_VERSIONS),
				1,
				new String[][] {
					{
						UpdateUIPlugin.getResourceString(
							KEY_UPDATE_VERSIONS_EQUIVALENT),
						EQUIVALENT_VALUE },
					{
				UpdateUIPlugin.getResourceString(
					KEY_UPDATE_VERSIONS_COMPATIBLE),
					COMPATIBLE_VALUE }
		}, getFieldEditorParent());
		addField(updateVersions);
		
		createSpacer(getFieldEditorParent(), 2);

		ColorFieldEditor topicColor =
			new ColorFieldEditor(
				UpdateColors.P_TOPIC_COLOR,
				UpdateUIPlugin.getResourceString(KEY_TOPIC_COLOR),
				getFieldEditorParent());
		addField(topicColor);
		
		createSpacer(getFieldEditorParent(), 2);
		createHttpProxy(getFieldEditorParent(),2);
		
	}
	protected void createSpacer(Composite composite, int columnSpan) {
		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData(gd);
	}
	protected void createHttpProxy(Composite composite, int columnSpan) {
		
		enableHttpProxy = new Button(composite,SWT.CHECK);
		enableHttpProxy.setText(UpdateUIPlugin.getResourceString(KEY_ENABLE_HTTP_PROXY));
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		enableHttpProxy.setLayoutData(gd);
		
		httpProxyHostLabel = new Label(composite, SWT.NONE);
		gd = new GridData();
		gd.horizontalSpan = 1;
		httpProxyHostLabel.setLayoutData(gd);
		httpProxyHostLabel.setText(UpdateUIPlugin.getResourceString(KEY_HTTP_PROXY_SERVER));
		
		httpProxyHostText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		httpProxyHostText.setFont(composite.getFont());
		gd = new GridData();
		gd.horizontalSpan = columnSpan-1;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace=true;
		httpProxyHostText.setLayoutData(gd);
		
		httpProxyPortLabel = new Label(composite, SWT.NONE);
		gd = new GridData();
		gd.horizontalSpan = 1;
		httpProxyPortLabel.setLayoutData(gd);
		httpProxyPortLabel.setText(UpdateUIPlugin.getResourceString(KEY_HTTP_PROXY_PORT));

		httpProxyPortText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		httpProxyPortText.setFont(composite.getFont());
		gd = new GridData();
		gd.horizontalSpan = columnSpan-1;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace=true;
		httpProxyPortText.setLayoutData(gd);

		performDefaults();
		
		enableHttpProxy.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				boolean enable = enableHttpProxy.getSelection();
				httpProxyPortLabel.setEnabled(enable);
				httpProxyPortText.setEnabled(enable);
				httpProxyHostLabel.setEnabled(enable);
				httpProxyHostText.setEnabled(enable);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}});
		
	}
	private int getHistorySize() {
		IPreferenceStore store =
			UpdateUIPlugin.getDefault().getPreferenceStore();
		return store.getInt(P_HISTORY_SIZE);
	}
	
	public static boolean getUseEmbeddedBrowser() {
		IPreferenceStore store =
			UpdateUIPlugin.getDefault().getPreferenceStore();
		return store.getString(P_BROWSER).equals(EMBEDDED_VALUE);
	}

	public static String getUpdateVersionsMode() {
		IPreferenceStore store =
			UpdateUIPlugin.getDefault().getPreferenceStore();
		return store.getString(P_UPDATE_VERSIONS);
	}

	public boolean performOk() {
		boolean result = super.performOk();
		if (result) {
			BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
				public void run() {
					try {
						SiteManager.getLocalSite().setMaximumHistoryCount(getHistorySize());
						SiteManager.setHttpProxyInfo(enableHttpProxy.getSelection(),httpProxyHostText.getText(),httpProxyPortText.getText());
					} catch (CoreException e) {
						UpdateUIPlugin.logException(e);
					}
				}
			});
		}
		UpdateUIPlugin.getDefault().savePluginPreferences();
		return result;
	}
	public void performApply() {
		super.performApply();
		BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
			public void run() {
				SiteManager.setHttpProxyInfo(enableHttpProxy.getSelection(),httpProxyHostText.getText(),httpProxyPortText.getText());
			}
		});
	}
	public void performDefaults() {
		super.performDefaults();
		
		enableHttpProxy.setSelection(SiteManager.isHttpProxyEnable());
		String serverValue = SiteManager.getHttpProxyServer();
		if (serverValue!=null)	httpProxyHostText.setText(serverValue);
		String portValue = SiteManager.getHttpProxyPort();
		if (portValue!=null) httpProxyPortText.setText(portValue);

		httpProxyPortLabel.setEnabled(enableHttpProxy.getSelection());
		httpProxyPortText.setEnabled(enableHttpProxy.getSelection());
		httpProxyHostLabel.setEnabled(enableHttpProxy.getSelection());
		httpProxyHostText.setEnabled(enableHttpProxy.getSelection());
	}

}