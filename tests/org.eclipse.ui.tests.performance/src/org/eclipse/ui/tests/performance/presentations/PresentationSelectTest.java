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
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.tests.performance.TestRunnable;
import org.eclipse.ui.tests.performance.layout.PresentationWidgetFactory;

public class PresentationSelectTest extends PresentationPerformanceTest {
    
    private int type;
    private int number;
    private AbstractPresentationFactory factory;
    
    public PresentationSelectTest(AbstractPresentationFactory factory, int type, int number) {
        super(PresentationWidgetFactory.describePresentation(factory, type) + " selection change");
        this.type = type;
        this.number = number;
        this.factory = factory;
    }
    
    protected void runTest() throws Throwable {
        final PresentationTestbed testbed = createPresentation(factory, type, number);
        
        final IPresentablePart[] parts = testbed.getPartList();
        
        exercise(new TestRunnable() {
            public void run() throws Exception {
                
                startMeasuring();
                
                for (int i = 0; i < parts.length; i++) {
                    IPresentablePart part = parts[i];
                    
                    testbed.setSelection(part);
                    processEvents();
                }
                
                stopMeasuring();
            } 
        });
        
        commitMeasurements();
        assertPerformance(); 
    }
}
