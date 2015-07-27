/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom;

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.ToolBar;
import org.w3c.dom.Node;

/**
 * {@link CSSStylableElement} implementation which wrap SWT {@link CTabFolder}.
 *
 */
public class ToolBarElement extends CompositeElement {

	public ToolBarElement(ToolBar toolbar, CSSEngine engine) {
		super(toolbar, engine);
	}

	public ToolBar getToolBar() {
		return (ToolBar)getNativeWidget();
	}

	@Override
	public Node item(int index) {
		return getElement(getToolBar().getItem(index));
	}

	@Override
	public int getLength() {
		return getToolBar().getItemCount();
	}

}
