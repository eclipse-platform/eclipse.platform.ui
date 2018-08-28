/*******************************************************************************
 * Copyright (c) 2017 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Fabian Pfaff <fabian.pfaff@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.css.dom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.CompositeElement;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

public class ExpandableCompositeElement extends CompositeElement {

	public ExpandableCompositeElement(ExpandableComposite composite, CSSEngine engine) {
		super(composite, engine);
	}

	@Override
	public void reset() {
		getExpandableComposite().setTitleBarForeground(null);
		super.reset();
	}

	private ExpandableComposite getExpandableComposite() {
		return (ExpandableComposite) getNativeWidget();
	}

}
