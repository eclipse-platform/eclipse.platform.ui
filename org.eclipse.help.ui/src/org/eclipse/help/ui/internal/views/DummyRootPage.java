/*
 * Created on Jan 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import org.eclipse.help.internal.search.ISearchScope;
import org.eclipse.help.ui.RootScopePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DummyRootPage extends RootScopePage {

	/**
	 * 
	 */
	public DummyRootPage() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.RootScopePage#createScopeContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createScopeContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		Label label = new Label(container, SWT.NULL);
		label.setText("Dummy content");
		return container;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.RootScopePage#getScope()
	 */
	public ISearchScope getScope() {
		// TODO Auto-generated method stub
		return null;
	}
}