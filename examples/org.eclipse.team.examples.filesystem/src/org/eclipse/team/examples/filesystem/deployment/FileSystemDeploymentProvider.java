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
package org.eclipse.team.examples.filesystem.deployment;

import org.eclipse.team.core.DeploymentProvider;
import org.eclipse.team.internal.core.IMemento;

public class FileSystemDeploymentProvider extends DeploymentProvider {

	public final static String ID = "org.eclipse.team.examples.filesystem.FileSystemDeploymentProvider";

	public String getID() {
		return ID;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.DeploymentProvider#init()
	 */
	public void init() {
		// TODO Auto-generated method stub
		System.out.println("Initialized " + getName());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.DeploymentProvider#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.DeploymentProvider#saveState(org.eclipse.team.core.IMemento)
	 */
	public void saveState(IMemento memento) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.DeploymentProvider#restoreState(org.eclipse.team.core.IMemento)
	 */
	public void restoreState(IMemento memento) {
		// TODO Auto-generated method stub
		
	}
}
