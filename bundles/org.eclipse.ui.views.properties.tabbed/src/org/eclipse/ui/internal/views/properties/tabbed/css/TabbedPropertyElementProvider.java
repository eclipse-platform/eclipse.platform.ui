/*******************************************************************************
 * Copyright (c) 2017 SAP SE and others.
 *
 * This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License 2.0 which accompanies this distribution, and is
t https://www.eclipse.org/legal/epl-2.0/
t
t SPDX-License-Identifier: EPL-2.0
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
