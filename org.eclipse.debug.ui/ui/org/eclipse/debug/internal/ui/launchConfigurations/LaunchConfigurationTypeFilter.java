/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Provides the implementation of the filter for filtering the launch configuration viewer based on the preference
 * <code>IInternalDebugUIConstants.PREF_FILTER_LAUNCH_TYPES</code> 
 * 
 * @since 3.2
 */
public class LaunchConfigurationTypeFilter extends ViewerFilter {

	/**
	 * Constructor
	 */
	public LaunchConfigurationTypeFilter() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if(element instanceof ILaunchConfiguration) {
			return true;
		}
		//we only care about launch configuration types
		if(element instanceof ILaunchConfigurationType) {
			IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
			String[] types = store.getString(IInternalDebugUIConstants.PREF_FILTER_TYPE_LIST).split("\\,"); //$NON-NLS-1$
			for(int i = 0; i < types.length; i++) {
				if(types[i].equals(((ILaunchConfigurationType)element).getIdentifier())) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

}
