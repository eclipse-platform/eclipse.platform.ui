/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.ViewLabelProvider;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.registry.IViewDescriptor;
import org.eclipse.ui.internal.registry.IViewRegistry;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;

/**
 * This action opens the Show In prompter, allowing the user to choose a view 
 * in which to show the current context (input and/or selection).
 * 
 * @see org.eclipse.ui.dialogs.ShowInContext
 * @see org.eclipse.ui.dialogs.IShowInSource
 * @see org.eclipse.ui.dialogs.IShowInTarget
 * 
 * @since 2.1
 */
public class ShowInAction extends Action {

	private IWorkbenchWindow window;
	
	/**
	 * Constructs a new <code>ShowInAction<code> for the given window.
	 * Note that this class does not track part activation itself.
	 * The caller must call <code>updateState</code> at the appropriate times
	 * to update the enabled state of this action.
	 */
	public ShowInAction(IWorkbenchWindow window) {
		Assert.isNotNull(window);
		this.window = window;
		setText(WorkbenchMessages.getString("ShowInAction.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("ShowInAction.toolTip")); //$NON-NLS-1$
		setId(IWorkbenchActionConstants.SHOW_IN);
		WorkbenchHelp.setHelp(this, IHelpContextIds.SHOW_IN_ACTION);
	}

	/**
	 * Returns the window.
	 * 
	 * @return the window
	 */
	public IWorkbenchWindow getWindow() {
		return window;
	}
	
	/**
	 * Updates the enabled state of this action.
	 * Calls <code>updateState(getSourcePart())</code>.
	 */
	public void updateState() {
		updateState(getSourcePart());
	}
	
	/**
	 * Updates the enabled state of this action given the source part.
	 * This action is enabled if and only if the source part provides an 
	 * <code>IShowInSource</code> (either directly or via <code>IAdaptable</code>),
	 * or the source part is an editor.
	 * 
	 * @param sourcePart the source part
	 */
	protected void updateState(IWorkbenchPart sourcePart) {
		setEnabled(sourcePart != null && (
			(sourcePart instanceof IEditorPart)
				|| getShowInSource(sourcePart) != null) && hasShowInItems(sourcePart));
	}

	/**
	 * Returns the Show In... part ids.  
	 * Don't expose this internal array to subclasses.
	 */
	private ArrayList getShowInPartIds() {
		WorkbenchPage page = (WorkbenchPage) getWindow().getActivePage();
		if (page != null)
			return page.getShowInPartIds();
		return new ArrayList();
	}
	
	/**
	 * Returns whether there are any items to list for the given part.
	 */
	private boolean hasShowInItems(IWorkbenchPart sourcePart) {
		ArrayList ids = getShowInPartIds();
		if (ids.isEmpty())
			return false;
		String srcId = sourcePart.getSite().getId();
		if (ids.contains(srcId))
			return ids.size() > 1;
		else
			return ids.size() > 0;
	}
	
	/**
	 * Returns the source part, or <code>null</code> if there is no applicable
	 * source part
	 * <p>
	 * This implementation returns the current part in the window.
	 * Subclasses may extend or reimplement.
	 * 
	 * @return the source part or <code>null</code>
	 */
	protected IWorkbenchPart getSourcePart() {
		IWorkbenchPage page = getWindow().getActivePage();
		if (page != null) {
			return page.getActivePart();
		}
		return null;
	}
	
	/**
	 * Returns the <code>IShowInSource</code> provided by the source part,
	 * or <code>null</code> if it does not provide one.
	 * 
	 * @param sourcePart the source part
	 * @return an <code>IShowInSource</code> or <code>null</code>
	 */
	protected IShowInSource getShowInSource(IWorkbenchPart sourcePart) {
		if (sourcePart instanceof IShowInSource) {
			return (IShowInSource) sourcePart;
		}
		Object o = sourcePart.getAdapter(IShowInSource.class);
		if (o instanceof IShowInSource) {
			return (IShowInSource) o;
		}
		return null;
	}
	
	/**
	 * Returns the <code>IShowInTarget</code> for the given part,
	 * or <code>null</code> if it does not provide one.
	 * 
	 * @param targetPart the target part
	 * @return the <code>IShowInTarget</code> or <code>null</code>
	 */
	protected IShowInTarget getShowInTarget(IWorkbenchPart targetPart) {
		if (targetPart instanceof IShowInTarget) {
			return (IShowInTarget) targetPart;
		}
		Object o = targetPart.getAdapter(IShowInTarget.class);
		if (o instanceof IShowInTarget) {
			return (IShowInTarget) o;
		}
		return null;
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
	protected ShowInContext getContext() {
		IWorkbenchPart source = getSourcePart();
		if (source == null) {
			return null;
		}
		Object o = source.getAdapter(IShowInSource.class);
		if (o instanceof IShowInSource) {
			ShowInContext context = ((IShowInSource) o).getShowInContext();
			if (context != null) {
				return context;
			}
		}
		else if (source instanceof IEditorPart) {
			Object input = ((IEditorPart) source).getEditorInput();
			ISelectionProvider sp = source.getSite().getSelectionProvider();
			ISelection sel = sp == null ? null : sp.getSelection();
			return new ShowInContext(input, sel);
		}
		return null;
	}
	
	/**
	 * Returns the view descriptors to show in the dialog.
	 */
	private IViewDescriptor[] getViewDescriptors() {
		String srcId = getSourcePart().getSite().getId();
		ArrayList ids = getShowInPartIds();
		ArrayList descs = new ArrayList();
		IViewRegistry reg = WorkbenchPlugin.getDefault().getViewRegistry();
		for (Iterator i = ids.iterator(); i.hasNext();) {
			String id = (String) i.next();
			if (!id.equals(srcId)) {
				IViewDescriptor desc = reg.find(id);
				if (desc != null) {
					descs.add(desc);
				}
			}
		}
		return (IViewDescriptor[]) descs.toArray(new IViewDescriptor[descs.size()]);
	}
	
	/**
	 * Opens the Show In prompter if the context is valid, otherwise generates
	 * a system beep.  If the user selects a target, then the context is passed
	 * to the target to show.
	 */
	public void run() {
		ShowInContext context = getContext();
		if (context == null) {
			getWindow().getShell().getDisplay().beep();
			return;
		}
		Object[] viewDescs = getViewDescriptors();
		if (viewDescs.length == 0) {
			getWindow().getShell().getDisplay().beep();
			return;
		}
		ListDialog dialog = new ListDialog(getWindow().getShell());
		dialog.setTitle(WorkbenchMessages.getString("ShowInDialog.title")); // $NON-NLS-1$
		dialog.setMessage(WorkbenchMessages.getString("ShowInDialog.message")); // $NON-NLS-1$
		dialog.setContentProvider(new ArrayContentProvider());
		dialog.setLabelProvider(new ViewLabelProvider());
		dialog.setInput(viewDescs);
		dialog.setInitialSelections(new Object[] { viewDescs[0] });
		if (dialog.open() == Dialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length > 0) {
				String targetId = ((IViewDescriptor) result[0]).getId();
				showIn(context, targetId);
			}
		}
	}

	/**
	 * Shows the context in the specified target view.
	 */	
	protected void showIn(ShowInContext context, String targetId) {
		IWorkbenchPage page = getWindow().getActivePage();
		if (page != null) {
			try {
				IViewPart view = page.showView(targetId);
				IShowInTarget target = getShowInTarget(view);
				if (target != null && target.show(context)) {
				}
				else {
					getWindow().getShell().getDisplay().beep();
				}
				((WorkbenchPage) page).performedShowIn(targetId);  // TODO: move back up
			}
			catch (PartInitException e) {
				WorkbenchPlugin.log("Error showing view in ShowInAction.run", e.getStatus());  // $NON-NLS-1$
			}
		}
	}

}
