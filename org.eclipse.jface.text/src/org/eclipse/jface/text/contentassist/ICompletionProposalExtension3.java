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
	 * @return the information control creator, or <code>null</code> if no custom control creater is available
	 */
	IInformationControlCreator getInformationControlCreator();
	
	/**
	 * Returns the string that would be inserted at the position returned from 
	 * {@see #getCompletionOffset()} if this proposal was applied. If the 
	 * replacement string cannot be determined, <code>null</code> may be returned.
	 * 
	 * @return the replacement string or <code>null</code> if it cannot be determined
	 */
	CharSequence getCompletionText();
	
	/**
	 * Returns the document offset at which the receiver would insert its proposal.
	 *  
	 * @return the offset at which the proposal would insert its proposal
	 */
	int getCompletionOffset();
}
