/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.navigator;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.eclipse.ui.actions.MoveFilesAndFoldersOperation;
import org.eclipse.ui.actions.ReadOnlyStateChecker;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.ide.dialogs.ImportTypeDialog;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages;
import org.eclipse.ui.part.PluginDropAdapter;
import org.eclipse.ui.part.ResourceTransfer;


/**
 * Implements drop behaviour for drag and drop operations
 * that land on the resource navigator.
 * 
 * @since 2.0
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
 */
public class NavigatorDropAdapter extends PluginDropAdapter implements IOverwriteQuery {

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
     * 
     * @param viewer the navigator's viewer
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


    /* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#dragOperationChanged(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void dragOperationChanged(DropTargetEvent event) {
		super.dragOperationChanged(event);
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
        ArrayList selectedResources = new ArrayList();

        ISelection selection = LocalSelectionTransfer.getInstance()
                .getSelection();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ssel = (IStructuredSelection) selection;
            for (Iterator i = ssel.iterator(); i.hasNext();) {
                Object o = i.next();
                if (o instanceof IResource) {
                    selectedResources.add(o);
                }
                else if (o instanceof IAdaptable) {
                    IAdaptable a = (IAdaptable) o;
                    IResource r = (IResource) a.getAdapter(IResource.class);
                    if (r != null) {
                        selectedResources.add(r);
                    }
                }
            }
        }
        return (IResource[]) selectedResources.toArray(new IResource[selectedResources.size()]);
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
        return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0,
                ResourceNavigatorMessages.DropAdapter_ok, null);
    }

    /**
     * Opens an error dialog if necessary.  Takes care of
     * complex rules necessary for making the error dialog look nice.
     */
    private void openError(IStatus status) {
        if (status == null) {
			return;
		}

        String genericTitle = ResourceNavigatorMessages.DropAdapter_title;
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
     * @see org.eclipse.swt.dnd.DropTargetListener#drop(org.eclipse.swt.dnd.DropTargetEvent)
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
        if (resources != null && resources.length > 0) {
            if (getCurrentOperation() == DND.DROP_COPY) {
				status = performResourceCopy(getShell(), resources);
			} else {
				status = performResourceMove(resources);
			}
        }
        openError(status);
        return result;
    }

