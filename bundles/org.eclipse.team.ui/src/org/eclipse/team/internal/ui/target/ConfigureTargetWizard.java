/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ui.target;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.target.IRemoteTargetResource;
import org.eclipse.team.internal.core.target.Site;
import org.eclipse.team.internal.core.target.TargetManager;
import org.eclipse.team.internal.core.target.TargetProvider;
import org.eclipse.team.internal.ui.ConfigurationWizardElement;
import org.eclipse.team.internal.ui.ConfigureProjectWizardMainPage;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.UIConstants;
import org.eclipse.team.ui.IConfigurationWizard;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.model.AdaptableList;

public class ConfigureTargetWizard extends Wizard implements IConfigurationWizard {
	protected IWorkbench workbench;
	protected IProject project;
	protected IConfigurationWizard wizard;
	
	protected ConfigureProjectWizardMainPage mainPage;
	protected static MappingSelectionPage mappingPage;
	
	private String pluginId = UIConstants.PLUGIN_ID;
	
	protected final static String TAG_WIZARD = "wizard"; //$NON-NLS-1$
	protected final static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	protected final static String ATT_NAME = "name"; //$NON-NLS-1$
	protected final static String ATT_CLASS = "class"; //$NON-NLS-1$
	protected final static String ATT_ICON = "icon"; //$NON-NLS-1$
	protected final static String ATT_ID = "id"; //$NON-NLS-1$
	
