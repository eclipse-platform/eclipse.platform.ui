package org.eclipse.update.internal.ui.views;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.UpdateManagerUtils;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.search.*;
import org.eclipse.update.internal.ui.security.UpdateManagerAuthenticator;
import org.eclipse.update.internal.ui.wizards.*;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public class UpdatesView
	extends BaseTreeView
	implements IUpdateModelChangedListener {
	private static final String KEY_NEW = "UpdatesView.Popup.new";
	private static final String KEY_NEW_SITE = "UpdatesView.Popup.newSite";
	private static final String KEY_NEW_FOLDER = "UpdatesView.Popup.newFolder";
	private static final String KEY_NEW_SEARCH = "UpdatesView.Popup.newSearch";
	private static final String KEY_NEW_LOCAL_SITE =
		"UpdatesView.Popup.newLocalSite";
	private static final String KEY_SHOW_SEARCH_RESULT =
		"UpdatesView.Popup.showSearchResult";
	private static final String KEY_DELETE = "UpdatesView.Popup.delete";
	private static final String KEY_OPEN_WEB = "UpdatesView.Popup.openWeb";
	private static final String KEY_CUT = "UpdatesView.Popup.cut";
	private static final String KEY_COPY = "UpdatesView.Popup.copy";
	private static final String KEY_PASTE = "UpdatesView.Popup.paste";
	private static final String KEY_REFRESH = "UpdatesView.Popup.refresh";
	private static final String KEY_REFRESH_TOOLTIP =
		"UpdatesView.Popup.refresh.tooltip";
	private static final String KEY_LINK_EXTENSION =
		"UpdatesView.Popup.linkExtension";
	private static final String KEY_FILTER_FILES = "UpdatesView.menu.showFiles";
	private static final String KEY_FILTER_ENVIRONMENT =
		"UpdatesView.menu.filterEnvironment";
	private static final String KEY_SHOW_CATEGORIES =
		"UpdatesView.menu.showCategories";
	private static final String KEY_NEW_SEARCH_TITLE =
		"UpdatesView.newSearch.title";
	private static final String KEY_NEW_BOOKMARK_TITLE =
		"UpdatesView.newBookmark.title";
	private static final String KEY_NEW_FOLDER_TITLE =
		"UpdatesView.newFolder.title";
	private static final String KEY_RESTART_TITLE = "UpdatesView.restart.title";
	private static final String KEY_RESTART_MESSAGE =
		"UpdatesView.restart.message";
	private static final String P_FILTER = "UpdatesView.matchingFilter";

	private Action propertiesAction;
	private Action newAction;
	private Action newFolderAction;
	private Action newSearchAction;
	private Action newLocalAction;
	private Action openWebAction;
	private Action cutAction;
	private Action copyAction;
	private Action pasteAction;
	private DeleteAction deleteAction;
	private Action fileFilterAction;
	private Action filterEnvironmentAction;
	private Action showCategoriesAction;
	private Action linkExtensionAction;
	private VolumeLabelProvider volumeLabelProvider;
	private Action refreshAction;
	private Action showSearchResultAction;
	private SearchObject updateSearchObject;
	private SelectionChangedListener selectionListener;
	private Hashtable fileImages = new Hashtable();
	private FileFilter fileFilter = new FileFilter();
	private EnvironmentFilter environmentFilter = new EnvironmentFilter();
	private Cursor handCursor;
	private Clipboard clipboard;
	private SearchMonitorManager searchMonitorManager;

	class DeleteAction extends Action implements IUpdate {
		public DeleteAction() {
			super("delete");
		}
		public void run() {
			performDelete();
		}
		public void update() {
			boolean enabled = true;
			IStructuredSelection sel =
				(IStructuredSelection) UpdatesView
					.this
					.getViewer()
					.getSelection();
			for (Iterator iter = sel.iterator(); iter.hasNext();) {
				Object obj = iter.next();
				if (obj instanceof NamedModelObject) {
					if (!(obj instanceof DiscoveryFolder))
						continue;
				}
				enabled = false;
				break;
			}
			setEnabled(enabled);
		}
	}

	class FileFilter extends ViewerFilter {
		public boolean select(Viewer viewer, Object parent, Object child) {
			if (child instanceof MyComputerFile) {
				return false;
			}
			return true;
		}
	}

	class EnvironmentFilter extends ViewerFilter {
		public boolean select(Viewer viewer, Object parent, Object child) {
			if (child instanceof IFeatureAdapter) {
				child = getFeature((IFeatureAdapter) child);
			}
			if (child instanceof IPlatformEnvironment) {
				return UpdateManagerUtils.isValidEnvironment(
					(IPlatformEnvironment) child);
			}
			return true;
		}
	}

	class SelectionChangedListener implements ISelectionChangedListener {
		public void selectionChanged(SelectionChangedEvent event) {
			updateForSelection((IStructuredSelection) event.getSelection());
		}
	}

	class UpdatesViewSorter extends FeatureSorter {
		public int category(Object obj) {
			// Level 0
			if (obj instanceof DiscoveryFolder)
				return 1;
			if (obj.equals(updateSearchObject))
				return 2;
			if (obj instanceof MyComputer)
				return 3;
			if (obj instanceof SiteBookmark
				|| obj instanceof BookmarkFolder
				|| obj instanceof SearchObject)
				return 4;
			return super.category(obj);
		}
	}

	class SiteProvider
		extends DefaultContentProvider
		implements ITreeContentProvider {
		public void inputChanged(
			Viewer viewer,
			Object oldInput,
			Object newInput) {
			if (newInput == null)
				return;
			updateTitle(newInput);
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof UpdateModel) {
				return getRootElements((UpdateModel) parent);
			}
			if (parent instanceof BookmarkFolder) {
				return ((BookmarkFolder) parent).getChildren(parent);
			}
			if (parent instanceof SiteBookmark) {
				return getSiteCatalog((SiteBookmark) parent);
			}
			/*
			if (parent instanceof SearchObject) {
				return ((SearchObject) parent).getChildren(null);
			}
			*/
			if (parent instanceof SearchResultSite) {
				return ((SearchResultSite) parent).getChildren(null);
			}
			if (parent instanceof MyComputer) {
				return ((MyComputer) parent).getChildren(parent);
			}
			if (parent instanceof MyComputerDirectory) {
				return ((MyComputerDirectory) parent).getChildren(parent);
			}
			if (parent instanceof SiteCategory) {
				final SiteCategory category = (SiteCategory) parent;
				category.touchFeatures(getViewSite().getWorkbenchWindow());
				return category.getChildren();
			}
			if (parent instanceof IFeatureAdapter) {
				return getIncludedFeatures((IFeatureAdapter) parent);
			}
			return new Object[0];
		}

		public Object getParent(Object child) {
			return null;
		}

		public boolean hasChildren(Object parent) {
			if (parent instanceof SiteBookmark) {
				SiteBookmark bookmark = (SiteBookmark) parent;
				return bookmark.isWebBookmark() == false;
			}
			if (parent instanceof MyComputer) {
				return true;
			}
			if (parent instanceof DiscoveryFolder) {
				return true;
			}
			if (parent instanceof BookmarkFolder) {
				return ((BookmarkFolder) parent).hasChildren();
			}
			/*
			if (parent instanceof SearchObject) {
				return ((SearchObject) parent).hasChildren();
			}
			*/
			if (parent instanceof MyComputerDirectory) {
				return ((MyComputerDirectory) parent).hasChildren(parent);
			}
			if (parent instanceof SiteCategory) {
				return ((SiteCategory) parent).getChildCount() > 0;
			}
			if (parent instanceof SearchResultSite) {
				return ((SearchResultSite) parent).getChildCount() > 0;
			}
			if (parent instanceof IFeatureAdapter) {
				return ((IFeatureAdapter) parent).hasIncludedFeatures(null);
			}
			return false;
		}

		public Object[] getElements(Object obj) {
			return getChildren(obj);
		}
	}

	class SiteLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			if (obj instanceof IFeature) {
				IFeature feature = (IFeature) obj;
				return feature.getLabel();
			}
			if (obj instanceof FeatureReferenceAdapter) {
				IFeatureReference feature =
					((FeatureReferenceAdapter) obj).getFeatureReference();
				VersionedIdentifier versionedIdentifier = null;
				try {
					versionedIdentifier =
						(feature != null)
							? feature.getVersionedIdentifier()
							: null;
				} catch (CoreException e) {
				}
				String version = "";
				if (versionedIdentifier != null)
					version = versionedIdentifier.getVersion().toString();
				String label = (feature != null) ? feature.getName() : "";
				return label + " " + version;
			}
			if (obj instanceof IFeatureAdapter) {
				IFeature feature = getFeature((IFeatureAdapter) obj);
				VersionedIdentifier versionedIdentifier = null;
				versionedIdentifier =
					(feature != null) ? feature.getVersionedIdentifier() : null;
				String version = "";
				if (versionedIdentifier != null)
					version = versionedIdentifier.getVersion().toString();
				String label = (feature != null) ? feature.getLabel() : "";
				return label + " " + version;
			}
			if (obj instanceof MyComputerDirectory) {
				MyComputerDirectory dir = (MyComputerDirectory) obj;
				IVolume volume = dir.getVolume();
				if (volume != null)
					return volumeLabelProvider.getText(volume);
				else
					return dir.getLabel(dir);
			}
			if (obj instanceof SearchObject) {
				return searchMonitorManager.getLabel((SearchObject) obj);
			}
			return super.getText(obj);
		}
		public Image getImage(Object obj) {
			UpdateLabelProvider provider =
				UpdateUI.getDefault().getLabelProvider();
			if (obj instanceof SiteBookmark) {
				SiteBookmark bookmark = (SiteBookmark) obj;
				return provider.get(
					bookmark.isWebBookmark()
						? UpdateUIImages.DESC_WEB_SITE_OBJ
						: UpdateUIImages.DESC_SITE_OBJ);
			}
			if (obj instanceof MyComputer) {
				return provider.get(UpdateUIImages.DESC_COMPUTER_OBJ);
			}
			if (obj instanceof DiscoveryFolder) {
				return provider.get(UpdateUIImages.DESC_PLACES_OBJ);
			}
			if (obj instanceof MyComputerDirectory) {
				IVolume volume = ((MyComputerDirectory) obj).getVolume();
				if (volume != null) {
					Image image = volumeLabelProvider.getImage(volume);
					if (image != null)
						return image;
				}
				return ((MyComputerDirectory) obj).getImage(obj);
			}
			if (obj instanceof ExtensionRoot) {
				return provider.get(UpdateUIImages.DESC_ESITE_OBJ);
			}
			if (obj instanceof MyComputerFile) {
				ImageDescriptor desc =
					((MyComputerFile) obj).getImageDescriptor(obj);
				return provider.get(desc);
			}
			if (obj instanceof SiteCategory) {
				return provider.get(UpdateUIImages.DESC_CATEGORY_OBJ);
			}
			if (obj instanceof BookmarkFolder) {
				return provider.get(UpdateUIImages.DESC_BFOLDER_OBJ);
			}
			if (obj instanceof SearchObject) {
				return getSearchObjectImage((SearchObject) obj);
			}

			if (obj instanceof IFeatureAdapter) {
				IFeatureAdapter adapter = (IFeatureAdapter) obj;
				obj = getFeature(adapter);
			}
			if (obj instanceof IFeature) {
				int flags = 0;
				if (obj instanceof MissingFeature)
					flags = UpdateLabelProvider.F_ERROR;
				boolean efix = false;
				if (flags == 0)
					efix = ((IFeature) obj).isPatch();
				return provider.get(
					efix
						? UpdateUIImages.DESC_EFIX_OBJ
						: UpdateUIImages.DESC_FEATURE_OBJ,
					flags);
			}
			return super.getImage(obj);
		}
		private Image getSearchObjectImage(SearchObject obj) {
			String categoryId = obj.getCategoryId();
			SearchCategoryDescriptor desc =
				SearchCategoryRegistryReader.getDefault().getDescriptor(
					categoryId);
			if (desc != null) {
				int flags =
					obj.isSearchInProgress()
						? UpdateLabelProvider.F_CURRENT
						: 0;
				return UpdateUI.getDefault().getLabelProvider().get(
					desc.getImageDescriptor(),
					flags);
			}
			return null;
		}
	}

	class UpdateTreeViewer extends TreeViewer {
		private Object lastElement;
		public UpdateTreeViewer(Composite parent, int styles) {
			super(parent, styles);
			/*
			getTree().addMouseMoveListener(new MouseMoveListener() {
				public void mouseMove(MouseEvent e) {
					processItem(getTree().getItem(new Point(e.x, e.y)));
				}
			});
			handCursor = new Cursor(getTree().getDisplay(), SWT.CURSOR_HAND);
			*/
		}
		private void processItem(Item item) {
			Object element = item != null ? item.getData() : null;
			if (element == lastElement)
				return;
			Cursor cursor = null;
			String tooltip = null;

			if (element instanceof SiteBookmark) {
				SiteBookmark bookmark = (SiteBookmark) element;
				if (bookmark.isWebBookmark()) {
					cursor = handCursor;
					tooltip = bookmark.getURL().toString();
				}
			}
			lastElement = element;
			getTree().setCursor(cursor);
			getTree().setToolTipText(tooltip);
		}
	}

	/**
	 * The constructor.
	 */
	public UpdatesView() {
		UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		model.addUpdateModelChangedListener(this);
		selectionListener = new SelectionChangedListener();
		updateSearchObject = new DefaultUpdatesSearchObject();
		UpdateUI.getDefault().getLabelProvider().connect(this);
		volumeLabelProvider = new VolumeLabelProvider();
	}

	public void dispose() {
		UpdateUI.getDefault().getLabelProvider().disconnect(this);
		volumeLabelProvider.dispose();
		UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		model.removeUpdateModelChangedListener(this);
		if (handCursor != null)
			handCursor.dispose();
		if (clipboard != null) {
			clipboard.dispose();
			clipboard = null;
		}
		searchMonitorManager.shutdown();
		super.dispose();
	}

	public void initProviders() {
		searchMonitorManager = new SearchMonitorManager();
		searchMonitorManager.register(updateSearchObject);
		getTreeViewer().setContentProvider(new SiteProvider());
		getTreeViewer().setLabelProvider(new SiteLabelProvider());
		getTreeViewer().setInput(UpdateUI.getDefault().getUpdateModel());
		WorkbenchHelp.setHelp(
			getControl(),
			"org.eclipse.update.ui.UpdatesView");
	}

	protected void initDragAndDrop() {
		clipboard = new Clipboard(getControl().getDisplay());
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers =
			new Transfer[] {
				UpdateModelDataTransfer.getInstance(),
				TextTransfer.getInstance()};
		getTreeViewer().addDragSupport(
			ops,
			transfers,
			new UpdatesDragAdapter((ISelectionProvider) getViewer()));
		getTreeViewer().addDropSupport(
			ops | DND.DROP_DEFAULT,
			transfers,
			new UpdatesDropAdapter(getTreeViewer()));
	}

	public void makeActions() {
		super.makeActions();
		initDrillDown();
		propertiesAction =
			new PropertyDialogAction(
				UpdateUI.getActiveWorkbenchShell(),
				getTreeViewer());
		newAction = new Action() {
			public void run() {
				performNewBookmark();
			}
		};
		WorkbenchHelp.setHelp(
			newAction,
			"org.eclipse.update.ui.UpdatesView_newAction");
		newAction.setText(UpdateUI.getString(KEY_NEW_SITE));

		newFolderAction = new Action("newFolder") {
			public void run() {
				performNewBookmarkFolder();
			}
		};
		WorkbenchHelp.setHelp(
			newFolderAction,
			"org.eclipse.update.ui.UpdatesView_newFolderAction");
		newFolderAction.setText(UpdateUI.getString(KEY_NEW_FOLDER));

		newSearchAction = new Action("newSearch") {
			public void run() {
				performNewSearch();
			}
		};
		WorkbenchHelp.setHelp(
			newSearchAction,
			"org.eclipse.update.ui.UpdatesView_newSearchAction");
		newSearchAction.setText(UpdateUI.getString(KEY_NEW_SEARCH));

		newLocalAction = new Action("newLocal") {
			public void run() {
				performNewLocal();
			}
		};

		WorkbenchHelp.setHelp(
			newLocalAction,
			"org.eclipse.update.ui.UpdatesView_newLocalAction");
		newLocalAction.setText(UpdateUI.getString(KEY_NEW_LOCAL_SITE));

		showSearchResultAction = new Action("showSearch") {
			public void run() {
				performShowSearchResult();
			}
		};
		WorkbenchHelp.setHelp(
			newLocalAction,
			"org.eclipse.update.ui.UpdatesView_showSearchResultAction");
		showSearchResultAction.setText(
			UpdateUI.getString(KEY_SHOW_SEARCH_RESULT));

		deleteAction = new DeleteAction();
		WorkbenchHelp.setHelp(
			deleteAction,
			"org.eclipse.update.ui.UpdatesView_deleteAction");
		deleteAction.setText(UpdateUI.getString(KEY_DELETE));

		openWebAction = new Action("openWeb") {
			public void run() {
				performOpenWeb();
			}
		};
		openWebAction.setText(UpdateUI.getString(KEY_OPEN_WEB));

		cutAction = new Action("cut") {
			public void run() {
				performCut();
			}
		};
		cutAction.setText(UpdateUI.getString(KEY_CUT));

		copyAction = new Action("copy") {
			public void run() {
				performCopy();
			}
		};
		copyAction.setText(UpdateUI.getString(KEY_COPY));

		pasteAction = new Action("paste") {
			public void run() {
				performPaste();
			}
		};
		pasteAction.setText(UpdateUI.getString(KEY_PASTE));

		refreshAction = new Action("refresh") {
			public void run() {
				performRefresh();
			}
		};
		WorkbenchHelp.setHelp(
			refreshAction,
			"org.eclipse.update.ui.UpdatesView_refreshAction");
		refreshAction.setText(UpdateUI.getString(KEY_REFRESH));
		refreshAction.setToolTipText(UpdateUI.getString(KEY_REFRESH_TOOLTIP));
		refreshAction.setImageDescriptor(UpdateUIImages.DESC_REFRESH_NAV);
		refreshAction.setDisabledImageDescriptor(
			UpdateUIImages.DESC_REFRESH_NAV_D);
		refreshAction.setHoverImageDescriptor(
			UpdateUIImages.DESC_REFRESH_NAV_H);

		fileFilterAction = new Action() {
			public void run() {
				if (fileFilterAction.isChecked()) {
					getTreeViewer().removeFilter(fileFilter);
				} else
					getTreeViewer().addFilter(fileFilter);
			}
		};
		WorkbenchHelp.setHelp(
			fileFilterAction,
			"org.eclipse.update.ui.UpdatesView_fileFilterAction");
		fileFilterAction.setText(UpdateUI.getString(KEY_FILTER_FILES));
		fileFilterAction.setChecked(false);

		getTreeViewer().addFilter(fileFilter);

		filterEnvironmentAction = new Action() {
			public void run() {
				boolean checked = filterEnvironmentAction.isChecked();
				if (checked) {
					getTreeViewer().addFilter(environmentFilter);
				} else
					getTreeViewer().removeFilter(environmentFilter);
				setStoredEnvironmentValue(checked);
			}
		};
		WorkbenchHelp.setHelp(
			filterEnvironmentAction,
			"org.eclipse.update.ui.UpdatesView_filterEnvironmentAction");
		filterEnvironmentAction.setText(
			UpdateUI.getString(KEY_FILTER_ENVIRONMENT));
		boolean envValue = getStoredEnvironmentValue();
		filterEnvironmentAction.setChecked(envValue);

		getTreeViewer().addFilter(environmentFilter);
		getTreeViewer().setSorter(new UpdatesViewSorter());

		showCategoriesAction = new Action() {
			public void run() {
				showCategories(!showCategoriesAction.isChecked());
			}
		};
		WorkbenchHelp.setHelp(
			showCategoriesAction,
			"org.eclipse.update.ui.UpdatesView_showCategoriesAction");
		showCategoriesAction.setText(UpdateUI.getString(KEY_SHOW_CATEGORIES));
		showCategoriesAction.setChecked(true);

		linkExtensionAction = new Action("link") {
			public void run() {
				linkProductExtension();
			}
		};
		WorkbenchHelp.setHelp(
			linkExtensionAction,
			"org.eclipse.update.ui.UpdatesView_linkExtensionAction");
		linkExtensionAction.setText(UpdateUI.getString(KEY_LINK_EXTENSION));

		getTreeViewer().addSelectionChangedListener(selectionListener);
		hookGlobalActions();
	}

	private void hookGlobalActions() {
		IViewSite site = getViewSite();
		IActionBars bars = site.getActionBars();
		bars.setGlobalActionHandler(IWorkbenchActionConstants.CUT, cutAction);
		bars.setGlobalActionHandler(IWorkbenchActionConstants.COPY, copyAction);
		bars.setGlobalActionHandler(
			IWorkbenchActionConstants.PASTE,
			pasteAction);
	}

	private boolean getStoredEnvironmentValue() {
		IDialogSettings settings = UpdateUI.getDefault().getDialogSettings();
		return !settings.getBoolean(P_FILTER);
	}

	private void setStoredEnvironmentValue(boolean value) {
		IDialogSettings settings = UpdateUI.getDefault().getDialogSettings();
		settings.put(P_FILTER, !value);
	}

	private void updateForSelection(IStructuredSelection selection) {
		refreshAction.setEnabled(selection.size() == 1);
	}

	public void fillActionBars(IActionBars bars) {
		IToolBarManager toolBarManager = bars.getToolBarManager();
		toolBarManager.add(refreshAction);
		toolBarManager.add(new Separator());
		addDrillDownAdapter(bars);
		toolBarManager.add(new Separator());
		toolBarManager.add(collapseAllAction);
		IMenuManager menuManager = bars.getMenuManager();
		menuManager.add(fileFilterAction);
		menuManager.add(new Separator());
		menuManager.add(showCategoriesAction);
		menuManager.add(filterEnvironmentAction);
		bars.setGlobalActionHandler(
			IWorkbenchActionConstants.DELETE,
			deleteAction);
	}

	public void fillContextMenu(IMenuManager manager) {
		Object obj = getSelectedObject();
		if (obj instanceof SiteBookmark) {
			SiteBookmark site = (SiteBookmark) obj;
			if (site.isWebBookmark())
				manager.add(openWebAction);
		}
		manager.add(refreshAction);
		manager.add(new Separator());
		MenuManager newMenu = new MenuManager(UpdateUI.getString(KEY_NEW));
		newMenu.add(newAction);
		newMenu.add(newFolderAction);
		newMenu.add(newSearchAction);
		manager.add(newMenu);
		if (obj instanceof SiteBookmark) {
			SiteBookmark site = (SiteBookmark) obj;
			if (site.getType() == SiteBookmark.LOCAL)
				manager.add(newLocalAction);
		}
		if (obj instanceof ExtensionRoot) {
			manager.add(linkExtensionAction);
		}
		manager.add(new Separator());
		cutAction.setEnabled(canCopy());
		copyAction.setEnabled(cutAction.isEnabled());
		pasteAction.setEnabled(canPaste());
		deleteAction.setEnabled(canDelete());
		manager.add(cutAction);
		manager.add(copyAction);
		manager.add(pasteAction);
		manager.add(deleteAction);
		manager.add(new Separator());
		if (obj instanceof SearchObject) {
			manager.add(showSearchResultAction);
			manager.add(new Separator());
		}
		addDrillDownAdapter(manager);
		manager.add(new Separator());

		super.fillContextMenu(manager);
		if (obj instanceof NamedModelObject)
			manager.add(propertiesAction);
	}

	private boolean canDelete() {
		IStructuredSelection sel =
			(IStructuredSelection) getTreeViewer().getSelection();
		if (sel.isEmpty())
			return false;
		for (Iterator iter = sel.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (!canDelete(obj))
				return false;
		}
		return true;
	}

	private boolean canDelete(Object obj) {
		if (obj instanceof SiteBookmark) {
			SiteBookmark site = (SiteBookmark) obj;
			return (site.getType() != SiteBookmark.LOCAL);
		}
		if (obj instanceof BookmarkFolder
			&& !(obj instanceof DiscoveryFolder)) {
			return true;
		}
		if (obj instanceof SearchObject
			&& !(obj instanceof DefaultUpdatesSearchObject)) {
			return true;
		}
		return false;
	}

	private Object getSelectedObject() {
		IStructuredSelection sel =
			(IStructuredSelection) getViewer().getSelection();
		if (sel.isEmpty() || sel.size() > 1)
			return null;
		return sel.getFirstElement();
	}

	private void performNewBookmark() {
		//UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		Shell shell = UpdateUI.getActiveWorkbenchShell();
		NewSiteBookmarkWizardPage page =
			new NewSiteBookmarkWizardPage(getSelectedFolder());
		NewWizard wizard =
			new NewWizard(page, UpdateUIImages.DESC_NEW_BOOKMARK);
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.create();
		dialog.getShell().setText(UpdateUI.getString(KEY_NEW_BOOKMARK_TITLE));
		//dialog.getShell().setSize(400, 400);
		dialog.open();
	}

	private BookmarkFolder getSelectedFolder() {
		Object sel = getSelectedObject();
		if (sel instanceof BookmarkFolder
			&& !(sel instanceof DiscoveryFolder)) {
			BookmarkFolder folder = (BookmarkFolder) sel;
			return folder;
		}
		return null;
	}

	private void performNewBookmarkFolder() {
		//UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		Shell shell = UpdateUI.getActiveWorkbenchShell();
		NewFolderWizardPage page = new NewFolderWizardPage(getSelectedFolder());
		NewWizard wizard = new NewWizard(page, UpdateUIImages.DESC_NEW_FOLDER);
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.create();
		dialog.getShell().setText(UpdateUI.getString(KEY_NEW_FOLDER_TITLE));
		//dialog.getShell().setSize(400, 350);
		dialog.open();
	}

	private void performNewSearch() {
		//UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		Shell shell = UpdateUI.getActiveWorkbenchShell();
		NewSearchWizardPage page = new NewSearchWizardPage(getSelectedFolder());
		NewWizard wizard = new NewWizard(page, UpdateUIImages.DESC_NEW_SEARCH);
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.create();
		dialog.getShell().setText(UpdateUI.getString(KEY_NEW_SEARCH_TITLE));
		//dialog.getShell().setSize(400, 350);
		dialog.open();
	}

	private void performNewLocal() {
		ISelection selection = getViewer().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			Object obj = ssel.getFirstElement();
			if (obj instanceof SiteBookmark) {
				SiteBookmark bookmark = (SiteBookmark) obj;
				if (bookmark.getType() == SiteBookmark.LOCAL) {
					//UpdateModel model =
					//	UpdateUI.getDefault().getUpdateModel();
					Shell shell = UpdateUI.getActiveWorkbenchShell();
					NewSiteBookmarkWizardPage page =
						new NewSiteBookmarkWizardPage(
							getSelectedFolder(),
							bookmark);
					NewWizard wizard =
						new NewWizard(page, UpdateUIImages.DESC_NEW_BOOKMARK);
					WizardDialog dialog = new WizardDialog(shell, wizard);
					dialog.create();
					dialog.getShell().setText(
						UpdateUI.getString(KEY_NEW_BOOKMARK_TITLE));
					dialog.open();
				}
			}
		}
	}

	private void performDelete() {
		ISelection selection = getViewer().getSelection();
		if (selection instanceof IStructuredSelection) {
			if (!confirmDeletion())
				return;
			doDelete((IStructuredSelection) selection);
		}
	}

	private void doDelete(IStructuredSelection selection) {
		UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (obj instanceof NamedModelObject) {
				NamedModelObject child = (NamedModelObject) obj;
				BookmarkFolder folder = (BookmarkFolder) child.getParent(child);
				if (folder != null)
					folder.removeChildren(new NamedModelObject[] { child });
				else
					model.removeBookmark(child);
			}
		}
	}

	private void performOpenWeb() {
		SiteBookmark bookmark = (SiteBookmark) getSelectedObject();
		URL url = bookmark.getURL();
		if (url != null) {
			DetailsView.showURL(url.toString());
		}
	}

	protected void handleDoubleClick(DoubleClickEvent e) {
		Object obj = getSelectedObject();
		if (obj != null && obj instanceof SiteBookmark) {
			SiteBookmark bookmark = (SiteBookmark) obj;
			if (bookmark.isWebBookmark()) {
				performOpenWeb();
				return;
			}
		}
		super.handleDoubleClick(e);
	}

	private void performCut() {
		if (!canDelete())
			return;
		if (!performCopy())
			return;
		doDelete((IStructuredSelection) getViewer().getSelection());
	}

	private boolean canCopy() {
		IStructuredSelection selection =
			(IStructuredSelection) getViewer().getSelection();
		return UpdatesDragAdapter.canCopy(selection);
	}

	private boolean performCopy() {
		IStructuredSelection selection =
			(IStructuredSelection) getViewer().getSelection();
		if (!UpdatesDragAdapter.canCopy(selection))
			return false;
		NamedModelObject[] objects =
			UpdatesDragAdapter.createObjectRepresentation(selection);
		String text = UpdatesDragAdapter.createTextualRepresentation(selection);
		return setClipboardContent(objects, text);
	}

	private boolean setClipboardContent(
		NamedModelObject[] objects,
		String names) {
		try {
			// set the clipboard contents
			clipboard.setContents(
				new Object[] { objects, names },
				new Transfer[] {
					UpdateModelDataTransfer.getInstance(),
					TextTransfer.getInstance()});
			return true;
		} catch (SWTError e) {
			UpdateUI.logException(e);
			return false;
		}
	}

	private boolean canPaste() {
		// try a data transfer
		UpdateModelDataTransfer dataTransfer =
			UpdateModelDataTransfer.getInstance();
		return clipboard.getContents(dataTransfer) != null;
	}

	private void performPaste() {
		// try a data transfer
		UpdateModelDataTransfer dataTransfer =
			UpdateModelDataTransfer.getInstance();
		Object[] objects = (Object[]) clipboard.getContents(dataTransfer);

		if (objects != null) {
			BookmarkFolder parentFolder =
				(BookmarkFolder) UpdatesDropAdapter.getRealTarget(
					getSelectedObject());
			for (int i = 0; i < objects.length; i++) {
				NamedModelObject object = (NamedModelObject) objects[i];
				if (!UpdatesDropAdapter
					.addToModel(getControl().getShell(), parentFolder, object))
					return;
			}
			return;
		}
		// try a text transfer
	}

	private void performShowSearchResult() {
		IWorkbenchPage page = UpdateUI.getActivePage();
		SearchResultView view =
			(SearchResultView) page.findView(
				UpdatePerspective.ID_SEARCH_RESULTS);
		if (view != null) {
			page.bringToTop(view);
		} else {
			try {
				view =
					(SearchResultView) page.showView(
						UpdatePerspective.ID_SEARCH_RESULTS);
				view.setSelectionActive(true);
			} catch (PartInitException e) {
				UpdateUI.logException(e);
			}
		}
		if (view != null)
			view.setCurrentSearch((SearchObject) getSelectedObject());
	}

	private void performRefresh() {
		IStructuredSelection sel =
			(IStructuredSelection) getViewer().getSelection();
		final Object obj = sel.getFirstElement();

		if (obj != null) {
			/*
			BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
				public void run() {
					try {
						// reinitialize the authenticator  
						UpdateManagerAuthenticator auth =
							UpdateUI.getDefault().getAuthenticator();
						if (auth != null)
							auth.reset();
						if (obj instanceof SiteBookmark)
							 ((SiteBookmark) obj).connect(false, null);
						getViewer().refresh(obj);
					} catch (CoreException e) {
						UpdateUI.logException(e);
					}
				}
			});
			*/
			IRunnableWithProgress op = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
					throws InvocationTargetException {
					try {
						monitor.beginTask("", 3);
						// reinitialize the authenticator  
						UpdateManagerAuthenticator auth =
							UpdateUI.getDefault().getAuthenticator();
						if (auth != null)
							auth.reset();
						monitor.worked(1);
						if (obj instanceof SiteBookmark) {
							SiteBookmark bookmark = (SiteBookmark) obj;
							if (bookmark.isWebBookmark())
								monitor.worked(1);
							else
								bookmark.connect(
									false,
									new SubProgressMonitor(monitor, 1));
						} else
							monitor.worked(1);
						monitor.setTaskName(
							UpdateUI.getString("UpdatesView.updating"));
						getControl().getDisplay().syncExec(new Runnable() {
							public void run() {
								getViewer().refresh(obj);
							}
						});
						monitor.worked(1);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			};
			try {
				getViewSite().getWorkbenchWindow().run(true, true, op);
			} catch (InvocationTargetException e) {
				UpdateUI.logException(e);
			} catch (InterruptedException e) {
			}
		}
	}

	private void linkProductExtension() {
		Object obj = getSelectedObject();
		ExtensionRoot extension = (ExtensionRoot) obj;
		File dir = extension.getInstallableDirectory();
		try {
			IInstallConfiguration config =
				InstallWizard.createInstallConfiguration();
			if (TargetPage
				.addConfiguredSite(getControl().getShell(), config, dir, true)
				!= null) {
				InstallWizard.makeConfigurationCurrent(config, null);
				InstallWizard.saveLocalSite();
				UpdateUI.informRestartNeeded();
			}
		} catch (CoreException e) {
			UpdateUI.logException(e);
		}
	}

	private Object[] getRootElements(UpdateModel model) {
		NamedModelObject[] bookmarks = model.getBookmarks();
		Object[] array = new Object[3 + bookmarks.length];

		array[0] = new DiscoveryFolder();
		array[1] = updateSearchObject;
		array[2] = new MyComputer();
		for (int i = 3; i < array.length; i++) {
			array[i] = bookmarks[i - 3];
		}
		return array;
	}
	public void selectUpdateObject() {
		getViewer().setSelection(
			new StructuredSelection(updateSearchObject),
			true);
	}

	class CatalogBag {
		Object[] catalog;
	}

	private Object[] getSiteCatalog(final SiteBookmark bookmark) {
		if (bookmark.isWebBookmark())
			return new Object[0];
		Object[] result =
			getSiteCatalogWithIndicator(bookmark, !bookmark.isSiteConnected());
		if (result != null)
			return result;
		else
			return new Object[0];
	}

	private Object[] getSiteCatalogWithIndicator(
		final SiteBookmark bookmark,
		final boolean connect) {
		final CatalogBag bag = new CatalogBag();

		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
				throws InvocationTargetException {
				try {
					monitor.beginTask(
						UpdateUI.getString("UpdatesView.connecting"),
						3);
					monitor.worked(1);

					if (connect)
						bookmark.connect(new SubProgressMonitor(monitor, 1));
					else
						monitor.worked(1);
					bag.catalog =
						bookmark.getCatalog(
							showCategoriesAction.isChecked(),
							new SubProgressMonitor(monitor, 1));
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getViewSite().getWorkbenchWindow().run(true, true, op);
		} catch (InvocationTargetException e) {
			UpdateUI.logException(e);
		} catch (InterruptedException e) {
		}
		return bag.catalog;
	}

	private void showCategories(boolean show) {
		Object[] expanded = getTreeViewer().getExpandedElements();
		for (int i = 0; i < expanded.length; i++) {
			if (expanded[i] instanceof SiteBookmark) {
				getViewer().refresh(expanded[i]);
			}
		}
	}

	protected void handleSelectionChanged(SelectionChangedEvent e) {
		deleteAction.update();
	}

	protected void deleteKeyPressed(Widget widget) {
		if (canDelete())
			deleteAction.run();
	}

	public void objectsAdded(Object parent, Object[] children) {
		Object child = children[0];
		if (child instanceof PendingChange)
			return;
		if (child instanceof NamedModelObject) {
			UpdateModel model = UpdateUI.getDefault().getUpdateModel();
			if (child instanceof SearchObject) {
				searchMonitorManager.register((SearchObject) child);
			}
			if (parent == null)
				parent = model;
			getTreeViewer().add(parent, children);
			if (parent != model)
				getTreeViewer().setExpandedState(parent, true);
			getViewer().setSelection(new StructuredSelection(children), true);
		}
	}

	public void objectsRemoved(Object parent, Object[] children) {
		if (children[0] instanceof PendingChange)
			return;
		if (children[0] instanceof NamedModelObject) {
			getTreeViewer().remove(children);
			getTreeViewer().setSelection(new StructuredSelection());
			for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof SearchObject)
					searchMonitorManager.unregister((SearchObject) children[i]);
			}
		}
	}

	public void objectChanged(Object object, String property) {
		if (object instanceof NamedModelObject) {
			if (property.equals(NamedModelObject.P_NAME)) {
				getTreeViewer().update(object, null);
			}
			if (object instanceof SiteBookmark) {
				if (property.equals(SiteBookmark.P_URL)) {
					getTreeViewer().refresh(object);
				}
			}
			//viewer.setSelection(viewer.getSelection());
		}
	}

	public void setSelection(IStructuredSelection selection) {
		getViewer().setSelection(selection, true);
	}

	private void disposeImages() {
		volumeLabelProvider.dispose();
	}

	private IFeature getFeature(final IFeatureAdapter adapter) {
		final IFeature[] result = new IFeature[1];

		try {
			result[0] = adapter.getFeature(null);
		} catch (CoreException e) {
			result[0] = new MissingFeature(adapter.getSite(), adapter.getURL());
		}
		return result[0];
	}

	private Object[] getIncludedFeatures(IFeatureAdapter adapter) {
		if (adapter instanceof FeatureReferenceAdapter) {
			((FeatureReferenceAdapter) adapter).touchIncludedFeatures(
				getViewSite().getWorkbenchWindow());
		}
		return adapter.getIncludedFeatures(null);
	}

	protected Object getRootObject() {
		return UpdateUI.getDefault().getUpdateModel();
	}
}