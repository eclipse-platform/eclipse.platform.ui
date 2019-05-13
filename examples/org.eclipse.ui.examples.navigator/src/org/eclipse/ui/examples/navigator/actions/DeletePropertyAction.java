/*******************************************************************************
 * Copyright (c) 2005, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.navigator.actions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.examples.navigator.PropertiesTreeData;
import org.eclipse.ui.internal.examples.navigator.Activator;

/**
 * A sample action that can delete a PropertiesTreeData item from a property file.
 *
 * @since 3.2
 */
public class DeletePropertyAction extends ActionDelegate {

	private IStructuredSelection selection = StructuredSelection.EMPTY;

	@Override
	public void selectionChanged(IAction action, ISelection sel) {
		if(sel instanceof IStructuredSelection)
			selection = (IStructuredSelection) sel;
		else
			selection = StructuredSelection.EMPTY;
	}

	@Override
	public void run(IAction action) {

		WorkspaceModifyOperation deletePropertyOperation = new WorkspaceModifyOperation() {
			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException {
				// In production code, you should always externalize strings, but this is an example.
				monitor.beginTask("Deleting property from selection", 5); //$NON-NLS-1$
				try {
					if(selection.size() == 1) {

						Object firstElement = selection.getFirstElement();
						if(firstElement instanceof PropertiesTreeData) {
							PropertiesTreeData data = (PropertiesTreeData) firstElement;

							IFile propertiesFile = data.getFile();
							monitor.worked(1);

							if(propertiesFile != null && propertiesFile.isAccessible()) {

								try {
									// load the model
									Properties properties = new Properties();
									properties.load(propertiesFile.getContents());
									monitor.worked(1);

									// delete the property
									properties.remove(data.getName());
									monitor.worked(1);

									// persist the model to a temporary storage medium (byte[])
									ByteArrayOutputStream output = new ByteArrayOutputStream();
									properties.store(output, null);
									monitor.worked(1);

									// set the contents of the properties file
									propertiesFile.setContents(
														new ByteArrayInputStream(output.toByteArray()),
																IResource.FORCE | IResource.KEEP_HISTORY, monitor);
									monitor.worked(1);
								} catch (IOException e) {
									// handle error gracefully
									Activator.logError(0, "Could not delete property!", e); //$NON-NLS-1$
									MessageDialog.openError(Display.getDefault().getActiveShell(),
											"Error Deleting Property",  //$NON-NLS-1$
											"Could not delete property!");   //$NON-NLS-1$
								}

							} else // shouldn't happen, but handle error condition
								MessageDialog.openError(Display.getDefault().getActiveShell(),
														"Error Deleting Property",  //$NON-NLS-1$
														"The properties file was not accessible!");   //$NON-NLS-1$

						} else // shouldn't happen, but handle error condition
							MessageDialog.openError(Display.getDefault().getActiveShell(),
										"Error Deleting Property",  //$NON-NLS-1$
										"The element that was selected was not of the right type.");   //$NON-NLS-1$
					} else // shouldn't happen, but handle error condition
						MessageDialog.openError(Display.getDefault().getActiveShell(),
									"Error Deleting Property",  //$NON-NLS-1$
									"An invalid number of properties were selected.");   //$NON-NLS-1$
				} finally {
					monitor.done();
				}
			}
		};
		try {
			PlatformUI.getWorkbench().getProgressService().run(true, false, deletePropertyOperation);
		} catch (InvocationTargetException e) {
			// handle error gracefully
			Activator.logError(0, "Could not delete property!", e); //$NON-NLS-1$
			MessageDialog.openError(Display.getDefault().getActiveShell(),
					"Error Deleting Property",  //$NON-NLS-1$
					"Could not delete property!");   //$NON-NLS-1$
		} catch (InterruptedException e) {
			// handle error gracefully
			Activator.logError(0, "Could not delete property!", e); //$NON-NLS-1$
			MessageDialog.openError(Display.getDefault().getActiveShell(),
					"Error Deleting Property",  //$NON-NLS-1$
					"Could not delete property!");   //$NON-NLS-1$
		}

	}
}
