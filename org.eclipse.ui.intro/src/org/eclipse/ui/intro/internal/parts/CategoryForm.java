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
package org.eclipse.ui.intro.internal.parts;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.intro.internal.*;
import org.eclipse.ui.intro.internal.model.*;
import org.eclipse.ui.intro.internal.util.*;
/**
 * A Form that represents an Intro Page categories are. It is swapped in the
 * categories page book in the PageForm class. It has the navigation toolbar UI
 * and it has a page book for swapping in categories of Intro Pages.
 */
public class CategoryForm implements IIntroConstants, IPropertyListener {
	private FormToolkit toolkit = null;
	private ScrolledPageBook categoryPageBook = null;
	private IntroModelRoot model = null;
	private ScrolledForm pageForm = null;
	private FormStyleManager styleManager;
	private HyperlinkAdapter hyperlinkAdapter = new HyperlinkAdapter() {
		public void linkActivated(HyperlinkEvent e) {
			Hyperlink imageLink = (Hyperlink) e.getSource();
			IntroLink introLink = (IntroLink) imageLink.getData(INTRO_LINK);
			IntroURLParser parser = new IntroURLParser(introLink.getUrl());
			if (parser.hasIntroUrl()) {
				// execute the action embedded in the IntroURL
				parser.getIntroURL().execute();
				return;
			} else if (parser.hasProtocol()) {
				Program.launch(introLink.getUrl());
				return;
			}
			DialogUtil.displayInfoMessage(imageLink.getShell(), "URL is: "
					+ introLink.getUrl());
		}
		public void linkEntered(HyperlinkEvent e) {
		}
		public void linkExited(HyperlinkEvent e) {
		}
	};
	/**
	 *  
	 */
	public CategoryForm(FormToolkit toolkit, IntroModelRoot modelRoot) {
		this.toolkit = toolkit;
		this.model = modelRoot;
		// add this CategoryForm as a listener to model changes to update the
		// title of this form.
		model.addPropertyListener(this);
	}
	/**
	 * Create the form for the root page. Number of columns there is equal to
	 * the number of links. Every image link does not cache a model object for
	 * data retrieval..
	 * 
	 * @param pageBook
	 */
	public void createPartControl(ScrolledPageBook categoryPageBook,
			FormStyleManager sharedStyleManager) {
		this.categoryPageBook = categoryPageBook;
		// first, create a page style manager from shared style manager
		// because we only need it for the UI navigation composite.
		this.styleManager = new FormStyleManager(model.getCurrentPage(),
				sharedStyleManager.getProperties());
		String pageId = model.getCurrentPageId();
		AbstractIntroPage currentPage = model.getCurrentPage();
		Composite categoriesComposite = categoryPageBook.createPage(pageId);
		// this is where we set the Table Layout.
		Util.highlight(categoriesComposite, SWT.COLOR_GREEN);
		TableWrapLayout layout = new TableWrapLayout();
		layout.topMargin = 15;
		layout.verticalSpacing = 15;
		layout.leftMargin = 15;
		layout.rightMargin = 15;
		layout.bottomMargin = 15;
		categoriesComposite.setLayout(layout);
		if (currentPage.getText() != null) {
			Label label = toolkit.createLabel(categoriesComposite, currentPage
					.getText(), SWT.WRAP);
			label.setFont(DEFAULT_FONT);
			TableWrapData td = new TableWrapData();
			td.align = TableWrapData.FILL;
			label.setLayoutData(td);
		}
		// DONOW: revisit.for now, just make code work.
		IntroElement[] children = currentPage.getChildren();
		boolean hasDivs = currentPage.getChildrenOfType(IntroElement.DIV).length != 0
				? true
				: false;
		boolean hasLinks = currentPage.getChildrenOfType(IntroElement.LINK).length != 0
				? true
				: false;
		if (hasDivs) {
			String layoutStyle = getLayoutStyle(currentPage);
			if (layoutStyle.equals("columns")) {
				createCategoryColumns(categoriesComposite, currentPage,
						getShowLinkDescription(currentPage),
						getVerticalLinkSpacing(currentPage));
			} else if (layoutStyle.equals("table")) {
				createCategoryTable(categoriesComposite, currentPage,
						getNumberOfColumns(currentPage),
						getShowLinkDescription(currentPage),
						getVerticalLinkSpacing(currentPage));
			}
		}
		if (hasLinks) {
			Control c = createCategory(categoriesComposite, null, null,
					currentPage.getLinks(), getNumberOfColumns(currentPage),
					getShowLinkDescription(currentPage),
					getVerticalLinkSpacing(currentPage));
			TableWrapData td = new TableWrapData(TableWrapData.FILL,
					TableWrapData.TOP);
			td.grabHorizontal = true;
			c.setLayoutData(td);
		}
		// Clear memory. No need for style manager any more.
		sharedStyleManager = null;
	}
	private String getLayoutStyle(AbstractIntroPage page) {
		String key = "page." + page.getId() + ".layout";
		String value = styleManager.getProperty(key);
		if (value == null)
			value = "table";
		return value;
	}
	private int getNumberOfColumns(AbstractIntroPage page) {
		String key = "page." + page.getId() + ".layout.ncolumns";
		int ncolumns = 0;
		String value = styleManager.getProperty(key);
		try {
			ncolumns = Integer.parseInt(value);
		} catch (NumberFormatException e) {
		}
		return ncolumns;
	}
	private int getNumberOfColumns(AbstractIntroPage page, IntroDiv category) {
		String key = "page." + page.getId() + "." + category.getId()
				+ ".layout.ncolumns";
		int ncolumns = 1;
		String value = styleManager.getProperty(key);
		try {
			ncolumns = Integer.parseInt(value);
		} catch (NumberFormatException e) {
		}
		return ncolumns;
	}
	private boolean getShowLinkDescription(AbstractIntroPage page) {
		String key = "page." + page.getId() + ".layout.link-description";
		String value = styleManager.getProperty(key);
		if (value == null)
			value = "false";
		return value.toLowerCase().equals("true");
	}
	private int getVerticalLinkSpacing(AbstractIntroPage page) {
		String key = "page." + page.getId() + ".layout.link-vspacing";
		int vspacing = 5;
		String value = styleManager.getProperty(key);
		try {
			vspacing = Integer.parseInt(value);
		} catch (NumberFormatException e) {
		}
		return vspacing;
	}
	private void createCategoryColumns(Composite parent,
			AbstractIntroPage page, boolean showLinkDescription, int vspacing) {
		Composite client = toolkit.createComposite(parent);
		ColumnLayout layout = new ColumnLayout();
		layout.topMargin = 0;
		layout.bottomMargin = 0;
		layout.leftMargin = 0;
		layout.rightMargin = 0;
		client.setLayout(layout);
		TableWrapData td = new TableWrapData();
		td.align = TableWrapData.FILL;
		td.grabHorizontal = true;
		client.setLayoutData(td);
		// DONOW: revisit.
		IntroElement[] children = page.getChildren();
		for (int i = 0; i < children.length; i++) {
			// Make code work for now. create UI only for categories.
			if (children[i].isOfType(IntroElement.DIV)) {
				IntroDiv div = (IntroDiv) children[i];
				Control ccontrol = createCategory(client, div.getLabel(), div
						.getText(), div.getLinks(), 1, showLinkDescription,
						vspacing);
			}
		}
	}
	private void createCategoryTable(Composite parent, AbstractIntroPage page,
			int ncolumns, boolean showLinkDescription, int vspacing) {
		Composite client = toolkit.createComposite(parent);
		TableWrapLayout layout = new TableWrapLayout();
		layout.topMargin = 0;
		layout.bottomMargin = 0;
		layout.leftMargin = 0;
		layout.rightMargin = 0;
		// DONOW: revisit:
		IntroDiv[] divs = (IntroDiv[]) page.getChildrenOfType(IntroElement.DIV);
		layout.numColumns = ncolumns == 0 ? divs.length : ncolumns;
		client.setLayout(layout);
		TableWrapData td = new TableWrapData();
		td.align = TableWrapData.FILL;
		td.grabHorizontal = true;
		client.setLayoutData(td);
		for (int i = 0; i < divs.length; i++) {
			IntroDiv div = divs[i];
			Control ccontrol = createCategory(client, div.getLabel(), div
					.getText(), div.getLinks(), getNumberOfColumns(page, div),
					showLinkDescription, vspacing);
			td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
			td.grabHorizontal = true;
			ccontrol.setLayoutData(td);
		}
	}
	private Control createCategory(Composite parent, String label,
			String description, IntroLink[] links, int ncolumns,
			boolean showLinkDescription, int vspacing) {
		int style = description != null ? Section.DESCRIPTION : SWT.NULL;
		Composite client = null;
		Composite control = null;
		if (description != null || label != null) {
			Section section = toolkit.createSection(parent, style);
			if (label != null)
				section.setText(label);
			if (description != null)
				section.setDescription(description);
			client = toolkit.createComposite(section, SWT.WRAP);
			section.setClient(client);
			control = section;
		} else {
			client = toolkit.createComposite(parent, SWT.WRAP);
			control = client;
		}
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = ncolumns > 0 ? ncolumns : links.length;
		layout.verticalSpacing = vspacing;
		client.setLayout(layout);
		Util.highlight(client, SWT.COLOR_YELLOW);
		for (int i = 0; i < links.length; i++) {
			Control lc = createImageHyperlink(client, links[i],
					showLinkDescription);
			if (!(lc instanceof Hyperlink)) {
				TableWrapData td = new TableWrapData(TableWrapData.FILL,
						TableWrapData.TOP);
				td.grabHorizontal = true;
				lc.setLayoutData(td);
			}
		}
		return control;
	}
	/**
	 * Creates an Image Hyperlink from an IntroLink. Model object is cached.
	 * 
	 * @param body
	 * @param link
	 */
	private Control createImageHyperlink(Composite body, IntroLink link,
			boolean showDescription) {
		Control control;
		Hyperlink linkControl;
		Image linkImage = styleManager.getImage(link, "icon");
		if (showDescription && link.getText() != null) {
			Composite container = toolkit.createComposite(body);
			TableWrapLayout layout = new TableWrapLayout();
			layout.leftMargin = layout.rightMargin = 0;
			layout.topMargin = layout.bottomMargin = 0;
			layout.verticalSpacing = 0;
			layout.numColumns = 2;
			container.setLayout(layout);
			Label ilabel = toolkit.createLabel(container, null);
			ilabel.setImage(linkImage);
			TableWrapData td = new TableWrapData();
			td.valign = TableWrapData.TOP;
			td.rowspan = 2;
			ilabel.setLayoutData(td);
			linkControl = toolkit.createHyperlink(container, null, SWT.WRAP);
			td = new TableWrapData(TableWrapData.LEFT, TableWrapData.BOTTOM);
			td.grabVertical = true;
			linkControl.setLayoutData(td);
			Util.highlight(linkControl, SWT.COLOR_RED);
			Util.highlight(container, SWT.COLOR_DARK_YELLOW);
			Label desc = toolkit.createLabel(container, link.getText(),
					SWT.WRAP);
			td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
			td.grabHorizontal = true;
			td.grabVertical = true;
			desc.setLayoutData(td);
			control = container;
		} else {
			ImageHyperlink imageLink = toolkit.createImageHyperlink(body,
					SWT.WRAP | SWT.CENTER);
			imageLink.setImage(linkImage);
			linkControl = imageLink;
			control = linkControl;
		}
		linkControl.setText(link.getLabel());
		linkControl.setFont(DEFAULT_FONT);
		// cache the intro link model object for description and URL.
		linkControl.setData(INTRO_LINK, link);
		linkControl.addHyperlinkListener(hyperlinkAdapter);
		return control;
	}
	/**
	 * Handle model property changes. The UI here is notified of a change to the
	 * current page in the model. This happens if an intro URL showPage method
	 * is executed.
	 * 
	 * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object,
	 *      int)
	 */
	public void propertyChanged(Object source, int propId) {
		//		if (propId == IntroModelRoot.CURRENT_PAGE_PROPERTY_ID) {
		//			String pageId = model.getCurrentPageId();
		//			pageForm.setText(model.getCurrentPage().getTitle());
		//		}
	}
}