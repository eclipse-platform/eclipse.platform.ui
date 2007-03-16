/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.intro.IIntroConstants;
import org.eclipse.ui.internal.intro.IntroDescriptor;
import org.eclipse.ui.internal.intro.IntroMessages;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IntroContentDetector;

/**
 * Workbench implementation of the IIntroManager interface.
 * 
 * @since 3.0
 */
public class WorkbenchIntroManager implements IIntroManager {	
	
    private final Workbench workbench;

    /**
     * Create a new instance of the receiver.
     * 
     * @param workbench the workbench instance
     */
    WorkbenchIntroManager(Workbench workbench) {
        this.workbench = workbench;
        workbench.getExtensionTracker().registerHandler(new IExtensionChangeHandler(){
            
            /* (non-Javadoc)
             * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#addExtension(org.eclipse.core.runtime.dynamicHelpers.IExtensionTracker, org.eclipse.core.runtime.IExtension)
             */
            public void addExtension(IExtensionTracker tracker,IExtension extension) {
                //Do nothing
            }
            
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#removeExtension(org.eclipse.core.runtime.IExtension, java.lang.Object[])
			 */
			public void removeExtension(IExtension source, Object[] objects) {
                for (int i = 0; i < objects.length; i++) {
                    if (objects[i] instanceof IIntroPart) {
                        closeIntro((IIntroPart) objects[i]);
                    }
                }
				
			}}, null);
        
    }

    /**
     * The currently active introPart in this workspace, <code>null</code> if none.
     */
    private IIntroPart introPart;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbench#closeIntro(org.eclipse.ui.intro.IIntroPart)
     */
    public boolean closeIntro(IIntroPart part) {
        if (introPart == null || !introPart.equals(part)) {
			return false;
		}

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
        
		// if there is no part then null our reference
		introPart = null;
        
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbench#showIntro(org.eclipse.ui.IWorkbenchWindow)
     */
    public IIntroPart showIntro(IWorkbenchWindow preferredWindow,
            boolean standby) {
        if (preferredWindow == null) {
			preferredWindow = this.workbench.getActiveWorkbenchWindow();
		}

        if (preferredWindow == null) {
			return null;
		}

        ViewIntroAdapterPart viewPart = getViewIntroAdapterPart();
        if (viewPart == null) {
            createIntro(preferredWindow);
        } else {
            try {
                IWorkbenchPage page = viewPart.getSite().getPage();
                IWorkbenchWindow window = page.getWorkbenchWindow();
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
     * @param testWindow the window to test
     * @return whether the intro exists in the given window
     */
    /*package*/boolean isIntroInWindow(IWorkbenchWindow testWindow) {
        ViewIntroAdapterPart viewPart = getViewIntroAdapterPart();
        if (viewPart == null) {
			return false;
		}

        IWorkbenchWindow window = viewPart.getSite().getWorkbenchWindow();
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
    private void createIntro(IWorkbenchWindow preferredWindow) {
        if (this.workbench.getIntroDescriptor() == null) {
			return;
		}

        IWorkbenchPage workbenchPage = preferredWindow.getActivePage();
        if (workbenchPage == null) {
			return;
		}
        try {
            workbenchPage.showView(IIntroConstants.INTRO_VIEW_ID);
        } catch (PartInitException e) {
            WorkbenchPlugin
                    .log(
                            IntroMessages.Intro_could_not_create_part, new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR, IntroMessages.Intro_could_not_create_part, e));
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbench#setIntroStandby(org.eclipse.ui.intro.IIntroPart, boolean)
     */
    public void setIntroStandby(IIntroPart part, boolean standby) {
        if (introPart == null || !introPart.equals(part)) {
			return;
		}

        ViewIntroAdapterPart viewIntroAdapterPart = getViewIntroAdapterPart();
        if (viewIntroAdapterPart == null) {
			return;
		}

        PartPane pane = ((PartSite) viewIntroAdapterPart.getSite()).getPane();
        if (standby == !pane.isZoomed()) {
            // the zoom state is already correct - just update the part's state.
            viewIntroAdapterPart.setStandby(standby);
            return;
        }

        viewIntroAdapterPart.getSite().getPage().toggleZoom(
				pane.getPartReference());
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#isIntroStandby(org.eclipse.ui.intro.IIntroPart)
	 */
    public boolean isIntroStandby(IIntroPart part) {
        if (introPart == null || !introPart.equals(part)) {
			return false;
		}

        ViewIntroAdapterPart viewIntroAdapterPart = getViewIntroAdapterPart();
        if (viewIntroAdapterPart == null) {
			return false;
		}

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
            IPerspectiveDescriptor[] perspDescs = page.getOpenPerspectives();
            for (int j = 0; j < perspDescs.length; j++) {
                IPerspectiveDescriptor descriptor = perspDescs[j];
                IViewReference reference = page.findPerspective(descriptor)
                        .findView(IIntroConstants.INTRO_VIEW_ID);
                if (reference != null) {
                    IViewPart part = reference.getView(false);
                    if (part != null && part instanceof ViewIntroAdapterPart) {
						return (ViewIntroAdapterPart) part;
					}
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
        	workbench.getExtensionTracker().registerObject(
					introDescriptor.getConfigurationElement()
							.getDeclaringExtension(), introPart,
					IExtensionTracker.REF_WEAK);
        }
    	return introPart;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbench#hasIntro()
     */
    public boolean hasIntro() {
        return workbench.getIntroDescriptor() != null;
    }
    
    public boolean isNewContentAvailable() {
		IntroDescriptor introDescriptor = workbench.getIntroDescriptor();
		if (introDescriptor == null) {
			return false;
		}
		try {
			IntroContentDetector contentDetector = introDescriptor
					.getIntroContentDetector();
			if (contentDetector != null) {
				return contentDetector.isNewContentAvailable();
			}
		} catch (CoreException ex) {
			WorkbenchPlugin.log(new Status(IStatus.WARNING,
					WorkbenchPlugin.PI_WORKBENCH, IStatus.WARNING,
					"Could not load intro content detector", ex)); //$NON-NLS-1$
		}
		return false;
	}
}
