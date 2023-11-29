/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import org.eclipse.core.commands.common.NamedHandleObject;

/**
 * <p>
 * A logical group for a set of commands. A command belongs to exactly one
 * category. The category has no functional effect, but may be used in graphical
 * tools that want to group the set of commands somehow.
 * </p>
 *
 * @since 3.1
 */
public final class Category extends NamedHandleObject {

	/**
	 * A collection of objects listening to changes to this category. This
	 * collection is <code>null</code> if there are no listeners.
	 */
	private Collection<ICategoryListener> categoryListeners;

	/**
	 * Constructs a new instance of <code>Category</code> based on the given
	 * identifier. When a category is first constructed, it is undefined.
	 * Category should only be constructed by the <code>CommandManager</code>
	 * to ensure that identifier remain unique.
	 *
	 * @param id
	 *            The identifier for the category. This value must not be
	 *            <code>null</code>, and must be unique amongst all
	 *            categories.
	 */
	Category(final String id) {
		super(id);
	}

	/**
	 * Adds a listener to this category that will be notified when this
	 * category's state changes.
	 *
	 * @param categoryListener
	 *            The listener to be added; must not be <code>null</code>.
	 */
	public final void addCategoryListener(
			final ICategoryListener categoryListener) {
		if (categoryListener == null) {
			throw new NullPointerException();
		}
		if (categoryListeners == null) {
			categoryListeners = new ArrayList<>();
		}
		if (!categoryListeners.contains(categoryListener)) {
			categoryListeners.add(categoryListener);
		}
	}

	/**
	 * <p>
	 * Defines this category by giving it a name, and possibly a description as
	 * well. The defined property automatically becomes <code>true</code>.
	 * </p>
	 * <p>
	 * Notification is sent to all listeners that something has changed.
	 * </p>
	 *
	 * @param name
	 *            The name of this command; must not be <code>null</code>.
	 * @param description
	 *            The description for this command; may be <code>null</code>.
	 */
	public final void define(final String name, final String description) {
		if (name == null) {
			throw new NullPointerException("The name of a command cannot be null"); //$NON-NLS-1$
		}

		final boolean definedChanged = !this.defined;
		this.defined = true;

		final boolean nameChanged = !Objects.equals(this.name, name);
		this.name = name;

		final boolean descriptionChanged = !Objects.equals(this.description, description);
		this.description = description;

		fireCategoryChanged(new CategoryEvent(this, definedChanged, descriptionChanged, nameChanged));
	}

	/**
	 * Notifies the listeners for this category that it has changed in some way.
	 *
	 * @param categoryEvent
	 *            The event to send to all of the listener; must not be
	 *            <code>null</code>.
	 */
	private final void fireCategoryChanged(final CategoryEvent categoryEvent) {
		if (categoryEvent == null) {
			throw new NullPointerException();
		}
		if (categoryListeners != null) {
			final Iterator<ICategoryListener> listenerItr = categoryListeners.iterator();
			while (listenerItr.hasNext()) {
				final ICategoryListener listener = listenerItr.next();
				listener.categoryChanged(categoryEvent);
			}
		}
	}

	/**
	 * Removes a listener from this category.
	 *
	 * @param categoryListener
	 *            The listener to be removed; must not be <code>null</code>.
	 */
	public final void removeCategoryListener(
			final ICategoryListener categoryListener) {
		if (categoryListener == null) {
			throw new NullPointerException();
		}

		if (categoryListeners != null) {
			categoryListeners.remove(categoryListener);
		}
	}

	@Override
	public String toString() {
		if (string == null) {
			final StringBuilder stringBuffer = new StringBuilder("Category("); //$NON-NLS-1$
			stringBuffer.append(id);
			stringBuffer.append(',');
			stringBuffer.append(name);
			stringBuffer.append(',');
			stringBuffer.append(description);
			stringBuffer.append(',');
			stringBuffer.append(defined);
			stringBuffer.append(')');
			string = stringBuffer.toString();
		}
		return string;
	}

	@Override
	public void undefine() {
		string = null;

		final boolean definedChanged = defined;
		defined = false;

		final boolean nameChanged = name != null;
		name = null;

		final boolean descriptionChanged = description != null;
		description = null;

		fireCategoryChanged(new CategoryEvent(this, definedChanged, descriptionChanged, nameChanged));
	}

}
