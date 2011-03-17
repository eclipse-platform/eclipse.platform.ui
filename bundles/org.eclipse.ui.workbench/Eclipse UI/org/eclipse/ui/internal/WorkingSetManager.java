/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tasktop Technologies - fix for bug 327396
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.statushandlers.IStatusAdapterConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleListener;

/**
 * A working set manager stores working sets and provides property change
 * notification when a working set is added or removed. Working sets are
 * persisted whenever one is added or removed.
 * 
 * @see IWorkingSetManager
 * @since 2.0
 */
public class WorkingSetManager extends AbstractWorkingSetManager implements
		IWorkingSetManager, BundleListener {

	// Working set persistence
	public static final String WORKING_SET_STATE_FILENAME = "workingsets.xml"; //$NON-NLS-1$

	private boolean restoreInProgress;

	private boolean savePending;

	public WorkingSetManager(BundleContext context) {
		super(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkingSetManager
	 */
	public void addRecentWorkingSet(IWorkingSet workingSet) {
		internalAddRecentWorkingSet(workingSet);
		saveState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkingSetManager
	 */
	public void addWorkingSet(IWorkingSet workingSet) {
		super.addWorkingSet(workingSet);
		saveState();
	}

	/**
	 * Returns the file used as the persistence store, or <code>null</code> if
	 * there is no available file.
	 * 
	 * @return the file used as the persistence store, or <code>null</code>
	 */
	private File getWorkingSetStateFile() {
		IPath path = WorkbenchPlugin.getDefault().getDataLocation();
		if (path == null) {
			return null;
		}
		path = path.append(WORKING_SET_STATE_FILENAME);
		return path.toFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkingSetManager
	 */
	public void removeWorkingSet(IWorkingSet workingSet) {
		if (internalRemoveWorkingSet(workingSet)) {
			saveState();
		}
	}

	/**
	 * Reads the persistence store and creates the working sets stored in it.
	 */
	public void restoreState() {
		File stateFile = getWorkingSetStateFile();

		if (stateFile != null && stateFile.exists()) {
			try {
				restoreInProgress = true;

				FileInputStream input = new FileInputStream(stateFile);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(input, "utf-8")); //$NON-NLS-1$

				IMemento memento = XMLMemento.createReadRoot(reader);
				restoreWorkingSetState(memento);
				restoreMruList(memento);
				reader.close();
			} catch (IOException e) {
				handleInternalError(
						e,
						WorkbenchMessages.ProblemRestoringWorkingSetState_title,
						WorkbenchMessages.ProblemRestoringWorkingSetState_message);
			} catch (WorkbenchException e) {
				handleInternalError(
						e,
						WorkbenchMessages.ProblemRestoringWorkingSetState_title,
						WorkbenchMessages.ProblemRestoringWorkingSetState_message);
			} finally {
				restoreInProgress = false;
			}

			if (savePending) {
				saveState();
				savePending = false;
			}
		}
	}

	/**
	 * Saves the working sets in the persistence store
	 */
	private void saveState() {
		if (restoreInProgress) {
			// bug 327396: avoid saving partial state
			savePending = true;
			return;
		}

		File stateFile = getWorkingSetStateFile();
		if (stateFile == null) {
			return;
		}
		try {
			saveState(stateFile);
		} catch (IOException e) {
			stateFile.delete();
			handleInternalError(e,
					WorkbenchMessages.ProblemSavingWorkingSetState_title,
					WorkbenchMessages.ProblemSavingWorkingSetState_message);
		}
	}

	/**
	 * Persists all working sets and fires a property change event for the
	 * changed working set. Should only be called by
	 * org.eclipse.ui.internal.WorkingSet.
	 * 
	 * @param changedWorkingSet
	 *            the working set that has changed
	 * @param propertyChangeId
	 *            the changed property. one of CHANGE_WORKING_SET_CONTENT_CHANGE
	 *            and CHANGE_WORKING_SET_NAME_CHANGE
	 */
	public void workingSetChanged(IWorkingSet changedWorkingSet,
			String propertyChangeId, Object oldValue) {
		saveState();
		super.workingSetChanged(changedWorkingSet, propertyChangeId, oldValue);
	}

	/**
	 * Show and Log the exception using StatusManager.
	 */
	private void handleInternalError(Exception exp, String title, String message) {
		Status status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
				message, exp);
		StatusAdapter sa = new StatusAdapter(status);
		sa.setProperty(IStatusAdapterConstants.TITLE_PROPERTY, title);
		StatusManager.getManager().handle(sa, StatusManager.LOG);
	}
}
