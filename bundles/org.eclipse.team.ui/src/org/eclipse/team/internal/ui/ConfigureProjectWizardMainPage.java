package org.eclipse.team.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.team.ui.IConfigurationWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * The main page of the configure project wizard. It contains a table
 * which lists possible team providers with which to configure the project.
 * The user may select one and press "Next", which will display a provider-
 * specific wizard page.
 */
public class ConfigureProjectWizardMainPage extends WizardPage {
	private Table table;
	private TableViewer viewer;
	private AdaptableList wizards;
	private IWorkbench workbench;
	private IProject project;
	
	private ConfigurationWizardElement selectedElement;
	
	/**
	 * Create a new ConfigureProjectWizardMainPage
	 * 
	 * @param pageName  the name of the page
	 * @param title  the title of the page
	 * @param titleImage  the image for the page title
	 * @param wizard  the wizards to populate the table with
	 */
	public ConfigureProjectWizardMainPage(String pageName, String title, ImageDescriptor titleImage, AdaptableList wizards) {
		super(pageName, title, titleImage);
		this.wizards = wizards;
	}
	/*
	 * @see WizardPage#canFlipToNextPage
	 */
	public boolean canFlipToNextPage() {
		return selectedElement != null;
	}
	/*
	 * @see WizardPage#createControl
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		setControl(composite);
		
		Label label = new Label(composite, SWT.LEFT);
		label.setText(Policy.bind("ConfigureProjectWizardMainPage.selectRepository"));
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
	
		table = new Table(composite, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = table.getItemHeight() * 7;
		table.setLayoutData(data);
		viewer = new TableViewer(table);
		viewer.setContentProvider(new WorkbenchContentProvider());
		viewer.setLabelProvider(new WorkbenchLabelProvider());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection == null || !(selection instanceof IStructuredSelection)) {
					selectedElement = null;
					setPageComplete(false);
					return;
				}
				IStructuredSelection ss = (IStructuredSelection)selection;
				if (ss.size() != 1) {
					selectedElement = null;
					setPageComplete(false);
					return;
				}
				selectedElement = (ConfigurationWizardElement)ss.getFirstElement();
				setPageComplete(true);
			}
		});
		viewer.setInput(wizards);
	}
	/**
	 * The <code>WizardSelectionPage</code> implementation of 
	 * this <code>IWizardPage</code> method returns the first page 
	 * of the currently selected wizard if there is one.
	 * 
	 * @see WizardPage#getNextPage
	 */
	public IWizardPage getNextPage() {
		IConfigurationWizard wizard;
		try {
			wizard = (IConfigurationWizard)selectedElement.createExecutableExtension();
			wizard.init(workbench, project);
		} catch (CoreException e) {
			System.out.println(Policy.bind("exceptionCreatingWizard"));
			return null;
		}
		wizard.addPages();			
		return wizard.getStartingPage();
	}
	/**
	 * Set the workbench to the argument
	 * 
	 * @param workbench  the workbench to set
	 */
	public void setWorkbench(IWorkbench workbench) {
		this.workbench = workbench;
	}
	/**
	 * Set the project to the argument
	 * 
	 * @param project  the project to set
	 */
	public void setProject(IProject project) {
		this.project = project;
	}
}
