package org.eclipse.ui.internal.decorators;

import org.eclipse.ui.internal.misc.Assert;

/************************************************************************
Copyright (c) 2000, 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/


/**
 * A DecorationReference is a class that holds onto the starting
 * text and image of a decoration.
 */
class DecorationReference {

	Object element;
	Object adaptedElement;

	DecorationReference(Object object) {
		Assert.isNotNull(object);
		element = object;
	}

	DecorationReference(Object object, Object adaptedObject) {
		this(object);
		this.adaptedElement = adaptedObject;
	}

	/**
	 * Returns the adaptedElement.
	 * @return Object
	 */
	public Object getAdaptedElement() {
		return adaptedElement;
	}

	/**
	 * Returns the element.
	 * @return Object
	 */
	public Object getElement() {
		return element;
	}

}
