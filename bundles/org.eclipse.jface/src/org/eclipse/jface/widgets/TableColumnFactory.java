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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

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
 * {@link TableColumn}. This offers several benefits over creating TableColumn
 * normal way:
 *
 * <ul>
 * <li>The same factory can be used many times to create several TableColumn
 * instances</li>
 * <li>The setters on TableColumnFactory all return "this", allowing them to be
 * chained</li>
 * <li>TableColumnFactory accepts a Lambda for {@link SelectionEvent} (see
 * {@link #onSelect})</li>
 * </ul>
 *
 * Example usage:
 *
 * <pre>
 * TableColumn column = TableColumnFactory.newTableColumn(SWT.CENTER) //
 * 		.text("Table Column") //
 * 		.onSelect(event -> columnClicked(event)) //
 * 		.create(table);
 * </pre>
 * <p>
 * The above example creates a table column, sets text, registers a
 * SelectionListener and finally creates the table column in "table".
 * <p>
 *
 * <pre>
 * TableColumnFactory factory = TableColumnFactory.newTableColumn(SWT.CENTER).onSelect(event -> columnClicked(event));
 * factory.text("Column 1").create(table);
 * factory.text("Column 2").create(table);
 * factory.text("Column 3").create(table);
 * </pre>
 * <p>
 * The above example creates three table columns using the same instance of
 * factory.
 * <p>
 *
 */
public class TableColumnFactory extends AbstractItemFactory<TableColumnFactory, TableColumn, Table> {

	private TableColumnFactory(int style) {
		super(TableColumnFactory.class, table -> new TableColumn(table, style));
	}

	/**
	 * Creates a new TableColumnFactory with the given style. Refer to
	 * {@link TableColumn#TableColumn(Table, int)} for possible styles.
	 *
	 * @param style
	 * @return a new TableColumnFactory instance
	 */
	public static TableColumnFactory newTableColumn(int style) {
		return new TableColumnFactory(style);

	}

	/**
	 * Creates a {@link SelectionListener} and registers it for the widgetSelected
	 * event. If event is raised it calls the given consumer. The
	 * {@link SelectionEvent} is passed to the consumer.
	 *
	 * @param consumer
	 * @return this
	 */
	public TableColumnFactory onSelect(Consumer<SelectionEvent> consumer) {
		addProperty(c -> c.addSelectionListener(SelectionListener.widgetSelectedAdapter(consumer)));
		return this;
	}

	/**
	 * Sets the alignment.
	 *
	 * @param alignment
	 * @return this
	 */
	public TableColumnFactory align(int alignment) {
		addProperty(c -> c.setAlignment(alignment));
		return this;
	}

	/**
	 * Sets the tooltip.
	 *
	 * @param tooltip
	 * @return this
	 */
	public TableColumnFactory tooltip(String tooltip) {
		addProperty(c -> c.setToolTipText(tooltip));
		return this;
	}

	/**
	 * Sets the width.
	 *
	 * @param width
	 * @return this
	 */
	public TableColumnFactory width(int width) {
		addProperty(c -> c.setWidth(width));
		return this;
	}

}
