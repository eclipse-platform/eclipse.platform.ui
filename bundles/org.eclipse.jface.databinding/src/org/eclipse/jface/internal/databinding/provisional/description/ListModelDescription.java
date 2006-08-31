/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional.description;

/**
 * TODO Javadoc
 * 
 * @since 1.0
 * @deprecated no longer part of the API
 * 
 */
public class ListModelDescription {

	private final Property collectionProperty;

	private final Object propertyID;

	/**
	 * @param collectionProperty
	 * @param propertyID 
	 */
	public ListModelDescription(Property collectionProperty, Object propertyID) {
		this.collectionProperty = collectionProperty;
		this.propertyID = propertyID;
	}

	/**
	 * @return the collection property
	 */
	public Property getCollectionProperty() {
		return collectionProperty;
	}

	/**
	 * @return Returns the propertyID.
	 */
	public Object getPropertyID() {
		return propertyID;
	}

}
