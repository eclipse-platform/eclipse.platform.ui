package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

/**
 * A temporary implementation for tabs groups that are not yet defined.
 * Collects all the tabs defined for a launch config type.
 * 
 * This will disappear when all clients have migrated from tab extensions
 * to tab group extentions.
 */
public class LaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {
	
	/**
	 * The launch config type this tab group is associated with.
	 */
	private ILaunchConfigurationType fType = null;


	/**
	 * Constructs a new tab group for the given type.
	 */
	public LaunchConfigurationTabGroup(ILaunchConfigurationType type) {
		fType = type;
	}
	
    /**
	 * @see ILaunchConfigurationTabGroup#createTabs(ILaunchConfigurationDialog, String)
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		LaunchConfigurationTabExtension[] exts = LaunchConfigurationPresentationManager.getDefault().getTabs(fType);
		List tabs = new ArrayList(exts.length);
		try {
			for (int i = 0; i < exts.length; i++) {
				if (exts[i].getMode() == null || exts[i].getMode().equals(mode)) {
					ILaunchConfigurationTab tab = (ILaunchConfigurationTab)exts[i].getConfigurationElement().createExecutableExtension("class");
					if (tab instanceof AbstractLaunchConfigurationTab) {
						((AbstractLaunchConfigurationTab)tab).setLaunchConfigurationTabExtension(exts[i]);
					}
					tab.setLaunchConfigurationDialog(dialog);
					tabs.add(tab);
				}
			}
		} catch (CoreException e) {
			tabs = new ArrayList(0);
		}
		setTabs((ILaunchConfigurationTab[])tabs.toArray(new ILaunchConfigurationTab[tabs.size()]));
	}

}
