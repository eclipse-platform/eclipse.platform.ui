/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.browser.internal;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
public class WebBrowserPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	protected Button internal;
   protected Button external;
	protected Table table;
	protected CheckboxTableViewer tableViewer;
	protected Button edit;
	protected Button remove;
	protected Button search;
	
	protected Label location;
	protected Label parameters;
	
	class BrowserContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			List list = new ArrayList();
			Iterator iterator = BrowserManager.getInstance().getWebBrowsers().iterator();
			while (iterator.hasNext()) {
				IBrowserDescriptor browser = (IBrowserDescriptor) iterator.next();
				list.add(browser);
			}
			return list.toArray();
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// do nothing
		}
		
		public void dispose() {
			// do nothing
		}
	}
	
	class BrowserTableLabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return ImageResource.getImage(ImageResource.IMG_EXTERNAL_BROWSER);
		}

		public String getColumnText(Object element, int columnIndex) {
			IBrowserDescriptor browser = (IBrowserDescriptor)element;
			return notNull(browser.getName());
		}
		
		protected String notNull(String s) {
			if (s == null)
				return "";
			return s;
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void addListener(ILabelProviderListener listener) {
			// do nothing
		}
		
		public void removeListener(ILabelProviderListener listener) {
			// do nothing
		}
		
		public void dispose() {
			// do nothing
		}
	}

	/**
	 * WebBrowserPreferencePage constructor comment.
	 */
	public WebBrowserPreferencePage() {
		super();
		noDefaultAndApplyButton();
	}

	/**
	 * Create the preference options.
	 *
	 * @param parent org.eclipse.swt.widgets.Composite
	 * @return org.eclipse.swt.widgets.Control
	 */
	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(4);
		layout.verticalSpacing = convertVerticalDLUsToPixels(3);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
		composite.setLayoutData(data);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, ContextIds.PREF_BROWSER);
		
		Label label = new Label(composite, SWT.WRAP);
		label.setText(WebBrowserUIPlugin.getResource("%preferenceWebBrowserDescription"));
		data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
      
      internal = new Button(composite, SWT.RADIO);
      internal.setText(WebBrowserUIPlugin.getResource("%prefInternalBrowser"));
		data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		data.horizontalSpan = 2;
		internal.setLayoutData(data);
		
		if (!WebBrowserUtil.canUseInternalWebBrowser())
			internal.setEnabled(false);
      
      external = new Button(composite, SWT.RADIO);
      external.setText(WebBrowserUIPlugin.getResource("%prefExternalBrowser"));
		data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		data.horizontalSpan = 2;
		external.setLayoutData(data);
		
		label = new Label(composite, SWT.NONE);
		label.setText(WebBrowserUIPlugin.getResource("%browserList"));
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER);
		data.horizontalSpan = 2;
		data.horizontalIndent = 15;
		label.setLayoutData(data);
		
		table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		data = new GridData(GridData.FILL_BOTH);
      data.horizontalIndent = 15;
		table.setLayoutData(data);
		table.setHeaderVisible(false);
		table.setLinesVisible(false);
		
		TableLayout tableLayout = new TableLayout();
		new TableColumn(table, SWT.NONE);
		tableLayout.addColumnData(new ColumnWeightData(100));
		table.setLayout(tableLayout);
		
		tableViewer = new CheckboxTableViewer(table);
		tableViewer.setContentProvider(new BrowserContentProvider());
		tableViewer.setLabelProvider(new BrowserTableLabelProvider());
		tableViewer.setInput("root");

		// uncheck any other elements that might be checked and leave only the element checked to
		// remain checked since one can only chose one brower at a time to be current.
		tableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent e) {
				checkNewDefaultBrowser(e.getElement());
				IBrowserDescriptor browser = (IBrowserDescriptor) e.getElement();
				BrowserManager.getInstance().setCurrentWebBrowser(browser);
				
				// if no other browsers are checked, don't allow the single one currently
				// checked to become unchecked, and lose a current browser. That is, don't
				// permit unchecking if no other item is checked which is supposed to be the case.
				Object[] obj = tableViewer.getCheckedElements();
			   if (obj.length == 0)
			    	tableViewer.setChecked(e.getElement(), true);
			}
		});
		
		// set a default, checked browser based on the current browser. If there is not a
		// current browser, but the first item exists, use that instead.
		// This will work currently until workbench shutdown, because current browser is not yet persisted.
		IBrowserDescriptor browser = BrowserManager.getInstance().getCurrentWebBrowser();
		if (browser != null)
			tableViewer.setChecked(browser, true);
		else {
			Object obj = tableViewer.getElementAt(0);
			if (obj != null)
				tableViewer.setChecked(obj, true);
		}
		
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object obj = getSelection(event.getSelection());
				boolean sel = !tableViewer.getSelection().isEmpty();
				remove.setEnabled(sel);
				edit.setEnabled(sel);
			}
		});
		
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = ((StructuredSelection) tableViewer.getSelection());
				if (sel.getFirstElement() != null) {
					Object browser2 = sel.getFirstElement();
					if (browser2 instanceof IBrowserDescriptor) {
						IBrowserDescriptorWorkingCopy wc = ((IBrowserDescriptor) browser2).getWorkingCopy();
						BrowserDescriptorDialog dialog = new BrowserDescriptorDialog(getShell(), wc);
						if (dialog.open() != Window.CANCEL) {
							try {
								tableViewer.refresh(wc.save());
							} catch (Exception ex) {
								// ignore
							}
						}
					}
				}
			}
		});
		
		table.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.DEL) {
					IStructuredSelection sel = ((StructuredSelection) tableViewer.getSelection());
					if (sel.getFirstElement() != null) {
						IBrowserDescriptor browser2 = (IBrowserDescriptor) sel.getFirstElement();
						try {
		               browser2.delete();
							tableViewer.remove(browser2);
							
							// need here to ensure that if the item deleted was checked, ie. was
							// the current browser, that the new current browser will be the first in the
							// list, typically, the internal browser, which cannot be
							// deleted, and be current
							BrowserManager manager = BrowserManager.getInstance();
							if (browser2 == manager.getCurrentWebBrowser()) {
								if (manager.browsers.size() > 0) {
									IBrowserDescriptor wb = (IBrowserDescriptor) manager.browsers.get(0);
									manager.setCurrentWebBrowser(wb);
									tableViewer.setChecked(wb, true);
								}
							}
						} catch (Exception ex) {
							// ignore
						}
					}
				}
			}

			public void keyReleased(KeyEvent e) {
				// ignore
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
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		buttonComp.setLayoutData(data);
		
		final Button add = SWTUtil.createButton(buttonComp, WebBrowserUIPlugin.getResource("%add"));
		add.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				BrowserDescriptorDialog dialog = new BrowserDescriptorDialog(getShell());
				if (dialog.open() == Window.CANCEL)
					return;
				tableViewer.refresh();
			}
		});
		
		edit = SWTUtil.createButton(buttonComp, WebBrowserUIPlugin.getResource("%edit"));
		edit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = ((StructuredSelection) tableViewer.getSelection());
				Object browser2 = sel.getFirstElement();
				if (browser2 instanceof IBrowserDescriptor) {
					IBrowserDescriptorWorkingCopy wc = ((IBrowserDescriptor) browser2).getWorkingCopy();
					BrowserDescriptorDialog dialog = new BrowserDescriptorDialog(getShell(), wc);
					if (dialog.open() != Window.CANCEL) {
						try {
							tableViewer.refresh(wc.save());
						} catch (Exception ex) {
							// ignore
						}
					}
				}
			}
		});

		remove = SWTUtil.createButton(buttonComp, WebBrowserUIPlugin.getResource("%remove"));
		remove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = ((StructuredSelection) tableViewer.getSelection());
				IBrowserDescriptor browser2 = (IBrowserDescriptor) sel.getFirstElement();
				try {
               browser2.delete();
					tableViewer.remove(browser2);
					
					// need here to ensure that if the item deleted was checked, ie. was
					// the current browser, that the new current browser will be the first in the
					// list, typically, the internal browser, which cannot be
					// deleted, and be current
					BrowserManager manager = BrowserManager.getInstance();
					if (browser2 == manager.getCurrentWebBrowser()) {
						if (manager.browsers.size() > 0) {
							IBrowserDescriptor wb = (IBrowserDescriptor) manager.browsers.get(0);
							manager.setCurrentWebBrowser(wb);
							tableViewer.setChecked(wb, true);
						}
					}
				} catch (Exception ex) {
					// ignore
				}
			}
		});
		
		search = SWTUtil.createButton(buttonComp, WebBrowserUIPlugin.getResource("%search"));
		data = (GridData) search.getLayoutData();
		data.verticalIndent = 9;
		search.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				final List foundBrowsers = new ArrayList();
				final List existingPaths = WebBrowserUtil.getExternalBrowserPaths();

				// select a target directory for the search
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setMessage(WebBrowserUIPlugin.getResource("%selectDirectory"));
				dialog.setText(WebBrowserUIPlugin.getResource("%directoryDialogTitle"));

				String path = dialog.open();
				if (path == null)
					return;
				
				final File rootDir = new File(path);
				ProgressMonitorDialog pm = new ProgressMonitorDialog(getShell());

				IRunnableWithProgress r = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) {
						monitor.beginTask(WebBrowserUIPlugin.getResource("%searchingTaskName"), IProgressMonitor.UNKNOWN);
						search(rootDir, existingPaths, foundBrowsers, monitor);
						monitor.done();
					}
				};

				try {
					pm.run(true, true, r);
				} catch (InvocationTargetException ex) {
					Trace.trace(Trace.SEVERE, "Invocation Exception occured running monitor: " + ex);
				} catch (InterruptedException ex) {
					Trace.trace(Trace.SEVERE, "Interrupted exception occured running monitor: " + ex);
					return;
				}
				
				if (pm.getProgressMonitor().isCanceled())
					return;
				
				List browsersToCreate = foundBrowsers;
				
				if (browsersToCreate == null) // cancelled
					return;
				
				if (browsersToCreate.isEmpty()) { // no browsers found
					WebBrowserUtil.openMessage(WebBrowserUIPlugin.getResource("%searchingNoneFound"));
					return;
				}
				
				Iterator iterator = browsersToCreate.iterator();
				while (iterator.hasNext()) {
					IBrowserDescriptorWorkingCopy browser2 = (IBrowserDescriptorWorkingCopy) iterator.next();
					browser2.save();
				}
				tableViewer.refresh();
			}
		});
		PlatformUI.getWorkbench().getHelpSystem().setHelp(search, ContextIds.PREF_BROWSER_EXTERNAL_SEARCH);
		
		tableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent e) {
				checkNewDefaultBrowser(e.getElement());
				IBrowserDescriptor browser2 = (IBrowserDescriptor) e.getElement();
				BrowserManager.getInstance().setCurrentWebBrowser(browser2);
			}
		});

		external.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				boolean inte = !external.getSelection();
				boolean sel = !tableViewer.getSelection().isEmpty();
				
				table.setEnabled(!inte);
				add.setEnabled(!inte);
				edit.setEnabled(!inte && sel);
				remove.setEnabled(!inte && sel);
				search.setEnabled(!inte);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// ignore
			}
		});
		internal.setSelection(WebBrowserPreference.isUseInternalBrowser());
		external.setSelection(!WebBrowserPreference.isUseInternalBrowser());
		
		boolean sel = !tableViewer.getSelection().isEmpty();
		boolean inte = WebBrowserPreference.isUseInternalBrowser();
		table.setEnabled(!inte);
		add.setEnabled(!inte);
		edit.setEnabled(!inte && sel);
		remove.setEnabled(!inte && sel);
		search.setEnabled(!inte);
		
		Dialog.applyDialogFont(composite);
		
		return composite;
	}
	
	/**
	 * Initializes this preference page using the passed workbench.
	 *
	 * @param workbench the current workbench
	 */
	public void init(IWorkbench workbench) {
		// do nothing
	}
	
	/**
	 * 
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible)
			setTitle(WebBrowserUIPlugin.getResource("%preferenceWebBrowserTitleLong"));
	}
	
	protected Object getSelection(ISelection sel2) {
		IStructuredSelection sel = (IStructuredSelection) sel2;
		return sel.getFirstElement();
	}
	
	// Uncheck all the items except the current one that was just checked
	protected void checkNewDefaultBrowser(Object browser) {
		TableItem[] children = tableViewer.getTable().getItems();
		for (int i = 0; i < children.length; i++) {
			TableItem item = children[i];
			
			if (!(item.getData().equals(browser))) 
				item.setChecked(false);
		}
	}

	protected static void search(File directory, List existingPaths, List foundBrowsers, IProgressMonitor monitor) {
		if (monitor.isCanceled())
			return;

		String[] names = directory.list();
		List subDirs = new ArrayList();

		for (int i = 0; i < names.length; i++) {
			if (monitor.isCanceled())
				return;

			File file = new File(directory, names[i]);
			
			if (existingPaths.contains(file.getAbsolutePath().toLowerCase()))
				continue;

			IBrowserDescriptorWorkingCopy wc = WebBrowserUtil.createExternalBrowser(file);
			if (wc != null)
				foundBrowsers.add(wc);

			try {
				monitor.subTask(
					MessageFormat.format(WebBrowserUIPlugin.getResource("%searching"),
						new String[] { Integer.toString(foundBrowsers.size()), file.getCanonicalPath()}));
			} catch (IOException ioe) {
				// ignore
			}

			if (file.isDirectory()) {
				if (monitor.isCanceled())
					return;
				subDirs.add(file);
			}
		}
		while (!subDirs.isEmpty()) {
			File subDir = (File) subDirs.remove(0);
			search(subDir, existingPaths, foundBrowsers, monitor);
			if (monitor.isCanceled()) {
				return;
			}
		}
	}
	
	/**
	 * Performs special processing when this page's Defaults button has been pressed.
	 */
	protected void performDefaults() {
		internal.setSelection(WebBrowserPreference.isDefaultUseInternalBrowser());
		external.setSelection(!WebBrowserPreference.isDefaultUseInternalBrowser());
	
		super.performDefaults();
	}

	/**
	 * Method declared on IPreferencePage.
	 * Subclasses should override
	 */
	public boolean performOk() {
		WebBrowserPreference.setUseInternalBrowser(internal.getSelection());
		
		return true;
	}
}