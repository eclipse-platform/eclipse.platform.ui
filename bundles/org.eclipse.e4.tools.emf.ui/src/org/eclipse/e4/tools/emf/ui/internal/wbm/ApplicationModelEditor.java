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

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.swt.widgets.Composite;

public class ApplicationModelEditor extends ModelEditor {

	private static final String EDITORPROJECT = "org.eclipse.e4.tools.emf.ui.editorproject";

	@Inject
	MPart part;

	@Inject
	EPartService partService;

	private Resource resource;

	private IProject project;

	@Inject
	public ApplicationModelEditor(Composite composite, IEclipseContext context, IModelResource modelProvider, @Named(EDITORPROJECT) @Optional IProject project, IResourcePool resourcePool) {
		super(composite, context, modelProvider, project, resourcePool);
		resource = modelProvider.getEditingDomain().getResourceSet().getResources().get(0);
	}

	@Inject
	public void addResourceListener(@Named(EDITORPROJECT) @Optional IProject project) {
		if (project != null) {
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
	 * Listener changes on the resource being edited. Will close the part if the
	 * resource was deleted or the containing project was closed.
	 */
	private IResourceChangeListener listener = new IResourceChangeListener() {
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
		}

		private void hidePart(boolean force) {
			partService.hidePart(part, force);
		}
	};
}
