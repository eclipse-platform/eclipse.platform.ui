/*******************************************************************************
 * Copyright (c) 2016 Fabio Zadrozny and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabio Zadrozny <fabiofz at gmail dot com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;

public class LinkElement extends ControlElement {

	public LinkElement(Control control, CSSEngine engine) {
		super(control, engine);
	}

	public Link getLink() {
		return (Link) getControl();
	}

}
