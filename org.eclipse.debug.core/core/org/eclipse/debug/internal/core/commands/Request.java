/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     John Cortell (Freescale) - update javadoc tags (Bug 292301)
 *******************************************************************************/
package org.eclipse.debug.internal.core.commands;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IRequest;

/**
 * @since 3.3
 */
public class Request implements IRequest {
	
	private IStatus fStatus;
	private boolean fCanceled = false;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IRequest#done()
	 */
	public void done() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IRequest#getStatus()
	 */
	public IStatus getStatus() {
		return fStatus;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IRequest#setStatus(org.eclipse.core.runtime.IStatus)
	 */
	public void setStatus(IStatus status) {
		fStatus = status;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IRequest#cancel()
	 */
	public synchronized void cancel() {
		fCanceled = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IRequest#isCanceled()
	 */
	public synchronized boolean isCanceled() {
		return fCanceled;
	}

}
