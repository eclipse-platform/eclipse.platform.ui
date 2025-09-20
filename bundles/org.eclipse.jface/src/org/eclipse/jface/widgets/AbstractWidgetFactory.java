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

import org.eclipse.pde.api.tools.annotations.NoExtend;
import org.eclipse.swt.widgets.Widget;

/**
 * Abstract factory for widgets. Factories for widgets that inherit from Widget
 * should extend this factory to handle creation of widgets and setting all the
 * properties.
 *
 * @param <F> factory
 * @param <W> widget
 * @param <P> parent
 *
 * @since 3.18
 */
@NoExtend
public abstract class AbstractWidgetFactory<F extends AbstractWidgetFactory<?, ?, ?>, W extends Widget, P extends Widget> {
	private final Class<F> factoryClass;

	private final WidgetSupplier<W, P> widgetCreator;

	private final List<Property<W>> properties = new ArrayList<>();

	AbstractWidgetFactory(Class<F> factoryClass, WidgetSupplier<W, P> widgetCreator) {
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
	 * @return this
	 */
	public final W create(P parent) {
		W widget = widgetCreator.create(parent);
		applyProperties(widget);
		return widget;
	}

	void applyProperties(W widget) {
		properties.forEach(p -> p.apply(widget));
	}

	/**
	 * Adds a property like image, text, enabled, listeners, ... to the widget.
	 *
	 * <br>
	 * Example:
	 *
	 * <pre>
	 * public LabelFactory text(String text) {
	 * 	addProperty(l -&gt; l.setText(text));
	 * 	return this;
	 * }
	 * </pre>
	 *
	 * @param property usually a lambda
	 */
	protected final void addProperty(Property<W> property) {
		this.properties.add(property);
	}

	/**
	 * Sets the application defined widget data associated with the receiver to be
	 * the argument. The <em>widget data</em> is a single, unnamed field that is
	 * stored with every widget.
	 *
	 * @param data the widget data
	 * @return this
	 *
	 * @see Widget#setData(Object)
	 *
	 * @since 3.21
	 */
	public F data(Object data) {
		addProperty(b -> b.setData(data));
		return cast(this);
	}

	/**
	 * Sets the application defined property of the receiver with the specified name
	 * to the given value.
	 *
	 * @param key   the name of the property
	 * @param value the new value for the property
	 * @return this
	 *
	 * @see Widget#setData(String, Object)
	 *
	 * @since 3.33
	 */
	public F data(String key, Object value) {
		addProperty(b -> b.setData(key, value));
		return cast(this);
	}
}