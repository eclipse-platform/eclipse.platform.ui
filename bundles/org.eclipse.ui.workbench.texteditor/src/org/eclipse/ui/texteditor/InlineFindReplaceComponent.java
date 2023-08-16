package org.eclipse.ui.texteditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.text.IFindReplaceTarget;

import org.eclipse.ui.IWorkbenchPart;

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
public class InlineFindReplaceComponent { // MW -> @HeikoKlare I'm not sure wether and what I
											// should extend from
	boolean currentlyActive = false;
	IWorkbenchPart targetPart;
	IFindReplaceTarget target;
	FindReplacer findReplacer;

	boolean dialogCreated = false;
	Composite container;
	Composite spacer;
	Composite searchOptions;
	Composite searchBarContainer;
	Text searchBar;
	Button searchUpButton;
	Button searchDownButton;
	Button regexSearchButton;

	public InlineFindReplaceComponent(IFindReplaceTarget findReplaceTarget, IWorkbenchPart workbenchPart) {
		targetPart = workbenchPart;
		target = findReplaceTarget;
		findReplacer = new FindReplacer(target);
	}

	public void updateState() { // MW -> @HeikoKlare a lot of these functions reference (StatusTextEditor).
								// Eventually, these references will need to be cast into an interface
		if (currentlyActive) {
			if (targetPart != null) {
				createDialog(((StatusTextEditor) targetPart).getInlineToolbarParent());
			}
		} else {
			if (dialogCreated) {
				hideDialog();
			}
		}
	}

	public void hideDialog() {
		container.setLayoutData(null);
		container.dispose();
		spacer.dispose();
		searchOptions.dispose();
		searchBarContainer.dispose();
		searchBar.dispose();
		searchDownButton.dispose();
		searchUpButton.dispose();
		regexSearchButton.dispose();

		dialogCreated = false;
		((StatusTextEditor) targetPart).updatePartControl(((StatusTextEditor) targetPart).getEditorInput());
	}

	public void createDialog(Composite parent) { // MW -> @HeikoKlare we need to rethink how the dialog is built if we
													// want to generalize this in the future
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(3, false));

		spacer = new Composite(container, SWT.NONE);
		GridData spacerData = new GridData();
		spacerData.grabExcessHorizontalSpace = true;
		spacer.setLayoutData(spacerData);

		searchOptions = new Composite(container, SWT.NONE);
		searchOptions.setLayout(new GridLayout(3, false));
		searchUpButton = new Button(searchOptions, SWT.PUSH);
		searchUpButton.setText(" ⬆️ "); //$NON-NLS-1$
		searchUpButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				findReplacer.setForwardSearch(false);
				search();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		searchDownButton = new Button(searchOptions, SWT.PUSH);
		searchDownButton.setText(" ⬇️ "); //$NON-NLS-1$
		searchDownButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				findReplacer.setForwardSearch(true);
				search();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		regexSearchButton = new Button(searchOptions, SWT.TOGGLE);
		regexSearchButton.setText(" ⚙️ ");
		regexSearchButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				findReplacer.setRegExSearch(regexSearchButton.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}
		});

		searchBarContainer = new Composite(container, SWT.NONE);
		searchBarContainer.setBackground(new Color(13, 15, 16));
		GridData searchBarData = new GridData(SWT.FILL);
		searchBarData.grabExcessHorizontalSpace = true;
		searchBarData.horizontalAlignment = SWT.FILL;
		searchBarContainer.setLayoutData(searchBarData);
		searchBarContainer.setLayout(new FillLayout());
		searchBar = new Text(searchBarContainer, SWT.SINGLE | SWT.BORDER);
		searchBar.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(final TraverseEvent event) {
				if (event.detail == SWT.TRAVERSE_RETURN) {
					search();
				}
			}
		});

		container.layout();
		parent.layout();
		((StatusTextEditor) targetPart).updatePartControl(((StatusTextEditor) targetPart).getEditorInput());
		dialogCreated = true;
	}

	public void retargetDialog(Composite parent) {
		container.setParent(parent);
		updateState();
	}

	public void toggleActive() {
		currentlyActive = !currentlyActive;
		updateState();
	}

	private String getFindString() {
		return searchBar.getText();
	}

	private int search() {
		return findReplacer.findAndSelectNext(getFindString());
	}
}
