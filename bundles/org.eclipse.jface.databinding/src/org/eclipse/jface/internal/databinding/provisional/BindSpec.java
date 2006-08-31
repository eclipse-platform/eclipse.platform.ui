/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds (bug 135316)
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.provisional;

import org.eclipse.jface.internal.databinding.provisional.conversion.IConverter;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertDeleteProvider;
import org.eclipse.jface.internal.databinding.provisional.validation.IDomainValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.IValidator;


/**
 * Data binding has three concerns, the target, the model, and the data flow
 * between the target and model. BindSpec contains values and settings that
 * influence how data binding manages this data flow between the target and the
 * model.
 * 
 * @since 1.0
 * @deprecated use {@link org.eclipse.jface.databinding.BindSpec} instead
 */
public class BindSpec extends org.eclipse.jface.databinding.BindSpec {

	/**
	 * Creates a bind spec with the given converters, validators, and update
	 * policies.
	 * 
	 * @param modelToTargetConverter or <code>null</code>
	 * @param targetToModelConverter or <code>null</code>
	 * @param targetValidator or <code>null</code>
	 * @param domainValidator or <code>null</code>
	 * @param modelUpdatePolicy
	 * @param validatePolicy
	 * @param targetUpdatePolicy
	 * 
	 */
	public BindSpec(IConverter modelToTargetConverter,
			IConverter targetToModelConverter, IValidator targetValidator,
			IDomainValidator domainValidator, Integer modelUpdatePolicy,
			Integer validatePolicy, Integer targetUpdatePolicy) {
		this(
				(modelToTargetConverter != null) ? new IConverter[] { modelToTargetConverter }
						: null,
				(targetToModelConverter != null) ? new IConverter[] { targetToModelConverter }
						: null,
				(targetValidator != null) ? new IValidator[] { targetValidator }
						: null, domainValidator, modelUpdatePolicy,
				validatePolicy, targetUpdatePolicy);
	}

	/**
	 * Creates a bind spec with the given converters, validators, and update
	 * policies.
	 * 
	 * @param modelToTargetConverters or <code>null</code>
	 * @param targetToModelConverters or <code>null</code>
	 * @param targetValidators or <code>null</code>
	 * @param domainValidator or <code>null</code>
	 * @param modelUpdatePolicy
	 * @param validatePolicy
	 * @param targetUpdatePolicy
	 * 
	 */
	public BindSpec(IConverter[] modelToTargetConverters,
			IConverter[] targetToModelConverters, IValidator[] targetValidators,
			IDomainValidator domainValidator, Integer modelUpdatePolicy,
			Integer validatePolicy, Integer targetUpdatePolicy) {
		this(modelToTargetConverters, targetToModelConverters, targetValidators, domainValidator, modelUpdatePolicy, validatePolicy, targetUpdatePolicy, null);
	}

	/**
	 * Creates a bind spec with the given converters, validators, and update
	 * policies.
	 * 
	 * @param modelToTargetConverters
	 * @param targetToModelConverters
	 * @param targetValidators
	 * @param domainValidator
	 * @param modelUpdatePolicy
	 * @param validatePolicy
	 * @param targetUpdatePolicy
	 * @param lazyInsertDeleteProvider 
	 * 
	 */
	public BindSpec(IConverter[] modelToTargetConverters,
			IConverter[] targetToModelConverters, IValidator[] targetValidators,
			IDomainValidator domainValidator, Integer modelUpdatePolicy,
			Integer validatePolicy, Integer targetUpdatePolicy,
			LazyInsertDeleteProvider lazyInsertDeleteProvider) {
		super(modelToTargetConverters, targetToModelConverters, targetValidators, domainValidator, modelUpdatePolicy, validatePolicy, targetUpdatePolicy, lazyInsertDeleteProvider);
	}

	/**
	 * Creates a bind spec with the given converter and validator. The update
	 * policies are set to <code>IBindSpec.POLICY_CONTEXT</code>.
	 * 
	 * @param modelToTargetConverter or <code>null</code>
	 * @param targetToModelConverter or <code>null</code>
	 * @param targetValidator or <code>null</code>
	 * @param domainValidator or <code>null</code>
	 * 
	 */
	public BindSpec(IConverter modelToTargetConverter,
			IConverter targetToModelConverter, IValidator targetValidator,
			IDomainValidator domainValidator) {
		this(modelToTargetConverter, targetToModelConverter, targetValidator,
				domainValidator, null, null, null);
	}

	/**
	 * Default constructor that initializes all objects to their defaults.
	 */
	public BindSpec() {
		this((IConverter) null, null, null, null, null, null, null);
	}

	/**
	 * @return true if the model should be updated by the binding
	 * @deprecated use {@link #isUpdateModel()} instead
	 */
	public boolean updateModel() {
		return isUpdateModel();
	}

	/**
	 * @return true if the target should be updated by the binding
	 * @deprecated use {@link #isUpdateTarget()} instead
	 */
	public boolean updateTarget() {
		return isUpdateTarget();
	}

}
