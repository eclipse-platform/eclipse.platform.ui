/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.commands.ws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.IMutableCommandManager;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.commands.NoSuchAttributeException;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.handlers.HandlerProxy;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.commands.CommandManagerFactory;
import org.eclipse.ui.internal.commands.MutableCommandManager;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeyFormatterFactory;
import org.eclipse.ui.keys.SWTKeySupport;

/**
 * Provides command support in terms of the workbench.
 * 
 * @since 3.0
 */
public class WorkbenchCommandSupport implements IWorkbenchCommandSupport {

    /**
     * Whether the workbench command support should kick into debugging mode.
     * This causes the unresolvable handler conflicts to be printed to the
     * console.
     */
    private static final boolean DEBUG = Policy.DEBUG_HANDLERS;

    /**
     * Whether the workbench command support should kick into verbose debugging
     * mode. This causes the resolvable handler conflicts to be printed to the
     * console.
     */
    private static final boolean DEBUG_VERBOSE = Policy.DEBUG_HANDLERS
            && Policy.DEBUG_HANDLERS_VERBOSE;

    /**
     * The command identifier to which the verbose output should be restricted.
     */
    private static final String DEBUG_VERBOSE_COMMAND_ID = Policy.DEBUG_HANDLERS_VERBOSE_COMMAND_ID;

    /**
     * Listens for shell activation events, and updates the list of enabled
     * handlers appropriately. This is used to keep the enabled handlers
     * synchronized with respect to the <code>activeShell</code> condition.
     */
    private Listener activationListener = new Listener() {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
         */
        public void handleEvent(Event event) {
            processHandlerSubmissions(false, event.display.getActiveShell());
        }
    };

    /**
     * The currently active shell. This value is never <code>null</code>.
     */
    private Shell activeShell;

    private IWorkbenchSite activeWorkbenchSite;

    private IWorkbenchWindow activeWorkbenchWindow;

    private Map handlerSubmissionsByCommandId = new HashMap();

    private IMutableCommandManager mutableCommandManager;

    private IPageListener pageListener = new IPageListener() {

        public void pageActivated(IWorkbenchPage workbenchPage) {
            processHandlerSubmissions(false);
        }

        public void pageClosed(IWorkbenchPage workbenchPage) {
            processHandlerSubmissions(false);
        }

        public void pageOpened(IWorkbenchPage workbenchPage) {
            processHandlerSubmissions(false);
        }
    };

    private IPartListener partListener = new IPartListener() {

        public void partActivated(IWorkbenchPart workbenchPart) {
            processHandlerSubmissions(false);
        }

        public void partBroughtToTop(IWorkbenchPart workbenchPart) {
            processHandlerSubmissions(false);
        }

        public void partClosed(IWorkbenchPart workbenchPart) {
            processHandlerSubmissions(false);
        }

        public void partDeactivated(IWorkbenchPart workbenchPart) {
            processHandlerSubmissions(false);
        }

        public void partOpened(IWorkbenchPart workbenchPart) {
            processHandlerSubmissions(false);
        }
    };

    private IPerspectiveListener perspectiveListener = new IPerspectiveListener() {

        public void perspectiveActivated(IWorkbenchPage workbenchPage,
                IPerspectiveDescriptor perspectiveDescriptor) {
            processHandlerSubmissions(false);
        }

        public void perspectiveChanged(IWorkbenchPage workbenchPage,
                IPerspectiveDescriptor perspectiveDescriptor, String changeId) {
            processHandlerSubmissions(false);
        }
    };

    private Workbench workbench;

