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
package org.eclipse.update.internal.operations;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;

/**
 * Operation validator
 */
public interface IOperationValidator {

	/**
	 * Called before performing operation.
	 * Returns null when status cannot be reported.
	 */
	public IStatus validatePendingChange(PendingOperation job);

	/**
	 * Called before processing a delta.
	 * Returns null when status cannot be reported.
	 */
	public IStatus validateSessionDelta(
		ISessionDelta delta,
		IFeatureReference[] deltaRefs);

	/**
	 * Called before doing a revert/ restore operation
	 * Returns null when status cannot be reported.
	 */
	public IStatus validatePendingRevert(IInstallConfiguration config);

	/**
	 * Called by the UI before doing a batched processing of
	 * several pending changes.
	 * Returns null when status cannot be reported.
	 */
	public IStatus validatePendingChanges(PendingOperation[] jobs);

	/**
	 * Check the current state.
	 * Returns null when status cannot be reported.
	 */
	public IStatus validateCurrentState();
}