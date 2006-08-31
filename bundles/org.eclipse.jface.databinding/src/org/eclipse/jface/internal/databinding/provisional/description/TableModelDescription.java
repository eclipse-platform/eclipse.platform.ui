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

import org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList;


/**
 * TODO Javadoc
 * 
 * @since 1.0
 * @deprecated no longer part of the API
 *
 */
public class TableModelDescription {
	
	private final Property collectionProperty;
	private final IObservableList observableList;
	private final Object[] columnIDs;

	/**
	 * @param collectionProperty
	 * @param columnIDs
	 */
	public TableModelDescription(Property collectionProperty, Object[] columnIDs) {
		this.collectionProperty = collectionProperty;
		this.columnIDs = columnIDs;
		this.observableList = null;
	}

	/**
	 * @param observableList
	 * @param columnIDs
	 */
	public TableModelDescription(IObservableList observableList, Object[] columnIDs) {
		this.observableList = observableList;
		this.columnIDs = columnIDs;
		this.collectionProperty = null;
	}

	/**
	 * @return the collection property
	 */
	public Property getCollectionProperty() {
		return collectionProperty;
	}

	/**
	 * @return the column identifiers
	 */
	public Object[] getColumnIDs() {
		return columnIDs;
	}

	/**
	 * @return the observable list
	 */
	public IObservableList getObservableList() {
		return observableList;
	}
}
