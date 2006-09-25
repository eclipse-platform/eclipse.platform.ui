/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.context;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousSuspendResumeAdapter;
import org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor;
import org.eclipse.jface.resource.ImageDescriptor;

public class ResumeAction extends AbstractDebugContextAction {
	
	public ResumeAction() {
		setActionDefinitionId("org.eclipse.debug.ui.commands.Resume"); //$NON-NLS-1$
	}

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#doAction(java.lang.Object)
     */
    protected void doAction(Object element) {
        if (element instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) element;
            IAsynchronousSuspendResumeAdapter suspendResumer = (IAsynchronousSuspendResumeAdapter) adaptable.getAdapter(IAsynchronousSuspendResumeAdapter.class);
            if (suspendResumer != null) 
                suspendResumer.resume(element, new ActionRequestMonitor());
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#isEnabledFor(java.lang.Object, org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor)
     */
    protected void isEnabledFor(Object element, IBooleanRequestMonitor monitor) {
        if (element instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) element;
            IAsynchronousSuspendResumeAdapter suspendResumer = (IAsynchronousSuspendResumeAdapter) adaptable.getAdapter(IAsynchronousSuspendResumeAdapter.class);
            if (suspendResumer != null) {
                suspendResumer.canResume(element, monitor); 
            } else {
            	notSupported(monitor);
            }
        }        
    }


    /**
     * Resumes all threads in the target associated with the given element
     * 
     * @param object
     *            debug element
     * @throws DebugException
     *             on failure
     */
    protected void doActionForAllThreads(Object object) throws DebugException {
        if (isEnabledForAllThreads(object)) {
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
     * Returns whether 'resume all threads' should be enabled for the given
     * element.
     */
    protected boolean isEnabledForAllThreads(Object element) {
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

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getStatusMessage()
     */
    protected String getStatusMessage() {
        return ActionMessages.ResumeActionDelegate_Exceptions_occurred_attempting_to_resume__2;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getErrorDialogMessage()
     */
    protected String getErrorDialogMessage() {
        return ActionMessages.ResumeActionDelegate_Resume_failed__1;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getText()
     */
    public String getText() {
        return ActionMessages.ResumeAction_0;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getHelpContextId()
     */
    public String getHelpContextId() {
        return "resume_action_context"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getId()
     */
    public String getId() {
        return "org.eclipse.debug.ui.debugview.toolbar.resume"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getToolTipText()
     */
    public String getToolTipText() {
        return ActionMessages.ResumeAction_3;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getDisabledImageDescriptor()
     */
    public ImageDescriptor getDisabledImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_RESUME);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getHoverImageDescriptor()
     */
    public ImageDescriptor getHoverImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_RESUME);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_RESUME);
    }
}
