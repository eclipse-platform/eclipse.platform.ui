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

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.core.target.Site;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.team.core.target.TargetProvider;
import org.eclipse.team.internal.core.target.UrlUtil;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Wizard page that allows selecting an existing target provider site
 * or to create a new site.
 */
public class SiteSelectionPage extends TargetWizardPage {
	private TableViewer table;
	private Button useExistingRepo;
	private Button useNewRepo;
	private Site site;
	private TargetProvider currentProvider;
	
	public SiteSelectionPage(String pageName, String title, ImageDescriptor titleImage, TargetProvider currentProvider) {
		super(pageName, title, titleImage);
		setDescription(Policy.bind("SiteSelectionPage.description")); //$NON-NLS-1$
		this.currentProvider = currentProvider;
	}

	protected TableViewer createTable(Composite parent) {
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(100, true));
		table.setLayout(layout);
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
	
		return new TableViewer(table);
	}
	
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 2);
		
		Label description = new Label(composite, SWT.WRAP);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.widthHint = 350;
		description.setLayoutData(data);
		description.setText(Policy.bind("SiteSelectionPage.label")); //$NON-NLS-1$

		useExistingRepo = createRadioButton(composite, Policy.bind("SiteSelectionPage.useExisting"), 2); //$NON-NLS-1$
		table = createTable(composite);
		table.setContentProvider(new WorkbenchContentProvider());
		table.setLabelProvider(new WorkbenchLabelProvider() {
			protected String decorateText(String input, Object element) {
				if(currentProvider != null && element.equals(new SiteElement(currentProvider.getSite()))) {
					IPath mapping = UrlUtil.getTrailingPath(currentProvider.getURL(), currentProvider.getSite().getURL());
					if(mapping.isEmpty()) {
						return Policy.bind("SiteSelectionPage.siteLabelCurrent", super.decorateText(input, element)); //$NON-NLS-1$
					} else {
						return Policy.bind("SiteSelectionPage.siteLabelCurrentWithMapping", super.decorateText(input, element), mapping.toString()); //$NON-NLS-1$
					}
				}
				return super.decorateText(input, element);
			}
		});
		
		table.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				SiteElement siteElement = (SiteElement)((IStructuredSelection)table.getSelection()).getFirstElement();
				if(siteElement != null) {
					site = siteElement.getSite();
				}
				setPageComplete(true);
			}
		});
		useNewRepo = createRadioButton(composite, Policy.bind("SiteSelectionPage.createNew"), 2); //$NON-NLS-1$

		useExistingRepo.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (useNewRepo.getSelection()) {
					table.getTable().setEnabled(false);
					site = null;
				} else {
					table.getTable().setEnabled(true);
					SiteElement siteElement = (SiteElement)((IStructuredSelection)table.getSelection()).getFirstElement();
					if(siteElement != null) {
						site = siteElement.getSite();
					}
				}
				setPageComplete(true);
			}
		});

		setControl(composite);
		initializeValues();
	}

	/**
	 * Initializes states of the controls.
	 */
	private void initializeValues() {
		Site[] sites = TargetManager.getSites();
		table.setInput(new SiteRootsElement());
		if (sites.length == 0) {
			useNewRepo.setSelection(true);	
		} else {
			useExistingRepo.setSelection(true);				
			if(currentProvider != null) {
				table.setSelection(new StructuredSelection(new SiteElement(currentProvider.getSite())));
			} else {
				table.setSelection(new StructuredSelection(new SiteElement(sites[0])));
			}
		}
	}
	
	public Site getSite() {
		return site;
	}
}