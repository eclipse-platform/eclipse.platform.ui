package org.eclipse.ui.texteditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * The widget for Inline finding and replacing of strings. Will be instantiated
 * and injected into the Text editor.
 *
 * @since 3.18
 */
public class InlineFindReplaceComponent { // MW -> @HeikoKlare I'm not sure wether and what I should extend from

	// MW -> @HeikoKlare It might make sense to implement this as a singleton: I
	// always want to have one single Inline Finder, which is then moved across
	// editors as they come into and leave focus
	Button b;

	public void createDialog(Composite parent) {
		b = new Button(parent, SWT.PUSH);
		b.setText("Hello World"); //$NON-NLS-1$
		parent.layout();
	}
}
