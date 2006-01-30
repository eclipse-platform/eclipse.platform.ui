/*******************************************************************************
 * Copyright (c) 2001, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.properties.tabbed.internal.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.tabbed.ITabItem;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.eclipse.ui.views.properties.tabbed.internal.l10n.TabbedPropertyMessages;


/**
 * Shows the list of tabs in the tabbed property sheet page.
 * 
 * @author Anthony Hunter
 */
public class TabbedPropertyList
	extends Composite {

	private static final ListElement[] ELEMENTS_EMPTY = new ListElement[0];

	protected static final int NONE = -1;

	protected static final int INDENT = 7;

	private boolean focus = false;

	private ListElement[] elements;

	private int selectedElementIndex = NONE;

	private int topVisibleIndex = NONE;

	private int bottomVisibleIndex = NONE;

	private TopNavigationElement topNavigationElement;

	private BottomNavigationElement bottomNavigationElement;

	private int widestLabelIndex = NONE;

	private int tabsThatFitInComposite = NONE;

	private Color hoverBackground;

	private Color defaultBackground;

	private Color defaultForeground;

	private Color activeBackground;

	private Color border;

	private Color darkShadow;

	private Color textColor;

	private TabbedPropertySheetWidgetFactory factory;

	public class ListElement
		extends Canvas {

		private ITabItem tab;

		private int index;

		private boolean selected;

		private boolean hover;

		public ListElement(Composite parent, final ITabItem tab,
				int index) {
			super(parent, SWT.NO_FOCUS);
			this.tab = tab;
			hover = false;
			selected = false;
			this.index = index;

			addPaintListener(new PaintListener() {

				public void paintControl(PaintEvent e) {
					paint(e);
				}
			});
			addMouseListener(new MouseAdapter() {

				public void mouseUp(MouseEvent e) {
					if (!selected) {
						select(getIndex(ListElement.this), true);
						/*
						 * We set focus to the tabbed property composite so that
						 * focus is moved to the appropriate widget in the
						 * section.
						 */
						Composite tabbedPropertyComposite = getParent();
						while (!(tabbedPropertyComposite instanceof TabbedPropertyComposite)) {
							tabbedPropertyComposite = tabbedPropertyComposite
								.getParent();
						}
						tabbedPropertyComposite.setFocus();
					}
				}
			});
			addMouseMoveListener(new MouseMoveListener() {

				public void mouseMove(MouseEvent e) {
					if (!hover) {
						hover = true;
						redraw();
					}
				}
			});
			addMouseTrackListener(new MouseTrackAdapter() {

				public void mouseExit(MouseEvent e) {
					hover = false;
					redraw();
				}
			});
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
			redraw();
		}

		/**
		 * Draws elements and collects element areas.
		 */
		private void paint(PaintEvent e) {
			e.gc.setBackground(defaultBackground);
			e.gc.setForeground(defaultForeground);
			Rectangle bounds = getBounds();
			e.gc.fillRectangle(0, 0, bounds.width, bounds.height);

			// draw tab
			if (selected) {
				e.gc.setBackground(activeBackground);
			} else if (hover) {
				e.gc.setBackground(hoverBackground);
			} else {
				e.gc.setBackground(defaultBackground);
			}

			/* draw the fill in the tab */
			if (selected) {
				e.gc.fillRectangle(4, 0, bounds.width, bounds.height);
				e.gc.fillRectangle(3, 1, 3, bounds.height);
			} else if (hover) {
				e.gc.fillRectangle(2, 0, bounds.width - 4, bounds.height);
			}

			/* draw the border */
			if (selected) {
				e.gc.setForeground(border);
				e.gc.drawLine(4, 0, bounds.width - 1, 0);
				e.gc.drawPoint(3, 1);
				e.gc.drawPoint(3, bounds.height - 1);
				e.gc.drawLine(2, 2, 2, bounds.height - 2);
			} else {
				e.gc.setForeground(border);
				if (getSelectionIndex() != NONE
					&& getSelectionIndex() + 1 == index) {
					e.gc.drawLine(4, 0, bounds.width - 1, 0);
				} else {
					e.gc.drawLine(2, 0, bounds.width - 3, 0);
				}
				e.gc.drawLine(bounds.width - 1, 0, bounds.width - 1,
					bounds.height - 1);
			}

			int textIndent = INDENT;
			FontMetrics fm = e.gc.getFontMetrics();
			int height = fm.getHeight();
			int textMiddle = (bounds.height - height) / 2;

			if (selected && tab.getImage() != null
				&& !tab.getImage().isDisposed()) {
				/* draw the icon for the selected tab */
				e.gc.drawImage(tab.getImage(), textIndent - 2, textMiddle);
				textIndent = textIndent + 16 + 2;
			} else if (tab.isIndented()) {
				/* draw the indent tiny square */
				e.gc.drawRectangle(20, textMiddle + 6, 1, 1);
				textIndent = textIndent + 16 + 4;
			}

			/* draw the text */
			e.gc.setForeground(textColor);
			e.gc.drawText(tab.getText(), textIndent, textMiddle);
			if (((TabbedPropertyList) getParent()).focus && selected) {
				/* draw a line if the tab has focus */
				Point point = e.gc.textExtent(tab.getText());
				e.gc.drawLine(textIndent, bounds.height - 4, textIndent
					+ point.x, bounds.height - 4);
			}
		}

		public String getText() {
			return tab.getText();
		}

		public String toString() {
			return tab.getText();
		}
	}

	public class TopNavigationElement
		extends Canvas {

		/**
		 * @param parent
		 */
		public TopNavigationElement(Composite parent) {
			super(parent, SWT.NO_FOCUS);
			addPaintListener(new PaintListener() {

				public void paintControl(PaintEvent e) {
					paint(e);
				}
			});
			addMouseListener(new MouseAdapter() {

				public void mouseUp(MouseEvent e) {
					if (isUpScrollRequired()) {
						bottomVisibleIndex--;
						if (topVisibleIndex != 0) {
							topVisibleIndex--;
						}
						layoutTabs();
						topNavigationElement.redraw();
						bottomNavigationElement.redraw();
					}
				}
			});
		}

		/**
		 * @param e
		 */
		private void paint(PaintEvent e) {
			e.gc.setBackground(defaultBackground);
			e.gc.setForeground(defaultForeground);
			Rectangle bounds = getBounds();

			if (elements.length != 0) {
				e.gc.fillRectangle(0, 0, bounds.width, bounds.height);
				e.gc.setForeground(border);
				e.gc.drawLine(bounds.width - 1, 0, bounds.width - 1,
					bounds.height - 1);
			} else {
				e.gc.setBackground(activeBackground);
				e.gc.fillRectangle(0, 0, bounds.width, bounds.height);
				int textIndent = INDENT;
				FontMetrics fm = e.gc.getFontMetrics();
				int height = fm.getHeight();
				int textMiddle = (bounds.height - height) / 2;
				e.gc.setForeground(textColor);
				String properties_not_available = TabbedPropertyMessages.TabbedPropertyList_properties_not_available;
				e.gc.drawText(properties_not_available, textIndent, textMiddle);
			}

			if (isUpScrollRequired()) {
				e.gc.setForeground(darkShadow);
				int middle = bounds.width / 2;
				e.gc.drawLine(middle + 1, 3, middle + 5, 7);
				e.gc.drawLine(middle, 3, middle - 4, 7);
				e.gc.drawLine(middle - 4, 8, middle + 5, 8);

				e.gc.setForeground(activeBackground);
				e.gc.drawLine(middle, 4, middle + 1, 4);
				e.gc.drawLine(middle - 1, 5, middle + 2, 5);
				e.gc.drawLine(middle - 2, 6, middle + 3, 6);
				e.gc.drawLine(middle - 3, 7, middle + 4, 7);
			}
		}
	}

	public class BottomNavigationElement
		extends Canvas {

		/**
		 * @param parent
		 */
		public BottomNavigationElement(Composite parent) {
			super(parent, SWT.NO_FOCUS);
			addPaintListener(new PaintListener() {

				public void paintControl(PaintEvent e) {
					paint(e);
				}
			});
			addMouseListener(new MouseAdapter() {

				public void mouseUp(MouseEvent e) {
					if (isDownScrollRequired()) {
						topVisibleIndex++;
						if (bottomVisibleIndex != elements.length - 1) {
							bottomVisibleIndex++;
						}
						layoutTabs();
						topNavigationElement.redraw();
						bottomNavigationElement.redraw();
					}
				}
			});
		}

		/**
		 * @param e
		 */
		private void paint(PaintEvent e) {
			e.gc.setBackground(defaultBackground);
			e.gc.setForeground(defaultForeground);
			Rectangle bounds = getBounds();

			if (elements.length != 0) {
				e.gc.fillRectangle(0, 0, bounds.width, bounds.height);
				e.gc.setForeground(border);
				e.gc.drawLine(bounds.width - 1, 0, bounds.width - 1,
					bounds.height - 1);
				if (getSelectionIndex() != NONE && elements.length != 0
					&& getSelectionIndex() == elements.length - 1) {
					e.gc.drawLine(4, 0, bounds.width - 1, 0);
				} else {
					e.gc.drawLine(2, 0, bounds.width - 1, 0);
				}
			} else {
				e.gc.setBackground(activeBackground);
				e.gc.fillRectangle(0, 0, bounds.width, bounds.height);
			}

			if (isDownScrollRequired()) {
				e.gc.setForeground(darkShadow);
				int middle = bounds.width / 2;
				int bottom = bounds.height - 2;
				e.gc.drawLine(middle + 1, bottom, middle + 5, bottom - 4);
				e.gc.drawLine(middle, bottom, middle - 4, bottom - 4);
				e.gc.drawLine(middle - 4, bottom - 5, middle + 5, bottom - 5);

				e.gc.setForeground(activeBackground);
				e.gc.drawLine(middle, bottom - 1, middle + 1, bottom - 1);
				e.gc.drawLine(middle - 1, bottom - 2, middle + 2, bottom - 2);
				e.gc.drawLine(middle - 2, bottom - 3, middle + 3, bottom - 3);
				e.gc.drawLine(middle - 3, bottom - 4, middle + 4, bottom - 4);
			}
		}
	}

	public TabbedPropertyList(Composite parent,
			TabbedPropertySheetWidgetFactory factory) {
		super(parent, SWT.NO_FOCUS);
		this.factory = factory;
		removeAll();
		setLayout(new FormLayout());
		initColours();
		initAccessible();
		topNavigationElement = new TopNavigationElement(this);
		bottomNavigationElement = new BottomNavigationElement(this);

		this.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
				focus = true;
				int i = getSelectionIndex();
				if (i >= 0)
					elements[i].redraw();
			}

			public void focusLost(FocusEvent e) {
				focus = false;
				int i = getSelectionIndex();
				if (i >= 0)
					elements[i].redraw();
			}
		});
		this.addControlListener(new ControlAdapter() {

			public void controlResized(ControlEvent e) {
				computeTopAndBottomTab();
			}
		});
		this.addTraverseListener(new TraverseListener() {

			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_ARROW_PREVIOUS
					|| e.detail == SWT.TRAVERSE_ARROW_NEXT) {
					int nMax = elements.length - 1;
					int nCurrent = getSelectionIndex();
					if (e.detail == SWT.TRAVERSE_ARROW_PREVIOUS) {
						nCurrent -= 1;
						nCurrent = Math.max(0, nCurrent);
					} else if (e.detail == SWT.TRAVERSE_ARROW_NEXT) {
						nCurrent += 1;
						nCurrent = Math.min(nCurrent, nMax);
					}
					select(nCurrent, true);
					redraw();
				} else {
					e.doit = true;
				}
			}
		});
	}

	/**
	 * Calculate the number of tabs that will fit in the tab list composite.
	 */
	protected void computeTabsThatFitInComposite() {
		tabsThatFitInComposite = Math
			.round((getSize().y - 22) / getTabHeight());
		if (tabsThatFitInComposite <= 0) {
			tabsThatFitInComposite = 1;
		}
	}

	/**
	 * Returns the element with the given index from this list viewer. Returns
	 * <code>null</code> if the index is out of range.
	 * 
	 * @param index
	 *            the zero-based index
	 * @return the element at the given index, or <code>null</code> if the
	 *         index is out of range
	 */
	public Object getElementAt(int index) {
		if (index >= 0 && index < elements.length) {
			return elements[index];
		}
		return null;
	}

	/**
	 * Returns the zero-relative index of the item which is currently selected
	 * in the receiver, or -1 if no item is selected.
	 * 
	 * @return the index of the selected item
	 */
	public int getSelectionIndex() {
		return selectedElementIndex;
	}

	/**
	 * Removes all elements from this list.
	 */
	public void removeAll() {
		if (elements != null) {
			for (int i = 0; i < elements.length; i++) {
				elements[i].dispose();
			}
		}
		elements = ELEMENTS_EMPTY;
		selectedElementIndex = NONE;
		widestLabelIndex = NONE;
		topVisibleIndex = NONE;
		bottomVisibleIndex = NONE;
	}

	/**
	 * Sets the new list elements.
	 */
	public void setElements(Object[] children) {
		if (elements != ELEMENTS_EMPTY) {
			removeAll();
		}
		elements = new ListElement[children.length];
		if (children.length == 0) {
			widestLabelIndex = NONE;
		} else {
			widestLabelIndex = 0;
			for (int i = 0; i < children.length; i++) {
				elements[i] = new ListElement(this,
					(ITabItem) children[i], i);
				elements[i].setVisible(false);
				elements[i].setLayoutData(null);

				if (i != widestLabelIndex) {
					String label = ((ITabItem) children[i]).getText();
					if (getTextDimension(label).x > getTextDimension(((ITabItem) children[widestLabelIndex])
						.getText()).x) {
						widestLabelIndex = i;
					}
				}
			}
		}

		computeTopAndBottomTab();
	}

	/**
	 * Selects one for the elements in the list.
	 */
	public void select(int index, boolean reveal) {
		if (getSelectionIndex() == index) {
			/*
			 * this index is already selected.
			 */
			return;
		}
		if (index >= 0 && index < elements.length) {
			int lastSelected = getSelectionIndex();
			elements[index].setSelected(true);
			selectedElementIndex = index;
			if (lastSelected != NONE) {
				elements[lastSelected].setSelected(false);
				if (getSelectionIndex() != elements.length - 1) {
					/*
					 * redraw the next tab to fix the border by calling
					 * setSelected()
					 */
					elements[getSelectionIndex() + 1].setSelected(false);
				}
			}
			topNavigationElement.redraw();
			bottomNavigationElement.redraw();

			if (selectedElementIndex < topVisibleIndex
				|| selectedElementIndex > bottomVisibleIndex) {
				computeTopAndBottomTab();
			}
		}
		notifyListeners(SWT.Selection, new Event());
	}

	/**
	 * Selects one for the elements in the list.
	 */
	public void deselectAll() {
		if (getSelectionIndex() != NONE) {
			elements[getSelectionIndex()].setSelected(false);
			selectedElementIndex = NONE;
		}
	}

	private int getIndex(ListElement element) {
		return element.index;
	}

	/**
	 * Computes the size based on the widest string in the list.
	 */
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Point result = super.computeSize(hHint, wHint, changed);
		if (widestLabelIndex == -1) {
			String properties_not_available = TabbedPropertyMessages.TabbedPropertyList_properties_not_available;
			result.x = getTextDimension(properties_not_available).x + INDENT;
		} else {
			int width = getTextDimension(elements[widestLabelIndex].getText()).x;
			/*
			 * To anticipate for the icon placement we should always keep the
			 * space available after the label. So when the active tab includes
			 * an icon the width of the tab doesn't change.
			 */
			result.x = width + 32;
		}
		return result;
	}

	private Point getTextDimension(String text) {
		Shell shell = new Shell();
		GC gc = new GC(shell);
		gc.setFont(getFont());
		Point point = gc.textExtent(text);
		point.x++;
		gc.dispose();
		shell.dispose();
		return point;
	}

	/**
	 * Initialize the colours used.
	 */
	private void initColours() {
		defaultBackground = Display.getCurrent().getSystemColor(
			SWT.COLOR_WIDGET_BACKGROUND);

		RGB rgb = defaultBackground.getRGB();
		rgb.blue = Math.min(255, Math.round(rgb.blue * 1.05F));
		rgb.red = Math.min(255, Math.round(rgb.red * 1.05F));
		rgb.green = Math.min(255, Math.round(rgb.green * 1.05F));
		hoverBackground = factory.getColors().createColor(
			"TabbedPropertyList.hoverBackground", rgb); //$NON-NLS-1$

		defaultForeground = Display.getCurrent().getSystemColor(
			SWT.COLOR_WIDGET_FOREGROUND);
		activeBackground = Display.getCurrent().getSystemColor(
			SWT.COLOR_LIST_BACKGROUND);
		border = Display.getCurrent().getSystemColor(
			SWT.COLOR_WIDGET_NORMAL_SHADOW);
		darkShadow = Display.getCurrent().getSystemColor(
			SWT.COLOR_WIDGET_DARK_SHADOW);
		textColor = Display.getCurrent().getSystemColor(
			SWT.COLOR_WIDGET_FOREGROUND);
	}

	/**
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	public void dispose() {
		hoverBackground.dispose();
		defaultBackground.dispose();
		defaultForeground.dispose();
		activeBackground.dispose();
		border.dispose();
		darkShadow.dispose();
		textColor.dispose();
		super.dispose();
	}

	/**
	 * Get the height of a tab. The height of the tab is the height of the text
	 * plus buffer.
	 * 
	 * @return the height of a tab.
	 */
	private int getTabHeight() {
		int tabHeight = getTextDimension("").y + INDENT; //$NON-NLS-1$ 
		if (tabsThatFitInComposite == 1) {
			/*
			 * if only one tab will fix, reduce the size of the tab height so
			 * that the navigation elements fit.
			 */
			int ret = getBounds().height - 20;
			return (ret > tabHeight) ? tabHeight
				: (ret < 5) ? 5
					: ret;
		}
		return tabHeight;
	}

	private boolean isDownScrollRequired() {
		return elements.length > tabsThatFitInComposite
			&& bottomVisibleIndex != elements.length - 1;
	}

	private boolean isUpScrollRequired() {
		return elements.length > tabsThatFitInComposite && topVisibleIndex != 0;
	}

	private void computeTopAndBottomTab() {
		computeTabsThatFitInComposite();
		if (elements.length == 0) {
			/*
			 * no tabs to display.
			 */
			topVisibleIndex = 0;
			bottomVisibleIndex = 0;
		} else if (tabsThatFitInComposite >= elements.length) {
			/*
			 * all the tabs fit.
			 */
			topVisibleIndex = 0;
			bottomVisibleIndex = elements.length - 1;
		} else if (getSelectionIndex() == NONE) {
			/*
			 * there is no selected tab yet, assume that tab one would
			 * be selected for now.
			 */
			topVisibleIndex = 0;
			bottomVisibleIndex = tabsThatFitInComposite - 1;
		} else if (getSelectionIndex() + tabsThatFitInComposite > elements.length) {
			/*
			 * the selected tab is near the bottom.
			 */
			bottomVisibleIndex = elements.length - 1;
			topVisibleIndex = bottomVisibleIndex - tabsThatFitInComposite + 1;
		} else {
			/*
			 * the selected tab is near the top.
			 */
			topVisibleIndex = selectedElementIndex;
			bottomVisibleIndex = selectedElementIndex + tabsThatFitInComposite
				- 1;
		}
		layoutTabs();
	}

	/**
	 * Layout the tabs.
	 * 
	 * @param up
	 *            if <code>true</code>, then we are laying out as a result of
	 *            an scroll up request.
	 */
	private void layoutTabs() {
		//System.out.println("TabFit " + tabsThatFitInComposite + " length "
		//	+ elements.length + " top " + topVisibleIndex + " bottom "
		//	+ bottomVisibleIndex);
		if (tabsThatFitInComposite == NONE || elements.length == 0) {
			FormData formData = new FormData();
			formData.left = new FormAttachment(0, 0);
			formData.right = new FormAttachment(100, 0);
			formData.top = new FormAttachment(0, 0);
			formData.height = getTabHeight();
			topNavigationElement.setLayoutData(formData);

			formData = new FormData();
			formData.left = new FormAttachment(0, 0);
			formData.right = new FormAttachment(100, 0);
			formData.top = new FormAttachment(topNavigationElement, 0);
			formData.bottom = new FormAttachment(100, 0);
			bottomNavigationElement.setLayoutData(formData);
		} else {

			FormData formData = new FormData();
			formData.left = new FormAttachment(0, 0);
			formData.right = new FormAttachment(100, 0);
			formData.top = new FormAttachment(0, 0);
			formData.height = 10;
			topNavigationElement.setLayoutData(formData);

			/*
			 * use nextElement to attach the layout to the previous canvas
			 * widget in the list.
			 */
			Canvas nextElement = topNavigationElement;

			for (int i = 0; i < elements.length; i++) {
				//System.out.print(i + " [" + elements[i].getText() + "]");
				if (i < topVisibleIndex || i > bottomVisibleIndex) {
					/*
					 * this tab is not visible
					 */
					elements[i].setLayoutData(null);
					elements[i].setVisible(false);
				} else {
					/*
					 * this tab is visible.
					 */
					//System.out.print(" visible");
					formData = new FormData();
					formData.height = getTabHeight();
					formData.left = new FormAttachment(0, 0);
					formData.right = new FormAttachment(100, 0);
					formData.top = new FormAttachment(nextElement, 0);
					nextElement = elements[i];
					elements[i].setLayoutData(formData);
					elements[i].setVisible(true);
				}

				//if (i == selectedElementIndex) {
				//	System.out.print(" selected");
				//}
				//System.out.println("");
			}
			formData = new FormData();
			formData.left = new FormAttachment(0, 0);
			formData.right = new FormAttachment(100, 0);
			formData.top = new FormAttachment(nextElement, 0);
			formData.bottom = new FormAttachment(100, 0);
			formData.height = 10;
			bottomNavigationElement.setLayoutData(formData);
		}
		//System.out.println("");

		// layout so that we have enough space for the new labels
		Composite grandparent = getParent().getParent();
		grandparent.layout(true);
		layout(true);
	}

	/**
	 * Initialize the accessibility adapter.
	 */
	private void initAccessible() {
		final Accessible accessible = getAccessible();
		accessible.addAccessibleListener(new AccessibleAdapter() {

			public void getName(AccessibleEvent e) {
				if (getSelectionIndex() != NONE) {
					e.result = elements[getSelectionIndex()].getText();
				}
			}

			public void getHelp(AccessibleEvent e) {
				if (getSelectionIndex() != NONE) {
					e.result = elements[getSelectionIndex()].getText();
				}
			}
		});

		accessible.addAccessibleControlListener(new AccessibleControlAdapter() {

			public void getChildAtPoint(AccessibleControlEvent e) {
				Point pt = toControl(new Point(e.x, e.y));
				e.childID = (getBounds().contains(pt)) ? ACC.CHILDID_SELF
					: ACC.CHILDID_NONE;
			}

			public void getLocation(AccessibleControlEvent e) {
				if (getSelectionIndex() != NONE) {
					Rectangle location = elements[getSelectionIndex()]
						.getBounds();
					Point pt = toDisplay(new Point(location.x, location.y));
					e.x = pt.x;
					e.y = pt.y;
					e.width = location.width;
					e.height = location.height;
				}
			}

			public void getChildCount(AccessibleControlEvent e) {
				e.detail = 0;
			}

			public void getRole(AccessibleControlEvent e) {
				e.detail = ACC.ROLE_TABITEM;
			}

			public void getState(AccessibleControlEvent e) {
				e.detail = ACC.STATE_NORMAL | ACC.STATE_SELECTABLE
					| ACC.STATE_SELECTED | ACC.STATE_FOCUSED
					| ACC.STATE_FOCUSABLE;
			}
		});

		addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event event) {
				if (isFocusControl()) {
					accessible.setFocus(ACC.CHILDID_SELF);
				}
			}
		});

		addListener(SWT.FocusIn, new Listener() {

			public void handleEvent(Event event) {
				accessible.setFocus(ACC.CHILDID_SELF);
			}
		});
	}
}
