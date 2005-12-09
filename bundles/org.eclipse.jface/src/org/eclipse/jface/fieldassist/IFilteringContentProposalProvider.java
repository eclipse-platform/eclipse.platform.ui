/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.fieldassist;

/**
 * IFilteringProposalProvider provides a list of objects that are appropriate
 * for a textual dialog field, given a filter.
 * 
 * <p>
 * This API is considered experimental. It is still evolving during 3.2 and is
 * subject to change. It is being released to obtain feedback from early
 * adopters.
 * 
 * @see IContentProposalProvider
 * @since 3.2
 */
public interface IFilteringContentProposalProvider extends
		IContentProposalProvider {

	/**
	 * Return an array of Objects representing the valid content proposals for a
	 * field when it contains the given text content.
	 * 
	 * @param content
	 *            the String representing the current filter for which proposals
	 *            are requested.
	 * @return the Objects that represent valid proposals for the field given
	 *         the current filter. Returned proposals may be a String, or an
	 *         Object that will be mapped to a text value using an
	 *         <code>org.eclipse.jface.viewers.ILabelProvider</code>.
	 */
	Object[] getProposals(String content);
}
