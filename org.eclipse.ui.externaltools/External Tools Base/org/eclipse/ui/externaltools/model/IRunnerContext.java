package org.eclipse.ui.externaltools.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

/**
 * Represents the context of the external tool to run. An implementation
 * of this interface is provided to the <code>IExternalToolRunner</code>.
 * <p>
 * This interface is not be extended nor implemented by clients.
 * </p>
 */
public interface IRunnerContext {
	/**
	 * Returns whether the external tool runner should capture
	 * output messages from the running tool and log these
	 * messages.
	 */
	public boolean getCaptureOutput();
	
	/**
	 * Returns the arguments for the external tool. All
	 * variables embedded in the arguments have been fully
	 * expanded.
	 */
	public String[] getExpandedArguments();
	
	/**
	 * Returns the path where the external tool is located. All
	 * variables embedded in the path have been fully
	 * expanded.
	 */
	public String getExpandedLocation();
	
	/**
	 * Returns the working directory to run the external tool in.
	 * All variables embedded in the path have been fully
	 * expanded.
	 */
	public String getExpandedWorkingDirectory();

	/**
	 * Returns the extra attribute value of the external tool.
	 * 
	 * @param key the unique attribute name
	 * @return the value of the attribute, or <code>null</code>
	 * 		if not such attribute name.
	 */
	public String getExtraAttribute(String key);

	/**
	 * Returns the log the runner can used to log
	 * messages captured from the running tool's output.
	 */
	public IRunnerLog getLog();
	
	/**
	 * Returns the name of the external tool.
	 */
	public String getName();
}
