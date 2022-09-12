/*******************************************************************************
 * Copyright (c) 2003, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 * Martin Oberhuber (Wind River) - [293159] cyclic link when searching browser
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * The preference page that holds Web browser preferences.
 */
public class WebBrowserPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	protected Button internal;

	protected Button external;

	protected Table table;

	protected CheckboxTableViewer tableViewer;

	protected Button edit;

	protected Button remove;

	protected Button search;

	protected Button restore;

	protected Label location;

	protected Label parameters;

	protected IBrowserDescriptor checkedBrowser;

	static class BrowserTableContentProvider implements IStructuredContentProvider {
		private BrowserManager input;

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			input = (BrowserManager) newInput;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return input.getWebBrowsers().toArray();
		}
	}

	static class BrowserTableLabelProvider implements ITableLabelProvider {
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			IBrowserDescriptor browser = (IBrowserDescriptor) element;
			return notNull(browser.getName());
		}

		protected String notNull(String s) {
			if (s == null)
				return ""; //$NON-NLS-1$
			return s;
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
			// do nothing
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			// do nothing
		}

		@Override
		public void dispose() {
			// do nothing
		}
	}

	/**
	 * WebBrowserPreferencePage constructor comment.
	 */
	public WebBrowserPreferencePage() {
		super();
	}

	/**
	 * Create the preference options.
	 *
	 * @param parent
	 *            org.eclipse.swt.widgets.Composite
	 * @return org.eclipse.swt.widgets.Control
	 */
	@Override
	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				ContextIds.PREF_BROWSER);

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(4);
		layout.verticalSpacing = convertVerticalDLUsToPixels(3);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		composite.setLayoutData(data);

		Label label = new Label(composite, SWT.WRAP);
		label.setText(Messages.preferenceWebBrowserDescription);
		data = new GridData(SWT.FILL, SWT.NONE, false, false);
		data.horizontalSpan = 2;
		data.widthHint = 275;
		label.setLayoutData(data);

		internal = new Button(composite, SWT.RADIO);
		internal.setText(Messages.prefInternalBrowser);
		data = new GridData(SWT.FILL, SWT.NONE, true, false);
		data.horizontalSpan = 2;
		internal.setLayoutData(data);

		if (!WebBrowserUtil.canUseInternalWebBrowser())
			internal.setEnabled(false);

		external = new Button(composite, SWT.RADIO);
		external.setText(Messages.prefExternalBrowser);
		data = new GridData(SWT.FILL, SWT.NONE, true, false);
		data.horizontalSpan = 2;
		external.setLayoutData(data);

		label = new Label(composite, SWT.NONE);
		label.setText(Messages.browserList);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(data);
		table.setHeaderVisible(false);
		table.setLinesVisible(false);

		TableLayout tableLayout = new TableLayout();
		new TableColumn(table, SWT.NONE);
		tableLayout.addColumnData(new ColumnWeightData(100));
		table.setLayout(tableLayout);

		tableViewer = new CheckboxTableViewer(table);
		tableViewer.setContentProvider(new BrowserTableContentProvider());
		tableViewer.setLabelProvider(new BrowserTableLabelProvider());
		tableViewer.setInput(BrowserManager.getInstance());

		// uncheck any other elements that might be checked and leave only the
		// element checked to remain checked since one can only chose one
		// brower at a time to be current.
		tableViewer.addCheckStateListener(e -> {
			checkNewDefaultBrowser(e.getElement());
			checkedBrowser = (IBrowserDescriptor) e.getElement();

			// if no other browsers are checked, don't allow the single one
			// currently checked to become unchecked, and lose a current
			// browser. That is, don't permit unchecking if no other item
			// is checked which is supposed to be the case.
			Object[] obj = tableViewer.getCheckedElements();
			if (obj.length == 0)
				tableViewer.setChecked(e.getElement(), true);
		});

		// set a default, checked browser based on the current browser. If there
		// is not a current browser, but the first item exists, use that instead.
		// This will work currently until workbench shutdown, because current
		// browser is not yet persisted.
		checkedBrowser = BrowserManager.getInstance().getCurrentWebBrowser();
		if (checkedBrowser != null)
			tableViewer.setChecked(checkedBrowser, true);
		else {
			Object obj = tableViewer.getElementAt(0);
			if (obj != null)
				tableViewer.setChecked(obj, true);
		}

		tableViewer
				.addSelectionChangedListener(event -> {
					IStructuredSelection sele = tableViewer.getStructuredSelection();
					boolean sel = sele.getFirstElement() != null
							&& !(sele.getFirstElement() instanceof SystemBrowserDescriptor);
					remove.setEnabled(sel);
					edit.setEnabled(sel);
				});

		tableViewer.addDoubleClickListener(event -> {
			IStructuredSelection sel = tableViewer.getStructuredSelection();
			Object firstElem = sel.getFirstElement();
			if (firstElem != null && !(firstElem instanceof SystemBrowserDescriptor)) {
				IBrowserDescriptor browser2 = (IBrowserDescriptor) sel.getFirstElement();
				IBrowserDescriptorWorkingCopy wc = browser2.getWorkingCopy();
				BrowserDescriptorDialog dialog = new BrowserDescriptorDialog(getShell(), wc);
				if (dialog.open() != Window.CANCEL) {
					try {
						tableViewer.refresh(wc.save());
					} catch (Exception ex) {
						// ignore
					}
				}
			}
		});

		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.DEL) {
					IStructuredSelection sel = tableViewer.getStructuredSelection();
					if (sel.getFirstElement() != null) {
						IBrowserDescriptor browser2 = (IBrowserDescriptor) sel
								.getFirstElement();
						try {
							browser2.delete();
							tableViewer.remove(browser2);

							// need here to ensure that if the item deleted was
							// checked, ie. was
							// the current browser, that the new current browser
							// will be the first in the
							// list, typically, the internal browser, which
							// cannot be
							// deleted, and be current
							BrowserManager manager = BrowserManager.getInstance();
							if (browser2 == checkedBrowser) {
								if (manager.browsers.size() > 0) {
									checkedBrowser = manager.browsers.get(0);
									tableViewer.setChecked(checkedBrowser, true);
								}
							}
						} catch (Exception ex) {
							// ignore
						}
					}
				}
			}
		});

		Composite buttonComp = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = convertVerticalDLUsToPixels(3);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 1;
		buttonComp.setLayout(layout);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_FILL);
		buttonComp.setLayoutData(data);

		final Button add = SWTUtil.createButton(buttonComp, Messages.add);
		add.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			BrowserDescriptorDialog dialog = new BrowserDescriptorDialog(getShell());
			if (dialog.open() == Window.CANCEL)
				return;
			tableViewer.refresh();
			if (checkedBrowser != null)
				tableViewer.setChecked(checkedBrowser, true);
		}));

		edit = SWTUtil.createButton(buttonComp, Messages.edit);
		edit.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			IStructuredSelection sel = tableViewer.getStructuredSelection();
			IBrowserDescriptor browser2 = (IBrowserDescriptor) sel.getFirstElement();
			IBrowserDescriptorWorkingCopy wc = browser2.getWorkingCopy();
			BrowserDescriptorDialog dialog = new BrowserDescriptorDialog(getShell(), wc);
			if (dialog.open() != Window.CANCEL) {
				try {
					tableViewer.refresh(wc.save());
				} catch (Exception ex) {
					// ignore
				}
			}
		}));

		remove = SWTUtil.createButton(buttonComp, Messages.remove);
		remove.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			IStructuredSelection sel = tableViewer.getStructuredSelection();
			IBrowserDescriptor browser2 = (IBrowserDescriptor) sel.getFirstElement();
			try {
				browser2.delete();
				tableViewer.remove(browser2);

				// need here to ensure that if the item deleted was checked,
				// ie. was the current browser, that the new current browser
				// will be
				// the first in the list, typically, the internal browser, which
				// cannot be
				// deleted, and be current
				BrowserManager manager = BrowserManager.getInstance();
				if (browser2 == checkedBrowser) {
					if (manager.browsers.size() > 0) {
						checkedBrowser = manager.browsers.get(0);
						tableViewer.setChecked(checkedBrowser, true);
					}
				}
			} catch (Exception ex) {
				// ignore
			}
		}));

		search = SWTUtil.createButton(buttonComp, Messages.search);
		data = (GridData) search.getLayoutData();
		data.verticalIndent = 9;
		search.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			final List<IBrowserDescriptorWorkingCopy> foundBrowsers = new ArrayList<>();
			final List<String> existingPaths = WebBrowserUtil.getExternalBrowserPaths();

			// select a target directory for the search
			DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SHEET);
			dialog.setMessage(Messages.selectDirectory);
			dialog.setText(Messages.directoryDialogTitle);

			String path = dialog.open();
			if (path == null)
				return;

			final File rootDir = new File(path);
			ProgressMonitorDialog pm = new ProgressMonitorDialog(getShell());

			IRunnableWithProgress r = monitor -> {
				monitor.beginTask(Messages.searchingTaskName, IProgressMonitor.UNKNOWN);
				search(rootDir, existingPaths, foundBrowsers, new HashSet<>(), monitor);
				monitor.done();
			};

			try {
				pm.run(true, true, r);
			} catch (InvocationTargetException ex) {
				Trace.trace(Trace.SEVERE, "Invocation Exception occured running monitor: " //$NON-NLS-1$
						+ ex);
			} catch (InterruptedException ex) {
				Trace.trace(Trace.SEVERE, "Interrupted exception occured running monitor: " //$NON-NLS-1$
						+ ex);
				return;
			}

			if (pm.getProgressMonitor().isCanceled())
				return;

			List<IBrowserDescriptorWorkingCopy> browsersToCreate = foundBrowsers;

			if (browsersToCreate.isEmpty()) { // no browsers found
				WebBrowserUtil.openMessage(Messages.searchingNoneFound);
				return;
			}

			Iterator<IBrowserDescriptorWorkingCopy> iterator = browsersToCreate.iterator();
			while (iterator.hasNext()) {
				IBrowserDescriptorWorkingCopy browser2 = iterator.next();
				browser2.save();
			}
			tableViewer.refresh();

			if (checkedBrowser != null)
				tableViewer.setChecked(checkedBrowser, true);
		}));

		restore = SWTUtil.createButton(buttonComp, Messages.restore);
		data = (GridData) restore.getLayoutData();
		data.verticalIndent = 9;
		restore.addSelectionListener(widgetSelectedAdapter(e -> addDefaults()));

		tableViewer.addCheckStateListener(e -> {
			checkNewDefaultBrowser(e.getElement());
			checkedBrowser = (IBrowserDescriptor) e.getElement();
		});

		internal.setSelection(WebBrowserPreference.getBrowserChoice() == WebBrowserPreference.INTERNAL);
		external.setSelection(WebBrowserPreference.getBrowserChoice() == WebBrowserPreference.EXTERNAL);

		//boolean sel = !tableViewer.getSelection().isEmpty();
		IStructuredSelection sele = tableViewer.getStructuredSelection();
		boolean sel = sele.getFirstElement() != null &&
				!(sele.getFirstElement() instanceof SystemBrowserDescriptor);
		edit.setEnabled(sel);
		remove.setEnabled(sel);

		Dialog.applyDialogFont(composite);

		return composite;
	}

	/**
	 * Initializes this preference page using the passed workbench.
	 *
	 * @param workbench
	 *            the current workbench
	 */
	@Override
	public void init(IWorkbench workbench) {
		// do nothing
	}

	/**
	 *
	 */
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible)
			setTitle(Messages.preferenceWebBrowserTitle);
	}

	protected Object getSelection(ISelection sel2) {
		IStructuredSelection sel = (IStructuredSelection) sel2;
		return sel.getFirstElement();
	}

	// Uncheck all the items except the current one that was just checked
	protected void checkNewDefaultBrowser(Object browser) {
		for (TableItem item : tableViewer.getTable().getItems()) {
			if (!(item.getData().equals(browser)))
				item.setChecked(false);
		}
	}

	protected static void search(File directory, List<String> existingPaths,
			List<IBrowserDescriptorWorkingCopy> foundBrowsers, Set<String> directoriesVisited, IProgressMonitor monitor) {
		if (monitor.isCanceled())
			return;
		try {
			//bug 293159: protect against recursion due to cyclic symbolic link
			String canonicalPath = directory.getCanonicalPath();
			if (!directoriesVisited.add(canonicalPath)) {
				//already been here
				return;
			}
		} catch(IOException ioe) {
			/*ignore*/
		}

		monitor.subTask(NLS.bind(Messages.searching,
				new String[] { Integer.toString(foundBrowsers.size()), directory.getAbsolutePath()}));

		String[] names = directory.list();
		List<File> subDirs = new ArrayList<>();
		if (names == null) {
			names = new String[0];
		}
		for (String name : names) {
			if (monitor.isCanceled())
				return;

			File file = new File(directory, name);

			if (existingPaths.contains(file.getAbsolutePath().toLowerCase()))
				continue;

			IBrowserDescriptorWorkingCopy wc = WebBrowserUtil.createExternalBrowser(file);
			if (wc != null)
				foundBrowsers.add(wc);

			if (file.isDirectory()) {
				if (monitor.isCanceled())
					return;
				subDirs.add(file);
			}
		}
		while (!subDirs.isEmpty()) {
			File subDir = subDirs.remove(0);
			search(subDir, existingPaths, foundBrowsers, directoriesVisited, monitor);
			if (monitor.isCanceled()) {
				return;
			}
		}
	}

	/**
	 * Performs special processing when this page's Defaults button has been
	 * pressed.
	 */
	@Override
	protected void performDefaults() {
		setDefaultChoiceSelection();

		BrowserManager.getInstance().currentBrowser = null;
		BrowserManager.getInstance().setupDefaultBrowsers();
		tableViewer.refresh();

		checkedBrowser = BrowserManager.getInstance().getCurrentWebBrowser();
		if (checkedBrowser != null)
			tableViewer.setChecked(checkedBrowser, true);

		super.performDefaults();
	}

	/**
	 * Re-adds absent default Browsers. This helps to restore the default Browsers
	 * in case of some of them were removed by user or any new browser definitions
	 * appeared after the Eclipse Platform update.
	 */
	protected void addDefaults() {
		setDefaultChoiceSelection();

		BrowserManager.getInstance().currentBrowser = null;
		BrowserManager.getInstance().addDefaultBrowsers();
		tableViewer.refresh();

		checkedBrowser = BrowserManager.getInstance().getCurrentWebBrowser();
		if (checkedBrowser != null)
			tableViewer.setChecked(checkedBrowser, true);

		super.updateApplyButton();
	}

	/**
	 * Method declared on IPreferencePage. Subclasses should override
	 */
	@Override
	public boolean performOk() {
		int choice;
		if (internal.getSelection())
			choice = WebBrowserPreference.INTERNAL;
		else
			choice = WebBrowserPreference.EXTERNAL;
		WebBrowserPreference.setBrowserChoice(choice);
		if (checkedBrowser != null) {
			BrowserManager.getInstance().setCurrentWebBrowser(checkedBrowser);
		} else {
			BrowserManager.getInstance().saveBrowsers();
		}

		return true;
	}

	@Override
	public boolean performCancel() {
		BrowserManager.getInstance().loadBrowsers();
		return super.performCancel();
	}

	private void setDefaultChoiceSelection() {
		int browserChoice = getBrowserChoiceDefaultPreference();
		internal.setSelection(browserChoice == WebBrowserPreference.INTERNAL);
		external.setSelection(browserChoice == WebBrowserPreference.EXTERNAL);
	}

	private static int getBrowserChoiceDefaultPreference() {
		IPreferenceStore preferenceStore = WebBrowserUIPlugin.getInstance().getPreferenceStore();
		int browserChoice = preferenceStore.getDefaultInt(WebBrowserPreference.PREF_BROWSER_CHOICE);
		return browserChoice;

	}
}
