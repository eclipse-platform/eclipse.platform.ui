/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part.services;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.graphics.Image;

/**
 * @since 3.1
 */
public class NullStatusLineManager extends NullContributionManager implements
		IStatusLineManager {

	private NullProgressMonitor progressMonitor = new NullProgressMonitor();
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IStatusLineManager#getProgressMonitor()
	 */
	public IProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IStatusLineManager#isCancelEnabled()
	 */
	public boolean isCancelEnabled() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IStatusLineManager#setCancelEnabled(boolean)
	 */
	public void setCancelEnabled(boolean enabled) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IStatusLineManager#setErrorMessage(java.lang.String)
	 */
	public void setErrorMessage(String message) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IStatusLineManager#setErrorMessage(org.eclipse.swt.graphics.Image, java.lang.String)
	 */
	public void setErrorMessage(Image image, String message) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IStatusLineManager#setMessage(java.lang.String)
	 */
	public void setMessage(String message) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IStatusLineManager#setMessage(org.eclipse.swt.graphics.Image, java.lang.String)
	 */
	public void setMessage(Image image, String message) {

	}

}
