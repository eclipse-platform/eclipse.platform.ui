/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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

package org.eclipse.jface.viewers;

/**
 * Parties interested in activation and deactivation of editors extend this
 * class and implement any or all of the methods
 *
 * @since 3.3
 */
public abstract class ColumnViewerEditorActivationListener {
	/**
	 * Called before an editor is activated
	 *
	 * @param event
	 *            the event
	 */
	public abstract void beforeEditorActivated(ColumnViewerEditorActivationEvent event);

	/**
	 * Called after an editor has been activated
	 *
	 * @param event the event
	 */
	public abstract void afterEditorActivated(ColumnViewerEditorActivationEvent event);

	/**
	 * Called before an editor is deactivated
	 *
	 * @param event
	 *            the event
	 */
	public abstract void beforeEditorDeactivated(ColumnViewerEditorDeactivationEvent event);


	/**
	 * Called after an editor is deactivated
	 *
	 * @param event the event
	 */
	public abstract void afterEditorDeactivated(ColumnViewerEditorDeactivationEvent event);
}
