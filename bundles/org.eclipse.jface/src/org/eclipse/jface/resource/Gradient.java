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
 * <em>EXPERIMENTAL</em>
 * 
 * @since 3.0
 */
public class Gradient {

    private Color [] colors;
    private int [] percents;
    private int direction;

    /**
     * 
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
     * @return Returns the gradient data.
     */    
    public GradientData getGradientData() {
        RGB [] rgbs = new RGB[colors.length];
        for (int i = 0; i < rgbs.length; i++) {
            rgbs[i] = colors[i].getRGB();
        }
        return new GradientData(rgbs, percents, direction);
    }

    /**
     * @return Returns the colors.
     */
    public Color [] getColors() {
        return colors;
    }
    /**
     * @return Returns the direction.
     */
    public int getDirection() {
        return direction;
    }
    /**
     * @return Returns the percentages.
     */
    public int [] getPercents() {
        return percents;
    }
    
    /**
     * Dispose of the colors in this gradient
     */
    public void dispose() {
        for (int i = 0; i < colors.length; i++) {
            colors[i].dispose();
        }
    }
}
