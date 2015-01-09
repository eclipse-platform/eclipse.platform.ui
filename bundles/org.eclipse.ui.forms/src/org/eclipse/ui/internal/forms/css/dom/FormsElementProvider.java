/*******************************************************************************
 * Copyright (c) 2014, 2015 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Lars Vogel <Lars.Vogel@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.css.dom;

import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.ui.forms.widgets.Section;
import org.w3c.dom.Element;

/**
 * Returns the CSS class which is responsible for styling a forms widget
 *
 * Registered via the "org.eclipse.e4.ui.css.core.elementProvider" extension
 * point for the widgets
 *
 * {@link IElementProvider} SWT implementation to retrieve w3c Element
 *
 */
public class FormsElementProvider implements IElementProvider {

	public static final IElementProvider INSTANCE = new FormsElementProvider();

	public Element getElement(Object element, CSSEngine engine) {
		if (element instanceof Section) {
			return new SectionElement((Section) element, engine);
		}

		return null;
	}
}
