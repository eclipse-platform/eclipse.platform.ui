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
package org.eclipse.ui.internal.registry;

import java.io.File;
import java.io.Serializable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.ProgramImageDescriptor;

/**
 * @see IEditorDescriptor
 */
public final class EditorDescriptor implements IEditorDescriptor, Serializable {
	private static final String ATT_EDITOR_CONTRIBUTOR = "contributorClass"; //$NON-NLS-1$
	/* package */ static final int OPEN_INTERNAL = 0x01;
	/* package */ static final int OPEN_INPLACE = 0x02;
	/* package */ static final int OPEN_EXTERNAL = 0x04;

	private String editorName;
	private String imageFilename;
	private transient ImageDescriptor imageDesc;
	private boolean testImage = true;
	private String className;
	private String launcherName;
	private String fileName;
	private String id;
	//Work in progress for OSEditors
	private Program program;

	//The id of the plugin which contributed this editor, null for external editors
	private String pluginIdentifier;
	private int openMode = 0;	
	private transient IConfigurationElement configurationElement;

	/**
	 * Create a new instance of an editor descriptor. Limited
	 * to internal framework calls.
	 */
	/* package */ EditorDescriptor() {
		super();
	}
	
	/**
	 * Creates a descriptor for an external program.
	 * 
	 * @param filename the external editor full path and filename
	 * @return the editor descriptor
	 */
	public static EditorDescriptor createForProgram(String filename) {
		if (filename == null) {
			throw new IllegalArgumentException();
		}
		EditorDescriptor editor = new EditorDescriptor();
		
		editor.setFileName(filename);
		editor.setID(filename);
		editor.setOpenMode(OPEN_EXTERNAL);
		
		//Isolate the program name (no directory or extension)
		int start = filename.lastIndexOf(File.separator);
		String name;
		if (start != -1) {
			name = filename.substring(start + 1);
		} else {
			name = filename;
		}
		int end = name.lastIndexOf('.');
		if (end != -1) {
			name = name.substring(0, end);
		}
		editor.setName(name);
		
		// get the program icon without storing it in the registry
		ImageDescriptor imageDescriptor = new ProgramImageDescriptor(filename, 0);
		editor.setImageDescriptor(imageDescriptor);
		
		return editor;
	}
	/**
	 * Return the program called programName. Return null if it is not found.
	 * @return org.eclipse.swt.program.Program
	 */
	private static Program findProgram(String programName) {

		Program[] programs = Program.getPrograms();
		for (int i = 0; i < programs.length; i++) {
			if (programs[i].getName().equals(programName))
				return programs[i];
		}

		return null;
	}
	/**
	 * Creates the action contributor for this editor.
	 */
	public IEditorActionBarContributor createActionBarContributor() {
		// Get the contributor class name.
		String className =
			configurationElement.getAttribute(ATT_EDITOR_CONTRIBUTOR);
		if (className == null)
			return null;

		// Create the contributor object.
		IEditorActionBarContributor contributor = null;
		try {
			contributor =
				(IEditorActionBarContributor) WorkbenchPlugin.createExtension(
					configurationElement,
					ATT_EDITOR_CONTRIBUTOR);
		} catch (CoreException e) {
			WorkbenchPlugin.log("Unable to create editor contributor: " + //$NON-NLS-1$
			id, e.getStatus());
		}
		return contributor;
	}
	/**
	 * @see IResourceEditorDescriptor
	 */
	public String getClassName() {
		return className;
	}
	/**
	 * @see IResourceEditorDescriptor
	 */
	public IConfigurationElement getConfigurationElement() {
		return configurationElement;
	}
	/**
	 * @see IResourceEditorDescriptor
	 */
	public String getFileName() {
		if (program == null)
			return fileName;
		return program.getName();
	}
	/**
	 * @see IResourceEditorDescriptor
	 */
	public String getId() {
		if (program == null)
			return id;
		return program.getName();
	}
	/**
	 * @see IResourceEditorDescriptor
	 */
	public ImageDescriptor getImageDescriptor() {
		if (testImage) {
			testImage = false;
			if (imageDesc == null) {
				// @issue what should be the default image?
				imageDesc =
					WorkbenchImages.getImageDescriptor(
						ISharedImages.IMG_OBJ_FILE);
			} else {
				Image img = imageDesc.createImage(false);
				if (img == null) {
					// @issue what should be the default image?
					imageDesc =
						WorkbenchImages.getImageDescriptor(
							ISharedImages.IMG_OBJ_FILE);
				} else {
					img.dispose();
				}
			}
		}
		return imageDesc;
	}
	/**
	 * @see IResourceEditorDescriptor
	 */
	public String getImageFilename() {
		return imageFilename;
	}
	/**
	 * @see IResourceEditorDescriptor
	 */
	public String getLabel() {
		if (program == null)
			return editorName;
		return program.getName();
	}
	/**
	 * Returns the class name of the launcher.
	 */
	public String getLauncher() {
		return launcherName;
	}
	/**
	 * @see IResourceEditorDescriptor
	 */
	public String getPluginID() {
		return pluginIdentifier;
	}
	/**
	 * Get the program for the receiver if there is one.
	 * @return Program
	 */
	public Program getProgram() {
		return this.program;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorDescriptor#isOpenInternal()
	 */
	public boolean isOpenInternal() {
		return openMode == OPEN_INTERNAL;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorDescriptor#isOpenInPlace()
	 */
	public boolean isOpenInPlace() {
		return openMode == OPEN_INPLACE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorDescriptor#isOpenExternal()
	 */
	public boolean isOpenExternal() {
		return openMode == OPEN_EXTERNAL;
	}

	/**
	 * Load the object properties from a IMemento.
	 */
	protected void loadValues(IMemento memento) {
		editorName = memento.getString(IWorkbenchConstants.TAG_LABEL);
		imageFilename = memento.getString(IWorkbenchConstants.TAG_IMAGE);
		className = memento.getString(IWorkbenchConstants.TAG_CLASS);
		launcherName = memento.getString(IWorkbenchConstants.TAG_LAUNCHER);
		fileName = memento.getString(IWorkbenchConstants.TAG_FILE);
		id = memento.getString(IWorkbenchConstants.TAG_ID);
		pluginIdentifier = memento.getString(IWorkbenchConstants.TAG_PLUGING);
		openMode = memento.getInteger(IWorkbenchConstants.TAG_OPEN_MODE).intValue();

		String programName =	memento.getString(IWorkbenchConstants.TAG_PROGRAM_NAME);
		if (programName != null) {
			this.program = findProgram(programName);
		}
	}
	
	/**
	 * Save the object values in a IMemento
	 */
	protected void saveValues(IMemento memento) {
		memento.putString(IWorkbenchConstants.TAG_LABEL, editorName);
		memento.putString(IWorkbenchConstants.TAG_IMAGE, imageFilename);
		memento.putString(IWorkbenchConstants.TAG_CLASS, className);
		memento.putString(IWorkbenchConstants.TAG_LAUNCHER, launcherName);
		memento.putString(IWorkbenchConstants.TAG_FILE, fileName);
		memento.putString(IWorkbenchConstants.TAG_ID, id);
		memento.putString(IWorkbenchConstants.TAG_PLUGING, pluginIdentifier);
		memento.putInteger(IWorkbenchConstants.TAG_OPEN_MODE, openMode);
		if (this.program != null)
			memento.putString(
				IWorkbenchConstants.TAG_PROGRAM_NAME,
				this.program.getName());
	}
	/**
	 * Set the class name of an internal editor.
	 */
	/* package */ void setClassName(String newClassName) {
		className = newClassName;
	}
	/**
	 * Set the configuration element which contributed this editor.
	 */
	/* package */ void setConfigurationElement(IConfigurationElement newConfigurationElement) {
		configurationElement = newConfigurationElement;
	}
	/**
	 * Set the filename of an external editor.
	 */
	/* package */ void setFileName(String aFileName) {
		fileName = aFileName;
	}
	/**
	 * Set the id of the editor.
	 * For internal editors this is the id as provided in the extension point
	 * For external editors it is path and filename of the editor
	 */
	/* package */ void setID(String anID) {
		id = anID;
	}
	/**
	 * The Image to use to repesent this editor
	 */
	/* package */ void setImageDescriptor(ImageDescriptor desc) {
		imageDesc = desc;
		testImage = true;
	}
	/**
	 * The name of the image to use for this editor.
	 */
	/* package */ void setImageFilename(String aFileName) {
		imageFilename = aFileName;
	}
	/**
	 * Sets the new launcher class name
	 *
	 * @param newLauncher the new launcher
	 */
	/* package */ void setLauncher(String newLauncher) {
		launcherName = newLauncher;
	}
	/**
	 * The label to show for this editor.
	 */
	/* package */ void setName(String newName) {
		editorName = newName;
	}
	/**
	 * Set the open mode of this editor descriptor.
	 * @param anID
	 */
	/* package */ void setOpenMode(int mode) {
		openMode = mode;
	}
	/**
	 * The id of the plugin which contributed this editor, null for external editors.
	 */
	/* package */ void setPluginIdentifier(String anID) {
		pluginIdentifier = anID;
	}
	/**
	 * Set the receivers program.
	 * @param newProgram
	 */
	/* package */ void setProgram(Program newProgram) {

		this.program = newProgram;
		if (editorName == null)
			setName(newProgram.getName());
	}
	/**
	 * For debugging purposes only.
	 */
	public String toString() {
		return "EditorDescriptor(" + editorName + ")"; //$NON-NLS-2$//$NON-NLS-1$
	}
}
