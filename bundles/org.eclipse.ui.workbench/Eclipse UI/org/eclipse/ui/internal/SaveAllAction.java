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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.Saveable;

/**
 * Global action that saves all targets in the
 * workbench that implement ISaveTarget interface.
 * The action keeps track of opened save targets
 * and their 'save' state. If none of the currently
 * opened targets needs saving, it will disable.
 * This action is somewhat different from all
 * other global actions in that it works on
 * multiple targets at the same time i.e. it
 * does not disconnect from the target when it
 * becomes deactivated.
 */
public class SaveAllAction extends PageEventAction implements IPropertyListener {
    /**
     * List of parts (element type: <code>IWorkbenchPart</code>)
     * against which this class has outstanding property listeners registered.
     */
    private List partsWithListeners = new ArrayList(1);
	private IWorkbenchPart openPart;

    /**
     * The default constructor.
     * 
     * @param window the window
     */
    public SaveAllAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.SaveAll_text, window);
        setToolTipText(WorkbenchMessages.SaveAll_toolTip);
        setId("saveAll"); //$NON-NLS-1$
        setEnabled(false);
        window.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.SAVE_ALL_ACTION);
        setImageDescriptor(WorkbenchImages
                .getImageDescriptor(ISharedImages.IMG_ETOOL_SAVEALL_EDIT));
        setDisabledImageDescriptor(WorkbenchImages
                .getImageDescriptor(ISharedImages.IMG_ETOOL_SAVEALL_EDIT_DISABLED));
        setActionDefinitionId("org.eclipse.ui.file.saveAll"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * Method declared on PageEventAction.
     */
    public void pageActivated(IWorkbenchPage page) {
        super.pageActivated(page);
        updateState();
    }

    /* (non-Javadoc)
     * Method declared on PageEventAction.
     */
    public void pageClosed(IWorkbenchPage page) {
        super.pageClosed(page);
        updateState();
    }

    /* (non-Javadoc)
     * Method declared on PartEventAction.
     */
    public void partClosed(IWorkbenchPart part) {
        super.partClosed(part);
        if (part instanceof ISaveablePart) {
            part.removePropertyListener(this);
            partsWithListeners.remove(part);
            updateState();
        }
    }

    /* (non-Javadoc)
     * Method declared on PartEventAction.
     */
    public void partOpened(IWorkbenchPart part) {
        super.partOpened(part);
        if (part instanceof ISaveablePart) {
            part.addPropertyListener(this);
            partsWithListeners.add(part);
			// We need to temporarily cache the opened part 
			// because saveable views are not registered 
			// with a perspective until after this method 
			// is called.  We can't pass it through to
			// update because it's protected. An async
			// call to update may be a better approach.
            // See bug 93784 [WorkbenchParts] View not yet added to perspective when partOpened sent
			openPart = part;
            updateState();
			openPart = null;
        }
    }

    /* (non-Javadoc)
     * Method declared on IPropertyListener.
     */
    public void propertyChanged(Object source, int propID) {
        if (source instanceof ISaveablePart) {
            if (propID == ISaveablePart.PROP_DIRTY) {
                updateState();
            }
        }
    }

    /* (non-Javadoc)
     * Method declared on Action.
     */
    public void run() {
        if (getWorkbenchWindow() == null) {
            // action has been disposed
            return;
        }
        WorkbenchPage page = (WorkbenchPage) getActivePage();
        if (page != null) {
        	// The second parameter is true to also save saveables from non-part
			// sources, see bug 139004.
            page.saveAllEditors(false, true);
            updateState();
        }
    }

    /**
     * Updates availability depending on number of
     * targets that need saving.
     */
    protected void updateState() {
        // Workaround for bug 93784 [WorkbenchParts] View not yet added to perspective when partOpened sent
		if (openPart != null && openPart.getSite().getPage().equals(getActivePage()) && ((ISaveablePart) openPart).isDirty()) {
			setEnabled(true);
		}
		else {
			WorkbenchPage page = (WorkbenchPage) getActivePage();
			if (page == null) {
				setEnabled(false);
			} else {
				if (page.getDirtyParts().length > 0) {
					setEnabled(true);
				} else {
					// Since Save All also saves saveables from non-part sources,
					// look if any such saveables exist and are dirty.
					SaveablesList saveablesList = (SaveablesList) page
							.getWorkbenchWindow().getWorkbench().getService(
									ISaveablesLifecycleListener.class);
					ISaveablesSource[] nonPartSources = saveablesList.getNonPartSources();
					for (int i = 0; i < nonPartSources.length; i++) {
						Saveable[] saveables = nonPartSources[i].getSaveables();
						for (int j = 0; j < saveables.length; j++) {
							if (saveables[j].isDirty()) {
								setEnabled(true);
								return;
							}
						}
					}
					setEnabled(false);
				}
			}
		}
    }

    /* (non-Javadoc)
     * Method declared on PageEventAction.
     */
    public void dispose() {
        super.dispose();
        for (Iterator it = partsWithListeners.iterator(); it.hasNext();) {
            IWorkbenchPart part = (IWorkbenchPart) it.next();
            part.removePropertyListener(this);
        }
        partsWithListeners.clear();
    }
}
