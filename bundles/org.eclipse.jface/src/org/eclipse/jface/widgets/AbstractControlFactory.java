/*******************************************************************************
* Copyright (c) 2018 SAP SE and others.
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
*     Simon Scholz <simon.scholz@vogella.com> - Bug 544471
******************************************************************************/
package org.eclipse.jface.widgets;

import java.util.function.Supplier;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Abstract factory for controls. Factories for widgets that inherit from
 * Control should extend this factory to handle the properties of Control
 * itself, like enabled or tool tip.
 *
 * @param <F> factory
 * @param <C> control
 *
 * @noextend this class is not intended to be subclassed by clients.
 *
 * @since 3.18
 */
public abstract class AbstractControlFactory<F extends AbstractControlFactory<?, ?>, C extends Control>
		extends AbstractWidgetFactory<F, C, Composite> {

	AbstractControlFactory(Class<F> factoryClass, WidgetSupplier<C, Composite> controlCreator) {
		super(factoryClass, controlCreator);
	}

	/**
	 * Sets the receiver's tool tip text to the argument, which may be null
	 * indicating that the default tool tip for the control will be shown. For a
	 * control that has a default tool tip, such as the Tree control on Windows,
	 * setting the tool tip text to an empty string replaces the default, causing no
	 * tool tip text to be shown.
	 *
	 * The mnemonic indicator (character '&amp;') is not displayed in a tool tip. To
	 * display a single '&amp;' in the tool tip, the character '&amp;' can be
	 * escaped by doubling it in the string.
	 *
	 * @param tooltipText the tool tip text
	 * @return this
	 *
	 * @see Control#setToolTipText(String)
	 */
	public F tooltip(String tooltipText) {
		addProperty(c -> c.setToolTipText(tooltipText));
		return cast(this);
	}

	/**
	 * Enables the receiver if the argument is true, and disables it otherwise. A
	 * disabled control is typically not selectable from the user interface and
	 * draws with an inactive or "grayed" look.
	 *
	 * @param enabled the enabled state
	 * @return this
	 *
	 * @see Control#setEnabled(boolean)
	 */
	public F enabled(boolean enabled) {
		addProperty(c -> c.setEnabled(enabled));
		return cast(this);
	}

	/**
	 * Sets a {@link Supplier} for the creation of layout data associated with the
	 * receiver. The supplier should always create a new instance of the layoutData
	 * in order to make this factory reusable, because each and every control needs
	 * its own unique layoutData.
	 *
	 * <pre>
	 * GridDataFactory gridDataFactory = GridDataFactory.fillDefaults().grab(true, false);
	 * ButtonFactory.newButton(SWT.PUSH).supplyLayoutData(gridDataFactory::create);
	 * </pre>
	 *
	 * or without GridDataFactory:
	 *
	 * <pre>
	 * ButtonFactory.newButton(SWT.PUSH).supplyLayoutData(GridData::new);
	 * </pre>
	 *
	 * @param layoutDataSupplier {@link Supplier} creating a new layout data
	 *                           instance on every call
	 * @return this
	 *
	 * @see Control#setLayoutData(Object)
	 * @see #layoutData(Object)
	 */
	public F supplyLayoutData(Supplier<?> layoutDataSupplier) {
		addProperty(c -> c.setLayoutData(layoutDataSupplier.get()));
		return cast(this);
	}

	/**
	 * Sets the layout data associated with the receiver to the argument.
	 *
	 * Sufficient for one time usage of this factory.
	 *
	 * <pre>
	 * GridData gd = new GridData(GridData.FILL_BOTH);
	 * ButtonFactory.newButton(SWT.PUSH).layoutData(gd);
	 * </pre>
	 *
	 * <p>
	 * In case this factory should be reused several time consider the usage of
	 * {@link #supplyLayoutData(Supplier)}.
	 * </p>
	 *
	 * @param layoutData the layout data for the receiver.
	 * @return this
	 *
	 * @see Control#setLayoutData(Object)
	 */
	public F layoutData(Object layoutData) {
		addProperty(c -> c.setLayoutData(layoutData));
		return cast(this);
	}

	/**
	 * Sets the font that the receiver will use to paint textual information to the
	 * font specified by the argument, or to the default font for that kind of
	 * control if the argument is null.
	 *
	 * @param font the font
	 * @return this
	 *
	 * @see Control#setFont(Font)
	 */
	public F font(Font font) {
		addProperty(c -> c.setFont(font));
		return cast(this);
	}

	/**
	 * Sets the receiver's foreground color to the color specified by the argument,
	 * or to the default system color for the control if the argument is null.
	 *
	 * @param color the color
	 * @return this
	 *
	 * @see Control#setForeground(Color)
	 */
	public F foreground(Color color) {
		addProperty(c -> c.setForeground(color));
		return cast(this);
	}

	/**
	 * Sets the receiver's background color to the color specified by the argument,
	 * or to the default system color for the control if the argument is null.
	 *
	 * @param color the color
	 * @return this
	 *
	 * @see Control#setBackground(Color)
	 */
	public F background(Color color) {
		addProperty(c -> c.setBackground(color));
		return cast(this);
	}

	/**
	 * Sets the orientation of the receiver, which must be one of the constants
	 * SWT.LEFT_TO_RIGHT or SWT.RIGHT_TO_LEFT.
	 *
	 * @param orientation the orientation style
	 * @return this
	 *
	 * @see Control#setOrientation(int)
	 */
	public F orientation(int orientation) {
		addProperty(t -> t.setOrientation(orientation));
		return cast(this);
	}
}