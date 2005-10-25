package org.eclipse.jface.binding;

/**
 * @since 3.2
 * 
 */
public class BindSpec implements IBindSpec {

	private final IConverter converter;

	private final IValidator validator;

	private final int modelUpdatePolicy;

	private final int validatePolicy;

	private final int targetUpdatePolicy;

	/**
	 * @param converter
	 * @param validator
	 * @param modelUpdatePolicy
	 * @param validatePolicy
	 * @param targetUpdatePolicy
	 */
	public BindSpec(IConverter converter, IValidator validator,
			int modelUpdatePolicy, int validatePolicy, int targetUpdatePolicy) {
		this.converter = converter;
		this.validator = validator;
		this.modelUpdatePolicy = modelUpdatePolicy;
		this.validatePolicy = validatePolicy;
		this.targetUpdatePolicy = targetUpdatePolicy;
	}

	/**
	 * @param converter
	 * @param validator
	 */
	public BindSpec(IConverter converter, IValidator validator) {
		this(converter, validator, IBindSpec.POLICY_CONTEXT,
				IBindSpec.POLICY_CONTEXT, IBindSpec.POLICY_CONTEXT);
	}

	public IConverter getConverter() {
		return converter;
	}

	public int getModelUpdatePolicy() {
		return modelUpdatePolicy;
	}

	public int getTargetUpdatePolicy() {
		return targetUpdatePolicy;
	}

	public int getValidatePolicy() {
		return validatePolicy;
	}

	public IValidator getValidator() {
		return validator;
	}

}
