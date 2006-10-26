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

package org.eclipse.debug.internal.ui.commands.actions;

import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.commands.provisional.IDebugCommand;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Abstract base class for actions performing debug commands
 *
 * @since 3.3
 */
public abstract class DebugCommandAction extends Action implements IDebugContextListener {

	/**
	 * The window this action is working for.
	 */
    private IWorkbenchWindow fWindow;
    
    /**
     * The part this action is working for, or <code>null</code> if global to
     * a window.
     */
    private IWorkbenchPart fPart;
    
    /**
     * Command service.
     */
    private DebugCommandService fUpdateService;
    
    /**
     * Delegate this action is working for or <code>null</code> if none.
     */
    private DebugCommandActionDelegate fDelegate;

    /**
     * Constructor
     */
    public DebugCommandAction() {
        super();
        String helpContextId = getHelpContextId();
        if (helpContextId != null)
            PlatformUI.getWorkbench().getHelpSystem().setHelp(this, helpContextId);
        setEnabled(false);
    }

	/**
     * Set the current delegate
     * @param delegate
     */
    public void setDelegate(DebugCommandActionDelegate delegate) {
        fDelegate = delegate;
    }

    /**
     * Executes this action on the given target object
     * 
     * @param target the target to perform the action on
     */
    protected boolean execute(Object target) {
    	if (target instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) target;
			IDebugCommand capability = (IDebugCommand) adaptable.getAdapter(getCommandType());
			if (capability != null) {
				return capability.execute(target, createStatusMonitor(target));
			}
		}
    	return false;
    }
    
    /**
     * Creates and returns the status monitor to execute this action with.
     * 
     * @param target target of the command
     * @return status monitor to execute with
     */
    protected IAsynchronousRequestMonitor createStatusMonitor(Object target) {
    	return new ActionRequestMonitor();
    }
    
    /**
     * Returns the command type this action executes.
     * 
     * @return command class.
     */
    abstract protected Class getCommandType();
    
    public void update(ISelection context) {
    	fUpdateService.postUpdateCommand(getCommandType(), new CommandMonitor(this));
    }    

    public void debugContextChanged(DebugContextEvent event) {
		update(event.getContext());
	}

	/*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.Action#setEnabled(boolean)
     */
    public synchronized void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (fDelegate != null) {
            fDelegate.setEnabled(enabled);
        }
    }

    /**
     * Initializes this action for a specific part.
     * 
     * @param part workbench part
     */
    public void init(IWorkbenchPart part) {
        fPart = part;
        fWindow = part.getSite().getWorkbenchWindow();
        fUpdateService = DebugCommandService.getService(fWindow);
        IDebugContextService service = getDebugContextService();
		String partId = part.getSite().getId();
		service.addDebugContextListener(this, partId);
        ISelection activeContext = service.getActiveContext(partId);
        if (activeContext != null) {
        	fUpdateService.updateCommand(getCommandType(), new CommandMonitor(this));
        } else {
        	setEnabled(getInitialEnablement());
        }
    }
    
    /**
     * Initializes the context action
     * @param window the window
     */
    public void init(IWorkbenchWindow window) {
        fWindow = window;
        fUpdateService = DebugCommandService.getService(fWindow);
        IDebugContextService contextService = getDebugContextService();
		contextService.addDebugContextListener(this);
        ISelection activeContext = contextService.getActiveContext();
        if (activeContext != null) {
        	fUpdateService.updateCommand(getCommandType(), new BooleanRequestMonitor(this, 1));
        } else {
        	setEnabled(getInitialEnablement());
        }
    }
    
    /**
     * Returns whether this action should be enabled when initialized
     * and there is no active debug context.
     * 
     * @return false, by default
     */
    protected boolean getInitialEnablement() {
    	return false;
    }

    /**
     * Returns the most recent selection
     * 
     * @return structured selection
     */
    protected ISelection getContext() {
		if (fPart != null) {
			getDebugContextService().getActiveContext(fPart.getSite().getId());
    	}
        return getDebugContextService().getActiveContext();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public synchronized void run() {
        ISelection selection = getContext();
        if (selection instanceof IStructuredSelection && isEnabled()) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            boolean enabled = true;
            for (Iterator iter = ss.iterator(); iter.hasNext();) {
                Object element = iter.next();
                enabled = execute(element) & enabled;
            }
            // disable the action according to the command
            setEnabled(enabled);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
     */
    public void runWithEvent(Event event) {
        run();
    }

    /**
     * Clean up when removing
     */
    public void dispose() {
        IDebugContextService service = getDebugContextService();
        if (fPart != null) {
        	service.removeDebugContextListener(this, fPart.getSite().getId());
        } else {
            service.removeDebugContextListener(this);
        }
        fWindow = null;
        fPart = null;
    }
    
    /**
     * Returns the context service this action linked to.
     * @return
     */
    protected IDebugContextService getDebugContextService() {
    	return DebugUITools.getDebugContextManager().getContextService(fWindow);
    }

    /**
     * @return The help context id for this action
     */ 
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
    
    /**
     * Returns the delegate associated with this action or <code>null</code>
     * if none.
     * 
     * @return delegate or <code>null</code>
     */
    protected DebugCommandActionDelegate getDelegate() {
    	return fDelegate;
    }
}
