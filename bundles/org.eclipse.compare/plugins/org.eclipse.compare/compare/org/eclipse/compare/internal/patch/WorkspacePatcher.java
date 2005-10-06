/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal.patch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * A Patcher 
 * - knows how to parse various patch file formats into some in-memory structure,
 * - holds onto the parsed data and the options to use when applying the patches,
 * - knows how to apply the patches to files and folders.
 */
public class WorkspacePatcher extends Patcher implements IAdaptable, IWorkbenchAdapter {

	private DiffProject[] fDiffProjects;

	private boolean fIsWorkspacePatch = false;

	//API for writing new multi-project patch format
	public static final String MULTIPROJECTPATCH_HEADER= "### Eclipse Workspace Patch"; //$NON-NLS-1$
	public static final String MULTIPROJECTPATCH_VERSION= "1.0"; //$NON-NLS-1$
	public static final String MULTIPROJECTPATCH_PROJECT= "#P"; //$NON-NLS-1$

	/**
	 * Appends the multiproject header and version number to the passed in stream. Users
	 * should call this first during the patch creation process if they want their patches
	 * to be applied across the workspace.  
	 *  
	 * @param stream
	 */
	public static void writeMultiProjectPatchHeader(PrintStream stream) {
		stream.println(MULTIPROJECTPATCH_HEADER+" "+MULTIPROJECTPATCH_VERSION); //$NON-NLS-1$
	}

	/**
	 * Appends the header for a multiproject patch project to the passed in stream. This should
	 * be called before adding any additional patch content for the passed in project in order to 
	 * allow the patch to be properly rooted across the workspace.  
	 * @param stream
	 * @param project
	 */
	public static void addMultiProjectPatchProject(PrintStream stream, IProject project) {
		stream.println(MULTIPROJECTPATCH_PROJECT+" "+project.getName()); //$NON-NLS-1$
	}
	
	public WorkspacePatcher() {
		// nothing to do
	}

	public DiffProject[] getDiffProjects() {
		return fDiffProjects;
	}

	boolean isWorkspacePatch() {
		return fIsWorkspacePatch;
	}

	//---- parsing patch files

	public void parse(BufferedReader reader) throws IOException {
		List diffs = new ArrayList();
		HashMap diffProjects = new HashMap(4);
		String line = null;
		boolean reread = false;
		String diffArgs = null;
		String fileName = null;
		//no project means this is a single patch,create a placeholder project for now
		//which will be replaced by the target selected by the user in the preview pane
		String project = ""; //$NON-NLS-1$
		fIsWorkspacePatch = false;

		LineReader lr = new LineReader(reader);
		if (!"carbon".equals(SWT.getPlatform())) //$NON-NLS-1$
			lr.ignoreSingleCR();

		// Test for our format
		line = lr.readLine();
		if (line.startsWith(MULTIPROJECTPATCH_HEADER)) {
			fIsWorkspacePatch = true;
		} else {
			parse(lr, line);
			return;
		}

		// read leading garbage
		while (true) {
			if (!reread)
				line = lr.readLine();
			reread = false;
			if (line == null)
				break;
			if (line.length() < 4)
				continue; // too short

			if (line.startsWith(MULTIPROJECTPATCH_PROJECT)) {
				project = line.substring(2).trim();
				continue;
			}

			if (line.startsWith("Index: ")) { //$NON-NLS-1$
				fileName = line.substring(7).trim();
				continue;
			}
			if (line.startsWith("diff")) { //$NON-NLS-1$
				diffArgs = line.substring(4).trim();
				continue;
			}

			if (line.startsWith("--- ")) { //$NON-NLS-1$
				//if there is no current project or
				//the current project doesn't equal the newly parsed project
				//reset the current project to the newly parsed one, create a new DiffProject
				//and add it to the array
				DiffProject diffProject;
				if (!diffProjects.containsKey(project)) {
					IProject iproject = ResourcesPlugin.getWorkspace().getRoot().getProject(project);
					diffProject = new DiffProject(iproject);
					diffProjects.put(project, diffProject);
				} else {
					diffProject = (DiffProject) diffProjects.get(project);
				}

				line = readUnifiedDiff(diffs, lr, line, diffArgs, fileName, diffProject);
				diffArgs = fileName = null;
				reread = true;
			}
		}

		lr.close();

		fDiffs = (Diff[]) diffs.toArray(new Diff[diffs.size()]);
		fDiffProjects = (DiffProject[]) diffProjects.values().toArray(new DiffProject[diffProjects.size()]);
	}

