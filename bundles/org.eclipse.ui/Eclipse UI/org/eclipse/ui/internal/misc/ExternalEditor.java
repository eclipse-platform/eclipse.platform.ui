package org.eclipse.ui.internal.misc;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.plugins.*;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.*;
import org.eclipse.swt.program.Program;
import java.net.*;
import java.io.*;

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
 * @exception	Throws a ExtensionException if the external editor could not be opened.
 */
public void open() throws CoreException {

	Program program = this.descriptor.getProgram();
	if (program == null)
		openWithUserDefinedProgram();
	else {
		String path = file.getLocation().toOSString();
		if (!program.execute(path)) 
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
 * @exception	Throws a ExtensionException if the external editor could not be opened.
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
	String path = file.getLocation().toOSString();

	// Open the file
	ShellCommand oCommand = new ShellCommand(new String[]{programFileName, path}, false);
	oCommand.run();
	if ((oCommand.getRetCode() != 0) || (oCommand.getException() != null))
		throw new CoreException(new Status(
			Status.ERROR, 
			WorkbenchPlugin.PI_WORKBENCH, 
			oCommand.getRetCode(), 
			WorkbenchMessages.format("ExternalEditor.errorMessage", new Object[] {path}), //$NON-NLS-1$
			oCommand.getException()));
}
}
