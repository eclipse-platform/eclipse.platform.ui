/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

/**
 * A concrete implementation of the {@link IDecorationContext} interface,
 * suitable for instantiating.
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 * @since 3.2
 */
public class DecorationContext implements IDecorationContext {
	
	/**
	 * Constant that defines a default decoration context that has
	 * no context ids associated with it.
	 */
	public static final IDecorationContext DEFAULT_CONTEXT = new DecorationContext(new String[0]);
	
	private String[] contextIds;

	/**
	 * Create a decoration context with the given context ids
	 * @param contextIds the context ids for this decoration context
	 */
	public DecorationContext(String[] contextIds) {
		this.contextIds = contextIds;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IDecorationContext#getContextIds()
	 */
	public String[] getContextIds() {
		return contextIds;
	}

}
