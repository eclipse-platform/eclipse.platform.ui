package org.eclipse.team.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * The interface that users of the extension point 
 * org.eclipse.team.ui.decorators must implement.
 * 
 * @see ILabelProvider
 */
public interface ITeamDecorator {
	
	/**
	 * Provider returns the annotated (e.g. including a postfix or
	 * prefix) text to be displayed to the user. The decorator does
	 * not know what type of view the resource is being displayed in.
	 * For example, the provider could decorate text such as:
	 *    MyJavaFile.java [1.1]
	 * 	  Main.java [1.2.1.1]
	 * 
	 * @param text to be decorated.
	 * @param element the element whose image is being decorated
	 * @return the decorated text label, or <code>null</code> if no 
	 * decoration is to be applied
	 */
	public String getText(String text, IResource resource);
	
	/**
	 * Returns images that are based on the given image,
	 * but decorated with additional information relating to the state
 	 * of the provided element.
 	 * 
 	 * @param element the element whose image is being decorated
 	 * @return images to overlay, or <code>null</code> if no decoration
 	 * is to be applied.
	 */
	public ImageDescriptor[][] getImage(IResource resource);
}

