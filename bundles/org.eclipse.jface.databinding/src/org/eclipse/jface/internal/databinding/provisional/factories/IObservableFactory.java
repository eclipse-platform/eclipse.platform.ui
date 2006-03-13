/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.provisional.factories;

import org.eclipse.jface.internal.databinding.provisional.observable.IObservable;

/**
 * A factory for creating observable objects from description objects.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 1.0
 * 
 */
public interface IObservableFactory {

	/**
	 * Returns an observable for the given description, or null if this factory
	 * cannot create observables for this description. The BindingException is
	 * only thrown in error cases, e.g. if the description itself is invalid, or
	 * if an error occurred during the creation of the observable.
	 * @param description
	 * 
	 * @return an updatable
	 */
	IObservable createObservable(Object description);
}
