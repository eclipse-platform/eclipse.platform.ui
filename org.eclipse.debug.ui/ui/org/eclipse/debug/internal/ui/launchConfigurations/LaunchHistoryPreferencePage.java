package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
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
		tab.setText("De&bug");
		tab.setImage(DebugPluginImages.getImage(IDebugUIConstants.IMG_ACT_DEBUG));
		tab.setControl(createDebugTab(tabFolder));
		
		tab = new TabItem(tabFolder, SWT.NONE);
		tab.setText("&Run");
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
	
	/*
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
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
		DebugUIPlugin plugin = DebugUIPlugin.getDefault();
		// debug favorites
		Vector list = convertToHisotryElements(getDebugTab().getFavorites(), ILaunchManager.DEBUG_MODE, true);
		plugin.setDebugFavorites(list);
		
		// debug recent history
		list = convertToHisotryElements(getDebugTab().getRecents(), ILaunchManager.DEBUG_MODE, false);
		plugin.setDebugHistory(list);
		
		// run favorites
		list = convertToHisotryElements(getRunTab().getFavorites(), ILaunchManager.RUN_MODE, true);
		plugin.setRunFavorites(list);
		
		// run recent history
		list = convertToHisotryElements(getRunTab().getRecents(), ILaunchManager.RUN_MODE, false);
		plugin.setRunHistory(list);		
		
		return true;
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
	protected Vector convertToHisotryElements(List configs, String mode, boolean favorite) {
		Vector  v = new Vector(configs.size());
		Iterator iter = configs.iterator();
		ILabelProvider lp = DebugUITools.getDefaultLabelProvider();
		while (iter.hasNext()) {
			ILaunchConfiguration config = (ILaunchConfiguration)iter.next();
			LaunchConfigurationHistoryElement hist = new LaunchConfigurationHistoryElement(config, mode, lp.getText(config));
			hist.setFavorite(favorite);
			v.add(hist);
		}
		return v;
	}

}
