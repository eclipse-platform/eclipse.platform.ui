/*******************************************************************************
 * Copyright (c) 2010 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation
 *     IBM - ongoing development
 *******************************************************************************/
package org.eclipse.core.resources;

/**
 * Represents a path variable contained in an {@link IPathVariableManager}.
 * <p>
 * A path variable is a pair of non-null elements (name,value) where the name is 
 * a case-sensitive string (containing only letters, digits and the underscore
 * character, and not starting with a digit), and the value is an absolute
 * <code>IPath</code> object.
 * </p>
 * <p>
 * Path variables allow for the creation of relative paths whose exact
 * location in the file system depends on the value of a variable. A variable
 * reference may only appear as the first segment of a relative path.
 * </p>
 * 
 * @see org.eclipse.core.runtime.IPath
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 3.6
*/
public interface IPathVariable {
	/**
	 * Returns whether a IPathVariable is read only or not.  Path variables
	 * defined for all projects through the extension point are read only, and
	 * cannot be edited nor removed.
	 * 
	 * @return true if the path variable is read only.
	 */
	public boolean isReadOnly();

	/**
	 * If the variable supports extensions (specified as
	 * "${VARNAME-EXTENSIONNAME}"), this method can return the list of possible
	 * extensions, or null if none are supported.
	 * 
	 * @return the possible variable extensions or null if none are supported.
	 */
	public String[] getExtensions();
}
