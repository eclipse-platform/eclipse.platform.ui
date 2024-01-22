/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

package org.eclipse.ui.commands;

/**
 * An instance of this class describes changes to an instance of
 * <code>ICategory</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 *
 * @since 3.0
 * @see org.eclipse.ui.commands.ICategoryListener#categoryChanged(CategoryEvent)
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead. This
 *             API is scheduled for deletion, see Bug 431177 for details
 * @see org.eclipse.core.commands.CategoryEvent
 * @noreference This class is scheduled for deletion.
 */
@Deprecated
public final class CategoryEvent {

	/**
	 * The category that has changed; this value is never <code>null</code>.
	 */
	private final ICategory category;

	/**
	 * Whether the defined state of the category has changed.
	 */
	private final boolean definedChanged;

	/**
	 * Whether the name of the category has changed.
	 */
	private final boolean nameChanged;

	/**
	 * Creates a new instance of this class.
	 *
	 * @param category       the instance of the interface that changed.
	 * @param definedChanged true, iff the defined property changed.
	 * @param nameChanged    true, iff the name property changed.
	 */
	@Deprecated
	public CategoryEvent(ICategory category, boolean definedChanged, boolean nameChanged) {
		if (category == null) {
			throw new NullPointerException();
		}

		this.category = category;
		this.definedChanged = definedChanged;
		this.nameChanged = nameChanged;
	}

	/**
	 * Returns the instance of the interface that changed.
	 *
	 * @return the instance of the interface that changed. Guaranteed not to be
	 *         <code>null</code>.
	 */
	@Deprecated
	public ICategory getCategory() {
		return category;
	}

	/**
	 * Returns whether or not the defined property changed.
	 *
	 * @return true, iff the defined property changed.
	 */
	@Deprecated
	public boolean hasDefinedChanged() {
		return definedChanged;
	}

	/**
	 * Returns whether or not the name property changed.
	 *
	 * @return true, iff the name property changed.
	 */
	@Deprecated
	public boolean hasNameChanged() {
		return nameChanged;
	}
}
