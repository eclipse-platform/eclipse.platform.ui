/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.ide.undo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.ui.internal.ide.undo.MarkerDescription;
import org.eclipse.ui.internal.ide.undo.UndoMessages;
import org.eclipse.ui.views.markers.internal.MarkerType;
import org.eclipse.ui.views.markers.internal.MarkerTypesModel;

/**
 * An AbstractMarkersOperation represents an undoable operation that affects
 * markers on a resource. It provides implementations for marker creation,
 * deletion, and updating. Clients may call the public API from a background
 * thread.
 * 
 * This class is not intended to be subclassed by clients.
 * 
 * @since 3.3
 * 
 */
abstract class AbstractMarkersOperation extends AbstractWorkspaceOperation {

	MarkerDescription[] markerDescriptions;

	IMarker[] markers;

	Map[] attributes;

	/**
	 * Create an AbstractMarkersOperation by specifying a combination of markers
	 * and attributes or marker descriptions.
	 * 
	 * @param markers
	 *            the markers used in the operation or <code>null</code> if no
	 *            markers yet exist
	 * @param markerDescriptions
	 *            the marker descriptions that should be used to create markers,
	 *            or <code>null</code> if the markers already exist
	 * @param attributes
	 *            The map of attributes that should be assigned to any existing
	 *            markers when the markers are updated. Ignored if the markers
	 *            parameter is <code>null</code>.
	 * @param name
	 *            the name used to describe the operation
	 */
	AbstractMarkersOperation(IMarker[] markers,
			MarkerDescription[] markerDescriptions, Map attributes, String name) {
		super(name);
		this.markers = markers;
		this.attributes = null;
		// If there is more than one marker, create an array with a copy
		// of the attributes map for each marker. Keeping a unique map
		// per marker allows us to support the scenario where attributes
		// are merged when updated. In this case, each marker's attributes
		// may differ since their original attributes may have differed.
		if (attributes != null && markers != null) {
			if (markers.length > 1) {
				this.attributes = new Map[markers.length];
				for (int i = 0; i < markers.length; i++) {
					Map copiedAttributes = new HashMap();
					copiedAttributes.putAll(attributes);
					this.attributes[i] = copiedAttributes;
				}
			} else {
				this.attributes = new Map[] { attributes };
			}
		}
		setMarkerDescriptions(markerDescriptions);
	}

	/**
	 * Delete any currently known markers and save their information in marker
	 * descriptions so that they can be restored.
	 * 
	 * @param work
	 *            the number of work ticks to be used by the delete
	 * @param monitor
	 *            the progress monitor to use for the delete
	 * @throws CoreException
	 *             propagates any CoreExceptions thrown from the resources API
	 * 
	 */
	protected void deleteMarkers(int work, IProgressMonitor monitor)
			throws CoreException {
		if (markers == null || markers.length == 0) {
			monitor.worked(work);
			return;
		}
		int markerWork = work / markers.length;
		markerDescriptions = new MarkerDescription[markers.length];
		for (int i = 0; i < markers.length; i++) {
			markerDescriptions[i] = new MarkerDescription(markers[i]);
			markers[i].delete();
			monitor.worked(markerWork);
		}
		markers = new IMarker[0];
	}

	/**
	 * Create markers from any currently known marker descriptions.
	 * 
	 * @param work
	 *            the number of work ticks to be used by the create
	 * @param monitor
	 *            the progress monitor to use for the create
	 * @throws CoreException
	 *             propagates any CoreExceptions thrown from the resources API
	 */
	protected void createMarkers(int work, IProgressMonitor monitor)
			throws CoreException {
		if (markerDescriptions == null || markerDescriptions.length == 0) {
			monitor.worked(work);
			return;
		}
		int markerWork = work / markerDescriptions.length;
		markers = new IMarker[markerDescriptions.length];

		// Recreate the markers from the descriptions
		for (int i = 0; i < markerDescriptions.length; i++) {
			markers[i] = markerDescriptions[i].createMarker();
			monitor.worked(markerWork);
		}
	}

