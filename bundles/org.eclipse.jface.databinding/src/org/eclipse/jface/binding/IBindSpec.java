package org.eclipse.jface.binding;

public interface IBindSpec {
	
	/**
	 * Policy constant specifying that the context's update or validation policy should be used. 
	 */
	public static final int POLICY_CONTEXT = 0;

	/**
	 * Policy constant specifying that update or validation should occur automatically. 
	 */
	public static final int POLICY_AUTOMATIC = 1;
	
	/**
	 * Policy constant specifying that update or validation should only occur when explicitly requested. 
	 */
	public static final int POLICY_EXPLICIT = 2;
	
	public IConverter getConverter();
	
	public IValidator getValidator();
	
	/**
	 * Returns the update policy to be used for updating the model when the target has changed
	 * @return the update policy
	 */
	public int getModelUpdatePolicy();
	
	/**
	 * Returns the validate policy to be used for validating changes to the target
	 * @return the update policy
	 */
	public int getValidatePolicy();
	
	/**
	 * Returns the update policy to be used for updating the target when the model has changed
	 * @return the update policy
	 */
	public int getTargetUpdatePolicy();
	
}
