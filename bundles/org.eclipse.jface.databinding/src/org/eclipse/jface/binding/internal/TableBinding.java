package org.eclipse.jface.binding.internal;

import org.eclipse.jface.binding.BindingException;
import org.eclipse.jface.binding.ChangeEvent;
import org.eclipse.jface.binding.DatabindingContext;
import org.eclipse.jface.binding.IChangeEvent;
import org.eclipse.jface.binding.IConverter;
import org.eclipse.jface.binding.ITableBindSpec;
import org.eclipse.jface.binding.IUpdatableTable;
import org.eclipse.jface.binding.IdentityConverter;

public class TableBinding extends Binding {

	private final IUpdatableTable targetTable;

	private final IUpdatableTable modelTable;

	private IConverter elementConverter;

	private IConverter[] valueConverters;

	/**
	 * @param context
	 * @param targetTable
	 * @param modelTable
	 * @param bindSpec
	 * @throws BindingException
	 */
	public TableBinding(DatabindingContext context,
			IUpdatableTable targetTable, IUpdatableTable modelTable,
			ITableBindSpec bindSpec) throws BindingException {
		super(context);
		this.targetTable = targetTable;
		this.modelTable = modelTable;
		initializeElementConverter(bindSpec);
		initializeValueConverters(bindSpec);
	}

	private void initializeValueConverters(ITableBindSpec bindSpec)
			throws BindingException {
		valueConverters = bindSpec == null ? null : bindSpec
				.getColumnConverters();
		if (valueConverters != null) {
			for (int i = 0; i < valueConverters.length; i++) {
				checkConverterTypes(valueConverters[i], modelTable
						.getColumnTypes()[i], targetTable.getColumnTypes()[i]);
			}
		} else {
			Class[] modelColumnTypes = modelTable.getColumnTypes();
			if (targetTable.getColumnTypes().length != modelColumnTypes.length) {
				throw new BindingException("column counts don't match"); //$NON-NLS-1$
			}
			valueConverters = new IConverter[modelColumnTypes.length];
			for (int i = 0; i < valueConverters.length; i++) {
				valueConverters[i] = new IdentityConverter(modelColumnTypes[i]);
			}
		}
	}

	private void initializeElementConverter(ITableBindSpec bindSpec)
			throws BindingException {
		elementConverter = bindSpec == null ? null : bindSpec.getConverter();
		if (elementConverter != null) {
			checkConverterTypes(elementConverter, targetTable.getElementType(),
					modelTable.getElementType());
		} else {
			Class targetElementType = targetTable.getElementType();
			Class modelElementType = modelTable.getElementType();
			if (!targetElementType.isAssignableFrom(modelElementType)) {
				throw new BindingException(
						"no converter from " + modelElementType //$NON-NLS-1$
								+ " to " + targetElementType); //$NON-NLS-1$
			}
			elementConverter = new IdentityConverter(targetElementType);
		}
	}

	private void checkConverterTypes(IConverter converter, Class targetType,
			Class modelType) throws BindingException {
		if (!converter.getModelType().isAssignableFrom(modelType)
				|| !targetType.isAssignableFrom(elementConverter
						.getTargetType())) {
			throw new BindingException(
					"converter from/to types don't match element types"); //$NON-NLS-1$
		}
	}

	public void updateTargetFromModel() {
		// TODO filling a table for the first time is not efficient - need one
		// call for filling the table, or some beginChange/endChange scheme
		// TODO don't forget support for virtual tables!
		while (targetTable.getSize() > modelTable.getSize()) {
			targetTable.removeElement(0);
		}
		for (int i = 0; i < targetTable.getSize(); i++) {
			targetTable.setElementAndValues(i, elementConverter
					.convertModelToTarget(modelTable.getElement(i)),
					getConvertedModelValues(modelTable, valueConverters, i));
		}
		while (targetTable.getSize() < modelTable.getSize()) {
			int index = targetTable.getSize();
			targetTable
					.addElementWithValues(index,
							elementConverter.convertModelToTarget(modelTable
									.getElement(index)),
							getConvertedModelValues(modelTable,
									valueConverters, index));
		}
	}

	public void handleChange(IChangeEvent changeEvent) {
		if (changeEvent.getUpdatable() == targetTable) {
			if (changeEvent.getChangeType() == ChangeEvent.CHANGE) {
				int row = changeEvent.getPosition();
				modelTable.setElementAndValues(row,
						targetTable.getElement(row), getConvertedModelValues(
								targetTable, valueConverters, row));
			}
			// TODO ADD case, REMOVE case
		} else {
			if (changeEvent.getChangeType() == ChangeEvent.CHANGE) {
				int row = changeEvent.getPosition();
				targetTable.setElementAndValues(row, elementConverter
						.convertModelToTarget(changeEvent.getNewValue()),
						getConvertedModelValues(modelTable, valueConverters,
								row));
			} else if (changeEvent.getChangeType() == ChangeEvent.ADD) {
				int row = changeEvent.getPosition();
				targetTable.addElementWithValues(row, elementConverter
						.convertModelToTarget(changeEvent.getNewValue()),
						getConvertedModelValues(modelTable, valueConverters,
								row));
			} else if (changeEvent.getChangeType() == ChangeEvent.REMOVE) {
				int row = changeEvent.getPosition();
				targetTable.removeElement(row);
			}
		}
	}

	private Object[] getConvertedModelValues(final IUpdatableTable modelTable,
			final IConverter[] modelToTargetValueConverters, int index) {
		Object[] modelValues = modelTable.getValues(index);
		Object[] convertedValues = new Object[modelToTargetValueConverters.length];
		for (int i = 0; i < modelToTargetValueConverters.length; i++) {
			convertedValues[i] = modelToTargetValueConverters[i]
					.convertModelToTarget(modelValues[i]);
		}
		return convertedValues;
	}

}
