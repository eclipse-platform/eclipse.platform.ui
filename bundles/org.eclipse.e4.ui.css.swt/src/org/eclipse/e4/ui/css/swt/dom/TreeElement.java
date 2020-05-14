/*******************************************************************************
 * Copyright (c) 2015 Fabio Zadrozny and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Fabio Zadrozny <fabiofz@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom;

import java.util.function.Supplier;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Tree;

public class TreeElement extends ControlElement implements IHeaderCustomizationElement {

	public TreeElement(Tree tree, CSSEngine engine) {
		super(tree, engine);
	}

	public Tree getTree() {
		return (Tree) getNativeWidget();
	}

	@Override
	public void reset() {
		setHeaderColor(null);
		setHeaderBackgroundColor(null);
		super.reset();
	}

	@Override
	public void setHeaderColor(Color color) {
		getTree().setHeaderForeground(color);
	}

	@Override
	public void setHeaderBackgroundColor(Color color) {
		getTree().setHeaderBackground(color);
	}

	@Override
	protected Supplier<String> internalGetAttribute(String attr) {
		if ("swt-lines-visible".equals(attr)) {
			Tree tree = getTree();
			return () -> String.valueOf(tree.getLinesVisible());
		}
		return super.internalGetAttribute(attr);
	}
}