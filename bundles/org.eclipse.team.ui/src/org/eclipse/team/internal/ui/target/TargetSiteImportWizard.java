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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.core.target.ISiteFactory;
import org.eclipse.team.internal.core.target.Site;
import org.eclipse.team.internal.core.target.TargetManager;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.UIConstants;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TargetSiteImportWizard extends Wizard implements IImportWizard {
	public static String lastFile;
	
	private ImportTargetSiteMainPage mainPage;
	
	/**
	 * Retrieves the Site object that the TargetProvider is contained in.
	 * @return Site
	 */
	Site getSite(Properties properties) {
		try {
			URL url = new URL(properties.getProperty("location")); //$NON-NLS-1$
			return TargetManager.getSite(properties.getProperty("target"), url); //$NON-NLS-1$
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	public TargetSiteImportWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle(Policy.bind("TargetSiteImportWizard.Target_Site")); //$NON-NLS-1$
	}
	
	public void addPages() {
		mainPage = new ImportTargetSiteMainPage(
			"targetSiteMainPage", //$NON-NLS-1$
			Policy.bind("TargetSiteImportWizard.Import_Target_Site"), //$NON-NLS-1$
			TeamImages.getImageDescriptor(UIConstants.IMG_PROJECTSET_IMPORT_BANNER)
		); 
		mainPage.setFileName(lastFile);
		addPage(mainPage);
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					InputStreamReader reader = null;
					try {
						String filename = mainPage.getFileName();
						lastFile = filename;
						reader = new InputStreamReader(new FileInputStream(filename), "UTF-8"); //$NON-NLS-1$

						SAXParserFactory factory = SAXParserFactory.newInstance();
						SAXParser parser;
						parser = factory.newSAXParser();
						TargetSiteContentHandler handler = new TargetSiteContentHandler();
						InputSource source = new InputSource(reader);
						parser.parse(source, handler);

						Properties[] propList = handler.getProperties();
						for (int i = 0; i < propList.length; i++) {
							String target = propList[i].getProperty("target"); //$NON-NLS-1$
							ISiteFactory siteFactory = TargetManager.getSiteFactory(target);
							Assert.isNotNull(siteFactory);

							final Site newsite = siteFactory.newSite(propList[i]);
							Site[] existingSites = TargetManager.getSites();
							boolean dupe = false;
							for (int j = 0; j < existingSites.length; j++) {
								if (existingSites[j].equals(newsite))
									dupe = true;
							}
							if (!dupe) {
								getShell().getDisplay().syncExec(new Runnable() {
									public void run() {
										TargetManager.addSite(newsite);
									}
								});
							}
						}

					} catch (IOException e) {
						throw new InvocationTargetException(e);
					} catch (ParserConfigurationException e) {
						throw new InvocationTargetException(e);
					} catch (SAXException e) {
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
		return true;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {}

}
