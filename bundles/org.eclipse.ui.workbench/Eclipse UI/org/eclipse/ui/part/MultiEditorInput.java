package org.eclipse.ui.part;

/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.*;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Implements a input for a MultiEditor
 * 
 * This class is intended to be instanciated by clients but its is 
 * not intented to be subclassed.
 */
public class MultiEditorInput implements IEditorInput {

	IEditorInput input[];
	String editors[];

	/**
	 * Constructor for MultiEditorInput.
	 */
	public MultiEditorInput(String[] editorIDs, IEditorInput[] innerEditors) {
		super();
		editors = editorIDs;
		input = innerEditors;
	}
	/**
	 * Returns an array with the input of all inner editors.
	 */
	public IEditorInput[] getInput() {
		return input;
	}
	/**
	 * Retunrs an array with the id of all inner editors.
	 */
	public String[] getEditors() {
		return editors;
	}
	/*
	 * @see IEditorInput#exists()
	 */
	public boolean exists() {
		return true;
	}
	/*
	 * @see IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return null;
	}
	/*
	 * @see IEditorInput#getName()
	 */
	public String getName() {
		String name = ""; //$NON-NLS-1$
		for (int i = 0; i < (input.length - 1); i++) {
			name = name + input[i].getName() + "/"; //$NON-NLS-1$
		}
		name = name + input[input.length - 1].getName();
		return name;
	}
	/*
	 * @see IEditorInput#getPersistable()
	 */
	public IPersistableElement getPersistable() {
		return null;
	}
	/*
	 * @see IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		return getName();
	}
	/*
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}
}