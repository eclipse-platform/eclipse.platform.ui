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
package org.eclipse.core.commands.common;

/**
 * A handle object that carries with it a name and a description. This type of
 * handle object is quite common across the commands code base. For example,
 * <code>Command</code>, <code>Context</code> and <code>Scheme</code>.
 * 
 * @since 3.1
 */
public abstract class NamedHandleObject extends HandleObject {

	/**
	 * The description for this handle. This value may be <code>null</code> if
	 * the handle is undefined or has no description.
	 */
	protected String description = null;

	/**
	 * The name of this handle. This valud should not be <code>null</code>
	 * unless the handle is undefined.
	 */
	protected String name = null;

	/**
	 * Constructs a new instance of <code>NamedHandleObject</code>.
	 * 
	 * @param id
	 *            The identifier for this handle; must not be <code>null</code>.
	 */
	protected NamedHandleObject(final String id) {
		super(id);
	}

	/**
	 * Returns the description for this handle.
	 * 
	 * @return The description; may be <code>null</code> if there is no
	 *         description.
	 * @throws NotDefinedException
	 *             If the handle is not currently defined.
	 */
	public String getDescription() throws NotDefinedException {
		if (!isDefined()) {
			throw new NotDefinedException(
					"Cannot get a description from an undefined object. " //$NON-NLS-1$
					+ id);
		}

		return description;
	}

	/**
	 * Returns the name for this handle.
	 * 
	 * @return The name for this handle; never <code>null</code>.
	 * @throws NotDefinedException
	 *             If the handle is not currently defined.
	 */
	public String getName() throws NotDefinedException {
		if (!isDefined()) {
			throw new NotDefinedException(
					"Cannot get the name from an undefined object. " //$NON-NLS-1$
					+ id);
		}

		return name;
	}
}
