package org.eclipse.toolscript.core.internal;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/

/**
 * Represents the context in which to run the
 * tool script.
 */
public interface IToolScriptContext {
	/**
	 * Returns the name of the script.
	 */
	public String getName();
	
	/**
	 * Returns the path where the script is located. All
	 * variables embedded in the path have been fully
	 * expanded.
	 */
	public String getExpandedLocation();
	
	/**
	 * Returns the arguments for the script. All
	 * variables embedded in the arguments have been fully
	 * expanded.
	 */
	public String getExpandedArguments();
	
	/**
	 * Returns the working directory to run the script in.
	 * All variables embedded in the path have been fully
	 * expanded.
	 */
	public String getExpandedWorkingDirectory();
}
