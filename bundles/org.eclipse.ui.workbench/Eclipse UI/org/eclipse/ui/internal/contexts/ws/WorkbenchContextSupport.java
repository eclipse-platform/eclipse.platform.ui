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
package org.eclipse.ui.internal.contexts.ws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;
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
import org.eclipse.ui.contexts.EnabledSubmission;
import org.eclipse.ui.contexts.IContext;
import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.contexts.NotDefinedException;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.contexts.ContextManagerFactory;
import org.eclipse.ui.internal.contexts.IMutableContextManager;
import org.eclipse.ui.internal.contexts.ProxyContextManager;
import org.eclipse.ui.internal.keys.WorkbenchKeyboard;
import org.eclipse.ui.internal.misc.Policy;

/**
 * Provides support for contexts within the workbench -- including key bindings,
 * and some default contexts for shell types.
 * 
 * @since 3.0
 */
public class WorkbenchContextSupport implements IWorkbenchContextSupport {

    /**
     * Whether the workbench context support should kick into debugging mode.
     * This causes the list of context identifier to the be reported before
     * every call to change the context identifiers.
     */
    private static final boolean DEBUG = Policy.DEBUG_CONTEXTS;

    /**
     * The number of stack trace elements to show when the contexts have
     * changed. This is used for debugging purposes to show what caused a
     * context switch to occur.
     */
    private static final int DEBUG_STACK_LENGTH_TO_SHOW = 5;

    /**
     * Whether the workbench context support should kick into verbose debugging
     * mode. This causes each context change to print out a bit of the current
     * stack -- letting you know what caused the context change to occur.
     */
    private static final boolean DEBUG_VERBOSE = Policy.DEBUG_CONTEXTS_VERBOSE;

    /**
     * Creates a tree of context identifiers, representing the hierarchical
     * structure of the given contexts. The tree is structured as a mapping from
     * child to parent.
     * 
     * @param contextIds
     *            The set of context identifiers to be converted into a tree;
     *            must not be <code>null</code>.
     * @return The tree of contexts to use; may be empty, but never
     *         <code>null</code>. The keys and values are both strings.
     */
    private final Map createContextTreeFor(final Set contextIds) {
        final Map contextTree = new HashMap();
        final IContextManager contextManager = getContextManager();

        final Iterator contextIdItr = contextIds.iterator();
        while (contextIdItr.hasNext()) {
            String childContextId = (String) contextIdItr.next();
            while (childContextId != null) {
                final IContext childContext = contextManager
                        .getContext(childContextId);

                try {
                    final String parentContextId = childContext.getParentId();
                    contextTree.put(childContextId, parentContextId);
                    childContextId = parentContextId;
                } catch (final NotDefinedException e) {
                    break; // stop ascending
                }
            }
        }

        return contextTree;
    }

