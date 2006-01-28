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

package org.eclipse.jface.databinding;


/**
 * @since 3.2
 *
 */
public interface IUpdatableTable extends IUpdatableSet {
	
	public void setCellProvider(ICellProvider cellProvider);
	public void updateElements(Object[] elements);

}

/*
 * implement class TableViewerTableThing that wraps a TableViewer
 */