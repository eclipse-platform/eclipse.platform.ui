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

package org.eclipse.ui.tests.performance;

import org.eclipse.ui.activities.IActivityManager;

/**
 * @since 3.1
 *
 */
public class GenerateIdentifiersTest extends BasicPerformanceTest {

    private int count;

    public GenerateIdentifiersTest(int numberOfIdentifiers) {
        super("Generate " + numberOfIdentifiers + " identifiers");
        this.count = numberOfIdentifiers;
    }
    
    protected void runTest() throws Throwable {       
        final IActivityManager activityManager = fWorkbench.getActivitySupport().getActivityManager();
        
        exercise(new TestRunnable() {
            public void run() throws Exception {
                // construct the Identifiers to test
                final String [] ids = new String[count];
                for (int i = 0; i < ids.length; i++) {
                    long timestamp = System.currentTimeMillis();
                    ids[i] = "org.eclipse.jdt.ui/" + i + timestamp;
                }
                
                startMeasuring();
                for(int i = 0; i < ids.length; i++) {
                    activityManager.getIdentifier(ids[i]);
                }                
                stopMeasuring();
            } 
        });
        commitMeasurements();
        assertPerformance();
    }
}
