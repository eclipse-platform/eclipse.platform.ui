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

 
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * This is a combination factory and persistable element for launch configuration types.
 * This is necessary because the IPersistableElement/IElementFactory framework used by
 * the platform working set support live in the UI plugin.  Launch configuration types are
 * defined in a non-UI plugin, thus we need this class to handle persistence and 
 * recreation on behalf of launch config types.
 * 
 */
public class PersistableLaunchConfigurationTypeFactory implements IPersistableElement, IElementFactory {

	private ILaunchConfigurationType fConfigType;

	private static final String KEY = "launchConfigTypeID"; //$NON-NLS-1$
	private static final String FACTORY_ID = "org.eclipse.debug.ui.PersistableLaunchConfigurationTypeFactory"; //$NON-NLS-1$

	public PersistableLaunchConfigurationTypeFactory() {
	}

	public PersistableLaunchConfigurationTypeFactory(ILaunchConfigurationType configType) {
		setConfigType(configType);
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
		String configTypeID = getConfigType().getIdentifier();
		memento.putString(KEY, configTypeID);
	}

	/**
	 * @see org.eclipse.ui.IElementFactory#createElement(IMemento)
	 */
	public IAdaptable createElement(IMemento memento) {
		String configTypeID = memento.getString(KEY);
		return getLaunchManager().getLaunchConfigurationType(configTypeID);
	}

	private void setConfigType(ILaunchConfigurationType configType) {
		fConfigType = configType;
	}

	private ILaunchConfigurationType getConfigType() {
		return fConfigType;
	}

	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

}
