package org.eclipse.jface.binding;

public interface ITableBindSpec extends IBindSpec {
	
	public IConverter[] getColumnConverters();

	public IValidator[] getColumnValidators();

}
