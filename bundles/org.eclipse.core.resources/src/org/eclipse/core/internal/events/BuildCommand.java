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
package org.eclipse.core.internal.events;

import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.resources.ModelObject;
import org.eclipse.core.resources.ICommand;
import java.util.*;
public class BuildCommand extends ModelObject implements ICommand {
	protected HashMap arguments;
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
	return result;
}
public boolean equals(Object object) {
	if (this == object)
		return true;
	if (!(object instanceof BuildCommand))
		return false;
	BuildCommand command = (BuildCommand) object;
	// equal if same builder name and equal argument tables
	return getBuilderName().equals(command.getBuilderName()) &&
		getArguments(false).equals(command.getArguments(false));
}
/**
 * @see ICommand#getArguments
 */
public Map getArguments() {
	return getArguments(true);
}
public Map getArguments(boolean makeCopy) {
	return arguments == null ? null : (makeCopy ? (Map) arguments.clone() : arguments);
}
/**
 * @see ICommand#getBuilderName
 */
public String getBuilderName() {
	return getName();
}
public int hashCode() {
	// hash on name alone
	return getName().hashCode();
}
/**
 * @see ICommand#setArguments
 */
public void setArguments(Map value) {
	// copy parameter for safety's sake
	arguments = value == null ? null : new HashMap(value);
}
/**
 * @see ICommand#setBuilderName
 */
public void setBuilderName(String value) {
	//don't allow builder name to be null
	setName(value == null ? "" : value); //$NON-NLS-1$
}
}
