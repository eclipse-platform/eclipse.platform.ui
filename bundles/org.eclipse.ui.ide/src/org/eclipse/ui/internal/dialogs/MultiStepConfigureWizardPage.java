package org.eclipse.ui.internal.dialogs;

/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *   IBM Corporation - initial API and implementation 
 *   Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font should be
 *      activated and used by other components.
*********************************************************************/
import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.misc.WizardStepGroup;

/**
 * This page allows the user to go thru a series of steps. Each
 * step that has a wizard will have its pages embedded into this
 * page. At the end, the step will be asked to complete its work.
 * <p>
 * Example useage:
 * <pre>
 * mainPage = new MultiStepConfigureWizardPage("multiStepWizardPage");
 * mainPage.setTitle("Project");
 * mainPage.setDescription("Configure project capability.");
 * </pre>
 * </p>
 */
public class MultiStepConfigureWizardPage extends WizardPage {
	private MultiStepWizardDialog wizardDialog;
	private Composite pageSite;
	private WizardStepGroup stepGroup;
	private WizardStepContainer stepContainer = new WizardStepContainer();
	
	/**
	 * Creates a new multi-step wizard page.
	 *
	 * @param pageName the name of this page
	 */
	public MultiStepConfigureWizardPage(String pageName) {
		super(pageName);
	}

	/* (non-Javadoc)
	 * Method declared on IWizardPage
	 */
	public boolean canFlipToNextPage() {
		return stepContainer.canFlipToNextPage();
	}

