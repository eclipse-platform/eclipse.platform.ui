/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.Gradient;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.internal.dnd.AbstractDragSource;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.dnd.IDragOverListener;
import org.eclipse.ui.internal.dnd.IDropTarget;
import org.eclipse.ui.internal.skins.newlook.BasicStackPresentation;

public class TabbedEditorWorkbook extends EditorWorkbook {

	CTabFolder2Adapter expandListener = new CTabFolder2Adapter() {
		
		public void minimize(CTabFolderEvent event) {
			/*
			Rectangle bounds = tabFolder.getBounds();
			int minimumHeight = getMinimumHeight();
			
			if (bounds.height != minimumHeight) {
				tabFolder.setSize(getMinimumWidth(), minimumHeight);	
				forceLayout();
			}
			*/
		}
		
        public void restore(CTabFolderEvent event) {
            EditorPane visibleEditor = getVisibleEditor();

            if (visibleEditor != null) {
                WorkbenchPage page = visibleEditor.getPage();

                if (page != null && page.isZoomed()) doZoom();

                //forceLayout();
            }
        }

        public void maximize(CTabFolderEvent event) {
            EditorPane visibleEditor = getVisibleEditor();

            if (visibleEditor != null) {
                WorkbenchPage page = visibleEditor.getPage();

                if (page != null && !page.isZoomed()) doZoom();
            }
        }
	};
	
	/*
	private void forceLayout() {
		PartSashContainer cont = (PartSashContainer) getContainer();
		
		if (cont != null)
			cont.getLayoutTree().setBounds(parent.getClientArea());
	}
	*/	
	
	private IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault().getPreferenceStore();
	private int tabLocation = -1; // Initialized in constructor.
	private CTabFolder tabFolder = null;
	private Map mapTabToEditor = new HashMap();
	private ToolBar pullDownBar;
	private ToolItem pullDownButton;
	private EditorList editorList;
	private ViewForm listComposite;
	private boolean assignFocusOnSelection = true;
	private boolean ignoreTabFocusHide = false;
	private boolean handleTabSelection = true;
	private boolean mouseDownListenerAdded = false;
	private boolean usePulldown = false;

	public TabbedEditorWorkbook(EditorArea editorArea) {
		super(editorArea);
	}

	protected Object createItem(EditorPane editorPane) {
		return createTab(editorPane);
	}

	/**
	 * Create a new tab for an item.
	 */
	private CTabItem createTab(EditorPane editorPane) {
		return createTab(editorPane, tabFolder.getItemCount());
	}

	/**
	 * Create a new tab for an item at a particular index.
	 */
	private CTabItem createTab(EditorPane editorPane, int index) {
		CTabItem tab = new CTabItem(tabFolder, SWT.NONE, index);
		mapTabToEditor.put(tab, editorPane);
		updateEditorTab((IEditorReference) editorPane.getPartReference());
		if (tabFolder.getItemCount() == 1) {
			if (tabFolder.getTopRight() != null) {
				pullDownBar.setVisible(true);
			}
		}
		return tab;
	}

	protected void setControlSize() {
		EditorPane visibleEditor = getVisibleEditor();
		if (visibleEditor == null || getControl() == null)
			return;
		Rectangle bounds = BasicStackPresentation.calculatePageBounds(tabFolder);
		visibleEditor.setBounds(bounds);
		visibleEditor.moveAbove(tabFolder);
	}

