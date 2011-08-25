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
package org.eclipse.e4.ui.css.core.dom.properties;

import org.w3c.dom.css.CSSPrimitiveValue;

/**
 * CSS Border properties interface.
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public interface CSSBorderProperties {

	/**
	 * Return border-color value.
	 * 
	 * @return
	 */
	public CSSPrimitiveValue getColor();	

	/**
	 * Set  border-color value.
	 * 
	 * @return
	 */
	public void setColor(CSSPrimitiveValue color);	
	
	/**
	 * Return border-width value.
	 * 
	 * @return
	 */	
	public int getWidth();
	
	/**
	 * Set border-width value.
	 * 
	 * @return
	 */	
	public void setWidth(int width);
	
	/**
	 * Return border-style value.
	 * 
	 * @return
	 */	
	public String getStyle();
	
	/**
	 * Set border-style value.
	 * 
	 * @return
	 */	
	public void setStyle(String style);
	
}
