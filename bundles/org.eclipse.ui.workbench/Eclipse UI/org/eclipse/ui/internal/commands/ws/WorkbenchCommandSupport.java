package org.eclipse.ui.internal.commands.ws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.CommandHandlerServiceFactory;
import org.eclipse.ui.commands.CommandManagerFactory;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.ICompoundCommandHandlerService;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.keys.WorkbenchKeyboard;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeyFormatterFactory;
import org.eclipse.ui.keys.SWTKeySupport;

public class WorkbenchCommandSupport implements IWorkbenchCommandSupport {

    private IWorkbenchWindow activeWorkbenchWindow;

    private String activePartId;

    private String activePerspectiveId;

    private ICompoundCommandHandlerService compoundCommandHandlerService;

    private Map handlerSubmissionsByCommandId = new HashMap();

    private WorkbenchKeyboard keyboard;

    private volatile boolean keyFilterEnabled;

    private ICommandManager mutableCommandManager;

    private IPageListener pageListener = new IPageListener() {

        public void pageActivated(IWorkbenchPage workbenchPage) {
            processHandlerSubmissionsByCommandId(false);
        }

        public void pageClosed(IWorkbenchPage workbenchPage) {
            processHandlerSubmissionsByCommandId(false);
        }

        public void pageOpened(IWorkbenchPage workbenchPage) {
            processHandlerSubmissionsByCommandId(false);
        }
    };

    private IPartListener partListener = new IPartListener() {

        public void partActivated(IWorkbenchPart workbenchPart) {
            processHandlerSubmissionsByCommandId(false);
        }

        public void partBroughtToTop(IWorkbenchPart workbenchPart) {
            processHandlerSubmissionsByCommandId(false);
        }

        public void partClosed(IWorkbenchPart workbenchPart) {
            processHandlerSubmissionsByCommandId(false);
        }

        public void partDeactivated(IWorkbenchPart workbenchPart) {
            processHandlerSubmissionsByCommandId(false);
        }

        public void partOpened(IWorkbenchPart workbenchPart) {
            processHandlerSubmissionsByCommandId(false);
        }
    };

    private IPerspectiveListener perspectiveListener = new IPerspectiveListener() {

        public void perspectiveActivated(IWorkbenchPage workbenchPage,
                IPerspectiveDescriptor perspectiveDescriptor) {
            processHandlerSubmissionsByCommandId(false);
        }

        public void perspectiveChanged(IWorkbenchPage workbenchPage,
                IPerspectiveDescriptor perspectiveDescriptor, String changeId) {
            processHandlerSubmissionsByCommandId(false);
        }
    };

    private IWindowListener windowListener = new IWindowListener() {

        public void windowActivated(IWorkbenchWindow window) {
            processHandlerSubmissionsByCommandId(false);
        }

        public void windowClosed(IWorkbenchWindow window) {
            processHandlerSubmissionsByCommandId(false);
        }

        public void windowDeactivated(IWorkbenchWindow window) {
            processHandlerSubmissionsByCommandId(false);
        }

        public void windowOpened(IWorkbenchWindow window) {
            processHandlerSubmissionsByCommandId(false);
        }
    };

    private Workbench workbench;

    /**
     * Constructs a new instance of <code>WorkbenchCommandSupport</code> with
     * the workbench it should support. The workbench must already have a
     * command manager defined, and must have a display. This initializes the
     * key binding support.
     * 
     * @param workbench
     *            The workbench which needs command support; must not be <code>null</code>.
     */
    public WorkbenchCommandSupport(Workbench workbench) {
        this.workbench = workbench;
        mutableCommandManager = CommandManagerFactory.getCommandManager();
        compoundCommandHandlerService = CommandHandlerServiceFactory
                .getCompoundCommandHandlerService();
        KeyFormatterFactory.setDefault(SWTKeySupport
                .getKeyFormatterForPlatform());
        keyboard = new WorkbenchKeyboard(workbench, workbench
                .getActivitySupport().getActivityManager(), getCommandManager());
        setKeyFilterEnabled(true);
        workbench.addWindowListener(windowListener);
    }

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

