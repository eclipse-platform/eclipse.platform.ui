/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.commands;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.commands.IElementReference;
import org.eclipse.ui.menus.UIElement;

/**
 * Our element reference that is used during element
 * registration/unregistration.
 * 
 * @since 3.3
 */
public class ElementReference implements IElementReference {

	private String commandId;
	private UIElement element;
	private HashMap parameters;

	/**
	 * Construct the reference.
	 * 
	 * @param id
	 *            command id. Must not be <code>null</code>.
	 * @param adapt
	 *            the element. Must not be <code>null</code>.
	 * @param parms.
	 *            parameters used for filtering. Must not be <code>null</code>.
	 */
	public ElementReference(String id, UIElement adapt, Map parms) {
		commandId = id;
		element = adapt;
		if (parms == null) {
			parameters = new HashMap();
		} else {
			parameters = new HashMap(parms);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.IElementReference#getElement()
	 */
	public UIElement getElement() {
		return element;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.commands.IElementReference#getCommandId()
	 */
	public String getCommandId() {
		return commandId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.commands.IElementReference#getParameters()
	 */
	public Map getParameters() {
		return parameters;
	}
}
