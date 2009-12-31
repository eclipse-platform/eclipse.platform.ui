/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

/**
 * This class is used to read resource editor registry descriptors from
 * the platform registry.
 */
public class EditorRegistryReader extends RegistryReader {

    private EditorRegistry editorRegistry;

    /**
     * Get the editors that are defined in the registry
     * and add them to the ResourceEditorRegistry
     *
     * Warning:
     * The registry must be passed in because this method is called during the
     * process of setting up the registry and at this time it has not been
     * safely setup with the plugin.
     */
    protected void addEditors(EditorRegistry registry) {
        IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
        this.editorRegistry = registry;
        readRegistry(extensionRegistry, PlatformUI.PLUGIN_ID,
                IWorkbenchRegistryConstants.PL_EDITOR);
    }

    /**
     * Implementation of the abstract method that
     * processes one configuration element.
     */
    protected boolean readElement(IConfigurationElement element) {
        if (!element.getName().equals(IWorkbenchRegistryConstants.TAG_EDITOR)) {
			return false;
		}

        String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
        if (id == null) {
            logMissingAttribute(element, IWorkbenchRegistryConstants.ATT_ID);
            return true;
        }
        
        EditorDescriptor editor = new EditorDescriptor(id, element);
        
        List extensionsVector = new ArrayList();
        List filenamesVector = new ArrayList();
		List contentTypeVector = new ArrayList();
        boolean defaultEditor = false;

        // Get editor name (required field).
        if (element.getAttribute(IWorkbenchRegistryConstants.ATT_NAME) == null) {
            logMissingAttribute(element, IWorkbenchRegistryConstants.ATT_NAME);
            return true;
        }
        
        // Get target extensions (optional field)
        String extensionsString = element.getAttribute(IWorkbenchRegistryConstants.ATT_EXTENSIONS);
        if (extensionsString != null) {
            StringTokenizer tokenizer = new StringTokenizer(extensionsString,
                    ",");//$NON-NLS-1$
            while (tokenizer.hasMoreTokens()) {
                extensionsVector.add(tokenizer.nextToken().trim());
            }
        }
        String filenamesString = element.getAttribute(IWorkbenchRegistryConstants.ATT_FILENAMES);
        if (filenamesString != null) {
            StringTokenizer tokenizer = new StringTokenizer(filenamesString,
                    ",");//$NON-NLS-1$
            while (tokenizer.hasMoreTokens()) {
                filenamesVector.add(tokenizer.nextToken().trim());
            }
        }
        
		IConfigurationElement [] bindings = element.getChildren(IWorkbenchRegistryConstants.TAG_CONTENT_TYPE_BINDING);
		for (int i = 0; i < bindings.length; i++) {
			String contentTypeId = bindings[i].getAttribute(IWorkbenchRegistryConstants.ATT_CONTENT_TYPE_ID);
			if (contentTypeId == null) {
				continue;
			}
			contentTypeVector.add(contentTypeId);
		}
		
        // Is this the default editor?
        String def = element.getAttribute(IWorkbenchRegistryConstants.ATT_DEFAULT);
        if (def != null) {
			defaultEditor = Boolean.valueOf(def).booleanValue();
		}

        // Add the editor to the manager.	
        editorRegistry.addEditorFromPlugin(editor, extensionsVector,
                filenamesVector, contentTypeVector, defaultEditor);
        return true;
    }


    /**
     * @param editorRegistry
     * @param element
     */
    public void readElement(EditorRegistry editorRegistry,
            IConfigurationElement element) {
        this.editorRegistry = editorRegistry;
        readElement(element);
    }
}
