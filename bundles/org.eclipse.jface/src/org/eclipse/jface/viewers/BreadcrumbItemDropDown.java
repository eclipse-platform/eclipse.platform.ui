/*******************************************************************************
 * Copyright (c) 2008, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fabio Zadrozny - Bug 465666
 *******************************************************************************/
package org.eclipse.jface.viewers;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageDataProvider;
import org.eclipse.swt.graphics.ImageGcDrawer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * The part of the breadcrumb item with the drop down menu.
 *
 * @since 3.41
 */
class BreadcrumbItemDropDown {
	private static final boolean IS_MAC_WORKAROUND = "carbon".equals(SWT.getPlatform()); //$NON-NLS-1$

	/**
	 * An arrow image descriptor. The images color is related to the list fore- and
	 * background color. This makes the arrow visible even in high contrast mode. If
	 * <code>ltr</code> is true the arrow points to the right, otherwise it points
	 * to the left.
	 */
	private final class AccessibleArrowImage extends CompositeImageDescriptor {

		private final static int ARROW_SIZE = 5;

		private final boolean fLTR;

		public AccessibleArrowImage(boolean ltr) {
			fLTR = ltr;
		}

		@Override
		protected void drawCompositeImage(int width, int height) {
			Display display = fParentComposite.getDisplay();

			ImageDataProvider imageProvider = zoom -> {
				ImageGcDrawer drawer = new ImageGcDrawer() {
					@Override
					public void drawOn(GC gc, int iWidth, int iHeight) {
						gc.setAntialias(SWT.ON);
						Color triangleColor = createColor(SWT.COLOR_LIST_FOREGROUND, SWT.COLOR_LIST_BACKGROUND, 20,
								display);
						gc.setBackground(triangleColor);
						gc.fillPolygon(new int[] { 0, 0, ARROW_SIZE, ARROW_SIZE, 0, ARROW_SIZE * 2 });
						triangleColor.dispose();
					}

					@Override
					public int getGcStyle() {
						return fLTR ? SWT.LEFT_TO_RIGHT : SWT.RIGHT_TO_LEFT;
					}
				};
				Image image = new Image(display, drawer, ARROW_SIZE, ARROW_SIZE * 2);

				ImageData imageData = image.getImageData(zoom);
				image.dispose();
				int zoomedArrowSize = ARROW_SIZE * zoom / 100;
				for (int y1 = 0; y1 < zoomedArrowSize; y1++) {
					// set opaque pixels for top half of the breadcrumb arrow
					for (int x1 = 0; x1 <= y1; x1++) {
						imageData.setAlpha(fLTR ? x1 : zoomedArrowSize - x1 - 1, y1, 255);
					}
					// set transparent pixels for top half of the breadcrumbe arrow
					for (int x1 = y1 + 1; x1 < zoomedArrowSize; x1++) {
						imageData.setAlpha(fLTR ? x1 : zoomedArrowSize - x1 - 1, y1, 0);
					}
				}
				for (int y2 = 0; y2 < zoomedArrowSize; y2++) {
					// set opaque pixels for bottom half of the breadcrumb arrow
					for (int x2 = 0; x2 <= y2; x2++) {
						imageData.setAlpha(fLTR ? x2 : zoomedArrowSize - x2 - 1, zoomedArrowSize * 2 - y2 - 1, 255);
					}
					// set transparent pixels for bottom half of the breadcrumbe arrow
					for (int x2 = y2 + 1; x2 < zoomedArrowSize; x2++) {
						imageData.setAlpha(fLTR ? x2 : zoomedArrowSize - x2 - 1, zoomedArrowSize * 2 - y2 - 1, 0);
					}
				}
				return imageData;
			};
			drawImage(imageProvider, (width / 2) - (ARROW_SIZE / 2), (height / 2) - ARROW_SIZE);
		}

		@Override
		protected Point getSize() {
			return new Point(10, 16);
		}

		private Color createColor(int color1, int color2, int ratio, Display display) {
			RGB rgb1 = display.getSystemColor(color1).getRGB();
			RGB rgb2 = display.getSystemColor(color2).getRGB();

			RGB blend = blend(rgb2, rgb1, ratio);

			return new Color(blend);
		}

