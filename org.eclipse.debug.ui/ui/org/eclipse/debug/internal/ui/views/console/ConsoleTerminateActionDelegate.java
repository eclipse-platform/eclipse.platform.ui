/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.console;

import org.eclipse.debug.internal.ui.actions.context.TerminateActionDelegate;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleView;

/**
 * Terminate action delegate for the console. The selection must be computed
 * by getting the process from the associated process console, rather than
 * the selection in the view (which is text viewer/text selection).
 * 
 * @since 3.1
 */
public class ConsoleTerminateActionDelegate extends TerminateActionDelegate {
    
    private IConsoleView fConsoleView;      
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#getSelection()
     */
    protected IStructuredSelection getSelection() {
        IConsole console = fConsoleView.getConsole();
        if (console instanceof ProcessConsole) {
            return new StructuredSelection(((ProcessConsole)console).getProcess());
        }
        return StructuredSelection.EMPTY;
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init(IViewPart view) {
        fConsoleView = (IConsoleView) view;
        super.init(view);
    }

    public synchronized void dispose() {
        super.dispose();
        IViewPart view = getView();
        if (view != null) {
            view.getSite().getSelectionProvider().removeSelectionChangedListener((ISelectionChangedListener) getAction());
        }
    }
    
    
}
