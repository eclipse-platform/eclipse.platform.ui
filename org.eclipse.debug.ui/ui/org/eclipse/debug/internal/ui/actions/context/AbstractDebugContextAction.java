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

import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.contexts.DebugContextManager;
import org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextListener;
import org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

public abstract class AbstractDebugContextAction extends Action implements IDebugContextListener {

    private IStructuredSelection fActiveContext;

    /**
     * Used to schedule jobs, or <code>null</code> if none
     */
    private IWorkbenchSiteProgressService fProgressService = null;

    private DebugRequestJob fRequestJob = null;

    class DebugRequestJob extends Job {

        private Object[] fElements = null;

        /**
         * Constructs a new job to perform a debug request (for example, step)
         * in the background.
         */
        public DebugRequestJob() {
            super(DebugUIPlugin.removeAccelerators(getText()));
            setPriority(Job.INTERACTIVE);
            setSystem(true);
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        protected IStatus run(IProgressMonitor monitor) {
            MultiStatus status = new MultiStatus(DebugUIPlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, getStatusMessage(), null);
            Object[] targets = null;
            synchronized (this) {
                targets = fElements;
                fElements = null;
            }
            for (int i = 0; i < targets.length; i++) {
                Object element = targets[i];
                Object target = getTarget(element);
                try {
                    // Action's enablement could have been changed since
                    // it was last enabled. Check that the action is still
                    // enabled before running the action.
                    if (target != null && isEnabledFor(target))
                        doAction(target);
                } catch (DebugException e) {
                    status.merge(e.getStatus());
                }
            }
            return status;
        }

        protected synchronized void setElements(Object[] elements) {
            fElements = elements;
        }

    }

    private UpdateEnablementJob fUpdateJob = null;

    private IWorkbenchWindow fWindow;

    private AbstractDebugContextActionDelegate fDelegate;

    class UpdateEnablementJob extends Job {

        ISelection targetSelection = null;

        public UpdateEnablementJob() {
            super(ActionMessages.AbstractDebugActionDelegate_1);
            setPriority(Job.INTERACTIVE);
            setSystem(true);
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        protected IStatus run(IProgressMonitor monitor) {
            ISelection context = null;
            synchronized (this) {
                context = targetSelection;
                targetSelection = null;
            }
            update(context);
            return Status.OK_STATUS;
        }

        protected synchronized void setContext(ISelection context) {
            targetSelection = context;
        }
    }

    public AbstractDebugContextAction() {
        super();
        String helpContextId = getHelpContextId();
        if (helpContextId != null)
            PlatformUI.getWorkbench().getHelpSystem().setHelp(this, helpContextId);
        setEnabled(false);
    }
   
    public void setDelegate(AbstractDebugContextActionDelegate delegate) {
        fDelegate = delegate;
    }

    protected abstract void doAction(Object target) throws DebugException;

    public AbstractDebugContextAction(String text) {
        super(text);
    }

    public AbstractDebugContextAction(String text, ImageDescriptor image) {
        super(text, image);
    }