    /**
     * <p>
     * Creates a tree of context identifiers, representing the hierarchical
     * structure of the given contexts. The tree is structured as a mapping from
     * child to parent. In this tree, the key binding specific filtering of
     * contexts will have taken place.
     * </p>
     * <p>
     * This method is intended for internal use only.
     * </p>
     * 
     * @param contextIds
     *            The set of context identifiers to be converted into a tree;
     *            must not be <code>null</code>.
     * @return The tree of contexts to use; may be empty, but never
     *         <code>null</code>. The keys and values are both strings.
     */
    public final Map createFilteredContextTreeFor(final Set contextIds) {
        // Check to see whether a dialog or window is active.
        boolean dialog = false;
        boolean window = false;
        Iterator contextIdItr = contextIds.iterator();
        while (contextIdItr.hasNext()) {
            final String contextId = (String) contextIdItr.next();
            if (CONTEXT_ID_DIALOG.equals(contextId)) {
                dialog = true;
                continue;
            }
            if (CONTEXT_ID_WINDOW.equals(contextId)) {
                window = true;
                continue;
            }
        }

        /*
         * Remove all context identifiers for contexts whose parents are dialog
         * or window, and the corresponding dialog or window context is not
         * active.
         */
        try {
            contextIdItr = contextIds.iterator();
            while (contextIdItr.hasNext()) {
                String contextId = (String) contextIdItr.next();
                IContext context = mutableContextManager.getContext(contextId);
                String parentId = context.getParentId();
                while (parentId != null) {
                    if (CONTEXT_ID_DIALOG.equals(parentId)) {
                        if (!dialog) {
                            contextIdItr.remove();
                        }
                        break;
                    }
                    if (CONTEXT_ID_WINDOW.equals(parentId)) {
                        if (!window) {
                            contextIdItr.remove();
                        }
                        break;
                    }
                    if (CONTEXT_ID_DIALOG_AND_WINDOW.equals(parentId)) {
                        if ((!window) && (!dialog)) {
                            contextIdItr.remove();
                        }
                        break;
                    }

                    context = mutableContextManager.getContext(parentId);
                    parentId = context.getParentId();
                }
            }
        } catch (NotDefinedException e) {
            if (DEBUG) {
                System.out.println("CONTEXTS >>> NotDefinedException('" //$NON-NLS-1$
                        + e.getMessage()
                        + "') while filtering dialog/window contexts"); //$NON-NLS-1$
            }
        }

        return createContextTreeFor(contextIds);
    }

    /**
     * Listens for shell activation events, and updates the list of enabled
     * contexts appropriately. This is used to keep the enabled contexts
     * synchronized with respect to the <code>activeShell</code> condition.
     */
    private Listener activationListener = new Listener() {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
         */
        public void handleEvent(Event event) {
            checkWindowType(event.display.getActiveShell());
        }
    };

    /**
     * The currently active shell. This value is never <code>null</code>.
     */
    private Shell activeShell;

    private IWorkbenchSite activeWorkbenchSite;

    /**
     * The workbench window on which the listeners are currently attached.
     */
    private IWorkbenchWindow activeWorkbenchWindow;

    private Map enabledSubmissionsByContextId = new HashMap();

    /**
     * The key binding support for the contexts. In the workbench, key bindings
     * are intimately tied to the context mechanism.
     */
    private WorkbenchKeyboard keyboard;

    /**
     * Whether the key binding service is currently active. That is, whether it
     * is currently listening for (and possibly eating) key events on the
     * display.
     */
    private volatile boolean keyFilterEnabled;

    private IMutableContextManager mutableContextManager;

    private IPageListener pageListener = new IPageListener() {

        public void pageActivated(IWorkbenchPage workbenchPage) {
            processEnabledSubmissions(false);
        }

        public void pageClosed(IWorkbenchPage workbenchPage) {
            processEnabledSubmissions(false);
        }

        public void pageOpened(IWorkbenchPage workbenchPage) {
            processEnabledSubmissions(false);
        }
    };

    private IPartListener partListener = new IPartListener() {

        public void partActivated(IWorkbenchPart workbenchPart) {
            processEnabledSubmissions(false);
        }

        public void partBroughtToTop(IWorkbenchPart workbenchPart) {
            processEnabledSubmissions(false);
        }

        public void partClosed(IWorkbenchPart workbenchPart) {
            processEnabledSubmissions(false);
        }

        public void partDeactivated(IWorkbenchPart workbenchPart) {
            processEnabledSubmissions(false);
        }

        public void partOpened(IWorkbenchPart workbenchPart) {
            processEnabledSubmissions(false);
        }
    };

    private IPerspectiveListener perspectiveListener = new IPerspectiveListener() {

        public void perspectiveActivated(IWorkbenchPage workbenchPage,
                IPerspectiveDescriptor perspectiveDescriptor) {
            processEnabledSubmissions(false);
        }

        public void perspectiveChanged(IWorkbenchPage workbenchPage,
                IPerspectiveDescriptor perspectiveDescriptor, String changeId) {
            processEnabledSubmissions(false);
        }
    };

