/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * A  {@link ContributionItem} specifically for contributions to the perspective switcher.
 *
 */
public class PerspectiveBarContributionItem extends ContributionItem {

    private IPerspectiveDescriptor perspective;

    private IPreferenceStore apiPreferenceStore = PrefUtil
            .getAPIPreferenceStore();

    private ToolItem toolItem = null;

    private Image image;

    private IWorkbenchPage workbenchPage;

    /**
     * Create a new perspective contribution item
     * 
     * @param perspective the descriptor for the perspective
     * @param workbenchPage the page that this perspective is in
     */
    public PerspectiveBarContributionItem(IPerspectiveDescriptor perspective,
            IWorkbenchPage workbenchPage) {
        super(perspective.getId());
        this.perspective = perspective;
        this.workbenchPage = workbenchPage;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.ContributionItem#dispose()
     */
    public void dispose() {
        super.dispose();
        if (image != null && !image.isDisposed()) {
            image.dispose();
            image = null;
        }
        apiPreferenceStore = null;
        workbenchPage = null;
        perspective = null;

    }

    public void fill(ToolBar parent, int index) {
        if (toolItem == null && parent != null && !parent.isDisposed()) {

            if (index >= 0) {
				toolItem = new ToolItem(parent, SWT.CHECK, index);
			} else {
				toolItem = new ToolItem(parent, SWT.CHECK);
			}

            if (image == null || image.isDisposed()) {
                createImage();
            }
            toolItem.setImage(image);

            toolItem.setToolTipText(NLS.bind(WorkbenchMessages.PerspectiveBarContributionItem_toolTip, perspective.getLabel()));
            toolItem.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent event) {
                    select();
                }
            });
            toolItem.setData(this); //TODO review need for this
            update();
        }
    }

    private void createImage() {
        ImageDescriptor imageDescriptor = perspective.getImageDescriptor();
        if (imageDescriptor != null) {
            image = imageDescriptor.createImage();
        } else {
            image = WorkbenchImages.getImageDescriptor(
                    ISharedImages.IMG_ETOOL_DEF_PERSPECTIVE)
                    .createImage();
        }
    }

    Image getImage() {
        if (image == null) {
            createImage();
        }
        return image;
    }

    /**
     * Select this perspective
     */
    public void select() {
        if (workbenchPage.getPerspective() != perspective) {
            workbenchPage.setPerspective(perspective);
        } else {
			toolItem.setSelection(true);
		}
    }

    public void update() {
        if (toolItem != null && !toolItem.isDisposed()) {
            toolItem
                    .setSelection(workbenchPage.getPerspective() == perspective);
            if (apiPreferenceStore
                    .getBoolean(IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR)) {
                if (apiPreferenceStore.getString(
                        IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR)
                        .equals(IWorkbenchPreferenceConstants.TOP_LEFT)) {
					toolItem.setText(perspective.getLabel());
				} else {
					toolItem.setText(shortenText(perspective.getLabel(),
                            toolItem));
				}
            } else {
                toolItem.setText(""); //$NON-NLS-1$
            }
        }
    }

    /**
     * Update this item with a new perspective descriptor
     * @param newDesc
     */
    public void update(IPerspectiveDescriptor newDesc) {
        perspective = newDesc;
        if (toolItem != null && !toolItem.isDisposed()) {
            ImageDescriptor imageDescriptor = perspective.getImageDescriptor();
            if (imageDescriptor != null) {
                toolItem.setImage(imageDescriptor.createImage());
            } else {
                toolItem.setImage(WorkbenchImages.getImageDescriptor(
                        ISharedImages.IMG_ETOOL_DEF_PERSPECTIVE)
                        .createImage());
            }
            toolItem.setToolTipText(NLS.bind(WorkbenchMessages.PerspectiveBarContributionItem_toolTip, perspective.getLabel() ));
        }
        update();
    }

    IWorkbenchPage getPage() {
        return workbenchPage;
    }

    IPerspectiveDescriptor getPerspective() {
        return perspective;
    }

    ToolItem getToolItem() {
        return toolItem;
    }

    /**
     * Answer whether the receiver is a match for the provided 
     * perspective descriptor
     * 
     * @param perspective the perspective descriptor
     * @param workbenchPage the page
     * @return <code>true</code> if it is a match
     */
    public boolean handles(IPerspectiveDescriptor perspective,
            IWorkbenchPage workbenchPage) {
        return this.perspective == perspective
                && this.workbenchPage == workbenchPage;
    }

    /**
     * Set the current perspective
     * @param newPerspective
     */
    public void setPerspective(IPerspectiveDescriptor newPerspective) {
        this.perspective = newPerspective;
    }

    // TODO review need for this method
    void setSelection(boolean b) {
        if (toolItem != null && !toolItem.isDisposed()) {
			toolItem.setSelection(b);
		}
    }

    static int getMaxWidth(Image image) {
        return image.getBounds().width * 5;
    }

    private static final String ellipsis = "..."; //$NON-NLS-1$

    protected String shortenText(String textValue, ToolItem item) {
        if (textValue == null || toolItem == null || toolItem.isDisposed()) {
			return null;
		}
        String returnText = textValue;
        GC gc = new GC(item.getParent());
        int maxWidth = getMaxWidth(item.getImage());
        if (gc.textExtent(textValue).x >= maxWidth) {
            for (int i = textValue.length(); i > 0; i--) {
                String test = textValue.substring(0, i);
                test = test + ellipsis;
                if (gc.textExtent(test).x < maxWidth) {
                    returnText = test;
                    break;
                }
            }
        }
        gc.dispose();
        return returnText;
    }
}
