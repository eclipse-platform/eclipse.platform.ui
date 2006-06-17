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

package org.eclipse.jface.internal.databinding.provisional.viewers;

import org.eclipse.jface.internal.databinding.provisional.conversion.IConverter;
import org.eclipse.jface.internal.databinding.provisional.observable.IObservableCollection;
import org.eclipse.jface.internal.databinding.provisional.observable.mapping.IMultiMapping;

/**
 * 
 * <p>
 * This interface is not intended to be implemented by clients. Clients should
 * instead subclass one of the classes that implement this interface. Note that
 * direct implementers of this interface outside of the framework will be broken
 * in future releases when methods are added to this interface.
 * </p>
 * 
 * @since 1.0
 * 
 */
public interface IObservableCollectionWithLabels extends IObservableCollection {

	/**
	 * @param labelMapping
	 */
	public void init(IMultiMapping labelMapping);

	/**
	 * @param elements
	 */
	public void updateElements(Object[] elements);

	/**
	 * @param converters
	 */
	public void setModelToTargetConverters(IConverter[] converters);

}
