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
package org.eclipse.core.internal.events;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.internal.resources.ModelObject;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IncrementalProjectBuilder;

public class BuildCommand extends ModelObject implements ICommand {
	protected HashMap arguments;
	/**
	 * The builder instance for this comment. Null if the builder has
	 * not yet been instantiated.
	 */
	protected IncrementalProjectBuilder builder;

	public BuildCommand() {
		super(""); //$NON-NLS-1$
		this.arguments = new HashMap(0);
	}

	public Object clone() {
		BuildCommand result = null;
		result = (BuildCommand) super.clone();
		if (result == null)
			return null;
		result.setArguments(getArguments());
		//don't let references to builder instances leak out because they reference trees
		result.setBuilder(null);
		return result;
	}

	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (!(object instanceof BuildCommand))
			return false;
		BuildCommand command = (BuildCommand) object;
		// equal if same builder name and equal argument tables
		return getBuilderName().equals(command.getBuilderName()) && getArguments(false).equals(command.getArguments(false));
	}

	/**
	 * @see ICommand#getArguments()
	 */
	public Map getArguments() {
		return getArguments(true);
	}

	public Map getArguments(boolean makeCopy) {
		return arguments == null ? null : (makeCopy ? (Map) arguments.clone() : arguments);
	}

	public IncrementalProjectBuilder getBuilder() {
		return builder;
	}

	/**
	 * @see ICommand#getBuilderName()
	 */
	public String getBuilderName() {
		return getName();
	}

	public int hashCode() {
		// hash on name alone
		return getName().hashCode();
	}

	/**
	 * @see ICommand#setArguments(Map)
	 */
	public void setArguments(Map value) {
		// copy parameter for safety's sake
		arguments = value == null ? null : new HashMap(value);
	}

	public void setBuilder(IncrementalProjectBuilder builder) {
		this.builder = builder;
	}

	/**
	 * @see ICommand#setBuilderName(String)
	 */
	public void setBuilderName(String value) {
		//don't allow builder name to be null
		setName(value == null ? "" : value); //$NON-NLS-1$
	}

	/**
	 * For debugging purposes only
	 */
	public String toString() {
		return "BuildCommand(" + getName() + ")";//$NON-NLS-1$ //$NON-NLS-2$
	}
}