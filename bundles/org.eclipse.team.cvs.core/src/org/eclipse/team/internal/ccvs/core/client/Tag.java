package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.ccvs.core.CVSStatus;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.client.listeners.ICommandOutputListener;
import org.eclipse.team.internal.ccvs.core.client.listeners.TagListener;

public class Tag extends Command {
	/*** Local options: specific to tag ***/
	public static final LocalOption CREATE_BRANCH = new LocalOption("-b", null);	 //$NON-NLS-1$	

	/*** Default command output listener ***/
	private static final ICommandOutputListener DEFAULT_OUTPUT_LISTENER = new TagListener();
	
	// handle added and removed resources in a special way
	private boolean customBehaviorEnabled;
	
	protected Tag(boolean customBehaviorEnabled) {
		this.customBehaviorEnabled = customBehaviorEnabled;
	}
	
	protected Tag() {
		this(false);
	}
	
	protected String getRequestId() {
		return "tag"; //$NON-NLS-1$
	}

	protected ICVSResource[] computeWorkResources(Session session, LocalOption[] localOptions,
		String[] arguments) throws CVSException {
			
		if (arguments.length < 1) throw new IllegalArgumentException();
		String[] allButFirst = new String[arguments.length - 1];
		System.arraycopy(arguments, 1, allButFirst, 0, arguments.length - 1);
		return super.computeWorkResources(session, localOptions, allButFirst);
	}

	public IStatus execute(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, CVSTag tag, String[] arguments, ICommandOutputListener listener,
		IProgressMonitor monitor) throws CVSException {
		
		if(tag.getType() != CVSTag.VERSION && tag.getType() != CVSTag.BRANCH) {
			throw new CVSException(new CVSStatus(IStatus.ERROR, Policy.bind("Tag.notVersionOrBranchError"))); //$NON-NLS-1$
		}
		
		// Add the CREATE_BRANCH option for a branch tag
		if (tag.getType() == tag.BRANCH) {
			if ( ! CREATE_BRANCH.isElementOf(localOptions)) {
				LocalOption[] newLocalOptions = new LocalOption[localOptions.length + 1];
				System.arraycopy(localOptions, 0, newLocalOptions, 0, localOptions.length);
				newLocalOptions[newLocalOptions.length - 1] = CREATE_BRANCH;
				localOptions = newLocalOptions;
			}
		}
		
		// Add the tag name to the start of the arguments
		String[] newArguments = new String[arguments.length + 1];
		newArguments[0] = tag.getName();
		System.arraycopy(arguments, 0, newArguments, 1, arguments.length);
		
		return execute(session, globalOptions, localOptions, newArguments, listener, monitor);	
	}

	protected ICommandOutputListener getDefaultCommandOutputListener() {
		return DEFAULT_OUTPUT_LISTENER;
	}
		
	protected void sendLocalResourceState(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, ICVSResource[] resources, IProgressMonitor monitor)
		throws CVSException {			

		// Send all folders that are already managed to the server
		if (customBehaviorEnabled) {
			new TagFileSender(session, monitor).visit(session, resources);
		} else {
			new FileStructureVisitor(session, false, false, monitor).visit(session, resources);
		}
	}
}