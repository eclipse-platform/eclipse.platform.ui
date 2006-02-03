/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

import org.eclipse.core.runtime.IPluginDescriptor;

/**
 * This class contains only compatibility-specific code.
 * 
 * @deprecated marked as deprecated to supress warnings
 */
public class ExtensionPointHandle extends BaseExtensionPointHandle {

	public ExtensionPointHandle(IObjectManager objectManager, int id) {
		super(objectManager, id);
	}

	public IPluginDescriptor getDeclaringPluginDescriptor() {
		return RegistryCompatibilityHelper.getPluginDescriptor(getNamespace());
	}
}
