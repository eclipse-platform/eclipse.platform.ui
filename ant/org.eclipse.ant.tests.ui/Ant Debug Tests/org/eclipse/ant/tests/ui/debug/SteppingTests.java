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

import org.eclipse.ant.internal.ui.debug.model.AntStackFrame;
import org.eclipse.ant.internal.ui.debug.model.AntThread;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILineBreakpoint;

public class SteppingTests extends AbstractAntDebugTest {

    public SteppingTests(String name) {
        super(name);
    }
    
    /**
     * bug 84400
     */
    public void testStepBackFromAntCall() throws Exception {
        antCallStack(false);
    }
    
    /**
     * bug 84400
     */
    public void testStepBackFromAntCallSepVM() throws Exception {
        antCallStack(false);
    }
    
    private void antCallStack(boolean sepVM) throws CoreException {
        String fileName = "debugAntCall";
        ILineBreakpoint bp = createLineBreakpoint(12, fileName + ".xml");
        AntThread thread= null;
        try {
            if (sepVM) {
                fileName+="SepVM";
            }
            ILaunchConfiguration config= getLaunchConfiguration(fileName);
            thread= launchToLineBreakpoint(config, bp);

            AntStackFrame frame = (AntStackFrame)thread.getTopStackFrame();
            assertNotNull(frame);
            stepOver(frame);
            assertFrame(thread, "default: echo", 7);
        } finally {
            terminateAndRemove(thread);
            removeAllBreakpoints();
        }
    }
    
    private AntStackFrame assertFrame(AntThread thread, String frameName, int lineNumber) {
        AntStackFrame frame = (AntStackFrame)thread.getTopStackFrame();
        String actualFrameName= frame.getName();
        int actualLineNumber= frame.getLineNumber();
        assertTrue("Name of stack frame incorrect. Expected " + frameName + "was: " + actualFrameName, frameName.equals(actualFrameName));
        assertTrue("Line number of stack frame incorrect. Expected " + lineNumber + "was: " + actualLineNumber, lineNumber == actualLineNumber);
        return frame;
    }
    
//    public void testStepBackFromAnt() throws Exception {
//        antCallStack(false);
//    }
//    
//    public void testStepBackFromAntSepVM() throws Exception {
//        antCallStack(false);
//    }
//    
//    public void testStepIntoAntCall() throws Exception {
//        antCallStack(false);
//    }
//    
//    public void testStepIntoAntCallSepVM() throws Exception {
//        antCallStack(false);
//    }
//
//    public void testStepOverAntCall() throws Exception {
//        antCallStack(false);
//    }
//    
//    public void testStepOverAntCallSepVM() throws Exception {
//        antCallStack(false);
//    }
//    
//    public void testStepIntoAnt() throws Exception {
//        antCallStack(false);
//    }
//    
//    public void testStepIntoAntSepVM() throws Exception {
//        antCallStack(false);
//    }
//
//    public void testStepOverAnt() throws Exception {
//        antCallStack(false);
//    }
//    
//    public void testStepOverAntSepVM() throws Exception {
//        antCallStack(false);
//    }
}
