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

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

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
 * {@link Tree}. This offers several benefits over creating Tree normal way:
 *
 * <ul>
 * <li>The same factory can be used many times to create several Tree
 * instances</li>
 * <li>The setters on TreeFactory all return "this", allowing them to be
 * chained</li>
 * <li>TreeFactory accepts a Lambda for {@link SelectionEvent} (see
 * {@link #onSelect})</li>
 * <li>TreeFactory accepts a Lambda for {@link TreeEvent} (see
 * {@link #onExpand})</li>
 * <li>TreeFactory accepts a Lambda for {@link TreeEvent} (see
 * {@link #onCollapse})</li>
 * </ul>
 *
 * Example usage:
 *
 * <pre>
 * Tree tree = TreeFactory.newTree(SWT.CHECK) //
 * 		.headerVisible(true) //
 * 		.linesVisible(true) //
 * 		.onSelect(e -> treeClicked(e)) //
 * 		.onExpand(e -> treeExpanded(e)) //
 * 		.create(parent);
 * </pre>
 * <p>
 * The above example creates a tree, sets some properties, registers a
 * SelectionListener and a TreeListener for expansion and finally creates the
 * tree in "parent".
 * <p>
 *
 */
public class TreeFactory extends AbstractCompositeFactory<TreeFactory, Tree> {

	private TreeFactory(int style) {
		super(TreeFactory.class, (parent) -> new Tree(parent, style));
	}

	/**
	 * Creates a new TreeFactory with the given style. Refer to
	 * {@link Tree#Tree(Composite, int)} for possible styles.
	 *
	 * @param style
	 * @return a new TreeFactory instance
	 */
	public static TreeFactory newTree(int style) {
		return new TreeFactory(style);
	}

	/**
	 * Sets the lines visibility.
	 *
	 * @param visible
	 * @return this
	 */
	public TreeFactory linesVisible(boolean visible) {
		this.addProperty(w -> w.setLinesVisible(visible));
		return this;
	}

	/**
	 * Sets the header visibility.
	 *
	 * @param visible
	 * @return this
	 */
	public TreeFactory headerVisible(boolean visible) {
		this.addProperty(w -> w.setHeaderVisible(visible));
		return this;
	}

	/**
	 * Sets the item count of the table.
	 *
	 * @param count
	 * @return this
	 */
	public TreeFactory itemCount(int count) {
		this.addProperty(w -> w.setItemCount(count));
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
	public TreeFactory onSelect(Consumer<SelectionEvent> consumer) {
		addProperty(c -> c.addSelectionListener(SelectionListener.widgetSelectedAdapter(consumer)));
		return this;
	}

	/**
	 * Creates a {@link TreeListener} and registers it for the collapsed event. If
	 * event is raised it calls the given consumer. The {@link TreeEvent} is passed
	 * to the consumer.
	 *
	 * @param consumer
	 * @return this
	 */
	public TreeFactory onCollapse(Consumer<TreeEvent> consumer) {
		addProperty(c -> c.addTreeListener(TreeListener.treeCollapsedAdapter(consumer)));
		return this;
	}

	/**
	 * Creates a {@link TreeListener} and registers it for the expanded event. If
	 * event is raised it calls the given consumer. The {@link TreeEvent} is passed
	 * to the consumer.
	 *
	 * @param consumer
	 * @return this
	 */
	public TreeFactory onExpand(Consumer<TreeEvent> consumer) {
		addProperty(c -> c.addTreeListener(TreeListener.treeExpandedAdapter(consumer)));
		return this;
	}

}