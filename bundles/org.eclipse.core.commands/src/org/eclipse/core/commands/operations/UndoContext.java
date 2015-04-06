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
 * A simple, lightweight undo context that can be used to tag any operation. It
 * does not provided a specialized label. This class may be instantiated by
 * clients. This class may also be subclassed.
 * </p>
 *
 * @since 3.1
 */
public class UndoContext implements IUndoContext {

	/**
	 * <p>
	 * Get the label that describes the undo context. The default implementation
	 * returns the empty String. Subclasses may override.
	 * </p>
	 *
	 * @return the label for the context.
	 */
	@Override
	public String getLabel() {
		return ""; //$NON-NLS-1$
	}

	/**
	 * <p>
	 * Return whether the specified context is considered a match for the
	 * receiving context. When a context matches another context, operations
	 * that have the context are considered to also have the matching context.
	 * The default implementation checks whether the supplied context is
	 * identical to this context. Subclasses may override.
	 * </p>
	 *
	 * @param context
	 *            the context to be checked against the receiving context.
	 *
	 * @return <code>true</code> if the receiving context can be considered a
	 *         match for the specified context, and <code>false</code> if it
	 *         cannot.
	 */
	@Override
	public boolean matches(IUndoContext context) {
		return context == this;
	}
}