	/* (non-Javadoc)
	 * Method declared on IDialogPage.
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(parent.getFont());

		WorkbenchHelp.setHelp(composite, IHelpContextIds.NEW_PROJECT_CONFIGURE_WIZARD_PAGE);

		createStepGroup(composite);
		createEmbeddedPageSite(composite);
		
		setControl(composite);
	}

	/**
	 * Creates the control where the step's wizard will
	 * display its pages.
	 */
	private void createEmbeddedPageSite(Composite parent) {
		pageSite = new Composite(parent, SWT.NONE);
		pageSite.setLayout(new GridLayout());
		pageSite.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	/**
	 * Creates the control for the step list
	 */
	private void createStepGroup(Composite parent) {
		stepGroup = new WizardStepGroup();
		stepGroup.createContents(parent);
	}

	/**
	 * Returns the container handler for the pages
	 * of the step's wizard.
	 */
	/* package */ WizardStepContainer getStepContainer() {
		return stepContainer;
	}
	
	/* (non-Javadoc)
	 * Method declared on IDialogPage.
	 */
	public String getMessage() {
		String msg = stepContainer.getMessage();
		if (msg == null || msg.length() == 0)
			msg = super.getMessage();
		return msg;
	}

	/* (non-Javadoc)
	 * Method declared on IWizardPage.
	 */
	public IWizardPage getPreviousPage() {
		return stepContainer.getPreviousPage();
	}

	/* (non-Javadoc)
	 * Method declared on IWizardPage.
	 */
	public void setPreviousPage(IWizardPage page) {
		// Do not allow to go back
		super.setPreviousPage(null);
	}

	/**
	 * Sets the steps to be displayed. Ignored if the
	 * createControl has not been called yet.
	 * 
	 * @param steps the collection of steps
	 */
	/* package */ void setSteps(WizardStep[] steps) {
		if (stepGroup != null)
			stepGroup.setSteps(steps);
	}
	
	/**
	 * Sets the multi-step wizard dialog processing this
	 * page.
	 */
	/* package */ void setWizardDialog(MultiStepWizardDialog dialog) {
		wizardDialog = dialog;
	}
	
	/* (non-Javadoc)
	 * Method declared on IDialogPage.
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		WizardStep[] steps = stepGroup.getSteps();
		MultiStepWizard stepWizard = wizardDialog.getMultiStepWizard();
		wizardDialog.setFinishLabel(stepWizard.getFinishStepLabel(steps));
		
		getControl().getDisplay().asyncExec(new Runnable() {
			public void run() {
				stepContainer.processCurrentStep();
			}
		});
	}
	
	/**
	 * Support for handling pages from the step's wizard
	 */
	/* package */ class WizardStepContainer implements IWizardContainer {
		private int stepIndex = 0;
		private IWizard wizard;
		private IWizardPage currentPage;

		/* (non-Javadoc)
		 * Method declared on IRunnableContext.
		 */
		public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
			getContainer().run(fork, cancelable, runnable);
		}
		
		/* (non-Javadoc)
		 * Method declared on IWizardContainer.
		 */
		public IWizardPage getCurrentPage() {
			return currentPage;
		}
		 
		/* (non-Javadoc)
		 * Method declared on IWizardContainer.
		 */
		public Shell getShell() {
			return getContainer().getShell();
		}
		
		/* (non-Javadoc)
		 * Method declared on IWizardContainer.
		 */
		public void showPage(IWizardPage page) {
			showPage(page, true);
		}

		/* (non-Javadoc)
		 * Method declared on IWizardContainer.
		 */
		public void updateButtons() {
			getContainer().updateButtons();
		}
		
		/* (non-Javadoc)
		 * Method declared on IWizardContainer.
		 */
		public void updateMessage() {
			getContainer().updateMessage();
		}
		
		/* (non-Javadoc)
		 * Method declared on IWizardContainer.
		 */
		public void updateTitleBar() {
			getContainer().updateTitleBar();
		}
		
		/* (non-Javadoc)
		 * Method declared on IWizardContainer.
		 */
		public void updateWindowTitle() {
			getContainer().updateWindowTitle();
		}
		
		/**
		 * Handles the back button pressed
		 */
		public void backPressed() {
			showPage(currentPage.getPreviousPage(), false);
		}
		
		/**
		 * Handles the next button pressed
		 */
		public void nextPressed() {
			showPage(currentPage.getNextPage(), true);
		}
		
		/**
		 * Handles the help button pressed
		 */
		public void helpPressed() {
			if (currentPage != null)
				currentPage.performHelp();
		}
		
		/**
		 * Handles close request
		 */
		public final boolean performCancel() {
			if (wizard != null)
				return wizard.performCancel();
			else
				return true;
		}

		/**
		 * Handles finish request
		 */
		public final boolean performFinish() {
			if (wizard != null) {
				if (wizard.performFinish()) {
					wizard.dispose();
					wizard.setContainer(null);
					stepGroup.markStepAsDone();
					stepIndex++;
					return true;
				} else {
					return false;
				}
			} else {
				return true;
			}
		}

		/**
		 * Calculates the difference in size between the given
		 * page and the page site. A larger page results 
		 * in a positive delta.
		 *
		 * @param page the page
		 * @return the size difference encoded
		 *   as a <code>new Point(deltaWidth,deltaHeight)</code>
		 */
		private Point calculatePageSizeDelta(IWizardPage page) {
			Control pageControl = page.getControl();
		
			if (pageControl == null)
				// control not created yet
				return new Point(0,0);
				
			Point contentSize = pageControl.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			Rectangle rect = pageSite.getClientArea();
			Point containerSize = new Point(rect.width, rect.height);
		
			return new Point(
				Math.max(0, contentSize.x - containerSize.x),
				Math.max(0, contentSize.y - containerSize.y));
		}

		/**
		 * Computes the correct page site size for the given page
		 * and resizes the dialog if nessessary.
		 *
		 * @param page the wizard page
		 */
		private void updateSizeForPage(IWizardPage page) {
			// ensure the page container is large enough
			Point delta = calculatePageSizeDelta(page);
			
			if (delta.x > 0 || delta.y > 0) {
				Point siteSize = pageSite.getSize();
				GridData data = (GridData)pageSite.getLayoutData();
				data.heightHint = siteSize.y + delta.y;
				data.widthHint = siteSize.x + delta.x;
			}
		}

		/**
		 * Computes the correct page site size for the given wizard
		 * and resizes the dialog if nessessary.
		 *
		 * @param wizard the wizard
		 */
		private void updateSizeForWizard(IWizard wizard) {
			Point delta = new Point(0,0);
			IWizardPage[] pages = wizard.getPages();
			for (int i = 0; i < pages.length; i++){
				// ensure the page site is large enough
				Point pageDelta = calculatePageSizeDelta(pages[i]);
		
				delta.x = Math.max(delta.x, pageDelta.x);
				delta.y = Math.max(delta.y, pageDelta.y);
			}
			
			if (delta.x > 0 || delta.y > 0) {
				Point siteSize = pageSite.getSize();
				GridData data = (GridData)pageSite.getLayoutData();
				data.heightHint = siteSize.y + delta.y;
				data.widthHint = siteSize.x + delta.x;
			}
		}

		/**
		 * Process the current step's wizard.
		 */
		public void processCurrentStep() {
			WizardStep[] steps = stepGroup.getSteps();
			while (stepIndex < steps.length) {
				// adjust the finish button label
				if (stepIndex == (steps.length - 1))
					wizardDialog.setFinishLabel(null);

				final WizardStep step = steps[stepIndex];
				stepGroup.setCurrentStep(step);

				final IWizard[] stepWizard = new IWizard[1];
				BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
					public void run() {
						stepWizard[0] = step.getWizard();
						int tries = 0;
						while (stepWizard[0] == null && tries++ < 3) {
							boolean tryAgain = wizardDialog.getMultiStepWizard().handleMissingStepWizard(step);
							if (!tryAgain)
								break;
							else
								stepWizard[0] = step.getWizard();
						}
					}
				});
					
				if (stepWizard[0] == null)
					break;
				setWizard(stepWizard[0]);
				if (stepWizard[0].getPageCount() > 0)
					return;
				else
					performFinish();
			}
			
