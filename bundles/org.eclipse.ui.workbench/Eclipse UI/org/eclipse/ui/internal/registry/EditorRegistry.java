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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.editorsupport.ComponentSupport;
import org.eclipse.ui.internal.misc.ExternalProgramImageDescriptor;
import org.eclipse.ui.internal.misc.ProgramImageDescriptor;

/**
 * Provides access to the collection of defined editors for resource types.
 */
public class EditorRegistry implements IEditorRegistry {

	/*
	 * Cached images - these include images from registered editors (via
	 * plugins) and others hence this table is not one to one with the mappings
	 * table. It is in fact a superset of the keys one would find in
	 * typeEditorMappings
	 */
	private Map extensionImages = new HashMap();

	/*
	 * Vector of EditorDescriptor - all the editors loaded from plugin files.
	 * The list is kept in order to be able to show in the editor selection
	 * dialog of the resource associations page.
	 */
	private List sortedEditorsFromPlugins = new ArrayList();

	// Map of EditorDescriptor - map editor id to editor.
	private Map mapIDtoEditor = initialIdToEditorMap(10);

	// Map of FileEditorMapping (extension to FileEditorMapping)
	private EditorMap typeEditorMappings;

	// List for prop changed listeners.
	private ListenerList propChangeListeners = new ListenerList();

	/*
	 * Compares the labels from two IEditorDescriptor objects
	 */
	private static final Comparator comparer = new Comparator() {
		private Collator collator = Collator.getInstance();

		public int compare(Object arg0, Object arg1) {
			String s1 = ((IEditorDescriptor) arg0).getLabel();
			String s2 = ((IEditorDescriptor) arg1).getLabel();
			return collator.compare(s1, s2);
		}
	};

	/**
	 * Return an instance of the receiver.
	 */
	public EditorRegistry() {
		super();
		initializeFromStorage();
	}

