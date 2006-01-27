/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.cheatsheets.composite.explorer;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.cheatsheets.ICompositeCheatSheetTask;
import org.eclipse.ui.internal.cheatsheets.CheatSheetPlugin;
import org.eclipse.ui.internal.cheatsheets.composite.views.TaskEditorManager;
import org.osgi.framework.Bundle;

public class TreeLabelProvider extends LabelProvider {

	private static int BLOCKED = -1;
	private Image defaultImage = null; // Image for tasks with null kind
	
	/*
	 * A set of related images
	 */
	private class ImageSet {
		// Use a map rather than array so the nuber of icons is not hard coded
		Map images = new HashMap();
		
		public void put(int index, Image image) {
			images.put(Integer.toString(index), image);
		}
		
		public Image getImage(int index) {
			return (Image)images.get(Integer.toString(index));
		}
		
		void dispose() {
			for (Iterator iter = images.values().iterator(); iter.hasNext(); ) {
				Image nextImage = (Image)iter.next();
				nextImage.dispose();
			}		
		}
	}
	
	private Map imageMap = null; // each entry is an ImageSet
		
	public TreeLabelProvider() {
		imageMap = new HashMap();
	}

	public String getText(Object obj) {
		if (obj instanceof ICompositeCheatSheetTask)
			return ((ICompositeCheatSheetTask) obj).getName();
		return obj.toString();
	}

	public Image getImage(Object obj) {
		if (obj instanceof ICompositeCheatSheetTask) {
			ICompositeCheatSheetTask task = (ICompositeCheatSheetTask) obj;
			return lookupImage(task.getKind(), task.getState(), task.isStartable());		
		}
		return super.getImage(obj);
	}
	
	private Image getDefaultImage() {
		if (defaultImage == null) {
			ImageDescriptor desc = createImageDescriptor("icons/obj16/info_task.gif"); //$NON-NLS-1$
			defaultImage = desc.createImage();	
		}
		return defaultImage;
	}

	public Image lookupImage(String kind, int state, boolean isStartable) {
		if (kind == null) {
			return getDefaultImage();
		}
		ImageSet images = (ImageSet) imageMap.get(kind);
		if (images == null) {
			images = createImages(kind);
			imageMap.put(kind, images);
		}
		if (isStartable) {
		    return images.getImage(state);
		}
		return images.getImage(BLOCKED);
	}

	private ImageSet createImages(String kind) {
		ImageSet images = new ImageSet();
		ImageDescriptor desc = TaskEditorManager.getInstance().getImageDescriptor(kind);
		if (desc != null) {		
			images.put(ICompositeCheatSheetTask.NOT_STARTED, desc.createImage());
			ImageDescriptor inProgress = createImageDescriptor("icons/ovr16/task_in_progress.gif"); //$NON-NLS-1$
			OverlayIcon icon = new OverlayIcon(desc, new ImageDescriptor[][] {
					{}, { inProgress } });
			images.put(ICompositeCheatSheetTask.IN_PROGRESS, icon.createImage());
			ImageDescriptor complete = createImageDescriptor("icons/ovr16/task_complete.gif"); //$NON-NLS-1$
			icon = new OverlayIcon(desc, new ImageDescriptor[][] {
					{}, { complete } });
			images.put(ICompositeCheatSheetTask.COMPLETED, icon.createImage());
			ImageDescriptor blocked = createImageDescriptor("icons/ovr16/task_blocked.gif"); //$NON-NLS-1$
			icon = new OverlayIcon(desc, new ImageDescriptor[][] {
					{}, { blocked } });
			images.put(BLOCKED, icon.createImage());
		}
		return images;
	}

	private ImageDescriptor createImageDescriptor(String relativePath) {
		Bundle bundle = CheatSheetPlugin.getPlugin().getBundle();
		URL url = Platform.find(bundle, new Path(relativePath));
		try {
			url = Platform.resolve(url);
			return ImageDescriptor.createFromURL(url);
		} catch (IOException e) {
			return null;
		}
	}

	public void dispose() {
		if (imageMap != null) {
			for (Iterator iter = imageMap.values().iterator(); iter.hasNext(); ) {
			    ImageSet nextImages = (ImageSet)iter.next();
			    nextImages.dispose();
			}
			imageMap = null;
		}
		if (defaultImage != null) {
			defaultImage.dispose();
			defaultImage = null;
		}
	}
}
