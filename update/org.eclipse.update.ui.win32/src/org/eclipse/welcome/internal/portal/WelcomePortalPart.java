/*
 * Created on Jun 19, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.welcome.internal.portal;

import java.util.Hashtable;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.internal.*;
import org.eclipse.welcome.internal.WelcomePortal;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class WelcomePortalPart {
	private static final int TAB_PADDING = 5;
	private Hashtable sectionDescriptors;
	class TabListener implements IHyperlinkListener {
		public void linkActivated(Control linkLabel) {
			IFormPage page = (IFormPage) linkLabel.getData();
			workbook.selectPage(page, false);
		}

		public void linkEntered(Control linkLabel) {
		}

		public void linkExited(Control linkLabel) {
			if (linkLabel==selectedTab)
				highlightTab(selectedTab, true);
		}
	}

	private TabListener tabListener = new TabListener();
	private Composite tabContainer;
	private FormWidgetFactory factory;
	private HyperlinkHandler hhandler;
	private WelcomePortalEditor editor;
	private Color tabColor;
	private NoTabsWorkbook workbook;
	private SelectableFormLabel selectedTab;

	public WelcomePortalPart(WelcomePortalEditor editor) {
		factory = new FormWidgetFactory();
		hhandler = new HyperlinkHandler();
		workbook = new NoTabsWorkbook();
		this.editor = editor;
	}
	
	public WelcomePortalEditor getEditor() {
		return editor;
	}

	public void dispose() {
		factory.dispose();
		hhandler.dispose();
	}
	
	public boolean isVisible(IFormPage page) {
		return workbook.getCurrentPage().equals(page);
	}

	public void createControl(Composite parent) {
		Composite container = factory.createComposite(parent);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 2;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		container.setLayout(layout);
		createTitle(container);
		createTabs(container);
		workbook.createControl(container);
		workbook.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		workbook.addFormSelectionListener(new IFormSelectionListener() {
			public void formSelected(IFormPage page, boolean setFocus) {
				hightlightTab((WelcomePortalPage) page);
			}
		});
		workbook.selectPage(workbook.getPages()[0], true);
	}

	private void createTitle(Composite parent) {
		WelcomeTitleArea title = new WelcomeTitleArea(parent, SWT.NULL);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		title.setLayoutData(gd);
		title.setText("Welcome");
		title.setBackground(factory.getBackgroundColor());
		title.setForeground(factory.getForegroundColor());
		title.setFont(JFaceResources.getHeaderFont());
	}

	private void createTabs(Composite parent) {
		tabContainer = factory.createComposite(parent);
		tabContainer.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Point size = tabContainer.getSize();
				Rectangle tabBounds = selectedTab.getBounds();
				GC gc = e.gc;
				gc.setBackground(factory.getBackgroundColor());
				gc.fillRectangle(0, tabBounds.y-TAB_PADDING, size.x, tabBounds.height+TAB_PADDING+TAB_PADDING);
			}
		});
		tabColor =
			factory.getColor(FormWidgetFactory.COLOR_COMPOSITE_SEPARATOR);
		tabContainer.setBackground(tabColor);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 10;
		layout.marginHeight = TAB_PADDING;
		layout.verticalSpacing = TAB_PADDING + TAB_PADDING;
		tabContainer.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		tabContainer.setLayoutData(gd);
		hhandler.setBackground(tabColor);
		hhandler.setForeground(factory.getBackgroundColor());
		hhandler.setHyperlinkUnderlineMode(
			HyperlinkSettings.UNDERLINE_ROLLOVER);
		loadTabs(tabContainer);
	}
	
	private void loadTabs(Composite parent) {
		IConfigurationElement [] pages = Platform.getPluginRegistry().getConfigurationElementsFor(WelcomePortal.getPluginId(), "welcomePages");
		for (int i=0; i<pages.length; i++) {
			createTab(parent, pages[i]);
		}
	}

	private void createTab(
		Composite parent,
		IConfigurationElement config) {
		WelcomePortalPage page = new WelcomePortalPage(this, config);
		SelectableFormLabel tab = factory.createSelectableLabel(parent, page.getTitle());
		GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		tab.setBackground(tabColor);
		tab.setForeground(factory.getBackgroundColor());
		hhandler.registerHyperlink(tab, tabListener);
		tab.setFont(JFaceResources.getBannerFont());
		tab.setLayoutData(gd);
		tab.setData(page);
		page.setTab(tab);
		workbook.addPage(page);
	}

	private void hightlightTab(WelcomePortalPage page) {
		if (selectedTab != null)
			highlightTab(selectedTab, false);
		SelectableFormLabel tab = page.getTab();
		highlightTab(tab, true);
		selectedTab = tab;
		tabContainer.redraw();
	}

	private void highlightTab(SelectableFormLabel tab, boolean selected) {
		Color bg = tabColor;
		Color fg = factory.getBackgroundColor();
		tab.setForeground(selected ? bg : fg);
		tab.setBackground(selected ? fg : bg);
	}

	public SectionDescriptor findSection(String id) {
		if (sectionDescriptors==null) {
			sectionDescriptors=new Hashtable();
			IConfigurationElement [] sections = Platform.getPluginRegistry().getConfigurationElementsFor(WelcomePortal.getPluginId(), "welcomeSections");
			for (int i=0; i<sections.length; i++) {
				SectionDescriptor desc = new SectionDescriptor(sections[i]);
				sectionDescriptors.put(desc.getId(), desc);
			}
		}
		return (SectionDescriptor)sectionDescriptors.get(id);
	}
}