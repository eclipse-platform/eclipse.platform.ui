package org.eclipse.ui.tutorials.rcp.part3;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.tutorials.rcp.part3.views.SampleView;

public class RcpPerspective implements IPerspectiveFactory {
    public static final String ID_PERSPECTIVE = "org.eclipse.ui.tutorials.rcp.part3.RcpPerspective"; //$NON-NLS-1$

    public RcpPerspective() {
    }

    public void createInitialLayout(IPageLayout layout) {
        layout.setEditorAreaVisible(false);
        layout.addView(
            SampleView.ID_VIEW,
            IPageLayout.TOP,
            IPageLayout.RATIO_MAX,
            IPageLayout.ID_EDITOR_AREA);
        layout.addPerspectiveShortcut(ID_PERSPECTIVE);
        layout.addShowViewShortcut(SampleView.ID_VIEW);
    }
}
