package org.eclipse.ui.views.markers.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;

/**
 * The FilterEnablementAction is an action for enabling 
 * or disabling a filter.
 *
 */
class FilterEnablementAction extends Action {

	private MarkerFilter markerFilter;
	private MarkerView markerView;

	/**
	 * Create a new action for the filter.
	 * @param filter
	 * @param view
	 */
	public FilterEnablementAction(MarkerFilter filter, MarkerView view) {
		super(filter.getName(),SWT.CHECK);
		setChecked(filter.isEnabled());
		markerFilter = filter;
		markerView = view;
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		markerFilter.setEnabled(!markerFilter.isEnabled());
		setChecked(markerFilter.isEnabled());
		markerView.updateForFilterChanges();
		markerView.refresh();
	}


}
