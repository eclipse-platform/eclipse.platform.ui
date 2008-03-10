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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
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

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
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
import org.eclipse.ui.internal.util.Util;

import com.ibm.icu.text.Collator;

/**
 * Provides access to the collection of defined editors for resource types.
 */
public class EditorRegistry extends EventManager implements IEditorRegistry,
		IExtensionChangeHandler {
	
	private final static IEditorDescriptor [] EMPTY = new IEditorDescriptor[0];
	
	class RelatedRegistry {

		/**
         * Return the objects related to the type.
         * 
         * @param type
         * @return the objects related to the type
		 */
		public IEditorDescriptor[] getRelatedObjects(IContentType type) {			
			IEditorDescriptor[] relatedObjects = (IEditorDescriptor[]) contentTypeToEditorMappings.get(type);
			if (relatedObjects == null) {
				return EMPTY;
			}
			return (IEditorDescriptor[]) WorkbenchActivityHelper.restrictArray(relatedObjects);
		}

		/**
         * Return the objects related to the filename
         * @param fileName
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
	
	private Map contentTypeToEditorMappings = new HashMap();
	
    /*
     * Cached images - these include images from registered editors (via
     * plugins) and others hence this table is not one to one with the mappings
     * table. It is in fact a superset of the keys one would find in
     * typeEditorMappings
     */
    private Map extensionImages = new HashMap();

    /**
     * Vector of EditorDescriptor - all the editors loaded from plugin files.
     * The list is kept in order to be able to show in the editor selection
     * dialog of the resource associations page.  This list is sorted based on the 
     * human readable label of the editor descriptor.
     * 
     * @see #comparer
     */
    private List sortedEditorsFromPlugins = new ArrayList();

    // Map of EditorDescriptor - map editor id to editor.
    private Map mapIDtoEditor = initialIdToEditorMap(10);

    // Map of FileEditorMapping (extension to FileEditorMapping)
    private EditorMap typeEditorMappings;

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

	private RelatedRegistry relatedRegistry;

	public static final String EMPTY_EDITOR_ID = "org.eclipse.ui.internal.emptyEditorTab"; //$NON-NLS-1$

    /**
     * Return an instance of the receiver. Adds listeners into the extension
     * registry for dynamic UI purposes.
     */
    public EditorRegistry() {
        super();
        initializeFromStorage();
        IExtensionTracker tracker = PlatformUI.getWorkbench().getExtensionTracker();
        tracker.registerHandler(this, ExtensionTracker.createExtensionPointFilter(getExtensionPointFilter()));
		relatedRegistry = new RelatedRegistry();
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
     * @param contentTypeVector 
     * @param bDefault
     *            Indicates whether the editor should be made the default editor
     *            and hence appear first inside a FileEditorMapping
     * 
     * This method is not API and should not be called outside the workbench
     * code.
     */
    public void addEditorFromPlugin(EditorDescriptor editor, List extensions,
            List filenames, List contentTypeVector, boolean bDefault) {

    	PlatformUI.getWorkbench().getExtensionTracker().registerObject(
				editor.getConfigurationElement().getDeclaringExtension(),
				editor, IExtensionTracker.REF_WEAK);
        // record it in our quick reference list
        sortedEditorsFromPlugins.add(editor);

        // add it to the table of mappings
        Iterator itr = extensions.iterator();
        while (itr.hasNext()) {
            String fileExtension = (String) itr.next();

            if (fileExtension != null && fileExtension.length() > 0) {
                FileEditorMapping mapping = getMappingFor("*." + fileExtension); //$NON-NLS-1$
                if (mapping == null) { // no mapping for that extension
                    mapping = new FileEditorMapping(fileExtension);
                    typeEditorMappings.putDefault(mappingKeyFor(mapping),
                            mapping);
                }
                mapping.addEditor(editor);
                if (bDefault) {
					mapping.setDefaultEditor(editor);
				}
            }
        }

        // add it to the table of mappings
        itr = filenames.iterator();
        while (itr.hasNext()) {
            String filename = (String) itr.next();

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
                if (bDefault) {
					mapping.setDefaultEditor(editor);
				}
            }
        }
		
		
		itr = contentTypeVector.iterator();
		while(itr.hasNext()) {
			String contentTypeId = (String) itr.next();
			if (contentTypeId != null && contentTypeId.length() > 0) {
				IContentType contentType = Platform.getContentTypeManager().getContentType(contentTypeId);
				if (contentType != null) {
					IEditorDescriptor [] editorArray = (IEditorDescriptor[]) contentTypeToEditorMappings.get(contentType);
					if (editorArray == null) {
						editorArray = new IEditorDescriptor[] {editor};
						contentTypeToEditorMappings.put(contentType, editorArray);
					}
					else {
						IEditorDescriptor [] newArray = new IEditorDescriptor[editorArray.length + 1];
						if (bDefault) { // default editors go to the front of the line
							newArray[0] = editor;
							System.arraycopy(editorArray, 0, newArray, 1, editorArray.length);
						}
						else {
							newArray[editorArray.length] = editor;
							System.arraycopy(editorArray, 0, newArray, 0, editorArray.length);
						}
						contentTypeToEditorMappings.put(contentType, newArray);
					}
				}
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
        addListenerObject(l);
    }

    /*
     * (non-Javadoc) Method declared on IEditorRegistry.
     */
    public IEditorDescriptor findEditor(String id) {
        Object desc = mapIDtoEditor.get(id);
        if (WorkbenchActivityHelper.restrictUseOf(desc)) {
        	return null;
        }
		return (IEditorDescriptor) desc;
    }

    /**
     * Fires a property changed event to all registered listeners.
     * 
     * @param type the type of event
     * @see IEditorRegistry#PROP_CONTENTS
     */
    private void firePropertyChange(final int type) {
        Object[] array = getListeners();
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
		return getDefaultEditor(filename, guessAtContentType(filename));
    }

	/**
	 * Return the (approximated) content type for a file with the given name.
	 * 
	 * @param filename the filename
	 * @return the content type or <code>null</code> if it could not be determined
	 * @since 3.1
	 */
	private IContentType guessAtContentType(String filename) {
		return Platform.getContentTypeManager().findContentTypeFor(filename);
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

    /*
     * (non-Javadoc) Method declared on IEditorRegistry.
     */
    public IEditorDescriptor[] getEditors(String filename) {
		return getEditors(filename, guessAtContentType(filename));
	}

    /*
     * (non-Javadoc) Method declared on IEditorRegistry.
     */
    public IFileEditorMapping[] getFileEditorMappings() {
        FileEditorMapping[] array = typeEditorMappings.allMappings();
        final Collator collator = Collator.getInstance();
        Arrays.sort(array, new Comparator() {
            
            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
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
		return getImageDescriptor(filename, guessAtContentType(filename));
	}

	/**
     * Find the file editor mapping for the file extension. Returns
     * <code>null</code> if not found.
     * 
     * @param ext
     *            the file extension
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
     * Return an array of two FileEditorMapping items, where the first mapping
     * is for the entire filename, and the second mapping is for the filename's
     * extension only. These items can be null if no mapping exist on the
     * filename and/or filename's extension.</p>
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
     * WARNING! The image described by each editor descriptor is *not* known by
     * the workbench's graphic registry. Therefore clients must take care to
     * ensure that if they access any of the images held by these editors that
     * they also dispose them
     * </p>
     * @return the editor descriptors
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
     * Return the editors loaded from plugins.
     * 
     * @return the sorted array of editors declared in plugins
     * @see #comparer
     */
    public IEditorDescriptor[] getSortedEditorsFromPlugins() {
		Collection descs = WorkbenchActivityHelper
				.restrictCollection(sortedEditorsFromPlugins, new ArrayList());
		return (IEditorDescriptor[]) descs.toArray(new IEditorDescriptor[descs
				.size()]);
	}

    /**
	 * Answer an intial id to editor map. This will create a new map and
	 * populate it with the default system editors.
	 * 
	 * @param initialSize
	 *            the initial size of the map
	 * @return the new map
	 */
    private HashMap initialIdToEditorMap(int initialSize) {
        HashMap map = new HashMap(initialSize);
        addSystemEditors(map);
        return map;
    }

    /**
     * Add the system editors to the provided map. This will always add an
     * editor with an id of {@link #SYSTEM_EXTERNAL_EDITOR_ID} and may also add
     * an editor with id of {@link #SYSTEM_INPLACE_EDITOR_ID} if the system
     * configuration supports it.
     * 
     * @param map the map to augment
     */
    private void addSystemEditors(HashMap map) {
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
				.setImageDescriptor(WorkbenchImages
						.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJ_ELEMENT));
		map.put(EMPTY_EDITOR_ID, emptyEditorDescriptor);
    }

    /**
     * Initialize the registry state from plugin declarations and preference
     * overrides.
     */
    private void initializeFromStorage() {
        typeEditorMappings = new EditorMap();
        extensionImages = new HashMap();

        //Get editors from the registry
        EditorRegistryReader registryReader = new EditorRegistryReader();
        registryReader.addEditors(this);
        sortInternalEditors();
        rebuildInternalEditorMap();

        IPreferenceStore store = PlatformUI.getPreferenceStore();
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
     * overwritten in the file properties.ini.  In the form: 
     * <p>
     * <code>ext1:id1;ext2:id2;...</code>
     * </p>
     * 
     * @param defaultEditors the default editors to set
     */
    private void setProductDefaults(String defaultEditors) {
        if (defaultEditors == null || defaultEditors.length() == 0) {
			return;
		}

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
        IPath workbenchStatePath = WorkbenchPlugin.getDefault().getDataLocation();
        if(workbenchStatePath == null) {
			return false;
		}        
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
                    if (editor.getProgram() == null) {
						descriptor = new ProgramImageDescriptor(editor
                                .getFileName(), 0);
					} else {
						descriptor = new ExternalProgramImageDescriptor(editor
                                .getProgram());
					}
                    editor.setImageDescriptor(descriptor);
                    editorTable.put(editor.getId(), editor);
                }
            }
        } catch (IOException e) {
            try {
                if (reader != null) {
					reader.close();
				}
            } catch (IOException ex) {
                e.printStackTrace();
            }
            //Ignore this as the workbench may not yet have saved any state
            return false;
        } catch (WorkbenchException e) {
            ErrorDialog.openError((Shell) null, WorkbenchMessages.EditorRegistry_errorTitle,
                    WorkbenchMessages.EditorRegistry_errorMessage, 
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
        String versionString = memento.getString(IWorkbenchConstants.TAG_VERSION);
        boolean versionIs31 = "3.1".equals(versionString); //$NON-NLS-1$
        
        IMemento[] extMementos = memento
                .getChildren(IWorkbenchConstants.TAG_INFO);
        for (int i = 0; i < extMementos.length; i++) {
            String name = extMementos[i]
                    .getString(IWorkbenchConstants.TAG_NAME);
            if (name == null) {
				name = "*"; //$NON-NLS-1$
			}
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
            
            List defaultEditors = new ArrayList();
            
            if (versionIs31) { // parse the new format
				idMementos = extMementos[i]
						.getChildren(IWorkbenchConstants.TAG_DEFAULT_EDITOR);
				String[] defaultEditorIds = new String[idMementos.length];
				for (int j = 0; j < idMementos.length; j++) {
					defaultEditorIds[j] = idMementos[j]
							.getString(IWorkbenchConstants.TAG_ID);
				}
				for (int j = 0; j < defaultEditorIds.length; j++) {
					if (defaultEditorIds[j] != null) {
						EditorDescriptor editor = (EditorDescriptor) editorTable
								.get(defaultEditorIds[j]);
						if (editor != null) {
							defaultEditors.add(editor);
						}
					}
				}
			}
            else { // guess at pre 3.1 format defaults
            		if (!editors.isEmpty()) {
            			EditorDescriptor editor = (EditorDescriptor) editors.get(0);
            			if (editor != null) {
                			defaultEditors.add(editor);	
                		}
            		}
            		defaultEditors.addAll(Arrays.asList(mapping.getDeclaredDefaultEditors()));
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
            mapping.setDefaultEditors(defaultEditors);
            typeEditorMappings.put(mappingKeyFor(mapping), mapping);
        }
    }

    /**
     * Determine if the editors list contains the editor descriptor.
     * 
     * @param editorsArray
     * 			The list of editors
     * @param editorDescriptor
     * 			The editor descriptor
     * @return <code>true</code> if the editors list contains the editor descriptor
     */
    private boolean contains(List editorsArray,
            IEditorDescriptor editorDescriptor) {
        IEditorDescriptor currentEditorDescriptor = null;
        Iterator i = editorsArray.iterator();
        while (i.hasNext()) {
            currentEditorDescriptor = (IEditorDescriptor) i.next();
            if (currentEditorDescriptor.getId()
                    .equals(editorDescriptor.getId())) {
				return true;
			}
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
        IPath workbenchStatePath = WorkbenchPlugin.getDefault().getDataLocation();
        // XXX: nobody cares about this return value
        if(workbenchStatePath == null) {
			return false;
		}
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
                if (reader != null) {
					reader.close();
				}
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            MessageDialog.openError((Shell) null, WorkbenchMessages.EditorRegistry_errorTitle,
                    WorkbenchMessages.EditorRegistry_errorMessage);
            return false;
        } catch (WorkbenchException e) {
            ErrorDialog.openError((Shell) null, WorkbenchMessages.EditorRegistry_errorTitle,
                    WorkbenchMessages.EditorRegistry_errorMessage,
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

    /**
     * Return a friendly version of the given key suitable for use in the editor
     * map.
     */
    private String mappingKeyFor(String type) {
        // keep everyting lower case for case-sensitive platforms
        return type.toLowerCase();
    }

    /**
     * Return a key that combines the file's name and extension of the given
     * mapping
     * 
     * @param mapping the mapping to generate a key for
     */
    private String mappingKeyFor(FileEditorMapping mapping) {
        return mappingKeyFor(mapping.getName()
                + (mapping.getExtension().length() == 0 ? "" : "." + mapping.getExtension())); //$NON-NLS-1$ //$NON-NLS-2$
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
        Iterator itr = null;
        IEditorDescriptor desc = null;

        // Allocate a new map.
        mapIDtoEditor = initialIdToEditorMap(mapIDtoEditor.size());

        // Add plugin editors.
        itr = sortedEditorsFromPlugins.iterator();
        while (itr.hasNext()) {
            desc = (IEditorDescriptor) itr.next();
            mapIDtoEditor.put(desc.getId(), desc);
        }
    }

    /*
     * (non-Javadoc) Method declared on IEditorRegistry.
     */
    public void removePropertyListener(IPropertyListener l) {
        removeListenerObject(l);
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
        memento.putString(IWorkbenchConstants.TAG_VERSION, "3.1"); //$NON-NLS-1$
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
            editorArray = type.getDeclaredDefaultEditors();
            for (int i = 0; i < editorArray.length; i++) {
                EditorDescriptor editor = (EditorDescriptor) editorArray[i];
                if (!editors.contains(editor)) {
                    editors.add(editor);
                }
                IMemento idMemento = editorMemento
                        .createChild(IWorkbenchConstants.TAG_DEFAULT_EDITOR);
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
                if (writer != null) {
					writer.close();
				}
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            MessageDialog.openError((Shell) null, "Saving Problems", //$NON-NLS-1$
                    "Unable to save resource associations."); //$NON-NLS-1$
            return;
        }

        memento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_EDITORS);
        Iterator itr = editors.iterator();
        while (itr.hasNext()) {
            EditorDescriptor editor = (EditorDescriptor) itr.next();
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
                if (writer != null) {
					writer.close();
				}
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
     * goes from an extension to the collection of editors that work on it. This
     * operation will rebuild the internal editor mappings.
     * 
     * @param newResourceTypes
     *            te new file editor mappings.
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
    private Object[] sortEditors(List unsortedList) {
        Object[] array = new Object[unsortedList.size()];
        unsortedList.toArray(array);

        Collections.sort(Arrays.asList(array), comparer);
        return array;
    }

    /**
     * Alphabetically sort the internal editors.
     * 
     * @see #comparer
     */
    private void sortInternalEditors() {
        Object[] array = sortEditors(sortedEditorsFromPlugins);
        sortedEditorsFromPlugins = new ArrayList();
        for (int i = 0; i < array.length; i++) {
            sortedEditorsFromPlugins.add(array[i]);
        }
    }

    /**
     * Map of FileEditorMapping (extension to FileEditorMapping) Uses two
     * java.util.HashMap: one keeps the default which are set by the plugins and
     * the other keeps the changes made by the user through the preference page.
     */
    private static class EditorMap {
        HashMap defaultMap = new HashMap();

        HashMap map = new HashMap();

        /**
         * Put a default mapping into the editor map.
         * 
         * @param key the key to set
         * @param value the value to associate
         */
        public void putDefault(String key, FileEditorMapping value) {
            defaultMap.put(key, value);
        }

        /**
         * Put a mapping into the user editor map.
         * 
         * @param key the key to set
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
         * Return the mapping associated to the key. First searches user
         * map, and then falls back to the default map if there is no match. May
         * return <code>null</code>
         * 
         * @param key
         *            the key to search for
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
         * Return all mappings. This will return default mappings overlayed with
         * user mappings.
         * 
         * @return the mappings
         */
        public FileEditorMapping[] allMappings() {
            HashMap merge = (HashMap) defaultMap.clone();
            merge.putAll(map);
            Collection values = merge.values();
            FileEditorMapping result[] = new FileEditorMapping[values.size()];
            return (FileEditorMapping[]) values.toArray(result);
        }

        /**
         * Return all user mappings.
         * 
         * @return the mappings
         */
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
        } 
        
        return new ExternalProgramImageDescriptor(externalProgram);
    }
    
    /**
     * Removes the entry with the value of the editor descriptor from the given
     * map. If the descriptor is the last descriptor in a given
     * FileEditorMapping then the mapping is removed from the map.
     * 
     * @param map
     *            the map to search
     * @param desc
     *            the descriptor value to remove
     */
    private void removeEditorFromMapping(HashMap map, IEditorDescriptor desc) {
        Iterator iter = map.values().iterator();
        FileEditorMapping mapping;
        IEditorDescriptor[] editors;
        while (iter.hasNext()) {
            mapping = (FileEditorMapping) iter.next();
            editors = mapping.getEditors();
            for (int i = 0; i < editors.length; i++) {
				if (editors[i] == desc) {
                    mapping.removeEditor((EditorDescriptor) editors[i]);
                    break;
                }
			}
            if (editors.length <= 0) {
                map.remove(mapping);
                break;
            }
        }
    }

	
    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#removeExtension(org.eclipse.core.runtime.IExtension, java.lang.Object[])
     */
    public void removeExtension(IExtension source, Object[] objects) {
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] instanceof EditorDescriptor) {
                EditorDescriptor desc = (EditorDescriptor) objects[i];

                sortedEditorsFromPlugins.remove(desc);
                mapIDtoEditor.values().remove(desc);
                removeEditorFromMapping(typeEditorMappings.defaultMap, desc);
                removeEditorFromMapping(typeEditorMappings.map, desc);
				//TODO remove from content type mappings
            }

        }
    }

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#addExtension(org.eclipse.core.runtime.dynamicHelpers.IExtensionTracker, org.eclipse.core.runtime.IExtension)
	 */
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
        EditorRegistryReader eReader = new EditorRegistryReader();
        IConfigurationElement[] elements = extension.getConfigurationElements();
        for (int i = 0; i < elements.length; i++) {
            String id = elements[i].getAttribute(IWorkbenchConstants.TAG_ID);
            if (id != null && findEditor(id) != null) {
				continue;
			}
            eReader.readElement(this, elements[i]);
        }
	}

	private IExtensionPoint getExtensionPointFilter() {
		return Platform.getExtensionRegistry().getExtensionPoint(PlatformUI.PLUGIN_ID, IWorkbenchRegistryConstants.PL_EDITOR);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorRegistry#getDefaultEditor(java.lang.String, org.eclipse.core.runtime.content.IContentType)
	 */
	public IEditorDescriptor getDefaultEditor(String fileName, IContentType contentType) {
        return getEditorForContentType(fileName, contentType);
	}

	/**
	 * Return the editor for a file with a given content type.
	 * 
	 * @param filename the file name
	 * @param contentType the content type
	 * @return the editor for a file with a given content type
	 * @since 3.1
	 */
	private IEditorDescriptor getEditorForContentType(String filename,
			IContentType contentType) {
		IEditorDescriptor desc = null;
		Object[] contentTypeResults = findRelatedObjects(contentType, filename, relatedRegistry);
		if (contentTypeResults != null && contentTypeResults.length > 0) {
			desc = (IEditorDescriptor) contentTypeResults[0];
		}
		return desc;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorRegistry#getEditors(java.lang.String, org.eclipse.core.runtime.content.IContentType)
	 */
	public IEditorDescriptor[] getEditors(String fileName, IContentType contentType) {
		return findRelatedObjects(contentType, fileName, relatedRegistry);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorRegistry#getImageDescriptor(java.lang.String, org.eclipse.core.runtime.content.IContentType)
	 */
	public ImageDescriptor getImageDescriptor(String filename, IContentType contentType) {
        if (filename == null) {
			return getDefaultImage();
		}

		if (contentType != null) {
			IEditorDescriptor desc = getEditorForContentType(filename, contentType);
			if (desc != null) {
				ImageDescriptor anImage = (ImageDescriptor) extensionImages.get(desc);	
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
        ImageDescriptor anImage = (ImageDescriptor) extensionImages.get(key);
        if (anImage != null) {
			return anImage;
		}

        // See if we have a mapping for the filename or extension
        FileEditorMapping[] mapping = getMappingForFilename(filename);
        for (int i = 0; i < 2; i++) {
            if (mapping[i] != null) {
                // Lookup in the cache first...
                String mappingKey = mappingKeyFor(mapping[i]);
                ImageDescriptor mappingImage = (ImageDescriptor) extensionImages
                        .get(key);
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
        //	for dynamic UI - comment out the next line
        //extensionImages.put(key, anImage);
        return anImage;

	}
    
    /**
	 * Find objects related to the content type.
	 * 
	 * This method is temporary and exists only to back us off of the
	 * soon-to-be-removed IContentTypeManager.IRelatedRegistry API.
	 * 
	 * @param type
	 * @param fileName
	 * @param registry
	 * @return the related objects
	 */
	private IEditorDescriptor [] findRelatedObjects(IContentType type, String fileName,
			RelatedRegistry registry) {
		List allRelated = new ArrayList();
		List nonDefaultFileEditors = new ArrayList();
		IEditorDescriptor [] related;
		
		if (fileName != null) {
			FileEditorMapping mapping = getMappingFor(fileName);
			if (mapping != null) {
				// backwards compatibility - add editors flagged as "default"
				related = mapping.getDeclaredDefaultEditors();
				for (int i = 0; i < related.length; i++) {
					// we don't want to return duplicates
					if (!allRelated.contains(related[i])) {
						// if it's not filtered, add it to the list
						if (!WorkbenchActivityHelper.filterItem(related[i])) {
							allRelated.add(related[i]);
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
					related = mapping.getDeclaredDefaultEditors();
					for (int i = 0; i < related.length; i++) {
						// we don't want to return duplicates
						if (!allRelated.contains(related[i])) {
							// if it's not filtered, add it to the list
							if (!WorkbenchActivityHelper.filterItem(related[i])) {
								allRelated.add(related[i]);
							}
						}
					}
					nonDefaultFileEditors.addAll(Arrays.asList(mapping.getEditors()));
				}
			}
		}
		
		if (type != null) {
			// now add any objects directly related to the content type
			related = registry.getRelatedObjects(type);
			for (int i = 0; i < related.length; i++) {
				// we don't want to return duplicates
				if (!allRelated.contains(related[i])) {
					// if it's not filtered, add it to the list
					if (!WorkbenchActivityHelper.filterItem(related[i])) {
						allRelated.add(related[i]);
					}
				}
			}

		}

		if (type != null) {
			// now add any indirectly related objects, walking up the content type hierarchy 
			while ((type = type.getBaseType()) != null) {
				related = registry.getRelatedObjects(type);
				for (int i = 0; i < related.length; i++) {
					// we don't want to return duplicates
					if (!allRelated.contains(related[i])) {
						// if it's not filtered, add it to the list
						if (!WorkbenchActivityHelper.filterItem(related[i])) {
							allRelated.add(related[i]);
						}
					}
				}
			}
		}
			
		// add all non-default editors to the list
		for (Iterator i = nonDefaultFileEditors.iterator(); i.hasNext();) {
			IEditorDescriptor editor = (IEditorDescriptor) i.next();
			if (!allRelated.contains(editor) && !WorkbenchActivityHelper.filterItem(editor)) {
				allRelated.add(editor);
			}
		}
		
		return (IEditorDescriptor []) allRelated.toArray(new IEditorDescriptor [allRelated
				.size()]);
	}

	/**
	 * Return the editors bound to this content type, either directly or indirectly.
	 * 
	 * @param type the content type to check
	 * @return the editors
	 * @since 3.1
     *
     * TODO: this should be rolled in with the above findRelatedObjects code
	 */
	public IEditorDescriptor [] getEditorsForContentType(IContentType type) {
		ArrayList allRelated = new ArrayList();
		if (type == null) {
			return new IEditorDescriptor [0];
		}
		
		Object [] related = relatedRegistry.getRelatedObjects(type);
		for (int i = 0; i < related.length; i++) {	
			// we don't want to return duplicates
			if (!allRelated.contains(related[i])) {
				// if it's not filtered, add it to the list
				if (!WorkbenchActivityHelper.filterItem(related[i])) {
					allRelated.add(related[i]);
				}
				
			}
		}
		
		// now add any indirectly related objects, walking up the content type hierarchy 
		while ((type = type.getBaseType()) != null) {
			related = relatedRegistry.getRelatedObjects(type);
			for (int i = 0; i < related.length; i++) {
				// we don't want to return duplicates
				if (!allRelated.contains(related[i])) {
					// if it's not filtered, add it to the list
					if (!WorkbenchActivityHelper.filterItem(related[i])) {
						allRelated.add(related[i]);
					}
				}
			}
		}
		
		return (IEditorDescriptor[]) allRelated.toArray(new IEditorDescriptor[allRelated.size()]);
	}
	
	/**
	 * Get filemappings for all defined filetypes, including those defined by content type.
	 * 
	 * @return the filetypes
	 * @since 3.1
	 */
	public IFileEditorMapping [] getUnifiedMappings() {
        IFileEditorMapping[] standardMappings = PlatformUI.getWorkbench()
                .getEditorRegistry().getFileEditorMappings();
        
        List allMappings = new ArrayList(Arrays.asList(standardMappings));
        // mock-up content type extensions into IFileEditorMappings
        IContentType [] contentTypes = Platform.getContentTypeManager().getAllContentTypes();
        for (int i = 0; i < contentTypes.length; i++) {
			IContentType type = contentTypes[i];
			String [] extensions = type.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
			for (int j = 0; j < extensions.length; j++) {
				String extension = extensions[j];
				boolean found = false;
				for (Iterator k = allMappings.iterator(); k.hasNext();) {
					IFileEditorMapping mapping = (IFileEditorMapping) k.next();
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
		
			String [] filenames = type.getFileSpecs(IContentType.FILE_NAME_SPEC);
			for (int j = 0; j < filenames.length; j++) {
				String wholename = filenames[j];
				int idx = wholename.indexOf('.');				
				String name = idx == -1 ? wholename : wholename.substring(0, idx);
				String extension = idx == -1 ? "" : wholename.substring(idx + 1); //$NON-NLS-1$
				
				boolean found = false;
				for (Iterator k = allMappings.iterator(); k.hasNext();) {
					IFileEditorMapping mapping = (IFileEditorMapping) k.next();
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
        
        return (IFileEditorMapping []) allMappings
				.toArray(new IFileEditorMapping [allMappings.size()]);
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

	public IEditorDescriptor getDefaultEditor() {
		IEditorDescriptor[] candidates = ((EditorRegistry) PlatformUI
				.getWorkbench().getEditorRegistry())
				.getEditorsForContentType(contentType);
		if (candidates.length == 0
				|| WorkbenchActivityHelper.restrictUseOf(candidates[0])) {
			return null;
		}
		return candidates[0];
	}

	public IEditorDescriptor[] getEditors() {
		IEditorDescriptor[] editorsForContentType = ((EditorRegistry) PlatformUI
				.getWorkbench().getEditorRegistry())
				.getEditorsForContentType(contentType);
		return (IEditorDescriptor[]) WorkbenchActivityHelper
				.restrictArray(editorsForContentType);
	}

	public IEditorDescriptor[] getDeletedEditors() {
		return new IEditorDescriptor[0];
	}

	public String getExtension() {
		return extension;
	}

	public ImageDescriptor getImageDescriptor() {
		IEditorDescriptor editor = getDefaultEditor();
		if (editor == null) {
			return WorkbenchImages
					.getImageDescriptor(ISharedImages.IMG_OBJ_FILE);
		}

		return editor.getImageDescriptor();
	}

	public String getLabel() {
		return filename + '.' + extension; 
	}

	public String getName() {
		return filename;
    }	

    /* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof MockMapping)) {
			return false;
		}

		MockMapping mapping = (MockMapping) obj;
		if (!this.filename.equals(mapping.filename)) {
			return false;
		}

		if (!this.extension.equals(mapping.extension)) {
			return false;
		}

		if (!Util.equals(this.getEditors(), mapping.getEditors())) {
			return false;
		}
		return Util.equals(this.getDeletedEditors(), mapping
				.getDeletedEditors());
	}
}

