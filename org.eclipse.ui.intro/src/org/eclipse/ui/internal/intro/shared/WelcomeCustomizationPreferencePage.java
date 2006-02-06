/***************************************************************************************************
 * Copyright (c) 2006 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.ui.internal.intro.shared;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.intro.impl.IntroPlugin;
import org.eclipse.ui.internal.intro.impl.Messages;
import org.eclipse.ui.internal.intro.impl.model.util.BundleUtil;
import org.osgi.framework.Bundle;


public class WelcomeCustomizationPreferencePage extends PreferencePage implements IWorkbenchPreferencePage,
		IExecutableExtension {

	private static final String INTRO_ROOT_PAGES = "INTRO_ROOT_PAGES"; //$NON-NLS-1$
	private static final String INTRO_DATA = "INTRO_DATA"; //$NON-NLS-1$
	private static final String INTRO_BACKGROUND_IMAGE = "INTRO_BACKGROUND_IMAGE"; //$NON-NLS-1$
	private TabFolder tabFolder;
	private Composite pageContainer;
	private TableViewer available;
	private TableViewer left;
	private TableViewer right;
	private TableViewer bottom;
	private CheckboxTableViewer rootPages;
	private ArrayList introRootPages = new ArrayList();
	private String introBackground;
	private IntroData introData;
	private Canvas bgPreview;
	private Image bgImage;
	private TableContentProvider contentProvider;
	private TableLabelProvider labelProvider;
	private Button applyToAll;

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

	class SerializeDialog extends Dialog {

		public SerializeDialog(Shell parentShell) {
			super(parentShell);
			setShellStyle(getShellStyle() | SWT.RESIZE);
		}

		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);
			Text text = new Text(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
			text.setLayoutData(new GridData(GridData.FILL_BOTH));
			text.setFont(JFaceResources.getTextFont());
			StringWriter writer = new StringWriter();
			PrintWriter pwriter = new PrintWriter(writer);
			introData.write(pwriter);
			pwriter.close();
			text.setText(writer.toString());
			return composite;
		}

		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		}
	}

	class TableContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			if (inputElement == ROOT_PAGE_TABLE)
				return ROOT_PAGE_TABLE;
			if (inputElement instanceof GroupData) {
				return ((GroupData) inputElement).getExtensions();
			}
			return new Object[0];
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	class TableLabelProvider extends LabelProvider {

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
			return super.getText(obj);
		}

		public Image getImage(Object obj) {
			return null;
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
		addPages();
		org.eclipse.jface.dialogs.Dialog.applyDialogFont(container);
		return container;
	}

	private void doSerializeState() {
		SerializeDialog sd = new SerializeDialog(getShell());
		sd.create();
		sd.getShell().setSize(600, 600);
		sd.getShell().setText(Messages.WelcomeCustomizationPreferencePage_serializeTitle);
		sd.open();
	}

	private boolean isCustomizationMode() {
		String[] args = Platform.getApplicationArgs();
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-welcomeCustomization")) //$NON-NLS-1$
				return true;
		}
		return false;
	}

	public void init(IWorkbench workbench) {
	}

	private void addPages() {
		loadData(false);
		addHomePage();
		createPageContainer();
		addRootPages();
		updateWidgetsFromData();
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
		label = new Label(pageContainer, SWT.NULL);
		label.setText(Messages.WelcomeCustomizationPreferencePage_available);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		label = new Label(pageContainer, SWT.NULL);
		label.setText(Messages.WelcomeCustomizationPreferencePage_left);
		label = new Label(pageContainer, SWT.NULL);
		label.setText(Messages.WelcomeCustomizationPreferencePage_right);
		available = new TableViewer(pageContainer, SWT.BORDER);
		available.setContentProvider(contentProvider);
		available.setLabelProvider(labelProvider);
		available.setData("id", "hidden"); //$NON-NLS-1$ //$NON-NLS-2$
		createPopupMenu(available);
		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
		gd.verticalSpan = 3;
		gd.horizontalSpan = 2;
		available.getControl().setLayoutData(gd);

		left = new TableViewer(pageContainer, SWT.BORDER);
		left.setContentProvider(contentProvider);
		left.setLabelProvider(labelProvider);
		left.setData("id", "left"); //$NON-NLS-1$ //$NON-NLS-2$
		createPopupMenu(left);
		gd = new GridData(GridData.FILL_BOTH);
		left.getControl().setLayoutData(gd);
		right = new TableViewer(pageContainer, SWT.BORDER);
		right.setContentProvider(contentProvider);
		right.setLabelProvider(labelProvider);
		right.setData("id", "right"); //$NON-NLS-1$ //$NON-NLS-2$
		createPopupMenu(right);
		gd = new GridData(GridData.FILL_BOTH);
		right.getControl().setLayoutData(gd);

		label = new Label(pageContainer, SWT.NULL);
		label.setText(Messages.WelcomeCustomizationPreferencePage_bottom);
		bottom = new TableViewer(pageContainer, SWT.BORDER);
		bottom.setContentProvider(contentProvider);
		bottom.setLabelProvider(labelProvider);
		bottom.setData("id", "bottom"); //$NON-NLS-1$ //$NON-NLS-2$
		createPopupMenu(bottom);
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
		// 2. Background image
		key = pid + "_" + INTRO_BACKGROUND_IMAGE; //$NON-NLS-1$
		String value = fromDefault ? prefs.getDefaultString(key) : prefs.getString(key);
		if (value.length() == 0) {
			key = INTRO_BACKGROUND_IMAGE;
			value = fromDefault ? prefs.getDefaultString(key) : prefs.getString(key);
		}
		if (value.length() > 0)
			introBackground = value;
		// 3. Intro data
		key = pid + "_" + INTRO_DATA; //$NON-NLS-1$
		value = fromDefault ? prefs.getDefaultString(key) : prefs.getString(key);
		if (value.length() == 0) {
			key = INTRO_DATA;
			value = fromDefault ? prefs.getDefaultString(key) : prefs.getString(key);
		}
		if (value.length() == 0)
			value = null;
		introData = new IntroData(pid, value, true);
		introData.addImplicitContent();
	}

	public void dispose() {
		if (bgImage != null) {
			bgImage.dispose();
			bgImage = null;
		}
		super.dispose();
	}

	private void updateBgImage() {
		if (bgImage != null)
			bgImage.dispose();
		if (introBackground != null) {
			BusyIndicator.showWhile(bgPreview.getDisplay(), new Runnable() {

				public void run() {
					IProduct product = Platform.getProduct();
					Bundle bundle = product.getDefiningBundle();
					String asLocal = BundleUtil.getResolvedResourceLocation(introBackground, bundle);
					try {
						ImageDescriptor desc = ImageDescriptor.createFromURL(new URL(asLocal));
						bgImage = desc.createImage();
					} catch (MalformedURLException e) {
					}
				}
			});
		}
	}

	private void updateWidgetsFromData() {
		updateBgImage();
		bgPreview.redraw();
		rootPages.setInput(ROOT_PAGE_TABLE);
		ArrayList selected = new ArrayList();
		for (int i = 0; i < ROOT_PAGE_TABLE.length; i++) {
			String id = ROOT_PAGE_TABLE[i].id;
			if (introRootPages.contains(id))
				selected.add(ROOT_PAGE_TABLE[i]);
		}
		rootPages.setCheckedElements(selected.toArray());
	}

	public boolean performOk() {
		saveData();
		return true;
	}

	protected void performDefaults() {
		loadData(true);
		updateWidgetsFromData();
		TabItem [] items = tabFolder.getItems();
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
		IntroPlugin.getDefault().savePluginPreferences();
	}

	private void addHomePage() {
		TabItem item = new TabItem(tabFolder, SWT.NULL);
		item.setText("Home"); //$NON-NLS-1$
		Composite container = new Composite(tabFolder, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		Label label = new Label(container, SWT.NULL);
		label.setText(Messages.WelcomeCustomizationPreferencePage_background);
		label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		label = new Label(container, SWT.NULL);
		label.setText(Messages.WelcomeCustomizationPreferencePage_preview);
		Combo combo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		combo.add("item1"); //$NON-NLS-1$
		combo.add("item2"); //$NON-NLS-1$
		bgPreview = new Canvas(container, SWT.NULL);
		GridData gd = new GridData();
		gd.widthHint = 160;
		gd.heightHint = 120;
		bgPreview.setLayoutData(gd);
		bgPreview.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
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
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		rootPages = CheckboxTableViewer.newCheckList(container, SWT.BORDER);
		rootPages.setContentProvider(contentProvider);
		rootPages.setLabelProvider(labelProvider);
		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		gd.horizontalSpan = 2;
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

	private void createPopupMenu(final Viewer viewer) {
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

	private void fillPopupMenu(IMenuManager manager, final Viewer viewer) {
		StructuredSelection ssel = (StructuredSelection) viewer.getSelection();

		if (ssel.size() == 1) {
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
			addMoveToAction(menu, available, viewer, Messages.WelcomeCustomizationPreferencePage_menu_available);
			addMoveToAction(menu, left, viewer, Messages.WelcomeCustomizationPreferencePage_menu_left);
			addMoveToAction(menu, right, viewer, Messages.WelcomeCustomizationPreferencePage_menu_right);
			addMoveToAction(menu, bottom, viewer, Messages.WelcomeCustomizationPreferencePage_menu_bottom);
			manager.add(menu);
		}
	}

	private void addMoveToAction(MenuManager menu, final Viewer target, final Viewer source, String name) {
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

	private void doMoveTo(Viewer source, Viewer target) {
		Object[] selObjs = ((StructuredSelection) source.getSelection()).toArray();
		GroupData sourceGd = (GroupData) source.getInput();
		GroupData targetGd = (GroupData) target.getInput();
		if (targetGd == null) {
			if (target == left)
				targetGd = new GroupData(PageData.P_LEFT, false);
			else if (target == right)
				targetGd = new GroupData(PageData.P_RIGHT, false);
			else if (target == bottom)
				targetGd = new GroupData(PageData.P_BOTTOM, false);
			else if (target == available)
				targetGd = new GroupData(ISharedIntroConstants.HIDDEN, false);
			TabItem [] items = tabFolder.getSelection();
			PageData pd = (PageData)items[0].getData("pageData"); //$NON-NLS-1$
			pd.add(targetGd);
		}
		for (int i = 0; i < selObjs.length; i++) {
			ExtensionData ed = (ExtensionData) selObjs[i];
			sourceGd.remove(ed);
			targetGd.add(ed);
		}
		source.refresh();
		if (target.getInput()!=null)
			target.refresh();
		else
			target.setInput(targetGd);
	}
}