	private String readUnifiedDiff(List diffs, LineReader lr, String line, String diffArgs, String fileName, DiffProject diffProject) throws IOException {
		List newDiffs = new ArrayList();
		String nextLine = readUnifiedDiff(newDiffs, lr, line, diffArgs, fileName);
		for (Iterator iter = newDiffs.iterator(); iter.hasNext();) {
			Diff diff = (Diff) iter.next();
			diff.setProject(diffProject);
			diffs.add(diff);
		}
		return nextLine;
	}

	public void applyAll(IProgressMonitor pm, Shell shell, String title) throws CoreException {
		if (!fIsWorkspacePatch) {
			super.applyAll(pm, shell, title);
		} else {
			final int WORK_UNIT = 10;

			// get all files to be modified in order to call validateEdit
			List list = new ArrayList();
			for (int j = 0; j < fDiffProjects.length; j++) {
				DiffProject diffProject = fDiffProjects[j];
				list.addAll(Arrays.asList(diffProject.getTargetFiles()));
			}
			//validate the files for editing
			if (!Utilities.validateResources(list, shell, title))
				return;

			if (pm != null) {
				String message = PatchMessages.Patcher_Task_message;
				pm.beginTask(message, fDiffs.length * WORK_UNIT);
			}

			for (int i = 0; i < fDiffs.length; i++) {

				int workTicks = WORK_UNIT;

				Diff diff = fDiffs[i];
				if (diff.isEnabled()) {
					IFile file = diff.getTargetFile();
					IPath path = file.getProjectRelativePath();
					if (pm != null)
						pm.subTask(path.toString());
					createPath(file.getProject(), path);

					List failed = new ArrayList();
					List result = null;

					int type = diff.getType();
					switch (type) {
						case Differencer.ADDITION :
							// patch it and collect rejected hunks
							result = apply(diff, file, true, failed);
							store(createString(result), file, new SubProgressMonitor(pm, workTicks));
							workTicks -= WORK_UNIT;
							break;
						case Differencer.DELETION :
							file.delete(true, true, new SubProgressMonitor(pm, workTicks));
							workTicks -= WORK_UNIT;
							break;
						case Differencer.CHANGE :
							// patch it and collect rejected hunks
							result = apply(diff, file, false, failed);
							store(createString(result), file, new SubProgressMonitor(pm, workTicks));
							workTicks -= WORK_UNIT;
							break;
					}

					if (failed.size() > 0) {
						IPath pp = null;
						if (path.segmentCount() > 1) {
							pp = path.removeLastSegments(1);
							pp = pp.append(path.lastSegment() + REJECT_FILE_EXTENSION);
						} else
							pp = new Path(path.lastSegment() + REJECT_FILE_EXTENSION);
						file = createPath(file.getProject(), pp);
						if (file != null) {
							store(getRejected(failed), file, pm);
							try {
								IMarker marker = file.createMarker(MARKER_TYPE);
								marker.setAttribute(IMarker.MESSAGE, PatchMessages.Patcher_Marker_message);
								marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
							} catch (CoreException ex) {
								// NeedWork
							}
						}
					}
				}

				if (pm != null) {
					if (pm.isCanceled())
						break;
					if (workTicks > 0)
						pm.worked(workTicks);
				}
			}
		}
	}

	public ISchedulingRule[] getTargetProjects() {
		List projects = new ArrayList();
		for (int i = 0; i < fDiffProjects.length; i++) {
			DiffProject diffProject = fDiffProjects[i];
			projects.add(diffProject.getProject());
		}
		return (ISchedulingRule[]) projects.toArray(new ISchedulingRule[projects.size()]);
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return this;
		}
		return null;
	}

	public Object[] getChildren(Object o) {
		if (fIsWorkspacePatch) {
			return fDiffProjects;
		}
		if (fDiffs != null)
			return fDiffs;
		return new Object[0];
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	public String getLabel(Object o) {
		return null;
	}

	public Object getParent(Object o) {
		return null;
	}
}
