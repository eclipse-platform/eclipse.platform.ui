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
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class has been added as part of a work in
 * progress. There is no guarantee that this API will work or that it will
 * remain the same. Feel free to use it and give feedback via
 * https://bugs.eclipse.org/bugs/buglist.cgi?component=UI&product=Platform, but
 * be aware that it might change.
 * </p>
 *
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
 */
public class LabelFactory extends AbstractControlFactory<LabelFactory, Label> {

	private LabelFactory(int style) {
		super(LabelFactory.class, (Composite parent) -> new Label(parent, style));
	}

	/**
	 * Creates a new LabelFactory with the given style. Refer to
	 * {@link Label#Label(Composite, int)} for possible styles.
	 *
	 * @param style
	 * @return a new LabelFactory instance
	 */
	public static LabelFactory newLabel(int style) {
		return new LabelFactory(style);
	}

	/**
	 * Sets the text.
	 *
	 * @param text
	 * @return this
	 */
	public LabelFactory text(String text) {
		addProperty(l -> l.setText(text));
		return this;
	}

	/**
	 * Sets the image.
	 *
	 * @param image
	 * @return this
	 */
	public LabelFactory image(Image image) {
		addProperty(l -> l.setImage(image));
		return this;
	}

	/**
	 * Sets the alignment.
	 *
	 * @param alignment
	 * @return this
	 */
	public LabelFactory align(int alignment) {
		addProperty(l -> l.setAlignment(alignment));
		return this;
	}
}