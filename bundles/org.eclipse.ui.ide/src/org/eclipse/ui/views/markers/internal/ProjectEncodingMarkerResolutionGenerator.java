/*******************************************************************************
 * Copyright (c) 2022 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.markers.internal;

import java.util.Arrays;

import org.eclipse.core.internal.resources.CharsetManager;
import org.eclipse.core.internal.resources.ValidateProjectEncoding;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.eclipse.ui.internal.UIPlugin;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;

/**
 * Provides a resolution for warning markers on projects without an explicit
 * encoding setting.
 */
@SuppressWarnings("restriction")
public class ProjectEncodingMarkerResolutionGenerator implements IMarkerResolutionGenerator2 {

	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		try {
			String defaultCharset = workspace.getRoot().getDefaultCharset();
			IMarkerResolution[] resolutions = { new ExplicitEncodingResolution(defaultCharset) };
			return resolutions;
		} catch (CoreException e) {
			UIPlugin.getDefault().getLog().log(e.getStatus());
			return new IMarkerResolution[0];
		}
	}

	@Override
	public boolean hasResolutions(IMarker marker) {
		return canResolve(marker);
	}

	private static boolean canResolve(IMarker marker) {
		String type;
		try {
			type = marker.getType();
		} catch (CoreException e) {
			return false;
		}
		IResource resource = marker.getResource();
		return ValidateProjectEncoding.MARKER_TYPE.equals(type) && resource instanceof IProject;
	}

	private static class ExplicitEncodingResolution extends WorkbenchMarkerResolution {

		private final String charset;
		private Image image;

		private ExplicitEncodingResolution(String encoding) {
			this.charset = encoding;
			ResourceLocator.imageDescriptorFromBundle(IDEWorkbenchPlugin.IDE_WORKBENCH,
					"$nl$/icons/full/elcl16/selected_mode.png").ifPresent(d -> image = d.createImage()); //$NON-NLS-1$
		}

		@Override
		public String getDescription() {
			return NLS.bind(MarkerMessages.ProjectEncodingMarkerResolution_description, charset);
		}

		@Override
		public Image getImage() {
			return image;
		}

		@Override
		public String getLabel() {
			return NLS.bind(MarkerMessages.ProjectEncodingMarkerResolution_label, charset);
		}

		@Override
		public void run(IMarker marker) {
			Runnable task = () -> run(marker);
			if (Display.getCurrent() != null) {
				runAsWorkspaceJob(task, getLabel());
				return;
			}
			IResource resource = marker.getResource();
			if (resource instanceof IProject) {
				IProject project = (IProject) resource;
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				if (workspace instanceof Workspace) {
					Workspace ws = (Workspace) workspace;
					CharsetManager charsetManager = ws.getCharsetManager();
					try {
						charsetManager.setCharsetFor(project.getFullPath(), charset);
					} catch (CoreException e) {
						UIPlugin.getDefault().getLog().log(e.getStatus());
					}
				}
			}
		}

		/**
		 * Runs this resolution. Resolve all <code>markers</code>. <code>markers</code>
		 * must be a subset of the markers returned by
		 * <code>findOtherMarkers(IMarker[])</code>.
		 *
		 * @param markers The markers to resolve, not null
		 * @param monitor The monitor to report progress
		 */
		@Override
		public void run(IMarker[] markers, IProgressMonitor monitor) {
			Runnable task = () -> super.run(markers, monitor);
			if (Display.getCurrent() == null) {
				task.run();
				return;
			}
			runAsWorkspaceJob(task, getLabel());
		}

		private static void runAsWorkspaceJob(Runnable task, String message) {
			WorkspaceJob job = new WorkspaceJob(message) {
				@Override
				public IStatus runInWorkspace(IProgressMonitor m) {
					task.run();
					return Status.OK_STATUS;
				}
			};
			job.setRule(ResourcesPlugin.getWorkspace().getRoot());
			job.setUser(true);
			job.schedule();
		}

		@Override
		public IMarker[] findOtherMarkers(IMarker[] markers) {
			IMarker[] otherMarkers = Arrays.stream(markers).filter(m -> canResolve(m)).toArray(IMarker[]::new);
			return otherMarkers;
		}
	}
}
