/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.context;

import org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor;

/**
 * @since 3.2
 *
 */
public class BooleanRequestMonitor extends AbstractRequestMonitor implements IBooleanRequestMonitor {
	
	private boolean fResult;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor#setResult(boolean)
	 */
	public void setResult(boolean result) {
		fResult = result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#done()
	 */
	public void done() {
		// nothing to do 
	}
	
	/**
	 * Returns the boolean result.
	 * 
	 * @return boolean result
	 */
	boolean getResult() { 
		return fResult;
	}

}
