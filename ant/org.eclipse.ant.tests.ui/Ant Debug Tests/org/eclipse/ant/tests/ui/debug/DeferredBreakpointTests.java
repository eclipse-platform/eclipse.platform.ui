/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.tests.ui.debug;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ant.internal.ui.debug.model.AntDebugTarget;
import org.eclipse.ant.internal.ui.debug.model.AntThread;
import org.eclipse.ant.internal.ui.launchConfigurations.IAntLaunchConfigurationConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;

/**
 * Tests deferred Ant breakpoints.
 */
public class DeferredBreakpointTests extends AbstractAntDebugTest {
	
	public DeferredBreakpointTests(String name) {
		super(name);
	}

	public void testDeferredBreakpoints() throws Exception {
		String fileName = "breakpoints";
        IFile file= getIFile(fileName + ".xml");
		List bps = new ArrayList();
		bps.add(createLineBreakpoint(5, file));
		bps.add(createLineBreakpoint(14, file));
		AntThread thread= null;
		try {
			thread= launchToBreakpoint(fileName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			while (!bps.isEmpty()) {
				IBreakpoint hit = getBreakpoint(thread);
				assertNotNull("suspended, but not by breakpoint", hit);
				assertTrue("hit un-registered breakpoint", bps.contains(hit));
				assertTrue("suspended, but not by line breakpoint", hit instanceof ILineBreakpoint);
				ILineBreakpoint breakpoint= (ILineBreakpoint) hit;
				int lineNumber = breakpoint.getLineNumber();
				int stackLine = thread.getTopStackFrame().getLineNumber();
				assertEquals("line numbers of breakpoint and stack frame do not match", lineNumber, stackLine);
				bps.remove(breakpoint);
				breakpoint.delete();
				if (!bps.isEmpty()) {
					thread = resume(thread);
				}
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}

	public void testDisabledBreakpoint() throws Exception {
        String fileName = "breakpoints";
        IFile file= getIFile(fileName + ".xml");
		ILineBreakpoint bp = createLineBreakpoint(5, file);
		bp.setEnabled(false);
		
		AntDebugTarget debugTarget = null;
		try {
			debugTarget= launchAndTerminate(fileName);
		} finally {
			terminateAndRemove(debugTarget);
			removeAllBreakpoints();
		}				
	}

	public void testEnableDisableBreakpoint() throws Exception {
        String fileName = "breakpoints";
        IFile file= getIFile(fileName + ".xml");
		ILineBreakpoint bp = createLineBreakpoint(4, file);
		bp.setEnabled(true);
		
		AntThread thread = null;
		try {
            ILaunchConfiguration config= getLaunchConfiguration(fileName);
            ILaunchConfigurationWorkingCopy copy= config.getWorkingCopy();
            copy.setAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_TARGETS, "entry1, entry2");
			thread= launchToLineBreakpoint(fileName, bp);
			bp.setEnabled(false);
			resumeAndExit(thread);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}				
	}
	
	public void testSkipLineBreakpoint() throws Exception {
        String fileName = "breakpoints";
        IFile file= getIFile(fileName + ".xml");
		ILineBreakpoint bp = createLineBreakpoint(4, file);
		createLineBreakpoint(15, file);
		
		AntThread thread = null;
		try {
		    thread= launchToLineBreakpoint(fileName, bp);
		    getBreakpointManager().setEnabled(false);
		    resumeAndExit(thread);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			getBreakpointManager().setEnabled(true);
		}			    
	}
}
