/*******************************************************************************
 * Copyright (c) 2016 Andrey Loskutov <loskutov@gmx.de>.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Interface for parts providing an adapter to {@link ISaveablePart} objects
 * created or managed originally by other parts.
 * <p>
 * In case the same {@link ISaveablePart} object is created originally by a
 * "primary" part and shown or edited by multiple parts, the "primary" part
 * might want be the only UI element showing the "dirty" state in the UI.
 * </p>
 * <p>
 * This interface allows "primary" parts define the default behavior for all
 * "secondary" parts; and allows "secondary" parts to override this and decide
 * how they should behave and how they should be represented in the UI.
 * </p>
 * <ul>
 * <li>Parts implementing this interface directly are considered to be
 * "secondary" parts and define only their own behavior.</li>
 * <li>Parts can also provide an adapter to this interface via
 * {@link IAdaptable#getAdapter(Class)}. If such part is not implementing this
 * interface directly, it can considered as primary "source" part, and can
 * define a default behavior for all secondary parts.</li>
 * </ul>
 * Per default, dirty state of "secondary" parts is ignored by the framework.
 *
 * @since 3.109
 */
public interface ISecondarySaveableSource {

	/**
	 * Whether the dirty state changes should be supported by the framework if the
	 * part directly implements {@link ISecondarySaveableSource}.
	 * <p>
	 * If the part providing the adapter is not implementing
	 * {@link ISecondarySaveableSource}, return value defines the default behavior
	 * of "secondary" parts connected to this part.
	 *
	 * @return default implementation returns {@code false}
	 */
	default boolean isDirtyStateSupported() {
		return false;
	}
}
