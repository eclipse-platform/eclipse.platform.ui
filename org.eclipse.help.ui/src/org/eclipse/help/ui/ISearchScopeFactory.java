/*
 * Created on Jan 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui;

import org.eclipse.help.internal.search.ISearchScope;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface ISearchScopeFactory {
	ISearchScope createSearchScope(IPreferenceStore store);
}
