/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package org.eclipse.team.examples.pessimistic.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.examples.pessimistic.PessimisticFilesystemProvider;

/**
 * Performs an uncheck out on the selected resources.  If a folder is 
 * selected all of its children are recursively unchecked out.
 */
public class UncheckOutAction extends CheckInAction {
	/**
	 * @see org.eclipse.team.examples.pessimistic.ui.SourceManagementAction#manageResources(PessimisticFilesystemProvider, IResource[], IProgressMonitor)
	 */
	protected void manageResources(PessimisticFilesystemProvider provider, IResource[] resources, IProgressMonitor monitor) {
		provider.uncheckout(resources, monitor);
	}

}
