package org.eclipse.update.internal.ui.manager;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.search.*;
import org.eclipse.update.internal.ui.views.*;
import org.eclipse.update.core.*;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.update.ui.forms.internal.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.core.runtime.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.core.IFeature;
import org.eclipse.swt.graphics.Image;
import java.net.URL;
import org.eclipse.ui.*;
import org.eclipse.jface.action.*;
import org.eclipse.update.ui.forms.internal.engine.*;

public class SearchResultSection {
	private static final String KEY_TITLE = "UpdatesPage.SearchResultSection.title";
	private static final String KEY_DESC = "UpdatesPage.SearchResultSection.desc";
	private static final String KEY_NODESC =
		"UpdatesPage.SearchResultSection.nodesc";
	private static final String KEY_STARTDESC =
		"UpdatesPage.SearchResultSection.startdesc";
	private static final String KEY_RESULT_ENTRY =
		"UpdatesPage.SearchResultSection.resultEntry";
	private static final String KEY_SITE_LINK = "openSite";
	private static final String KEY_FEATURE_LINK = "openFeature";

	private Composite container;
	private FormWidgetFactory factory;
	private int counter = 0;
	private boolean fullMode = false;
	private Label header;
	private Label descLabel;
	private Image featureImage;
	private UpdateFormPage page;
	private String searchString;

	public SearchResultSection(UpdateFormPage page) {
		this.page = page;
		featureImage = UpdateUIPluginImages.DESC_FEATURE_OBJ.createImage();
	}
	
	public void setSearchString(String text) {
		this.searchString = text;
		updateTitle();
	}
	
	private void updateTitle() {
		String text = UpdateUIPlugin.getResourceString(KEY_TITLE);
		if (searchString!=null)
			text += ": "+searchString;
		header.setText(text);
	}	

	public Composite createControl(Composite parent, FormWidgetFactory factory) {
		HTMLTableLayout layout = new HTMLTableLayout();
		this.factory = factory;

		layout.leftMargin = 10;
		layout.rightMargin = 10;
		layout.horizontalSpacing = 5;
		layout.numColumns = 2;

		header =
			factory.createHeadingLabel(parent, UpdateUIPlugin.getResourceString(KEY_TITLE));
		header.setForeground(factory.getColor(factory.COLOR_COMPOSITE_SEPARATOR));
		TableData td = new TableData();
		td.align = TableData.FILL;
		td.colspan = 2;
		header.setLayoutData(td);

		descLabel =
			factory.createLabel(
				parent,
				UpdateUIPlugin.getResourceString(KEY_NODESC),
				SWT.WRAP);
		td = new TableData();
		td.align = TableData.FILL;
		td.colspan = 2;
		descLabel.setLayoutData(td);

		container = factory.createComposite(parent);
		container.setLayout(layout);

		initialize();
		return container;
	}

	public void dispose() {
		featureImage.dispose();
	}

	public void setFullMode(boolean value) {
		if (fullMode != value) {
			this.fullMode = value;
			if (container != null)
				reflow();
		}
	}

	public void reflow() {
		reset();
		searchFinished();
	}
	
	public void reset() {
		counter = 0;
		Control[] children = container.getChildren();
		for (int i = 0; i < children.length; i++) {
			Control child = children[i];
			child.dispose();
		}
	}

	public void searchStarted() {
		reset();
		descLabel.setText(UpdateUIPlugin.getResourceString(KEY_STARTDESC));
		container.layout(true);
	}
	
	public void searchFinished() {
		initialize();
		container.layout(true);
	}

	private void initialize() {
		// add children
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		AvailableUpdates updates = model.getUpdates();
		Object[] sites = updates.getChildren(null);
		for (int i = 0; i < sites.length; i++) {
			UpdateSearchSite site = (UpdateSearchSite) sites[i];
			Object[] features = site.getChildren(null);
			for (int j = 0; j < features.length; j++) {
				IFeatureAdapter adapter = (IFeatureAdapter) features[j];
				try {
					addFeature(adapter.getFeature());
				}
				catch (CoreException e) {
				}
			}
		}
		if (counter > 0) {
			String desc = UpdateUIPlugin.getFormattedMessage(KEY_DESC, ("" + counter));
			descLabel.setText(desc);
		} else {
			descLabel.setText(UpdateUIPlugin.getResourceString(KEY_NODESC));
		}
	}

