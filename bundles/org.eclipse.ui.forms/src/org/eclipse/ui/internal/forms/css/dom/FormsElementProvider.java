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
 *      Lars Vogel <Lars.Vogel@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.css.dom;

import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.Section;
import org.w3c.dom.Element;

/**
 * Returns the CSS class which is responsible for styling a forms widget
 *
 * Registered via the "org.eclipse.e4.ui.css.core.elementProvider" extension
 * point for the widgets
 *
 * {@link IElementProvider} SWT implementation to retrieve w3c Element
 */
public class FormsElementProvider implements IElementProvider {

	public static final IElementProvider INSTANCE = new FormsElementProvider();

	@Override
	public Element getElement(Object element, CSSEngine engine) {
		if (element instanceof Section) {
			return new SectionElement((Section) element, engine);
		}
		if (element instanceof ExpandableComposite) {
			return new ExpandableCompositeElement((ExpandableComposite) element, engine);
		}
		if (element instanceof Form) {
			return new FormElement((Form) element, engine);
		}

		return null;
	}
}
