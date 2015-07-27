/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 * An undo context is used to "tag" operations as being applicable to a certain
 * context. The undo context is used to filter the history of operations
 * available for undo or redo so that only operations appropriate for a given
 * undo context are shown when the application is presenting that context.
 * </p>
 * <p>
 * The scope of an undo context is defined by the application that is making
 * undo and redo of operations available. Undo contexts may be related to
 * application models, or may be associated with UI objects that are providing
 * undo and redo support.
 * </p>
 * <p>
 * An undo context may be defined as "matching" another context. This allows
 * applications to provide specialized implementations of an undo context that
 * will appear in the operation history for their matching context.
 *
 * @since 3.1
 */

public interface IUndoContext {

	/**
	 * Get the label that describes the undo context.
	 *
	 * @return the label for the context.
	 */
	public String getLabel();

	/**
	 * Return whether the specified context is considered a match for the
	 * receiving context. When a context matches another context, operations
	 * that have the context are considered to also have the matching context.
	 *
	 * @param context
	 *            the context to be checked against the receiving context.
	 *
	 * @return <code>true</code> if the receiving context can be considered a
	 *         match for the specified context, and <code>false</code> if it
	 *         cannot.
	 */
	public boolean matches(IUndoContext context);

}
