/*******************************************************************************
 * Copyright (c) 2007, 2012 Dakshinamurthy Karra, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dakshinamurthy Karra (Jalian Systems) - Templates View - https://bugs.eclipse.org/bugs/show_bug.cgi?id=69581
 *     Piotr Maj <pm@jcake.com> - no access to template store and current selection - https://bugs.eclipse.org/bugs/show_bug.cgi?id=296439
 *******************************************************************************/
package org.eclipse.ui.texteditor.templates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.ibm.icu.text.Collator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.texteditor.NLSUtility;
import org.eclipse.ui.internal.texteditor.TextEditorPlugin;
import org.eclipse.ui.part.Page;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorExtension2;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage.EditTemplateDialog;


/**
 * Abstract default implementation for {@link ITemplatesPage}.
 * <p>
 * Clients who are defining an editor may elect to provide a corresponding
 * templates page. This templates page will be presented to the user via the
 * Templates View (the user decides whether their workbench window contains this
 * view) whenever that editor is active. This class should be subclassed by
 * clients.
 * </p>
 * <p>
 * Internally, a AbstractTemplatesPage uses the template store to display different
 * categories. A link to editor mode on the templates page allows to filtering
 * of the categories to only that are supported in this context.
 * </p>
 *
 * @since 3.4
 */
public abstract class AbstractTemplatesPage extends Page implements ITemplatesPage, ITemplatesPageExtension {

	/**
	 * Sashform size
	 */
	private static final String SASH_SIZE_PREF_ID= TextEditorPlugin.PLUGIN_ID
			+ ".templates.templatesPage.sashSize"; //$NON-NLS-1$
	/**
	 * Tree columns widths
	 */
	private static final String COLUMN_NAME_WIDTH_PREF_ID= TextEditorPlugin.PLUGIN_ID
			+ ".templates.templatesPage.nameWidth"; //$NON-NLS-1$
	private static final String COLUMN_DESCRIPTION_WIDTH_PREF_ID= TextEditorPlugin.PLUGIN_ID
			+ ".templates.templatesPage.descriptionWidth"; //$NON-NLS-1$
	/**
	 * Link to editor action setting
	 */
	private static final String LINK_ACTION_PREF_ID= TextEditorPlugin.PLUGIN_ID
			+ ".templates.templatesPage.linkAction"; //$NON-NLS-1$

	/**
	 * Context expand/collapse setting prefix
	 */
	private static final String CONTEXT_COLLAPSE_PREF_ID= TextEditorPlugin.PLUGIN_ID
			+ "templates.templatesPage.context.expand."; //$NON-NLS-1$

	/**
	 * The ID for the popup menu for this templates page
	 */
	private static final String POPUP_MENU_ID= "org.eclipse.ui.texteditor.templates.PopupMenu"; //$NON-NLS-1$


	/**
	 * A post selection changed listener for the editor. Depending on the caret
	 * position updates the templates
	 */
	private final class SelectionChangedListener implements ISelectionChangedListener {
		/*
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		public void selectionChanged(SelectionChangedEvent event) {
			String[] contextTypes= getEditorContextTypeIds();
			if (needUpdate(contextTypes)) {
				fCurrentContextTypeIds= contextTypes;
				updateContextTypes(fCurrentContextTypeIds);
				return;
			}
		}

		/**
		 * Check whether an update of the AbstractTemplatesPage is needed
		 *
		 * @param contextTypes the context types
		 * @return true if update is needed
		 */
		private boolean needUpdate(String[] contextTypes) {
			return fCurrentContextTypeIds == null
					|| fCurrentContextTypeIds.length != contextTypes.length
					|| contextTypeChanged(contextTypes);
		}

		/**
		 * Check whether there is any change in the context types needed
		 *
		 * @param contextTypes the context types
		 * @return true if any of the context types changed
		 */
		private boolean contextTypeChanged(String[] contextTypes) {
			for (int i= 0; i < contextTypes.length; i++) {
				if (!contextTypes[i].equals(fCurrentContextTypeIds[i]))
					return true;
			}
			return false;
		}
	}


	/**
	 * Drop support for the editor linked to this page. When a user drops a
	 * template into the active editor, the template is applied at the drop
	 * position.
	 */
	private final class EditorDropTargetListener extends DropTargetAdapter {
		/*
		 * @see org.eclipse.swt.dnd.DropTargetAdapter#dragEnter(org.eclipse.swt.dnd.DropTargetEvent)
		 */
		public void dragEnter(DropTargetEvent event) {
			if (!TemplatesTransfer.getInstance().isSupportedType(event.currentDataType))
				return;

			event.detail= DND.DROP_COPY;
		}

		/*
		 * @see org.eclipse.swt.dnd.DropTargetAdapter#dragOperationChanged(org.eclipse.swt.dnd.DropTargetEvent)
		 */
		public void dragOperationChanged(DropTargetEvent event) {
			if (!TemplatesTransfer.getInstance().isSupportedType(event.currentDataType))
				return;

			event.detail= DND.DROP_COPY;
		}

		/*
		 * @see org.eclipse.swt.dnd.DropTargetAdapter#dragOver(org.eclipse.swt.dnd.DropTargetEvent)
		 */
		public void dragOver(DropTargetEvent event) {
			if (!TemplatesTransfer.getInstance().isSupportedType(event.currentDataType))
				return;

			event.feedback |= DND.FEEDBACK_SCROLL | DND.FEEDBACK_SELECT;
			event.detail= DND.DROP_NONE;
			TemplatePersistenceData[] selectedTemplates= getSelectedTemplates();
			if (fTextEditor instanceof ITextEditorExtension2 && ((ITextEditorExtension2)fTextEditor).isEditorInputModifiable() && selectedTemplates.length == 1 &&
					isTemplateValidAtLocation(selectedTemplates[0].getTemplate(), new Point(event.x, event.y)))
				event.detail= DND.DROP_COPY;
		}

		/*
		 * @see org.eclipse.swt.dnd.DropTargetAdapter#drop(org.eclipse.swt.dnd.DropTargetEvent)
		 */
		public void drop(DropTargetEvent event) {
			if (!TemplatesTransfer.getInstance().isSupportedType(event.currentDataType))
				return;

			TemplatePersistenceData[] selectedTemplates= getSelectedTemplates();
			insertTemplate(selectedTemplates[0].getTemplate());
			// The highlight of the item is removed once the drop happens -
			// restore it
			fTreeViewer.setSelection(new StructuredSelection(selectedTemplates), true);
		}

	}


