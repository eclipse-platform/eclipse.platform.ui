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

package org.eclipse.ui.part;

/**
 * Parts which need to provide a particular context to a Show In... target can
 * provide this interface. The part can either directly implement this
 * interface, or provide it via
 * <code>IAdaptable.getAdapter(IShowInSource.class)</code>.
 *
 * @see IShowInTarget
 *
 * @since 2.1
 */
public interface IShowInSource {

	/**
	 * Returns the context to show, or <code>null</code> if there is currently no
	 * valid context to show.
	 *
	 * @return the context to show, or <code>null</code>
	 */
	ShowInContext getShowInContext();
}
