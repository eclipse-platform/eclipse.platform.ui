/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 475860
 *******************************************************************************/
package org.eclipse.jface.util;

import java.util.EventListener;

/**
 * Listener for property changes.
 * <p>
 * Usage:
 * </p>
 * 
 * <pre>
 * IPropertyChangeListener listener =
 *   new IPropertyChangeListener() {
 *      public void propertyChange(PropertyChangeEvent event) {
 *         ... // code to deal with occurrence of property change
 *      }
 *   };
 * emitter.addPropertyChangeListener(listener);
 * ...
 * emitter.removePropertyChangeListener(listener);
 * </pre>
 */
@FunctionalInterface
public interface IPropertyChangeListener extends EventListener {
	/**
	 * Notification that a property has changed.
	 * <p>
	 * This method gets called when the observed object fires a property
	 * change event.
	 * </p>
	 *
	 * @param event the property change event object describing which property
	 * changed and how
	 */
	public void propertyChange(PropertyChangeEvent event);
}
