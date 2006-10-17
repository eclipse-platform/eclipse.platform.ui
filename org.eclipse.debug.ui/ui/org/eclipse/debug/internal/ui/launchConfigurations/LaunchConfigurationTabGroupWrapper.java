package org.eclipse.debug.internal.ui.launchConfigurations;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;

/**
 * This class is used to wrap a contributed <code>ILaunchConfigurationTabGroup</code> with any contributed tabs
 * for that group (from a <code>launchConfigurationTabs</code> extension point).
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class has been added as
 * part of a work in progress. There is no guarantee that this API will
 * remain unchanged during the 3.3 release cycle. Please do not use this API
 * without consulting with the Platform/Debug team.
 * </p>
 * @since 3.3
 */
public class LaunchConfigurationTabGroupWrapper implements ILaunchConfigurationTabGroup {
	
	private ILaunchConfigurationTabGroup fGroup = null;
	private String fGroupId = null;
	private ILaunchConfigurationTab[] fContributedTabs = null;
	
	/**
	 * Constructor
	 * @param group the existing group to wrapper
	 */
	public LaunchConfigurationTabGroupWrapper(ILaunchConfigurationTabGroup group, String groupId) {
		fGroup = group;
		fGroupId = groupId;
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#createTabs(org.eclipse.debug.ui.ILaunchConfigurationDialog, java.lang.String)
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		if(fGroup != null) {
			fGroup.createTabs(dialog, mode);
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#dispose()
	 */
	public void dispose() {
		if(fGroup != null) {
			fGroup.dispose();
		}
		if(fContributedTabs != null) {
			for(int i = 0; i < fContributedTabs.length; i++) {
				fContributedTabs[i].dispose();
			}
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#getTabs()
	 */
	public ILaunchConfigurationTab[] getTabs() {
		if(fContributedTabs == null) {
			fContributedTabs = LaunchConfigurationPresentationManager.getDefault().createContributedTabs(fGroupId);
		}
		ILaunchConfigurationTab[] grouptabs = fGroup.getTabs();
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[grouptabs.length + fContributedTabs.length];
		System.arraycopy(grouptabs, 0, tabs, 0, grouptabs.length);
		System.arraycopy(fContributedTabs, 0, tabs, grouptabs.length, fContributedTabs.length);
		return tabs;
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		if(fGroup != null) {
			fGroup.initializeFrom(configuration);
		}
		if(fContributedTabs == null) {
			getTabs();
		}
		for(int i = 0; i < fContributedTabs.length; i++) {
			fContributedTabs[i].initializeFrom(configuration);
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#launched(org.eclipse.debug.core.ILaunch)
	 */
	public void launched(ILaunch launch) {
		if(fGroup != null) {
			fGroup.launched(launch);
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if(fGroup != null) {
			fGroup.performApply(configuration);
			if(fContributedTabs == null) {
				getTabs();
			}
			for(int i = 0; i < fContributedTabs.length; i++) {
				fContributedTabs[i].performApply(configuration);
			}
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		if(fGroup != null) {
			fGroup.setDefaults(configuration);
			if(fContributedTabs == null) { 
				getTabs();
			}
			for(int i = 0; i < fContributedTabs.length; i++) {
				fContributedTabs[i].setDefaults(configuration);
			}
		}
	}

}
