/*******************************************************************************
 * Copyright (c) 2017 SAP SE and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.ui.internal.views.properties.tabbed.css;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.CompositeElement;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyTitle;

public class TabbedPropertyTitleAdapter extends CompositeElement {

	public TabbedPropertyTitleAdapter(TabbedPropertyTitle composite, CSSEngine engine) {
		super(composite, engine);
	}

	@Override
	public void reset() {
		getTabbedPropertyTitle().resetSectionToolBarColors();
		super.reset();
	}

	private TabbedPropertyTitle getTabbedPropertyTitle() {
		return (TabbedPropertyTitle) getNativeWidget();
	}
}
