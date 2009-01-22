/*******************************************************************************
 * Copyright (c) 2009 Oakland Software and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Francis Upton IV, Oakland Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

/**
 * Contains a set of {@link IWorkingSet}.  
 * 
 * Sets of working sets are used by viewers to contain all of the working
 * sets being shown.  Sets can also be nested.
 * 
 * In classes that implement this, the {@link IWorkingSet#getElements()} returns
 * all of the elements of each of the working sets in this set.
 * 
 * Instances of {@link IWorkingSet} can be cast to IAggregateWorkingSet if
 * {@link IWorkingSet#isAggregateWorkingSet()} is true.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 3.5 initial version
 */
public interface IAggregateWorkingSet extends IWorkingSet {
	
	/**
	 * Returns the working sets contained in this aggregate working set.
	 * 
	 * <p>
	 * The returned array is subject to change if the aggregate working set
	 * changes.  Clients should not modify the contents of the array.
	 * 
	 * @return the working sets contained in this aggregate working set.
	 */
	public IWorkingSet[] getComponents();
}
