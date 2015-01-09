/*******************************************************************************
 * Copyright (c) 2014, 2015 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.css.dom;

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.CompositeElement;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.forms.widgets.Section;

/**
 * {@link CSSStylableElement} implementation which wrap SWT {@link Section}.
 *
 */
public class SectionElement extends CompositeElement {


	private Color titleBarBackground;
	private Color titleBarBorderColor;
	private Color titleBarGradientBackground;

	public SectionElement(Section section, CSSEngine engine) {
		super(section, engine);
		titleBarBackground = section.getTitleBarBackground();
		titleBarBorderColor = section.getTitleBarBorderColor();
		titleBarGradientBackground = section.getTitleBarGradientBackground();
	}

	public void reset() {
		super.reset();
		Section section = (Section) getWidget();
		section.setTitleBarBackground(titleBarBackground);
		section.setTitleBarBorderColor(titleBarBorderColor);
		section.setTitleBarGradientBackground(titleBarGradientBackground);
	}

}
