package org.eclipse.ui.internal.progress;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * The GroupInfoItem is the item for displaying groups of 
 * JobInfos.
 * @since 3.1
 *
 */
class GroupInfoItem extends ProgressInfoItem {

	private GroupInfo info;

	/**
	 * Create a new instance of the receiver for a JobInfo.
	 * 
	 * @param parent
	 * @param style
	 * @param groupInfo
	 */
	public GroupInfoItem(Composite parent, int style, GroupInfo groupInfo) {
		super(parent, style);
		info = groupInfo;
		createChildren();
		setData(info);
		setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

	}

	/**
	 * Create the children of the receiver.
	 *
	 */
	private void createChildren() {
		
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.ProgressInfoItem#remap(org.eclipse.ui.internal.progress.JobTreeElement)
	 */
	void remap(JobTreeElement element) {
		info = (GroupInfo) element;
		refresh();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.ProgressInfoItem#refresh()
	 */
	void refresh() {
	}
}
