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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * This class is used to read resource editor registry descriptors from
 * the platform registry.
 */
public class EditorRegistryReader extends RegistryReader {

    public static final String TAG_EDITOR = "editor";//$NON-NLS-1$

    public static final String P_TRUE = "true";//$NON-NLS-1$

    private EditorRegistry editorRegistry;

    /**
     * Get the editors that are defined in the registry
     * and add them to the ResourceEditorRegistry
     * The readAll flag indicates if we should read non modified plugins
     *
     * Warning:
     * The registry must be passed in because this method is called during the
     * process of setting up the registry and at this time it has not been
     * safely setup with the plugin.
     */
    protected void addEditors(boolean readAll, EditorRegistry registry) {
        IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
        this.editorRegistry = registry;
        readRegistry(extensionRegistry, PlatformUI.PLUGIN_ID,
                IWorkbenchConstants.PL_EDITOR);
    }

    /**
     * Implementation of the abstract method that
     * processes one configuration element.
     */
    protected boolean readElement(IConfigurationElement element) {
        if (!element.getName().equals(TAG_EDITOR))
            return false;

        EditorDescriptor editor = new EditorDescriptor();
        editor.setConfigurationElement(element);
        String id = element.getAttribute(EditorDescriptor.ATT_ID);
        if (id == null) {
            logMissingAttribute(element, EditorDescriptor.ATT_ID);
            return true;
        }
        
        List extensionsVector = new ArrayList();
        List filenamesVector = new ArrayList();
        boolean defaultEditor = false;

        // Get editor name (required field).
        String name = element.getAttribute(EditorDescriptor.ATT_NAME);
        if (name == null) {
            logMissingAttribute(element, EditorDescriptor.ATT_NAME);
            return true;
        }

        // Get editor icon (required field for internal editors)
        String icon = element.getAttribute(EditorDescriptor.ATT_ICON);
        if (icon == null) {
            if (element.getAttribute(EditorDescriptor.ATT_CLASS) != null) {
                logMissingAttribute(element, EditorDescriptor.ATT_ICON);
                return true;
            }
        }
        if (icon != null) {
            String extendingPluginId = element.getNamespace();
            editor.setImageDescriptor(AbstractUIPlugin
                    .imageDescriptorFromPlugin(extendingPluginId, icon));            
        }

        // Get target extensions (optional field)
        String extensionsString = element.getAttribute(EditorDescriptor.ATT_EXTENSIONS);
        if (extensionsString != null) {
            StringTokenizer tokenizer = new StringTokenizer(extensionsString,
                    ",");//$NON-NLS-1$
            while (tokenizer.hasMoreTokens()) {
                extensionsVector.add(tokenizer.nextToken().trim());
            }
        }
        String filenamesString = element.getAttribute(EditorDescriptor.ATT_FILENAMES);
        if (filenamesString != null) {
            StringTokenizer tokenizer = new StringTokenizer(filenamesString,
                    ",");//$NON-NLS-1$
            while (tokenizer.hasMoreTokens()) {
                filenamesVector.add(tokenizer.nextToken().trim());
            }
        }

        // Get launcher class or command.	
        String launcher = element.getAttribute(EditorDescriptor.ATT_LAUNCHER);
        String command = element.getAttribute(EditorDescriptor.ATT_COMMAND);
        if (launcher != null) {
            // open using a launcer
            editor.setOpenMode(EditorDescriptor.OPEN_EXTERNAL);
        } else if (command != null) {
            // open using an external editor 	
            editor.setOpenMode(EditorDescriptor.OPEN_EXTERNAL);
            if (icon == null) {
                editor.setImageDescriptor(WorkbenchImages
                        .getImageDescriptorFromProgram(command, 0));
            }
        } else {
            // open using an internal editor
            editor.setOpenMode(EditorDescriptor.OPEN_INTERNAL);
        }

        // Is this the default editor?
        String def = element.getAttribute(EditorDescriptor.ATT_DEFAULT);
        if (def != null)
            defaultEditor = def.equalsIgnoreCase(P_TRUE);

        // Add the editor to the manager.	
        editorRegistry.addEditorFromPlugin(editor, extensionsVector,
                filenamesVector, defaultEditor);
        return true;
    }

    //for dynamic UI
    public void readElement(EditorRegistry editorRegistry,
            IConfigurationElement element) {
        this.editorRegistry = editorRegistry;
        readElement(element);
    }
}