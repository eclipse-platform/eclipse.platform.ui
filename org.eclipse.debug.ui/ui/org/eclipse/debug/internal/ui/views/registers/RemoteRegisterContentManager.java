package org.eclipse.debug.internal.ui.views.registers;

import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.views.RemoteTreeViewer;
import org.eclipse.debug.internal.ui.views.variables.RemoteVariableContentManager;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;

public class RemoteRegisterContentManager extends RemoteVariableContentManager {
    public RemoteRegisterContentManager(ITreeContentProvider provider, RemoteTreeViewer viewer, IWorkbenchPartSite site, VariablesView view) {
        super(provider, viewer, site, view);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.views.variables.RemoteVariableContentManager#getAdapter(java.lang.Object)
     */
    protected IDeferredWorkbenchAdapter getAdapter(Object element) {
        if (element instanceof IStackFrame) {
            return new DeferredRegisterViewStackFrame();
        }
        return super.getAdapter(element);
    }    
}