    /**
     * Constructs a new instance of <code>WorkbenchCommandSupport</code>
     * 
     * @param workbenchToSupport
     *            The workbench for which the support should be created; must
     *            not be <code>null</code>.
     */
    public WorkbenchCommandSupport(final Workbench workbenchToSupport) {
        workbench = workbenchToSupport;
        mutableCommandManager = CommandManagerFactory.getMutableCommandManager();
        KeyFormatterFactory.setDefault(SWTKeySupport
                .getKeyFormatterForPlatform());

        // Attach a hook to latch on to the first workbench window to open.
        workbenchToSupport.getDisplay().addFilter(SWT.Activate,
                activationListener);
        
        final List submissions = new ArrayList();
        final MutableCommandManager commandManager = (MutableCommandManager) mutableCommandManager;
        final Set handlers = commandManager.getDefinedHandlers();
        final Iterator handlerItr = handlers.iterator();
        
        while (handlerItr.hasNext()) {
            final HandlerProxy proxy = (HandlerProxy) handlerItr.next();
            try {
                final String commandId = (String) proxy.getAttributeValue(HandlerProxy.ATTRIBUTE_ID);
                final Integer priority = (Integer) proxy.getAttributeValue(HandlerProxy.ATTRIBUTE_PRIORITY);
                final HandlerSubmission submission = new HandlerSubmission(null, null, commandId, proxy, priority.intValue());
                submissions.add(submission);
            } catch (final NoSuchAttributeException e) {
                // This submission can't be created.  Nothing to do.
            }
        }
        
        if (!submissions.isEmpty()) {
            addHandlerSubmissions(submissions);
        }
        // TODO Should these be removed at shutdown?  Is life cycle important?
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.commands.IWorkbenchCommandSupport#addHandlerSubmissions(java.util.List)
     */
    public void addHandlerSubmissions(List handlerSubmissions) {
        handlerSubmissions = Util.safeCopy(handlerSubmissions,
                HandlerSubmission.class);

        for (Iterator iterator = handlerSubmissions.iterator(); iterator
                .hasNext();) {
            HandlerSubmission handlerSubmission = (HandlerSubmission) iterator
                    .next();
            String commandId = handlerSubmission.getCommandId();
            List handlerSubmissions2 = (List) handlerSubmissionsByCommandId
                    .get(commandId);

            if (handlerSubmissions2 == null) {
                handlerSubmissions2 = new ArrayList();
                handlerSubmissionsByCommandId.put(commandId,
                        handlerSubmissions2);
            }

            handlerSubmissions2.add(handlerSubmission);
        }

        processHandlerSubmissions(true);
    }

    // TODO Remove this method -- deprecated.
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.commands.IWorkbenchCommandSupport#deregisterFromKeyBindings(org.eclipse.swt.widgets.Shell)
     */
    public void deregisterFromKeyBindings(Shell shell) {
        IWorkbenchContextSupport contextSupport = workbench.getContextSupport();
        contextSupport.unregisterShell(shell);
    }

    /**
     * An accessor for the underlying command manager.
     * 
     * @return The command manager used by this support class.
     */
    public ICommandManager getCommandManager() {
        // TODO need to proxy this to prevent casts to IMutableCommandManager
        return mutableCommandManager;
    }

    public void setActiveContextIds(Set activeContextIds) {
        mutableCommandManager.setActiveContextIds(activeContextIds);
    }
    
    // TODO Remove this method -- deprecated.
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.commands.IWorkbenchCommandSupport#isKeyFilterEnabled()
     */
    public final boolean isKeyFilterEnabled() {
        return workbench.getContextSupport().isKeyFilterEnabled();
    }

    private void processHandlerSubmissions(boolean force) {
        processHandlerSubmissions(force, workbench.getDisplay()
                .getActiveShell());
    }

    /**
     * If you use this method, I will break your legs.
     * 
     * TODO See WorkbenchKeyboard.  Switch to private when Bug 56231 is 
     * resolved.
     */
    public void processHandlerSubmissions(boolean force,
            final Shell newActiveShell) {
        IWorkbenchSite newWorkbenchSite = null;
        IWorkbenchWindow newWorkbenchWindow = workbench
                .getActiveWorkbenchWindow();
        boolean update = false;

        // Update the active shell, and swap the listener.
        if (activeShell != newActiveShell) {
            activeShell = newActiveShell;
            update = true;
        }

        if (activeWorkbenchWindow != newWorkbenchWindow) {
            if (activeWorkbenchWindow != null) {
                activeWorkbenchWindow.removePageListener(pageListener);
                activeWorkbenchWindow
                        .removePerspectiveListener(perspectiveListener);
                activeWorkbenchWindow.getPartService().removePartListener(
                        partListener);
            }

            if (newWorkbenchWindow != null) {
                newWorkbenchWindow.addPageListener(pageListener);
                newWorkbenchWindow
                        .addPerspectiveListener(perspectiveListener);
                newWorkbenchWindow.getPartService().addPartListener(
                        partListener);
            }

            activeWorkbenchWindow = newWorkbenchWindow;

            update = true;
        }

        if ((newWorkbenchWindow != null) && (newWorkbenchWindow.getShell() == newActiveShell)) {
            IWorkbenchPage activeWorkbenchPage = newWorkbenchWindow
                    .getActivePage();

            if (activeWorkbenchPage != null) {
                IWorkbenchPart activeWorkbenchPart = activeWorkbenchPage
                        .getActivePart();

                if (activeWorkbenchPart != null) {
                        newWorkbenchSite = activeWorkbenchPart.getSite();
                }
            }
        } else {
            newWorkbenchSite = null;
        }

        if (force || update
                || !Util.equals(activeWorkbenchSite, newWorkbenchSite)) {
            activeWorkbenchSite = newWorkbenchSite;
            Map handlersByCommandId = new HashMap();

            for (Iterator iterator = handlerSubmissionsByCommandId.entrySet()
                    .iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String commandId = (String) entry.getKey();
                List handlerSubmissions = (List) entry.getValue();
                Iterator submissionItr = handlerSubmissions.iterator();
                HandlerSubmission bestHandlerSubmission = null;
                boolean conflict = false;

                while (submissionItr.hasNext()) {
                    HandlerSubmission handlerSubmission = (HandlerSubmission) submissionItr
                            .next();
                    IWorkbenchSite activeWorkbenchSite2 = handlerSubmission
                            .getActiveWorkbenchSite();

                    if (activeWorkbenchSite2 != null
                            && activeWorkbenchSite2 != newWorkbenchSite)
                            continue;

                    IWorkbenchWindow activeWorkbenchWindow2 = handlerSubmission
                            .getActiveWorkbenchWindow();

                    if (activeWorkbenchWindow2 != null
                            && ((activeWorkbenchWindow2 != newWorkbenchWindow) || activeWorkbenchWindow2
                                    .getShell() != activeShell)) continue;

                    Shell activeShell2 = handlerSubmission.getActiveShell();

                    if (activeShell2 != null
                            && activeShell2 != activeShell)
                            continue;
                    
                    if (bestHandlerSubmission == null)
                        bestHandlerSubmission = handlerSubmission;
                    else {
                        int compareTo = Util.compare(activeWorkbenchSite2,
                                bestHandlerSubmission.getActiveWorkbenchSite());

                        if (compareTo == 0) {
                            compareTo = Util.compare(activeWorkbenchWindow2,
                                    bestHandlerSubmission
                                            .getActiveWorkbenchWindow());

                            if (compareTo == 0)
                                compareTo = Util.compare(activeShell2,
                                        bestHandlerSubmission
                                        	.getActiveShell());
                            
                            if (compareTo == 0)
                                    compareTo = Util.compare(-handlerSubmission
                                            .getPriority(),
                                            -bestHandlerSubmission
                                                    .getPriority());
                        }

                        if (compareTo > 0) {
                            if ((DEBUG_VERBOSE)
                                    && ((DEBUG_VERBOSE_COMMAND_ID == null) || (DEBUG_VERBOSE_COMMAND_ID
                                            .equals(commandId)))) {
                                System.out
                                        .println("HANDLERS >>> Resolved conflict detected for " //$NON-NLS-1$
                                                + bestHandlerSubmission);
                            }
                            conflict = false;
                            bestHandlerSubmission = handlerSubmission;
                        } else if ((compareTo == 0)
                                && (bestHandlerSubmission.getHandler() != handlerSubmission
                                        .getHandler())) {
                            if (DEBUG) {
                                System.out
                                        .println("HANDLERS >>> Unresolved conflict detected for " //$NON-NLS-1$
                                                + commandId);
                            }
                            conflict = true;
                        } else if ((DEBUG_VERBOSE)
                                && ((DEBUG_VERBOSE_COMMAND_ID == null) || (DEBUG_VERBOSE_COMMAND_ID
                                        .equals(commandId)))) {
                            System.out
                                    .println("HANDLERS >>> Resolved conflict detected for " //$NON-NLS-1$
                                            + bestHandlerSubmission);
                        }
                    }
                }

                if (bestHandlerSubmission != null && !conflict)
                        handlersByCommandId.put(commandId,
                                bestHandlerSubmission.getHandler());
            }

            mutableCommandManager.setHandlersByCommandId(handlersByCommandId);
        }
    }

    // TODO Remove this method -- deprecated
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.commands.IWorkbenchCommandSupport#registerForKeyBindings(org.eclipse.swt.widgets.Shell,boolean)
     */
    public void registerForKeyBindings(Shell shell, boolean dialogOnly) {
        IWorkbenchContextSupport contextSupport = workbench.getContextSupport();
        if (dialogOnly) {
            contextSupport.registerShell(shell,
                    IWorkbenchContextSupport.TYPE_DIALOG);
        } else {
            contextSupport.registerShell(shell,
                    IWorkbenchContextSupport.TYPE_WINDOW);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.commands.IWorkbenchCommandSupport#removeHandlerSubmissions(java.util.List)
     */
    public void removeHandlerSubmissions(List handlerSubmissions) {
        handlerSubmissions = Util.safeCopy(handlerSubmissions,
                HandlerSubmission.class);

        for (Iterator iterator = handlerSubmissions.iterator(); iterator
                .hasNext();) {
            HandlerSubmission handlerSubmission = (HandlerSubmission) iterator
                    .next();
            String commandId = handlerSubmission.getCommandId();
            List handlerSubmissions2 = (List) handlerSubmissionsByCommandId
                    .get(commandId);

            if (handlerSubmissions2 != null) {
                handlerSubmissions2.remove(handlerSubmission);

                if (handlerSubmissions2.isEmpty())
                        handlerSubmissionsByCommandId.remove(commandId);
            }
        }

        processHandlerSubmissions(true);
    }

    // TODO Remove this method -- deprecated
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.commands.IWorkbenchCommandSupport#setKeyFilterEnabled(boolean)
     */
    public final void setKeyFilterEnabled(boolean keyFilterEnabled) {
        workbench.getContextSupport().setKeyFilterEnabled(keyFilterEnabled);
    }
}