	/**
	 * Add an editor for the given extensions with the specified (possibly null)
	 * extended type. The editor is being registered from a plugin
	 * 
	 * @param editor
	 *            The description of the editor (as obtained from the plugin
	 *            file and built by the registry reader)
	 * @param extensions
	 *            Collection of file extensions the editor applies to
	 * @param filenames
	 *            Collection of filenames the editor applies to
	 * @param bDefault
	 *            Indicates whether the editor should be made the default editor
	 *            and hence appear first inside a FileEditorMapping
	 * 
	 * This method is not API and should not be called outside the workbench
	 * code.
	 */
	public void addEditorFromPlugin(EditorDescriptor editor, List extensions,
			List filenames, boolean bDefault) {

		// record it in our quick reference list
		sortedEditorsFromPlugins.add(editor);

		// add it to the table of mappings
		Iterator enum = extensions.iterator();
		while (enum.hasNext()) {
			String fileExtension = (String) enum.next();

			if (fileExtension != null && fileExtension.length() > 0) {
				FileEditorMapping mapping = getMappingFor("*." + fileExtension); //$NON-NLS-1$
				if (mapping == null) { // no mapping for that extension
					mapping = new FileEditorMapping(fileExtension);
					typeEditorMappings.putDefault(mappingKeyFor(mapping),
							mapping);
				}
				mapping.addEditor(editor);
				if (bDefault)
					mapping.setDefaultEditor(editor);
			}
		}

		// add it to the table of mappings
		enum = filenames.iterator();
		while (enum.hasNext()) {
			String filename = (String) enum.next();

			if (filename != null && filename.length() > 0) {
				FileEditorMapping mapping = getMappingFor(filename);
				if (mapping == null) { // no mapping for that extension
					String name;
					String extension;
					int index = filename.indexOf('.');
					if (index < 0) {
						name = filename;
						extension = ""; //$NON-NLS-1$
					} else {
						name = filename.substring(0, index);
						extension = filename.substring(index + 1);
					}
					mapping = new FileEditorMapping(name, extension);
					typeEditorMappings.putDefault(mappingKeyFor(mapping),
							mapping);
				}
				mapping.addEditor(editor);
				if (bDefault)
					mapping.setDefaultEditor(editor);
			}
		}

		// Update editor map.
		mapIDtoEditor.put(editor.getId(), editor);
	}
	/**
	 * Add external editors to the editor mapping.
	 */
	private void addExternalEditorsToEditorMap() {
		IEditorDescriptor desc = null;

		// Add registered editors (may include external editors).
		FileEditorMapping maps[] = typeEditorMappings.allMappings();
		for (int i = 0; i < maps.length; i++) {
			FileEditorMapping map = maps[i];
			IEditorDescriptor[] descArray = map.getEditors();
			for (int n = 0; n < descArray.length; n++) {
				desc = descArray[n];
				mapIDtoEditor.put(desc.getId(), desc);
			}
		}
	}
	/*
	 * (non-Javadoc) Method declared on IEditorRegistry.
	 */
	public void addPropertyListener(IPropertyListener l) {
		propChangeListeners.add(l);
	}
	/*
	 * (non-Javadoc) Method declared on IEditorRegistry.
	 */
	public IEditorDescriptor findEditor(String id) {
		return (IEditorDescriptor) mapIDtoEditor.get(id);
	}
	/**
	 * Fires a property changed event.
	 */
	private void firePropertyChange(final int type) {
		Object[] array = propChangeListeners.getListeners();
		for (int nX = 0; nX < array.length; nX++) {
			final IPropertyListener l = (IPropertyListener) array[nX];
			Platform.run(new SafeRunnable() {
				public void run() {
					l.propertyChanged(EditorRegistry.this, type);
				}
			});
		}
	}
	/*
	 * (non-Javadoc) Method declared on IEditorRegistry.
	 * 
	 * @deprecated
	 */
	public IEditorDescriptor getDefaultEditor() {
		// the default editor will always be the system external editor
		// this should never return null
		return findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
	}
	/*
	 * (non-Javadoc) Method declared on IEditorRegistry.
	 */
	public IEditorDescriptor getDefaultEditor(String filename) {
		FileEditorMapping[] mapping = getMappingForFilename(filename);
		IEditorDescriptor desc = null;
		if (mapping[0] != null)
			desc = mapping[0].getDefaultEditor();
		if (desc == null && mapping[1] != null)
			desc = mapping[1].getDefaultEditor();

		if (WorkbenchActivityHelper.filterItem(desc))
			return null;

		return desc;
	}
	/**
	 * Returns the default file image.
	 */
	private ImageDescriptor getDefaultImage() {
		// @issue what should be the default image?
		return WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FILE);
	}
	/*
	 * (non-Javadoc) Method declared on IEditorRegistry.
	 */
	public IEditorDescriptor[] getEditors(String filename) {
		IEditorDescriptor[] editors = new IEditorDescriptor[0];
		IEditorDescriptor[] filenameEditors = editors;
		IEditorDescriptor[] extensionEditors = editors;

		FileEditorMapping mapping[] = getMappingForFilename(filename);
		if (mapping[0] != null) {
			editors = mapping[0].getEditors();
			if (editors != null)
				filenameEditors = editors;
		}
		if (mapping[1] != null) {
			editors = mapping[1].getEditors();
			if (editors != null)
				extensionEditors = editors;
		}

		editors = new IEditorDescriptor[filenameEditors.length
				+ extensionEditors.length];
		System
				.arraycopy(filenameEditors, 0, editors, 0,
						filenameEditors.length);
		System.arraycopy(extensionEditors, 0, editors, filenameEditors.length,
				extensionEditors.length);

		ArrayList list = new ArrayList(Arrays.asList(editors));
		ArrayList filtered = new ArrayList();
		for (Iterator i = list.iterator(); i.hasNext();) {
			Object next = i.next();
			if (WorkbenchActivityHelper.filterItem(next))
				continue;
			filtered.add(next);
		}
		editors = (IEditorDescriptor[]) filtered
				.toArray(new IEditorDescriptor[filtered.size()]);

		return editors;
	}
	/*
	 * (non-Javadoc) Method declared on IEditorRegistry.
	 */
	public IFileEditorMapping[] getFileEditorMappings() {
		FileEditorMapping[] array = typeEditorMappings.allMappings();
		final Collator collator = Collator.getInstance();
		Arrays.sort(array, new Comparator() {
			public int compare(Object o1, Object o2) {
				String s1 = ((FileEditorMapping) o1).getLabel();
				String s2 = ((FileEditorMapping) o2).getLabel();
				return collator.compare(s1, s2);
			}
		});
		return array;
	}
	/*
	 * (non-Javadoc) Method declared on IEditorRegistry.
	 */
	public ImageDescriptor getImageDescriptor(String filename) {
		if (filename == null)
			return getDefaultImage();

		// Lookup in the cache first...
		String key = mappingKeyFor(filename);
		ImageDescriptor anImage = (ImageDescriptor) extensionImages.get(key);
		if (anImage != null)
			return anImage;

		// See if we have a mapping for the filename or extension
		FileEditorMapping[] mapping = getMappingForFilename(filename);
		for (int i = 0; i < 2; i++) {
			if (mapping[i] != null) {
				// Lookup in the cache first...
				String mappingKey = mappingKeyFor(mapping[i]);
				ImageDescriptor mappingImage = (ImageDescriptor) extensionImages
						.get(key);
				if (mappingImage != null)
					return mappingImage;
				// Create it and cache it
				IEditorDescriptor editor = mapping[i].getDefaultEditor();
				if (editor != null) {
					mappingImage = editor.getImageDescriptor();
					extensionImages.put(mappingKey, mappingImage);
					return mappingImage;
				}
			}
		}

		// Nothing - time to look externally for the icon
		anImage = getSystemExternalEditorImageDescriptor(filename);
		if (anImage == null)
			anImage = getDefaultImage();
		//	for dynamic UI - comment out the next line
		//extensionImages.put(key, anImage);
		return anImage;
	}
	/**
	 * Find the file editor mapping for the type. Returns null if not found.
	 */
	private FileEditorMapping getMappingFor(String type) {
		if (type == null)
			return null;
		String key = mappingKeyFor(type);
		return typeEditorMappings.get(key);
	}
	/**
	 * Find the file editor mappings for the given filename.
	 * 
	 * Return an array of two FileEditorMapping items, where the first mapping
	 * is for the entire filename, and the second mapping is for the filename's
	 * extension only. These items can be null if no mapping exist on the
	 * filename and/or filename's extension.
	 */
	private FileEditorMapping[] getMappingForFilename(String filename) {
		FileEditorMapping[] mapping = new FileEditorMapping[2];

		// Lookup on entire filename
		mapping[0] = getMappingFor(filename);

		// Lookup on filename's extension
		int index = filename.lastIndexOf('.');
		if (index > -1) {
			String extension = filename.substring(index);
			mapping[1] = getMappingFor("*" + extension); //$NON-NLS-1$
		}

		return mapping;
	}
	/*
	 * WARNING! The image described by each editor descriptor is *not* known by
	 * the workbench's graphic registry. Therefore clients must take care to
	 * ensure that if they access any of the images held by these editors that
	 * they also dispose them
	 */

	public IEditorDescriptor[] getSortedEditorsFromOS() {
		List externalEditors = new ArrayList();
		Program[] programs = Program.getPrograms();

		for (int i = 0; i < programs.length; i++) {
			//1FPLRL2: ITPUI:WINNT - NOTEPAD editor cannot be launched
			//Some entries start with %SystemRoot%
			//For such cases just use the file name as they are generally
			//in directories which are on the path
			/*
			 * if (fileName.charAt(0) == '%') { fileName = name + ".exe"; }
			 */

			EditorDescriptor editor = new EditorDescriptor();
			editor.setOpenMode(EditorDescriptor.OPEN_EXTERNAL);
			editor.setProgram(programs[i]);

			// determine the program icon this editor would need (do not let it
			// be cached in the workbench registry)
			ImageDescriptor desc = new ExternalProgramImageDescriptor(
					programs[i]);
			editor.setImageDescriptor(desc);
			externalEditors.add(editor);
		}

		Object[] tempArray = sortEditors(externalEditors);
		IEditorDescriptor[] array = new IEditorDescriptor[externalEditors
				.size()];
		for (int i = 0; i < tempArray.length; i++) {
			array[i] = (IEditorDescriptor) tempArray[i];
		}
		return array;
	}
	/**
	 *  
	 */
	public IEditorDescriptor[] getSortedEditorsFromPlugins() {
		IEditorDescriptor[] array = new IEditorDescriptor[sortedEditorsFromPlugins
				.size()];
		sortedEditorsFromPlugins.toArray(array);
		return array;

	}
	/**
	 * Answer an intial id to editor map.
	 */
	private HashMap initialIdToEditorMap(int initialSize) {
		HashMap map = new HashMap(initialSize);
		addSystemEditors(map);
		return map;
	}
	private void addSystemEditors(HashMap map) {
		// there will always be a system external editor descriptor
		EditorDescriptor editor = new EditorDescriptor();
		editor.setID(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
		editor.setName(WorkbenchMessages
				.getString("SystemEditorDescription.name")); //$NON-NLS-1$
		editor.setOpenMode(EditorDescriptor.OPEN_EXTERNAL);
		// @issue we need a real icon for this editor?
		map.put(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID, editor);

		// there may be a system in-place editor if supported by platform
		if (ComponentSupport.inPlaceEditorSupported()) {
			editor = new EditorDescriptor();
			editor.setID(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);
			editor.setName(WorkbenchMessages
					.getString("SystemInPlaceDescription.name")); //$NON-NLS-1$
			editor.setOpenMode(EditorDescriptor.OPEN_INPLACE);
			// @issue we need a real icon for this editor?
			map.put(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID, editor);
		}
	}
	private void initializeFromStorage() {
		typeEditorMappings = new EditorMap();
		extensionImages = new HashMap();

		//Get editors from the registry
		EditorRegistryReader registryReader = new EditorRegistryReader();
		registryReader.addEditors(true, this);
		sortInternalEditors();
		rebuildInternalEditorMap();

		IWorkbench workbench = PlatformUI.getWorkbench();
		IPreferenceStore store = workbench.getPreferenceStore();
		String defaultEditors = store
				.getString(IPreferenceConstants.DEFAULT_EDITORS);
		String chachedDefaultEditors = store
				.getString(IPreferenceConstants.DEFAULT_EDITORS_CACHE);

		//If defaults has changed load it afterwards so it overrides the users
		// associations.
		if (defaultEditors == null
				|| defaultEditors.equals(chachedDefaultEditors)) {
			setProductDefaults(defaultEditors);
			loadAssociations(); //get saved earlier state
		} else {
			loadAssociations(); //get saved earlier state
			setProductDefaults(defaultEditors);
			store.putValue(IPreferenceConstants.DEFAULT_EDITORS_CACHE,
					defaultEditors);
		}
		addExternalEditorsToEditorMap();
	}
	/**
	 * Set the default editors according to the preference store which can be
	 * overwritten in the file properties.ini.
	 */
	private void setProductDefaults(String defaultEditors) {
		if (defaultEditors == null || defaultEditors.length() == 0)
			return;

		StringTokenizer extEditors = new StringTokenizer(defaultEditors,
				new Character(IPreferenceConstants.SEPARATOR).toString());
		while (extEditors.hasMoreTokens()) {
			String extEditor = extEditors.nextToken().trim();
			int index = extEditor.indexOf(':');
			if (extEditor.length() < 3 || index <= 0
					|| index >= (extEditor.length() - 1)) {
				//Extension and id must have at least one char.
				WorkbenchPlugin
						.log("Error setting default editor. Could not parse '" + extEditor + "'. Default editors should be specified as '*.ext1:editorId1;*.ext2:editorId2'"); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
			String ext = extEditor.substring(0, index).trim();
			String editorId = extEditor.substring(index + 1).trim();
			FileEditorMapping mapping = getMappingFor(ext);
			if (mapping == null) {
				WorkbenchPlugin
						.log("Error setting default editor. Could not find mapping for '" + ext + "'."); //$NON-NLS-1$ //$NON-NLS-2$
				continue;
			}
			EditorDescriptor editor = (EditorDescriptor) findEditor(editorId);
			if (editor == null) {
				WorkbenchPlugin
						.log("Error setting default editor. Could not find editor: '" + editorId + "'."); //$NON-NLS-1$ //$NON-NLS-2$
				continue;
			}
			mapping.setDefaultEditor(editor);
		}
	}
	/**
	 * Read the editors defined in the preferences store.
	 * 
	 * @param editorTable
	 *            Editor table to store the editor definitions.
	 * @return true if the table is built succesfully.
	 */
	private boolean readEditors(Map editorTable) {
		//Get the workbench plugin's working directory
		WorkbenchPlugin workbenchPlugin = WorkbenchPlugin.getDefault();
		IPath workbenchStatePath = workbenchPlugin.getStateLocation();
		IPreferenceStore store = WorkbenchPlugin.getDefault()
				.getPreferenceStore();
		Reader reader = null;
		try {
			// Get the editors defined in the preferences store
			String xmlString = store.getString(IPreferenceConstants.EDITORS);
			if (xmlString == null || xmlString.length() == 0) {
				FileInputStream stream = new FileInputStream(workbenchStatePath
						.append(IWorkbenchConstants.EDITOR_FILE_NAME)
						.toOSString());
				reader = new BufferedReader(new InputStreamReader(stream,
						"utf-8")); //$NON-NLS-1$
			} else {
				reader = new StringReader(xmlString);
			}
			XMLMemento memento = XMLMemento.createReadRoot(reader);
			EditorDescriptor editor;
			IMemento[] edMementos = memento
					.getChildren(IWorkbenchConstants.TAG_DESCRIPTOR);
			// Get the editors and validate each one
			for (int i = 0; i < edMementos.length; i++) {
				editor = new EditorDescriptor();
				boolean valid = editor.loadValues(edMementos[i]);
				if (!valid) {
					continue;
				}
				if (editor.getPluginID() != null) {
					//If the editor is from a plugin we use its ID to look it
					// up in the mapping of editors we
					//have obtained from plugins. This allows us to verify that
					// the editor is still valid
					//and allows us to get the editor description from the
					// mapping table which has
					//a valid config element field.
					EditorDescriptor validEditorDescritor = (EditorDescriptor) mapIDtoEditor
							.get(editor.getId());
					if (validEditorDescritor != null) {
						editorTable.put(validEditorDescritor.getId(),
								validEditorDescritor);
					}
				} else { //This is either from a program or a user defined
					// editor
					ImageDescriptor descriptor;
					if (editor.getProgram() == null)
						descriptor = new ProgramImageDescriptor(editor
								.getFileName(), 0);
					else
						descriptor = new ExternalProgramImageDescriptor(editor
								.getProgram());
					editor.setImageDescriptor(descriptor);
					editorTable.put(editor.getId(), editor);
				}
			}
		} catch (IOException e) {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException ex) {
				e.printStackTrace();
			}
			//Ignore this as the workbench may not yet have saved any state
			return false;
		} catch (WorkbenchException e) {
			ErrorDialog.openError((Shell) null, WorkbenchMessages
					.getString("EditorRegistry.errorTitle"), //$NON-NLS-1$
					WorkbenchMessages.getString("EditorRegistry.errorMessage"), //$NON-NLS-1$
					e.getStatus());
			return false;
		}

		return true;
	}

	/**
	 * Read the file types and associate them to their defined editor(s).
	 * 
	 * @param editorTable
	 *            The editor table containing the defined editors.
	 * @param reader
	 *            Reader containing the preferences content for the resources.
	 * 
	 * @throws WorkbenchException
	 */
	public void readResources(Map editorTable, Reader reader)
			throws WorkbenchException {
		XMLMemento memento = XMLMemento.createReadRoot(reader);
		IMemento[] extMementos = memento
				.getChildren(IWorkbenchConstants.TAG_INFO);
		for (int i = 0; i < extMementos.length; i++) {
			String name = extMementos[i]
					.getString(IWorkbenchConstants.TAG_NAME);
			if (name == null)
				name = "*"; //$NON-NLS-1$
			String extension = extMementos[i]
					.getString(IWorkbenchConstants.TAG_EXTENSION);
			IMemento[] idMementos = extMementos[i]
					.getChildren(IWorkbenchConstants.TAG_EDITOR);
			String[] editorIDs = new String[idMementos.length];
			for (int j = 0; j < idMementos.length; j++) {
				editorIDs[j] = idMementos[j]
						.getString(IWorkbenchConstants.TAG_ID);
			}
			idMementos = extMementos[i]
					.getChildren(IWorkbenchConstants.TAG_DELETED_EDITOR);
			String[] deletedEditorIDs = new String[idMementos.length];
			for (int j = 0; j < idMementos.length; j++) {
				deletedEditorIDs[j] = idMementos[j]
						.getString(IWorkbenchConstants.TAG_ID);
			}
			FileEditorMapping mapping = getMappingFor(name + "." + extension); //$NON-NLS-1$
			if (mapping == null) {
				mapping = new FileEditorMapping(name, extension);
			}
			List editors = new ArrayList();
			for (int j = 0; j < editorIDs.length; j++) {
				if (editorIDs[j] != null) {
					EditorDescriptor editor = (EditorDescriptor) editorTable
							.get(editorIDs[j]);
					if (editor != null) {
						editors.add(editor);
					}
				}
			}
			List deletedEditors = new ArrayList();
			for (int j = 0; j < deletedEditorIDs.length; j++) {
				if (deletedEditorIDs[j] != null) {
					EditorDescriptor editor = (EditorDescriptor) editorTable
							.get(deletedEditorIDs[j]);
					if (editor != null) {
						deletedEditors.add(editor);
					}
				}
			}
			// Add any new editors that have already been read from the registry
			// which were not deleted.
			IEditorDescriptor[] editorsArray = mapping.getEditors();
			for (int j = 0; j < editorsArray.length; j++) {
				if (!contains(editors, editorsArray[j])
						&& !deletedEditors.contains(editorsArray[j])) {
					editors.add(editorsArray[j]);
				}
			}
			// Map the editor(s) to the file type
			mapping.setEditorsList(editors);
			mapping.setDeletedEditorsList(deletedEditors);
			typeEditorMappings.put(mappingKeyFor(mapping), mapping);
		}
	}
	/**
	 * Determine if the editors list contains the editor descriptor.
	 * 
	 * @param editorsArray
	 * 			The list of editors.
	 * @param editorDescriptor
	 * 			The editor descriptor.
	 * @return true if the editors list contains the editor descriptor.
	 */
	private boolean contains(List editorsArray, IEditorDescriptor editorDescriptor)
	{
		IEditorDescriptor currentEditorDescriptor = null;
		Iterator i = editorsArray.iterator();
		while(i.hasNext())
		{
			currentEditorDescriptor  = (IEditorDescriptor) i.next();
			if(currentEditorDescriptor.getId().equals(editorDescriptor.getId()))
				return true;
		}
		return false;
	
	}
	
	/**
	 * Creates the reader for the resources preferences defined in the
	 * preference store.
	 * 
	 * @param editorTable
	 *            The editor table containing the defined editors.
	 * @return true if the resources are read succesfully.
	 */
	private boolean readResources(Map editorTable) {
		//Get the workbench plugin's working directory
		WorkbenchPlugin workbenchPlugin = WorkbenchPlugin.getDefault();
		IPath workbenchStatePath = workbenchPlugin.getStateLocation();
		IPreferenceStore store = WorkbenchPlugin.getDefault()
				.getPreferenceStore();
		Reader reader = null;
		try {
			// Get the resource types
			String xmlString = store.getString(IPreferenceConstants.RESOURCES);
			if (xmlString == null || xmlString.length() == 0) {
				FileInputStream stream = new FileInputStream(workbenchStatePath
						.append(IWorkbenchConstants.RESOURCE_TYPE_FILE_NAME)
						.toOSString());
				reader = new BufferedReader(new InputStreamReader(stream,
						"utf-8")); //$NON-NLS-1$
			} else {
				reader = new StringReader(xmlString);
			}
			// Read the defined resources into the table
			readResources(editorTable, reader);
		} catch (IOException e) {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			MessageDialog.openError((Shell) null, WorkbenchMessages
					.getString("EditorRegistry.errorTitle"), //$NON-NLS-1$
					WorkbenchMessages.getString("EditorRegistry.errorMessage")); //$NON-NLS-1$
			return false;
		} catch (WorkbenchException e) {
			ErrorDialog.openError((Shell) null, WorkbenchMessages
					.getString("EditorRegistry.errorTitle"), //$NON-NLS-1$
					WorkbenchMessages.getString("EditorRegistry.errorMessage"), //$NON-NLS-1$
					e.getStatus());
			return false;
		}
		return true;

	}
	/**
	 * Load the serialized resource associations Return true if the operation
	 * was successful, false otherwise
	 */
	private boolean loadAssociations() {
		Map editorTable = new HashMap();
		if (!readEditors(editorTable)) {
			return false;
		}
		return readResources(editorTable);
	}
	/*
	 *  
	 */
	private String mappingKeyFor(String type) {
		// keep everyting lower case for case-sensitive platforms
		return type.toLowerCase();
	}
	/**
	 * Return a key that combines the file's name and extension of the given
	 * mapping
	 */
	private String mappingKeyFor(FileEditorMapping mapping) {
		return mappingKeyFor(mapping.getName()
				+ (mapping.getExtension().length() == 0
						? "" : "." + mapping.getExtension())); //$NON-NLS-1$ //$NON-NLS-2$
	}
	/**
	 * Rebuild the editor map
	 */
	private void rebuildEditorMap() {
		rebuildInternalEditorMap();
		addExternalEditorsToEditorMap();
	}
	/**
	 * Rebuild the internal editor mapping.
	 */
	private void rebuildInternalEditorMap() {
		Iterator enum = null;
		IEditorDescriptor desc = null;

		// Allocate a new map.
		mapIDtoEditor = initialIdToEditorMap(mapIDtoEditor.size());

		// Add plugin editors.
		enum = sortedEditorsFromPlugins.iterator();
		while (enum.hasNext()) {
			desc = (IEditorDescriptor) enum.next();
			mapIDtoEditor.put(desc.getId(), desc);
		}
	}
	/*
	 * (non-Javadoc) Method declared on IEditorRegistry.
	 */
	public void removePropertyListener(IPropertyListener l) {
		propChangeListeners.remove(l);
	}
	/**
	 * Save the registry to the filesystem by serializing the current resource
	 * associations.
	 */
	public void saveAssociations() {
		//Save the resource type descriptions
		List editors = new ArrayList();
		IPreferenceStore store = WorkbenchPlugin.getDefault()
				.getPreferenceStore();

		XMLMemento memento = XMLMemento
				.createWriteRoot(IWorkbenchConstants.TAG_EDITORS);
		FileEditorMapping maps[] = typeEditorMappings.userMappings();
		for (int mapsIndex = 0; mapsIndex < maps.length; mapsIndex++) {
			FileEditorMapping type = maps[mapsIndex];
			IMemento editorMemento = memento
					.createChild(IWorkbenchConstants.TAG_INFO);
			editorMemento.putString(IWorkbenchConstants.TAG_NAME, type
					.getName());
			editorMemento.putString(IWorkbenchConstants.TAG_EXTENSION, type
					.getExtension());
			IEditorDescriptor[] editorArray = type.getEditors();
			for (int i = 0; i < editorArray.length; i++) {
				EditorDescriptor editor = (EditorDescriptor) editorArray[i];
				if (!editors.contains(editor)) {
					editors.add(editor);
				}
				IMemento idMemento = editorMemento
						.createChild(IWorkbenchConstants.TAG_EDITOR);
				idMemento.putString(IWorkbenchConstants.TAG_ID, editorArray[i]
						.getId());
			}
			editorArray = type.getDeletedEditors();
			for (int i = 0; i < editorArray.length; i++) {
				EditorDescriptor editor = (EditorDescriptor) editorArray[i];
				if (!editors.contains(editor)) {
					editors.add(editor);
				}
				IMemento idMemento = editorMemento
						.createChild(IWorkbenchConstants.TAG_DELETED_EDITOR);
				idMemento.putString(IWorkbenchConstants.TAG_ID, editorArray[i]
						.getId());
			}
		}
		Writer writer = null;
		try {
			writer = new StringWriter();
			memento.save(writer);
			writer.close();
			store.setValue(IPreferenceConstants.RESOURCES, writer.toString());
		} catch (IOException e) {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			MessageDialog.openError((Shell) null, "Saving Problems", //$NON-NLS-1$
					"Unable to save resource associations."); //$NON-NLS-1$
			return;
		}

		memento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_EDITORS);
		Iterator enum = editors.iterator();
		while (enum.hasNext()) {
			EditorDescriptor editor = (EditorDescriptor) enum.next();
			IMemento editorMemento = memento
					.createChild(IWorkbenchConstants.TAG_DESCRIPTOR);
			editor.saveValues(editorMemento);
		}
		writer = null;
		try {
			writer = new StringWriter();
			memento.save(writer);
			writer.close();
			store.setValue(IPreferenceConstants.EDITORS, writer.toString());
		} catch (IOException e) {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			MessageDialog.openError((Shell) null,
					"Error", "Unable to save resource associations."); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
	}
	/**
	 * Set the collection of FileEditorMappings. The given collection is
	 * converted into the internal hash table for faster lookup Each mapping
	 * goes from an extension to the collection of editors that work on it.
	 */
	public void setFileEditorMappings(FileEditorMapping[] newResourceTypes) {
		typeEditorMappings = new EditorMap();
		for (int i = 0; i < newResourceTypes.length; i++) {
			FileEditorMapping mapping = newResourceTypes[i];
			typeEditorMappings.put(mappingKeyFor(mapping), mapping);
		}
		extensionImages = new HashMap();
		rebuildEditorMap();
		firePropertyChange(PROP_CONTENTS);
	}
	/*
	 * (non-Javadoc) Method declared on IEditorRegistry.
	 */
	public void setDefaultEditor(String fileName, String editorId) {
		EditorDescriptor desc = (EditorDescriptor) findEditor(editorId);
		FileEditorMapping[] mapping = getMappingForFilename(fileName);
		if (mapping[0] != null)
			mapping[0].setDefaultEditor(desc);
		if (mapping[1] != null)
			mapping[1].setDefaultEditor(desc);
	}
	/**
	 * Alphabetically sort the internal editors
	 */
	private Object[] sortEditors(List unsortedList) {
		Object[] array = new Object[unsortedList.size()];
		unsortedList.toArray(array);

		Collections.sort(Arrays.asList(array), comparer);
		return array;
	}
	/**
	 * Alphabetically sort the internal editors
	 */
	private void sortInternalEditors() {
		Object[] array = sortEditors(sortedEditorsFromPlugins);
		sortedEditorsFromPlugins = new ArrayList();
		for (int i = 0; i < array.length; i++) {
			sortedEditorsFromPlugins.add(array[i]);
		}
	}

	/*
	 * Map of FileEditorMapping (extension to FileEditorMapping) Uses two
	 * java.util.HashMap: one keeps the default which are set by the plugins and
	 * the other keeps the changes made by the user through the preference page.
	 */
	private static class EditorMap {
		HashMap defaultMap = new HashMap();
		HashMap map = new HashMap();

		public void putDefault(String key, FileEditorMapping value) {
			defaultMap.put(key, value);
		}
		public void put(String key, FileEditorMapping value) {
			Object result = defaultMap.get(key);
			if (value.equals(result))
				map.remove(key);
			else
				map.put(key, value);
		}
		public FileEditorMapping get(String key) {
			Object result = map.get(key);
			if (result == null)
				result = defaultMap.get(key);
			return (FileEditorMapping) result;
		}
		public FileEditorMapping[] allMappings() {
			HashMap merge = (HashMap) defaultMap.clone();
			merge.putAll(map);
			Collection values = merge.values();
			FileEditorMapping result[] = new FileEditorMapping[values.size()];
			return (FileEditorMapping[]) values.toArray(result);
		}
		public FileEditorMapping[] userMappings() {
			Collection values = map.values();
			FileEditorMapping result[] = new FileEditorMapping[values.size()];
			return (FileEditorMapping[]) values.toArray(result);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorRegistry#isSystemInPlaceEditorAvailable(String)
	 */
	public boolean isSystemInPlaceEditorAvailable(String filename) {
		return ComponentSupport.inPlaceEditorAvailable(filename);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorRegistry#isSystemExternalEditorAvailable(String)
	 */
	public boolean isSystemExternalEditorAvailable(String filename) {
		int nDot = filename.lastIndexOf('.');
		if (nDot >= 0) {
			String strName = filename.substring(nDot);
			return Program.findProgram(strName) != null;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorRegistry#getSystemExternalEditorImageDescriptor(java.lang.String)
	 */
	public ImageDescriptor getSystemExternalEditorImageDescriptor(
			String filename) {
		Program externalProgram = null;
		int extensionIndex = filename.lastIndexOf('.');
		if (extensionIndex >= 0) {
			externalProgram = Program.findProgram(filename
					.substring(extensionIndex));
		}
		if (externalProgram == null) {
			return null;
		} else {
			return new ExternalProgramImageDescriptor(externalProgram);
		}
	}
	//	for dynamic UI
	public void remove(String id) {
		IEditorDescriptor desc = findEditor(id);
		if (id == null)
			return;
		sortedEditorsFromPlugins.remove(desc);
		mapIDtoEditor.remove(id);
		removeEditorFromMapping(typeEditorMappings.defaultMap, id);
		removeEditorFromMapping(typeEditorMappings.map, id);
	}

	//	for dynamic UI
	private void removeEditorFromMapping(HashMap map, String id) {
		Iterator iter = map.values().iterator();
		FileEditorMapping mapping;
		IEditorDescriptor[] editors;
		while (iter.hasNext()) {
			mapping = (FileEditorMapping) iter.next();
			editors = mapping.getEditors();
			for (int i = 0; i < editors.length; i++)
				if (editors[i].getId().equals(id)) {
					mapping.removeEditor((EditorDescriptor) editors[i]);
					break;
				}
			if (editors.length <= 0) {
				map.remove(mapping);
				break;
			}
		}
	}
}
