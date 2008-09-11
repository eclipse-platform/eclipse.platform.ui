/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;


import java.util.ResourceBundle;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;


/**
 * An action which enters the incremental find mode like in emacs.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @since 2.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class IncrementalFindAction extends ResourceAction implements IUpdate {

	/** The action's target */
	private IFindReplaceTarget fTarget;
	/** The part the action is bound to */
	private IWorkbenchPart fWorkbenchPart;
	/** The workbench window */
	private IWorkbenchWindow fWorkbenchWindow;
	/**
	 * The direction to run the incremental find
	 * @since 2.1
	 */
	private boolean fForward;

	/**
	 * Creates a new incremental find action for the given workbench part.
	 * The action configures its visual representation from the given
	 * resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @param workbenchPart the workbench part
	 * @param forward <code>true</code> if the search direction is forward
	 * @see ResourceAction#ResourceAction(ResourceBundle, String)
	 * @since 2.1
	 */
	public IncrementalFindAction(ResourceBundle bundle, String prefix, IWorkbenchPart workbenchPart, boolean forward) {
		super(bundle, prefix);
		fWorkbenchPart= workbenchPart;
		fForward= forward;
		update();
	}

	/**
	 * Creates a new incremental find action for the given workbench window.
	 * The action configures its visual representation from the given
	 * resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @param workbenchWindow the workbench window
	 * @param forward <code>true</code> if the search direction is forward
	 * @see ResourceAction#ResourceAction(ResourceBundle, String)
	 *
	 * @deprecated use FindReplaceAction(ResourceBundle, String, IWorkbenchPart, boolean) instead
	 * @since 2.1
	 */
	public IncrementalFindAction(ResourceBundle bundle, String prefix, IWorkbenchWindow workbenchWindow, boolean forward) {
		super(bundle, prefix);
		fWorkbenchWindow= workbenchWindow;
		fForward= forward;
		update();
	}

	/*
	 * @see IAction#run()
	 */
	public void run() {

		if (fTarget == null)
			return;

		if (fTarget instanceof IncrementalFindTarget)
			((IncrementalFindTarget) fTarget).setDirection(fForward);

		if (fTarget instanceof IFindReplaceTargetExtension)
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
