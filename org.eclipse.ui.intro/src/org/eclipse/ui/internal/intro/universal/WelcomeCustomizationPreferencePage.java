/***************************************************************************************************
 * Copyright (c) 2006 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.ui.internal.intro.universal;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.intro.impl.IntroPlugin;
import org.eclipse.ui.internal.intro.impl.Messages;
import org.eclipse.ui.internal.intro.impl.model.IntroModelRoot;
import org.eclipse.ui.internal.intro.impl.model.util.BundleUtil;
import org.eclipse.ui.internal.intro.impl.util.ImageUtil;
import org.eclipse.ui.internal.intro.impl.util.Log;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.config.CustomizableIntroPart;
import org.eclipse.ui.intro.config.IIntroURL;
import org.eclipse.ui.intro.config.IntroURLFactory;
import org.osgi.framework.Bundle;


public class WelcomeCustomizationPreferencePage extends PreferencePage implements IWorkbenchPreferencePage,
		IExecutableExtension {

	private static final String INTRO_ROOT_PAGES = "INTRO_ROOT_PAGES"; //$NON-NLS-1$
	private static final String INTRO_DATA = "INTRO_DATA"; //$NON-NLS-1$
	private static final String INTRO_BACKGROUND_IMAGE = "INTRO_BACKGROUND_IMAGE"; //$NON-NLS-1$
	private static final String INTRO_BACKGROUND_IMAGE_LIST = "INTRO_BACKGROUND_IMAGE_LIST";//$NON-NLS-1$
	private TabFolder tabFolder;
	private String firstPageId;
	private Composite pageContainer;
	private TableViewer backgrounds;
	private TableViewer available;
	private TableViewer left;
	private TableViewer right;
	private TableViewer bottom;
	private CheckboxTableViewer rootPages;
	private ArrayList introRootPages = new ArrayList();
	private ArrayList backgroundImageList = new ArrayList();
	private IntroBackground introBackground;
	private String introBackgroundName;
	private IntroData introData;
	private Canvas bgPreview;
	private TableContentProvider contentProvider;
	private TableLabelProvider labelProvider;
	private Button applyToAll;
	private Image extensionImage;
	private Image bgImage;
	private Image ihighImage;
	private Image ilowImage;
	private Image inewImage;
	private Image icalloutImage;
	private static final Transfer[] TRANSFER_TYPES = new Transfer[] { ExtensionDataTransfer.getInstance() };


	private static final RootPage ROOT_PAGE_TABLE[] = new RootPage[] {
			new RootPage(ISharedIntroConstants.ID_OVERVIEW,
					Messages.WelcomeCustomizationPreferencePage_overview),
			new RootPage(ISharedIntroConstants.ID_FIRSTSTEPS,
					Messages.WelcomeCustomizationPreferencePage_firststeps),
			new RootPage(ISharedIntroConstants.ID_TUTORIALS,
					Messages.WelcomeCustomizationPreferencePage_tutorials),
			new RootPage(ISharedIntroConstants.ID_SAMPLES,
					Messages.WelcomeCustomizationPreferencePage_samples),
			new RootPage(ISharedIntroConstants.ID_WHATSNEW,
					Messages.WelcomeCustomizationPreferencePage_whatsnew),
			new RootPage(ISharedIntroConstants.ID_WEBRESOURCES,
					Messages.WelcomeCustomizationPreferencePage_webresources),
			new RootPage(ISharedIntroConstants.ID_MIGRATE,
					Messages.WelcomeCustomizationPreferencePage_migrate) };

	static class RootPage {

		public String id;
		public String name;

		public RootPage(String id, String name) {
			this.id = id;
			this.name = name;
		}

		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj == null)
				return false;
			if (obj instanceof RootPage) {
				RootPage page = (RootPage) obj;
				return page.id.equals(id) && page.name.equals(name);
			}
			return false;
		}

		public String toString() {
			return name;
		}
	}

	class TableContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			if (inputElement == ROOT_PAGE_TABLE)
				return ROOT_PAGE_TABLE;
			if (inputElement instanceof GroupData) {
				return ((GroupData) inputElement).getExtensions();
			}
			if (inputElement == backgrounds) {
				return backgroundImageList.toArray();
			}
			return new Object[0];
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	class TableDragSourceListener implements DragSourceListener {

		TableViewer viewer;
		ExtensionData[] sel;

		public TableDragSourceListener(TableViewer viewer) {
			this.viewer = viewer;
		}

		public void dragStart(DragSourceEvent event) {
			IStructuredSelection ssel = (IStructuredSelection) viewer.getSelection();
			if (ssel.size() > 0) {
				event.doit = true;
			} else {
				event.doit = false;
			}
		};

		public void dragSetData(DragSourceEvent event) {
			IStructuredSelection ssel = (IStructuredSelection) viewer.getSelection();
			ExtensionData[] array = new ExtensionData[ssel.size()];
			int i = 0;
			for (Iterator iter = ssel.iterator(); iter.hasNext();) {
				array[i++] = (ExtensionData) iter.next();
			}
			event.data = array;
			sel = array;
		}

		public void dragFinished(DragSourceEvent event) {
			if (event.detail == DND.DROP_MOVE) {
				GroupData gd = (GroupData) viewer.getInput();
				for (int i = 0; i < sel.length; i++) {
					ExtensionData ed = sel[i];
					gd.remove(ed);
				}
				viewer.refresh();
				updateColumnSizes(viewer);
			}
			sel = null;
		}
	}

	class TableDropTargetListener extends ViewerDropAdapter {

		public TableDropTargetListener(TableViewer viewer) {
			super(viewer);
		}

		public boolean performDrop(Object data) {
			ExtensionData target = (ExtensionData) getCurrentTarget();
			int loc = getCurrentLocation();
			GroupData gd = (GroupData) getViewer().getInput();
			if (gd == null)
				gd = createTargetGd(getViewer());
			ExtensionData[] sel = (ExtensionData[]) data;

			int index = target != null ? gd.getIndexOf(target) : -1;
			int startingIndex = getStartIndex(gd, sel);
			if (target != null) {
				if (loc == LOCATION_AFTER
						|| (loc == LOCATION_ON && startingIndex != -1 && startingIndex < index))
					index++;
				else if (index > 0 && loc == LOCATION_BEFORE)
					index--;
			}

			for (int i = 0; i < sel.length; i++) {
				ExtensionData ed = sel[i];
				if (index == -1)
					gd.add(ed);
				else
					gd.add(index++, ed);
			}
			if (getViewer().getInput() != null)
				getViewer().refresh();
			else
				getViewer().setInput(gd);
			updateColumnSizes((TableViewer) getViewer());
			return true;
		}

		private int getStartIndex(GroupData gd, ExtensionData[] sel) {
			for (int i = 0; i < sel.length; i++) {
				ExtensionData ed = sel[i];
				int index = gd.getIndexOf(ed.getId());
				if (index != -1)
					return index;
			}
			return -1;
		}

		public boolean validateDrop(Object target, int operation, TransferData transferType) {
			return ExtensionDataTransfer.getInstance().isSupportedType(transferType);
		}
	}

	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

		public String getText(Object obj) {
			if (obj instanceof RootPage) {
				return ((RootPage) obj).name;
			}
			if (obj instanceof ExtensionData) {
				ExtensionData ed = (ExtensionData) obj;
				String name = ed.getName();
				if (name != null && name.length() > 0)
					return name;
				return ed.getId();
			}
			if (obj instanceof IntroBackground) {
				IntroBackground bg = (IntroBackground) obj;
				return bg.getName();
			}
			return super.getText(obj);
		}

		public Image getImage(Object obj) {
			if (obj instanceof ExtensionData) {
				ExtensionData ed = (ExtensionData) obj;
				switch (ed.getImportance()) {
				case ExtensionData.HIGH:
					return ihighImage;
				case ExtensionData.LOW:
					return ilowImage;
				case ExtensionData.NEW:
					return inewImage;
				case ExtensionData.CALLOUT:
					return icalloutImage;
				}
				return extensionImage;
			}
			if (obj instanceof IntroBackground)
				return bgImage;
			return null;
		}

		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0)
				return getImage(element);
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (columnIndex == 1 || element instanceof IntroBackground || element instanceof RootPage)
				return getText(element);
			return null;
		}
	}

	class IntroBackground {

		static final int ABSOLUTE = 0;
		static final int INTRO = 1;
		static final int PRODUCT = 2;
		int kind = ABSOLUTE;
		String fullName;
		String path;
		Image image;

		public void dispose() {
			if (image != null) {
				image.dispose();
				image = null;
			}
		}

		public String getFullName() {
			return fullName;
		}

		public String getName() {
			IPath ipath = new Path(path);
			return ipath.lastSegment();
		}

		public IntroBackground(String fullName) {
			this.fullName = fullName;
			if (fullName.startsWith("intro:")) { //$NON-NLS-1$
				kind = INTRO;
				path = fullName.substring(6);
			} else if (fullName.startsWith("product:")) { //$NON-NLS-1$
				kind = PRODUCT;
				path = fullName.substring(8);
			} else
				path = fullName;
		}

		public Image getImage() {
			if (image == null) {
				BusyIndicator.showWhile(bgPreview.getDisplay(), new Runnable() {

					public void run() {
						String asLocal = null;
						if (kind != ABSOLUTE) {
							Bundle bundle = null;
							if (kind == PRODUCT) {
								IProduct product = Platform.getProduct();
								bundle = product.getDefiningBundle();
							} else {
								bundle = IntroPlugin.getDefault().getBundle();
							}
							asLocal = BundleUtil.getResolvedResourceLocation(path, bundle);
						} else
							asLocal = "file:" + path; //$NON-NLS-1$
						try {
							ImageDescriptor desc = ImageDescriptor.createFromURL(new URL(asLocal));
							image = desc.createImage();
						} catch (MalformedURLException e) {
						}
					}
				});
			}
			return image;
		}
	}

	public WelcomeCustomizationPreferencePage() {
	}

	public WelcomeCustomizationPreferencePage(String title) {
		super(title);
	}

	public WelcomeCustomizationPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		container.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);
		tabFolder = new TabFolder(container, SWT.TOP);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		tabFolder.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				TabItem[] selection = tabFolder.getSelection();
				onTabChange(selection[0]);
			}
		});
		applyToAll = new Button(container, SWT.CHECK);
		applyToAll.setText(Messages.WelcomeCustomizationPreferencePage_applyToAll);
		contentProvider = new TableContentProvider();
		labelProvider = new TableLabelProvider();
		if (isCustomizationMode()) {
			Button serialize = new Button(container, SWT.PUSH);
			serialize.setText(Messages.WelcomeCustomizationPreferencePage_serialize);
			serialize.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					doSerializeState();
				}
			});
		}
		extensionImage = ImageUtil.createImage("full/obj16/extension_obj.gif"); //$NON-NLS-1$
		ihighImage = ImageUtil.createImage("full/obj16/ihigh_obj.gif"); //$NON-NLS-1$
		ilowImage = ImageUtil.createImage("full/obj16/ilow_obj.gif"); //$NON-NLS-1$
		inewImage = ImageUtil.createImage("full/obj16/inew_obj.gif"); //$NON-NLS-1$
		icalloutImage = ImageUtil.createImage("full/obj16/icallout_obj.gif"); //$NON-NLS-1$
		bgImage = ImageUtil.createImage("full/obj16/image_obj.gif"); //$NON-NLS-1$
		addPages();
		org.eclipse.jface.dialogs.Dialog.applyDialogFont(container);
		return container;
	}

	private void doSerializeState() {
		FileDialog fd = new FileDialog(getShell(), SWT.SAVE);
		fd.setText(Messages.WelcomeCustomizationPreferencePage_serializeTitle);
		String fileName = fd.open();
		if (fileName != null) {
			try {
				FileWriter writer = new FileWriter(fileName);
				PrintWriter pwriter = new PrintWriter(writer);
				introData.write(pwriter);
				pwriter.close();
			} catch (IOException e) {
				// show an error dialog in addition
				Log.error("Error while saving the intro data file", e); //$NON-NLS-1$
			}
		}
	}

	private boolean isCustomizationMode() {
		/*
		 * String[] args = Platform.getApplicationArgs(); for (int i = 0; i < args.length; i++) { if
		 * (args[i].equalsIgnoreCase("-welcomeCustomization")) //$NON-NLS-1$ return true; } return
		 * false;
		 */
		return true;
	}

	public void init(IWorkbench workbench) {
	}

	private void addPages() {
		loadData(false);
		addHomePage();
		createPageContainer();
		addRootPages();
		updateWidgetsFromData();
		selectFirstPage();
	}

	private void addRootPages() {
		addPage(ISharedIntroConstants.ID_OVERVIEW);
		addPage(ISharedIntroConstants.ID_FIRSTSTEPS);
		addPage(ISharedIntroConstants.ID_TUTORIALS);
		addPage(ISharedIntroConstants.ID_SAMPLES);
		addPage(ISharedIntroConstants.ID_WHATSNEW);
		addPage(ISharedIntroConstants.ID_WEBRESOURCES);
		addPage(ISharedIntroConstants.ID_MIGRATE);
	}

	private void createPageContainer() {
		pageContainer = new Composite(tabFolder, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 10;
		pageContainer.setLayout(layout);
		layout.numColumns = 4;
		Label label;
		label = new Label(pageContainer, SWT.WRAP);
		label.setText(Messages.WelcomeCustomizationPreferencePage_pageDesc);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 4;
		label.setLayoutData(gd);
		label = new Label(pageContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 4;
		label.setLayoutData(gd);
		label = new Label(pageContainer, SWT.NULL);
		label.setText(Messages.WelcomeCustomizationPreferencePage_available);
		gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		label = new Label(pageContainer, SWT.NULL);
		label.setText(Messages.WelcomeCustomizationPreferencePage_left);
		label = new Label(pageContainer, SWT.NULL);
		label.setText(Messages.WelcomeCustomizationPreferencePage_right);
		available = createTableViewer(pageContainer, "hidden"); //$NON-NLS-1$
		available.setSorter(new ViewerSorter());
		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
		gd.verticalSpan = 3;
		gd.widthHint = 100;
		gd.horizontalSpan = 2;
		available.getControl().setLayoutData(gd);

		left = createTableViewer(pageContainer, "left"); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_BOTH);
		left.getControl().setLayoutData(gd);

		right = createTableViewer(pageContainer, "right"); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_BOTH);
		right.getControl().setLayoutData(gd);

		label = new Label(pageContainer, SWT.NULL);
		label.setText(Messages.WelcomeCustomizationPreferencePage_bottom);
		bottom = createTableViewer(pageContainer, "bottom"); //$NON-NLS-1$
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		gd.horizontalSpan = 2;
		bottom.getControl().setLayoutData(gd);
	}

	private void updatePageContainer(String pageId, PageData pd) {
		if (pageId == null || pd == null)
			return;
		left.setInput(pd.findGroup(ISharedIntroConstants.DIV_LAYOUT_LEFT));
		right.setInput(pd.findGroup(ISharedIntroConstants.DIV_LAYOUT_RIGHT));
		bottom.setInput(pd.findGroup(ISharedIntroConstants.DIV_LAYOUT_BOTTOM));
		available.setInput(pd.findGroup(ISharedIntroConstants.HIDDEN));
		updateColumnSizes(left);
		updateColumnSizes(right);
		updateColumnSizes(bottom);
		updateColumnSizes(available);
	}

	private void onTabChange(TabItem item) {
		String id = (String) item.getData();
		if (item.getControl() == pageContainer)
			updatePageContainer(id, (PageData) item.getData("pageData")); //$NON-NLS-1$
	}

	private void loadData(boolean fromDefault) {
		IProduct product = Platform.getProduct();
		if (product == null)
			return;
		String pid = product.getId();
		introRootPages.clear();
		Preferences prefs = IntroPlugin.getDefault().getPluginPreferences();
		// 1. Root pages
		// try product-qualified value first
		String key = pid + "_" + INTRO_ROOT_PAGES; //$NON-NLS-1$
		String rootPages = fromDefault ? prefs.getDefaultString(key) : prefs.getString(key);
		if (rootPages.length() == 0) {
			rootPages = fromDefault ? prefs.getDefaultString(INTRO_ROOT_PAGES) : prefs
					.getString(INTRO_ROOT_PAGES);
		}
		if (rootPages.length() > 0) {
			StringTokenizer stok = new StringTokenizer(rootPages, ","); //$NON-NLS-1$
			while (stok.hasMoreTokens()) {
				String tok = stok.nextToken().trim();
				introRootPages.add(tok);
			}
		}
		// 2. Background images
		key = pid + "_" + INTRO_BACKGROUND_IMAGE_LIST; //$NON-NLS-1$
		String value = fromDefault ? prefs.getDefaultString(key) : prefs.getString(key);
		if (value.length() == 0) {
			key = INTRO_BACKGROUND_IMAGE_LIST;
			value = fromDefault ? prefs.getDefaultString(key) : prefs.getString(key);
		}
		if (value.length() > 0) {
			StringTokenizer stok = new StringTokenizer(value, ","); //$NON-NLS-1$
			while (stok.hasMoreTokens()) {
				backgroundImageList.add(new IntroBackground(stok.nextToken()));
			}
		}
		// 3. Background image
		key = pid + "_" + INTRO_BACKGROUND_IMAGE; //$NON-NLS-1$
		value = fromDefault ? prefs.getDefaultString(key) : prefs.getString(key);
		if (value.length() == 0) {
			key = INTRO_BACKGROUND_IMAGE;
			value = fromDefault ? prefs.getDefaultString(key) : prefs.getString(key);
		}
		if (value.length() > 0)
			introBackgroundName = value;
		// 4. Intro data
		key = pid + "_" + INTRO_DATA; //$NON-NLS-1$
		value = fromDefault ? prefs.getDefaultString(key) : prefs.getString(key);
		if (value.length() == 0) {
			key = INTRO_DATA;
			value = fromDefault ? prefs.getDefaultString(key) : prefs.getString(key);
		}
		if (value.length() == 0)
			value = null;
		if (value != null && value.startsWith("product:")) //$NON-NLS-1$
			value = value.substring(8);
		value = BundleUtil.getResolvedResourceLocation(value, product.getDefiningBundle());
		introData = new IntroData(pid, value, true);
		introData.addImplicitContent();
	}

	public void dispose() {
		for (int i = 0; i < backgroundImageList.size(); i++)
			((IntroBackground) backgroundImageList.get(i)).dispose();
		backgroundImageList.clear();
		extensionImage.dispose();
		ihighImage.dispose();
		ilowImage.dispose();
		inewImage.dispose();
		icalloutImage.dispose();
		bgImage.dispose();
		super.dispose();
	}

	private void updateIntroBackgroundFromData() {
		IntroBackground newBg = null;
		if (introBackgroundName != null) {
			for (int i = 0; i < backgroundImageList.size(); i++) {
				IntroBackground bg = (IntroBackground) backgroundImageList.get(i);
				if (bg.getFullName().equals(introBackgroundName)) {
					newBg = bg;
					break;
				}
			}
			if (newBg == null) {
				// not on the list - make it
				newBg = new IntroBackground(introBackgroundName);
				backgroundImageList.add(newBg);
			}
			introBackground = newBg;
			updateBackground();
		}
	}

	private void updateWidgetsFromData() {
		// sync up intro background part
		updateIntroBackgroundFromData();
		updateBackground();
		// sync up the root page checklist
		rootPages.setInput(ROOT_PAGE_TABLE);
		ArrayList selected = new ArrayList();
		for (int i = 0; i < ROOT_PAGE_TABLE.length; i++) {
			String id = ROOT_PAGE_TABLE[i].id;
			if (introRootPages.contains(id))
				selected.add(ROOT_PAGE_TABLE[i]);
		}
		rootPages.setCheckedElements(selected.toArray());
	}

	private void updateBackground() {
		backgrounds.setInput(backgrounds);
		if (introBackground != null)
			backgrounds.setSelection(new StructuredSelection(introBackground), true);
		bgPreview.redraw();
	}

	public boolean performOk() {
		saveData();
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {

			public void run() {
				restartIntro();
			}
		});
		return true;
	}

	/**
	 * Remember the current page, close intro, reopen it and show the saved page.
	 * 
	 */
	private void restartIntro() {
		IIntroManager manager = PlatformUI.getWorkbench().getIntroManager();
		IIntroPart part = manager.getIntro();
		if (part != null && part instanceof CustomizableIntroPart) {
			IntroModelRoot modelRoot = IntroPlugin.getDefault().getIntroModelRoot();
			String currentPageId = modelRoot.getCurrentPageId();
			IWorkbenchWindow window = part.getIntroSite().getWorkbenchWindow();
			boolean standby = manager.isIntroStandby(part);
			PlatformUI.getWorkbench().getIntroManager().closeIntro(part);
			part = PlatformUI.getWorkbench().getIntroManager().showIntro(window, standby);
			if (part != null) {
				StringBuffer url = new StringBuffer();
				url.append("http://org.eclipse.ui.intro/showPage?id="); //$NON-NLS-1$
				url.append(currentPageId);
				IIntroURL introURL = IntroURLFactory.createIntroURL(url.toString());
				if (introURL != null)
					introURL.execute();
			}
		}
	}

	protected void performDefaults() {
		loadData(true);
		// Dispose all the root page tabs
		TabItem[] items = tabFolder.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].getData("pageData") != null) //$NON-NLS-1$
				items[i].dispose();
		}
		// Add them back in based on the checked state
		addRootPages();
		updateWidgetsFromData();
		// Get the items again
		items = tabFolder.getItems();
		// Select root
		onTabChange(items[0]);
		super.performDefaults();
	}

	private void saveData() {
		Preferences prefs = IntroPlugin.getDefault().getPluginPreferences();
		boolean toAll = applyToAll.getSelection();
		IProduct product = Platform.getProduct();
		if (product == null)
			return;
		String pid = product.getId();
		StringBuffer sbuf = new StringBuffer();
		for (int i = 0; i < introRootPages.size(); i++) {
			if (i > 0)
				sbuf.append(","); //$NON-NLS-1$
			sbuf.append((String) introRootPages.get(i));
		}
		String key = pid + "_" + INTRO_ROOT_PAGES; //$NON-NLS-1$
		prefs.setValue(key, sbuf.toString());
		if (toAll) {
			key = INTRO_ROOT_PAGES;
			prefs.setValue(key, sbuf.toString());
		}
		// store page layouts
		StringWriter writer = new StringWriter();
		PrintWriter pwriter = new PrintWriter(writer);
		introData.write(pwriter);
		pwriter.close();
		String value = writer.toString();
		key = pid + "_" + INTRO_DATA; //$NON-NLS-1$
		prefs.setValue(key, value);
		if (toAll) {
			key = INTRO_DATA;
			prefs.setValue(key, value);
		}
		if (backgroundImageList.size() > 0) {
			sbuf = new StringBuffer();
			key = pid + "_" + INTRO_BACKGROUND_IMAGE_LIST; //$NON-NLS-1$
			for (int i = 0; i < backgroundImageList.size(); i++) {
				if (i > 0)
					sbuf.append(","); //$NON-NLS-1$
				sbuf.append(((IntroBackground) backgroundImageList.get(i)).getFullName());
			}
			value = sbuf.toString();
			prefs.setValue(key, value);
			if (toAll) {
				key = INTRO_BACKGROUND_IMAGE_LIST;
				prefs.setValue(key, value);
			}
		}
		if (introBackground != null) {
			key = pid + "_" + INTRO_BACKGROUND_IMAGE; //$NON-NLS-1$
			value = introBackground.getFullName();
			prefs.setValue(key, value);
		}
		IntroPlugin.getDefault().savePluginPreferences();
	}

	private void addHomePage() {
		TabItem item = new TabItem(tabFolder, SWT.NULL);
		item.setText("Home"); //$NON-NLS-1$
		Composite container = new Composite(tabFolder, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		container.setLayout(layout);
		Label label = new Label(container, SWT.NULL);
		label.setText(Messages.WelcomeCustomizationPreferencePage_background);
		GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		label = new Label(container, SWT.NULL);
		label.setText(Messages.WelcomeCustomizationPreferencePage_preview);
		backgrounds = new TableViewer(container, SWT.BORDER);
		backgrounds.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		backgrounds.setContentProvider(contentProvider);
		backgrounds.setLabelProvider(labelProvider);
		backgrounds.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent e) {
				Object sel = ((StructuredSelection) e.getSelection()).getFirstElement();
				introBackground = (IntroBackground) sel;
				bgPreview.redraw();
			}
		});
		Button browse = new Button(container, SWT.PUSH);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		browse.setLayoutData(gd);
		browse.setText(Messages.WelcomeCustomizationPreferencePage_browse);
		browse.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				doBrowseBackground();
			}
		});
		bgPreview = new Canvas(container, SWT.NULL);
		gd = new GridData();
		gd.widthHint = 160;
		gd.heightHint = 120;
		bgPreview.setLayoutData(gd);
		bgPreview.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				if (introBackground == null)
					return;
				Image bgImage = introBackground.getImage();
				if (bgImage == null)
					return;
				Rectangle carea = bgPreview.getClientArea();
				Rectangle ibounds = bgImage.getBounds();
				e.gc.drawImage(bgImage, 0, 0, ibounds.width, ibounds.height, 0, 0, carea.width, carea.height);
			}
		});
		label = new Label(container, SWT.NULL);
		label.setText(Messages.WelcomeCustomizationPreferencePage_rootpages);
		gd = new GridData();
		gd.horizontalSpan = 3;
		label.setLayoutData(gd);
		rootPages = CheckboxTableViewer.newCheckList(container, SWT.BORDER);
		rootPages.setContentProvider(contentProvider);
		rootPages.setLabelProvider(labelProvider);
		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		gd.horizontalSpan = 3;
		rootPages.getControl().setLayoutData(gd);
		rootPages.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(CheckStateChangedEvent event) {
				RootPage page = (RootPage) event.getElement();
				boolean checked = event.getChecked();
				onPageChecked(page.id, checked);
			}
		});
		item.setControl(container);
	}

	private void addPage(String id) {
		if (!getRootPageSelected(id))
			return;
		TabItem item = new TabItem(tabFolder, SWT.NULL);
		item.setText(getRootPageName(id));
		item.setControl(pageContainer);
		item.setData(id);
		PageData pd = introData.getPage(id);
		item.setData("pageData", pd); //$NON-NLS-1$
	}

	private void onPageChecked(String id, boolean checked) {
		TabItem[] items = tabFolder.getItems();
		if (checked) {
			for (int i = 0; i < items.length; i++) {
				TabItem item = items[i];
				if (item.getData() != null)
					item.dispose();
			}
			introRootPages.add(id);
			addRootPages();
		} else {
			for (int i = 0; i < items.length; i++) {
				TabItem item = items[i];
				String itemId = (String) item.getData();
				if (itemId != null && itemId.equals(id)) {
					item.dispose();
					introRootPages.remove(id);
					return;
				}
			}
		}
	}

	private String getRootPageName(String id) {
		for (int i = 0; i < ROOT_PAGE_TABLE.length; i++) {
			if (ROOT_PAGE_TABLE[i].id.equals(id))
				return ROOT_PAGE_TABLE[i].name;
		}
		return "?"; //$NON-NLS-1$
	}

	private boolean getRootPageSelected(String id) {
		for (int i = 0; i < introRootPages.size(); i++) {
			String cid = (String) introRootPages.get(i);
			if (cid.equals(id))
				return true;
		}
		return false;
	}

	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
	}

	private void createPopupMenu(final TableViewer viewer) {
		MenuManager manager = new MenuManager();
		manager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(manager.getMenu());
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				fillPopupMenu(manager, viewer);
			}
		});
	}

	private void addDNDSupport(TableViewer viewer) {
		viewer.addDragSupport(DND.DROP_MOVE, TRANSFER_TYPES, new TableDragSourceListener(viewer));
		viewer.addDropSupport(DND.DROP_MOVE, TRANSFER_TYPES, new TableDropTargetListener(viewer));
	}

	private TableViewer createTableViewer(Composite parent, String id) {
		final Table table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
		final TableColumn column1 = new TableColumn(table, SWT.NULL);
		column1.setMoveable(false);
		column1.setWidth(20);
		column1.setResizable(false);
		final TableColumn column2 = new TableColumn(table, SWT.NULL);
		column2.setResizable(true);
		final TableViewer viewer = new TableViewer(table);
		CellEditor[] editors = new CellEditor[2];
		editors[0] = new ComboBoxCellEditor(table, ExtensionData.IMPORTANCE_NAME_TABLE, SWT.READ_ONLY);
		viewer.setCellEditors(editors);
		viewer.setColumnProperties(new String[] { ISharedIntroConstants.P_IMPORTANCE,
				ISharedIntroConstants.P_NAME });
		viewer.setCellModifier(new ICellModifier() {

			public boolean canModify(Object element, String property) {
				return property.equals(ISharedIntroConstants.P_IMPORTANCE);
			}

			public Object getValue(Object element, String property) {
				ExtensionData ed = (ExtensionData) element;
				if (property.equals(ISharedIntroConstants.P_IMPORTANCE))
					return new Integer(ed.getImportance());
				return null;
			}

			public void modify(Object element, String property, Object value) {
				Integer ivalue = (Integer) value;
				TableItem item = (TableItem) element;
				ExtensionData ed = (ExtensionData) item.getData();
				ed.setImportance(ivalue.intValue());
				viewer.update(ed, new String [] {ISharedIntroConstants.P_IMPORTANCE});
			}
		});
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(labelProvider);
		viewer.setData("id", id); //$NON-NLS-1$
		createPopupMenu(viewer);
		addDNDSupport(viewer);
		return viewer;
	}

	private void fillPopupMenu(IMenuManager manager, final TableViewer viewer) {
		StructuredSelection ssel = (StructuredSelection) viewer.getSelection();

		if (ssel.size() == 1 && viewer != available) {
			Action upAction = new Action(Messages.WelcomeCustomizationPreferencePage_up) {

				public void run() {
					doMove(viewer, true);
				}
			};
			Action downAction = new Action(Messages.WelcomeCustomizationPreferencePage_down) {

				public void run() {
					doMove(viewer, false);
				}
			};
			ExtensionData ed = (ExtensionData) ssel.getFirstElement();
			GroupData gd = (GroupData) viewer.getInput();
			upAction.setEnabled(gd.canMoveUp(ed));
			downAction.setEnabled(gd.canMoveDown(ed));
			manager.add(upAction);
			manager.add(downAction);
		}
		if (ssel.size() > 0) {
			manager.add(new Separator());
			MenuManager menu = new MenuManager(Messages.WelcomeCustomizationPreferencePage_moveTo);
			addMoveToAction(menu, available, viewer,
					Messages.WelcomeCustomizationPreferencePage_menu_available);
			addMoveToAction(menu, left, viewer, Messages.WelcomeCustomizationPreferencePage_menu_left);
			addMoveToAction(menu, right, viewer, Messages.WelcomeCustomizationPreferencePage_menu_right);
			addMoveToAction(menu, bottom, viewer, Messages.WelcomeCustomizationPreferencePage_menu_bottom);
			manager.add(menu);
		}
	}

	private void doBrowseBackground() {
		FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
		fd.setText(Messages.WelcomeCustomizationPreferencePage_browseTitle);
		String fullPath = fd.open();
		if (fullPath != null) {
			IPath filePath = new Path(fullPath);
			IntroBackground found = null;
			for (int i = 0; i < backgroundImageList.size(); i++) {
				IntroBackground bg = (IntroBackground) backgroundImageList.get(i);
				if (bg.kind == IntroBackground.ABSOLUTE) {
					IPath bgPath = new Path(bg.getFullName());

					if (bgPath.equals(filePath)) {
						found = bg;
					}
				}
			}
			if (found == null) {
				found = new IntroBackground(fullPath);
				backgroundImageList.add(found);
				introBackground = found;
			}
			updateBackground();
		}
	}

	private void addMoveToAction(MenuManager menu, final TableViewer target, final TableViewer source,
			String name) {
		if (source == target)
			return;
		Action action = new Action(name) {

			public void run() {
				doMoveTo(source, target);
			}
		};
		menu.add(action);
	}

	private void doMove(Viewer viewer, boolean up) {
		Object obj = ((StructuredSelection) viewer.getSelection()).getFirstElement();
		GroupData gd = (GroupData) viewer.getInput();
		if (up)
			gd.moveUp((ExtensionData) obj);
		else
			gd.moveDown((ExtensionData) obj);
		viewer.refresh();
	}

	private void doMoveTo(TableViewer source, TableViewer target) {
		Object[] selObjs = ((StructuredSelection) source.getSelection()).toArray();
		GroupData sourceGd = (GroupData) source.getInput();
		GroupData targetGd = (GroupData) target.getInput();
		if (targetGd == null) {
			targetGd = createTargetGd(target);
		}
		for (int i = 0; i < selObjs.length; i++) {
			ExtensionData ed = (ExtensionData) selObjs[i];
			sourceGd.remove(ed);
			targetGd.add(ed);
		}
		source.refresh();
		updateColumnSizes(source);
		if (target.getInput() != null)
			target.refresh();
		else
			target.setInput(targetGd);
		updateColumnSizes(target);
	}

	private void updateColumnSizes(TableViewer viewer) {
		TableColumn sc = viewer.getTable().getColumn(1);
		sc.pack();
	}

	private GroupData createTargetGd(Viewer target) {
		GroupData targetGd = null;
		if (target == left)
			targetGd = new GroupData(PageData.P_LEFT, false);
		else if (target == right)
			targetGd = new GroupData(PageData.P_RIGHT, false);
		else if (target == bottom)
			targetGd = new GroupData(PageData.P_BOTTOM, false);
		else if (target == available)
			targetGd = new GroupData(ISharedIntroConstants.HIDDEN, false);
		else
			return null;
		TabItem[] items = tabFolder.getSelection();
		PageData pd = (PageData) items[0].getData("pageData"); //$NON-NLS-1$
		pd.add(targetGd);
		return targetGd;
	}

	public void setCurrentPage(String pageId) {
		firstPageId = pageId;
	}

	private void selectFirstPage() {
		if (firstPageId == null)
			return;
		TabItem[] items = tabFolder.getItems();
		for (int i = 0; i < items.length; i++) {
			TabItem item = items[i];
			PageData pd = (PageData) item.getData("pageData"); //$NON-NLS-1$
			if (pd != null && pd.getId().equals(firstPageId)) {
				tabFolder.setSelection(i);
				onTabChange(item);
				return;
			}
		}
	}
}