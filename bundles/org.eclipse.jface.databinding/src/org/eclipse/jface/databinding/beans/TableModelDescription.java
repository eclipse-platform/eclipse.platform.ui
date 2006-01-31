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

package org.eclipse.jface.databinding.beans;

import org.eclipse.jface.databinding.Property;

/**
 * @since 3.2
 *
 */
public class TableModelDescription {
	
	private final Property collectionProperty;
	private final Object[] columnIDs;

	public TableModelDescription(Property collectionProperty, Object[] columnIDs) {
		this.collectionProperty = collectionProperty;
		this.columnIDs = columnIDs;}

	public Property getCollectionProperty() {
		return collectionProperty;
	}

	public Object[] getColumnIDs() {
		return columnIDs;
	}

}
