/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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