	/**
	 * Comparator for the viewer. Sorts the templates by name and then
	 * description and context types by names.
	 */
	private static final class TemplateViewerComparator extends ViewerComparator {
		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public int compare(Viewer viewer, Object object1, Object object2) {
			if ((object1 instanceof TemplatePersistenceData)
					&& (object2 instanceof TemplatePersistenceData)) {
				Template left= ((TemplatePersistenceData) object1).getTemplate();
				Template right= ((TemplatePersistenceData) object2).getTemplate();
				int result= Collator.getInstance().compare(left.getName(), right.getName());
				if (result != 0)
					return result;
				return Collator.getInstance()
						.compare(left.getDescription(), right.getDescription());
			}
			if ((object1 instanceof TemplateContextType)
					&& (object2 instanceof TemplateContextType)) {
				return Collator.getInstance().compare(((TemplateContextType) object1).getName(),
						((TemplateContextType) object1).getName());
			}
			return super.compare(viewer, object1, object2);
		}

		/*
		 * @see org.eclipse.jface.viewers.ViewerComparator#isSorterProperty(java.lang.Object, java.lang.String)
		 */
		public boolean isSorterProperty(Object element, String property) {
			return false;
		}
	}


	/**
	 * Label provider for templates.
	 */
	private final class TemplateLabelProvider extends LabelProvider implements ITableLabelProvider {

		/*
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex != 0)
				return null;
			if (element instanceof TemplateContextType)
				return TemplatesPageImages.get(TemplatesPageImages.IMG_OBJ_CONTEXT);
			return AbstractTemplatesPage.this.getImage(((TemplatePersistenceData) element).getTemplate());
		}

		/*
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof TemplatePersistenceData)
				return getTemplateColumnText((TemplatePersistenceData) element, columnIndex);
			return getContextColumnText((TemplateContextType) element, columnIndex);
		}

		private String getTemplateColumnText(TemplatePersistenceData data, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return data.getTemplate().getName();
			case 1:
				return data.getTemplate().getDescription();
			default:
				return ""; //$NON-NLS-1$
			}
		}

		private String getContextColumnText(TemplateContextType contextType, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return contextType.getName();
			default:
				return ""; //$NON-NLS-1$
			}
		}
	}


	/**
	 * Content provider for templates. Provides all the enabled templates
	 * defined for this editor.
	 */
	private final class TemplatesContentProvider implements ITreeContentProvider {

		/*
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		 */
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof TemplatePersistenceData)
				return new Object[0];
			else if (parentElement instanceof TemplateContextType) {
				TemplateContextType contextType= (TemplateContextType) parentElement;
				return getTemplates(contextType.getId());
			}
			return null;
		}

		private TemplatePersistenceData[] getTemplates(String contextId) {
			List templateList= new ArrayList();
			TemplatePersistenceData[] datas= getTemplateStore().getTemplateData(false);
			for (int i= 0; i < datas.length; i++) {
				if (datas[i].isEnabled() && datas[i].getTemplate().getContextTypeId().equals(contextId))
					templateList.add(datas[i]);
			}
			return (TemplatePersistenceData[]) templateList
					.toArray(new TemplatePersistenceData[templateList.size()]);
		}

		/*
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object element) {
			if (element instanceof TemplatePersistenceData) {
				TemplatePersistenceData templateData= (TemplatePersistenceData) element;
				return getContextTypeRegistry().getContextType(
						templateData.getTemplate().getContextTypeId());
			}
			return null;
		}

		/*
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		public boolean hasChildren(Object parentElement) {
			if (parentElement instanceof TemplatePersistenceData)
				return false;
			else if (parentElement instanceof TemplateContextType) {
				String contextId= ((TemplateContextType) parentElement).getId();

				TemplatePersistenceData[] datas= getTemplateStore().getTemplateData(false);
				if (datas.length <= 0)
					return false;

				for (int i= 0; i < datas.length; i++) {
					if (datas[i].isEnabled() && datas[i].getTemplate().getContextTypeId().equals(contextId))
						return true;
				}
				return false;
			}
			return false;
		}

		/*
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			List contextTypes= new ArrayList();

			for (Iterator iterator= getContextTypeRegistry().contextTypes(); iterator.hasNext();) {
				TemplateContextType contextType= (TemplateContextType) iterator.next();
				if (!fLinkWithEditorAction.isChecked() || isActiveContext(contextType))
					contextTypes.add(contextType);
			}
			return contextTypes.toArray(new TemplateContextType[contextTypes.size()]);
		}

		private boolean isActiveContext(TemplateContextType contextType) {
			return fActiveTypes == null || fActiveTypes.contains(contextType.getId());
		}

		/*
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/*
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}


	/** The text editor. */
	private final ITextEditor fTextEditor;
	/** The source viewer attached to this editor. */
	private final ISourceViewer fViewer;

	/* Listener to monitor changes to template store */
	private IPropertyChangeListener fTemplateChangeListener;

	/* Control for this pagebook view */
	private SashForm fControl;

	/* Actions */
	private Action fInsertAction;
	private Action fAddAction;
	private Action fEditAction;
	private Action fRemoveAction;
	private Action fLinkWithEditorAction;
	private Action fCollapseAllAction;
	private Action fPreferencePageAction;

	/* Clipboard actions */
	private Action fPasteAction;
	private Action fCopyAction;

	/* Current active context types for the editor */
	private List fActiveTypes;

	/* Preference stores */
	private IPreferenceStore fPreferenceStore;

	/* Controls */
	private Tree fTemplatesTree;
	private TreeViewer fTreeViewer;
	private Menu fContextMenu;

	/** Current selection. */
	private TemplatePersistenceData[] fSelectedTemplates= new TemplatePersistenceData[0];

	/** The pattern viewer to be used with this view. */
	private SourceViewer fPatternViewer;

	/* Cached results for avoiding processing while drag-over the editor. */
	private int fCachedOffset;
	private boolean fCachedResult;
	private Point fCachedPosition;

	/** The current context type ids. */
	private String[] fCurrentContextTypeIds;

	/** The selection changed listener to monitor the editor selections. */
	private SelectionChangedListener fSelectionChangedListener;

