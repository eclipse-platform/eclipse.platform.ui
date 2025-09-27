/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Nicolaj Hoess <nicohoess@gmail.com> - Editor templates pref page: Allow to sort by column - https://bugs.eclipse.org/203722
 *******************************************************************************/
package org.eclipse.ui.texteditor.templates;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.osgi.framework.FrameworkUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.core.expressions.Expression;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.text.templates.TemplatePersistenceData;
import org.eclipse.text.templates.TemplateReaderWriter;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import org.eclipse.ui.ActiveShellExpression;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.texteditor.NLSUtility;
import org.eclipse.ui.internal.texteditor.SWTUtil;
import org.eclipse.ui.internal.texteditor.TextEditorPlugin;
import org.eclipse.ui.internal.texteditor.templates.TextViewerAction;

import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.IUpdate;


/**
 * A template preference page allows configuration of the templates for an
 * editor. It provides controls for adding, removing and changing templates as
 * well as enablement, default management and an optional formatter preference.
 * <p>
 * Subclasses need to provide a {@link TemplateStore} and a
 * {@link ContextTypeRegistry} and should set the preference store. They may
 * optionally override {@link #isShowFormatterSetting()}.
 * </p>
 *
 * @since 3.0
 */
public abstract class TemplatePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {


	/**
	 * Dialog to edit a template. Clients will usually instantiate, but
	 * may also extend.
	 *
	 * @since 3.3
	 */
	protected static class EditTemplateDialog extends StatusDialog {

		private final Template fOriginalTemplate;

		private Text fNameText;
		private Text fDescriptionText;
		private Combo fContextCombo;
		private SourceViewer fPatternEditor;
		private Button fInsertVariableButton;
		private Button fAutoInsertCheckbox;
		private boolean fIsNameModifiable;

		private StatusInfo fValidationStatus;
		private boolean fSuppressError= true; // #4354
		private final Map<String, TextViewerAction> fGlobalActions= new HashMap<>(10);
		private final List<String> fSelectionActions = new ArrayList<>(3);
		private String[][] fContextTypes;

		private ContextTypeRegistry fContextTypeRegistry;

		private final TemplateVariableProcessor fTemplateProcessor= new TemplateVariableProcessor();

		private Template fNewTemplate;


		/**
		 * Creates a new dialog.
		 *
		 * @param parent the shell parent of the dialog
		 * @param template the template to edit
		 * @param edit whether this is a new template or an existing being edited
		 * @param isNameModifiable whether the name of the template may be modified
		 * @param registry the context type registry to use
		 */
		public EditTemplateDialog(Shell parent, Template template, boolean edit, boolean isNameModifiable, ContextTypeRegistry registry) {
			super(parent);

			String title= edit
				? TemplatesMessages.EditTemplateDialog_title_edit
				: TemplatesMessages.EditTemplateDialog_title_new;
			setTitle(title);

			fOriginalTemplate= template;
			fIsNameModifiable= isNameModifiable;

			List<String[]> contexts= new ArrayList<>();
			for (Iterator<TemplateContextType> it= registry.contextTypes(); it.hasNext();) {
				TemplateContextType type= it.next();
				contexts.add(new String[] { type.getId(), type.getName() });
			}
			Collections.sort(contexts, new Comparator<String[]>() {
				Collator fCollator= Collator.getInstance();
				@Override
				public int compare(String[] o1, String[] o2) {
					return fCollator.compare(o1[1], o2[1]);
				}
			});
			fContextTypes= contexts.toArray(new String[contexts.size()][]);

			fValidationStatus= new StatusInfo();

			fContextTypeRegistry= registry;

			TemplateContextType type= fContextTypeRegistry.getContextType(template.getContextTypeId());
			fTemplateProcessor.setContextType(type);
		}

		@Override
		protected boolean isResizable() {
			return true;
		}

		@Override
		public void create() {
			super.create();
			// update initial OK button to be disabled for new templates
			boolean valid= fNameText == null || !fNameText.getText().trim().isEmpty();
			if (!valid) {
				StatusInfo status = new StatusInfo();
				status.setError(TemplatesMessages.EditTemplateDialog_error_noname);
				updateButtonsEnableState(status);
	 		}
		}

		@Override
		protected Control createDialogArea(Composite ancestor) {
			Composite parent= new Composite(ancestor, SWT.NONE);
			GridLayout layout= new GridLayout();
			layout.numColumns= 2;
			layout.marginHeight= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
			layout.marginWidth= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
			layout.verticalSpacing= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
			layout.horizontalSpacing= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
			parent.setLayout(layout);
			parent.setLayoutData(new GridData(GridData.FILL_BOTH));

			ModifyListener listener = e -> doTextWidgetChanged(e.widget);

			if (fIsNameModifiable) {
				createLabel(parent, TemplatesMessages.EditTemplateDialog_name);

				Composite composite= new Composite(parent, SWT.NONE);
				composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				layout= new GridLayout();
				layout.numColumns= 4;
				layout.marginWidth= 0;
				layout.marginHeight= 0;
				composite.setLayout(layout);

				fNameText= createText(composite);
				fNameText.addModifyListener(listener);
				fNameText.addFocusListener(new FocusListener() {

					@Override
					public void focusGained(FocusEvent e) {
					}

					@Override
					public void focusLost(FocusEvent e) {
						if (fSuppressError) {
							fSuppressError= false;
							updateButtons();
						}
					}
				});
				BidiUtils.applyBidiProcessing(fNameText, BidiUtils.BTD_DEFAULT);

				createLabel(composite, TemplatesMessages.EditTemplateDialog_context);
				fContextCombo= new Combo(composite, SWT.READ_ONLY);

				for (String[] fContextType : fContextTypes) {
					fContextCombo.add(fContextType[1]);
				}

				fContextCombo.addModifyListener(listener);
				SWTUtil.setDefaultVisibleItemCount(fContextCombo);

				fAutoInsertCheckbox= createCheckbox(composite, TemplatesMessages.EditTemplateDialog_autoinsert);
				fAutoInsertCheckbox.setSelection(fOriginalTemplate.isAutoInsertable());
			}

			createLabel(parent, TemplatesMessages.EditTemplateDialog_description);

			int descFlags= fIsNameModifiable ? SWT.BORDER : SWT.BORDER | SWT.READ_ONLY;
			fDescriptionText= new Text(parent, descFlags );
			fDescriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			fDescriptionText.addModifyListener(listener);
			BidiUtils.applyBidiProcessing(fDescriptionText, BidiUtils.BTD_DEFAULT);

			Label patternLabel= createLabel(parent, TemplatesMessages.EditTemplateDialog_pattern);
			patternLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
			fPatternEditor= createEditor(parent, fOriginalTemplate.getPattern());

			Label filler= new Label(parent, SWT.NONE);
			filler.setLayoutData(new GridData());

			Composite composite= new Composite(parent, SWT.NONE);
			layout= new GridLayout();
			layout.marginWidth= 0;
			layout.marginHeight= 0;
			composite.setLayout(layout);
			composite.setLayoutData(new GridData());

			fInsertVariableButton= new Button(composite, SWT.NONE);
			fInsertVariableButton.setLayoutData(getButtonGridData(fInsertVariableButton));
			fInsertVariableButton.setText(TemplatesMessages.EditTemplateDialog_insert_variable);
			fInsertVariableButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					fPatternEditor.getTextWidget().setFocus();
					fPatternEditor.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {}
			});

