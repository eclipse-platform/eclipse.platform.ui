/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.resources.internal.workbench;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.eclipse.ui.actions.MoveFilesAndFoldersOperation;
import org.eclipse.ui.actions.ReadOnlyStateChecker;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.navigator.AdaptabilityUtility;
import org.eclipse.ui.navigator.internal.dnd.CommonNavigatorDropAdapter;
import org.eclipse.ui.navigator.internal.dnd.IDropValidator;
import org.eclipse.ui.navigator.internal.dnd.NavigatorDropActionDelegate;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;
import org.eclipse.ui.views.navigator.ResourceNavigatorMessages;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * @since 3.2
 *  
 */
public class ResourceDropAction extends NavigatorDropActionDelegate implements IDropValidator, IOverwriteQuery {

	/**
	 * A flag indicating that overwrites should always occur.
	 */
	private boolean alwaysOverwrite = false;

	/**
	 * The last valid operation.
	 */
	private int lastValidOperation = DND.DROP_NONE;

	protected static final Class IRESOURCE_CLASS = IResource.class;

	private CopyFilesAndFoldersOperation copyOperation;

	private CopyFilesAndFoldersOperation moveOperation;

	/**
	 *  
	 */
	public ResourceDropAction() {
		super();
	}

	/**
	 * Returns an error status with the given info.
	 */
	private IStatus error(String message) {
		return error(message, null);
	}

