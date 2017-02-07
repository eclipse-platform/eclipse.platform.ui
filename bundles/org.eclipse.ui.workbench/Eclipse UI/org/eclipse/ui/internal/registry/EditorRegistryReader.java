/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ui.IEditorDescriptor;
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
    @Override
	protected boolean readElement(IConfigurationElement element) {
		if (element.getName().equals(IWorkbenchRegistryConstants.TAG_EDITOR)) {
			return readEditorElement(element);
		}
		if (element.getName().equals(IWorkbenchRegistryConstants.TAG_EDITOR_CONTENT_TYPTE_BINDING)) {
			return readEditorContentTypeBinding(element);
		}
		return false;
    }

	/**
	 * @param element
	 * @return
	 */
	private boolean readEditorContentTypeBinding(IConfigurationElement element) {
		IEditorDescriptor descriptor = null;
		String editorId = element.getAttribute(IWorkbenchRegistryConstants.ATT_EDITOR_ID);
		if (editorId == null) {
			logMissingAttribute(element, IWorkbenchRegistryConstants.ATT_EDITOR_ID);
		} else {
			descriptor = editorRegistry.findEditor(editorId);
			if (descriptor == null) {
				logError(element, "Unknown editor with id: " + editorId); //$NON-NLS-1$
			}
		}

		IContentType contentType = null;
		String contentTypeId = element.getAttribute(IWorkbenchRegistryConstants.ATT_CONTENT_TYPE_ID);
		if (contentTypeId == null) {
			logMissingAttribute(element, IWorkbenchRegistryConstants.ATT_CONTENT_TYPE_ID);
		} else {
			contentType = Platform.getContentTypeManager().getContentType(contentTypeId);
			if (contentType == null) {
				logError(element, "Unknown content-type with id: " + contentTypeId); //$NON-NLS-1$
			}
		}

		if (descriptor != null && contentType != null) {
			editorRegistry.addContentTypeBinding(contentType, descriptor, false);
		}
		return true;
	}

	private boolean readEditorElement(IConfigurationElement element) {
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
		for (IConfigurationElement binding : bindings) {
			String contentTypeId = binding.getAttribute(IWorkbenchRegistryConstants.ATT_CONTENT_TYPE_ID);
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
