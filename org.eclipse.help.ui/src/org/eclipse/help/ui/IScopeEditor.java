/*
 * Created on Jan 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui;

import org.eclipse.help.internal.search.ISearchScope;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.swt.widgets.Composite;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface IScopeEditor extends IDialogPage {
/**
 * 
 */
	void createControl(Composite parent);
/**
 * Returns the object that represents the search scope. The data should
 * be collected from the widget state.
 * @return
 */
	ISearchScope getScope();
}