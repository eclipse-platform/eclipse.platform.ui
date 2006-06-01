package org.eclipse.team.examples.filesystem.history;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.history.provider.FileHistoryProvider;

public class FileSystemHistoryProvider extends FileHistoryProvider {

	public IFileHistory getFileHistoryFor(IResource resource, int flags, IProgressMonitor monitor) {
		return null;
	}

	public IFileHistory getFileHistoryFor(IFileStore store, int flags, IProgressMonitor monitor) {
		return null;
	}

	public IFileRevision getWorkspaceFileRevision(IResource resource) {
		return null;
	}

}
