package org.eclipse.help.ui.internal.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.help.IHelpResource;
import org.eclipse.help.internal.ui.WorkbenchHelpPlugin;
import org.eclipse.help.internal.ui.util.WorkbenchResources;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
/**
 * Label and image provider for topic elements
 */
public class CheckboxLabelProvider extends LabelProvider {
	static CheckboxLabelProvider instance = null;
	static ImageRegistry imgRegistry = null;
	/**
	 * ElementLabelProvider Constructor
	 */
	CheckboxLabelProvider() {
		imgRegistry = WorkbenchHelpPlugin.getDefault().getImageRegistry();
		if (imgRegistry.get(SearchUIConstants.IMAGE_KEY_TOPIC) == null)
			imgRegistry.put(
				SearchUIConstants.IMAGE_KEY_TOPIC,
				ImageDescriptor.createFromURL(
					WorkbenchResources.getImagePath(SearchUIConstants.IMAGE_KEY_TOPIC)));
	}
	public static CheckboxLabelProvider getDefault() {
		if (instance == null)
			instance = new CheckboxLabelProvider();
		return instance;
	}
	public Image getImage(Object element) {
		return imgRegistry.get(SearchUIConstants.IMAGE_KEY_TOPIC);
	}
	public String getText(Object element) {
		return ((IHelpResource) element).getLabel();
	}
}