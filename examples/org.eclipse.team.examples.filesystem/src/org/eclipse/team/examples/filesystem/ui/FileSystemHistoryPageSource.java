package org.eclipse.team.examples.filesystem.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.examples.filesystem.FileSystemProvider;
import org.eclipse.team.ui.history.HistoryPageSource;
import org.eclipse.ui.part.Page;

public class FileSystemHistoryPageSource extends HistoryPageSource {

	public boolean canShowHistoryFor(Object object) {
		if (object instanceof IResource && ((IResource) object).getType() == IResource.FILE) {
			RepositoryProvider provider = RepositoryProvider.getProvider(((IFile) object).getProject());
			if (provider instanceof FileSystemProvider)
				return true;
		}

		return false;
	}

	public Page createPage(Object object) {
		FileSystemHistoryPage page = new FileSystemHistoryPage();
		return page;
	}

}
