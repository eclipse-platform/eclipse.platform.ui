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
package org.eclipse.debug.internal.ui.actions;


import java.util.Iterator;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jface.viewers.IStructuredSelection;

public class ResumeAllThreadsActionDelegate extends ResumeActionDelegate {

	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction(Object object) throws DebugException {
		if (object instanceof IDebugElement) {
            IDebugElement debugElement = (IDebugElement) object;
            IThread[] threads = debugElement.getDebugTarget().getThreads();
            for (int i = 0; i < threads.length; i++) {
                IThread thread = threads[i];
                if (thread.canResume()) {
                    thread.resume();
                }
            }
		}
	}
	
	/**
	 * @see AbstractDebugActionDelegate#isEnabledFor(Object)
	 */
	protected boolean isEnabledFor(Object element) {
		if (element instanceof IDebugElement) {
            IDebugElement debugElement = (IDebugElement) element;
            try {
                IThread[] threads = debugElement.getDebugTarget().getThreads();
                for (int i = 0; i < threads.length; i++) {
                    if (threads[i].canResume()) {
                        return true;
                    }
                }
            } catch (DebugException e) {
            }
        }
		return false;
	}

	/**
	 * @see AbstractDebugActionDelegate#getEnableStateForSelection(IStructuredSelection)
	 */
	protected boolean getEnableStateForSelection(IStructuredSelection selection) {
		boolean enabled = false;
		for (Iterator i = selection.iterator(); i.hasNext(); ) {
			Object element = i.next();
			if (!(element instanceof IDebugElement)) {
				return false; //all elements should be IStructuredSelection
			}
			if (!enabled && isEnabledFor(element)) {
				enabled = true;
			}
		}
		return enabled;
	}

}
