/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.tests.ui.debug;

public class SteppingTests extends AbstractAntDebugTest {

    public SteppingTests(String name) {
        super(name);
    }
    
    public void testStepBackFromAntCall() throws Exception {
        antCallStack(false);
    }
    
    private void antCallStack(boolean b) {
        // TODO Auto-generated method stub
        
    }

    public void testStepBackFromAntCallSepVM() throws Exception {
        antCallStack(false);
    }
    
    public void testStepBackFromAnt() throws Exception {
        antCallStack(false);
    }
    
    public void testStepBackFromAntSepVM() throws Exception {
        antCallStack(false);
    }
    
    public void testStepIntoAntCall() throws Exception {
        antCallStack(false);
    }
    
    public void testStepIntoAntCallSepVM() throws Exception {
        antCallStack(false);
    }

    public void testStepOverAntCall() throws Exception {
        antCallStack(false);
    }
    
    public void testStepOverAntCallSepVM() throws Exception {
        antCallStack(false);
    }
    
    public void testStepIntoAnt() throws Exception {
        antCallStack(false);
    }
    
    public void testStepIntoAntSepVM() throws Exception {
        antCallStack(false);
    }

    public void testStepOverAnt() throws Exception {
        antCallStack(false);
    }
    
    public void testStepOverAntSepVM() throws Exception {
        antCallStack(false);
    }
}
