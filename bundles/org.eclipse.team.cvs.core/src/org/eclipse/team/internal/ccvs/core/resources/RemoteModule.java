package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.client.Checkout;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;

/**
 * @version 	1.0
 * @author
 */
public class RemoteModule extends RemoteFolder {

	public static final String VIRTUAL_DIRECTORY = "CVSROOT/Emptydir";
	
	private String label;
	private RemoteModule[] referencedModules;
	private LocalOption[] localOption;
	
	/**
	 * Create a set of RemoteModules from the provided module definition strings returned from the server
	 * 
	 */
	public static RemoteModule[] createRemoteModules(String[] moduleDefinitionStrings, ICVSRepositoryLocation repository, CVSTag tag) {
		
		Map modules = new HashMap();
		Map referencedModulesTable = new HashMap();
		
		// First pass: Create the remote module instances based on remote mapping
		for (int i = 0; i < moduleDefinitionStrings.length; i++) {
			
			// Read the module name
			StringTokenizer tokenizer = new StringTokenizer(moduleDefinitionStrings[i]);
			String moduleName = tokenizer.nextToken();
			
			// Read the options associated with the module
			List localOptionsList = new ArrayList();
			String next = tokenizer.nextToken();
			String localName = null;
			while (next.charAt(0) == '-') {
				switch (next.charAt(1)) {
					case 'a': // alias
						localOptionsList.add(Checkout.ALIAS);
						break;
					case 'l': // don't recurse
						localOptionsList.add(Checkout.DO_NOT_RECURSE);
						break;
					case 'd': // directory
						localName = tokenizer.nextToken();
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
				
				if (localOptions.length > 1) {
					// XXX This is an error condition that needs to be reported
				}
				
				// An alias expands to one or more modules or paths
				List expansions = new ArrayList(10);
				expansions.add(next);
				while (tokenizer.hasMoreTokens())
					expansions.add(tokenizer.nextToken());
					
				// XXX Need to create a remote module representing the alias!!!
				
			} else {
				
				// The module definition must have a leading directory which can be followed by some files
				if (!(next.charAt(0) == '&')) {
					String directory = next;
					List files = new ArrayList();
					while (tokenizer.hasMoreTokens() && (next.charAt(0) != '&')) {
						next = tokenizer.nextToken() ;
						if ((next.charAt(0) != '&'))
							files.add(next);
					}
					RemoteModule remoteModule = new RemoteModule(moduleName, null, localName, repository, new Path(directory), tag, ! files.isEmpty());
					modules.put(moduleName, remoteModule);
					if ( ! files.isEmpty()) {
						ICVSRemoteResource[] children = new ICVSRemoteResource[files.size()];
						for (int j = 0; j < children.length; j++) {
							children[j] = new RemoteFile(remoteModule, (String)files.get(j), tag);
							remoteModule.setChildren(children);
						}
					}
				} else {
					modules.put(moduleName, new RemoteModule(moduleName, null, localName, repository, null, tag, true));
				}
				
				// Record any referenced modules so that can be cross-referenced below
				if (next.charAt(0) == '&') {
					List children = new ArrayList(10);
					children.add(next);
					while (tokenizer.hasMoreTokens())
						children.add(tokenizer.nextToken());
					referencedModulesTable.put(moduleName, (String[])children.toArray(new String[children.size()]));
				}
			}
		}
		
		// Second pass: Cross reference remote modules where necessary
		Iterator iter = modules.keySet().iterator();
		while (iter.hasNext()) {
			String moduleName = (String)iter.next();
			String[] children = (String[])referencedModulesTable.get(moduleName);
			if (children != null) {
				RemoteModule module = (RemoteModule)modules.get(moduleName);
				RemoteModule[] referencedModules = new RemoteModule[children.length];
				for (int i = 0; i < referencedModules.length; i++) {
					referencedModules[i] = (RemoteModule)modules.get(children[i]);
				}
				module.setReferencedModules(referencedModules);
			}
		}
						
		return (RemoteModule[])modules.values().toArray(new RemoteModule[modules.size()]);
	}
		
	public RemoteModule(String label, RemoteFolder parent, String localName, ICVSRepositoryLocation repository, IPath repositoryRelativePath, CVSTag tag, boolean isStatic) {
		super(parent, 
			localName == null ? label : localName, 
			repository, 
			repositoryRelativePath == null ? new Path(VIRTUAL_DIRECTORY) : repositoryRelativePath, 
			tag, 
			isStatic);
		this.label = label;
	}
	
	/* 
	 * Override of inherited getMembers in order to combine the physical members with any referenced modules
	 */
	public ICVSRemoteResource[] getMembers(CVSTag tagName, IProgressMonitor monitor) throws TeamException {
		ICVSRemoteResource[] physicalChildren;
		if ( folderInfo.getIsStatic()) {
			physicalChildren = getChildren();
		} else {
			physicalChildren = super.getMembers(tagName, monitor);
		}
		ICVSRemoteResource[] allChildren;
		if (referencedModules != null && referencedModules.length > 0) {
			if (physicalChildren == null) {
				allChildren = referencedModules;
			} else {
				// Combine two sets of children
				allChildren = new ICVSRemoteResource[physicalChildren.length + referencedModules.length];
			}
		} else if (physicalChildren != null) {
			allChildren = physicalChildren;
		} else {
			allChildren = new ICVSRemoteResource[0];
		}
		return allChildren;
	}
	
	/*
	 * Set the children to a static set of children
	 */
	protected void setChildren(ICVSRemoteResource[] children) {
		super.setChildren(children);
		if ( ! folderInfo.getIsStatic())
			this.folderInfo = new FolderSyncInfo(folderInfo.getRepository(), folderInfo.getRoot(), folderInfo.getTag(), true);
	}
	
	private void setReferencedModules(RemoteModule[] referencedModules) {
		this.referencedModules = referencedModules;
	}
	
}
