/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.texteditor;


import java.util.ResourceBundle;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;


/**
 * An action which enters the incremental find mode a la emacs.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @since 2.0
 */
public class IncrementalFindAction extends ResourceAction implements IUpdate {

	/** The action's target */
	private IFindReplaceTarget fTarget;
	/** The part the action is bound to */
	private IWorkbenchPart fWorkbenchPart;
	/** The workbench window */
	private IWorkbenchWindow fWorkbenchWindow;
	
	/**
	 * Creates a new incremental find action for the given text editor. 
	 * The action configures its visual representation from the given 
	 * resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or 
	 *   <code>null</code> if none
	 * @param editor the text editor
	 * @see ResourceAction#ResourceAction
	 */
	public IncrementalFindAction(ResourceBundle bundle, String prefix, IWorkbenchPart workbenchPart) {
		super(bundle, prefix);
		fWorkbenchPart= workbenchPart;
		update();
	}
	
	/**
	 * Creates a new incremental find action for the given text editor. 
	 * The action configures its visual representation from the given 
	 * resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or 
	 *   <code>null</code> if none
	 * @param workbenchWindow the workbench window
	 * @see ResourceAction#ResourceAction
	 * 
	 * @deprecated use FindReplaceAction(ResourceBundle, String, IWorkbenchPart) instead
	 */
	public IncrementalFindAction(ResourceBundle bundle, String prefix, IWorkbenchWindow workbenchWindow) {
		super(bundle, prefix);
		fWorkbenchWindow= workbenchWindow;
		update();
	}

	/*
	 *	@see IAction#run
	 */
	public void run() {
		if (fTarget != null && fTarget instanceof IFindReplaceTargetExtension)
			((IFindReplaceTargetExtension) fTarget).beginSession();
	}

	/*
	 * @see IUpdate#update()
	 */
	public void update() {
		
		if (fWorkbenchPart == null && fWorkbenchWindow != null)
			fWorkbenchPart= fWorkbenchWindow.getPartService().getActivePart();
			
		if (fWorkbenchPart != null)
			fTarget= (IFindReplaceTarget) fWorkbenchPart.getAdapter(IncrementalFindTarget.class);
		else
			fTarget= null;
			
		setEnabled(fTarget != null && fTarget.canPerformFind());
	}

}
