package org.eclipse.jface.binding;

/**
 * @since 3.2
 * 
 */
public interface IValidationContext {
	/**
	 * @param listener
	 * @param validationErrorOrNull
	 */
	public void updateValidationError(IChangeListener listener, String validationErrorOrNull);
	
}