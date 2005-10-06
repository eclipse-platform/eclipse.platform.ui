/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal.patch;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.DiffImage;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;

public class PreviewPatchLabelDecorator implements ILabelDecorator {

	/** Maps strings to images */
	private Map fImages= new Hashtable(10);
	private List fDisposeOnShutdownImages= new ArrayList();

	ImageDescriptor errId= CompareUIPlugin.getImageDescriptor("ovr16/error_ov.gif");	//$NON-NLS-1$
	
	static final String error = "error"; //$NON-NLS-1$
	static final String add = "add"; //$NON-NLS-1$
	static final String delete = "del"; //$NON-NLS-1$
	
	public Image decorateImage(Image image, Object element) {
		
		if (element instanceof Diff){
		  Diff diff = (Diff) element;
		  switch (diff.getType()){
			  case Differencer.ADDITION:
			  return getImageFor(add + (diff.fMatches ? "" : error), image, diff.fMatches); //$NON-NLS-1$
		
			  case Differencer.DELETION:
			  return getImageFor(delete + (diff.fMatches ? "" : error), image, diff.fMatches); //$NON-NLS-1$
			  
			  default:
			  return getImageFor(diff.fMatches ? "" : error, image, diff.fMatches); //$NON-NLS-1$
		  }
		} else if (element instanceof Hunk){
			Hunk hunk = (Hunk) element;
			return getImageFor((hunk.fMatches ? "" : error),image, hunk.fMatches); //$NON-NLS-1$
		}
	
		return null;
	}

	private Image getImageFor(String id, Image image, boolean hasMatches) {
		Image cached_image = (Image) fImages.get(id);
		if (cached_image == null){
			DiffImage diffImage = new DiffImage(image, hasMatches ? null : errId, 16, false);
			cached_image = diffImage.createImage();
			fImages.put(id, cached_image);
			fDisposeOnShutdownImages.add(cached_image);
		}
		return cached_image;
	}

	public String decorateText(String text, Object element) {
		if (element instanceof DiffProject){
			DiffProject project = (DiffProject)element;
			//Check to see if this project exists in the workspace
			IResource projectExistsInWorkspace = ResourcesPlugin.getWorkspace().getRoot().findMember(project.getProject().getFullPath());
			if(projectExistsInWorkspace == null)
				return NLS.bind(PatchMessages.Diff_2Args, new String[]{text, PatchMessages.PreviewPatchLabelDecorator_ProjectDoesNotExist});
			
			if (!project.getName().equals(project.getOriginalProjectName()))	
				return NLS.bind(PatchMessages.Diff_2Args, 
						new String[]{project.getOriginalProjectName(),
						NLS.bind(PatchMessages.PreviewPatchPage_Target, new String[]{project.getName()})});
			
		}
		return null; 
	}
	
	public void dispose() {
		if (fDisposeOnShutdownImages != null) {
			Iterator i= fDisposeOnShutdownImages.iterator();
			while (i.hasNext()) {
				Image img= (Image) i.next();
				if (!img.isDisposed())
					img.dispose();
			}
			fImages= null;
		}
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void addListener(ILabelProviderListener listener) {
		//don't need listener	
	}
	
	public void removeListener(ILabelProviderListener listener) {
		//don't need listener
	} 
}
