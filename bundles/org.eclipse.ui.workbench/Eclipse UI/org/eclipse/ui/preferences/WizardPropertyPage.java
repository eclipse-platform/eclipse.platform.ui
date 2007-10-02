/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.preferences;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * The wizard property page can wrap a property page around a wizard.
 * The property page shows the first page of the wizard. It is therefore
 * required, that the wizard consists of exactly one page.
 * 
 * @since 3.4
 */
public abstract class WizardPropertyPage extends PropertyPage {
	
	private static final class PropertyPageWizardContainer implements IWizardContainer {
		
		private final IWizard fWizard;
		private final PropertyPage fPage;
		private String fMessage;
		
		private PropertyPageWizardContainer(PropertyPage page, IWizard wizard) {
			Assert.isLegal(wizard.getPageCount() == 1);
			
			fPage= page;
			fWizard= wizard;
		}
		
		public IWizardPage getCurrentPage() {
			return fWizard.getPages()[0];
		}
		
		public Shell getShell() {
			return fPage.getShell();
		}
		
		public void showPage(IWizardPage page) {
		}
		
		public void updateButtons() {
			fPage.setValid(fWizard.canFinish());
		}
		
		public void updateMessage() {
			IWizardPage page= getCurrentPage();
			
			String message= fPage.getMessage();
			if (message != null &&
					fMessage == null)
				fMessage= message;
			
			if (page.getErrorMessage() != null) {
				fPage.setMessage(page.getErrorMessage(), ERROR);
			} else if (page instanceof IMessageProvider) {
				IMessageProvider messageProvider= (IMessageProvider)page;
				if (messageProvider.getMessageType() != IMessageProvider.NONE) {
					fPage.setMessage(messageProvider.getMessage(), messageProvider.getMessageType());
				} else {
					if (messageProvider.getMessage() != null &&
							fMessage == null)
						fMessage= messageProvider.getMessage();
					
					fPage.setMessage(fMessage, NONE);
				}
			} else {
				fPage.setErrorMessage(null);
			}
		}
		
		public void updateTitleBar() {
			IWizardPage page= getCurrentPage();
			String name= page.getTitle();
			if (name == null)
				name= page.getName();
			
			fPage.setMessage(name);
		}
		
		public void updateWindowTitle() {
		}
		
		public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
			ProgressMonitorDialog dialog= new ProgressMonitorDialog(getShell());
			dialog.run(fork, cancelable, runnable);
		}
	}
	
	private IWizard fWizard;
	private Composite fWizardPageContainer;
	
	public WizardPropertyPage() {
	}
	
	/**
	 * @return the wizard which is wrapped by this page or <b>null</b> if not yet created
	 */
	public IWizard getWizard() {
		return fWizard;
	}
	
	/**
	 * Return a wizard.
	 * 
	 * @return an instance of the wizard to be wrapped or <b>null</b> if creation failed
	 */
	protected abstract IWizard createWizard();
	
	/**
	 * Apply the changes made on the property page
	 */
	protected abstract void applyChanges();
	
	/**
	 * {@inheritDoc}
	 */
	protected Control createContents(final Composite parent) {
		fWizardPageContainer= new Composite(parent, SWT.NONE);
		fWizardPageContainer.setFont(parent.getFont());
		fWizardPageContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout= new GridLayout(1, false);
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		fWizardPageContainer.setLayout(layout);
		
		createWizardPageContent(fWizardPageContainer);
		
		return fWizardPageContainer;
	}
	
	private void createWizardPageContent(Composite parent) {
		fWizard= createWizard();
		if (fWizard == null)
			return;
		
		fWizard.addPages();
		
		PropertyPageWizardContainer wizardContainer= new PropertyPageWizardContainer(this, fWizard);
		wizardContainer.updateButtons();
		wizardContainer.updateMessage();
		fWizard.setContainer(wizardContainer);
		
		Composite messageComposite= new Composite(parent, SWT.NONE);
		messageComposite.setFont(parent.getFont());
		messageComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		GridLayout layout= new GridLayout(1, false);
		layout.marginHeight= 0;
		messageComposite.setLayout(layout);
		
		Label messageLabel= new Label(messageComposite, SWT.WRAP);
		messageLabel.setFont(messageComposite.getFont());
		messageLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		fWizard.createPageControls(parent);
		
		IWizardPage page= fWizard.getPages()[0];
		if (page.getControl() == null)
			page.createControl(parent);
		
		Control pageControl= page.getControl();
		pageControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		setPageName(page);
		setDescription(page, messageLabel);
		
		page.setVisible(true);
		
		setValid(fWizard.canFinish());
	}
	
	private void setPageName(IWizardPage page) {
		String name= page.getTitle();
		if (name == null)
			name= page.getName();
		
		setMessage(name);
	}
	
	private void setDescription(IWizardPage page, Label messageLabel) {
		String description= null;
		if (page.getDescription() != null) {
			description= page.getDescription();
		} else if (page instanceof IMessageProvider) {
			IMessageProvider messageProvider= (IMessageProvider)page;
			if (messageProvider.getMessageType() == IMessageProvider.NONE) {
				description= messageProvider.getMessage();
			}
		}
		
		if (description != null) {
			messageLabel.setText(description);
		} else {
			messageLabel.setVisible(false);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean performOk() {
		fWizard.performFinish();
		applyChanges();
		fWizard.dispose();
		
		return super.performOk();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean performCancel() {
		fWizard.performCancel();
		fWizard.dispose();
		
		return super.performCancel();
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void performApply() {
		fWizard.performFinish();
		applyChanges();
		fWizard.dispose();
		
		rebuildWizardPage();
		
		super.performApply();
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void performDefaults() {
		fWizard.performCancel();
		fWizard.dispose();
		
		rebuildWizardPage();
		
		super.performDefaults();
	}
	
	/**
	 * Rebuilds the wizard page
	 */
	private void rebuildWizardPage() {
		Control[] children= fWizardPageContainer.getChildren();
		for (int i= 0; i < children.length; i++) {
			children[i].dispose();
		}
		
		createWizardPageContent(fWizardPageContainer);
		fWizardPageContainer.getParent().layout(true, true);
	}
}
