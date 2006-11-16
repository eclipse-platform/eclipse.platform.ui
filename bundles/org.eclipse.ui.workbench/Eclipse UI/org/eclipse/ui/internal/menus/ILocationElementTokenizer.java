/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

/**
 * <p>
 * A utility class for breaking apart locations into their component parts.
 * </p>
 * <p>
 * Only intended for use within the <code>org.eclipse.jface.menus</code>
 * package.
 * </p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * <p>
 * This class will eventually exist in <code>org.eclipse.jface.menus</code>.
 * </p>
 * 
 * @since 3.2
 */
interface ILocationElementTokenizer {

	/**
	 * Returns whether there are more elements in this tokenizer.
	 * 
	 * @return <code><code>true</code> if there are more tokens; <code>false</code> otherwise.
	 */
	boolean hasMoreTokens();

	/**
	 * Returns the next token, and increments the internal state.
	 * 
	 * @return The next token; may be <code>null</code> if there are no more
	 *         tokens.
	 */
	LocationElementToken nextToken();
}

