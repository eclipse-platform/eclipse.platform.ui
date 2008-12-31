/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import org.osgi.framework.Bundle;

import org.eclipse.swt.graphics.Image;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

public class RefactoringPluginImages {

	private static final String NAME_PREFIX= "org.eclipse.ltk.ui.refactoring"; //$NON-NLS-1$
	private static final int    NAME_PREFIX_LENGTH= NAME_PREFIX.length();

	private static final IPath ICONS_PATH= new Path("$nl$/icons/full"); //$NON-NLS-1$

 	private static ImageRegistry fgImageRegistry= null;
 	private static HashMap fgAvoidSWTErrorMap= null;

	private static final String T_WIZBAN= "wizban"; 	//$NON-NLS-1$
	private static final String T_OBJ= "obj16"; 		//$NON-NLS-1$
	private static final String T_OVR= "ovr16"; 		//$NON-NLS-1$
	private static final String T_ELCL= "elcl16"; 	//$NON-NLS-1$
	private static final String T_DLCL= "dlcl16"; 	//$NON-NLS-1$

	public static final ImageDescriptor DESC_WIZBAN_REFACTOR= createUnManaged(T_WIZBAN, "refactor_wiz.png"); 			//$NON-NLS-1$

	/** @since 3.2 */
	public static final ImageDescriptor DESC_WIZBAN_SHOW_HISTORY= createUnManaged(T_WIZBAN, "show_history_wiz.png"); 			//$NON-NLS-1$
	/** @since 3.2 */
	public static final ImageDescriptor DESC_WIZBAN_APPLY_SCRIPT= createUnManaged(T_WIZBAN, "apply_rescript_wiz.png"); 			//$NON-NLS-1$
	/** @since 3.2 */
	public static final ImageDescriptor DESC_WIZBAN_CREATE_SCRIPT= createUnManaged(T_WIZBAN, "create_rescript_wiz.png"); 			//$NON-NLS-1$

	public static final String IMG_OBJS_REFACTORING_FATAL= NAME_PREFIX + "fatalerror_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_REFACTORING_ERROR= NAME_PREFIX + "error_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_REFACTORING_WARNING= NAME_PREFIX + "warning_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_REFACTORING_INFO= NAME_PREFIX + "info_obj.gif"; 	//$NON-NLS-1$

	/** @since 3.2 */
	public static final String IMG_OBJS_REFACTORING_DATE= NAME_PREFIX + "date_obj.gif"; //$NON-NLS-1$
	/** @since 3.2 */
	public static final String IMG_OBJS_REFACTORING_TIME= NAME_PREFIX + "time_obj.gif"; //$NON-NLS-1$