	public ConfigureTargetWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle(getWizardWindowTitle()); //$NON-NLS-1$
	}
	
	public static final String MAPPING_PAGE_NAME = "mapping-page"; //$NON-NLS-1$
	
	protected SiteSelectionPage siteSelectionPage = null;
	/**
	 * @see ConfigureProjectWizard#getExtensionPoint()
	 */
	protected String getExtensionPoint() {
		return UIConstants.PT_TARGETCONFIG;
	}
	
	/**
	 * @see ConfigureProjectWizard#getWizardDescription()
	 */
	protected String getWizardDescription() {
		return Policy.bind("TargetSiteCreationWizard.description"); //$NON-NLS-1$
	}

	/**
	 * @see ConfigureProjectWizard#getWizardLabel()
	 */
	protected String getWizardLabel() {
		return Policy.bind("TargetSiteCreationWizard.label"); //$NON-NLS-1$
	}

	/**
	 * @see ConfigureProjectWizard#getWizardWindowTitle()
	 */
	protected String getWizardWindowTitle() {
		if(project != null) {
			return Policy.bind("TargetSiteCreationWizard.windowTitleProject"); //$NON-NLS-1$
		} else {
			return Policy.bind("TargetSiteCreationWizard.windowTitleNoProject"); //$NON-NLS-1$
		}
	}
	
	/*
	 * @see Wizard#addPages
	 */
	public void addPages() {
		Site[] sites = TargetManager.getSites();
		AdaptableList wizards = getAvailableWizards();
		
		if(sites.length > 0 && project != null) {
			TargetProvider provider = null;
			try {
				provider = TargetManager.getProvider(project);
			} catch (TeamException e) {
				TeamUIPlugin.log(e.getStatus());
			}			
			siteSelectionPage = new SiteSelectionPage("site-selection-page", Policy.bind("TargetSiteCreationWizard.siteSelectionPage"), TeamImages.getImageDescriptor(UIConstants.IMG_WIZBAN_SHARE), provider); //$NON-NLS-1$ //$NON-NLS-2$			
			addPage(siteSelectionPage);
		}
		
		if(project != null) {
			mappingPage = new MappingSelectionPage(ConfigureTargetWizard.MAPPING_PAGE_NAME, Policy.bind("MappingSelectionPage.mappingTitle"), TeamImages.getImageDescriptor(UIConstants.IMG_WIZBAN_SHARE)); //$NON-NLS-1$
			mappingPage.setWizard(this);	
		}	
		
		try {
			if(wizards.size() == 1) {
				ConfigurationWizardElement element = (ConfigurationWizardElement)wizards.getChildren()[0];
				this.wizard = (IConfigurationWizard)element.createExecutableExtension();
				wizard.init(workbench, project);
				wizard.addPages();
				if (wizard.getPageCount() > 0) {
					wizard.setContainer(getContainer());
					IWizardPage[] pages = wizard.getPages();
					for (int i = 0; i < pages.length; i++) {
						addPage(pages[i]);
					}
				}
			} else {
				mainPage = new ConfigureProjectWizardMainPage("target-selection-page", getWizardLabel(), TeamImages.getImageDescriptor(UIConstants.IMG_WIZBAN_SHARE), wizards, Policy.bind("ConfigureProjectWizardMainPage.selectTarget")); //$NON-NLS-1$
				mainPage.setDescription(getWizardDescription());
				mainPage.setProject(project);
				mainPage.setWorkbench(workbench);
				addPage(mainPage);
			}
		} catch (CoreException e) {
			TeamUIPlugin.log(e.getStatus());
			return;
		}
	}
	
	public IWizardPage getNextPage(IWizardPage page) {
		if(page == siteSelectionPage) {
			if(siteSelectionPage.isDisconnect()) {
				return null;
			}
			if(siteSelectionPage.getSite() != null) {
				mappingPage.setSite(siteSelectionPage.getSite());
				mappingPage.setPreviousPage(page);
				return mappingPage;
			} else if(mainPage != null) {
				return mainPage;
			} else if(wizard != null) {
				return wizard.getStartingPage();
			}
		}
		if(wizard != null) {
			return wizard.getNextPage(page);
		}
		return super.getNextPage(page);
	}
	
	public static MappingSelectionPage getMappingPage() {
		return mappingPage;
	}
	
	public boolean canFinish() {
		// If we are on the first page, never allow finish unless the selected wizard has no pages.
		IWizardPage currentPage = getContainer().getCurrentPage();
		if (currentPage == mainPage) {
			if (mainPage.getSelectedWizard() != null && mainPage.getNextPage() == null) {
				return true;
			}
			return false;
		} else if(currentPage == siteSelectionPage) {
			if(siteSelectionPage.getSite() != null || siteSelectionPage.isDisconnect()) {
				return true;
			} else {
				return false;
			}
		}
		
		MappingSelectionPage mappingPage = getMappingPage();
		if(mappingPage != null && currentPage == mappingPage) {
			return mappingPage.isPageComplete();
		}
		if(wizard != null) {
			return wizard.canFinish();
		}
		return super.canFinish();
	}
	
	/*
	 * @see Wizard#performFinish
	 */
	public boolean performFinish() {
		// handles finish on site selection page and on mapping page
		IWizardPage currentPage = getContainer().getCurrentPage();
		if(currentPage == siteSelectionPage || currentPage == getMappingPage()) {
			Site site;
			if(currentPage == siteSelectionPage) {
				// if the user selected the disconnect option then disconnect
				// this project from the target
				if(siteSelectionPage.isDisconnect()) {
					try {
						TargetManager.unmap(project);
					} catch (TeamException e) {
						ErrorDialog.openError(getShell(), Policy.bind("Error"), Policy.bind("ConfigureTargetWizard.errorUnmappingProject"), e.getStatus()); //$NON-NLS-1$ //$NON-NLS-2$
						return false;
					}
					return true;
				}
				site = siteSelectionPage.getSite(); 
			} else {
				site = getMappingPage().getSite(); 
			}
			// a site has been selected on either the site selection page or the
			// mapping page. Use this site information to map the project to the
			// target.
			IPath path = Path.EMPTY;
			if(getMappingPage() != null) {
				path = getMappingPage().getMapping();
			}
			if(! setMapping(getContainer(), project, site, path))
				return false;	//mapping failed so fail finish
		}
		
		// allow target wizard to finish
		if (wizard != null) {
			return wizard.performFinish();
		}
		return true;
	}
	
	public static boolean setMapping(IWizardContainer container, IProject project, Site site, IPath path) {
		if(validateSite(site, container)) {				
			if(TargetManager.getSite(site.getType(), site.getURL()) == null) {
				TargetManager.addSite(site);
			}
			try {				
				TargetProvider provider = TargetManager.getProvider(project);
				if(provider != null) {
					if(!provider.getSite().equals(site) && !MessageDialog.openQuestion(container.getShell(),
						Policy.bind("ConfigureTargetWizardQuestion_2"), //$NON-NLS-1$
						Policy.bind("ConfigureTargetWizard.alreadyMapped", project.getName(), provider.getURL().toExternalForm()))) { //$NON-NLS-1$
					return false;
					}
					TargetManager.unmap(project);
				}
				TargetManager.map(project, site, path);
				return true;
			} catch (TeamException e) {
				ErrorDialog.openError(container.getShell(), Policy.bind("ConfigureTargetWizardError_6"), Policy.bind("ConfigureTargetWizardError_mapping_the_project_with_this_site_7"), e.getStatus()); //$NON-NLS-1$ //$NON-NLS-2$
				return false;
			}
		} else {
			return false;
		}
	}
	
	public static boolean validateSite(final Site site, final IWizardContainer container) {
		final boolean[] valid = new boolean[] {true};
		final String[] message = new String[] {Policy.bind("ConfigureTargetWizardURL_doesn__t_exist_on_the_server_8")}; //$NON-NLS-1$
		final int[] code = new int[] {-1};
		try {
			container.run(true, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
						try {
							monitor.beginTask(Policy.bind("ConfigureTargetWizardValidating_connection_to_Site..._9"), monitor.UNKNOWN); //$NON-NLS-1$
							IRemoteTargetResource remote = site.getRemoteResource();
							valid[0] = remote.canBeReached(monitor);
						} catch(TeamException e) {
							message[0] = e.getStatus().getMessage();
							code[0] = e.getStatus().getCode();
							valid[0] = false;
						} finally {
							monitor.done();
						}
				}
			});
		} catch (InvocationTargetException e) {
			valid[0] = false;
			message[0] = e.getTargetException().getMessage();
		} catch (InterruptedException e) {
			return false;
		}
		if(! valid[0]) {
			if(! MessageDialog.openQuestion(container.getShell(),
				Policy.bind("ConfigureTargetWizardConnection_Error_10"), //$NON-NLS-1$
				Policy.bind("ConfigureTargetWizard.errorOccurred", new Object[] {site.getURL().toExternalForm(), new Integer(code[0]), message[0]}))) { //$NON-NLS-1$
					return false;
			}
		}
		return true;		
	}
	
	/**
	 * Returns the configuration wizards that are available for invocation.
	 * 
	 * @return the available wizards
	 */
	protected AdaptableList getAvailableWizards() {
		AdaptableList result = new AdaptableList();
		IPluginRegistry registry = Platform.getPluginRegistry();
		IExtensionPoint point = registry.getExtensionPoint(pluginId, getExtensionPoint());
		if (point != null) {
			IExtension[] extensions = point.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement[] elements = extensions[i].getConfigurationElements();
				for (int j = 0; j < elements.length; j++) {
					IConfigurationElement element = elements[j];
					if (element.getName().equals(TAG_WIZARD)) {
						ConfigurationWizardElement wizard = createWizardElement(element);
						if (wizard != null) {
							result.add(wizard);
						}
					}
				}
			}
		}
		
		return result;
	}
	/**
	 * Returns a new ConfigurationWizardElement configured according to the parameters
	 * contained in the passed Registry.  
	 *
	 * May answer null if there was not enough information in the Extension to create 
	 * an adequate wizard
	 * 
	 * @param element  the element for which to create a wizard element
	 * @return the wizard element for the given element
	 */
	protected ConfigurationWizardElement createWizardElement(IConfigurationElement element) {
		// WizardElements must have a name attribute
		String nameString = element.getAttribute(ATT_NAME);
		if (nameString == null) {
			// Missing attribute
			return null;
		}
		ConfigurationWizardElement result = new ConfigurationWizardElement(nameString);
		if (initializeWizard(result, element)) {
			// initialization was successful
			return result;
		}
		return null;
	}
	/**
	 *	Initialize the passed element's properties based on the contents of
	 *	the passed registry.  Answer a boolean indicating whether the element
	 *	was able to be adequately initialized.
	 *
	 *	@param element  the element to initialize the properties for
	 *	@param extension  the registry to get properties from
	 *	@return whether initialization was successful
	 */
	protected boolean initializeWizard(ConfigurationWizardElement element, IConfigurationElement config) {
		element.setID(config.getAttribute(ATT_ID));
		String description = ""; //$NON-NLS-1$
		IConfigurationElement [] children = config.getChildren(TAG_DESCRIPTION);
		if (children.length >= 1) {
			description = children[0].getValue();
		}

		element.setDescription(description);
	
		// apply CLASS and ICON properties	
		element.setConfigurationElement(config);
		String iconName = config.getAttribute(ATT_ICON);
		if (iconName != null) {
			IExtension extension = config.getDeclaringExtension();
			element.setImageDescriptor(TeamImages.getImageDescriptorFromExtension(extension, iconName));
		}
		// ensure that a class was specified
		if (element.getConfigurationElement() == null) {
			// Missing attribute
			return false;
		}
		setForcePreviousAndNextButtons(true);
		return true;	
	}
	/*
	 * Method declared on IConfigurationWizard
	 */
	public void init(IWorkbench workbench, IProject project) {
		this.workbench = workbench;
		this.project = project;
	}
}