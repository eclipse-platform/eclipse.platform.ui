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
 * A tool script consist of a user defined name, a path to the location
 * of the script, optional arguments for the script, and the working
 * directory.
 */
public interface IToolScript {
	/**
	 * Returns the name of the script.
	 */
	public String getName();
	
	/**
	 * Returns the path where the script is located.
	 */
	public String getLocation();
	
	/**
	 * Returns the arguments for the script.
	 */
	public String getArguments();
	
	/**
	 * Returns the working directory to run the script in.
	 */
	public String getWorkingDirectory();
}
