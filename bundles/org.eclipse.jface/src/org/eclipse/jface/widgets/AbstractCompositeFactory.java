/*******************************************************************************
 * Copyright (c) 2019 Marcus Hoepfner and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marcus Hoepfner - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.widgets;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

/**
 * Abstract factory for composites. Factories for widgets that inherit from
 * Composite should extend this factory to handle the properties of Composite
 * itself, like layout.
 *
 * @param <F> factory
 * @param <C> control
 *
 * @noextend this class is not intended to be subclassed by clients.
 *
 * @since 3.18
 */
public abstract class AbstractCompositeFactory<F extends AbstractCompositeFactory<?, ?>, C extends Composite>
		extends AbstractControlFactory<F, C> {

	protected AbstractCompositeFactory(Class<F> factoryClass, WidgetSupplier<C, Composite> controlCreator) {
		super(factoryClass, controlCreator);
	}

	/**
	 * Sets the layout which is associated with the receiver to be the argument
	 * which may be null.
	 *
	 * @param layout the receiver's layout or null
	 * @return this
	 *
	 * @see Composite#setLayout(Layout)
	 */
	public F layout(Layout layout) {
		addProperty(control -> control.setLayout(layout));
		return cast(this);
	}
}