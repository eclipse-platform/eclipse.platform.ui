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
package org.eclipse.ui.internal.registry;

import java.io.File;
import java.io.Serializable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.ProgramImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @see IEditorDescriptor
 */
public final class EditorDescriptor implements IEditorDescriptor, Serializable,
        IPluginContribution {

    /**
     * Generated serial version UID for this class.
     * @since 3.1
     */
    private static final long serialVersionUID = 3905241225668998961L;

    private static final String ATT_EDITOR_CONTRIBUTOR = "contributorClass"; //$NON-NLS-1$

    // @issue the following constants need not be public; see bug 47600
    public static final int OPEN_INTERNAL = 0x01;

    public static final int OPEN_INPLACE = 0x02;

    public static final int OPEN_EXTERNAL = 0x04;

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

	public static final String ATT_CLASS = "class";//$NON-NLS-1$

	public static final String ATT_NAME = "name";//$NON-NLS-1$

	public static final String ATT_COMMAND = "command";//$NON-NLS-1$

	public static final String ATT_LAUNCHER = "launcher";//$NON-NLS-1$

	public static final String ATT_DEFAULT = "default";//$NON-NLS-1$

	public static final String ATT_ID = "id";//$NON-NLS-1$

	public static final String ATT_ICON = "icon";//$NON-NLS-1$

	public static final String ATT_EXTENSIONS = "extensions";//$NON-NLS-1$

	public static final String ATT_FILENAMES = "filenames";//$NON-NLS-1$

    /**
     * Create a new instance of an editor descriptor. Limited
     * to internal framework calls.
     * @param element
     * @param id2
     */
    /* package */EditorDescriptor(String id2, IConfigurationElement element) {
        setID(id2);
        setConfigurationElement(element);
    }

    

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
        ImageDescriptor imageDescriptor = new ProgramImageDescriptor(filename,
                0);
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
        // Handle case for predefined editor descriptors, like the
        // one for IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID, which
        // don't have a configuration element.
        if (configurationElement == null) {
            return null;
        }

        // Get the contributor class name.
        String className = configurationElement
                .getAttribute(ATT_EDITOR_CONTRIBUTOR);
        if (className == null)
            return null;

        // Create the contributor object.
        IEditorActionBarContributor contributor = null;
        try {
            contributor = (IEditorActionBarContributor) WorkbenchPlugin
                    .createExtension(configurationElement,
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
    	if (configurationElement == null) {
    		return className;
    	}
    	return configurationElement.getAttribute(ATT_CLASS);
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
        if (program == null) {
        	if (configurationElement == null) {
        		return fileName;
        	}
        	return configurationElement.getAttribute(ATT_COMMAND);
    	}
        return program.getName();
    }

    /**
     * @see IResourceEditorDescriptor
     */
    public String getId() {
        if (program == null) {
        	if (configurationElement == null) {
        		return id;
        	}
        	return configurationElement.getAttribute(ATT_ID);
        	
        }
        return program.getName();
    }

    /**
     * @see IResourceEditorDescriptor
     */
    public ImageDescriptor getImageDescriptor() {
    	if (testImage) {
    		testImage = false;
			if (imageDesc == null) {
				String imageFileName = getImageFilename();
				String command = getFileName();
				if (imageFileName != null && configurationElement != null) {
					imageDesc = AbstractUIPlugin.imageDescriptorFromPlugin(
							configurationElement.getNamespace(), imageFileName);
				} else if (command != null) {
					imageDesc = WorkbenchImages.getImageDescriptorFromProgram(
							command, 0);
				}
			}
			verifyImage();    		
    	}
    	
        return imageDesc;
    }

    /**
	 * Verifies that the image descriptor generates an image.  If not, the 
	 * descriptor is replaced with the default image.
	 * 
	 * @since 3.1
	 */
	private void verifyImage() {
		if (imageDesc == null) {
			imageDesc = WorkbenchImages
         		.getImageDescriptor(ISharedImages.IMG_OBJ_FILE);
		}
		else {
			Image img = imageDesc.createImage(false);
			if (img == null) {
			    // @issue what should be the default image?
			    imageDesc = WorkbenchImages
			            .getImageDescriptor(ISharedImages.IMG_OBJ_FILE);
			} else {
			    img.dispose();
			}
		}
	}

	/**
     * @see IResourceEditorDescriptor
     */
    public String getImageFilename() {
    	if (configurationElement == null)
    		return imageFilename;
    	return configurationElement.getAttribute(ATT_ICON);
    }

    /**
     * @see IResourceEditorDescriptor
     */
    public String getLabel() {
        if (program == null) {
        	if (configurationElement == null) {
        		return editorName;        		
        	}
        	return configurationElement.getAttribute(ATT_NAME);
        }
        return program.getName();
    }

    /**
     * Returns the class name of the launcher.
     */
    public String getLauncher() {
    	if (configurationElement == null)
    		return launcherName;
    	return configurationElement.getAttribute(EditorDescriptor.ATT_LAUNCHER);
    }

    /**
     * @see IResourceEditorDescriptor
     */
    public String getPluginID() {
    	if (configurationElement != null)
    		return configurationElement.getNamespace();
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
     * @see org.eclipse.ui.IEditorDescriptor#isInternal
     */
    public boolean isInternal() {
        return getOpenMode() == OPEN_INTERNAL;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorDescriptor#isOpenInPlace
     */
    public boolean isOpenInPlace() {
        return getOpenMode() == OPEN_INPLACE;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorDescriptor#isOpenExternal
     */
    public boolean isOpenExternal() {
        return getOpenMode() == OPEN_EXTERNAL;
    }

    /**
     * Load the object properties from a memento.
     * 
     * @return <code>true</code> if the values are valid, <code>false</code> otherwise
     */
    protected boolean loadValues(IMemento memento) {
        editorName = memento.getString(IWorkbenchConstants.TAG_LABEL);
        imageFilename = memento.getString(IWorkbenchConstants.TAG_IMAGE);
        className = memento.getString(IWorkbenchConstants.TAG_CLASS);
        launcherName = memento.getString(IWorkbenchConstants.TAG_LAUNCHER);
        fileName = memento.getString(IWorkbenchConstants.TAG_FILE);
        id = memento.getString(IWorkbenchConstants.TAG_ID);
        pluginIdentifier = memento.getString(IWorkbenchConstants.TAG_PLUGIN);

        Integer openModeInt = memento
                .getInteger(IWorkbenchConstants.TAG_OPEN_MODE);
        if (openModeInt != null) {
            openMode = openModeInt.intValue();
        } else {
            // legacy: handle the older attribute names, needed to allow reading of pre-3.0-RCP workspaces 
            boolean internal = new Boolean(memento
                    .getString(IWorkbenchConstants.TAG_INTERNAL))
                    .booleanValue();
            boolean openInPlace = new Boolean(memento
                    .getString(IWorkbenchConstants.TAG_OPEN_IN_PLACE))
                    .booleanValue();
            if (internal) {
                openMode = OPEN_INTERNAL;
            } else {
                if (openInPlace) {
                    openMode = OPEN_INPLACE;
                } else {
                    openMode = OPEN_EXTERNAL;
                }
            }
        }
        if (openMode != OPEN_EXTERNAL && openMode != OPEN_INTERNAL
                && openMode != OPEN_INPLACE) {
            WorkbenchPlugin
                    .log("Ignoring editor descriptor with invalid openMode: " + this); //$NON-NLS-1$
            return false;
        }

        String programName = memento
                .getString(IWorkbenchConstants.TAG_PROGRAM_NAME);
        if (programName != null) {
            this.program = findProgram(programName);
        }
        return true;
    }

    /**
     * Save the object values in a IMemento
     */
    protected void saveValues(IMemento memento) {
        memento.putString(IWorkbenchConstants.TAG_LABEL, getLabel());
        memento.putString(IWorkbenchConstants.TAG_IMAGE, getImageFilename());
        memento.putString(IWorkbenchConstants.TAG_CLASS, getClassName());
        memento.putString(IWorkbenchConstants.TAG_LAUNCHER, getLauncher());
        memento.putString(IWorkbenchConstants.TAG_FILE, getFileName());
        memento.putString(IWorkbenchConstants.TAG_ID, getId());
        memento.putString(IWorkbenchConstants.TAG_PLUGIN, getPluginId());

        memento.putInteger(IWorkbenchConstants.TAG_OPEN_MODE, getOpenMode());
        // legacy: handle the older attribute names, needed to allow reading of workspace by pre-3.0-RCP eclipses
        memento.putString(IWorkbenchConstants.TAG_INTERNAL, String
                .valueOf(isInternal()));
        memento.putString(IWorkbenchConstants.TAG_OPEN_IN_PLACE, String
                .valueOf(isOpenInPlace()));

        if (this.program != null)
            memento.putString(IWorkbenchConstants.TAG_PROGRAM_NAME,
                    this.program.getName());
    }

    /**
     * Return the open mode of this editor.
     *
	 * @return the open mode of this editor
	 * @since 3.1
	 */
	private int getOpenMode() {
        if (getLauncher() != null) {
            // open using a launcer
        	return EditorDescriptor.OPEN_EXTERNAL;
        } else if (getFileName() != null) {
            // open using an external editor 	
            return EditorDescriptor.OPEN_EXTERNAL;
        } else {
        	// open using an internal editor
        	return EditorDescriptor.OPEN_INTERNAL;
        }
	}

	/**
     * Set the class name of an internal editor.
     */
    /* package */void setClassName(String newClassName) {
        className = newClassName;
    }

    /**
     * Set the configuration element which contributed this editor.
     */
    /* package */void setConfigurationElement(
            IConfigurationElement newConfigurationElement) {
        configurationElement = newConfigurationElement;
    }

    /**
     * Set the filename of an external editor.
     */
    /* package */void setFileName(String aFileName) {
        fileName = aFileName;
    }

    /**
     * Set the id of the editor.
     * For internal editors this is the id as provided in the extension point
     * For external editors it is path and filename of the editor
     */
    /* package */void setID(String anID) {
        id = anID;
    }

    /**
     * The Image to use to repesent this editor
     */
    /* package */void setImageDescriptor(ImageDescriptor desc) {
        imageDesc = desc;
        testImage = true;
    }

    /**
     * The name of the image to use for this editor.
     */
    /* package */void setImageFilename(String aFileName) {
        imageFilename = aFileName;
    }

    /**
     * Sets the new launcher class name
     *
     * @param newLauncher the new launcher
     */
    /* package */void setLauncher(String newLauncher) {
        launcherName = newLauncher;
    }

    /**
     * The label to show for this editor.
     */
    /* package */void setName(String newName) {
        editorName = newName;
    }

    /**
     * Sets the open mode of this editor descriptor.
     * 
     * @param mode the open mode
     * 
     * @issue this method is public as a temporary fix for bug 47600
     */
    public void setOpenMode(int mode) {
        openMode = mode;
    }

    /**
     * The id of the plugin which contributed this editor, null for external editors.
     */
    /* package */void setPluginIdentifier(String anID) {
        pluginIdentifier = anID;
    }

    /**
     * Set the receivers program.
     * @param newProgram
     */
    /* package */void setProgram(Program newProgram) {

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

    /* (non-Javadoc)
     * @see org.eclipse.ui.activities.support.IPluginContribution#getLocalId()
     */
    public String getLocalId() {
        return getId();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.activities.support.IPluginContribution#getPluginId()
     */
    public String getPluginId() {
        return getPluginID();
    }
}