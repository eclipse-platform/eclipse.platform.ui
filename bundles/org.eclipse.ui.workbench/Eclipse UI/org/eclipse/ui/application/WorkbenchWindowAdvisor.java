/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.application;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.WorkbenchWindowConfigurer;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * Public base class for configuring a workbench window.
 * <p>
 * The workbench window advisor object is created in response to a workbench
 * window being created (one per window), and is used to configure the window.
 * </p>
 * <p>
 * An application should declare a subclass of <code>WorkbenchWindowAdvisor</code>
 * and override methods to configure workbench windows to suit the needs of the
 * particular application.
 * </p>
 * <p>
 * The following advisor methods are called at strategic points in the
 * workbench window's lifecycle (as with the workbench advisor, all occur 
 * within the dynamic scope of the call to 
 * {@link PlatformUI#createAndRunWorkbench PlatformUI.createAndRunWorkbench}):
 * <ul>
 * <li><code>preWindowOpen</code> - called as the window is being opened; 
 *  use to configure aspects of the window other than actions bars</li>
 * <li><code>postWindowRestore</code> - called after the window has been
 * recreated from a previously saved state; use to adjust the restored
 * window</li>
 * <li><code>postWindowCreate</code> -  called after the window has been created,
 * either from an initial state or from a restored state;  used to adjust the
 * window</li>
 * <li><code>openIntro</code> - called immediately before the window is opened in
 * order to create the introduction component, if any.</li>
 * <li><code>postWindowOpen</code> - called after the window has been
 * opened; use to hook window listeners, etc.</li>
 * <li><code>preWindowShellClose</code> - called when the window's shell
 * is closed by the user; use to pre-screen window closings</li>
 * </ul>
 * </p>
 * 
 * @since 3.1
 */
public class WorkbenchWindowAdvisor {

    private IWorkbenchWindowConfigurer windowConfigurer;

// TODO: Will be needed when the window advisor is created via extension point     
//    /**
//     * Creates a new workbench window advisor for configuring a 
//     * workbench window.
//     * 
//     * @see #initialize(IWorkbenchWindowConfigurer)
//     */
//    public WorkbenchWindowAdvisor() {
//    }
//    
//    /**
//     * Initializes the workbench window advisor, and remembers the given
//     * workbench window configurer.
//     * It can be obtained later via <code>getWindowConfigurer()</code>. 
//     * <p>
//     * This method is called during workbench window initialization prior to any
//     * windows being opened. 
//     * Clients must not call this method directly.
//     * Clients should subclass <code>preWindowOpen</code> for any further
//     * configuration needed before the window opens.
//     * </p>
//     * 
//     * @param configurer an object for configuring the workbench
//     */
//    public final void initialize(IWorkbenchWindowConfigurer configurer) {
//        Assert.isNotNull(configurer);
//        if (windowConfigurer != null) {
//            throw new IllegalStateException();
//        }
//        this.windowConfigurer = configurer;
//    }
//

    /**
     * Creates a new workbench window advisor for configuring a workbench
     * window via the given workbench window configurer.
     * 
     * @param configurer an object for configuring the workbench window
     */
    public WorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        Assert.isNotNull(configurer);
        this.windowConfigurer = configurer;
    }

    /**
     * Returns the workbench window configurer.
     * 
     * @return the workbench window configurer
     */
    protected IWorkbenchWindowConfigurer getWindowConfigurer() {
        return windowConfigurer;
    }
    
    /**
     * Performs arbitrary actions before the window is opened.
     * <p>
     * This method is called before the window's controls have been created.
     * Clients must not call this method directly (although super calls are okay).
     * The default implementation does nothing. Subclasses may override.
     * Typical clients will use the window configurer to tweak the
     * workbench window in an application-specific way; however, filling the
     * window's menu bar, tool bar, and status line must be done in 
     * {@link ActionBarAdvisor#fillActionBars}, which is called immediately
     * after this method is called.
     * </p>
     * TODO: pass in initial page input from openWindow call
     */
    public void preWindowOpen() {
        // do nothing
    }

    /**
     * Creates a new action bar advisor to configure the action bars of the window
     * via the given action bar configurer.
     * The default implementation returns a new instance of {@link ActionBarAdvisor}.
     * 
     * @param configurer the action bar configurer for the window
     * @return the action bar advisor for the window
     */
    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ActionBarAdvisor(configurer);
    }
    
    /**
     * Performs arbitrary actions after the window has been restored, 
     * but before it is opened.
     * <p>
     * This method is called after a previously-saved window has been
     * recreated. This method is not called when a new window is created from
     * scratch. This method is never called when a workbench is started for the
     * very first time, or when workbench state is not saved or restored.
     * Clients must not call this method directly (although super calls are okay).
     * The default implementation does nothing. Subclasses may override.
     * It is okay to call <code>IWorkbench.close()</code> from this method.
     * </p>
     * 
     * @exception WorkbenchException thrown if there are any errors to report
     *   from post-restoration of the window
     */
    public void postWindowRestore() throws WorkbenchException {
        // do nothing
    }

    /**
     * Opens the introduction componenet.  
     * <p>
     * Clients must not call this method directly (although super calls are okay).
     * The default implementation opens the intro in the first window provided
     * if the preference IWorkbenchPreferences.SHOW_INTRO is <code>true</code>.  If 
     * an intro is shown then this preference will be set to <code>false</code>.  
     * Subsequently, and intro will be shown only if 
     * <code>WorkbenchConfigurer.getSaveAndRestore()</code> returns 
     * <code>true</code> and the introduction was visible on last shutdown.  
     * Subclasses may override.
     * </p>
     * 
     * TODO: Refactor this into an IIntroManager.openIntro(IWorkbenchWindow) call
     */
    public void openIntro() {
        // introOpened flag needs to be global
        IWorkbenchConfigurer wbConfig = getWindowConfigurer().getWorkbenchConfigurer();
        final String key = "introOpened"; //$NON-NLS-1$
        Boolean introOpened = (Boolean) wbConfig.getData(key);
        if (introOpened != null && introOpened.booleanValue())
            return;

        wbConfig.setData(key, Boolean.TRUE);

        boolean showIntro = PrefUtil.getAPIPreferenceStore().getBoolean(
                IWorkbenchPreferenceConstants.SHOW_INTRO);

        if (!showIntro)
            return;

        if (wbConfig.getWorkbench().getIntroManager()
                .hasIntro()) {
            wbConfig.getWorkbench().getIntroManager()
                    .showIntro(getWindowConfigurer().getWindow(), false);

            PrefUtil.getAPIPreferenceStore().setValue(
                    IWorkbenchPreferenceConstants.SHOW_INTRO, false);
            PrefUtil.saveAPIPrefs();
        }
    }

    /**
     * Performs arbitrary actions after the window has been created (possibly 
     * after being restored), but has not yet been opened.
     * <p>
     * This method is called after the window has been created from scratch, 
     * or when it has been restored from a previously-saved window.  In the latter case,
     * this method is called after <code>postWindowRestore</code>.
     * Clients must not call this method directly (although super calls are okay).
     * The default implementation does nothing. Subclasses may override.
     * </p>
     */
    public void postWindowCreate() {
        // do nothing
    }

    /**
     * Performs arbitrary actions after the window has been opened (possibly 
     * after being restored).
     * <p>
     * This method is called after the window has been opened. This method is 
     * called after the window has been created from scratch, or when
     * it has been restored from a previously-saved window.
     * Clients must not call this method directly (although super calls are okay).
     * The default implementation does nothing. Subclasses may override.
     * </p>
     */
    public void postWindowOpen() {
        // do nothing
    }

    /**
     * Performs arbitrary actions as the window's shell is being closed
     * directly, and possibly veto the close.
     * <p>
     * This method is called from a ShellListener associated with the window. It
     * is not called when the window is being closed for other reasons. Clients
     * must not call this method directly (although super calls are okay). If
     * this method returns <code>false</code>, then the user's request to
     * close the shell is ignored. This gives the workbench advisor an
     * opportunity to query the user and/or veto the closing of a window under
     * some circumstances.
     * </p>
     * 
     * @return <code>true</code> to allow the window to close, and
     *         <code>false</code> to prevent the window from closing
     * @see org.eclipse.ui.IWorkbenchWindow#close
     */
    public boolean preWindowShellClose() {
        // do nothing, but allow the close() to proceed
        return true;
    }

    /**
     * Performs arbitrary actions after the window is closed.
     * <p>
     * This method is called after the window's controls have been disposed.
     * Clients must not call this method directly (although super calls are okay).
     * The default implementation does nothing. Subclasses may override.
     * </p>
     */
    public void postWindowClose() {
        // do nothing
    }