	private void addFeature(final IFeature feature) {
		counter++;
		Label imageLabel = factory.createLabel(container, null);
		imageLabel.setImage(featureImage);
		TableData td = new TableData();
		imageLabel.setLayoutData(td);

		if (fullMode) {
			URL siteURL = feature.getSite().getURL();
			IURLEntry desc = feature.getDescription();
			String description = "";
			if (desc != null) {
				String text = desc.getAnnotation();
				if (text != null) {
					description = getDescriptionMarkup(text);
				}
			}

			FormEngine engine = factory.createFormEngine(container);
			String[] variables =
				new String[] {
					getFeatureLabel(feature),
					feature.getProvider(),
					siteURL.toString(),
					description };
			String markup = UpdateUIPlugin.getFormattedMessage(KEY_RESULT_ENTRY, variables);
			engine.setHyperlinkSettings(factory.getHyperlinkHandler());
			engine.marginWidth = 1;
			engine.load(markup, true, false);
			HyperlinkAction siteAction = new HyperlinkAction() {
				public void linkActivated(IHyperlinkSegment link) {
					openSite(feature.getSite());
				}
			};
			siteAction.setDescription(feature.getSite().getURL().toString());
			siteAction.setStatusLineManager(getStatusLineManager());

			HyperlinkAction featureAction = new HyperlinkAction() {
				public void linkActivated(IHyperlinkSegment link) {
					openFeature(feature);
				}
			};
			featureAction.setDescription(feature.getURL().toString());
			featureAction.setStatusLineManager(getStatusLineManager());
			engine.registerTextObject(KEY_SITE_LINK, siteAction);
			engine.registerTextObject(KEY_FEATURE_LINK, featureAction);
			td = new TableData();
			td.grabHorizontal = true;
			td.align = TableData.FILL;
			engine.setLayoutData(td);
		} else {
			SelectableFormLabel featureLabel = new SelectableFormLabel(container, SWT.WRAP);
			featureLabel.setText(getFeatureLabel(feature));
			featureLabel.setData(feature);
			factory.turnIntoHyperlink(featureLabel, new HyperlinkAdapter() {
				public void linkEntered(Control link) {
					showStatus(feature.getURL().toString());
				}
				public void linkActivated(Control link) {
					openFeature(feature);
				}
				public void linkExited(Control link) {
					showStatus(null);
				}
			});
		}
	}

	private String getDescriptionMarkup(String text) {
		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '\n') {
				buf.append("</p><p addVerticalSpace=\"false\">");
			} else
				buf.append(c);
		}
		return "<p>" + buf.toString() + "</p>";
	}

	private String getFeatureLabel(IFeature feature) {
		String fullLabel = feature.getLabel();
		return fullLabel
			+ " "
			+ feature.getVersionedIdentifier().getVersion().toString();
	}

	private void openFeature(IFeature feature) {
		DetailsView view = (DetailsView) page.getView();
		SimpleFeatureAdapter sfeature = new SimpleFeatureAdapter(feature);
		view.showPageWithInput(DetailsView.DETAILS_PAGE, sfeature);
	}

	private void openSite(ISite site) {
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		SiteBookmark[] siteBookmarks = model.getBookmarkLeafs();
		URL siteURL = site.getURL();
		String siteURLName = siteURL.toString();
		SiteBookmark targetBookmark = null;
		for (int i = 0; i < siteBookmarks.length; i++) {
			SiteBookmark bookmark = siteBookmarks[i];
			URL bookmarkURL = bookmark.getURL();
			String bookmarkURLName = bookmarkURL.toString();
			if (siteURLName.startsWith(bookmarkURLName)) {
				// we have it - just select it in the sites
				targetBookmark = bookmark;
				break;
			}
		}
		if (targetBookmark == null) {
			targetBookmark = new SiteBookmark(siteURL.toString(), siteURL);
			model.addBookmark(targetBookmark);
		}
		try {
			SiteView view =
				(SiteView) UpdateUIPlugin.getActivePage().showView(UpdatePerspective.ID_SITES);
			view.selectSiteBookmark(targetBookmark);
		} catch (Exception e) {
			UpdateUIPlugin.logException(e);
		}
	}

	private IStatusLineManager getStatusLineManager() {
		IActionBars bars = page.getView().getViewSite().getActionBars();
		IStatusLineManager manager = bars.getStatusLineManager();
		return manager;
	}

	private void showStatus(String message) {
		getStatusLineManager().setMessage(message);
	}
}