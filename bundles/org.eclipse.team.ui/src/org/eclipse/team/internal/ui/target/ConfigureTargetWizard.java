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
import org.eclipse.team.core.target.IRemoteTargetResource;
import org.eclipse.team.core.target.Site;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.team.core.target.TargetProvider;
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
	
	public static final String MAPPING_PAGE_NAME = "mapping-page";
	
	protected SiteSelectionPage siteSelectionPage = null;
	protected IWizardPage firstTargetPage = null;
	
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
		setWindowTitle(getWizardWindowTitle());
		
		if(sites.length > 0 && project != null) {
			Site site = null;
			TargetProvider provider = null;
			try {
				provider = TargetManager.getProvider(project);
			} catch (TeamException e) {
				TeamUIPlugin.log(e.getStatus());
			}
			if(provider != null) {
				site = provider.getSite();
			}			
			siteSelectionPage = new SiteSelectionPage("site-selection-page", Policy.bind("TargetSiteCreationWizard.siteSelectionPage"), TeamImages.getImageDescriptor(UIConstants.IMG_WIZBAN_SHARE), site); //$NON-NLS-1$ //$NON-NLS-2$
			addPage(siteSelectionPage);
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
				mainPage = new ConfigureProjectWizardMainPage("target-selection-page", getWizardLabel(), TeamImages.getImageDescriptor(UIConstants.IMG_WIZBAN_SHARE), wizards); //$NON-NLS-1$
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
		// This is what we really want to do, but will have to rework the 
		// target wizards first.
		//		if(getPage(page.getName()) != null) {
		//			// this is one of our pages
		//			// 1. site selection 
		//			// 2. target selection
		//			// 3. mapping
		//		} else {
		//			// not one of our pages, is a target specific page
		//			IWizardPage nextPage;
		//			if(wizard != null) {
		//				nextPage = wizard.getNextPage(page);
		//			} else {
		//				nextPage = mainPage.getSelectedWizard().getNextPage(page);
		//			}
		//			if(nextPage != null) {
		//				return nextPage;
		//			} else {
		//				MappingSelectionPage mappingPage = getMappingPage();
		//				mappingPage.setPreviousPage(page);
		//			}
		//		}		
		if(page == siteSelectionPage) {
			if(siteSelectionPage.getSite() != null) {
				MappingSelectionPage mappingPage = getMappingPage();
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
	
	private MappingSelectionPage getMappingPage() {
		return (MappingSelectionPage)getPage(MAPPING_PAGE_NAME);
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
			if(siteSelectionPage.getSite() != null) {
				return true;
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
				site = siteSelectionPage.getSite(); 
			} else {
				site = getMappingPage().getSite(); 
			}
			IPath path = Path.EMPTY;
			if(getMappingPage() != null) {
				path = getMappingPage().getMapping();
			}
			setMapping(getContainer(), project, site, path);
			return true;
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
					if(! MessageDialog.openQuestion(container.getShell(),
						"Question",
						"'" + project.getName() + "' is already mapped to '" + provider.getSite().getURL().toExternalForm() +"'. Are you sure you want to change to another location?")) {
					return false;
					}
					TargetManager.unmap(project);
				}
				TargetManager.map(project, site, path);
				return true;
			} catch (TeamException e) {
				ErrorDialog.openError(container.getShell(), "Error", "Error mapping the project with this site", e.getStatus());
				return false;
			}
		} else {
			return false;
		}
	}
	
	public static boolean validateSite(final Site site, final IWizardContainer container) {
		final boolean[] valid = new boolean[] {true};
		final String[] message = new String[] {"URL doesn't exist on the server"};
		final int[] code = new int[] {-1};
		try {
			container.run(true, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
						try {
							monitor.beginTask("Validating connection to Site...", monitor.UNKNOWN);
							IRemoteTargetResource remote = site.getRemoteResource();
							valid[0] = remote.exists();
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
				"Connection Error",
				"An error occured connecting to '" + site.getURL().toExternalForm() + "'.\n\nCode: " + code[0] + "\nMessage: " + message[0] + "\n\nDo you still want to keep this connection?")) {
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