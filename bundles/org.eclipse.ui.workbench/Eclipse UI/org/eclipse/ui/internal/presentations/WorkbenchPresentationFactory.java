package org.eclipse.ui.internal.presentations;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;

public class WorkbenchPresentationFactory extends AbstractPresentationFactory {

    public StackPresentation createPartPresentation(Composite parent,
            IStackPresentationSite site, int role, int flags,
            String perspectiveId, String folderId) {
        return null;
    }
}