package org.eclipse.jface.binding;


/**
 * @since 3.2
 *
 */
public interface ITableBindSpec extends IBindSpec {
	
	/**
	 * @return the column converters
	 */
	public IConverter[] getColumnConverters();

	/**
	 * @return the column validators
	 */
	public IValidator[] getColumnValidators();

}
