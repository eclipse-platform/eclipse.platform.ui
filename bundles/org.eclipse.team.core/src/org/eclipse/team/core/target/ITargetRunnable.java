/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.core.target;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;

public interface ITargetRunnable {
	public void run(IProgressMonitor monitor) throws TeamException;
}
