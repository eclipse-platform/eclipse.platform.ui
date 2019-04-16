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

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class has been added as part of a work in
 * progress. There is no guarantee that this API will work or that it will
 * remain the same. Feel free to use it and give feedback via
 * https://bugs.eclipse.org/bugs/buglist.cgi?component=UI&product=Platform, but
 * be aware that it might change.
 * </p>
 *
 * This class provides a convenient shorthand for creating and initializing
 * {@link Spinner}. This offers several benefits over creating Spinner normal
 * way:
 *
 * <ul>
 * <li>The same factory can be used many times to create several Spinner
 * instances</li>
 * <li>The setters on SpinnerFactory all return "this", allowing them to be
 * chained</li>
 * </ul>
 *
 */
public class SpinnerFactory extends AbstractCompositeFactory<SpinnerFactory, Spinner> {

	private SpinnerFactory(int style) {
		super(SpinnerFactory.class, (Composite parent) -> new Spinner(parent, style));
	}

	/**
	 * Creates a new SpinnerFactory with the given style. Refer to
	 * {@link Spinner#Spinner(Composite, int)} for possible styles.
	 *
	 * @param style
	 * @return a new SpinnerFactory instance
	 */
	public static SpinnerFactory newSpinner(int style) {
		return new SpinnerFactory(style);
	}

	/**
	 * Sets minimum and maximum.
	 *
	 * @param minimum or SWT.DEFAULT
	 * @param maximum or SWT.DEFAULT
	 * @return this
	 */
	public SpinnerFactory bounds(int minimum, int maximum) {
		if (minimum != SWT.DEFAULT) {
			addProperty(s -> s.setMinimum(minimum));
		}
		if (maximum != SWT.DEFAULT) {
			addProperty(s -> s.setMaximum(maximum));
		}
		return this;
	}

	/**
	 * Sets the increments.
	 *
	 * @param increment     or SWT.DEFAULT
	 * @param pageIncrement or SWT.DEFAULT
	 * @return this
	 */
	public SpinnerFactory increment(int increment, int pageIncrement) {
		if (increment != SWT.DEFAULT) {
			addProperty(s -> s.setIncrement(increment));
		}
		if (pageIncrement != SWT.DEFAULT) {
			addProperty(s -> s.setPageIncrement(pageIncrement));
		}
		return this;
	}

	/**
	 * Sets the text limit.
	 *
	 * @param limit
	 * @return this
	 */
	public SpinnerFactory limitTo(int limit) {
		addProperty(s -> s.setTextLimit(limit));
		return this;
	}

	/**
	 * Creates a {@link SelectionListener} and registers it for the widgetSelected
	 * event. If event is raised it calls the given consumer. The
	 * {@link SelectionEvent} is passed to the consumer.
	 *
	 * @param consumer
	 * @return this
	 */
	public SpinnerFactory onSelect(Consumer<SelectionEvent> consumer) {
		SelectionListener listener = SelectionListener.widgetSelectedAdapter(consumer);
		addProperty(s -> s.addSelectionListener(listener));
		return this;
	}

	/**
	 * Adds a ModifyListener. Can be called several times to add more than one
	 * ModifyListener.
	 *
	 * @param listener
	 * @return this
	 */
	public SpinnerFactory onModify(ModifyListener listener) {
		addProperty(s -> s.addModifyListener(listener));
		return this;
	}
}