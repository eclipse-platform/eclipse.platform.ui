/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * @since 3.1 
 */
public class ThirdLevelConfigurationElementHandle extends ConfigurationElementHandle {

    public ThirdLevelConfigurationElementHandle(IObjectManager objectManager, int id) {
        super(objectManager, id);
    }
	
    protected ConfigurationElement getConfigurationElement() {
		return (ConfigurationElement) objectManager.getObject(getId(), RegistryObjectManager.THIRDLEVEL_CONFIGURATION_ELEMENT);
	}
	
	public IConfigurationElement[] getChildren() {
	    return (IConfigurationElement[]) objectManager.getHandles(getConfigurationElement().getRawChildren(), RegistryObjectManager.THIRDLEVEL_CONFIGURATION_ELEMENT);
	}
	
}
