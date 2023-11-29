/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.e4.ui.progress.internal;

/**
 * The IProgressUpdateCollector is the interface that content providers
 * conform to in order that the ProgressViewUpdater can talk to various
 * types of content provider.
 */
public interface IProgressUpdateCollector {

	/**
	 * Refresh the viewer.
	 */
	void refresh();

	/**
	 * Refresh the elements.
	 */
	void refresh(Object[] elements);

	/**
	 * Add the elements.
	 * @param elements Array of JobTreeElement
	 */
	void add(Object[] elements);

	/**
	 * Remove the elements.
	 * @param elements Array of JobTreeElement
	 */
	void remove(Object[] elements);

}
