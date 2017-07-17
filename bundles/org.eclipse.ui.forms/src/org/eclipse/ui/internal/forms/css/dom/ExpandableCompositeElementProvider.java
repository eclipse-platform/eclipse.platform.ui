/*******************************************************************************
 * Copyright (c) 2017 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Fabian Pfaff <fabian.pfaff@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.css.dom;
import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.w3c.dom.Element;

public class ExpandableCompositeElementProvider implements IElementProvider {

	@Override
	public Element getElement(Object element, CSSEngine engine) {
		if (element instanceof ExpandableComposite) {
			return new ExpandableCompositeElement((ExpandableComposite) element, engine);
		}
		return null;
	}

}
