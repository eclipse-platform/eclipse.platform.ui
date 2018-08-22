/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
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
 * Extends {@link org.eclipse.jface.text.contentassist.ICompletionListener} with an additional
 * notification after applying a proposal.
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @since 3.8
 */
public interface ICompletionListenerExtension2 {

	/**
	 * Called after applying a proposal.
	 *
	 * @param proposal the applied proposal
	 */
	void applied(ICompletionProposal proposal);

}