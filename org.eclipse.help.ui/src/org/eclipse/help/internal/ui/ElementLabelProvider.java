package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.util.Iterator;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.internal.ui.util.WorkbenchResources;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.help.*;
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
		if (element instanceof IHelpResource) {
			IHelpResource topic = (IHelpResource) element;
			if (("".equals(topic.getHref())) || (null == topic.getHref()))
				return imgRegistry.get(IMAGE_TOPIC_FOLDER);
			else {
				ITopic[] children = null;
				if (element instanceof ITopic)
					children = ((ITopic)element).getSubtopics();
				else if (element instanceof IToc)
					children = ((IToc)element).getTopics();
				if (children == null || children.length == 0)
					return imgRegistry.get(IMAGE_TOPIC);
			}
			return imgRegistry.get(IMAGE_TOPIC_AND_FOLDER);
		}
		if (element instanceof IHelpResource) {
			return imgRegistry.get(IMAGE_TOPIC);
		}
		return null;
	}
	public String getText(Object element) {
		if (element instanceof IHelpResource)
			return ((IHelpResource) element).getLabel();
		if (element instanceof IHelpResource)
			return ((IHelpResource) element).getLabel();
		return null;
	}
}
