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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.xerces.parsers.SAXParser;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.core.IProjectSetSerializer;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ProjectSetImportWizard extends Wizard implements IImportWizard {
	ImportProjectSetMainPage mainPage;
	public static String lastFile;
		
	public ProjectSetImportWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle(Policy.bind("ProjectSetImportWizard.Project_Set_1")); //$NON-NLS-1$
	}
	
	public void addPages() {
		mainPage = new ImportProjectSetMainPage("projectSetMainPage", Policy.bind("ProjectSetImportWizard.Import_a_Project_Set_3"), null); //$NON-NLS-1$ //$NON-NLS-2$
		mainPage.setFileName(lastFile);
		addPage(mainPage);
	}
	public boolean performFinish() {
		final boolean[] result = new boolean[] {false};
		try {
			getContainer().run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						String filename = mainPage.getFileName();
						lastFile = filename;
						InputStreamReader reader = new InputStreamReader(new FileInputStream(filename), "UTF-8"); //$NON-NLS-1$
						
						SAXParser parser = new SAXParser();
						ProjectSetContentHandler handler = new ProjectSetContentHandler();
						parser.setContentHandler(handler);
						InputSource source = new InputSource(reader);
						parser.parse(source);
						
						Map map = handler.getReferences();
						if (map.size() == 0 && handler.isVersionOne) {
							IProjectSetSerializer serializer = Team.getProjectSetSerializer("versionOneSerializer"); //$NON-NLS-1$
							if (serializer != null) {
								serializer.addToWorkspace(new String[0], filename, getShell(), monitor);
							}
						} else {
							Iterator it = map.keySet().iterator();
							while (it.hasNext()) {
								String id = (String)it.next();
								List references = (List)map.get(id);
								IProjectSetSerializer serializer = Team.getProjectSetSerializer(id);
								if (serializer != null) {
									serializer.addToWorkspace((String[])references.toArray(new String[references.size()]), filename, getShell(), monitor);
								}
							}
						}
						reader.close();
						result[0] = true;
					} catch (IOException e) {
						throw new InvocationTargetException(e);
					} catch (SAXException e) {
						throw new InvocationTargetException(e);
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
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
	}
}