	/** Paste action support for the editor. */
	private IAction fEditorOldPasteAction;
	private IAction fEditorPasteAction;


	/**
	 * Creates a new templates page.
	 *
	 * @param editor the editor
	 * @param viewer the source viewer
	 */
	protected AbstractTemplatesPage(ITextEditor editor, ISourceViewer viewer) {
		Assert.isLegal(editor != null);
		Assert.isLegal(viewer != null);
		fTextEditor= editor;
		fViewer= viewer;
		setupPreferenceStore();
		setupEditorDropTarget();
		setupSelectionProvider();
		setupPasteOperation();
	}

	/*
	 * @see org.eclipse.ui.part.Page#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite ancestor) {
		setupActions();

		fControl= new SashForm(ancestor, SWT.VERTICAL);

		createTemplateTree(fControl);
		createPatternForm(fControl);

		hookContextMenu();
		initializeDND();
		updateButtons();

		int sashSize= fPreferenceStore.getInt(SASH_SIZE_PREF_ID);
		fControl.setWeights(new int[] { sashSize, 100 - sashSize });
		fTemplateChangeListener= new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				getShell().getDisplay().asyncExec(new Runnable() {
					public void run() {
						refresh();
					}
				});
			}
		};
		getTemplatePreferenceStore().addPropertyChangeListener(fTemplateChangeListener);
		updateContextTypes(getEditorContextTypeIds());
	}

	/*
	 * @see org.eclipse.ui.part.Page#setFocus()
	 */
	public void setFocus() {
	}

	/*
	 * @see org.eclipse.ui.part.Page#getControl()
	 */
	public Control getControl() {
		return fControl;
	}

	/*
	 * @see org.eclipse.ui.part.Page#dispose()
	 */
	public void dispose() {
		ISelectionProvider selectionProvider= fViewer.getSelectionProvider();
		if (selectionProvider instanceof IPostSelectionProvider)
			((IPostSelectionProvider) selectionProvider).removePostSelectionChangedListener(fSelectionChangedListener);
		else
			selectionProvider.removeSelectionChangedListener(fSelectionChangedListener);
		fTextEditor.setAction(ITextEditorActionConstants.PASTE, fEditorOldPasteAction);
		if (fContextMenu != null && !fContextMenu.isDisposed())
			fContextMenu.dispose();
		if (fTemplateChangeListener != null)
			getTemplatePreferenceStore().removePropertyChangeListener(fTemplateChangeListener);
		super.dispose();
	}

	/**
	 * Returns the shell in which this page is displayed.
	 *
	 * @return the shell
	 */
	private Shell getShell() {
		return getSite().getShell();
	}

	/**
	 * Returns the image to be used for the given template.
	 * <p>
	 * Clients can override to provide a different image.</p>
	 *
	 * @param template the template
	 * @return the image, must not be disposed
	 */
	protected Image getImage(Template template) {
		return TemplatesPageImages.get(TemplatesPageImages.IMG_OBJ_TEMPLATE);
	}

	/**
	 * Creates and opens a dialog to edit the given template.
	 * <p
	 * Subclasses may override this method to provide a custom dialog.</p>
	 *
	 * @param template the template being edited
	 * @param edit <code>true</code> if the dialog allows editing
	 * @param isNameModifiable <code>true</code> if the template name may be modified
	 * @return the created or modified template, or <code>null</code> if the editing failed
	 */
	protected Template editTemplate(Template template, boolean edit, boolean isNameModifiable) {
		EditTemplateDialog dialog= new EditTemplateDialog(getShell(), template, edit, isNameModifiable, getContextTypeRegistry());
		if (dialog.open() == Window.OK)
			return dialog.getTemplate();
		return null;
	}

	/**
	 * Update the pattern viewer to show the current template.
	 * <p>
	 * Subclasses can extend this method to update their own pattern viewer.
	 * </p>
	 *
	 * @param template the template
	 */
	protected void updatePatternViewer(Template template) {
		String pattern= template != null ? template.getPattern() : ""; //$NON-NLS-1$
		fPatternViewer.getDocument().set(pattern);
	}

	/**
	 * Creates, configures and returns a source viewer to present the template
	 * pattern on the templates page.
	 * <p>
	 * Clients may override to provide a custom source viewer featuring e.g. syntax coloring.</p>
	 *
	 * @param parent the parent control
	 * @return a configured source viewer
	 */
	protected SourceViewer createPatternViewer(Composite parent) {
		SourceViewer viewer= new SourceViewer(parent, null, null, false, SWT.V_SCROLL | SWT.H_SCROLL);
		SourceViewerConfiguration configuration= new SourceViewerConfiguration();
		viewer.configure(configuration);
		IDocument document= new Document();
		viewer.setDocument(document);
		viewer.setEditable(false);
		return viewer;
	}

	/**
	 * Returns the pattern viewer created by createPatternViewer()
	 *
	 * @return the pattern viewer
	 */
	protected final SourceViewer getPatternViewer() {
		return fPatternViewer;
	}

	/**
	 * The caret position in the editor has moved into a new context type. It is
	 * the subclasses responsibility to see that this is called only when needed
	 * by keeping track of editor contents (eg. partitions).
	 *
	 * @param ids the ids
	 */
	private void updateContextTypes(String[] ids) {
		fActiveTypes= Arrays.asList(ids);
		if (fLinkWithEditorAction != null && fLinkWithEditorAction.isChecked())
			refresh();
	}

	/**
	 * Inserts the given template into the editor.
	 *
	 * @param template the template
	 * @param document the document
	 */
	abstract protected void insertTemplate(Template template, IDocument document);

	/**
	 * Returns the context type registry used in this page.
	 *
	 * @return the context type registry
	 */
	abstract protected ContextTypeRegistry getContextTypeRegistry();

	/**
	 * Returns the template store used in this page.
	 * 
	 * @return the template store
	 * @since 3.6 public, before it was protected
	 */
	abstract public TemplateStore getTemplateStore();

	/**
	 * Returns the preference store used to create the template store returned by
	 * {@link AbstractTemplatesPage#getTemplateStore()}.
	 *
	 * @return the preference store
	 */
	abstract protected IPreferenceStore getTemplatePreferenceStore();

	/**
	 * Returns the Template preference page id to be used by this template page.
	 *
	 * @return id the preference page if or <code>null</code> if none exists
	 */
	abstract protected String getPreferencePageId();

