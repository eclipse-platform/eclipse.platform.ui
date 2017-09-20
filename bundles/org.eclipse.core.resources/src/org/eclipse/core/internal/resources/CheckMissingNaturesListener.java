/*******************************************************************************
 * Copyright (c) 2016-2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.util.*;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;

public class CheckMissingNaturesListener implements IResourceChangeListener, IPreferenceChangeListener {

	public static final String MARKER_TYPE = ResourcesPlugin.getPlugin().getBundle().getSymbolicName() + ".unknownNature"; //$NON-NLS-1$
	public static final String NATURE_ID_ATTRIBUTE = "natureId"; //$NON-NLS-1$

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getDelta() == null) {
			return;
		}
		try {
			final Set<IProject> modifiedProjects = new HashSet<>();
			event.getDelta().accept(delta -> {
				if (delta.getResource() != null && delta.getResource().getType() == IResource.PROJECT && (delta.getKind() == IResourceDelta.ADDED || delta.getKind() == IResourceDelta.CHANGED)) {
					modifiedProjects.add((IProject) delta.getResource());
				}
				return delta.getResource() == null || delta.getResource().getType() == IResource.ROOT;
			});
			updateMarkers(modifiedProjects);
		} catch (CoreException e) {
			ResourcesPlugin.getPlugin().getLog().log(new Status(IStatus.ERROR, ResourcesPlugin.getPlugin().getBundle().getSymbolicName(), e.getMessage(), e));
		}
	}

	int getMissingNatureSeverity(final IProject project) {
		int severity = PreferenceInitializer.PREF_MISSING_NATURE_MARKER_SEVERITY_DEFAULT;
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(ResourcesPlugin.PI_RESOURCES);
		if (node != null) {
			severity = node.getInt(ResourcesPlugin.PREF_MISSING_NATURE_MARKER_SEVERITY, PreferenceInitializer.PREF_MISSING_NATURE_MARKER_SEVERITY_DEFAULT);
		}
		return severity;
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (ResourcesPlugin.PREF_MISSING_NATURE_MARKER_SEVERITY.equals(event.getKey())) {
			final int newSeverity = event.getNewValue() != null ? Integer.parseInt((String) event.getNewValue()) : PreferenceInitializer.PREF_MISSING_NATURE_MARKER_SEVERITY_DEFAULT;
			final int oldSeverity = event.getOldValue() != null ? Integer.parseInt((String) event.getOldValue()) : PreferenceInitializer.PREF_MISSING_NATURE_MARKER_SEVERITY_DEFAULT;
			if (newSeverity < 0) {
				removeAllMarkers(ResourcesPlugin.getWorkspace().getRoot());
			} else if (oldSeverity < 0 && newSeverity >= 0) {
				updateMarkers(Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects()));
			} else {
				updateExistingMarkersSeverity(ResourcesPlugin.getWorkspace().getRoot(), newSeverity);
			}
		}
	}

	private void removeAllMarkers(IContainer workspaceRootOrProject) {
		final Collection<IMarker> markers = getRelatedMarkers(workspaceRootOrProject);
		if (markers.isEmpty()) {
			return;
		}
		WorkspaceJob job = new WorkspaceJob(Messages.updateUnknownNatureMarkers) {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				for (IMarker marker : markers) {
					if (marker.exists() && marker.getResource().isAccessible()) {
						marker.delete();
					}
				}
				return Status.OK_STATUS;
			}

			@Override
			public boolean belongsTo(Object family) {
				return super.belongsTo(family) || MARKER_TYPE.equals(family);
			}
		};
		job.setUser(false);
		job.setSystem(true);
		job.setPriority(Job.DECORATE);
		job.setRule(workspaceRootOrProject);
		job.schedule();
	}

	private void updateExistingMarkersSeverity(IContainer workspaceRootOrProject, int newSeverity) {
		final Collection<IMarker> markers = getRelatedMarkers(workspaceRootOrProject);
		if (markers.isEmpty()) {
			return;
		}
		WorkspaceJob job = new WorkspaceJob(Messages.updateUnknownNatureMarkers) {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				for (IMarker marker : markers) {
					if (marker.exists() && marker.getResource().isAccessible()) {
						marker.setAttribute(IMarker.SEVERITY, newSeverity);
					}
				}
				return Status.OK_STATUS;
			}

			@Override
			public boolean belongsTo(Object family) {
				return super.belongsTo(family) || MARKER_TYPE.equals(family);
			}
		};
		job.setUser(false);
		job.setSystem(true);
		job.setPriority(Job.DECORATE);
		job.setRule(workspaceRootOrProject);
		job.schedule();
	}

	private void updateMarkers(Collection<IProject> projects) {
		for (IProject project : projects) {
			if (!project.isAccessible()) {
				continue;
			}
			int severity = getMissingNatureSeverity(project);
			try {
				if (severity < 0) {
					removeAllMarkers(project);
					continue;
				}

				final Set<String> missingNatures = new HashSet<>();
				for (String natureId : project.getDescription().getNatureIds()) {
					if (project.getWorkspace().getNatureDescriptor(natureId) == null) {
						missingNatures.add(natureId);
					}
				}

				final Set<IMarker> toRemove = new HashSet<>();
				for (IMarker existingMarker : getRelatedMarkers(project)) {
					String markerNature = existingMarker.getAttribute(NATURE_ID_ATTRIBUTE, ""); //$NON-NLS-1$
					if (!missingNatures.contains(markerNature)) {
						toRemove.add(existingMarker);
					} else {
						// no need to create a new marker
						missingNatures.remove(markerNature);
					}
				}

				if (!toRemove.isEmpty() || !missingNatures.isEmpty()) {
					WorkspaceJob workspaceJob = new WorkspaceJob(Messages.updateUnknownNatureMarkers) {
						@Override
						public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
							for (IMarker marker : toRemove) {
								marker.delete();
							}
							for (String natureId : missingNatures) {
								IMarker marker = project.createMarker(MARKER_TYPE);
								marker.setAttribute(IMarker.SEVERITY, getMissingNatureSeverity(project));
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
					workspaceJob.setRule(project);
					workspaceJob.setUser(false);
					workspaceJob.setSystem(true);
					workspaceJob.setPriority(Job.DECORATE);
					workspaceJob.schedule();
				}
			} catch (CoreException e) {
				ResourcesPlugin.getPlugin().getLog().log(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, e.getMessage(), e));
			}
		}
	}

	protected Collection<IMarker> getRelatedMarkers(IContainer workspaceRootOrProject) {
		if (!workspaceRootOrProject.isAccessible()) {
			return Collections.emptyList();
		}
		try {
			return Arrays.asList(workspaceRootOrProject.findMarkers(MARKER_TYPE, true, workspaceRootOrProject.getType() == IResource.ROOT ? IResource.DEPTH_ONE : IResource.DEPTH_ZERO));
		} catch (CoreException e) {
			ResourcesPlugin.getPlugin().getLog().log(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, e.getMessage(), e));
			return Collections.emptyList();
		}
	}

}
