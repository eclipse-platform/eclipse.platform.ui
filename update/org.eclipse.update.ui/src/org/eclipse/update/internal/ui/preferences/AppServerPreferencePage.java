package org.eclipse.update.internal.ui.preferences;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.update.internal.ui.UpdateUIPlugin;

/**
 * Insert the type's description here.
 * @see PreferencePage
 */
public class AppServerPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {
	private static final String KEY_DESCRIPTION =
		"AppServerPreferencePage.description";
	private static final String PREFIX = UpdateUIPlugin.getPluginId();
	public static final String P_MASTER_SWITCH = PREFIX + ".appServer";
	public static final String P_ENCODE_URLS = PREFIX + ".encodeURLs";
	private static final String KEY_MASTER_SWITCH =
		"AppServerPreferencePage.masterSwitch";
	private static final String KEY_ENCODE_URLS =
		"AppServerPreferencePage.encodeURLs";
	private MasterField masterField;

	class MasterField extends BooleanFieldEditor {
		BooleanFieldEditor slave;
		public MasterField(String property, String key, Composite parent) {
			super(property, key, parent);
		}

		protected void valueChanged(boolean oldValue, boolean newValue) {
			super.valueChanged(oldValue, newValue);
			slave.setEnabled(newValue, getFieldEditorParent());
		}

		void update() {
			slave.setEnabled(getBooleanValue(), getFieldEditorParent());
		}

		public void setSlave(BooleanFieldEditor slave) {
			this.slave = slave;
		}
	}

	/**
	 * The constructor.
	 */
	public AppServerPreferencePage() {
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
		WorkbenchHelp.setHelp(
			getFieldEditorParent(),
			"org.eclipse.update.ui.AppServerPreferencePage");
		masterField =
			new MasterField(
				P_MASTER_SWITCH,
				UpdateUIPlugin.getResourceString(KEY_MASTER_SWITCH),
				getFieldEditorParent());
		addField(masterField);
		BooleanFieldEditor encodeURLs =
			new BooleanFieldEditor(
				P_ENCODE_URLS,
				UpdateUIPlugin.getResourceString(KEY_ENCODE_URLS),
				getFieldEditorParent());
		addField(encodeURLs);
		masterField.setSlave(encodeURLs);
	}

	protected void initialize() {
		super.initialize();
		masterField.update();
	}
	protected void createSpacer(Composite composite, int columnSpan) {
		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData(gd);
	}
	public static boolean getUseApplicationServer() {
		IPreferenceStore store =
			UpdateUIPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(P_MASTER_SWITCH);
	}

	public static boolean getEncodeURLs() {
		IPreferenceStore store =
			UpdateUIPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(P_ENCODE_URLS);
	}

	public boolean performOk() {
		boolean result = super.performOk();
		if (result) {
			final boolean bag[] = new boolean[1];
			BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
				public void run() {
					try {
						handleServerActivation();
						bag[0] = true;
					} catch (CoreException e) {
						UpdateUIPlugin.logException(e);
						bag[0] = false;
					}
				}
			});
			result = bag[0];
		}
		if (result)
			UpdateUIPlugin.getDefault().savePluginPreferences();
		return result;
	}

	private void handleServerActivation() throws CoreException {
		boolean masterSwitch = getUseApplicationServer();
		boolean webAppRunning = UpdateUIPlugin.getDefault().isWebAppStarted();

		if (!masterSwitch && webAppRunning) {
			// remove Web app
			UpdateUIPlugin.getDefault().stopWebApp();
		} else if (masterSwitch && !webAppRunning) {
			// add Web app
			UpdateUIPlugin.getDefault().startWebApp();
		}
	}
}