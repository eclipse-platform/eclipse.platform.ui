package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.net.URL;

import org.eclipse.ant.internal.core.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.*;
import org.eclipse.ui.externaltools.internal.core.ExternalToolsPlugin;

/**
 * Label provider for the items in the custmomize ant preference page: 
 * URLs, Tasks, and Types.
 */
public class AntPageLabelProvider
	extends LabelProvider
	implements ITableLabelProvider {
		
	private Image folderImage;
	private Image jarImage;
	private Image taskImage;
	private Image typeImage;

	public AntPageLabelProvider() {
	}
	public void dispose() {
		//note: folder and task are shared images
		folderImage = null;
		taskImage = null;
		if (jarImage != null) {
			jarImage.dispose();
			jarImage = null;
		}
		if (typeImage != null) {
			typeImage.dispose();
			typeImage = null;
		}
	}
	public Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof URL) {
			URL url = (URL) element;
			if (url.getFile().endsWith("/")) { //$NON-NLS-1$
				return folderImage();
			} else {
				return jarImage();
			}
		}
		if (element instanceof Task) {
			return taskImage();
		}
		if (element instanceof Type) {
			return typeImage();
		}
		return null;
	}
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof URL) {
			return ((URL) element).getFile();
		}
		if (element instanceof Task) {
			Task task = (Task) element;
			return task.getTaskName() + " (" + task.getLibrary().getFile() + ": " + task.getClassName() + ")"; //$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
		}
		if (element instanceof Type) {
			Type type = (Type) element;
			return type.getTypeName() + " (" + type.getLibrary().getFile() + ": " + type.getClassName() + ")"; //$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
		}
		return element.toString();
	}
	private Image folderImage() {
		if (folderImage != null)
			return folderImage;
		folderImage =
			PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_OBJ_FOLDER);
		return folderImage;
	}
	private Image jarImage() {
		if (jarImage != null)
			return jarImage;
		jarImage =
			ExternalToolsPlugin
				.getDefault()
				.getImageDescriptor(ExternalToolsPlugin.IMG_JAR_FILE)
				.createImage();
		return jarImage;
	}
	private Image taskImage() {
		if (taskImage != null)
			return taskImage;
		taskImage =
			PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_OBJS_TASK_TSK);
		return taskImage;
	}
	private Image typeImage() {
		if (typeImage != null)
			return typeImage;
		typeImage =
			ExternalToolsPlugin
				.getDefault()
				.getImageDescriptor(ExternalToolsPlugin.IMG_TYPE)
				.createImage();
		return typeImage;
	}
}