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
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.internal.AssociatedWindow;
import org.eclipse.ui.internal.WorkbenchWindow;
/**
 * The ProgressFloatingWindow is a window that opens next to an animation item.
 */
class ProgressFloatingWindow extends AssociatedWindow {
	TableViewer viewer;
	WorkbenchWindow window;
	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param workbenchWindow
	 * @param associatedControl
	 */
	ProgressFloatingWindow(WorkbenchWindow workbenchWindow,
			Control associatedControl) {
		super(workbenchWindow.getShell(), associatedControl);
		this.window = workbenchWindow;
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
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite root) {
		Control buttonBar = createButtons(root);
		viewer = new TableViewer(root, SWT.MULTI) {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.TableViewer#doUpdateItem(org.eclipse.swt.widgets.Widget,
			 *      java.lang.Object, boolean)
			 */
			protected void doUpdateItem(Widget widget, Object element,
					boolean fullMap) {
				super.doUpdateItem(widget, element, fullMap);
				adjustSize();
			}
		};
		viewer.setUseHashlookup(true);
		viewer.setSorter(ProgressManagerUtil.getProgressViewerSorter());
		setBackground(viewer.getControl());
		FormData tableData = new FormData();
		tableData.left = new FormAttachment(0);
		tableData.right = new FormAttachment(buttonBar,0);
		tableData.top = new FormAttachment(0);
		viewer.getTable().setLayoutData(tableData);
		initContentProvider();
		viewer.setLabelProvider(viewerLabelProvider());
		return viewer.getControl();
	}
	/**
	 * Return the label provider for the viewer.
	 * 
	 * @return LabelProvider
	 */
	private LabelProvider viewerLabelProvider() {
		return new LabelProvider() {
			private String ellipsis = ProgressMessages.getString("ProgressFloatingWindow.EllipsisValue"); //$NON-NLS-1$
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				JobTreeElement info = (JobTreeElement) element;
				return shortenText(info.getCondensedDisplayString(), viewer
						.getControl().getDisplay());
			}
			/**
			 * Shorten the given text <code>t</code> so that its length
			 * doesn't exceed the given width. The default implementation
			 * replaces characters in the center of the original string with an
			 * ellipsis ("..."). Override if you need a different strategy.
			 */
			protected String shortenText(String textValue, Display display) {
				if (textValue == null)
					return null;
				GC gc = new GC(display);
				int maxWidth = viewer.getControl().getBounds().width - 25;
				if (gc.textExtent(textValue).x < maxWidth) {
					gc.dispose();
					return textValue;
				}
				int length = textValue.length();
				int ellipsisWidth = gc.textExtent(ellipsis).x;
				int pivot = length / 2;
				int start = pivot;
				int end = pivot + 1;
				while (start >= 0 && end < length) {
					String s1 = textValue.substring(0, start);
					String s2 = textValue.substring(end, length);
					int l1 = gc.textExtent(s1).x;
					int l2 = gc.textExtent(s2).x;
					if (l1 + ellipsisWidth + l2 < maxWidth) {
						gc.dispose();
						return s1 + ellipsis + s2;
					}
					start--;
					end++;
				}
				//If for some reason we fall through abort
				gc.dispose();
				return textValue;
			}
		};
	}
	/**
	 * Adjust the size of the viewer.
	 */
	private void adjustSize() {
		Point size = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		size.x += 5;
		size.y += 5;
		int maxSize = getMaximumSize(viewer.getTable().getDisplay());
		if (size.x > maxSize);
		size.x = maxSize;
		getShell().setSize(size);
		moveShell(getShell());
		setRegion();
	}
	/**
	 * Set the region of the shell.
	 */
	private void setRegion() {
		Region oldRegion = getShell().getRegion();
		Point shellSize = getShell().getSize();
		Region r = new Region(getShell().getDisplay());
		Rectangle rect = new Rectangle(0, 0, shellSize.x, shellSize.y);
		r.add(rect);
		Region cornerRegion = new Region(getShell().getDisplay());
		//top right corner region
		cornerRegion.add(new Rectangle(shellSize.x - 5, 0, 5, 1));
		cornerRegion.add(new Rectangle(shellSize.x - 3, 1, 3, 1));
		cornerRegion.add(new Rectangle(shellSize.x - 2, 2, 2, 1));
		cornerRegion.add(new Rectangle(shellSize.x - 1, 3, 1, 2));
		//bottom right corner region
		int y = shellSize.y;
		cornerRegion.add(new Rectangle(shellSize.x - 5, y - 1, 5, 1));
		cornerRegion.add(new Rectangle(shellSize.x - 3, y - 2, 3, 1));
		cornerRegion.add(new Rectangle(shellSize.x - 2, y - 3, 2, 1));
		cornerRegion.add(new Rectangle(shellSize.x - 1, y - 5, 1, 2));
		//top left corner region
		cornerRegion.add(new Rectangle(0, 0, 5, 1));
		cornerRegion.add(new Rectangle(0, 1, 3, 1));
		cornerRegion.add(new Rectangle(0, 2, 2, 1));
		cornerRegion.add(new Rectangle(0, 3, 1, 2));
		//bottom left corner region
		cornerRegion.add(new Rectangle(0, y - 5, 1, 2));
		cornerRegion.add(new Rectangle(0, y - 3, 2, 1));
		cornerRegion.add(new Rectangle(0, y - 2, 3, 1));
		cornerRegion.add(new Rectangle(0, y - 1, 5, 1));
		r.subtract(cornerRegion);
		getShell().setRegion(r);
		if (oldRegion != null)
			oldRegion.dispose();
	}
	/**
	 * Sets the content provider for the viewer.
	 */
	protected void initContentProvider() {
		IContentProvider provider = new ProgressTableContentProvider(viewer);
		viewer.setContentProvider(provider);
		viewer.setInput(provider);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.AssociatedWindow#getTransparencyValue()
	 */
	protected int getTransparencyValue() {
		return 50;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		if (getShell() == null)
			return super.close();
		Region oldRegion = getShell().getRegion();
		boolean result = super.close();
		if (result && oldRegion != null)
			oldRegion.dispose();
		return result;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#open()
	 */
	public int open() {
		if (getShell() == null) {
			// create the window
			create();
		}
		// limit the shell size to the display size
		constrainShellSize();
		// open the window
		getShell().setVisible(true);
		return getReturnCode();
	}
	/**
	 * Get the maximum size of the window based on the display.
	 * 
	 * @param display
	 * @return int
	 */
	private int getMaximumSize(Display display) {
		return display.getBounds().width / 5;
	}
	/**
	 * Set the background color of the control to the info background.
	 * 
	 * @param control
	 */
	private void setBackground(Control control) {
		control.setBackground(control.getDisplay().getSystemColor(
				SWT.COLOR_INFO_BACKGROUND));
	}
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
			}
		});
		
		minimize.setToolTipText(ProgressMessages.getString("ProgressFloatingWindow.CloseToolTip")); //$NON-NLS-1$
		
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
				ProgressManagerUtil.openProgressView(window);
			}
		});
		
		maximize.setToolTipText(ProgressMessages.getString("ProgressFloatingWindow.OpenToolTip")); //$NON-NLS-1$
		
		FormData barData = new FormData();
		barData.right = new FormAttachment(100);
		barData.top = new FormAttachment(0);
		buttonBar.setLayoutData(barData);
		return buttonBar;
	}
}
