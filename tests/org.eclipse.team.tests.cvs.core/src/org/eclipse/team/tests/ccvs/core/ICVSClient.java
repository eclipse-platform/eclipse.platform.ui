package org.eclipse.team.tests.ccvs.core;

import org.eclipse.core.resources.IContainer;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;

public interface ICVSClient {
	public void executeCommand(
		ICVSRepositoryLocation repositoryLocation, IContainer localRoot, String command,
		String[] globalOptions, String[] localOptions, String[] arguments)
		throws CVSException;
}
