/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.resources;

import java.net.URI;
import org.eclipse.core.runtime.*;

/**
 * Manages a collection of path variables and resolves paths containing a
 * variable reference.
 * <p>
 * A path variable is a pair of non-null elements (name,value) where name is 
 * a case-sensitive string (containing only letters, digits and the underscore
 * character, and not starting with a digit), and value is an absolute
 * <code>IPath</code> object.
 * </p>
 * <p>
 * Path variables allow for the creation of relative paths whose exact
 * location in the file system depends on the value of a variable. A variable
 * reference may only appear as the first segment of a relative path.
 * </p>
 * 
 * @see org.eclipse.core.runtime.IPath
 * @since 2.1
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPathVariableManager {

	/**
	 * Sets the path variable with the given name to be the specified value.
	 * Depending on the value given and if the variable is currently defined
	 * or not, there are several possible outcomes for this operation:
	 * <p>
	 * <ul>
	 * <li>A new variable will be created, if there is no variable defined with
	 * the given name, and the given value is not <code>null</code>.
	 * </li>
	 * 
	 * <li>The referred variable's value will be changed, if it already exists
	 * and the given value is not <code>null</code>.</li>
	 * 
	 * <li>The referred variable will be removed, if a variable with the given
	 * name is currently defined and the given value is <code>null</code>.
	 * </li>
	 *  
	 * <li>The call will be ignored, if a variable with the given name is not
	 * currently defined and the given value is <code>null</code>, or if it is
	 * defined but the given value is equal to its current value.
	 * </li>
	 * </ul>
	 * <p>If a variable is effectively changed, created or removed by a call to
	 * this method, notification will be sent to all registered listeners.</p>
	 * 
	 * @param name the name of the variable 
	 * @param value the value for the variable (may be <code>null</code>)
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li>The variable name is not valid</li>
	 * <li>The variable value is relative</li>
	 * </ul>
	 */
	public void setValue(String name, IPath value) throws CoreException;

	/**
	 * Returns the value of the path variable with the given name. If there is
	 * no variable defined with the given name, returns <code>null</code>.
	 * 
	 * @param name the name of the variable to return the value for  
	 * @return the value for the variable, or <code>null</code> if there is no
	 *    variable defined with the given name
	 */
	public IPath getValue(String name);

	/**
	 * Returns an array containing all defined path variable names.
	 *  
	 * @return an array containing all defined path variable names
	 */
	public String[] getPathVariableNames();

	/**
	 * Registers the given listener to receive notification of changes to path
	 * variables. The listener will be notified whenever a variable has been
	 * added, removed or had its value changed. Has no effect if an identical
	 * path variable change listener is already registered.
	 * 
	 * @param listener the listener
	 * @see IPathVariableChangeListener
	 */
	public void addChangeListener(IPathVariableChangeListener listener);

	/**
	 * Removes the given path variable change listener from the listeners list.
	 * Has no effect if an identical listener is not registered.
	 * 
	 * @param listener the listener 
	 * @see IPathVariableChangeListener
	 */
	public void removeChangeListener(IPathVariableChangeListener listener);

	/**
	 * Resolves a relative <code>URI</code> object potentially containing a
	 * variable reference as its first segment, replacing the variable reference
	 * (if any) with the variable's value (which is a concrete absolute URI).
	 * If the given URI is absolute or has a non- <code>null</code> device then
	 * no variable substitution is done and that URI is returned as is.  If the
	 * given URI is relative and has a <code>null</code> device, but the first
	 * segment does not correspond to a defined variable, then the URI is
	 * returned as is.
	 * <p>
	 * If the given URI is <code>null</code> then <code>null</code> will be
	 * returned.  In all other cases the result will be non-<code>null</code>.
	 * </p>
	 * 
	 * @param uri  the URI to be resolved
	 * @return the resolved URI or <code>null</code>
	 * @since 3.2
	 */
	public URI resolveURI(URI uri);

	/**
	 * Resolves a relative <code>IPath</code> object potentially containing a
	 * variable reference as its first segment, replacing the variable reference
	 * (if any) with the variable's value (which is a concrete absolute path).
	 * If the given path is absolute or has a non- <code>null</code> device then
	 * no variable substitution is done and that path is returned as is.  If the
	 * given path is relative and has a <code>null</code> device, but the first
	 * segment does not correspond to a defined variable, then the path is
	 * returned as is.
	 * <p>
	 * If the given path is <code>null</code> then <code>null</code> will be
	 * returned.  In all other cases the result will be non-<code>null</code>.
	 * </p>
	 * 
	 * <p>
	 * For example, consider the following collection of path variables:
	 * </p>
	 * <ul>
	 * <li>TEMP = c:/temp</li>
	 * <li>BACKUP = /tmp/backup</li>
	 * </ul>
	 * <p>The following paths would be resolved as:
	 * <p>c:/bin => c:/bin</p>
	 * <p>c:TEMP => c:TEMP</p>
	 * <p>/TEMP => /TEMP</p>
	 * <p>TEMP  => c:/temp</p>
	 * <p>TEMP/foo  => c:/temp/foo</p>
	 * <p>BACKUP  => /tmp/backup</p>
	 * <p>BACKUP/bar.txt  => /tmp/backup/bar.txt</p>
	 * <p>SOMEPATH/foo => SOMEPATH/foo</p></p>
	 * 
	 * @param path the path to be resolved
	 * @return the resolved path or <code>null</code>
	 */
	public IPath resolvePath(IPath path);

	/**
	 * Returns <code>true</code> if the given variable is defined and
	 * <code>false</code> otherwise. Returns <code>false</code> if the given
	 * name is not a valid path variable name.
	 * 
	 * @param name the variable's name
	 * @return <code>true</code> if the variable exists, <code>false</code>
	 *    otherwise
	 */
	public boolean isDefined(String name);

	/**
	 * Validates the given name as the name for a path variable. A valid path
	 * variable name is made exclusively of letters, digits and the underscore
	 * character, and does not start with a digit.
	 * 
	 * @param name a possibly valid path variable name
	 * @return a status object with code <code>IStatus.OK</code> if
	 *    the given name is a valid path variable name, otherwise a status
	 *    object indicating what is wrong with the string
	 * @see IStatus#OK
	 */
	public IStatus validateName(String name);

	/**
	 * Validates the given path as the value for a path variable. A path
	 * variable value must be a valid path that is absolute.
	 *
	 * @param path a possibly valid path variable value
	 * @return a status object with code <code>IStatus.OK</code> if the given
	 * path is a valid path variable value, otherwise a status object indicating
	 * what is wrong with the value
	 * @see IPath#isValidPath(String)
	 * @see IStatus#OK
	 */
	public IStatus validateValue(IPath path);
}