	/**
	 * Update the currently known markers with the corresponding array of marker
	 * descriptions.
	 * 
	 * @param work
	 *            the number of work ticks to be used by the update
	 * @param monitor
	 *            the progress monitor to use for the update
	 * @param mergeAttributes
	 *            a boolean specifying whether the attributes are merged or
	 *            considered to be a replacement of the previous attributes.
	 * @throws CoreException
	 *             propagates any CoreExceptions thrown from the resources API
	 * 
	 */
	protected void updateMarkers(int work, IProgressMonitor monitor,
			boolean mergeAttributes) throws CoreException {
		if (attributes == null || markers == null
				|| attributes.length != markers.length || markers.length == 0) {
			monitor.worked(work);
			return;
		}
		int markerWork = work / markers.length;
		for (int i = 0; i < markers.length; i++) {
			if (mergeAttributes) {
				Map oldAttributes = markers[i].getAttributes();
				int increment = markerWork / attributes[i].size();
				Map replacedAttributes = new HashMap();

				for (Iterator iter = attributes[i].keySet().iterator(); iter
						.hasNext();) {
					String key = (String) iter.next();
					Object val = attributes[i].get(key);
					markers[i].setAttribute(key, val);
					replacedAttributes.put(key, oldAttributes.get(key));
					monitor.worked(increment);
				}
				attributes[i] = replacedAttributes;
			} else {
				// replace all of the attributes
				Map oldAttributes = markers[i].getAttributes();
				markers[i].setAttributes(attributes[i]);
				attributes[i] = oldAttributes;
			}
		}
	}

	/**
	 * Set the marker descriptions that describe markers that can be created.
	 * 
	 * @param descriptions
	 *            the descriptions of markers that can be created.
	 */
	protected void setMarkerDescriptions(MarkerDescription[] descriptions) {
		markerDescriptions = descriptions;
		addUndoContexts();
		updateTargetResources();
	}

	/*
	 * Update the target resources by traversing the currently known markers or
	 * marker descriptions and getting their resources.
	 */

	private void updateTargetResources() {
		IResource[] resources = null;
		if (markers == null) {
			if (markerDescriptions != null) {
				resources = new IResource[markerDescriptions.length];
				for (int i = 0; i < markerDescriptions.length; i++) {
					resources[i] = markerDescriptions[i].getResource();
				}
			}
		} else {
			resources = new IResource[markers.length];
			for (int i = 0; i < markers.length; i++) {
				resources[i] = markers[i].getResource();
			}
		}
		setTargetResources(resources);
	}

	/*
	 * Add undo contexts according to marker types. Any unknown marker types
	 * will cause the workspace undo context to be added.
	 * 
	 * This is an optimization that allows us to add specific undo contexts for
	 * tasks and bookmarks, without also adding the workspace undo context. Note
	 * that clients with different marker types may still assign their own
	 * specific undo context using AbstractOperation.addContext(IUndoContext) in
	 * addition to the workspace context assigned by this method.
	 */

	private void addUndoContexts() {
		String[] types = null;
		if (markers == null) {
			if (markerDescriptions != null) {
				types = new String[markerDescriptions.length];
				for (int i = 0; i < markerDescriptions.length; i++) {
					types[i] = markerDescriptions[i].getType();
				}
			}
		} else {
			types = new String[markers.length];
			for (int i = 0; i < markers.length; i++) {
				try {
					types[i] = markers[i].getType();
				} catch (CoreException e) {
				}

			}
		}

		if (types != null) {
			MarkerType bookmarkType= MarkerTypesModel.getInstance().getType(IMarker.BOOKMARK);
			MarkerType taskType= MarkerTypesModel.getInstance().getType(IMarker.TASK);
			MarkerType problemType= MarkerTypesModel.getInstance().getType(IMarker.PROBLEM);

			for (int i = 0; i < types.length; i++) {
				// Marker type could be null if marker did not exist.
				// This shouldn't happen, but can.
				// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=158129
				if (types[i] != null) {
					MarkerType type= MarkerTypesModel.getInstance().getType(types[i]);
					if (type == null) {
						// type is not known, use the workspace undo context
						addContext(WorkspaceUndoUtil.getWorkspaceUndoContext());
					} else if (type.isSubtypeOf(bookmarkType)) {
						addContext(WorkspaceUndoUtil.getBookmarksUndoContext());
					} else if (type.isSubtypeOf(taskType)) {
						addContext(WorkspaceUndoUtil.getTasksUndoContext());
					} else if (type.isSubtypeOf(problemType)) {
						addContext(WorkspaceUndoUtil.getProblemsUndoContext());
					} else {
						// type is not known, use the workspace undo context
						addContext(WorkspaceUndoUtil.getWorkspaceUndoContext());
					}
				}
			}
		}
	}

	/**
	 * Return the array of markers that has been updated or created.
	 * 
	 * @return the array of markers that have been updated or created, or
	 *         <code>null</code> if no markers have been created or updated.
	 */
	public IMarker[] getMarkers() {
		return markers;
	}

	/**
	 * Return whether the markers known by this operation currently exist.
	 * 
	 * @return <code>true</code> if there are existing markers and
	 *         <code>false</code> if there are no known markers or any one of
	 *         them does not exist
	 */
	protected boolean markersExist() {
		if (markers == null || markers.length == 0) {
			return false;
		}
		for (int i = 0; i < markers.length; i++) {
			if (!markers[i].exists()) {
				return false;
			}
		}
		return true;

	}