//  TODO:  Should getDefaultPageInput be added to allow different windows 
//  to have different default page inputs?
//    /**
//     * Returns the default input for workbench pages that are newly
//     * created within this window.
//     * <p>
//     * The default implementation returns <code>null</code>.
//     * Subclasses may override.
//     * </p>
//     * 
//     * @return the default input for a new workbench page, or
//     * <code>null</code> if none
//     */
//    public IAdaptable getDefaultPageInput() {
//        // default: no input
//        return null;
//    }

//  TODO:  Should getDefaultPerspectiveId be added to allow different windows 
//  to have different default perspectives?
//    /**
//     * Returns the id of the perspective to use if not explicitly specified by
//     * a caller opening a new window.
//     * Returns <code>null</code> if no perspective should be shown.
//     * <p>
//     * This method is called during startup when the workbench is creating 
//     * the first new window, and when opening a window via 
//     * {@link org.eclipse.ui.IWorkbench#openWorkbenchWindow(org.eclipse.core.runtime.IAdaptable)}.
//     * The default implementation returns <code>null</code>.
//     * Subclasses may override.
//     * </p>
//     * <p>
//     * If the {@link IWorkbenchPreferenceConstants#DEFAULT_PERSPECTIVE_ID} preference
//     * is specified, it supercedes the perspective specified here.
//     * </p>
//     * 
//     * @return the id of the perspective to use by default
//     */
//    public String getDefaultPerspectiveId() {
//        return null;
//    }

