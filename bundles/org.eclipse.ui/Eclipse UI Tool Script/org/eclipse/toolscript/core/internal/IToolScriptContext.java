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
	 * Returns the targets for an Ant script. The
	 * targets are collected from the corresponding
	 * variable tags in the script's arguments.
	 */
	public String[] getAntTargets();
	
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
	
	/**
	 * Returns whether or not the execution log for the
	 * script should appear on the log console.
	 */
	public boolean getShowLog();
}
