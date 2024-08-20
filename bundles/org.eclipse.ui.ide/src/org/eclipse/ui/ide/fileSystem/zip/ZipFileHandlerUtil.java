package org.eclipse.ui.ide.fileSystem.zip;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * @since 3.23
 *
 */
public class ZipFileHandlerUtil {
	public static void refreshAllViewers() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				for (IViewReference viewReference : page.getViewReferences()) {
					IWorkbenchPart part = viewReference.getPart(false);
					if (part != null) {
						ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
						if (selectionProvider instanceof StructuredViewer viewer) {
							if (viewer.getControl() != null && !viewer.getControl().isDisposed()) {
								viewer.refresh();
							}
						}
					}
				}
			}
		}
	}
}
