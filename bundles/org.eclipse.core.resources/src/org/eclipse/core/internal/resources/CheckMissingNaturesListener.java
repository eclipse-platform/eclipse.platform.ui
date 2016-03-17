/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;

public class CheckMissingNaturesListener implements IResourceChangeListener {

	public static final String MARKER_TYPE = ResourcesPlugin.getPlugin().getBundle().getSymbolicName() + ".unknownNature"; //$NON-NLS-1$
	public static final String NATURE_ID_ATTRIBUTE = "natureId"; //$NON-NLS-1$

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getDelta() == null) {
			return;
		}
		try {
			event.getDelta().accept(new IResourceDeltaVisitor() {
				@Override
				public boolean visit(IResourceDelta delta) throws CoreException {
					if (delta.getResource() != null && delta.getResource().getType() == IResource.PROJECT && (delta.getKind() == IResourceDelta.ADDED || delta.getKind() == IResourceDelta.CHANGED)) {
						final IProject project = (IProject) delta.getResource();
						if (!project.isAccessible()) {
							return false;
						}
						int severity = getMissingNatureSeverity(project);
						if (severity < 0) {
							return false;
						}

						final Set<String> missingNatures = new HashSet<>();
						for (String natureId : project.getDescription().getNatureIds()) {
							if (project.getWorkspace().getNatureDescriptor(natureId) == null) {
								missingNatures.add(natureId);
							}
						}
						final Set<IMarker> toRemove = new HashSet<>();
						for (IMarker existingMarker : project.findMarkers(MARKER_TYPE, true, IResource.DEPTH_ZERO)) {
							String markerNature = existingMarker.getAttribute(NATURE_ID_ATTRIBUTE, ""); //$NON-NLS-1$
							if (!missingNatures.contains(markerNature)) {
								toRemove.add(existingMarker);
							} else {
								// no need to create a new marker
								missingNatures.remove(markerNature);
							}
						}
						if (!toRemove.isEmpty() || !missingNatures.isEmpty()) {
							WorkspaceJob workspaceJob = new WorkspaceJob(NLS.bind(Messages.addingMissingNatureMarkersOnProject, project.getName())) {
								@Override
								public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
									for (IMarker marker : toRemove) {
										marker.delete();
									}
									for (String natureId : missingNatures) {
										IMarker marker = project.createMarker(MARKER_TYPE);
										marker.setAttribute(IMarker.SEVERITY, severity);
										marker.setAttribute(IMarker.MESSAGE, NLS.bind(Messages.natures_missingNature, natureId));
										marker.setAttribute(NATURE_ID_ATTRIBUTE, natureId);
									}
									return Status.OK_STATUS;
								}

								@Override
								public boolean belongsTo(Object family) {
									return super.belongsTo(family) || MARKER_TYPE.equals(family);
								}
							};
							workspaceJob.setUser(false);
							workspaceJob.setSystem(true);
							workspaceJob.setPriority(Job.DECORATE);
							workspaceJob.schedule();
						}
					}
					return delta.getResource() == null || delta.getResource().getType() == IResource.ROOT;
				}

				private int getMissingNatureSeverity(final IProject project) {
					int severity = PreferenceInitializer.PREF_MISSING_NATURE_MARKER_SEVERITY_DEFAULT;
					IEclipsePreferences node = InstanceScope.INSTANCE.getNode(ResourcesPlugin.PI_RESOURCES);
					if (node != null) {
						severity = node.getInt(ResourcesPlugin.PREF_MISSING_NATURE_MARKER_SEVERITY, PreferenceInitializer.PREF_MISSING_NATURE_MARKER_SEVERITY_DEFAULT);
					}
					return severity;
				}
			});
		} catch (CoreException e) {
			ResourcesPlugin.getPlugin().getLog().log(new Status(IStatus.ERROR, ResourcesPlugin.getPlugin().getBundle().getSymbolicName(), e.getMessage(), e));
		}

	}

}
