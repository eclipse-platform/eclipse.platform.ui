/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTImageHelper;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Item;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class CTabItemElement extends ItemElement {

	public CTabItemElement(Item item, CSSEngine engine) {
		super(item, engine);
	}

	@Override
	protected void computeStaticPseudoInstances() {
		super.computeStaticPseudoInstances();
		// it's CTabFolder. Set selected as static pseudo instance.
		// because this widget define methods
		// CTabFolder#setSelectionBackground (Color color)
		// which set background Color when a CTabItem is selected.
		super.addStaticPseudoInstance("selected");
	}

	@Override
	public Node getParentNode() {
		CTabItem item = getItem();
		CTabFolder parent = item.getParent();
		if (parent != null) {
			Element element = getElement(parent);
			return element;
		}
		return null;
	}

	private CTabItem getItem() {
		return (CTabItem) getNativeWidget();
	}

	@Override
	public void reset() {
		CTabItem item = getItem();
		CSSSWTImageHelper.restoreDefaultImage(item);
		item.setFont(null); // in such case the parent's font will be taken
		super.reset();
	}

}
