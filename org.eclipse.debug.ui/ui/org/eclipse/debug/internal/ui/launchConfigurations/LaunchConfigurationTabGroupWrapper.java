package org.eclipse.debug.internal.ui.launchConfigurations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.LaunchConfigurationTabExtension;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
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
	private List fTabs = null;
	
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
		if(fTabs != null) {
			ILaunchConfigurationTab[] tabs = getTabs();
			for(int i = 0; i < tabs.length; i++) {
				tabs[i].dispose();
			}
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#getTabs()
	 */
	public ILaunchConfigurationTab[] getTabs() {
		if(fTabs == null) {
			fTabs = new ArrayList();
			ILaunchConfigurationTab[] tmp = fGroup.getTabs();
			for(int i = 0; i < tmp.length; i++) {
				fTabs.add(tmp[i]);
			}
			LaunchConfigurationTabExtension[] ext = LaunchConfigurationPresentationManager.getDefault().getTabExtensions(fGroupId);
			//copy contributed into correct postion or end if no id or id is not found
			AbstractLaunchConfigurationTab alct = null;
			String id = null;
			List item = null;
			for(int i = 0; i < ext.length; i++) {
				id = ext[i].getRelativeTabId();
				if(id != null) {
					//position specified, try to find it
					boolean found = false;
					for(int j = 0; j < tmp.length; j++) {
						if(tmp[j] instanceof AbstractLaunchConfigurationTab) {
							alct = (AbstractLaunchConfigurationTab) tmp[j];
							if(id.equals(alct.getId())) {
								if(j != tmp.length-1) {
									item = new ArrayList();
									item.add(ext[i].getTab());
									fTabs.addAll(j+1, item);
									found = true;
									break;
								}
							}
						}
					}
					if(!found) {
						//id did not match any tabs, add it to the end
						fTabs.add(ext[i].getTab());
					}
				}
				else {
					//no position specified, add it to the end
					fTabs.add(ext[i].getTab());
				}
			}
		}
		return (ILaunchConfigurationTab[]) fTabs.toArray(new ILaunchConfigurationTab[fTabs.size()]);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		if(fTabs == null) {
			getTabs();
		}
		for(int i = 0; i < fTabs.size(); i++) {
			((ILaunchConfigurationTab)fTabs.get(i)).initializeFrom(configuration);
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
		if(fTabs != null) {
			for(int i = 0; i < fTabs.size(); i++) {
				((ILaunchConfigurationTab)fTabs.get(i)).performApply(configuration);
			}
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		if(fTabs != null) {
			for(int i = 0; i < fTabs.size(); i++) {
				((ILaunchConfigurationTab)fTabs.get(i)).setDefaults(configuration);
			}
		}
	}

}
