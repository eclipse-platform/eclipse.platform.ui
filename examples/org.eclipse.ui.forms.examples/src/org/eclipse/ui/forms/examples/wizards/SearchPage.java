/*
 * Created on Dec 12, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.forms.examples.wizards;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SearchPage implements IHelpContentPage {
	public static final String ID = "search";
	Composite emptyPage;
	/**
	 * 
	 */
	public SearchPage() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.examples.wizards.IHelpContentPage#init(org.eclipse.ui.forms.examples.wizards.ContentSectionPart, org.eclipse.ui.IMemento)
	 */
	public void init(ContentSectionPart section, IMemento memento) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.examples.wizards.IHelpContentPage#createControl(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	public void createControl(Composite parent, FormToolkit toolkit) {
		emptyPage = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		emptyPage.setLayout(layout);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.examples.wizards.IHelpContentPage#getControl()
	 */
	public Control getControl() {
		return emptyPage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.examples.wizards.IHelpContentPage#setFocus()
	 */
	public void setFocus() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.examples.wizards.IHelpContentPage#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.examples.wizards.IHelpContentPage#addToActionBars(org.eclipse.ui.IActionBars)
	 */
	public void addToActionBars(IActionBars bars) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.examples.wizards.IHelpContentPage#getId()
	 */
	public String getId() {
		return ID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.examples.wizards.IHelpContentPage#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
	}
}
