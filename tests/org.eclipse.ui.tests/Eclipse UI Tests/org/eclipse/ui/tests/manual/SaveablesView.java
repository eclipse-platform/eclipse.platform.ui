/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.manual;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.SaveablesLifecycleEvent;
import org.eclipse.ui.internal.SaveablesList;
import org.eclipse.ui.part.ViewPart;

/**
 * Helper view to see the open Saveable objects and their dirty state.
 */

public class SaveablesView extends ViewPart {
	private TableViewer viewer;

	private Action printSourcesAction;

	private ISaveablesLifecycleListener saveablesLifecycleListener = new ISaveablesLifecycleListener() {
		@Override
		public void handleLifecycleEvent(SaveablesLifecycleEvent event) {
			if (event.getEventType() == SaveablesLifecycleEvent.DIRTY_CHANGED) {
				Saveable[] saveables = event.getSaveables();
				viewer.update(saveables, null);
			} else {
				viewer.refresh();
			}
		}
	};

	private LocalResourceManager resourceManager;

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	class ViewContentProvider implements IStructuredContentProvider {
		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public Object[] getElements(Object parent) {
			return ((SaveablesList) getSite().getService(
					ISaveablesLifecycleListener.class)).getOpenModels();
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		@Override
		public String getColumnText(Object obj, int index) {
			Saveable saveable = (Saveable) obj;
			return (saveable.isDirty()?"* ":"")+saveable.getName();
		}

		@Override
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		@Override
		public Image getImage(Object obj) {
			ImageDescriptor descriptor = ((Saveable) obj)
					.getImageDescriptor();
			Image image = resourceManager.createImage(descriptor);
			return image;
		}
	}

	class NameSorter extends ViewerSorter {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((Saveable)e1).getName().compareTo(((Saveable)e2).getName());
		}
	}

	/**
	 * The constructor.
	 */
	public SaveablesView() {
		this.resourceManager = new LocalResourceManager(JFaceResources.getResources());
	}
	
	@Override
	public void dispose() {
		((SaveablesList) getSite().getService(
				ISaveablesLifecycleListener.class))
				.removeModelLifecycleListener(saveablesLifecycleListener);
		saveablesLifecycleListener = null;
		resourceManager.dispose();
		resourceManager = null;
		super.dispose();
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());
		((SaveablesList) getSite().getService(
				ISaveablesLifecycleListener.class))
				.addModelLifecycleListener(saveablesLifecycleListener);
		makeActions();
		hookContextMenu();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				SaveablesView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(printSourcesAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void makeActions() {
		printSourcesAction = new Action() {
			@Override
			public void run() {
				Saveable saveable = (Saveable) ((IStructuredSelection)viewer.getSelection()).getFirstElement();
				SaveablesList manager = (SaveablesList) getSite().getService(ISaveablesLifecycleListener.class);
				Object[] sources = manager.testGetSourcesForModel(saveable);
				for (int i = 0; i < sources.length; i++) {
					Object source = sources[i];
					System.out.println(source);
				}
			}
		};
		printSourcesAction.setText("Print sources to stdout");
		printSourcesAction.setToolTipText("Action tooltip");
		printSourcesAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}