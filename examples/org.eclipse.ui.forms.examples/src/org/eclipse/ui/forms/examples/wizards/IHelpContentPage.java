/*
 * Created on Dec 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.forms.examples.wizards;

import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface IHelpContentPage {
	void init(ContentSectionPart section, IMemento memento);
	void createControl(Composite parent, FormToolkit toolkit);
	Control getControl();
	void setFocus();
	void dispose();
	void addToActionBars(IActionBars bars);
	String getId();
	void saveState(IMemento memento);
}