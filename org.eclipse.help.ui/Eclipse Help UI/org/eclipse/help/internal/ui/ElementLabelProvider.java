package org.eclipse.help.internal.ui;

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

/**
 * Label and image provider for topic elements
 */
public class ElementLabelProvider extends LabelProvider {
	static ElementLabelProvider instance = null;

	static final String IMAGE_TOPIC = "topic_icon";
	static final String IMAGE_TOPIC_FOLDER = "topicfolder_icon";
	static final String IMAGE_TOPIC_AND_FOLDER = "topicandfolder_icon";
	static ImageRegistry imgRegistry = null;

	/**
	 * ElementLabelProvider Constructor
	 */
	ElementLabelProvider() {
		imgRegistry = WorkbenchHelpPlugin.getDefault().getImageRegistry();
		imgRegistry.put(
			IMAGE_TOPIC,
			ImageDescriptor.createFromURL(WorkbenchResources.getImagePath(IMAGE_TOPIC)));
		imgRegistry.put(
			IMAGE_TOPIC_AND_FOLDER,
			ImageDescriptor.createFromURL(
				WorkbenchResources.getImagePath(IMAGE_TOPIC_AND_FOLDER)));
		imgRegistry.put(
			IMAGE_TOPIC_FOLDER,
			ImageDescriptor.createFromURL(
				WorkbenchResources.getImagePath(IMAGE_TOPIC_FOLDER)));
	}
	public static ElementLabelProvider getDefault() {
		if (instance == null)
			instance = new ElementLabelProvider();
		return instance;
	}
	public Image getImage(Object element) {
		Topic topic = (Topic) element;
		Iterator children = topic.getChildren();
		if (children == null || !children.hasNext())
			return imgRegistry.get(IMAGE_TOPIC);
		else
			if (("".equals(topic.getHref())) || (null == topic.getHref()))
				return imgRegistry.get(IMAGE_TOPIC_FOLDER);
			else
				return imgRegistry.get(IMAGE_TOPIC_AND_FOLDER);
	}
	public String getText(Object element) {
		return ((Contribution) element).getLabel();
	}
}
