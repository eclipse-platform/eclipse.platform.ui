package org.eclipse.debug.internal.ui.preferences;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.launchConfigurations.DebugHistoryPreferenceTab;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationHistoryElement;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;
import org.eclipse.debug.internal.ui.launchConfigurations.RunHistoryPreferenceTab;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Preference page to manage launch history & favorites
 */
public class LaunchHistoryPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private IntegerFieldEditor fHistoryMaxEditor;
	/**
	 * Debug tab.
	 */
	protected LaunchHistoryPreferenceTab fDebugTab;
	
	/**
	 * Run tab.
	 */
	protected LaunchHistoryPreferenceTab fRunTab;
	
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		
		fHistoryMaxEditor = new IntegerFieldEditor(IDebugUIConstants.PREF_MAX_HISTORY_SIZE, LaunchConfigurationsMessages.getString("LaunchHistoryPreferencePage.Maximum_launch_history_size_1"), composite); //$NON-NLS-1$
		int historyMax = IDebugPreferenceConstants.MAX_LAUNCH_HISTORY_SIZE;
		fHistoryMaxEditor.setPreferenceStore(DebugUIPlugin.getDefault().getPreferenceStore());
		fHistoryMaxEditor.setPreferencePage(this);
		fHistoryMaxEditor.setTextLimit(Integer.toString(historyMax).length());
		fHistoryMaxEditor.setErrorMessage(MessageFormat.format(LaunchConfigurationsMessages.getString("LaunchHistoryPreferencePage.The_size_of_the_launch_history_should_be_between_{0}_and_{1}_1"), new Object[] { new Integer(1), new Integer(historyMax)})); //$NON-NLS-1$
		fHistoryMaxEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		fHistoryMaxEditor.setValidRange(1, historyMax);
		fHistoryMaxEditor.load();
		fHistoryMaxEditor.setPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(FieldEditor.IS_VALID)) 
					setValid(fHistoryMaxEditor.isValid());
			}
		});
		fHistoryMaxEditor.fillIntoGrid(composite, 2);

		TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan= 2;
		tabFolder.setLayoutData(gd);
		
		TabItem tab = new TabItem(tabFolder, SWT.NONE);
		tab.setText(LaunchConfigurationsMessages.getString("LaunchHistoryPreferencePage.De&bug_1")); //$NON-NLS-1$
		tab.setImage(DebugPluginImages.getImage(IDebugUIConstants.IMG_ACT_DEBUG));
		tab.setControl(createDebugTab(tabFolder));
		
		tab = new TabItem(tabFolder, SWT.NONE);
		tab.setText(LaunchConfigurationsMessages.getString("LaunchHistoryPreferencePage.&Run_2")); //$NON-NLS-1$
		tab.setImage(DebugPluginImages.getImage(IDebugUIConstants.IMG_ACT_RUN));
		tab.setControl(createRunTab(tabFolder));
				
		return composite;
	}
	
	/**
	 * Creates the control for the debug favorites
	 */
	protected Control createDebugTab(Composite parent) {
		setDebugTab(new DebugHistoryPreferenceTab());
		return getDebugTab().createControl(parent);
	}

	/**
	 * Creates the control for the debug favorites
	 */
	protected Control createRunTab(Composite parent) {
		setRunTab(new RunHistoryPreferenceTab());
		return getRunTab().createControl(parent);
	}
	
	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		setDescription(LaunchConfigurationsMessages.getString("LaunchHistoryPreferencePage.description")); //$NON-NLS-1$
	}

	/**
	 * Returns the run tab.
	 * 
	 * @return a lanuch history preference tab
	 */
	protected LaunchHistoryPreferenceTab getRunTab() {
		return fRunTab;
	}

	/**
	 * Sets the run tab.
	 * 
	 * @param tab lanuch history preference tab
	 */
	private void setRunTab(LaunchHistoryPreferenceTab tab) {
		fRunTab = tab;
	}
	
	/**
	 * Returns the debug tab.
	 * 
	 * @return a lanuch history preference tab
	 */
	protected LaunchHistoryPreferenceTab getDebugTab() {
		return fDebugTab;
	}

	/**
	 * Sets the debug tab.
	 * 
	 * @param tab lanuch history preference tab
	 */
	private void setDebugTab(LaunchHistoryPreferenceTab tab) {
		fDebugTab = tab;
	}

	/**
	 * @see PreferencePage#performOk()
	 */
	public boolean performOk() {
		
		ILaunchConfiguration[] debugOriginals = getDebugTab().getInitialFavorites();
		ILaunchConfiguration[] runOriginals = getRunTab().getInitialFavorites();
		
		LaunchConfigurationManager manager= DebugUIPlugin.getLaunchConfigurationManager();
		// debug favorites
		Vector list = convertToHistoryElements(getDebugTab().getFavorites(), ILaunchManager.DEBUG_MODE);
		manager.setDebugFavorites(list);
		
		// debug recent history
		list = convertToHistoryElements(getDebugTab().getRecents(), ILaunchManager.DEBUG_MODE);
		manager.setDebugHistory(list);
		
		// run favorites
		list = convertToHistoryElements(getRunTab().getFavorites(), ILaunchManager.RUN_MODE);
		manager.setRunFavorites(list);
		
		// run recent history
		list = convertToHistoryElements(getRunTab().getRecents(), ILaunchManager.RUN_MODE);
		manager.setRunHistory(list);	
		
		// update config attributes for favorites
		List current = getDebugTab().getFavorites();
		updateAttributes(debugOriginals, current, IDebugUIConstants.ATTR_DEBUG_FAVORITE);
		
		current = getRunTab().getFavorites();
		updateAttributes(runOriginals, current, IDebugUIConstants.ATTR_RUN_FAVORITE);			
		
		fHistoryMaxEditor.store();
		
		DebugUIPlugin.getDefault().savePluginPreferences();
		return true;
	}
	
	/**
	 * Update the 'favorite' attributes to reflect the current list.
	 */
	protected void updateAttributes(ILaunchConfiguration[] originals, List current, String attribute) {
		List added = new ArrayList(current);
		List removed = new ArrayList();

		for (int i = 0; i < originals.length; i++) {
			added.remove(originals[i]);
			if (!current.contains(originals[i])) {
				removed.add(originals[i]);
			}
		}
		
		try {
			Iterator a = added.iterator();
			while (a.hasNext()) {
				ILaunchConfiguration config = (ILaunchConfiguration)a.next();
				ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
				wc.setAttribute(attribute, true);
				wc.doSave();
			}
			
			Iterator r = removed.iterator();
			while (r.hasNext()) {
				ILaunchConfiguration config = (ILaunchConfiguration)r.next();
				ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
				wc.setAttribute(attribute, (String)null);
				wc.doSave();
			}				
		} catch (CoreException e) {
			DebugUIPlugin.log(e);
		}
	}
	
	/**
	 * Converts the list of launch configurations to a vector
	 * of history elements.
	 * 
	 * @param configs list of configs
	 * @param mode the mode for the history elements
	 * @return vector of history elements corresponding to the
	 *  given launch configurations
	 */
	protected Vector convertToHistoryElements(List configs, String mode) {
		Vector  v = new Vector(configs.size());
		Iterator iter = configs.iterator();
		while (iter.hasNext()) {
			ILaunchConfiguration config = (ILaunchConfiguration)iter.next();
			LaunchConfigurationHistoryElement hist = new LaunchConfigurationHistoryElement(config, mode);
			v.add(hist);
		}
		return v;
	}
	
	/**
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		getDebugTab().performDefaults();
		getRunTab().performDefaults();
		fHistoryMaxEditor.loadDefault();
		super.performDefaults();
	}
	
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(IDebugUIConstants.PREF_MAX_HISTORY_SIZE, 10);	
	}
	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(getControl(), IDebugHelpContextIds.LAUNCH_HISTORY_PREFERENCE_PAGE);
	}

}
