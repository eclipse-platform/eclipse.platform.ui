package org.eclipse.team.internal.ui.target;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.target.Site;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.team.internal.ui.ConfigurationWizardElement;
import org.eclipse.team.internal.ui.ConfigureProjectWizard;
import org.eclipse.team.internal.ui.ConfigureProjectWizardMainPage;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.UIConstants;
import org.eclipse.team.ui.IConfigurationWizard;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.internal.model.AdaptableList;

public class ConfigureTargetWizard extends ConfigureProjectWizard {
	
	protected SiteSelectionPage siteSelectionPage = null;
	protected MappingSelectionPage mappingSelectionPage = null;
	protected IWizardPage firstTargetPage = null;
	
	public ConfigureTargetWizard() {
		super();		
	}
	
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
		
		mappingSelectionPage = new MappingSelectionPage("mapping1", Policy.bind("TargetSiteCreationWizard.mappingPageTitle"), TeamImages.getImageDescriptor(UIConstants.IMG_WIZBAN_SHARE)); //$NON-NLS-1$ //$NON-NLS-2$
		if(sites.length > 0 && project != null) {
			siteSelectionPage = new SiteSelectionPage("mapping2", Policy.bind("TargetSiteCreationWizard.siteSelectionPage"), TeamImages.getImageDescriptor(UIConstants.IMG_WIZBAN_SHARE)); //$NON-NLS-1$ //$NON-NLS-2$
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
						if(i == 0) {
							firstTargetPage = pages[i];
						}
					}
				}
			} else {
				mainPage = new ConfigureProjectWizardMainPage("configurePage1", getWizardLabel(), TeamImages.getImageDescriptor(UIConstants.IMG_WIZBAN_SHARE), wizards); //$NON-NLS-1$
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
			if(siteSelectionPage.getSite() != null) {
				mappingSelectionPage.setSite(siteSelectionPage.getSite());
				addPage(mappingSelectionPage);				
				return mappingSelectionPage;
			} else if(mainPage != null) {
				return mainPage;
			} else if(firstTargetPage != null) {
				return firstTargetPage;
			}
		}
		return super.getNextPage(page);
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
			return false;
		} else if(currentPage == mappingSelectionPage) {
			return mappingSelectionPage.isPageComplete();
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
		IWizardPage currentPage = getContainer().getCurrentPage();
		if(currentPage == mappingSelectionPage) {
			IPath path = mappingSelectionPage.getMapping();
			Site site = siteSelectionPage.getSite();
			try {
				if(TargetManager.getProvider(project) != null) {
					TargetManager.unmap(project);
				}
				TargetManager.map(project, site, path);
			} catch (TeamException e) {
				ErrorDialog.openError(getContainer().getShell(), "Error", "Error associating project with target location", e.getStatus());
				return false;
			}
		} else if (wizard != null) {
			return wizard.performFinish();
		}
		return true;
	}
	
	/**
	 * @see IWizard#getPreviousPage(IWizardPage)
	 */
	public IWizardPage getPreviousPage(IWizardPage page) {
		return super.getPreviousPage(page);
	}
}