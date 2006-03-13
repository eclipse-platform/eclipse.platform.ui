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

package org.eclipse.jface.internal.databinding.provisional.factories;

import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.description.ListModelDescription;
import org.eclipse.jface.internal.databinding.provisional.description.TableModelDescription;
import org.eclipse.jface.internal.databinding.provisional.observable.IObservable;

/**
 * @since 3.2
 * 
 */
public class DefaultObservableFactory implements IObservableFactory {

	private final DataBindingContext dataBindingContext;

	/**
	 * @param dataBindingContext
	 *            TODO
	 * 
	 */
	public DefaultObservableFactory(DataBindingContext dataBindingContext) {
		this.dataBindingContext = dataBindingContext;
	}

	public IObservable createObservable(Object description) {
		if (description instanceof ListModelDescription) {
			ListModelDescription listModelDescription = (ListModelDescription) description;
			TableModelDescription tableModelDescription = new TableModelDescription(
					listModelDescription.getCollectionProperty(),
					new Object[] { listModelDescription.getPropertyID() });
			return dataBindingContext.createObservable(tableModelDescription);
		}
		return null;
	}

}
