package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;

import java.io.IOException;
import java.io.File;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import java.util.*;

/**
 * Perspective registry.
 */
public class PerspectiveRegistry implements IPerspectiveRegistry {
	private File rootFolder;
	private ArrayList children = new ArrayList(10);
	private String defPerspID;
	private static final String EXT = "_persp.xml";//$NON-NLS-1$
	private static final String ID_DEF_PERSP = "PerspectiveRegistry.DEFAULT_PERSP";//$NON-NLS-1$
/**
 * Construct a new registry.
 *
 * @param rootFolder is the root folder for perspective files.
 */
public PerspectiveRegistry(File rootFolder) {
	super();
	this.rootFolder = rootFolder;
}
/**
 * Adds a perspective.  This is typically used by the reader.
 */
public void addPerspective(PerspectiveDescriptor desc) {
	children.add(desc);
	desc.setCustomFile(new File(rootFolder, desc.getId() + EXT));
}
/**
 * Create a new perspective.
 * Return null if the creation failed.
 */
public PerspectiveDescriptor createPerspective(String label,PerspectiveDescriptor originalDescriptor) {
	// Sanity check to avoid duplicate labels.
	if (!validateLabel(label))
		return null;

	// Calculate ID.
	String id = label.replace(' ', '_');
	id = id.trim();

	// Calculate storage file
	String name = id + EXT;
	File file = new File(rootFolder, name);

	// Create descriptor.
	PerspectiveDescriptor desc = new PerspectiveDescriptor(id, label,originalDescriptor);
	desc.setCustomFile(file);
	children.add(desc);
	return desc;
}
/**
 * Delete a perspective.
 * Has no effect if the perspective is defined in an extension.
 */
public void deletePerspective(IPerspectiveDescriptor in) {
	PerspectiveDescriptor desc = (PerspectiveDescriptor)in;
	if (!desc.isPredefined()) {
		children.remove(desc);
		desc.deleteCustomFile();
		verifyDefaultPerspective();
	}
}
/**
 * @see IPerspectiveRegistry
 */
public IPerspectiveDescriptor findPerspectiveWithId(String id) {
	Iterator enum = children.iterator();
	while (enum.hasNext()) {
		IPerspectiveDescriptor desc = (IPerspectiveDescriptor)enum.next();
		if (desc.getId().equals(id))
			return desc;
	}
	return null;
}
/**
 * @see IPerspectiveRegistry
 */
public IPerspectiveDescriptor findPerspectiveWithLabel(String label) {
	Iterator enum = children.iterator();
	while (enum.hasNext()) {
		IPerspectiveDescriptor desc = (IPerspectiveDescriptor)enum.next();
		if (desc.getLabel().equals(label))
			return desc;
	}
	return null;
}
/**
 * Returns the id of the default perspective for the workbench.  This identifies one
 * perspective extension within the workbench's perspective registry.
 *
 * @return the default perspective id; will never be <code>null</code>
 */
public String getDefaultPerspective() {
	return defPerspID;
}
/**
 * @see IPerspectiveRegistry
 */
public IPerspectiveDescriptor[] getPerspectives() {
	int nSize = children.size();
	IPerspectiveDescriptor [] retArray = new IPerspectiveDescriptor[nSize];
	for (int nX = 0; nX < nSize; nX ++) {
		retArray[nX] = (IPerspectiveDescriptor)children.get(nX);
	}
	return retArray;
}
/**
 * Loads the registry.
 */
public void load() {
	// Load the registries.  
	loadPredefined();
	loadCustom();

	// Get default perspective.
	
	defPerspID = 
		WorkbenchPlugin.getDefault().getPreferenceStore().getString(IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID);
	verifyDefaultPerspective();
}
/**
 * Read children from the file system.
 */
private void loadCustom() {
	if (rootFolder.isDirectory()) {
		File [] fileList = rootFolder.listFiles();
		int nSize = fileList.length;
		for (int nX = 0; nX < nSize; nX ++) {
			File file = fileList[nX];
			if (file.getName().endsWith(EXT)) {
				try {
					PerspectiveDescriptor newPersp = new PerspectiveDescriptor(file);
					String id = newPersp.getId();
					IPerspectiveDescriptor oldPersp = findPerspectiveWithId(id);
					if (oldPersp == null)
						children.add(newPersp);
				} catch (IOException e) {
				} catch (WorkbenchException e) {
				}
			}
		}
	}
}
/**
 * Read children from the plugin registry.
 */
private void loadPredefined() {
	PerspectiveRegistryReader reader = new PerspectiveRegistryReader();
	reader.readPerspectives(Platform.getPluginRegistry(), this);
}
/**
 * Sets the default perspective for the workbench to the given perspective id.
 * The id must correspond to one perspective extension within the workbench's 
 * perspective registry.
 *
 * @param id a perspective id; must not be <code>null</code>
 */
public void setDefaultPerspective(String id) {
	IPerspectiveDescriptor desc = findPerspectiveWithId(id);
	if (desc != null) {
		defPerspID = id;
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		store.setValue(IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID, id);
	}
}
/**
 * Return true if a label is valid and unused.
 */
public boolean validateLabel(String label) {
	label = label.trim();
	if (label.length() <= 0) 
		return false;
	return true;
}
/**
 * Verifies the id of the default perspective.  If the
 * default perspective is invalid use the workbench default.
 */
private void verifyDefaultPerspective() {
	// Step 1: Try current defPerspId value.
	IPerspectiveDescriptor desc = null;
	if (defPerspID != null)
		desc = findPerspectiveWithId(defPerspID);
	if (desc != null)
		return;

	// Step 2. Read default value.
	defPerspID = 
		WorkbenchPlugin.getDefault().getPreferenceStore().getDefaultString(IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID);
	if (defPerspID != null)
		desc = findPerspectiveWithId(defPerspID);
	if (desc != null)
		return;

	// Step 3. Use internal workbench default.
	defPerspID = IWorkbenchConstants.DEFAULT_LAYOUT_ID;
}
}
