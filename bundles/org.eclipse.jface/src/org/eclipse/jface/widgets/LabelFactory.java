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

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * This class provides a convenient shorthand for creating and initializing
 * {@link Label}. This offers several benefits over creating Label normal way:
 *
 * <ul>
 * <li>The same factory can be used many times to create several Label
 * instances</li>
 * <li>The setters on LabelFactory all return "this", allowing them to be
 * chained</li>
 * </ul>
 *
 * Example usage:
 *
 * <pre>
 * Label label = LabelFactory.newLabel(SWT.LEFT)//
 * 		.text("Label:") //
 * 		.layoutData(gridData) //
 * 		.create(parent);
 * </pre>
 * <p>
 * The above example creates a Label with a text and aligns it left. Finally the
 * label is created in "parent".
 * </p>
 *
 * <pre>
 * LabelFactory labelFactory = LabelFactory.newLabel(SWT.LEFT);
 * labelFactory.text("Label 1:").create(parent);
 * labelFactory.text("Label 2:").create(parent);
 * labelFactory.text("Label 3:").create(parent);
 * </pre>
 * <p>
 * The above example creates three labels using the same instance of
 * LabelFactory.
 * </p>
 *
 * @since 3.18
 */
public final class LabelFactory extends AbstractControlFactory<LabelFactory, Label> {

	private LabelFactory(int style) {
		super(LabelFactory.class, (Composite parent) -> new Label(parent, style));
	}

	/**
	 * Creates a new LabelFactory with the given style. Refer to
	 * {@link Label#Label(Composite, int)} for possible styles.
	 *
	 * @return a new LabelFactory instance
	 */
	public static LabelFactory newLabel(int style) {
		return new LabelFactory(style);
	}

	/**
	 * Sets the receiver's text.
	 * <p>
	 * This method sets the widget label. The label may include the mnemonic
	 * character and line delimiters.
	 * </p>
	 * <p>
	 * Mnemonics are indicated by an '&amp;' that causes the next character to be
	 * the mnemonic. When the user presses a key sequence that matches the mnemonic,
	 * focus is assigned to the control that follows the label. On most platforms,
	 * the mnemonic appears underlined but may be emphasised in a platform specific
	 * manner. The mnemonic indicator character '&amp;' can be escaped by doubling
	 * it in the string, causing a single '&amp;' to be displayed.
	 * </p>
	 *
	 * @param text the text
	 * @return this
	 *
	 * @see Label#setText(String)
	 */
	public LabelFactory text(String text) {
		addProperty(l -> l.setText(text));
		return this;
	}

	/**
	 * Sets the receiver's image to the argument, which may be null indicating that
	 * no image should be displayed.
	 *
	 * @param image the image to display on the receiver (may be null)
	 * @return this
	 *
	 * @see Label#setImage(Image)
	 */
	public LabelFactory image(Image image) {
		addProperty(l -> l.setImage(image));
		return this;
	}

	/**
	 * Controls how text and images will be displayed in the receiver. The argument
	 * should be one of <code>LEFT</code>, <code>RIGHT</code> or
	 * <code>CENTER</code>. If the receiver is a <code>SEPARATOR</code> label, the
	 * argument is ignored and the alignment is not changed.
	 *
	 * @param alignment the alignment
	 * @return this
	 *
	 * @see Label#setAlignment(int)
	 */
	public LabelFactory align(int alignment) {
		addProperty(l -> l.setAlignment(alignment));
		return this;
	}
}