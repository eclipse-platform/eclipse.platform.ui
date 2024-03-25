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

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * This class provides a convenient shorthand for creating and initializing
 * {@link Text}. This offers several benefits over creating Text normal way:
 *
 * <ul>
 * <li>The same factory can be used many times to create several Text
 * instances</li>
 * <li>The setters on TextFactory all return "this", allowing them to be
 * chained</li>
 * <li>TextFactory accepts a Lambda for {@link SelectionEvent} (see
 * {@link #onSelect})</li>
 * </ul>
 *
 * Example usage:
 *
 * <pre>
 * Text text = new TextFactory(SWT.WRAP)//
 * 		.limitTo(16) //
 * 		.message("Enter credit card number") //
 * 		.layoutData(gridData) //
 * 		.create(parent);
 * </pre>
 * <p>
 * The above example creates a Text with wrapped style, limits it to 16
 * characters, sets a message and finally creates it in "parent".
 * </p>
 *
 * <pre>
 * TextFactory textFactory = new TextFactory(SWT.NONE);
 * textFactory.message("Enter text 1").create(parent);
 * textFactory.message("Enter text 2").create(parent);
 * textFactory.message("Enter text 3").create(parent);
 * </pre>
 * <p>
 * The above example creates three texts using the same instance of TextFactory.
 * </p>
 *
 * @since 3.18
 */
public final class TextFactory extends AbstractControlFactory<TextFactory, Text> {

	private TextFactory(int style) {
		super(TextFactory.class, (Composite parent) -> new Text(parent, style));
	}

	/**
	 * Creates a new TextFactory with the given style and text. Refer to
	 * {@link Text#Text(Composite, int)} for possible styles.
	 *
	 * @return a new TextFactory instance
	 *
	 * @see Text#Text(Composite, int)
	 */
	public static TextFactory newText(int style) {
		return new TextFactory(style);
	}

	/**
	 * Sets the contents of the receiver to the given string.
	 *
	 * @param text the text
	 * @return this
	 *
	 * @see Text#setText(String)
	 */
	public TextFactory text(String text) {
		addProperty(t -> t.setText(text));
		return this;
	}

	/**
	 * Sets the widget message. The message text is displayed as a hint for the
	 * user, indicating the purpose of the field.
	 *
	 * @param message the message
	 * @return this
	 *
	 * @see Text#setMessage(String)
	 */
	public TextFactory message(String message) {
		addProperty(t -> t.setMessage(message));
		return this;
	}

	/**
	 * Sets the maximum number of characters that the receiver is capable of holding
	 * to be the argument.
	 * <p>
	 * Instead of trying to set the text limit to zero, consider creating a
	 * read-only text widget.
	 * </p>
	 *
	 * @param limit the text limit
	 * @return this
	 *
	 * @see Text#setTextLimit(int)
	 */
	public TextFactory limitTo(int limit) {
		addProperty(t -> t.setTextLimit(limit));
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
	 * @see Text#addSelectionListener(SelectionListener)
	 * @see SelectionListener#widgetSelectedAdapter(Consumer)
	 */
	public TextFactory onSelect(Consumer<SelectionEvent> consumer) {
		addProperty(t -> t.addSelectionListener(SelectionListener.widgetSelectedAdapter(consumer)));
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
	 * @see Text#addModifyListener(ModifyListener)
	 * @see ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	public TextFactory onModify(ModifyListener listener) {
		addProperty(t -> t.addModifyListener(listener));
		return this;
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified when
	 * the receiver's text is verified, by calling the verifyText method.
	 * <p>
	 * Can be called several times to add more than one VerifyListener.
	 * </p>
	 *
	 * @param listener the listener which should be notified
	 * @return this
	 *
	 * @see Text#addVerifyListener(VerifyListener)
	 * @see VerifyListener#verifyText(org.eclipse.swt.events.VerifyEvent)
	 */
	public TextFactory onVerify(VerifyListener listener) {
		addProperty(l -> l.addVerifyListener(listener));
		return this;
	}
}