/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
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
package org.akrogen.tkui.css.core.resources;

import java.io.InputStream;

public class CSSCoreResources {

	/*--- HTML Styles --*/

	public static InputStream getHTMLSimple() {
		return CSSCoreResources.class.getResourceAsStream("html/simple-html.css");
	}
	
	public static InputStream getHTMLFont() {
		return CSSCoreResources.class.getResourceAsStream("html/font-html.css");
	}
	

	public static InputStream getHTMLMatrix() {
		return CSSCoreResources.class.getResourceAsStream("html/Matrix.css");
	}
	
	public static InputStream getStyleCLass() {
		return CSSCoreResources.class.getResourceAsStream("style-class.css");
	}


}
