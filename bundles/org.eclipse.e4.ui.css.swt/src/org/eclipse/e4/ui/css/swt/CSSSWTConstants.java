/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt;

import org.eclipse.e4.ui.css.core.dom.properties.CSSBorderProperties;

/**
 * CSS SWT constants used to store CSS Data into SWT Widget data.
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public class CSSSWTConstants {

	/**
	 * Constant used to store {@link CSSBorderProperties} instance into SWT
	 * control data.
	 */
	public static final String CONTROL_CSS2BORDER_KEY = "org.eclipse.e4.ui.core.css.swt.CONTROL_CSS2BORDER_KEY";

	/**
	 * Constant used to store {@link CSS2FontProperties} instance into SWT
	 * control data.
	 */
	// public static final String CONTROL_CSS2FONT_KEY =
	// "org.eclipse.e4.ui.core.css.swt.CONTROL_CSS2FONT_KEY";
	/**
	 * Constant used to store String Text into SWT control data.
	 */
	public static final String TEXT_KEY = "org.eclipse.e4.ui.core.css.swt.TEXT_KEY";

}