    /**
     * Whether the context support should process enabled submissions. If it is
     * not processing enabled submissions, then it will update the listeners,
     * but do no further work. This flag is used to avoid excessive updating
     * when the workbench is performing some large change (e.g., opening an
     * editor, starting up, shutting down, switching perspectives, etc.)
     */
    private boolean processing = true;

    private ProxyContextManager proxyContextManager;

    /**
     * This is a map of shell to a list of submissions. When a shell is
     * registered, it is added to this map with the list of submissions that
     * should be submitted when the shell is active. When the shell is
     * deactivated, this same list should be withdrawn. A shell is removed from
     * this map using the {@link #unregisterShell(Shell)}method. This value may
     * be empty, but is never <code>null</code>. The <code>null</code> key
     * is reserved for active shells that have not been registered but have a
     * parent (i.e., default dialog service).
     */
    private final Map registeredWindows = new WeakHashMap();

    private Workbench workbench;

    /**
     * Constructs a new instance of <code>WorkbenchCommandSupport</code>.
     * This attaches the key binding support, and adds a global shell activation
     * filter.
     * 
     * @param workbenchToSupport
     *            The workbench that needs to be supported by this instance;
     *            must not be <code>null</code>.
     */
    public WorkbenchContextSupport(final Workbench workbenchToSupport) {
        workbench = workbenchToSupport;
        mutableContextManager = ContextManagerFactory
                .getMutableContextManager();
        proxyContextManager = new ProxyContextManager(mutableContextManager);

        // And hook up a shell activation filter.
        workbenchToSupport.getDisplay().addFilter(SWT.Activate,
                activationListener);
    }

    public void addEnabledSubmission(EnabledSubmission enabledSubmission) {
        addEnabledSubmissionReal(enabledSubmission);
        processEnabledSubmissions(true);
    }

    /**
     * Adds a single enabled submission without causing the submissions to be
     * reprocessed. This is an internal method used by the two API methods.
     * 
     * @param enabledSubmission
     *            The enabled submission to add; must not be <code>null</code>.
     */
    private final void addEnabledSubmissionReal(
            EnabledSubmission enabledSubmission) {
        final String contextId = enabledSubmission.getContextId();
        List enabledSubmissions2 = (List) enabledSubmissionsByContextId
                .get(contextId);

        if (enabledSubmissions2 == null) {
            enabledSubmissions2 = new ArrayList();
            enabledSubmissionsByContextId.put(contextId, enabledSubmissions2);
        }

        enabledSubmissions2.add(enabledSubmission);
    }

    public void addEnabledSubmissions(Collection enabledSubmissions) {
        final Iterator submissionItr = enabledSubmissions.iterator();
        while (submissionItr.hasNext()) {
            addEnabledSubmissionReal((EnabledSubmission) submissionItr.next());
        }
        processEnabledSubmissions(true);
    }

