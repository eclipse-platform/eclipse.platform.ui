package org.eclipse.ui.externaltools.internal.view;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolType;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolTypeRegistry;
import org.eclipse.ui.externaltools.model.ExternalTool;

/**
 * Provides the label for external tools and types.
 */
public class ExternalToolLabelProvider extends LabelProvider {
	/**
	 * The cache of images that have been dispensed by this provider.
	 * Maps a tool type to an Image. These images will be
	 * disposed when provider no longer needed.
	 */
	private Map imageTable;
	
	/**
	 * The shared workbench supplied folder image. Never disposed
	 * by provider.
	 */
	private Image folderImage;
	
	/**
	 * Create a new external tools label provider
	 */
	public ExternalToolLabelProvider() {
		super();
		folderImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
	}

	/* (non-Javadoc)
	 * Method declared on IBaseLabelProvider
	 */
	public final void dispose() {
		// Dispose images created by this provider
		if (imageTable != null) {
			Iterator enum = imageTable.values().iterator();
			while (enum.hasNext()) {
				((Image) enum.next()).dispose();
			}
			imageTable = null;
		}
		
		// Do not dispose this shared workbench image
		folderImage = null;
	}

	/* (non-Javadoc)
	 * Method declared on ILabelProvider.
	 */
	public Image getImage(Object element) {
		if (element instanceof ExternalToolType) {
			return folderImage;
		}
		
		if (element instanceof ExternalTool) {
			ExternalTool tool = (ExternalTool) element;
			if (imageTable == null)
				imageTable = new Hashtable(10);
			Image image = (Image) imageTable.get(tool.getType());
			if (image == null) {
				ExternalToolTypeRegistry registry = ExternalToolsPlugin.getDefault().getTypeRegistry();
				ExternalToolType type = registry.getToolType(tool.getType());
				if (type == null)
					image = ImageDescriptor.getMissingImageDescriptor().createImage();
				else
					image = type.getImageDescriptor().createImage();
				imageTable.put(tool.getType(), image);
			}
			return image;
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * Method declared on ILabelProvider.
	 */
	public String getText(Object element) {
		if (element instanceof ExternalToolType)
			return ((ExternalToolType)element).getName();
		
		if (element instanceof ExternalTool)
			return ((ExternalTool)element).getName();
		
		return ""; //$NON-NLS-1$;
	}
}
