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

import org.eclipse.swt.graphics.RGB;


/**
 * <em>EXPERIMENTAL</em>
 * 
 * @since 3.0
 */
public class GradientData {

    public RGB [] colors;
    public int [] percents;
    public int direction;

    /**
     * 
     */
    public GradientData(RGB [] colors, int [] percents, int direction) {
        if ((colors.length - 1) != percents.length)
            throw new IllegalArgumentException();
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
}
