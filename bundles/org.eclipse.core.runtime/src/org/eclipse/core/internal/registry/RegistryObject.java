/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

/**
 * An object which has the general characteristics of all the nestable elements
 * in a plug-in manifest.
 */
public abstract class RegistryObject implements KeyedElement {
	//Object identifier
	private int objectId = RegistryObjectManager.UNKNOWN;
	//The children of the element
	protected int[] children = RegistryObjectManager.EMPTY_INT_ARRAY;
	//The position of the extra data when available
	protected int extraDataOffset = -1;

	void setRawChildren(int[] values) {
		children = values;
	}

	//This can not return null. It returns the singleton empty array or an array 
	int[] getRawChildren() {
		return children;
	}

	void setObjectId(int value) {
		objectId = value;
	}

	int getObjectId() {
		return objectId;
	}

	//Implementation of the KeyedElement interface
	public int getKeyHashCode() {
		return objectId;
	}

	public Object getKey() {
		return new Integer(objectId);
	}

	public boolean compare(KeyedElement other) {
		return objectId == ((RegistryObject) other).objectId;
	}

}