    /**
     * Checks whether the new active shell is registered. If it is already
     * registered, then it does no work. If it is not registered, then it checks
     * what type of contexts the shell should have by default. This is
     * determined by parenting. A shell with no parent receives no contexts. A
     * shell with a parent, receives the dialog contexts.
     * 
     * @param newShell
     *            The newly active shell; may be <code>null</code> or
     *            disposed.
     */
    private final void checkWindowType(final Shell newShell) {
        boolean submissionsProcessed = false;
        final Shell oldShell = activeShell;

        if (newShell != oldShell) {
            /*
             * If the previous active shell was recognized as a dialog by
             * default, then remove its submissions.
             */
            List oldSubmissions = (List) registeredWindows.get(oldShell);
            if (oldSubmissions == null) {
                /*
                 * The old shell wasn't registered. So, we need to check if it
                 * was considered a dialog by default.
                 */
                oldSubmissions = (List) registeredWindows.get(null);
                if (oldSubmissions != null) {
                    removeEnabledSubmissions(oldSubmissions);
                    submissionsProcessed = true;
                }
            }

            /*
             * If the new active shell is recognized as a dialog by default,
             * then create some submissions, remember them, and submit them for
             * processing.
             */
            if ((newShell != null) && (!newShell.isDisposed())) {
                final List newSubmissions;

                if ((newShell.getParent() != null)
                        && (registeredWindows.get(newShell) == null)) {
                    // This is a dialog by default.
                    newSubmissions = new ArrayList();
                    newSubmissions.add(new EnabledSubmission(null, newShell,
                            null, CONTEXT_ID_DIALOG_AND_WINDOW));
                    newSubmissions.add(new EnabledSubmission(null, newShell,
                            null, CONTEXT_ID_DIALOG));
                    registeredWindows.put(null, newSubmissions);

                    /*
                     * Make sure the submissions will be removed in event of
                     * disposal. This is really just a paranoid check. The
                     * "oldSubmissions" code above should take care of this.
                     */
                    newShell.addDisposeListener(new DisposeListener() {

                        /*
                         * (non-Javadoc)
                         * 
                         * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
                         */
                        public void widgetDisposed(DisposeEvent e) {
                            registeredWindows.remove(null);
                            removeEnabledSubmissions(newSubmissions);
                            newShell.removeDisposeListener(this);
                        }
                    });

                } else {
                    // Shells that are not dialogs by default must register.
                    newSubmissions = (List) registeredWindows.get(newShell);

                }

                if (newSubmissions != null) {
                    addEnabledSubmissions(newSubmissions);
                    submissionsProcessed = true;
                }
            }
        }

        // If we still haven't reprocessed the submissions, then do it now.
        if (!submissionsProcessed) {
            processEnabledSubmissions(false, newShell);
        }
    }

    public IContextManager getContextManager() {
        return proxyContextManager;
    }

    /**
     * An accessor for the underlying key binding support. This method is
     * internal, and is not intended to be used by clients. It is currently only
     * used for testing purposes.
     * 
     * @return A reference to the key binding support; never <code>null</code>.
     */
    public final WorkbenchKeyboard getKeyboard() {
        return keyboard;
    }

