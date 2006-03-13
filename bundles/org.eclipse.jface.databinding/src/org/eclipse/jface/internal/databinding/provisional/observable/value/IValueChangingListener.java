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
package org.eclipse.jface.internal.databinding.provisional.observable.value;

/**
 * @since 1.0
 * 
 */
public interface IValueChangingListener {

	/**
	 * This method is called when the value is about to change and provides an
	 * opportunity to veto the change.
	 * 
	 * @param source
	 * @param diff
	 * @return false if this listener is vetoing the change, true otherwise
	 */
	public boolean handleValueChanging(IVetoableValue source, ValueDiff diff);

}
