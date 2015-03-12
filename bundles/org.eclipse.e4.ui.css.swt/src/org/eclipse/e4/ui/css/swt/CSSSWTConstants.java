/*******************************************************************************
 * Copyright (c) 2008, 2009 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt;

import org.eclipse.e4.ui.css.core.dom.properties.CSSBorderProperties;

public class CSSSWTConstants {

	/**
	 * Key value for setting and getting the CSS class name of  a widget.
	 * Clients may rely on the value of this key if they want to avoid a dependency on this package.
	 * @see Widget.getData(String) Widget.setData(String, Object)
	 */
	public static final String CSS_CLASS_NAME_KEY = "org.eclipse.e4.ui.css.CssClassName";

	/**
	 * Key value for setting and getting the CSS ID of a widget.
	 * Clients may rely on the value of this key if they want to avoid a dependency on this package.
	 * @see Widget.getData(String) Widget.setData(String, Object)
	 */
	public static final String CSS_ID_KEY = "org.eclipse.e4.ui.css.id";

	/**
	 * Key value for setting and getting the CSS engine styling a widget.
	 * Clients may rely on the value of this key if they want to avoid a dependency on this package.
	 * @see Widget.getData(String) Widget.setData(String, Object)
	 */
	public static final String CSS_ENGINE_KEY = "org.eclipse.e4.ui.css.core.engine";

	//HACK non API - see bug #267434
	public static final String MARGIN_WRAPPER_KEY = "org.eclipse.e4.ui.css.swt.marginWrapper";

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
	public static final String TEXT_KEY = "org.eclipse.e4.ui.css.swt.TEXT_KEY";

	public static final String ACTIVE_LOST = "org.eclipse.e4.ui.css.swt.ACTIVE_LOST";

	public static final String ACTIVE_LISTENER = "org.eclipse.e4.ui.css.swt.ACTIVE_LISTENER";

	public static final String FOCUS_LOST = "org.eclipse.e4.ui.css.swt.FOCUS_LOST";

	public static final String FOCUS_LISTENER = "org.eclipse.e4.ui.css.swt.FOCUS_LISTENER";

	public static final String MOUSE_HOVER = "org.eclipse.e4.ui.css.swt.HOVER";

	public static final String MOUSE_HOVER_LOST = "org.eclipse.e4.ui.css.swt.HOVER_LOST";

	public static final String BUTTON_SELECTED_LISTENER = "org.eclipse.e4.ui.css.swt.BUTTON_SELECTED_LISTENER";




}
