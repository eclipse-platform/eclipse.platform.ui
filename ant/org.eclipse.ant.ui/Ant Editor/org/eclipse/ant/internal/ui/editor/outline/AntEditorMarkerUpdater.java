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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ui.texteditor.MarkerUtilities;

public class AntEditorMarkerUpdater {
	
	private AntModel fModel= null;
	private List fCollectedProblems= new ArrayList();
	public static final String BUILDFILE_PROBLEM_MARKER = AntUIPlugin.PI_ANTUI + ".buildFileProblem"; //$NON-NLS-1$
	private IFile fFile= null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.outline.IProblemRequestor#acceptProblem(org.eclipse.ant.internal.ui.editor.outline.IProblem)
	 */
	public void acceptProblem(IProblem problem) {
		if (fCollectedProblems.contains(problem)) {
			return;
		}
		fCollectedProblems.add(problem);
	}
	
	public void beginReporting() {
		fCollectedProblems.clear();
	}
	
	private void removeProblems() {
		IFile file= getFile();
		if (file == null || !file.exists()) {
			return;
		}
		try {
			file.deleteMarkers(BUILDFILE_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			// assume there were no problems
		}
	}
	
	private void createMarker(IProblem problem) {
		IFile file = getFile();
		Map attributes= getMarkerAttributes(problem);
		try {
			MarkerUtilities.createMarker(file, attributes, BUILDFILE_PROBLEM_MARKER);
		} catch (CoreException e) {			
		} 
	}
	
	public void setModel(AntModel model) {
		fModel= model;
	}
	
	public void updateMarkers() {
		removeProblems();
		if (!shouldAddMarkers()) {
			return;
		}

		if (fCollectedProblems.size() > 0) {
			Iterator e= fCollectedProblems.iterator();
			while (e.hasNext()) {
				IProblem problem= (IProblem) e.next();
				createMarker(problem);
			}
			fCollectedProblems.clear();
		}
	}
	
	private IFile getFile() {
		if (fFile == null) {
			fFile= fModel.getFile();
		}
		return fFile;
	}
	
	/**
	 * Returns the attributes with which a newly created marker will be
	 * initialized.
	 *
	 * @return the initial marker attributes
	 */
	private Map getMarkerAttributes(IProblem problem) {
		
		Map attributes= new HashMap(11);
		int severity= IMarker.SEVERITY_ERROR;
		if (problem.isWarning()) {
			severity= IMarker.SEVERITY_WARNING;
		}
		// marker line numbers are 1-based
		MarkerUtilities.setMessage(attributes, problem.getUnmodifiedMessage());
		MarkerUtilities.setLineNumber(attributes, problem.getLineNumber());
		MarkerUtilities.setCharStart(attributes, problem.getOffset());
		MarkerUtilities.setCharEnd(attributes, problem.getOffset() + problem.getLength());
		attributes.put(IMarker.SEVERITY, new Integer(severity));
		return attributes;
	}
	
	/**
	 * Returns whether or not to add markers to the file based on the file's content type.
	 * The content type is considered an Ant buildfile if the XML has a root &quot;project&quot; element.
	 * Content type is defined in the org.eclipse.ant.core plugin.xml.
	 * @return whether or not to add markers to the file based on the files content type
	 */
	private boolean shouldAddMarkers() {
		IFile file= getFile();
		if (file == null || !file.exists()) {
			return false;
		}
		IContentDescription description;
		try {
			description = file.getContentDescription();
		} catch (CoreException e) {
			return false;
		}
		if (description != null) {
			IContentType type= description.getContentType();
			return type != null && AntCorePlugin.ANT_BUILDFILE_CONTENT_TYPE.equals(type.getId());
		}
		return false;
	}
}