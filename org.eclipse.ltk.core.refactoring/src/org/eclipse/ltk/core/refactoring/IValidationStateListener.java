/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A listener which is notified when a change's validation state
 * changes.
 * 
 * @see Change#isValid(IProgressMonitor)
 * 
 * @since 3.0
 */
public interface IValidationStateListener {
	
	/**
	 * Notifies that the validation state of a change
	 * has changed.
	 * 
	 * @param event event object describing the change
	 */
	public void stateChanged(ValidationStateChangedEvent event);
}
