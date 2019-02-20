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
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class has been added as part of a work in
 * progress. There is no guarantee that this API will work or that it will
 * remain the same. Feel free to use it and give feedback via
 * https://bugs.eclipse.org/bugs/buglist.cgi?component=UI&product=Platform, but
 * be aware that it might change.
 * </p>
 *
 * This class provides a convenient shorthand for creating and initializing
 * {@link Button}. This offers several benefits over creating Button normal way:
 *
 * <ul>
 * <li>The same factory can be used many times to create several Button
 * instances</li>
 * <li>The setters on ButtonFactory all return "this", allowing them to be
 * chained</li>
 * <li>ButtonFactory accepts a Lambda for {@link SelectionEvent} (see
 * {@link #onSelect})</li>
 * </ul>
 *
 * Example usage:
 *
 * <pre>
 * Button button = ButtonFactory.newButton(SWT.PUSH) //
 * 		.text("Click me!") //
 * 		.onSelect(event -> buttonClicked(event)) //
 * 		.layoutData(gridData) //
 * 		.create(parent);
 * </pre>
 * <p>
 * The above example creates a push button with a text, registers a
 * SelectionListener and finally creates the button in "parent".
 * <p>
 *
 * <pre>
 * GridDataFactory gridDataFactory = GridDataFactory.swtDefaults();
 * ButtonFactory buttonFactory = ButtonFactory.newButton(SWT.PUSH).onSelect(event -> buttonClicked(event))
 * 		.layout(gridDataFactory::create);
 * buttonFactory.text("Button 1").create(parent);
 * buttonFactory.text("Button 2").create(parent);
 * buttonFactory.text("Button 3").create(parent);
 * </pre>
 * <p>
 * The above example creates three buttons using the same instance of
 * ButtonFactory. Note the layout method. A Supplier is used to create unique
 * GridData for every single button.
 * <p>
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
	 * Sets the Button text.
	 *
	 * @param text
	 * @return this
	 */
	public ButtonFactory text(String text) {
		addProperty(b -> b.setText(text));
		return this;
	}

	/**
	 * Sets the Button image.
	 *
	 * @param image
	 * @return this
	 */
	public ButtonFactory image(Image image) {
		addProperty(b -> b.setImage(image));
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
	public ButtonFactory onSelect(Consumer<SelectionEvent> consumer) {
		addProperty(c -> c.addSelectionListener(SelectionListener.widgetSelectedAdapter(consumer)));
		return this;
	}
}