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
package org.eclipse.jface.preference;

import junit.framework.TestCase;

import org.eclipse.jface.resource.GradientData;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;


/**
 * A place to put all string conversion tests.
 * 
 * @since 3.0
 */
public class StringConverterTest extends TestCase {

    /**
     * 
     */
    public StringConverterTest() {
        super();
    }

    /**
     * @param name
     */
    public StringConverterTest(String name) {
        super(name);
    }

    
    public void testGradientFunctions() {
        RGB rgb1 = new RGB(255, 0, 0);
        int p1 = 33;
        RGB rgb2 = new RGB(0, 255, 255);
        int p2 = 33;
        RGB rgb3 = new RGB(0, 0 , 255);
        
        int d1 = SWT.HORIZONTAL;
        int d2 = SWT.VERTICAL;
        
    
        GradientData gd1 = new GradientData(new RGB [] {rgb1, rgb2, rgb3}, new int[] {p1, p2}, d1);
        assertEquals(gd1, StringConverter.asGradient(StringConverter.asString(gd1)));
        
        GradientData gd2 = new GradientData(new RGB [] {rgb1}, new int[] {}, d2);
        assertEquals(gd2, StringConverter.asGradient(StringConverter.asString(gd2)));        
    }
}
