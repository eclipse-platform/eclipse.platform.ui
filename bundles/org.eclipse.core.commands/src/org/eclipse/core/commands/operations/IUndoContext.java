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
package org.eclipse.core.commands.operations;

/**
 * <p>
 * An interface for an undo context that can be used to identify any operation as
 * having a specific context.
 * </p>
 * <p>
 * Note: This class/interface is part of a new API under development. It has
 * been added to builds so that clients can start using the new features.
 * However, it may change significantly before reaching stability. It is being
 * made available at this early stage to solicit feedback with the understanding
 * that any code that uses this API may be broken as the API evolves.
 * </p>
 * 
 * @since 3.1
 * @experimental
 */

public interface IUndoContext {

	/**
	 * Get the label that should be used to describe the context in any views.
	 * Contexts may be shown when filtered operation histories are shown to the
	 * user.
	 * 
	 * @return the label for the context.
	 */
	public String getLabel();

	/**
	 * Return whether the specified context is considered a match for the
	 * receiving context. When a context matches another context, objects that
	 * have the context are considered to also have the matching context.
	 * 
	 * @param context -
	 *            the context to be checked against the receiving context.
	 * 
	 * @return <code>true</code> if the receiving context can be considered a
	 *         match for the specified context, and <code>false</code> if it
	 *         cannot.
	 */
	public boolean matches(IUndoContext context);

}
