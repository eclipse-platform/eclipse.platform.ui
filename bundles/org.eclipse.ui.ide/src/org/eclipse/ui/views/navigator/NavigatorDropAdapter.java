/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.navigator;

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
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.eclipse.ui.actions.MoveFilesAndFoldersOperation;
import org.eclipse.ui.actions.ReadOnlyStateChecker;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.part.PluginDropAdapter;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * Implements drop behaviour for drag and drop operations
 * that land on the resource navigator.
 * 
 * @since 2.0
 */
public class NavigatorDropAdapter extends PluginDropAdapter implements
        IOverwriteQuery {

    /**
     * A flag indicating that overwrites should always occur.
     */
    private boolean alwaysOverwrite = false;

    /**
     * The last valid operation.
     */
    private int lastValidOperation = DND.DROP_NONE;

    /**
     * Constructs a new drop adapter.
     */
    public NavigatorDropAdapter(StructuredViewer viewer) {
        super(viewer);
    }

    /*
     * @see org.eclipse.swt.dnd.DropTargetListener#dragEnter(org.eclipse.swt.dnd.DropTargetEvent)
     */
    public void dragEnter(DropTargetEvent event) {
        if (FileTransfer.getInstance().isSupportedType(event.currentDataType)
                && event.detail == DND.DROP_DEFAULT) {
            // default to copy when dragging from outside Eclipse. Fixes bug 16308.
            event.detail = DND.DROP_COPY;
        }
        super.dragEnter(event);
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
        return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message,
                exception);
    }

    /**
     * Returns the actual target of the drop, given the resource
     * under the mouse.  If the mouse target is a file, then the drop actually 
     * occurs in its parent.  If the drop location is before or after the
     * mouse target and feedback is enabled, the target is also the parent.
     */
    private IContainer getActualTarget(IResource mouseTarget) {
        /* if cursor is before or after mouseTarget, set target to parent */
        if (getFeedbackEnabled()) {
            if (getCurrentLocation() == LOCATION_BEFORE
                    || getCurrentLocation() == LOCATION_AFTER) {
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
     * Returns the display
     */
    private Display getDisplay() {
        return getViewer().getControl().getDisplay();
    }

    /**
     * Returns the resource selection from the LocalSelectionTransfer.
     * 
     * @return the resource selection from the LocalSelectionTransfer
     */
    private IResource[] getSelectedResources() {
        IResource[] selectedResources = null;

        ISelection selection = LocalSelectionTransfer.getInstance()
                .getSelection();
        if (selection instanceof IStructuredSelection) {
            List selectionList = ((IStructuredSelection) selection).toList();
            selectedResources = (IResource[]) selectionList
                    .toArray(new IResource[selectionList.size()]);
        }
        return selectedResources;
    }

    /**
     * Returns the shell
     */
    private Shell getShell() {
        return getViewer().getControl().getShell();
    }

    /**
     * Returns an error status with the given info.
     */
    private IStatus info(String message) {
        return new Status(IStatus.INFO, PlatformUI.PLUGIN_ID, 0, message, null);
    }

    /**
     * Adds the given status to the list of problems.  Discards
     * OK statuses.  If the status is a multi-status, only its children
     * are added.
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
        return new Status(Status.OK, PlatformUI.PLUGIN_ID, 0,
                ResourceNavigatorMessages.getString("DropAdapter.ok"), null); //$NON-NLS-1$
    }

    /**
     * Opens an error dialog if necessary.  Takes care of
     * complex rules necessary for making the error dialog look nice.
     */
    private void openError(IStatus status) {
        if (status == null)
            return;

        String genericTitle = ResourceNavigatorMessages
                .getString("DropAdapter.title"); //$NON-NLS-1$
        int codes = IStatus.ERROR | IStatus.WARNING;

        //simple case: one error, not a multistatus
        if (!status.isMultiStatus()) {
            ErrorDialog
                    .openError(getShell(), genericTitle, null, status, codes);
            return;
        }

        //one error, single child of multistatus
        IStatus[] children = status.getChildren();
        if (children.length == 1) {
            ErrorDialog.openError(getShell(), status.getMessage(), null,
                    children[0], codes);
            return;
        }
        //several problems
        ErrorDialog.openError(getShell(), genericTitle, null, status, codes);
    }

    /**
     * Perform the drop.
     * @see DropTargetListener#drop(org.eclipse.swt.dnd.DropTargetEvent)
     */
    public boolean performDrop(final Object data) {
        alwaysOverwrite = false;
        if (getCurrentTarget() == null || data == null) {
            return false;
        }
        boolean result = false;
        IStatus status = null;
        IResource[] resources = null;
        TransferData currentTransfer = getCurrentTransfer();
        if (LocalSelectionTransfer.getInstance().isSupportedType(
                currentTransfer)) {
            resources = getSelectedResources();
        } else if (ResourceTransfer.getInstance().isSupportedType(
                currentTransfer)) {
            resources = (IResource[]) data;
        } else if (FileTransfer.getInstance().isSupportedType(currentTransfer)) {
            status = performFileDrop(data);
            result = status.isOK();
        } else {
            result = NavigatorDropAdapter.super.performDrop(data);
        }
        if (resources != null) {
            if (getCurrentOperation() == DND.DROP_COPY)
                status = performResourceCopy(getShell(), resources);
            else
                status = performResourceMove(resources);
        }
        openError(status);
        return result;
    }

    /**
     * Performs a drop using the FileTransfer transfer type.
     */
    private IStatus performFileDrop(Object data) {
        MultiStatus problems = new MultiStatus(PlatformUI.PLUGIN_ID, 0,
                ResourceNavigatorMessages
                        .getString("DropAdapter.problemImporting"), null); //$NON-NLS-1$
        mergeStatus(problems, validateTarget(getCurrentTarget(),
                getCurrentTransfer()));

        final IContainer target = getActualTarget((IResource) getCurrentTarget());
        final String[] names = (String[]) data;
        // Run the import operation asynchronously. 
        // Otherwise the drag source (e.g., Windows Explorer) will be blocked 
        // while the operation executes. Fixes bug 16478.
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                getShell().forceActive();
                CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(
                        getShell());
                operation.copyFiles(names, target);
            }
        });
        return problems;
    }

    /**
     * Performs a resource copy
     */
    private IStatus performResourceCopy(Shell shell, IResource[] sources) {
        MultiStatus problems = new MultiStatus(PlatformUI.PLUGIN_ID, 1,
                ResourceNavigatorMessages
                        .getString("DropAdapter.problemsMoving"), null); //$NON-NLS-1$
        mergeStatus(problems, validateTarget(getCurrentTarget(),
                getCurrentTransfer()));

        IContainer target = getActualTarget((IResource) getCurrentTarget());
        CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(
                shell);
        operation.copyResources(sources, target);

        return problems;
    }

    /**
     * Performs a resource move
     */
    private IStatus performResourceMove(IResource[] sources) {
        MultiStatus problems = new MultiStatus(PlatformUI.PLUGIN_ID, 1,
                ResourceNavigatorMessages
                        .getString("DropAdapter.problemsMoving"), null); //$NON-NLS-1$
        mergeStatus(problems, validateTarget(getCurrentTarget(),
                getCurrentTransfer()));

        IContainer target = getActualTarget((IResource) getCurrentTarget());
        ReadOnlyStateChecker checker = new ReadOnlyStateChecker(
                getShell(),
                ResourceNavigatorMessages.getString("MoveResourceAction.title"), //$NON-NLS-1$
                ResourceNavigatorMessages
                        .getString("MoveResourceAction.checkMoveMessage"));//$NON-NLS-1$	
        sources = checker.checkReadOnlyResources(sources);
        MoveFilesAndFoldersOperation operation = new MoveFilesAndFoldersOperation(
                getShell());
        operation.copyResources(sources, target);

        return problems;
    }

    /*
     * @see IOverwriteQuery#queryOverwrite(String)
     */
    public String queryOverwrite(String pathString) {
        if (alwaysOverwrite)
            return ALL;

        final String returnCode[] = { CANCEL };
        final String msg = ResourceNavigatorMessages.format(
                "DropAdapter.overwriteQuery", new Object[] { pathString }); //$NON-NLS-1$
        final String[] options = { IDialogConstants.YES_LABEL,
                IDialogConstants.YES_TO_ALL_LABEL, IDialogConstants.NO_LABEL,
                IDialogConstants.CANCEL_LABEL };
        getDisplay().syncExec(new Runnable() {
            public void run() {
                MessageDialog dialog = new MessageDialog(
                        getShell(),
                        ResourceNavigatorMessages
                                .getString("DropAdapter.question"), null, msg, MessageDialog.QUESTION, options, 0); //$NON-NLS-1$
                dialog.open();
                int returnVal = dialog.getReturnCode();
                String[] returnCodes = { YES, ALL, NO, CANCEL };
                returnCode[0] = returnVal < 0 ? CANCEL : returnCodes[returnVal];
            }
        });
        if (returnCode[0] == ALL)
            alwaysOverwrite = true;
        return returnCode[0];
    }

    /**
     * This method is used to notify the action that some aspect of
     * the drop operation has changed.
     */
    public boolean validateDrop(Object target, int dragOperation,
            TransferData transferType) {

        if (dragOperation != DND.DROP_NONE) {
            lastValidOperation = dragOperation;
        }
        if (FileTransfer.getInstance().isSupportedType(transferType)
                && lastValidOperation != DND.DROP_COPY) {
            // only allow copying when dragging from outside Eclipse
            return false;
        }
        if (super.validateDrop(target, dragOperation, transferType)) {
            return true;
        }
        return validateTarget(target, transferType).isOK();
    }

    /**
     * Ensures that the drop target meets certain criteria
     */
    private IStatus validateTarget(Object target, TransferData transferType) {
        if (!(target instanceof IResource)) {
            return info(ResourceNavigatorMessages
                    .getString("DropAdapter.targetMustBeResource")); //$NON-NLS-1$
        }
        IResource resource = (IResource) target;
        if (!resource.isAccessible()) {
            return error(ResourceNavigatorMessages
                    .getString("DropAdapter.canNotDropIntoClosedProject")); //$NON-NLS-1$
        }
        IContainer destination = getActualTarget(resource);
        if (destination.getType() == IResource.ROOT) {
            return error(ResourceNavigatorMessages
                    .getString("DropAdapter.resourcesCanNotBeSiblings")); //$NON-NLS-1$
        }
        String message = null;
        // drag within Eclipse?
        if (LocalSelectionTransfer.getInstance().isSupportedType(transferType)) {
            IResource[] selectedResources = getSelectedResources();

            if (selectedResources == null)
                message = ResourceNavigatorMessages
                        .getString("DropAdapter.dropOperationErrorOther"); //$NON-NLS-1$
            else {
                CopyFilesAndFoldersOperation operation;
                if (lastValidOperation == DND.DROP_COPY) {
                    operation = new CopyFilesAndFoldersOperation(getShell());
                } else {
                    operation = new MoveFilesAndFoldersOperation(getShell());
                }
                message = operation.validateDestination(destination,
                        selectedResources);
            }
        } // file import?
        else if (FileTransfer.getInstance().isSupportedType(transferType)) {
            String[] sourceNames = (String[]) FileTransfer.getInstance()
                    .nativeToJava(transferType);
            if (sourceNames == null) {
                // source names will be null on Linux. Use empty names to do destination validation.
                // Fixes bug 29778
                sourceNames = new String[0];
            }
            CopyFilesAndFoldersOperation copyOperation = new CopyFilesAndFoldersOperation(
                    getShell());
            message = copyOperation.validateImportDestination(destination,
                    sourceNames);
        }
        if (message != null) {
            return error(message);
        }
        return ok();
    }
}