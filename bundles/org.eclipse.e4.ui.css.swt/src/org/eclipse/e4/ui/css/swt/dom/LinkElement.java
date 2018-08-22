/*******************************************************************************
 * Copyright (c) 2016 Fabio Zadrozny and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
