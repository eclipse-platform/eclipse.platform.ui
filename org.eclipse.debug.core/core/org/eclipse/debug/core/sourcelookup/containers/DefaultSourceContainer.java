/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.sourcelookup.containers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupMessages;

/**
 * A source container that computers the default source lookup path
 * for a launch configuration on each launch using a launch configuration's
 * associated source path computer.
 * <p>
 * Clients may instantiate this class. This class is not intended to
 * be subclassed.
 * </p>
 * @since 3.0
 */
public class DefaultSourceContainer extends CompositeSourceContainer {  
	
	/**
	 * Unique identifier for the default source container type
	 * (value <code>org.eclipse.debug.core.containerType.default</code>).
	 */
	public static final String TYPE_ID = DebugPlugin.getUniqueIdentifier() + ".containerType.default"; //$NON-NLS-1$

	/**
	 * Constructs a default source container. 
	 */
	public DefaultSourceContainer() {
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return obj instanceof DefaultSourceContainer;
	}	

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getClass().hashCode();
	}	
	
	/**
	 * Returns the launch configuration for which a default source lookup
	 * path will be computed, or <code>null</code> if none.
	 * 
	 * @return the launch configuration for which a default source lookup
	 * path will be computed, or <code>null</code>
	 */
	protected ILaunchConfiguration getLaunchConfiguration() {
		ISourceLookupDirector director = getDirector();
		if (director != null) {
			return director.getLaunchConfiguration();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}
	
	/**
	 * Returns the source path computer to use, or <code>null</code>
	 * if none.
	 * 
	 * @return the source path computer to use, or <code>null</code>
	 * if none
	 */
	private ISourcePathComputer getSourcePathComputer() {	
		return getDirector().getSourcePathComputer();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getName()
	 */
	public String getName() {
		return SourceLookupMessages.getString("DefaultSourceContainer.0"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.containers.CompositeSourceContainer#createSourceContainers()
	 */
	protected ISourceContainer[] createSourceContainers() throws CoreException {
		ISourcePathComputer sourcePathComputer = getSourcePathComputer();
		if (sourcePathComputer == null) {
			return new ISourceContainer[0];
		}
		return sourcePathComputer.computeSourceContainers(getLaunchConfiguration(), null);
	}
}
