/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
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
import java.util.Collection;
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
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.commands.Priority;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.commands.CommandManagerFactory;
import org.eclipse.ui.internal.commands.IMutableCommandManager;
import org.eclipse.ui.internal.commands.MutableCommandManager;
import org.eclipse.ui.internal.contexts.ws.WorkbenchContextSupport;
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
     * The least specific way in which a submissions may match another item.
     * This means that the submissions will match any value.
     */
    private static final int MATCH_ANY = 0;

    /**
     * The submissions specifies a shell, and this shell is not the active
     * shell. However, it is the active workbench window's shell. This is
     * halfway between matching any and matching specifically.
     */
    private static final int MATCH_PARTIAL = 1;

    /**
     * The items match exactly.
     */
    private static final int MATCH_EXACT = 2;

    static {
        MutableCommandManager.DEBUG_HANDLERS = Policy.DEBUG_HANDLERS
                && Policy.DEBUG_HANDLERS_VERBOSE;
        MutableCommandManager.DEBUG_HANDLERS_COMMAND_ID = Policy.DEBUG_HANDLERS_VERBOSE_COMMAND_ID;
        MutableCommandManager.DEBUG_COMMAND_EXECUTION = Policy.DEBUG_KEY_BINDINGS_VERBOSE;
    }

    /**
     * Generates an integer value representing the quality of the match between
     * <code>shellToMatch</code> and the active shell and workbench window's
     * shell. It is assumed that <code>shellToMatch</code> is either
     * <code>null</code>,<code>activeShell</code> or the active workbench
     * window's shell.
     * 
     * @param shellToMatch
     *            The shell to match; may be <code>null</code>.
     * @param activeShell
     *            The active shell shell; may be <code>null</code>.
     * @return One of <code>MATCH_ANY</code>,<code>MATCH_PARTIAL</code>,
     *         or <code>MATCH_EXACT</code>.
     */
    private static final int compareWindows(final Shell shellToMatch,
            final Shell activeShell) {
        if (shellToMatch == null) {
            return MATCH_ANY;
        } else if (shellToMatch == activeShell) {
            return MATCH_EXACT;
        }

        return MATCH_PARTIAL;
    }

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

    /**
     * The active workbench site when the handler submissions were last
     * processed. This value may be <code>null</code> if no workbench site is
     * selected.
     */
    private IWorkbenchSite activeWorkbenchSite;

    /**
     * The active workbench window when the handler submissions were last
     * processed. This value may be <code>null</code> if Eclipse is not the
     * active application.
     */
    private IWorkbenchWindow activeWorkbenchWindow;

    /**
     * The map of the handler submissions indexed by command identifier. This
     * value is never <code>null</code>, but may be empty. The command
     * identifiers are strings, and the handler submissions are instances of
     * <code>HandlerSubmission</code>.
     */
    private final Map handlerSubmissionsByCommandId = new HashMap();

    /**
     * The mutable command manager that should be notified of changes to the
     * list of active handlers. This value is never <code>null</code>.
     */
    private final IMutableCommandManager mutableCommandManager;

    /**
     * A listener for changes in the active page. Changes to the active page
     * causes the handler submissions to be reprocessed.
     */
    private final IPageListener pageListener = new IPageListener() {

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

    /**
     * A listener for changes in the active part. Changes to the active part
     * causes the handler submissions to be reprocessed.
     */
    private final IPartListener partListener = new IPartListener() {

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

    /**
     * A listener for changes in the active perspective. Changes to the active
     * perspective causes the handler submissions to be reprocessed.
     */
    private final IPerspectiveListener perspectiveListener = new IPerspectiveListener() {

        public void perspectiveActivated(IWorkbenchPage workbenchPage,
                IPerspectiveDescriptor perspectiveDescriptor) {
            processHandlerSubmissions(false);
        }

        public void perspectiveChanged(IWorkbenchPage workbenchPage,
                IPerspectiveDescriptor perspectiveDescriptor, String changeId) {
            processHandlerSubmissions(false);
        }
    };

    /**
     * Whether the command support should process handler submissions. If it is
     * not processing handler submissions, then it will update the listeners,
     * but do no further work. This flag is used to avoid excessive updating
     * when the workbench is performing some large change (e.g., opening an
     * editor, starting up, shutting down, switching perspectives, etc.)
     */
    private boolean processing = true;

    /**
     * The workbench this class is supporting. This value should never be
     * <code>null</code>.
     */
    private final Workbench workbench;

    /**
     * Constructs a new instance of <code>WorkbenchCommandSupport</code>
     * 
     * @param workbenchToSupport
     *            The workbench for which the support should be created; must
     *            not be <code>null</code>.
     */
    public WorkbenchCommandSupport(final Workbench workbenchToSupport) {
        workbench = workbenchToSupport;
        mutableCommandManager = CommandManagerFactory
                .getMutableCommandManager();
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
            final String commandId = proxy.getCommandId();
            final HandlerSubmission submission = new HandlerSubmission(null,
                    null, null, commandId, proxy, Priority.LOW);
            submissions.add(submission);
        }

        if (!submissions.isEmpty()) {
            addHandlerSubmissions(submissions);
        }
        // TODO Should these be removed at shutdown? Is life cycle important?
    }

    public void addHandlerSubmission(HandlerSubmission handlerSubmission) {
        addHandlerSubmissionReal(handlerSubmission);
        processHandlerSubmissions(true);
    }

    /**
     * Adds a single handler submission. This method is used by the two API
     * methods to actually add a single handler submission.
     * 
     * @param handlerSubmission
     *            The submission to be added; must not be <code>null</code>.
     */
    private final void addHandlerSubmissionReal(
            final HandlerSubmission handlerSubmission) {
        final String commandId = handlerSubmission.getCommandId();
        List handlerSubmissions2 = (List) handlerSubmissionsByCommandId
                .get(commandId);

        if (handlerSubmissions2 == null) {
            handlerSubmissions2 = new ArrayList();
            handlerSubmissionsByCommandId.put(commandId, handlerSubmissions2);
        }

        handlerSubmissions2.add(handlerSubmission);
    }

    public void addHandlerSubmissions(Collection handlerSubmissions) {
        final Iterator submissionItr = handlerSubmissions.iterator();
        while (submissionItr.hasNext()) {
            addHandlerSubmissionReal((HandlerSubmission) submissionItr.next());
        }

        processHandlerSubmissions(true);
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

    /**
     * Processes incoming handler submissions, and decides which handlers should
     * be active. If <code>force</code> is <code>false</code>, then it will
     * only reconsider handlers if the state of the workbench has changed.
     * 
     * @param force
     *            Whether to force reprocessing of the handlers -- regardless of
     *            whether the workbench has changed.
     */
    private void processHandlerSubmissions(boolean force) {
        processHandlerSubmissions(force, workbench.getDisplay()
                .getActiveShell());
    }

    /**
     * TODO See WorkbenchKeyboard. Switch to private when Bug 56231 is resolved.
     * 
     * @param force
     *            Whether to force reprocessing of the handlers -- regardless of
     *            whether the workbench has changed.
     * @param newActiveShell
     *            The shell that is now active. This could be the same as the
     *            current active shell, or it could indicate that a new shell
     *            has become active. This value can be <code>null</code> if
     *            there is no active shell currently (this can happen during
     *            shell transitions).
     */
    public void processHandlerSubmissions(boolean force,
            final Shell newActiveShell) {

        // We do not need to update the listeners until everything is done.
        if (!processing) {
            return;
        }

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
                newWorkbenchWindow.addPerspectiveListener(perspectiveListener);
                newWorkbenchWindow.getPartService().addPartListener(
                        partListener);
            }

            activeWorkbenchWindow = newWorkbenchWindow;

            update = true;
        }

        if (newWorkbenchWindow != null) {
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

        if (force || update || (activeWorkbenchSite != newWorkbenchSite)) {
            activeWorkbenchSite = newWorkbenchSite;
            Map handlersByCommandId = new HashMap();
            final WorkbenchContextSupport contextSupport = (WorkbenchContextSupport) workbench
                    .getContextSupport();
            final Map contextTree = contextSupport
                    .createFilteredContextTreeFor(contextSupport
                            .getContextManager().getEnabledContextIds());
            final boolean dialogOpen = contextTree
                    .containsKey(IWorkbenchContextSupport.CONTEXT_ID_DIALOG);

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
                            .getActiveWorkbenchPartSite();

                    if (activeWorkbenchSite2 != null
                            && activeWorkbenchSite2 != newWorkbenchSite)
                        continue;

                    final Shell activeShell2 = handlerSubmission
                            .getActiveShell();
                    final Shell wbWinShell;
                    if (activeWorkbenchWindow == null) {
                        wbWinShell = null;
                    } else {
                        wbWinShell = activeWorkbenchWindow.getShell();
                    }

                    if ((activeShell2 != null) && (activeShell2 != activeShell)
                            && ((activeShell2 != wbWinShell) || dialogOpen))
                        continue;

                    if (bestHandlerSubmission == null) {
                        bestHandlerSubmission = handlerSubmission;
                    } else {
                        int compareTo = Util.compareIdentity(
                                activeWorkbenchSite2, bestHandlerSubmission
                                        .getActiveWorkbenchPartSite());
                        final int currentMatch = compareWindows(activeShell2,
                                activeShell);
                        final Shell bestMatchingShell = bestHandlerSubmission
                                .getActiveShell();
                        final int bestMatch = compareWindows(bestMatchingShell,
                                activeShell);

                        if ((bestHandlerSubmission.getHandler() instanceof HandlerProxy)
                                && (currentMatch <= MATCH_PARTIAL)
                                && (dialogOpen)) {
                            /*
                             * TODO This is a workaround for the fact that there
                             * is no API to specify the shell for handlers
                             * contributed via XML. This would mean that these
                             * handlers would always lose to the workbench
                             * window fallback mechanism.
                             * 
                             * This workaround assumes that the handler
                             * submitted via XML is intended to match more
                             * closely than the fallback mechanism.
                             * 
                             * In the future, this XML-contributed handlers
                             * should be allowed to specify the level at which
                             * they are given priority: ANY, PARTIAL or EXACT.
                             */
                            compareTo = -1;

                            if ((DEBUG_VERBOSE)
                                    && ((DEBUG_VERBOSE_COMMAND_ID == null) || (DEBUG_VERBOSE_COMMAND_ID
                                            .equals(commandId)))) {
                                System.out
                                        .println("HANDLERS >>> A handler contributed via XML will win over a less than exact match: " //$NON-NLS-1$
                                                + bestHandlerSubmission
                                                        .getHandler());
                            }

                        } else if ((handlerSubmission.getHandler() instanceof HandlerProxy)
                                && (bestMatch <= MATCH_PARTIAL) && (dialogOpen)) {
                            /*
                             * TODO This is a workaround for the fact that there
                             * is no API to specify the shell for handlers
                             * contributed via XML. This would mean that these
                             * handlers would always lose to the workbench
                             * window fallback mechanism.
                             * 
                             * This workaround assumes that the handler
                             * submitted via XML is intended to match more
                             * closely than the fallback mechanism.
                             * 
                             * In the future, this XML-contributed handlers
                             * should be allowed to specify the level at which
                             * they are given priority: ANY, PARTIAL or EXACT.
                             */
                            compareTo = 1;
                            if ((DEBUG_VERBOSE)
                                    && ((DEBUG_VERBOSE_COMMAND_ID == null) || (DEBUG_VERBOSE_COMMAND_ID
                                            .equals(commandId)))) {
                                System.out
                                        .println("HANDLERS >>> A handler contributed via XML will win over a less than exact match: " //$NON-NLS-1$
                                                + handlerSubmission
                                                        .getHandler());
                            }

                        } else if ((currentMatch > bestMatch)
                                || (compareTo == 0)) {

                            /*
                             * Compare the two, to see if one is a better match.
                             */
                            compareTo = currentMatch - bestMatch;

                            if (compareTo == 0)
                                compareTo = Util.compare(handlerSubmission
                                        .getPriority(), bestHandlerSubmission
                                        .getPriority());
                        }

                        if (compareTo > 0) {
                            if ((DEBUG_VERBOSE)
                                    && ((DEBUG_VERBOSE_COMMAND_ID == null) || (DEBUG_VERBOSE_COMMAND_ID
                                            .equals(commandId)))) {
                                System.out
                                        .println("HANDLERS >>> Resolved conflict detected between "); //$NON-NLS-1$
                                System.out.println("HANDLERS >>>     win: " //$NON-NLS-1$
                                        + handlerSubmission);
                                System.out.println("HANDLERS >>>    lose: " //$NON-NLS-1$
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
                                    .println("HANDLERS >>> Resolved conflict detected between "); //$NON-NLS-1$
                            System.out.println("HANDLERS >>>     win: " //$NON-NLS-1$
                                    + bestHandlerSubmission);
                            System.out.println("HANDLERS >>>    lose: " //$NON-NLS-1$
                                    + handlerSubmission);
                        }
                    }
                }

                if (bestHandlerSubmission != null && !conflict)
                    handlersByCommandId.put(commandId, bestHandlerSubmission
                            .getHandler());
            }

            mutableCommandManager.setHandlersByCommandId(handlersByCommandId);
        }
    }

    public void removeHandlerSubmission(HandlerSubmission handlerSubmission) {
        removeHandlerSubmissionReal(handlerSubmission);
        processHandlerSubmissions(true);
    }

    /**
     * Removes a single handler submission. This method is used by the two API
     * methods to actually remove a single handler submission.
     * 
     * @param handlerSubmission
     *            The submission to be removed; must not be <code>null</code>.
     */
    private final void removeHandlerSubmissionReal(
            final HandlerSubmission handlerSubmission) {
        final String commandId = handlerSubmission.getCommandId();
        final List handlerSubmissions2 = (List) handlerSubmissionsByCommandId
                .get(commandId);

        if (handlerSubmissions2 != null) {
            handlerSubmissions2.remove(handlerSubmission);

            if (handlerSubmissions2.isEmpty()) {
                handlerSubmissionsByCommandId.remove(commandId);
            }
        }
    }

    public void removeHandlerSubmissions(Collection handlerSubmissions) {
        final Iterator submissionItr = handlerSubmissions.iterator();
        while (submissionItr.hasNext()) {
            removeHandlerSubmissionReal((HandlerSubmission) submissionItr
                    .next());
        }

        processHandlerSubmissions(true);
    }

    /**
     * Sets the active context identifiers on the mutable command manager this
     * class interacts with.
     * 
     * @param activeContextIds
     *            The new map of active context identifiers -- representing the
     *            hierarchy of active contexts. This should be a map of string
     *            values. It may be empty, but it should never be
     *            <code>null</code>.
     */
    public void setActiveContextIds(Map activeContextIds) {
        mutableCommandManager.setActiveContextIds(activeContextIds);
    }

    /**
     * Sets whether the workbench's command support should process handler
     * submissions. The workbench should not allow the event loop to spin unless
     * this value is set to <code>true</code>. If the value changes from
     * <code>false</code> to <code>true</code>, this automatically triggers
     * a re-processing of the handler submissions.
     * 
     * @param processing
     *            Whether to process handler submissions
     */
    public final void setProcessing(final boolean processing) {
        final boolean reprocess = !this.processing && processing;
        this.processing = processing;
        if (reprocess) {
            processHandlerSubmissions(true);
        }
    }
}