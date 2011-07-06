/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.*;

/**
 * Proxy to a logical structure type extension.
 * 
 * @see IConfigurationElementConstants
 */
public class LogicalStructureType implements ILogicalStructureType {

	private IConfigurationElement fConfigurationElement;
	private ILogicalStructureTypeDelegate fDelegate;
	private String fModelId;
	// whether the 'description' attribute has been verified to exist: it is only
	// required when the delegate does *not* implement ILogicalStructureTypeDelegate2.
	private boolean fVerifiedDescription = false; 
	
	/**
	 * Constructs a new logical structure type, and verifies required attributes.
	 * 
	 * @param element configuration element
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
		verifyAttributeExists(IConfigurationElementConstants.ID);
		verifyAttributeExists(IConfigurationElementConstants.CLASS);
		fModelId = fConfigurationElement.getAttribute(IConfigurationElementConstants.MODEL_IDENTIFIER); 
		if (fModelId == null) {
			missingAttribute(IConfigurationElementConstants.MODEL_IDENTIFIER);
		}
	}
	
	/**
	 * Verifies the given attribute exists
	 * @param name the name to verify
	 * 
	 * @exception CoreException if attribute does not exist
	 */
	private void verifyAttributeExists(String name) throws CoreException {
		if (fConfigurationElement.getAttribute(name) == null) {
			missingAttribute(name);
		}
	}

	/**
	 * Throws a new <code>CoreException</code> about the specified attribute being missing
	 * @param attrName the name of the missing attribute
	 * @throws CoreException if a problem is encountered
	 */
	private void missingAttribute(String attrName) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.ERROR, MessageFormat.format(DebugCoreMessages.LogicalStructureType_1, new String[]{attrName}), null));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.ILogicalStructureType#getDescription()
	 */
	public String getDescription() {
		return fConfigurationElement.getAttribute(IConfigurationElementConstants.DESCRIPTION);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.ILogicalStructureType#getId()
	 */
	public String getId() {
		return fConfigurationElement.getAttribute(IConfigurationElementConstants.ID);
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

	/**
	 * Returns the <code>ILogicalStructuresTypeDelegate</code> delegate
	 * @return the delegate
	 */
	protected ILogicalStructureTypeDelegate getDelegate() {
		if (fDelegate == null) {
			try {
				fDelegate = (ILogicalStructureTypeDelegate) fConfigurationElement.createExecutableExtension(IConfigurationElementConstants.CLASS);
			} catch (CoreException e) {
				DebugPlugin.log(e);
			}
		}
		return fDelegate;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILogicalStructureTypeDelegate2#getDescription(org.eclipse.debug.core.model.IValue)
	 */
	public String getDescription(IValue value) {
		ILogicalStructureTypeDelegate delegate = getDelegate();
		if (delegate instanceof ILogicalStructureTypeDelegate2) {
			ILogicalStructureTypeDelegate2 d2 = (ILogicalStructureTypeDelegate2) delegate;
			return d2.getDescription(value);
		}
		if (!fVerifiedDescription) {
		    fVerifiedDescription = true;
		    try {
                verifyAttributeExists(IConfigurationElementConstants.DESCRIPTION);
            } catch (CoreException e) {
                DebugPlugin.log(e);
            }
		}
		String description = getDescription();
		if (description == null) {
		    return DebugCoreMessages.LogicalStructureType_0; 
		}
		return description;
	}
}
