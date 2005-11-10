package org.eclipse.jface.databinding.internal;

import org.eclipse.jface.databinding.IChangeListener;

/**
 * @since 3.2
 *
 */
abstract public class Binding implements IChangeListener {

	protected final DataBindingContext context;

	/**
	 * @param context
	 */
	public Binding(DataBindingContext context) {
		this.context = context;
	}
	
	/**
	 * 
	 */
	abstract public void updateTargetFromModel();

}