		private static RGB blend(RGB rgb1, RGB rgb2, int ratio) {
			int r = (rgb1.red * ratio + rgb2.red * (100 - ratio)) / 100;
			int g = (rgb1.green * ratio + rgb2.green * (100 - ratio)) / 100;
			int b = (rgb1.blue * ratio + rgb2.blue * (100 - ratio)) / 100;

			return new RGB(r, g, b);
		}
	}

	private static final int DROP_DOWN_MIN_WIDTH = 250;
	private static final int DROP_DOWN_MAX_WIDTH = 500;

	private static final int DROP_DOWN_DEFAULT_MIN_HEIGHT = 200;
	private static final int DROP_DOWN_DEFAULT_MAX_HEIGHT = 300;

	private static final String DIALOG_SETTINGS = "BreadcrumbItemDropDown"; //$NON-NLS-1$
	private static final String DIALOG_HEIGHT = "height"; //$NON-NLS-1$

	private final BreadcrumbItem fParent;
	private final Composite fParentComposite;
	private final ToolBar fToolBar;

	private boolean fMenuIsShown;
	private boolean fEnabled;
	private TreeViewer fDropDownViewer;
	private Shell fShell;
	private boolean isResizingProgrammatically;

	public BreadcrumbItemDropDown(BreadcrumbItem parent, Composite composite) {
		fParent = parent;
		fParentComposite = composite;
		fMenuIsShown = false;
		fEnabled = true;

		String showChildrenText = JFaceResources.getString("BreadcrumbItemDropDown_showDropDownMenu_action_toolTip"); //$NON-NLS-1$

		fToolBar = new ToolBar(composite, SWT.FLAT);
		fToolBar.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		fToolBar.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent event) {
				event.result = showChildrenText;
			}
		});
		ToolBarManager manager = new ToolBarManager(fToolBar);

		final Action showDropDownMenuAction = new Action(null, SWT.NONE) {
			@Override
			public void run() {
				Shell shell = fParent.getDropDownShell();
				if (shell != null)
					return;

				shell = fParent.getViewer().getDropDownShell();
				if (shell != null)
					shell.close();

				showMenu();

				fShell.setFocus();
			}
		};

		showDropDownMenuAction.setImageDescriptor(new AccessibleArrowImage(isLTR()));
		showDropDownMenuAction
				.setToolTipText(showChildrenText);
		manager.add(showDropDownMenuAction);

		manager.update(true);
		if (IS_MAC_WORKAROUND) {
			manager.getControl().addMouseListener(new MouseAdapter() {
				// see also BreadcrumbItemDetails#addElementListener(Control)
				@Override
				public void mouseDown(MouseEvent e) {
					showDropDownMenuAction.run();
				}
			});
		}
		fToolBar.setData("org.eclipse.e4.ui.css.id", "BreadcrumbItemDropDownToolBar"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Return the width of this element.
	 *
	 * @return the width of this element
	 */
	public int getWidth() {
		return fToolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
	}

	/**
	 * Set whether the drop down menu is available.
	 *
	 * @param enabled true if available
	 */
	public void setEnabled(boolean enabled) {
		fEnabled = enabled;

		fToolBar.setVisible(enabled);
	}

	/**
	 * Tells whether the menu is shown.
	 *
	 * @return true if the menu is open
	 */
	public boolean isMenuShown() {
		return fMenuIsShown;
	}

	/**
	 * Returns the shell used for the drop down menu if it is shown.
	 *
	 * @return the drop down shell or <code>null</code>
	 */
	public Shell getDropDownShell() {
		if (!isMenuShown())
			return null;

		return fShell;
	}

	/**
	 * Returns the drop down selection provider.
	 *
	 * @return the selection provider of the drop down if the menu is shown,
	 *         {@code null} otherwise.
	 */
	public ISelectionProvider getDropDownSelectionProvider() {
		if (!fMenuIsShown)
			return null;

		return fDropDownViewer;
	}

	/**
	 * Opens the drop down menu.
	 */
	public void showMenu() {

		if (!fEnabled || fMenuIsShown)
			return;

		fMenuIsShown = true;

		fShell = new Shell(fToolBar.getShell(), SWT.RESIZE | SWT.TOOL | SWT.ON_TOP);

		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		fShell.setLayout(layout);

		Composite composite = new Composite(fShell, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		composite.setLayout(gridLayout);

		fDropDownViewer = fParent.getViewer().createDropDownViewer(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		fDropDownViewer.setUseHashlookup(true);

		final Tree tree = (Tree) fDropDownViewer.getControl();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Object input = fParent.getData();
		fParent.getViewer().configureDropDownViewer(fDropDownViewer, input);
		fDropDownViewer.setInput(input);

		setShellBounds(fShell);

		fShell.addControlListener(new ControlAdapter() {
			/*
			 * @see
			 * org.eclipse.swt.events.ControlAdapter#controlResized(org.eclipse.swt.events.
			 * ControlEvent)
			 */
			@Override
			public void controlResized(ControlEvent e) {
				if (isResizingProgrammatically)
					return;

				Point size = fShell.getSize();
				getDialogSettings().put(DIALOG_HEIGHT, size.y);
			}
		});

		fDropDownViewer.addOpenListener(event -> {

			ISelection selection = event.getSelection();
			if (!(selection instanceof IStructuredSelection))
				return;

			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element == null)
				return;

			openElement(element);
		});

		tree.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {

				if (e.button != 1)
					return;

				if ((OpenStrategy.getOpenMethod() & OpenStrategy.SINGLE_CLICK) != 0)
					return;

				Item item = tree.getItem(new Point(e.x, e.y));
				if (item == null)
					return;

				openElement(item.getData());
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		tree.addMouseMoveListener(new MouseMoveListener() {
			TreeItem fLastItem = null;

			@Override
			public void mouseMove(MouseEvent e) {
				if (tree.equals(e.getSource())) {
					TreeItem o = tree.getItem(new Point(e.x, e.y));
					if ((o != null && !o.equals(fLastItem)) || (fLastItem != null && o == null)) {
						boolean showHandPointer = false;
						if (o != null) {
							Object itemData = o.getData();
							showHandPointer = itemData != null && fParent.getViewer().isElementOpenable(itemData);
						}
						tree.setCursor(showHandPointer ? tree.getDisplay().getSystemCursor(SWT.CURSOR_HAND) : null);
					}
					if (o != null) {
						Rectangle clientArea = tree.getClientArea();
						TreeItem currentItem = o;
						if (!o.equals(fLastItem)) {
							fLastItem = o;
							tree.setSelection(new TreeItem[] { fLastItem });
						} else if (e.y - clientArea.y < tree.getItemHeight() / 4) {
							// Scroll up
							if (currentItem.getParentItem() == null) {
								int index = tree.indexOf(o);
								if (index < 1)
									return;

								fLastItem = tree.getItem(index - 1);
								tree.setSelection(new TreeItem[] { fLastItem });
							} else {
								Point p = tree.toDisplay(e.x, e.y);
								Item item = fDropDownViewer.scrollUp(p.x, p.y);
								if (item instanceof TreeItem) {
									fLastItem = (TreeItem) item;
									tree.setSelection(new TreeItem[] { fLastItem });
								}
							}
						} else if (clientArea.y + clientArea.height - e.y < tree.getItemHeight() / 4) {
							// Scroll down
							if (currentItem.getParentItem() == null) {
								int index = tree.indexOf(o);
								if (index >= tree.getItemCount() - 1)
									return;

								fLastItem = tree.getItem(index + 1);
								tree.setSelection(new TreeItem[] { fLastItem });
							} else {
								Point p = tree.toDisplay(e.x, e.y);
								Item item = fDropDownViewer.scrollDown(p.x, p.y);
								if (item instanceof TreeItem) {
									fLastItem = (TreeItem) item;
									tree.setSelection(new TreeItem[] { fLastItem });
								}
							}
						}
					} else {
						fLastItem = null;
					}
				}
			}
		});

		tree.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_UP) {
					TreeItem[] selection = tree.getSelection();
					if (selection.length != 1)
						return;

					int selectionIndex = tree.indexOf(selection[0]);
					if (selectionIndex != 0)
						return;

					fShell.close();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});

		fDropDownViewer.addTreeListener(new ITreeViewerListener() {
			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
			}

			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				tree.setRedraw(false);
				fShell.getDisplay().asyncExec(() -> {
					if (fShell.isDisposed())
						return;

					try {
						resizeShell(fShell);
					} finally {
						tree.setRedraw(true);
					}
				});
			}
		});

		int index = fParent.getViewer().getIndexOfItem(fParent);
		if (index < fParent.getViewer().getItemCount() - 1) {
			BreadcrumbItem childItem = fParent.getViewer().getItem(index + 1);
			Object child = childItem.getData();

			fDropDownViewer.setSelection(new StructuredSelection(child), true);

			TreeItem[] selection = tree.getSelection();
			if (selection.length > 0) {
				tree.setTopItem(selection[0]);
			}
		}

		fShell.setVisible(true);
		installCloser(fShell);
	}

	private void openElement(Object data) {
		if (data == null)
			return;

		// This might or might not open an editor
		fParent.getViewer().fireMenuSelection(data);

		boolean treeHasFocus = !fShell.isDisposed() && fDropDownViewer.getTree().isFocusControl();

		if (fShell.isDisposed())
			return;

		if (!treeHasFocus) {
			fShell.close();
			return;
		}

		toggleExpansionState(data);
	}

	private void toggleExpansionState(Object element) {
		Tree tree = fDropDownViewer.getTree();
		if (fDropDownViewer.getExpandedState(element))
			fDropDownViewer.collapseToLevel(element, 1);
		else {
			tree.setRedraw(false);
			try {
				fDropDownViewer.expandToLevel(element, 1);
				resizeShell(fShell);
			} finally {
				tree.setRedraw(true);
			}
		}
	}

	/**
	 * The closer closes the given shell when the focus is lost.
	 *
	 * @param shell the shell to install the closer to
	 */
	private void installCloser(final Shell shell) {
		final Listener focusListener = event -> {
			Widget focusElement = event.widget;
			boolean isFocusBreadcrumbTreeFocusWidget = focusElement == shell
					|| focusElement instanceof Tree && ((Tree) focusElement).getShell() == shell;
			boolean isFocusWidgetParentShell = focusElement instanceof Control
					&& ((Control) focusElement).getShell().getParent() == shell;

			switch (event.type) {
			case SWT.FocusIn:

				if (!isFocusBreadcrumbTreeFocusWidget && !isFocusWidgetParentShell) {
					shell.close();
				}
				break;

			case SWT.FocusOut:

				if (event.display.getActiveShell() == null) {
					shell.close();
				}
				break;

			default:
				Assert.isTrue(false);
			}
		};

		final Display display = shell.getDisplay();
		display.addFilter(SWT.FocusIn, focusListener);
		display.addFilter(SWT.FocusOut, focusListener);

		final ControlListener controlListener = new ControlListener() {
			@Override
			public void controlMoved(ControlEvent e) {
				shell.close();
			}

			@Override
			public void controlResized(ControlEvent e) {
				shell.close();
			}
		};
		fToolBar.getShell().addControlListener(controlListener);

		shell.addDisposeListener(e -> {

			display.removeFilter(SWT.FocusIn, focusListener);
			display.removeFilter(SWT.FocusOut, focusListener);

			if (!fToolBar.isDisposed()) {
				fToolBar.getShell().removeControlListener(controlListener);
			}
		});
		shell.addShellListener(new ShellListener() {
			@Override
			public void shellActivated(ShellEvent e) {
			}

			@Override
			public void shellClosed(ShellEvent e) {

				if (!fMenuIsShown)
					return;

				fMenuIsShown = false;
				fDropDownViewer = null;
			}

			@Override
			public void shellDeactivated(ShellEvent e) {
			}

			@Override
			public void shellDeiconified(ShellEvent e) {
			}

			@Override
			public void shellIconified(ShellEvent e) {
			}
		});
	}

	private IDialogSettings getDialogSettings() {
		IDialogSettings viewerSettings = fParent.getViewer().getDialogSettings();
		IDialogSettings settings = viewerSettings.getSection(DIALOG_SETTINGS);
		if (settings == null)
			settings = viewerSettings.addNewSection(DIALOG_SETTINGS);
		return settings;
	}

	private int getMaxHeight() {
		try {
			return getDialogSettings().getInt(DIALOG_HEIGHT);
		} catch (NumberFormatException e) {
			return DROP_DOWN_DEFAULT_MAX_HEIGHT;
		}
	}

	/**
	 * Calculates a useful size for the given shell.
	 *
	 * @param shell the shell to calculate the size for.
	 */
	private void setShellBounds(Shell shell) {
		Rectangle rect = fParentComposite.getBounds();
		Rectangle toolbarBounds = fToolBar.getBounds();

		shell.pack();
		Point size = shell.getSize();
		int height = Math.max(Math.min(size.y, getMaxHeight()), DROP_DOWN_DEFAULT_MIN_HEIGHT);
		int width = Math.max(Math.min(size.x, DROP_DOWN_MAX_WIDTH), DROP_DOWN_MIN_WIDTH);

		int imageBoundsX = 0;
		if (fDropDownViewer.getTree().getItemCount() > 0) {
			TreeItem item = fDropDownViewer.getTree().getItem(0);
			imageBoundsX = item.getImageBounds(0).x;
		}

		Rectangle trim = fShell.computeTrim(0, 0, width, height);
		int x = toolbarBounds.x + toolbarBounds.width + 2 + trim.x - imageBoundsX;
		if (!isLTR())
			x += width;

		Point pt = new Point(x, rect.y + rect.height);
		pt = fParentComposite.toDisplay(pt);

		Rectangle monitor = Util.getClosestMonitor(shell.getDisplay(), pt).getClientArea();
		int overlap = (pt.x + width) - (monitor.x + monitor.width);
		if (overlap > 0)
			pt.x -= overlap;
		if (pt.x < monitor.x)
			pt.x = monitor.x;

		shell.setLocation(pt);
		shell.setSize(width, height);
	}

	/**
	 * Set the size of the given shell such that more content can be shown. The
	 * shell size does not exceed a user-configurable maximum.
	 *
	 * @param shell the shell to resize
	 */
	private void resizeShell(final Shell shell) {
		Point size = shell.getSize();
		int currentWidth = size.x;
		int currentHeight = size.y;

		int maxHeight = getMaxHeight();

		if (currentHeight >= maxHeight && currentWidth >= DROP_DOWN_MAX_WIDTH)
			return;

		Point preferedSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);

		int newWidth;
		if (currentWidth >= DROP_DOWN_MAX_WIDTH) {
			newWidth = currentWidth;
		} else {
			newWidth = Math.min(Math.max(preferedSize.x, currentWidth), DROP_DOWN_MAX_WIDTH);
		}
		int newHeight;
		if (currentHeight >= maxHeight) {
			newHeight = currentHeight;
		} else {
			newHeight = Math.min(Math.max(preferedSize.y, currentHeight), maxHeight);
		}

		if (newHeight != currentHeight || newWidth != currentWidth) {
			shell.setRedraw(false);
			try {
				isResizingProgrammatically = true;
				shell.setSize(newWidth, newHeight);
				if (!isLTR()) {
					Point location = shell.getLocation();
					shell.setLocation(location.x - (newWidth - currentWidth), location.y);
				}
			} finally {
				isResizingProgrammatically = false;
				shell.setRedraw(true);
			}
		}
	}

	/**
	 * Tells whether this the breadcrumb is in LTR or RTL mode.
	 *
	 * @return <code>true</code> if the breadcrumb in left-to-right mode,
	 *         <code>false</code> otherwise
	 */
	private boolean isLTR() {
		return (fParentComposite.getStyle() & SWT.RIGHT_TO_LEFT) == 0;
	}
}
