/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.util.KnownRepositories;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.CheckoutMultipleProjectsOperation;
import org.eclipse.team.internal.ccvs.ui.operations.HasProjectMetaFileOperation;
import org.eclipse.ui.*;

/**
 * Gathers all information necesary for a checkout from a repository.
 */
public class CheckoutWizard extends Wizard implements ICVSWizard, INewWizard {
	
	private RepositorySelectionPage locationPage;
	private ConfigurationWizardMainPage createLocationPage;
	private ModuleSelectionPage modulePage;
	private CheckoutAsWizard wizard;
	private ICVSRepositoryLocation location;
	private boolean isNewLocation;
	private CVSWizardPage dummyPage;
	
	public CheckoutWizard() {
		setWindowTitle(Policy.bind("CheckoutWizard.0")); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages() {
		setNeedsProgressMonitor(true);
		
		ImageDescriptor substImage = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_CHECKOUT);

		ICVSRepositoryLocation[] locations = CVSUIPlugin.getPlugin().getRepositoryManager().getKnownRepositoryLocations();
		if (locations.length > 0) {
			locationPage = new RepositorySelectionPage("locationSelection", Policy.bind("CheckoutWizard.7"), substImage); //$NON-NLS-1$ //$NON-NLS-2$
			locationPage.setDescription(Policy.bind("SharingWizard.importTitleDescription")); //$NON-NLS-1$
			locationPage.setExtendedDescription(Policy.bind("CheckoutWizard.8")); //$NON-NLS-1$
			addPage(locationPage);
		}
		
		createLocationPage = new ConfigurationWizardMainPage("createLocationPage", Policy.bind("SharingWizard.enterInformation"), substImage); //$NON-NLS-1$ //$NON-NLS-2$
		createLocationPage.setDescription(Policy.bind("SharingWizard.enterInformationDescription")); //$NON-NLS-1$
		addPage(createLocationPage);
		createLocationPage.setDialogSettings(NewLocationWizard.getLocationDialogSettings());
		
		modulePage = new ModuleSelectionPage("moduleSelection", Policy.bind("CheckoutWizard.10"), substImage); //$NON-NLS-1$ //$NON-NLS-2$
		modulePage.setDescription(Policy.bind("CheckoutWizard.11")); //$NON-NLS-1$
		modulePage.setHelpContxtId(IHelpContextIds.CHECKOUT_MODULE_SELECTION_PAGE);
		modulePage.setSupportsMultiSelection(true);
		addPage(modulePage);
		
		// Dummy page to allow lazy creation of CheckoutAsWizard
		dummyPage = new CVSWizardPage("dummyPage") { //$NON-NLS-1$
			public void createControl(Composite parent) {
				Composite composite = createComposite(parent, 1);
				setControl(composite);
			}
		};
		addPage(dummyPage);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#canFinish()
	 */
	public boolean canFinish() {
		return (wizard == null && getSelectedModules().length > 0) || 
			(wizard != null && wizard.canFinish());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		if (wizard != null) {
			// The finish of the child wizard will get called directly.
			// We only get here if it completed successfully
			if (isNewLocation) {
				KnownRepositories.getInstance().addRepository(location, true /* broadcast */);
			}
			return true;
		} else {
			try {
				new CheckoutMultipleProjectsOperation(getPart(), getSelectedModules(), null)
					.run();
				if (isNewLocation) {
					KnownRepositories.getInstance().addRepository(location, true /* broadcast */);
				}
				return true;
			} catch (InvocationTargetException e) {
				CVSUIPlugin.openError(getShell(), null, null, e);
			} catch (InterruptedException e) {
				// Cancelled. fall through.
			}
			return false;
		}
	}
	
	private IWorkbenchPart getPart() {
		// This wizard doesn't have a part
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performCancel()
	 */
	public boolean performCancel() {
		if (location != null && isNewLocation) {
			KnownRepositories.getInstance().disposeRepository(location);
			location = null;
		}
		return wizard == null || wizard.performCancel();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		// Assume the page is about to be shown when this method is
		// invoked
		return getNextPage(page, true /* about to show*/);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.wizards.ICVSWizard#getNextPage(org.eclipse.jface.wizard.IWizardPage, boolean)
	 */
	public IWizardPage getNextPage(IWizardPage page, boolean aboutToShow) {
		if (page == locationPage) {
			if (locationPage.getLocation() == null) {
				return createLocationPage;
			} else {
				if (aboutToShow) {
					try {
						modulePage.setLocation(getLocation());
					} catch (TeamException e1) {
						CVSUIPlugin.log(e1);
					}
				}
				return modulePage;
			}
		}
		if (page == createLocationPage) {
			if (aboutToShow) {
				try {
					ICVSRepositoryLocation l = getLocation();
					if (l != null) {
						modulePage.setLocation(l);
					}
				} catch (TeamException e1) {
					CVSUIPlugin.log(e1);
				}
			}
			return modulePage;
		}
		if (page == modulePage) {
			ICVSRemoteFolder[] selectedModules = getSelectedModules();
			if (selectedModules.length == 0) return null;
			for (int i = 0; i < selectedModules.length; i++) {
				ICVSRemoteFolder folder = selectedModules[i];
				if (folder.isDefinedModule()) {
					// No further configuration is possible for defined modules
					return null;
				}
			}
			if (aboutToShow) {
				try {
					boolean hasMetafile = true;
					if (selectedModules.length == 1) {
						// Only allow configuration if one module is selected
						hasMetafile = hasProjectMetafile(selectedModules[0]);
					}
					wizard = new CheckoutAsWizard(getPart(), selectedModules, ! hasMetafile /* allow configuration */);
					wizard.addPages();
					return wizard.getStartingPage();
				} catch (InvocationTargetException e) {
					// Show the error and fall through to return null as the next page
					CVSUIPlugin.openError(getShell(), null, null, e);
				} catch (InterruptedException e) {
					// Cancelled by user. Fall through and return null
				}
				return null;
			} else {
				if (wizard == null) {
					return dummyPage;
				} else {
					return wizard.getStartingPage();
				}
			}
		}
		if (wizard != null) {
			return wizard.getNextPage(page);
		}
		return null;
	}

	private boolean hasProjectMetafile(final ICVSRemoteFolder selectedModule) throws InvocationTargetException, InterruptedException {
		final boolean[] result = new boolean[] { true };
		getContainer().run(true, true, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				HasProjectMetaFileOperation op = new HasProjectMetaFileOperation(getPart(), selectedModule);
				op.run(monitor);
				result[0] = op.metaFileExists();
			}
		});
		return result[0];
	}

	private ICVSRemoteFolder[] getSelectedModules() {
		if (modulePage == null) return null;
		return modulePage.getSelectedModules();
	}

	/**
	 * Return an ICVSRepositoryLocation
	 */
	private ICVSRepositoryLocation getLocation() throws TeamException {
		// If the location page has a location, use it.
		if (locationPage != null) {
			ICVSRepositoryLocation newLocation = locationPage.getLocation();
			if (newLocation != null) {
				return recordLocation(newLocation);
			}
		}
		
		// Otherwise, get the location from the create location page
		final ICVSRepositoryLocation[] locations = new ICVSRepositoryLocation[] { null };
		final CVSException[] exception = new CVSException[] { null };
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				try {
					locations[0] = createLocationPage.getLocation();
				} catch (CVSException e) {
					exception[0] = e;
				}
			}
		});
		if (exception[0] != null) {
			throw exception[0];
		}
		return recordLocation(locations[0]);
	}

	private ICVSRepositoryLocation recordLocation(ICVSRepositoryLocation newLocation) {
		if (newLocation == null) return location;
		if (location == null || !newLocation.equals(location)) {
			if (location != null && isNewLocation) {
				// Dispose of the previous location
				KnownRepositories.getInstance().disposeRepository(location);
			}
			location = newLocation;
			isNewLocation = !KnownRepositories.getInstance().isKnownRepository(newLocation.getLocation());
			if (isNewLocation) {
				// Add the location silently so we can work with it
				location = KnownRepositories.getInstance().addRepository(location, false /* silently */);
			}
		}
		return location;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
}
