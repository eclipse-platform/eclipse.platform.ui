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
package org.eclipse.ant.internal.ui;

import java.net.URL;

import org.eclipse.ant.internal.core.Task;
import org.eclipse.ant.internal.core.Type;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Label provider for the items in the custmomize ant preference page: 
 * URLs, Tasks, and Types.
 */
public class CustomizeAntLabelProvider extends LabelProvider implements ITableLabelProvider {
	protected Image folderImage;
	protected Image jarImage;
	protected Image taskImage;
	protected Image typeImage;
	
public CustomizeAntLabelProvider() {
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
		URL url = (URL)element;
		if (url.getFile().endsWith("/")) {
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
		return ((URL)element).getFile();
	}
	if (element instanceof Task) {
		Task task = (Task)element;
		return task.getTaskName() + " (" + task.getLibrary().getFile()+ ": " + task.getClassName() + ")";
	}
	if (element instanceof Type) {
		Type type = (Type)element;
		return type.getTypeName() + " (" + type.getLibrary().getFile()+ ": " + type.getClassName() + ")";
	}
	return element.toString();
}
protected Image folderImage() {
	if (folderImage != null)
		return folderImage;
	folderImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
	return folderImage;
}
protected Image jarImage() {
	if (jarImage != null)
		return jarImage;
	jarImage = AntUIPlugin.getPlugin().getImageDescriptor(AntUIPlugin.IMG_JAR_FILE).createImage();
	return jarImage;
}
protected Image taskImage() {
	if (taskImage != null)
		return taskImage;
	taskImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_TASK_TSK);
	return taskImage;
}
protected Image typeImage() {
	if (typeImage != null)
		return typeImage;
	typeImage = AntUIPlugin.getPlugin().getImageDescriptor(AntUIPlugin.IMG_TYPE).createImage();
	return typeImage;
}
}