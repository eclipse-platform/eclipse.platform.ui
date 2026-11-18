/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

/**
 * Interface allowing breadcrumb drop-down implementors to communicate with
 * their containing breadcrumb.
 *
 * @since 3.41
 */
public interface TreePathBreadcrumbDropDownSite {

	/**
	 * Notifies the breadcrumb that the given selection was made in the drop-down
	 * viewer.
	 * @param selection Selection to set to breadcrumb.
	 */
	void notifySelection(ISelection selection);

	/**
	 * Notifies the breadcrumb that the drop-down viewer should be closed.
	 */
	void close();

	/**
	 * Notifies the breadcrumb that the drop-down viewer's contents have
	 * changed and viewer shell should be adjusted for the new size.
	 */
	void updateSize();
}
