/*******************************************************************************
 * Copyright (c) 2015, 2017 Fabio Zadrozny and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabio Zadrozny <fabiofz@gmail.com> - initial API and implementation
 *     Ralf Petter <ralf.petter@gmail.com> - Bug 484506
 *******************************************************************************/
package org.eclipse.ui.internal.forms.css.dom;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.CompositeElement;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.Form;

public class FormElement extends CompositeElement {
	private Map<String, Color> headerColors = new HashMap<>();
	private static final String[] headerColorKeys = { IFormColors.H_BOTTOM_KEYLINE1, IFormColors.H_BOTTOM_KEYLINE2,
			IFormColors.H_GRADIENT_END, IFormColors.H_GRADIENT_START, IFormColors.H_HOVER_FULL,
			IFormColors.H_HOVER_LIGHT, IFormColors.H_PREFIX };
	public FormElement(Form formHeading, CSSEngine engine) {
		super(formHeading, engine);
		for (String headerColorKey : headerColorKeys) {
			headerColors.put(headerColorKey, formHeading.getHeadColor(headerColorKey));
		}
	}

	@Override
	public void reset() {
		super.reset();
		Form formHeading = (Form) getWidget();
		for (String headerColorKey : headerColorKeys) {
			formHeading.setHeadColor(headerColorKey, headerColors.get(headerColorKey));
		}
	}

	@Override
	public void dispose() {
		headerColors.clear();
		super.dispose();
	}
}
