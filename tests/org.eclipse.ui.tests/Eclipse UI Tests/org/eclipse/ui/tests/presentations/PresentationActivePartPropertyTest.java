/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.presentations;

import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.tests.performance.TestRunnable;
import org.eclipse.ui.tests.performance.layout.PresentationWidgetFactory;

public class PresentationActivePartPropertyTest extends PresentationPerformanceTest {
    
    private int type;
    private int number;
    private AbstractPresentationFactory factory;
    
    public PresentationActivePartPropertyTest(AbstractPresentationFactory factory, int type, int number) {
        super(PresentationWidgetFactory.describePresentation(factory, type) + " active part properties");
        this.type = type;
        this.number = number;
        this.factory = factory;
       
    }
    
    protected void runTest() throws Throwable {
        
    	setDegradationComment("See https://bugs.eclipse.org/bugs/show_bug.cgi?id=101072");
    	 
        final PresentationTestbed testbed = createPresentation(factory, type, number);
        
        final TestPresentablePart part = (TestPresentablePart)testbed.getSelection();
        
        exercise(new TestRunnable() {
            public void run() throws Exception {
                
                startMeasuring();
            
                twiddleProperty(DESCRIPTION, part);
                twiddleProperty(DIRTY, part);
                twiddleProperty(IMAGE, part);
                twiddleProperty(NAME, part);
                twiddleProperty(TITLE, part);
                twiddleProperty(TOOLBAR, part);
                twiddleProperty(TOOLTIP, part);
                
                stopMeasuring();
            } 
        });
        
        commitMeasurements();
        assertPerformance();                
    }
}
