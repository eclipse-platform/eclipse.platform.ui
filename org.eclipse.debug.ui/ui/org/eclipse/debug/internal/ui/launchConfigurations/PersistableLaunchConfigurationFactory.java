/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * This is a combination factory and persistable element for launch configurations.
 * This is necessary because the IPersistableElement/IElementFactory framework used by
 * the platform working set support live in the UI plugin.  Launch configurations are
 * defined in a non-UI plugin, thus we need this class to handle persistence and 
 * recreation on behalf of launch configs.
 * 
 */
public class PersistableLaunchConfigurationFactory implements IPersistableElement, IElementFactory {

	private ILaunchConfiguration fConfig;
	
	private static final String KEY = "launchConfigMemento"; //$NON-NLS-1$
	private static final String FACTORY_ID = "org.eclipse.debug.ui.PersistableLaunchConfigurationFactory"; //$NON-NLS-1$

	public PersistableLaunchConfigurationFactory() {
	}

	public PersistableLaunchConfigurationFactory(ILaunchConfiguration config) {
		setConfig(config);
	}

	/**
	 * @see org.eclipse.ui.IPersistableElement#getFactoryId()
	 */
	public String getFactoryId() {
		return FACTORY_ID;
	}

	/**
	 * @see org.eclipse.ui.IPersistableElement#saveState(IMemento)
	 */
	public void saveState(IMemento memento) {
		try {
			String configMemento = getConfig().getMemento();
			memento.putString(KEY, configMemento);		
		} catch (CoreException ce) {
		}
	}

	/**
	 * @see org.eclipse.ui.IElementFactory#createElement(IMemento)
	 */
	public IAdaptable createElement(IMemento memento) {
		try {
			String launchConfigMemento = memento.getString(KEY);
			return getLaunchManager().getLaunchConfiguration(launchConfigMemento);
		} catch (CoreException ce) {
		}
		return null;
	}

	private void setConfig(ILaunchConfiguration config) {
		fConfig = config;
	}

	private ILaunchConfiguration getConfig() {
		return fConfig;
	}
	
	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

}
