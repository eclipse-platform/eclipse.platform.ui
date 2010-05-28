/*******************************************************************************
 * Copyright (c) 2008, 2009 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.engine;

import org.eclipse.swt.widgets.Display;

/**
 * CSS SWT Engine implementation which configure CSSEngineImpl to apply styles
 * to SWT widgets with lazy handler strategy.
 */
public class CSSSWTLazyHandlerEngineImpl extends AbstractCSSSWTEngineImpl {

	public CSSSWTLazyHandlerEngineImpl(Display display) {
		super(display);
	}

	public CSSSWTLazyHandlerEngineImpl(Display display,
			boolean lazyApplyingStyles) {
		super(display, lazyApplyingStyles);
	}

	protected void initializeCSSPropertyHandlers() {
		super
				.registerPackage("org.eclipse.e4.ui.css.swt.properties.css2.lazy.classification");
		super
				.registerPackage("org.eclipse.e4.ui.css.swt.properties.css2.lazy.border");
		super
				.registerPackage("org.eclipse.e4.ui.css.swt.properties.css2.lazy.font");
		super
				.registerPackage("org.eclipse.e4.ui.css.swt.properties.css2.lazy.background");
		super
				.registerPackage("org.eclipse.e4.ui.css.swt.properties.css2.lazy.text");

	}

}