	private final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
			boolean multiChanged = IPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS.equals(propertyChangeEvent.getProperty());
			boolean styleChanged = IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS.equals(propertyChangeEvent.getProperty());
			if ((multiChanged || styleChanged) && tabFolder != null) {
				if (multiChanged) {
					boolean multi = preferenceStore.getBoolean(IPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS);					
					tabFolder.setSingleTab(!multi);
				} else {
					boolean simple = preferenceStore.getBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS);
					tabFolder.setSimpleTab(simple);
				}
				Iterator iterator = getEditorList().iterator();
				while (iterator.hasNext())
				    updateItem((EditorPane) iterator.next());
			}
		}
	};
	
	protected void createPresentation(final Composite parent) {
		usePulldown = preferenceStore.getBoolean(IPreferenceConstants.EDITORLIST_PULLDOWN_ACTIVE);

		preferenceStore.addPropertyChangeListener(propertyChangeListener);
		
		boolean multi = preferenceStore.getBoolean(IPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS);
		boolean simple = preferenceStore.getBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS);
		
	    int style = SWT.CLOSE | SWT.BORDER | (multi ? SWT.MULTI : SWT.SINGLE);
		
		tabFolder = new CTabFolder(parent, style);
		tabFolder.setSimpleTab(simple);
	
		//tabFolder.setBorderVisible(true);
		ColorSchemeService.setTabColors(tabFolder);

		// prevent close button and scroll buttons from taking focus
		tabFolder.setTabList(new Control[0]);
		
		// redirect drop request to the workbook
		tabFolder.setData(this);

		// listener to close the editor
		tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent e) {
				e.doit = false; // otherwise tab is auto disposed on return
				EditorPane pane = (EditorPane) mapTabToEditor.get(e.item);
				pane.doHide();
			}
		});

		tabFolder.addCTabFolder2Listener(expandListener);				
		tabFolder.setMaximizeVisible(true);
		//tabFolder.setMinimizeVisible(true);
		
		int shellStyle = SWT.RESIZE | SWT.ON_TOP | SWT.NO_TRIM;
		int tableStyle = SWT.V_SCROLL | SWT.H_SCROLL;
		final EditorsInformationControl info =
			new EditorsInformationControl(tabFolder.getShell(), shellStyle, tableStyle);

		tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {

			public void showList(CTabFolderEvent event) {
				Point p = tabFolder.toDisplay(event.x, event.y);
				p.y += +event.height;
				//showList(event.widget.getDisplay(), p.x, p.y);
				showList(tabFolder.getShell(), p.x, p.y);
				event.doit = false;
			}

			void showList(Shell parentShell, int x, int y) {
				info.setInput(TabbedEditorWorkbook.this);
				Point size = info.computeSizeHint();
				int minX = 50; //labelComposite.getSize().x;
				int minY = 300;
				if (size.x < minX)
					size.x = minX;
				if (size.y < minY)
					size.y = minY;
				info.setSize(size.x, size.y);				
				Rectangle bounds = Display.getCurrent().getBounds();			
				
				if (x + size.x > bounds.width)
				    x = bounds.width - size.x;
				
				if (y + size.y > bounds.height)
				    y = bounds.height - size.y;
				
				info.setLocation(new Point(x, y));
				info.setVisible(true);
				info.setFocus();
				info
					.getTableViewer()
					.getTable()
					.getShell()
					.addListener(SWT.Deactivate, new Listener() {
					public void handleEvent(Event event) {
						info.setVisible(false);
					}
				});
				//			EditorsInformationControl e = new EditorsInformationControl(tabFolder.getShell(), SWT.ON_TOP | SWT.NO_TRIM, SWT.NONE);
				//			e.setMatcherString("*");
				//			e.setVisible(true);
			}

			void showList(Display display, int x, int y) {
				final Shell shell = new Shell(tabFolder.getShell(), SWT.ON_TOP | SWT.NO_TRIM);
				shell.addFocusListener(new FocusListener() {

					public void focusGained(FocusEvent e) {
						// TODO Auto-generated method stub
					}

					public void focusLost(FocusEvent e) {
						if (!shell.getDisplay().getActiveShell().equals(shell)) {
							shell.dispose();
						}
					}

				});
				FillLayout fl = new FillLayout(SWT.VERTICAL);
				fl.marginHeight = 3;
				fl.marginWidth = 3;
				shell.setLayout(fl);

				final Text text = new Text(shell, SWT.SINGLE);
				final Table table = new Table(shell, SWT.NONE); //SWT.BORDER);
				CTabItem[] items = tabFolder.getItems();
				final String[] stringItems = new String[items.length];
				for (int i = 0; i < items.length; i++) {
					CTabItem tab = items[i];
					stringItems[i] = tab.getText();
					TableItem item = new TableItem(table, SWT.NONE);
					item.setText(tab.getText());
					item.setImage(tab.getImage());
				}
				final int idx = tabFolder.getSelectionIndex();
				if (idx != -1) {
					table.setSelection(idx);
				}
				Listener listener = new Listener() {
					public void handleEvent(Event e) {
						switch (e.type) {
							case SWT.FocusOut :
								if (e.widget.equals(table))
									shell.dispose();
								break;
							case SWT.Modify :
								String s = text.getText();
								// if there is no text then do nothing
								if (s.length() == 0)
									break;
								for (int i = 0; i < stringItems.length; i++) {
									String item = stringItems[i];
									if (item
										.toLowerCase(Locale.getDefault())
										.startsWith(s.toLowerCase(Locale.getDefault()))) {
										table.setSelection(i);
										break;
									}
								}
								break;
							case SWT.DefaultSelection :
							case SWT.MouseUp :
								int index = table.getSelectionIndex();
								if (index != idx) {
									tabFolder.setSelection(index);
									setFocus();
								}
								shell.dispose();
								handleTabSelection(tabFolder.getSelection());
								break;
						}
					}
				};
				text.addListener(SWT.Modify, listener);
				text.addListener(SWT.DefaultSelection, listener);
				table.addListener(SWT.MouseUp, listener);
				table.addListener(SWT.DefaultSelection, listener);
				table.addListener(SWT.FocusOut, listener);
				Point size = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				Rectangle displayRect = tabFolder.getMonitor().getClientArea();
				size.y = Math.min(displayRect.height / 3, size.y);
				shell.setSize(size);
				shell.setLocation(x, y);
				shell.open();
				text.setFocus();
			}

		});

		// listener to switch between visible tabItems
		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleTabSelection(e.item);
			}
		});

		// listener to resize visible components
		tabFolder.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				setControlSize();
			}
		});

		// listen for mouse down on tab area to set focus.
		tabFolder.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent event) {
				/* chris: i think this is no longer necessary now that we are using CTabFolder2 and listening to it for 'maximize' events..				 
				Rectangle clientArea = tabFolder.getClientArea();
				if ((tabFolder.getStyle() & SWT.TOP) != 0) {
					if (event.y > clientArea.y)
						return;
				} else {
					if (event.y < clientArea.y + clientArea.height)
						return;
				}
				doZoom();
				*/
			}

			public void mouseDown(MouseEvent event) {
			    EditorPane visibleEditor = getVisibleEditor();
				
				if (visibleEditor != null) {
					CTabItem item = getTab(visibleEditor);
					visibleEditor.setFocus();
					Rectangle bounds = item.getBounds();

					if (event.button == 3 && bounds.contains(event.x, event.y)) {
					    visibleEditor.showPaneMenu();
					}
				}
			}
		});
		
		// Listen for popup menu mouse event
		tabFolder.addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				EditorPane visibleEditor = getVisibleEditor();
				
				if (visibleEditor != null) {
				    CTabItem item = getTab(visibleEditor);
					visibleEditor.setFocus();					
					Rectangle bounds = item.getBounds();					
					Point point = tabFolder.toControl(event.x, event.y);
					
					if (bounds.contains(point.x, point.y))
					    visibleEditor.showPaneMenu();
				}
			}
		});

		// register the interested mouse down listener
		if (!mouseDownListenerAdded && getEditorArea() != null) {
			tabFolder.addListener(SWT.MouseDown, getEditorArea().getMouseDownListener());
			mouseDownListenerAdded = true;
		}

		// Create the pulldown menu on the CTabFolder
		editorList = new EditorList(getEditorArea().getWorkbenchWindow(), this);
		pullDownBar = new ToolBar(tabFolder, SWT.FLAT);
		pullDownButton = new ToolItem(pullDownBar, SWT.PUSH);
		//	Image image = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU);
		Image hotImage =
			WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU_HOVER);
		pullDownButton.setDisabledImage(hotImage); // TODO: comment this out?
		pullDownButton.setImage(hotImage);
		//	pullDownButton.setHotImage(hotImage);
		pullDownButton.setToolTipText(WorkbenchMessages.getString("EditorList.button.toolTip")); //$NON-NLS-1$

		pullDownButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				openEditorList();

			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Present the editorList pull-down if requested
		if (usePulldown) {
			tabFolder.setTopRight(pullDownBar);
			pullDownBar.setVisible(true);
		} else {
			pullDownBar.setVisible(false);
			tabFolder.setTopRight(null);
		}

		// Set the tab width to an arbitrarily large number to prevent shrinking the tabs
		tabFolder.MIN_TAB_WIDTH = 1000; //preferenceStore.getInt(IPreferenceConstants.EDITOR_TAB_WIDTH);

		DragUtil.addDragSource(tabFolder, new AbstractDragSource() {

			public Object getDraggedItem(Point position) {
				Point localPos = tabFolder.toControl(position);
				CTabItem tabUnderPointer = tabFolder.getItem(localPos);
				
				if (tabUnderPointer == null) {
					return null;
				}

				return mapTabToEditor.get(tabUnderPointer);
			}

			public Rectangle getDragRectangle(Object draggedItem) {
				return DragUtil.getDisplayBounds(((LayoutPart)draggedItem).getControl());
			}
			
		});
		
		DragUtil.addDragTarget(tabFolder, new IDragOverListener() {

			public IDropTarget drag(Control currentControl, final Object draggedObject, 
					Point position, Rectangle dragRectangle) {
				
				// We can only drop ViewPanes here
				if (!(draggedObject instanceof EditorPane)) {
					return null;
				}
				
				final EditorPane pane = (EditorPane)draggedObject;
				
				// Don't allow views to be dragged between windows
				if (pane.getWorkbenchWindow() != getWorkbenchWindow()) {
					return null;
				}
				
				Point localPos = tabFolder.toControl(position);
				final CTabItem tabUnderPointer = tabFolder.getItem(localPos);
				
				// This drop target only handles the tab area -- if we're not on the tab area
				// or we're still on the original tab, we can't drop here.
				if (tabUnderPointer == null || mapTabToEditor.get(tabUnderPointer) == draggedObject) {
					return null;
				}
				
				return new IDropTarget() {

					public void drop() {
						ILayoutContainer container = pane.getContainer();
						if (container != TabbedEditorWorkbook.this) {
							if (container != null) {
								container.remove(pane);
								LayoutPart[] children = container.getChildren();
								if (children == null || children.length == 0) {
									// There are no more children in this container, so get rid of it
									if (container instanceof LayoutPart) {
										LayoutPart parent = (LayoutPart)container;
										ILayoutContainer parentContainer = parent.getContainer();
										if (parentContainer != null) {
											parentContainer.remove(parent);
											parent.dispose();
										}
									}
								}
							}
							add(pane);
							setVisibleItem(pane);	
							pane.setFocus();
						}
						
						reorderTab(pane, getTab(pane), tabFolder.indexOf(tabUnderPointer));

						getParent().setRedraw(true);
					}

					public Cursor getCursor() {
						return DragCursors.getCursor(DragCursors.CENTER);
					}

					public Rectangle getSnapRectangle() {
						if (tabUnderPointer == null) {
							return Geometry.toDisplay(tabFolder.getParent(), tabFolder.getBounds());
						}
						
						return Geometry.toDisplay(tabFolder, tabUnderPointer.getBounds());
					}
				};
			}
		});

		
	}

	/**
	 * @param widget
	 */
	protected void handleTabSelection(Widget tabItem) {
		if (handleTabSelection) {
			EditorPane pane = (EditorPane) mapTabToEditor.get(tabItem);
			// Pane can be null when tab is just created but not map yet.
			if (pane != null) {
				setVisibleEditor(pane);
				if (assignFocusOnSelection) {
					// If we get a tab focus hide request, it's from
					// the previous editor in this workbook which had focus.
					// Therefore ignore it to avoid paint flicker
					ignoreTabFocusHide = true;
					pane.setFocus();
					ignoreTabFocusHide = false;
				}
			}
		}
	}

	/**
	 * Returns the tab for a part.
	 */
	private CTabItem getTab(LayoutPart child) {
		Iterator tabs = mapTabToEditor.keySet().iterator();
		while (tabs.hasNext()) {
			CTabItem tab = (CTabItem) tabs.next();
			if (mapTabToEditor.get(tab) == child)
				return tab;
		}

		return null;
	}

	public boolean isDragAllowed(EditorPane pane, Point p) {
		CTabItem tab = getTab(pane);
		return tab != null && overImage(tab, p.x);
	}

	/**
	 * Returns true if <code>x</code> is over the label image.
	 */
	private boolean overImage(CTabItem item, int x) {
		if (item.getImage() == null) {
			return false;
		} else {
			Rectangle imageBounds = item.getImage().getBounds();
			return x < (item.getBounds().x + imageBounds.x + imageBounds.width);
		}
	}

	private void closeEditorList() {
		editorList.destroyControl();
	}

	public void openEditorList() {
		// don't think this check is necessary, need to verify
		if (listComposite != null) {
			return;
		}
		Shell parent = getEditorArea().getWorkbenchWindow().getShell();

		listComposite = new ViewForm(parent, SWT.BORDER);
		listComposite.setVisible(false);
		listComposite.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				listComposite = null;
			}
		});
		parent.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				if (listComposite != null) {
					closeEditorList();
				}
			}
		});

		Control editorListControl = editorList.createControl(listComposite);
		editorListControl.setVisible(false);
		Table editorsTable = ((Table) editorListControl);
		TableItem[] items = editorsTable.getItems();
		if (items.length == 0) {
			listComposite.dispose();
			return;
		}

		listComposite.setContent(editorListControl);
		listComposite.pack();

		setEditorListBounds(parent);

		listComposite.setVisible(true);
		listComposite.moveAbove(null);
		editorListControl.setVisible(true);
		editorListControl.setFocus();
		editorsTable.showSelection();

		editorListControl.addListener(SWT.Deactivate, new Listener() {
			public void handleEvent(Event event) {
				if (listComposite != null) {
					closeEditorList();
				}
			}
		});
	}

	private void setEditorListBounds(Shell parent) {
		final int MAX_ITEMS = 20;

		Rectangle r = listComposite.getBounds();

		int width = r.width;
		int height =
			Math.min(r.height, MAX_ITEMS * ((Table) editorList.getControl()).getItemHeight());
		Rectangle bounds = tabFolder.getClientArea();
		Point point = new Point(bounds.x + bounds.width - width, bounds.y);
		point = tabFolder.toDisplay(point);
		point = parent.toControl(point);
		listComposite.setBounds(listComposite.computeTrim(point.x, point.y, width, height));
	}

	public void resizeEditorList() {
		Shell parent = getEditorArea().getWorkbenchWindow().getShell();
		listComposite.pack();
		setEditorListBounds(parent);
	}

	public void showPaneMenu() {
		EditorPane visibleEditor = getVisibleEditor();
		if (visibleEditor != null) {
			CTabItem item = getTab(visibleEditor);
			Rectangle bounds = item.getBounds();							
			visibleEditor.showPaneMenu(
				tabFolder,
				tabFolder.toDisplay(new Point(bounds.x, bounds.y + bounds.height)));
		}
	}

	protected void disposePresentation() {
		// dispose of disabled images
		for (int i = 0; i < tabFolder.getItemCount(); i++) {
			CTabItem tab = tabFolder.getItem(i);
			if (tab.getDisabledImage() != null)
				tab.getDisabledImage().dispose();
		}

		tabFolder.dispose();
		tabFolder = null;
		mouseDownListenerAdded = false;

		mapTabToEditor.clear();
	}

	// getMinimumHeight() added by cagatayk@acm.org 
	/**
	 * @see LayoutPart#getMinimumHeight()
	 */
	public int getMinimumHeight() {
		if (tabFolder != null && !tabFolder.isDisposed() && getItemCount() > 0)
			/* Subtract 1 for divider line, bottom border is enough
			 * for editor tabs. 
			 */
			return tabFolder.computeTrim(0, 0, 0, 0).height - 1;
		else
			return super.getMinimumHeight();
	}

	public Control getControl() {
		return tabFolder;
	}

	public Control[] getTabList() {
		if (tabFolder == null) {
			return new Control[0];
		}
		EditorPane visibleEditor = getVisibleEditor();
		if (visibleEditor == null) {
			return new Control[] { tabFolder };
		}
		if ((tabFolder.getStyle() & SWT.TOP) != 0) {
			return new Control[] { tabFolder, visibleEditor.getControl()};
		} else {
			return new Control[] { visibleEditor.getControl(), tabFolder };
		}
	}

	public void showVisibleEditor() {
		if (tabFolder != null) {
			tabFolder.showSelection();
		}
	}

	protected void disposeItem(EditorPane editorPane) {
		removeTab(getTab(editorPane));
		if (tabFolder.getItemCount() == 0) {
			pullDownBar.setVisible(false);
		}
	}

	/**
	 * Remove the tab item from the tab folder
	 */
	private void removeTab(CTabItem tab) {
		if (tabFolder != null) {
			if (tab != null) {
				mapTabToEditor.remove(tab);
				if (tab.getDisabledImage() != null)
					tab.getDisabledImage().dispose();
				// Note, that disposing of the tab causes the
				// tab folder to select another tab and fires
				// a selection event. In this situation, do
				// not assign focus.
				assignFocusOnSelection = false;
				tab.dispose();
				assignFocusOnSelection = true;
			}
		}
	}

	public void setContainer(ILayoutContainer container) {
		super.setContainer(container);
		// register the interested mouse down listener
		if (!mouseDownListenerAdded && getEditorArea() != null && tabFolder != null) {
			tabFolder.addListener(SWT.MouseDown, getEditorArea().getMouseDownListener());
			mouseDownListenerAdded = true;
		}
	}

	protected void setVisibleItem(EditorPane editorPane) {
		CTabItem key = getTab(editorPane);
		if (key != null) {
			int index = tabFolder.indexOf(key);
			tabFolder.setSelection(index);			
			Iterator iterator = getEditorList().iterator();
			
			while (iterator.hasNext())
			    updateItem((EditorPane) iterator.next());
		}
	}

	public void tabFocusHide() {
		if (!ignoreTabFocusHide) {
			super.tabFocusHide();
		}
	}

	protected void updateItem(EditorPane editorPane) {
		// Get tab.
		CTabItem tab = getTab(editorPane);
		if (tab == null)
			return;

		IEditorReference editorReference =
		    editorPane.getEditorReference();
		String title = editorReference.getTitle().trim();
		String text = title;
		
		if (editorReference.isDirty())
			text = "*" + text; //$NON-NLS-1$
		
		if (editorPane == getVisibleEditor() && tabFolder != null && (tabFolder.getStyle() & SWT.MULTI) == 0) {		
			String titleTooltip = editorReference.getTitleToolTip().trim();
	
			if (titleTooltip.endsWith(title))
				titleTooltip =
					titleTooltip
						.substring(0, titleTooltip.lastIndexOf(title))
						.trim();
	
			if (titleTooltip.length() >= 1)
				text += " - " + titleTooltip; //$NON-NLS-1$
		}
		
		tab.setText(text);
				
		boolean useColorIcons = ActionContributionItem.getUseColorIconsInToolbars();

		Image image = editorReference.getTitleImage();
		// Update the tab image
		if (image == null || image.isDisposed()) {
			// Normal image.
			tab.setImage(null);
			// Disabled image.
			if (!useColorIcons) {
				Image disableImage = tab.getDisabledImage();
				if (disableImage != null) {
					disableImage.dispose();
					tab.setDisabledImage(null); // TODO: comment this out?
				}
			}
		} else if (!image.equals(tab.getImage())) {
			// Normal image.
			tab.setImage(image);
			// Disabled image.
			if (!useColorIcons) {
				Image disableImage = tab.getDisabledImage();
				if (disableImage != null)
					disableImage.dispose();
				disableImage = new Image(tab.getDisplay(), image, SWT.IMAGE_DISABLE);
				tab.setDisabledImage(disableImage); // TODO: comment this out?
			}
		}

		// Tool tip.
		String toolTip = editorReference.getTitleToolTip();
		tab.setToolTipText(toolTip);
		tab.getParent().update();
	}

	protected void disposeAllItems() {
		// turn off tab selection handling so as
		// not to activate another editor when a
		// tab is disposed. See PR 1GBXAWZ
		handleTabSelection = false;

		Iterator tabs = mapTabToEditor.keySet().iterator();
		while (tabs.hasNext()) {
			CTabItem tab = (CTabItem) tabs.next();
			if (tab.getDisabledImage() != null)
				tab.getDisabledImage().dispose();
			tab.dispose();
		}
		// disable the pulldown menu
		pullDownBar.setVisible(false);

		// Clean up
		mapTabToEditor.clear();
		handleTabSelection = true;
	}

	public void reorderTab(EditorPane pane, int x, int y) {
		CTabItem sourceTab = getTab(pane);
		if (sourceTab == null)
			return;

		// adjust the y coordinate to fall within the tab area
		Point location = new Point(1, 1);
		if ((tabFolder.getStyle() & SWT.BOTTOM) != 0)
			location.y = tabFolder.getSize().y - 4; // account for 3 pixel border

		// adjust the x coordinate to be within the tab area
		if (x > location.x)
			location.x = x;

		// find the tab under the adjusted location.
		CTabItem targetTab = tabFolder.getItem(location);

		// no tab under location so move editor's tab to end
		if (targetTab == null) {
			// do nothing if already at the end
			if (tabFolder.indexOf(sourceTab) != tabFolder.getItemCount() - 1)
				reorderTab(pane, sourceTab, -1);

			return;
		}

		// do nothing if over editor's own tab
		if (targetTab == sourceTab)
			return;

		// do nothing if already before target tab
		int sourceIndex = tabFolder.indexOf(sourceTab);
		int targetIndex = tabFolder.indexOf(targetTab);

		if (sourceIndex == targetIndex - 1)
			return;

		//Source is going to be dispose so the target index will change.
		if (sourceIndex < targetIndex)
			targetIndex--;

		reorderTab(pane, sourceTab, targetIndex);
	}
	/**
	 * Move the specified editor to the a new position. 
	 * Move to the end if <code>newIndex</code> is less then
	 * zero.
	 */
	public void reorderTab(EditorPane pane, int newIndex) {
		reorderTab(pane, getTab(pane), newIndex);
	}

	/**
	 * Reorder the tab representing the specified pane.
	 */
	private void reorderTab(EditorPane pane, CTabItem sourceTab, int newIndex) {
		int oldIndex = tabFolder.indexOf(sourceTab);
		if (newIndex < 0)
			if (oldIndex == tabFolder.getItemCount() - 1)
				return;
			else if (oldIndex == newIndex)
				return;

		// remember if the source tab was the visible one
		boolean wasVisible = (tabFolder.getSelection() == sourceTab);

		// Remove old tab.
		removeTab(sourceTab);

		// Create the new tab at the specified index
		CTabItem newTab;
		if (newIndex < 0)
			newTab = createTab(pane);
		else
			newTab = createTab(pane, newIndex);

		// update order of editors.

		List editors = getEditorList();
		editors.remove(pane);
		if (newIndex < 0)
			editors.add(pane);
		else
			editors.add(newIndex, pane);

		// update the new tab's visibility but do
		// not set focus...caller's responsibility.
		// Note, if the pane already had focus, it
		// will still have it after the tab order change.
		if (wasVisible) {
			tabFolder.setSelection(newTab);
			setVisibleEditor(pane);
		}
	}

	public void findSashes(LayoutPart part, ViewPane.Sashes sashes) {
		ILayoutContainer container = getContainer();
		
		if (container != null) {
			container.findSashes(this, sashes);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.ILayoutContainer#allowsAutoFocus()
	 */
	public boolean allowsAutoFocus() {
		return true;
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.EditorWorkbook#drawGradient(org.eclipse.swt.graphics.Color, org.eclipse.jface.resource.Gradient, boolean)
     */
    protected void drawGradient(Color fgColor, Gradient gradient, boolean activeState) {
		tabFolder.setSelectionForeground(fgColor);
		tabFolder.setSelectionBackground(gradient.getColors(), gradient.getPercents(), gradient.getDirection() == SWT.VERTICAL);
		tabFolder.update();        
    }

	}
	
