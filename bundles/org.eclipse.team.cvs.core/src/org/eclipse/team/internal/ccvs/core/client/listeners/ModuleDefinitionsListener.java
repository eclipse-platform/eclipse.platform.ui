package org.eclipse.team.internal.ccvs.core.client.listeners;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;

/*
 * This class pares the output of the "cvs checkout -c" command which returns the list of modules 
 * defined in the CVSROOT/modules file.
 */
public class ModuleDefinitionsListener implements ICommandOutputListener {

	// the last line read from the context (used to accumulate multi-line definitions)
	private String lastLine = ""; //$NON-NLS-1$
	
	private Map moduleMap;
	
	public ModuleDefinitionsListener() {
		reset();
	}
	
	/*
	 * @see ICommandOutputListener#messageLine(String, ICVSFolder, IProgressMonitor)
	 */
	public IStatus messageLine(
		String line,
		ICVSFolder commandRoot,
		IProgressMonitor monitor) {
		
		// Lines that start with a space indicate a multi line entry
		if( line.charAt(0) == ' ' ) {
			lastLine += line;
			line = lastLine;
		}
		else
			lastLine = line;
		
		// Use the module name as the key so that multi-line modules will be recorded properly
		int firstSpace = line.indexOf(" "); //$NON-NLS-1$
		if (firstSpace > -1) {
			String module = line.substring(0, firstSpace);
			moduleMap.put(module, line);
		}
		return OK;
	}

	/*
	 * @see ICommandOutputListener#errorLine(String, ICVSFolder, IProgressMonitor)
	 */
	public IStatus errorLine(String line, ICVSFolder commandRoot, IProgressMonitor monitor) {	
		return new CVSStatus(CVSStatus.ERROR, CVSStatus.ERROR_LINE, commandRoot, line);
	}
	
	public String[] getModuleExpansions() {
		return (String[])moduleMap.values().toArray(new String[moduleMap.size()]);
	}

	public void reset() {
		this.moduleMap = new HashMap();
	}
}
