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
package org.eclipse.ant.internal.ui.debug;

import org.eclipse.debug.core.model.IBreakpoint;

public interface IAntDebugController {
	
	public void resume();
	public void suspend();
	public void stepInto();
	public void stepOver();
	public void handleBreakpoint(IBreakpoint breakpoint, boolean added);
	public void getProperties();
	public void getStackFrames();
	
}
