/*
 * Created on May 26, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.search;

import org.eclipse.update.core.IFeature;

/**
 * Classes that implement this interface can be used to filter the
 * results of the update search.
 */
public interface IUpdateSearchFilter {
	boolean select(IFeature match);
}
