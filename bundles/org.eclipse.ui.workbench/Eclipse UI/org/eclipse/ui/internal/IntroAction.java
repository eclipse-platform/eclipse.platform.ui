/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.intro.IntroMessages;

/**
 * Action that will launch the intro in the given window.
 * 
 * @since 3.0
 */
public class IntroAction extends Action implements ActionFactory.IWorkbenchAction {

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
		
		super(IntroMessages.getString("Intro.action_text")); //$NON-NLS-1$
		if (window == null) {
			throw new IllegalArgumentException();
		}
		this.workbenchWindow = window;
		
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
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		workbenchWindow.getWorkbench().getIntroManager().showIntro(workbenchWindow, false);		
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return ((Workbench)workbenchWindow.getWorkbench()).getIntroDescriptor().getImageDescriptor();
    }
}
