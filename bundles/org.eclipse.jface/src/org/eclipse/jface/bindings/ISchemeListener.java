/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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

package org.eclipse.jface.bindings;

/**
 * <p>
 * An instance of <code>ISchemeListener</code> can be used by clients to
 * receive notification of changes to one or more instances of
 * <code>IScheme</code>.
 * </p>
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @since 3.1
 * @see Scheme#addSchemeListener(ISchemeListener)
 * @see Scheme#removeSchemeListener(ISchemeListener)
 * @see SchemeEvent
 */
public interface ISchemeListener {

	/**
	 * Notifies that one or more attributes of an instance of
	 * <code>IScheme</code> have changed. Specific details are described in
	 * the <code>SchemeEvent</code>.
	 *
	 * @param schemeEvent
	 *            the scheme event. Guaranteed not to be <code>null</code>.
	 */
	void schemeChanged(SchemeEvent schemeEvent);
}
