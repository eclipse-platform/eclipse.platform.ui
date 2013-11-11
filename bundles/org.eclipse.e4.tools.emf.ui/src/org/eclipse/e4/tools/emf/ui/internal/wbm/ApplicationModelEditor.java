/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.


 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Wim Jongman <wim.jongman@remainsoftware.com> - https://bugs.eclipse.org/bugs/show_bug.cgi?id=393150
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.wbm;

import java.io.IOException;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.FrameworkUtil;

public class ApplicationModelEditor extends ModelEditor {

	private static final String EDITORPROJECT = "org.eclipse.e4.tools.emf.ui.editorproject"; //$NON-NLS-1$

	@Inject
	@Optional
	MPart part;

	@Inject
	EPartService partService;

	private Resource resource;

	private IProject project;

	@Inject
	Shell shell;

	@Inject
	UISynchronize sync;

	@Inject
	public ApplicationModelEditor(Composite composite, IEclipseContext context, IModelResource modelProvider, @Named(EDITORPROJECT) @Optional IProject project, IResourcePool resourcePool) {
		super(composite, context, modelProvider, project, resourcePool);
		EList<Resource> resources = modelProvider.getEditingDomain().getResourceSet().getResources();
		if (!resources.isEmpty()) {
			resource = resources.get(0);
		}
	}

	@Inject
	public void addResourceListener(@Named(EDITORPROJECT) @Optional IProject project) {
		if (project != null && resource != null) {
			this.project = project;
			project.getWorkspace().addResourceChangeListener(listener);
		}
	}

	@PreDestroy
	private void removeResourceListener() {
		if (project != null) {
			project.getWorkspace().removeResourceChangeListener(listener);
		}
	}

	/**
	 * Listen for changes on the resource being edited. Will close the part if
	 * the resource was deleted or the containing project was closed.
	 */
	private IResourceChangeListener listener = new IResourceChangeListener() {
		@Override
		public void resourceChanged(IResourceChangeEvent event) {

			if (event.getType() == IResourceChangeEvent.PRE_CLOSE || event.getType() == IResourceChangeEvent.PRE_DELETE) {
				if (event.getResource().equals(project)) {
					hidePart(true);
				}
				return;
			}

			if (resource == null)
				return;

			IResourceDelta delta = event.getDelta().findMember(new Path(resource.getURI().toPlatformString(false)));
			if (delta == null) {
				return;
			}

			if (delta.getKind() == IResourceDelta.REMOVED) {
				hidePart(true);
			}

			// If the current model editor is causing this resource change event
			// then skip the reload.
			if (!isSaving()) {

				// reload the model if the file has changed
				if (delta.getKind() == IResourceDelta.CHANGED) {
					reloadModel();
				}
			}
		}

		private void hidePart(boolean force) {
			partService.hidePart(part, force);
		}
	};

	/**
	 * Shows an error dialog based on the passed exception. It should never
	 * occur but if it does, the user can report a problem.
	 * 
	 * @param exc
	 */
	protected void statusDialog(final Exception exc) {
		try {
			sync.syncExec(new Runnable() {
				@Override
				public void run() {
					String bundle = FrameworkUtil.getBundle(getClass()).getSymbolicName();
					Status status = new Status(IStatus.ERROR, bundle, exc.getMessage());
					ErrorDialog.openError(shell, exc.getMessage(), exc.getMessage(), status);
					exc.printStackTrace(System.err);
				}
			});
		} catch (Exception e) {
		}
	}

	/**
	 * Reload the model.
	 */
	protected void reloadModel() {
		getModelProvider().getRoot().getRealm().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					resource.unload();
					resource.load(null);
					getModelProvider().replaceRoot(resource.getContents().get(0));
					doSave(new NullProgressMonitor());
				} catch (IOException e) {
					statusDialog(e);
				}
			}
		});
	}
}