/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.filebuffers;

import org.eclipse.core.runtime.IPath;


/**
 * Type-safe enum of the available location kinds.
 * 
 * @since 3.3
 */
public final class LocationKind {
	
	/**
	 * The corresponding argument is a location
	 * in in the file system.
	 */
	public static final LocationKind LOCATION= new LocationKind("location"); //$NON-NLS-1$
	
	/**
	 * The corresponding argument is the full path
	 * of an {@link org.eclipse.core.resources.IFile}.
	 * 
	 * @since 3.3
	 */
	public static final LocationKind IFILE= new LocationKind("IFile"); //$NON-NLS-1$
	
	/**
	 * Tells to normalize the corresponding argument according
	 * to {@link FileBuffers#normalizeLocation(IPath)}.
	 * </p>
	 * 
	 * @since 3.3
	 */
	public static final LocationKind NORMALIZE= new LocationKind("normalize"); //$NON-NLS-1$

	
	private final String fName;
	
	LocationKind(String name) {
		fName= name;
	}
	
	/*
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return fName;
	}
}