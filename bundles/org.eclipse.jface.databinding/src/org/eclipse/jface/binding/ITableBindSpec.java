package org.eclipse.jface.binding;


public interface ITableBindSpec extends IBindSpec {
	
	public IConverter[] getColumnConverters();

	public IValidator[] getColumnValidators();
	
	// TODO <bb>I would like to discuss this - what is the meaning of this method?</bb>
	public IColumn[] getColumns();

}
