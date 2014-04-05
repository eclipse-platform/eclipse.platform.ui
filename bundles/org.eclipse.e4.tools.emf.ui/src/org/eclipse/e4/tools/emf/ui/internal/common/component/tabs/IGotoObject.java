/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
