package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.Option;
import org.eclipse.team.internal.ccvs.core.client.listeners.ModuleDefinitionsListener;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.resources.RemoteModule;
import org.eclipse.team.internal.ccvs.core.util.Assert;

public class Checkout extends Command {
	/*** Local options: specific to checkout ***/
	public static final LocalOption FETCH_MODULE_ALIASES = new LocalOption("-c");
	public static LocalOption makeDirectoryNameOption(String moduleName) {
		return new LocalOption("-d", moduleName);
	}

	/** Command options found in the CVSROOT/modules file */
	public static LocalOption ALIAS = new LocalOption("-a");
	public static LocalOption makeStatusOption(String status) {
		return new LocalOption("-s", status);
	}
	
	protected Checkout() { }	
	protected String getCommandId() {
		return "co";
	}
	
	protected ICVSResource[] computeWorkResources(Session session, String[] arguments, LocalOption[] localOptions)
		throws CVSException {
		if (arguments.length < 1 && ! FETCH_MODULE_ALIASES.isElementOf(localOptions)) throw new IllegalArgumentException();
		return new ICVSResource[0];
	}
	
	/**
	 * Start the Checkout command:
	 *    Send the module that is going to be checked-out to the server 
	 *    by reading the name of the resource given
	 *    (This has to change to we give it the name of the modul and the
	 *    Checkout creates everything for us)
	 */
	protected void sendLocalResourceState(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, ICVSResource[] resources, IProgressMonitor monitor)
		throws CVSException {			
		
		// We need a folder to put the project(s) we checkout into
		Assert.isTrue(session.getLocalRoot().isFolder());
	}

	protected void sendLocalWorkingDirectory(Session session) throws CVSException {
		session.sendDefaultRootDirectory();
	}

	
	/**
	 * On sucessful finish, prune empty directories if 
	 * the -P option was specified (or is implied by -D or -r)
	 */
	protected void commandFinished(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, ICVSResource[] resources, IProgressMonitor monitor,
		boolean succeeded) throws CVSException {
		// If we didn't succeed, don't do any post processing
		if (! succeeded) return;
	
		// If we are retrieving the modules file, ignore other options
		if (FETCH_MODULE_ALIASES.isElementOf(localOptions)) return;

		// If we are pruning (-P) or getting a sticky copy (-D or -r), then prune empty directories
		if (PRUNE_EMPTY_DIRECTORIES.isElementOf(localOptions) ||
			(findOption(localOptions, "-D") != null) ||
			(findOption(localOptions, "-r") != null)) {				
			// Get the name of the resulting directory
			Option dOption = findOption(localOptions, "-d");
			if (dOption != null) resources = new ICVSResource[] {
				session.getLocalRoot().getFolder(dOption.argument) };

			// Prune empty directories
			ICVSResourceVisitor visitor = new PruneFolderVisitor();
			for (int i=0; i<resources.length; i++) {
				resources[i].accept(visitor);
			}
		}	
	}
	
	/**
	 * Perform a checkout to get the module expansions defined in the CVSROOT/modules file
	 */
	public RemoteModule[] getRemoteModules(Session session, CVSTag tag, IProgressMonitor monitor)
		throws CVSException {
		
		ModuleDefinitionsListener moduleDefinitionListener = new ModuleDefinitionsListener();
		
		IStatus status = execute(session, NO_GLOBAL_OPTIONS, new LocalOption[] {FETCH_MODULE_ALIASES}, NO_ARGUMENTS, 
			moduleDefinitionListener, monitor);
			
		if (status.getCode() == CVSException.SERVER_ERROR) {
			// XXX diff errors??
			throw new CVSServerException(status);
		}
		
		return RemoteModule.createRemoteModules(moduleDefinitionListener.getModuleExpansions(), session.getCVSRepositoryLocation(), tag);
	}
}