/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.performance.presentations;

import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.tests.performance.layout.PresentationWidgetFactory;

public class PresentationActivePartPropertyTest extends PresentationPerformanceTest {
    
    private int type;
    private int number;
    private int iterations;
    private boolean fastTest;
    private AbstractPresentationFactory factory;
    
    public PresentationActivePartPropertyTest(AbstractPresentationFactory factory, int type, int number, boolean fastTest) {
        super(PresentationWidgetFactory.describePresentation(factory, type) + " active part properties");
        this.type = type;
        this.number = number;
        this.factory = factory;
        this.fastTest = fastTest;
       
    }
    
    protected void runTest() throws Throwable {
        
    	setDegradationComment("<a href=https://bugs.eclipse.org/bugs/show_bug.cgi?id=101072>See Bug 101072</a> ");
    	 
        final PresentationTestbed testbed = createPresentation(factory, type, number);
        
        final TestPresentablePart part = (TestPresentablePart)testbed.getSelection();
        
        if(fastTest)
        	iterations = 300;
        else
        	iterations = 5;
        
		for (int j = 0; j < 50; j++) {

			startMeasuring();
			for (int i = 0; i < iterations; i++) {
            
                twiddleProperty(DESCRIPTION, part);
                twiddleProperty(DIRTY, part);
                twiddleProperty(IMAGE, part);
                twiddleProperty(NAME, part);
                twiddleProperty(TITLE, part);
                twiddleProperty(TOOLBAR, part);
                twiddleProperty(TOOLTIP, part);
                
            } 
            stopMeasuring();
		}
        
        commitMeasurements();
        assertPerformance();                
    }
}
