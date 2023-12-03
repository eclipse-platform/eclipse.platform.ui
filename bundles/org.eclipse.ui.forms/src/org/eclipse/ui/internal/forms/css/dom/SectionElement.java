/*******************************************************************************
 * Copyright (c) 2014, 2015 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.css.dom;

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.CompositeElement;
import org.eclipse.ui.forms.widgets.Section;

/**
 * {@link CSSStylableElement} implementation which wrap SWT {@link Section}.
 */
public class SectionElement extends CompositeElement {


	public SectionElement(Section section, CSSEngine engine) {
		super(section, engine);
	}

	@Override
	public void reset() {
		Section section = (Section) getWidget();
		section.setTitleBarForeground(null);
		section.setToggleColor(null);
		section.setActiveToggleColor(null);
		super.reset();
	}

}
