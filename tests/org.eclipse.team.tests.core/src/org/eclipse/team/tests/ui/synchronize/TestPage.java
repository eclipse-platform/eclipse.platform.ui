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
package org.eclipse.team.tests.ui.synchronize;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.internal.ui.synchronize.StructuredViewerAdvisor;
import org.eclipse.team.internal.ui.synchronize.TreeViewerAdvisor;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.Page;

/**
 * Page for testing
 */
public class TestPage extends Page implements ISynchronizePage {
	
	private ISynchronizePageConfiguration configuration;
	private Composite composite;
	private Viewer changesViewer;
	private TreeViewerAdvisor viewerAdvisor;

	public TestPage(ISynchronizePageConfiguration configuration) {
		this.configuration = configuration;
		configuration.setPage(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE); 
		//sc.setContent(composite);
		GridLayout gridLayout= new GridLayout();
		gridLayout.makeColumnsEqualWidth= false;
		gridLayout.marginWidth= 0;
		gridLayout.marginHeight = 0;
		gridLayout.verticalSpacing = 0;
		composite.setLayout(gridLayout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessVerticalSpace = true;
		composite.setLayoutData(data);
		
		// Create the changes section which, in turn, creates the changes viewer and its configuration
		this.changesViewer = createChangesViewer(composite);
	}
	
	protected Viewer createChangesViewer(Composite parent) {
		viewerAdvisor = new TreeViewerAdvisor(parent, configuration);
		return viewerAdvisor.getViewer();
	}
	
	public StructuredViewerAdvisor getViewerAdvisor() {
		return viewerAdvisor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#getControl()
	 */
	public Control getControl() {
		return composite;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#setFocus()
	 */
	public void setFocus() {
		changesViewer.getControl().setFocus();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizePage#init(org.eclipse.team.ui.synchronize.ISynchronizePageSite)
	 */
	public void init(ISynchronizePageSite site) {
		// Noop
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#setActionBars(org.eclipse.ui.IActionBars)
	 */
	public void setActionBars(IActionBars actionBars) {
		// Delegate menu creation to the advisor
		viewerAdvisor.setActionBars(actionBars);		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.Page#dispose()
	 */
	public void dispose() {
		composite.dispose();
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizePage#getViewer()
	 */
	public Viewer getViewer() {
		return changesViewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizePage#aboutToChangeProperty(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration, java.lang.String, java.lang.Object)
	 */
	public boolean aboutToChangeProperty(ISynchronizePageConfiguration configuration, String key, Object newValue) {
		// Allow all changes
		return true;
	}
}
