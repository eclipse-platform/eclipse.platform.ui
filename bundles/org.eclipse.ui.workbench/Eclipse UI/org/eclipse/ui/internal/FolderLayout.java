/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Chris Gross chris.gross@us.ibm.com Bug 107443
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * This layout is used to define the initial set of views and placeholders
 * in a folder.
 * <p>
 * Views are added to the folder by ID. This id is used to identify 
 * a view descriptor in the view registry, and this descriptor is used to 
 * instantiate the <code>IViewPart</code>.
 * </p>
 */
public class FolderLayout implements IFolderLayout {
    private ViewStack folder;

    private PageLayout pageLayout;

    private ViewFactory viewFactory;

    /**
     * Create an instance of a <code>FolderLayout</code> belonging to a 
     * <code>PageLayout</code>.
     */
    public FolderLayout(PageLayout pageLayout, ViewStack folder,
            ViewFactory viewFactory) {
        super();
        this.folder = folder;
        this.viewFactory = viewFactory;
        this.pageLayout = pageLayout;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPlaceholderFolderLayout#addPlaceholder(java.lang.String)
     */
    public void addPlaceholder(String viewId) {
        if (!pageLayout.checkValidPlaceholderId(viewId)) {
            return;
        }

        // Create the placeholder.
        PartPlaceholder newPart = new PartPlaceholder(viewId);
        linkPartToPageLayout(viewId, newPart);

        // Add it to the folder layout.
        folder.add(newPart);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IFolderLayout#addView(java.lang.String)
     */
    public void addView(String viewId) {
        if (pageLayout.checkPartInLayout(viewId)) {
			return;
		}

        try {
            IViewDescriptor descriptor = viewFactory.getViewRegistry().find(
                    ViewFactory.extractPrimaryId(viewId));
            if (descriptor == null) {
                throw new PartInitException("View descriptor not found: " + viewId); //$NON-NLS-1$
            }
            if (WorkbenchActivityHelper.filterItem(descriptor)) {
                //create a placeholder instead.
                addPlaceholder(viewId);
                LayoutHelper.addViewActivator(pageLayout, viewId);
            } else {

                ViewPane newPart = LayoutHelper.createView(pageLayout
                        .getViewFactory(), viewId);
                linkPartToPageLayout(viewId, newPart);
                folder.add(newPart);
            }
        } catch (PartInitException e) {
            // cannot safely open the dialog so log the problem
            WorkbenchPlugin.log(getClass(), "addView(String)", e); //$NON-NLS-1$
        }
    }

    /**
     * Inform the page layout of the new part created
     * and the folder the part belongs to.
     */
    private void linkPartToPageLayout(String viewId, LayoutPart newPart) {
        pageLayout.setRefPart(viewId, newPart);
        pageLayout.setFolderPart(viewId, folder);
        // force creation of the view layout rec
        pageLayout.getViewLayoutRec(viewId, true);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPlaceholderFolderLayout#getProperty(java.lang.String)
     */
    public String getProperty(String id) {
        return folder.getProperty(id);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPlaceholderFolderLayout#setProperty(java.lang.String, java.lang.String)
     */
    public void setProperty(String id, String value) {
        folder.setProperty(id,value);
    }
}
