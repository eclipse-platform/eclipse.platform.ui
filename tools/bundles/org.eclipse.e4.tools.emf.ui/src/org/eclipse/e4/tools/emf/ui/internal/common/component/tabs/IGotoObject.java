/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.component.tabs;

import org.eclipse.emf.ecore.EObject;

/**
 * This interface allows a UI object to reveal and select an EMF object.
 *
 * @author Steven Spungin
 *
 */
public interface IGotoObject {
	/**
	 *
	 * @param targetHint
	 *            An object specific hint used for resolution when the same
	 *            object is available in multiple locations on the target.
	 * @param object
	 *            the EObject to reveal, and select.
	 */
	void gotoEObject(int targetHint, EObject object);
}
