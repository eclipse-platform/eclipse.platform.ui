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
import org.eclipse.team.ccvs.core.*;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;

public class RTag extends Command {
	/*** Local options: specific to tag ***/
	public static final LocalOption CREATE_BRANCH = Tag.CREATE_BRANCH;

	/**
	 * Makes a -r or -D option for a tag.
	 * Valid for: checkout export history rdiff update
	 */
	public static LocalOption makeTagOption(CVSTag tag) {
		int type = tag.getType();
		switch (type) {
			case CVSTag.BRANCH:
			case CVSTag.VERSION:
			case CVSTag.HEAD:
				return new LocalOption("-r", tag.getName()); //$NON-NLS-1$
			case CVSTag.DATE:
				return new LocalOption("-D", tag.getName()); //$NON-NLS-1$
			default:
				// Unknow tag type!!!
				throw new IllegalArgumentException();
		}
	}
	
	protected RTag() { }
	protected String getCommandId() {
		return "rtag"; //$NON-NLS-1$
	}

	protected ICVSResource[] computeWorkResources(Session session, LocalOption[] localOptions,
		String[] arguments) throws CVSException {
		if (arguments.length < 2) throw new IllegalArgumentException();
		return new ICVSResource[0];
	}

	protected void sendLocalResourceState(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, ICVSResource[] resources, IProgressMonitor monitor)
		throws CVSException {
	}
	
	public IStatus execute(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, CVSTag tag, String name, ICVSResource[] resources,
		IProgressMonitor monitor) throws CVSException {
		
		// Add the source tag to the local options
		List modifiedLocalOptions = new ArrayList(localOptions.length + 1);
		if (tag==null) tag = CVSTag.DEFAULT;
		modifiedLocalOptions.addAll(Arrays.asList(localOptions));
		modifiedLocalOptions.add(makeTagOption(tag));
		
		// Build the arguments from the parameters
		List arguments = new ArrayList(resources.length + 1);
		arguments.add(name);
		for (int i = 0; i < resources.length; i++) {
			ICVSResource resource = resources[i];
			arguments.add(resource.getRemoteLocation(null));
		}
		return super.execute(session, globalOptions, 
			(LocalOption[]) modifiedLocalOptions.toArray(new LocalOption[modifiedLocalOptions.size()]), 
			(String[]) arguments.toArray(new String[arguments.size()]), null, monitor);
	}
}