	/**
	 * Returns the context type ids supported at the given document offset.
	 *
	 * @param document the document
	 * @param offset the offset
	 * @return an array of supported context ids
	 */
	protected abstract String[] getContextTypeIds(IDocument document, int offset);

	/**
	 * Returns the context type ids supported at the current
	 * caret position of the editor.
	 *
	 * @return an array with the the supported context type ids
	 */
	private String[] getEditorContextTypeIds() {
		Point selectedRange=fViewer.getSelectedRange();
		int offset= selectedRange.x + selectedRange.y;
		IDocument document= fTextEditor.getDocumentProvider().getDocument(fTextEditor.getEditorInput());
		return getContextTypeIds(document, offset);
	}

	/**
	 * Checks whether the given template is valid for the document at the given
	 * offset and length.
	 *
	 * @param document the document
	 * @param template the template
	 * @param offset the offset
	 * @param length the length
	 * @return <code>true</code> if the template is valid
	 */
	protected abstract boolean isValidTemplate(IDocument document, Template template, int offset, int length);

	/**
	 * Setup the preference store
	 */
	private void setupPreferenceStore() {
		fPreferenceStore= TextEditorPlugin.getDefault().getPreferenceStore();
		fPreferenceStore.setDefault(LINK_ACTION_PREF_ID, true);
		fPreferenceStore.setDefault(SASH_SIZE_PREF_ID, 80);
	}

