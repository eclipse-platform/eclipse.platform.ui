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
package org.eclipse.debug.internal.core.sourcelookup.containers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.internal.core.sourcelookup.ISourcePathComputer;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupMessages;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupUtils;

/**
 * A source container that can be used as the root container for default source containers.
 * Listens to the configuration file and marks itself as dirty when changes have occurred.
 * When dirty, it will refresh it's children before the next time it is searched or displayed.
 * 
 * @since 3.0
 */
public class DefaultSourceContainer extends CompositeSourceContainer {

	private ILaunchConfiguration fConfiguration = null;  
	
	/**
	 * Constructs a source container with the default source containers
	 * for the given launch configuration. 
	 */
	public DefaultSourceContainer(ILaunchConfiguration configuration) {
		fConfiguration = configuration;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return obj instanceof DefaultSourceContainer &&
		  getLaunchConfiguration().equals(((DefaultSourceContainer)obj).getLaunchConfiguration());
	}	

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getClass().hashCode();
	}	
	
	/**
	 * Returns the launch configuration for which a default source lookup
	 * path will be computed.
	 * 
	 * @return the launch configuration for which a default source lookup
	 * path will be computed
	 */
	public ILaunchConfiguration getLaunchConfiguration() {
		return fConfiguration;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return SourceLookupUtils.getSourceContainerType(DefaultSourceContainerType.TYPE_ID);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getSourceContainers(ILaunchConfiguration)
	 */
	public ISourceContainer[] getSourceContainers() {
		ISourcePathComputer sourcePathComputer = getSourcePathComputer();
		if (sourcePathComputer == null) {
			return new ISourceContainer[0];
		}
		return sourcePathComputer.computeSourceContainers(getLaunchConfiguration(), null);
	}
	
	/**
	 * Returns the source path computer to use, or <code>null</code>
	 * if none.
	 * 
	 * @return the source path computer to use, or <code>null</code>
	 * if none
	 */
	private ISourcePathComputer getSourcePathComputer() {
		String id;
		try {
			id = fConfiguration.getAttribute(ISourcePathComputer.ATTR_SOURCE_PATH_COMPUTER_ID, (String)null);
		} catch (CoreException e) {
			return null;
		}
		if (id == null) {
			return null;
		}
		return SourceLookupUtils.getSourcePathComputer(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getName()
	 */
	public String getName() {
		return SourceLookupMessages.getString("DefaultSourceContainer.0"); //$NON-NLS-1$
	}

}
