package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.client.listeners.ICommandOutputListener;
import org.eclipse.team.internal.ccvs.core.client.listeners.UpdateListener;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResourceVisitor;

public class Update extends Command {
	/*** Local options: specific to update ***/
	public static final LocalOption CLEAR_STICKY = new LocalOption("-A"); //$NON-NLS-1$
	public static final LocalOption IGNORE_LOCAL_CHANGES = new LocalOption("-C"); //$NON-NLS-1$
	public static final LocalOption RETRIEVE_ABSENT_DIRECTORIES = new LocalOption("-d"); //$NON-NLS-1$
	
	/*** Default command output listener ***/
	private static final ICommandOutputListener DEFAULT_OUTPUT_LISTENER = new UpdateListener(null);

	/**
	 * Makes a -r or -D or -A option for a tag.
	 * Valid for: checkout export history rdiff update
	 */
	public static LocalOption makeTagOption(CVSTag tag) {
		int type = tag.getType();
		switch (type) {
			case CVSTag.HEAD:
				return CLEAR_STICKY;
			default:
				return Command.makeTagOption(tag);
		}
	}
	
	protected Update() { }
	protected String getCommandId() {
		return "update"; //$NON-NLS-1$
	}
	
	protected ICommandOutputListener getDefaultCommandOutputListener() {
		return DEFAULT_OUTPUT_LISTENER;
	}
	
	protected void sendLocalResourceState(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, ICVSResource[] resources, IProgressMonitor monitor)
		throws CVSException {			
		
		// Send all folders that are already managed to the server
		// even folders that are empty
		sendFileStructure(session, resources, false, true, monitor);
	}
	
	/**
	 * On successful finish, prune empty directories if the -P or -D option was specified.
	 */
	protected void commandFinished(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, ICVSResource[] resources, IProgressMonitor monitor,
		boolean succeeded) throws CVSException {
		// If we didn't succeed, don't do any post processing
		if (! succeeded) return;

		// If we are pruning (-P) or getting a sticky copy using -D, then prune empty directories
		if (PRUNE_EMPTY_DIRECTORIES.isElementOf(localOptions) ||
			findOption(localOptions, "-D") != null) { //$NON-NLS-1$
			// Delete empty directories
			ICVSResourceVisitor visitor = new PruneFolderVisitor(session);
			for (int i = 0; i < resources.length; i++) {
				resources[i].accept(visitor);
			}
			
		}	
	}
}


