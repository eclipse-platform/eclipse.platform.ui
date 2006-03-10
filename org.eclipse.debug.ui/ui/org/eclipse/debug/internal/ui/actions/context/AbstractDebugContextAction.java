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

import org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor;
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

public abstract class AbstractDebugContextAction extends Action implements IDebugContextListener {

    private IStructuredSelection fActiveContext;

    private IWorkbenchWindow fWindow;

    private AbstractDebugContextActionDelegate fDelegate;

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

    protected abstract void doAction(Object target);

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
        fActiveContext = null;
        update(context);
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
            updateEnableStateForContext(ss);
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
    protected void updateEnableStateForContext(IStructuredSelection selection) {
        int size = selection.size();
        if (size == 0) {
            setEnabled(false);
        }
        BooleanRequestMonitor monitor = new BooleanRequestMonitor(this, size);
        Iterator itr = selection.iterator();
        while (itr.hasNext()) {
            Object element = itr.next();
            Object target = getTarget(element);
            if (target != null)
                isEnabledFor(target, monitor);
        }
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

    protected abstract void isEnabledFor(Object element, IBooleanRequestMonitor monitor);

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
//            try {
//                fUpdateJob.join();
//            } catch (InterruptedException e) {
//            }
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
            for (Iterator iter = selection.iterator(); iter.hasNext();) {
                Object element = iter.next();
                Object target = getTarget(element);
                doAction(target);
            }
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
