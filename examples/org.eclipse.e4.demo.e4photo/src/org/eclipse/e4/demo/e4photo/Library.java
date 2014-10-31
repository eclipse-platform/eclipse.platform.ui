/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.e4photo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.viewers.ObservableSetTreeContentProvider;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.osgi.service.prefs.BackingStoreException;

public class Library {

	Map<IContainer, IObservableSet> observableSets = new HashMap<IContainer, IObservableSet>();
	
	private IResourceChangeListener listener = new IResourceChangeListener() {
		public void resourceChanged(IResourceChangeEvent event) {
			if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
				try {
					event.getDelta().accept(new IResourceDeltaVisitor() {
						public boolean visit(IResourceDelta delta)
								throws CoreException {
							if (delta.getKind() == IResourceDelta.ADDED) {
								handleChange(delta.getResource(), delta
										.getResource().getParent(), true);
							} else if (delta.getKind() == IResourceDelta.REMOVED) {
								handleChange(delta.getResource(), delta
										.getResource().getParent(), false);
							}
							return true;
						}

						private void handleChange(final IResource resource,
								final IContainer parent, final boolean added) {
							final IObservableSet set = observableSets
									.get(parent);
							Realm realm = set != null ? set.getRealm() : null;
							if (realm != null) {
								realm.asyncExec(new Runnable() {
									public void run() {
										if (added) {
											set.add(resource);
										} else {
											set.remove(resource);
										}
									}
								});
							}
						}
					});
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	private final IWorkspace workspace;

	static int counter;
	
	@Inject
	private ESelectionService selectionService;
	
	private TreeViewer viewer;

	@Inject
	public Library(Composite parent, final IWorkspace workspace) {
		final Realm realm = DisplayRealm.getRealm(parent.getDisplay());
		this.workspace = workspace;
		initializeWorkspace();
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.getTree().setData("org.eclipse.e4.ui.css.id", "library");
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection)event.getSelection();
				selectionService.setSelection(selection.size() == 1 ? selection.getFirstElement() : selection.toArray());
			}
		});
		IObservableFactory setFactory = new IObservableFactory() {
			public IObservable createObservable(Object element) {
				if (element instanceof IContainer && ((IContainer)element).exists()) {
					IObservableSet observableSet = observableSets.get(element);
					if (observableSet == null) {
						observableSet = new WritableSet(realm);
						try {
							observableSet.addAll(Arrays
									.asList(((IContainer) element).members()));
						} catch (CoreException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						observableSets.put((IContainer) element, observableSet);
					}
					return observableSet;
				}
				return Observables.emptyObservableSet();
			}
		};
		viewer.setContentProvider(new ObservableSetTreeContentProvider(
				setFactory, new TreeStructureAdvisor() {
					public Boolean hasChildren(Object element) {
						return Boolean.valueOf(element instanceof IContainer);
					}
				}));
		
		viewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				if(element instanceof IResource)
					return ((IResource) element).getName();
				return element == null ? "" : element.toString();
			}
		});

		viewer.setSorter(new ViewerSorter());
		viewer.setInput(workspace.getRoot());

//		Button button = new Button(parent, SWT.PUSH);
//		button.setText("Create Project");
//		button.addSelectionListener(new SelectionListener() {
//			public void widgetSelected(SelectionEvent e) {
//				String projectName = "Project" + (counter++);
//				final IProject project = workspace.getRoot().getProject(
//						projectName);
//				final IProjectDescription pd = workspace
//						.newProjectDescription(projectName);
//				try {
//					workspace.run(new IWorkspaceRunnable() {
//						public void run(IProgressMonitor monitor)
//								throws CoreException {
//							project.create(pd, monitor);
//							project.open(monitor);
//						}
//					}, new NullProgressMonitor());
//				} catch (CoreException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//			}
//
//			public void widgetDefaultSelected(SelectionEvent e) {
//			}
//		});

		GridLayoutFactory.fillDefaults().generateLayout(parent);
	}

	private void initializeWorkspace() {
		workspace.addResourceChangeListener(listener);
		IEclipsePreferences node = new InstanceScope().getNode(ResourcesPlugin.PI_RESOURCES);
		node.putBoolean(ResourcesPlugin.PREF_AUTO_REFRESH, true);
		try {
			node.flush();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Focus
	void setFocus() {
		viewer.getControl().setFocus();
	}

	@PreDestroy
	public void dispose() {
		workspace.removeResourceChangeListener(listener);
	}
}
