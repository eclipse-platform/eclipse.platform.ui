/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/
package org.eclipse.ui.internal.registry;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Perspective registry.
 */
public class PerspectiveRegistry implements IPerspectiveRegistry {
	private ArrayList children = new ArrayList(10);
	private String defaultPerspID;
	private static final String EXT = "_persp.xml";//$NON-NLS-1$
	private static final String ID_DEF_PERSP = "PerspectiveRegistry.DEFAULT_PERSP";//$NON-NLS-1$
	private static final String PERSP = "_persp"; //$NON-NLS-1$
	private static final char SPACE_DELIMITER = ' '; //$NON-NLS-1$
	//keep track of the perspectives the user has selected to remove or revert
	private ArrayList perspToRemove = new ArrayList(5);
	
/**
 * Construct a new registry.
 *
 * @param rootFolder is the root folder for perspective files.
 */
public PerspectiveRegistry() {
	super();
	
	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
	store.addPropertyChangeListener(new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			/* To ensure the that no custom perspective definitions are deleted
			 * when preferences are imported, merge old and new values */
			if (event.getProperty().endsWith(PERSP)) {
				/* A Perspective is being changed, merge */
				mergePerspectives(event);
			} else if (event.getProperty().equals(IPreferenceConstants.PERSPECTIVES)) {
				/* The list of perpsectives is being changed, merge */
				updatePreferenceList((IPreferenceStore)event.getSource());
			}
		}

		private void mergePerspectives(PropertyChangeEvent event) {
			IPreferenceStore store = (IPreferenceStore)event.getSource();			
			if (event.getNewValue() == null) { 
				/* Perpsective is being removed; if the user has deleted or reverted 
				 * a custom perspective, let the change pass through.
				 * Otherwise, restore the custom perspective entry */
				 
				// Find the matching descriptor in the registry
				IPerspectiveDescriptor [] perspectiveList = getPerspectives();
				for (int i = 0; i < perspectiveList.length; i++) {
					String id = perspectiveList[i].getId();	
					if (event.getProperty().startsWith(id)) { //found descriptor
						//see if the perspective has been flagged for reverting or deleting
						if ( ! perspToRemove.contains(id)) { //restore
							store.setValue(id + PERSP, (String)event.getOldValue());
						} else {	//remove element from the list
							perspToRemove.remove(id);
						}
					}
				}
			} else if ((event.getOldValue() == null || event.getOldValue().equals(""))) {
				
				/* New perspective is being added, update the perspectiveRegistry to 
				 * contain the new custom perspective */
				 
				String id = event.getProperty().substring(0, event.getProperty().lastIndexOf(PERSP));
				if (findPerspectiveWithId(id) == null) { 
					//perspective does not already exist in registry, add it
					PerspectiveDescriptor desc = new PerspectiveDescriptor(null, null, null);
					StringReader reader = new StringReader((String)event.getNewValue());
					try {
						XMLMemento memento = XMLMemento.createReadRoot(reader);
						desc.restoreState(memento);
						children.add(desc);						
					} catch (WorkbenchException e) {
						unableToLoadPerspective(e.getStatus());
					}
				}
			}
			/* If necessary, add to the list of perspectives */
			updatePreferenceList(store);
		}

		/* Update the list of perspectives from the registry.  This will be called 
		 * for each perspective during an import preferences, but is necessary
		 * to ensure the perspectives list stays consistent with the registry */
		private void updatePreferenceList(IPreferenceStore store) {
			IPerspectiveDescriptor [] perspectiveList = getPerspectives();
			StringBuffer perspBuffer = new StringBuffer();
			for (int i = 0; i < perspectiveList.length; i++) {
				PerspectiveDescriptor desc = (PerspectiveDescriptor) perspectiveList[i];
				if (hasCustomDefinition(desc)) {
					perspBuffer.append(desc.getId()).append(SPACE_DELIMITER);
				}
			}
			String newList = perspBuffer.toString().trim();
			store.setValue(IPreferenceConstants.PERSPECTIVES, newList);
		}		
	});

}
/**
 * Adds a perspective.  This is typically used by the reader.
 */
public void addPerspective(PerspectiveDescriptor desc) {
	children.add(desc);
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

	// Create descriptor.
	PerspectiveDescriptor desc = new PerspectiveDescriptor(id, label,originalDescriptor);
	children.add(desc);
	return desc;
}
/**
 * Reverts a list of perspectives back to the plugin definition
 * @param perspToRevert
 */
public void revertPerspectives(ArrayList perspToRevert) {
	//indicate that the user is removing these perspectives	
	for (int i = 0; i < perspToRevert.size(); i++) {
		PerspectiveDescriptor desc = (PerspectiveDescriptor) perspToRevert.get(i);
		perspToRemove.add(desc.getId());
		desc.revertToPredefined();
	}
}
/**
 * Deletes a list of perspectives
 * @param perspToDelete
 */
public void deletePerspectives(ArrayList perspToDelete) {	
	for (int i = 0; i < perspToDelete.size(); i++)
		deletePerspective((IPerspectiveDescriptor) perspToDelete.get(i));
}
/**
 * Delete a perspective.
 * Has no effect if the perspective is defined in an extension.
 */
public void deletePerspective(IPerspectiveDescriptor in) {
	PerspectiveDescriptor desc = (PerspectiveDescriptor)in;
	if (!desc.isPredefined()) {
		//indicate that the user is removing these perspectives
		perspToRemove.add(desc.getId());
		children.remove(desc);
		desc.deleteCustomDefinition();
		verifyDefaultPerspective();
	}
}
/**
 * Removes the custom definition of a perspective from the preference sotre
 * @param perspectiveDescriptor
 */
