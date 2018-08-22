/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
package org.eclipse.jface.text.contentassist;

/**
 * Extends {@link org.eclipse.jface.text.contentassist.ICompletionListener}
 * with an additional notification about restarting the current code assist session.
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @since 3.4
 */
public interface ICompletionListenerExtension {
	/**
	 * Called when code assist is invoked when there is already a current code assist session.
	 *
	 * @param event the content assist event
	 */
	void assistSessionRestarted(ContentAssistEvent event);

}