	public static final ImageDescriptor DESC_ELCL_FILTER= createUnManaged(T_ELCL, "filter_ps.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_DLCL_FILTER= createUnManaged(T_DLCL, "filter_ps.gif"); //$NON-NLS-1$

	/** @since 3.2 */
	public static final ImageDescriptor DESC_ELCL_SORT_PROJECT= createUnManaged(T_ELCL, "prj_mode.gif"); //$NON-NLS-1$
	/** @since 3.2 */
	public static final ImageDescriptor DESC_DLCL_SORT_PROJECT= createUnManaged(T_DLCL, "prj_mode.gif"); //$NON-NLS-1$
	/** @since 3.2 */
	public static final ImageDescriptor DESC_ELCL_SORT_DATE= createUnManaged(T_ELCL, "date_mode.gif"); //$NON-NLS-1$
	/** @since 3.2 */
	public static final ImageDescriptor DESC_DLCL_SORT_DATE= createUnManaged(T_DLCL, "date_mode.gif"); //$NON-NLS-1$

	public static final ImageDescriptor DESC_OBJS_REFACTORING_FATAL= createManaged(T_OBJ, IMG_OBJS_REFACTORING_FATAL);
	public static final ImageDescriptor DESC_OBJS_REFACTORING_ERROR= createManaged(T_OBJ, IMG_OBJS_REFACTORING_ERROR);
	public static final ImageDescriptor DESC_OBJS_REFACTORING_WARNING= createManaged(T_OBJ, IMG_OBJS_REFACTORING_WARNING);
	public static final ImageDescriptor DESC_OBJS_REFACTORING_INFO= createManaged(T_OBJ, IMG_OBJS_REFACTORING_INFO);

	/** @since 3.2 */
	public static final ImageDescriptor DESC_OBJS_REFACTORING_DATE= createUnManaged(T_OBJ, "date_obj.gif"); //$NON-NLS-1$
	/** @since 3.2 */
	public static final ImageDescriptor DESC_OBJS_REFACTORING_TIME= createUnManaged(T_OBJ, "time_obj.gif"); //$NON-NLS-1$
	/** @since 3.2 */
	public static final ImageDescriptor DESC_OBJS_REFACTORING= createUnManaged(T_OBJ, "refactoring_obj.gif"); //$NON-NLS-1$
	/** @since 3.2 */
	public static final ImageDescriptor DESC_OBJS_REFACTORING_COLL= createUnManaged(T_OBJ, "refactorings_obj.gif"); //$NON-NLS-1$
	/** @since 3.2 */
	public static final ImageDescriptor DESC_OVR_WORKSPACE= createUnManaged(T_OVR, "workspace_ovr.gif"); //$NON-NLS-1$

	public static final ImageDescriptor DESC_OBJS_DEFAULT_CHANGE= createUnManaged(T_OBJ, "change.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_OBJS_COMPOSITE_CHANGE= createUnManaged(T_OBJ, "composite_change.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_OBJS_FILE_CHANGE= createUnManaged(T_OBJ, "file_change.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_OBJS_TEXT_EDIT= createUnManaged(T_OBJ, "text_edit.gif"); //$NON-NLS-1$

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
	 * Helper method to access the image registry from the Refactoring Plugin class.
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
		ImageDescriptor id= create("d" + type, relPath, false); //$NON-NLS-1$
		if (id != null)
			action.setDisabledImageDescriptor(id);

		ImageDescriptor descriptor= create("e" + type, relPath, true); //$NON-NLS-1$
		action.setHoverImageDescriptor(descriptor);
		action.setImageDescriptor(descriptor);
	}

	private static ImageDescriptor createManaged(String prefix, String name) {
		ImageDescriptor result= create(prefix, name.substring(NAME_PREFIX_LENGTH), true);
		if (fgAvoidSWTErrorMap == null) {
			fgAvoidSWTErrorMap= new HashMap();
		}
		fgAvoidSWTErrorMap.put(name, result);
		if (fgImageRegistry != null) {
			RefactoringUIPlugin.logErrorMessage("Image registry already defined"); //$NON-NLS-1$
		}
		return result;
	}

	private static ImageDescriptor createUnManaged(String prefix, String name) {
		return create(prefix, name, true);
	}

	/*
	 * Creates an image descriptor for the given prefix and name in the Refactoring UI bundle. The path can
	 * contain variables like $NL$.
	 * If no image could be found, <code>useMissingImageDescriptor</code> decides if either
	 * the 'missing image descriptor' is returned or <code>null</code>.
	 * or <code>null</code>.
	 */
	private static ImageDescriptor create(String prefix, String name, boolean useMissingImageDescriptor) {
		IPath path= ICONS_PATH.append(prefix).append(name);
		return createImageDescriptor(RefactoringUIPlugin.getDefault().getBundle(), path, useMissingImageDescriptor);
	}

	/*
	 * Since 3.1.1. Load from icon paths with $NL$
	 */
	private static ImageDescriptor createImageDescriptor(Bundle bundle, IPath path, boolean useMissingImageDescriptor) {
		URL url= FileLocator.find(bundle, path, null);
		if (url != null) {
			return ImageDescriptor.createFromURL(url);
		}
		if (useMissingImageDescriptor) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
		return null;
	}
}