/* package */ void deleteCustomDefinition(PerspectiveDescriptor desc) {
	//remove the entry from the preference store.
	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
	
	/* To delete the perspective definition from the preference store, use the 
	 * setToDefault method.  Since no default is defined, this will remove the
	 * entry */
	store.setToDefault(desc.getId() + PERSP);

}
/**
 * Method hasCustomDefinition.
 * @param perspectiveDescriptor
 */
/* package */ boolean hasCustomDefinition(PerspectiveDescriptor desc) {
	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
	return store.contains(desc.getId() + PERSP);
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
	return defaultPerspID;
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
	// Get it from the R1.0 dialog settings first. Fixes bug 17039 
	IDialogSettings dialogSettings = WorkbenchPlugin.getDefault().getDialogSettings();
	String str = dialogSettings.get(ID_DEF_PERSP);
	if (str != null && str.length() > 0) {
		setDefaultPerspective(str);
		dialogSettings.put(ID_DEF_PERSP, "");		//$NON-NLS-1$
	} else {
		AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
		defaultPerspID = 
			plugin.getPreferenceStore().getString(IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID);
	}
	verifyDefaultPerspective();
}
/**
 * Read children from the file system.
 */
private void loadCustom() {
	Reader reader = null;	
	
	/* Get the entries from the Preference store */
	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
	
	/* Get the space-delimited list of custom perspective ids */ 
	String customPerspectives = store.getString(IPreferenceConstants.PERSPECTIVES);
	String [] perspectivesList = StringConverter.asArray(customPerspectives);

	for (int i = 0; i < perspectivesList.length; i++) {
		try {
			String xmlString = store.getString(perspectivesList[i] + PERSP);
			if(xmlString != null && xmlString.length() != 0) 
				reader = new StringReader(xmlString);
				
			// Restore the layout state.
			XMLMemento memento = XMLMemento.createReadRoot(reader);
			PerspectiveDescriptor newPersp = new PerspectiveDescriptor(null, null, null);
			newPersp.restoreState(memento);
			String id = newPersp.getId();
			IPerspectiveDescriptor oldPersp = findPerspectiveWithId(id);
			if (oldPersp == null)
				children.add(newPersp);	
			reader.close();
			} catch (IOException e) {
				unableToLoadPerspective(null);
			} catch (WorkbenchException e) {
				unableToLoadPerspective(e.getStatus());
			}
	}
		
	/* Get the entries from files, if any */
	IPath path = WorkbenchPlugin.getDefault().getStateLocation();
	File folder = path.toFile();
	
	if (folder.isDirectory()) {
		File [] fileList = folder.listFiles();
		int nSize = fileList.length;
		for (int nX = 0; nX < nSize; nX ++) {
			File file = fileList[nX];
			if (file.getName().endsWith(EXT)) {
				//get the memento
				InputStream stream = null;
				try {
					stream = new FileInputStream(file);
					reader = new InputStreamReader(stream, "utf-8"); //$NON-NLS-1$
					
					// Restore the layout state.
					XMLMemento memento = XMLMemento.createReadRoot(reader);
					PerspectiveDescriptor newPersp = new PerspectiveDescriptor(null, null, null);
					newPersp.restoreState(memento);
					IPerspectiveDescriptor oldPersp = findPerspectiveWithId(newPersp.getId());
					if (oldPersp == null)
						children.add(newPersp);
					
					//save to the preference store
					saveCustomPersp(newPersp, memento);
					
					//delete the file
					file.delete();

					reader.close();
					stream.close();					
					} catch (IOException e) {
						unableToLoadPerspective(null);
					} catch (WorkbenchException e) {
						unableToLoadPerspective(e.getStatus());
					}
			}
		}
	}
}
private void unableToLoadPerspective(IStatus status) {
	String title = WorkbenchMessages.getString("Perspective.problemLoadingTitle");  //$NON-NLS-1$
	String msg = WorkbenchMessages.getString("Perspective.errorLoadingState"); //$NON-NLS-1$
	if(status == null) {
		MessageDialog.openError((Shell)null,title,msg);
	} else {
		ErrorDialog.openError((Shell)null,title,msg,status);
	}
}
/**
 * Saves a custom perspective definition to the preference store.
 * @param desc
 */
public void saveCustomPersp(PerspectiveDescriptor realDesc, XMLMemento memento) throws IOException {

	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
	
	// Save it to the preference store.
	Writer writer = new StringWriter();;
	
	memento.save(writer);
	writer.close();
	store.setValue(realDesc.getId() + PERSP,writer.toString());
	
}
/**
 * Gets the Custom perspective definition from the preference store.
 * @param string
 * @return IMemento
 */
public IMemento getCustomPersp(String id) throws WorkbenchException, IOException {
	Reader reader = null;
	
	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
	String xmlString = store.getString(id + PERSP);
	if(xmlString != null && xmlString.length() != 0) { //defined in store
		reader = new StringReader(xmlString);
	} 
	XMLMemento memento = XMLMemento.createReadRoot(reader);
	reader.close();
	return memento;
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
		defaultPerspID = id;
		AbstractUIPlugin uiPlugin =
			(AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);

		uiPlugin.getPreferenceStore().
			setValue(IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID, id);
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
	if (defaultPerspID != null)
		desc = findPerspectiveWithId(defaultPerspID);
	if (desc != null)
		return;

	// Step 2. Read default value.
	AbstractUIPlugin uiPlugin =
		(AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
	defaultPerspID = 
		uiPlugin.getPreferenceStore().getDefaultString(IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID);
	if (defaultPerspID != null)
		desc = findPerspectiveWithId(defaultPerspID);
	if (desc != null)
		return;

	// Step 3. Use internal workbench default.
	defaultPerspID = IWorkbenchConstants.DEFAULT_LAYOUT_ID;
}
}
