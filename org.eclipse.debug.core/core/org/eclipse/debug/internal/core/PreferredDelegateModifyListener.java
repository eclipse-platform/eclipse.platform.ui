/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferenceNodeVisitor;
import org.eclipse.core.runtime.preferences.PreferenceModifyListener;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Used to listen for preference imports that include changes to 
 * preferred launch delegates.
 * 
 * @since 3.6
 */
public class PreferredDelegateModifyListener extends PreferenceModifyListener {
	class Visitor implements IPreferenceNodeVisitor {

		public boolean visit(IEclipsePreferences node) throws BackingStoreException {
			if (node.name().equals(DebugPlugin.getUniqueIdentifier())) {
				// reset preferred delegates, so they are re-initialized from the preferences
				LaunchManager manager = (LaunchManager)DebugPlugin.getDefault().getLaunchManager();
				manager.resetPreferredDelegates();
				ILaunchConfigurationType[] types = manager.getLaunchConfigurationTypes();
				for (int i = 0; i < types.length; i++) {
					((LaunchConfigurationType) types[i]).resetPreferredDelegates();
				}
				return false;
			}
			return true;
		}
		
	}

	public IEclipsePreferences preApply(IEclipsePreferences node) {
		try {
			// force VMs to be initialized before we import the new VMs
			node.accept(new Visitor());
		} catch (BackingStoreException e) {
            DebugPlugin.log(e);
		}
		return node;
	}

}
