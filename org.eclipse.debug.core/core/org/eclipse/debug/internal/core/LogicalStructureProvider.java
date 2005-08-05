/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILogicalStructureProvider;
import org.eclipse.debug.core.ILogicalStructureType;
import org.eclipse.debug.core.model.IValue;

/**
 * Manage logical structure provider extensions
 */
public class LogicalStructureProvider {

	private IConfigurationElement fConfigurationElement;
	
	private String fModelIdentifier;

	private ILogicalStructureProvider fDelegate;

	public LogicalStructureProvider(IConfigurationElement element) throws CoreException {
		fConfigurationElement= element;
		fModelIdentifier= fConfigurationElement.getAttribute("modelIdentifier"); //$NON-NLS-1$
		if (fModelIdentifier == null) {
			throw new CoreException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, DebugCoreMessages.LogicalStructureProvider_0, null)); 
		}
		String className= fConfigurationElement.getAttribute("class"); //$NON-NLS-1$
		if (className == null) {
			throw new CoreException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, DebugCoreMessages.LogicalStructureProvider_1, null)); 
		}
	}
	
	/**
	 * Return the logical structure type able to provide a logical structure for
	 * the given value.
	 */
	public ILogicalStructureType[] getLogicalStructures(IValue value) {
		if (fModelIdentifier.equals(value.getModelIdentifier())) {
			return getDelegate().getLogicalStructureTypes(value);
		}
		return new ILogicalStructureType[0];
	}

	/**
	 * Return the ILogicalStructureProvider for this extension.
	 */
	protected ILogicalStructureProvider getDelegate() {
		if (fDelegate == null) {
			try {
				fDelegate = (ILogicalStructureProvider) fConfigurationElement.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				DebugPlugin.log(e);
			}
		}
		return fDelegate;
	}

}
