package org.eclipse.update.internal.ui.preferences;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.*;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.internal.ui.UpdateUIPlugin;

/**
 * Insert the type's description here.
 * @see PreferencePage
 */
public class MainPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {
	private static final String KEY_DESCRIPTION = "MainPreferencePage.description";
	private static final String PREFIX = UpdateUIPlugin.getPluginId();
	private static final String P_HISTORY_SIZE = PREFIX + ".historySize";
	private static final String P_BROWSER = PREFIX + ".browser";
	private static final String EMBEDDED_VALUE = "embedded";
	private static final String SYSTEM_VALUE = "system";
	private static final String KEY_HISTORY_SIZE = "MainPreferencePage.historySize";
	private static final String KEY_BROWSER_CHOICE =
		"MainPreferencePage.browserChoice";
	private static final String KEY_BROWSER_CHOICE_EMBEDDED =
		"MainPreferencePage.browserChoice.embedded";
	private static final String KEY_BROWSER_CHOICE_SYSTEM =
		"MainPreferencePage.browserChoice.system";
	/**
	 * The constructor.
	 */
	public MainPreferencePage() {
		super(GRID);
		setPreferenceStore(UpdateUIPlugin.getDefault().getPreferenceStore());
		setDescription(UpdateUIPlugin.getResourceString(KEY_DESCRIPTION));
		initializeDefaults(getPreferenceStore());
	}

	/**
	 * Insert the method's description here.
	 * @see PreferencePage#init
	 */
	public void init(IWorkbench workbench) {
	}
	public void createFieldEditors() {
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
							UpdateUIPlugin.getResourceString(KEY_BROWSER_CHOICE_EMBEDDED),
							EMBEDDED_VALUE },
						{
					UpdateUIPlugin.getResourceString(KEY_BROWSER_CHOICE_SYSTEM), SYSTEM_VALUE }
			}, getFieldEditorParent());
			addField(browser);
		}
	}
	private static void initializeDefaults(IPreferenceStore store) {
		store.setDefault(P_HISTORY_SIZE, 5);
		store.setDefault(P_BROWSER, EMBEDDED_VALUE);
	}
	private int getHistorySize() {
		IPreferenceStore store = UpdateUIPlugin.getDefault().getPreferenceStore();
		initializeDefaults(store);
		return store.getInt(P_HISTORY_SIZE);
	}
	public static boolean getUseEmbeddedBrowser() {
		IPreferenceStore store = UpdateUIPlugin.getDefault().getPreferenceStore();
		initializeDefaults(store);
		return store.getString(P_BROWSER).equals(EMBEDDED_VALUE);
	}

	public boolean performOk() {
		boolean result = super.performOk();
		if (result) {
			BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
				public void run() {
					try {
						SiteManager.getLocalSite().setMaximumHistoryCount(getHistorySize());
					} catch (CoreException e) {
						UpdateUIPlugin.logException(e);
					}
				}
			});
		}
		return result;
	}
}