/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.themes;

import org.eclipse.swt.graphics.RGB;

/**
 * A factory interface that may be used to specify a color value.  This is 
 * (optionally) used by the themes extension point for color value 
 * definitions.
 * <p>
 * Example usage:
 * <br/>
 * <code>
 * &lt;colorDefinition
 *     label="Custom Color"
 *     id="example.customColor"
 * 	   colorFactory="some.implementor.of.IColorFactory"&gt;
 * &lt;/colorDefinition&gt;
 * </code>
 * </p>
 * 
 * @since 3.0
 */
public interface IColorFactory {

    /**
     * Create a new color.
     * 
     * @return a new color.  This must never be <code>null</code>.
     */
    RGB createColor();
}
