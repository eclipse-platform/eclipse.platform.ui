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

package org.eclipse.ui.texteditor;


import java.util.ResourceBundle;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.IFindReplaceTarget;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;


/**
 * An action which opens a Find/Replace dialog.
 * The dialog while open, tracks the active workbench part
 * and retargets itself to the active find/replace target.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @see IFindReplaceTarget
 */
public class FindReplaceAction extends ResourceAction implements IUpdate {

	/**
	 * Represents the "global" find/replace dialog. It tracks the active
	 * part and retargets the find/replace dialog accordingly. The find/replace
	 * target is retrieved from the active part using
	 * <code>getAdapter(IFindReplaceTarget.class)</code>.
	 * <p>
	 * The stub has the same life cycle as the find/replace dialog.</p>
	 */
	static class FindReplaceDialogStub implements IPartListener, DisposeListener {

		/** The workbench part */
		private IWorkbenchPart fPart;
		/** The previous workbench part */
		private IWorkbenchPart fPreviousPart;
		/** The previous find/replace target */
		private IFindReplaceTarget fPreviousTarget;

		/** The workbench window */
		private IWorkbenchWindow fWindow;
		/** The find/replace dialog */
		private FindReplaceDialog fDialog;

		/**
		 * Creates a new find/replace dialog accessor anchored at the given part site.
		 * @param site the part site
		 */
		public FindReplaceDialogStub(IWorkbenchPartSite site) {

			fWindow= site.getWorkbenchWindow();

			fDialog= new FindReplaceDialog(site.getShell());
			fDialog.create();
			fDialog.getShell().addDisposeListener(this);

			IPartService service= fWindow.getPartService();
			service.addPartListener(this);
			partActivated(service.getActivePart());
		}

		/**
		 * Returns the find/replace dialog.
		 * @return the find/replace dialog
		 */
		public FindReplaceDialog getDialog() {
			return fDialog;
		}

		/*
		 * @see IPartListener#partActivated(IWorkbenchPart)
		 */
		public void partActivated(IWorkbenchPart part) {

			IFindReplaceTarget target= part == null ? null : (IFindReplaceTarget) part.getAdapter(IFindReplaceTarget.class);
			fPreviousPart= fPart;
			fPart= target == null ? null : part;

			if (fPreviousTarget != target) {
				fPreviousTarget= target;
				if (fDialog != null) {
					boolean isEditable= false;
					if (fPart instanceof ITextEditorExtension2) {
						ITextEditorExtension2 extension= (ITextEditorExtension2) fPart;
						isEditable= extension.isEditorInputModifiable();
					}
					fDialog.updateTarget(target, isEditable, false);
				}
			}
		}

		/*
		 * @see IPartListener#partClosed(IWorkbenchPart)
		 */
		public void partClosed(IWorkbenchPart part) {

			if (part == fPreviousPart) {
				fPreviousPart= null;
				fPreviousTarget= null;
			}

			if (part == fPart)
				partActivated(null);
		}

		/*
		 * @see DisposeListener#widgetDisposed(DisposeEvent)
		 */
		public void widgetDisposed(DisposeEvent event) {

			if (fgFindReplaceDialogStub == this)
				fgFindReplaceDialogStub= null;

			if (fWindow != null) {
				fWindow.getPartService().removePartListener(this);
				fWindow= null;
			}
			fDialog= null;
			fPart= null;
			fPreviousPart= null;
			fPreviousTarget= null;
		}

		/*
		 * @see IPartListener#partOpened(IWorkbenchPart)
		 */
		public void partOpened(IWorkbenchPart part) {}

		/*
		 * @see IPartListener#partDeactivated(IWorkbenchPart)
		 */
		public void partDeactivated(IWorkbenchPart part) {}

		/*
		 * @see IPartListener#partBroughtToTop(IWorkbenchPart)
		 */
		public void partBroughtToTop(IWorkbenchPart part) {}
	}


	/** Lister for disabling the dialog on editor close */
	private static FindReplaceDialogStub fgFindReplaceDialogStub;
	/** The action's target */
	private IFindReplaceTarget fTarget;
	/** The part the action is bound to */
	private IWorkbenchPart fWorkbenchPart;
	/** The workbench window */
	private IWorkbenchWindow fWorkbenchWindow;

	/**
	 * Creates a new find/replace action for the given workbench part.
	 * The action configures its visual representation from the given
	 * resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @param workbenchPart	 the workbench part
	 * @see ResourceAction#ResourceAction(ResourceBundle, String)
	 */
	public FindReplaceAction(ResourceBundle bundle, String prefix, IWorkbenchPart workbenchPart) {
		super(bundle, prefix);
		fWorkbenchPart= workbenchPart;
		update();
	}

	/**
	 * Creates a new find/replace action for the given workbench window.
	 * The action configures its visual representation from the given
	 * resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @param workbenchWindow the workbench window
	 * @see ResourceAction#ResourceAction(ResourceBundle, String)
	 *
	 * @deprecated use FindReplaceAction(ResourceBundle, String, IWorkbenchPart) instead
	 */
	public FindReplaceAction(ResourceBundle bundle, String prefix, IWorkbenchWindow workbenchWindow) {
		super(bundle, prefix);
		fWorkbenchWindow= workbenchWindow;
		update();
	}

	/*
	 *	@see IAction#run()
	 */
	public void run() {
		if (fTarget == null)
			return;

		if (fgFindReplaceDialogStub != null) {
			Shell shell= fWorkbenchPart.getSite().getShell();
			FindReplaceDialog dialog= fgFindReplaceDialogStub.getDialog();
			if (dialog != null && shell != dialog.getParentShell()) {
				fgFindReplaceDialogStub= null; // here to avoid timing issues
				dialog.close();
			}
		}

		if (fgFindReplaceDialogStub == null)
			fgFindReplaceDialogStub= new FindReplaceDialogStub(fWorkbenchPart.getSite());

		boolean isEditable= false;
		if (fWorkbenchPart instanceof ITextEditorExtension2)
			isEditable= ((ITextEditorExtension2) fWorkbenchPart).isEditorInputModifiable();

		FindReplaceDialog dialog= fgFindReplaceDialogStub.getDialog();
		dialog.updateTarget(fTarget, isEditable, true);
		dialog.open();
	}

	/*
	 * @see IUpdate#update()
	 */
	public void update() {

		if (fWorkbenchPart == null && fWorkbenchWindow != null)
			fWorkbenchPart= fWorkbenchWindow.getPartService().getActivePart();

		if (fWorkbenchPart != null)
			fTarget= (IFindReplaceTarget) fWorkbenchPart.getAdapter(IFindReplaceTarget.class);
		else
			fTarget= null;

		setEnabled(fTarget != null && fTarget.canPerformFind());
	}
}
