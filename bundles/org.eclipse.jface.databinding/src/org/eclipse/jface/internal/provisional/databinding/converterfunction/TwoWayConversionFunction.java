/*******************************************************************************
 * Copyright (c) 2005, 2006 Coconut Palm Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Coconut Palm Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.provisional.databinding.converterfunction;


/**
 * Converter that looks up its target to model and model to target methods
 * in the ConversionFunctionRegistry.
 * 
 * @since 3.2
 */
public class TwoWayConversionFunction {

	private IConversionFunction targetToModel;
	private IConversionFunction modelToTarget;
	
	/**
	 * Constructs a Converter that looks up its target to model and model to target methods
	 * in the ConversionFunctionRegistry.
	 * 
	 * @param targetType The target type Class
	 * @param modelType The model type Class
	 */
	public TwoWayConversionFunction(Class targetType, Class modelType) {
		targetToModel = ConversionFunctionRegistry.get(targetType, modelType);
		modelToTarget = ConversionFunctionRegistry.get(modelType, targetType);
	}
	
	/**
	 * Constructs a two-way converter function by supplying two one-way
	 * converter functions to handle the target2model and model2target
	 * conversion directions.
	 *
	 * @param targetToModel
	 * @param modelToTarget
	 */
	public TwoWayConversionFunction(IConversionFunction targetToModel, IConversionFunction modelToTarget) {
		this.targetToModel = targetToModel;
		this.modelToTarget = modelToTarget;
	}

	/**
	 * Convert a target-typed object to the model's data type.
	 * 
	 * @param targetObject an object in the target's data type.
	 * @return a copy of the targetObject, converted to the model's data type.
	 */
	public Object convertTargetToModel(Object targetObject) {
		return targetToModel.convert(targetObject);
	}

	/**
	 * Convert a model-typed object to the model's data type.
	 * 
	 * @param modelObject an object in the model's data type.
	 * @return a copy of the mnodelObject, converted to the target's data type.
	 */
	public Object convertModelToTarget(Object modelObject) {
 		return modelToTarget.convert(modelObject);
	}

}