			wizardDialog.forceClose();
		}
		
		/**
		 * Sets the current wizard
		 */
		public void setWizard(IWizard newWizard) {
			wizard = newWizard;
			
			// Allow the wizard pages to precreate their page controls
			// This allows the wizard to open to the correct size
			wizard.createPageControls(pageSite);
				
			// Ensure that all of the created pages are initially not visible
			IWizardPage[] pages = wizard.getPages();
			for (int i = 0; i < pages.length; i++) {
				IWizardPage page = (IWizardPage)pages[i];
				if (page.getControl() != null) 
					page.getControl().setVisible(false);
			}
				
			// Ensure the dialog is large enough for the wizard
			updateSizeForWizard(wizard);
			wizardDialog.updateLayout();
				
			wizard.setContainer(this);
			showPage(wizard.getStartingPage(), false);
		}
		
		/**
		 * Show the requested page
		 */
		public void showPage(IWizardPage page, boolean rememberPrevious) {
			if (page == null || page == currentPage)
				return;
			
			if (rememberPrevious && currentPage != null)
				page.setPreviousPage(currentPage);
				
			if (wizard != page.getWizard())
				Assert.isTrue(false);
			
			// ensure that page control has been created
			// (this allows lazy page control creation)
			if (page.getControl() == null) {
				page.createControl(pageSite);
				// the page is responsible for ensuring the created control is accessable
				// via getControl.
				Assert.isNotNull(page.getControl());
				// ensure the dialog is large enough for this page
				updateSizeForPage(page);
				wizardDialog.updateLayout();
			}
		
			// make the new page visible
			IWizardPage oldPage = currentPage;
			currentPage = page;
			currentPage.setVisible(true);
			if (oldPage != null)
				oldPage.setVisible(false);
			page.getControl().setBounds(pageSite.getClientArea());
		
			// update the dialog controls
			wizardDialog.updateAll();
		}
		
		/**
		 * Returns whether the current wizard can finish
		 */
		public boolean canWizardFinish() {
			if (wizard != null)
				return wizard.canFinish();
			else
				return false;
		}

		/**
		 * Returns whether the current page can flip to
		 * the next page
		 */
		public boolean canFlipToNextPage() {
			if (currentPage != null)
				return currentPage.canFlipToNextPage();
			else
				return false;
		}
		
		/**
		 * Returns the current page's message
		 */
		public String getMessage() {
			if (currentPage != null)
				return currentPage.getMessage();
			else
				return null;
		}
		
		/**
		 * Returns the current page's previous page
		 */
		public IWizardPage getPreviousPage() {
			if (currentPage != null)
				return currentPage.getPreviousPage();
			else
				return null;
		}
	}
}
