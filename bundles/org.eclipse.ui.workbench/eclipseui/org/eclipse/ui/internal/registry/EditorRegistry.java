/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jan-Hendrik Diederich, Bredex GmbH - bug 201052
 *     Carsten Pfeiffer, Gebit Solutions GmbH - bug 259536
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *     Mickael Istria, Red Hat Inc. - [91965] Add ct/editor mapping in user space
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 527069
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.editorsupport.ComponentSupport;
import org.eclipse.ui.internal.misc.ExternalProgramImageDescriptor;
import org.eclipse.ui.internal.misc.ProgramImageDescriptor;
import org.eclipse.ui.internal.misc.StatusUtil;

/**
 * Provides access to the collection of defined editors for resource types.
 */
public class EditorRegistry extends EventManager implements IEditorRegistry, IExtensionChangeHandler {

	private static final IEditorDescriptor[] EMPTY = new IEditorDescriptor[0];

	class RelatedRegistry {

		/**
		 * Return the objects related to the type.
		 *
		 * @return the objects related to the type
		 */
		public IEditorDescriptor[] getRelatedObjects(IContentType type) {
			LinkedHashSet<IEditorDescriptor> editors = new LinkedHashSet<>();
			if (contentTypeToEditorMappingsFromPlugins.containsKey(type)) {
				editors.addAll(Arrays.asList(contentTypeToEditorMappingsFromPlugins.get(type)));
			}
			if (contentTypeToEditorMappingsFromUser.containsKey(type)) {
				editors.addAll(contentTypeToEditorMappingsFromUser.get(type));
			}
			if (editors.isEmpty()) {
				return EMPTY;
			}
			return (IEditorDescriptor[]) WorkbenchActivityHelper
					.restrictArray(editors.toArray(new IEditorDescriptor[editors.size()]));
		}

		/**
		 * Return the objects related to the filename
		 *
		 * @return the objects related to the filename
		 */
		public IEditorDescriptor[] getRelatedObjects(String fileName) {
			IFileEditorMapping mapping = getMappingFor(fileName);
			if (mapping == null) {
				return EMPTY;
			}

			return (IEditorDescriptor[]) WorkbenchActivityHelper.restrictArray(mapping.getEditors());
		}

	}

	private Map<IContentType, IEditorDescriptor[]> contentTypeToEditorMappingsFromPlugins = new HashMap<>();
	private Map<IContentType, LinkedHashSet<IEditorDescriptor>> contentTypeToEditorMappingsFromUser = new HashMap<>();

	/**
	 * Cached images - these include images from registered editors (via plugins)
	 * and others hence this table is not one to one with the mappings table. It is
	 * in fact a superset of the keys one would find in typeEditorMappings
	 */
	private Map<Object, ImageDescriptor> extensionImages = new HashMap<>();

	/**
	 * Vector of EditorDescriptor - all the editors loaded from plugin files. The
	 * list is kept in order to be able to show in the editor selection dialog of
	 * the resource associations page. This list is sorted based on the human
	 * readable label of the editor descriptor.
	 *
	 * @see #comparer
	 */
	private List<IEditorDescriptor> sortedEditorsFromPlugins = new ArrayList<>();

	/** cache of OS editors **/
	private IEditorDescriptor[] sortedEditorsFromOS;
	final Object sortedEditorsFromOSSynchronizer = new Object();

	// Map of EditorDescriptor - map editor id to editor.
	private Map<String, IEditorDescriptor> mapIDtoInternalEditor = initialIdToEditorMap(10);
	// Map of EditorDescriptor - map editor id to OS editor.
	private Map<String, IEditorDescriptor> mapIDtoOSEditors;

	// Map of FileEditorMapping (extension to FileEditorMapping)
	private EditorMap typeEditorMappings;

	/*
	 * Compares the labels from two IEditorDescriptor objects
	 */
	private static final Comparator<IEditorDescriptor> comparer = new Comparator<>() {
		private Collator collator = Collator.getInstance();

		@Override
		public int compare(IEditorDescriptor arg0, IEditorDescriptor arg1) {
			String s1 = arg0.getLabel();
			String s2 = arg1.getLabel();
			return collator.compare(s1, s2);
		}
	};

	private RelatedRegistry relatedRegistry;

	private final IContentTypeManager contentTypeManager;

	public static final String EMPTY_EDITOR_ID = "org.eclipse.ui.internal.emptyEditorTab"; //$NON-NLS-1$

	/**
	 * Return an instance of the receiver. Adds listeners into the extension
	 * registry for dynamic UI purposes.
	 */
	public EditorRegistry(IContentTypeManager contentTypeManager) {
		super();
		this.contentTypeManager = contentTypeManager;
		initializeFromStorage();
		IExtensionTracker tracker = PlatformUI.getWorkbench().getExtensionTracker();
		tracker.registerHandler(this, ExtensionTracker.createExtensionPointFilter(getExtensionPointFilter()));
		relatedRegistry = new RelatedRegistry();
		contentTypeManager.addContentTypeChangeListener(event -> {
			if (contentTypeManager.getContentType(event.getContentType().getId()) == null) {
				contentTypeToEditorMappingsFromUser.remove(event.getContentType());
				saveAssociations();
			}
		});
	}

