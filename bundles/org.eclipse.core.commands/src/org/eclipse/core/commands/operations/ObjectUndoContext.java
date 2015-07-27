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

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * An undo context that can be used to represent any given object. Clients
 * can add matching contexts to this context.  This class may be instantiated
 * by clients.
 * </p>
 *
 * @since 3.1
 */
public final class ObjectUndoContext extends UndoContext {

	private Object object;

	private String label;

	private List<IUndoContext> children = new ArrayList<>();

	/**
	 * Construct an operation context that represents the given object.
	 *
	 * @param object
	 *            the object to be represented.
	 */
	public ObjectUndoContext(Object object) {
		this(object, null);
	}

	/**
	 * Construct an operation context that represents the given object and has a
	 * specialized label.
	 *
	 * @param object
	 *            the object to be represented.
	 * @param label
	 *            the label for the context
	 */
	public ObjectUndoContext(Object object, String label) {
		super();
		this.object = object;
		this.label = label;
	}

	@Override
	public String getLabel() {
		if (label != null) {
			return label;
		}
		if (object != null) {
			return object.toString();
		}
		return super.getLabel();
	}

	/**
	 * Return the object that is represented by this context.
	 *
	 * @return the object represented by this context.
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * Add the specified context as a match of this context. Contexts added as
	 * matches of this context will be interpreted as a match of this context
	 * when the history is filtered for a particular context. Adding a match
	 * allows components to create their own contexts for implementing
	 * specialized behavior, yet have their operations appear in a more
	 * global context.
	 *
	 * @param context
	 *            the context to be added as a match of this context
	 */
	public void addMatch(IUndoContext context) {
		children.add(context);
	}

	/**
	 * Remove the specified context as a match of this context. The context will
	 * no longer be interpreted as a match of this context when the history is
	 * filtered for a particular context. This method has no effect if the
	 * specified context was never previously added as a match.
	 *
	 * @param context
	 *            the context to be removed from the list of matches for this
	 *            context
	 */
	public void removeMatch(IUndoContext context) {
		children.remove(context);
	}

	@Override
	public boolean matches(IUndoContext context) {
		// Check first for explicit matches that have been assigned.
		if (children.contains(context)) {
			return true;
		}
		// Contexts for equal objects are considered matching
		if (context instanceof ObjectUndoContext && getObject() != null) {
			return getObject().equals(((ObjectUndoContext)context).getObject());
		}
		// Use the normal matching implementation
		return super.matches(context);
	}

	/**
	 * The string representation of this operation.  Used for debugging purposes only.
	 * This string should not be shown to an end user.
	 *
	 * @return The string representation.
	 */
	@Override
	public String toString() {
		return getLabel();
	}


}
