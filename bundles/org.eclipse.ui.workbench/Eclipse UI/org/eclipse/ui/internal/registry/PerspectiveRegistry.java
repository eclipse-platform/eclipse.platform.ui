/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jan-Hendrik Diederich, Bredex GmbH - bug 201052
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.handlers.ClosePerspectiveHandler;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Perspective registry.
 */
public class PerspectiveRegistry implements IPerspectiveRegistry,
		IExtensionChangeHandler {
	private String defaultPerspID;

	private static final String EXT = "_persp.xml"; //$NON-NLS-1$

	private static final String ID_DEF_PERSP = "PerspectiveRegistry.DEFAULT_PERSP"; //$NON-NLS-1$

	private static final String PERSP = "_persp"; //$NON-NLS-1$

	private static final char SPACE_DELIMITER = ' ';

	private List perspectives = new ArrayList(10);

	// keep track of the perspectives the user has selected to remove or revert
	private ArrayList perspToRemove = new ArrayList(5);

	private IPropertyChangeListener preferenceListener;

	/**
	 * Construct a new registry.
	 */
	public PerspectiveRegistry() {
		IExtensionTracker tracker = PlatformUI.getWorkbench()
				.getExtensionTracker();
		tracker.registerHandler(this, null);

		initializePreferenceChangeListener();
		WorkbenchPlugin.getDefault().getPreferenceStore()
				.addPropertyChangeListener(preferenceListener);

	}

	/**
	 * Initialize the preference change listener.
	 */
	private void initializePreferenceChangeListener() {
		preferenceListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				/*
				 * To ensure the that no custom perspective definitions are
				 * deleted when preferences are imported, merge old and new
				 * values
				 */
				if (event.getProperty().endsWith(PERSP)) {
					/* A Perspective is being changed, merge */
					mergePerspectives(event);
				} else if (event.getProperty().equals(
						IPreferenceConstants.PERSPECTIVES)) {
					/* The list of perpsectives is being changed, merge */
					updatePreferenceList((IPreferenceStore) event.getSource());
				}
			}

			private void mergePerspectives(PropertyChangeEvent event) {
				IPreferenceStore store = (IPreferenceStore) event.getSource();
				if (event.getNewValue() == null
						|| event.getNewValue().equals("")) { //$NON-NLS-1$
					/*
					 * Perpsective is being removed; if the user has deleted or
					 * reverted a custom perspective, let the change pass
					 * through. Otherwise, restore the custom perspective entry
					 */

					// Find the matching descriptor in the registry
					IPerspectiveDescriptor[] perspectiveList = getPerspectives();
					for (int i = 0; i < perspectiveList.length; i++) {
						String id = perspectiveList[i].getId();
						if (event.getProperty().equals(id + PERSP)) { // found
							// descriptor
							// see if the perspective has been flagged for
							// reverting or deleting
							if (!perspToRemove.contains(id)) { // restore
								store.setValue(id + PERSP, (String) event
										.getOldValue());
							} else { // remove element from the list
								perspToRemove.remove(id);
							}
						}
					}
				} else if ((event.getOldValue() == null || event.getOldValue()
						.equals(""))) { //$NON-NLS-1$

					/*
					 * New perspective is being added, update the
					 * perspectiveRegistry to contain the new custom perspective
					 */

					String id = event.getProperty().substring(0,
							event.getProperty().lastIndexOf(PERSP));
					if (findPerspectiveWithId(id) == null) {
						// perspective does not already exist in registry, add
						// it
						PerspectiveDescriptor desc = new PerspectiveDescriptor(
								null, null, null);
						StringReader reader = new StringReader((String) event
								.getNewValue());
						try {
							XMLMemento memento = XMLMemento
									.createReadRoot(reader);
							desc.restoreState(memento);
							addPerspective(desc);
						} catch (WorkbenchException e) {
							unableToLoadPerspective(e.getStatus());
						}
					}
				}
				/* If necessary, add to the list of perspectives */
				updatePreferenceList(store);
			}

			/*
			 * Update the list of perspectives from the registry. This will be
			 * called for each perspective during an import preferences, but is
			 * necessary to ensure the perspectives list stays consistent with
			 * the registry
			 */
			private void updatePreferenceList(IPreferenceStore store) {
				IPerspectiveDescriptor[] perspectiveList = getPerspectives();
				StringBuffer perspBuffer = new StringBuffer();
				for (int i = 0; i < perspectiveList.length; i++) {
					PerspectiveDescriptor desc = (PerspectiveDescriptor) perspectiveList[i];
					if (hasCustomDefinition(desc)) {
						perspBuffer.append(desc.getId())
								.append(SPACE_DELIMITER);
					}
				}
				String newList = perspBuffer.toString().trim();
				store.setValue(IPreferenceConstants.PERSPECTIVES, newList);
			}
		};
	}

	/**
	 * Adds a perspective. This is typically used by the reader.
	 * 
	 * @param desc
	 */
	public void addPerspective(PerspectiveDescriptor desc) {
		if (desc == null) {
			return;
		}
		add(desc);
	}

	/**
	 * @param desc
	 */
	private void add(PerspectiveDescriptor desc) {
		perspectives.add(desc);
		IConfigurationElement element = desc.getConfigElement();
		if (element != null) {
			PlatformUI.getWorkbench().getExtensionTracker().registerObject(
					element.getDeclaringExtension(), desc,
					IExtensionTracker.REF_WEAK);
		}
	}

	/**
	 * Create a new perspective.
	 * 
	 * @param label
	 *            the name of the new descriptor
	 * @param originalDescriptor
	 *            the descriptor on which to base the new descriptor
	 * @return a new perspective descriptor or <code>null</code> if the
	 *         creation failed.
	 */
	public PerspectiveDescriptor createPerspective(String label,
			PerspectiveDescriptor originalDescriptor) {
		// Sanity check to avoid invalid or duplicate labels.
		if (!validateLabel(label)) {
			return null;
		}
		if (findPerspectiveWithLabel(label) != null) {
			return null;
		}

		// Calculate ID.
		String id = label.replace(' ', '_');
		id = id.trim();

		// Create descriptor.
		PerspectiveDescriptor desc = new PerspectiveDescriptor(id, label,
				originalDescriptor);
		add(desc);
		return desc;
	}

	/**
	 * Reverts a list of perspectives back to the plugin definition
	 * 
	 * @param perspToRevert
	 */
	public void revertPerspectives(ArrayList perspToRevert) {
		// indicate that the user is removing these perspectives
		for (int i = 0; i < perspToRevert.size(); i++) {
			PerspectiveDescriptor desc = (PerspectiveDescriptor) perspToRevert
					.get(i);
			perspToRemove.add(desc.getId());
			desc.revertToPredefined();
		}
	}

	/**
	 * Deletes a list of perspectives
	 * 
	 * @param perspToDelete
	 */
	public void deletePerspectives(ArrayList perspToDelete) {
		for (int i = 0; i < perspToDelete.size(); i++) {
			deletePerspective((IPerspectiveDescriptor) perspToDelete.get(i));
		}
	}

	/**
	 * Delete a perspective. Has no effect if the perspective is defined in an
	 * extension.
	 * 
	 * @param in
	 */
	public void deletePerspective(IPerspectiveDescriptor in) {
		PerspectiveDescriptor desc = (PerspectiveDescriptor) in;
		// Don't delete predefined perspectives
		if (!desc.isPredefined()) {
			perspToRemove.add(desc.getId());
			perspectives.remove(desc);
			desc.deleteCustomDefinition();
			verifyDefaultPerspective();
		}
	}

	/**
	 * Delete a perspective. This will remove perspectives defined in
	 * extensions.
	 * 
	 * @param desc
	 *            the perspective to delete
	 * @since 3.1
	 */
	private void internalDeletePerspective(PerspectiveDescriptor desc) {
		perspToRemove.add(desc.getId());
		perspectives.remove(desc);
		desc.deleteCustomDefinition();
		verifyDefaultPerspective();
	}

	/**
	 * Removes the custom definition of a perspective from the preference store
	 * 
	 * @param desc
	 */
	/* package */
	void deleteCustomDefinition(PerspectiveDescriptor desc) {
		// remove the entry from the preference store.
		IPreferenceStore store = WorkbenchPlugin.getDefault()
				.getPreferenceStore();

		/*
		 * To delete the perspective definition from the preference store, use
		 * the setToDefault method. Since no default is defined, this will
		 * remove the entry
		 */
		store.setToDefault(desc.getId() + PERSP);

	}

	/**
	 * Method hasCustomDefinition.
	 * 
	 * @param desc
	 */
	/* package */
	boolean hasCustomDefinition(PerspectiveDescriptor desc) {
		IPreferenceStore store = WorkbenchPlugin.getDefault()
				.getPreferenceStore();
		return store.contains(desc.getId() + PERSP);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveRegistry#findPerspectiveWithId(java.lang.String)
	 */
	public IPerspectiveDescriptor findPerspectiveWithId(String id) {	    
		for (Iterator i = perspectives.iterator(); i.hasNext();) {
		    PerspectiveDescriptor desc = (PerspectiveDescriptor) i.next();
			if (desc.getId().equals(id)) {
			    if (WorkbenchActivityHelper.restrictUseOf(desc)) {
			        return null;
			    }
				return desc;
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveRegistry#findPerspectiveWithLabel(java.lang.String)
	 */
	public IPerspectiveDescriptor findPerspectiveWithLabel(String label) {
		for (Iterator i = perspectives.iterator(); i.hasNext();) {
			PerspectiveDescriptor desc = (PerspectiveDescriptor) i.next();
			if (desc.getLabel().equals(label)) {
			    if (WorkbenchActivityHelper.restrictUseOf(desc)) {
			        return null;
			    }
				return desc;
			}
		}
		return null;
	}

	/**
	 * @see IPerspectiveRegistry#getDefaultPerspective()
	 */
	public String getDefaultPerspective() {
		return defaultPerspID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveRegistry#getPerspectives()
	 */
	public IPerspectiveDescriptor[] getPerspectives() {
		Collection descs = WorkbenchActivityHelper.restrictCollection(perspectives, new ArrayList());
		return (IPerspectiveDescriptor[]) descs.toArray(new IPerspectiveDescriptor[descs.size()]);
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
		IDialogSettings dialogSettings = WorkbenchPlugin.getDefault()
				.getDialogSettings();
		String str = dialogSettings.get(ID_DEF_PERSP);
		if (str != null && str.length() > 0) {
			setDefaultPerspective(str);
			dialogSettings.put(ID_DEF_PERSP, ""); //$NON-NLS-1$
		}
		verifyDefaultPerspective();
	}

	/**
	 * Read children from the file system.
	 */
	private void loadCustom() {
		Reader reader = null;

		/* Get the entries from the Preference store */
		IPreferenceStore store = WorkbenchPlugin.getDefault()
				.getPreferenceStore();

		/* Get the space-delimited list of custom perspective ids */
		String customPerspectives = store
				.getString(IPreferenceConstants.PERSPECTIVES);
		String[] perspectivesList = StringConverter.asArray(customPerspectives);

		for (int i = 0; i < perspectivesList.length; i++) {
			try {
				String xmlString = store.getString(perspectivesList[i] + PERSP);
				if (xmlString != null && xmlString.length() != 0) {
					reader = new StringReader(xmlString);
				}

				// Restore the layout state.
				XMLMemento memento = XMLMemento.createReadRoot(reader);
				PerspectiveDescriptor newPersp = new PerspectiveDescriptor(
						null, null, null);
				newPersp.restoreState(memento);
				String id = newPersp.getId();
				IPerspectiveDescriptor oldPersp = findPerspectiveWithId(id);
				if (oldPersp == null) {
					add(newPersp);
				}
				reader.close();
			} catch (IOException e) {
				unableToLoadPerspective(null);
			} catch (WorkbenchException e) {
				unableToLoadPerspective(e.getStatus());
			}
		}

		// Get the entries from files, if any
		// if -data @noDefault specified the state location may not be
		// initialized
		IPath path = WorkbenchPlugin.getDefault().getDataLocation();
		if (path == null) {
			return;
		}

		File folder = path.toFile();

		if (folder.isDirectory()) {
			File[] fileList = folder.listFiles();
			int nSize = fileList.length;
			for (int nX = 0; nX < nSize; nX++) {
				File file = fileList[nX];
				if (file.getName().endsWith(EXT)) {
					// get the memento
					InputStream stream = null;
					try {
						stream = new FileInputStream(file);
						reader = new BufferedReader(new InputStreamReader(
								stream, "utf-8")); //$NON-NLS-1$

						// Restore the layout state.
						XMLMemento memento = XMLMemento.createReadRoot(reader);
						PerspectiveDescriptor newPersp = new PerspectiveDescriptor(
								null, null, null);
						newPersp.restoreState(memento);
						IPerspectiveDescriptor oldPersp = findPerspectiveWithId(newPersp
								.getId());
						if (oldPersp == null) {
							add(newPersp);
						}

						// save to the preference store
						saveCustomPersp(newPersp, memento);

						// delete the file
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

	/**
	 * @param status
	 */
	private void unableToLoadPerspective(IStatus status) {
		String msg = WorkbenchMessages.Perspective_errorLoadingState;
		if (status == null) {
			IStatus errStatus = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, msg); 
	    	StatusManager.getManager().handle(errStatus, StatusManager.SHOW);
		} else {
			IStatus errStatus = StatusUtil.newStatus(status, msg); 
	    	StatusManager.getManager().handle(errStatus, StatusManager.SHOW);
		}
	}

	/**
	 * Saves a custom perspective definition to the preference store.
	 * 
	 * @param desc
	 *            the perspective
	 * @param memento
	 *            the memento to save to
	 * @throws IOException
	 */
	public void saveCustomPersp(PerspectiveDescriptor desc, XMLMemento memento)
			throws IOException {

		IPreferenceStore store = WorkbenchPlugin.getDefault()
				.getPreferenceStore();

		// Save it to the preference store.
		Writer writer = new StringWriter();

		memento.save(writer);
		writer.close();
		store.setValue(desc.getId() + PERSP, writer.toString());

	}

	/**
	 * Gets the Custom perspective definition from the preference store.
	 * 
	 * @param id
	 *            the id of the perspective to find
	 * @return IMemento a memento containing the perspective description
	 * 
	 * @throws WorkbenchException
	 * @throws IOException
	 */
	public IMemento getCustomPersp(String id) throws WorkbenchException,
			IOException {
		Reader reader = null;

		IPreferenceStore store = WorkbenchPlugin.getDefault()
				.getPreferenceStore();
		String xmlString = store.getString(id + PERSP);
		if (xmlString != null && xmlString.length() != 0) { // defined in store
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
		PerspectiveRegistryReader reader = new PerspectiveRegistryReader(this);
		reader.readPerspectives(Platform.getExtensionRegistry());
	}

	/**
	 * @see IPerspectiveRegistry#setDefaultPerspective(String)
	 */
	public void setDefaultPerspective(String id) {
		IPerspectiveDescriptor desc = findPerspectiveWithId(id);
		if (desc != null) {
			defaultPerspID = id;
			PrefUtil.getAPIPreferenceStore().setValue(
					IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID, id);
		}
	}

	/**
	 * Return <code>true</code> if a label is valid. This checks only the
	 * given label in isolation. It does not check whether the given label is
	 * used by any existing perspectives.
	 * 
	 * @param label
	 *            the label to test
	 * @return whether the label is valid
	 */
	public boolean validateLabel(String label) {
		label = label.trim();
		if (label.length() <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * Verifies the id of the default perspective. If the default perspective is
	 * invalid use the workbench default.
	 */
	private void verifyDefaultPerspective() {
		// Step 1: Try current defPerspId value.
		IPerspectiveDescriptor desc = null;
		if (defaultPerspID != null) {
			desc = findPerspectiveWithId(defaultPerspID);
		}
		if (desc != null) {
			return;
		}

		// Step 2. Read default value.
		String str = PrefUtil.getAPIPreferenceStore().getString(
				IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID);
		if (str != null && str.length() > 0) {
			desc = findPerspectiveWithId(str);
		}
		if (desc != null) {
			defaultPerspID = str;
			return;
		}

		// Step 3. Use application-specific default
		defaultPerspID = Workbench.getInstance().getDefaultPerspectiveId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveRegistry#clonePerspective(java.lang.String,
	 *      java.lang.String, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public IPerspectiveDescriptor clonePerspective(String id, String label,
			IPerspectiveDescriptor originalDescriptor) {

		// Check for invalid labels
		if (label == null || !(label.trim().length() > 0)) {
			throw new IllegalArgumentException();
		}

		// Check for duplicates
		IPerspectiveDescriptor desc = findPerspectiveWithId(id);
		if (desc != null) {
			throw new IllegalArgumentException();
		}

		// Create descriptor.
		desc = new PerspectiveDescriptor(id, label,
				(PerspectiveDescriptor) originalDescriptor);
		add((PerspectiveDescriptor) desc);
		return desc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveRegistry#revertPerspective(org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void revertPerspective(IPerspectiveDescriptor perspToRevert) {
		PerspectiveDescriptor desc = (PerspectiveDescriptor) perspToRevert;
		perspToRemove.add(desc.getId());
		desc.revertToPredefined();
	}

	/**
	 * Dispose the receiver.
	 */
	public void dispose() {
		PlatformUI.getWorkbench().getExtensionTracker().unregisterHandler(this);
		WorkbenchPlugin.getDefault().getPreferenceStore()
				.removePropertyChangeListener(preferenceListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#removeExtension(org.eclipse.core.runtime.IExtension,
	 *      java.lang.Object[])
	 */
	public void removeExtension(IExtension source, Object[] objects) {
		for (int i = 0; i < objects.length; i++) {
			if (objects[i] instanceof PerspectiveDescriptor) {
				// close the perspective in all windows
				IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
						.getWorkbenchWindows();
				PerspectiveDescriptor desc = (PerspectiveDescriptor) objects[i];
				for (int w = 0; w < windows.length; ++w) {
					IWorkbenchWindow window = windows[w];
					IWorkbenchPage[] pages = window.getPages();
					for (int p = 0; p < pages.length; ++p) {
						WorkbenchPage page = (WorkbenchPage) pages[p];
						ClosePerspectiveHandler.closePerspective(page, page
								.findPerspective(desc));
					}
				}

				// ((Workbench)PlatformUI.getWorkbench()).getPerspectiveHistory().removeItem(desc);

				internalDeletePerspective(desc);
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#addExtension(org.eclipse.core.runtime.dynamicHelpers.IExtensionTracker,
	 *      org.eclipse.core.runtime.IExtension)
	 */
	public void addExtension(IExtensionTracker tracker,
			IExtension addedExtension) {
		IConfigurationElement[] addedElements = addedExtension
				.getConfigurationElements();
		for (int i = 0; i < addedElements.length; i++) {
			PerspectiveRegistryReader reader = new PerspectiveRegistryReader(
					this);
			reader.readElement(addedElements[i]);
		}
	}
}
