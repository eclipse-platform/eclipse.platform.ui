/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.commands.contexts;

import org.eclipse.core.commands.common.AbstractNamedHandleEvent;

/**
 * An instance of this class describes changes to an instance of
 * <code>IContext</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.1
 * @see IContextListener#contextChanged(ContextEvent)
 */
public final class ContextEvent extends AbstractNamedHandleEvent {

	/**
	 * The bit used to represent whether the context has changed its parent.
	 */
	private static final int CHANGED_PARENT_ID = LAST_USED_BIT << 1;

	/**
	 * The context that has changed. This value is never <code>null</code>.
	 */
	private final Context context;

	/**
     * Creates a new instance of this class.
     * 
     * @param context
     *            the instance of the interface that changed; must not be
     *            <code>null</code>.
     * @param definedChanged
     *            <code>true</code>, iff the defined property changed.
     * @param nameChanged
     *            <code>true</code>, iff the name property changed.
     * @param descriptionChanged
     *            <code>true</code>, iff the description property changed.
     * @param parentIdChanged
     *            <code>true</code>, iff the parentId property changed.
     */
	public ContextEvent(final Context context, final boolean definedChanged,
			final boolean nameChanged, final boolean descriptionChanged,
			final boolean parentIdChanged) {
		super(definedChanged, descriptionChanged, nameChanged);
		
		if (context == null) {
			throw new NullPointerException();
		}
		this.context = context;
		
		if (parentIdChanged) {
			changedValues |= CHANGED_PARENT_ID;
		}
	}

	/**
	 * Returns the instance of the interface that changed.
	 * 
	 * @return the instance of the interface that changed. Guaranteed not to be
	 *         <code>null</code>.
	 */
	public final Context getContext() {
		return context;
	}

	/**
	 * Returns whether or not the parentId property changed.
	 * 
	 * @return <code>true</code>, iff the parentId property changed.
	 */
	public final boolean isParentIdChanged() {
		return ((changedValues & CHANGED_PARENT_ID) != 0);
	}
}
