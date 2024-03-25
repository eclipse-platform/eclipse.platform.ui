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
import org.eclipse.swt.widgets.Link;

/**
 * This class provides a convenient shorthand for creating and initializing
 * {@link Link}. This offers several benefits over creating Link objects via the
 * new operator:
 *
 * <ul>
 * <li>The same factory can be used many times to create several Link
 * instances</li>
 * <li>The setters all return "this", allowing them to be chained</li>
 * <li>This class accepts a Lambda for {@link SelectionEvent} (see
 * {@link #onSelect})</li>
 * </ul>
 *
 * Example usage:
 *
 * <pre>
 * Link link = LinkFactory.newLink(SWT.NONE) //
 * 		.text("Click me!") //
 * 		.onSelect(event -&gt; clicked(event)) //
 * 		.layoutData(gridData) //
 * 		.create(parent);
 * </pre>
 * <p>
 * The above example creates a link with a text, registers a SelectionListener
 * and finally creates the link in "parent".
 * </p>
 *
 * <pre>
 * GridDataFactory gridDataFactory = GridDataFactory.swtDefaults();
 * LinkFactory linkFactory = LinkFactory.newLink(SWT.PUSH).onSelect(event -&gt; clicked(event))
 * 		.layout(gridDataFactory::create);
 * linkFactory.text("Link 1").create(parent);
 * linkFactory.text("Link 2").create(parent);
 * linkFactory.text("Link 3").create(parent);
 * </pre>
 * <p>
 * The above example creates three objects using the same instance of the
 * factory. Note the layout method. A Supplier is used to create unique GridData
 * for every single link.
 * </p>
 *
 * @since 3.21
 */
public final class LinkFactory extends AbstractControlFactory<LinkFactory, Link> {

	private LinkFactory(int style) {
		super(LinkFactory.class, (Composite parent) -> new Link(parent, style));
	}

	/**
	 * Creates a new factory with the given style. Refer to
	 * {@link Link#Link(Composite, int)} for possible styles.
	 *
	 * @return a new LinkFactory instance
	 */
	public static LinkFactory newLink(int style) {
		return new LinkFactory(style);
	}

	/**
	 * Sets the receiver's text.
	 * <p>
	 * This method sets the label. The label may include the mnemonic character but
	 * must not contain line delimiters.
	 * </p>
	 * <p>
	 * Mnemonics are indicated by an '&amp;' that causes the next character to be
	 * the mnemonic. When the user presses a key sequence that matches the mnemonic,
	 * a selection event occurs. On most platforms, the mnemonic appears underlined
	 * but may be emphasized in a platform specific manner. The mnemonic indicator
	 * character '&amp;' can be escaped by doubling it in the string, causing a
	 * single '&amp;' to be displayed.
	 * </p>
	 *
	 * @param text the text
	 * @return this
	 *
	 * @see Link#setText(String)
	 */
	public LinkFactory text(String text) {
		addProperty(l -> l.setText(text));
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
	 * @see Link#addSelectionListener(SelectionListener)
	 * @see SelectionListener#widgetSelectedAdapter(Consumer)
	 */
	public LinkFactory onSelect(Consumer<SelectionEvent> consumer) {
		addProperty(c -> c.addSelectionListener(SelectionListener.widgetSelectedAdapter(consumer)));
		return this;
	}

}