/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.IProjectSetSerializer;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

public class ProjectSetExportWizard extends Wizard implements IExportWizard {
	ExportProjectSetMainPage mainPage;
	IStructuredSelection selection;
	
	public ProjectSetExportWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle(Policy.bind("ProjectSetExportWizard.Project_Set_1")); //$NON-NLS-1$
	}
	
	public void addPages() {
		mainPage = new ExportProjectSetMainPage("projectSetMainPage", Policy.bind("ProjectSetExportWizard.Export_a_Project_Set_3"), TeamImages.getImageDescriptor(UIConstants.IMG_PROJECTSET_EXPORT_BANNER)); //$NON-NLS-1$ //$NON-NLS-2$
		IProject[] projects = (IProject[])selection.toList().toArray(new IProject[0]);
		mainPage.setSelectedProjects(projects);
		mainPage.setFileName(ProjectSetImportWizard.lastFile);
		addPage(mainPage);
	}
	public boolean performFinish() {
		final boolean[] result = new boolean[] {false};
		try {
			getContainer().run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					String filename = mainPage.getFileName();
					Path path = new Path(filename);
					if (path.getFileExtension() == null) {
						filename = filename + ".psf"; //$NON-NLS-1$
					}
					ProjectSetImportWizard.lastFile = filename;
					File file = new File(filename);
					File parentFile = file.getParentFile();
					if (parentFile != null && !parentFile.exists()) {
						boolean r = MessageDialog.openQuestion(getShell(), Policy.bind("ProjectSetExportWizard.Question_4"), Policy.bind("ProjectSetExportWizard.Target_directory_does_not_exist._Would_you_like_to_create_it__5")); //$NON-NLS-1$ //$NON-NLS-2$
						if (!r) {
							result[0] = false;
							return;
						}
						r = parentFile.mkdirs();
						if (!r) {
							MessageDialog.openError(getShell(), Policy.bind("ProjectSetExportWizard.Export_Problems_6"), Policy.bind("ProjectSetExportWizard.An_error_occurred_creating_the_target_directory_7")); //$NON-NLS-1$ //$NON-NLS-2$
							result[0] = false;
							return;
						}
					}
					if (file.exists() && file.isFile()) {
						boolean r = MessageDialog.openQuestion(getShell(), Policy.bind("ProjectSetExportWizard.Question_8"), Policy.bind("ProjectSetExportWizard.Target_already_exists._Would_you_like_to_overwrite_it__9")); //$NON-NLS-1$ //$NON-NLS-2$
						if (!r) {
							result[0] = false;
							return;
						}
					}
					BufferedWriter writer = null;
					try {
						writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")); //$NON-NLS-1$
						
						writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
						writer.newLine();
						writer.write("<psf version=\"2.0\">"); //$NON-NLS-1$
						writer.newLine();
						IProject[] projects = mainPage.getSelectedProjects();
						
						// Hash the projects by provider
						Map map = new HashMap();
						for (int i = 0; i < projects.length; i++) {
							IProject project = projects[i];
							RepositoryProvider provider = RepositoryProvider.getProvider(project);
							if (provider != null) {
								String id = provider.getID();
								List list = (List)map.get(id);
								if (list == null) {
									list = new ArrayList();
									map.put(id, list);
								}
								list.add(project);
							}
						}
						
						// For each provider id, do the writing
						Shell shell = getShell();
						Iterator it = map.keySet().iterator();
						monitor.beginTask(null, 1000 * map.keySet().size());
						while (it.hasNext()) {
							String id = (String)it.next();
							writer.write("\t<provider id=\""); //$NON-NLS-1$
							writer.write(id);
							writer.write("\">"); //$NON-NLS-1$
							writer.newLine();
							List list = (List)map.get(id);
							IProject[] projectArray = (IProject[])list.toArray(new IProject[list.size()]);
							IProjectSetSerializer serializer = Team.getProjectSetSerializer(id);
							if (serializer != null) {
								String[] references = serializer.asReference(projectArray, shell, new SubProgressMonitor(monitor, 1000));
								for (int i = 0; i < references.length; i++) {
									writer.write("\t\t<project reference=\""); //$NON-NLS-1$
									writer.write(references[i]);
									writer.write("\"/>"); //$NON-NLS-1$
									writer.newLine();
								}
							}
							writer.write("\t</provider>"); //$NON-NLS-1$
							writer.newLine();
						}
						writer.write("</psf>"); //$NON-NLS-1$
						writer.newLine();
						result[0] = true;
					} catch (IOException e) {
						throw new InvocationTargetException(e);
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
						if (writer != null) {
							try {
								writer.close();
							} catch (IOException e) {
								throw new InvocationTargetException(e);
							}
						}
					}
				}
			});
		} catch (InterruptedException e) {
			return true;
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof TeamException) {
				ErrorDialog.openError(getShell(), null, null, ((TeamException)target).getStatus());
				return false;
			}
			if (target instanceof RuntimeException) {
				throw (RuntimeException)target;
			}
			if (target instanceof Error) {
				throw (Error)target;
			}
		}
		return result[0];
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}
