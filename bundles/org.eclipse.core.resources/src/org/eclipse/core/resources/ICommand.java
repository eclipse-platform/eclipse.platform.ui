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
package org.eclipse.core.resources;

import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;

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
	 * @see #setArguments(Map)
	 */
	public Map getArguments();

	/**
	 * Returns the name of the builder to run for this command, or
	 * <code>null</code> if the name has not been set.
	 *
	 * @return the name of the builder, or <code>null</code> if not set
	 * @see #setBuilderName(String)
	 */
	public String getBuilderName();

	/**
	 * Returns whether this build command responds to the given trigger.
	 * <p>
	 * By default, build commands respond to all build triggers.
	 * 
	 * @param trigger One of the <tt>*_BUILD</code> constants defined
	 * on <code>IncrementalProjectBuilder</code>
	 * @return <code>true</code> if this build command responds to the specified
	 * trigger, and <code>false</code> otherwise.
	 * @see #setBuilding(int, boolean)
	 * @since 3.1
	 */
	public boolean isBuilding(int trigger);

	/**
	 * Returns whether this command allows configuring of what triggers
	 * it responds to.  By default, commands are only configurable
	 * if the corresponding builder defines the <code>isConfigurable</code>
	 * attribute in its builder extension declaration. A command that is not 
	 * configurable will always respond to all build triggers.
	 * 
	 * @return <code>true</code> If this command allows configuration of
	 * its triggers, and <code>false</code> otherwise.
	 * @see #setBuilding(int, boolean)
	 * @since 3.1
	 */
	public boolean isConfigurable();

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
	 * @see #getArguments()
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
	 * @see #getBuilderName()
	 */
	public void setBuilderName(String builderName);

	/**
	 * Specifies whether this build command responds to the provided trigger.
	 * <p>
	 * When a command is configured to not respond to a given trigger, the
	 * builder instance will not be called when a build with that trigger is initiated.
	 * <p>
	 * This method has no effect if this build command does not allow its
	 * build triggers to be configured.
	 * 
	 * @param trigger One of the <tt>*_BUILD</code> constants defined
	 * on <code>IncrementalProjectBuilder</code>
	 * @param value <code>true</code> if this build command responds to the 
	 * specified trigger, and <code>false</code> otherwise.
	 * @see #isBuilding(int)
	 * @see #isConfigurable()
	 * @see IWorkspace#build(int, IProgressMonitor)
	 * @see IProject#build(int, IProgressMonitor)
	 * @since 3.1
	 */
	public void setBuilding(int trigger, boolean value);
}