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

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.Checkout;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;

public class RemoteModule extends RemoteFolder {
	
	private String label;
	private ICVSRemoteResource[] referencedModules;
	private LocalOption[] localOptions;
	private boolean expandable;
	
	public static RemoteModule[] getRemoteModules(ICVSRepositoryLocation repository, CVSTag tag, IProgressMonitor monitor) throws TeamException {
		monitor = Policy.monitorFor(monitor);
		monitor.beginTask(Policy.bind("RemoteModule.getRemoteModules"), 100); //$NON-NLS-1$
		try {		
			RemoteModule[] modules;
			Session s = new Session(repository, (ICVSFolder)CVSWorkspaceRoot.getCVSResourceFor(ResourcesPlugin.getWorkspace().getRoot()), false);
			s.open(Policy.subMonitorFor(monitor, 10));
			try {
				modules = Command.CHECKOUT.getRemoteModules(s, tag, Policy.subMonitorFor(monitor, 90));
			} finally {
				s.close();
			}
			return modules;
		} finally {
			monitor.done();
		}
	}
	
	/**
	 * Create a set of RemoteModules from the provided module definition strings returned from the server
	 * 
	 * At the moment, we are very restrictive on the types of modules we support.
	 */
	public static RemoteModule[] createRemoteModules(String[] moduleDefinitionStrings, ICVSRepositoryLocation repository, CVSTag tag) {
		
		Map modules = new HashMap();
		Map referencedModulesTable = new HashMap();
		Map moduleAliases = new HashMap();
		List acceptableModules = new ArrayList();
		
		// First pass: Create the remote module instances based on remote mapping
		for (int i = 0; i < moduleDefinitionStrings.length; i++) {
			
			// Read the module name
			StringTokenizer tokenizer = new StringTokenizer(moduleDefinitionStrings[i]);
			String moduleName = tokenizer.nextToken();
			if ( ! tokenizer.hasMoreTokens()) {
				// If it only has a name and no tokens, skip it.
				continue;
			}
			
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
				
				if (localOptions.length > 1) {
					// XXX This is an error condition that needs to be reported
				}
				
				// An alias expands to one or more modules or paths
				List expansions = new ArrayList(10);
				expansions.add(next);
				while (tokenizer.hasMoreTokens())
					expansions.add(tokenizer.nextToken());
					
				moduleAliases.put(moduleName, (String[]) expansions.toArray(new String[expansions.size()]));
				modules.put(moduleName, new RemoteModule(moduleName, null, repository, null, localOptions, tag, true));

			} else {
				
				// The module definition may have a leading directory which can be followed by some files
				if (!(next.charAt(0) == '&')) {
					String directory = next;
					List files = new ArrayList();
					while (tokenizer.hasMoreTokens() && (next.charAt(0) != '&')) {
						next = tokenizer.nextToken() ;
						if ((next.charAt(0) != '&'))
							files.add(next);
					}
					RemoteModule remoteModule = new RemoteModule(moduleName, null, repository, new Path(directory), localOptions, tag, ! files.isEmpty());
					modules.put(moduleName, remoteModule);
					if ( ! files.isEmpty()) {
						ICVSRemoteResource[] children = new ICVSRemoteResource[files.size()];
						for (int j = 0; j < children.length; j++) {
							children[j] = new RemoteFile(remoteModule, Update.STATE_NONE, (String)files.get(j), tag);
							remoteModule.setChildren(children);
						}
					}
				} else {
					modules.put(moduleName, new RemoteModule(moduleName, null, repository, null, localOptions, tag, true));
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
		
		// Second pass: Cross reference aliases to modules
		// XXX Aliases can reference other aliases which confuses the expansion!
		Iterator iter = moduleAliases.keySet().iterator();
		while (iter.hasNext()) {
			String moduleName = (String)iter.next();
			RemoteModule module = (RemoteModule)modules.get(moduleName);
			String[] expansion = (String[])moduleAliases.get(moduleName);
			List referencedFolders = new ArrayList();
			boolean expandable = true;
			for (int i = 0; i < expansion.length; i++) {
				if (expansion[i].charAt(0) == '!') {
					// XXX Unsupported for now
					expandable = false;
				} else {
					IPath path = new Path(expansion[i]);
					if (path.segmentCount() > 1) {
						// XXX Unsupported for now
						expandable = false;
					} else {
						RemoteModule child = (RemoteModule)modules.get(expansion[i]);
						if (child == null) {
							referencedFolders.add(new RemoteFolder(null, repository, path, tag));
						} else {
							// Need to check if the child is a module alias
							if (child.isAlias()) {
								// XXX Unsupported for now
								expandable = false;
							} else {
								 referencedFolders.add(child);
							}
						}
					}
				}
			}
			if (expandable) {
				module.setChildren((ICVSRemoteResource[]) referencedFolders.toArray(new ICVSRemoteResource[referencedFolders.size()]));
			} else {
				module.setExpandable(false);
			}
		}
		
		// Third pass: Cross reference remote modules where necessary
		iter = modules.keySet().iterator();
		while (iter.hasNext()) {
			String moduleName = (String)iter.next();
			String[] children = (String[])referencedModulesTable.get(moduleName);
			if (children != null) {
				RemoteModule module = (RemoteModule)modules.get(moduleName);
				List referencedFolders = new ArrayList();
				boolean expandable = true;
				for (int i = 0; i < children.length; i++) {
					RemoteModule child = (RemoteModule)modules.get(children[i].substring(1));
					if (child.isAlias()) {
						// Include alias children in-line
						expandable = false;
//						referencedFolders.addAll(Arrays.asList(child.getChildren()));
					} else {
						// XXX not expandable if child has local directory option (-d)
						if (Command.findOption(child.getLocalOptions(), "-d") != null) { //$NON-NLS-1$
							expandable = false;
						} else {
							referencedFolders.add(child);
						}
					}
				}
				if (expandable) {
					module.setReferencedModules((ICVSRemoteResource[]) referencedFolders.toArray(new ICVSRemoteResource[referencedFolders.size()]));
				} else {
					module.setExpandable(false);
				}
			}
		}
						
		return (RemoteModule[])modules.values().toArray(new RemoteModule[modules.size()]);
	}
		
	public RemoteModule(String label, RemoteFolder parent, ICVSRepositoryLocation repository, IPath repositoryRelativePath, LocalOption[] localOptions, CVSTag tag, boolean isStatic) {
		super(parent, 
			label, 
			repository, 
			repositoryRelativePath == null ? new Path(FolderSyncInfo.VIRTUAL_DIRECTORY) : repositoryRelativePath, 
			tag, 
			isStatic);
		this.localOptions = localOptions;
		this.label = label;
		this.expandable = true;
	}
	
	public LocalOption[] getLocalOptions() {
		return localOptions;
	}
	/* 
	 * Override of inherited getMembers in order to combine the physical members with any referenced modules
	 */
	public ICVSRemoteResource[] getMembers(CVSTag tagName, IProgressMonitor monitor) throws CVSException {
		
		if ( ! expandable) return new ICVSRemoteResource[0];
		
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
				for (int i = 0; i < physicalChildren.length; i++) {
					allChildren[i] = physicalChildren[i];
				}
				for (int i = 0; i < referencedModules.length; i++) {
					allChildren[i + physicalChildren.length] = referencedModules[i];
				}
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
	
	private void setReferencedModules(ICVSRemoteResource[] referencedModules) {
		this.referencedModules = referencedModules;
	}
	
	public boolean isAlias() {
		return Checkout.ALIAS.isElementOf(localOptions);
	}
	
	/**
	 * @see ICVSRemoteFolder#isExpandable()
	 */
	public boolean isExpandable() {
		return expandable;
	}
	
	private void setExpandable(boolean expandable) {
		this.expandable = expandable;
	}
	
}
