/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core.commands;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IStep;

/**
 * Common function for step commands.
 * 
 * @since 3.3
 */
public abstract class StepCommand extends DebugCommand {

	protected void doExecute(Object[] targets, IProgressMonitor monitor, IRequest request) throws CoreException {
		for (int i = 0; i < targets.length; i++) {
			step(targets[i]);
		}
	}

	protected abstract void step(Object target) throws CoreException ;
	
	protected boolean isExecutable(Object[] targets, IProgressMonitor monitor, IEnabledStateRequest collector) throws CoreException {
		if (isThreadCompatible(targets)) {
			for (int i = 0; i < targets.length; i++) {
				if (!isSteppable(targets[i])) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	protected abstract boolean isSteppable(Object target) throws CoreException;
	
	protected boolean isThreadCompatible(Object[] targets) {
		if (targets.length == 1) {
			return true;
		}
		// check if frames from same thread
		Set threads = new HashSet(targets.length);
		for (int i = 0; i < targets.length; i++) {
			Object object = targets[i];
			IStackFrame frame = null;
			if (object instanceof IStackFrame) {
				frame = (IStackFrame) object;
			} else if (object instanceof IAdaptable) {
				frame = (IStackFrame)((IAdaptable)object).getAdapter(IStackFrame.class);
			}
			if (frame != null) {
				if (!threads.add(frame.getThread())) { 
					return false;
				}
			}
		}
		return true;
	}
	
	protected Object getTarget(Object element) {
		return getAdapter(element, IStep.class);
	}

}
