package org.eclipse.jface.databinding.internal;

import org.eclipse.jface.databinding.DatabindingContext;
import org.eclipse.jface.databinding.IChangeListener;

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
