/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Joe Bowbeer (jozart@blarg.net) - removed dependency on runtime compatibility layer (bug 74526)
 *******************************************************************************/
package org.eclipse.ui.examples.readmetool;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Creates the sections used in the <code>ContentOutline</code>
 *
 * @see ReadmeContentOutlinePage#getContentOutline(IAdaptable)
 */
public class ReadmeModelFactory {
    private static ReadmeModelFactory instance = new ReadmeModelFactory();

    private boolean registryLoaded = false;

    IReadmeFileParser parser = null;

    /**
     * Creates a new ReadmeModelFactory.
     */
    private ReadmeModelFactory() {
        // do nothing
    }

    /**
     * Adds all mark elements to the list for the subtree rooted
     * at the given mark element.
     */
    protected void addSections(AdaptableList list, MarkElement element) {
        list.add(element);
        Object[] children = element.getChildren(element);
        for (int i = 0; i < children.length; ++i) {
            addSections(list, (MarkElement) children[i]);
        }
    }

    /**
     * Returns the content outline for the given Readme file.
     *
     * @param adaptable  the element for which to return the content outline
     * @return the content outline for the argument
     */
    public AdaptableList getContentOutline(IAdaptable adaptable) {
        return new AdaptableList(getToc((IFile) adaptable));
    }

    /**
     * Returns the singleton readme adapter.
     */
    public static ReadmeModelFactory getInstance() {
        return instance;
    }

    /**
     * Returns a list of all sections in this readme file.
     *
     * @param file  the file for which to return section heading and subheadings
     * @return A list containing headings and subheadings
     */
    public AdaptableList getSections(IFile file) {
        MarkElement[] topLevel = getToc(file);
        AdaptableList list = new AdaptableList();
        for (int i = 0; i < topLevel.length; i++) {
            addSections(list, topLevel[i]);
        }
        return list;
    }

    /**
     * Convenience method.  Looks for a readme file in the selection,
     * and if one is found, returns the sections for it.  Returns null
     * if there is no readme file in the selection.
     */
    public AdaptableList getSections(ISelection sel) {
        // If sel is not a structured selection just return.
        if (!(sel instanceof IStructuredSelection))
            return null;
        IStructuredSelection structured = (IStructuredSelection) sel;

        //if the selection is a readme file, get its sections.
        Object object = structured.getFirstElement();
        if (object instanceof IFile) {
            IFile file = (IFile) object;
            String extension = file.getFileExtension();
            if (extension != null
                    && extension.equals(IReadmeConstants.EXTENSION)) {
                return getSections(file);
            }
        }

        //the selected object is not a readme file
        return null;
    }

    /**
     * Parses the contents of the Readme file by looking for lines 
     * that start with a number.
     *
     * @param file  the file representing the Readme file
     * @return an element collection representing the table of contents
     */
    private MarkElement[] getToc(IFile file) {
        if (registryLoaded == false)
            loadParser();
        return parser.parse(file);
    }

    /**
     * Loads the parser from the registry by searching for
     * extensions that satisfy our published extension point.
     * For the sake of simplicity, we will pick the last extension,
     * allowing tools to override what is used. In a more
     * elaborate tool, all the extensions would be processed.
     */
    private void loadParser() {
        IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(
                IReadmeConstants.PLUGIN_ID, IReadmeConstants.PP_SECTION_PARSER);
        if (point != null) {
            IExtension[] extensions = point.getExtensions();
            for (int i = 0; i < extensions.length; i++) {
                IExtension currentExtension = extensions[i];
                // in a real application, we would collection
                // the entire list and probably expose it
                // as a drop-down list. For the sake
                // of simplicity, we will pick the last extension only.
                if (i == extensions.length - 1) {
                    IConfigurationElement[] configElements = currentExtension
                            .getConfigurationElements();
                    for (int j = 0; j < configElements.length; j++) {
                        IConfigurationElement config = configElements[i];
                        if (config.getName()
                                .equals(IReadmeConstants.TAG_PARSER)) {
                            // process the first 'parser' element and stop
                            processParserElement(config);
                            break;
                        }
                    }
                }
            }
        }
        if (parser == null)
            parser = new DefaultSectionsParser();
        registryLoaded = true;
    }

    /**
     * Tries to create the Readme file parser. If an error occurs during
     * the creation of the parser, print an error and set the parser
     * to null.
     *
     * @param element  the element to process
     */
    private void processParserElement(IConfigurationElement element) {
        try {
            parser = (IReadmeFileParser) element
                    .createExecutableExtension(IReadmeConstants.ATT_CLASS);
        } catch (CoreException e) {
            // since this is an example just write to the console
            System.out
                    .println(MessageUtil
                            .getString("Unable_to_create_file_parser") + e.getStatus().getMessage()); //$NON-NLS-1$
            parser = null;
        }
    }
}
