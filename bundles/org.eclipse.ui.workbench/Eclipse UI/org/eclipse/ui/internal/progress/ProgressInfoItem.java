package org.eclipse.ui.internal.progress;

import org.eclipse.swt.widgets.Composite;

/**
 * JobTreeElementInfoItem is the abstract superclass of 
 * items for displaying jobs and groups of jobs.
 * @since 3.1
 *
 */
abstract class ProgressInfoItem extends Composite {

	/**
	 * Create a new instance of the receiver.
	 * @param parent
	 * @param style
	 */
	public ProgressInfoItem(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Remap the receiver to display element.
	 * @param element
	 */
	abstract void remap(JobTreeElement element);
	
	/**
	 * Refresh the receiver.
	 */
	abstract void refresh();

	/**
	 * Set the color based on the index.
	 * 
	 * @param i
	 */
	void setColor(int i){
		//Do nothing by default
	}

}
