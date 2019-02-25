/*******************************************************************************
* Copyright (c) 2019 SAP SE and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     SAP SE - initial version
******************************************************************************/
package org.eclipse.jface.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Widget;

/**
 * Abstract factory for widgets. Factories for widgets that inherit from Widget
 * should extend this factory to handle creation of widgets and setting all the
 * properties.
 *
 * @param <F> factory
 * @param <W> widget
 * @param <P> parent
 */
public abstract class AbstractWidgetFactory<F extends AbstractWidgetFactory<?, ?, ?>, W extends Widget, P extends Widget> {
	private Class<F> factoryClass;

	private WidgetSupplier<W, P> widgetCreator;

	private List<Property<W>> properties = new ArrayList<>();

	/**
	 * @param factoryClass
	 * @param widgetCreator
	 */
	protected AbstractWidgetFactory(Class<F> factoryClass, WidgetSupplier<W, P> widgetCreator) {
		this.factoryClass = factoryClass;
		this.widgetCreator = widgetCreator;
	}

	/**
	 * Casts to the concrete instance of factory class. Needs to be called by
	 * abstract factory classes.
	 *
	 * @param factory extending WidgetFactory, usually "this"
	 * @return casted factory
	 */
	protected final F cast(AbstractWidgetFactory<F, W, P> factory) {
		return factoryClass.cast(factory);
	}

	/**
	 * @param parent
	 * @return this
	 */
	public final W create(P parent) {
		W widget = widgetCreator.create(parent);
		properties.forEach(p -> p.apply(widget));
		return widget;
	}

	/**
	 * Adds a property like image, text, enabled, listeners, ... to the widget.
	 *
	 * <br/>
	 * Example:
	 *
	 * <pre>
	 * public LabelFactory text(String text) {
	 * 	addProperty(l -> l.setText(text));
	 * 	return this;
	 * }
	 * </pre>
	 *
	 * @param property usually a lambda
	 */
	protected final void addProperty(Property<W> property) {
		this.properties.add(property);
	}
}