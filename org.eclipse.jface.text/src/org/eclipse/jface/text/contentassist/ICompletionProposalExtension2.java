/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.jface.text.contentassist;


import org.eclipse.jface.text.ITextViewer;


/**
 * Extension interface to <code>ICompletionProposal</code>.
 * Add the following functions:
 * <ul>
 * <li> handling of trigger characters with modifiers
 * <li> visual indication for selection of a proposal
 * </ul>
 *  * @since 2.1
 */
public interface ICompletionProposalExtension2 {
	
	/**
	 * Applies the proposed completion to the given document. The insertion
	 * has been triggered by entering the given character with a modifier at the given offset.
	 * This method assumes that <code>isValidFor</code> returns
	 * <code>true</code> if called for <code>offset</code>.
	 *
	 * @param viewer the text viewer into which to insert the proposed completion
	 * @param trigger the trigger to apply the completion
	 * @param stateMask the state mask of the modifiers
	 * @param offset the offset at which the trigger has been activated
	 */
	void apply(ITextViewer viewer, char trigger, int stateMask, int offset);

	/**
	 * Called when the proposal is selected.
	 */
	void selected(ITextViewer viewer);

	/**
	 * Called when the proposal is unselected.
	 */
	void unselected(ITextViewer viewer);

}
