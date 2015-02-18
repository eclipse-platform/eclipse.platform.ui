/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.model;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ILibrary;

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
 * This class may be instantiated, or further subclassed.
 * </p>
 * @deprecated In Eclipse 3.0 the runtime was refactored and all 
 * non-essential elements removed.  This class provides facilities primarily intended
 * for tooling.  As such it has been removed and no directly substitutable API provided.
 * This API will be deleted in a future release. See bug 370248 for details.
 */
public class LibraryModel extends PluginModelObject {

	// DTD properties (included in plug-in manifest)
	private String[] exports = null;
	private String type = CODE;
	private String[] packagePrefixes = null;

	// transient properties (not included in plug-in manifest)
	private boolean isExported = false;
	private boolean isFullyExported = false;

	/**
	 * Constant string (value "code") indicating the code library type.
	 */
	public static final String CODE = "code"; //$NON-NLS-1$

	/**
	 * Constant string (value "resource") indicating the resource library type.
	 */
	public static final String RESOURCE = "resource"; //$NON-NLS-1$

	/**
	 * Creates a new library model in which all fields
	 * are <code>null</code>.
	 */
	public LibraryModel() {
		super();
	}

	/**
	 * Returns this library's export mask.
	 *
	 * @return this library's export mask or <code>null</code>
	 */
	public String[] getExports() {
		return exports;
	}

	/**
	 * Returns this library's type.  
	 *
	 * @return the type of this library.  The valid types are: <code>CODE</code> and <code>RESOURCE</code>.
	 * @see #CODE
	 * @see #RESOURCE
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns whether or not any of the code in this library is exported.
	 *
	 * @return whether or not any of the code in this library represents is exported
	 */
	public boolean isExported() {
		return isExported;
	}

	/**
	 * Returns whether or not all of the code in this library is exported.
	 *
	 * @return whether or not all of the code in this library is exported
	 */
	public boolean isFullyExported() {
		return isFullyExported;
	}

	/**
	 * Sets this library's export mask.
	 * This object must not be read-only.
	 *
	 * @param value this library's export mask.  May be <code>null</code>.
	 */
	public void setExports(String[] value) {
		assertIsWriteable();
		exports = value;
		if (value == null) {
			isExported = false;
			isFullyExported = false;
		} else {
			for (int i = 0; i < value.length; i++) {
				if (!value[i].equals("")) //$NON-NLS-1$
					isExported = true;
				if (value[i].equals("*")) //$NON-NLS-1$
					isFullyExported = true;
			}
		}
	}

	/**
	 * Sets this library's type. The valid types are: <code>CODE</code> and <code>RESOURCE</code>.
	 * The given type value is canonicalized before being set.
	 * This object must not be read-only.
	 *
	 * @param value the type of this library.
	 * @see #CODE
	 * @see #RESOURCE
	 */
	public void setType(String value) {
		assertIsWriteable();
		String lcValue = value.toLowerCase();
		Assert.isTrue(lcValue.equals(CODE) || lcValue.equals(RESOURCE));
		type = lcValue;
	}

	/**
	 * @see ILibrary#getPackagePrefixes()
	 */
	public String[] getPackagePrefixes() {
		return packagePrefixes;
	}

	/**
	 * Sets this library's package prefixes to be the specified array or <code>null</code>.
	 * 
	 * @param value the list of package prefixes for this library
	 */
	public void setPackagePrefixes(String[] value) {
		packagePrefixes = value;
	}
}
