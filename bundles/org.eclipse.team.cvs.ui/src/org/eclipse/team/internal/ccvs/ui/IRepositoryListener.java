package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.team.ccvs.core.IRemoteRoot;
import org.eclipse.team.internal.ccvs.ui.model.Tag;

public interface IRepositoryListener {
	public void repositoryAdded(IRemoteRoot root);
	public void repositoryRemoved(IRemoteRoot root);
	public void tagAdded(Tag tag, IRemoteRoot root);
	public void tagRemoved(Tag tag, IRemoteRoot root);
}

