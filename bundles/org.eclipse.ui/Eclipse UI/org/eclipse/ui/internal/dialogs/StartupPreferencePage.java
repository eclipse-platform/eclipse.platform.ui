package org.eclipse.ui.internal.dialogs;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.util.StringTokenizer;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;

public class StartupPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private Table pluginsList;
	private Workbench workbench;
	
	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL);
		composite.setLayoutData(data);

		Label label = new Label(composite,SWT.NONE);
		label.setText(WorkbenchMessages.getString("StartupPreferencePage.label")); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(data);		
		pluginsList = new Table(composite,SWT.BORDER | SWT.CHECK | SWT.H_SCROLL | SWT.V_SCROLL);
		data = new GridData(GridData.FILL_BOTH);	
		pluginsList.setLayoutData(data);
		populatePluginsList();

		return composite;
	}
	private void populatePluginsList() {
		IPluginDescriptor descriptors[] = workbench.getEarlyActivatedPlugins();
		IPreferenceStore store = workbench.getPreferenceStore();
		String pref = store.getString(IPreferenceConstants.PLUGINS_NOT_ACTIVATED_ON_STARTUP);
		if(pref == null)
			pref = new String();
		for (int i = 0; i < descriptors.length; i++) {
			IPluginDescriptor desc = descriptors[i];
			TableItem item = new TableItem(pluginsList,SWT.NONE);
			item.setText(desc.getLabel());
			item.setData(desc);
			String id = desc.getUniqueIdentifier() + IPreferenceConstants.SEPARATOR;
			item.setChecked(pref.indexOf(id) < 0);
		}
	}
	/**
	 * @see IWorkbenchPreferencePage
	 */
	public void init(IWorkbench workbench) {
		this.workbench = (Workbench)workbench;
	}
	/**
	 * @see PreferencePage
	 */
	protected void performDefaults() {
		TableItem items[] = pluginsList.getItems();
		for (int i = 0; i < items.length; i++) {
			items[i].setChecked(true);
		}
	}
	/**
	 * @see PreferencePage
	 */
	public boolean performOk() {
		StringBuffer preference = new StringBuffer();
		TableItem items[] = pluginsList.getItems();
		for (int i = 0; i < items.length; i++) {
			if(!items[i].getChecked()) {
				IPluginDescriptor descriptor = (IPluginDescriptor)items[i].getData();
				preference.append(descriptor.getUniqueIdentifier());
				preference.append(IPreferenceConstants.SEPARATOR);
			}
		}
		String pref = preference.toString();
		IPreferenceStore store = workbench.getPreferenceStore();
		store.putValue(IPreferenceConstants.PLUGINS_NOT_ACTIVATED_ON_STARTUP,pref);
		return true;
	}
}
