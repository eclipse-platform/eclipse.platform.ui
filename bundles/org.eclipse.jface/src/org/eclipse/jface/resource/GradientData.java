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

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;


/**
 * Instances of this class are descriptions of color gradients.  A gradient 
 * consists of an array of RGB data (describing the colors in the gradient), an
 * array of integers (describing the positions of the colors in the gradient),
 * and an integer that specifies the direction that the gradient should follow.
 * 
 * <p>
 * GradientData objects are used in the construction of Gradient objects.  The
 * relationship between Gradient and GradientData is analogous to the 
 * relationship between SWT Color and RGB, or SWT Font and FontData.
 * </p>
 * 
 * <p>
 * Unlike Gradient, instances of this class do not contain actual Colors.  As
 * such, there is no need for it to be disposed of and no dispose() method is 
 * provided.
 * </p>
 * 
 * <em>EXPERIMENTAL</em>
 * 
 * @since 3.0
 * @see Gradient
 * @see org.eclipse.swt.graphics.RGB
 */
public class GradientData {

    /**
     * The RGB components of this gradient.  The length of this array must be at 
     * least 1 and it may not contain <code>null</code> values.
     */
    public RGB [] colors;
    
    /**
     * The percentage components of this gradient.  The length of this array 
     * must be 1 less than the length of the color array.  No value will be
     * less than 0 or greater than 100 and the values must be in ascending order 
     * with no duplicates.
     */
    public int [] percents;
    
    /**
     * An integer that describes the direction of this gradient.  This must be
     * <code>SWT.HORIZONTAL</code> or <code>SWT.VERTICAL</code>.
     */
    public int direction;

    /**
     * Create a new instance.
     * 
     * @param colors the RGB components of this gradient.  The length of this 
     * array must be at least 1 and it may not contain <code>null</code> values.
     * @param percents the percentage components of this gradient.  The length 
     * of this array must be 1 less than the length of the color array. No value
     * will be less than 0 or greater than 100 and the values must be in 
     * ascending order with no duplicates. 
     * @param direction the integer that describes the direction of this 
     * gradient.  This must be <code>SWT.HORIZONTAL</code> or 
     * <code>SWT.VERTICAL</code>.
     */
    public GradientData(RGB [] colors, int [] percents, int direction) {
        if (colors.length == 0)
            throw new IllegalArgumentException();
        
        if ((colors.length - 1) != percents.length)
            throw new IllegalArgumentException();
        
        if (direction != SWT.HORIZONTAL && direction != SWT.VERTICAL)
            throw new IllegalArgumentException();
       
		for (int i = 0; i < percents.length; i++) {
			if (percents[i] < 0 || percents[i] > 100) {
			    throw new IllegalArgumentException();
			}
			if (i > 0 && percents[i] < percents[i-1]) {
			    throw new IllegalArgumentException();
			}
		}
        
        this.colors = colors;
        this.percents = percents;
        this.direction = direction;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (o instanceof GradientData) {
            GradientData other = (GradientData) o;
            return direction == other.direction 
            	&& Arrays.equals(percents, other.percents) 
            	&& Arrays.equals(colors, other.colors);
            
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {        
        return GradientData.class.hashCode() 
        	* direction 
        	* colors.length 
        	* colors[0].hashCode() ;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {        
        return colors.toString() + '/' + percents.toString() + '/' + direction;
    }
}
