package org.eclipse.ui.internal.dialogs;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.swt.layout.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.preference.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.UIHackFinder;
import org.eclipse.ui.dialogs.*;

/*
 * A page used as a filler for nodes in the property page dialog
 * for which no page is suppplied.
 */
public class EmptyPropertyPage extends PropertyPage {
/**
 * Creates empty composite for this page content.
 */

protected Control createContents(Composite parent) {
	return new Composite(parent, SWT.NULL);
}
}
