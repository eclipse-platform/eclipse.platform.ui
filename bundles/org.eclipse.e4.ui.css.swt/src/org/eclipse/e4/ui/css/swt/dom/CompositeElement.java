/*******************************************************************************
 * Copyright (c) 2009, 2014 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom;

import java.util.Arrays;
import java.util.stream.Stream;
import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.dom.IStreamingNodeList;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.Node;

/**
 * {@link CSSStylableElement} implementation which wrap SWT {@link Composite}.
 */
public class CompositeElement extends ControlElement implements IStreamingNodeList {
	private static final String BACKGROUND_OVERRIDDEN_BY_CSS_MARKER = "bgOverriddenByCSS";

	public CompositeElement(Composite composite, CSSEngine engine) {
		super(composite, engine);
	}

	@Override
	public int getLength() {
		return getComposite().getChildren().length;
	}

	@Override
	public Node item(int index) {
		Widget w = getComposite().getChildren()[index];
		return getElement(w);
	}

	protected Composite getComposite() {
		return (Composite) getNativeWidget();
	}

	@Override
	public void reset() {
		Composite composite = getComposite();

		if (composite.getData(BACKGROUND_OVERRIDDEN_BY_CSS_MARKER) != null) {
			composite.setData(BACKGROUND_OVERRIDDEN_BY_CSS_MARKER, null);
		}
		super.reset();
	}

	public static boolean hasBackgroundOverriddenByCSS(Control control) {
		return control.getData(BACKGROUND_OVERRIDDEN_BY_CSS_MARKER) != null;
	}

	public static void setBackgroundOverriddenByCSSMarker(Widget widget) {
		if (widget instanceof Composite && !(widget instanceof CTabFolder)) {
			widget.setData(BACKGROUND_OVERRIDDEN_BY_CSS_MARKER, true);
		}
	}

	@Override
	public Stream<Node> stream() {
		return Arrays.stream(getComposite().getChildren()).map(this::getElement);
	}

}
