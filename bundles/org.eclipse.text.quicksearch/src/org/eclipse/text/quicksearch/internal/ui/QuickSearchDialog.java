/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Willian Mitsuda <wmitsuda@gmail.com>
 *    - Fix for bug 196553 - [Dialogs] Support IColorProvider/IFontProvider in FilteredItemsSelectionDialog
 * Peter Friese <peter.friese@gentleware.com>
 *    - Fix for bug 208602 - [Dialogs] Open Type dialog needs accessible labels
 * Simon Muschel <smuschel@gmx.de> - bug 258493
 * Kris De Volder Copied and modified from org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog
 *                to create QuickSearchDialog
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.ui;

import static org.eclipse.jface.resource.JFaceResources.TEXT_FONT;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR;
import static org.eclipse.ui.texteditor.AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND;
import static org.eclipse.ui.texteditor.AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT;
import static org.eclipse.ui.texteditor.AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND;
import static org.eclipse.ui.texteditor.AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.CursorLinePainter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.osgi.util.NLS;
import org.eclipse.search.internal.ui.text.EditorOpener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageGcDrawer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.text.quicksearch.internal.core.LineItem;
import org.eclipse.text.quicksearch.internal.core.QuickTextQuery;
import org.eclipse.text.quicksearch.internal.core.QuickTextQuery.TextRange;
import org.eclipse.text.quicksearch.internal.core.QuickTextSearchRequestor;
import org.eclipse.text.quicksearch.internal.core.QuickTextSearcher;
import org.eclipse.text.quicksearch.internal.core.pathmatch.ResourceMatchers;
import org.eclipse.text.quicksearch.internal.util.DocumentFetcher;
import org.eclipse.ui.ActiveShellExpression;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.osgi.framework.FrameworkUtil;

/**
 * Shows a list of items to the user with a text entry field for a string
 * pattern used to filter the list of items.
 *
 * @since 3.3
 */
@SuppressWarnings({ "rawtypes", "restriction", "unchecked" })
public class QuickSearchDialog extends SelectionStatusDialog {

	private static final int OPEN_BUTTON_ID = IDialogConstants.OK_ID;
	private static final int REFRESH_BUTTON_ID = IDialogConstants.RETRY_ID;

	public static final Styler HIGHLIGHT_STYLE = org.eclipse.search.internal.ui.text.DecoratingFileSearchLabelProvider.HIGHLIGHT_STYLE;

	private UIJob refreshJob = UIJob.create(Messages.QuickSearchDialog_RefreshJob,
			(ICoreRunnable) m -> refreshWidgets());

	protected void openSelection() {
		try {
			LineItem item = (LineItem) this.getFirstResult();
			if (item!=null) {
				QuickTextQuery q = this.getQuery();
				TextRange range = q.findFirst(item.getText());
				EditorOpener opener = new EditorOpener();
				IWorkbenchPage page = window.getActivePage();
				if (page!=null) {
					opener.openAndSelect(page, item.getFile(), range.getOffset()+item.getOffset(),
						range.getLength(), true);
				}
			}
		} catch (PartInitException e) {
			QuickSearchActivator.log(e);
		}
	}

	/**
	 * Job that shows a simple busy indicator while a search is active.
	 * The job must be scheduled when a search starts/resumes.
	 */
	private UIJob progressJob =  new UIJob(Messages.QuickSearchDialog_RefreshJob) {
		int animate = 0; // number of dots to display.

		protected String dots(int animate) {
			char[] chars = new char[animate];
			for (int i = 0; i < chars.length; i++) {
				chars[i] = '.';
			}
			return new String(chars);
		}

		protected String currentFileInfo(IFile currentFile, int animate) {
			if (currentFile!=null) {
				String path = currentFile.getFullPath().toString();
				if (path.length()<=30) {
					return path;
				}
				return "..."+path.substring(path.length()-30); //$NON-NLS-1$
			}
			return dots(animate);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor mon) {
			if (!mon.isCanceled() && progressLabel != null && !progressLabel.isDisposed()) {
				if (searcher == null || !searcher.isActive()) {
					if (searcher != null && searcher.searchTookMs != 0) {
						progressLabel.setText(Messages.QuickSearchDialog_notFound);
					} else {
						progressLabel.setText(EMPTY_STRING);
					}
				} else {
					progressLabel.setText(NLS.bind(Messages.QuickSearchDialog_searching,
							currentFileInfo(searcher.getCurrentFile(), animate)));
					animate = (animate + 1) % 4;
				}
				if (searcher != null) {
					this.schedule(100);
				}
			}
			return Status.OK_STATUS;
		}
	};

	public final StyledCellLabelProvider LINE_NUMBER_LABEL_PROVIDER = new StyledCellLabelProvider() {
		@Override
		public void update(ViewerCell cell) {
			LineItem item = (LineItem) cell.getElement();
			if (item!=null) {
				cell.setText(Integer.toString(item.getLineNumber()));
			} else {
				cell.setText("?"); //$NON-NLS-1$
			}
			cell.setImage(getBlankImage());
		};
	};

