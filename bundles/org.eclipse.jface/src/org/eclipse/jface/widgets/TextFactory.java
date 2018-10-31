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

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
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
public class TextFactory extends ControlFactory<TextFactory, Text> {

	private String text;
	private String message;
	private int limit = SWT.DEFAULT;
	private Collection<SelectionListener> selectionListeners = new ArrayList<>();
	private Collection<ModifyListener> modifyListeners = new ArrayList<>();
	private Collection<VerifyListener> verifyListeners = new ArrayList<>();

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
		this.text = text;
		return this;
	}

	/**
	 * Sets the hint message.
	 *
	 * @param message
	 * @return this
	 */
	public TextFactory message(String message) {
		this.message = message;
		return this;
	}

	/**
	 * Sets the text limit.
	 *
	 * @param limit
	 * @return this
	 */
	public TextFactory limitTo(int limit) {
		this.limit = limit;
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
		this.selectionListeners.add(SelectionListener.widgetSelectedAdapter(consumer));
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
		this.modifyListeners.add(listener);
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
		this.verifyListeners.add(listener);
		return this;
	}

	@Override
	protected void applyProperties(Text text) {
		super.applyProperties(text);

		if (this.text != null) {
			text.setText(this.text);
		}
		if (this.limit > -1) {
			text.setTextLimit(this.limit);
		}
		if (this.message != null) {
			text.setMessage(this.message);
		}
		this.selectionListeners.forEach(l -> text.addSelectionListener(l));
		this.modifyListeners.forEach(l -> text.addModifyListener(l));
		this.verifyListeners.forEach(l -> text.addVerifyListener(l));
	}
}