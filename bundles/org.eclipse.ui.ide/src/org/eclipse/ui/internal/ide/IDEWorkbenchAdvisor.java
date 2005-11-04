/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.model.WorkbenchAdapterBuilder;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;
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
     * Array of <code>AboutInfo</code> for all new installed
     * features that specify a welcome perspective.
     */
    private AboutInfo[] welcomePerspectiveInfos = null;

    /**
     * Helper for managing activites in response to workspace changes.
     */
    private IDEWorkbenchActivityHelper activityHelper = null;
    
    /**
     * Helper for managing work that is performed when the system is
     * otherwise idle.
     */
    private IDEIdleHelper idleHelper;

	/**
     * Creates a new workbench advisor instance.
     */
    public IDEWorkbenchAdvisor() {
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
            	String name = null;
            	if (cmdLineArgs.length > i + 1)
            		name = cmdLineArgs[i+1];
            	if (name != null && name.indexOf("-") == -1)		//$NON-NLS-1$
            		workspaceLocation = name;
            	else
            		workspaceLocation = Platform.getLocation().toOSString();
                break;
            }
        }
        
        // register shared images
        declareWorkbenchImages();

        // initialize the activity helper
        activityHelper = IDEWorkbenchActivityHelper.getInstance();

        //initialize idle handler
        idleHelper = new IDEIdleHelper(configurer);
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor#preStartup()
     */
    public void preStartup() {

        //Suspend background jobs while we startup
        Platform.getJobManager().suspend();

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
        if (idleHelper != null) {
        	idleHelper.shutdown();
        	idleHelper = null;
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

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor#createWorkbenchWindowAdvisor(org.eclipse.ui.application.IWorkbenchWindowConfigurer)
     */
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return  new IDEWorkbenchWindowAdvisor(this, configurer);
    }
    
    /**
     * Return true if the intro plugin is present and false otherwise.
     */
    public boolean hasIntro() {
        return getWorkbenchConfigurer().getWorkbench().getIntroManager()
                .hasIntro();
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
                ErrorDialog.openError(shell, IDEWorkbenchMessages.Workspace_problemsTitle,
                        IDEWorkbenchMessages.Workspace_problemMessage,
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
                IDEWorkbenchPlugin.IDE_WORKBENCH, 1, IDEWorkbenchMessages.ProblemSavingWorkbench, null);
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
                            IDEWorkbenchMessages.InternalError, e.getTargetException()));
        } catch (InterruptedException e) {
            status.merge(new Status(IStatus.ERROR,
                    IDEWorkbenchPlugin.IDE_WORKBENCH, 1, IDEWorkbenchMessages.InternalError, e));
        }
        ErrorDialog.openError(null, IDEWorkbenchMessages.ProblemsSavingWorkspace,
                null, status, IStatus.ERROR | IStatus.WARNING);
        if (!status.isOK()) {
            IDEWorkbenchPlugin.log(IDEWorkbenchMessages.ProblemsSavingWorkspace, status);
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
        AboutInfo[] welcomeInfos = getWelcomePerspectiveInfos();
        if (index >= 0 && welcomeInfos != null
                && index < welcomeInfos.length) {
            perspectiveId = welcomeInfos[index].getWelcomePerspectiveId();
        }
        if (perspectiveId == null) {
            perspectiveId = IDE.RESOURCE_PERSPECTIVE_ID;
        }
        return perspectiveId;
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
    public Map getNewlyAddedBundleGroups() {
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

    /**
     * Declares all IDE-specific workbench images. This includes both "shared"
     * images (named in {@link IDE.SharedImages}) and
     * internal images (named in
     * {@link org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages}).
     * 
     * @see IWorkbenchConfigurer#declareImage
     */
    private void declareWorkbenchImages() {

        final String ICONS_PATH = "$nl$/icons/full/";//$NON-NLS-1$
        final String PATH_ELOCALTOOL = ICONS_PATH + "elcl16/"; //Enabled toolbar icons.//$NON-NLS-1$
        final String PATH_ETOOL = ICONS_PATH + "etool16/"; //Enabled toolbar icons.//$NON-NLS-1$
        final String PATH_DTOOL = ICONS_PATH + "dtool16/"; //Disabled toolbar icons.//$NON-NLS-1$
        final String PATH_OBJECT = ICONS_PATH + "obj16/"; //Model object icons//$NON-NLS-1$
        final String PATH_WIZBAN = ICONS_PATH + "wizban/"; //Wizard icons//$NON-NLS-1$

        Bundle ideBundle = Platform.getBundle(IDEWorkbenchPlugin.IDE_WORKBENCH);

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
                IDEInternalWorkbenchImages.IMG_WIZBAN_NEWPRJ_WIZ, PATH_WIZBAN
                        + "newprj_wiz.gif", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_NEWFOLDER_WIZ,
                PATH_WIZBAN + "newfolder_wiz.gif", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_NEWFILE_WIZ, PATH_WIZBAN
                        + "newfile_wiz.gif", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_IMPORTDIR_WIZ,
                PATH_WIZBAN + "importdir_wiz.gif", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_IMPORTZIP_WIZ,
                PATH_WIZBAN + "importzip_wiz.gif", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_EXPORTDIR_WIZ,
                PATH_WIZBAN + "exportdir_wiz.gif", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_EXPORTZIP_WIZ,
                PATH_WIZBAN + "exportzip_wiz.gif", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_RESOURCEWORKINGSET_WIZ,
                PATH_WIZBAN + "workset_wiz.gif", false); //$NON-NLS-1$
        
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_DLGBAN_SAVEAS_DLG,
                PATH_WIZBAN + "saveas_wiz.gif", false); //$NON-NLS-1$

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
        
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_LCL_FLAT_LAYOUT, PATH_ELOCALTOOL
                        + "flatLayout.gif", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_LCL_HIERARCHICAL_LAYOUT, PATH_ELOCALTOOL
                        + "hierarchicalLayout.gif", true); //$NON-NLS-1$

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
		URL url = Platform.find(ideBundle, new Path(path));
        ImageDescriptor desc = ImageDescriptor.createFromURL(url);
        getWorkbenchConfigurer().declareImage(symbolicName, desc, shared);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor#getMainPreferencePageId
     */
    public String getMainPreferencePageId() {
        // indicate that we want the Workench preference page to be prominent
        return WORKBENCH_PREFERENCE_CATEGORY_ID;
    }
    
    /**
     * @return the workspace location string, or <code>null</code> if
     * the location is not being shown
     */
    public String getWorkspaceLocation() {
        return workspaceLocation;
    }

    /**
     * @return the welcome perspective infos, or <code>null</code> if none
     * or if they should be ignored due to the new intro being present
     */
    public AboutInfo[] getWelcomePerspectiveInfos() {
        if (welcomePerspectiveInfos == null) {
            // support old welcome perspectives if intro plugin is not present
            if (!hasIntro()) {
                Map m = getNewlyAddedBundleGroups();
                ArrayList list = new ArrayList(m.size());
                for (Iterator i = m.values().iterator(); i.hasNext();) {
                    AboutInfo info = (AboutInfo) i.next();
                    if (info != null && info.getWelcomePerspectiveId() != null
                            && info.getWelcomePageURL() != null)
                        list.add(info);
                }
                welcomePerspectiveInfos = new AboutInfo[list.size()];
                list.toArray(welcomePerspectiveInfos);
            }
        }
        return welcomePerspectiveInfos;
    }
}
