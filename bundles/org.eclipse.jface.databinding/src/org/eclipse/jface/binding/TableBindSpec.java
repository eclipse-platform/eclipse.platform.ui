package org.eclipse.jface.binding;

public class TableBindSpec extends BindSpec implements ITableBindSpec {

	private IConverter[] columnConverters;

	private IValidator[] columnValidators;

	/**
	 * @param columnConverters
	 * @param columnValidators
	 */
	public TableBindSpec(IConverter[] columnConverters,
			IValidator[] columnValidators) {
		super(null, null);
		this.columnConverters = columnConverters;
		if (columnConverters != null) {
			for (int i = 0; i < columnConverters.length; i++) {
				if (columnConverters[i] == null) {
					columnConverters[i] = new IdentityConverter(String.class);
				}
			}
		}
		this.columnValidators = columnValidators;
	}

	public IConverter[] getColumnConverters() {
		return columnConverters;
	}

	public IValidator[] getColumnValidators() {
		return columnValidators;
	}

	public IConverter getConverter() {
		return null;
	}

	public IValidator getValidator() {
		return null;
	}

	public IColumn[] getColumns() {
		// TODO Auto-generated method stub
		return null;
	}

}
