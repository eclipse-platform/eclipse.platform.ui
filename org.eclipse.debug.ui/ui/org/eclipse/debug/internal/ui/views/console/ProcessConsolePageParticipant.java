/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.console;

import java.io.IOException;
import java.util.Map;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.IStreamsProxy2;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.ExecutionException;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.commands.Priority;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.contexts.EnabledSubmission;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ShowInContext;

/**
 * Creates and manages process console specific actions
 * 
 * @since 3.1
 */
public class ProcessConsolePageParticipant implements IConsolePageParticipant, IShowInSource, IShowInTargetList, IDebugEventSetListener, ISelectionListener {
	
	// actions
	private ConsoleTerminateAction fTerminate;
    private ConsoleRemoveLaunchAction fRemoveTerminated;
	private ConsoleRemoveAllTerminatedAction fRemoveAllTerminated;

    private ProcessConsole fConsole;

    private IPageBookViewPage fPage;

    private IConsoleView fView;
    
    private EOFHandler fEOFHandler;
    private EnabledSubmission fEnabledSubmission;
    private HandlerSubmission fHandlerSubmission;
	/**
	 * Handler to send EOF
	 */	
	private class EOFHandler extends AbstractHandler {
		/* (non-Javadoc)
		 * @see org.eclipse.ui.commands.IHandler#execute(java.lang.Object)
		 */
		public Object execute(Map parameter) throws ExecutionException {
            IStreamsProxy proxy = getProcess().getStreamsProxy();
            if (proxy instanceof IStreamsProxy2) {
                IStreamsProxy2 proxy2 = (IStreamsProxy2) proxy;
                try {
                    proxy2.closeInputStream();
                } catch (IOException e1) {
                }
            }
			return null;
		}
		
	}    
    		
    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsolePageParticipant#init(IPageBookViewPage, IConsole)
     */
    public void init(IPageBookViewPage page, IConsole console) {
        fPage = page;
        fConsole = (ProcessConsole) console;
        
        fRemoveTerminated = new ConsoleRemoveLaunchAction(fConsole.getProcess().getLaunch());
        fRemoveAllTerminated = new ConsoleRemoveAllTerminatedAction();
        fTerminate = new ConsoleTerminateAction(fConsole);
        
        fView = (IConsoleView) fPage.getSite().getPage().findView(IConsoleConstants.ID_CONSOLE_VIEW);
        
        DebugPlugin.getDefault().addDebugEventListener(this);
        fPage.getSite().getPage().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
        
        // contribute to toolbar
        IActionBars actionBars = fPage.getSite().getActionBars();
        configureToolBar(actionBars.getToolBarManager());
        
        // create handler and submissions for EOF
        fEOFHandler = new EOFHandler();
        fEnabledSubmission = new EnabledSubmission(IConsoleConstants.ID_CONSOLE_VIEW, page.getSite().getShell(), null, "org.eclipse.debug.ui.console"); //$NON-NLS-1$
        fHandlerSubmission = new HandlerSubmission(IConsoleConstants.ID_CONSOLE_VIEW, page.getSite().getShell(), null, "org.eclipse.debug.ui.commands.eof", fEOFHandler, Priority.MEDIUM); //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsolePageParticipant#dispose()
     */
    public void dispose() {
        deactivated();
        fPage.getSite().getPage().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		DebugPlugin.getDefault().removeDebugEventListener(this);
        if (fRemoveTerminated != null) {
            fRemoveTerminated.dispose();
            fRemoveTerminated = null;
        }
		if (fRemoveAllTerminated != null) {
			fRemoveAllTerminated.dispose();
			fRemoveAllTerminated = null;
		}
		if (fTerminate != null) {
		    fTerminate.dispose();
		    fTerminate = null;
		}
		fConsole = null;
    }

    /**
     * Contribute actions to the toolbar
     */
    protected void configureToolBar(IToolBarManager mgr) {
		mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fTerminate);
        mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fRemoveTerminated);
		mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fRemoveAllTerminated);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class required) {
        if (IShowInSource.class.equals(required)) {
            return this;
        }
        if (IShowInTargetList.class.equals(required)) {
            return this; 
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.IShowInSource#getShowInContext()
     */
    public ShowInContext getShowInContext() {
        IProcess process = getProcess();
        if (process == null) {
            return null;
        } 
        IDebugTarget target = (IDebugTarget)process.getAdapter(IDebugTarget.class);
        ISelection selection = null;
        if (target == null) {
            selection = new StructuredSelection(process);
        } else {
            selection = new StructuredSelection(target);
        }
        return new ShowInContext(null, selection);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.IShowInTargetList#getShowInTargetIds()
     */
    public String[] getShowInTargetIds() {
        return new String[] {IDebugUIConstants.ID_DEBUG_VIEW};
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
     */
    public void handleDebugEvents(DebugEvent[] events) {
        for (int i = 0; i < events.length; i++) {
            DebugEvent event = events[i];
            if (event.getSource().equals(getProcess())) {
                Runnable r = new Runnable() {
                    public void run() {
                        if (fTerminate != null) {
                            fTerminate.update();
                        }
                    }
                };
                
                DebugUIPlugin.getStandardDisplay().asyncExec(r);           
            }
        }
    }
    
    protected IProcess getProcess() {
        return fConsole != null ? fConsole.getProcess() : null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (fView != null && getProcess().equals(DebugUITools.getCurrentProcess())) {
            fView.display(fConsole);
        }
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsolePageParticipant#activated()
     */
    public void activated() {
        // add EOF submissions
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchCommandSupport commandSupport = workbench.getCommandSupport();
		IWorkbenchContextSupport contextSupport = workbench.getContextSupport();
		contextSupport.addEnabledSubmission(fEnabledSubmission);
		commandSupport.addHandlerSubmission(fHandlerSubmission);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsolePageParticipant#deactivated()
     */
    public void deactivated() {
        // remove EOF submissions
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchCommandSupport commandSupport = workbench.getCommandSupport();
		IWorkbenchContextSupport contextSupport = workbench.getContextSupport();
		commandSupport.removeHandlerSubmission(fHandlerSubmission);
		contextSupport.removeEnabledSubmission(fEnabledSubmission);
    }
}
