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

import org.eclipse.ui.application.WorkbenchAdviser;
import org.eclipse.ui.application.IWorkbenchConfigurer;

/**
 * Example implementation of a workbench adviser showing how an application
 * configures the workbench for its needs.
 * 
 * @since 3.0
 */
class ExampleWorkbenchAdviser extends WorkbenchAdviser {

	/**
	 * Special object for configuring the workbench.
	 */
	IWorkbenchConfigurer configurer;	
	
	/**
	 * Creates a new workbench adviser instance.
	 */
	ExampleWorkbenchAdviser() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdviser#initialize
	 */
	public void initialize(IWorkbenchConfigurer configurer) {
		// TODO Auto-generated method stub
		this.configurer = configurer;
	}
	
}