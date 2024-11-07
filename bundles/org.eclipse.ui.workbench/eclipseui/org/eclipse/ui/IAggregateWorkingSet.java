/*******************************************************************************
 * Copyright (c) 2009, 2015 Oakland Software and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Francis Upton IV, Oakland Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

/**
 * Contains a set of {@link IWorkingSet}.
 *
 * Sets of working sets are used by viewers to contain all of the working sets
 * being shown. Sets can also be nested.
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
	 * Clients can do what they wish with the returned array, as it will have no
	 * effect on the state of the this object.
	 *
	 * @return the working sets contained in this aggregate working set.
	 */
	IWorkingSet[] getComponents();
}
