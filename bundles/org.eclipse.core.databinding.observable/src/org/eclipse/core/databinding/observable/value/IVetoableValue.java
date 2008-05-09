/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.databinding.observable.value;

/**
 * An observable value whose changes can be vetoed by listeners.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 *              Clients should instead subclass one of the classes that
 *              implement this interface. Note that direct implementers of this
 *              interface outside of the framework will be broken in future
 *              releases when methods are added to this interface.
 * 
 * @since 1.0
 * 
 */
public interface IVetoableValue extends IObservableValue {
	
	/**
	 * @param listener
	 */
	public void addValueChangingListener(IValueChangingListener listener);
	
	/**
	 * @param listener
	 */
	public void removeValueChangingListener(IValueChangingListener listener);

}