    /**
     * Performs a drop using the FileTransfer transfer type.
     */
    private IStatus performFileDrop(Object data) {
        MultiStatus problems = new MultiStatus(PlatformUI.PLUGIN_ID, 0,
                ResourceNavigatorMessages.DropAdapter_problemImporting, null);
        mergeStatus(problems, validateTarget(getCurrentTarget(),
                getCurrentTransfer()));

		final int currentOperation= getCurrentOperation();
        final IContainer target = getActualTarget((IResource) getCurrentTarget());
        final String[] names = (String[]) data;
        // Run the import operation asynchronously.
        // Otherwise the drag source (e.g., Windows Explorer) will be blocked
        // while the operation executes. Fixes bug 16478.
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                getShell().forceActive();
				new CopyFilesAndFoldersOperation(getShell()).copyOrLinkFiles(names, target, currentOperation);
            }
        });
        return problems;
    }

	/**
     * Performs a resource copy
     */
    private IStatus performResourceCopy(Shell shell, IResource[] sources) {
        MultiStatus problems = new MultiStatus(PlatformUI.PLUGIN_ID, 1,
                ResourceNavigatorMessages.DropAdapter_problemsMoving, null);
        mergeStatus(problems, validateTarget(getCurrentTarget(),
                getCurrentTransfer()));

        IContainer target = getActualTarget((IResource) getCurrentTarget());

        boolean shouldLinkAutomatically = false;
		if (target.isVirtual()) {
			shouldLinkAutomatically = true;
			for (int i = 0; i < sources.length; i++) {
				if (sources[0].getType() != IResource.FILE) {
					shouldLinkAutomatically = false;
					break;
				}
			}
		}

		CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(
                shell);

		// if the target is a group and all sources are files, then automatically create links
		if (shouldLinkAutomatically) {
			operation.setCreateLinks(true);
			operation.copyResources(sources, target);
		}
		else {
			boolean allSourceAreLinksOrGroups = true;
			for (int i = 0; i < sources.length; i++) {
				if (!sources[0].isVirtual() && !sources[0].isLinked()) {
					allSourceAreLinksOrGroups = false;
					break;
				}
			}
			// if all sources are either links or groups, copy then normally, don't show the dialog
			if (!allSourceAreLinksOrGroups) {
				IPreferenceStore store= IDEWorkbenchPlugin.getDefault().getPreferenceStore();
				String dndPreference= store.getString(target.isVirtual() ? IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_VIRTUAL_FOLDER_MODE : IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_MODE);

				if (dndPreference.equals(IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_MODE_PROMPT)) {
					ImportTypeDialog dialog = new ImportTypeDialog(getShell(), getCurrentOperation(), sources, target);
					dialog.setResource(target);
					if (dialog.open() == Window.OK) {
						if (dialog.getSelection() == ImportTypeDialog.IMPORT_VIRTUAL_FOLDERS_AND_LINKS)
							operation.setVirtualFolders(true);
						if (dialog.getSelection() == ImportTypeDialog.IMPORT_LINK)
							operation.setCreateLinks(true);
						if (dialog.getVariable() != null)
							operation.setRelativeVariable(dialog.getVariable());
						operation.copyResources(sources, target);
					}
					else
						return problems;
				}
				else
					operation.copyResources(sources, target);
			}
			else
				operation.copyResources(sources, target);
		}

        return problems;
    }

    /**
     * Performs a resource move
     */
    private IStatus performResourceMove(IResource[] sources) {
        MultiStatus problems = new MultiStatus(PlatformUI.PLUGIN_ID, 1,
                ResourceNavigatorMessages.DropAdapter_problemsMoving, null);
        mergeStatus(problems, validateTarget(getCurrentTarget(),
                getCurrentTransfer()));

        IContainer target = getActualTarget((IResource) getCurrentTarget());
        
        boolean shouldLinkAutomatically = false;
		if (target.isVirtual()) {
			shouldLinkAutomatically = true;
			for (int i = 0; i < sources.length; i++) {
				if (sources[0].isVirtual() || sources[0].isLinked()) {
					shouldLinkAutomatically = false;
					break;
				}
			}
		}
		
		if (shouldLinkAutomatically) {
			CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(
	                getShell());
			operation.setCreateLinks(true);
	        operation.copyResources(sources, target);
		}
		else {
			ReadOnlyStateChecker checker = new ReadOnlyStateChecker(
	                getShell(),
	                ResourceNavigatorMessages.MoveResourceAction_title,
	                ResourceNavigatorMessages.MoveResourceAction_checkMoveMessage);
	        sources = checker.checkReadOnlyResources(sources);
	        MoveFilesAndFoldersOperation operation = new MoveFilesAndFoldersOperation(
	                getShell());
	        operation.copyResources(sources, target);
		}

        return problems;
    }

    /*
     * @see org.eclipse.ui.dialogs.IOverwriteQuery#queryOverwrite(java.lang.String)
     */
    public String queryOverwrite(String pathString) {
        if (alwaysOverwrite) {
			return ALL;
		}

        final String returnCode[] = { CANCEL };
        final String msg = NLS.bind(ResourceNavigatorMessages.DropAdapter_overwriteQuery, pathString);
        final String[] options = { IDialogConstants.YES_LABEL,
                IDialogConstants.YES_TO_ALL_LABEL, IDialogConstants.NO_LABEL,
                IDialogConstants.CANCEL_LABEL };
        getDisplay().syncExec(new Runnable() {
            public void run() {
                MessageDialog dialog = new MessageDialog(
                        getShell(),
                        ResourceNavigatorMessages.DropAdapter_question, null, msg, MessageDialog.QUESTION, options, 0);
                dialog.open();
                int returnVal = dialog.getReturnCode();
                String[] returnCodes = { YES, ALL, NO, CANCEL };
                returnCode[0] = returnVal < 0 ? CANCEL : returnCodes[returnVal];
            }
        });
        if (returnCode[0] == ALL) {
			alwaysOverwrite = true;
		}
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
                && (lastValidOperation != DND.DROP_COPY)) {
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
            return info(ResourceNavigatorMessages.DropAdapter_targetMustBeResource);
        }
        IResource resource = (IResource) target;
        if (!resource.isAccessible()) {
            return error(ResourceNavigatorMessages.DropAdapter_canNotDropIntoClosedProject);
        }
        IContainer destination = getActualTarget(resource);
        if (destination.getType() == IResource.ROOT) {
            return error(ResourceNavigatorMessages.DropAdapter_resourcesCanNotBeSiblings);
        }
        String message = null;
        // drag within Eclipse?
        if (LocalSelectionTransfer.getInstance().isSupportedType(transferType)) {
            IResource[] selectedResources = getSelectedResources();

            if (selectedResources.length == 0) {
				message = ResourceNavigatorMessages.DropAdapter_dropOperationErrorOther;
			} else {
                CopyFilesAndFoldersOperation operation;
                if (lastValidOperation == DND.DROP_COPY) {
                    operation = new CopyFilesAndFoldersOperation(getShell());
					if (operation.validateDestination(destination, selectedResources) != null) {
						operation.setVirtualFolders(true);
						message = operation.validateDestination(destination, selectedResources);
					}
                } else {
                    operation = new MoveFilesAndFoldersOperation(getShell());
                	if (operation.validateDestination(destination, selectedResources) != null) {
						operation.setVirtualFolders(true);
						message = operation.validateDestination(destination, selectedResources);
						if (message == null)
							lastValidOperation = DND.DROP_COPY;
					}
                }
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
