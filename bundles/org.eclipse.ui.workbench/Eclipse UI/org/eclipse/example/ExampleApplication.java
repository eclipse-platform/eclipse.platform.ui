/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.example;

import org.eclipse.core.boot.IPlatformRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * Example Eclipse application that creates and configures a generic workbench.
 * 
 * @since 3.0
 */
public class ExampleApplication implements IPlatformRunnable, IExecutableExtension {
	
	/**
	 * Creates a new application.
	 */
	public ExampleApplication() {
		// TODO Auto-generated method stub
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.boot.IPlatformRunnable#run(java.lang.Object)
	 */
	public Object run(Object args) throws Exception {
		
		// adviser provides application-specific configuration of the workbench
		ExampleWorkbenchAdviser workbenchAdviser = new ExampleWorkbenchAdviser();
		
		// create the workbench with this adviser
		IWorkbench workbench = PlatformUI.createWorkbench(workbenchAdviser);
		
		// N.B. createWorkbench remembers the adviser, and also registers the
		// workbench globally so that all UI plug-ins can find it using
		// PlatformUI.getWorkbench() or AbstractUIPlugin.getWorkbench()
		
		// run the workbench until it exits
		boolean restart = workbench.runUI();
		
		// exit the application with an appropriate return code
		if (restart) {
			return IPlatformRunnable.EXIT_RESTART;
		} else {
			return IPlatformRunnable.EXIT_OK;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(
		IConfigurationElement config,
		String propertyName,
		Object data)
		throws CoreException {
		// TODO Auto-generated method stub
	}
}
