/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.application.IWorkbenchPreferences;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Sets the current perspective of the workbench
 * page.
 */
public class SetPagePerspectiveAction extends Action {
	
	private IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault().getPreferenceStore();	
	
	private WorkbenchPage page;
	private IPerspectiveDescriptor persp;
	
	private class ResizedDescriptor extends ImageDescriptor{
		
		ImageData cachedData;
		
		ResizedDescriptor(ImageData data){
			super();
			cachedData = data;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.jface.resource.ImageDescriptor#getImageData()
		 */
		public ImageData getImageData() {
			return cachedData;
		}
	}
	
	/**
	 *	Create an instance of this class
	 */
	public SetPagePerspectiveAction(IPerspectiveDescriptor perspective, WorkbenchPage workbenchPage) {
		super(WorkbenchMessages.getString("SetPagePerspectiveAction.text")); //$NON-NLS-1$
		setChecked(false);
		persp = perspective;
		page = workbenchPage;
		update(persp);
		WorkbenchHelp.setHelp(this, IHelpContextIds.SWITCH_TO_PERSPECTIVE_ACTION);
	}

	/**
	 * Returns the page this action applies to
	 */
	/* package */ WorkbenchPage getPage() {
		return page;
	}
	
	/**
	 * Returns the perspective this action applies to
	 */
	/* package */ IPerspectiveDescriptor getPerspective() {
		return persp;
	}
	
	/**
	 * Returns whether this action handles the specified
	 * workbench page and perspective.
	 */
	public boolean handles(IPerspectiveDescriptor perspective, WorkbenchPage workbenchPage) {
		return persp == perspective && page == workbenchPage;
	}
	
	/**
	 * Replaces the perspective used
	 */
	public void setPerspective(IPerspectiveDescriptor newPerspective) {
		persp = newPerspective;
	}
	
	/**
	 * The user has invoked this action
	 */
	public void run() {
		page.setPerspective(persp);
		// Force the button into proper checked state
		setChecked(page.getPerspective() == persp);
	}
	
	/**
	 *	Update the action.
	 */
	public void update(IPerspectiveDescriptor newDesc) {
		persp = newDesc;
		setToolTipText(WorkbenchMessages.format("SetPagePerspectiveAction.toolTip", new Object[] {persp.getLabel()})); //$NON-NLS-1$
		ImageDescriptor image = persp.getImageDescriptor();
		ImageDescriptor mainImage = adjustMainImage(persp.getImageDescriptor());
		if (image != null) {
			setImageDescriptor(mainImage);
			setHoverImageDescriptor(null);
		} else {
			setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_DEF_PERSPECTIVE));
			setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_DEF_PERSPECTIVE_HOVER));
		}
		setText(persp.getLabel());
	}
	
	/**
	 * Return whether or not this actions shows text in the toolbar.
	 * @return
	 */
	public boolean showTextInToolBar(){
		return preferenceStore.getBoolean(IWorkbenchPreferences.SHOW_TEXT_ON_PERSPECTIVE_BAR);
	}
	
	/**
	 * Get the supplied image adjusted for the perspective bar.
	 * @param start
	 * @return
	 */
	private ImageDescriptor adjustMainImage(ImageDescriptor start){
		
		return start;
//		Device device = PlatformUI.getWorkbench().getDisplay();
//		Image sizeImage = start.createImage(device);
//		
//		if(sizeImage == null)
//			return start;
//		
//		ImageData data = sizeImage.getImageData().scaledTo(32,32);
//		
//		ImageDescriptor result = new ResizedDescriptor(data);
//		sizeImage.dispose();
//		return result;
		
	}
	
}
