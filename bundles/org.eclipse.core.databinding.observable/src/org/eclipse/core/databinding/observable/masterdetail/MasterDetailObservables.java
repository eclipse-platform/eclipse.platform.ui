/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 147515
 *     Matthew Hall - bug 221704
 *******************************************************************************/

package org.eclipse.core.databinding.observable.masterdetail;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.internal.databinding.observable.masterdetail.DetailObservableList;
import org.eclipse.core.internal.databinding.observable.masterdetail.DetailObservableMap;
import org.eclipse.core.internal.databinding.observable.masterdetail.DetailObservableSet;
import org.eclipse.core.internal.databinding.observable.masterdetail.DetailObservableValue;

/**
 * Allows for the observation of an attribute, the detail, of an observable
 * representing selection or another transient instance, the master.
 * 
 * @since 1.0
 */
public class MasterDetailObservables {
	
	/**
	 * Creates a detail observable value from a master observable value and a
	 * factory. This can be used to create observable values that represent a
	 * property of a selected object in a table.
	 * 
	 * @param master
	 *            the observable value to track
	 * @param detailFactory
	 *            a factory for creating {@link IObservableValue} instances
	 *            given a current value of the master
	 * @param detailType
	 *            the value type of the detail observable value, typically of
	 *            type java.lang.Class and can be <code>null</code>
	 * @return an observable value of the given value type that, for any current
	 *         value of the given master value, behaves like the observable
	 *         value created by the factory for that current value.
	 */
	public static IObservableValue detailValue(IObservableValue master,
			IObservableFactory detailFactory, Object detailType) {
		return new DetailObservableValue(master, detailFactory, detailType);
	}

	/**
	 * Creates a detail observable list from a master observable value and a
	 * factory. This can be used to create observable lists that represent a
	 * list property of a selected object in a table.
	 * 
	 * @param master
	 *            the observable value to track
	 * @param detailFactory
	 *            a factory for creating {@link IObservableList} instances given
	 *            a current value of the master
	 * @param detailElementType
	 *            the element type of the detail observable list, typically of
	 *            type java.lang.Class and can be <code>null</code>
	 * @return an observable list with the given element type that, for any
	 *         current value of the given master value, behaves like the
	 *         observable list created by the factory for that current value.
	 */
	public static IObservableList detailList(IObservableValue master,
			IObservableFactory detailFactory, Object detailElementType) {
		return new DetailObservableList(detailFactory, master,
				detailElementType);
	}

	/**
	 * Creates a detail observable set from a master observable value and a
	 * factory. This can be used to create observable sets that represent a set
	 * property of a selected object in a table.
	 * 
	 * @param master
	 *            the observable value to track
	 * @param detailFactory
	 *            a factory for creating {@link IObservableSet} instances given
	 *            a current value of the master
	 * @param detailElementType
	 *            the element type of the detail observable set, typically of
	 *            type java.lang.Class and can be <code>null</code>
	 * @return an observable set with the given element type that, for any
	 *         current value of the given master value, behaves like the
	 *         observable set created by the factory for that current value.
	 */
	public static IObservableSet detailSet(IObservableValue master,
			IObservableFactory detailFactory, Object detailElementType) {
		return new DetailObservableSet(detailFactory, master, detailElementType);
	}

	/**
	 * Creates a detail observable map from a master observable value and a
	 * factory. This can be used to create observable maps that represent a map
	 * property of a selected object in a table.
	 * 
	 * @param master
	 *            the observable value to track
	 * @param detailFactory
	 *            a factory for createing {@link IObservableMap} instances given
	 *            a current value of the master
	 * @return an observable map that, for any current value of the given master
	 *         value, behaves like the observable map created by the factory for
	 *         that current value.
	 * @since 1.1
	 */
	public static IObservableMap detailMap(IObservableValue master,
			IObservableFactory detailFactory) {
		return new DetailObservableMap(detailFactory, master);
	}
}
