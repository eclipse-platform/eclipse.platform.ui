/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.*;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.ui.*;

public class ProjectSetImporter {
	
	public static IProject[] importProjectSet(String filename, Shell shell, IProgressMonitor monitor) throws InvocationTargetException {
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(new FileInputStream(filename), "UTF-8"); //$NON-NLS-1$
			
			XMLMemento xmlMemento = XMLMemento.createReadRoot(reader);
			String version = xmlMemento.getString("version"); //$NON-NLS-1$
			
			List newProjects = new ArrayList();
			if (version.equals("1.0")){ //$NON-NLS-1$
				IProjectSetSerializer serializer = Team.getProjectSetSerializer("versionOneSerializer"); //$NON-NLS-1$
				if (serializer != null) {
					IProject[] projects = serializer.addToWorkspace(new String[0], filename, shell, monitor);
					if (projects != null)
						newProjects.addAll(Arrays.asList(projects));
				}
			} else {
				UIProjectSetSerializationContext context = new UIProjectSetSerializationContext(shell, filename);
				List errors = new ArrayList();
				ArrayList referenceStrings = new ArrayList();
			  	IMemento[] providers = xmlMemento.getChildren("provider"); //$NON-NLS-1$
			  	for (int i = 0; i < providers.length; i++) {
					IMemento[] projects = providers[i].getChildren("project"); //$NON-NLS-1$
					for (int j = 0; j < projects.length; j++) {
						referenceStrings.add(projects[j].getString("reference")); //$NON-NLS-1$
					}
					try {
                        String id = providers[i].getString("id"); //$NON-NLS-1$
                        TeamCapabilityHelper.getInstance().processRepositoryId(id, 
                        		PlatformUI.getWorkbench().getActivitySupport());
                        RepositoryProviderType providerType = RepositoryProviderType.getProviderType(id);
                        if (providerType == null) {
                            // The provider type is absent. Perhaps there is another provider that can import this type
                            providerType = TeamPlugin.getAliasType(id);
                        }
                        if (providerType == null) {
                            throw new TeamException(new Status(IStatus.ERROR, TeamUIPlugin.ID, 0, NLS.bind(TeamUIMessages.ProjectSetImportWizard_0, new String[] { id }), null)); 
                        }
                    	ProjectSetCapability serializer = providerType.getProjectSetCapability();
                    	ProjectSetCapability.ensureBackwardsCompatible(providerType, serializer);
                    	if (serializer != null) {
                    		IProject[] allProjects = serializer.addToWorkspace((String[])referenceStrings.toArray(new String[referenceStrings.size()]), context, monitor);
                    		if (allProjects != null)
                    			newProjects.addAll(Arrays.asList(allProjects));
                    	}
                    } catch (TeamException e) {
                        errors.add(e);
                    }
				}
			  	if (!errors.isEmpty()) {
				    if (errors.size() == 1) {
				        throw (TeamException)errors.get(0);
				    } else {
				        TeamException[] exceptions = (TeamException[]) errors.toArray(new TeamException[errors.size()]);
				        IStatus[] status = new IStatus[exceptions.length];
				        for (int i = 0; i < exceptions.length; i++) {
                            status[i] = exceptions[i].getStatus();
                        }
				        throw new TeamException(new MultiStatus(TeamUIPlugin.ID, 0, status, TeamUIMessages.ProjectSetImportWizard_1, null)); 
				    }
				}
			  	
			  	//try working sets
			  	IMemento[] sets = xmlMemento.getChildren("workingSets"); //$NON-NLS-1$
			  	IWorkingSetManager wsManager = TeamUIPlugin.getPlugin().getWorkbench().getWorkingSetManager();
			  	
			  	for (int i = 0; i < sets.length; i++) {
					IWorkingSet newWs = wsManager.createWorkingSet(sets[i]);
					if (newWs != null) {
						IWorkingSet oldWs = wsManager.getWorkingSet(newWs
								.getName());
						if (oldWs == null) {
							wsManager.addWorkingSet(newWs);
						} else {
							// a working set with the same name has been found
							String title = TeamUIMessages.ImportProjectSetDialog_duplicatedWorkingSet_title;
							String msg = NLS
									.bind(
											TeamUIMessages.ImportProjectSetDialog_duplicatedWorkingSet_message,
											newWs.getName());
							String[] buttons = new String[] {
									TeamUIMessages.ImportProjectSetDialog_duplicatedWorkingSet_replace,
									TeamUIMessages.ImportProjectSetDialog_duplicatedWorkingSet_merge,
									TeamUIMessages.ImportProjectSetDialog_duplicatedWorkingSet_skip,
									IDialogConstants.CANCEL_LABEL };

							final MessageDialog dialog = new MessageDialog(
									shell, title, null, msg,
									MessageDialog.QUESTION, buttons, 0);
							
							shell.getDisplay().syncExec(new Runnable() {
								public void run() {
									 dialog.open();
								}
							});
							
							switch (dialog.getReturnCode()) {
							case 0: // overwrite
								if (oldWs != null)
									wsManager.removeWorkingSet(oldWs);
								wsManager.addWorkingSet(newWs);
								break;
							case 1: // combine
								IAdaptable[] oldElements = oldWs.getElements();
								IAdaptable[] newElements = newWs.getElements();
								
								Set combinedElements = new HashSet();
								combinedElements.addAll(Arrays.asList(oldElements));
								combinedElements.addAll(Arrays.asList(newElements));
								
								oldWs.setElements((IAdaptable[]) combinedElements.toArray(new IAdaptable[0]));
								break;
							case 2: // skip
								break;
							case 3: // cancel
							default:
								throw new OperationCanceledException();
							}
						}
					}
				}
			  	
			}
			
			return (IProject[]) newProjects.toArray(new IProject[newProjects.size()]);
		} catch (IOException e) {
			throw new InvocationTargetException(e);
		} catch (TeamException e) {
			throw new InvocationTargetException(e);
		} catch (WorkbenchException e) {
			throw new InvocationTargetException(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					throw new InvocationTargetException(e);
				}
			}
		}
	}
	
}
