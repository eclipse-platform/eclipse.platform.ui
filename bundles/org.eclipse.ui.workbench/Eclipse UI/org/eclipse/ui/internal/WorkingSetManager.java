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
package org.eclipse.ui.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.eclipse.core.runtime.IPath;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleListener;

/**
 * A working set manager stores working sets and provides property 
 * change notification when a working set is added or removed.
 * Working sets are persisted whenever one is added or removed.
 * 
 * @see IWorkingSetManager
 * @since 2.0
 */
public class WorkingSetManager extends AbstractWorkingSetManager implements IWorkingSetManager, BundleListener {

    // Working set persistence
    private static final String WORKING_SET_STATE_FILENAME = "workingsets.xml"; //$NON-NLS-1$

    public WorkingSetManager(BundleContext context) {
    	super(context);
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkingSetManager
     */
    public void addRecentWorkingSet(IWorkingSet workingSet) {
        internalAddRecentWorkingSet(workingSet);
        saveState();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkingSetManager
     */
    public void addWorkingSet(IWorkingSet workingSet) {
    	super.addWorkingSet(workingSet);
        saveState();
    }

    /**
     * Returns the file used as the persistence store
     * 
     * @return the file used as the persistence store
     */
    private File getWorkingSetStateFile() {
        IPath path = WorkbenchPlugin.getDefault().getStateLocation();
        path = path.append(WORKING_SET_STATE_FILENAME);
        return path.toFile();
    }

   /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkingSetManager
     */
    public void removeWorkingSet(IWorkingSet workingSet) {
        if (internalRemoveWorkingSet(workingSet)) {
            saveState();
        }
    }

    /**
     * Reads the persistence store and creates the working sets 
     * stored in it.
     */
    public void restoreState() {
        File stateFile = getWorkingSetStateFile();

        if (stateFile.exists()) {
            try {
                FileInputStream input = new FileInputStream(stateFile);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(input, "utf-8")); //$NON-NLS-1$

                IMemento memento = XMLMemento.createReadRoot(reader);
                restoreWorkingSetState(memento);
                restoreMruList(memento);
                reader.close();
            } catch (IOException e) {
                MessageDialog
                        .openError(
                                (Shell) null,
                                WorkbenchMessages
                                        .getString("ProblemRestoringWorkingSetState.title"), //$NON-NLS-1$
                                WorkbenchMessages
                                        .getString("ProblemRestoringWorkingSetState.message")); //$NON-NLS-1$
            } catch (WorkbenchException e) {
                ErrorDialog
                        .openError(
                                (Shell) null,
                                WorkbenchMessages
                                        .getString("ProblemRestoringWorkingSetState.title"),//$NON-NLS-1$
                                WorkbenchMessages
                                        .getString("ProblemRestoringWorkingSetState.message"), //$NON-NLS-1$
                                e.getStatus());
            }
        }
    }

    /**
     * Saves the working sets in the persistence store
     */
    private void saveState() {
        XMLMemento memento = XMLMemento
                .createWriteRoot(IWorkbenchConstants.TAG_WORKING_SET_MANAGER);
        File stateFile = getWorkingSetStateFile();

        saveWorkingSetState(memento);
        saveMruList(memento);
        try {
            FileOutputStream stream = new FileOutputStream(stateFile);
            OutputStreamWriter writer = new OutputStreamWriter(stream, "utf-8"); //$NON-NLS-1$
            memento.save(writer);
            writer.close();
        } catch (IOException e) {
            stateFile.delete();
            MessageDialog.openError((Shell) null, WorkbenchMessages
                    .getString("ProblemSavingWorkingSetState.title"), //$NON-NLS-1$
                    WorkbenchMessages
                            .getString("ProblemSavingWorkingSetState.message")); //$NON-NLS-1$
        }
    }
    
    /**
     * Persists all working sets and fires a property change event for 
     * the changed working set.
     * Should only be called by org.eclipse.ui.internal.WorkingSet.
     * @param changedWorkingSet the working set that has changed
     * @param propertyChangeId the changed property. one of 
     * 	CHANGE_WORKING_SET_CONTENT_CHANGE and CHANGE_WORKING_SET_NAME_CHANGE
     */
    public void workingSetChanged(IWorkingSet changedWorkingSet,
            String propertyChangeId, Object oldValue) {
        saveState();
        super.workingSetChanged(changedWorkingSet, propertyChangeId, oldValue);
    }   
 }