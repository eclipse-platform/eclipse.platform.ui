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

package org.eclipse.jface.text.contentassist;


import org.eclipse.jface.text.IInformationControlCreator;


/**
 * Extension interface to <code>ICompletionProposal</code>.
 * Add the following functions:
 * <ul>
 * <li> provision of a custom information control creator
 * </ul>
 * 
 * @since 3.0
 */
public interface ICompletionProposalExtension3 {
	/**
	 * Returns the information control creator of this completion proposal.
	 * 
	 * @return the information control creator
	 */
	IInformationControlCreator getInformationControlCreator();
}
