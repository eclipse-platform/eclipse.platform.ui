/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.engine.html;

/**
 * CSS SWT Engine implementation which configure CSSEngineImpl to apply styles
 * to SWT widgets with static handler strategy and manage HTML selector.
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 *
 */
import org.eclipse.e4.ui.css.swt.dom.html.SWTHTMLElementProvider;
import org.eclipse.e4.ui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.swt.widgets.Display;

public class CSSSWTHTMLEngineImpl extends CSSSWTEngineImpl {

	public CSSSWTHTMLEngineImpl(Display display) {
		this(display, false);
	}

	public CSSSWTHTMLEngineImpl(Display display, boolean lazyApplyingStyles) {
		super(display, lazyApplyingStyles);
		// Register SWT HTML Element Provider to retrieve
		// w3c Element SWTHTMLElement coming from SWT widget.
		super.setElementProvider(SWTHTMLElementProvider.INSTANCE);
	}
}
