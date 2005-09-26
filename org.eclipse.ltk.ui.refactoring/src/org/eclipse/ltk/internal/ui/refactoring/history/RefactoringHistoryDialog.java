/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.history;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.core.resources.IProject;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import org.eclipse.ltk.internal.core.refactoring.history.RefactoringDescriptorHandle;
import org.eclipse.ltk.internal.ui.refactoring.Assert;
import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ui.PlatformUI;

import org.eclipse.compare.CompareViewerPane;
import org.eclipse.compare.CompareViewerSwitchingPane;
import org.eclipse.compare.Splitter;

/**
 * Dialog to show the refactoring history.
 * 
 * @since 3.2
 */
public class RefactoringHistoryDialog extends Dialog {

	/** The button label key */
	private static final String BUTTON_LABEL= "buttonLabel"; //$NON-NLS-1$

	/** The comment caption key */
	private static final String COMMENT_CAPTION= "commentCaption"; //$NON-NLS-1$

	/** The day format key */
	private static final String DAY_FORMAT= "dayFormat"; //$NON-NLS-1$

	/** The dialog bounds key */
	private static final String DIALOG_BOUNDS= "RefactoringHistoryDialog"; //$NON-NLS-1$

	/** The dialog title key */
	private static final String DIALOG_TITLE= "title"; //$NON-NLS-1$

	/** The height key */
	private static final String HEIGHT= "height"; //$NON-NLS-1$

	/** The project caption key */
	private static final String PROJECT_CAPTION= "projectCaption"; //$NON-NLS-1$

	/** The refactoring collection key */
	private static final String REFACTORING_COLLECTION= "refactoringCollection"; //$NON-NLS-1$

	/** The refactoring format key */
	private static final String REFACTORING_FORMAT= "refactoringFormat"; //$NON-NLS-1$

	/** The today format key */
	private static final String TODAY_FORMAT= "todayFormat"; //$NON-NLS-1$

	/** The width key */
	private static final String WIDTH= "width"; //$NON-NLS-1$

	/** The workspace caption key */
	private static final String WORKSPACE_CAPTION= "workspaceCaption"; //$NON-NLS-1$

	/** The x coordinate key */
	private static final String X= "x"; //$NON-NLS-1$

	/** The y coordinate key */
	private static final String Y= "y"; //$NON-NLS-1$

	/** The yesterday format key */
	private static final String YESTERDAY_FORMAT= "yesterdayFormat"; //$NON-NLS-1$

