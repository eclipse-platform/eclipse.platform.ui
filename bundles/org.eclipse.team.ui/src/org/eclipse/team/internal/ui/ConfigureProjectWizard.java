package org.eclipse.team.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.ui.IConfigurationWizard;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.model.AdaptableList;

/**
 * The wizard for associating projects with team providers
 */
public class ConfigureProjectWizard extends Wizard implements IConfigurationWizard {
	private IWorkbench workbench;
	private IProject project;
	private IConfigurationWizard wizard;
	
	private ConfigureProjectWizardMainPage mainPage;
	private String pluginId = UIConstants.PLUGIN_ID;
	private String extensionPoint = UIConstants.PT_CONFIGURATION;
	
	protected final static String TAG_WIZARD = "wizard"; //$NON-NLS-1$
	protected final static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	protected final static String ATT_NAME = "name"; //$NON-NLS-1$
	protected final static String ATT_CLASS = "class"; //$NON-NLS-1$
	protected final static String ATT_ICON = "icon"; //$NON-NLS-1$
	protected final static String ATT_ID = "id"; //$NON-NLS-1$
	
	public ConfigureProjectWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle(Policy.bind("ConfigureProjectWizard.title")); //$NON-NLS-1$
	}
	
	/*
	 * @see Wizard#addPages
	 */
	public void addPages() {
		AdaptableList wizards = getAvailableWizards();
		if (wizards.size() == 1) {
			// If there is only one wizard, skip the first page.
			// Only skip the first page if the one wizard has at least one page.
			ConfigurationWizardElement element = (ConfigurationWizardElement)wizards.getChildren()[0];
			try {
				this.wizard = (IConfigurationWizard)element.createExecutableExtension();
				wizard.init(workbench, project);
				wizard.addPages();
				if (wizard.getPageCount() > 0) {
					wizard.setContainer(getContainer());
					IWizardPage[] pages = wizard.getPages();
					for (int i = 0; i < pages.length; i++) {
						addPage(pages[i]);
					}
					return;
				}
			} catch (CoreException e) {
				TeamUIPlugin.log(e.getStatus());
				return;
			}
		}
		mainPage = new ConfigureProjectWizardMainPage("configurePage1", Policy.bind("ConfigureProjectWizard.configureProject"), TeamImages.getImageDescriptor(UIConstants.IMG_WIZBAN_SHARE), wizards); //$NON-NLS-1$ //$NON-NLS-2$
		mainPage.setDescription(Policy.bind("ConfigureProjectWizard.description")); //$NON-NLS-1$
		mainPage.setProject(project);
		mainPage.setWorkbench(workbench);
		addPage(mainPage);
	}
	public IWizardPage getNextPage(IWizardPage page) {
		if (wizard != null) {
			return wizard.getNextPage(page);
		}
		return super.getNextPage(page);
	}
	public boolean canFinish() {
		// If we are on the first page, never allow finish unless the selected wizard has no pages.
		if (getContainer().getCurrentPage() == mainPage) {
			if (mainPage.getSelectedWizard() != null && mainPage.getNextPage() == null) {
				return true;
			}
			return false;
		}
		if (wizard != null) {
			return wizard.canFinish();
		}
		return super.canFinish();
	}
	/*
	 * @see Wizard#performFinish
	 */
	public boolean performFinish() {
		if (wizard != null) {
			return wizard.performFinish();
		}
		return true;
	}
	/**
	 * Returns the configuration wizards that are available for invocation.
	 * 
	 * @return the available wizards
	 */
	AdaptableList getAvailableWizards() {
		AdaptableList result = new AdaptableList();
		IPluginRegistry registry = Platform.getPluginRegistry();
		IExtensionPoint point = registry.getExtensionPoint(pluginId, extensionPoint);
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
