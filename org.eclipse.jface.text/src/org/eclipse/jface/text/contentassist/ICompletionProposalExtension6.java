/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.contentassist;

import org.eclipse.jface.viewers.StyledStringBuilder;


/**
 * Extends {@link org.eclipse.jface.text.contentassist.ICompletionProposal} with the following
 * function:
 * <ul>
 * 	<li>Allow styled ranges in the display string.</li>
 * </ul>
 * 
 * @since 3.4
 */
public interface ICompletionProposalExtension6 {
	/**
	 * Returns the string to be displayed in the list of completion proposals.
	 *
	 * <p>
	 * If this interface  implemented, {@link #getStyledDisplayString} will be used
	 * instead of {@link ICompletionProposal#getDisplayString()}.
	 * </p>
	 *
	 * @return the string to be displayed
	 */
	StyledStringBuilder getStyledDisplayString();
}
