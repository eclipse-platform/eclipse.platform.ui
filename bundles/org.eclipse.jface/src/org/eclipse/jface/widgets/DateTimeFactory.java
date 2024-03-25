/*******************************************************************************
* Copyright (c) 2020 vogella GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Lars Vogel - initial version
******************************************************************************/
package org.eclipse.jface.widgets;

import java.util.function.Consumer;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;

/**
 * This class provides a convenient shorthand for creating and initializing
 * {@link DateTime}. This offers several benefits over creating with widget the
 * normal way:
 *
 * <ul>
 * <li>The same factory can be used many times to create several instances</li>
 * <li>The setters all return "this", allowing them to be chained</li>
 * <li>The {@link #onSelect}) accepts a Lambda for {@link SelectionEvent}</li>
 * </ul>
 *
 * Example usage:
 *
 * <pre>
 * DateTime DateTime = DateTimeFactory.newDateTime(SWT.BORDER) //
 * 		.onSelect(event -&gt; DateTimeClicked(event)) //
 * 		.layoutData(gridData) //
 * 		.create(parent);
 * </pre>
 * <p>
 * The above example creates a new DateTime, registers a SelectionListener and
 * finally creates the DateTime in "parent".
 * </p>
 *
 * <pre>
 * GridDataFactory gridDataFactory = GridDataFactory.swtDefaults();
 * DateTimeFactory DateTimeFactory = DateTimeFactory.newDateTime(SWT.BORDER).onSelect(event -&gt; DateTimeClicked(event))
 * 		.layout(gridDataFactory::create);
 * DateTimeFactory.create(parent);
 * DateTimeFactory.create(parent);
 * DateTimeFactory.create(parent);
 * </pre>
 * <p>
 * The above example creates three DateTimes using the same instance of
 * DateTimeFactory. Note the layout method. A Supplier is used to create unique
 * GridData for every single widget.
 * </p>
 *
 * @since 3.22
 */
public final class DateTimeFactory extends AbstractControlFactory<DateTimeFactory, DateTime> {

	private DateTimeFactory(int style) {
		super(DateTimeFactory.class, (Composite parent) -> new DateTime(parent, style));
	}

	/**
	 * Creates a new DateTimeFactory with the given style. Refer to
	 * {@link DateTime#DateTime(Composite, int)} for possible styles.
	 *
	 * @return a new DateTimeFactory instance
	 */
	public static DateTimeFactory newDateTime(int style) {
		return new DateTimeFactory(style);
	}

	/**
	 * Creates a {@link SelectionListener} and registers it for the widgetSelected
	 * event. If the receiver is selected by the user the given consumer is invoked.
	 * The {@link SelectionEvent} is passed to the consumer.
	 *
	 * @param consumer the consumer whose accept method is called
	 * @return this
	 *
	 * @see DateTime#addSelectionListener(SelectionListener)
	 * @see SelectionListener#widgetSelectedAdapter(Consumer)
	 */
	public DateTimeFactory onSelect(Consumer<SelectionEvent> consumer) {
		addProperty(c -> c.addSelectionListener(SelectionListener.widgetSelectedAdapter(consumer)));
		return this;
	}

	/**
	 * Creates a {@link SelectionListener} and registers it for the
	 * widgetDefaultSelected event. If the receiver is selected by the user the
	 * given consumer is invoked. The {@link SelectionEvent} is passed to the
	 * consumer.
	 *
	 * @param consumer the consumer whose accept method is called
	 * @return this
	 *
	 * @see DateTime#addSelectionListener(SelectionListener)
	 * @see SelectionListener#widgetDefaultSelectedAdapter(Consumer)
	 */
	public DateTimeFactory onDefaultSelect(Consumer<SelectionEvent> consumer) {
		addProperty(c -> c.addSelectionListener(SelectionListener.widgetDefaultSelectedAdapter(consumer)));
		return this;
	}
}