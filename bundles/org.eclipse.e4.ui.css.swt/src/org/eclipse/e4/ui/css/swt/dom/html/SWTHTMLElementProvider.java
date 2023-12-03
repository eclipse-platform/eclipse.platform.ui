/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom.html;

import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.SWTElementProvider;
import org.eclipse.e4.ui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.Element;

/**
 * {@link IElementProvider} SWT implementation to retrieve w3c Element
 * {@link SWTHTMLElement} linked to SWT widget.
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 */
public class SWTHTMLElementProvider extends SWTElementProvider {

	public static final IElementProvider INSTANCE = new SWTHTMLElementProvider();

	@Override
	public Element getElement(Object element, CSSEngine engine) {
		if (element instanceof Widget) {
			Widget widget = (Widget) element;
			return SWTElementHelpers.getHTMLElement(widget, engine);
		}
		return null;
	}
}
