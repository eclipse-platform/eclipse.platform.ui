package org.eclipse.ui.internal.part;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

public interface IPartHost {
	public IWorkbenchPartSite getSite();
	public IEditorPart getActiveEditor();
	public IWorkbenchPart getActivePart();
}
