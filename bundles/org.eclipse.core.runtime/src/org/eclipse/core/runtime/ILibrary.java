/**********************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.runtime;

import org.eclipse.core.runtime.model.LibraryModel;

 /**
 * A runtime library declared in a plug-in.  Libraries contribute elements to the search path.
 * These contributions are specified as a path to a directory or Jar file.  This path is always
 * considered to be relative to the containing plug-in.  
 * <p>
 * Libraries are typed.  The type is used to determine to which search path the library's
 * contribution should be added.  The valid types are: <code>CODE</code> and
 * <code>RESOURCE</code>.  
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IPluginDescriptor#getRuntimeLibraries 
 */
public interface ILibrary {
	/**
	 * Constant string (value "code") indicating the code library type.
	 * @see LibraryModel#CODE
	 */
	public static final String CODE = LibraryModel.CODE;
	
	/**
	 * Constant string (value "resource") indicating the resource library type.
	 * @see LibraryModel#RESOURCE
	 */
	public static final String RESOURCE = LibraryModel.RESOURCE;
	
/**
 * Returns the content filters, or <code>null</code>.
 * Each content filter identifies a specific class, or
 * a group of classes, using a notation and matching rules
 * equivalent to Java <code>import</code> declarations
 * (e.g., "java.io.File", or "java.io.*"). Returns <code>null</code>
 * if the library is not exported, or it is fully exported
 * (no filtering).
 *
 * @return the content filters, or <code>null</codel> if none
 */
public String[] getContentFilters();
/**
 * Returns the path of this runtime library, relative to the
 * installation location.
 *
 * @return the path of the library
 * @see IPluginDescriptor#getInstallURL
 */
public IPath getPath();
/**
 * Returns this library's type.
 *
 * @return the type of this library.   The valid types are: <code>CODE</code> and <code>RESOURCE</code>.
 * @see #CODE
 * @see #RESOURCE
 */
public String getType();
/**
 * Returns whether the library is exported. The contents of an exported
 * library may be visible to other plug-ins that declare a dependency
 * on the plug-in containing this library, subject to content filtering.
 * Libraries that are not exported are entirely private to the declaring
 * plug-in.
 *
 * @return <code>true</code> if the library is exported, <code>false</code>
 *    if it is private
 */
public boolean isExported();
/**
 * Returns whether this library is fully exported. A library is considered
 * fully exported iff it is exported and has no content filters.
 *
 * @return <code>true</code> if the library is fully exported, and
 *    <code>false</code> if it is private or has filtered content
 */
public boolean isFullyExported();
}
