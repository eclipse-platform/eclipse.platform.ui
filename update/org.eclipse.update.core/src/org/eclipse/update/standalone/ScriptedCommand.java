/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.standalone;
import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.operations.*;

/**
 * Parent class for all the update manager standalone commands.
 * Subclasses will provide specific operations and the implementation of the run() method.
 */
public abstract class ScriptedCommand implements IOperationListener {

	private IInstallConfiguration config;
	protected boolean verifyOnly;

	/**
	 * Constructor
	 *
	 */
	public ScriptedCommand() {
		this(null);
	}

	/**
	 * Constructor.
	 * 
	 * @param verifyOnly if true, the command is not executed, but will only attempt to run the command. 
	 * This is mostly used when wanted to know if the command would fail.
	 */
	public ScriptedCommand(String verifyOnly) {
		this.verifyOnly = "true".equals(verifyOnly);
	}

	/**
	 * @return  true if the command should only be run in simulation mode,
	 * to verify if it can execute.
	 */
	protected final boolean isVerifyOnly() {
		return verifyOnly;
	}

	/**
	 * Convenience method that executes the command with a null progress monitor.
	 */
	public final boolean run() {
		return run(new NullProgressMonitor());
	}
	
	/**
	 * Executes the command. Subclasses are responsible for implementing this method.
	 * If the command was constructed with verifyOnly=true, the command should not execute, but only verify it can execute.
	 * @param monitor progress monitor during command execution.
	 */
	public abstract boolean run(IProgressMonitor monitor);

	/**
	 * Applies the changes made to the current configuration.
	 */
	public void applyChangesNow() {
		InstallConfiguration.applyChanges();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.update.operations.IOperationListener#afterExecute(org.eclipse.update.operations.IOperation)
	 */
	public boolean afterExecute(IOperation operation, Object data) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.operations.IOperationListener#beforeExecute(org.eclipse.update.operations.IOperation)
	 */
	public boolean beforeExecute(IOperation operation, Object data) {
		return true;
	}

	/**
	 * @return the installation configuration affected by the command
	 */
	public final IInstallConfiguration getConfiguration() {
//		if (config == null) {
			try {
				ILocalSite localSite = SiteManager.getLocalSite();
				config = localSite.getCurrentConfiguration();
//				if (!isVerifyOnly()) {
//					config = UpdateUtils.createInstallConfiguration();
//					UpdateUtils.makeConfigurationCurrent(config, null);
//				}
			} catch (CoreException e) {
				StandaloneUpdateApplication.exceptionLogged();
				UpdateCore.log(e);
			}
//		}
		return config;
	}

}