	/**
	 * Setup the paste operation
	 *
	 * We get the editors Paste operation and sets up a new operation that
	 * checks for the clipboard contents for {@link TemplatesTransfer} data.
	 */
	private void setupPasteOperation() {
		fEditorOldPasteAction= fTextEditor.getAction(ITextEditorActionConstants.PASTE);
		fEditorPasteAction= new Action(TemplatesMessages.TemplatesPage_paste) {
			public void run() {
				Clipboard clipboard= new Clipboard(getShell().getDisplay());
				try {
					Template template= getTemplateFromClipboard(clipboard);
					if (template != null)
						insertTemplate(template);
					else
						fEditorOldPasteAction.run();
				} finally {
					clipboard.dispose();
				}
			}

			public void runWithEvent(Event event) {
				run();
			}

			/**
			 * Convert the clipboard contents into a template
			 *
			 * @param clipboard the clipboard
			 * @return the template or null if contents are not valid
			 */
			private Template getTemplateFromClipboard(Clipboard clipboard) {
				TemplatePersistenceData[] contents= (TemplatePersistenceData[])clipboard
						.getContents(TemplatesTransfer.getInstance());
				if (contents != null && contents.length == 1)
					return contents[0].getTemplate();
				return null;
			}
		};
		fEditorPasteAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_PASTE);
		fTextEditor.setAction(ITextEditorActionConstants.PASTE, fEditorPasteAction);
	}

	/**
	 * Setup a selection listener to monitor the editor
	 */
	private void setupSelectionProvider() {
		ISelectionProvider selectionProvider= fViewer.getSelectionProvider();
		fSelectionChangedListener= new SelectionChangedListener();
		if (selectionProvider instanceof IPostSelectionProvider)
			((IPostSelectionProvider) selectionProvider)
					.addPostSelectionChangedListener(fSelectionChangedListener);
		else
			selectionProvider.addSelectionChangedListener(fSelectionChangedListener);
	}

	/**
	 * Setup the editor site as a drop target.
	 */
	private void setupEditorDropTarget() {
		Control control= (Control)fTextEditor.getAdapter(Control.class);
		if (control == null)
			return;

		DropTarget dropTarget= (DropTarget)control.getData(DND.DROP_TARGET_KEY);
		if (dropTarget == null)
			dropTarget= new DropTarget(control, DND.DROP_COPY);

		Transfer[] currentTransfers= dropTarget.getTransfer();
		int currentLength= currentTransfers.length;
		Transfer[] newTransfers= new Transfer[currentLength + 1];
		System.arraycopy(currentTransfers, 0, newTransfers, 0, currentLength);
		newTransfers[currentLength]= TemplatesTransfer.getInstance();
		dropTarget.setTransfer(newTransfers);

		EditorDropTargetListener editorDropTarget= new EditorDropTargetListener();
		dropTarget.addDropListener(editorDropTarget);
	}

	/**
	 * Setup the menu, context menu and toolbar actions.
	 */
	private void setupActions() {
		createActions();
		IActionBars actionBars= getSite().getActionBars();

		actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), fPasteAction);
		fPasteAction.setActionDefinitionId(ActionFactory.PASTE.getCommandId());
		fPasteAction.setText(TemplatesMessages.TemplatesPage_paste);
		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), fCopyAction);
		fCopyAction.setActionDefinitionId(ActionFactory.COPY.getCommandId());
		fCopyAction.setText(TemplatesMessages.TemplatesPage_copy);
		fillToolbar(actionBars);
		fillMenu(actionBars);
	}

	/**
	 * Create all the actions
	 */
	private void createActions() {
		fInsertAction= new Action(TemplatesMessages.TemplatesPage_insert) {
			public void run() {
				TemplatePersistenceData[] selectedTemplates= getSelectedTemplates();
				insertTemplate(selectedTemplates[0].getTemplate());
			}
		};
		fInsertAction.setImageDescriptor(TemplatesPageImages
				.getDescriptor(TemplatesPageImages.IMG_ELCL_TEMPLATE_INSERT));
		fInsertAction.setDisabledImageDescriptor(TemplatesPageImages
				.getDescriptor(TemplatesPageImages.IMG_DLCL_TEMPLATE_INSERT));
		fInsertAction.setToolTipText(TemplatesMessages.TemplatesPage_insert_tooltip);

		fAddAction= new Action(TemplatesMessages.TemplatesPage_new) {
			public void run() {
				addTemplate();
			}
		};
		fAddAction.setImageDescriptor(TemplatesPageImages
				.getDescriptor(TemplatesPageImages.IMG_ELCL_TEMPLATE_NEW));
		fAddAction.setToolTipText(TemplatesMessages.TemplatesPage_new_tooltip);

		fEditAction= new Action(TemplatesMessages.TemplatesPage_edit) {
			public void run() {
				editTemplate();
			}
		};
		fEditAction.setImageDescriptor(TemplatesPageImages
				.getDescriptor(TemplatesPageImages.IMG_ELCL_TEMPLATE_EDIT));
		fEditAction.setDisabledImageDescriptor(TemplatesPageImages
				.getDescriptor(TemplatesPageImages.IMG_DLCL_TEMPLATE_EDIT));
		fEditAction.setToolTipText(TemplatesMessages.TemplatesPage_edit_tooltip);

		fRemoveAction= new Action(TemplatesMessages.TemplatesPage_remove) {
			public void run() {
				removeTemplates();
			}
		};
		fRemoveAction.setImageDescriptor(TemplatesPageImages
				.getDescriptor(TemplatesPageImages.IMG_DLCL_TEMPLATE_DELETE));
		fRemoveAction.setImageDescriptor(TemplatesPageImages
				.getDescriptor(TemplatesPageImages.IMG_ELCL_TEMPLATE_DELETE));
		fRemoveAction.setToolTipText(TemplatesMessages.TemplatesPage_remove_tooltip);

		fLinkWithEditorAction= new Action(TemplatesMessages.TemplatesPage_link_to_editor,
				IAction.AS_CHECK_BOX) {
			public void run() {
				fPreferenceStore.setValue(LINK_ACTION_PREF_ID, fLinkWithEditorAction.isChecked());
				refresh();
			}
		};
		fLinkWithEditorAction.setImageDescriptor(TemplatesPageImages
				.getDescriptor(TemplatesPageImages.IMG_ELCL_TEMPLATE_LINK));
		fLinkWithEditorAction.setChecked(fPreferenceStore.getBoolean(LINK_ACTION_PREF_ID));
		fLinkWithEditorAction
				.setToolTipText(TemplatesMessages.TemplatesPage_link_to_editor_tooltip);
		fCollapseAllAction= new Action(TemplatesMessages.TemplatesPage_collapse_all) {
			public void run() {
				fTreeViewer.collapseAll();
			}
		};
		fCollapseAllAction.setImageDescriptor(TemplatesPageImages
				.getDescriptor(TemplatesPageImages.IMG_ELCL_TEMPLATE_COLLAPSE_ALL));
		fCollapseAllAction.setToolTipText(TemplatesMessages.TemplatesPage_collapse_all_tooltip);

		if (getPreferencePageId() != null) {
			fPreferencePageAction= new Action(TemplatesMessages.TemplatesPage_preference_page) {
				public void run() {
					showPreferencePage();
				}
			};
			fPreferencePageAction
					.setToolTipText(TemplatesMessages.TemplatesPage_preference_page_tooltip);
		}

		fPasteAction= new Action() {
			public void run() {
				Clipboard clipboard= new Clipboard(getShell().getDisplay());
				try {
					String pattern= ((String)clipboard.getContents(TextTransfer.getInstance()));
					if (pattern != null) {
						final Template template= new Template(createTemplateName(), TemplatesMessages.TemplatesPage_paste_description, getContextTypeId(), pattern.replaceAll("\\$", "\\$\\$"), true); //$NON-NLS-1$//$NON-NLS-2$
						getShell().getDisplay().asyncExec(new Runnable() {
							public void run() {
								addTemplate(template);
							}
						});
						return;
					}
					TemplatePersistenceData[] templates= (TemplatePersistenceData[])clipboard.getContents(TemplatesTransfer.getInstance());
					if (templates != null)
						copyTemplates(templates, getContextTypeId());
				} finally {
					clipboard.dispose();
				}

			}
		};

		fCopyAction= new Action() {
			public void run() {
				Clipboard clipboard= new Clipboard(getShell().getDisplay());
				try {
					clipboard.setContents(new Object[] { getSelectedTemplates() }, new Transfer[] { TemplatesTransfer.getInstance() });
				} finally {
					clipboard.dispose();
				}
			}
		};
	}

	/**
	 * Inserts the given template into the editor.
	 *
	 * @param template the template
	 */
	private void insertTemplate(Template template) {
		IDocument document= fTextEditor.getDocumentProvider().getDocument(fTextEditor.getEditorInput());
		insertTemplate(template, document);
	}

	/**
	 * Fill the toolbar.
	 *
	 * @param actionBars the action bars
	 */
	private void fillToolbar(IActionBars actionBars) {
		IToolBarManager toolBarManager= actionBars.getToolBarManager();
		toolBarManager.add(fInsertAction);
		toolBarManager.add(fAddAction);
		toolBarManager.add(fEditAction);
		toolBarManager.add(fRemoveAction);

		toolBarManager.add(new Separator());

		toolBarManager.add(fLinkWithEditorAction);
		toolBarManager.add(fCollapseAllAction);
	}

	/**
	 * Fill the view menu.
	 *
	 * @param actionBars the action bars
	 */
	private void fillMenu(IActionBars actionBars) {
		IMenuManager menuManager= actionBars.getMenuManager();

		if (fPreferencePageAction != null) {
			menuManager.add(fPreferencePageAction);
			menuManager.add(new Separator());
		}

		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * Fill the context menu items.
	 *
	 * @param manager the menu manager
	 */
	private void fillContextMenu(IMenuManager manager) {
		manager.add(fInsertAction);
		manager.add(new Separator());
		manager.add(fAddAction);
		manager.add(fEditAction);
		manager.add(fRemoveAction);
		manager.add(new Separator());
		manager.add(fCopyAction);
		manager.add(fPasteAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * Create the tree to display templates.
	 *
	 * @param parent the parent composite
	 */
	private void createTemplateTree(Composite parent) {
		Composite treeComposite= new Composite(parent, SWT.NONE);
		GridData data= new GridData(GridData.FILL_BOTH);
		treeComposite.setLayoutData(data);

		TreeColumnLayout columnLayout= new TreeColumnLayout();
		treeComposite.setLayout(columnLayout);
		fTemplatesTree= new Tree(treeComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
				| SWT.FULL_SELECTION);
		fTemplatesTree.setHeaderVisible(true);
		fTemplatesTree.setLinesVisible(true);

		PixelConverter pixelConverter= new PixelConverter(fTemplatesTree);

		TreeColumn columnName= new TreeColumn(fTemplatesTree, SWT.NONE);
		columnName.setText(TemplatesMessages.TemplatesPage_column_name);
		int minWidth= fPreferenceStore.getInt(COLUMN_NAME_WIDTH_PREF_ID);
		if (minWidth == 0) {
			minWidth= pixelConverter.convertWidthInCharsToPixels(30);
		}
		columnLayout.setColumnData(columnName, new ColumnPixelData(minWidth, true));
		columnName.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}

			public void controlResized(ControlEvent e) {
				int nameWidth= ((TreeColumn) e.getSource()).getWidth();
				fPreferenceStore.setValue(COLUMN_NAME_WIDTH_PREF_ID, nameWidth);
			}
		});

		TreeColumn columnDescription= new TreeColumn(fTemplatesTree, SWT.NONE);
		columnDescription.setText(TemplatesMessages.TemplatesPage_column_description);
		minWidth= fPreferenceStore.getInt(COLUMN_DESCRIPTION_WIDTH_PREF_ID);
		if (minWidth == 0) {
			minWidth= pixelConverter.convertWidthInCharsToPixels(45);
		}
		columnLayout.setColumnData(columnDescription, new ColumnPixelData(minWidth, false));
		columnDescription.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}

			public void controlResized(ControlEvent e) {
				int descriptionWidth= ((TreeColumn) e.getSource()).getWidth();
				fPreferenceStore.setValue(COLUMN_DESCRIPTION_WIDTH_PREF_ID, descriptionWidth);
			}
		});

		createTreeViewer(fTemplatesTree);
	}

	/**
	 * Create the tree viewer and setup the providers.
	 *
	 * @param templatesTree the tree used to show the templates
	 */
	private void createTreeViewer(Tree templatesTree) {
		fTreeViewer= new TreeViewer(fTemplatesTree);
		fTreeViewer.setLabelProvider(new TemplateLabelProvider());
		fTreeViewer.setContentProvider(new TemplatesContentProvider());

		fTreeViewer.setComparator(new TemplateViewerComparator());
		fTreeViewer.setInput(getTemplatePreferenceStore());
		fTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				updateSelectedItems();
				TemplatePersistenceData[] selectedTemplates= getSelectedTemplates();
				if (selectedTemplates.length > 0)
					insertTemplate(selectedTemplates[0].getTemplate());
			}
		});

		fTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				updateSelectedItems();
				updateButtons();
			}
		});
		fTreeViewer.expandAll();
	}

	/**
	 * Setup the pattern viewer.
	 *
	 * @param parent the parent composite
	 */
	private void createPatternForm(Composite parent) {
		ViewForm viewForm= new ViewForm(parent, SWT.NONE);
		viewForm.setBorderVisible(false);
		CLabel previewLabel= new CLabel(viewForm, SWT.NONE);
		previewLabel.setText(TemplatesMessages.TemplatesPage_preview);
		previewLabel.setImage(TemplatesPageImages.get(TemplatesPageImages.IMG_OBJ_PREVIEW));
		viewForm.setTopLeft(previewLabel);

		fPatternViewer= createPatternViewer(viewForm);
		viewForm.setContent(fPatternViewer.getControl());
		viewForm.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}

			public void controlResized(ControlEvent e) {
				int[] weights= fControl.getWeights();
				int sashSize= (int) (weights[0] * 100.0 / (weights[0] + weights[1]));
				fPreferenceStore.setValue(SASH_SIZE_PREF_ID, sashSize);
			}
		});
	}

	/**
	 * Hookup the context menu
	 */
	private void hookContextMenu() {
		MenuManager menuMgr= new MenuManager(POPUP_MENU_ID);
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		fContextMenu= menuMgr.createContextMenu(fTreeViewer.getControl());
		fTreeViewer.getControl().setMenu(fContextMenu);
		getSite().registerContextMenu(POPUP_MENU_ID, menuMgr, fTreeViewer);
	}

	/**
	 * Check whether the template is valid for the given drop location.
	 *
	 * @param template the template
	 * @param location the drop location
	 * @return <code>true</code> if the template is valid
	 */
	private boolean isTemplateValidAtLocation(Template template, Point location) {
		StyledText textWidget= (StyledText) fTextEditor.getAdapter(Control.class);
		IDocument document= fTextEditor.getDocumentProvider().getDocument(fTextEditor.getEditorInput());
		try {
			if (location.equals(fCachedPosition))
				return fCachedResult;
			fCachedPosition= location;
			int offset= getOffset(document, textWidget, textWidget.toControl(location.x,
					location.y));
			if (fCachedOffset == offset)
				return fCachedResult;
			fCachedOffset= offset;
			if (isValidTemplate(document, template, offset, 0))
				return fCachedResult= true;
		} catch (BadLocationException e) {
		}
		return fCachedResult= false;
	}

	/**
	 * Updates the selected items.
	 */
	private void updateSelectedItems() {
		setSelectedTemplates();
		TemplatePersistenceData[] selectedTemplates= getSelectedTemplates();

		if (selectedTemplates.length == 1)
			updatePatternViewer(selectedTemplates[0].getTemplate());
		else
			updatePatternViewer(null);
	}

	/**
	 * Shows the preference page.
	 */
	private void showPreferencePage() {
		PreferencesUtil.createPreferenceDialogOn(getShell(), getPreferencePageId(), null, null).open();
	}

	/**
	 * Update the state of the buttons
	 */
	private void updateButtons() {
		TemplatePersistenceData[] selectedTemplates= getSelectedTemplates();
		fCopyAction.setEnabled(selectedTemplates.length > 0);
		fInsertAction.setEnabled(selectedTemplates.length == 1);
		fEditAction.setEnabled(selectedTemplates.length == 1);
		fRemoveAction.setEnabled(selectedTemplates.length > 0);
	}

	/**
	 * Set the selected templates
	 */
	private void setSelectedTemplates() {
		IStructuredSelection selection= (IStructuredSelection) fTreeViewer.getSelection();

		Iterator it= selection.iterator();
		TemplatePersistenceData[] data= new TemplatePersistenceData[selection.size()];
		int i= 0;
		while (it.hasNext()) {
			Object o= it.next();
			if (o instanceof TemplatePersistenceData)
				data[i++]= (TemplatePersistenceData) o;
			else {
				fSelectedTemplates= new TemplatePersistenceData[0];
				return;
			}
		}
		fSelectedTemplates= data;
	}

	/**
	 * Returns the currently selected templates
	 * 
	 * @return selected templates
	 * @since 3.6 public, before it was private
	 */
	public TemplatePersistenceData[] getSelectedTemplates() {
		return fSelectedTemplates;
	}

	/**
	 * Add a template
	 */
	private void addTemplate() {
		String id= getContextTypeId();
		if (id != null) {
			Template template= new Template("", "", id, "", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			addTemplate(template);
		}
	}

	/**
	 * Returns the context type id of the selected template.
	 *
	 * @return the context type id of the selected template or the first from the
	 *         registry if no templates are selected
	 */
	private String getContextTypeId() {
		IStructuredSelection selection= (IStructuredSelection) fTreeViewer.getSelection();
		Object item;

		if (selection.size() == 0)
			return ((TemplateContextType) getContextTypeRegistry().contextTypes().next()).getId();

		if (selection.size() == 1) {
			item= selection.getFirstElement();
			if (item instanceof TemplatePersistenceData)
				return ((TemplatePersistenceData) item).getTemplate().getContextTypeId();
			return ((TemplateContextType) item).getId();
		}
		Iterator it= selection.iterator();
		String contextId= null;
		while (it.hasNext()) {
			item= it.next();
			if (contextId == null)
				contextId= getContextId(item);
			else if (!contextId.equals(getContextId(item)))
				return ((TemplateContextType) getContextTypeRegistry().contextTypes().next())
						.getId();
		}
		return contextId;
	}

	/**
	 * Returns the context id for the given item which is either a template or a context type.
	 *
	 * @param item the item
	 * @return the context type id
	 */
	private String getContextId(Object item) {
		String contextId;
		if (item instanceof TemplatePersistenceData)
			contextId= ((TemplatePersistenceData) item).getTemplate().getContextTypeId();
		else
			contextId= ((TemplateContextType) item).getId();
		return contextId;
	}

	/**
	 * Add a template. The dialog is filled with the values from the given template.
	 *
	 * @param template the template
	 */
	private void addTemplate(Template template) {
		Template newTemplate;
		newTemplate= editTemplate(template, false, true);
		if (newTemplate != null) {
			TemplatePersistenceData data= new TemplatePersistenceData(newTemplate, true);
			getTemplateStore().add(data);
			saveTemplateStore();
			refresh();
			fTreeViewer.setSelection(new StructuredSelection(data), true);
		}
	}

	/**
	 * Save the template store
	 */
	private void saveTemplateStore() {
		try {
			getTemplateStore().save();
		} catch (IOException e) {
			e.printStackTrace();
			MessageDialog.openError(getShell(),
					TemplatesMessages.TemplatesPage_save_error_message, e.getMessage());
		}
	}

	/**
	 * Edits the selected template.
	 */
	private void editTemplate() {
		TemplatePersistenceData selectedTemplate= getSelectedTemplates()[0];
		Template oldTemplate= selectedTemplate.getTemplate();
		Template newTemplate= editTemplate(new Template(oldTemplate), true, true);
		if (newTemplate != null) {
			if (!newTemplate.getName().equals(oldTemplate.getName())
					&& MessageDialog.openQuestion(getShell(),
							TemplatesMessages.TemplatesPage_question_create_new_title,
							TemplatesMessages.TemplatesPage_question_create_new_message)) {
				TemplatePersistenceData templateData= new TemplatePersistenceData(newTemplate,
						true);
				getTemplateStore().add(templateData);
				refresh();
				fTreeViewer.setSelection(new StructuredSelection(templateData), true);
			} else {
				selectedTemplate.setTemplate(newTemplate);
				updatePatternViewer(newTemplate);
			}
		}
		saveTemplateStore();
	}

	/**
	 * Moves the selected template from one context to another.
	 *
	 * @param templates an array of template data
	 * @param contextId the contextId
	 *
	 */
	private void moveTemplates(TemplatePersistenceData[] templates, String contextId) {
		for (int i= 0; i < templates.length; i++) {
			Template t= templates[i].getTemplate();
			templates[i].setTemplate(new Template(t.getName(), t.getDescription(), contextId, t
					.getPattern(), t.isAutoInsertable()));
		}
		saveTemplateStore();
		fTreeViewer.setSelection(new StructuredSelection(templates), true);
	}

	/**
	 * Copy the selected templates to another context
	 *
	 * @param templates an array of template data
	 * @param contextId the context id
	 *
	 */
	private void copyTemplates(TemplatePersistenceData[] templates, String contextId) {
		TemplatePersistenceData[] newTemplates= new TemplatePersistenceData[templates.length];
		for (int i= 0; i < templates.length; i++) {
			Template t= templates[i].getTemplate();
			newTemplates[i]= new TemplatePersistenceData(new Template(t.getName(), t
					.getDescription(), contextId, t.getPattern(), t.isAutoInsertable()), true);
			getTemplateStore().add(newTemplates[i]);
		}
		saveTemplateStore();
		refresh();
		fTreeViewer.setSelection(new StructuredSelection(newTemplates), true);
	}

	/**
	 * Remove one or more selected templates
	 */
	private void removeTemplates() {
		String title;
		TemplatePersistenceData[] selectedTemplates= getSelectedTemplates();
		if (selectedTemplates.length == 1)
			title= TemplatesMessages.TemplatesPage_remove_title_single;
		else
			title= TemplatesMessages.TemplatesPage_remove_title_multi;
		String message;
		if (selectedTemplates.length == 1)
			message= TemplatesMessages.TemplatesPage_remove_message_single;
		else
			message= NLSUtility.format(TemplatesMessages.TemplatesPage_remove_message_multi,
					new Object[] { new Integer(selectedTemplates.length) });
		if (!MessageDialog.openQuestion(getShell(), title, message))
			return;
		for (int i= 0; i < selectedTemplates.length; i++) {
			getTemplateStore().delete(selectedTemplates[i]);
		}
		saveTemplateStore();
		fTreeViewer.setSelection(new StructuredSelection(new Object[] {}), true);
	}

	/**
	 * Initializes drag and drop the template items
	 */
	private void initializeDND() {
		DragSourceAdapter dragListener= new DragSourceAdapter() {
			/*
			 * (non-Javadoc)
			 *
			 * @see org.eclipse.swt.dnd.DragSourceAdapter#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
			 */
			public void dragStart(DragSourceEvent event) {
				if (getSelectedTemplates().length == 0) {
					event.doit= false;
				}
			}

			/*
			 * @see org.eclipse.swt.dnd.DragSourceAdapter#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
			 */
			public void dragSetData(DragSourceEvent event) {
				if (TemplatesTransfer.getInstance().isSupportedType(event.dataType)) {
					event.data= getSelectedTemplates();
				}
			}
		};
		fTreeViewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { TemplatesTransfer
				.getInstance() }, dragListener);
		DropTargetAdapter dropListener= new DropTargetAdapter() {
			Transfer textTransfer= TextTransfer.getInstance();
			Transfer templateTransfer= TemplatesTransfer.getInstance();

			/*
			 * @see org.eclipse.swt.dnd.DropTargetAdapter#dragEnter(org.eclipse.swt.dnd.DropTargetEvent)
			 */
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT)
					event.detail= DND.DROP_COPY;
			}

			/*
			 * @see org.eclipse.swt.dnd.DropTargetAdapter#dragOperationChanged(org.eclipse.swt.dnd.DropTargetEvent)
			 */
			public void dragOperationChanged(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT)
					event.detail= DND.DROP_COPY;
			}

			/*
			 * @see org.eclipse.swt.dnd.DropTargetAdapter#dragOver(org.eclipse.swt.dnd.DropTargetEvent)
			 */
			public void dragOver(DropTargetEvent event) {
				event.feedback |= DND.FEEDBACK_SCROLL;
				if (event.item == null) {
					event.detail= DND.DROP_NONE;
					return;
				}
				int index= 0;
				boolean isTemplateTransfer= false;
				while (index < event.dataTypes.length) {
					if (textTransfer.isSupportedType(event.dataTypes[index])) {
						break;
					}
					if (templateTransfer.isSupportedType(event.dataTypes[index])) {
						isTemplateTransfer= true;
						break;
					}
					index++;
				}
				if (index < event.dataTypes.length) {
					event.currentDataType= event.dataTypes[index];
					if (event.detail == DND.DROP_DEFAULT || !isTemplateTransfer)
						event.detail= DND.DROP_COPY;
					return;
				}
			}

			/*
			 * @see org.eclipse.swt.dnd.DropTargetAdapter#drop(org.eclipse.swt.dnd.DropTargetEvent)
			 */
			public void drop(DropTargetEvent event) {
				if (event.item == null)
					return;
				Object object= ((TreeItem) event.item).getData();
				final String contextId;
				if (object instanceof TemplateContextType)
					contextId= ((TemplateContextType) object).getId();
				else
					contextId= ((TemplatePersistenceData) object).getTemplate().getContextTypeId();
				if (textTransfer.isSupportedType(event.currentDataType)) {
					String text= ((String) event.data).replaceAll("\\$", "\\$\\$"); //$NON-NLS-1$ //$NON-NLS-2$
					final Template template= new Template(createTemplateName(),
							TemplatesMessages.TemplatesPage_paste_description, contextId, text,
							true);
					getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							addTemplate(template);
						}
					});
					return;
				}
				if (templateTransfer.isSupportedType(event.currentDataType)) {
					final TemplatePersistenceData[] templates= (TemplatePersistenceData[]) event.data;
					final int dropType= event.detail;
					getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (dropType == DND.DROP_COPY)
								copyTemplates(templates, contextId);
							else
								moveTemplates(templates, contextId);
						}
					});
				}
			}
		};
		Transfer[] transfers= new Transfer[] { TextTransfer.getInstance(),
				TemplatesTransfer.getInstance() };
		fTreeViewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, transfers, dropListener);
	}

	/**
	 * Create a template name.
	 *
	 * @return the created template name
	 */
	private String createTemplateName() {
		for (int i= 1; i < Integer.MAX_VALUE; i++) {
			String name= TemplatesMessages.TemplatesPage_snippet + i;
			if (getTemplateStore().findTemplate(name) == null)
				return name;
		}
		return null;
	}

	/**
	 * Stores the collapse state of a context node.
	 */
	private void storeCollapseState() {
		TreeItem[] items= fTreeViewer.getTree().getItems();
		for (int i= 0; i < items.length; i++) {
			fPreferenceStore.setValue(CONTEXT_COLLAPSE_PREF_ID
					+ ((TemplateContextType) items[i].getData()).getId(), !items[i].getExpanded());
		}
	}

	/**
	 * Refreshes the template tree contents.
	 */
	private void refresh() {
		storeCollapseState();
		fTreeViewer.getTree().setRedraw(false);
		try {
			fTreeViewer.refresh();
			TreeItem[] items= fTreeViewer.getTree().getItems();
			for (int i= 0; i < items.length; i++) {
				boolean isExpanded= !fPreferenceStore.getBoolean(CONTEXT_COLLAPSE_PREF_ID
						+ ((TemplateContextType) items[i].getData()).getId());
				if (isExpanded)
					fTreeViewer.expandToLevel(items[i].getData(), AbstractTreeViewer.ALL_LEVELS);
				else
					fTreeViewer.collapseToLevel(items[i].getData(), AbstractTreeViewer.ALL_LEVELS);
			}
		} finally {
			fTreeViewer.getTree().setRedraw(true);
		}
	}

	/**
	 * Returns the document relative offset from the text widget relative point
	 *
	 * @param document the document
	 * @param textWidget the text widget
	 * @param point the point for which to get the offset
	 * @return the offset
	 * @throws BadLocationException if the document is accessed with an invalid line
	 */
	private int getOffset(IDocument document, StyledText textWidget, Point point)
			throws BadLocationException {
		int widgetCaret= fViewer.getTextWidget().getCaretOffset();
		if (fViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 ext= (ITextViewerExtension5) fViewer;
			try {
				return ext.widgetOffset2ModelOffset(textWidget.getOffsetAtLocation(point));
			} catch (IllegalArgumentException e) {
				int docLineIndex= ext.widgetLine2ModelLine(textWidget.getLineIndex(point.y));
				String lineDelimiter= document.getLineDelimiter(docLineIndex);
				int delimLength= lineDelimiter == null ? 0 : lineDelimiter.length();
				return document.getLineOffset(docLineIndex) + document.getLineLength(docLineIndex)
						- delimLength;
			}
		}
		IRegion visible= fViewer.getVisibleRegion();
		return widgetCaret + visible.getOffset();
	}
}
