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
package org.eclipse.debug.internal.core.sourcelookup;

import org.eclipse.core.runtime.CoreException;

/**
 * A source lookup participant participates in source lookup by searching an ordered
 * list of source containers for source elements corresponding to a debug artifact.
 * For example, a participant may be asked to find source corresponding to a stack
 * frame or breakpoint. An implementation of a source lookup participant is debug
 * model specific, since it must convert the debug model artifact (stack frame,
 * breakpoint, etc.),  into a source name that can be recognized by a source container
 * (<code>ISourceContainer</code>), to search for source elements. Source containers
 * are generally debug model independant, whereas source lookup participants are
 * debug model specific.  
 *  
 * @since 3.0
 */
public interface ISourceLookupParticipant {

	/**
	 * Sets the source containers to consider when searching for source elements. 
     *
	 * @param containers the source containers to consider when seaching
	 *  for source elements
	 */
	public void setSourceContainers(ISourceContainer[] containers);
	
	/**
	 * Returns a collection of source elements corresponding to the given debug
	 * artifact (for example, a stack frame or breakpoint). Returns an empty
	 * collection if no source elements are found. When <code>findDuplicates</code>
	 * is <code>false</code> the returned collection should contain at most
	 * one source element.
	 * <p>
	 * If the given debug artifact is not recognized by this participant, an empty
	 * collection is returned. Otherwise, this participant generates a source name
	 * from the given artifact and performs a search for associated source elements
	 * in its source containers.
	 * </p>
	 * @param object the debug artifact for which source needs to be found (e.g., stack frame)
	 * @param findDuplicates whether searching should continue after the first match is
	 *  found, or if all source locations should be searched exhaustively for all potential
	 *  source elements corresponding to the given debug artifact	
	 * @return a collection of source elements corresponding to the given
	 *  debug artifact, possibly empty
	 * @exception CoreException if an exception occurrs while searching for source
	 */
	public Object[] findSourceElements(Object object, boolean findDuplicates) throws CoreException;

	
}
