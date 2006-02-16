package org.eclipse.debug.internal.ui.views.launch;

import org.eclipse.debug.internal.ui.model.viewers.AsynchronousModel;
import org.eclipse.debug.internal.ui.model.viewers.AsynchronousTreeModelViewer;
import org.eclipse.debug.internal.ui.model.viewers.IModelUpdatePolicy;
import org.eclipse.swt.widgets.Composite;

public class LaunchViewer extends AsynchronousTreeModelViewer {
	
	private LaunchView fView;

	public LaunchViewer(Composite parent, LaunchView view) {
		super(parent);
		fView = view;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.viewers.AsynchronousViewer#isSuppressEqualSelections()
	 */
	protected boolean isSuppressEqualSelections() {
		// fire activation changes all the time
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousModelViewer#createUpdatePolicy()
	 */
	public IModelUpdatePolicy createUpdatePolicy() {
		IModelUpdatePolicy policy = new LaunchViewUpdatePolicy(fView);
		policy.init(this);
		return policy;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.AsynchronousModelViewer#createModel()
     */
    protected AsynchronousModel createModel() {
        return new LaunchTreeModel(this);
    }	

}
