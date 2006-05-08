/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.misc;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.osgi.framework.Bundle;

public class ExternalEditor {
    private IPath filePath;

    private EditorDescriptor descriptor;

    /**
     * Create an external editor.
     */
    public ExternalEditor(IPath newFilePath, EditorDescriptor editorDescriptor) {
        this.filePath = newFilePath;
        this.descriptor = editorDescriptor;
    }

    /**
     * open the editor. If the descriptor has a program then use it - otherwise build its
     * info from the descriptor.
     * @exception	Throws a CoreException if the external editor could not be opened.
     */
    public void open() throws CoreException {

        Program program = this.descriptor.getProgram();
        if (program == null) {
            openWithUserDefinedProgram();
        } else {
            String path = ""; //$NON-NLS-1$
            if (filePath != null) {
                path = filePath.toOSString();
                if (program.execute(path)) {
					return;
				}
            }
            throw new CoreException(
                    new Status(
                            IStatus.ERROR,
                            WorkbenchPlugin.PI_WORKBENCH,
                            0,
                            NLS.bind(WorkbenchMessages.ExternalEditor_errorMessage, path), 
                            null));
        }
    }

    /**
     * open the editor.
     * @exception	Throws a CoreException if the external editor could not be opened.
     */
    public void openWithUserDefinedProgram() throws CoreException {
        // We need to determine if the command refers to a program in the plugin
        // install directory. Otherwise we assume the program is on the path.

        String programFileName = null;
        IConfigurationElement configurationElement = descriptor
                .getConfigurationElement();

        // Check if we have a config element (if we don't it is an
        // external editor created on the resource associations page).
        if (configurationElement != null) {
            try {
                Bundle bundle = Platform.getBundle(configurationElement
                        .getNamespace());
                // See if the program file is in the plugin directory
                URL entry = bundle.getEntry(descriptor.getFileName());
                if (entry != null) {
                    // this will bring the file local if the plugin is on a server
                    URL localName = Platform.asLocalURL(entry);
                    File file = new File(localName.getFile());
                    //Check that it exists before we assert it is valid
                    if (file.exists()) {
						programFileName = file.getAbsolutePath();
					}
                }
            } catch (IOException e) {
                // Program file is not in the plugin directory
            }
        }

        if (programFileName == null) {
			// Program file is not in the plugin directory therefore
            // assume it is on the path
            programFileName = descriptor.getFileName();
		}

        // Get the full path of the file to open
        if (filePath == null) {
            throw new CoreException(
                    new Status(
                            IStatus.ERROR,
                            WorkbenchPlugin.PI_WORKBENCH,
                            0,
                            NLS.bind(WorkbenchMessages.ExternalEditor_errorMessage,programFileName), 
                            null));
        }
        String path = filePath.toOSString();

        // Open the file

        // ShellCommand was removed in response to PR 23888.  If an exception was 
        // thrown, it was not caught in time, and no feedback was given to user

        try {
            Runtime.getRuntime().exec(new String[] { programFileName, path });
        } catch (Exception e) {
            throw new CoreException(
                    new Status(
                            IStatus.ERROR,
                            WorkbenchPlugin.PI_WORKBENCH,
                            0,
                            NLS.bind(WorkbenchMessages.ExternalEditor_errorMessage,programFileName),
                            e));
        }
    }
}
