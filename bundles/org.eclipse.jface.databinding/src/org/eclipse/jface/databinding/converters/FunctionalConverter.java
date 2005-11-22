package org.eclipse.jface.databinding.converters;

import org.eclipse.jface.databinding.converter.Converter;
import org.eclipse.jface.databinding.converterfunction.ConversionFunctionRegistry;
import org.eclipse.jface.databinding.converterfunction.IConversionFunction;

/**
 * Converter that looks up its target to model and model to target methods
 * in the ConvertsionFunctionRegistry.
 * 
 * @since 3.2
 */
public class FunctionalConverter extends Converter {

	private IConversionFunction targetToModel;
	private IConversionFunction modelToTarget;
	
	/**
	 * Constructs a Converter that looks up its target to model and model to target methods
	 * in the ConvertsionFunctionRegistry.
	 * 
	 * @param targetType The target type Class
	 * @param modelType The model type Class
	 */
	public FunctionalConverter(Class targetType, Class modelType) {
		super(targetType, modelType);
		targetToModel = ConversionFunctionRegistry.get(targetType, modelType);
		modelToTarget = ConversionFunctionRegistry.get(modelType, targetType);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.converter.IConverter#convertTargetToModel(java.lang.Object)
	 */
	public Object convertTargetToModel(Object targetObject) {
		return targetToModel.convert(targetObject);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.converter.IConverter#convertModelToTarget(java.lang.Object)
	 */
	public Object convertModelToTarget(Object modelObject) {
		return modelToTarget.convert(modelObject);
	}

}