//  TODO:  Should getMainPreferencePageId be added to allow different windows 
//    to organize prefs differently?
//    /**
//     * Returns the id of the preference page that should be presented most
//     * prominently.
//     * <p>
//     * The default implementation returns <code>null</code>. 
//     * Subclasses may override.
//     * </p>
//     * 
//     * @return the id of the preference page, or <code>null</code> if none
//     */
//    public String getMainPreferencePageId() {
//        // default: no opinion
//        return null;
//    }
//
    
    /**
     * Creates the contents of the window.
     * <p>
     * The default implementation adds a menu bar, a cool bar, a status line, 
     * a perspective bar, and a fast view bar.  The visibility of these controls
     * can be configured using the <code>setShow*</code> methods on
     * <code>IWorkbenchWindowConfigurer</code>.
     * </p>
     * <p>
     * Subclasses may override to define custom window contents and layout,
     * but must call <code>IWorkbenchWindowConfigurer.createPageComposite</code>.
     * </p> 
     * 
     * @param shell the window's shell
     * @see IWorkbenchWindowConfigurer#createMenuBar
     * @see IWorkbenchWindowConfigurer#createCoolBarControl
     * @see IWorkbenchWindowConfigurer#createStatusLineControl
     * @see IWorkbenchWindowConfigurer#createPageComposite
     */
    public void createWindowContents(Shell shell) {
        ((WorkbenchWindowConfigurer) getWindowConfigurer()).createDefaultContents(shell);
    }

    /**
     * Creates and returns the control to be shown when the window has no open pages.
     * If <code>null</code> is returned, the default window background is shown.
     * <p>
     * The default implementation returns <code>null</code>.
     * Subclasses may override.
     * </p>
     * 
     * @param parent the parent composite
     * @return the control or <code>null</code>
     */
    public Control createEmptyWindowContents(Composite parent) {
        return null;
    }

    /**
     * Disposes any resources allocated by this window advisor.
     * This is the last method called on this window advisor by the workbench.
     * The default implementation does nothing.
     * Subclasses may extend.
     */
    public void dispose() {
        // do nothing.
    }
    
}