	/**
	 * Return a status indicating the projected outcome of undoing the marker
	 * operation. The receiver is not responsible for remembering the result of
	 * this computation.
	 * 
	 * @return the status indicating whether the operation can be undone
	 */
	protected abstract IStatus getBasicUndoStatus();

	/**
	 * Return a status indicating the projected outcome of redoing the marker
	 * operation. The receiver is not responsible for remembering the result of
	 * this computation.
	 * 
	 * @return the status indicating whether the operation can be undone
	 */
	protected abstract IStatus getBasicRedoStatus();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#computeExecutionStatus(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus computeExecutionStatus(IProgressMonitor monitor) {
		IStatus status = getBasicRedoStatus();
		if (status.isOK()) {
			return super.computeExecutionStatus(monitor);
		}
		if (status.getSeverity() == IStatus.ERROR) {
			markInvalid();
		}
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#computeUndoableStatus(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus computeUndoableStatus(IProgressMonitor monitor) {
		IStatus status = getBasicUndoStatus();
		if (status.isOK()) {
			return super.computeUndoableStatus(monitor);
		}
		if (status.getSeverity() == IStatus.ERROR) {
			markInvalid();
		}
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#computeRedoableStatus(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus computeRedoableStatus(IProgressMonitor monitor) {
		IStatus status = getBasicRedoStatus();
		if (status.isOK()) {
			return super.computeRedoableStatus(monitor);
		}
		if (status.getSeverity() == IStatus.ERROR) {
			markInvalid();
		}
		return status;
	}

	/**
	 * Compute the status for deleting any known markers. A status severity of
	 * <code>OK</code> indicates that the delete is likely to be successful. A
	 * status severity of <code>ERROR</code> indicates that the operation is
	 * no longer valid. Other status severities are open to interpretation by
	 * the caller.
	 * 
	 * @return the status indicating the projected outcome of deleting the
	 *         markers.
	 * 
	 */
	protected IStatus getMarkerDeletionStatus() {
		if (markersExist()) {
			return Status.OK_STATUS;
		}
		return getErrorStatus(UndoMessages.MarkerOperation_MarkerDoesNotExist);
	}

	/**
	 * Compute the status for creating any known markers. A status severity of
	 * <code>OK</code> indicates that the create is likely to be successful. A
	 * status severity of <code>ERROR</code> indicates that the operation is
	 * no longer valid. Other status severities are open to interpretation by
	 * the caller.
	 * 
	 * @return the status indicating the projected outcome of creating the
	 *         markers.
	 * 
	 */
	protected IStatus getMarkerCreationStatus() {
		if (!resourcesExist()) {
			return getErrorStatus(UndoMessages.MarkerOperation_ResourceDoesNotExist);
		} else if (markerDescriptions == null) {
			return getErrorStatus(UndoMessages.MarkerOperation_NotEnoughInfo);
		}
		return Status.OK_STATUS;
	}

	/**
	 * Compute the status for updating any known markers. A status severity of
	 * <code>OK</code> indicates that the update is likely to be successful. A
	 * status severity of <code>ERROR</code> indicates that the operation is
	 * no longer valid. Other status severities are open to interpretation by
	 * the caller.
	 * 
	 * @return the status indicating the projected outcome of updating the
	 *         markers.
	 * 
	 */
	protected IStatus getMarkerUpdateStatus() {
		if (!markersExist()) {
			return getErrorStatus(UndoMessages.MarkerOperation_MarkerDoesNotExist);
		} else if (attributes == null) {
			return getErrorStatus(UndoMessages.MarkerOperation_NotEnoughInfo);
		}
		return Status.OK_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#getExecuteSchedulingRule()
	 */
	protected ISchedulingRule getExecuteSchedulingRule() {
		ISchedulingRule[] ruleArray = new ISchedulingRule[resources.length];
		for (int i = 0; i < resources.length; i++) {
			ruleArray[i] = getWorkspaceRuleFactory().markerRule(resources[i]);
		}
		return MultiRule.combine(ruleArray);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#getUndoSchedulingRule()
	 */
	protected ISchedulingRule getUndoSchedulingRule() {
		return getExecuteSchedulingRule();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#appendDescriptiveText(java.lang.StringBuffer)
	 */
	protected void appendDescriptiveText(StringBuffer text) {
		super.appendDescriptiveText(text);
		text.append(" markers: "); //$NON-NLS-1$
		text.append(markers);
		text.append('\'');
		text.append(" markerDescriptions: "); //$NON-NLS-1$
		text.append(markerDescriptions);
		text.append('\'');
		text.append(" attributes: "); //$NON-NLS-1$
		text.append(attributes);
		text.append('\'');
	}
}
