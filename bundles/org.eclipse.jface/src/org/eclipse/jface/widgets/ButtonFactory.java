/*******************************************************************************
* Copyright (c) 2018 SAP SE and others.
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * This class provides a convenient shorthand for creating and initializing
 * {@link Button}. This offers several benefits over creating with widget the
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
 * Button button = ButtonFactory.newButton(SWT.PUSH) //
 * 		.text("Click me!") //
 * 		.onSelect(event -&gt; buttonClicked(event)) //
 * 		.layoutData(gridData) //
 * 		.create(parent);
 * </pre>
 * <p>
 * The above example creates a push button with a text, registers a
 * SelectionListener and finally creates the button in "parent".
 * </p>
 *
 * <pre>
 * GridDataFactory gridDataFactory = GridDataFactory.swtDefaults();
 * ButtonFactory buttonFactory = ButtonFactory.newButton(SWT.PUSH).onSelect(event -&gt; buttonClicked(event))
 * 		.layout(gridDataFactory::create);
 * buttonFactory.text("Button 1").create(parent);
 * buttonFactory.text("Button 2").create(parent);
 * buttonFactory.text("Button 3").create(parent);
 * </pre>
 * <p>
 * The above example creates three buttons using the same instance of
 * ButtonFactory. Note the layout method. A Supplier is used to create unique
 * GridData for every single widget.
 * </p>
 *
 * @since 3.18
 *
 */
public final class ButtonFactory extends AbstractControlFactory<ButtonFactory, Button> {

	private ButtonFactory(int style) {
		super(ButtonFactory.class, (Composite parent) -> new Button(parent, style));
	}

	/**
	 * Creates a new ButtonFactory with the given style. Refer to
	 * {@link Button#Button(Composite, int)} for possible styles.
	 *
	 * @param style
	 * @return a new ButtonFactory instance
	 */
	public static ButtonFactory newButton(int style) {
		return new ButtonFactory(style);
	}

	/**
	 * Sets the receiver's text.
	 * <p>
	 * This method sets the button label. The label may include the mnemonic
	 * character but must not contain line delimiters.
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
	 * @see Button#setText(String)
	 */
	public ButtonFactory text(String text) {
		addProperty(b -> b.setText(text));
		return this;
	}

	/**
	 * Sets the receiver's image to the argument, which may be <code>null</code>
	 * indicating that no image should be displayed.
	 *
	 * @param image the image to display on the receiver (may be <code>null</code>)
	 * @return this
	 *
	 * @see Button#setImage(Image)
	 */
	public ButtonFactory image(Image image) {
		addProperty(b -> b.setImage(image));
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
	 * @see Button#addSelectionListener(SelectionListener)
	 * @see SelectionListener#widgetSelectedAdapter(Consumer)
	 */
	public ButtonFactory onSelect(Consumer<SelectionEvent> consumer) {
		addProperty(c -> c.addSelectionListener(SelectionListener.widgetSelectedAdapter(consumer)));
		return this;
	}
}