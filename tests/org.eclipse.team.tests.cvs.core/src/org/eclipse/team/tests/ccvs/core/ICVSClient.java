/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.tests.ccvs.core;

import org.eclipse.core.resources.IContainer;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;

public interface ICVSClient {
	public void executeCommand(
		ICVSRepositoryLocation repositoryLocation, IContainer localRoot, String command,
		String[] globalOptions, String[] localOptions, String[] arguments)
		throws CVSException;
}