			fDescriptionText.setText(fOriginalTemplate.getDescription());
			if (fIsNameModifiable) {
				fNameText.setText(fOriginalTemplate.getName());
				fNameText.addModifyListener(listener);
				fContextCombo.select(getIndex(fOriginalTemplate.getContextTypeId()));
			} else {
				fPatternEditor.getControl().setFocus();
			}
			initializeActions();

			applyDialogFont(parent);
			return composite;
		}

		private void doTextWidgetChanged(Widget w) {
			if (w == fNameText) {
				fSuppressError= false;
				updateButtons();
			} else if (w == fContextCombo) {
				String contextId= getContextId();
				fTemplateProcessor.setContextType(fContextTypeRegistry.getContextType(contextId));
			} else if (w == fDescriptionText) {
				// oh, nothing
			}
		}

		private String getContextId() {
			if (fContextCombo != null && !fContextCombo.isDisposed()) {
				String name= fContextCombo.getText();
				for (String[] fContextType : fContextTypes) {
					if (name.equals(fContextType[1])) {
						return fContextType[0];
					}
				}
			}

			return fOriginalTemplate.getContextTypeId();
		}

		private void doSourceChanged(IDocument document) {
			String text= document.get();
			fValidationStatus.setOK();
			TemplateContextType contextType= fContextTypeRegistry.getContextType(getContextId());
			if (contextType != null) {
				try {
					contextType.validate(text);
				} catch (TemplateException e) {
					fValidationStatus.setError(e.getLocalizedMessage());
				}
			}

			updateAction(ITextEditorActionConstants.UNDO);
			updateButtons();
		}

		/**
		 * Return the grid data for the button.
		 *
		 * @param button the button
		 * @return the grid data
		 */
		private static GridData getButtonGridData(Button button) {
			GridData data= new GridData(GridData.FILL_HORIZONTAL);
			// TODO get some button hints.
//			data.heightHint= SWTUtil.getButtonHeightHint(button);

			return data;
		}

		private static Label createLabel(Composite parent, String name) {
			Label label= new Label(parent, SWT.NULL);
			label.setText(name);
			label.setLayoutData(new GridData());

			return label;
		}

		private static Text createText(Composite parent) {
			Text text= new Text(parent, SWT.BORDER);
			text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			return text;
		}

		private static Button createCheckbox(Composite parent, String name) {
			Button button= new Button(parent, SWT.CHECK);
			button.setText(name);
			button.setLayoutData(new GridData());

			return button;
		}

		private SourceViewer createEditor(Composite parent, String pattern) {
			SourceViewer viewer= createViewer(parent);
			viewer.setEditable(true);

			IDocument document= viewer.getDocument();
			if (document != null) {
				document.set(pattern);
			} else {
				document= new Document(pattern);
				viewer.setDocument(document);
			}

			int nLines= document.getNumberOfLines();
			if (nLines < 5) {
				nLines= 5;
			} else if (nLines > 12) {
				nLines= 12;
			}

			Control control= viewer.getControl();
			GridData data= new GridData(GridData.FILL_BOTH);
			data.widthHint= convertWidthInCharsToPixels(80);
			data.heightHint= convertHeightInCharsToPixels(nLines);
			control.setLayoutData(data);

			viewer.addTextListener(event -> {
				if (event.getDocumentEvent() != null) {
					doSourceChanged(event.getDocumentEvent().getDocument());
				}
			});

			viewer.addSelectionChangedListener(event -> updateSelectionDependentActions());

			return viewer;
		}

		/**
		 * Creates the viewer to be used to display the pattern. Subclasses may override.
		 *
		 * @param parent the parent composite of the viewer
		 * @return a configured <code>SourceViewer</code>
		 */
		protected SourceViewer createViewer(Composite parent) {
			SourceViewer viewer= new SourceViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
			SourceViewerConfiguration configuration= new SourceViewerConfiguration() {
				@Override
				public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {

					ContentAssistant assistant= new ContentAssistant();
					assistant.enableAutoActivation(true);
					assistant.enableAutoInsert(true);
					assistant.setContentAssistProcessor(fTemplateProcessor, IDocument.DEFAULT_CONTENT_TYPE);
					return assistant;
				}
			};
			viewer.configure(configuration);
			return viewer;
		}

		private void initializeActions() {
			final ArrayList<IHandlerActivation> handlerActivations= new ArrayList<>(3);
			final IHandlerService handlerService= PlatformUI.getWorkbench().getAdapter(IHandlerService.class);
			final Expression expression= new ActiveShellExpression(fPatternEditor.getControl().getShell());

			getShell().addDisposeListener(e -> handlerService.deactivateHandlers(handlerActivations));

			fPatternEditor.getTextWidget().addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					handlerService.deactivateHandlers(handlerActivations);
				}

				@Override
				public void focusGained(FocusEvent e) {
					IAction action= fGlobalActions.get(ITextEditorActionConstants.REDO);
					handlerActivations.add(handlerService.activateHandler(IWorkbenchCommandConstants.EDIT_REDO, new ActionHandler(action), expression));
					action= fGlobalActions.get(ITextEditorActionConstants.UNDO);
					handlerActivations.add(handlerService.activateHandler(IWorkbenchCommandConstants.EDIT_UNDO, new ActionHandler(action), expression));
					action= fGlobalActions.get(ITextEditorActionConstants.CONTENT_ASSIST);
					handlerActivations.add(handlerService.activateHandler(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, new ActionHandler(action), expression));
				}
			});

			TextViewerAction action= new TextViewerAction(fPatternEditor, ITextOperationTarget.UNDO);
			action.setActionDefinitionId(ActionFactory.UNDO.getCommandId());
			action.setText(TemplatesMessages.EditTemplateDialog_undo);
			fGlobalActions.put(ITextEditorActionConstants.UNDO, action);

			action= new TextViewerAction(fPatternEditor, ITextOperationTarget.REDO);
			action.setActionDefinitionId(ActionFactory.REDO.getCommandId());
			action.setText(TemplatesMessages.EditTemplateDialog_redo);
			fGlobalActions.put(ITextEditorActionConstants.REDO, action);

			action = new TextViewerAction(fPatternEditor, ITextOperationTarget.CUT);
			action.setActionDefinitionId(ActionFactory.CUT.getCommandId());
			action.setText(TemplatesMessages.EditTemplateDialog_cut);
			fGlobalActions.put(ITextEditorActionConstants.CUT, action);

			action = new TextViewerAction(fPatternEditor, ITextOperationTarget.COPY);
			action.setActionDefinitionId(ActionFactory.COPY.getCommandId());
			action.setText(TemplatesMessages.EditTemplateDialog_copy);
			fGlobalActions.put(ITextEditorActionConstants.COPY, action);

			action= new TextViewerAction(fPatternEditor, ITextOperationTarget.PASTE);
			action.setActionDefinitionId(ActionFactory.PASTE.getCommandId());
			action.setText(TemplatesMessages.EditTemplateDialog_paste);
			fGlobalActions.put(ITextEditorActionConstants.PASTE, action);

			action= new TextViewerAction(fPatternEditor, ITextOperationTarget.SELECT_ALL);
			action.setActionDefinitionId(ActionFactory.SELECT_ALL.getCommandId());
			action.setText(TemplatesMessages.EditTemplateDialog_select_all);
			fGlobalActions.put(ITextEditorActionConstants.SELECT_ALL, action);

			action= new TextViewerAction(fPatternEditor, ISourceViewer.CONTENTASSIST_PROPOSALS);
			action.setText(TemplatesMessages.EditTemplateDialog_content_assist);
			fGlobalActions.put(ITextEditorActionConstants.CONTENT_ASSIST, action);

			fSelectionActions.add(ITextEditorActionConstants.CUT);
			fSelectionActions.add(ITextEditorActionConstants.COPY);
			fSelectionActions.add(ITextEditorActionConstants.PASTE);

			// create context menu
			MenuManager manager= new MenuManager(null, null);
			manager.setRemoveAllWhenShown(true);
			manager.addMenuListener(this::fillContextMenu);

			StyledText text= fPatternEditor.getTextWidget();
			Menu menu= manager.createContextMenu(text);
			text.setMenu(menu);
		}

		private void fillContextMenu(IMenuManager menu) {
			menu.add(new GroupMarker(ITextEditorActionConstants.GROUP_UNDO));
			menu.appendToGroup(ITextEditorActionConstants.GROUP_UNDO, fGlobalActions.get(ITextEditorActionConstants.UNDO));
			menu.appendToGroup(ITextEditorActionConstants.GROUP_UNDO, fGlobalActions.get(ITextEditorActionConstants.REDO));

			menu.add(new Separator(ITextEditorActionConstants.GROUP_EDIT));
			menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, fGlobalActions.get(ITextEditorActionConstants.CUT));
			menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, fGlobalActions.get(ITextEditorActionConstants.COPY));
			menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, fGlobalActions.get(ITextEditorActionConstants.PASTE));
			menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, fGlobalActions.get(ITextEditorActionConstants.SELECT_ALL));

			menu.add(new Separator("templates")); //$NON-NLS-1$
			menu.appendToGroup("templates", fGlobalActions.get("ContentAssistProposal")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		private void updateSelectionDependentActions() {
			Iterator<String> iterator= fSelectionActions.iterator();
			while (iterator.hasNext()) {
				updateAction(iterator.next());
			}
		}

		private void updateAction(String actionId) {
			IAction action= fGlobalActions.get(actionId);
			if (action instanceof IUpdate) {
				((IUpdate) action).update();
			}
		}

		private int getIndex(String contextid) {

			if (contextid == null) {
				return -1;
			}

			for (int i= 0; i < fContextTypes.length; i++) {
				if (contextid.equals(fContextTypes[i][0])) {
					return i;
				}
			}
			return -1;
		}

		private void updateButtons() {
			StatusInfo status;

			boolean valid= fNameText == null || !fNameText.getText().trim().isEmpty();
			if (!valid) {
				status = new StatusInfo();
				if (!fSuppressError) {
					status.setError(TemplatesMessages.EditTemplateDialog_error_noname);
				}
			} else if (!isValidPattern(fPatternEditor.getDocument().get())) {
				status = new StatusInfo();
				if (!fSuppressError) {
					status.setError(TemplatesMessages.EditTemplateDialog_error_invalidPattern);
				}
	 		} else {
	 			status= fValidationStatus;
	 		}
			updateStatus(status);
		}

		/**
		 * Validates the pattern.
		 * <p>
		 * The default implementation rejects invalid XML characters.
		 * </p>
		 *
		 * @param pattern the pattern to verify
		 * @return <code>true</code> if the pattern is valid
		 * @since 3.7 protected, before it was private
		 */
		protected boolean isValidPattern(String pattern) {
			for (int i= 0; i < pattern.length(); i++) {
				char ch= pattern.charAt(i);
				if (!(ch == 9 || ch == 10 || ch == 13 || ch >= 32)) {
					return false;
				}
			}
			return true;
		}

		/*
		 * @since 3.1
		 */
		@Override
		protected void okPressed() {
			String name= fNameText == null ? fOriginalTemplate.getName() : fNameText.getText();
			boolean isAutoInsertable= fAutoInsertCheckbox != null && fAutoInsertCheckbox.getSelection();
			fNewTemplate= new Template(name, fDescriptionText.getText(), getContextId(), fPatternEditor.getDocument().get(), isAutoInsertable);
			super.okPressed();
		}

		/**
		 * Returns the created template.
		 *
		 * @return the created template
		 * @since 3.1
		 */
		public Template getTemplate() {
			return fNewTemplate;
		}

		/**
		 * Returns the content assist processor that
		 * suggests template variables.
		 *
		 * @return the processor to suggest variables
		 * @since 3.3
		 */
		protected IContentAssistProcessor getTemplateProcessor() {
			return fTemplateProcessor;
		}

		@Override
		protected IDialogSettings getDialogBoundsSettings() {
			String sectionName= getClass().getName() + "_dialogBounds"; //$NON-NLS-1$
			IDialogSettings settings = PlatformUI
					.getDialogSettingsProvider(FrameworkUtil.getBundle(TemplatePreferencePage.class))
					.getDialogSettings();
			IDialogSettings section= settings.getSection(sectionName);
			if (section == null) {
				section= settings.addNewSection(sectionName);
			}
			return section;
		}

	}


	/**
	 * Label provider for templates.
	 */
	private class TemplateLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			TemplatePersistenceData data = (TemplatePersistenceData) element;
			Template template= data.getTemplate();

			switch (columnIndex) {
				case 0:
					return template.getName();
				case 1:
					TemplateContextType type= fContextTypeRegistry.getContextType(template.getContextTypeId());
					if (type != null) {
						return type.getName();
					}
					return template.getContextTypeId();
				case 2:
					return template.getDescription();
				case 3:
					return template.isAutoInsertable() ? TemplatesMessages.TemplatePreferencePage_on : "";  //$NON-NLS-1$
				default:
					return ""; //$NON-NLS-1$
			}
		}
	}


	/** Qualified key for formatter preference. */
	private static final String DEFAULT_FORMATTER_PREFERENCE_KEY= "org.eclipse.ui.texteditor.templates.preferences.format_templates"; //$NON-NLS-1$

	/** The table presenting the templates. */
	private CheckboxTableViewer fTableViewer;

	/* buttons */
	private Button fAddButton;
	private Button fEditButton;
	private Button fImportButton;
	private Button fExportButton;
	private Button fRemoveButton;
	private Button fRestoreButton;
	private Button fRevertButton;

	/** The viewer displays the pattern of selected template. */
	private SourceViewer fPatternViewer;
	/** Format checkbox. This gets conditionally added. */
	private Button fFormatButton;
	/** The store for our templates. */
	private TemplateStore fTemplateStore;
	/** The context type registry. */
	private ContextTypeRegistry fContextTypeRegistry;

	private TextViewerAction fPatternViewerCopyAction;

	private TextViewerAction fPatternViewerSelectAllAction;

	/**
	 * Creates a new template preference page.
	 */
	protected TemplatePreferencePage() {
		super();

		setDescription(TemplatesMessages.TemplatePreferencePage_message);
	}

	/**
	 * Returns the template store.
	 *
	 * @return the template store
	 */
	public TemplateStore getTemplateStore() {
		return fTemplateStore;
	}

	/**
	 * Returns the context type registry.
	 *
	 * @return the context type registry
	 */
	public ContextTypeRegistry getContextTypeRegistry() {
		return fContextTypeRegistry;
	}

	/**
	 * Sets the template store.
	 *
	 * @param store the new template store
	 */
	public void setTemplateStore(TemplateStore store) {
		fTemplateStore= store;
	}

	/**
	 * Sets the context type registry.
	 *
	 * @param registry the new context type registry
	 */
	public void setContextTypeRegistry(ContextTypeRegistry registry) {
		fContextTypeRegistry= registry;
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite ancestor) {
		Composite parent= new Composite(ancestor, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		parent.setLayout(layout);

		Composite innerParent= new Composite(parent, SWT.NONE);
		GridLayout innerLayout= new GridLayout();
		innerLayout.numColumns= 2;
		innerLayout.marginHeight= 0;
		innerLayout.marginWidth= 0;
		innerParent.setLayout(innerLayout);
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan= 2;
		innerParent.setLayoutData(gd);

		Composite tableComposite= new Composite(innerParent, SWT.NONE);
		GridData data= new GridData(GridData.FILL_BOTH);
		data.widthHint= 360;
		data.heightHint= convertHeightInCharsToPixels(10);
		tableComposite.setLayoutData(data);

		ColumnLayout columnLayout= new ColumnLayout();
		tableComposite.setLayout(columnLayout);
		Table table= new Table(tableComposite, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GC gc= new GC(getShell());
		gc.setFont(JFaceResources.getDialogFont());

		TemplateViewerComparator viewerComparator= new TemplateViewerComparator();

		TableColumn column1= new TableColumn(table, SWT.NONE);
		column1.setText(TemplatesMessages.TemplatePreferencePage_column_name);
		int minWidth= computeMinimumColumnWidth(gc, TemplatesMessages.TemplatePreferencePage_column_name);
		columnLayout.addColumnData(new ColumnWeightData(2, minWidth, true));
		column1.addSelectionListener(new TemplateColumnSelectionAdapter(column1, 0, viewerComparator));

		TableColumn column2= new TableColumn(table, SWT.NONE);
		column2.setText(TemplatesMessages.TemplatePreferencePage_column_context);
		minWidth= computeMinimumContextColumnWidth(gc);
		columnLayout.addColumnData(new ColumnWeightData(1, minWidth, true));
		column2.addSelectionListener(new TemplateColumnSelectionAdapter(column2, 1, viewerComparator));

		TableColumn column3= new TableColumn(table, SWT.NONE);
		column3.setText(TemplatesMessages.TemplatePreferencePage_column_description);
		minWidth= computeMinimumColumnWidth(gc, TemplatesMessages.TemplatePreferencePage_column_description);
		columnLayout.addColumnData(new ColumnWeightData(3, minWidth, true));
		column3.addSelectionListener(new TemplateColumnSelectionAdapter(column3, 2, viewerComparator));

		TableColumn column4= new TableColumn(table, SWT.NONE);
		column4.setAlignment(SWT.CENTER);
		column4.setText(TemplatesMessages.TemplatePreferencePage_column_autoinsert);
		minWidth= computeMinimumColumnWidth(gc, TemplatesMessages.TemplatePreferencePage_column_autoinsert);
		minWidth= Math.max(minWidth, computeMinimumColumnWidth(gc, TemplatesMessages.TemplatePreferencePage_on));
		columnLayout.addColumnData(new ColumnPixelData(minWidth, false, false));
		column4.addSelectionListener(new TemplateColumnSelectionAdapter(column4, 3, viewerComparator));

		gc.dispose();

		fTableViewer= new CheckboxTableViewer(table);
		fTableViewer.setLabelProvider(new TemplateLabelProvider());
		fTableViewer.setContentProvider(new TemplateContentProvider());
		fTableViewer.setComparator(viewerComparator);

		// Specify default sorting
		table.setSortColumn(column1);
		table.setSortDirection(viewerComparator.getDirection());

		fTableViewer.addDoubleClickListener(e -> edit());

		fTableViewer.addSelectionChangedListener(e -> selectionChanged1());

		fTableViewer.addCheckStateListener(event -> {
			TemplatePersistenceData d = (TemplatePersistenceData) event.getElement();
			d.setEnabled(event.getChecked());
		});

		BidiUtils.applyTextDirection(fTableViewer.getControl(), BidiUtils.BTD_DEFAULT);

		Composite buttons= new Composite(innerParent, SWT.NONE);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		buttons.setLayout(layout);

		fAddButton= new Button(buttons, SWT.PUSH);
		fAddButton.setText(TemplatesMessages.TemplatePreferencePage_new);
		fAddButton.setLayoutData(getButtonGridData(fAddButton));
		fAddButton.addListener(SWT.Selection, e -> add());

		fEditButton= new Button(buttons, SWT.PUSH);
		fEditButton.setText(TemplatesMessages.TemplatePreferencePage_edit);
		fEditButton.setLayoutData(getButtonGridData(fEditButton));
		fEditButton.addListener(SWT.Selection, e -> edit());

		fRemoveButton= new Button(buttons, SWT.PUSH);
		fRemoveButton.setText(TemplatesMessages.TemplatePreferencePage_remove);
		fRemoveButton.setLayoutData(getButtonGridData(fRemoveButton));
		fRemoveButton.addListener(SWT.Selection, e -> remove());

		createSeparator(buttons);

		fRestoreButton= new Button(buttons, SWT.PUSH);
		fRestoreButton.setText(TemplatesMessages.TemplatePreferencePage_restore);
		fRestoreButton.setLayoutData(getButtonGridData(fRestoreButton));
		fRestoreButton.addListener(SWT.Selection, e -> restoreDeleted());

		fRevertButton= new Button(buttons, SWT.PUSH);
		fRevertButton.setText(TemplatesMessages.TemplatePreferencePage_revert);
		fRevertButton.setLayoutData(getButtonGridData(fRevertButton));
		fRevertButton.addListener(SWT.Selection, e -> revert());

		createSeparator(buttons);

		fImportButton= new Button(buttons, SWT.PUSH);
		fImportButton.setText(TemplatesMessages.TemplatePreferencePage_import);
		fImportButton.setLayoutData(getButtonGridData(fImportButton));
		fImportButton.addListener(SWT.Selection, e -> import_());

		fExportButton= new Button(buttons, SWT.PUSH);
		fExportButton.setText(TemplatesMessages.TemplatePreferencePage_export);
		fExportButton.setLayoutData(getButtonGridData(fExportButton));
		fExportButton.addListener(SWT.Selection, e -> export());

		fPatternViewer= doCreateViewer(parent);

		if (isShowFormatterSetting()) {
			fFormatButton= new Button(parent, SWT.CHECK);
			fFormatButton.setText(TemplatesMessages.TemplatePreferencePage_use_code_formatter);
			GridData gd1= new GridData();
			gd1.horizontalSpan= 2;
			fFormatButton.setLayoutData(gd1);
			fFormatButton.setSelection(getPreferenceStore().getBoolean(getFormatterPreferenceKey()));
		}

		fTableViewer.setInput(fTemplateStore);
		fTableViewer.setAllChecked(false);
		fTableViewer.setCheckedElements(getEnabledTemplates());

		updateButtons();
		Dialog.applyDialogFont(parent);
		innerParent.layout();

		return parent;
	}

	/*
	 * @since 3.2
	 */
	private int computeMinimumColumnWidth(GC gc, String string) {
		return gc.stringExtent(string).x + 10; // pad 10 to accommodate table header trimmings
	}

	/*
	 * @since 3.4
	 */
	private int computeMinimumContextColumnWidth(GC gc) {
		int width= gc.stringExtent(TemplatesMessages.TemplatePreferencePage_column_context).x;
		Iterator<TemplateContextType> iter= getContextTypeRegistry().contextTypes();
		while (iter.hasNext()) {
			TemplateContextType contextType= iter.next();
			width= Math.max(width, gc.stringExtent(contextType.getName()).x);
		}
		return width;
	}

	/**
	 * Creates a separator between buttons.
	 *
	 * @param parent the parent composite
	 * @return a separator
	 */
	private Label createSeparator(Composite parent) {
		Label separator= new Label(parent, SWT.NONE);
		separator.setVisible(false);
		GridData gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.verticalAlignment= GridData.BEGINNING;
		gd.heightHint= 4;
		separator.setLayoutData(gd);
		return separator;
	}


	/**
	 * Returns whether the formatter preference checkbox should be shown.
	 *
	 * @return <code>true</code> if the formatter preference checkbox should
	 *         be shown, <code>false</code> otherwise
	 */
	protected boolean isShowFormatterSetting() {
		return true;
	}

	private TemplatePersistenceData[] getEnabledTemplates() {
		List<TemplatePersistenceData> enabled= new ArrayList<>();
		TemplatePersistenceData[] datas= fTemplateStore.getTemplateData(false);
		for (TemplatePersistenceData data : datas) {
			if (data.isEnabled()) {
				enabled.add(data);
			}
		}
		return enabled.toArray(new TemplatePersistenceData[enabled.size()]);
	}

	private SourceViewer doCreateViewer(Composite parent) {
		Label label= new Label(parent, SWT.NONE);
		label.setText(TemplatesMessages.TemplatePreferencePage_preview);
		GridData data= new GridData();
		data.horizontalSpan= 2;
		label.setLayoutData(data);

		SourceViewer viewer= createViewer(parent);

		viewer.setEditable(false);
		Cursor arrowCursor= viewer.getTextWidget().getDisplay().getSystemCursor(SWT.CURSOR_ARROW);
		viewer.getTextWidget().setCursor(arrowCursor);

		// Don't set caret to 'null' as this causes https://bugs.eclipse.org/293263
//		viewer.getTextWidget().setCaret(null);

		Control control= viewer.getControl();
		data= new GridData(GridData.FILL_BOTH);
		data.horizontalSpan= 2;
		data.heightHint= convertHeightInCharsToPixels(5);
		control.setLayoutData(data);

		addActionsToPatternViewer(viewer);

		return viewer;
	}

	private void addActionsToPatternViewer(SourceViewer viewer) {
		// create actions
		fPatternViewerCopyAction = new TextViewerAction(viewer, ITextOperationTarget.COPY);
		fPatternViewerCopyAction.setActionDefinitionId(ActionFactory.COPY.getCommandId());
		fPatternViewerCopyAction.setText(TemplatesMessages.EditTemplateDialog_copy);

		fPatternViewerSelectAllAction = new TextViewerAction(viewer, ITextOperationTarget.SELECT_ALL);
		fPatternViewerSelectAllAction.setActionDefinitionId(ActionFactory.SELECT_ALL.getCommandId());
		fPatternViewerSelectAllAction.setText(TemplatesMessages.EditTemplateDialog_select_all);

		viewer.addSelectionChangedListener(this::updateCopyAction);
		// create context menu
		MenuManager manager = new MenuManager(null, null);
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(this::fillPatternViewerContextMenu);

		StyledText text = viewer.getTextWidget();
		Menu menu = manager.createContextMenu(text);
		text.setMenu(menu);
	}

	private void fillPatternViewerContextMenu(IMenuManager menu) {
		menu.add(new Separator(ITextEditorActionConstants.GROUP_EDIT));
		menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, fPatternViewerCopyAction);
		menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, fPatternViewerSelectAllAction);
	}

	/**
	 * @param event The event
	 */
	private void updateCopyAction(SelectionChangedEvent event) {
		if (fPatternViewerCopyAction != null) {
			fPatternViewerCopyAction.update();
		}
	}

	/**
	 * Creates, configures and returns a source viewer to present the template
	 * pattern on the preference page. Clients may override to provide a custom
	 * source viewer featuring e.g. syntax coloring.
	 *
	 * @param parent the parent control
	 * @return a configured source viewer
	 */
	protected SourceViewer createViewer(Composite parent) {
		SourceViewer viewer= new SourceViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		SourceViewerConfiguration configuration= new SourceViewerConfiguration();
		viewer.configure(configuration);
		IDocument document= new Document();
		viewer.setDocument(document);
		return viewer;
	}

	/**
	 * Return the grid data for the button.
	 *
	 * @param button the button
	 * @return the grid data
	 */
	private static GridData getButtonGridData(Button button) {
		GridData data= new GridData(GridData.FILL_HORIZONTAL);
		// TODO replace SWTUtil
//		data.widthHint= SWTUtil.getButtonWidthHint(button);
//		data.heightHint= SWTUtil.getButtonHeightHint(button);

		return data;
	}

	private void selectionChanged1() {
		updateViewerInput();
		updateButtons();
	}

	/**
	 * Updates the pattern viewer.
	 */
	protected void updateViewerInput() {
		IStructuredSelection selection = fTableViewer.getStructuredSelection();

		if (selection.size() == 1) {
			TemplatePersistenceData data= (TemplatePersistenceData) selection.getFirstElement();
			Template template= data.getTemplate();
			fPatternViewer.getDocument().set(template.getPattern());
		} else {
			fPatternViewer.getDocument().set(""); //$NON-NLS-1$
		}
	}

	/**
	 * Updates the buttons.
	 */
	protected void updateButtons() {
		IStructuredSelection selection = fTableViewer.getStructuredSelection();
		int selectionCount= selection.size();
		int itemCount= fTableViewer.getTable().getItemCount();
		boolean canRestore= fTemplateStore.getTemplateData(true).length != fTemplateStore.getTemplateData(false).length;
		boolean canRevert= false;
		for (Iterator<?> it= selection.iterator(); it.hasNext();) {
			TemplatePersistenceData data= (TemplatePersistenceData) it.next();
			if (data.isModified()) {
				canRevert= true;
				break;
			}
		}

		fEditButton.setEnabled(selectionCount == 1);
		fExportButton.setEnabled(selectionCount > 0);
		fRemoveButton.setEnabled(selectionCount > 0 && selectionCount <= itemCount);
		fRestoreButton.setEnabled(canRestore);
		fRevertButton.setEnabled(canRevert);
	}

	private void add() {

		Iterator<TemplateContextType> it= fContextTypeRegistry.contextTypes();
		if (it.hasNext()) {
			Template template= new Template("", "", it.next().getId(), "", true);   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			Template newTemplate= editTemplate(template, false, true);
			if (newTemplate != null) {
				TemplatePersistenceData data= new TemplatePersistenceData(newTemplate, true);
				fTemplateStore.add(data);
				fTableViewer.refresh();
				fTableViewer.setChecked(data, true);
				fTableViewer.setSelection(new StructuredSelection(data));
			}
		}
	}

	/**
	 * Creates the edit dialog. Subclasses may override this method to provide a
	 * custom dialog.
	 *
	 * @param template the template being edited
	 * @param edit whether the dialog should be editable
	 * @param isNameModifiable whether the template name may be modified
	 * @return an <code>EditTemplateDialog</code> which will be opened.
	 * @deprecated not called any longer as of 3.1 - use {@link #editTemplate(Template, boolean, boolean)}
	 */
	@Deprecated
	protected Dialog createTemplateEditDialog(Template template, boolean edit, boolean isNameModifiable) {
		return new EditTemplateDialog(getShell(), template, edit, isNameModifiable, fContextTypeRegistry);
	}

	/**
	 * Creates the edit dialog. Subclasses may override this method to provide a
	 * custom dialog.
	 *
	 * @param template the template being edited
	 * @param edit whether the dialog should be editable
	 * @param isNameModifiable whether the template name may be modified
	 * @return the created or modified template, or <code>null</code> if the edition failed
	 * @since 3.1
	 */
	protected Template editTemplate(Template template, boolean edit, boolean isNameModifiable) {
		EditTemplateDialog dialog= new EditTemplateDialog(getShell(), template, edit, isNameModifiable, fContextTypeRegistry);
		if (dialog.open() == Window.OK) {
			return dialog.getTemplate();
		}
		return null;
	}

	private void edit() {
		IStructuredSelection selection = fTableViewer.getStructuredSelection();

		Object[] objects= selection.toArray();
		if ((objects == null) || (objects.length != 1)) {
			return;
		}

		TemplatePersistenceData data= (TemplatePersistenceData) selection.getFirstElement();
		edit(data);
	}

	private void edit(TemplatePersistenceData data) {
		Template oldTemplate= data.getTemplate();
		Template newTemplate= editTemplate(new Template(oldTemplate), true, true);
		if (newTemplate != null) {

			if (!newTemplate.getName().equals(oldTemplate.getName()) &&
					openCreateNewOrRenameDialog())
			{
				data= new TemplatePersistenceData(newTemplate, true);
				fTemplateStore.add(data);
				fTableViewer.refresh();
			} else {
				data.setTemplate(newTemplate);
				fTableViewer.refresh(data);
			}
			selectionChanged1();
			fTableViewer.setChecked(data, data.isEnabled());
			fTableViewer.setSelection(new StructuredSelection(data));
		}
	}

	private void import_() {
		FileDialog dialog= new FileDialog(getShell());
		dialog.setText(TemplatesMessages.TemplatePreferencePage_import_title);
		dialog.setFilterExtensions(new String[] {TemplatesMessages.TemplatePreferencePage_import_extension});
		String path= dialog.open();

		if (path == null) {
			return;
		}

		try {
			ArrayList<TemplatePersistenceData> selection= new ArrayList<>();
			TemplateReaderWriter reader= new TemplateReaderWriter();
			File file= new File(path);
			if (file.exists()) {
				try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
					TemplatePersistenceData[] datas= reader.read(input, null);
					for (TemplatePersistenceData data : datas) {
						fTemplateStore.add(data);
						String id= data.getId();
						if (id == null) {
							selection.add(data);
						} else {
							data= fTemplateStore.getTemplateData(id);
							if (data != null) {
								selection.add(data);
							}
						}
					}
				}
			}

			fTableViewer.refresh();
			fTableViewer.setAllChecked(false);
			fTableViewer.setCheckedElements(getEnabledTemplates());
			fTableViewer.setSelection(new StructuredSelection(selection), true);
			selectionChanged1();

		} catch (FileNotFoundException e) {
			openReadErrorDialog(e);
		} catch (IOException e) {
			openReadErrorDialog(e);
		}
	}

	private void export() {
		IStructuredSelection selection = fTableViewer.getStructuredSelection();
		Object[] templates= selection.toArray();

		TemplatePersistenceData[] datas= new TemplatePersistenceData[templates.length];
		for (int i= 0; i != templates.length; i++) {
			datas[i]= (TemplatePersistenceData) templates[i];
		}

		export(datas);
	}

	private void export(TemplatePersistenceData[] templates) {
		FileDialog dialog= new FileDialog(getShell(), SWT.SAVE);
		dialog.setText(TemplatesMessages.TemplatePreferencePage_export_title);
		dialog.setFilterExtensions(new String[] {TemplatesMessages.TemplatePreferencePage_export_extension});
		dialog.setFileName(TemplatesMessages.TemplatePreferencePage_export_filename);
		String path= dialog.open();

		if (path == null) {
			return;
		}

		File file= new File(path);

		if (file.isHidden()) {
			String title= TemplatesMessages.TemplatePreferencePage_export_error_title;
			String message= NLSUtility.format(TemplatesMessages.TemplatePreferencePage_export_error_hidden, file.getAbsolutePath());
			MessageDialog.openError(getShell(), title, message);
			return;
		}

		if (file.exists() && !file.canWrite()) {
			String title= TemplatesMessages.TemplatePreferencePage_export_error_title;
			String message= NLSUtility.format(TemplatesMessages.TemplatePreferencePage_export_error_canNotWrite, file.getAbsolutePath());
			MessageDialog.openError(getShell(), title, message);
			return;
		}

		if (!file.exists() || confirmOverwrite(file)) {
			try (OutputStream output = new BufferedOutputStream(new FileOutputStream(file))) {
				TemplateReaderWriter writer= new TemplateReaderWriter();
				writer.save(templates, output);
			} catch (IOException e) {
				openWriteErrorDialog(e);
			}
		}
	}

	private boolean confirmOverwrite(File file) {
		return MessageDialog.openQuestion(getShell(),
			TemplatesMessages.TemplatePreferencePage_export_exists_title,
			NLSUtility.format(TemplatesMessages.TemplatePreferencePage_export_exists_message, file.getAbsolutePath()));
	}

	private void remove() {
		IStructuredSelection selection = fTableViewer.getStructuredSelection();

		Iterator<?> elements= selection.iterator();
		while (elements.hasNext()) {
			TemplatePersistenceData data= (TemplatePersistenceData) elements.next();
			fTemplateStore.delete(data);
		}

		fTableViewer.refresh();
	}

	private void restoreDeleted() {
		TemplatePersistenceData[] oldTemplates= fTemplateStore.getTemplateData(false);
		fTemplateStore.restoreDeleted();
		TemplatePersistenceData[] newTemplates= fTemplateStore.getTemplateData(false);
		fTableViewer.refresh();
		fTableViewer.setCheckedElements(getEnabledTemplates());
		ArrayList<TemplatePersistenceData> selection= new ArrayList<>();
		selection.addAll(Arrays.asList(newTemplates));
		selection.removeAll(Arrays.asList(oldTemplates));
		fTableViewer.setSelection(new StructuredSelection(selection), true);
		selectionChanged1();
	}

	private void revert() {
		IStructuredSelection selection = fTableViewer.getStructuredSelection();

		Iterator<?> elements= selection.iterator();
		while (elements.hasNext()) {
			TemplatePersistenceData data= (TemplatePersistenceData) elements.next();
			data.revert();
			fTableViewer.setChecked(data, data.isEnabled());
		}

		selectionChanged1();
		fTableViewer.refresh();
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			setTitle(TemplatesMessages.TemplatePreferencePage_title);
		}
	}

	@Override
	protected void performDefaults() {
		if (isShowFormatterSetting()) {
			IPreferenceStore prefs= getPreferenceStore();
			fFormatButton.setSelection(prefs.getDefaultBoolean(getFormatterPreferenceKey()));
		}

		fTemplateStore.restoreDefaults(false);

		// refresh
		fTableViewer.refresh();
		fTableViewer.setAllChecked(false);
		fTableViewer.setCheckedElements(getEnabledTemplates());
	}

	@Override
	public boolean performOk() {
		if (isShowFormatterSetting()) {
			IPreferenceStore prefs= getPreferenceStore();
			prefs.setValue(getFormatterPreferenceKey(), fFormatButton.getSelection());
		}

		try {
			fTemplateStore.save();
		} catch (IOException e) {
			openWriteErrorDialog(e);
		}

		return super.performOk();
	}

	/**
	 * Returns the key to use for the formatter preference.
	 *
	 * @return the formatter preference key
	 */
	protected String getFormatterPreferenceKey() {
		return DEFAULT_FORMATTER_PREFERENCE_KEY;
	}

	@Override
	public boolean performCancel() {
		try {
			fTemplateStore.load();
		} catch (IOException e) {
			openReadErrorDialog(e);
			return false;
		}
		return super.performCancel();
	}

	/*
	 * @since 3.2
	 */
	private void openReadErrorDialog(IOException ex) {
		IStatus status= new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, IStatus.OK, "Failed to read templates.", ex); //$NON-NLS-1$
		TextEditorPlugin.getDefault().getLog().log(status);
		String title= TemplatesMessages.TemplatePreferencePage_error_read_title;
		String message= TemplatesMessages.TemplatePreferencePage_error_read_message;
		MessageDialog.openError(getShell(), title, message);
	}

	/*
	 * @since 3.2
	 */
	private void openWriteErrorDialog(IOException ex) {
		IStatus status= new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, IStatus.OK, "Failed to write templates.", ex); //$NON-NLS-1$
		TextEditorPlugin.getDefault().getLog().log(status);
		String title= TemplatesMessages.TemplatePreferencePage_error_write_title;
		String message= TemplatesMessages.TemplatePreferencePage_error_write_message;
		MessageDialog.openError(getShell(), title, message);
	}

	private boolean openCreateNewOrRenameDialog() {
		MessageDialog dialog = new MessageDialog(getShell(),
				TemplatesMessages.TemplatePreferencePage_question_create_new_title, null,
				TemplatesMessages.TemplatePreferencePage_question_create_new_message, MessageDialog.QUESTION, 0,
				TemplatesMessages.TemplatePreferencePage_question_create_new_button_create,
				TemplatesMessages.TemplatePreferencePage_question_create_new_button_rename);
		return dialog.open() == 0;
	}

	protected SourceViewer getViewer() {
		return fPatternViewer;
	}

	protected TableViewer getTableViewer() {
		return fTableViewer;
	}

	private final class TemplateViewerComparator extends ViewerComparator {

		private int fSortColumn;

		private int fSortOrder; // 1 = asc, -1 = desc

		public TemplateViewerComparator() {
			fSortColumn= 0;
			fSortOrder= 1;
		}

		/**
		 * Returns the {@linkplain SWT} style constant for the sort direction.
		 *
		 * @return {@link SWT#DOWN} for asc sorting, {@link SWT#UP} otherwise
		 */
		public int getDirection() {
			return fSortOrder == 1 ? SWT.DOWN : SWT.UP;
		}

		/**
		 * Sets the sort column. If the newly set sort column equals the previous set sort column,
		 * the sort direction changes.
		 *
		 * @param column New sort column
		 */
		public void setColumn(int column) {
			if (column == fSortColumn) {
				fSortOrder*= -1;
			} else {
				fSortColumn= column;
				fSortOrder= 1;
			}
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {

			if (viewer instanceof TableViewer) {
				IBaseLabelProvider baseLabel= ((TableViewer)viewer).getLabelProvider();

				String left= ((TemplateLabelProvider)baseLabel).getColumnText(e1, fSortColumn);
				String right= ((TemplateLabelProvider)baseLabel).getColumnText(e2, fSortColumn);
				int sortResult= getComparator().compare(left, right);
				return sortResult * fSortOrder;
			}

			return super.compare(viewer, e1, e2);
		}
	}

	private final class TemplateColumnSelectionAdapter extends SelectionAdapter {

		private final TableColumn fTableColumn;

		private final int fColumnIndex;

		private final TemplateViewerComparator fViewerComparator;

		public TemplateColumnSelectionAdapter(TableColumn column, int index, TemplateViewerComparator vc) {
			fTableColumn= column;
			fColumnIndex= index;
			fViewerComparator= vc;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			fViewerComparator.setColumn(fColumnIndex);
			int dir= fViewerComparator.getDirection();
			fTableViewer.getTable().setSortDirection(dir);
			fTableViewer.getTable().setSortColumn(fTableColumn);
			fTableViewer.refresh();
		}
	}
}
