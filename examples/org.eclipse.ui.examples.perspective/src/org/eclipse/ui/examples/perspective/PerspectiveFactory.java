package org.eclipse.ui.examples.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class PerspectiveFactory implements IPerspectiveFactory {

	String ID_NAVIGATOR_FOLDER_VIEW= "org.eclipse.ui.examples.NavigatorFolderView"; //$NON-NLS-1$
	String ID_TOOLS_FOLDER_VIEW= "org.eclipse.ui.examples.ToolsFolderView"; //$NON-NLS-1$
	
	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();

		IFolderLayout navFolder = layout.createFolder(ID_NAVIGATOR_FOLDER_VIEW, IPageLayout.LEFT, (float) 0.25, editorArea);
		navFolder.addView(IPageLayout.ID_PROJECT_EXPLORER);
		
		IFolderLayout toolsFolder = layout.createFolder(ID_TOOLS_FOLDER_VIEW, IPageLayout.BOTTOM, (float) 0.75, editorArea);
		toolsFolder.addView(IPageLayout.ID_PROBLEM_VIEW);
	}
	
//	IPerspectiveDescriptor desc = PlatformUI.getWorkbench().getPerspectiveRegistry()
//			.findPerspectiveWithId(persp.getElementId());
//	helpful classes
//	PerspectiveDescriptor
//	and
//	PerspectiveExtensionReader

}
