package org.eclipse.team.internal.ui.target;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.target.IRemoteTargetResource;
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
	
	public static final String MAPPING_PAGE_NAME = "mapping-page";
	
	protected SiteSelectionPage siteSelectionPage = null;
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
		
		if(sites.length > 0 && project != null) {
			siteSelectionPage = new SiteSelectionPage("site-selection-page", Policy.bind("TargetSiteCreationWizard.siteSelectionPage"), TeamImages.getImageDescriptor(UIConstants.IMG_WIZBAN_SHARE)); //$NON-NLS-1$ //$NON-NLS-2$
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
			return false;
		}
		
		MappingSelectionPage mappingPage = getMappingPage();
		if(mappingPage != null && currentPage == mappingPage) {
			return mappingPage.isPageComplete();
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
		MappingSelectionPage mappingPage = getMappingPage();
		// set mapping
		if(mappingPage != null && currentPage == mappingPage) {
			Site currentSite = mappingPage.getSite();			
			if(validateSite(currentSite, getContainer())) {				
				if(TargetManager.getSite(currentSite.getType(), currentSite.getURL()) ==null) {
					TargetManager.addSite(currentSite);
				}
				try {				
					TargetManager.map(project, currentSite, mappingPage.getMapping());
					return true;
				} catch (TeamException e) {
					ErrorDialog.openError(getShell(), "Error", "Error mapping the project with this site", e.getStatus());
					return false;
				}
			} else {
				return false;
			}
		}
		// allow target wizard to finish
		if (wizard != null) {
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
	
	public static boolean validateSite(final Site site, final IWizardContainer container) {
		final boolean[] valid = new boolean[] {true};
		final String[] message = new String[] {"ok"};
		final int[] code = new int[] {-1};
		try {
			container.run(true, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
						try {
							monitor.beginTask("Validating connection to Site...", monitor.UNKNOWN);
							IRemoteTargetResource remote = site.getRemoteResource();
						} catch(TeamException e) {
							message[0] = e.getStatus().getMessage();
							code[0] = e.getStatus().getCode();
							valid[0] = false;
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
				"Error connecting to Site",
				"An error occured connecting to '" + site.getURL().toExternalForm() + "'.\n\nCode: " + code[0] + "\nMessage: " + message[0] + "\n\nDo you still want to keep this connection?")) {
					return false;
			}
		}
		return true;		
	}
}