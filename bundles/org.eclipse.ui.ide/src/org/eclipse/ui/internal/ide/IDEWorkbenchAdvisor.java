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
package org.eclipse.ui.internal.ide;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.ide.dialogs.WelcomeEditorInput;
import org.eclipse.ui.internal.ide.model.WorkbenchAdapterBuilder;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.MarkerTransfer;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.update.core.SiteManager;
import org.osgi.framework.Bundle;

/**
 * IDE-specified workbench advisor which configures the workbench for use as
 * an IDE.
 * <p>
 * Note: This class replaces <code>org.eclipse.ui.internal.Workbench</code>.
 * </p>
 * 
 * @since 3.0
 */
public class IDEWorkbenchAdvisor extends WorkbenchAdvisor {
    private static final String ACTION_BUILDER = "ActionBuilder"; //$NON-NLS-1$

    private static final String WELCOME_EDITOR_ID = "org.eclipse.ui.internal.ide.dialogs.WelcomeEditor"; //$NON-NLS-1$

    private static final String WORKBENCH_PREFERENCE_CATEGORY_ID = "org.eclipse.ui.preferencePages.Workbench"; //$NON-NLS-1$

    /**
     * The dialog setting key to access the known installed features
     * since the last time the workbench was run.
     */
    private static final String INSTALLED_FEATURES = "installedFeatures"; //$NON-NLS-1$

    private static IDEWorkbenchAdvisor workbenchAdvisor = null;

    /**
     * Event loop exception handler for the advisor.
     */
    private IDEExceptionHandler exceptionHandler = null;

    /**
     * Contains the workspace location if the -showlocation command line
     * argument is specified, or <code>null</code> if not specified.
     */
    private String workspaceLocation = null;

    /**
     * Ordered map of versioned feature ids -> info that are new for this
     * session; <code>null</code> if uninitialized. Key type:
     * <code>String</code>, Value type: <code>AboutInfo</code>.
     */
    private Map newlyAddedBundleGroups;

    /**
     * List of <code>AboutInfo</code> for all new installed
     * features that specify a welcome perspective.
     */
    private ArrayList welcomePerspectiveInfos = null;

    /**
     * Helper for managing activites in response to workspace changes.
     */
    private IDEWorkbenchActivityHelper activityHelper = null;

    /**
     * Signals that the welcome editors and/or intros have been opened.
     */
    private boolean editorsAndIntrosOpened = false;

