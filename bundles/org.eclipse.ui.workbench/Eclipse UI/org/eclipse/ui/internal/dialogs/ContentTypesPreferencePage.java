/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Preference page that allows manipulation of core content types. Unlike most
 * preference pages, it does not work on the preference store itself but rather
 * the content type manager. As such, there are no apply/default buttons and all
 * changes made take effect immediately.
 *
 * @since 3.1
 */
public class ContentTypesPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private ListViewer fileAssociationViewer;

	private Button removeButton;

	private TreeViewer contentTypesViewer;

	private Button addButton;

	private Button editButton;

	private Text charsetField;

	private Button setButton;

	private IWorkbench workbench;

	private Button removeContentTypeButton;

	private Button addChildContentTypeButton;

	private class Spec {
		String name;

		String ext;

		boolean isPredefined;

		int sortValue;

		@Override
		public String toString() {
			String toString;
			if (name != null) {
				toString = name;
			} else {
				toString = "*." + ext; //$NON-NLS-1$
			}

			if (isPredefined) {
				toString = NLS.bind(
						WorkbenchMessages.ContentTypes_lockedFormat, toString);
			}

			return toString;
		}
	}

	private class FileSpecComparator extends ViewerComparator {
		@Override
		public int category(Object element) {
			// only Spec objects in here - unchecked cast
			return ((Spec) element).sortValue;
		}
	}

	private class FileSpecLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			String label = super.getText(element);
			return TextProcessor.process(label, "*."); //$NON-NLS-1$
		}
	}

	private class FileSpecContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			IContentType contentType = (IContentType) inputElement;
			String[] userextfileSpecs = contentType
					.getFileSpecs(IContentType.FILE_EXTENSION_SPEC | IContentType.IGNORE_PRE_DEFINED);
			String[] usernamefileSpecs = contentType
					.getFileSpecs(IContentType.FILE_NAME_SPEC | IContentType.IGNORE_PRE_DEFINED);
			String[] preextfileSpecs = contentType
					.getFileSpecs(IContentType.FILE_EXTENSION_SPEC | IContentType.IGNORE_USER_DEFINED);
			String[] prenamefileSpecs = contentType
					.getFileSpecs(IContentType.FILE_NAME_SPEC | IContentType.IGNORE_USER_DEFINED);

			return createSpecs(userextfileSpecs, usernamefileSpecs,
					preextfileSpecs, prenamefileSpecs);
		}

		private Object[] createSpecs(String[] userextfileSpecs,
				String[] usernamefileSpecs, String[] preextfileSpecs,
				String[] prenamefileSpecs) {
			List returnValues = new ArrayList();
			for (String usernamefileSpec : usernamefileSpecs) {
				Spec spec = new Spec();
				spec.name = usernamefileSpec;
				spec.isPredefined = false;
				spec.sortValue = 0;
				returnValues.add(spec);
			}

			for (String prenamefileSpec : prenamefileSpecs) {
				Spec spec = new Spec();
				spec.name = prenamefileSpec;
				spec.isPredefined = true;
				spec.sortValue = 1;
				returnValues.add(spec);
			}

			for (String userextfileSpec : userextfileSpecs) {
				Spec spec = new Spec();
				spec.ext = userextfileSpec;
				spec.isPredefined = false;
				spec.sortValue = 2;
				returnValues.add(spec);
			}

			for (String preextfileSpec : preextfileSpecs) {
				Spec spec = new Spec();
				spec.ext = preextfileSpec;
				spec.isPredefined = true;
				spec.sortValue = 3;
				returnValues.add(spec);
			}

			return returnValues.toArray();
		}
	}

	private class ContentTypesLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			IContentType contentType = (IContentType) element;
			return contentType.getName();
		}
	}

	private class ContentTypesContentProvider implements ITreeContentProvider {

		private IContentTypeManager manager;

		@Override
		public Object[] getChildren(Object parentElement) {
			List elements = new ArrayList();
			IContentType baseType = (IContentType) parentElement;
			for (IContentType contentType : manager.getAllContentTypes()) {
				if (Util.equals(contentType.getBaseType(), baseType)) {
					elements.add(contentType);
				}
			}
			return elements.toArray();
		}

		@Override
		public Object getParent(Object element) {
			IContentType contentType = (IContentType) element;
			return contentType.getBaseType();
		}

		@Override
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return getChildren(null);
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			manager = (IContentTypeManager) newInput;
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		composite.setLayout(layout);

		PreferenceLinkArea contentTypeArea = new PreferenceLinkArea(
				composite,
				SWT.NONE,
				"org.eclipse.ui.preferencePages.FileEditors", WorkbenchMessages.ContentTypes_FileEditorsRelatedLink,//$NON-NLS-1$
				(IWorkbenchPreferenceContainer) getContainer(), null);

		GridData data = new GridData(GridData.FILL_HORIZONTAL
				| GridData.GRAB_HORIZONTAL);
		contentTypeArea.getControl().setLayoutData(data);

		createContentTypesTree(composite);
		createFileAssociations(composite);
		createCharset(composite);

		workbench.getHelpSystem().setHelp(parent,
				IWorkbenchHelpContextIds.CONTENT_TYPES_PREFERENCE_PAGE);

		applyDialogFont(composite);
		return composite;
	}

	private void createCharset(final Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = layout.marginWidth = 0;
		GridData compositeData = new GridData(GridData.FILL_HORIZONTAL);
		compositeData.horizontalSpan = 2;
		composite.setLayoutData(compositeData);
		composite.setLayout(layout);

		Label label = new Label(composite, SWT.NONE);
		label.setFont(parent.getFont());
		label.setText(WorkbenchMessages.ContentTypes_characterSetLabel);
		charsetField = new Text(composite, SWT.SINGLE | SWT.BORDER);
		charsetField.setFont(parent.getFont());
		charsetField.setEnabled(false);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		charsetField.setLayoutData(data);
		setButton = new Button(composite, SWT.PUSH);
		setButton.setFont(parent.getFont());
		setButton
				.setText(WorkbenchMessages.ContentTypes_characterSetUpdateLabel);
		setButton.setEnabled(false);
		setButtonLayoutData(setButton);
		setButton.addSelectionListener(widgetSelectedAdapter(e -> {
			try {
				String text = charsetField.getText().trim();
				if (text.length() == 0) {
					text = null;
				}
				getSelectedContentType().setDefaultCharset(text);
				setButton.setEnabled(false);
			} catch (CoreException e1) {
				StatusUtil.handleStatus(e1.getStatus(), StatusManager.SHOW,
						parent.getShell());
			}
		}));

		charsetField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				IContentType contentType = getSelectedContentType();
				String charset = contentType.getDefaultCharset();
				if (charset == null) {
					charset = ""; //$NON-NLS-1$
				}
				setButton.setEnabled(!charset.equals(charsetField.getText())
						&& getErrorMessage() == null);
			}
		});

		charsetField.addModifyListener(e -> {
			String errorMessage = null;
			String text = charsetField.getText();
			try {
				if (text.length() != 0 && !Charset.isSupported(text))
					errorMessage = WorkbenchMessages.ContentTypes_unsupportedEncoding;
			} catch (IllegalCharsetNameException ex) {
				errorMessage = WorkbenchMessages.ContentTypes_unsupportedEncoding;
			}
			setErrorMessage(errorMessage);
		});

	}

	/**
	 * @param composite
	 */
	private void createFileAssociations(final Composite composite) {
		{
			Label label = new Label(composite, SWT.NONE);
			label.setText(WorkbenchMessages.ContentTypes_fileAssociationsLabel);
			GridData data = new GridData();
			data.horizontalSpan = 2;
			label.setLayoutData(data);
		}
		{
			fileAssociationViewer = new ListViewer(composite);
			fileAssociationViewer.setComparator(new FileSpecComparator());
			fileAssociationViewer.getControl().setFont(composite.getFont());
			fileAssociationViewer
					.setContentProvider(new FileSpecContentProvider());
			fileAssociationViewer.setLabelProvider(new FileSpecLabelProvider());
			GridData data = new GridData(GridData.FILL_BOTH);
			data.horizontalSpan = 1;
			fileAssociationViewer.getControl().setLayoutData(data);
			fileAssociationViewer
					.addSelectionChangedListener(event -> {
						IStructuredSelection selection = (IStructuredSelection) event
								.getSelection();
						if (selection.isEmpty()) {
							editButton.setEnabled(false);
							removeButton.setEnabled(false);
							return;
						}
						boolean enabled = true;
						List elements = selection.toList();
						for (Iterator i = elements.iterator(); i.hasNext();) {
							Spec spec = (Spec) i.next();
							if (spec.isPredefined) {
								enabled = false;
							}
						}
						editButton.setEnabled(enabled && selection.size() == 1);
						removeButton.setEnabled(enabled);
					});
		}
		{
			Composite buttonArea = new Composite(composite, SWT.NONE);
			GridLayout layout = new GridLayout(1, false);
			buttonArea.setLayout(layout);
			GridData data = new GridData(SWT.DEFAULT, SWT.TOP, false, false);
			buttonArea.setLayoutData(data);

			addButton = new Button(buttonArea, SWT.PUSH);
			addButton.setFont(composite.getFont());
			addButton
					.setText(WorkbenchMessages.ContentTypes_fileAssociationsAddLabel);
			addButton.setEnabled(false);
			setButtonLayoutData(addButton);
			addButton.addSelectionListener(widgetSelectedAdapter(e -> {
				Shell shell = composite.getShell();
				IContentType selectedContentType = getSelectedContentType();
				FileExtensionDialog dialog = new FileExtensionDialog(
						shell,
						WorkbenchMessages.ContentTypes_addDialog_title,
						IWorkbenchHelpContextIds.FILE_EXTENSION_DIALOG,
						WorkbenchMessages.ContentTypes_addDialog_messageHeader,
						WorkbenchMessages.ContentTypes_addDialog_message,
						WorkbenchMessages.ContentTypes_addDialog_label);
				if (dialog.open() == Window.OK) {
					String name = dialog.getName();
					String extension = dialog.getExtension();
					try {
						if (name.equals("*")) { //$NON-NLS-1$
							selectedContentType.addFileSpec(extension,
									IContentType.FILE_EXTENSION_SPEC);
						} else {
							selectedContentType
									.addFileSpec(
											name
													+ (extension.length() > 0 ? ('.' + extension)
															: ""), //$NON-NLS-1$
											IContentType.FILE_NAME_SPEC);
						}
					} catch (CoreException ex) {
						StatusUtil.handleStatus(ex.getStatus(),
								StatusManager.SHOW, shell);
						WorkbenchPlugin.log(ex);
					} finally {
						fileAssociationViewer.refresh(false);
					}
				}
			}));

			editButton = new Button(buttonArea, SWT.PUSH);
			editButton.setFont(composite.getFont());
			editButton
					.setText(WorkbenchMessages.ContentTypes_fileAssociationsEditLabel);
			editButton.setEnabled(false);
			setButtonLayoutData(editButton);
			editButton.addSelectionListener(widgetSelectedAdapter(e -> {
				Shell shell = composite.getShell();
				IContentType selectedContentType = getSelectedContentType();
				Spec spec = getSelectedSpecs()[0];
				FileExtensionDialog dialog = new FileExtensionDialog(
						shell,
						WorkbenchMessages.ContentTypes_editDialog_title,
						IWorkbenchHelpContextIds.FILE_EXTENSION_DIALOG,
						WorkbenchMessages.ContentTypes_editDialog_messageHeader,
						WorkbenchMessages.ContentTypes_editDialog_message,
						WorkbenchMessages.ContentTypes_editDialog_label);
				if (spec.name == null) {
					dialog.setInitialValue("*." + spec.ext); //$NON-NLS-1$
				} else {
					dialog.setInitialValue(spec.name);
				}
				if (dialog.open() == Window.OK) {
					String name = dialog.getName();
					String extension = dialog.getExtension();
					try {
						// remove the original spec
						if (spec.name != null) {
							selectedContentType.removeFileSpec(spec.name,
									IContentType.FILE_NAME_SPEC);
						} else if (spec.ext != null) {
							selectedContentType.removeFileSpec(spec.ext,
									IContentType.FILE_EXTENSION_SPEC);
						}

						// add the new one
						if (name.equals("*")) { //$NON-NLS-1$
							selectedContentType.addFileSpec(extension,
									IContentType.FILE_EXTENSION_SPEC);
						} else {
							selectedContentType
									.addFileSpec(
											name
													+ (extension.length() > 0 ? ('.' + extension)
															: ""), //$NON-NLS-1$
											IContentType.FILE_NAME_SPEC);
						}
					} catch (CoreException ex) {
						StatusUtil.handleStatus(ex.getStatus(),
								StatusManager.SHOW, shell);
						WorkbenchPlugin.log(ex);
					} finally {
						fileAssociationViewer.refresh(false);
					}
				}
			}));

			removeButton = new Button(buttonArea, SWT.PUSH);
			removeButton.setEnabled(false);
			removeButton
					.setText(WorkbenchMessages.ContentTypes_fileAssociationsRemoveLabel);
			setButtonLayoutData(removeButton);
			removeButton.addSelectionListener(widgetSelectedAdapter(event -> {
				IContentType contentType = getSelectedContentType();
				Spec[] specs = getSelectedSpecs();
				MultiStatus result = new MultiStatus(PlatformUI.PLUGIN_ID,
						0, new IStatus[0],
						WorkbenchMessages.ContentTypes_errorDialogMessage,
						null);
				for (Spec spec : specs) {
					try {
						if (spec.name != null) {
							contentType.removeFileSpec(spec.name,
									IContentType.FILE_NAME_SPEC);
						} else if (spec.ext != null) {
							contentType.removeFileSpec(spec.ext,
									IContentType.FILE_EXTENSION_SPEC);
						}
					} catch (CoreException e) {
						result.add(e.getStatus());
					}
				}
				if (!result.isOK()) {
					StatusUtil.handleStatus(result, StatusManager.SHOW,
							composite.getShell());
				}
				fileAssociationViewer.refresh(false);
			}));
		}
	}

	protected Spec[] getSelectedSpecs() {
		List<Spec> list = fileAssociationViewer.getStructuredSelection().toList();
		return list.toArray(new Spec[list.size()]);
	}

	protected IContentType getSelectedContentType() {
		return (IContentType) contentTypesViewer.getStructuredSelection().getFirstElement();
	}

	/**
	 * @param composite
	 */
	private void createContentTypesTree(Composite composite) {
		{
			Label label = new Label(composite, SWT.NONE);
			label.setFont(composite.getFont());
			label.setText(WorkbenchMessages.ContentTypes_contentTypesLabel);
			GridData data = new GridData();
			data.horizontalSpan = 2;
			label.setLayoutData(data);
		}
		{
			contentTypesViewer = new TreeViewer(composite, SWT.SINGLE
					| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			contentTypesViewer.getControl().setFont(composite.getFont());
			contentTypesViewer
					.setContentProvider(new ContentTypesContentProvider());
			contentTypesViewer
					.setLabelProvider(new ContentTypesLabelProvider());
			contentTypesViewer.setComparator(new ViewerComparator());
			contentTypesViewer.setInput(Platform.getContentTypeManager());
			GridData data = new GridData(GridData.FILL_BOTH);
			contentTypesViewer.getControl().setLayoutData(data);

			contentTypesViewer
					.addSelectionChangedListener(event -> {
						IContentType contentType = (IContentType) ((IStructuredSelection) event
								.getSelection()).getFirstElement();
						fileAssociationViewer.setInput(contentType);
						editButton.setEnabled(false);
						removeButton.setEnabled(false);

						if (contentType != null) {
							String charset = contentType
									.getDefaultCharset();
							if (charset == null) {
								charset = ""; //$NON-NLS-1$
							}
							charsetField.setText(charset);
						} else {
							charsetField.setText(""); //$NON-NLS-1$
						}

						charsetField.setEnabled(contentType != null);
						addButton.setEnabled(contentType != null);
						setButton.setEnabled(false);

						addChildContentTypeButton.setEnabled(contentType != null);
						removeContentTypeButton.setEnabled(contentType != null && contentType.isUserDefined());
					});
		}
		Composite buttonsComposite = new Composite(composite, SWT.NONE);
		buttonsComposite.setLayoutData(new GridData(SWT.DEFAULT, SWT.TOP, false, false));
		buttonsComposite.setLayout(new GridLayout(1, false));
		Button addRootContentTypeButton = new Button(buttonsComposite, SWT.PUSH);
		setButtonLayoutData(addRootContentTypeButton);
		addRootContentTypeButton.setText(WorkbenchMessages.ContentTypes_addRootContentTypeButton);
		addRootContentTypeButton.addSelectionListener(widgetSelectedAdapter(e -> {
			String id = "userCreated" + System.currentTimeMillis(); //$NON-NLS-1$
			IContentTypeManager manager = (IContentTypeManager) contentTypesViewer.getInput();
			NewContentTypeDialog dialog = new NewContentTypeDialog(ContentTypesPreferencePage.this.getShell(),
					manager, null);
			if (dialog.open() == IDialogConstants.OK_ID) {
				try {
					IContentType newContentType = manager.addContentType(id, dialog.getName(), null);
					contentTypesViewer.refresh();
					contentTypesViewer.setSelection(new StructuredSelection(newContentType));
				} catch (CoreException e1) {
					MessageDialog.openError(getShell(), WorkbenchMessages.ContentTypes_failedAtEditingContentTypes,
							e1.getMessage());
				}
			}
		}));
		addChildContentTypeButton = new Button(buttonsComposite, SWT.PUSH);
		setButtonLayoutData(addChildContentTypeButton);
		addChildContentTypeButton.setText(WorkbenchMessages.ContentTypes_addChildContentTypeButton);
		addChildContentTypeButton.addSelectionListener(widgetSelectedAdapter(e -> {
			String id = "userCreated" + System.currentTimeMillis(); //$NON-NLS-1$
			IContentTypeManager manager = (IContentTypeManager) contentTypesViewer.getInput();
			NewContentTypeDialog dialog = new NewContentTypeDialog(ContentTypesPreferencePage.this.getShell(),
					manager,
					getSelectedContentType());
			if (dialog.open() == IDialogConstants.OK_ID) {
				try {
					IContentType newContentType = manager.addContentType(id, dialog.getName(),
							getSelectedContentType());
					contentTypesViewer.refresh(getSelectedContentType());
					contentTypesViewer.setSelection(new StructuredSelection(newContentType));
				} catch (CoreException e1) {
					MessageDialog.openError(getShell(), WorkbenchMessages.ContentTypes_failedAtEditingContentTypes,
							e1.getMessage());
				}
			}
		}));
		addChildContentTypeButton.setEnabled(getSelectedContentType() != null);
		removeContentTypeButton = new Button(buttonsComposite, SWT.PUSH);
		setButtonLayoutData(removeContentTypeButton);
		removeContentTypeButton.setText(WorkbenchMessages.ContentTypes_removeContentTypeButton);
		removeContentTypeButton.addSelectionListener(widgetSelectedAdapter(e -> {
			IContentType selectedContentType = getSelectedContentType();
			try {
				Platform.getContentTypeManager().removeContentType(selectedContentType.getId());
				contentTypesViewer.refresh();
			} catch (CoreException e1) {
				MessageDialog.openError(getShell(), WorkbenchMessages.ContentTypes_failedAtEditingContentTypes,
						e1.getMessage());
			}
		}));
		removeContentTypeButton
				.setEnabled(getSelectedContentType() != null && getSelectedContentType().isUserDefined());
	}

	@Override
	public void init(IWorkbench workbench) {
		this.workbench = workbench;
		noDefaultAndApplyButton();
	}
}
