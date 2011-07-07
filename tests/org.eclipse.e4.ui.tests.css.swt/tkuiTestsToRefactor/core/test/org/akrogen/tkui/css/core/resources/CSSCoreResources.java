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
