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
package org.eclipse.ui.editors.text;

import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;
import org.eclipse.ui.texteditor.AnnotationTypeHierarchy;
import org.eclipse.ui.texteditor.AnnotationTypeLookup;

import org.eclipse.ui.internal.editors.text.EditorsPlugin;

/**
 * The central class for access to the Search Plug-in's User Interface. 
 * This class cannot be instantiated; all functionality is provided by 
 * static methods.
 * <p>
 * TODO this API class is provisional and might still change for 3.0.
 * </p>
 * 
 * @since 3.0
 */
public final class EditorsUI {
	
	/**
	 * TextEditor Plug-in ID (value <code>{@value}</code>).
	 */
	public static final String PLUGIN_ID= "org.eclipse.ui.editors"; //$NON-NLS-1$
	
	/**
	 * The ID of the default text editor.
	 * 
	 * @since 3.0
	 */
	public static final String DEFAULT_TEXT_EDITOR_ID = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$


	/**
	 * Returns the annotation type lookup of this plug-in.
	 * 
	 * @return the annotation type lookup
	 * @since 3.0
	 */
	public static AnnotationTypeLookup getAnnotationTypeLookup() {
		return EditorsPlugin.getDefault().getAnnotationTypeLookup();
	}
	
	/**
	 * Returns the annotation preference lookup of this plug-in.
	 * 
	 * @return the annotation preference lookup
	 * @since 3.0
	 */
	public static AnnotationPreferenceLookup getAnnotationPreferenceLookup() {
		return EditorsPlugin.getDefault().getAnnotationPreferenceLookup();
	}
	
	/**
	 * Returns the annotation type hierarchy for this plug-in.
	 * 
	 * @return the annotation type hierarchy
	 * @since 3.0
	 */
	public static AnnotationTypeHierarchy getAnnotationTypeHierarchy() {
		return EditorsPlugin.getDefault().getAnnotationTypeHierarchy();
	}

	private EditorsUI() {
		// block instantiation
	}
}
