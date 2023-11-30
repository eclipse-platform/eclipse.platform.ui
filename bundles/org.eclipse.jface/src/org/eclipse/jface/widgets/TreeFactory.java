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
 * 		.onSelect(e -&gt; treeClicked(e)) //
 * 		.onExpand(e -&gt; treeExpanded(e)) //
 * 		.create(parent);
 * </pre>
 *
 * <p>
 * The above example creates a tree, sets some properties, registers a
 * SelectionListener and a TreeListener for expansion and finally creates the
 * tree in "parent".
 * </p>
 *
 * <p>
 * Note that this class does not extend {@link AbstractCompositeFactory} even
 * though Tree extends Composite. This is because Tree is not supposed to be
 * used like a Composite.
 * </p>
 *
 * @since 3.18
 */
public final class TreeFactory extends AbstractControlFactory<TreeFactory, Tree> {

	private TreeFactory(int style) {
		super(TreeFactory.class, parent -> new Tree(parent, style));
	}

	/**
	 * Creates a new TreeFactory with the given style. Refer to
	 * {@link Tree#Tree(Composite, int)} for possible styles.
	 *
	 * @return a new TreeFactory instance
	 */
	public static TreeFactory newTree(int style) {
		return new TreeFactory(style);
	}

	/**
	 * Marks the receiver's lines as visible if the argument is true, and marks it
	 * invisible otherwise. Note that some platforms draw grid lines while others
	 * may draw alternating row colors.
	 *
	 * @param visible the visibility state
	 * @return this
	 *
	 * @see Tree#setLinesVisible(boolean)
	 */
	public TreeFactory linesVisible(boolean visible) {
		this.addProperty(w -> w.setLinesVisible(visible));
		return this;
	}

	/**
	 * Marks the receiver's header as visible if the argument is true, and marks it
	 * invisible otherwise.
	 *
	 * @param visible the visibility state
	 * @return this
	 *
	 * @see Tree#setHeaderVisible(boolean)
	 */
	public TreeFactory headerVisible(boolean visible) {
		this.addProperty(w -> w.setHeaderVisible(visible));
		return this;
	}

	/**
	 * Sets the number of root-level items contained in the receiver.
	 *
	 * @param count the number of items
	 * @return this
	 *
	 * @see Tree#setItemCount(int)
	 */
	public TreeFactory itemCount(int count) {
		this.addProperty(w -> w.setItemCount(count));
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
	 * @see Tree#addSelectionListener(SelectionListener)
	 * @see SelectionListener#widgetSelectedAdapter(Consumer)
	 */
	public TreeFactory onSelect(Consumer<SelectionEvent> consumer) {
		addProperty(c -> c.addSelectionListener(SelectionListener.widgetSelectedAdapter(consumer)));
		return this;
	}

	/**
	 * Creates a {@link TreeListener} and registers it for the collapsed event. If
	 * the receiver is collapsed by the user the given consumer is invoked. The
	 * {@link TreeEvent} is passed to the consumer.
	 *
	 * @param consumer the consumer whose accept method is called
	 * @return this
	 *
	 * @see Tree#addTreeListener(TreeListener)
	 * @see SelectionListener#widgetSelectedAdapter(Consumer)
	 */
	public TreeFactory onCollapse(Consumer<TreeEvent> consumer) {
		addProperty(c -> c.addTreeListener(TreeListener.treeCollapsedAdapter(consumer)));
		return this;
	}

	/**
	 * Creates a {@link TreeListener} and registers it for the expanded event. If
	 * the receiver is expanded by the user the given consumer is invoked. The
	 * {@link TreeEvent} is passed to the consumer.
	 *
	 * @param consumer the consumer whose accept method is called
	 * @return this
	 *
	 * @see Tree#addTreeListener(TreeListener)
	 * @see SelectionListener#widgetSelectedAdapter(Consumer)
	 */
	public TreeFactory onExpand(Consumer<TreeEvent> consumer) {
		addProperty(c -> c.addTreeListener(TreeListener.treeExpandedAdapter(consumer)));
		return this;
	}
}