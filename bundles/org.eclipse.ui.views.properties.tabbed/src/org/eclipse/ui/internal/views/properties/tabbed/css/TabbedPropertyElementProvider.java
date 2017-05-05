/*******************************************************************************
 * Copyright (c) 2017 SAP SE and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.ui.internal.views.properties.tabbed.css;

import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyList;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyTitle;
import org.w3c.dom.Element;

public class TabbedPropertyElementProvider implements IElementProvider {

	@Override
	public Element getElement(Object element, CSSEngine engine) {
		if (element instanceof TabbedPropertyTitle) {
			return new TabbedPropertyTitleAdapter((TabbedPropertyTitle) element, engine);
		} else if (element instanceof TabbedPropertyList) {
			return new TabbedPropertyListAdapter((TabbedPropertyList) element, engine);
		}
		return null;
	}

}
