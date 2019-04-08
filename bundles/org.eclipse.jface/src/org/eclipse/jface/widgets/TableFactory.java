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
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class has been added as part of a work in
 * progress. There is no guarantee that this API will work or that it will
 * remain the same. Feel free to use it and give feedback via
 * https://bugs.eclipse.org/bugs/buglist.cgi?component=UI&product=Platform, but
 * be aware that it might change.
 * </p>
 *
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
 * 		.onSelect(e -> tableClicked(e)) //
 * 		.create(parent);
 * </pre>
 * <p>
 * The above example creates a table, sets some properties, registers a
 * SelectionListener and finally creates the table in "parent".
 * <p>
 *
 */
public class TableFactory extends AbstractCompositeFactory<TableFactory, Table> {

	private TableFactory(int style) {
		super(TableFactory.class, (parent) -> new Table(parent, style));
	}

	/**
	 * Creates a new TableFactory with the given style. Refer to
	 * {@link Table#Table(Composite, int)} for possible styles.
	 *
	 * @param style
	 * @return a new TableFactory instance
	 */
	public static TableFactory newTable(int style) {
		return new TableFactory(style);
	}

	/**
	 * Creates a {@link SelectionListener} and registers it for the widgetSelected
	 * event. If event is raised it calls the given consumer. The
	 * {@link SelectionEvent} is passed to the consumer.
	 *
	 * @param consumer
	 * @return this
	 */
	public TableFactory onSelect(Consumer<SelectionEvent> consumer) {
		addProperty(c -> c.addSelectionListener(SelectionListener.widgetSelectedAdapter(consumer)));
		return this;
	}

	/**
	 * Sets the header visibility.
	 *
	 * @param visible
	 * @return this
	 */
	public TableFactory headerVisible(boolean visible) {
		addProperty((t) -> t.setHeaderVisible(visible));
		return this;
	}

	/**
	 * Sets the lines visibility.
	 *
	 * @param visible
	 * @return this
	 */
	public TableFactory linesVisible(boolean visible) {
		addProperty((t) -> t.setLinesVisible(visible));
		return this;
	}

	/**
	 * Sets the item count of the table.
	 *
	 * @param count
	 * @return this
	 */
	public TableFactory itemCount(int count) {
		addProperty((t) -> t.setItemCount(count));
		return this;
	}

}
