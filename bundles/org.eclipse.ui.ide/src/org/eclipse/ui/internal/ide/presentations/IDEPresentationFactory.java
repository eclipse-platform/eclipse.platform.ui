package org.eclipse.ui.internal.ide.presentations;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;

public class IDEPresentationFactory extends AbstractPresentationFactory {

    public StackPresentation createPartPresentation(Composite parent,
            IStackPresentationSite site, int role, int flags,
            String perspectiveId, String folderId) {
        return null;
    }
}