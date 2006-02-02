package org.eclipse.debug.internal.ui.launchConfigurations;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

/**
 * This class extends <code>LaunchGroupFilter</code> by allowing more than one launch group to be used in 
 * the filtering
 * 
 * @since 3.2
 */
public class MultiLaunchGroupFilter extends ViewerFilter {

	/**
	 * array of launchgroup extensions to test for filtering against
	 */
	private LaunchGroupExtension[] fGroups;
	
	public MultiLaunchGroupFilter(LaunchGroupExtension[] groups) {
		fGroups = groups;
	}                                                
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		ILaunchConfigurationType type = null;
		ILaunchConfiguration config = null;
		boolean priv = false;
		if (parentElement instanceof ILaunchConfigurationType) {
			type = (ILaunchConfigurationType)parentElement;
		}
		if (element instanceof ILaunchConfigurationType) {
			type = (ILaunchConfigurationType)element;
		}
		if (element instanceof ILaunchConfiguration) {
			config = (ILaunchConfiguration)element;
			try {
				type = config.getType();
				if (config != null) {
					priv = config.getAttribute(IDebugUIConstants.ATTR_PRIVATE, false);
				}
			} 
			catch (CoreException e) {}
		}
		if (type != null) {
			return !priv && equalModes(type) && equalCategories(type.getCategory()) && !WorkbenchActivityHelper.filterItem(new LaunchConfigurationTypeContribution(type));
		}
		return false;
	}

	/**
	 * compares a mode against the modes form the list of group modes
	 * @param mode the mode to compare
	 * @return true if the mode matches any one of the modes in the listing, false otherwise
	 */
	private boolean equalModes(ILaunchConfigurationType type) {
		for(int i = 0; i < fGroups.length; i++) {
			if(type.supportsMode(fGroups[i].getMode())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * compares a category against those passed in the creation of the filter
	 * @param category the category to compare
	 * @return true if the category matches any one fo the categories in the listing, false otherwise
	 */
	private boolean equalCategories(String category) {
		String lcat = null;
		for(int i = 0; i < fGroups.length; i++) {
			lcat = fGroups[i].getCategory();
			if(category == null || lcat == null) {
				if(category == lcat) {
					return true;
				}
			}
			else {
				if(category.equals(lcat)) {
					return true;
				}
			}
		}
		return false;
	}
	
}
