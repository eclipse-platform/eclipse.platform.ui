/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.progress;

import org.eclipse.jface.action.IAction;

/**
 * Interface for a workbench action.
 */
public interface IDisposableAction extends IAction {
	/**
	 * Disposes of this action. Once disposed, this action cannot be used.
	 * This operation has no effect if the action has already been
	 * disposed.
	 */
	public void dispose();
}