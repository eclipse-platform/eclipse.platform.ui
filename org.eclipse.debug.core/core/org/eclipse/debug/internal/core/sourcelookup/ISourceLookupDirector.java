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

import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * A source lookup director directs the source lookup process
 * among a set of participants and source containers.
 * 
 * @since 3.0
 */
public interface ISourceLookupDirector extends IPersistableSourceLocator2 {
	
	/**
	 * Returns the launch configuration associated with this source 
	 * lookup director, or <code>null</code> if none.
	 * 
	 * @return the launch configuration associated with this source 
	 * lookup director, or <code>null</code> if none
	 */
	public ILaunchConfiguration getLaunchConfiguration();
	
	/**
	 * Returns the source lookup participants currently registered with
	 * this director, possibly an empty collection.
	 * 
	 * @return the source lookup participants currently registered with
	 * this director, possibly an empty collection
	 */
	public ISourceLookupParticipant[] getParticipants();
	
	/**
	 * Returns the source containers currently registered with this 
	 * director, possibly an empty collection.
	 * 
	 * @return the source containers currently registered with this 
	 * director, possibly an empty collection
	 */
	public ISourceContainer[] getSourceContainers();
	
	/**
	 * Sets the source containers this source lookup director
	 * should search when looking for source, possibly an empty collection.
	 * 
	 * @param containers the source containers this source lookup director
	 * should search when looking for source, possibly an empty collection
	 */
	public void setSourceContainers(ISourceContainer[] containers);	
	
	/**
	 * Returns whether to search exhaustively for all source elements
	 * with the same name in all registered source containers, or
	 * whether to stop searching when the first source element matching
	 * the required name is found.
	 * 
	 * @return whether to search exhaustively for all source elements
	 * with the same name
	 */
	public boolean isFindDuplicates();
	
	/**
	 * Sets whether to search exhaustively for all source elements
	 * with the same name in all registered source containers, or
	 * whether to stop searching when the first source element matching
	 * the required name is found.
	 * 
	 * @param findDuplicates whether to search exhaustively for all source elements
	 * with the same name
	 */
	public void setFindDuplicates(boolean findDuplicates);	
	
	/**
	 * Notifies this source lookup director that it should initialize
	 * its set of source lookup participants.
	 */
	public void initializeParticipants();
	
	/**
	 * Returns whether this source director supports the given type
	 * of source location. 
	 * 
	 * @param type source container type
	 * @return whether this source director supports the given type
	 * of source location
	 */
	public boolean supportsSourceContainerType(ISourceContainerType type);
}
