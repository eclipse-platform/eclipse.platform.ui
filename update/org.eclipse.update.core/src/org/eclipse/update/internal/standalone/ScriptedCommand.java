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
package org.eclipse.update.internal.standalone;
import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.operations.*;

public abstract class ScriptedCommand implements IOperationListener {

	private IInstallConfiguration config;
	protected boolean verifyOnly;

	public ScriptedCommand() {
		this(null);
	}

	public ScriptedCommand(String verifyOnly) {
		this.verifyOnly = "true".equals(verifyOnly);
	}

	/**
	 * Returns true if the command should only be run in simulation mode,
	 * to verify if it can execute.
	 * @return
	 */
	public boolean isVerifyOnly() {
		return verifyOnly;
	}

	/**
	 */
	public abstract boolean run();

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

	protected IInstallConfiguration getConfiguration() {
		if (config == null) {
			try {
				ILocalSite localSite = SiteManager.getLocalSite();
				config = localSite.getCurrentConfiguration();
			} catch (CoreException e) {
				StandaloneUpdateApplication.exceptionLogged();
				UpdateCore.log(e);
			}
		}
		return config;
	}

}
