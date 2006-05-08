/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.misc.StringMatcher;
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultTabItem;

/**
 * @since 3.0
 */
public abstract class AbstractTableInformationControl {

    /**
     * The NamePatternFilter selects the elements which match the given string
     * patterns.
     */
    protected class NamePatternFilter extends ViewerFilter {

        public NamePatternFilter() {
            //no-op
        }

        /*
         * (non-Javadoc) Method declared on ViewerFilter.
         */
        public boolean select(Viewer viewer, Object parentElement,
                Object element) {
            StringMatcher matcher = getMatcher();
            if (matcher == null || !(viewer instanceof TableViewer)) {
				return true;
			}
            TableViewer tableViewer = (TableViewer) viewer;

            String matchName = ((ILabelProvider) tableViewer.getLabelProvider())
                    .getText(element);
            
            if(matchName == null) {
				return false;
			}
            // A dirty editor's label will start with dirty prefix, this prefix 
            // should not be taken in consideration when matching with a pattern
            String prefix = DefaultTabItem.DIRTY_PREFIX;
            if (matchName.startsWith(prefix)) {
				matchName = matchName.substring(prefix.length());
			}
            return matchName != null && matcher.match(matchName);
        }
    }

    private static class BorderFillLayout extends Layout {

        /** The border widths. */
        final int fBorderSize;

        /**
         * Creates a fill layout with a border.
         */
        public BorderFillLayout(int borderSize) {
            if (borderSize < 0) {
				throw new IllegalArgumentException();
			}
            fBorderSize = borderSize;
        }

        /**
         * Returns the border size.
         */
        public int getBorderSize() {
            return fBorderSize;
        }

        /*
         * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite,
         *      int, int, boolean)
         */
        protected Point computeSize(Composite composite, int wHint, int hHint,
                boolean flushCache) {

            Control[] children = composite.getChildren();
            Point minSize = new Point(0, 0);

            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    Point size = children[i].computeSize(wHint, hHint,
                            flushCache);
                    minSize.x = Math.max(minSize.x, size.x);
                    minSize.y = Math.max(minSize.y, size.y);
                }
            }

            minSize.x += fBorderSize * 2 + RIGHT_MARGIN;
            minSize.y += fBorderSize * 2;