	/**
	 * Returns an error status with the given info.
	 */
	private IStatus error(String message, Throwable exception) {
		return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, exception);
	}

	/**
	 * Returns the actual target of the drop, given the resource under the mouse. If the mouse
	 * target is a file, then the drop actually occurs in its parent. If the drop location is before
	 * or after the mouse target and feedback is enabled, the target is also the parent.
	 */
	private IContainer getActualTarget(CommonNavigatorDropAdapter dropAdapter, IResource mouseTarget) {
		/* if cursor is before or after mouseTarget, set target to parent */
		if (dropAdapter.getFeedbackEnabled()) {
			if (dropAdapter.getCurrentLocation() == ViewerDropAdapter.LOCATION_BEFORE || dropAdapter.getCurrentLocation() == ViewerDropAdapter.LOCATION_AFTER) {
				return mouseTarget.getParent();
			}
		}
		/* if cursor is on a file, return the parent */
		if (mouseTarget.getType() == IResource.FILE) {
			return mouseTarget.getParent();
		}
		/* otherwise the mouseTarget is the real target */
		return (IContainer) mouseTarget;
	}

	/**
	 * Returns the resource selection from the LocalSelectionTransfer.
	 * 
	 * @return the resource selection from the LocalSelectionTransfer
	 */
	private IResource[] getSelectedResources() {
		IResource[] selectedResources = null;

		ISelection selection = LocalSelectionTransfer.getInstance().getSelection();
		if (selection instanceof IStructuredSelection) {
			List selectionList = ((IStructuredSelection) selection).toList();
			selectedResources = (IResource[]) selectionList.toArray(new IResource[selectionList.size()]);
		}
		return selectedResources;
	}

	/**
	 * Returns an error status with the given info.
	 */
	private IStatus info(String message) {
		return new Status(IStatus.INFO, PlatformUI.PLUGIN_ID, 0, message, null);
	}

	/**
	 * Adds the given status to the list of problems. Discards OK statuses. If the status is a
	 * multi-status, only its children are added.
	 */
	private void mergeStatus(MultiStatus status, IStatus toMerge) {
		if (!toMerge.isOK()) {
			status.merge(toMerge);
		}
	}

	/**
	 * Returns an status indicating success.
	 */
	private IStatus ok() {
		return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0, ResourceNavigatorMessages.getString("DropAdapter.ok"), null); //$NON-NLS-1$
	}

	/**
	 * Opens an error dialog if necessary. Takes care of complex rules necessary for making the
	 * error dialog look nice.
	 */
	private void openError(IStatus status) {
		if (status == null)
			return;

		String genericTitle = ResourceNavigatorMessages.getString("DropAdapter.title"); //$NON-NLS-1$
		int codes = IStatus.ERROR | IStatus.WARNING;

		//simple case: one error, not a multistatus
		if (!status.isMultiStatus()) {
			ErrorDialog.openError(getShell(), genericTitle, null, status, codes);
			return;
		}

		//one error, single child of multistatus
		IStatus[] children = status.getChildren();
		if (children.length == 1) {
			ErrorDialog.openError(getShell(), status.getMessage(), null, children[0], codes);
			return;
		}
		//several problems
		ErrorDialog.openError(getShell(), genericTitle, null, status, codes);
	}

	/**
	 * Performs a drop using the FileTransfer transfer type.
	 */
	private IStatus performFileDrop(CommonNavigatorDropAdapter dropAdapter, Object data) {
		MultiStatus problems = new MultiStatus(PlatformUI.PLUGIN_ID, 0, ResourceNavigatorMessages.getString("DropAdapter.problemImporting"), null); //$NON-NLS-1$
		mergeStatus(problems, validateTarget(dropAdapter, dropAdapter.getCurrentTarget(), dropAdapter.getCurrentTransfer()));

		try {
			final IContainer target = getActualTarget(dropAdapter, getResource(dropAdapter.getCurrentTarget()));
			final String[] names = (String[]) data;
			// Run the import operation asynchronously.
			// Otherwise the drag source (e.g., Windows Explorer) will be
			// blocked
			// while the operation executes. Fixes bug 16478.
			Display.getCurrent().asyncExec(new Runnable() {

				public void run() {
					getShell().forceActive();
					CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(getShell());
					operation.copyFiles(names, target);
				}
			});
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return problems;
	}

	/**
	 * Performs a resource copy
	 */
	private IStatus performResourceCopy(CommonNavigatorDropAdapter dropAdapter, IResource[] sources) {
		MultiStatus problems = new MultiStatus(PlatformUI.PLUGIN_ID, 1, ResourceNavigatorMessages.getString("DropAdapter.problemsMoving"), null); //$NON-NLS-1$
		mergeStatus(problems, validateTarget(dropAdapter, dropAdapter.getCurrentTarget(), dropAdapter.getCurrentTransfer()));

		IContainer target = getActualTarget(dropAdapter, getResource(dropAdapter.getCurrentTarget()));
		getCopyOperation().copyResources(sources, target);
		copyOperation = null;
		return problems;
	}

	/**
	 * Performs a resource move
	 */
	private IStatus performResourceMove(CommonNavigatorDropAdapter dropAdapter, IResource[] sources) {
		MultiStatus problems = new MultiStatus(PlatformUI.PLUGIN_ID, 1, ResourceNavigatorMessages.getString("DropAdapter.problemsMoving"), null); //$NON-NLS-1$
		mergeStatus(problems, validateTarget(dropAdapter, dropAdapter.getCurrentTarget(), dropAdapter.getCurrentTransfer()));

		IContainer target = getActualTarget(dropAdapter, getResource(dropAdapter.getCurrentTarget()));
		ReadOnlyStateChecker checker = new ReadOnlyStateChecker(getShell(), ResourceNavigatorMessages.getString("MoveResourceAction.title"), //$NON-NLS-1$
					ResourceNavigatorMessages.getString("MoveResourceAction.checkMoveMessage"));//$NON-NLS-1$	
		sources = checker.checkReadOnlyResources(sources);

		getMoveOperation().copyResources(sources, target);
		moveOperation = null;

		return problems;
	}

	/*
	 * @see IOverwriteQuery#queryOverwrite(String)
	 */
	public String queryOverwrite(String pathString) {
		if (this.alwaysOverwrite)
			return ALL;

		final String returnCode[] = {CANCEL};
		final String msg = ResourceNavigatorMessages.format("DropAdapter.overwriteQuery", new Object[]{pathString}); //$NON-NLS-1$
		final String[] options = {IDialogConstants.YES_LABEL, IDialogConstants.YES_TO_ALL_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL};
		getDisplay().syncExec(new Runnable() {

			public void run() {
				MessageDialog dialog = new MessageDialog(getShell(), ResourceNavigatorMessages.getString("DropAdapter.question"), null, msg, MessageDialog.QUESTION, options, 0); //$NON-NLS-1$
				dialog.open();
				int returnVal = dialog.getReturnCode();
				String[] returnCodes = {YES, ALL, NO, CANCEL};
				returnCode[0] = returnVal < 0 ? CANCEL : returnCodes[returnVal];
			}
		});
		if (returnCode[0] == ALL)
			this.alwaysOverwrite = true;
		return returnCode[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.navigator.internal.views.navigator.dnd.IDropValidator#validateDrop(org.eclipse.jface.viewers.ViewerDropAdapter,
	 *      java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
	 */
	public boolean validateDrop(CommonNavigatorDropAdapter dropAdapter, Object target, int operation, TransferData transferType) {
		return validateTarget(dropAdapter, target, transferType).isOK();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.navigator.internal.views.navigator.dnd.NavigatorDropActionDelegate#run(int,
	 *      int, java.lang.Object, java.lang.Object)
	 */
	public boolean run(CommonNavigatorDropAdapter dropAdapter, Object data, Object target) {

		this.alwaysOverwrite = false;
		if (dropAdapter.getCurrentTarget() == null || data == null) {
			return false;
		}
		boolean result = false;
		IStatus status = null;
		IResource[] resources = null;
		TransferData currentTransfer = dropAdapter.getCurrentTransfer();
		if (LocalSelectionTransfer.getInstance().isSupportedType(currentTransfer)) {
			resources = getSelectedResources();
		} else if (ResourceTransfer.getInstance().isSupportedType(currentTransfer)) {
			resources = (IResource[]) data;
		} else if (FileTransfer.getInstance().isSupportedType(currentTransfer)) {
			status = performFileDrop(dropAdapter, data);
			result = status.isOK();
		}
		if (resources != null) {
			if (dropAdapter.getCurrentOperation() == DND.DROP_COPY)
				status = performResourceCopy(dropAdapter, resources);
			else
				status = performResourceMove(dropAdapter, resources);
		}
		openError(status);
		return result;
	}

	/**
	 * Ensures that the drop target meets certain criteria
	 */
	private IStatus validateTarget(CommonNavigatorDropAdapter dropAdapter, Object target, TransferData transferType) {
		if (!(target instanceof IResource)) {
			return info(ResourceNavigatorMessages.getString("DropAdapter.targetMustBeResource")); //$NON-NLS-1$
		}
		IResource resource = getResource(dropAdapter.getCurrentTarget());
		if (!resource.isAccessible()) {
			return error(ResourceNavigatorMessages.getString("DropAdapter.canNotDropIntoClosedProject")); //$NON-NLS-1$
		}
		IContainer destination = getActualTarget(dropAdapter, resource);
		if (destination.getType() == IResource.ROOT) {
			return error(ResourceNavigatorMessages.getString("DropAdapter.resourcesCanNotBeSiblings")); //$NON-NLS-1$
		}
		String message = null;
		// drag within Eclipse?
		if (LocalSelectionTransfer.getInstance().isSupportedType(transferType)) {
			IResource[] selectedResources = getSelectedResources();

			if (selectedResources == null)
				message = ResourceNavigatorMessages.getString("DropAdapter.dropOperationErrorOther"); //$NON-NLS-1$
			else {
				CopyFilesAndFoldersOperation operation;
				if (this.lastValidOperation == DND.DROP_COPY) {
					operation = getCopyOperation();
				} else {
					operation = getMoveOperation();
				}
				message = operation.validateDestination(destination, selectedResources);
			}
		} // file import?
		else if (FileTransfer.getInstance().isSupportedType(transferType)) {
			String[] sourceNames = (String[]) FileTransfer.getInstance().nativeToJava(transferType);
			if (sourceNames == null) {
				// source names will be null on Linux. Use empty names to do
				// destination validation.
				// Fixes bug 29778
				sourceNames = new String[0];
			}
			message = getCopyOperation().validateImportDestination(destination, sourceNames);
		}
		if (message != null) {
			return error(message);
		}
		return ok();
	}

	/**
	 * @return
	 */
	private CopyFilesAndFoldersOperation getMoveOperation() {
		if (moveOperation == null)
			moveOperation = new MoveFilesAndFoldersOperation(getShell());
		return moveOperation;
	}

	/**
	 * @return
	 */
	private CopyFilesAndFoldersOperation getCopyOperation() {
		if (copyOperation == null)
			copyOperation = new CopyFilesAndFoldersOperation(getShell());
		return copyOperation;
	}

	protected IResource getResource(Object target) {
		return (IResource) AdaptabilityUtility.getAdapter(target, IRESOURCE_CLASS);
	}

}