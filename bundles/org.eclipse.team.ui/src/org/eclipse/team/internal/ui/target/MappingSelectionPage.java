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

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.target.IRemoteTargetResource;
import org.eclipse.team.core.target.Site;
import org.eclipse.team.internal.core.target.UrlUtil;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class MappingSelectionPage extends TargetWizardPage {
	private IPath path = Path.EMPTY;
	private Site site;
	private TreeViewer viewer;
	private Text textPath;
	
	public MappingSelectionPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		setDescription(Policy.bind("MappingSelectionPage.description")); //$NON-NLS-1$
	}

	public void setSite(Site site) {
		this.site = site;
	}
	
	public Site getSite() {
		return site;
	}
	
	public void createControl(Composite p) {
		Composite composite = createComposite(p, 1);
		
		createLabel(composite, Policy.bind("MappingSelectionPage.label")); //$NON-NLS-1$
		
		viewer = new TreeViewer(composite, SWT.BORDER | SWT.SINGLE);
		
		GridData data = new GridData (GridData.FILL_BOTH);
		viewer.getTree().setLayoutData(data);
		viewer.setContentProvider(new SiteLazyContentProvider());
		viewer.setLabelProvider(new WorkbenchLabelProvider());
		viewer.setSorter(new SiteViewSorter());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateTextPath();
			}
		});
		
		// include only folders in view
		viewer.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if(element instanceof RemoteResourceElement) {
					return ((RemoteResourceElement)element).getRemoteResource().isContainer();
				}
				return false;
			}
		});
		
		Button newFolderButton = new Button(composite, SWT.PUSH);
		newFolderButton.setText(Policy.bind("MappingSelectionPage.newFolderLabel")); //$NON-NLS-1$
		newFolderButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event event) {
				Shell shell = getShell();
				try {
					// assume that only one folder is selected in the folder tree
					IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
					Object currentSelection = selection.getFirstElement();
					final IRemoteTargetResource selectedFolder = getSelectedRemoteFolder(selection);					
					String defaultName = ((ConfigureTargetWizard) getWizard()).project.getName();

					IRemoteTargetResource newFolder = CreateNewFolderAction.createDir(getShell(), selectedFolder, defaultName);
					if (newFolder == null)
						return;

					RemoteResourceElement newFolderUIElement = new RemoteResourceElement(newFolder);

					((RemoteResourceElement)currentSelection).setCachedChildren(null);
					viewer.refresh(currentSelection);
					viewer.setExpandedState(currentSelection, true);
					viewer.setSelection(new StructuredSelection(newFolderUIElement));
				} catch (TeamException e) {
					TeamUIPlugin.handle(e);
					return;
				}
			}			
		});
		setViewerInput();
		setControl(composite);
		setPageComplete(true);
	}
	
	private IRemoteTargetResource getSelectedRemoteFolder(IStructuredSelection selection) {		
		if (!selection.isEmpty()) {
			final List filesSelection = new ArrayList();
			Iterator it = selection.iterator();
			while(it.hasNext()) {
				Object o = it.next();
				if(o instanceof RemoteResourceElement) {
					return ((RemoteResourceElement)o).getRemoteResource();
				} else if(o instanceof SiteElement) {
					try {
						return ((SiteElement)o).getSite().getRemoteResource();
					} catch (TeamException e) {
						return null;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Method updateTextPath.
	 */
	private void updateTextPath() {
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		if (!selection.isEmpty()) {
			final List filesSelection = new ArrayList();
			Iterator it = selection.iterator();
			while(it.hasNext()) {
				Object o = it.next();
				if(o instanceof RemoteResourceElement) {
						RemoteResourceElement element = (RemoteResourceElement) o;
						URL remoteResourceURL;
						remoteResourceURL = element.getRemoteResource().getURL();
						this.path = UrlUtil.getTrailingPath(
							remoteResourceURL,
							this.site.getURL());
					return;
				}
			}
		}
	}

	public IPath getMapping() {
		return this.path;
	}
	
	/**
	 * Attempt to set the viewer input.
	 * Do nothing if we don't have enough info yet to set it.
	 */
	private void setViewerInput() {
		if(this.site == null || viewer == null)
			return;
		viewer.setInput(new SiteRootsElement(new Site[] {site}, getContainer()));		
	}

	/**
	 * @see IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		if(visible) {
			setViewerInput();
			viewer.setSelection(new StructuredSelection(new SiteElement(site)));
		}
		super.setVisible(visible);
	}
}