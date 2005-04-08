package org.eclipse.team.tests.ccvs.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class PerspectiveFactory implements IPerspectiveFactory {

    public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();
        IFolderLayout top =
            layout.createFolder("top", IPageLayout.LEFT, 0.40f, editorArea);    //$NON-NLS-1$
        layout.setEditorAreaVisible(true);
    }

}
