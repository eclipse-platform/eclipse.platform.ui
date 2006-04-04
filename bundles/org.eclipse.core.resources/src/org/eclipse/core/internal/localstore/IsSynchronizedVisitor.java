/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.localstore;

import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Visits a unified tree, and throws a ResourceChangedException on the first 
 * node that is discovered to be out of sync.  The exception that is thrown 
 * will not have any meaningful status, message, or stack trace.  Nodes 
 * discovered to be out of sync are not brought to be in sync with the workspace.
 */
public class IsSynchronizedVisitor extends CollectSyncStatusVisitor {
	static class ResourceChangedException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	protected static ResourceChangedException exception = new ResourceChangedException();

	/**
	 * Creates a new IsSynchronizedVisitor.
	 */
	public IsSynchronizedVisitor(IProgressMonitor monitor) {
		super("", monitor); //$NON-NLS-1$
	}

	/**
	 * @see CollectSyncStatusVisitor#changed(Resource)
	 */
	protected void changed(Resource target) {
		throw exception;
	}
}
