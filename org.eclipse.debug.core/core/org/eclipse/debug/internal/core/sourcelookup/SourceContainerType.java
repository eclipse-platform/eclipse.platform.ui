/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core.sourcelookup;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.core.sourcelookup.*;

/**
 * Proxy to contributed source container type extension.
 * 
 * @since 3.0
 */
public class SourceContainerType implements ISourceContainerType {
	
	// lazily instantiated delegate
	private ISourceContainerTypeDelegate fDelegate = null;
	
	// extension definition
	private IConfigurationElement fElement = null;
	
	/**
	 * Constructs a source container type on the given extension.
	 * 
	 * @param element extension definition
	 */
	public SourceContainerType(IConfigurationElement element) {
		fElement = element;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType#createSourceContainer(java.lang.String)
	 */
	public ISourceContainer createSourceContainer(String memento) throws CoreException {
		return getDelegate().createSourceContainer(memento);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType#getMemento(org.eclipse.debug.internal.core.sourcelookup.ISourceContainer)
	 */
	public String getMemento(ISourceContainer container) throws CoreException {
		if (this.equals(container.getType())) {
			return getDelegate().getMemento(container);
		}
		SourceLookupUtils.abort(SourceLookupMessages.getString("SourceContainerType.3"), null); //$NON-NLS-1$
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType#getName()
	 */
	public String getName() {
		return fElement.getAttribute("name"); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType#getId()
	 */
	public String getId() {
		return fElement.getAttribute("id"); //$NON-NLS-1$
	}
	
	/**
	 * Lazily instantiates and returns the underlying source container type.
	 * 
	 * @exception CoreException if unable to instantiate
	 */
	private ISourceContainerTypeDelegate getDelegate() throws CoreException {
		if (fDelegate == null) {
			fDelegate = (ISourceContainerTypeDelegate) fElement.createExecutableExtension("class"); //$NON-NLS-1$
		}
		return fDelegate;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType#getDescription()
	 */
	public String getDescription() {
		return fElement.getAttribute("description"); //$NON-NLS-1$
	}
}
