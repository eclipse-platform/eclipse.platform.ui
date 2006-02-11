package org.eclipse.jface.internal.databinding.api.observable.value;

/**
 * @since 3.2
 *
 */
public class ChangeVetoException extends RuntimeException {
	
	/**
	 * @param string
	 */
	public ChangeVetoException(String string) {
		super(string);
	}

	private static final long serialVersionUID = 1L;

}
