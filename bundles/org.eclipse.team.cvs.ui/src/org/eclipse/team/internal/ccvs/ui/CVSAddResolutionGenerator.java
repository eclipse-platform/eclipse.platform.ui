/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/

package org.eclipse.team.internal.ccvs.ui;
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSResource;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.ui.IMarkerResolution;

/**
 * Generate marker resoultions for a cvs remove marker
 */
public class CVSAddResolutionGenerator extends CVSAbstractResolutionGenerator {
	/*
	 * @see IMarkerResolutionGenerator#getResolutions(IMarker)
	 */
	public IMarkerResolution[] getResolutions(IMarker marker) {
		IMarkerResolution manage = new IMarkerResolution() {
			public String getLabel() {
				return Policy.bind("CVSAddResolutionGenerator.Add_Resource_to_CVS_1"); //$NON-NLS-1$
			}
			public void run(IMarker marker) {
				try {
					final IResource resource = marker.getResource();
					ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
					final TeamException[] exception = new TeamException[] {null};
					if ( ! cvsResource.isManaged()) {
						CVSAddResolutionGenerator.this.run(new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor)throws InvocationTargetException, InterruptedException {
								try {
									((CVSTeamProvider)RepositoryProvider.getProvider(resource.getProject())).add(new IResource[] {resource}, IResource.DEPTH_ZERO, monitor);
								} catch (TeamException e) {
									exception[0] = e;
								}
							}
						});
					}
					if (exception[0] != null) {
						throw exception[0];
					}
					marker.delete();
				} catch (TeamException e) {
					handle(e, null, null);
				} catch (CoreException e) {
					handle(e, null, null);
				} catch (InvocationTargetException e) {
					handle(e, null, null);
				}  catch (InterruptedException e) {
					// do nothing
				}
			}
		};
		IMarkerResolution manageDeep = new IMarkerResolution() {
			public String getLabel() {
				return Policy.bind("CVSAddResolutionGenerator.Add_Resource_and_Children_to_CVS_2"); //$NON-NLS-1$
			}
			public void run(IMarker marker) {
				try {
					final IResource resource = marker.getResource();
					ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
					final TeamException[] exception = new TeamException[] {null};
					if ( ! cvsResource.isManaged()) {
						CVSAddResolutionGenerator.this.run(new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor)throws InvocationTargetException, InterruptedException {
								try {
									((CVSTeamProvider)RepositoryProvider.getProvider(resource.getProject())).add(new IResource[] {resource}, IResource.DEPTH_INFINITE, monitor);
								} catch (TeamException e) {
									exception[0] = e;
								}
							}
						});
					}
					if (exception[0] != null) {
						throw exception[0];
					}
					marker.delete();
				} catch (TeamException e) {
					handle(e, null, null);
				} catch (CoreException e) {
					handle(e, null, null);
				} catch (InvocationTargetException e) {
					handle(e, null, null);
				}  catch (InterruptedException e) {
					// do nothing
				}
			}

		};
		IMarkerResolution ignore =  new IMarkerResolution() {
			public String getLabel() {
				return Policy.bind("CVSAddResolutionGenerator.Add_to_.cvsignore_3"); //$NON-NLS-1$
			}
			public void run(IMarker marker) {
				try {
					ICVSResource resource = CVSWorkspaceRoot.getCVSResourceFor(marker.getResource());
					if ( resource.isManaged()) {
						resource.unmanage(null);
					}
					resource.setIgnored();
					marker.delete();
				} catch (CVSException e) {
					handle(e, null, null);
				} catch (CoreException e) {
					handle(e, null, null);
				}
			}

		};
		if (marker.getResource().getType() == IResource.FILE) {
			return new IMarkerResolution[] {manage, ignore};
		} else {
			return new IMarkerResolution[] {manageDeep, manage, ignore};
		}
	}
}
