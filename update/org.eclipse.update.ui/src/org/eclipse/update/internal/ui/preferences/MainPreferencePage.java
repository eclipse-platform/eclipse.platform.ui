package org.eclipse.update.internal.ui.preferences;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
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
		if (SWT.getPlatform().equals("win32")) {
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

	}
	protected void createSpacer(Composite composite, int columnSpan) {
		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData(gd);
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
						SiteManager.getLocalSite().setMaximumHistoryCount(
							getHistorySize());
					} catch (CoreException e) {
						UpdateUIPlugin.logException(e);
					}
				}
			});
		}
		UpdateUIPlugin.getDefault().savePluginPreferences();
		return result;
	}
}