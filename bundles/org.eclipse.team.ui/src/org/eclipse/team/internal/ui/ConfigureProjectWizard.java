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
import org.eclipse.team.ui.TeamUIPlugin;
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
	
	protected final static String TAG_WIZARD = "wizard";
	protected final static String TAG_DESCRIPTION = "description";
	protected final static String ATT_NAME = "name";
	protected final static String ATT_CLASS = "class";
	protected final static String ATT_ICON = "icon";
	protected final static String ATT_ID = "id";
	
	/*
	 * @see Wizard#addPages
	 */
	public void addPages() {
		AdaptableList wizards = getAvailableWizards();
		if (wizards.size() == 1) {
			// If there is only one wizard, skip the first page.
			ConfigurationWizardElement element = (ConfigurationWizardElement)wizards.getChildren()[0];
			try {
				this.wizard = (IConfigurationWizard)element.createExecutableExtension();
				wizard.init(workbench, project);
				wizard.setContainer(getContainer());
				wizard.addPages();
				IWizardPage[] pages = wizard.getPages();
				for (int i = 0; i < pages.length; i++) {
					addPage(pages[i]);
				}
			} catch (CoreException e) {
				TeamUIPlugin.log(e.getStatus());
			}
			return;
		}
		mainPage = new ConfigureProjectWizardMainPage("configurePage1", Policy.bind("ConfigureProjectWizard.configureProject"), null, wizards);
		mainPage.setDescription(Policy.bind("ConfigureProjectWizard.description"));
		mainPage.setProject(project);
		mainPage.setWorkbench(workbench);
		addPage(mainPage);
	}
	public boolean canFinish() {
		// If we are on the first page, never allow finish.
		if (getContainer().getCurrentPage() == mainPage) return false;
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
		String description = "";
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
			element.setImageDescriptor(TeamUIPlugin.getImageDescriptorFromExtension(extension, iconName));
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
