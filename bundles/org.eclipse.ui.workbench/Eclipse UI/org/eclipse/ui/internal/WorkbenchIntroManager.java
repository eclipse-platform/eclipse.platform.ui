/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.intro.IIntroConstants;
import org.eclipse.ui.internal.intro.IntroDescriptor;
import org.eclipse.ui.internal.intro.IntroMessages;
import org.eclipse.ui.internal.registry.experimental.ConfigurationElementTracker;
import org.eclipse.ui.internal.registry.experimental.IConfigurationElementRemovalHandler;
import org.eclipse.ui.internal.registry.experimental.IConfigurationElementTracker;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;

/**
 * Workbench implementation of the IIntroManager interface.
 * 
 * @since 3.0
 */
public class WorkbenchIntroManager implements IIntroManager {

	private final IConfigurationElementTracker tracker = new ConfigurationElementTracker();
	
    private final Workbench workbench;

    /**
     * Create a new instance of the receiver.
     * 
     * @param workbench the workbench instance
     */
    WorkbenchIntroManager(Workbench workbench) {
        this.workbench = workbench;
        tracker.registerRemovalHandler(new IConfigurationElementRemovalHandler(){

			public void removeInstance(IConfigurationElement source, Object object) {
				closeIntro((IIntroPart) object);				
			}});
    }

    /**
     * The currently active introPart in this workspace, <code>null</code> if none.
     */
    private IIntroPart introPart;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbench#closeIntro(org.eclipse.ui.intro.IIntroPart)
     */
    public boolean closeIntro(IIntroPart part) {
        if (introPart == null || !introPart.equals(part))
            return false;

        IViewPart introView = getViewIntroAdapterPart();
        if (introView != null) {
            //assumption is that there is only ever one intro per workbench
            //if we ever support one per window then this will need revisiting
            IWorkbenchPage page = introView.getSite().getPage();
            IViewReference reference = page
                    .findViewReference(IIntroConstants.INTRO_VIEW_ID);
            page.hideView(introView);
            if (reference == null || reference.getPart(false) == null) {
                introPart = null;                
                return true;
            }
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbench#showIntro(org.eclipse.ui.IWorkbenchWindow)
     */
    public IIntroPart showIntro(IWorkbenchWindow preferredWindow,
            boolean standby) {
        if (preferredWindow == null)
            preferredWindow = this.workbench.getActiveWorkbenchWindow();

        if (preferredWindow == null)
            return null;

        if (getViewIntroAdapterPart() == null) {
            createIntro((WorkbenchWindow) preferredWindow);
        } else {
            try {
                ViewIntroAdapterPart viewPart = getViewIntroAdapterPart();
                WorkbenchPage page = (WorkbenchPage) viewPart.getSite()
                        .getPage();
                WorkbenchWindow window = (WorkbenchWindow) page
                        .getWorkbenchWindow();
                if (!window.equals(preferredWindow)) {
                    window.getShell().setActive();
                }

                page.showView(IIntroConstants.INTRO_VIEW_ID);
            } catch (PartInitException e) {
                WorkbenchPlugin
                        .log(
                                "Could not open intro", new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR, "Could not open intro", e)); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        setIntroStandby(introPart, standby);
        return introPart;
    }

    /**	 
     * @param window the window to test
     * @return whether the intro exists in the given window
     */
    /*package*/boolean isIntroInWindow(IWorkbenchWindow testWindow) {
        ViewIntroAdapterPart viewPart = getViewIntroAdapterPart();
        if (viewPart == null)
            return false;

        WorkbenchPage page = (WorkbenchPage) viewPart.getSite().getPage();
        WorkbenchWindow window = (WorkbenchWindow) page.getWorkbenchWindow();
        if (window.equals(testWindow)) {
            return true;
        }
        return false;
    }

    /**
     * Create a new Intro area (a view, currently) in the provided window.  If there is no intro
     * descriptor for this workbench then no work is done.
     *
     * @param preferredWindow the window to create the intro in.
     */
    private void createIntro(WorkbenchWindow preferredWindow) {
        if (this.workbench.getIntroDescriptor() == null)
            return;

        WorkbenchPage workbenchPage = preferredWindow.getActiveWorkbenchPage();
        if (workbenchPage == null)
            return;
        try {
            workbenchPage.showView(IIntroConstants.INTRO_VIEW_ID);
        } catch (PartInitException e) {
            WorkbenchPlugin
                    .log(
                            IntroMessages
                                    .getString("Intro.could_not_create_part"), new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR, IntroMessages.getString("Intro.could_not_create_part"), e)); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbench#setIntroStandby(org.eclipse.ui.intro.IIntroPart, boolean)
     */
    public void setIntroStandby(IIntroPart part, boolean standby) {
        if (introPart == null || !introPart.equals(part))
            return;

        ViewIntroAdapterPart viewIntroAdapterPart = getViewIntroAdapterPart();
        if (viewIntroAdapterPart == null)
            return;

        PartPane pane = ((PartSite) viewIntroAdapterPart.getSite()).getPane();
        if (standby == !pane.isZoomed()) {
            // the zoom state is already correct - just update the part's state.
            viewIntroAdapterPart.setStandby(standby);
            return;
        }

        ((WorkbenchPage) viewIntroAdapterPart.getSite().getPage())
                .toggleZoom(pane.getPartReference());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbench#isIntroStandby(org.eclipse.ui.intro.IIntroPart)
     */
    public boolean isIntroStandby(IIntroPart part) {
        if (introPart == null || !introPart.equals(part))
            return false;

        ViewIntroAdapterPart viewIntroAdapterPart = getViewIntroAdapterPart();
        if (viewIntroAdapterPart == null)
            return false;

        return !((PartSite) viewIntroAdapterPart.getSite()).getPane()
                .isZoomed();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbench#findIntro()
     */
    public IIntroPart getIntro() {
        return introPart;
    }

    /** 
     * @return the <code>ViewIntroAdapterPart</code> for this workbench, <code>null</code> if it 
     * cannot be found.
     */
    /*package*/ViewIntroAdapterPart getViewIntroAdapterPart() {
        IWorkbenchWindow[] windows = this.workbench.getWorkbenchWindows();
        for (int i = 0; i < windows.length; i++) {
            IWorkbenchWindow window = windows[i];
            WorkbenchPage page = (WorkbenchPage) window.getActivePage();
            if (page == null) {
                continue;
            }
            IPerspectiveDescriptor[] perspDescs = page.getOpenedPerspectives();
            for (int j = 0; j < perspDescs.length; j++) {
                IPerspectiveDescriptor descriptor = perspDescs[j];
                IViewReference reference = page.findPerspective(descriptor)
                        .findView(IIntroConstants.INTRO_VIEW_ID);
                if (reference != null) {
                    IViewPart part = reference.getView(false);
                    if (part != null && part instanceof ViewIntroAdapterPart)
                        return (ViewIntroAdapterPart) part;
                }
            }
        }
        return null;
    }

    /**
     * @return a new IIntroPart.  This has the side effect of setting the introPart field to the new
     * value.
     */
    /*package*/IIntroPart createNewIntroPart() throws CoreException {
        IntroDescriptor introDescriptor = workbench.getIntroDescriptor();
		introPart = introDescriptor == null ? null
                : introDescriptor.createIntro();
        if (introPart != null) {
        	tracker.registerObject(introDescriptor.getConfigurationElement(), introPart);
        }
    	return introPart;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbench#hasIntro()
     */
    public boolean hasIntro() {
        return workbench.getIntroDescriptor() != null;
    }
}