/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.fieldassist;

/**
 * This interface is used to listen to notifications from a
 * {@link ContentProposalAdapter}.
 *
 * @since 3.2
 */
public interface IContentProposalListener {
	/**
	 * A content proposal has been accepted.
	 *
	 * @param proposal
	 *            the accepted content proposal
	 */
	public void proposalAccepted(IContentProposal proposal);
}