    public AbstractDebugContextAction(String text, int style) {
        super(text, style);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextListener#contextActivated(org.eclipse.jface.viewers.ISelection, org.eclipse.ui.IWorkbenchPart)
     */
    public void contextActivated(ISelection context, IWorkbenchPart part) {
        if (part != null)
            fProgressService = (IWorkbenchSiteProgressService) part.getSite().getAdapter(IWorkbenchSiteProgressService.class);
        else
            fProgressService = null;

        fActiveContext = null;
        if (fUpdateJob == null) {
            fUpdateJob = new UpdateEnablementJob();
        }
        fUpdateJob.setContext(context);
        schedule(fUpdateJob);
    }

    /**
     * Schedules the given job with this action's progress service
     * 
     * @param job
     */
    private void schedule(Job job) {
        if (fProgressService == null) {
            job.schedule();
        } else {
            fProgressService.schedule(job);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextListener#contextChanged(org.eclipse.jface.viewers.ISelection, org.eclipse.ui.IWorkbenchPart)
     */
    public void contextChanged(ISelection context, IWorkbenchPart part) {
        contextActivated(context, part);
    }

    protected void update(ISelection context) {
        if (context instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) context;
            setEnabled(getEnableStateForContext(ss));
            fActiveContext = (IStructuredSelection) context;
        } else {
            setEnabled(false);
            fActiveContext = StructuredSelection.EMPTY;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.Action#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (fDelegate != null) {
            fDelegate.setEnabled(enabled);
        }
    }

    /**
     * Return whether the action should be enabled or not based on the given
     * selection.
     */
    protected boolean getEnableStateForContext(IStructuredSelection selection) {
        if (selection.size() == 0) {
            return false;
        }
        Iterator itr = selection.iterator();
        while (itr.hasNext()) {
            Object element = itr.next();
            Object target = getTarget(element);
            if (target == null || !isEnabledFor(target)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Translates the selected object to the target to operate on as required.
     * For example, an adapter on the selection.
     * 
     * @param selectee
     * @return target to operate/enable action on
     */
    protected Object getTarget(Object selectee) {
        return selectee;
    }

    protected boolean isEnabledFor(Object element) {
        return true;
    }

    public void init(IWorkbenchWindow window) {
        setWindow(window);
        IDebugContextManager manager = DebugContextManager.getDefault();
        manager.addDebugContextListener(this, window);
        ISelection activeContext = manager.getActiveContext(window);
        if (activeContext != null) {
            contextActivated(activeContext, null);
            // the window action is not instantiated until invoked the first
            // time must wait for enablement update or action will not run the first
            // time it is invoked, when it might be pending enablement
            try {
                fUpdateJob.join();
            } catch (InterruptedException e) {
            }
        }
    }

    protected void setWindow(IWorkbenchWindow window) {
        fWindow = window;
    }

    /**
     * Returns the most recent selection
     * 
     * @return structured selection
     */
    protected IStructuredSelection getContext() {
        return fActiveContext;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        IStructuredSelection selection = getContext();
        if (selection != null && isEnabled()) {
            // disable the action so it cannot be run again until an event or
            // selection change updates the enablement
            setEnabled(false);
            if (fRequestJob == null) {
                fRequestJob = new DebugRequestJob();
            }
            fRequestJob.setElements(selection.toArray());
            schedule(fRequestJob);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
     */
    public void runWithEvent(Event event) {
        run();
    }

    public void dispose() {
        IWorkbenchWindow window = getWindow();
        if (getWindow() != null) {
            DebugContextManager.getDefault().removeDebugContextListener(this, window);
        }
    }

    protected IWorkbenchWindow getWindow() {
        return fWindow;
    }

    /**
     * Returns the String to use as an error dialog message for a failed action.
     * This message appears as the "Message:" in the error dialog for this
     * action. Default is to return null.
     */
    protected String getErrorDialogMessage() {
        return null;
    }

    /**
     * Returns the String to use as a status message for a failed action. This
     * message appears as the "Reason:" in the error dialog for this action.
     * Default is to return the empty String.
     */
    protected String getStatusMessage() {
        return ""; //$NON-NLS-1$
    }

    public abstract String getHelpContextId();

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.Action#getId()
     */
    public abstract String getId();

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.Action#getText()
     */
    public abstract String getText();

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.Action#getToolTipText()
     */
    public abstract String getToolTipText();

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.Action#getDisabledImageDescriptor()
     */
    public abstract ImageDescriptor getDisabledImageDescriptor();

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.Action#getHoverImageDescriptor()
     */
    public abstract ImageDescriptor getHoverImageDescriptor();

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.Action#getImageDescriptor()
     */
    public abstract ImageDescriptor getImageDescriptor();
}
