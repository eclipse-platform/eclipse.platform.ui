package org.eclipse.jface.databinding;


/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
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