	/**
	 * Add an editor for the given extensions with the specified (possibly null)
	 * extended type. The editor is being registered from a plugin
	 *
	 * @param editor            The description of the editor (as obtained from the
	 *                          plugin file and built by the registry reader)
	 * @param extensions        Collection of file extensions the editor applies to
	 * @param filenames         Collection of filenames the editor applies to
	 * @param bDefault          Indicates whether the editor should be made the
	 *                          default editor and hence appear first inside a
	 *                          FileEditorMapping
	 *
	 *                          This method is not API and should not be called
	 *                          outside the workbench code.
	 */
	public void addEditorFromPlugin(EditorDescriptor editor, List<String> extensions, List<String> filenames,
			List<String> contentTypeVector, boolean bDefault) {

		PlatformUI.getWorkbench().getExtensionTracker().registerObject(
				editor.getConfigurationElement().getDeclaringExtension(), editor, IExtensionTracker.REF_WEAK);
		// record it in our quick reference list
		sortedEditorsFromPlugins.add(editor);

		// add it to the table of mappings
		for (String fileExtension : extensions) {
			if (fileExtension != null && fileExtension.length() > 0) {
				FileEditorMapping mapping = getMappingFor("*." + fileExtension); //$NON-NLS-1$
				if (mapping == null) { // no mapping for that extension
					mapping = new FileEditorMapping(fileExtension);
					typeEditorMappings.putDefault(mappingKeyFor(mapping), mapping);
				}
				mapping.addEditor(editor);
				if (bDefault) {
					mapping.setDefaultEditor(editor);
				}
			}
		}

		// add it to the table of mappings
		for (String filename : filenames) {
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
					typeEditorMappings.putDefault(mappingKeyFor(mapping), mapping);
				}
				mapping.addEditor(editor);
				if (bDefault) {
					mapping.setDefaultEditor(editor);
				}
			}
		}

		for (String contentTypeId : contentTypeVector) {
			if (contentTypeId != null && contentTypeId.length() > 0) {
				IContentType contentType = contentTypeManager.getContentType(contentTypeId);
				if (contentType != null) {
					addContentTypeBindingFromPlugin(contentType, editor, bDefault);
				}
			}
		}

		// Update editor map.
		mapIDtoInternalEditor.put(editor.getId(), editor);
	}

	public void addContentTypeBindingFromPlugin(IContentType contentType, IEditorDescriptor editor, boolean bDefault) {
		IEditorDescriptor[] editorArray = contentTypeToEditorMappingsFromPlugins.get(contentType);
		if (editorArray == null) {
			editorArray = new IEditorDescriptor[] { editor };
			contentTypeToEditorMappingsFromPlugins.put(contentType, editorArray);
		} else {
			IEditorDescriptor[] newArray = new IEditorDescriptor[editorArray.length + 1];
			if (bDefault) { // default editors go to the front of the line
				newArray[0] = editor;
				System.arraycopy(editorArray, 0, newArray, 1, editorArray.length);
			} else {
				newArray[editorArray.length] = editor;
				System.arraycopy(editorArray, 0, newArray, 0, editorArray.length);
			}
			contentTypeToEditorMappingsFromPlugins.put(contentType, newArray);
		}
	}

	/**
	 * Add external editors to the editor mapping.
	 */
	private void addExternalEditorsToEditorMap() {
		// Add registered editors (may include external editors).
		for (FileEditorMapping map : typeEditorMappings.allMappings()) {
			IEditorDescriptor[] descArray = map.getEditors();
			for (IEditorDescriptor desc : descArray) {
				mapIDtoInternalEditor.put(desc.getId(), desc);
			}
		}

		// reset external editors from OS
		synchronized (this) {
			mapIDtoOSEditors = null;
		}
	}

	private synchronized IEditorDescriptor getOSEditor(String id) {
		if (id == null || id.isEmpty()) {
			// can not find external editor anyway.
			// for example @see org.eclipse.ui.part.MultiPageEditorSite#getId
			return null;
		}
		if (IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID.equals(id)) {
			// on systems which have ComponentSupport.inPlaceEditorSupported()==false
			// @see #addSystemEditors
			return null;
		}

		if (mapIDtoOSEditors == null) {
			mapIDtoOSEditors = new HashMap<>();
			IEditorDescriptor[] sortedEditorsFromOS = getSortedEditorsFromOS();
			for (IEditorDescriptor desc : sortedEditorsFromOS) {
				mapIDtoOSEditors.put(desc.getId(), desc); // ignore duplicates
			}
			IEditorDescriptor editor = mapIDtoOSEditors.get(id);
			if (editor == null && WorkbenchPlugin.getDefault().isDebugging()) {
				WorkbenchPlugin.getDefault().getLog()
						.log(StatusUtil.newStatus(IStatus.WARNING, "Editor descriptor for id '" + id + "' not found.", //$NON-NLS-1$ //$NON-NLS-2$
								new Exception("IEditorRegistry.findEditor(String) called for unknown id"))); //$NON-NLS-1$
			}
		}
		return mapIDtoOSEditors.get(id);
	}

	@Override
	public void addPropertyListener(IPropertyListener l) {
		addListenerObject(l);
	}

	@Override
	public IEditorDescriptor findEditor(String id) {
		IEditorDescriptor desc = mapIDtoInternalEditor.get(id);
		if (desc == null) {
			desc = getOSEditor(id);
		}
		if (WorkbenchActivityHelper.restrictUseOf(desc)) {
			return null;
		}
		return desc;
	}

	/**
	 * Fires a property changed event to all registered listeners.
	 *
	 * @param type the type of event
	 * @see IEditorRegistry#PROP_CONTENTS
	 */
	private void firePropertyChange(final int type) {
		for (Object listener : getListeners()) {
			final IPropertyListener propertyListener = (IPropertyListener) listener;
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() {
					propertyListener.propertyChanged(EditorRegistry.this, type);
				}
			});
		}
	}

	@Override
	public IEditorDescriptor getDefaultEditor() {
		// the default editor will always be the system external editor
		// this should never return null
		return findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
	}

	@Override
	public IEditorDescriptor getDefaultEditor(String filename) {
		IEditorDescriptor defaultEditor = getDefaultEditor(filename, guessAtContentType(filename));
		if (defaultEditor != null) {
			return defaultEditor;
		}

		IContentType[] contentTypes = contentTypeManager.findContentTypesFor(filename);
		for (IContentType contentType : contentTypes) {
			IEditorDescriptor editor = getDefaultEditor(filename, contentType);
			if (editor != null) {
				return editor;
			}
		}
		return null;
	}

	/**
	 * Return the (approximated) content type for a file with the given name.
	 *
	 * @param filename the filename
	 * @return the content type or <code>null</code> if it could not be determined
	 * @since 3.1
	 */
	private IContentType guessAtContentType(String filename) {
		return contentTypeManager.findContentTypeFor(filename);
	}

	/**
	 * Returns the default file image descriptor.
	 *
	 * @return the image descriptor
	 */
	private ImageDescriptor getDefaultImage() {
		// @issue what should be the default image?
		return WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FILE);
	}

	@Override
	public IEditorDescriptor[] getEditors(String filename) {
		return getEditors(filename, guessAtContentType(filename));
	}

	@Override
	public IFileEditorMapping[] getFileEditorMappings() {
		FileEditorMapping[] array = typeEditorMappings.allMappings();
		final Collator collator = Collator.getInstance();
		Arrays.sort(array, (o1, o2) -> {
			String s1 = o1.getLabel();
			String s2 = o2.getLabel();
			return collator.compare(s1, s2);
		});
		return array;
	}

	@Override
	public ImageDescriptor getImageDescriptor(String filename) {
		return getImageDescriptor(filename, guessAtContentType(filename));
	}

	/**
	 * Find the file editor mapping for the file extension. Returns
	 * <code>null</code> if not found.
	 *
	 * @param ext the file extension
	 * @return the mapping, or <code>null</code>
	 */
	private FileEditorMapping getMappingFor(String ext) {
		if (ext == null) {
			return null;
		}
		String key = mappingKeyFor(ext);
		return typeEditorMappings.get(key);
	}

	/**
	 * Find the file editor mappings for the given filename.
	 * <p>
	 * Return an array of two FileEditorMapping items, where the first mapping is
	 * for the entire filename, and the second mapping is for the filename's
	 * extension only. These items can be null if no mapping exist on the filename
	 * and/or filename's extension.
	 * </p>
	 *
	 * @param filename the filename
	 * @return the mappings
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

	/**
	 * Return the editor descriptors pulled from the OS.
	 * <p>
	 * WARNING! The image described by each editor descriptor is *not* known by the
	 * workbench's graphic registry. Therefore clients must take care to ensure that
	 * if they access any of the images held by these editors that they also dispose
	 * them
	 * </p>
	 *
	 * @return the editor descriptors
	 */
	public IEditorDescriptor[] getSortedEditorsFromOS() {
		synchronized (sortedEditorsFromOSSynchronizer) {
			if (sortedEditorsFromOS == null) {
				loadEditorsFromOS();
			}
			return sortedEditorsFromOS;
		}
	}

	/**
	 * refreshes cache.
	 *
	 * @see #getSortedEditorsFromOS
	 */
	// public just in case someone wants to reload
	public void loadEditorsFromOS() {
		synchronized (sortedEditorsFromOSSynchronizer) {
			sortedEditorsFromOS = getStaticSortedEditorsFromOS();
		}
	}

	private static IEditorDescriptor[] getStaticSortedEditorsFromOS() {
		List<IEditorDescriptor> externalEditors = new ArrayList<>();

		for (Program program : Program.getPrograms()) {
			// 1FPLRL2: ITPUI:WINNT - NOTEPAD editor cannot be launched
			// Some entries start with %SystemRoot%
			// For such cases just use the file name as they are generally
			// in directories which are on the path
			/*
			 * if (fileName.charAt(0) == '%') { fileName = name + ".exe"; }
			 */

			EditorDescriptor editor = new EditorDescriptor();
			editor.setOpenMode(EditorDescriptor.OPEN_EXTERNAL);
			editor.setProgram(program);

			// determine the program icon this editor would need (do not let it
			// be cached in the workbench registry)
			ImageDescriptor desc = new ExternalProgramImageDescriptor(program);
			editor.setImageDescriptor(desc);
			externalEditors.add(editor);
		}

		Object[] tempArray = sortEditors(externalEditors);
		IEditorDescriptor[] array = new IEditorDescriptor[externalEditors.size()];
		for (int i = 0; i < tempArray.length; i++) {
			array[i] = (IEditorDescriptor) tempArray[i];
		}
		return array;
	}

	/**
	 * Return the editors loaded from plugins.
	 *
	 * @return the sorted array of editors declared in plugins
	 */
	public IEditorDescriptor[] getSortedEditorsFromPlugins() {
		// see #comparer
		Collection<IEditorDescriptor> descs = WorkbenchActivityHelper.restrictCollection(sortedEditorsFromPlugins,
				new ArrayList<>());
		return descs.toArray(new IEditorDescriptor[descs.size()]);
	}

	/**
	 * Answer an intial id to editor map. This will create a new map and populate it
	 * with the default system editors.
	 *
	 * @param initialSize the initial size of the map
	 * @return the new map
	 */
	private Map<String, IEditorDescriptor> initialIdToEditorMap(int initialSize) {
		Map<String, IEditorDescriptor> map = new HashMap<>(initialSize);
		addSystemEditors(map);
		return map;
	}

	/**
	 * Add the system editors to the provided map. This will always add an editor
	 * with an id of {@link #SYSTEM_EXTERNAL_EDITOR_ID} and may also add an editor
	 * with id of {@link #SYSTEM_INPLACE_EDITOR_ID} if the system configuration
	 * supports it.
	 *
	 * @param map the map to augment
	 */
	private void addSystemEditors(Map<String, IEditorDescriptor> map) {
		// there will always be a system external editor descriptor
		EditorDescriptor editor = new EditorDescriptor();
		editor.setID(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
		editor.setName(WorkbenchMessages.SystemEditorDescription_name);
		editor.setOpenMode(EditorDescriptor.OPEN_EXTERNAL);
		// @issue we need a real icon for this editor?
		map.put(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID, editor);

		// there may be a system in-place editor if supported by platform
		if (ComponentSupport.inPlaceEditorSupported()) {
			editor = new EditorDescriptor();
			editor.setID(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);
			editor.setName(WorkbenchMessages.SystemInPlaceDescription_name);
			editor.setOpenMode(EditorDescriptor.OPEN_INPLACE);
			// @issue we need a real icon for this editor?
			map.put(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID, editor);
		}

		EditorDescriptor emptyEditorDescriptor = new EditorDescriptor();
		emptyEditorDescriptor.setID(EMPTY_EDITOR_ID);
		emptyEditorDescriptor.setName("(Empty)"); //$NON-NLS-1$
		emptyEditorDescriptor
				.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJ_ELEMENT));
		map.put(EMPTY_EDITOR_ID, emptyEditorDescriptor);
	}

	/**
	 * Initialize the registry state from plugin declarations and preference
	 * overrides.
	 */
	private void initializeFromStorage() {
		typeEditorMappings = new EditorMap();
		extensionImages = new HashMap<>();

		// Get editors from the registry
		EditorRegistryReader registryReader = new EditorRegistryReader();
		registryReader.addEditors(this);
		sortInternalEditors();
		rebuildInternalEditorMap();

		IPreferenceStore store = PlatformUI.getPreferenceStore();
		String defaultEditors = store.getString(IPreferenceConstants.DEFAULT_EDITORS);
		String chachedDefaultEditors = store.getString(IPreferenceConstants.DEFAULT_EDITORS_CACHE);

		// If defaults has changed load it afterwards so it overrides the users
		// associations.
		if (defaultEditors == null || defaultEditors.equals(chachedDefaultEditors)) {
			setProductDefaults(defaultEditors);
			loadAssociations(); // get saved earlier state
		} else {
			loadAssociations(); // get saved earlier state
			setProductDefaults(defaultEditors);
			store.putValue(IPreferenceConstants.DEFAULT_EDITORS_CACHE, defaultEditors);
		}
		addExternalEditorsToEditorMap();
	}

	/**
	 * Set the default editors according to the preference store which can be
	 * overwritten in the file properties.ini. In the form:
	 * <p>
	 * <code>ext1:id1;ext2:id2;...</code>
	 * </p>
	 *
	 * @param defaultEditors the default editors to set
	 */
	private void setProductDefaults(String defaultEditors) {
		if (defaultEditors == null || defaultEditors.isEmpty()) {
			return;
		}

		StringTokenizer extEditors = new StringTokenizer(defaultEditors,
				Character.toString(IPreferenceConstants.SEPARATOR));
		while (extEditors.hasMoreTokens()) {
			String extEditor = extEditors.nextToken().trim();
			int index = extEditor.indexOf(':');
			if (extEditor.length() < 3 || index <= 0 || index >= (extEditor.length() - 1)) {
				// Extension and id must have at least one char.
				WorkbenchPlugin.log("Error setting default editor. Could not parse '" + extEditor //$NON-NLS-1$
						+ "'. Default editors should be specified as '*.ext1:editorId1;*.ext2:editorId2'"); //$NON-NLS-1$
				return;
			}
			String ext = extEditor.substring(0, index).trim();
			String editorId = extEditor.substring(index + 1).trim();
			FileEditorMapping mapping = getMappingFor(ext);
			if (mapping == null) {
				WorkbenchPlugin.log("Error setting default editor. Could not find mapping for '" + ext + "'."); //$NON-NLS-1$ //$NON-NLS-2$
				continue;
			}
			IEditorDescriptor editor = findEditor(editorId);
			if (editor == null) {
				WorkbenchPlugin.log("Error setting default editor. Could not find editor: '" + editorId + "'."); //$NON-NLS-1$ //$NON-NLS-2$
				continue;
			}
			mapping.setDefaultEditor(editor);
		}
	}

	/**
	 * Read the editors defined in the preferences store.
	 *
	 * @param editorTable Editor table to store the editor definitions.
	 * @return true if the table is built successfully.
	 */
	private boolean readEditors(Map<String, IEditorDescriptor> editorTable) {
		// Get the workbench plugin's working directory
		IPath workbenchStatePath = WorkbenchPlugin.getDefault().getDataLocation();
		if (workbenchStatePath == null) {
			return false;
		}
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		// Get the editors defined in the preferences store
		String xmlString = store.getString(IPreferenceConstants.EDITORS);
		try (Reader reader = createReader(xmlString, workbenchStatePath, IWorkbenchConstants.EDITOR_FILE_NAME)) {
			XMLMemento memento = XMLMemento.createReadRoot(reader);
			// Get the editors and validate each one
			for (IMemento childMemento : memento.getChildren(IWorkbenchConstants.TAG_DESCRIPTOR)) {
				EditorDescriptor editor = new EditorDescriptor();
				boolean valid = editor.loadValues(childMemento);
				if (!valid) {
					continue;
				}
				if (isSystem(editor.getId())) {
					// bug 502514: check if there is internal editor
					// descriptor (they are always recreated via
					// addSystemEditors(Map<String, IEditorDescriptor>))
					lookupEditorFromTable(editorTable, editor);
					continue;
				}
				if (editor.getPluginID() != null) {
					// If the editor is from a plugin we use its ID to look it
					// up in the mapping of editors we
					// have obtained from plugins. This allows us to verify that
					// the editor is still valid
					// and allows us to get the editor description from the
					// mapping table which has
					// a valid config element field.
					lookupEditorFromTable(editorTable, editor);
					continue;
				}
				// This is either from a program or a user defined editor
				ImageDescriptor descriptor;
				if (editor.getProgram() == null) {
					String fileName = editor.getFileName();
					if (fileName == null) {
						String error = "Both editor program and path are null for descriptor id: "; //$NON-NLS-1$
						error += editor.getId() + " with name: " + editor.getLabel(); //$NON-NLS-1$
						WorkbenchPlugin.log(error, new IllegalStateException());
						continue;
					}
					descriptor = new ProgramImageDescriptor(fileName, 0);
				} else {
					descriptor = new ExternalProgramImageDescriptor(editor.getProgram());
				}
				editor.setImageDescriptor(descriptor);
				editorTable.put(editor.getId(), editor);
			}
		} catch (IOException e) {
			// Ignore this as the workbench may not yet have saved any state
			return false;
		} catch (WorkbenchException e) {
			ErrorDialog.openError((Shell) null, WorkbenchMessages.EditorRegistry_errorTitle,
					WorkbenchMessages.EditorRegistry_errorMessage, e.getStatus());
			return false;
		}
		return true;
	}

	/**
	 * @param id descriptor id
	 * @return true if the id is one of the system internal id's:
	 *         {@link IEditorRegistry#SYSTEM_EXTERNAL_EDITOR_ID} or
	 *         {@link IEditorRegistry#SYSTEM_INPLACE_EDITOR_ID}
	 */
	private static boolean isSystem(String id) {
		return IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID.equals(id)
				|| IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID.equals(id);
	}

	private void lookupEditorFromTable(Map<String, IEditorDescriptor> editorTable, EditorDescriptor editor) {
		IEditorDescriptor validEditorDescritor = mapIDtoInternalEditor.get(editor.getId());
		if (validEditorDescritor != null) {
			editorTable.put(validEditorDescritor.getId(), validEditorDescritor);
		}
	}

	/**
	 * Read the file types and associate them to their defined editor(s).
	 *
	 * @param editorTable The editor table containing the defined editors.
	 * @param reader      Reader containing the preferences content for the
	 *                    resources.
	 */
	public void readResources(Map<String, IEditorDescriptor> editorTable, Reader reader) throws WorkbenchException {
		XMLMemento memento = XMLMemento.createReadRoot(reader);

		for (IMemento childMemento : memento.getChildren(IWorkbenchConstants.TAG_INFO)) {
			List<IEditorDescriptor> editors = getEditorDescriptors(
					childMemento.getChildren(IWorkbenchConstants.TAG_EDITOR), editorTable);
			editors.forEach(editor -> {
				if (!mapIDtoInternalEditor.containsKey(editor.getId())) {
					mapIDtoInternalEditor.put(editor.getId(), editor);
				}
			});
			String contentTypeId = childMemento.getString(IWorkbenchConstants.TAG_CONTENT_TYPE);
			if (contentTypeId != null) {
				IContentType contentType = contentTypeManager.getContentType(contentTypeId);
				if (contentType != null) {
					contentTypeToEditorMappingsFromUser.put(contentType, new LinkedHashSet<>(editors));
				}
			} else {
				String name = childMemento.getString(IWorkbenchConstants.TAG_NAME);
				if (name == null) {
					name = "*"; //$NON-NLS-1$
				}
				String extension = childMemento.getString(IWorkbenchConstants.TAG_EXTENSION);
				String key = name;
				if (extension != null && extension.length() > 0) {
					key = key + "." + extension; //$NON-NLS-1$
				}
				FileEditorMapping mapping = getMappingFor(key);
				if (mapping == null) {
					mapping = new FileEditorMapping(name, extension);
				}

				List<IEditorDescriptor> deletedEditors = getEditorDescriptors(
						childMemento.getChildren(IWorkbenchConstants.TAG_DELETED_EDITOR), editorTable);

				List<IEditorDescriptor> defaultEditors = null;
				defaultEditors = getEditorDescriptors(childMemento.getChildren(IWorkbenchConstants.TAG_DEFAULT_EDITOR),
						editorTable);

				// Add any new editors that have already been read from the registry
				// which were not deleted.
				for (IEditorDescriptor descriptor : mapping.getEditors()) {
					if (descriptor != null && !contains(editors, descriptor) && !deletedEditors.contains(descriptor)) {
						editors.add(descriptor);
					}
				}
				// Map the editor(s) to the file type
				mapping.setEditorsList(editors);
				mapping.setDeletedEditorsList(deletedEditors);
				mapping.setDefaultEditors(defaultEditors);
				typeEditorMappings.put(mappingKeyFor(mapping), mapping);
			}
		}
	}

	private List<IEditorDescriptor> getEditorDescriptors(IMemento[] children,
			Map<String, IEditorDescriptor> editorTable) {
		if (children == null || children.length == 0) {
			return new ArrayList<>(0); // need a mutable list
		}
		List<IEditorDescriptor> res = new ArrayList<>(children.length);
		for (IMemento child : children) {
			String editorId = child.getString(IWorkbenchConstants.TAG_ID);
			if (editorId != null && editorTable.containsKey(editorId)) {
				res.add(editorTable.get(editorId));
			}
		}
		return res;
	}

	/**
	 * Determine if the editors list contains the editor descriptor.
	 *
	 * @param editors          The list of editors
	 * @param editorDescriptor The editor descriptor
	 * @return <code>true</code> if the editors list contains the editor descriptor
	 */
	private boolean contains(List<IEditorDescriptor> editors, IEditorDescriptor editorDescriptor) {
		for (IEditorDescriptor currentEditorDescriptor : editors) {
			if (currentEditorDescriptor.getId().equals(editorDescriptor.getId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates the reader for the resources preferences defined in the preference
	 * store.
	 *
	 * @param editorTable The editor table containing the defined editors.
	 * @return true if the resources are read successfully.
	 */
	private boolean readResources(Map<String, IEditorDescriptor> editorTable) {
		// Get the workbench plugin's working directory
		IPath workbenchStatePath = WorkbenchPlugin.getDefault().getDataLocation();
		// XXX: nobody cares about this return value
		if (workbenchStatePath == null) {
			return false;
		}
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		// Get the resource types
		String xmlString = store.getString(IPreferenceConstants.RESOURCES);

		try (Reader reader = createReader(xmlString, workbenchStatePath, IWorkbenchConstants.RESOURCE_TYPE_FILE_NAME)) {
			// Read the defined resources into the table
			readResources(editorTable, reader);
		} catch (IOException e) {
			MessageDialog.openError((Shell) null, WorkbenchMessages.EditorRegistry_errorTitle,
					WorkbenchMessages.EditorRegistry_errorMessage);
			return false;
		} catch (WorkbenchException e) {
			ErrorDialog.openError((Shell) null, WorkbenchMessages.EditorRegistry_errorTitle,
					WorkbenchMessages.EditorRegistry_errorMessage, e.getStatus());
			return false;
		}
		return true;

	}

	private Reader createReader(String xmlString, IPath workbenchStatePath, String fileName)
			throws FileNotFoundException {
		if (xmlString == null || xmlString.isEmpty()) {
			return new BufferedReader(new InputStreamReader(
					new FileInputStream(
							workbenchStatePath.append(fileName).toOSString()),
					StandardCharsets.UTF_8));
		}
		return new StringReader(xmlString);
	}

	/**
	 * Load the serialized resource associations Return true if the operation was
	 * successful, false otherwise
	 */
	private boolean loadAssociations() {
		Map<String, IEditorDescriptor> editorTable = new HashMap<>();
		if (!readEditors(editorTable)) {
			return false;
		}
		return readResources(editorTable);
	}

	/**
	 * Return a friendly version of the given key suitable for use in the editor
	 * map.
	 */
	private String mappingKeyFor(String type) {
		// keep everything lower case for case-sensitive platforms
		return type.toLowerCase();
	}

	/**
	 * Return a key that combines the file's name and extension of the given mapping
	 *
	 * @param mapping the mapping to generate a key for
	 */
	private String mappingKeyFor(FileEditorMapping mapping) {
		return mappingKeyFor(
				mapping.getName() + (mapping.getExtension().isEmpty() ? "" : "." + mapping.getExtension())); //$NON-NLS-1$ //$NON-NLS-2$
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
		// Allocate a new map.
		mapIDtoInternalEditor = initialIdToEditorMap(mapIDtoInternalEditor.size());

		// Add plugin editors.
		for (IEditorDescriptor desc : sortedEditorsFromPlugins) {
			mapIDtoInternalEditor.put(desc.getId(), desc);
		}
	}

	@Override
	public void removePropertyListener(IPropertyListener l) {
		removeListenerObject(l);
	}

	/**
	 * Save the registry to the filesystem by serializing the current resource
	 * associations.
	 */
	public void saveAssociations() {
		// Save the resource type descriptions
		LinkedHashSet<IEditorDescriptor> editors = new LinkedHashSet<>();
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();

		XMLMemento memento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_EDITORS);
		memento.putString(IWorkbenchConstants.TAG_VERSION, "3.1"); //$NON-NLS-1$
		for (FileEditorMapping fileEditorMapping : typeEditorMappings.userMappings()) {
			IMemento editorMemento = memento.createChild(IWorkbenchConstants.TAG_INFO);
			editorMemento.putString(IWorkbenchConstants.TAG_NAME, fileEditorMapping.getName());
			editorMemento.putString(IWorkbenchConstants.TAG_EXTENSION, fileEditorMapping.getExtension());
			IEditorDescriptor[] editorArray = fileEditorMapping.getEditors();
			for (IEditorDescriptor editor : editorArray) {
				if (editor == null) {
					continue;
				}
				editors.add(editor);
				IMemento idMemento = editorMemento.createChild(IWorkbenchConstants.TAG_EDITOR);
				idMemento.putString(IWorkbenchConstants.TAG_ID, editor.getId());
			}
			editorArray = fileEditorMapping.getDeletedEditors();
			for (IEditorDescriptor editor : editorArray) {
				if (editor == null) {
					continue;
				}
				editors.add(editor);
				IMemento idMemento = editorMemento.createChild(IWorkbenchConstants.TAG_DELETED_EDITOR);
				idMemento.putString(IWorkbenchConstants.TAG_ID, editor.getId());
			}
			editorArray = fileEditorMapping.getDeclaredDefaultEditors();
			for (IEditorDescriptor editor : editorArray) {
				if (editor == null) {
					continue;
				}
				editors.add(editor);
				IMemento idMemento = editorMemento.createChild(IWorkbenchConstants.TAG_DEFAULT_EDITOR);
				idMemento.putString(IWorkbenchConstants.TAG_ID, editor.getId());
			}
		}
		for (Entry<IContentType, LinkedHashSet<IEditorDescriptor>> mapping : contentTypeToEditorMappingsFromUser
				.entrySet()) {
			IMemento editorMemento = memento.createChild(IWorkbenchConstants.TAG_INFO);
			editorMemento.putString(IWorkbenchConstants.TAG_CONTENT_TYPE, mapping.getKey().getId());
			for (IEditorDescriptor editor : mapping.getValue()) {
				if (editor == null) {
					continue;
				}
				editors.add(editor);
				IMemento idMemento = editorMemento.createChild(IWorkbenchConstants.TAG_EDITOR);
				idMemento.putString(IWorkbenchConstants.TAG_ID, editor.getId());
			}
		}
		try (Writer writer = new StringWriter()) {
			memento.save(writer);
			writer.close();
			store.setValue(IPreferenceConstants.RESOURCES, writer.toString());
		} catch (IOException e) {
			MessageDialog.openError((Shell) null, "Saving Problems", //$NON-NLS-1$
					"Unable to save resource associations."); //$NON-NLS-1$
			return;
		}

		memento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_EDITORS);
		for (IEditorDescriptor editor : editors) {
			if (isSystem(editor.getId())) {
				// bug 502514: don't persist internal editor descriptors,
				// they are always recreated via addSystemEditors(Map<String,
				// IEditorDescriptor>)
				continue;
			}
			IMemento editorMemento = memento.createChild(IWorkbenchConstants.TAG_DESCRIPTOR);
			((EditorDescriptor) editor).saveValues(editorMemento);
		}
		try (Writer writer = new StringWriter()) {
			memento.save(writer);
			writer.close();
			store.setValue(IPreferenceConstants.EDITORS, writer.toString());
		} catch (IOException e) {
			MessageDialog.openError((Shell) null, "Error", "Unable to save resource associations."); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
	}

	/**
	 * Set the collection of FileEditorMappings. The given collection is converted
	 * into the internal hash table for faster lookup Each mapping goes from an
	 * extension to the collection of editors that work on it. This operation will
	 * rebuild the internal editor mappings.
	 *
	 * @param newResourceTypes te new file editor mappings.
	 */
	public void setFileEditorMappings(FileEditorMapping[] newResourceTypes) {
		typeEditorMappings = new EditorMap();
		for (FileEditorMapping mapping : newResourceTypes) {
			typeEditorMappings.put(mappingKeyFor(mapping), mapping);
		}
		extensionImages = new HashMap<>();
		rebuildEditorMap();
		firePropertyChange(PROP_CONTENTS);
	}

	@Override
	public void setDefaultEditor(String fileName, String editorId) {
		IEditorDescriptor desc = findEditor(editorId);
		setDefaultEditor(fileName, desc);
	}

	public void setDefaultEditor(String fileName, IEditorDescriptor desc) {
		FileEditorMapping[] mapping = getMappingForFilename(fileName);
		if (mapping[0] != null) {
			mapping[0].setDefaultEditor(desc);
		}
		if (mapping[1] != null) {
			mapping[1].setDefaultEditor(desc);
		}
	}

	/**
	 * Alphabetically sort the internal editors.
	 *
	 * @see #comparer
	 */
	private static IEditorDescriptor[] sortEditors(List<IEditorDescriptor> unsortedList) {
		IEditorDescriptor[] array = new IEditorDescriptor[unsortedList.size()];
		unsortedList.toArray(array);

		Arrays.asList(array).sort(comparer);
		return array;
	}

	/**
	 * Alphabetically sort the internal editors.
	 *
	 * @see #comparer
	 */
	private void sortInternalEditors() {
		IEditorDescriptor[] array = sortEditors(sortedEditorsFromPlugins);
		sortedEditorsFromPlugins = new ArrayList<>();
		sortedEditorsFromPlugins.addAll(Arrays.asList(array));
	}

	/**
	 * Map of FileEditorMapping (extension to FileEditorMapping) Uses two
	 * java.util.HashMap: one keeps the default which are set by the plugins and the
	 * other keeps the changes made by the user through the preference page.
	 */
	private static class EditorMap {
		HashMap<String, FileEditorMapping> defaultMap = new HashMap<>();

		HashMap<String, FileEditorMapping> map = new HashMap<>();

		/**
		 * Put a default mapping into the editor map.
		 *
		 * @param key   the key to set
		 * @param value the value to associate
		 */
		public void putDefault(String key, FileEditorMapping value) {
			defaultMap.put(key, value);
		}

		/**
		 * Put a mapping into the user editor map.
		 *
		 * @param key   the key to set
		 * @param value the value to associate
		 */
		public void put(String key, FileEditorMapping value) {
			Object result = defaultMap.get(key);
			if (value.equals(result)) {
				map.remove(key);
			} else {
				map.put(key, value);
			}
		}

		/**
		 * Return the mapping associated to the key. First searches user map, and then
		 * falls back to the default map if there is no match. May return
		 * <code>null</code>
		 *
		 * @param key the key to search for
		 * @return the mapping associated to the key or <code>null</code>
		 */
		public FileEditorMapping get(String key) {
			Object result = map.get(key);
			if (result == null) {
				result = defaultMap.get(key);
			}
			return (FileEditorMapping) result;
		}

		/**
		 * Return all mappings. This will return default mappings overlayed with user
		 * mappings.
		 *
		 * @return the mappings
		 */
		public FileEditorMapping[] allMappings() {
			HashMap<String, FileEditorMapping> merge = (HashMap<String, FileEditorMapping>) defaultMap.clone();
			merge.putAll(map);
			Collection<FileEditorMapping> values = merge.values();
			FileEditorMapping result[] = new FileEditorMapping[values.size()];
			return values.toArray(result);
		}

		/**
		 * Return all user mappings.
		 *
		 * @return the mappings
		 */
		public FileEditorMapping[] userMappings() {
			Collection<FileEditorMapping> values = map.values();
			FileEditorMapping result[] = new FileEditorMapping[values.size()];
			return values.toArray(result);
		}
	}

	@Override
	public boolean isSystemInPlaceEditorAvailable(String filename) {
		return ComponentSupport.inPlaceEditorAvailable(filename);
	}

	@Override
	public boolean isSystemExternalEditorAvailable(String filename) {
		int nDot = filename.lastIndexOf('.');
		if (nDot >= 0) {
			String strName = filename.substring(nDot);
			return Program.findProgram(strName) != null;
		}
		return false;
	}

	@Override
	public ImageDescriptor getSystemExternalEditorImageDescriptor(String filename) {
		Program externalProgram = null;
		int extensionIndex = filename.lastIndexOf('.');
		if (extensionIndex >= 0) {
			externalProgram = Program.findProgram(filename.substring(extensionIndex));
		}
		if (externalProgram == null) {
			return null;
		}

		return new ExternalProgramImageDescriptor(externalProgram);
	}

	/**
	 * Removes the entry with the value of the editor descriptor from the given map.
	 * If the descriptor is the last descriptor in a given FileEditorMapping then
	 * the mapping is removed from the map.
	 *
	 * @param map  the map to search
	 * @param desc the descriptor value to remove
	 */
	private void removeEditorFromMapping(HashMap<String, FileEditorMapping> map, IEditorDescriptor desc) {
		Iterator<FileEditorMapping> iter = map.values().iterator();
		while (iter.hasNext()) {
			FileEditorMapping mapping = iter.next();
			for (IEditorDescriptor editor : mapping.getUnfilteredEditors()) {
				if (editor == desc) {
					mapping.removeEditor(editor);
					break;
				}
			}
			IEditorDescriptor[] editors = mapping.getUnfilteredEditors();
			if (editors.length == 0) {
				iter.remove();
			}
		}
	}

	@Override
	public void removeExtension(IExtension source, Object[] objects) {
		for (Object object : objects) {
			if (object instanceof IEditorDescriptor) {
				IEditorDescriptor desc = (IEditorDescriptor) object;

				sortedEditorsFromPlugins.remove(desc);
				mapIDtoInternalEditor.values().remove(desc);
				removeEditorFromMapping(typeEditorMappings.defaultMap, desc);
				removeEditorFromMapping(typeEditorMappings.map, desc);
				removeEditorFromContentTypeMappings(contentTypeToEditorMappingsFromPlugins, desc);
			}

		}
	}

	/**
	 * Removes all occurrences of the given editor descriptor from the map of
	 * content types. If the descriptor was the only editor, the whole content type
	 * is removed from the map.
	 */
	private void removeEditorFromContentTypeMappings(Map<IContentType, IEditorDescriptor[]> map,
			IEditorDescriptor desc) {
		for (Iterator<Entry<IContentType, IEditorDescriptor[]>> iter = map.entrySet().iterator(); iter.hasNext();) {
			Entry<IContentType, IEditorDescriptor[]> entry = iter.next();
			IEditorDescriptor[] descriptors = entry.getValue();
			IEditorDescriptor[] newDescriptors = removeDescriptor(descriptors, desc);
			if (descriptors != newDescriptors) {
				if (newDescriptors == null) {
					iter.remove();
				} else {
					entry.setValue(newDescriptors);
				}
			}
		}
	}

	/**
	 * Checks the given IEditorDescriptor for an occurrence of the given descriptor
	 * and returns an array not containing this descriptor. If the result would then
	 * be an empty array, <code>null</code> is returned. If the descriptor is not
	 * contained at all in the given array, it is returned as is.
	 */
	private IEditorDescriptor[] removeDescriptor(IEditorDescriptor[] descriptors, IEditorDescriptor desc) {
		for (int i = 0; i < descriptors.length; i++) {
			if (descriptors[i] == desc) {
				// remove the whole mapping
				if (descriptors.length == 1) {
					return null;
				}

				IEditorDescriptor[] newDescriptors = new IEditorDescriptor[descriptors.length - 1];
				if (i == 0) {
					System.arraycopy(descriptors, 1, newDescriptors, 0, newDescriptors.length);
				} else {
					System.arraycopy(descriptors, 0, newDescriptors, 0, i);
					if (i < newDescriptors.length) {
						System.arraycopy(descriptors, i + 1, newDescriptors, i, newDescriptors.length - i);
					}
				}
				return newDescriptors;
			}
		}

		return descriptors;
	}

	@Override
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		EditorRegistryReader eReader = new EditorRegistryReader();
		IConfigurationElement[] elements = extension.getConfigurationElements();
		for (IConfigurationElement element : elements) {
			String id = element.getAttribute(IWorkbenchConstants.TAG_ID);
			if (id != null && findEditor(id) != null) {
				continue;
			}
			eReader.readElement(this, element);
		}
	}

	private IExtensionPoint getExtensionPointFilter() {
		return Platform.getExtensionRegistry().getExtensionPoint(PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_EDITOR);
	}

	@Override
	public IEditorDescriptor getDefaultEditor(String fileName, IContentType contentType) {
		return getEditorForContentType(fileName, contentType);
	}

	/**
	 * Return the editor for a file with a given content type.
	 *
	 * @param filename    the file name
	 * @param contentType the content type
	 * @return the editor for a file with a given content type
	 * @since 3.1
	 */
	private IEditorDescriptor getEditorForContentType(String filename, IContentType contentType) {
		List<IEditorDescriptor> contentTypeResults = findRelatedObjects(contentType, filename, relatedRegistry);
		if (contentTypeResults.isEmpty()) {
			return null;
		}
		if (contentTypeResults.size() == 1 || contentType == null) {
			return contentTypeResults.get(0);
		}
		return selectDefaultEditor(contentTypeResults, contentType);
	}

	/**
	 * Selects preferred editor from the list, based on user preferences
	 *
	 * @param descriptors non empty list with editor descriptors for given type
	 * @param contentType non null content type
	 * @return user preferred editor for content type, or just the first editor from
	 *         given list if no preferences are set
	 */
	private IEditorDescriptor selectDefaultEditor(List<IEditorDescriptor> descriptors, IContentType contentType) {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		String key = IPreferenceConstants.DEFAULT_EDITOR_FOR_CONTENT_TYPE + contentType.getId();
		String defaultEditorId = store.getString(key);
		IEditorDescriptor descriptor = null;
		if (defaultEditorId != null && !defaultEditorId.isBlank()) {
			descriptor = descriptors.stream().filter(d -> defaultEditorId.equals(d.getId())).findFirst().orElse(null);
		}
		if (descriptor != null) {
			// There is some editor set as default for this content type by user
			return descriptor;
		}
		// Just return the first one as before
		return descriptors.get(0);
	}

	@Override
	public IEditorDescriptor[] getEditors(String fileName, IContentType contentType) {
		return findRelatedObjects(contentType, fileName, relatedRegistry).toArray(IEditorDescriptor[]::new);
	}

	@Override
	public ImageDescriptor getImageDescriptor(String filename, IContentType contentType) {
		if (filename == null) {
			return getDefaultImage();
		}

		if (contentType != null) {
			IEditorDescriptor desc = getEditorForContentType(filename, contentType);
			if (desc != null) {
				ImageDescriptor anImage = extensionImages.get(desc);
				if (anImage != null) {
					return anImage;
				}
				anImage = desc.getImageDescriptor();
				extensionImages.put(desc, anImage);
				return anImage;
			}
		}
		// Lookup in the cache first...
		String key = mappingKeyFor(filename);
		ImageDescriptor anImage = extensionImages.get(key);
		if (anImage != null) {
			return anImage;
		}

		// See if we have a mapping for the filename or extension
		FileEditorMapping[] mapping = getMappingForFilename(filename);
		for (int i = 0; i < 2; i++) {
			if (mapping[i] != null) {
				// Lookup in the cache first...
				String mappingKey = mappingKeyFor(mapping[i]);
				ImageDescriptor mappingImage = extensionImages.get(key);
				if (mappingImage != null) {
					return mappingImage;
				}
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
		if (anImage == null) {
			anImage = getDefaultImage();
		}
		// for dynamic UI - comment out the next line
		// extensionImages.put(key, anImage);
		return anImage;

	}

	/**
	 * Find objects related to the content type.
	 *
	 * This method is temporary and exists only to back us off of the
	 * soon-to-be-removed IContentTypeManager.IRelatedRegistry API.
	 *
	 * @return the related objects
	 */
	private List<IEditorDescriptor> findRelatedObjects(IContentType type, String fileName, RelatedRegistry registry) {
		List<IEditorDescriptor> allRelated = new ArrayList<>();
		List<IEditorDescriptor> nonDefaultFileEditors = new ArrayList<>();

		if (fileName != null) {
			FileEditorMapping mapping = getMappingFor(fileName);
			if (mapping != null) {
				// backwards compatibility - add editors flagged as "default"
				IEditorDescriptor[] related = mapping.getDeclaredDefaultEditors();
				for (IEditorDescriptor editor : related) {
					// we don't want to return duplicates
					if (editor != null && !allRelated.contains(editor)) {
						// if it's not filtered, add it to the list
						if (!WorkbenchActivityHelper.filterItem(editor)) {
							allRelated.add(editor);
						}
					}
				}

				// add all filename editors to the nonDefaultList
				// we'll later try to add them all after content types are resolved
				// duplicates (ie: default editors) will be ignored
				nonDefaultFileEditors.addAll(Arrays.asList(mapping.getEditors()));
			}

			int index = fileName.lastIndexOf('.');
			if (index > -1) {
				String extension = "*" + fileName.substring(index); //$NON-NLS-1$
				mapping = getMappingFor(extension);
				if (mapping != null) {
					IEditorDescriptor[] related = mapping.getDeclaredDefaultEditors();
					for (IEditorDescriptor editor : related) {
						// we don't want to return duplicates
						if (editor != null && !allRelated.contains(editor)) {
							// if it's not filtered, add it to the list
							if (!WorkbenchActivityHelper.filterItem(editor)) {
								allRelated.add(editor);
							}
						}
					}
					nonDefaultFileEditors.addAll(Arrays.asList(mapping.getEditors()));
				}
			}
		}

		if (type != null) {
			// now add any objects directly related to the content type
			IEditorDescriptor[] related = registry.getRelatedObjects(type);
			for (IEditorDescriptor r : related) {
				// we don't want to return duplicates
				if (!allRelated.contains(r)) {
					// if it's not filtered, add it to the list
					if (!WorkbenchActivityHelper.filterItem(r)) {
						allRelated.add(r);
					}
				}
			}

		}

		if (type != null) {
			// now add any indirectly related objects, walking up the content type hierarchy
			while ((type = type.getBaseType()) != null) {
				IEditorDescriptor[] related = registry.getRelatedObjects(type);
				for (IEditorDescriptor r : related) {
					// we don't want to return duplicates
					if (!allRelated.contains(r)) {
						// if it's not filtered, add it to the list
						if (!WorkbenchActivityHelper.filterItem(r)) {
							allRelated.add(r);
						}
					}
				}
			}
		}


		// add all non-default editors to the list
		for (IEditorDescriptor editor : nonDefaultFileEditors) {
			if (editor != null && !allRelated.contains(editor) && !WorkbenchActivityHelper.filterItem(editor)) {
				allRelated.add(editor);
			}
		}

		return allRelated;
	}

	/**
	 * Return the editors bound to this content type, either directly or indirectly.
	 *
	 * @param type the content type to check
	 * @return the editors
	 * @since 3.1
	 *
	 *        TODO: this should be rolled in with the above findRelatedObjects code
	 */
	public IEditorDescriptor[] getEditorsForContentType(IContentType type) {
		List<IEditorDescriptor> allRelated = new ArrayList<>();
		if (type == null) {
			return new IEditorDescriptor[0];
		}

		IEditorDescriptor[] related = relatedRegistry.getRelatedObjects(type);
		for (IEditorDescriptor r : related) {
			// we don't want to return duplicates
			if (!allRelated.contains(r)) {
				// if it's not filtered, add it to the list
				if (!WorkbenchActivityHelper.filterItem(r)) {
					allRelated.add(r);
				}
			}
		}

		// now add any indirectly related objects, walking up the content type hierarchy
		while ((type = type.getBaseType()) != null) {
			related = relatedRegistry.getRelatedObjects(type);
			for (IEditorDescriptor r : related) {
				// we don't want to return duplicates
				if (!allRelated.contains(r)) {
					// if it's not filtered, add it to the list
					if (!WorkbenchActivityHelper.filterItem(r)) {
						allRelated.add(r);
					}
				}
			}
		}

		return allRelated.toArray(new IEditorDescriptor[allRelated.size()]);
	}

	/**
	 * Get file mappings for all defined file types, including those defined by
	 * content type.
	 *
	 * @return the file types
	 * @since 3.1
	 */
	public IFileEditorMapping[] getUnifiedMappings() {
		IFileEditorMapping[] standardMappings = PlatformUI.getWorkbench().getEditorRegistry().getFileEditorMappings();

		List<IFileEditorMapping> allMappings = new ArrayList<>(Arrays.asList(standardMappings));
		// mock-up content type extensions into IFileEditorMappings
		for (IContentType type : contentTypeManager.getAllContentTypes()) {
			for (String extension : type.getFileSpecs(IContentType.FILE_EXTENSION_SPEC)) {
				boolean found = false;
				for (IFileEditorMapping mapping : allMappings) {
					if ("*".equals(mapping.getName()) && extension.equals(mapping.getExtension())) { //$NON-NLS-1$
						found = true;
						break;
					}
				}
				if (!found) {
					MockMapping mockMapping = new MockMapping(type, "*", extension); //$NON-NLS-1$
					allMappings.add(mockMapping);
				}
			}

			for (String wholename : type.getFileSpecs(IContentType.FILE_NAME_SPEC)) {
				int idx = wholename.indexOf('.');
				String name = idx == -1 ? wholename : wholename.substring(0, idx);
				String extension = idx == -1 ? "" : wholename.substring(idx + 1); //$NON-NLS-1$

				boolean found = false;
				for (IFileEditorMapping mapping : allMappings) {
					if (name.equals(mapping.getName()) && extension.equals(mapping.getExtension())) {
						found = true;
						break;
					}
				}
				if (!found) {
					MockMapping mockMapping = new MockMapping(type, name, extension);
					allMappings.add(mockMapping);
				}
			}
		}

		return allMappings.toArray(new IFileEditorMapping[allMappings.size()]);
	}

	/**
	 * @return whether the association between content-type and editor was defined
	 *         in user space
	 */
	public boolean isUserAssociation(IContentType contentType, IEditorDescriptor editor) {
		return this.contentTypeToEditorMappingsFromUser.containsKey(contentType)
				&& this.contentTypeToEditorMappingsFromUser.get(contentType).contains(editor);
	}

	public void removeUserAssociation(IContentType contentType, IEditorDescriptor editor) {
		if (this.contentTypeToEditorMappingsFromUser.containsKey(contentType)) {
			this.contentTypeToEditorMappingsFromUser.get(contentType).remove(editor);
		}
		saveAssociations();
	}

	public void addUserAssociation(IContentType contentType, IEditorDescriptor selectedEditor) {
		if (!this.contentTypeToEditorMappingsFromUser.containsKey(contentType)) {
			this.contentTypeToEditorMappingsFromUser.put(contentType, new LinkedHashSet<>());
		}
		if (!mapIDtoInternalEditor.containsKey(selectedEditor.getId())) {
			mapIDtoInternalEditor.put(selectedEditor.getId(), selectedEditor);
		}
		this.contentTypeToEditorMappingsFromUser.get(contentType).add(selectedEditor);
		saveAssociations();
	}

}

class MockMapping implements IFileEditorMapping {

	private IContentType contentType;
	private String extension;
	private String filename;

	MockMapping(IContentType type, String name, String ext) {
		this.contentType = type;
		this.filename = name;
		this.extension = ext;
	}

	@Override
	public IEditorDescriptor getDefaultEditor() {
		IEditorDescriptor[] candidates = ((EditorRegistry) PlatformUI.getWorkbench().getEditorRegistry())
				.getEditorsForContentType(contentType);
		if (candidates.length == 0 || WorkbenchActivityHelper.restrictUseOf(candidates[0])) {
			return null;
		}
		return candidates[0];
	}

	@Override
	public IEditorDescriptor[] getEditors() {
		IEditorDescriptor[] editorsForContentType = ((EditorRegistry) PlatformUI.getWorkbench().getEditorRegistry())
				.getEditorsForContentType(contentType);
		return (IEditorDescriptor[]) WorkbenchActivityHelper.restrictArray(editorsForContentType);
	}

	@Override
	public IEditorDescriptor[] getDeletedEditors() {
		return new IEditorDescriptor[0];
	}

	@Override
	public String getExtension() {
		return extension;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		IEditorDescriptor editor = getDefaultEditor();
		if (editor == null) {
			return WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FILE);
		}

		return editor.getImageDescriptor();
	}

	@Override
	public String getLabel() {
		return filename + '.' + extension;
	}

	@Override
	public String getName() {
		return filename;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof MockMapping)) {
			return false;
		}

		MockMapping mapping = (MockMapping) obj;
		return this.filename.equals(mapping.filename) && this.extension.equals(mapping.extension)
				&& Arrays.equals(this.getEditors(), mapping.getEditors())
				&& Arrays.equals(this.getDeletedEditors(), mapping.getDeletedEditors());
	}
}
