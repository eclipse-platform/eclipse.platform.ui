/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IIdentifier;
import org.eclipse.ui.activities.IIdentifierListener;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.activities.IdentifierEvent;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * Helper methods that the internal layout classes (<code>PageLayout</code> and 
 * <code>FolderLayout</code>) utilize for activities support and view creation.
 * 
 * @since 3.0
 */
class LayoutHelper {

    /**
     * Not intended to be instantiated.
     */
    private LayoutHelper() {
        //no-op
    }

    /**
     * Creates a series of listeners that will activate the provided view on the
     * provided page layout when <code>IIdenfier</code> enablement changes. The 
     * rules for this activation are as follows: <p>
     * <ul>
     * <li> if the identifier becomes enabled and the perspective of the page 
     * layout is the currently active perspective in its window, then activate 
     * the views immediately.
     * <li> if the identifier becomes enabled and the perspective of the page 
     * layout is not the currently active perspecitve in its window, then add an
     * <code>IPerspectiveListener</code> to the window and activate the views 
     * when the perspective becomes active. 
     * 
     * @param pageLayout <code>PageLayout</code>.
     * @param viewId the view id to activate upon <code>IIdentifier</code> enablement.
     */
    public static final void addViewActivator(PageLayout pageLayout,
            final String viewId) {
        if (viewId == null) {
			return;
		}

        ViewFactory viewFactory = pageLayout.getViewFactory();

        final IWorkbenchPage partPage = viewFactory.getWorkbenchPage();
        if (partPage == null) {
			return;
		}

        final IPerspectiveDescriptor partPerspective = pageLayout
                .getDescriptor();

        IWorkbenchActivitySupport support = PlatformUI.getWorkbench()
                .getActivitySupport();

        IViewDescriptor descriptor = viewFactory.getViewRegistry().find(viewId);
        if (!(descriptor instanceof IPluginContribution)) {
			return;
		}

        IIdentifier identifier = support.getActivityManager().getIdentifier(
                WorkbenchActivityHelper
                        .createUnifiedId((IPluginContribution) descriptor));

        identifier.addIdentifierListener(new IIdentifierListener() {

            /* (non-Javadoc)
             * @see org.eclipse.ui.activities.IIdentifierListener#identifierChanged(org.eclipse.ui.activities.IdentifierEvent)
             */
            public void identifierChanged(IdentifierEvent identifierEvent) {
                if (identifierEvent.hasEnabledChanged()) {
                    IIdentifier thisIdentifier = identifierEvent
                            .getIdentifier();
                    if (thisIdentifier.isEnabled()) {
                        // show view
                        thisIdentifier.removeIdentifierListener(this);
                        IWorkbenchPage activePage = partPage
                                .getWorkbenchWindow().getActivePage();
                        if (partPage == activePage
                                && partPerspective == activePage
                                        .getPerspective()) {
                            // show immediately.
                            try {
                                partPage.showView(viewId);
                            } catch (PartInitException e) {
                                WorkbenchPlugin.log(getClass(), "identifierChanged", e); //$NON-NLS-1$
                            }
                        } else { // show when the perspective becomes active							
                            partPage.getWorkbenchWindow()
                                    .addPerspectiveListener(
                                            new IPerspectiveListener() {

                                                /* (non-Javadoc)
                                                 * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
                                                 */
                                                public void perspectiveActivated(
                                                        IWorkbenchPage page,
                                                        IPerspectiveDescriptor newPerspective) {
                                                    if (partPerspective == newPerspective) {
                                                        partPage
                                                                .getWorkbenchWindow()
                                                                .removePerspectiveListener(
                                                                        this);
                                                        try {
                                                            page
                                                                    .showView(viewId);
                                                        } catch (PartInitException e) {
                                                            WorkbenchPlugin.log(getClass(), "perspectiveActivated", e); //$NON-NLS-1$
                                                        }
                                                    }
                                                }

                                                /* (non-Javadoc)
                                                 * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, java.lang.String)
                                                 */
                                                public void perspectiveChanged(
                                                        IWorkbenchPage page,
                                                        IPerspectiveDescriptor perspective,
                                                        String changeId) {
                                                    // no-op
                                                }
                                            });
                        }
                    }
                }
            }
        });
    }

    /**
     * Create the view.  If it's already been been created in the provided 
     * factory, return the shared instance.
     * 
     * @param factory the <code>ViewFactory</code> to use.
     * @param viewID the view id to use.
     * @return the new <code>ViewPane</code>.
     * @throws PartInitException thrown if there is a problem creating the view.
     */
    public static final ViewPane createView(ViewFactory factory, String viewId)
            throws PartInitException {
        WorkbenchPartReference ref = (WorkbenchPartReference) factory
				.createView(ViewFactory.extractPrimaryId(viewId), ViewFactory
						.extractSecondaryId(viewId));
        ViewPane newPart = (ViewPane) ref.getPane();
        return newPart;
    }

}
