/*
 * Created on May 26, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.search;

import org.eclipse.update.core.IFeature;
import org.eclipse.update.internal.core.UpdateManagerUtils;

/**
 * This class can be added to the update search request
 * to filter out features that do not match the current
 * environment settings.
 * 
 * @see UpdateSearchRequest
 * @see IUpdateSearchFilter
 */
public class EnvironmentFilter implements IUpdateSearchFilter {
	public boolean select(IFeature match) {
		return UpdateManagerUtils.isValidEnvironment(match);
	}
}
