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
 * @param <F>
 * @param <C>
 *
 */
public abstract class AbstractCompositeFactory<F extends AbstractCompositeFactory<?, ?>, C extends Composite>
		extends AbstractControlFactory<F, C> {

	/**
	 * @param factoryClass
	 * @param controlCreator
	 */
	protected AbstractCompositeFactory(Class<F> factoryClass, WidgetSupplier<C, Composite> controlCreator) {
		super(factoryClass, controlCreator);
	}

	/**
	 * Sets the layout.
	 *
	 * @param layout
	 * @return this
	 */
	public F layout(Layout layout) {
		addProperty(control -> control.setLayout(layout));
		return cast(this);
	}
}