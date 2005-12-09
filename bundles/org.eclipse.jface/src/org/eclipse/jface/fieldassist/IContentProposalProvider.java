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
 * IContentProposalProvider provides a list of objects that are appropriate
 * for a textual dialog field, given the field's current content. Proposals may
 * optionally provide a description.
 * 
 * <p>
 * This API is considered experimental. It is still evolving during 3.2 and is
 * subject to change. It is being released to obtain feedback from early
 * adopters.
 * 
 * @since 3.2
 */
public interface IContentProposalProvider {

	/**
	 * Return an array of Objects representing the valid content proposals for a
	 * field.
	 * 
	 * @return the Objects that represent valid proposals for the field. The
	 *         proposal may be a String, or an Object that will be mapped to a
	 *         text value using an associated
	 *         <code>org.eclipse.jface.viewers.ILabelProvider</code>.
	 */
	Object[] getProposals();

	/**
	 * Return a String that describes the given proposal in more detail.
	 * 
	 * @param proposal
	 *            the Object representing a valid proposal
	 * @return a String that further describes the proposal, or
	 *         <code>null</code> if there is no description available.
	 */
	String getProposalDescription(Object proposal);

}
