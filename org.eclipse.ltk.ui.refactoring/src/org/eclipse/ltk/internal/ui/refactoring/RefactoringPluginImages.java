/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

public class RefactoringPluginImages {

	private static final String NAME_PREFIX= "org.eclipse.ltk.ui.refactoring"; //$NON-NLS-1$
	private static final int    NAME_PREFIX_LENGTH= NAME_PREFIX.length();

	private static URL fgIconBaseURL= null;
	
	static {
		fgIconBaseURL= RefactoringUIPlugin.getDefault().getBundle().getEntry("/icons/full/"); //$NON-NLS-1$
	}
	
	
 	private static ImageRegistry fgImageRegistry= null;
 	private static HashMap fgAvoidSWTErrorMap= null;

	private static final String T_WIZBAN= "wizban"; 	//$NON-NLS-1$
	private static final String T_OBJ= "obj16"; 		//$NON-NLS-1$

	public static final ImageDescriptor DESC_WIZBAN_REFACTOR= create(T_WIZBAN, "refactor_wiz.gif"); 			//$NON-NLS-1$

	
	public static final String IMG_OBJS_REFACTORING_FATAL= NAME_PREFIX + "fatalerror_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_REFACTORING_ERROR= NAME_PREFIX + "error_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_REFACTORING_WARNING= NAME_PREFIX + "warning_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_REFACTORING_INFO= NAME_PREFIX + "info_obj.gif"; 	//$NON-NLS-1$

	public static final ImageDescriptor DESC_OBJS_REFACTORING_FATAL= createManaged(T_OBJ, IMG_OBJS_REFACTORING_FATAL);
	public static final ImageDescriptor DESC_OBJS_REFACTORING_ERROR= createManaged(T_OBJ, IMG_OBJS_REFACTORING_ERROR);
	public static final ImageDescriptor DESC_OBJS_REFACTORING_WARNING= createManaged(T_OBJ, IMG_OBJS_REFACTORING_WARNING);
	public static final ImageDescriptor DESC_OBJS_REFACTORING_INFO= createManaged(T_OBJ, IMG_OBJS_REFACTORING_INFO);
	
	
	public static final ImageDescriptor DESC_OBJS_DEFAULT_CHANGE= create(T_OBJ, "change.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_OBJS_COMPOSITE_CHANGE= create(T_OBJ, "composite_change.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_OBJS_CU_CHANGE= create(T_OBJ, "cu_change.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_OBJS_FILE_CHANGE= create(T_OBJ, "file_change.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_OBJS_TEXT_EDIT= create(T_OBJ, "text_edit.gif"); //$NON-NLS-1$

	
	
	/**
	 * Returns the image managed under the given key in this registry.
	 * 
	 * @param key the image's key
	 * @return the image managed under the given key
	 */ 
	public static Image get(String key) {
		return getImageRegistry().get(key);
	}
	
	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to an action. The actions
	 * are retrieved from the *tool16 folders.
	 * 
	 * @param action the action to set the icons to
	 * @param iconName the iconName
	 */
	public static void setToolImageDescriptors(IAction action, String iconName) {
		setImageDescriptors(action, "tool16", iconName); //$NON-NLS-1$
	}
	
	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to an action. The actions
	 * are retrieved from the *lcl16 folders.
	 * 
	 * @param action the action to set the icons to
	 * @param iconName the iconName
	 */
	public static void setLocalImageDescriptors(IAction action, String iconName) {
		setImageDescriptors(action, "lcl16", iconName); //$NON-NLS-1$
	}
	
	/*
	 * Helper method to access the image registry from the JavaPlugin class.
	 */
	/* package */ static ImageRegistry getImageRegistry() {
		if (fgImageRegistry == null) {
			fgImageRegistry= new ImageRegistry();
			for (Iterator iter= fgAvoidSWTErrorMap.keySet().iterator(); iter.hasNext();) {
				String key= (String) iter.next();
				fgImageRegistry.put(key, (ImageDescriptor) fgAvoidSWTErrorMap.get(key));
			}
			fgAvoidSWTErrorMap= null;
		}
		return fgImageRegistry;
	}

	//---- Helper methods to access icons on the file system --------------------------------------

	private static void setImageDescriptors(IAction action, String type, String relPath) {
		
		try {
			ImageDescriptor id= ImageDescriptor.createFromURL(makeIconFileURL("d" + type, relPath)); //$NON-NLS-1$
			if (id != null)
				action.setDisabledImageDescriptor(id);
		} catch (MalformedURLException e) {
		}
	
		ImageDescriptor descriptor= create("e" + type, relPath); //$NON-NLS-1$
		action.setHoverImageDescriptor(descriptor);
		action.setImageDescriptor(descriptor); 
	}
	
	private static ImageDescriptor createManaged(String prefix, String name) {
		try {
			ImageDescriptor result= ImageDescriptor.createFromURL(makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));
			if (fgAvoidSWTErrorMap == null) {
				fgAvoidSWTErrorMap= new HashMap();
			}
			fgAvoidSWTErrorMap.put(name, result);
			if (fgImageRegistry != null) {
				RefactoringUIPlugin.logErrorMessage("Image registry already defined"); //$NON-NLS-1$
			}
			return result;
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}
	
	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}
	
	private static URL makeIconFileURL(String prefix, String name) throws MalformedURLException {
		if (fgIconBaseURL == null)
			throw new MalformedURLException();
			
		StringBuffer buffer= new StringBuffer(prefix);
		buffer.append('/');
		buffer.append(name);
		return new URL(fgIconBaseURL, buffer.toString());
	}	
}
