package org.eclipse.update.internal.ui.forms;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.pages.UpdateFormPage;
import org.eclipse.update.internal.ui.preferences.UpdateColors;
import org.eclipse.update.internal.ui.views.*;
import org.eclipse.update.ui.forms.internal.*;
import org.eclipse.update.ui.forms.internal.engine.*;

public class MainForm extends UpdateWebForm {
	private static final String KEY_TITLE = "HomePage.title";
	private static final String KEY_UPDATES_TITLE = "HomePage.updates.title";
	private static final String KEY_UPDATES_DESC = "HomePage.updates.desc";
	private static final String KEY_INSTALLS_TITLE = "HomePage.installs.title";
	private static final String KEY_INSTALLS_DESC = "HomePage.installs.desc";
	private static final String KEY_UNINSTALLS_TITLE =
		"HomePage.uninstalls.title";
	private static final String KEY_UNINSTALLS_DESC = "HomePage.uninstals.desc";
	private static final String KEY_HISTORY_TITLE = "HomePage.history.title";
	private static final String KEY_HISTORY_DESC = "HomePage.history.desc";

	Image itemImage;
	Image configsImage;
	Image sitesImage;
	ArrayList topics = new ArrayList();

	public MainForm(UpdateFormPage page) {
		super(page);
		itemImage = UpdateUIPluginImages.DESC_ITEM.createImage();
		configsImage = UpdateUIPluginImages.DESC_CONFIGS_VIEW.createImage();
		sitesImage = UpdateUIPluginImages.DESC_SITES_VIEW.createImage();
	}

	public void dispose() {
		itemImage.dispose();
		configsImage.dispose();
		sitesImage.dispose();
		super.dispose();
	}

	public void initialize(Object modelObject) {
		setHeadingText(UpdateUIPlugin.getResourceString(KEY_TITLE));
		super.initialize(modelObject);
		IPreferenceStore pstore =
			UpdateUIPlugin.getDefault().getPreferenceStore();
		pstore.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				if (e.getProperty().equals(UpdateColors.P_TOPIC_COLOR))
					updateColors();
			}
		});
	}

	private void updateColors() {
		for (int i = 0; i < topics.size(); i++) {
			Control topic = (Control) topics.get(i);
			topic.setForeground(UpdateColors.getTopicColor(topic.getDisplay()));
		}
	}

	protected int getNumColumns() {
		return 1;
	}

	protected void createContents(Composite parent) {
		HTMLTableLayout layout = new HTMLTableLayout();
		parent.setLayout(layout);
		layout.leftMargin = layout.rightMargin = 10;
		layout.topMargin = 15;
		layout.horizontalSpacing = 5;
		layout.verticalSpacing = 0;
		layout.numColumns = 2;

		FormWidgetFactory factory = getFactory();

		Label topicImage;
		Label topic;
		FormEngine text;
		HyperlinkAction action;

		Color topicColor = UpdateColors.getTopicColor(parent.getDisplay());

		action = new HyperlinkAction() {
			public void linkActivated(IHyperlinkSegment link) {
				UpdatesView view =
					(UpdatesView) showView(UpdatePerspective.ID_UPDATES);
				if (view != null)
					view.selectUpdateObject();
			}
		};
		topicImage = factory.createLabel(parent, null);
		topicImage.setImage(itemImage);
		topic =
			factory.createHeadingLabel(
				parent,
				UpdateUIPlugin.getResourceString(KEY_UPDATES_TITLE),
				SWT.WRAP);
		topic.setForeground(topicColor);
		topics.add(topic);
		factory.createLabel(parent, null);
		text = factory.createFormEngine(parent);
		setFocusControl(text);
		text.load(
			UpdateUIPlugin.getResourceString(KEY_UPDATES_DESC),
			true,
			false);
		text.registerTextObject("action1", action);
		text.registerTextObject("image1", sitesImage);
		TableData td = new TableData();
		td.grabHorizontal = true;
		text.setLayoutData(td);

		addSeparator(parent);

		action = new HyperlinkAction() {
			public void linkActivated(IHyperlinkSegment link) {
				showView(UpdatePerspective.ID_UPDATES);
			}
		};
		topicImage = factory.createLabel(parent, null);
		topicImage.setImage(itemImage);
		topic =
			factory.createHeadingLabel(
				parent,
				UpdateUIPlugin.getResourceString(KEY_INSTALLS_TITLE),
				SWT.WRAP);
		topic.setForeground(topicColor);
		topics.add(topic);
		factory.createLabel(parent, null);
		text = factory.createFormEngine(parent);
		text.load(
			UpdateUIPlugin.getResourceString(KEY_INSTALLS_DESC),
			true,
			false);
		text.registerTextObject("action1", action);
		text.registerTextObject("image1", sitesImage);
		td = new TableData();
		td.grabHorizontal = true;
		text.setLayoutData(td);
		addSeparator(parent);

		action = new HyperlinkAction() {
			public void linkActivated(IHyperlinkSegment link) {
				ConfigurationView view =
					(ConfigurationView) showView(UpdatePerspective
						.ID_CONFIGURATION);
				if (view != null)
					view.selectCurrentConfiguration();
			}
		};
		topicImage = factory.createLabel(parent, null);
		topicImage.setImage(itemImage);
		topic =
			factory.createHeadingLabel(
				parent,
				UpdateUIPlugin.getResourceString(KEY_UNINSTALLS_TITLE),
				SWT.WRAP);
		topic.setForeground(topicColor);
		topics.add(topic);
		factory.createLabel(parent, null);
		text = factory.createFormEngine(parent);
		text.load(
			UpdateUIPlugin.getResourceString(KEY_UNINSTALLS_DESC),
			true,
			false);
		text.registerTextObject("action1", action);
		text.registerTextObject("image1", configsImage);
		td = new TableData();
		td.grabHorizontal = true;
		text.setLayoutData(td);

		addSeparator(parent);

		action = new HyperlinkAction() {
			public void linkActivated(IHyperlinkSegment link) {
				ConfigurationView view =
					(ConfigurationView) showView(UpdatePerspective
						.ID_CONFIGURATION);
				if (view != null)
					view.selectHistoryFolder();
			}
		};
		topicImage = factory.createLabel(parent, null);
		topicImage.setImage(itemImage);
		topic =
			factory.createHeadingLabel(
				parent,
				UpdateUIPlugin.getResourceString(KEY_HISTORY_TITLE),
				SWT.WRAP);
		topic.setForeground(topicColor);
		topics.add(topic);
		factory.createLabel(parent, null);
		text = factory.createFormEngine(parent);
		text.load(
			UpdateUIPlugin.getResourceString(KEY_HISTORY_DESC),
			true,
			false);
		text.registerTextObject("action1", action);
		td = new TableData();
		td.grabHorizontal = true;
		text.setLayoutData(td);
		WorkbenchHelp.setHelp(parent, "org.eclipse.update.ui.MainForm");
	}

	private void addSeparator(Composite parent) {
		Label label = new Label(parent, SWT.NULL);
		TableData td = new TableData();
		td.colspan = 2;
		label.setLayoutData(td);
	}

	private IViewPart showView(String viewId) {
		try {
			IViewPart part = UpdateUIPlugin.getActivePage().showView(viewId);
			return part;
		} catch (PartInitException e) {
			return null;
		}
	}
}