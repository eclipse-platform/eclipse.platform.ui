package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.internal.ui.DebugActionGroupsManager.DebugActionGroup;
import org.eclipse.debug.internal.ui.DebugActionGroupsManager.DebugActionGroupAction;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class DebugActionGroupsLabelProvider extends LabelProvider {

	private String UNKNOWN = "<Unknown>";

	public DebugActionGroupsLabelProvider() {
		super();
	}
	
	/**
	 * @see ILabelProvider#getText(Object)
	 */
	public String getText(Object element) {
		String label = UNKNOWN;
		if (element instanceof DebugActionGroup) {
			label = ((DebugActionGroup) element).getName();
		} else if (element instanceof DebugActionGroupAction) {
			label = ((DebugActionGroupAction) element).getName();
		} else if (element instanceof String) {
			label= (String)element;
		}
		return label;
	}
	
	/**
	 * @see ILabelProvider#getImage(Object)
	 */
	public Image getImage(Object element) {
		Image image= null;
		if (element instanceof DebugActionGroupAction) {
			image = ((DebugActionGroupAction) element).getImage();
		}
		return image;
	}
}