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
 * <p>
 * Note that this class does not extend {@link AbstractCompositeFactory} even
 * though Spinner extends Composite. This is because Spinner is not supposed to
 * be used like a Composite.
 * </p>
 *
 * @since 3.18
 */
public final class SpinnerFactory extends AbstractControlFactory<SpinnerFactory, Spinner> {

	private SpinnerFactory(int style) {
		super(SpinnerFactory.class, (Composite parent) -> new Spinner(parent, style));
	}

	/**
	 * Creates a new SpinnerFactory with the given style. Refer to
	 * {@link Spinner#Spinner(Composite, int)} for possible styles.
	 *
	 * @return a new SpinnerFactory instance
	 */
	public static SpinnerFactory newSpinner(int style) {
		return new SpinnerFactory(style);
	}

	/**
	 * Sets the minimum and maximum value that the receiver will allow.
	 *
	 * @param minimum the minimum
	 * @param maximum the maximum
	 * @return this
	 *
	 * @see Spinner#setMinimum(int)
	 * @see Spinner#setMaximum(int)
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
	 * Sets the amounts (which must be at least one) that the receiver's value will
	 * be modified by when the up/down arrows or the page up/down keys are pressed
	 * to the argument.
	 *
	 * @param increment     the increment (must be greater than zero)
	 * @param pageIncrement the page increment (must be greater than zero)
	 * @return this
	 *
	 * @see Spinner#setIncrement(int)
	 * @see Spinner#setPageIncrement(int)
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
	 * Sets the maximum number of characters that the receiver's text field is
	 * capable of holding to be the argument.
	 *
	 * @param limit the limit
	 * @return this
	 *
	 * @see Spinner#setTextLimit(int)
	 */
	public SpinnerFactory limitTo(int limit) {
		addProperty(s -> s.setTextLimit(limit));
		return this;
	}

	/**
	 * Creates a {@link SelectionListener} and registers it for the widgetSelected
	 * event. If the receiver is selected by the user the given consumer is invoked.
	 * The {@link SelectionEvent} is passed to the consumer.
	 *
	 * @param consumer the consumer whose accept method is called
	 * @return this
	 *
	 * @see Spinner#addSelectionListener(SelectionListener)
	 * @see SelectionListener#widgetSelectedAdapter(Consumer)
	 */
	public SpinnerFactory onSelect(Consumer<SelectionEvent> consumer) {
		SelectionListener listener = SelectionListener.widgetSelectedAdapter(consumer);
		addProperty(s -> s.addSelectionListener(listener));
		return this;
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified when
	 * the receiver's text is modified, by calling the modifyText method.
	 * <p>
	 * Can be called several times to add more than one ModifyListener.
	 * </p>
	 *
	 * @param listener the listener which should be notified
	 * @return this
	 *
	 * @see Spinner#addModifyListener(ModifyListener)
	 * @see ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	public SpinnerFactory onModify(ModifyListener listener) {
		addProperty(s -> s.addModifyListener(listener));
		return this;
	}
}