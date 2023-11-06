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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 448831
 *******************************************************************************/

package org.eclipse.core.commands.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * A manager of {@link HandleObject} instances. This has some common behaviour
 * which is shared between all such managers.
 * </p>
 * <p>
 * Clients may extend.
 * </p>
 *
 * @param <T> The type of handled objects
 *
 * @since 3.2
 */
public abstract class HandleObjectManager<T extends NamedHandleObject> extends EventManager {

	/**
	 * The set of handle objects that are defined. This value may be empty, but
	 * it is never <code>null</code>.
	 */
	protected final Set<T> definedHandleObjects = new HashSet<>();

	/**
	 * The map of identifiers (<code>String</code>) to handle objects (
	 * <code>HandleObject</code>). This collection may be empty, but it is
	 * never <code>null</code>.
	 */
	protected final Map<String, T> handleObjectsById = new HashMap<>();

	/**
	 * Verifies that the identifier is valid. Exceptions will be thrown if the
	 * identifier is invalid in some way.
	 *
	 * @param id
	 *            The identifier to validate; may be anything.
	 */
	protected final void checkId(final String id) {
		if (id == null) {
			throw new IllegalArgumentException("A handle object may not have a null identifier"); //$NON-NLS-1$
		}

		if (id.length() < 1) {
			throw new IllegalArgumentException("The handle object must not have a zero-length identifier"); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the set of identifiers for those handle objects that are defined.
	 *
	 * @return The set of defined handle object identifiers; this value may be
	 *         empty, but it is never <code>null</code>.
	 */
	@SuppressWarnings("rawtypes")
	protected final Set getDefinedHandleObjectIds() {
		final HashSet<String> definedHandleObjectIds = new HashSet<>(definedHandleObjects.size());
		final Iterator<T> handleObjectItr = definedHandleObjects.iterator();
		while (handleObjectItr.hasNext()) {
			final HandleObject handleObject = handleObjectItr.next();
			final String id = handleObject.getId();
			definedHandleObjectIds.add(id);
		}
		return definedHandleObjectIds;
	}
}
