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
package org.eclipse.debug.internal.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IValue;

/**
 * Proxy to a logical structure type extension.
 */
public class LogicalStructureType implements ILogicalStructureType {

	private IConfigurationElement fConfigurationElement;
	private ILogicalStructureTypeDelegate fDelegate;
	private String fModelId;
	
	/**
	 * Constructs a new logical structure type, and verifies required attributes.
	 * 
	 * @exception CoreException if required attributes are missing
	 */
	public LogicalStructureType(IConfigurationElement element) throws CoreException {
		fConfigurationElement = element;
		verifyAttributes();
	}

	/**
	 * Verifies required attributes.
	 * 
	 * @exception CoreException if required attributes are missing
	 */
	private void verifyAttributes() throws CoreException {
		// TODO:
		fModelId = fConfigurationElement.getAttribute("modelIdentifier"); //$NON-NLS-1$
		if (fModelId == null) {
			// TODO: exception
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.ILogicalStructureType#getDescription()
	 */
	public String getDescription() {
		return fConfigurationElement.getAttribute("description"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.ILogicalStructureType#getLogicalStructure(org.eclipse.debug.core.model.IValue)
	 */
	public IValue getLogicalStructure(IValue value) throws CoreException {
		return getDelegate().getLogicalStructure(value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.ILogicalStructureType#providesLogicalStructure(org.eclipse.debug.core.model.IValue)
	 */
	public boolean providesLogicalStructure(IValue value) {
		if (value.getModelIdentifier().equals(fModelId)) {
			return getDelegate().providesLogicalStructure(value);
		}
		return false;
	}

	protected ILogicalStructureTypeDelegate getDelegate() {
		if (fDelegate == null) {
			try {
				fDelegate = (ILogicalStructureTypeDelegate) fConfigurationElement.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				DebugPlugin.log(e);
			}
		}
		return fDelegate;
	}
}
