/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.resources;

import java.util.Map;

/**
 * A builder command names a builder and supplies a table of
 * name-value argument pairs.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IProjectDescription
 */
public interface ICommand {
/**
 * Returns a table of the arguments for this command, or <code>null</code>
 * if there are no arguments. The argument names and values are both strings.
 *
 * @return a table of command arguments (key type : <code>String</code> 
 *		value type : <code>String</code>), or <code>null</code>
 * @see #setArguments
 */
public Map getArguments();
/**
 * Returns the name of the builder to run for this command, or
 * <code>null</code> if the name has not been set.
 *
 * @return the name of the builder, or <code>null</code> if not set
 * @see #setBuilderName
 */
public String getBuilderName();
/**
 * Sets this command's arguments to be the given table of name-values
 * pairs, or to <code>null</code> if there are no arguments. The argument
 * names and values are both strings.
 * <p>
 * Individual builders specify their argument expectations.
 * </p>
 * <p>
 * Note that modifications to the arguments of a command
 * being used in a running builder may affect the run of that builder
 * but will not affect any subsequent runs.  To change a command
 * permanently you must install the command into the relevant project
 * build spec using <code>IProjectDescription.setBuildSpec</code>.
 * </p>
 *
 * @param args a table of command arguments (keys and values must
 *   both be of type <code>String</code>), or <code>null</code>
 * @see #getArguments
 */
public void setArguments(Map args);
/**
 * Sets the name of the builder to run for this command.
 * <p>
 * The builder name comes from the extension that plugs in
 * to the standard <code>org.eclipse.core.resources.builders</code> 
 * extension point.
 * </p>
 *
 * @param builderName the name of the builder
 * @see #getBuilderName
 */
public void setBuilderName(String builderName);
}
