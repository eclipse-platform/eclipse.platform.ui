package org.eclipse.ui.internal.misc;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.EditorDescriptor;

public class ExternalEditor {
	private IFile file;
	private EditorDescriptor descriptor;
/**
 * Create an external editor.
 */
public ExternalEditor(IFile newFile, EditorDescriptor editorDescriptor) {
	this.file = newFile;
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
		String path = new String();
		IPath location = file.getLocation();
		if(location != null) {
			path = location.toOSString();
			if(program.execute(path))
				return;
		}
		throw new CoreException(new Status(
			Status.ERROR, 
			WorkbenchPlugin.PI_WORKBENCH, 
			0, 
			WorkbenchMessages.format("ExternalEditor.errorMessage", new Object[] {path}), //$NON-NLS-1$
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
	IConfigurationElement configurationElement = descriptor.getConfigurationElement();

	// Check if we have a config element (if we don't it is an
	// external editor created on the resource associations page).
	if (configurationElement != null) {
		// Get editor's plugin directory.
		URL installURL = configurationElement.getDeclaringExtension().getDeclaringPluginDescriptor().getInstallURL();
		try {
			// See if the program file is in the plugin directory
			URL commandURL = new URL(installURL, descriptor.getFileName());
			URL localName = Platform.asLocalURL(commandURL); // this will bring the file local if the plugin is on a server
			File file = new File(localName.getFile());
			//Check that it exists before we assert it is valid
			if(file.exists())
				programFileName = file.getAbsolutePath();
		} catch (IOException e) {
			// Program file is not in the plugin directory
		}
	}

	if (programFileName == null) 
		// Program file is not in the plugin directory therefore
		// assume it is on the path
		programFileName = descriptor.getFileName();

	// Get the full path of the file to open
	IPath location = file.getLocation();
	if(location == null) {
		throw new CoreException(new Status(
			Status.ERROR, 
			WorkbenchPlugin.PI_WORKBENCH, 
			0, 
			WorkbenchMessages.format("ExternalEditor.errorMessage", new Object[] {programFileName}), //$NON-NLS-1$
			null));
	}
	String path = location.toOSString();

	// Open the file
	
	// ShellCommand was removed in response to PR 23888.  If an exception was 
	// thrown, it was not caught in time, and no feedback was given to user
	
	try {
		Runtime.getRuntime().exec(new String[]{programFileName, path});
	} catch (Exception e) {
		throw new CoreException(new Status(
			Status.ERROR, 
			WorkbenchPlugin.PI_WORKBENCH, 
			0, 
			WorkbenchMessages.format("ExternalEditor.errorMessage", new Object[] {programFileName}), //$NON-NLS-1$
			e));
	}
}
}
