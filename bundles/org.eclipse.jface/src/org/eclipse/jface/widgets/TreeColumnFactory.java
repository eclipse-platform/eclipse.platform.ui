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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

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
 * {@link TreeColumn}. This offers several benefits over creating TreeColumn
 * normal way:
 *
 * <ul>
 * <li>The same factory can be used many times to create several TreeColumn
 * instances</li>
 * <li>The setters on TreeColumnFactory all return "this", allowing them to be
 * chained</li>
 * <li>TreeColumnFactory accepts a Lambda for {@link SelectionEvent} (see
 * {@link #onSelect})</li>
 * </ul>
 *
 * Example usage:
 *
 * <pre>
 * TreeColumn column = TreeColumnFactory.newTreeColumn(SWT.CENTER) //
 * 		.text("Tree Column") //
 * 		.onSelect(event -> columnClicked(event)) //
 * 		.create(tree);
 * </pre>
 * <p>
 * The above example creates a tree column, sets text, registers a
 * SelectionListener and finally creates the tree column in "tree".
 * <p>
 *
 * <pre>
 * TreeColumnFactory factory = TreeColumnFactory.newTreeColumn(SWT.CENTER).onSelect(event -> columnClicked(event));
 * factory.text("Column 1").create(tree);
 * factory.text("Column 2").create(tree);
 * factory.text("Column 3").create(tree);
 * </pre>
 * <p>
 * The above example creates three tree columns using the same instance of
 * factory.
 * <p>
 *
 */
public class TreeColumnFactory extends AbstractItemFactory<TreeColumnFactory, TreeColumn, Tree> {

	private TreeColumnFactory(int style) {
		super(TreeColumnFactory.class, tree -> new TreeColumn(tree, style));
	}

	/**
	 * Creates a new TreeColumnFactory with the given style. Refer to
	 * {@link TreeColumn#TreeColumn(Tree, int)} for possible styles.
	 *
	 * @param style
	 * @return a new TreeColumnFactory instance
	 */
	public static TreeColumnFactory newTreeColumn(int style) {
		return new TreeColumnFactory(style);

	}

	/**
	 * Creates a {@link SelectionListener} and registers it for the widgetSelected
	 * event. If event is raised it calls the given consumer. The
	 * {@link SelectionEvent} is passed to the consumer.
	 *
	 * @param consumer
	 * @return this
	 */
	public TreeColumnFactory onSelect(Consumer<SelectionEvent> consumer) {
		addProperty(c -> c.addSelectionListener(SelectionListener.widgetSelectedAdapter(consumer)));
		return this;
	}

	/**
	 * Sets the alignment.
	 *
	 * @param alignment
	 * @return this
	 */
	public TreeColumnFactory align(int alignment) {
		addProperty(c -> c.setAlignment(alignment));
		return this;
	}

	/**
	 * Sets the tooltip.
	 *
	 * @param tooltip
	 * @return this
	 */
	public TreeColumnFactory tooltip(String tooltip) {
		addProperty(c -> c.setToolTipText(tooltip));
		return this;
	}

	/**
	 * Sets the width.
	 *
	 * @param width
	 * @return this
	 */
	public TreeColumnFactory width(int width) {
		addProperty(c -> c.setWidth(width));
		return this;
	}

}