            return minSize;
        }

        /*
         * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite,
         *      boolean)
         */
        protected void layout(Composite composite, boolean flushCache) {

            Control[] children = composite.getChildren();
            Point minSize = new Point(composite.getClientArea().width,
                    composite.getClientArea().height);

            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    Control child = children[i];
                    child.setSize(minSize.x - fBorderSize * 2, minSize.y
                            - fBorderSize * 2);
                    child.setLocation(fBorderSize, fBorderSize);
                }
            }
        }
    }

    /** Border thickness in pixels. */
    private static final int BORDER = 1;

    /** Right margin in pixels. */
    private static final int RIGHT_MARGIN = 3;

    /** The control's shell */
    private Shell fShell;

    /** The composite */
    protected Composite fComposite;

    /** The control's text widget */
    private Text fFilterText;

    /** The control's table widget */
    private TableViewer fTableViewer;

    /** The control width constraint */
    //private int fMaxWidth= -1;
    /** The control height constraint */
    //private int fMaxHeight= -1;
    /** The current string matcher */
    private StringMatcher fStringMatcher;

    /**
     * Creates an information control with the given shell as parent. The given
     * styles are applied to the shell and the table widget.
     * 
     * @param parent
     *            the parent shell
     * @param shellStyle
     *            the additional styles for the shell
     * @param controlStyle
     *            the additional styles for the control
     */
    public AbstractTableInformationControl(Shell parent, int shellStyle,
            int controlStyle) {
        fShell = new Shell(parent, shellStyle);
        Display display = fShell.getDisplay();
        fShell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));

        // Composite for filter text and viewer

        fComposite = new Composite(fShell, SWT.RESIZE);
        GridLayout layout = new GridLayout(1, false);
        fComposite.setLayout(layout);
        //		fComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createFilterText(fComposite);

        fTableViewer = createTableViewer(fComposite, controlStyle);

        final Table table = fTableViewer.getTable();
        table.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.character == SWT.ESC) {
					dispose();
				} else if (e.character == SWT.DEL) {
                    removeSelectedItems();
                    e.character = SWT.NONE;
                    e.doit = false;
                }
            }

            public void keyReleased(KeyEvent e) {
                // do nothing
            }
        });

        table.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                // do nothing;
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                gotoSelectedElement();
            }
        });

        /*
         * Bug in GTK, see SWT bug: 62405 Editor drop down performance slow on
         * Linux-GTK on mouse move.
         * Rather then removing the support altogether this feature has been 
         * worked around for GTK only as we expect that newer versions of GTK 
         * will no longer exhibit this quality and we will be able to have the 
         * desired support running on all platforms. See
         * comment https://bugs.eclipse.org/bugs/show_bug.cgi?id=62405#c22 
         * TODO: remove this code once bug 62405 is fixed for the mainstream GTK
         * version
         */
        final int ignoreEventCount = Platform.getWS().equals(Platform.WS_GTK) ? 4 : 1;
    
        table.addMouseMoveListener(new MouseMoveListener() {
            TableItem fLastItem = null;
            int lastY = 0;
            int itemHeightdiv4 = table.getItemHeight() / 4;
            int tableHeight = table.getBounds().height;
            Point tableLoc = table.toDisplay(0,0);
            int divCount = 0;
            public void mouseMove(MouseEvent e) {
                if (divCount == ignoreEventCount) {
					divCount = 0;
				}
                if (table.equals(e.getSource()) & ++divCount == ignoreEventCount) {
                    Object o = table.getItem(new Point(e.x, e.y));
                    if (o instanceof TableItem && lastY != e.y) {
                        lastY = e.y;
                        if (!o.equals(fLastItem)) {
                            fLastItem = (TableItem) o;
                            table.setSelection(new TableItem[] { fLastItem });
                        } else if (e.y < itemHeightdiv4) {
                            // Scroll up
                            Item item = fTableViewer.scrollUp(e.x + tableLoc.x, e.y + tableLoc.y);
                            if (item instanceof TableItem) {
                                fLastItem = (TableItem) item;
                                table.setSelection(new TableItem[] { fLastItem });
                            }
                        } else if (e.y > tableHeight - itemHeightdiv4) {
                            // Scroll down
                            Item item = fTableViewer.scrollDown(e.x + tableLoc.x, e.y + tableLoc.y);
                            if (item instanceof TableItem) {
                                fLastItem = (TableItem) item;
                                table.setSelection(new TableItem[] { fLastItem });
                            }
                        }
                    }
                }
            }
        });
    
        table.addMouseListener(new MouseAdapter() {
            public void mouseUp(MouseEvent e) {
                if (table.getSelectionCount() < 1) {
					return;
				}

                if (e.button == 1) {
					if (table.equals(e.getSource())) {
                        Object o = table.getItem(new Point(e.x, e.y));
                        TableItem selection = table.getSelection()[0];
                        if (selection.equals(o)) {
							gotoSelectedElement();
						}
                    }
				}
                if (e.button == 3) {
                    TableItem tItem = fTableViewer.getTable().getItem(
                            new Point(e.x, e.y));
                    if (tItem != null) {
                        Menu menu = new Menu(fTableViewer.getTable());
                        MenuItem mItem = new MenuItem(menu, SWT.NONE);
                        mItem.setText(WorkbenchMessages.PartPane_close);
                        mItem.addSelectionListener(new SelectionAdapter() {
                            public void widgetSelected(
                                    SelectionEvent selectionEvent) {
                                removeSelectedItems();
                            }
                        });
                        menu.setVisible(true);
                    }
                }
            }
        });

        int border = ((shellStyle & SWT.NO_TRIM) == 0) ? 0 : BORDER;
        fShell.setLayout(new BorderFillLayout(border));

        setInfoSystemColor();
        installFilter();
    }

    /**
     * Removes the selected items from the list and closes their corresponding tabs
     * Selects the next item in the list or disposes it if its presentation is disposed
     */
    protected void removeSelectedItems() {
        int selInd = fTableViewer.getTable().getSelectionIndex();
        if (deleteSelectedElements()) {
            return;
        }
        fTableViewer.refresh();
        if (selInd >= fTableViewer.getTable().getItemCount()) {
			selInd = fTableViewer.getTable().getItemCount() - 1;
		}
        if (selInd >= 0) {
			fTableViewer.getTable().setSelection(selInd);
		}
    }

    protected abstract TableViewer createTableViewer(Composite parent, int style);

    public TableViewer getTableViewer() {
        return fTableViewer;
    }

    protected Text createFilterText(Composite parent) {
        fFilterText = new Text(parent, SWT.NONE);

        GridData data = new GridData();
        GC gc = new GC(parent);
        gc.setFont(parent.getFont());
        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();

        data.heightHint = org.eclipse.jface.dialogs.Dialog
                .convertHeightInCharsToPixels(fontMetrics, 1);
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.BEGINNING;
        fFilterText.setLayoutData(data);

        fFilterText.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == 0x0D) {
					gotoSelectedElement();
				}
                if (e.keyCode == SWT.ARROW_DOWN) {
                    fTableViewer.getTable().setFocus();
                    fTableViewer.getTable().setSelection(0);
                }
                if (e.keyCode == SWT.ARROW_UP) {
                    fTableViewer.getTable().setFocus();
                    fTableViewer.getTable().setSelection(
                            fTableViewer.getTable().getItemCount() - 1);
                }
                if (e.character == 0x1B) {
					dispose();
				}
            }

            public void keyReleased(KeyEvent e) {
                // do nothing
            }
        });

        // Horizontal separator line
        Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL
                | SWT.LINE_DOT);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return fFilterText;
    }

    private void setInfoSystemColor() {
        Display display = fShell.getDisplay();
        setForegroundColor(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        setBackgroundColor(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    }

    private void installFilter() {
        fFilterText.setText(""); //$NON-NLS-1$

        fFilterText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                String text = ((Text) e.widget).getText();
                int length = text.length();
                if (length > 0 && text.charAt(length - 1) != '*') {
                    text = text + '*';
                }
                setMatcherString(text);
            }
        });
    }

    /**
     * The string matcher has been modified. The default implementation
     * refreshes the view and selects the first macthed element
     */
    protected void stringMatcherUpdated() {
        // refresh viewer to refilter
        fTableViewer.getControl().setRedraw(false);
        fTableViewer.refresh();
        selectFirstMatch();
        fTableViewer.getControl().setRedraw(true);
    }

    /**
     * Sets the patterns to filter out for the receiver.
     * <p>
     * The following characters have special meaning: ? => any character * =>
     * any string
     * </p>
     */
    protected void setMatcherString(String pattern) {
        if (pattern.length() == 0) {
            fStringMatcher = null;
        } else {
            boolean ignoreCase = pattern.toLowerCase().equals(pattern);
            fStringMatcher = new StringMatcher(pattern, ignoreCase, false);
        }
        stringMatcherUpdated();
    }

    protected StringMatcher getMatcher() {
        return fStringMatcher;
    }

    /**
     * Implementers can modify
     */
    protected Object getSelectedElement() {
        return ((IStructuredSelection) fTableViewer.getSelection())
                .getFirstElement();
    }

    /**
     * Implementers can modify
     */
    protected IStructuredSelection getSelectedElements() {
        return (IStructuredSelection) fTableViewer.getSelection();
    }

    protected abstract void gotoSelectedElement();

    /**
     * Delete all selected elements.
     *  
     * @return <code>true</code> if there are no elements left after deletion.
     */
    protected abstract boolean deleteSelectedElements();

    /**
     * Selects the first element in the table which matches the current filter
     * pattern.
     */
    protected void selectFirstMatch() {
        Table table = fTableViewer.getTable();
        Object element = findElement(table.getItems());
        if (element != null) {
			fTableViewer.setSelection(new StructuredSelection(element), true);
		} else {
			fTableViewer.setSelection(StructuredSelection.EMPTY);
		}
    }

    private Object findElement(TableItem[] items) {
        ILabelProvider labelProvider = (ILabelProvider) fTableViewer
                .getLabelProvider();
        for (int i = 0; i < items.length; i++) {
            Object element = items[i].getData();
            if (fStringMatcher == null) {
				return element;
			}

            if (element != null) {
                String label = labelProvider.getText(element);
                if(label == null) {
					return null;
				}
                // remove the dirty prefix from the editor's label
                String prefix = DefaultTabItem.DIRTY_PREFIX;
                if (label.startsWith(prefix)) {
					label = label.substring(prefix.length());
				}
                if (fStringMatcher.match(label)) {
					return element;
				}
            }
        }
        return null;
    }

    public abstract void setInput(Object information);

    protected void inputChanged(Object newInput, Object newSelection) {
        fFilterText.setText(""); //$NON-NLS-1$
        fTableViewer.setInput(newInput);

        // Resize the table's height accordingly to the new input
        Table viewerTable = fTableViewer.getTable();
        Point tableSize = viewerTable.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        int tableMaxHeight = fComposite.getDisplay().getBounds().height / 2;
        // removes padding if necessary
        int tableHeight = (tableSize.y <= tableMaxHeight) ? tableSize.y
                - viewerTable.getItemHeight() - viewerTable.getItemHeight() / 2
                : tableMaxHeight;
        ((GridData) viewerTable.getLayoutData()).heightHint = tableHeight;
        Point fCompSize = fComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        fComposite.setSize(fCompSize);
        fComposite.getShell().setSize(fCompSize);
    }

    public void setVisible(boolean visible) {
        fShell.setVisible(visible);
    }

    public void dispose() {
        if (fShell != null) {
            if (!fShell.isDisposed()) {
				fShell.dispose();
			}
            fShell = null;
            fTableViewer = null;
            fComposite = null;
            fFilterText = null;
        }
    }

    public boolean hasContents() {
        return fTableViewer != null && fTableViewer.getInput() != null;
    }

    public void setSizeConstraints(int maxWidth, int maxHeight) {
        //fMaxWidth= maxWidth;
        //fMaxHeight= maxHeight;
    }

    public Point computeSizeHint() {
        return fShell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    }

    public void setLocation(Point location) {
        Rectangle trim = fShell.computeTrim(0, 0, 0, 0);
        Point textLocation = fComposite.getLocation();
        location.x += trim.x - textLocation.x;
        location.y += trim.y - textLocation.y;
        fShell.setLocation(location);
    }

    public void setSize(int width, int height) {
        fShell.setSize(width, height);
    }

    public void addDisposeListener(DisposeListener listener) {
        fShell.addDisposeListener(listener);
    }

    public void removeDisposeListener(DisposeListener listener) {
        fShell.removeDisposeListener(listener);
    }

    public void setForegroundColor(Color foreground) {
        fTableViewer.getTable().setForeground(foreground);
        fFilterText.setForeground(foreground);
        fComposite.setForeground(foreground);
    }

    public void setBackgroundColor(Color background) {
        fTableViewer.getTable().setBackground(background);
        fFilterText.setBackground(background);
        fComposite.setBackground(background);
    }

    public boolean isFocusControl() {
        return fTableViewer.getControl().isFocusControl()
                || fFilterText.isFocusControl();
    }

    public void setFocus() {
        fShell.forceFocus();
        fFilterText.setFocus();
    }

    public void addFocusListener(FocusListener listener) {
        fShell.addFocusListener(listener);
    }

    public void removeFocusListener(FocusListener listener) {
        fShell.removeFocusListener(listener);
    }
}
