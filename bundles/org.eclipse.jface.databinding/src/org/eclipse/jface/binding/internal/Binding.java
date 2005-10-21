package org.eclipse.jface.binding.internal;

import org.eclipse.jface.binding.DatabindingContext;
import org.eclipse.jface.binding.IChangeListener;

/**
 * @since 3.2
 *
 */
abstract public class Binding implements IChangeListener {

	protected final DatabindingContext context;

	/**
	 * @param context
	 */
	public Binding(DatabindingContext context) {
		this.context = context;
	}
	
	/**
	 * 
	 */
	abstract public void updateTargetFromModel();

}