    /**
     * Initializes the key binding support.
     */
    public final void initialize() {
        // Hook up the key binding support.
        keyboard = new WorkbenchKeyboard(workbench, workbench
                .getActivitySupport().getActivityManager(), workbench
                .getCommandSupport().getCommandManager());
        setKeyFilterEnabled(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.contexts.IWorkbenchContextSupport#isKeyFilterEnabled()
     */
    public boolean isKeyFilterEnabled() {
        synchronized (keyboard) {
            return keyFilterEnabled;
        }
    }

    private void processEnabledSubmissions(boolean force) {
        processEnabledSubmissions(force, workbench.getDisplay()
                .getActiveShell());
    }

    /**
     * TODO See WorkbenchKeyboard. Switch to private when Bug 56231 is resolved.
     */
    public void processEnabledSubmissions(boolean force,
            final Shell newActiveShell) {

        // If we are not currently processing, then wait.
        if (!processing) {
            return;
        }

        IWorkbenchSite newActiveWorkbenchSite = null;
        final IWorkbenchWindow newActiveWorkbenchWindow = workbench
                .getActiveWorkbenchWindow();
        boolean update = false;

        // Update the active shell, and swap the listener.
        if (activeShell != newActiveShell) {
            activeShell = newActiveShell;
            update = true;
        }

        // Update the active workbench window, and swap the listeners.
        if (activeWorkbenchWindow != newActiveWorkbenchWindow) {
            if (activeWorkbenchWindow != null) {
                activeWorkbenchWindow.removePageListener(pageListener);
                activeWorkbenchWindow
                        .removePerspectiveListener(perspectiveListener);
                activeWorkbenchWindow.getPartService().removePartListener(
                        partListener);
            }

            if (newActiveWorkbenchWindow != null) {
                newActiveWorkbenchWindow.addPageListener(pageListener);
                newActiveWorkbenchWindow
                        .addPerspectiveListener(perspectiveListener);
                newActiveWorkbenchWindow.getPartService().addPartListener(
                        partListener);
            }

            activeWorkbenchWindow = newActiveWorkbenchWindow;
            update = true;
        }

        /*
         * Get a reference to the active workbench site on the new active
         * workbench window.
         */
        if (newActiveWorkbenchWindow != null) {
            IWorkbenchPage activeWorkbenchPage = newActiveWorkbenchWindow
                    .getActivePage();

            if (activeWorkbenchPage != null) {
                IWorkbenchPart activeWorkbenchPart = activeWorkbenchPage
                        .getActivePart();

                if (activeWorkbenchPart != null)
                    newActiveWorkbenchSite = activeWorkbenchPart.getSite();
            }
        }

        if (force || update || (activeWorkbenchSite != newActiveWorkbenchSite)) {
            activeWorkbenchSite = newActiveWorkbenchSite;
            final Set enabledContextIds = new HashSet();

            for (Iterator iterator = enabledSubmissionsByContextId.entrySet()
                    .iterator(); iterator.hasNext();) {
                final Map.Entry entry = (Map.Entry) iterator.next();
                final String contextId = (String) entry.getKey();
                final List enabledSubmissions = (List) entry.getValue();

                for (int i = 0; i < enabledSubmissions.size(); i++) {
                    EnabledSubmission enabledSubmission = (EnabledSubmission) enabledSubmissions
                            .get(i);

                    Shell activeShell2 = enabledSubmission.getActiveShell();

                    if (activeShell2 != null && activeShell2 != newActiveShell)
                        continue;

                    IWorkbenchSite activeWorkbenchSite2 = enabledSubmission
                            .getActiveWorkbenchPartSite();

                    if (activeWorkbenchSite2 != null
                            && activeWorkbenchSite2 != newActiveWorkbenchSite)
                        continue;

                    enabledContextIds.add(contextId);
                    break;
                }
            }

            if ((DEBUG)
                    && (!mutableContextManager.getEnabledContextIds().equals(
                            enabledContextIds))) {
                System.out.println("CONTEXTS >>> " + enabledContextIds); //$NON-NLS-1$
                if (DEBUG_VERBOSE) {
                    final Exception exception = new Exception();
                    exception.fillInStackTrace();
                    final StackTraceElement[] stackTrace = exception
                            .getStackTrace();
                    final int elementsToShow = (stackTrace.length < DEBUG_STACK_LENGTH_TO_SHOW) ? stackTrace.length
                            : DEBUG_STACK_LENGTH_TO_SHOW;
                    for (int i = 0; i < elementsToShow; i++) {
                        final StackTraceElement element = stackTrace[i];
                        System.out.println("CONTEXTS >>>     " //$NON-NLS-1$
                                + element.toString());
                    }
                }
            }

            // Set the list of enabled identifiers to be the stripped list.
            mutableContextManager.setEnabledContextIds(enabledContextIds);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.contexts.IWorkbenchContextSupport#registerShell(org.eclipse.swt.widgets.Shell,
     *      int)
     */
    public boolean registerShell(final Shell shell, final int type) {
        // We do not allow null shell registration. It is reserved.
        if (shell == null) {
            throw new NullPointerException("The shell was null"); //$NON-NLS-1$
        }

        // Build the list of submissions.
        final List submissions = new ArrayList();
        switch (type) {
        case TYPE_DIALOG:
            submissions.add(new EnabledSubmission(null, shell, null,
                    CONTEXT_ID_DIALOG_AND_WINDOW));
            submissions.add(new EnabledSubmission(null, shell, null,
                    CONTEXT_ID_DIALOG));
            break;
        case TYPE_NONE:
            break;
        case TYPE_WINDOW:
            submissions.add(new EnabledSubmission(null, shell, null,
                    CONTEXT_ID_DIALOG_AND_WINDOW));
            submissions.add(new EnabledSubmission(null, shell, null,
                    CONTEXT_ID_WINDOW));
            break;
        default:
            throw new IllegalArgumentException("The type is not recognized: " //$NON-NLS-1$
                    + type);
        }

        // Check to see if the submissions are already present.
        boolean returnValue = false;
        List previousSubmissions = (List) registeredWindows.get(shell);
        if (previousSubmissions != null) {
            returnValue = true;
            removeEnabledSubmissions(previousSubmissions);
        }

        // Add the new submissions, and force some reprocessing to occur.
        registeredWindows.put(shell, submissions);
        addEnabledSubmissions(submissions);

        // Make sure the submissions will be removed in event of disposal.
        shell.addDisposeListener(new DisposeListener() {

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
             */
            public void widgetDisposed(DisposeEvent e) {
                registeredWindows.remove(shell);
                removeEnabledSubmissions(submissions);
            }
        });

        return returnValue;
    }

    public void removeEnabledSubmission(EnabledSubmission enabledSubmission) {
        removeEnabledSubmissionReal(enabledSubmission);
        processEnabledSubmissions(true);
    }

    /**
     * Removes a single enabled submission without causing all of the
     * submissions to be reprocessed. This is used by the two API methods to
     * carry out work.
     * 
     * @param enabledSubmission
     *            The submission to remove; must not be <code>null</code>.
     */
    private final void removeEnabledSubmissionReal(
            EnabledSubmission enabledSubmission) {
        final String contextId = enabledSubmission.getContextId();
        final List enabledSubmissions2 = (List) enabledSubmissionsByContextId
                .get(contextId);

        if (enabledSubmissions2 != null) {
            enabledSubmissions2.remove(enabledSubmission);

            if (enabledSubmissions2.isEmpty())
                enabledSubmissionsByContextId.remove(contextId);
        }
    }

    public void removeEnabledSubmissions(Collection enabledSubmissions) {
        final Iterator submissionItr = enabledSubmissions.iterator();
        while (submissionItr.hasNext()) {
            removeEnabledSubmissionReal((EnabledSubmission) submissionItr
                    .next());
        }
        processEnabledSubmissions(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.contexts.IWorkbenchContextSupport#setKeyFilterEnabled(boolean)
     */
    public void setKeyFilterEnabled(boolean enabled) {
        synchronized (keyboard) {
            Display currentDisplay = Display.getCurrent();
            Listener keyFilter = keyboard.getKeyDownFilter();

            if (enabled) {
                currentDisplay.addFilter(SWT.KeyDown, keyFilter);
                currentDisplay.addFilter(SWT.Traverse, keyFilter);
            } else {
                currentDisplay.removeFilter(SWT.KeyDown, keyFilter);
                currentDisplay.removeFilter(SWT.Traverse, keyFilter);
            }

            keyFilterEnabled = enabled;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.contexts.IWorkbenchContextSupport#unregisterShell(org.eclipse.swt.widgets.Shell)
     */
    public boolean unregisterShell(Shell shell) {
        // Don't allow this method to play with the special null slot.
        if (shell == null) {
            return false;
        }

        List previousSubmissions = (List) registeredWindows.get(shell);
        if (previousSubmissions != null) {
            registeredWindows.remove(shell);
            removeEnabledSubmissions(previousSubmissions);
            return true;
        }

        return false;
    }

    /**
     * Sets whether the workbench's context support should process enabled
     * submissions. The workbench should not allow the event loop to spin unless
     * this value is set to <code>true</code>. If the value changes from
     * <code>false</code> to <code>true</code>, this automatically triggers
     * a re-processing of the enabled submissions.
     * 
     * @param processing
     *            Whether to process enabled submissions
     */
    public final void setProcessing(final boolean processing) {
        final boolean reprocess = !this.processing && processing;
        this.processing = processing;
        if (reprocess) {
            processEnabledSubmissions(true);
        }
    }
}