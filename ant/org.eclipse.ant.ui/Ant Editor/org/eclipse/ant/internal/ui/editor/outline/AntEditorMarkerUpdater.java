/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor.outline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class AntEditorMarkerUpdater {
	
	private AntModel fModel= null;
	private List fCollectedProblems= new ArrayList();
	public static final String BUILDFILE_PROBLEM_MARKER = AntUIPlugin.PI_ANTUI + ".buildFileProblem"; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.outline.IProblemRequestor#acceptProblem(org.eclipse.ant.internal.ui.editor.outline.IProblem)
	 */
	public void acceptProblem(IProblem problem) {
		fCollectedProblems.add(problem);
	}
	
	public void beginReporting() {
		fCollectedProblems.clear();
	}
	
	private void removeProblems() {
		IFile file= fModel.getFile();
		try {
			if (file != null && file.exists())
				file.deleteMarkers(BUILDFILE_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			// assume there were no problems
		}
	}
	
	private void createMarker(IProblem problem) {
		IFile file = fModel.getFile();
		int lineNumber= problem.getLineNumber();
		
		int severity= IMarker.SEVERITY_ERROR;
		if (problem.isWarning()) {
			severity= IMarker.SEVERITY_WARNING;
		}
		try {
			IMarker marker = file.createMarker(BUILDFILE_PROBLEM_MARKER);
			marker.setAttributes(
					new String[] { 
							IMarker.MESSAGE, 
							IMarker.SEVERITY, 
							IMarker.LOCATION,
							IMarker.CHAR_START, 
							IMarker.CHAR_END, 
							IMarker.LINE_NUMBER
					},
					new Object[] {
							problem.getMessage(),
							new Integer(severity), 
							problem.getMessage(),
							new Integer(problem.getOffset()),
							new Integer(problem.getOffset() + problem.getLength()),
							new Integer(lineNumber)
					}
			);
		} catch (CoreException e) {			
		} 
	}
	
	public void setModel(AntModel model) {
		fModel= model;
	}
	
	public void updateMarkers() {
		removeProblems();
		if (fCollectedProblems.size() > 0) {
			Iterator e= fCollectedProblems.iterator();
			while (e.hasNext()) {
				IProblem problem= (IProblem) e.next();
				createMarker(problem);
			}
			fCollectedProblems.clear();
		}
	}
}