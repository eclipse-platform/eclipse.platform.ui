/*
 * Copyright (c) 2002, Roscoe Rush. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public License
 * Version 0.5 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.eclipse.org/
 *
 */
package org.eclipse.ui.externaltools.internal.ant.antview.views;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import org.eclipse.ui.externaltools.internal.ant.antview.core.IAntViewConstants;
import org.eclipse.ui.externaltools.internal.ant.antview.core.ResourceMgr;
import org.eclipse.ui.externaltools.internal.ant.antview.tree.TreeNode;

public class AntViewLabelProvider extends LabelProvider implements  ILabelDecorator, IAntViewConstants {
	/**
	 * Method getText.
	 * @param obj
	 * @return String
	 */
	public String getText(Object obj) {		
		try { 
		   return ((TreeNode) obj).getText();
		} catch (ClassCastException e) {
		   return ResourceMgr.getString("Tree.Unknown");
		}	
	}
	/**
	 * Method getImage.
	 * @param obj
	 * @return Image
	 */
	public Image getImage(Object obj) {
		try {
			return ((TreeNode) obj).getImage();
		} catch (ClassCastException e) {
			return ResourceMgr.getImage(IMAGE_DEFAULT);
		} 
	}
	/**
	 * Method decorateText.
	 * @param text
	 * @param obj
	 * @return String
	 */
	public String decorateText(String text, Object obj) {
		try { 
			return ((TreeNode) obj).decorateText(text);
		} catch (ClassCastException e) {
			return text;
		}
	}
	/**
	 * Method decorateImage.
	 * @param image
	 * @param obj
	 * @return Image
	 */
	public Image decorateImage(Image image, Object obj) {
		try { 
			return ((TreeNode) obj).decorateImage(image);
		} catch (ClassCastException e) {
			return image;
		}
	}
}