/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.provisional;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IStatusMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**
 * A request monitor that collects attributes of a element's
 * label.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @since 3.2
 */
public interface ILabelRequestMonitor extends IStatusMonitor {

	/**
	 * Sets the text of the label. Cannot be <code>null</code>.
	 * 
	 * @param text
	 */
    public void setLabels(String[] text);
    
    /**
     * Sets the font of the label.
     * 
     * @param fontData
     */
    public void setFontDatas(FontData[] fontData);
    
    /**
     * Sets the image of the label.
     * 
     * @param image
     */
    public void setImageDescriptors(ImageDescriptor[] image);
    
    /**
     * Sets the foreground color of the label.
     * 
     * @param foreground
     */
    public void setForegrounds(RGB[] foreground);
    
    /**
     * Sets the background color of the label.
     * 
     * @param background
     */
    public void setBackgrounds(RGB[] background);

}