    /**
     * Creates a new workbench advisor instance.
     */
    protected IDEWorkbenchAdvisor() {
        super();
        if (workbenchAdvisor != null) {
            throw new IllegalStateException();
        }
        workbenchAdvisor = this;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor#initialize
     */
    public void initialize(IWorkbenchConfigurer configurer) {

        // make sure we always save and restore workspace state
        configurer.setSaveAndRestore(true);

        // setup the event loop exception handler
        exceptionHandler = new IDEExceptionHandler(configurer);

        // register workspace adapters
        WorkbenchAdapterBuilder.registerAdapters();

        // get the command line arguments
        String[] cmdLineArgs = Platform.getCommandLineArgs();

        // include the workspace location in the title 
        // if the command line option -showlocation is specified
        for (int i = 0; i < cmdLineArgs.length; i++) {
            if ("-showlocation".equalsIgnoreCase(cmdLineArgs[i])) { //$NON-NLS-1$
                workspaceLocation = Platform.getLocation().toOSString();
                break;
            }
        }

        // register shared images
        declareWorkbenchImages();

        // initialize the activity helper
        activityHelper = IDEWorkbenchActivityHelper.getInstance();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor#preStartup()
     */
    public void preStartup() {

        //Suspend background jobs while we startup
        Platform.getJobManager().suspend();

        // support old welcome perspectives if intro plugin is not present
        if (!hasIntro()) {
            Map m = getNewlyAddedBundleGroups();
            welcomePerspectiveInfos = new ArrayList(m.size());
            for (Iterator i = m.values().iterator(); i.hasNext();) {
                AboutInfo info = (AboutInfo) i.next();
                if (info != null && info.getWelcomePerspectiveId() != null
                        && info.getWelcomePageURL() != null)
                    welcomePerspectiveInfos.add(info);
            }
        }

        //Register the build actions
        IProgressService service = PlatformUI.getWorkbench()
                .getProgressService();
        ImageDescriptor newImage = IDEInternalWorkbenchImages
                .getImageDescriptor(IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC);
        service.registerIconForFamily(newImage,
                ResourcesPlugin.FAMILY_MANUAL_BUILD);
        service.registerIconForFamily(newImage,
                ResourcesPlugin.FAMILY_AUTO_BUILD);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor#postStartup()
     */
    public void postStartup() {
        try {
            refreshFromLocal();
            checkUpdates();
        } finally {//Resume background jobs after we startup
            Platform.getJobManager().resume();
        }

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor#postShutdown
     */
    public void postShutdown() {
        if (activityHelper != null) {
            activityHelper.shutdown();
            activityHelper = null;
        }
        if (IDEWorkbenchPlugin.getPluginWorkspace() != null) {
            disconnectFromWorkspace();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor#eventLoopException
     */
    public void eventLoopException(Throwable exception) {
        super.eventLoopException(exception);
        if (exceptionHandler != null) {
            exceptionHandler.handleException(exception);
        } else {
            if (getWorkbenchConfigurer() != null) {
                getWorkbenchConfigurer().emergencyClose();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.application.WorkbenchAdvisor#preWindowShellClose
     */
    public boolean preWindowShellClose(
            IWorkbenchWindowConfigurer windowConfigurer) {
        if (getWorkbenchConfigurer().getWorkbench().getWorkbenchWindowCount() > 1) {
            return true;
        }
        // the user has asked to close the last window, while will cause the
        // workbench to close in due course - prompt the user for confirmation
        IPreferenceStore store = IDEWorkbenchPlugin.getDefault()
                .getPreferenceStore();
        boolean promptOnExit = store
                .getBoolean(IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW);

        if (promptOnExit) {
            String message;

            String productName = null;
            IProduct product = Platform.getProduct();
            if (product != null) {
                productName = product.getName();
            }
            if (productName == null) {
                message = IDEWorkbenchMessages
                        .getString("PromptOnExitDialog.message0"); //$NON-NLS-1$
            } else {
                message = IDEWorkbenchMessages
                        .format(
                                "PromptOnExitDialog.message1", new Object[] { productName }); //$NON-NLS-1$
            }

            MessageDialogWithToggle dlg = MessageDialogWithToggle
                    .openOkCancelConfirm(windowConfigurer.getWindow()
                            .getShell(), IDEWorkbenchMessages
                            .getString("PromptOnExitDialog.shellTitle"), //$NON-NLS-1$,
                            message, IDEWorkbenchMessages
                                    .getString("PromptOnExitDialog.choice"), //$NON-NLS-1$,
                            false, null, null);
            if (dlg.getReturnCode() != IDialogConstants.OK_ID) {
                return false;
            }
            if (dlg.getToggleState()) {
                store
                        .setValue(
                                IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW,
                                false);
                IDEWorkbenchPlugin.getDefault().savePluginPreferences();
            }
        }

        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor#preWindowOpen
     */
    public void preWindowOpen(IWorkbenchWindowConfigurer windowConfigurer) {

        // show the shortcut bar and progress indicator, which are hidden by default
        windowConfigurer.setShowPerspectiveBar(true);
        windowConfigurer.setShowFastViewBars(true);
        windowConfigurer.setShowProgressIndicator(true);

        // add the drag and drop support for the editor area
        windowConfigurer.addEditorAreaTransfer(EditorInputTransfer
                .getInstance());
        windowConfigurer.addEditorAreaTransfer(ResourceTransfer.getInstance());
        windowConfigurer.addEditorAreaTransfer(MarkerTransfer.getInstance());
        windowConfigurer
                .configureEditorAreaDropListener(new EditorAreaDropAdapter(
                        windowConfigurer.getWindow()));

        // hook up the listeners to update the window title
        windowConfigurer.getWindow().addPageListener(new IPageListener() {
            public void pageActivated(IWorkbenchPage page) {
                // do nothing
            }

            public void pageClosed(IWorkbenchPage page) {
                updateTitle(page.getWorkbenchWindow());
            }

            public void pageOpened(IWorkbenchPage page) {
                // do nothing
            }
        });
        windowConfigurer.getWindow().addPerspectiveListener(
                new IPerspectiveListener() {
                    public void perspectiveActivated(IWorkbenchPage page,
                            IPerspectiveDescriptor perspective) {
                        updateTitle(page.getWorkbenchWindow());
                    }

                    public void perspectiveChanged(IWorkbenchPage page,
                            IPerspectiveDescriptor perspective, String changeId) {
                        // do nothing
                    }
                });
        windowConfigurer.getWindow().getPartService().addPartListener(
                new IPartListener2() {
                    public void partActivated(IWorkbenchPartReference ref) {
                        if (ref instanceof IEditorReference
                                || ref.getPage().getActiveEditor() == null) {
                            updateTitle(ref.getPage().getWorkbenchWindow());
                        }
                    }

                    public void partBroughtToTop(IWorkbenchPartReference ref) {
                        if (ref instanceof IEditorReference
                                || ref.getPage().getActiveEditor() == null) {
                            updateTitle(ref.getPage().getWorkbenchWindow());
                        }
                    }

                    public void partClosed(IWorkbenchPartReference ref) {
                        // do nothing
                    }

                    public void partDeactivated(IWorkbenchPartReference ref) {
                        // do nothing
                    }

                    public void partOpened(IWorkbenchPartReference ref) {
                        // do nothing
                    }

                    public void partHidden(IWorkbenchPartReference ref) {
                        // do nothing
                    }

                    public void partVisible(IWorkbenchPartReference ref) {
                        // do nothing
                    }

                    public void partInputChanged(IWorkbenchPartReference ref) {
                        // do nothing
                    }
                });
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor#postWindowRestore
     */
    public void postWindowRestore(IWorkbenchWindowConfigurer windowConfigurer)
            throws WorkbenchException {
        int index = PlatformUI.getWorkbench().getWorkbenchWindowCount() - 1;

        if (index >= 0 && welcomePerspectiveInfos != null
                && index < welcomePerspectiveInfos.size()) {
            // find a page that exist in the window
            IWorkbenchPage page = windowConfigurer.getWindow().getActivePage();
            if (page == null) {
                IWorkbenchPage pages[] = windowConfigurer.getWindow()
                        .getPages();
                if (pages != null && pages.length > 0)
                    page = pages[0];
            }

            // if the window does not contain a page, create one
            String perspectiveId = ((AboutInfo) welcomePerspectiveInfos
                    .get(index)).getWelcomePerspectiveId();
            if (page == null) {
                IAdaptable root = getDefaultPageInput();
                page = windowConfigurer.getWindow().openPage(perspectiveId,
                        root);
            } else {
                IPerspectiveRegistry reg = PlatformUI.getWorkbench()
                        .getPerspectiveRegistry();
                IPerspectiveDescriptor desc = reg
                        .findPerspectiveWithId(perspectiveId);
                if (desc != null) {
                    page.setPerspective(desc);
                }
            }

            // set the active page and open the welcome editor
            windowConfigurer.getWindow().setActivePage(page);
            page.openEditor(new WelcomeEditorInput(
                    (AboutInfo) welcomePerspectiveInfos.get(index)),
                    WELCOME_EDITOR_ID, true);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor#postWindowClose
     */
    public void postWindowClose(IWorkbenchWindowConfigurer windowConfigurer) {
        WorkbenchActionBuilder a = (WorkbenchActionBuilder) windowConfigurer
                .getData(ACTION_BUILDER);
        if (a != null) {
            windowConfigurer.setData(ACTION_BUILDER, null);
            a.dispose();
        }
    }

    private void refreshFromLocal() {
        String[] commandLineArgs = Platform.getCommandLineArgs();
        IPreferenceStore store = IDEWorkbenchPlugin.getDefault()
                .getPreferenceStore();
        boolean refresh = store
                .getBoolean(IDEInternalPreferences.REFRESH_WORKSPACE_ON_STARTUP);
        if (!refresh)
            return;

        //Do not refresh if it was already done by core on startup.
        for (int i = 0; i < commandLineArgs.length; i++)
            if (commandLineArgs[i].equalsIgnoreCase("-refresh")) //$NON-NLS-1$
                return;

        IWorkbenchWindow window = getWorkbenchConfigurer().getWorkbench()
                .getActiveWorkbenchWindow();
        Shell shell = window == null ? null : window.getShell();
        ProgressMonitorDialog dlg = new ProgressMonitorJobsDialog(shell);
        final CoreException ex[] = new CoreException[1];
        try {
            dlg.run(true, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    try {
                        IContainer root = ResourcesPlugin.getWorkspace()
                                .getRoot();
                        root.refreshLocal(IResource.DEPTH_INFINITE, monitor);
                    } catch (CoreException e) {
                        ex[0] = e;
                    }
                }
            });
            if (ex[0] != null) {
                ErrorDialog.openError(shell, IDEWorkbenchMessages
                        .getString("Workspace.problemsTitle"), //$NON-NLS-1$ 
                        IDEWorkbenchMessages
                                .getString("Workspace.problemMessage"), //$NON-NLS-1$
                        ex[0].getStatus());
            }
        } catch (InterruptedException e) {
            //Do nothing. Operation was canceled.
        } catch (InvocationTargetException e) {
            String msg = "InvocationTargetException refreshing from local on startup"; //$NON-NLS-1$
            IDEWorkbenchPlugin.log(msg, new Status(IStatus.ERROR,
                    IDEWorkbenchPlugin.IDE_WORKBENCH, 0, msg, e
                            .getTargetException()));
        }
    }

    /**
     * Disconnect from the core workspace.
     */
    private void disconnectFromWorkspace() {
        // save the workspace
        final MultiStatus status = new MultiStatus(
                IDEWorkbenchPlugin.IDE_WORKBENCH, 1, IDEWorkbenchMessages
                        .getString("ProblemSavingWorkbench"), null); //$NON-NLS-1$
        IRunnableWithProgress runnable = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) {
                try {
                    status.merge(ResourcesPlugin.getWorkspace().save(true,
                            monitor));
                } catch (CoreException e) {
                    status.merge(e.getStatus());
                }
            }
        };
        try {
            new ProgressMonitorJobsDialog(null).run(true, false, runnable);
        } catch (InvocationTargetException e) {
            status
                    .merge(new Status(
                            IStatus.ERROR,
                            IDEWorkbenchPlugin.IDE_WORKBENCH,
                            1,
                            IDEWorkbenchMessages.getString("InternalError"), e.getTargetException())); //$NON-NLS-1$
        } catch (InterruptedException e) {
            status.merge(new Status(IStatus.ERROR,
                    IDEWorkbenchPlugin.IDE_WORKBENCH, 1, IDEWorkbenchMessages
                            .getString("InternalError"), e)); //$NON-NLS-1$
        }
        ErrorDialog.openError(null, IDEWorkbenchMessages
                .getString("ProblemsSavingWorkspace"), //$NON-NLS-1$
                null, status, IStatus.ERROR | IStatus.WARNING);
        if (!status.isOK()) {
            IDEWorkbenchPlugin.log(IDEWorkbenchMessages
                    .getString("ProblemsSavingWorkspace"), status); //$NON-NLS-1$
        }
    }

    /**
     * Checks if the -newUpdates command line argument is present
     * and if so, opens the update manager.
     */
    private void checkUpdates() {
        boolean newUpdates = false;
        String[] commandLineArgs = Platform.getCommandLineArgs();
        for (int i = 0; i < commandLineArgs.length; i++) {
            if (commandLineArgs[i].equalsIgnoreCase("-newUpdates")) { //$NON-NLS-1$
                newUpdates = true;
                break;
            }
        }

        if (newUpdates) {
            try {
                SiteManager.handleNewChanges();
            } catch (CoreException e) {
                IDEWorkbenchPlugin.log(
                        "Problem opening update manager", e.getStatus()); //$NON-NLS-1$
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor#isApplicationMenu
     */
    public boolean isApplicationMenu(
            IWorkbenchWindowConfigurer windowConfigurer, String menuID) {
        WorkbenchActionBuilder a = (WorkbenchActionBuilder) windowConfigurer
                .getData(ACTION_BUILDER);
        return a.isContainerMenu(menuID);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor#getDefaultPageInput
     */
    public IAdaptable getDefaultPageInput() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor
     */
    public String getInitialWindowPerspectiveId() {
        int index = PlatformUI.getWorkbench().getWorkbenchWindowCount() - 1;

        String perspectiveId = null;
        if (index >= 0 && welcomePerspectiveInfos != null
                && index < welcomePerspectiveInfos.size()) {
            perspectiveId = ((AboutInfo) welcomePerspectiveInfos.get(index))
                    .getWelcomePerspectiveId();
        }
        if (perspectiveId == null) {
            perspectiveId = IDE.RESOURCE_PERSPECTIVE_ID;
        }
        return perspectiveId;
    }

    /*
     * Open the welcome editor for the primary feature and
     * for any newly installed features.
     */
    private void openWelcomeEditors(IWorkbenchWindow window) {
        if (IDEWorkbenchPlugin.getDefault().getPreferenceStore().getBoolean(
                IDEInternalPreferences.WELCOME_DIALOG)) {
            // show the welcome page for the product the first time the workbench opens
            IProduct product = Platform.getProduct();
            if (product == null)
                return;

            AboutInfo productInfo = new AboutInfo(product);
            URL url = productInfo.getWelcomePageURL();
            if (url == null)
                return;

            IDEWorkbenchPlugin.getDefault().getPreferenceStore().setValue(
                    IDEInternalPreferences.WELCOME_DIALOG, false);
            openWelcomeEditor(window, new WelcomeEditorInput(productInfo), null);
        } else {
            // Show the welcome page for any newly installed features
            List welcomeFeatures = new ArrayList();
            for (Iterator it = getNewlyAddedBundleGroups().entrySet()
                    .iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                String versionedId = (String) entry.getKey();
                String featureId = versionedId.substring(0, versionedId
                        .indexOf(':'));
                AboutInfo info = (AboutInfo) entry.getValue();

                if (info != null && info.getWelcomePageURL() != null) {
                    welcomeFeatures.add(info);
                    // activate the feature plug-in so it can run some install code
                    IPlatformConfiguration platformConfiguration = BootLoader
                            .getCurrentPlatformConfiguration();
                    IPlatformConfiguration.IFeatureEntry feature = platformConfiguration
                            .findConfiguredFeatureEntry(featureId);
                    if (feature != null) {
                        String pi = feature.getFeaturePluginIdentifier();
                        if (pi != null) {
                            Platform.getPlugin(pi);
                        }
                    }
                }
            }

            int wCount = getWorkbenchConfigurer().getWorkbench()
                    .getWorkbenchWindowCount();
            for (int i = 0; i < welcomeFeatures.size(); i++) {
                AboutInfo newInfo = (AboutInfo) welcomeFeatures.get(i);
                String id = newInfo.getWelcomePerspectiveId();
                // Other editors were already opened in postWindowRestore(..)
                if (id == null || i >= wCount) {
                    openWelcomeEditor(window, new WelcomeEditorInput(newInfo),
                            id);
                }
            }
        }
    }

    /**
     * Returns the map of versioned feature ids -> info object for all installed
     * features. The format of the versioned feature id (the key of the map) is
     * featureId + ":" + versionId.
     * 
     * @return map of versioned feature ids -> info object (key type:
     *         <code>String</code>, value type: <code>AboutInfo</code>)
     * @since 3.0
     */
    private Map computeBundleGroupMap() {
        // use tree map to get predicable order
        Map ids = new TreeMap();

        IBundleGroupProvider[] providers = Platform.getBundleGroupProviders();
        for (int i = 0; i < providers.length; ++i) {
            IBundleGroup[] groups = providers[i].getBundleGroups();
            for (int j = 0; j < groups.length; ++j) {
                IBundleGroup group = groups[j];
                AboutInfo info = new AboutInfo(group);

                String version = info.getVersionId();
                version = version == null ? "0.0.0" //$NON-NLS-1$
                        : new PluginVersionIdentifier(version).toString();
                String versionedFeature = group.getIdentifier() + ":" + version; //$NON-NLS-1$

                ids.put(versionedFeature, info);
            }
        }

        return ids;
    }

    /**
     * Returns the ordered map of versioned feature ids -> AboutInfo that are
     * new for this session.
     * 
     * @return ordered map of versioned feature ids (key type:
     *         <code>String</code>) -> infos (value type:
     *         <code>AboutInfo</code>).
     */
    private Map getNewlyAddedBundleGroups() {
        if (newlyAddedBundleGroups == null)
            newlyAddedBundleGroups = createNewBundleGroupsMap();
        return newlyAddedBundleGroups;
    }

    /**
     * Updates the old features setting and returns a map of new features.
     */
    private Map createNewBundleGroupsMap() {
        // retrieve list of installed bundle groups from last session	
        IDialogSettings settings = IDEWorkbenchPlugin.getDefault()
                .getDialogSettings();
        String[] previousFeaturesArray = settings.getArray(INSTALLED_FEATURES);

        // get a map of currently installed bundle groups and store it for next session
        Map bundleGroups = computeBundleGroupMap();
        String[] currentFeaturesArray = new String[bundleGroups.size()];
        bundleGroups.keySet().toArray(currentFeaturesArray);
        settings.put(INSTALLED_FEATURES, currentFeaturesArray);

        // remove the previously known from the current set
        if (previousFeaturesArray != null)
            for (int i = 0; i < previousFeaturesArray.length; ++i)
                bundleGroups.remove(previousFeaturesArray[i]);

        return bundleGroups;
    }

    /*
     * Open a welcome editor for the given input
     */
    private void openWelcomeEditor(IWorkbenchWindow window,
            WelcomeEditorInput input, String perspectiveId) {
        if (getWorkbenchConfigurer().getWorkbench().getWorkbenchWindowCount() == 0) {
            // Something is wrong, there should be at least
            // one workbench window open by now.
            return;
        }

        IWorkbenchWindow win = window;
        if (perspectiveId != null) {
            try {
                win = getWorkbenchConfigurer().getWorkbench()
                        .openWorkbenchWindow(perspectiveId,
                                getDefaultPageInput());
                if (win == null) {
                    win = window;
                }
            } catch (WorkbenchException e) {
                IDEWorkbenchPlugin
                        .log(
                                "Error opening window with welcome perspective.", e.getStatus()); //$NON-NLS-1$
                return;
            }
        }

        if (win == null) {
            win = getWorkbenchConfigurer().getWorkbench().getWorkbenchWindows()[0];
        }

        IWorkbenchPage page = win.getActivePage();
        String id = perspectiveId;
        if (id == null) {
            id = getWorkbenchConfigurer().getWorkbench()
                    .getPerspectiveRegistry().getDefaultPerspective();
        }

        if (page == null) {
            try {
                page = win.openPage(id, getDefaultPageInput());
            } catch (WorkbenchException e) {
                ErrorDialog.openError(win.getShell(), IDEWorkbenchMessages
                        .getString("Problems_Opening_Page"), //$NON-NLS-1$
                        e.getMessage(), e.getStatus());
            }
        }
        if (page == null)
            return;

        if (page.getPerspective() == null) {
            try {
                page = getWorkbenchConfigurer().getWorkbench().showPerspective(
                        id, win);
            } catch (WorkbenchException e) {
                ErrorDialog
                        .openError(
                                win.getShell(),
                                IDEWorkbenchMessages
                                        .getString("Workbench.openEditorErrorDialogTitle"), //$NON-NLS-1$
                                IDEWorkbenchMessages
                                        .getString("Workbench.openEditorErrorDialogMessage"), //$NON-NLS-1$
                                e.getStatus());
                return;
            }
        }

        page.setEditorAreaVisible(true);

        // see if we already have an editor
        IEditorPart editor = page.findEditor(input);
        if (editor != null) {
            page.activate(editor);
            return;
        }

        try {
            page.openEditor(input, WELCOME_EDITOR_ID);
        } catch (PartInitException e) {
            ErrorDialog
                    .openError(
                            win.getShell(),
                            IDEWorkbenchMessages
                                    .getString("Workbench.openEditorErrorDialogTitle"), //$NON-NLS-1$
                            IDEWorkbenchMessages
                                    .getString("Workbench.openEditorErrorDialogMessage"), //$NON-NLS-1$
                            e.getStatus());
        }
        return;
    }

    /**
     * Updates the window title. Format will be:
     * [pageInput -] [currentPerspective -] [editorInput -] [workspaceLocation -] productName
     * @param window The window being updated.
     */
    private void updateTitle(IWorkbenchWindow window) {
        IWorkbenchWindowConfigurer windowConfigurer = getWorkbenchConfigurer()
                .getWindowConfigurer(window);

        String title = null;
        IProduct product = Platform.getProduct();
        if (product != null) {
            title = product.getName();
        }
        if (title == null) {
            title = ""; //$NON-NLS-1$
        }

        if (workspaceLocation != null) {
            title = IDEWorkbenchMessages
                    .format(
                            "WorkbenchWindow.shellTitle", new Object[] { workspaceLocation, title }); //$NON-NLS-1$
        }

        IWorkbenchPage currentPage = window.getActivePage();
        if (currentPage != null) {
            IEditorPart editor = currentPage.getActiveEditor();
            if (editor != null) {
                String editorTitle = editor.getTitle();
                title = IDEWorkbenchMessages
                        .format(
                                "WorkbenchWindow.shellTitle", new Object[] { editorTitle, title }); //$NON-NLS-1$
            }
            IPerspectiveDescriptor persp = currentPage.getPerspective();
            String label = ""; //$NON-NLS-1$
            if (persp != null)
                label = persp.getLabel();
            IAdaptable input = currentPage.getInput();
            if (input != null && !input.equals(getDefaultPageInput())) {
                label = currentPage.getLabel();
            }
            if (label != null && !label.equals("")) { //$NON-NLS-1$	
                title = IDEWorkbenchMessages
                        .format(
                                "WorkbenchWindow.shellTitle", new Object[] { label, title }); //$NON-NLS-1$
            }
        }

        windowConfigurer.setTitle(title);
    }

    /**
     * Declares all IDE-specific workbench images. This includes both "shared"
     * images (named in {@link IDE.SharedImages}) and
     * internal images (named in
     * {@link org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages}).
     * 
     * @see IWorkbenchConfigurer#declareImage
     */
    private void declareWorkbenchImages() {

        final String ICONS_PATH = "icons/full/";//$NON-NLS-1$
        final String PATH_ELOCALTOOL = ICONS_PATH + "elcl16/"; //Enabled toolbar icons.//$NON-NLS-1$
        final String PATH_ETOOL = ICONS_PATH + "etool16/"; //Enabled toolbar icons.//$NON-NLS-1$
        final String PATH_DTOOL = ICONS_PATH + "dtool16/"; //Disabled toolbar icons.//$NON-NLS-1$
        final String PATH_OBJECT = ICONS_PATH + "obj16/"; //Model object icons//$NON-NLS-1$
        final String PATH_WIZBAN = ICONS_PATH + "wizban/"; //Wizard icons//$NON-NLS-1$

        Bundle ideBundle = Platform.getBundle(IDEWorkbenchPlugin.IDE_WORKBENCH);
        declareWorkbenchImage(ideBundle, ISharedImages.IMG_TOOL_NEW_WIZARD,
                PATH_ETOOL + "new_wiz.gif", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                ISharedImages.IMG_TOOL_NEW_WIZARD_HOVER, PATH_ETOOL
                        + "new_wiz.gif", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                ISharedImages.IMG_TOOL_NEW_WIZARD_DISABLED, PATH_DTOOL
                        + "new_wiz.gif", true); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IWorkbenchGraphicConstants.IMG_ETOOL_IMPORT_WIZ, PATH_ETOOL
                        + "import_wiz.gif", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IWorkbenchGraphicConstants.IMG_ETOOL_EXPORT_WIZ, PATH_ETOOL
                        + "export_wiz.gif", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC, PATH_ETOOL
                        + "build_exec.gif", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC_HOVER,
                PATH_ETOOL + "build_exec.gif", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC_DISABLED,
                PATH_DTOOL + "build_exec.gif", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_ETOOL_SEARCH_SRC, PATH_ETOOL
                        + "search_src.gif", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_ETOOL_SEARCH_SRC_HOVER,
                PATH_ETOOL + "search_src.gif", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_ETOOL_SEARCH_SRC_DISABLED,
                PATH_DTOOL + "search_src.gif", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_ETOOL_NEXT_NAV, PATH_ETOOL
                        + "next_nav.gif", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_ETOOL_PREVIOUS_NAV, PATH_ETOOL
                        + "prev_nav.gif", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IWorkbenchGraphicConstants.IMG_WIZBAN_NEW_WIZ, PATH_WIZBAN
                        + "new_wiz.gif", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_NEWPRJ_WIZ, PATH_WIZBAN
                        + "newprj_wiz.gif", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_NEWFOLDER_WIZ,
                PATH_WIZBAN + "newfolder_wiz.gif", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_NEWFILE_WIZ, PATH_WIZBAN
                        + "newfile_wiz.gif", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IWorkbenchGraphicConstants.IMG_WIZBAN_IMPORT_WIZ, PATH_WIZBAN
                        + "import_wiz.gif", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_IMPORTDIR_WIZ,
                PATH_WIZBAN + "importdir_wiz.gif", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_IMPORTZIP_WIZ,
                PATH_WIZBAN + "importzip_wiz.gif", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IWorkbenchGraphicConstants.IMG_WIZBAN_EXPORT_WIZ, PATH_WIZBAN
                        + "export_wiz.gif", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_EXPORTDIR_WIZ,
                PATH_WIZBAN + "exportdir_wiz.gif", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_EXPORTZIP_WIZ,
                PATH_WIZBAN + "exportzip_wiz.gif", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_RESOURCEWORKINGSET_WIZ,
                PATH_WIZBAN + "workset_wiz.gif", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJ_PROJECT,
                PATH_OBJECT + "prj_obj.gif", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED, PATH_OBJECT
                        + "cprj_obj.gif", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OPEN_MARKER,
                PATH_ELOCALTOOL + "gotoobj_tsk.gif", true); //$NON-NLS-1$

        // task objects
        //declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_HPRIO_TSK, PATH_OBJECT+"hprio_tsk.gif");
        //declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_MPRIO_TSK, PATH_OBJECT+"mprio_tsk.gif");
        //declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_LPRIO_TSK, PATH_OBJECT+"lprio_tsk.gif");

        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJS_TASK_TSK,
                PATH_OBJECT + "taskmrk_tsk.gif", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJS_BKMRK_TSK,
                PATH_OBJECT + "bkmrk_tsk.gif", true); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_OBJS_COMPLETE_TSK, PATH_OBJECT
                        + "complete_tsk.gif", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_OBJS_INCOMPLETE_TSK, PATH_OBJECT
                        + "incomplete_tsk.gif", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_OBJS_WELCOME_ITEM, PATH_OBJECT
                        + "welcome_item.gif", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_OBJS_WELCOME_BANNER, PATH_OBJECT
                        + "welcome_banner.gif", true); //$NON-NLS-1$

        // synchronization indicator objects
        //declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_WBET_STAT, PATH_OVERLAY+"wbet_stat.gif");
        //declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_SBET_STAT, PATH_OVERLAY+"sbet_stat.gif");
        //declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_CONFLICT_STAT, PATH_OVERLAY+"conflict_stat.gif");

        // content locality indicator objects
        //declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_NOTLOCAL_STAT, PATH_STAT+"notlocal_stat.gif");
        //declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_LOCAL_STAT, PATH_STAT+"local_stat.gif");
        //declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_FILLLOCAL_STAT, PATH_STAT+"filllocal_stat.gif");
    }

    /**
     * Declares an IDE-specific workbench image.
     * 
     * @param symbolicName the symbolic name of the image
     * @param path the path of the image file; this path is relative to the base
     * of the IDE plug-in
     * @param shared <code>true</code> if this is a shared image, and
     * <code>false</code> if this is not a shared image
     * @see IWorkbenchConfigurer#declareImage
     */
    private void declareWorkbenchImage(Bundle ideBundle, String symbolicName,
            String path, boolean shared) {
        URL url = ideBundle.getEntry(path);
        ImageDescriptor desc = ImageDescriptor.createFromURL(url);
        getWorkbenchConfigurer().declareImage(symbolicName, desc, shared);
    }

    public void fillActionBars(IWorkbenchWindow window,
            IActionBarConfigurer actionConfigurer, int flags) {

        // setup the action builder to populate the toolbar and menubar in the configurer
        WorkbenchActionBuilder actionBuilder = null;
        IWorkbenchWindowConfigurer windowConfigurer = getWorkbenchConfigurer()
                .getWindowConfigurer(window);

        // For proxy calls to this method it is important that we use the same object
        // associated with the windowConfigurer
        actionBuilder = (WorkbenchActionBuilder) windowConfigurer
                .getData(ACTION_BUILDER);
        if (actionBuilder == null) {
            actionBuilder = new WorkbenchActionBuilder(window);
        }

        if ((flags & FILL_PROXY) != 0) {
            // Filling in fake actionbars
            if ((flags & FILL_MENU_BAR) != 0) {
                actionBuilder.populateMenuBar(actionConfigurer);
            }
            if ((flags & FILL_COOL_BAR) != 0) {
                actionBuilder.populateCoolBar(actionConfigurer);
            }
        } else {
            // make, fill, and hook listeners to action builder
            // reference to IWorkbenchConfigurer is need for the ABOUT action
            windowConfigurer.setData(ACTION_BUILDER, actionBuilder);
            actionBuilder.makeAndPopulateActions(getWorkbenchConfigurer(),
                    actionConfigurer);
        }

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor#getMainPreferencePageId
     */
    public String getMainPreferencePageId() {
        // indicate that we want the Workench preference page to be prominent
        return WORKBENCH_PREFERENCE_CATEGORY_ID;
    }

    /**
     * Tries to open the intro,if one exists and otherwise will open the legacy 
     * Welcome pages.
     * 
     * @see org.eclipse.ui.application.WorkbenchAdvisor#openIntro(org.eclipse.ui.application.IWorkbenchWindowConfigurer)
     */
    public void openIntro(IWorkbenchWindowConfigurer windowConfigurer) {
        if (editorsAndIntrosOpened)
            return;

        editorsAndIntrosOpened = true;

        // don't try to open the welcome editors if there is an intro
        if (hasIntro())
            super.openIntro(windowConfigurer);
        else {
            openWelcomeEditors(windowConfigurer.getWindow());
            // save any preferences changes caused by the above actions
            IDEWorkbenchPlugin.getDefault().savePluginPreferences();
        }
    }

    /**
     * Return true if the intro plugin is present and false otherwise.
     */
    private boolean hasIntro() {
        IWorkbenchConfigurer wc = getWorkbenchConfigurer();
        return wc == null ? false : wc.getWorkbench().getIntroManager()
                .hasIntro();
    }

    /**
     * Returns the workbench action builder for the given window 
     * @param window
     * @return WorkbenchActionBuilder
     */
    static WorkbenchActionBuilder getActionBuilder(IWorkbenchWindow window) {
        IWorkbenchWindowConfigurer configurer = workbenchAdvisor
                .getWorkbenchConfigurer().getWindowConfigurer(window);
        return (WorkbenchActionBuilder) configurer.getData("ActionBuilder"); //$NON-NLS-1$
    }
}