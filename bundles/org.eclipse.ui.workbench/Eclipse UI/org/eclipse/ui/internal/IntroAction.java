/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - Fix for bug 95292 - [Intro] 
 *     		Help > Welcome missing F1 context
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.intro.IntroDescriptor;
import org.eclipse.ui.internal.intro.IntroMessages;

/**
 * Action that will launch the intro in the given window.
 * 
 * @since 3.0
 */
public class IntroAction extends Action implements
        ActionFactory.IWorkbenchAction {

    private IWorkbenchWindow workbenchWindow;

    private IPageListener pageListener = new IPageListener() {

        public void pageActivated(IWorkbenchPage page) {
            //no-op
        }

        public void pageClosed(IWorkbenchPage page) {
            setEnabled(workbenchWindow.getPages().length > 0);
        }

        public void pageOpened(IWorkbenchPage page) {
            setEnabled(true);
        }
    };

    /**
     * @param window the window to bind the action to. 
     */
    public IntroAction(IWorkbenchWindow window) {
        super(IntroMessages.Intro_action_text); 
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.workbenchWindow = window;
        window.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.INTRO_ACTION);
        
        IntroDescriptor introDescriptor = ((Workbench) workbenchWindow
                .getWorkbench()).getIntroDescriptor();
        String labelOverride = introDescriptor.getLabelOverride();
        if (labelOverride != null)
        	setText(labelOverride);
        
        setActionDefinitionId("org.eclipse.ui.help.quickStartAction"); //$NON-NLS-1$
        
        window.addPageListener(pageListener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.ActionFactory.IWorkbenchAction#dispose()
     */
    public void dispose() {
        workbenchWindow.removePageListener(pageListener);
        workbenchWindow = null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
     */
    public void runWithEvent(Event event) {        
        IntroDescriptor introDescriptor = ((Workbench) workbenchWindow
                .getWorkbench()).getIntroDescriptor();
        if (introDescriptor == null) {
            MessageDialog.openWarning(workbenchWindow.getShell(),
                    IntroMessages.Intro_missing_product_title,
                    IntroMessages.Intro_missing_product_message);
        } else {
			workbenchWindow.getWorkbench().getIntroManager().showIntro(
                    workbenchWindow, false);
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        IntroDescriptor introDescriptor = ((Workbench) workbenchWindow
                .getWorkbench()).getIntroDescriptor();
        if (introDescriptor == null) {
			return null;
		}
        return introDescriptor.getImageDescriptor();
    }
}
