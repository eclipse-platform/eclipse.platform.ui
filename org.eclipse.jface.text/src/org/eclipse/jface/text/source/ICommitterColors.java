package org.eclipse.jface.text.source;

import org.eclipse.swt.graphics.RGB;


/**
 * Lookup used by the annotate ruler column to find out the color that an annotate revision should
 * be draw with.
 * <p>
 * Clients may implement.
 * </p>
 * <p>
 * XXX This API is provisional and may change any time during the development of eclipse 3.2.
 * </p>
 * 
 * @since 3.2
 */
public interface ICommitterColors {

	/**
	 * Returns the color to be used for a certain revision.
	 * 
	 * @param revision the revision to lookup the color for
	 * @return the color to be used for a certain revision
	 */
	RGB getCommitterRGB(AnnotateRevision revision);

}
