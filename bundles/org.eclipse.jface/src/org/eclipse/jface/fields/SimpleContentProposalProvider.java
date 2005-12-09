/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.fields;

/**
 * SimpleContentProposalProvider is a class designed to return a static list
 * of items when queried for content proposals.
 * <p>
 * This API is considered experimental. It is still evolving during 3.2 and is
 * subject to change. It is being released to obtain feedback from early
 * adopters.
 * 
 * @see IContentProposalProvider
 * @since 3.2
 *
 */
public class SimpleContentProposalProvider implements IContentProposalProvider {

	/*
	 * The proposals to display.
	 */
	private Object[] proposals;

	/**
	 * Construct a SimpleContentProposalProvider whose content proposals are
	 * always the specified array of Objects.
	 * 
	 * @param proposals
	 *            the array of Objects to be returned whenever proposals are
	 *            requested.
	 */
	public SimpleContentProposalProvider(Object[] proposals) {
		super();
		this.proposals = proposals;
	}

	/**
	 * Return an array of Objects representing the valid content proposals for a
	 * field.
	 * 
	 * @return the array of Objects that represent valid proposals for the field
	 *         given its current content.
	 */
	public Object[] getProposals() {
		return this.proposals;
	}

	/**
	 * Set the proposals to be returned by the receiver whenever content
	 * proposals are requested.
	 * 
	 * @param items
	 *            Object[]
	 */
	public void setProposals(Object[] items) {
		this.proposals = items;
	}

	/**
	 * Return a String that describes the given proposal in more detail.
	 * 
	 * @param proposal
	 *            the Object representing a valid proposal
	 * @return return <code>null</code> to indicate that there is no
	 *         description available.
	 */
	public String getProposalDescription(Object proposal) {
		return null;
	}
}