	/**
	 * Converts a time stamp to a date representation.
	 * 
	 * @param stamp
	 *            the time stamp to convert
	 * @return the date representation
	 */
	private static long stampToDate(final long stamp) {
		final int ONE_DAY_MS= 24 * 60 * 60 * 1000;
		final Calendar calendar= Calendar.getInstance();
		return (stamp + (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET))) / ONE_DAY_MS;
	}

	/** The dialog bounds, or <code>null</code> */
	private Rectangle fBounds= null;

	/** The resource bundle to use */
	protected final ResourceBundle fBundle;

	/** The button id */
	protected final int fButtonId;

	/** The caption image */
	private Image fCaptionImage= null;

	/** The collection item, or <code>null</code> */
	private TreeItem fCollectionItem= null;

	/** The comment pane */
	private CompareViewerSwitchingPane fCommentPane= null;

	/** The container image */
	private Image fContainerImage= null;

	/** The element image */
	private Image fElementImage= null;

	/** The history input of the dialog */
	protected final RefactoringDescriptorHandle[] fHistoryInput;

	/**
	 * The history model (element type:
	 * <code>&lt;Date, Collection&lt;RefactoringDescriptorHandle&gt;&gt;</code>)
	 */
	protected final Map fHistoryModel= new HashMap(4);

	/** The history pane */
	private CompareViewerPane fHistoryPane= null;

	/** The history tree */
	private Tree fHistoryTree= null;

	/** The item image */
	private Image fItemImage= null;

	/** The project, or <code>null</code> */
	private IProject fProject= null;

	/** The dialog settings, or <code>null</code> */
	private IDialogSettings fSettings= null;

	/**
	 * Creates a new refactoring history dialog.
	 * 
	 * @param parent
	 *            the parent shell
	 * @param bundle
	 *            the resource bundle to use
	 * @param input
	 *            the sorted input of the dialog
	 * @param id
	 *            the ID of the dialog button
	 */
	public RefactoringHistoryDialog(final Shell parent, final ResourceBundle bundle, final RefactoringDescriptorHandle[] input, final int id) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		fBundle= bundle;
		fHistoryInput= input;
		fButtonId= id;
		fSettings= RefactoringUIPlugin.getDefault().getDialogSettings();
	}

	/**
	 * Adds a refactoring descriptor handle to the history model.
	 * 
	 * @param handle
	 *            the handle of the descriptor
	 * @param selected
	 *            <code>true</code> if the refactoring should be initially
	 *            selected, <code>false</code> otherwise
	 */
	protected void addDescriptor(final RefactoringDescriptorHandle handle, final boolean selected) {
		Assert.isNotNull(handle);
		if (fHistoryTree == null || fHistoryTree.isDisposed())
			return;
		final long stamp= handle.getTimeStamp();
		TreeItem item= null;
		if (stamp > 0) {
			final TreeItem[] items= fHistoryTree.getItems();
			TreeItem lastDay= null;
			if (items.length > 0)
				lastDay= items[items.length - 1];
			final long day= stampToDate(stamp);
			final Date date= new Date(stamp);
			if (lastDay == null || day != stampToDate(((Date) lastDay.getData()).getTime())) {
				lastDay= new TreeItem(fHistoryTree, SWT.NONE);
				lastDay.setImage(fContainerImage);
				final long today= stampToDate(System.currentTimeMillis());
				String formatted= DateFormat.getDateInstance().format(date);
				String key;
				if (day == today)
					key= TODAY_FORMAT;
				else if (day == today - 1)
					key= YESTERDAY_FORMAT;
				else
					key= DAY_FORMAT;
				final String pattern= fBundle.getString(key);
				if (pattern != null)
					formatted= MessageFormat.format(pattern, new String[] { formatted});
				lastDay.setText(formatted);
				lastDay.setData(date);
				fHistoryModel.put(date, new ArrayList(8));
			}
			item= new TreeItem(lastDay, SWT.NONE);
			item.setImage(fElementImage);
			final List list= (List) fHistoryModel.get(lastDay.getData());
			list.add(handle);
			item.setText(MessageFormat.format(fBundle.getString(REFACTORING_FORMAT), new String[] { DateFormat.getTimeInstance().format(date), handle.getDescription()}));
			item.setData(handle);
			if (selected)
				lastDay.setExpanded(true);
		} else {
			if (fCollectionItem == null) {
				fCollectionItem= new TreeItem(fHistoryTree, SWT.NONE);
				fCollectionItem.setImage(fCaptionImage);
				fCollectionItem.setText(fBundle.getString(REFACTORING_COLLECTION));
			}
			item= new TreeItem(fCollectionItem, SWT.NONE);
			item.setImage(fItemImage);
			item.setText(handle.getDescription());
			item.setData(handle);
		}
		if (selected) {
			fHistoryTree.setSelection(new TreeItem[] { item});
			handleSelection(item, handle, true);
		}
	}

	/**
	 * @inheritDoc
	 */
	public final boolean close() {
		final boolean result= super.close();
		if (result && fBounds != null) {
			IDialogSettings settings= fSettings.getSection(DIALOG_BOUNDS);
			if (settings == null) {
				settings= new DialogSettings(DIALOG_BOUNDS);
				fSettings.addSection(settings);
			}
			settings.put(X, fBounds.x);
			settings.put(Y, fBounds.y);
			settings.put(WIDTH, fBounds.width);
			settings.put(HEIGHT, fBounds.height);
		}
		return result;
	}

	/**
	 * @inheritDoc
	 */
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, IRefactoringHelpContextIds.REFACTORING_HISTORY_DIALOG);
	}

	/**
	 * @inheritDoc
	 */
	public final void create() {
		fHistoryModel.clear();
		super.create();
		fCollectionItem= null;
		for (int index= 0; index < fHistoryInput.length - 1; index++)
			addDescriptor(fHistoryInput[index], false);
		if (fHistoryInput.length > 0)
			addDescriptor(fHistoryInput[fHistoryInput.length - 1], true);
		fCollectionItem= null;
	}

	/**
	 * @inheritDoc
	 */
	protected void createButtonsForButtonBar(final Composite parent) {
		Button button= createButton(parent, fButtonId, fBundle.getString(BUTTON_LABEL), true);
		button.setFocus();
		final SelectionAdapter adapter= new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				close();
			}
		};
		button.addSelectionListener(adapter);
		button= createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		button.addSelectionListener(adapter);
	}

	/**
	 * @inheritDoc
	 */
	protected Control createDialogArea(final Composite parent) {
		final Composite result= (Composite) super.createDialogArea(parent);
		getShell().setText(fBundle.getString(DIALOG_TITLE));

		fItemImage= RefactoringPluginImages.DESC_OBJS_DEFAULT_CHANGE.createImage();
		fContainerImage= RefactoringPluginImages.DESC_OBJS_REFACTORING_DATE.createImage();
		fElementImage= RefactoringPluginImages.DESC_OBJS_REFACTORING_TIME.createImage();
		fCaptionImage= RefactoringPluginImages.DESC_OBJS_COMPOSITE_CHANGE.createImage();

		final Composite container= new Composite(result, SWT.NONE);
		GridLayout layout= new GridLayout(2, false);
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.horizontalSpacing= 0;
		layout.verticalSpacing= 0;
		container.setLayout(layout);

		GridData data= new GridData();
		data.grabExcessHorizontalSpace= true;
		data.grabExcessVerticalSpace= true;
		data.horizontalAlignment= SWT.FILL;
		data.verticalAlignment= SWT.FILL;
		container.setLayoutData(data);

		final Splitter splitter= new Splitter(container, SWT.VERTICAL);
		splitter.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL));
		splitter.addDisposeListener(new DisposeListener() {

			public final void widgetDisposed(final DisposeEvent event) {
				if (fContainerImage != null)
					fContainerImage.dispose();
				if (fElementImage != null)
					fElementImage.dispose();
				if (fCaptionImage != null)
					fCaptionImage.dispose();
				if (fElementImage != null)
					fElementImage.dispose();
			}
		});

		createVerticalButtonBar(container);

		final Composite leftPane= new Composite(splitter, SWT.NONE);
		layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 2;
		layout.verticalSpacing= 2;
		leftPane.setLayout(layout);

		fHistoryPane= new CompareViewerPane(leftPane, SWT.BORDER | SWT.FLAT);
		fHistoryPane.setImage(fCaptionImage);
		String text= null;
		if (fProject != null)
			text= MessageFormat.format(fBundle.getString(PROJECT_CAPTION), new String[] { fProject.getName()});
		else
			text= fBundle.getString(WORKSPACE_CAPTION);
		fHistoryPane.setText(text);
		fHistoryPane.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

		fHistoryTree= createHistoryTree(fHistoryPane);
		fHistoryTree.addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				final Widget widget= event.item;
				handleSelection(widget, widget.getData(), event.detail == SWT.CHECK);
			}
		});
		fHistoryPane.setContent(fHistoryTree);

		fCommentPane= new CompareViewerSwitchingPane(splitter, SWT.BORDER | SWT.FLAT) {

			protected final Viewer getViewer(final Viewer viewer, final Object input) {
				if (input instanceof String) {
					final String comment= (String) input;
					final SourceViewer extended= new SourceViewer(fCommentPane, null, SWT.NULL);
					extended.setDocument(new Document(comment));
					setText(fBundle.getString(COMMENT_CAPTION));
					return extended;
				}
				return null;
			}
		};
		fCommentPane.setText(fBundle.getString(COMMENT_CAPTION));
		splitter.setWeights(new int[] { 70, 30});
		applyDialogFont(parent);

		return result;
	}

	/**
	 * Creates the history tree of the dialog.
	 * 
	 * @param parent
	 *            the parent composite
	 * @return the history tree
	 */
	protected Tree createHistoryTree(final Composite parent) {
		Assert.isNotNull(parent);
		return new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL);
	}

	/**
	 * Creates the vertical button bar at the right of the dialog.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	protected void createVerticalButtonBar(final Composite parent) {
		// Do nothing
	}

	/**
	 * @inheritDoc
	 */
	protected final Point getInitialLocation(final Point size) {
		final Point location= super.getInitialLocation(size);
		final IDialogSettings settings= fSettings.getSection(DIALOG_BOUNDS);
		if (settings != null) {
			try {
				location.x= settings.getInt(X);
			} catch (NumberFormatException event) {
				// Do nothing
			}
			try {
				location.y= settings.getInt(Y);
			} catch (NumberFormatException event) {
				// Do nothing
			}
		}
		return location;
	}

	/**
	 * @inheritDoc
	 */
	protected final Point getInitialSize() {
		int width= 0;
		int height= 0;
		final Shell shell= getShell();
		if (shell != null) {
			shell.addControlListener(new ControlListener() {

				public void controlMoved(final ControlEvent event) {
					fBounds= shell.getBounds();
				}

				public void controlResized(final ControlEvent event) {
					fBounds= shell.getBounds();
				}
			});
		}
		final IDialogSettings settings= fSettings.getSection(DIALOG_BOUNDS);
		if (settings == null) {
			if (fBundle != null) {
				try {
					String string= fBundle.getString(WIDTH);
					if (string != null)
						width= Integer.parseInt(string);
					string= fBundle.getString(HEIGHT);
					if (string != null)
						height= Integer.parseInt(string);
				} catch (NumberFormatException exception) {
					// Do nothing
				}
				final Shell parent= getParentShell();
				if (parent != null) {
					final Point size= parent.getSize();
					if (width <= 0)
						width= size.x - 300;
					if (height <= 0)
						height= size.y - 200;
				}
			} else {
				final Shell parent= getParentShell();
				if (parent != null) {
					final Point size= parent.getSize();
					width= size.x - 100;
					height= size.y - 100;
				}
			}
			if (width < 600)
				width= 600;
			if (height < 500)
				height= 500;
		} else {
			try {
				width= settings.getInt(WIDTH);
			} catch (NumberFormatException exception) {
				width= 600;
			}
			try {
				height= settings.getInt(HEIGHT);
			} catch (NumberFormatException exception) {
				height= 500;
			}
		}
		return new Point(width, height);
	}

	/**
	 * Returns the selected refactoring descriptor handles of this dialog.
	 * 
	 * @return the selected refactoring descriptor handles
	 */
	public final RefactoringDescriptorHandle[] getSelection() {
		final Set set= new HashSet();
		final TreeItem[] selection= fHistoryTree.getSelection();
		for (int index= 0; index < selection.length; index++) {
			final TreeItem item= selection[index];
			final Object data= item.getData();
			if (data instanceof RefactoringDescriptorHandle)
				set.add(data);
			else if (data instanceof Date) {
				final Collection collection= (Collection) fHistoryModel.get(data);
				if (collection != null)
					set.addAll(collection);
			}
		}
		return (RefactoringDescriptorHandle[]) set.toArray(new RefactoringDescriptorHandle[set.size()]);
	}

	/**
	 * Handles the selection event.
	 * 
	 * @param widget
	 *            the selected widget
	 * @param object
	 *            the selected object, or <code>null</code>
	 * @param check
	 *            <code>true</code> if the object has been checked,
	 *            <code>false</code> otherwise
	 */
	protected void handleSelection(final Widget widget, final Object object, final boolean check) {
		Assert.isNotNull(widget);
		if (object instanceof RefactoringDescriptorHandle) {
			final RefactoringDescriptorHandle handle= (RefactoringDescriptorHandle) object;
			final RefactoringDescriptor descriptor= handle.resolveDescriptor();
			if (descriptor != null) {
				fCommentPane.setInput(descriptor.getComment());
				fCommentPane.setText(fBundle.getString(COMMENT_CAPTION));
				return;
			}
		}
		fCommentPane.setInput(null);
		fCommentPane.setText(fBundle.getString(COMMENT_CAPTION));
	}

	/**
	 * Sets the project which the history belongs to.
	 * <p>
	 * The project does not have to exist.
	 * </p>
	 * 
	 * @param project
	 *            the project, or <code>null</code> for the workspace
	 */
	public final void setProject(final IProject project) {
		fProject= project;
	}
}
