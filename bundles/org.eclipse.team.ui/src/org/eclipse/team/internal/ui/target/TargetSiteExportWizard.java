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
package org.eclipse.team.internal.ui.target;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.target.Site;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.ProjectSetImportWizard;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * This wizard allows the user to save the settings used by Target sites to a file.
 */
public class TargetSiteExportWizard extends Wizard implements IExportWizard {
	private ExportTargetSiteMainPage mainPage;
	private IStructuredSelection selection;
	/**
	 * Constructor for TargetSiteExportWizard.
	 */
	public TargetSiteExportWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle(Policy.bind("TargetSiteExportWizard.Target_Site")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		final boolean[] result = new boolean[] { false };
		try {
			getContainer().run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					String filename = mainPage.getFileName();
					Path path = new Path(filename);
					if (path.getFileExtension() == null) {
						filename = filename + ".tsf"; //$NON-NLS-1$
					}
					ProjectSetImportWizard.lastFile = filename;
					File file = new File(filename);
					File parentFile = file.getParentFile();
					if (parentFile != null && !parentFile.exists()) {
						boolean r = MessageDialog.openQuestion(
							getShell(), 
							Policy.bind("TargetSiteExportWizard.Creation_Question"), //$NON-NLS-1$
							Policy.bind("TargetSiteExportWizard.Create_dir")); //$NON-NLS-1$ 
						if (!r) {
							result[0] = false;
							return;
						}
						r = parentFile.mkdirs();
						if (!r) {
							MessageDialog.openError(getShell(), 
								Policy.bind("TargetSiteExportWizard.Export_Problems"), //$NON-NLS-1$
								Policy.bind("TargetSiteExportWizard.Directory_creation_error") //$NON-NLS-1$
							); 
							result[0] = false;
							return;
						}
					}
					if (file.exists() && file.isFile()) {
						boolean r = MessageDialog.openQuestion(
							getShell(), 
							Policy.bind("TargetSiteExportWizard.Overwrite_Question"), //$NON-NLS-1$
							Policy.bind("TargetSiteExportWizard.Overwrite_file") //$NON-NLS-1$
						); 
						if (!r) {
							result[0] = false;
							return;
						}
					}
					BufferedWriter writer = null;
					try {
						OutputStream jout = new FileOutputStream(file);

						writer = new BufferedWriter(new OutputStreamWriter(jout, "UTF-8")); //$NON-NLS-1$
						writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
						writer.newLine();
						writer.write("<tsf version=\"2.0\">"); //$NON-NLS-1$
						writer.newLine();
						writer.flush();

						Site[] sites = mainPage.getSelectedSites();
						Properties props;
						monitor.beginTask(null, 5 * sites.length);
						String target;
						
						//The 1st one needs to have a unique comment since it only requires an opening tag:
						props = sites[0].getConfiguration();
						monitor.worked(2);
						target=sites[0].getType();
						monitor.worked(1);
						props.store(jout, "<site target=\""+target+"\">"); //$NON-NLS-1$ //$NON-NLS-2$
						monitor.worked(2);
						
						// For each additional site, write the config to disk:
						for (int i = 1; i < sites.length; i++) {
							props = sites[i].getConfiguration();
							monitor.worked(2);
							target=sites[i].getType();
							monitor.worked(1);
							props.store(jout, "</site> <site target=\""+target+"\">"); //$NON-NLS-1$ //$NON-NLS-2$
							monitor.worked(2);
						}
						jout.flush();//This line may not actually be needed but it's not doing any harm either.
						
						writer.write("</site>"); //$NON-NLS-1$
						writer.newLine();
						writer.write("</tsf>"); //$NON-NLS-1$
						writer.newLine();
						result[0] = true;
					} catch (IOException e) {
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
				ErrorDialog.openError(getShell(), null, null, ((TeamException) target).getStatus());
				return false;
			}
			if (target instanceof RuntimeException) {
				throw (RuntimeException) target;
			}
			if (target instanceof Error) {
				throw (Error) target;
			}
		}
		return result[0];
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages() {
		mainPage = new ExportTargetSiteMainPage("targetSiteMainPage", //$NON-NLS-1$
			Policy.bind("TargetSiteExportWizard.Export_a_Target_Site"), //$NON-NLS-1$
			TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_PROJECTSET_EXPORT_BANNER) //TODO: We need our own banner...
		);
		Site[] sites = (Site[]) selection.toList().toArray(new Site[0]);
		mainPage.setSelectedSites(sites);
		mainPage.setFileName(ProjectSetImportWizard.lastFile);
		addPage(mainPage);
	}

}
