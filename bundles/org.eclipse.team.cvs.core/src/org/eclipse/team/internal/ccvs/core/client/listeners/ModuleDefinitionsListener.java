package org.eclipse.team.internal.ccvs.core.client.listeners;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.client.Checkout;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;

/*
 * This class pares the output of the "cvs checkout -c" command which returns the list of modules 
 * defined in the CVSROOT/modules file.
 */
public class ModuleDefinitionsListener implements ICommandOutputListener {

	// the last line read from the context (used to accumulate multi-line definitions)
	private String lastLine = "";
	
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
		
		StringTokenizer tokenizer = new StringTokenizer(line);
		
		String module = tokenizer.nextToken();
		
		List expansions = new ArrayList(10);
		
		// Read the options associated with the module
		List localOptionsList = new ArrayList();
		String next = tokenizer.nextToken();
		while (next.charAt(0) == '-') {
			switch (next.charAt(1)) {
				case 'a': // alias
					localOptionsList.add(Checkout.ALIAS);
					break;
				case 'l': // don't recurse
					localOptionsList.add(Checkout.DO_NOT_RECURSE);
					break;
				case 'd': // directory
					localOptionsList.add(Checkout.makeDirectoryNameOption(tokenizer.nextToken()));
					break;
				case 'e':
				case 'i':
				case 'o':
				case 't':
				case 'u': // Ignore any programs
					tokenizer.nextToken();
					break;
				case 's': // status
					localOptionsList.add(Checkout.makeStatusOption(tokenizer.nextToken()));
					break;
				default: // unanticipated option. Ignore it and go on
			}
			next = tokenizer.nextToken();
		}
		LocalOption[] localOptions = (LocalOption[]) localOptionsList.toArray(new LocalOption[localOptionsList.size()]);
		
		if (Checkout.ALIAS.isElementOf(localOptions)) {
			// An alias expands to one or more projects or modules
			expansions.add(next);
			while (tokenizer.hasMoreTokens())
				expansions.add(tokenizer.nextToken());
		} else {
			// The module definition can have a leading directory followed by some files
			if (!(next.charAt(0) == '&')) {
				String directory = next;
				List files = new ArrayList();
				while (tokenizer.hasMoreTokens() && (next.charAt(0) != '&')) {
					next = tokenizer.nextToken() ;
					if ((next.charAt(0) != '&'))
						files.add(next);
				}
				if (files.size() > 0) {
					for (int i=0;i<files.size();i++)
						expansions.add(directory + "/" + (String)files.get(i));
				}
				else
					expansions.add(directory);
			} 
			// It can also have one or more module references
			if (next.charAt(0) == '&') {
				expansions.add(next);
				while (tokenizer.hasMoreTokens())
					expansions.add(tokenizer.nextToken());
			}
		}
		moduleMap.put(module, new ModuleExpansion(module, (String[])expansions.toArray(new String[expansions.size()]), localOptions));
		return OK;
	}

	/*
	 * @see ICommandOutputListener#errorLine(String, ICVSFolder, IProgressMonitor)
	 */
	public IStatus errorLine(
		String line,
		ICVSFolder commandRoot,
		IProgressMonitor monitor) {
			
		// XXX We should get any errors!!!
		return OK;
	}
	
	public ModuleExpansion[] getModuleExpansions() {
		return (ModuleExpansion[])moduleMap.values().toArray(new ModuleExpansion[moduleMap.size()]);
	}

	public void reset() {
		this.moduleMap = new HashMap();
	}
}
