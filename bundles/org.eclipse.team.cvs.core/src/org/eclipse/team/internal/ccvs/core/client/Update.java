package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.client.listeners.ICommandOutputListener;
import org.eclipse.team.internal.ccvs.core.client.listeners.UpdateListener;

public class Update extends Command {
	/*** Local options: specific to update ***/
	public static final LocalOption CLEAR_STICKY = new LocalOption("-A"); //$NON-NLS-1$
	public static final LocalOption IGNORE_LOCAL_CHANGES = new LocalOption("-C"); //$NON-NLS-1$
	public static final LocalOption RETRIEVE_ABSENT_DIRECTORIES = new LocalOption("-d"); //$NON-NLS-1$
	public static final LocalOption JOIN = new LocalOption("-j"); //$NON-NLS-1$
	
	/*** Default command output listener ***/
	private static final ICommandOutputListener DEFAULT_OUTPUT_LISTENER = new UpdateListener(null);
	
	/*** File information status returned from update ***/
	public static final int STATE_NONE = 0;							// no state information available
	public static final int STATE_ADDED_LOCAL = 1; 			// new file locally that was added but not comitted to server yet
	public static final int STATE_UNKOWN = 2; 						// new file locally but not added to server
	public static final int STATE_REMOTE_CHANGES = 3; 		// remote changes to an unmodified local file
	public static final int STATE_DELETED = 4; 						// removed locally but still exists on the server
	public static final int STATE_MODIFIED = 5; 					// modified locally
	public static final int STATE_CONFLICT = 6; 					// modified locally and on the server but cannot be auto-merged
	public static final int STATE_MERGEABLE_CONFLICT = 7;  // modified locally and on the server but can be auto-merged

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
	protected String getRequestId() {
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
		sendFileStructure(session, resources, true, monitor);
	}
	
	/**
	 * Convenience method that allows the creation of .# files to be disabled.
	 * @param createBackups if true, creates .# files
	 * @see Command.execute
	 */
	public final IStatus execute(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, String[] arguments, ICommandOutputListener listener,
		IProgressMonitor pm, boolean createBackups) throws CVSException {
		session.setCreateBackups(createBackups);
		try {
			return super.execute(session, globalOptions, localOptions, arguments, listener, pm);
		} finally {
			session.setCreateBackups(true);
		}
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
	
	protected LocalOption[] filterLocalOptions(Session session, GlobalOption[] globalOptions, LocalOption[] localOptions) {
		List newOptions = new ArrayList(Arrays.asList(localOptions));
		
		// Look for absent directories if enabled and the option is not already included
		if (CVSProviderPlugin.getPlugin().getFetchAbsentDirectories() && ! RETRIEVE_ABSENT_DIRECTORIES.isElementOf(localOptions)) {
			newOptions.add(Update.RETRIEVE_ABSENT_DIRECTORIES);
		}
		
		// Prune empty directories if pruning is enabled and the command in not being run in non-update mode
		if (CVSProviderPlugin.getPlugin().getPruneEmptyDirectories() && ! PRUNE_EMPTY_DIRECTORIES.isElementOf(localOptions)) {
			if (! DO_NOT_CHANGE.isElementOf(globalOptions)) {
				newOptions.add(Update.PRUNE_EMPTY_DIRECTORIES);
			}
		}
		localOptions = (LocalOption[]) newOptions.toArray(new LocalOption[newOptions.size()]);
		return super.filterLocalOptions(session, globalOptions, localOptions);
	}
}