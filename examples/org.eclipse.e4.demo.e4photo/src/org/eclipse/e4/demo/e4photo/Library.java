package org.eclipse.e4.demo.e4photo;

import java.util.*;
import org.eclipse.core.databinding.observable.*;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ObservableSetTreeContentProvider;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.osgi.service.prefs.BackingStoreException;

public class Library implements IDisposable {

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
							if (set != null) {
								set.getRealm().asyncExec(new Runnable() {
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

	public Library(Composite parent, final IWorkspace workspace, final IEclipseContext outputContext) {
		final Realm realm = SWTObservables.getRealm(parent.getDisplay());
		this.workspace = workspace;
		initializeWorkspace();
		TreeViewer viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.getTree().setData("org.eclipse.e4.ui.css.id", "library");
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {
				outputContext.set(IServiceConstants.SELECTION, event.getSelection());
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
	
	public void dispose() {
		workspace.removeResourceChangeListener(listener);
	}
}
