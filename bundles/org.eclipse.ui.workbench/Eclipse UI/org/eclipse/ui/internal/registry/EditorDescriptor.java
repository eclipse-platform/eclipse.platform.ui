package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.Serializable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;

/**
 * @see IEditorDescriptor
 */
public class EditorDescriptor implements IEditorDescriptor, Serializable {
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

	private String pluginIdentifier;
	//The id of the plugin which contributed this editor, null for external editors
	private boolean internal = false;
	private boolean openInPlace = false;
	private transient IConfigurationElement configurationElement;
	private static String ATT_EDITOR_CONTRIBUTOR = "contributorClass"; //$NON-NLS-1$

	// Single descriptor instance to represent the system editor
	private static EditorDescriptor systemEditorDescriptor;
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
			if (imageDesc != null) {
				Image img = imageDesc.createImage(false);
				if (img == null)
					imageDesc =
						WorkbenchImages.getImageDescriptor(
							ISharedImages.IMG_OBJ_FILE);
				else
					img.dispose();
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
	/**
	 * Return the single descriptor instance of a system editor
	 */
	public static EditorDescriptor getSystemEditorDescriptor() {
		if (systemEditorDescriptor == null) {
			systemEditorDescriptor = new EditorDescriptor();
			systemEditorDescriptor.setID(IWorkbenchConstants.SYSTEM_EDITOR_ID);
			systemEditorDescriptor.setName(WorkbenchMessages.getString("SystemEditorDescription.name")); //$NON-NLS-1$
		}
		return systemEditorDescriptor;
	}
	/**
	 * @see IResourceEditorDescriptor
	 */
	public boolean isInternal() {
		return internal;
	}
	/**
	 * @see IResourceEditorDescriptor
	 */
	public boolean isOpenInPlace() {
		return openInPlace;
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
		internal =
			new Boolean(memento.getString(IWorkbenchConstants.TAG_INTERNAL))
				.booleanValue();
		openInPlace =
			new Boolean(
				memento.getString(IWorkbenchConstants.TAG_OPEN_IN_PLACE))
				.booleanValue();

		String programName =
			memento.getString(IWorkbenchConstants.TAG_PROGRAM_NAME);
		if (programName != null)
			this.program = findProgram(programName);

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
		memento.putString(
			IWorkbenchConstants.TAG_INTERNAL,
			String.valueOf(internal));
		memento.putString(
			IWorkbenchConstants.TAG_OPEN_IN_PLACE,
			String.valueOf(openInPlace));
		if (this.program != null)
			memento.putString(
				IWorkbenchConstants.TAG_PROGRAM_NAME,
				this.program.getName());
	}
	/**
	 * Set the class name of an internal editor.
	 */
	public void setClassName(String newClassName) {
		className = newClassName;
	}
	/**
	 * Set the configuration element which contributed this editor.
	 */
	public void setConfigurationElement(IConfigurationElement newConfigurationElement) {
		configurationElement = newConfigurationElement;
	}
	/**
	 * Set the filename of an external editor.
	 */
	public void setFileName(String aFileName) {
		fileName = aFileName;
	}
	/**
	 * Set the id of the editor.
	 * For internal editors this is the id as provided in the extension point
	 * For external editors it is path and filename of the editor
	 */
	public void setID(String anID) {
		id = anID;
	}
	/**
	 * The Image to use to repesent this editor
	 */
	public void setImageDescriptor(ImageDescriptor desc) {
		imageDesc = desc;
		testImage = true;
	}
	/**
	 * The name of the image to use for this editor.
	 */
	public void setImageFilename(String aFileName) {
		imageFilename = aFileName;
	}
	/**
	 * True if this editor is an interal editor.
	 */
	public void setInternal(boolean newInternal) {
		internal = newInternal;
	}
	/**
	 * Sets the new launcher class name
	 *
	 * @param newLauncher the new launcher
	 */
	public void setLauncher(String newLauncher) {
		launcherName = newLauncher;
	}
	/**
	 * The label to show for this editor.
	 */
	public void setName(String newName) {
		editorName = newName;
	}
	/**
	 * Set if this external editor should be opened inplace.
	 */
	public void setOpenInPlace(boolean aBoolean) {
		openInPlace = aBoolean;
	}
	/**
	 * The id of the plugin which contributed this editor, null for external editors.
	 */
	public void setPluginIdentifier(String anID) {
		pluginIdentifier = anID;
	}
	/**
	 * Set the receivers program.
	 * @param newProgram
	 */
	public void setProgram(Program newProgram) {

		this.program = newProgram;
		if (editorName == null)
			setName(newProgram.getName());
	}
	/**
	 * For debugging purposes only.
	 */
	public String toString() {
		return "ResourceEditorDescriptor(" + editorName + ")"; //$NON-NLS-2$//$NON-NLS-1$
	}
}
