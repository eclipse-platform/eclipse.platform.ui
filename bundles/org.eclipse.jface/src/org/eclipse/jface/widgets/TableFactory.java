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

import java.util.function.Consumer;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

/**
 * This class provides a convenient shorthand for creating and initializing
 * {@link Table}. This offers several benefits over creating Table normal way:
 *
 * <ul>
 * <li>The same factory can be used many times to create several Table
 * instances</li>
 * <li>The setters on TableFactory all return "this", allowing them to be
 * chained</li>
 * <li>TableFactory accepts a Lambda for {@link SelectionEvent} (see
 * {@link #onSelect})</li>
 * </ul>
 *
 * Example usage:
 *
 * <pre>
 * Table table = TableFactory.newTable(SWT.CHECK) //
 * 		.headerVisible(true) //
 * 		.linesVisible(true) //
 * 		.onSelect(e -&gt; tableClicked(e)) //
 * 		.create(parent);
 * </pre>
 *
 * <p>
 * The above example creates a table, sets some properties, registers a
 * SelectionListener and finally creates the table in "parent".
 * </p>
 *
 * <p>
 * Note that this class does not extend {@link AbstractCompositeFactory} even
 * though Table extends Composite. This is because Table is not supposed to be
 * used like a Composite.
 * </p>
 *
 * @since 3.18
 */
public final class TableFactory extends AbstractControlFactory<TableFactory, Table> {

	private TableFactory(int style) {
		super(TableFactory.class, parent -> new Table(parent, style));
	}

	/**
	 * Creates a new TableFactory with the given style. Refer to
	 * {@link Table#Table(Composite, int)} for possible styles.
	 *
	 * @return a new TableFactory instance
	 */
	public static TableFactory newTable(int style) {
		return new TableFactory(style);
	}

	/**
	 * Creates a {@link SelectionListener} and registers it for the widgetSelected
	 * event. If the receiver is selected by the user the given consumer is invoked.
	 * The {@link SelectionEvent} is passed to the consumer.
	 *
	 * @param consumer the consumer whose accept method is called
	 * @return this
	 *
	 * @see Table#addSelectionListener(SelectionListener)
	 * @see SelectionListener#widgetSelectedAdapter(Consumer)
	 */
	public TableFactory onSelect(Consumer<SelectionEvent> consumer) {
		addProperty(c -> c.addSelectionListener(SelectionListener.widgetSelectedAdapter(consumer)));
		return this;
	}

	/**
	 * Marks the receiver's header as visible if the argument is true, and marks it
	 * invisible otherwise.
	 *
	 * @param visible the visibility state
	 * @return this
	 *
	 * @see Table#setHeaderVisible(boolean)
	 */
	public TableFactory headerVisible(boolean visible) {
		addProperty(t -> t.setHeaderVisible(visible));
		return this;
	}

	/**
	 * Marks the receiver's lines as visible if the argument is true, and marks it
	 * invisible otherwise. Note that some platforms draw grid lines while others
	 * may draw alternating row colors.
	 *
	 * @param visible the visibility state
	 * @return this
	 *
	 * @see Table#setLinesVisible(boolean)
	 */
	public TableFactory linesVisible(boolean visible) {
		addProperty(t -> t.setLinesVisible(visible));
		return this;
	}

	/**
	 * Sets the number of items contained in the receiver.
	 *
	 * @param count the number of items
	 * @return this
	 *
	 * @see Table#setItemCount(int)
	 */
	public TableFactory itemCount(int count) {
		addProperty(t -> t.setItemCount(count));
		return this;
	}
}