/*
 * Created on Jun 19, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.welcome.internal.portal;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.internal.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class WelcomePortalPart {
	class TabListener implements IHyperlinkListener {
		public void linkActivated(Control linkLabel) {
			IFormPage page = (IFormPage)linkLabel.getData();
			workbook.selectPage(page, false);
		}

		public void linkEntered(Control linkLabel) {
		}

		public void linkExited(Control linkLabel) {
		}
	}
	
	class DummyPage implements IFormPage {
		private Control control;
		private String label;
		private SelectableFormLabel tab;
			/* (non-Javadoc)
		 * @see org.eclipse.update.ui.forms.internal.IFormPage#becomesInvisible(org.eclipse.update.ui.forms.internal.IFormPage)
		 */
		 
		public DummyPage(String label) {
			this.label = label;
		}
		public SelectableFormLabel getTab() {
			return tab;
		}
		public void setTab(SelectableFormLabel tab) {
			this.tab = tab;
		}
		public boolean becomesInvisible(IFormPage newPage) {
			return true;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.update.ui.forms.internal.IFormPage#becomesVisible(org.eclipse.update.ui.forms.internal.IFormPage)
		 */
		public void becomesVisible(IFormPage previousPage) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.update.ui.forms.internal.IFormPage#createControl(org.eclipse.swt.widgets.Composite)
		 */
		public void createControl(Composite parent) {
			control = factory.createLabel(parent, label);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.update.ui.forms.internal.IFormPage#getControl()
		 */
		public Control getControl() {
			// TODO Auto-generated method stub
			return control;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.update.ui.forms.internal.IFormPage#getLabel()
		 */
		public String getLabel() {
			return label;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.update.ui.forms.internal.IFormPage#getTitle()
		 */
		public String getTitle() {
			return label;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.update.ui.forms.internal.IFormPage#isSource()
		 */
		public boolean isSource() {
			// TODO Auto-generated method stub
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.update.ui.forms.internal.IFormPage#isVisible()
		 */
		public boolean isVisible() {
			// TODO Auto-generated method stub
			return workbook.getCurrentPage()==this;
		}

}
	
	private TabListener tabListener = new TabListener();
	private FormWidgetFactory factory;
	private NoTabsWorkbook workbook;
	private SelectableFormLabel selectedTab;
	
	public WelcomePortalPart() {
		factory = new FormWidgetFactory();
		workbook = new NoTabsWorkbook();
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
				hightlightTab((DummyPage)page);
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
		Composite tabContainer = factory.createComposite(parent);
		tabContainer.setBackground(factory.getColor(FormWidgetFactory.COLOR_COMPOSITE_SEPARATOR));
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 10;
		tabContainer.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		tabContainer.setLayoutData(gd);
		factory.getHyperlinkHandler().setBackground(factory.getColor(FormWidgetFactory.COLOR_COMPOSITE_SEPARATOR));
		factory.getHyperlinkHandler().setForeground(factory.getBackgroundColor());
		factory.getHyperlinkHandler().setHyperlinkUnderlineMode(HyperlinkSettings.UNDERLINE_ROLLOVER);
		createTab(tabContainer, "Home", factory);
		createTab(tabContainer, "News", factory);
		createTab(tabContainer, "Samples", factory);
		createTab(tabContainer, "Community", factory);
	}
	
	private void createTab(Composite parent, String name, FormWidgetFactory factory) {
		SelectableFormLabel tab = factory.createSelectableLabel(parent, name);
		GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING|GridData.FILL_HORIZONTAL);
		factory.turnIntoHyperlink(tab, tabListener);
		tab.setFont(JFaceResources.getBannerFont());
		tab.setLayoutData(gd);
		DummyPage page = new DummyPage(name);
		tab.setData(page);
		page.setTab(tab);
		workbook.addPage(page);
	}
	
	private void hightlightTab(DummyPage page) {
		if (selectedTab!=null) toggleTab(selectedTab);
		SelectableFormLabel tab = page.getTab();
		toggleTab(tab);
		selectedTab = tab;
	}
	private void toggleTab(SelectableFormLabel tab) {
		Color bg = tab.getBackground();
		Color fg = tab.getForeground();
		tab.setForeground(bg);
		tab.setBackground(fg);
	}
}