/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.ui.internal.AssociatedWindow;
import org.eclipse.ui.internal.WorkbenchWindow;
/**
 * The ProgressFloatingWindow is a window that opens next to an animation item.
 */
class ProgressFloatingWindow extends AssociatedWindow {
	ProgressViewer viewer;
	WorkbenchWindow window;
	final int borderSize = 1;
	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param workbenchWindow
	 *            the workbench window.
	 * @param associatedControl
	 *            the associated control.
	 */
	ProgressFloatingWindow(WorkbenchWindow workbenchWindow,
			Control associatedControl) {
		super(workbenchWindow.getShell(), associatedControl,
				AssociatedWindow.TRACK_OUTER_BOTTOM_RHS);
		this.window = workbenchWindow;
		//Workaround for Bug 50917
		if ("carbon".equals(SWT.getPlatform())) //$NON-NLS-1$
			setShellStyle(SWT.NO_TRIM | SWT.ON_TOP);
		else
			setShellStyle(SWT.NO_TRIM);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#getLayout()
	 */
	protected Layout getLayout() {
		FormLayout layout = new FormLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		return layout;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.AssociatedWindow#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setLayout(getLayout());
		setBackground(newShell);
		addRoundBorder(newShell,borderSize);
		
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite root) {
		Control buttonBar = createButtons(root);
		viewer = new ProgressViewer(root, SWT.NONE,2) {
			/*
			 * * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.TableViewer#doUpdateItem(org.eclipse.swt.widgets.Widget,
			 *      java.lang.Object, boolean)
			 */
			protected void doUpdateItem(Widget widget, Object element,
					boolean fullMap) {
				super.doUpdateItem(widget, element, fullMap);
				moveShell(getShell(), AssociatedWindow.ALWAYS_VISIBLE);
				
			}
		};
		viewer.setUseHashlookup(true);
		viewer.setSorter(ProgressManagerUtil.getProgressViewerSorter());
		Control control = viewer.getControl();
		setBackground(control);
		FormData tableData = new FormData();
		tableData.left = new FormAttachment(0);
		tableData.right = new FormAttachment(buttonBar, 0);
		tableData.top = new FormAttachment(0);
		tableData.bottom = new FormAttachment(100);
		
		Point preferredSize = viewer.getSizeHints();
		
		tableData.width = preferredSize.x;
		tableData.height = preferredSize.y;
		
		viewer.getControl().setLayoutData(tableData);
		initContentProvider();
		viewer.setLabelProvider(viewerLabelProvider());
		root.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail == SWT.TRAVERSE_ESCAPE) {
					event.doit = false;
				}
			}
		});
		
		return viewer.getControl();
	}
	/**
	 * Return the label provider for the viewer.
	 * 
	 * @return LabelProvider the shortened text.
	 */
	private LabelProvider viewerLabelProvider() {
		return new ProgressViewerLabelProvider(viewer.getControl());
	}
	
	
	/**
	 * Set the content provider for the viewer.
	 */
	protected void initContentProvider() {
		IContentProvider provider = new ProgressViewerContentProvider(viewer);
		viewer.setContentProvider(provider);
		viewer.setInput(provider);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#open()
	 */
	public int open() {
		if (getShell() == null) {
			create();
		}
		constrainShellSize();
		getShell().setVisible(true);
		moveShell(getShell(), AssociatedWindow.ALWAYS_VISIBLE);
		return getReturnCode();
	}
	/**
	 * Set the background color of the control to the info background.
	 * 
	 * @param control
	 *            the shell's control.
	 */
	private void setBackground(Control control) {
		control.setBackground(control.getDisplay().getSystemColor(
				SWT.COLOR_INFO_BACKGROUND));
	}
	/**
	 * Create the buttons for the progress floating window.
	 * 
	 * @param parent
	 *            the parent composite.
	 */
	private Control createButtons(Composite parent) {
		ToolBar buttonBar = new ToolBar(parent, SWT.HORIZONTAL);
		setBackground(buttonBar);
		ToolItem minimize = new ToolItem(buttonBar, SWT.NONE);
		minimize
				.setImage(JFaceResources.getImage(ProgressManager.MINIMIZE_KEY));
		minimize.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				window.toggleFloatingWindow();
				//If the minimize failed to close the floating
				//window do a close anyways
				Shell remainingShell = getShell();
				if (remainingShell == null || remainingShell.isDisposed())
					return;
				close();
			}
		});
		minimize.setToolTipText(ProgressMessages
				.getString("ProgressFloatingWindow.CloseToolTip")); //$NON-NLS-1$
		createMaximizeButton(buttonBar);
		FormData barData = new FormData();
		barData.right = new FormAttachment(100);
		barData.top = new FormAttachment(0);
		buttonBar.setLayoutData(barData);
		return buttonBar;
	}
	/**
	 * Create the maximize button if there is a progress view we can open.
	 * 
	 * @param buttonBar
	 */
	private void createMaximizeButton(ToolBar buttonBar) {
		//If there is no progress view do not create the
		//button.
		if (ProgressManagerUtil.missingProgressView(window))
			return;
		ToolItem maximize = new ToolItem(buttonBar, SWT.NONE);
		maximize
				.setImage(JFaceResources.getImage(ProgressManager.MAXIMIZE_KEY));
		maximize.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				window.toggleFloatingWindow();
				ProgressManagerUtil.openProgressView(window);
			}
		});
		maximize.setToolTipText(ProgressMessages
				.getString("ProgressFloatingWindow.OpenToolTip")); //$NON-NLS-1$
	}
}