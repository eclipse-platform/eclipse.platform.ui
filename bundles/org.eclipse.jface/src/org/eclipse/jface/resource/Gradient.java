/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.RGB;


/**
 * Instances of this class are color gradients.  A gradient 
 * consists of an array of Color data (describing the colors in the gradient), 
 * an array of integers (describing the positions of the colors in the gradient),
 * and an integer that specifies the direction that the gradient should follow.
 * 
 * <p>
 * Gradient objects require GradientData for their contstruction.  The
 * relationship between Gradient and GradientData is analogous to the 
 * relationship between SWT Color and RGB, or SWT Font and FontData.
 * </p>
 * 
 * <p>
 * Unlike GradientData, this class contains Color objects that need to be 
 * manually disposed.  A dispose() method is provided for this.
 * </p>
 * 
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see GradientData
 * @see org.eclipse.swt.graphics.Color
 */
public class Gradient {

    private Color [] colors;
    private int [] percents;
    private int direction;

    /**
     * Create a new Gradient.
     * 
     * @param device The Device on which the component colors should be created.
     * @param data GradientData describing the colors, percentages and direction
     * of this gradient.
     */
    public Gradient(Device device, GradientData data) {
        this.direction = data.direction;
        this.percents = data.percents;
        
        RGB[] rgbs = data.colors;
        colors = new Color[rgbs.length];
        
        for (int i = 0; i < rgbs.length; i++) {
            colors[i] = new Color(device, rgbs[i]);
        }
    }
    
    /**
     * Dispose of the Colors in this gradient.
     */
    public void dispose() {
        for (int i = 0; i < colors.length; i++) {
            colors[i].dispose();
        }
    }
    
    /**
     * Returns a GradientData object that represents this Gradient.
     * 
     * @return the GradientData object that represents this Gradient
	 * @throws SWTException <ul>
	 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
	 * </ul> 
     */    
    public GradientData getGradientData() {
        RGB [] rgbs = new RGB[colors.length];
        for (int i = 0; i < rgbs.length; i++) {
            rgbs[i] = colors[i].getRGB();
        }
        return new GradientData(rgbs, percents, direction);
    }

    /**
     * Returns a Color array that is used by this Gradient.
     * 
     * @return the Color array that is used by this Gradient.  The length of 
     * this array is guarenteed to be at least 1 and no item will be 
     * <code>null</code>. 
     */    
    public Color [] getColors() {
        return colors;
    }
    
    /**
     * Returns an integer that describes the direction of this gradient.
     * 
     * @return the integer that describes the direction of this gradient.  This 
     * is guarenteed to be <code>SWT.HORIZONTAL</code> or 
     * <code>SWT.VERTICAL</code>.
     */
    public int getDirection() {
        return direction;
    }
    /**
     * Returns the percentage components of this gradient.  
     * 
     * @return the percentage component of this gradient.  The length of this 
     * array must be 1 less than the length of the color array. No value will be
     * less than 0 or greater than 100 and the values will be in ascending order 
     * with no duplicates.
     */
    public int [] getPercents() {
        return percents;
    }    
}
