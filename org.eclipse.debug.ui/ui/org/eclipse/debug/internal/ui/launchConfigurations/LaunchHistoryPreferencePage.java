package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


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
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page to manage launch history & favorites
 */
public class LaunchHistoryPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage {
		
	/**
	 * Debug tab.
	 */
	protected LaunchHistoryPreferenceTab fDebugTab;
	
	/**
	 * Run tab.
	 */
	protected LaunchHistoryPreferenceTab fRunTab;
	
	protected Control createContents(Composite parent) {
		TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		tabFolder.setLayoutData(gd);
		
		TabItem tab = new TabItem(tabFolder, SWT.NONE);
		tab.setText(LaunchConfigurationsMessages.getString("LaunchHistoryPreferencePage.De&bug_1")); //$NON-NLS-1$
		tab.setImage(DebugPluginImages.getImage(IDebugUIConstants.IMG_ACT_DEBUG));
		tab.setControl(createDebugTab(tabFolder));
		
		tab = new TabItem(tabFolder, SWT.NONE);
		tab.setText(LaunchConfigurationsMessages.getString("LaunchHistoryPreferencePage.&Run_2")); //$NON-NLS-1$
		tab.setImage(DebugPluginImages.getImage(IDebugUIConstants.IMG_ACT_RUN));
		tab.setControl(createRunTab(tabFolder));
				
		return tabFolder;
		
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
		setDescription(LaunchConfigurationsMessages.getString("LaunchHistoryPreferencePage.description"));
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
		
		DebugUIPlugin plugin = DebugUIPlugin.getDefault();
		// debug favorites
		Vector list = convertToHistoryElements(getDebugTab().getFavorites(), ILaunchManager.DEBUG_MODE);
		plugin.setDebugFavorites(list);
		
		// debug recent history
		list = convertToHistoryElements(getDebugTab().getRecents(), ILaunchManager.DEBUG_MODE);
		plugin.setDebugHistory(list);
		
		// run favorites
		list = convertToHistoryElements(getRunTab().getFavorites(), ILaunchManager.RUN_MODE);
		plugin.setRunFavorites(list);
		
		// run recent history
		list = convertToHistoryElements(getRunTab().getRecents(), ILaunchManager.RUN_MODE);
		plugin.setRunHistory(list);	
		
		// update config attributes for favorites
		List current = getDebugTab().getFavorites();
		updateAttributes(debugOriginals, current, IDebugUIConstants.ATTR_DEBUG_FAVORITE);
		
		current = getRunTab().getFavorites();
		updateAttributes(runOriginals, current, IDebugUIConstants.ATTR_RUN_FAVORITE);			
		
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
		super.performDefaults();
	}

}
