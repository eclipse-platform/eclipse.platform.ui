/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.contextlaunching.ContextRunner;
import org.eclipse.debug.ui.actions.ILaunchable;

/**
 * Tests if an object is launchable.
 */
public class LaunchablePropertyTester extends PropertyTester {

	/**
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if ("launchable".equals(property)) { //$NON-NLS-1$
				if (DebugUIPlugin.getDefault().getLaunchConfigurationManager().launchModeAvailable((String)expectedValue)) {
					return Platform.getAdapterManager().hasAdapter(receiver, ILaunchable.class.getName());
				}
		}
		if("resource".equals(property)) { //$NON-NLS-1$
			IResource res = getResource(receiver);
			if(res != null) {
				return res.isAccessible();
			}
		}
		if("contextlaunch".equals(property)) { //$NON-NLS-1$
			return DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_USE_CONTEXTUAL_LAUNCH);
		}
		if("contextlaunchable".equals(property)) { //$NON-NLS-1$
			try {
				return ContextRunner.getDefault().getLaunchShortcuts(getResource(receiver)).size() > 0 && ContextRunner.getDefault().isSharedConfig(receiver) == null;
			} 
			catch (CoreException e) {return false;}
		}
		return false;
	}

	/**
	 * Returns the resource this property page is open on.
	 * 
	 * @return resource
	 */
	protected IResource getResource(Object element) {
		IResource resource = null;
		if (element instanceof IResource) {
			resource = (IResource) element;
		} else if (element instanceof IAdaptable) {
			resource = (IResource) ((IAdaptable)element).getAdapter(IResource.class);
		}
		return resource;
	}
}
