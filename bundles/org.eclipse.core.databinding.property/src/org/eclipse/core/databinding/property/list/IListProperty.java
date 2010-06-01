/*******************************************************************************
 * Copyright (c) 2008, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 195222
 ******************************************************************************/

package org.eclipse.core.databinding.property.list;

import java.util.List;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;

/**
 * Interface for list-typed properties.
 * 
 * @since 1.2
 * @noimplement This interface is not intended to be implemented by clients.
 *              Clients should instead subclass one of the classes that
 *              implement this interface. Note that direct implementers of this
 *              interface outside of the framework will be broken in future
 *              releases when methods are added to this interface.
 * @see ListProperty
 * @see SimpleListProperty
 */
public interface IListProperty extends IProperty {
	/**
	 * Returns the type of the elements in the collection or <code>null</code>
	 * if untyped
	 * 
	 * @return the type of the elements in the collection or <code>null</code>
	 *         if untyped
	 */
	public Object getElementType();

	/**
	 * Returns an unmodifiable List with the current contents of the source's
	 * list property
	 * 
	 * @param source
	 *            the property source (may be null)
	 * @return an unmodifiable List with the current contents of the source's
	 *         list property
	 * @since 1.3
	 */
	public List getList(Object source);

	/**
	 * Updates the property on the source with the specified change
	 * <p>
	 * <b>Note:</b> This method is made available to facilitate basic property
	 * access. However if the property source lacks property change
	 * notification, then observables on the source object may not be notified
	 * of the change. In most cases it is preferable to modify the source
	 * through an {@link IObservableList} than through the property directly.
	 * </p>
	 * 
	 * @param source
	 *            the property source (may be null)
	 * @param list
	 *            the new list
	 * @since 1.3
	 */
	public void setList(Object source, List list);

	/**
	 * Updates the property on the source with the specified change
	 * <p>
	 * <b>Note:</b> This method is made available to facilitate basic property
	 * access. However if the property source lacks property change
	 * notification, then observables on the source object may not be notified
	 * of the change. In most cases it is preferable to modify the source
	 * through an {@link IObservableList} than through the property directly.
	 * 
	 * @param source
	 *            the property source (may be null)
	 * @param diff
	 *            a diff describing the change
	 * @since 1.3
	 */
	public void updateList(Object source, ListDiff diff);

	/**
	 * Returns an observable list observing this list property on the given
	 * property source
	 * 
	 * @param source
	 *            the property source
	 * @return an observable list observing this list property on the given
	 *         property source
	 */
	public IObservableList observe(Object source);

	/**
	 * Returns an observable list observing this list property on the given
	 * property source
	 * 
	 * @param realm
	 *            the observable's realm
	 * @param source
	 *            the property source
	 * @return an observable list observing this list property on the given
	 *         property source
	 */
	public IObservableList observe(Realm realm, Object source);

	/**
	 * Returns a factory for creating observable lists tracking this property of
	 * a particular property source.
	 * 
	 * @return a factory for creating observable lists tracking this property of
	 *         a particular property source.
	 */
	public IObservableFactory listFactory();

	/**
	 * Returns a factory for creating observable lists in the given realm,
	 * tracking this property of a particular property source.
	 * 
	 * @param realm
	 *            the realm
	 * 
	 * @return a factory for creating observable lists in the given realm,
	 *         tracking this property of a particular property source.
	 */
	public IObservableFactory listFactory(Realm realm);

	/**
	 * Returns an observable list on the master observable's realm which tracks
	 * this property of the current value of <code>master</code>.
	 * 
	 * @param master
	 *            the master observable
	 * @return an observable list on the given realm which tracks this property
	 *         of the current value of <code>master</code>.
	 */
	public IObservableList observeDetail(IObservableValue master);

	/**
	 * Returns the nested combination of this property and the specified detail
	 * value property. Note that because this property is a projection of value
	 * properties over a list, the only modification supported is through the
	 * {@link IObservableList#set(int, Object)} method. Modifications made
	 * through the returned property are delegated to the detail property, using
	 * the corresponding list element from the master property as the source.
	 * 
	 * @param detailValue
	 *            the detail property
	 * @return the nested combination of the master list and detail value
	 *         properties
	 */
	public IListProperty values(IValueProperty detailValue);
}
