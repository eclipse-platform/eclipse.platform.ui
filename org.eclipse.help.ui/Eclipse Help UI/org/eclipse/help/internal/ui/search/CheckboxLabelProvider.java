package org.eclipse.help.internal.ui.search;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.util.Iterator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.help.internal.contributions.*;
import org.eclipse.help.internal.ui.util.WorkbenchResources;
import org.eclipse.help.internal.ui.*;

/**
 * Label and image provider for topic elements
 */
public class CheckboxLabelProvider extends LabelProvider {
	static CheckboxLabelProvider instance = null;

	static final String IMAGE_TOPIC = "topic_icon";
	static final String IMAGE_TOPIC_FOLDER = "topicfolder_icon";
	static final String IMAGE_TOPIC_AND_FOLDER = "topicandfolder_icon";
	static ImageRegistry imgRegistry = null;

	/**
	 * ElementLabelProvider Constructor
	 */
	CheckboxLabelProvider() {
		imgRegistry = WorkbenchHelpPlugin.getDefault().getImageRegistry();
		if (imgRegistry.get(IMAGE_TOPIC) == null)
			imgRegistry.put(
				IMAGE_TOPIC,
				ImageDescriptor.createFromURL(WorkbenchResources.getImagePath(IMAGE_TOPIC)));
	}
	public static CheckboxLabelProvider getDefault() {
		if (instance == null)
			instance = new CheckboxLabelProvider();
		return instance;
	}
	public Image getImage(Object element) {
		return imgRegistry.get(IMAGE_TOPIC_FOLDER);
	}
	public String getText(Object element) {
		return ((Contribution) element).getLabel();
	}
}
