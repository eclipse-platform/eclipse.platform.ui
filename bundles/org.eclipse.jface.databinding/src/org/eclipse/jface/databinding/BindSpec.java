package org.eclipse.jface.databinding;

/**
 * A concrete implementation of IBindSpec, suitable either for instantiating or
 * subclassing.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class BindSpec implements IBindSpec {

	private final IConverter converter;

	private final IValidator validator;

	private final int modelUpdatePolicy;

	private final int validatePolicy;

	private final int targetUpdatePolicy;

	/**
	 * Creates a bind spec with the given converter, validator, and update
	 * policies.
	 * 
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
	 * Creates a bind spec with the given converter and validator. The update
	 * policies are set to <code>IBindSpec.POLICY_CONTEXT</code>.
	 * 
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
