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
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class has been added as part of a work in
 * progress. There is no guarantee that this API will work or that it will
 * remain the same. Feel free to use it and give feedback via
 * https://bugs.eclipse.org/bugs/buglist.cgi?component=UI&product=Platform, but
 * be aware that it might change.
 * </p>
 *
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
 */
public class TextFactory extends AbstractControlFactory<TextFactory, Text> {

	private TextFactory(int style) {
		super(TextFactory.class, (Composite parent) -> new Text(parent, style));
	}

	/**
	 * Creates a new TextFactory with the given style and text. Refer to
	 * {@link Text#Text(Composite, int)} for possible styles.
	 *
	 * @param style
	 * @return a new TextFactory instance
	 */
	public static TextFactory newText(int style) {
		return new TextFactory(style);
	}

	/**
	 * Sets the text.
	 *
	 * @param text
	 * @return this
	 */
	public TextFactory text(String text) {
		addProperty(t -> t.setText(text));
		return this;
	}

	/**
	 * Sets the hint message.
	 *
	 * @param message
	 * @return this
	 */
	public TextFactory message(String message) {
		addProperty(t -> t.setMessage(message));
		return this;
	}

	/**
	 * Sets the text limit.
	 *
	 * @param limit
	 * @return this
	 */
	public TextFactory limitTo(int limit) {
		addProperty(t -> t.setTextLimit(limit));
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
	public TextFactory onSelect(Consumer<SelectionEvent> consumer) {
		addProperty(t -> t.addSelectionListener(SelectionListener.widgetSelectedAdapter(consumer)));
		return this;
	}

	/**
	 * Adds a ModifyListener. Can be called several times to add more than one
	 * ModifyListener.
	 *
	 * @param listener
	 * @return this
	 */
	public TextFactory onModify(ModifyListener listener) {
		addProperty(t -> t.addModifyListener(listener));
		return this;
	}

	/**
	 * Adds a VerifyListener. Can be called several times to add more than one
	 * VerifyListener.
	 *
	 * @param listener
	 * @return this
	 */
	public TextFactory onVerify(VerifyListener listener) {
		addProperty(l -> l.addVerifyListener(listener));
		return this;
	}
}