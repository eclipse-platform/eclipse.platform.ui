/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.cssbridge.ui.views;

import static org.eclipse.e4.demo.cssbridge.util.ViewUtils.getDisplay;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.e4.demo.cssbridge.core.IMailService;
import org.eclipse.e4.demo.cssbridge.model.FolderType;
import org.eclipse.e4.demo.cssbridge.model.Importance;
import org.eclipse.e4.demo.cssbridge.model.Mail;
import org.eclipse.e4.demo.cssbridge.model.TreeItem;
import org.eclipse.e4.demo.cssbridge.util.DateUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class FolderPreviewView extends ViewPart {
	public static final String ID = "org.eclipse.e4.demo.cssbridge.ui.views.folderPreviewView";

	private static final String[] VIEWER_COLUMN_NAMES = { "Importance",
			"Sender", "Subject", "Date" };

	private Composite messageBodyComposite;

	private Text messageText;

	private Label subjectLabel;

	private Label dateLabel;

	private Link senderLink;

	private ISelectionService selectionService;

	private IMailService mailService;

	private TableViewer viewer;

	private ISelectionListener mailFolderChangedListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (part instanceof FoldersView
					&& selection instanceof StructuredSelection) {
				Object selected = ((TreeSelection) selection).getFirstElement();
				if (selected instanceof TreeItem
						&& ((TreeItem) selected).getValue() instanceof FolderType) {
					updateMailFolder((FolderType) ((TreeItem) selected)
							.getValue());
				}
			}
		}
	};

	private Listener tableItemPaintListener = new ItemPaintListener<TableItem>() {
		@Override
		protected String getText(TableItem item, int index) {
			return item.getText(index);
		}

		@Override
		protected Rectangle getBounds(TableItem item, int index) {
			return item.getBounds(index);
		}

		@Override
		protected Rectangle getParentBounds(TableItem item) {
			return item.getParent().getBounds();
		}

		@Override
		protected Font getFont(TableItem item) {
			return item.getFont();
		}

		@Override
		protected Image getImage(TableItem item, int index) {
			if (index == 0 && item.getImage() != null) {
				return item.getImage();
			}
			return null;
		}

		@Override
		protected int calculateTextLeftPadding(TableItem item, int index) {
			return 7;
		}
	};

	private SelectionAdapter tableSelectionChangedListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			Object data = e.item.getData();
			updateMailBody(data instanceof Mail ? (Mail) data : null);
		}
	};

	private Listener shellReskinListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			viewer.refresh();
			refreshControl(messageBodyComposite);
			messageText.setBackground(viewer.getTable().getBackground());
		}
	};

	private SelectionAdapter senderLinkSelectionAdapter = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			MessageDialog
					.openInformation(getSite().getShell(), "Not Implemented",
							"Imagine the address book or a new message being created now.");
		}
	};

	private PaintListener senderLinkPaintListener = new PaintListener() {
		private Pattern HREF_TAG_PATTERN = Pattern.compile("<a>(.+)</a>");

		@Override
		public void paintControl(PaintEvent e) {
			Link link = (Link) e.widget;
			e.gc.setForeground(Theme.getColor(Theme.Shell.LINK_FOREGROUND));
			e.gc.setBackground(link.getBackground());
			e.gc.fillRectangle(link.getBounds());
			e.gc.drawText(trimTags(link.getText()), e.x, e.y, true);
		}

		private String trimTags(String linkText) {
			Matcher matcher = HREF_TAG_PATTERN.matcher(linkText);
			if (matcher.find() && matcher.groupCount() > 0) {
				return matcher.group(1);
			}
			return linkText;
		}
	};

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		selectionService = (ISelectionService) site
				.getService(ISelectionService.class);
		selectionService.addPostSelectionListener(mailFolderChangedListener);
		mailService = (IMailService) site.getService(IMailService.class);
	}

	@Override
	public void dispose() {
		selectionService.removePostSelectionListener(mailFolderChangedListener);
		getDisplay(getSite()).removeListener(SWT.Skin, shellReskinListener);

		if (!viewer.getTable().isDisposed()) {
			viewer.getTable().removeListener(SWT.PaintItem,
					tableItemPaintListener);
			viewer.getTable().removeSelectionListener(
					tableSelectionChangedListener);
		}
		if (!senderLink.isDisposed()) {
			senderLink.removeSelectionListener(senderLinkSelectionAdapter);
			senderLink.removePaintListener(senderLinkPaintListener);
		}

		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		SashForm form = new SashForm(parent, SWT.VERTICAL);
		form.setLayout(new FillLayout());

		createMessageListComposite(form);
		createMessageBodyComposite(form);
	}

	private void createMessageListComposite(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		viewer.getTable().setHeaderVisible(true);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.getTable().addSelectionListener(tableSelectionChangedListener);
		viewer.getTable().addListener(SWT.PaintItem, tableItemPaintListener);

		getDisplay(getSite()).addListener(SWT.Skin, shellReskinListener);

		for (int i = 0; i < VIEWER_COLUMN_NAMES.length; i++) {
			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText(VIEWER_COLUMN_NAMES[i]);
			column.setLabelProvider(new ColumnLabelProviderExt(i));
		}
	}

	private void updateMailFolder(FolderType folderType) {
		List<Mail> mails = mailService.getMails(folderType);
		viewer.setInput(mails != null ? mails : Collections.emptyList());

		for (TableColumn column : viewer.getTable().getColumns()) {
			column.pack();
		}

		if (!mails.isEmpty()) {
			viewer.setSelection(new StructuredSelection(mails.get(0)));
			updateMailBody(mails.get(0));
		} else {
			updateMailBody(null);
		}
	}

	private void updateMailBody(Mail mail) {
		if (mail != null) {
			messageText.setText(mail.getBody());
			subjectLabel.setText(mail.getSubject());
			senderLink.setText(String.format("<a>%s</a>", mail.getSender()));
			dateLabel.setText(DateUtils.toString(mail.getDate()));
		}

		messageBodyComposite.setVisible(mail != null);
		messageBodyComposite.getParent().layout();
	}

	private void refreshControl(org.eclipse.swt.widgets.Control control) {
		if (control instanceof Composite) {
			for (Control child : ((Composite) control).getChildren()) {
				refreshControl(child);
			}
		} else {
			control.setForeground(Theme
					.getColor(Theme.Shell.TEXT_AND_LABEL_FOREGROUND));
		}
	}

	private void createMessageBodyComposite(Composite parent) {
		messageBodyComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		messageBodyComposite.setLayout(layout);

		// top banner
		Composite banner = new Composite(messageBodyComposite, SWT.NONE);
		banner.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL,
				GridData.VERTICAL_ALIGN_BEGINNING, true, false));
		layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 10;
		layout.numColumns = 2;
		banner.setLayout(layout);

		// setup bold font
		Font boldFont = JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT);

		Label l = new Label(banner, SWT.NONE);
		l.setText("Subject:");
		l.setFont(boldFont);
		l.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		l.setForeground(Theme.getColor(Theme.Shell.TEXT_AND_LABEL_FOREGROUND));

		GridData gridData = new GridData(SWT.BEGINNING, SWT.BEGINNING, true,
				false);
		gridData.minimumWidth = 100;

		subjectLabel = new Label(banner, SWT.WRAP);
		subjectLabel.setLayoutData(gridData);
		subjectLabel.setForeground(Theme
				.getColor(Theme.Shell.TEXT_AND_LABEL_FOREGROUND));

		l = new Label(banner, SWT.NONE);
		l.setText("From:");
		l.setFont(boldFont);
		l.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		l.setForeground(Theme.getColor(Theme.Shell.TEXT_AND_LABEL_FOREGROUND));

		senderLink = new Link(banner, SWT.NONE);
		senderLink.addSelectionListener(senderLinkSelectionAdapter);
		senderLink.setLayoutData(gridData);
		senderLink.addPaintListener(senderLinkPaintListener);

		l = new Label(banner, SWT.NONE);
		l.setText("Date:");
		l.setFont(boldFont);
		l.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		l.setForeground(Theme.getColor(Theme.Shell.TEXT_AND_LABEL_FOREGROUND));

		dateLabel = new Label(banner, SWT.WRAP);
		dateLabel.setLayoutData(gridData);
		dateLabel.setForeground(Theme
				.getColor(Theme.Shell.TEXT_AND_LABEL_FOREGROUND));

		// message contents
		messageText = new Text(messageBodyComposite, SWT.BORDER | SWT.MULTI
				| SWT.WRAP);
		messageText.setLayoutData(new GridData(GridData.FILL_BOTH));
		messageText.setEditable(false);
		messageText.setBackground(messageText.getDisplay().getSystemColor(
				SWT.COLOR_WHITE));
		messageText.setForeground(Theme
				.getColor(Theme.Shell.TEXT_AND_LABEL_FOREGROUND));
		messageText.setBackground(viewer.getTable().getBackground());
	}

	@Override
	public void setFocus() {
	}

	private static class ColumnLabelProviderExt extends ColumnLabelProvider {
		private int columnIndex;

		public ColumnLabelProviderExt(int columnIndex) {
			this.columnIndex = columnIndex;
		}

		@Override
		public Image getImage(Object element) {
			if (columnIndex == 0
					&& ((Mail) element).getImportance() == Importance.High) {
				return PlatformUI.getWorkbench().getSharedImages()
						.getImage(ISharedImages.IMG_DEC_FIELD_WARNING);
			}
			return null;
		}

		@Override
		public Font getFont(Object element) {
			Mail mail = (Mail) element;
			if (mail.getImportance() == Importance.High) {
				return Theme
						.getFont(Theme.FolderPreviewView.HIGH_IMP_MAIL_FONT);
			}
			if (mail.getImportance() == Importance.Normal) {
				return Theme
						.getFont(Theme.FolderPreviewView.NORMAL_IMP_MAIL_FONT);
			}
			return Theme.getFont(Theme.FolderPreviewView.LOW_IMP_MAIL_FONT);
		}

		@Override
		public Color getForeground(Object element) {
			Mail mail = (Mail) element;
			if (mail.getImportance() == Importance.High) {
				return Theme
						.getColor(Theme.FolderPreviewView.HIGH_IMP_MAIL_FOREGROUND);
			}
			if (mail.getImportance() == Importance.Normal) {
				return Theme
						.getColor(Theme.FolderPreviewView.NORMAL_IMP_MAIL_FOREGROUND);
			}
			return Theme
					.getColor(Theme.FolderPreviewView.LOW_IMP_MAIL_FOREGROUND);
		}

		@Override
		public String getText(Object element) {
			Mail mail = (Mail) element;
			switch (columnIndex) {
			case 1:
				return mail.getSender();
			case 2:
				return mail.getSubject();
			case 3:
				return DateUtils.toString(mail.getDate());
			default:
				return "";
			}
		}
	}
}
