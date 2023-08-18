package org.eclipse.ui.texteditor;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
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
											// should extend from -- maybe Composite?
	Timer timer = new Timer();
	private final long waitForUserDoneTypingDelay = 300;
	boolean currentlyActive = false;
	IWorkbenchPart targetPart;
	IFindReplaceTarget target;
	FindReplacer findReplacer;

	boolean dialogCreated = false;
	Composite container;
	Composite searchContainer;
	Text searchBar;
	Button findAllButton;
	Button searchUpButton;
	Button searchDownButton;
	Button regexSearchButton;
	Button openReplaceButton;
	Label searchLabel;

	Composite replaceContainer;
	Composite replaceOptions;
	Composite replaceBarContainer;
	Text replaceBar;
	Button replaceButton;
	Button replaceAllButton;

	public InlineFindReplaceComponent(IFindReplaceTarget findReplaceTarget, IWorkbenchPart workbenchPart) {
		targetPart = workbenchPart;
		target = findReplaceTarget;
		findReplacer = new FindReplacer(workbenchPart.getSite().getShell(), target);
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
		if (openReplaceButton.getSelection()) {
			hideReplace();
		}
		container.setLayoutData(null);
		container.dispose();
		searchBar.dispose();
		searchDownButton.dispose();
		searchUpButton.dispose();
		regexSearchButton.dispose();

		dialogCreated = false;
		((StatusTextEditor) targetPart).updatePartControl(((StatusTextEditor) targetPart).getEditorInput());
	}

	public void hideReplace() {
		replaceContainer.setLayout(null);
		replaceContainer.dispose();
		replaceOptions.dispose();
		replaceBar.dispose();
		((StatusTextEditor) targetPart).updatePartControl(((StatusTextEditor) targetPart).getEditorInput());
	}

	public void createDialog(Composite parent) { // MW -> @HeikoKlare we need to rethink how the dialog is built if we
													// want to generalize this in the future
		// TODO clean up this mess
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(3, false));
		GridData findContainerGD = new GridData();
		findContainerGD.grabExcessHorizontalSpace = true;
		findContainerGD.horizontalAlignment = GridData.FILL;
		findContainerGD.verticalAlignment = GridData.FILL;
		container.setLayoutData(findContainerGD);

		searchContainer = new Composite(container, SWT.BORDER); // The border looks funny. TODO
		GridData searchContainerGD = new GridData();
		searchContainerGD.grabExcessHorizontalSpace = true;
		searchContainerGD.horizontalAlignment = GridData.FILL;
		searchContainerGD.verticalAlignment = GridData.FILL;
		searchContainer.setLayoutData(searchContainerGD);
		searchContainer.setLayout(new GridLayout(6, false));
		searchBar = new Text(searchContainer, SWT.SINGLE);
		searchLabel = new Label(searchContainer, SWT.NONE);
		searchLabel.setText(" ");
		GridData searchBarGridData = new GridData();
		searchBarGridData.grabExcessHorizontalSpace = true;
		searchBarGridData.grabExcessVerticalSpace = true;
		searchBarGridData.horizontalAlignment = GridData.FILL;
		searchBarGridData.verticalAlignment = GridData.CENTER;
		searchBar.setLayoutData(searchBarGridData);
		searchBar.forceFocus();
		searchBar.selectAll();
		searchUpButton = new Button(searchContainer, SWT.TOGGLE);
		searchUpButton.setText(" ⬆️ "); //$NON-NLS-1$
		searchUpButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchUpButton.setSelection(true);
				searchDownButton.setSelection(false);
				findReplacer.setForwardSearch(false);
				search();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		searchDownButton = new Button(searchContainer, SWT.TOGGLE);
		searchDownButton.setSelection(true); // by default, search down
		searchDownButton.setText(" ⬇️ "); //$NON-NLS-1$
		searchDownButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				searchUpButton.setSelection(false);
				searchDownButton.setSelection(true);
				findReplacer.setForwardSearch(true);
				search();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		regexSearchButton = new Button(searchContainer, SWT.TOGGLE);
		regexSearchButton.setText(" ⚙️ "); //$NON-NLS-1$
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
		openReplaceButton = new Button(searchContainer, SWT.TOGGLE);
		openReplaceButton.setText(" 🔄 "); //$NON-NLS-1$
		openReplaceButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println(openReplaceButton.getSelection());
				if (openReplaceButton.getSelection()) {
					createReplaceDialog();
				} else {
					hideReplace();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		// Problem: shift+8 ("(") will not be typed into the search Bar since it is
		// already a bound key. We need to override this behaviour FIXME
		// Problem also generalizes to other keybinds: Ctrl + backspace, ...
		// We need to convince the editor that we aren't writing to it.
		searchBar.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(final TraverseEvent event) {
				if (event.detail == SWT.TRAVERSE_RETURN) {
					search();
				}
			}
		});
		searchBar.addModifyListener(new ModifyListener() { // MW -> @HeikoKlare we need to revisit this!
			@Override
			public void modifyText(ModifyEvent e) {
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						Display.getDefault().asyncExec(new Runnable() { // https://wiki.eclipse.org/FAQ_Why_do_I_get_an_invalid_thread_access_exception%3F
							@Override
							public void run() {
								selectAll();
							}
						});
					}

				}, waitForUserDoneTypingDelay);
			}

		});
		searchBar.setMessage(" 🔎 Find"); //$NON-NLS-1$

		container.layout();
		parent.layout();

		((StatusTextEditor) targetPart).updatePartControl(((StatusTextEditor) targetPart).getEditorInput());
		dialogCreated = true;

	}

	public void createReplaceDialog() {
		Composite parent = ((StatusTextEditor) targetPart).getInlineToolbarParent();
		replaceContainer = new Composite(parent, SWT.NONE);
		replaceContainer.setLayout(new GridLayout(2, false));
		GridData replaceContainerLayoutData = new GridData();
		replaceContainerLayoutData.grabExcessHorizontalSpace = true;
		replaceContainerLayoutData.horizontalAlignment = SWT.FILL;
		replaceContainer.setLayoutData(replaceContainerLayoutData);

		replaceOptions = new Composite(replaceContainer, SWT.NONE);
		replaceOptions.setLayout(new GridLayout(3, false));
		replaceButton = new Button(replaceOptions, SWT.PUSH);
		replaceButton.setText("Replace"); //$NON-NLS-1$
		replaceButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				replaceNext();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		replaceAllButton = new Button(replaceOptions, SWT.PUSH);
		replaceAllButton.setText("Replace All"); //$NON-NLS-1$
		replaceAllButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				replaceAll();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		replaceBarContainer = new Composite(replaceContainer, SWT.NONE);
		GridData replaceBarData = new GridData(SWT.FILL);
		replaceBarData.grabExcessHorizontalSpace = true;
		replaceBarData.horizontalAlignment = SWT.FILL;
		replaceBarContainer.setLayoutData(replaceBarData);
		replaceBarContainer.setLayout(new FillLayout());
		replaceBarContainer.setBackground(new Color(255, 0, 0));
		replaceBar = new Text(replaceBarContainer, SWT.SINGLE | SWT.BORDER);
		replaceBar.setMessage(" 🔄 Replace"); //$NON-NLS-1$
		((StatusTextEditor) targetPart).updatePartControl(((StatusTextEditor) targetPart).getEditorInput());
	}

	public void toggleActive() {
		currentlyActive = !currentlyActive;
		updateState();
	}

	private String getFindString() {
		return searchBar.getText();
	}

	private String getReplaceString() {
		return replaceBar.getText();
	}

	private int search() {
		return findReplacer.performSelectNext(getFindString());
	}

	private void selectAll() { // TODO: we want the editor to go back to the current scroll state after
								// "selecting all"
		int occurrences = findReplacer.performSelectAll(getFindString());
		// XXX We need to consider how the display of occurrences behaves in languages
		// that are not written left-to-right. This leads to a frequent resize of
		// the write box which looks like the beginning of the text moves around a lot
		// in a language written right-to-left
		if (getFindString() != "" && getFindString() != null) {
			searchLabel.setText(Integer.toString(occurrences)); // $NON-NLS-1$
		} else {
			searchLabel.setText("");
		}
		searchContainer.layout();
	}

	private void replaceNext() { // where does replaceNext know the direction from?
		findReplacer.performReplaceNext(getFindString(), getReplaceString());
	}

	private void replaceAll() {
		findReplacer.performReplaceAll(getFindString(), getReplaceString());
	}
}
