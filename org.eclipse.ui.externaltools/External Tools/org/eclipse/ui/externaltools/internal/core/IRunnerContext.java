package org.eclipse.ui.externaltools.internal.core;

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
 * external tools.
 */
public interface IRunnerContext {
	/**
	 * Returns the name of the external tool.
	 */
	public String getName();
	
	/**
	 * Returns the path where the external tool is located. All
	 * variables embedded in the path have been fully
	 * expanded.
	 */
	public String getExpandedLocation();
	
	/**
	 * Returns the targets for an Ant file. The
	 * targets are collected from the corresponding
	 * variable tags in the external tool's arguments.
	 */
	public String[] getAntTargets();
	
	/**
	 * Returns the arguments for the external tool. All
	 * variables embedded in the arguments have been fully
	 * expanded.
	 */
	public String getExpandedArguments();
	
	/**
	 * Returns the working directory to run the external tool in.
	 * All variables embedded in the path have been fully
	 * expanded.
	 */
	public String getExpandedWorkingDirectory();
	
	/**
	 * Returns whether or not the execution log for the
	 * external tool should appear on the log console.
	 */
	public boolean getShowLog();
}