	private static final Color GREY = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);

	private final StyledCellLabelProvider LINE_TEXT_LABEL_PROVIDER = new StyledCellLabelProvider() {

		@Override
		public void update(ViewerCell cell) {
			LineItem item = (LineItem) cell.getElement();
			if (item!=null) {
				StyledString text = highlightMatches(item.getText());
				cell.setText(text.getString());
				cell.setStyleRanges(text.getStyleRanges());
			} else {
				cell.setText(EMPTY_STRING);
				cell.setStyleRanges(null);
			}
			cell.setImage(getBlankImage());
			super.update(cell);
		}
	};

	private Image blankImage;

	private Image getBlankImage() {
		if (blankImage==null) {
			final ImageGcDrawer noOp = (gc, width, height) -> {};
			blankImage = new Image(Display.getDefault(), noOp, 1, 1);
		}
		return blankImage;
	}

	private final StyledCellLabelProvider LINE_FILE_LABEL_PROVIDER = new StyledCellLabelProvider() {

		@Override
		public void update(ViewerCell cell) {
			LineItem item = (LineItem) cell.getElement();
			if (item!=null) {
				IPath path = item.getFile().getFullPath();
				String name = path.lastSegment();
				String dir = path.removeLastSegments(1).toString();
				if (dir.startsWith("/")) { //$NON-NLS-1$
					dir = dir.substring(1);
				}
				cell.setText(name + " - " + dir); //$NON-NLS-1$
				StyleRange[] styleRanges = new StyleRange[] {
						new StyleRange(name.length(), dir.length()+3, GREY, null)
				};
				cell.setStyleRanges(styleRanges);
			} else {
				cell.setText(EMPTY_STRING);
				cell.setStyleRanges(null);
			}
			cell.setImage(getBlankImage());
			super.update(cell);
		}

//		public String getToolTipText(Object element) {
//			LineItem item = (LineItem) element;
//			if (item!=null) {
//				return ""+item.getFile().getFullPath();
//			}
//			return "";
//		};

//		public String getText(Object _item) {
//			if (_item!=null) {
//				LineItem item = (LineItem) _item;
//				return item.getFile().getName().toString();
//			}
//			return "?";
//		};
	};

	private static final String DIALOG_SETTINGS = QuickSearchDialog.class.getName()+".DIALOG_SETTINGS"; //$NON-NLS-1$

	private static final String DIALOG_BOUNDS_SETTINGS = "DialogBoundsSettings"; //$NON-NLS-1$

	private static final String DIALOG_HEIGHT = "DIALOG_HEIGHT"; //$NON-NLS-1$
	private static final String DIALOG_WIDTH = "DIALOG_WIDTH"; //$NON-NLS-1$
	private static final String DIALOG_COLUMNS = "COLUMN_WIDTHS"; //$NON-NLS-1$
	private static final String DIALOG_SASH_WEIGHTS = "SASH_WEIGHTS"; //$NON-NLS-1$

	private static final String DIALOG_LAST_QUERY = "LAST_QUERY"; //$NON-NLS-1$
	private static final String CASE_SENSITIVE = "CASE_SENSITIVE"; //$NON-NLS-1$
	private static final boolean CASE_SENSITIVE_DEFAULT = true;

	private static final String KEEP_OPEN = "KEEP_OPEN"; //$NON-NLS-1$
	private static final boolean KEEP_OPEN_DEFAULT = false;

	private final Deque<String> filterHistory = new LinkedList<>();

	private static final int FILTER_HISTORY_SIZE = 5;

	private static final String FILTER_HISTORY = "FILTER_HISTORY"; //$NON-NLS-1$
	/**
	 * Represents an empty selection in the pattern input field (used only for
	 * initial pattern).
	 */
	public static final int NONE = 0;

	/**
	 * Pattern input field selection where caret is at the beginning (used only
	 * for initial pattern).
	 */
	public static final int CARET_BEGINNING = 1;

	/**
	 * Represents a full selection in the pattern input field (used only for
	 * initial pattern).
	 */
	public static final int FULL_SELECTION = 2;

	private Text pattern;

	private TableViewer list;

	private MenuManager menuManager;

	private MenuManager contextMenuManager;

	private boolean multi;

	private ToolBar toolBar;

	private ToolItem toolItem;

	private Label progressLabel;

	private ContentProvider contentProvider;

	private String initialPatternText;

	private int selectionMode;

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private final int MAX_LINE_LEN;
	private final int MAX_RESULTS;

	private IHandlerActivation showViewHandler;

	private QuickTextSearcher searcher;

	private SourceViewer viewer;
	private LineNumberRulerColumn lineNumberColumn;
	private FixedLineHighlighter targetLineHighlighter;
	private final IPropertyChangeListener preferenceChangeListener = this::handlePropertyChangeEvent;

	private DocumentFetcher documents;


	private ToggleCaseSensitiveAction toggleCaseSensitiveAction;
	private ToggleKeepOpenAction toggleKeepOpenAction;


	private QuickSearchContext context;


	private SashForm sashForm;

	private Label headerLabel;

	private IWorkbenchWindow window;
	private Combo searchIn;
	private Label listLabel;

	private int sortColumnIndex = -1;
	private boolean sortDirectionAscending = true;

	/**
	 * Creates a new instance of the class.
	 *
	 * @param window
	 *           to parent the dialog on
	 */
	public QuickSearchDialog(IWorkbenchWindow window) {
		super(window.getShell());
		this.window = window;
		setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
		setBlockOnOpen(false);
		this.setTitle(Messages.QuickSearchDialog_title);
		this.context = new QuickSearchContext(window);
		this.multi = false;
		contentProvider = new ContentProvider();
		selectionMode = NONE;
		MAX_LINE_LEN = QuickSearchActivator.getDefault().getPreferences().getMaxLineLen();
		MAX_RESULTS = QuickSearchActivator.getDefault().getPreferences().getMaxResults();
		progressJob.setSystem(true);
		EditorsUI.getPreferenceStore().addPropertyChangeListener(preferenceChangeListener);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.window.Window#create()
	 */
	@Override
	public void create() {
		super.create();
		pattern.setFocus();
	}

	/**
	 * Restores dialog using persisted settings.
	 */
	protected void restoreDialog(IDialogSettings settings) {
		try {
			if (initialPatternText==null) {
				String lastSearch = settings.get(DIALOG_LAST_QUERY);
				if (lastSearch==null) {
					lastSearch = EMPTY_STRING;
				}
				pattern.setText(lastSearch);
				pattern.selectAll();
			}

			// Retrieve the last locations where the user searched (works across restarts)
			String[] array = settings.getArray(FILTER_HISTORY);
			if (array != null) {
				filterHistory.addAll(List.of(array));
			}

			if (!filterHistory.isEmpty()) {
				String filter = filterHistory.getFirst();

				// Filter out blanks
				filterHistory.removeIf(String::isBlank);

				searchIn.setItems(filterHistory.toArray(String[]::new));
				searchIn.setText(filter);
			}

			if (settings.getArray(DIALOG_COLUMNS)!=null) {
				String[] columnWidths = settings.getArray(DIALOG_COLUMNS);
				Table table = list.getTable();
				int cols = table.getColumnCount();
				for (int i = 0; i < cols; i++) {
					TableColumn col = table.getColumn(i);
					try {
						if (col!=null) {
							col.setWidth(Integer.parseInt(columnWidths[i]));
						}
					} catch (Throwable e) {
						QuickSearchActivator.log(e);
					}
				}
			}

			if (settings.getArray(DIALOG_SASH_WEIGHTS)!=null) {
				String[] _weights = settings.getArray(DIALOG_SASH_WEIGHTS);
				int[] weights = new int[_weights.length];
				for (int i = 0; i < weights.length; i++) {
					weights[i] = Integer.parseInt(_weights[i]);
				}
				sashForm.setWeights(weights);
			}
		} catch (Throwable e) {
			//None of this stuff is critical so shouldn't stop opening dialog if it fails!
			QuickSearchActivator.log(e);
		}
	}

	private void inputNewSearchFilter(String searchIn) {
		filterHistory.remove(searchIn);
		filterHistory.addFirst(searchIn);

		if (filterHistory.size() > FILTER_HISTORY_SIZE) {
			filterHistory.removeLast();
		}
	}

	private class ToggleKeepOpenAction extends Action {
		public ToggleKeepOpenAction(IDialogSettings settings) {
			super(
					Messages.QuickSearchDialog_keepOpen_toggle,
					IAction.AS_CHECK_BOX
			);
			if (settings.get(KEEP_OPEN)==null) {
				setChecked(KEEP_OPEN_DEFAULT);
			} else{
				setChecked(settings.getBoolean(KEEP_OPEN));
			}
		}

		@Override
		public void run() {
			//setChecked(!isChecked());
		}

	}


	private class ToggleCaseSensitiveAction extends Action {

		public ToggleCaseSensitiveAction(IDialogSettings settings) {
			super(
					Messages.QuickSearchDialog_caseSensitive_toggle,
					IAction.AS_CHECK_BOX
			);
			if (settings.get(CASE_SENSITIVE)==null) {
				setChecked(CASE_SENSITIVE_DEFAULT);
			} else{
				setChecked(settings.getBoolean(CASE_SENSITIVE));
			}
		}

		@Override
		public void run() {
			//setChecked(!isChecked());
			refreshHeaderLabel();
			applyFilter(false);
		}
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.window.Window#close()
	 */
	@Override
	public boolean close() {
		this.progressJob.cancel();
		this.progressJob = null;
//		this.refreshProgressMessageJob.cancel();
		if (showViewHandler != null) {
			IHandlerService service = PlatformUI
					.getWorkbench().getService(IHandlerService.class);
			service.deactivateHandler(showViewHandler);
			showViewHandler.getHandler().dispose();
			showViewHandler = null;
		}
		if (menuManager != null)
			menuManager.dispose();
		if (contextMenuManager != null)
			contextMenuManager.dispose();
		storeDialog(getDialogSettings());
		if (searcher!=null) {
			searcher.cancel();
		}
		return super.close();
	}

	/**
	 * Stores dialog settings.
	 *
	 * @param settings
	 *           settings used to store dialog
	 */
	protected void storeDialog(IDialogSettings settings) {
		String currentSearch = pattern.getText();
		settings.put(DIALOG_LAST_QUERY, currentSearch);

		inputNewSearchFilter(searchIn.getText());
		settings.put(FILTER_HISTORY, filterHistory.toArray(String[]::new));

		if (toggleCaseSensitiveAction!=null) {
			settings.put(CASE_SENSITIVE, toggleCaseSensitiveAction.isChecked());
		}
		if (toggleKeepOpenAction!=null) {
			settings.put(KEEP_OPEN, toggleKeepOpenAction.isChecked());
		}
		Table table = list.getTable();
		if (table.getColumnCount()>0) {
			String[] columnWidths = new String[table.getColumnCount()];
			for (int i = 0; i < columnWidths.length; i++) {
				columnWidths[i] = Integer.toString(table.getColumn(i).getWidth());
			}
			settings.put(DIALOG_COLUMNS, columnWidths);
		}
		if (sashForm.getWeights()!=null) {
			int[] w = sashForm.getWeights();
			String[] ws = new String[w.length];
			for (int i = 0; i < ws.length; i++) {
				ws[i] = Integer.toString(w[i]);
			}
			settings.put(DIALOG_SASH_WEIGHTS, ws);
		}
	}

	/**
	 * Create a new header which is labelled by headerLabel.
	 *
	 * @return Label the label of the header
	 */
	private Label createHeader(Composite parent) {
		Composite header = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		header.setLayout(layout);

		headerLabel = new Label(header, SWT.NONE);
		headerLabel.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_MNEMONIC && e.doit) {
				e.detail = SWT.TRAVERSE_NONE;
				pattern.setFocus();
			}
		});

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		headerLabel.setLayoutData(gd);

		createViewMenu(header);
		header.setLayoutData(gd);

		refreshHeaderLabel();
		return headerLabel;
	}

	private void refreshHeaderLabel() {
		String msg = toggleCaseSensitiveAction.isChecked() ? Messages.QuickSearchDialog_caseSensitive_label : Messages.QuickSearchDialog_caseInsensitive_label;
		headerLabel.setText(msg);
	}

	/**
	 * Create the labels for the list and the progress. Return the list label.
	 *
	 * @return Label
	 */
	private Label createLabels(Composite parent) {
		Composite labels = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		labels.setLayout(layout);

		Label listLabel = new Label(labels, SWT.NONE);
		listLabel
				.setText(""); //$NON-NLS-1$

		listLabel.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_MNEMONIC && e.doit) {
				e.detail = SWT.TRAVERSE_NONE;
				list.getTable().setFocus();
			}
		});

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		listLabel.setLayoutData(gd);

		progressLabel = new Label(labels, SWT.RIGHT);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		progressLabel.setLayoutData(gd);
		createButton(labels, REFRESH_BUTTON_ID, Messages.QuickSearchDialog_Refresh, false);

		labels.setLayoutData(gd);
		return listLabel;
	}

	private void createViewMenu(Composite parent) {
		toolBar = new ToolBar(parent, SWT.FLAT);
		toolItem = new ToolItem(toolBar, SWT.PUSH, 0);

		GridData data = new GridData();
		data.horizontalAlignment = GridData.END;
		toolBar.setLayoutData(data);

		toolBar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				showViewMenu();
			}
		});

		toolItem.setImage(WorkbenchImages
				.getImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU));
		toolItem
				.setToolTipText(""); //$NON-NLS-1$
		toolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showViewMenu();
			}
		});

		menuManager = new MenuManager();

		fillViewMenu(menuManager);

		IHandlerService service = PlatformUI.getWorkbench()
				.getService(IHandlerService.class);
		IHandler handler = new AbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event) {
				showViewMenu();
				return null;
			}
		};
		showViewHandler = service.activateHandler(
				IWorkbenchCommandConstants.WINDOW_SHOW_VIEW_MENU, handler,
				new ActiveShellExpression(getShell()));
	}

	/**
	 * Fills the menu of the dialog.
	 *
	 * @param menuManager
	 *           the menu manager
	 */
	protected void fillViewMenu(IMenuManager menuManager) {
		IDialogSettings settings = getDialogSettings();
		toggleCaseSensitiveAction = new ToggleCaseSensitiveAction(settings);
		menuManager.add(toggleCaseSensitiveAction);
		toggleKeepOpenAction = new ToggleKeepOpenAction(settings);
		menuManager.add(toggleKeepOpenAction);
	}

	private void showViewMenu() {
		Menu menu = menuManager.createContextMenu(getShell());
		Rectangle bounds = toolItem.getBounds();
		Point topLeft = new Point(bounds.x, bounds.y + bounds.height);
		topLeft = toolBar.toDisplay(topLeft);
		menu.setLocation(topLeft.x, topLeft.y);
		menu.setVisible(true);
	}

    /**
     * Hook that allows to add actions to the context menu.
	 * <p>
	 * Subclasses may extend in order to add other actions.</p>
     *
     * @param menuManager the context menu manager
     * @since 3.5
     */
	protected void fillContextMenu(IMenuManager menuManager) {
	}

	private void createPopupMenu() {

		contextMenuManager = new MenuManager();
		contextMenuManager.setRemoveAllWhenShown(true);
		contextMenuManager.addMenuListener(this::fillContextMenu);

		final Table table = list.getTable();
		Menu menu= contextMenuManager.createContextMenu(table);
		table.setMenu(menu);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		dialogArea.addDisposeListener(e -> QuickSearchDialog.this.dispose());

		Composite content = createNestedComposite(dialogArea, 1, false);
		GridData gd = new GridData(GridData.FILL_BOTH);
		content.setLayoutData(gd);

		final Label headerLabel = createHeader(content);

		Composite inputRow = createNestedComposite(content, 10, true);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(inputRow);
		pattern = new Text(inputRow, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
		pattern.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result = LegacyActionTools.removeMnemonics(headerLabel
						.getText());
			}
		});
		GridDataFactory.fillDefaults().span(6,1).grab(true, false).applyTo(pattern);

		Composite searchInComposite = createNestedComposite(inputRow, 2, false);
		GridDataFactory.fillDefaults().span(4,1).grab(true, false).applyTo(searchInComposite);
		Label searchInLabel = new Label(searchInComposite, SWT.NONE);
		searchInLabel.setText(Messages.QuickSearchDialog_In);
		GridDataFactory.swtDefaults().indent(5, 0).applyTo(searchInLabel);
		searchIn = new Combo(searchInComposite, SWT.SINGLE | SWT.BORDER | SWT.ICON_CANCEL);
		searchIn.setToolTipText(Messages.QuickSearchDialog_InTooltip);
		GridDataFactory.fillDefaults().grab(true, false).indent(5, 0).applyTo(searchIn);

		listLabel = createLabels(content);

		sashForm = new SashForm(content, SWT.VERTICAL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(sashForm);

		list = new TableViewer(sashForm, (multi ? SWT.MULTI : SWT.SINGLE) |
				SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.VIRTUAL);
//		ColumnViewerToolTipSupport.enableFor(list, ToolTip.NO_RECREATE);

		list.getTable().setHeaderVisible(true);
		list.getTable().setLinesVisible(true);
		list.getTable().getAccessible().addAccessibleListener(
				new AccessibleAdapter() {
					@Override
					public void getName(AccessibleEvent e) {
						if (e.childID == ACC.CHILDID_SELF) {
							e.result = LegacyActionTools
									.removeMnemonics(listLabel.getText());
						}
					}
				});
		list.setContentProvider(contentProvider);
//		new ScrollListener(list.getTable().getVerticalBar());
//		new SelectionChangedListener(list);

		Table table = list.getTable();
		TableViewerColumn col = new TableViewerColumn(list, SWT.RIGHT);
		TableColumn column = col.getColumn();
		column.setText(Messages.QuickSearchDialog_line);
		column.setWidth(40);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Comparator<LineItem> lineNumberComparator = Comparator.comparingInt(LineItem::getLineNumber);
				handleColumnSort(table, 0, lineNumberComparator);
			}
		});
		col.setLabelProvider(LINE_NUMBER_LABEL_PROVIDER);

		col = new TableViewerColumn(list, SWT.LEFT);
		column = col.getColumn();
		column.setText(Messages.QuickSearchDialog_text);
		column.setWidth(400);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Comparator<LineItem> textComparator = Comparator.comparing( LineItem::getText);
				handleColumnSort(table, 1, textComparator);
			}
		});
		col.setLabelProvider(LINE_TEXT_LABEL_PROVIDER);

		col = new TableViewerColumn(list, SWT.LEFT);
		column = col.getColumn();
		column.setText(Messages.QuickSearchDialog_path);
		column.setWidth(150);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Comparator<LineItem> lineItemComparator = Comparator.comparing(item -> item.getFile().getFullPath().toString());
				handleColumnSort(table, 2, lineItemComparator);
			}
		});
		col.setLabelProvider(LINE_FILE_LABEL_PROVIDER);
		new TableResizeHelper(list).enableResizing();

		//list.setLabelProvider(getItemsListLabelProvider());
		list.setInput(new Object[0]);
		list.setItemCount(contentProvider.getNumberOfElements());
		gd = new GridData(GridData.FILL_BOTH);
		applyDialogFont(list.getTable());
		gd.heightHint= list.getTable().getItemHeight() * 15;
		list.getTable().setLayoutData(gd);

		createPopupMenu();

		pattern.addModifyListener(e -> {
			applyFilter(false);
		});

		searchIn.addModifyListener(e -> {
			applyPathMatcher();
		});

		pattern.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_DOWN) {
					if (list.getTable().getItemCount() > 0) {
						list.getTable().setFocus();
						list.getTable().select(0);
						//programatic selection may not fire selection events so...
						refreshDetails();
					}
				}
			}
		});

		list.addSelectionChangedListener(event -> {
			StructuredSelection selection = (StructuredSelection) event
					.getSelection();
			handleSelected(selection);
		});

		list.addDoubleClickListener(event -> handleDoubleClick());

		list.getTable().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {

				if (e.keyCode == SWT.ARROW_UP && (e.stateMask & SWT.SHIFT) == 0
						&& (e.stateMask & SWT.CTRL) == 0) {
					StructuredSelection selection = (StructuredSelection) list
							.getSelection();

					if (selection.size() == 1) {
						Object element = selection.getFirstElement();
						if (element.equals(list.getElementAt(0))) {
							pattern.setFocus();
						}
						list.getTable().notifyListeners(SWT.Selection,
								new Event());

					}
				}

				if (e.keyCode == SWT.ARROW_DOWN
						&& (e.stateMask & SWT.SHIFT) != 0
						&& (e.stateMask & SWT.CTRL) != 0) {

					list.getTable().notifyListeners(SWT.Selection, new Event());
				}

			}
		});

		createDetailsArea(sashForm);
		sashForm.setWeights(new int[] {5,2});

		applyDialogFont(content);

		restoreDialog(getDialogSettings());

		if (initialPatternText != null) {
			pattern.setText(initialPatternText);
		}

		switch (selectionMode) {
		case CARET_BEGINNING:
			pattern.setSelection(0, 0);
			break;
		case FULL_SELECTION:
			pattern.selectAll();
			break;
		}

		// apply filter even if pattern is empty (display history)
		applyFilter(false);

		return dialogArea;
	}

	private void handleColumnSort(Table table, int columnIndex, Comparator sorter) {
		if (sortColumnIndex == columnIndex) {
			sortDirectionAscending = !sortDirectionAscending;
		} else {
			sortColumnIndex = columnIndex;
			sortDirectionAscending = true;
		}
		table.setSortColumn(table.getColumn(columnIndex));
		table.setSortDirection(sortDirectionAscending ? SWT.UP : SWT.DOWN);

		contentProvider.setComparator(sortDirectionAscending ? sorter : sorter.reversed());
		contentProvider.sortList();
		refreshWidgets();
	}

	private Composite createNestedComposite(Composite parent, int numRows, boolean equalRows) {
		Composite nested = new Composite(parent, SWT.NONE);
		{
			GridLayout layout = new GridLayout(numRows, equalRows);
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			layout.marginLeft = 0;
			layout.marginRight = 0;
			layout.horizontalSpacing = 0;
			nested.setLayout(layout);
		}
		GridDataFactory.fillDefaults().grab(true, false).applyTo(nested);
		return nested;
	}

	protected void dispose() {
		if (blankImage!=null) {
			blankImage.dispose();
			blankImage = null;
		}
		EditorsUI.getPreferenceStore().removePropertyChangeListener(preferenceChangeListener);
	}

	private void createDetailsArea(Composite parent) {
		var viewerParent = new Canvas(parent, SWT.BORDER);
		viewerParent.setLayout(new FillLayout());

		viewer = new SourceViewer(viewerParent, new CompositeRuler(), SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		viewer.getTextWidget().setFont(JFaceResources.getFont(TEXT_FONT));
		createViewerDecorations();

		list.addSelectionChangedListener(event -> refreshDetails());

		viewer.getTextWidget().addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				refreshDetails();
			}
		});
	}

	private void setColors() {
		RGB background = null;
		RGB foreground = null;
		var textWidget = viewer.getTextWidget();
		ISharedTextColors sharedColors = EditorsUI.getSharedTextColors();

		var isUsingSystemBackground = EditorsUI.getPreferenceStore().getBoolean(PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT);
		if (!isUsingSystemBackground) {
			background = getColorFromStore(PREFERENCE_COLOR_BACKGROUND);
		}
		if (background != null) {
			var color = sharedColors.getColor(background);
			textWidget.setBackground(color);
			lineNumberColumn.setBackground(color);
		} else {
			textWidget.setBackground(null);
			lineNumberColumn.setBackground(null);
		}

		var isUsingSystemForeground = EditorsUI.getPreferenceStore().getBoolean(PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT);
		if (!isUsingSystemForeground) {
			foreground = getColorFromStore(PREFERENCE_COLOR_FOREGROUND);
		}
		if (foreground != null) {
			textWidget.setForeground(sharedColors.getColor(foreground));
		} else {
			textWidget.setForeground(null);
		}
	}

	private Color getLineNumbersColor() {
		var lineNumbersColor =  getColorFromStore(EDITOR_LINE_NUMBER_RULER_COLOR);
		return EditorsUI.getSharedTextColors().getColor(lineNumbersColor == null ? new RGB(0, 0, 0) : lineNumbersColor);
	}

	private Color getTargetLineHighlightColor() {
		RGB background = getColorFromStore(EDITOR_CURRENT_LINE_COLOR);
		ISharedTextColors sharedColors = EditorsUI.getSharedTextColors();
		return sharedColors.getColor(background);
	}

	private void createViewerDecorations() {
		lineNumberColumn = new LineNumberRulerColumn();
		lineNumberColumn.setForeground(getLineNumbersColor());
		viewer.addVerticalRulerColumn(lineNumberColumn);

		var sourceViewerDecorationSupport = new SourceViewerDecorationSupport(viewer, null, null, EditorsUI.getSharedTextColors());
		sourceViewerDecorationSupport.setCursorLinePainterPreferenceKeys(EDITOR_CURRENT_LINE, EDITOR_CURRENT_LINE_COLOR);
		sourceViewerDecorationSupport.install(EditorsUI.getPreferenceStore());
		targetLineHighlighter = new FixedLineHighlighter();
		targetLineHighlighter.highlightColor = getTargetLineHighlightColor();
		viewer.getTextWidget().addLineBackgroundListener(targetLineHighlighter);

		setColors();
	}

	private void handlePropertyChangeEvent(PropertyChangeEvent event) {
		if (viewer == null) {
			return;
		}
		var prop = event.getProperty();
		if (PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT.equals(prop)
				|| PREFERENCE_COLOR_BACKGROUND.equals(prop)
				|| PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT.equals(prop)
				|| PREFERENCE_COLOR_FOREGROUND.equals(prop)) {
			setColors();
			viewer.getTextWidget().redraw();
		} else if (EDITOR_LINE_NUMBER_RULER_COLOR.equals(prop)) {
			lineNumberColumn.setForeground(getLineNumbersColor());
			lineNumberColumn.redraw();
		} else if (EDITOR_CURRENT_LINE_COLOR.equals(prop)) {
			targetLineHighlighter.highlightColor = getTargetLineHighlightColor();
			viewer.getTextWidget().redraw();
		}
	}

	private RGB getColorFromStore(String key) {
		var store = EditorsUI.getPreferenceStore();
		RGB rgb = null;
		if (store.contains(key)) {
			if (store.isDefault(key)) {
				rgb = PreferenceConverter.getDefaultColor(store, key);
			} else {
				rgb = PreferenceConverter.getColor(store, key);
			}
		}
		return rgb;
	}

	private void refreshDetails() {
		if (viewer!=null && list!=null && !list.getTable().isDisposed()) {
			if (documents==null) {
				documents = new DocumentFetcher();
			}
			IStructuredSelection sel = (IStructuredSelection) list.getSelection();
			if (sel==null || sel.isEmpty()) {
				viewer.setDocument(null);
			} else {
				//Not empty selection
				final int context = 100; // number of lines before and after match to include in preview
				int numLines = computeLines();
				if (numLines > 0) {
					LineItem item = (LineItem) sel.getFirstElement();
					IDocument document = documents.getDocument(item.getFile());
					if (document!=null) {
						try {
							int line = item.getLineNumber()-1; //in document lines are 0 based. In search 1 based.
							int contextStartLine = Math.max(line-(numLines-1)/2 - context, 0);
							int start = document.getLineOffset(contextStartLine);
							int displayedEndLine = line + numLines/2;
							int end = document.getLength();
							if (displayedEndLine + context <= document.getNumberOfLines()) {
								try {
									IRegion lineInfo = document.getLineInformation(displayedEndLine + context);
									end = lineInfo.getOffset() + lineInfo.getLength();
								} catch (BadLocationException e) {
									//Presumably line number is past the end of document.
									//ignore.
								}
							}
							int contextLenght = end-start;

							viewer.setDocument(document);
							viewer.setVisibleRegion(start, contextLenght);

							targetLineHighlighter.setTargetLineOffset(item.getOffset() - start);

							// center target line in the displayed area
							IRegion rangeEndLineInfo = document.getLineInformation(Math.min(displayedEndLine, document.getNumberOfLines() - 1));
							int rangeStart = document.getLineOffset(Math.max(line - numLines/2, 0));
							int rangeEnd = rangeEndLineInfo.getOffset() + rangeEndLineInfo.getLength();
							viewer.revealRange(rangeStart, rangeEnd - rangeStart);

							var targetLineFirstMatch = getQuery().findFirst(document.get(item.getOffset(), contextLenght - (item.getOffset() - start)));
							int targetLineFirstMatchStart = item.getOffset() + targetLineFirstMatch.getOffset();
							// sets caret position
							viewer.setSelectedRange(targetLineFirstMatchStart, 0);
							// does horizontal scrolling if necessary to reveal 1st occurrence in target line
							viewer.revealRange(targetLineFirstMatchStart, targetLineFirstMatch.getLength());

							// above setVisibleRegion() call makes these ranges to be aligned with content of text widget
							StyledString styledString = highlightMatches(document.get(start, contextLenght));
							viewer.getTextWidget().setStyleRanges(styledString.getStyleRanges());
							return;
						} catch (BadLocationException e) {
						}
					}
				}
			}
			//empty selection or some error:
			viewer.setDocument(null);
		}
	}

	/**
	 * Computes how many lines of text can be displayed in the details section.
	 */
	private int computeLines() {
		StyledText details;
		if (viewer!=null && !(details = viewer.getTextWidget()).isDisposed()) {
			int lineHeight = details.getLineHeight();
			int areaHeight = details.getClientArea().height;
			return (areaHeight + lineHeight - 1) / lineHeight;
		}
		return 0;
	}

	/**
	 * Helper function to highlight all the matches for the current query in a given piece
	 * of text.
	 *
	 * @return StyledString instance.
	 */
	private StyledString highlightMatches(String visibleText) {
		StyledString styledText = new StyledString(visibleText);
		List<TextRange> matches = getQuery().findAll(visibleText);
		for (TextRange m : matches) {
			styledText.setStyle(m.getOffset(), m.getLength(), HIGHLIGHT_STYLE);
		}
		return styledText;
	}

	/**
	 * Handle selection in the items list by updating labels of selected and
	 * unselected items and refresh the details field using the selection.
	 *
	 * @param selection
	 *           the new selection
	 */
	protected void handleSelected(StructuredSelection selection) {
		IStatus status = new Status(IStatus.OK, PlatformUI.PLUGIN_ID,
				IStatus.OK, EMPTY_STRING, null);

		updateStatus(status);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.window.Dialog#getDialogBoundsSettings()
	 */
	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings settings = getDialogSettings();
		IDialogSettings section = settings.getSection(DIALOG_BOUNDS_SETTINGS);
		if (section == null) {
			section = settings.addNewSection(DIALOG_BOUNDS_SETTINGS);
			section.put(DIALOG_HEIGHT, 500);
			section.put(DIALOG_WIDTH, 600);
		}
		return section;
	}

	/**
	 * Returns the dialog settings. Returned object can't be null.
	 *
	 * @return return dialog settings for this dialog
	 */
	protected IDialogSettings getDialogSettings() {
		IDialogSettings dialogSettings = PlatformUI.getDialogSettingsProvider(FrameworkUtil.getBundle(IDEWorkbenchPlugin.class))
				.getDialogSettings();
		IDialogSettings settings = dialogSettings.getSection(DIALOG_SETTINGS);
		if (settings == null) {
			settings = dialogSettings
					.addNewSection(DIALOG_SETTINGS);
		}
		return settings;
	}

	/**
	 * Has to be called in UI thread.
	 */
	public void refreshWidgets() {
		if (list != null && !list.getTable().isDisposed()) {
			int itemCount = contentProvider.getNumberOfElements();
			list.setItemCount(itemCount);
			if (itemCount < MAX_RESULTS) {
				listLabel.setText(NLS.bind(Messages.QuickSearchDialog_listLabel, itemCount));
			} else {
				listLabel.setText(NLS.bind(Messages.QuickSearchDialog_listLabel_limit_reached, itemCount));
			}
			listLabel.pack();
			list.refresh(true, false);
			Button openButton = getButton(OPEN_BUTTON_ID);
			if (openButton!=null && !openButton.isDisposed()) {
				//Even if no element is selected. The dialog should be have as if the first
				//element in the list is selected. So the button is enabled if any
				//element is available in the list.
				openButton.setEnabled(itemCount>0);
			}
			refreshDetails();
		}
	}

	/**
	 * Schedule refresh job.
	 */
	public void scheduleRefresh() {
		refreshJob.schedule();
//		refreshCacheJob.cancelAll();
//		refreshCacheJob.schedule();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.dialogs.SelectionStatusDialog#computeResult()
	 */
	@Override
	protected void computeResult() {
		List objectsToReturn = ((StructuredSelection) list.getSelection())
				.toList();
		if (objectsToReturn.isEmpty()) {
			//Pretend that the first element is selected.
			Object first = list.getElementAt(0);
			if (first!=null) {
				objectsToReturn = Arrays.asList(first);
			}
		}
		setResult(objectsToReturn);
	}



	/**
	 * Handles double-click of items, but *also* by pressing the 'enter' key.
	 */
	protected void handleDoubleClick() {
		okPressed();
	}

	protected void refreshButtonPressed() {
		applyFilter(true);
	}


	@Override
	protected void okPressed() {
		computeResult();
		openSelection();
		if (!toggleKeepOpenAction.isChecked()) {
			setReturnCode(OK);
			close();
		}
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == REFRESH_BUTTON_ID) {
			refreshButtonPressed();
		} else {
			super.buttonPressed(buttonId);
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, OPEN_BUTTON_ID, Messages.QuickSearchDialog_Open, true);
		refreshWidgets();
	}

	/**
	 * Sets the initial pattern used by the filter. This text is copied into the
	 * selection input on the dialog. A full selection is used in the pattern
	 * input field.
	 *
	 * @param text
	 *           initial pattern for the filter
	 * @see QuickSearchDialog#FULL_SELECTION
	 */
	public void setInitialPattern(String text) {
		setInitialPattern(text, FULL_SELECTION);
	}

	/**
	 * Sets the initial pattern used by the filter. This text is copied into the
	 * selection input on the dialog. The <code>selectionMode</code> is used
	 * to choose selection type for the input field.
	 *
	 * @param text
	 *           initial pattern for the filter
	 * @param selectionMode
	 *           one of: {@link QuickSearchDialog#NONE},
	 *           {@link QuickSearchDialog#CARET_BEGINNING},
	 *           {@link QuickSearchDialog#FULL_SELECTION}
	 */
	public void setInitialPattern(String text, int selectionMode) {
		this.initialPatternText = text;
		this.selectionMode = selectionMode;
	}

	/**
	 * Gets initial pattern.
	 *
	 * @return initial pattern, or <code>null</code> if initial pattern is not
	 *        set
	 */
	protected String getInitialPattern() {
		return this.initialPatternText;
	}

	/**
	 * Returns the current selection.
	 *
	 * @return the current selection
	 */
	protected StructuredSelection getSelectedItems() {

		StructuredSelection selection = (StructuredSelection) list
				.getSelection();

		List selectedItems = selection.toList();

		return new StructuredSelection(selectedItems);
	}

	/**
	 * Validates the item. When items on the items list are selected or
	 * deselected, it validates each item in the selection and the dialog status
	 * depends on all validations.
	 *
	 * @param item
	 *           an item to be checked
	 * @return status of the dialog to be set
	 */
	protected IStatus validateItem(Object item) {
		return Status.OK_STATUS;
	}

	/**
	 * Creates an instance of a filter.
	 *
	 * @return a filter for items on the items list. Can be <code>null</code>,
	 *        no filtering will be applied then, causing no item to be shown in
	 *        the list.
	 */
	protected QuickTextQuery createFilter() {
		return new QuickTextQuery(pattern.getText(), !toggleCaseSensitiveAction.isChecked());
	}

	/**
	 * Applies the filter created by <code>createFilter()</code> method to the
	 * items list. When new filter is different than previous one it will cause
	 * refiltering.
	 * <p>
	 * The 'force' parameter forces a full refresh of the search results / filter even
	 * when the filter is unchanged, or when a incremenal filtering optimisation could be
	 * applied based on query structure. (The use case for this, is to trigger forced refresh
	 * because the underlying resources may have changed).
	 */
	protected void applyFilter(boolean force) {
		QuickTextQuery newFilter = createFilter();
		getShell().setText(Messages.QuickSearchDialog_title + " - " + pattern.getText()); //$NON-NLS-1$
		if (this.searcher==null) {
			if (!newFilter.isTrivial()) {
				//Create the QuickTextSearcher with the inital query.
				this.searcher = new QuickTextSearcher(newFilter, context.createPriorityFun(), MAX_LINE_LEN, new QuickTextSearchRequestor() {
					@Override
					public void add(LineItem match) {
						contentProvider.add(match);
						contentProvider.refresh();
					}
					@Override
					public void clear() {
						contentProvider.reset();
						contentProvider.refresh();
					}
					@Override
					public void revoke(LineItem match) {
						contentProvider.remove(match);
						contentProvider.refresh();
					}
					@Override
					public void update(LineItem match) {
						contentProvider.refresh();
					}
				});
				this.searcher.setMaxResults(MAX_RESULTS);
				applyPathMatcher();
				refreshWidgets();
			}
//			this.list.setInput(input)
		} else {
			//The QuickTextSearcher is already active update the query
			this.searcher.setQuery(newFilter, force);
		}
		if (progressJob!=null) {
			progressJob.schedule();
		}
	}

	private void applyPathMatcher() {
		if (this.searcher!=null) {
			this.searcher.setPathMatcher(ResourceMatchers.commaSeparatedPaths(searchIn.getText()));
		}
	}


	/**
	 * Collects filtered elements. Contains one synchronized, sorted set for
	 * collecting filtered elements.
	 * Implementation of <code>ItemsFilter</code> is used to filter elements.
	 * The key function of filter used in to filtering is
	 * <code>matchElement(Object item)</code>.
	 * <p>
	 * The <code>ContentProvider</code> class also provides item filtering
	 * methods. The filtering has been moved from the standard TableView
	 * <code>getFilteredItems()</code> method to content provider, because
	 * <code>ILazyContentProvider</code> and virtual tables are used. This
	 * class is responsible for adding a separator below history items and
	 * marking each items as duplicate if its name repeats more than once on the
	 * filtered list.
	 */
	private class ContentProvider implements IStructuredContentProvider, ILazyContentProvider {

		private List items;
		private Comparator<LineItem> comparator;
		/**
		 * Creates new instance of <code>ContentProvider</code>.
		 */
		public ContentProvider() {
			this.items = Collections.synchronizedList(new ArrayList(2048));
//			this.duplicates = Collections.synchronizedSet(new HashSet(256));
//			this.lastSortedItems = Collections.synchronizedList(new ArrayList(
//					2048));
		}

		public void remove(LineItem match) {
			this.items.remove(match);
		}

		/**
		 * Removes all content items and resets progress message.
		 */
		public void reset() {
			this.items.clear();
		}

		/**
		 * Adds filtered item.
		 */
		public void add(LineItem match) {
			this.items.add(match);
		}

		/**
		 * Refresh dialog.
		 */
		public void refresh() {
			scheduleRefresh();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		@Override
		public Object[] getElements(Object inputElement) {
			return items.toArray();
		}

		public int getNumberOfElements() {
			return items.size();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		@Override
		public void dispose() {
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *     java.lang.Object, java.lang.Object)
		 */
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.jface.viewers.ILazyContentProvider#updateElement(int)
		 */
		@Override
		public void updateElement(int index) {

			QuickSearchDialog.this.list.replace((items
					.size() > index) ? items.get(index) : null,
					index);

		}

		/**
		 * Sorts the current search results based on current comparator
		 */
		public void sortList() {
			if (comparator == null) {
				return;
			}
			items.sort(comparator);
		}

		/**
		 * Sets a custom comparator for comparing LineItem objects.
		 *
		 * @param comparator a <code>Comparator<code> object that defines the custom comparison logic.
		 */
		public void setComparator(Comparator<LineItem> comparator) {
			this.comparator = comparator;
		}

	}

	/**
	 * Get the control where the search pattern is entered. Any filtering should
	 * be done using an {@code ItemsFilter}. This control should only be
	 * accessed for listeners that wish to handle events that do not affect
	 * filtering such as custom traversal.
	 *
	 * @return Control or <code>null</code> if the pattern control has not
	 *        been created.
	 */
	public Control getPatternControl() {
		return pattern;
	}

	public QuickTextQuery getQuery() {
		return searcher.getQuery();
	}

	/**
	 * A line background listener that provides the color that is used for current line highlighting (what
	 * {@link CursorLinePainter} does) but for single fixed line only and does so always regardless of show current
	 * line highlighting on/off preference.
	 *
	 * @see CursorLinePainter
	 */
	private static class FixedLineHighlighter implements LineBackgroundListener {

		private int lineOffset = -1;
		private Color highlightColor;

		public void setTargetLineOffset(int lineOffset) {
			this.lineOffset = lineOffset;
		}

		@Override
		public void lineGetBackground(LineBackgroundEvent event) {
			if (lineOffset == event.lineOffset) {
				event.lineBackground = highlightColor;
			}
		}

	}

}
