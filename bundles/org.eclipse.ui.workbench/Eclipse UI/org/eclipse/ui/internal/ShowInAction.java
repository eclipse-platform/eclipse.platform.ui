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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * Action for a particular target in the Show In menu.
 */
public class ShowInAction extends Action {
    private IWorkbenchWindow window;

    private IViewDescriptor desc;

    /**
     * Creates a new <code>ShowInAction</code>.
     */
    protected ShowInAction(IWorkbenchWindow window, IViewDescriptor desc) {
        super(desc.getLabel());
        setImageDescriptor(desc.getImageDescriptor());
        window.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.SHOW_IN_ACTION);
        this.window = window;
        this.desc = desc;
    }

    /**
     * Shows the current context in this action's view.
     */
    public void run() {
        IWorkbenchPage page = window.getActivePage();
        if (page == null) {
            beep();
            return;
        }

        IWorkbenchPart sourcePart = page.getActivePart();
        if (sourcePart == null) {
            beep();
            return;
        }

        ShowInContext context = getContext(sourcePart);
        if (context == null) {
            beep();
            return;
        }

        try {
            IViewPart view = page.showView(desc.getId());
            IShowInTarget target = getShowInTarget(view);
            if (target != null && target.show(context)) {
                // success
            } else {
                beep();
            }
            ((WorkbenchPage) page).performedShowIn(desc.getId()); // TODO: move back up
        } catch (PartInitException e) {
            WorkbenchPlugin.log(
                    "Error showing view in ShowInAction.run", e.getStatus()); //$NON-NLS-1$
        }
    }

    /**
     * Returns the <code>IShowInSource</code> provided by the source part,
     * or <code>null</code> if it does not provide one.
     * 
     * @param sourcePart the source part
     * @return an <code>IShowInSource</code> or <code>null</code>
     */
    private IShowInSource getShowInSource(IWorkbenchPart sourcePart) {
        return (IShowInSource)Util.getAdapter(sourcePart, IShowInSource.class);
    }

    /**
     * Returns the <code>IShowInTarget</code> for the given part,
     * or <code>null</code> if it does not provide one.
     * 
     * @param targetPart the target part
     * @return the <code>IShowInTarget</code> or <code>null</code>
     */
    private IShowInTarget getShowInTarget(IWorkbenchPart targetPart) {
        return (IShowInTarget)Util.getAdapter(targetPart, IShowInTarget.class);
    }

    /**
     * Returns the <code>ShowInContext</code> to show in the selected target,
     * or <code>null</code> if there is no valid context to show.
     * <p>
     * This implementation obtains the context from the <code>IShowInSource</code>
     * of the source part (if provided), or, if the source part is an editor,
     * it creates the context from the editor's input and selection.
     * <p>
     * Subclasses may extend or reimplement.
     * 
     * @return the <code>ShowInContext</code> to show or <code>null</code>
     */
    private ShowInContext getContext(IWorkbenchPart sourcePart) {
        IShowInSource source = getShowInSource(sourcePart);
        if (source != null) {
            ShowInContext context = source.getShowInContext();
            if (context != null) {
                return context;
            }
        } else if (sourcePart instanceof IEditorPart) {
            Object input = ((IEditorPart) sourcePart).getEditorInput();
            ISelectionProvider sp = sourcePart.getSite().getSelectionProvider();
            ISelection sel = sp == null ? null : sp.getSelection();
            return new ShowInContext(input, sel);
        }
        return null;
    }

    /**
     * Generates a system beep.
     */
    private void beep() {
        window.getShell().getDisplay().beep();
    }

}
