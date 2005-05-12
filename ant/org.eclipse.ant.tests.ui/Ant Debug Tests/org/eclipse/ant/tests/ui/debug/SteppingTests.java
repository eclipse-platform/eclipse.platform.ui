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
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
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
		antCallStack(false, 12, DebugEvent.STEP_OVER, "default: echo", 7);
    }
    
    /**
     * bug 84400
     */
    public void testStepBackFromAntCallSepVM() throws Exception {
		antCallStack(true, 12, DebugEvent.STEP_OVER, "default: echo", 7);
    }
	
	 /**
     * bug 88218
     */
    public void testStepIntoAntCall() throws Exception {
		antCallStack(false, 5, DebugEvent.STEP_INTO, "pre-call: echo", 16);
    }
    
    /**
     * bug 88218
     */
    public void testStepIntoAntCallSepVM() throws Exception {
		antCallStack(true, 5, DebugEvent.STEP_INTO, "pre-call: echo", 16);
    }
	
	 public void testStepOverAntCall() throws Exception {
		 antCallStack(false, 5, DebugEvent.STEP_OVER, "default: echo", 7);
	 }
	 
	 public void testStepOverAntCallSepVM() throws Exception {
		 antCallStack(true, 5, DebugEvent.STEP_OVER, "default: echo", 7);
	 }
	 
	 public void testStepOverAntCallHitBreakpoint() throws Exception {
		 String fileName = "debugAntCall";
		 createLineBreakpoint(12, fileName + ".xml");
		 antCallStack(false, 5, DebugEvent.BREAKPOINT, "call: sleep", 12);
	 }
	 
	 public void testStepOverAntCallHitBreakpointSepVM() throws Exception {
		 String fileName = "debugAntCall";
		 createLineBreakpoint(12, fileName + ".xml");
		 antCallStack(true, 5, DebugEvent.BREAKPOINT, "call: sleep", 12);
	 }
    
	private void antCallStack(boolean sepVM, int lineNumber, int kind, String frameName, int frameLineNumber) throws CoreException {
		String fileName = "debugAntCall";
		ILineBreakpoint bp = createLineBreakpoint(lineNumber, fileName + ".xml");
		AntThread thread= null;
		try {
			if (sepVM) {
				fileName+="SepVM";
			}
			ILaunchConfiguration config= getLaunchConfiguration(fileName);
			thread= launchToLineBreakpoint(config, bp);
			
			AntStackFrame frame = (AntStackFrame)thread.getTopStackFrame();
			assertNotNull(frame);
			switch (kind) {
				case DebugEvent.BREAKPOINT: 
					stepOverToHitBreakpoint(frame);
					break;
				case DebugEvent.STEP_OVER:
					stepOver(frame);
					break;
				case DebugEvent.STEP_INTO:
					stepInto(frame);
					break;
			}
			assertFrame(thread, frameName, frameLineNumber);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}
	
    private AntStackFrame assertFrame(AntThread thread, String frameName, int lineNumber) throws DebugException {
        AntStackFrame frame = (AntStackFrame)thread.getTopStackFrame();
        String actualFrameName= frame.getName();
        int actualLineNumber= frame.getLineNumber();
        assertTrue("Name of stack frame incorrect. Expected " + frameName + " was: " + actualFrameName, frameName.equals(actualFrameName));
        assertTrue("Line number of stack frame incorrect. Expected " + lineNumber + " was: " + actualLineNumber, lineNumber == actualLineNumber);
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
