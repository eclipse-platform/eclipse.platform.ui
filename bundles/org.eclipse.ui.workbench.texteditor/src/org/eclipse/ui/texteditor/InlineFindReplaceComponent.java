package org.eclipse.ui.texteditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * The widget for Inline finding and replacing of strings. Will be instantiated
 * and injected into the Text editor.
 *
 * The widget automatically retargets when switching editors. // TODO The widget
 * will automatically hook itself up into any editor that implements
 * IInlineToolable // TODO The widget uses the same internal logic as the "find
 * replace" dialog // TODO The widget will by default perform a "search all"
 *
 * @since 3.18
 */
public class InlineFindReplaceComponent { // MW -> @HeikoKlare I'm not sure wether and what I should extend from
	boolean currentlyActive = false;

	Composite container;
	Composite spacer;
	Composite searchOptions;
	Composite searchBarContainer;
	Text searchBar;
	Button b;

	public void createDialog(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(3, false));

		spacer = new Composite(container, SWT.NONE);
		GridData spacerData = new GridData();
		spacerData.grabExcessHorizontalSpace = true;
		spacer.setLayoutData(spacerData);

		searchOptions = new Composite(container, SWT.NONE);
		searchOptions.setLayout(new GridLayout(1, false));
		b = new Button(searchOptions, SWT.CHECK);
		b.setText("Regex Search"); //$NON-NLS-1$

		searchBarContainer = new Composite(container, SWT.NONE);
		GridData searchBarData = new GridData(SWT.FILL);
		searchBarData.grabExcessHorizontalSpace = true;
		searchBarData.horizontalAlignment = SWT.FILL;
		searchBarContainer.setLayoutData(searchBarData);
		searchBarContainer.setLayout(new FillLayout());
		searchBar = new Text(searchBarContainer, SWT.SINGLE | SWT.BORDER);

		container.layout();
		parent.layout();
	}

	public void retargetDialog(Composite parent) {
		container.setParent(parent);
	}
}