        processHandlerSubmissionsByCommandId(true);
    }

    public void deregisterFromKeyBindings(Shell shell) {
        if (keyboard != null)
            keyboard.deregister(shell);
        else {
            String message = "deregisterFromKeyBindings: Global key bindings are not available."; //$NON-NLS-1$
            WorkbenchPlugin.log(message, new Status(IStatus.ERROR,
                    WorkbenchPlugin.PI_WORKBENCH, 0, message, new Exception()));
        }
    }

    public ICommandManager getCommandManager() {
        // TODO need to proxy this to prevent casts to IMutableCommandManager
        return mutableCommandManager;
    }

    public ICompoundCommandHandlerService getCompoundCommandHandlerService() {
        return compoundCommandHandlerService;
    }

    /**
     * <p>
     * An accessor for the keyboard interface this workbench is using. This can
     * be used by external class to get a reference with which they can
     * simulate key presses in the key binding architecture. This is used for
     * testing purposes currently.
     * </p>
     * 
     * @return A reference to the workbench keyboard interface; never <code>null</code>.
     */
    public WorkbenchKeyboard getKeyboard() {
        return keyboard;
    }

    public final boolean isKeyFilterEnabled() {
        synchronized (keyboard) {
            return keyFilterEnabled;
        }
    }

    private void processHandlerSubmissionsByCommandId(boolean force) {
        Map handlersByCommandId = new HashMap();
        String activePartId = null;
        String activePerspectiveId = null;
        IWorkbenchWindow activeWorkbenchWindow = workbench
                .getActiveWorkbenchWindow();

        if (this.activeWorkbenchWindow != activeWorkbenchWindow) {
            if (this.activeWorkbenchWindow != null) {
                this.activeWorkbenchWindow.removePageListener(pageListener);
                this.activeWorkbenchWindow
                        .removePerspectiveListener(perspectiveListener);
                this.activeWorkbenchWindow.getPartService().removePartListener(
                        partListener);
            }

            this.activeWorkbenchWindow = activeWorkbenchWindow;

            if (this.activeWorkbenchWindow != null) {
                this.activeWorkbenchWindow.addPageListener(pageListener);
                this.activeWorkbenchWindow
                        .addPerspectiveListener(perspectiveListener);
                this.activeWorkbenchWindow.getPartService().addPartListener(
                        partListener);
            }
        }

        if (activeWorkbenchWindow != null) {
            IWorkbenchPage activeWorkbenchPage = activeWorkbenchWindow
                    .getActivePage();

            if (activeWorkbenchPage != null) {
                IWorkbenchPart activeWorkbenchPart = activeWorkbenchPage
                        .getActivePart();

                if (activeWorkbenchPart != null) {
                    IWorkbenchPartSite activeWorkbenchPartSite = activeWorkbenchPart
                            .getSite();

                    if (activeWorkbenchPartSite != null)
                            activePartId = activeWorkbenchPartSite.getId();
                }

                IPerspectiveDescriptor perspectiveDescriptor = activeWorkbenchPage
                        .getPerspective();

                if (perspectiveDescriptor != null)
                        activePerspectiveId = perspectiveDescriptor.getId();
            }
        }

        if (force || !Util.equals(this.activePartId, activePartId)
                || !Util.equals(this.activePerspectiveId, activePerspectiveId)) {
            this.activePartId = activePartId;
            this.activePerspectiveId = activePerspectiveId;

            for (Iterator iterator = handlerSubmissionsByCommandId.entrySet()
                    .iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String commandId = (String) entry.getKey();
                List handlerSubmissions = (List) entry.getValue();
                SortedSet matchingHandlerSubmissions = null;

                for (Iterator iterator2 = handlerSubmissions.iterator(); iterator2
                        .hasNext();) {
                    HandlerSubmission handlerSubmission = (HandlerSubmission) iterator2
                            .next();
                    String activePartId2 = handlerSubmission.getActivePartId();
                    String activePerspectiveId2 = handlerSubmission
                            .getActivePerspectiveId();

                    if (activePartId2 != null && activePartId2 != activePartId)
                            continue;

                    if (activePerspectiveId2 != null
                            && activePerspectiveId2 != activePerspectiveId)
                            continue;

                    if (matchingHandlerSubmissions == null)
                            matchingHandlerSubmissions = new TreeSet();

                    matchingHandlerSubmissions.add(handlerSubmission);
                }

                if (matchingHandlerSubmissions != null) {
                    HandlerSubmission bestHandlerSubmission = (HandlerSubmission) matchingHandlerSubmissions
                            .last();
                    handlersByCommandId.put(commandId, bestHandlerSubmission
                            .getHandler());
                }
            }

            // TODO switch to this from the old mechanism in
            // WorkbenchCommandsAndContexts
            //((CommandManager) mutableCommandManager)
            //        .setHandlersByCommandId(handlersByCommandId);
        }
    }

    public void registerForKeyBindings(Shell shell, boolean dialogOnly) {
        if (keyboard != null)
            keyboard.register(shell, dialogOnly);
        else {
            String message = "registerForKeyBindings: Global key bindings are not available."; //$NON-NLS-1$
            WorkbenchPlugin.log(message, new Status(IStatus.ERROR,
                    WorkbenchPlugin.PI_WORKBENCH, 0, message, new Exception()));
        }
    }

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

        processHandlerSubmissionsByCommandId(true);
    }

    public final void setKeyFilterEnabled(boolean keyFilterEnabled) {
        synchronized (keyboard) {
            Display currentDisplay = Display.getCurrent();
            Listener keyFilter = keyboard.getKeyDownFilter();

            if (keyFilterEnabled) {
                currentDisplay.addFilter(SWT.KeyDown, keyFilter);
                currentDisplay.addFilter(SWT.Traverse, keyFilter);
            } else {
                currentDisplay.removeFilter(SWT.KeyDown, keyFilter);
                currentDisplay.removeFilter(SWT.Traverse, keyFilter);
            }

            this.keyFilterEnabled = keyFilterEnabled;
        }
    }
}
