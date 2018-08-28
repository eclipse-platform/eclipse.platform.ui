/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.jface.dialogs;

/**
 * A listener which is notified when the current page of the multi-page dialog
 * is changed.
 *
 * @see IPageChangeProvider
 * @see PageChangedEvent
 *
 * @since 3.1
 */
public interface IPageChangedListener {
	/**
	 * Notifies that the selected page has changed.
	 *
	 * @param event
	 *            event object describing the change
	 */
	public void pageChanged(PageChangedEvent event);
}
