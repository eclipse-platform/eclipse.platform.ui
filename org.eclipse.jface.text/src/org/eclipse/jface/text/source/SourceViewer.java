/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

import java.util.Stack;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

import org.eclipse.jface.internal.text.NonDeletingPositionUpdater;

import org.eclipse.jface.text.AbstractHoverInformationControlManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ISlaveDocumentManager;
import org.eclipse.jface.text.ISlaveDocumentManagerExtension;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.FormattingContext;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.IContentFormatterExtension;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.projection.ChildDocument;
import org.eclipse.jface.text.reconciler.IReconciler;

/**
 * SWT based implementation of
 * {@link org.eclipse.jface.text.source.ISourceViewer} and its extension
 * interfaces. The same rules apply as for
 * {@link org.eclipse.jface.text.TextViewer}. A source viewer uses an
 * <code>IVerticalRuler</code> as its annotation presentation area. The
 * vertical ruler is a small strip shown left of the viewer's text widget. A
 * source viewer uses an <code>IOverviewRuler</code> as its presentation area
 * for the annotation overview. The overview ruler is a small strip shown right
 * of the viewer's text widget.
 * <p>
 * Clients are supposed to instantiate a source viewer and subsequently to
 * communicate with it exclusively using the <code>ISourceViewer</code>
 * interface.</p>
 * <p>
 * Clients may subclass this class but should expect some breakage by future releases.</p>
 */
public class SourceViewer extends TextViewer implements ISourceViewer, ISourceViewerExtension, ISourceViewerExtension2 {


	/**
	 * Layout of a source viewer. Vertical ruler, text widget, and overview ruler are shown side by side.
	 */
	protected class RulerLayout extends Layout {

		/** The gap between the text viewer and the vertical ruler. */
		protected int fGap;

		/**
		 * Creates a new ruler layout with the given gap between text viewer and vertical ruler.
		 *
		 * @param gap the gap between text viewer and vertical ruler
		 */
		public RulerLayout(int gap) {
			fGap= gap;
		}

		/*
		 * @see Layout#computeSize(Composite, int, int, boolean)
		 */
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
			Control[] children= composite.getChildren();
			Point s= children[children.length - 1].computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
			if (fVerticalRuler != null && fIsVerticalRulerVisible)
				s.x += fVerticalRuler.getWidth() + fGap;
			return s;
		}

		/*
		 * @see Layout#layout(Composite, boolean)
		 */
		protected void layout(Composite composite, boolean flushCache) {
			Rectangle clArea= composite.getClientArea();
			Rectangle trim= getTextWidget().computeTrim(0, 0, 0, 0);
			int topTrim= - trim.y;
			int scrollbarHeight= trim.height - topTrim; // scrollbar is only under the client area

			int x= clArea.x;
			int width= clArea.width;

			if (fOverviewRuler != null && fIsOverviewRulerVisible) {
				int overviewRulerWidth= fOverviewRuler.getWidth();
				fOverviewRuler.getControl().setBounds(clArea.x + clArea.width - overviewRulerWidth - 1, clArea.y + scrollbarHeight, overviewRulerWidth, clArea.height - 3*scrollbarHeight);
				fOverviewRuler.getHeaderControl().setBounds(clArea.x + clArea.width - overviewRulerWidth - 1, clArea.y, overviewRulerWidth, scrollbarHeight);

				width -= overviewRulerWidth + fGap;
			}

			if (fVerticalRuler != null && fIsVerticalRulerVisible) {
				int verticalRulerWidth= fVerticalRuler.getWidth();
				fVerticalRuler.getControl().setBounds(clArea.x, clArea.y + topTrim, verticalRulerWidth, clArea.height - scrollbarHeight - topTrim);

				x += verticalRulerWidth + fGap;
				width -= verticalRulerWidth + fGap;
			}

			getTextWidget().setBounds(x, clArea.y, width, clArea.height);
		}
	}

	/**
	 * The size of the gap between the vertical ruler and the text widget
	 * (value <code>2</code>).
	 * <p>
	 * Note: As of 3.2, the text editor framework is no longer using 2 as
	 * gap but 1, see {{@link #GAP_SIZE_1 }.
	 * </p>
	 */
	protected final static int GAP_SIZE= 2;
	/**
	 * The size of the gap between the vertical ruler and the text widget
	 * (value <code>1</code>).
	 * @since 3.2
	 */
	protected final static int GAP_SIZE_1= 1;
	/**
	 * Partial name of the position category to manage remembered selections.
	 * @since 3.0
	 */
	protected final static String _SELECTION_POSITION_CATEGORY= "__selection_category"; //$NON-NLS-1$
	/**
	 * Key of the model annotation model inside the visual annotation model.
	 * @since 3.0
	 */
	protected final static Object MODEL_ANNOTATION_MODEL= new Object();

	/** The viewer's content assistant */
	protected IContentAssistant fContentAssistant;
	/**
	 * Flag indicating whether the viewer's content assistant is installed
	 * @since 2.0
	 */
	protected boolean fContentAssistantInstalled;
	/** The viewer's content formatter */
	protected IContentFormatter fContentFormatter;
	/** The viewer's model reconciler */
	protected IReconciler fReconciler;
	/** The viewer's presentation reconciler */
	protected IPresentationReconciler fPresentationReconciler;
	/** The viewer's annotation hover */
	protected IAnnotationHover fAnnotationHover;
	/**
	 * Stack of saved selections in the underlying document
	 * @since 3.0
	 */
	protected final Stack fSelections= new Stack();
	/**
	 * Position updater for saved selections
	 * @since 3.0
	 */
	protected IPositionUpdater fSelectionUpdater= null;
	/**
	 * Position category used by the selection updater
	 * @since 3.0
	 */
	protected String fSelectionCategory;
	/**
	 * The viewer's overview ruler annotation hover
	 * @since 3.0
	 */
	protected IAnnotationHover fOverviewRulerAnnotationHover;
	/**
	 * The viewer's information presenter
	 * @since 2.0
	 */
	protected IInformationPresenter fInformationPresenter;

	/** Visual vertical ruler */
	private IVerticalRuler fVerticalRuler;
	/** Visibility of vertical ruler */
	private boolean fIsVerticalRulerVisible;
	/** The SWT widget used when supporting a vertical ruler */
	private Composite fComposite;
	/** The vertical ruler's annotation model */
	private IAnnotationModel fVisualAnnotationModel;
	/** The viewer's range indicator to be shown in the vertical ruler */
	private Annotation fRangeIndicator;
	/** The viewer's vertical ruler hovering controller */
	private AbstractHoverInformationControlManager fVerticalRulerHoveringController;
	/**
	 * The viewer's overview ruler hovering controller
	 * @since 2.1
	 */
	private AbstractHoverInformationControlManager fOverviewRulerHoveringController;

	/**
	 * The overview ruler.
	 * @since 2.1
	 */
	private IOverviewRuler fOverviewRuler;
	/**
	 * The visibility of the overview ruler
	 * @since 2.1
	 */
	private boolean fIsOverviewRulerVisible;


	/**
	 * Constructs a new source viewer. The vertical ruler is initially visible.
	 * The viewer has not yet been initialized with a source viewer configuration.
	 *
	 * @param parent the parent of the viewer's control
	 * @param ruler the vertical ruler used by this source viewer
	 * @param styles the SWT style bits
	 */
	public SourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		this(parent, ruler, null, false, styles);
	}

	/**
	 * Constructs a new source viewer. The vertical ruler is initially visible.
	 * The overview ruler visibility is controlled by the value of <code>showAnnotationsOverview</code>.
	 * The viewer has not yet been initialized with a source viewer configuration.
	 *
	 * @param parent the parent of the viewer's control
	 * @param verticalRuler the vertical ruler used by this source viewer
	 * @param overviewRuler the overview ruler
	 * @param showAnnotationsOverview <code>true</code> if the overview ruler should be visible, <code>false</code> otherwise
	 * @param styles the SWT style bits
	 * @since 2.1
	 */
	public SourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler, boolean showAnnotationsOverview, int styles) {
		super();

		fVerticalRuler= verticalRuler;
		fIsVerticalRulerVisible= (verticalRuler != null);
		fOverviewRuler= overviewRuler;
		fIsOverviewRulerVisible= (showAnnotationsOverview && overviewRuler != null);

		createControl(parent, styles);
	}

	/*
	 * @see TextViewer#createControl(Composite, int)
	 */
	protected void createControl(Composite parent, int styles) {

		if (fVerticalRuler != null || fOverviewRuler != null) {
			styles= (styles & ~SWT.BORDER);
			fComposite= new Canvas(parent, SWT.NONE);
			fComposite.setLayout(createLayout());
			parent= fComposite;
		}

		super.createControl(parent, styles);

		if (fVerticalRuler != null)
			fVerticalRuler.createControl(fComposite, this);
		if (fOverviewRuler != null)
			fOverviewRuler.createControl(fComposite, this);
	}

	/**
	 * Creates the layout used for this viewer.
	 * Subclasses may override this method.
	 *
	 * @return the layout used for this viewer
	 * @since 3.0
	 */
	protected Layout createLayout() {
		return new RulerLayout(GAP_SIZE_1);
	}

	/*
	 * @see TextViewer#getControl()
	 */
	public Control getControl() {
		if (fComposite != null)
			return fComposite;
		return super.getControl();
	}

	/*
	 * @see ISourceViewer#setAnnotationHover(IAnnotationHover)
	 */
	public void setAnnotationHover(IAnnotationHover annotationHover) {
		fAnnotationHover= annotationHover;
	}

	/**
	 * Sets the overview ruler's annotation hover of this source viewer.
	 * The annotation hover provides the information to be displayed in a hover
	 * popup window if requested over the overview rulers area. The annotation
	 * hover is assumed to be line oriented.
	 *
	 * @param annotationHover the hover to be used, <code>null</code> is a valid argument
	 * @since 3.0
	 */
	public void setOverviewRulerAnnotationHover(IAnnotationHover annotationHover) {
		fOverviewRulerAnnotationHover= annotationHover;
	}

	/*
	 * @see ISourceViewer#configure(SourceViewerConfiguration)
	 */
	public void configure(SourceViewerConfiguration configuration) {

		if (getTextWidget() == null)
			return;

		setDocumentPartitioning(configuration.getConfiguredDocumentPartitioning(this));

		// install content type independent plug-ins
		fPresentationReconciler= configuration.getPresentationReconciler(this);
		if (fPresentationReconciler != null)
			fPresentationReconciler.install(this);

		fReconciler= configuration.getReconciler(this);
		if (fReconciler != null)
			fReconciler.install(this);

		fContentAssistant= configuration.getContentAssistant(this);
		if (fContentAssistant != null) {
			fContentAssistant.install(this);
			fContentAssistantInstalled= true;
		}

		fContentFormatter= configuration.getContentFormatter(this);

		fInformationPresenter= configuration.getInformationPresenter(this);
		if (fInformationPresenter != null)
			fInformationPresenter.install(this);

		setUndoManager(configuration.getUndoManager(this));

		getTextWidget().setTabs(configuration.getTabWidth(this));

		setAnnotationHover(configuration.getAnnotationHover(this));
		setOverviewRulerAnnotationHover(configuration.getOverviewRulerAnnotationHover(this));

		setHoverControlCreator(configuration.getInformationControlCreator(this));

		setHyperlinkPresenter(configuration.getHyperlinkPresenter(this));
		IHyperlinkDetector[] hyperlinkDetectors= configuration.getHyperlinkDetectors(this);
		int eventStateMask= configuration.getHyperlinkStateMask(this);
		setHyperlinkDetectors(hyperlinkDetectors, eventStateMask);

		// install content type specific plug-ins
		String[] types= configuration.getConfiguredContentTypes(this);
		for (int i= 0; i < types.length; i++) {

			String t= types[i];

			setAutoEditStrategies(configuration.getAutoEditStrategies(this, t), t);
			setTextDoubleClickStrategy(configuration.getDoubleClickStrategy(this, t), t);

			int[] stateMasks= configuration.getConfiguredTextHoverStateMasks(this, t);
			if (stateMasks != null) {
				for (int j= 0; j < stateMasks.length; j++)	{
					int stateMask= stateMasks[j];
					setTextHover(configuration.getTextHover(this, t, stateMask), t, stateMask);
				}
			} else {
				setTextHover(configuration.getTextHover(this, t), t, ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK);
			}

			String[] prefixes= configuration.getIndentPrefixes(this, t);
			if (prefixes != null && prefixes.length > 0)
				setIndentPrefixes(prefixes, t);

			prefixes= configuration.getDefaultPrefixes(this, t);
			if (prefixes != null && prefixes.length > 0)
				setDefaultPrefixes(prefixes, t);
		}

		activatePlugins();
	}

	/**
	 * After this method has been executed the caller knows that any installed annotation hover has been installed.
	 */
	protected void ensureAnnotationHoverManagerInstalled() {
		if (fVerticalRuler != null && fAnnotationHover != null && fVerticalRulerHoveringController == null && fHoverControlCreator != null) {
			fVerticalRulerHoveringController= new AnnotationBarHoverManager(fVerticalRuler, this, fAnnotationHover, fHoverControlCreator);
			fVerticalRulerHoveringController.install(fVerticalRuler.getControl());
		}
	}

	/**
	 * After this method has been executed the caller knows that any installed overview hover has been installed.
	 */
	protected void ensureOverviewHoverManagerInstalled() {
		if (fOverviewRuler != null &&  fOverviewRulerAnnotationHover != null  && fOverviewRulerHoveringController == null && fHoverControlCreator != null)	{
			fOverviewRulerHoveringController= new OverviewRulerHoverManager(fOverviewRuler, this, fOverviewRulerAnnotationHover, fHoverControlCreator);
			fOverviewRulerHoveringController.install(fOverviewRuler.getControl());
		}
	}

	/*
	 * @see TextViewer#activatePlugins()
	 */
	public void activatePlugins() {
		ensureAnnotationHoverManagerInstalled();
		ensureOverviewHoverManagerInstalled();
		super.activatePlugins();
	}

	/*
	 * @see ISourceViewer#setDocument(IDocument, IAnnotationModel)
	 */
	public void setDocument(IDocument document) {
		setDocument(document, null, -1, -1);
	}

	/*
	 * @see ISourceViewer#setDocument(IDocument, IAnnotationModel, int, int)
	 */
	public void setDocument(IDocument document, int visibleRegionOffset, int visibleRegionLength) {
		setDocument(document, null, visibleRegionOffset, visibleRegionLength);
	}

	/*
	 * @see ISourceViewer#setDocument(IDocument, IAnnotationModel)
	 */
	public void setDocument(IDocument document, IAnnotationModel annotationModel) {
		setDocument(document, annotationModel, -1, -1);
	}

	/**
	 * Creates the visual annotation model on top of the given annotation model.
	 *
	 * @param annotationModel the wrapped annotation model
	 * @return the visual annotation model on top of the given annotation model
	 * @since 3.0
	 */
	protected IAnnotationModel createVisualAnnotationModel(IAnnotationModel annotationModel) {
		IAnnotationModelExtension model= new AnnotationModel();
		model.addAnnotationModel(MODEL_ANNOTATION_MODEL, annotationModel);
		return (IAnnotationModel) model;
	}

	/**
	 * Disposes the visual annotation model.
	 * 
	 * @since 3.1
	 */
	protected void disposeVisualAnnotationModel() {
		if (fVisualAnnotationModel != null) {
			if (getDocument() != null)
				fVisualAnnotationModel.disconnect(getDocument());
		
			if ( fVisualAnnotationModel instanceof IAnnotationModelExtension)
				((IAnnotationModelExtension)fVisualAnnotationModel).removeAnnotationModel(MODEL_ANNOTATION_MODEL);
			
			fVisualAnnotationModel= null;
		}
	}

	/*
	 * @see ISourceViewer#setDocument(IDocument, IAnnotationModel, int, int)
	 */
	public void setDocument(IDocument document, IAnnotationModel annotationModel, int modelRangeOffset, int modelRangeLength) {
		if (fVerticalRuler == null && fOverviewRuler == null) {

			if (modelRangeOffset == -1 && modelRangeLength == -1)
				super.setDocument(document);
			else
				super.setDocument(document, modelRangeOffset, modelRangeLength);

		} else {

			disposeVisualAnnotationModel();

			if (annotationModel != null && document != null) {
				fVisualAnnotationModel= createVisualAnnotationModel(annotationModel);
				fVisualAnnotationModel.connect(document);
			}

			if (modelRangeOffset == -1 && modelRangeLength == -1)
				super.setDocument(document);
			else
				super.setDocument(document, modelRangeOffset, modelRangeLength);

			if (fVerticalRuler != null)
				fVerticalRuler.setModel(fVisualAnnotationModel);

			if (fOverviewRuler != null)
				fOverviewRuler.setModel(fVisualAnnotationModel);
		}
	}

	/*
	 * @see ISourceViewer#getAnnotationModel()
	 */
	public IAnnotationModel getAnnotationModel() {
		if (fVisualAnnotationModel instanceof IAnnotationModelExtension) {
			IAnnotationModelExtension extension= (IAnnotationModelExtension) fVisualAnnotationModel;
			return extension.getAnnotationModel(MODEL_ANNOTATION_MODEL);
		}
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.source.ISourceViewerExtension2#getVisualAnnotationModel()
	 * @since 3.0
	 */
	public IAnnotationModel getVisualAnnotationModel() {
		return fVisualAnnotationModel;
	}

	/*
	 * @see org.eclipse.jface.text.source.ISourceViewerExtension2#unconfigure()
	 * @since 3.0
	 */
	public void unconfigure() {
		clearRememberedSelection();

		if (fPresentationReconciler != null) {
			fPresentationReconciler.uninstall();
			fPresentationReconciler= null;
		}

		if (fReconciler != null) {
			fReconciler.uninstall();
			fReconciler= null;
		}

		if (fContentAssistant != null) {
			fContentAssistant.uninstall();
			fContentAssistantInstalled= false;
			fContentAssistant= null;
		}

		fContentFormatter= null;

		if (fInformationPresenter != null) {
			fInformationPresenter.uninstall();
			fInformationPresenter= null;
		}

		fAutoIndentStrategies= null;
		fDoubleClickStrategies= null;
		fTextHovers= null;
		fIndentChars= null;
		fDefaultPrefixChars= null;

		if (fVerticalRulerHoveringController != null) {
			fVerticalRulerHoveringController.dispose();
			fVerticalRulerHoveringController= null;
		}

		if (fOverviewRulerHoveringController != null) {
			fOverviewRulerHoveringController.dispose();
			fOverviewRulerHoveringController= null;
		}

		if (fUndoManager != null) {
			fUndoManager.disconnect();
			fUndoManager= null;
		}

		setHyperlinkDetectors(null, SWT.NONE);
	}

	/*
	 * @see org.eclipse.jface.text.TextViewer#handleDispose()
	 */
	protected void handleDispose() {
		unconfigure();

		disposeVisualAnnotationModel();

		fVerticalRuler= null;

		fOverviewRuler= null;

		// http://dev.eclipse.org/bugs/show_bug.cgi?id=15300
		fComposite= null;

		super.handleDispose();
	}

	/*
	 * @see ITextOperationTarget#canDoOperation(int)
	 */
	public boolean canDoOperation(int operation) {

		if (getTextWidget() == null || (!redraws() && operation != FORMAT))
			return false;

		if (operation == CONTENTASSIST_PROPOSALS)
			return fContentAssistant != null && fContentAssistantInstalled && isEditable();

		if (operation == CONTENTASSIST_CONTEXT_INFORMATION)
			return fContentAssistant != null && fContentAssistantInstalled && isEditable();

		if (operation == INFORMATION)
			return fInformationPresenter != null;

		if (operation == FORMAT) {
			return fContentFormatter != null && isEditable();
		}

		return super.canDoOperation(operation);
	}

	/**
	 * Creates a new formatting context for a format operation.
	 * <p>
	 * After the use of the context, clients are required to call
	 * its <code>dispose</code> method.
	 *
	 * @return The new formatting context
	 * @since 3.0
	 */
	protected IFormattingContext createFormattingContext() {
		return new FormattingContext();
	}

	/**
	 * Remembers and returns the current selection. The saved selection can be restored
	 * by calling <code>restoreSelection()</code>.
	 *
	 * @return the current selection
	 * @see org.eclipse.jface.text.ITextViewer#getSelectedRange()
	 * @since 3.0
	 */
	protected Point rememberSelection() {

		final Point selection= getSelectedRange();
		final IDocument document= getDocument();

		if (fSelections.isEmpty()) {
			fSelectionCategory= _SELECTION_POSITION_CATEGORY + hashCode();
			fSelectionUpdater= new NonDeletingPositionUpdater(fSelectionCategory);
			document.addPositionCategory(fSelectionCategory);
			document.addPositionUpdater(fSelectionUpdater);
		}

		try {

			final Position position= new Position(selection.x, selection.y);
			document.addPosition(fSelectionCategory, position);
			fSelections.push(position);

		} catch (BadLocationException exception) {
			// Should not happen
		} catch (BadPositionCategoryException exception) {
			// Should not happen
		}

		return selection;
	}

	/**
	 * Restores a previously saved selection in the document.
	 * <p>
	 * If no selection was previously saved, nothing happens.
	 *
	 * @since 3.0
	 */
	protected void restoreSelection() {

		if (!fSelections.isEmpty()) {

			final IDocument document= getDocument();
			final Position position= (Position) fSelections.pop();

			try {
				document.removePosition(fSelectionCategory, position);
				Point currentSelection= getSelectedRange();
				if (currentSelection == null || currentSelection.x != position.getOffset() || currentSelection.y != position.getLength())
					setSelectedRange(position.getOffset(), position.getLength());

				if (fSelections.isEmpty())
					clearRememberedSelection();
			} catch (BadPositionCategoryException exception) {
				// Should not happen
			}
		}
	}

	protected void clearRememberedSelection() {
		if (!fSelections.isEmpty())
			fSelections.clear();

		IDocument document= getDocument();
		if (document != null && fSelectionUpdater != null) {
			document.removePositionUpdater(fSelectionUpdater);
			try {
				document.removePositionCategory(fSelectionCategory);
			} catch (BadPositionCategoryException e) {
				// ignore
			}
		}
		fSelectionUpdater= null;
		fSelectionCategory= null;
	}

	/*
	 * @see ITextOperationTarget#doOperation(int)
	 */
	public void doOperation(int operation) {

		if (getTextWidget() == null || (!redraws() && operation != FORMAT))
			return;

		switch (operation) {
			case CONTENTASSIST_PROPOSALS:
				fContentAssistant.showPossibleCompletions();
				return;
			case CONTENTASSIST_CONTEXT_INFORMATION:
				fContentAssistant.showContextInformation();
				return;
			case INFORMATION:
				fInformationPresenter.showInformation();
				return;
			case FORMAT:
				{
					final Point selection= rememberSelection();
					final IRewriteTarget target= getRewriteTarget();
					final IDocument document= getDocument();
					IFormattingContext context= null;
					DocumentRewriteSession rewriteSession= null;

					if (document instanceof IDocumentExtension4) {
						IDocumentExtension4 extension= (IDocumentExtension4) document;
						rewriteSession= extension.startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
					} else {
						setRedraw(false);
						startSequentialRewriteMode(false);
						target.beginCompoundChange();
					}

					try {

						final String rememberedContents= document.get();

						try {

							if (fContentFormatter instanceof IContentFormatterExtension) {
								final IContentFormatterExtension extension= (IContentFormatterExtension) fContentFormatter;
								context= createFormattingContext();
								if (selection.y == 0) {
									context.setProperty(FormattingContextProperties.CONTEXT_DOCUMENT, Boolean.TRUE);
								} else {
									context.setProperty(FormattingContextProperties.CONTEXT_DOCUMENT, Boolean.FALSE);
									context.setProperty(FormattingContextProperties.CONTEXT_REGION, new Region(selection.x, selection.y));
								}
								extension.format(document, context);
							} else {
								IRegion r;
								if (selection.y == 0) {
									IRegion coverage= getModelCoverage();
									r= coverage == null ? new Region(0, 0) : coverage;
								} else {
									r= new Region(selection.x, selection.y);
								}
								fContentFormatter.format(document, r);
							}

							updateSlaveDocuments(document);

						} catch (RuntimeException x) {
							// fire wall for https://bugs.eclipse.org/bugs/show_bug.cgi?id=47472
							// if something went wrong we undo the changes we just did
							// TODO to be removed after 3.0 M8
							document.set(rememberedContents);
							throw x;
						}

					} finally {

						if (document instanceof IDocumentExtension4) {
							IDocumentExtension4 extension= (IDocumentExtension4) document;
							extension.stopRewriteSession(rewriteSession);
						} else {
							target.endCompoundChange();
							stopSequentialRewriteMode();
							setRedraw(true);
						}

						restoreSelection();
						if (context != null)
							context.dispose();
					}
					return;
				}
			default:
				super.doOperation(operation);
		}
	}

	/**
	 * Updates all slave documents of the given document. This default implementation calls <code>updateSlaveDocument</code>
	 * for their current visible range. Subclasses may reimplement.
	 *
	 * @param masterDocument the master document
	 * @since 3.0
	 */
	protected void updateSlaveDocuments(IDocument masterDocument) {
		ISlaveDocumentManager manager= getSlaveDocumentManager();
		if (manager instanceof ISlaveDocumentManagerExtension) {
			ISlaveDocumentManagerExtension extension= (ISlaveDocumentManagerExtension) manager;
			IDocument[] slaves= extension.getSlaveDocuments(masterDocument);
			if (slaves != null) {
				for (int i= 0; i < slaves.length; i++) {
					if (slaves[i] instanceof ChildDocument) {
						ChildDocument child= (ChildDocument) slaves[i];
						Position p= child.getParentDocumentRange();
						try {

							if (!updateSlaveDocument(child, p.getOffset(), p.getLength()))
								child.repairLineInformation();

						} catch (BadLocationException e) {
							// ignore
						}
					}
				}
			}
		}
	}

	/*
	 * @see ITextOperationTargetExtension#enableOperation(int, boolean)
	 * @since 2.0
	 */
	public void enableOperation(int operation, boolean enable) {

		switch (operation) {
			case CONTENTASSIST_PROPOSALS:
			case CONTENTASSIST_CONTEXT_INFORMATION: {

				if (fContentAssistant == null)
					return;

				if (enable) {
					if (!fContentAssistantInstalled) {
						fContentAssistant.install(this);
						fContentAssistantInstalled= true;
					}
				} else if (fContentAssistantInstalled) {
					fContentAssistant.uninstall();
					fContentAssistantInstalled= false;
				}
			}
		}
	}

	/*
	 * @see ISourceViewer#setRangeIndicator(Annotation)
	 */
	public void setRangeIndicator(Annotation rangeIndicator) {
		fRangeIndicator= rangeIndicator;
	}

	/*
	 * @see ISourceViewer#setRangeIndication(int, int, boolean)
	 */
	public void setRangeIndication(int start, int length, boolean moveCursor) {

		if (moveCursor) {
			setSelectedRange(start, 0);
			revealRange(start, length);
		}

		if (fRangeIndicator != null && fVisualAnnotationModel instanceof IAnnotationModelExtension) {
			IAnnotationModelExtension extension= (IAnnotationModelExtension) fVisualAnnotationModel;
			extension.modifyAnnotationPosition(fRangeIndicator, new Position(start, length));
		}
	}

	/*
	 * @see ISourceViewer#getRangeIndication()
	 */
	public IRegion getRangeIndication() {
		if (fRangeIndicator != null && fVisualAnnotationModel != null) {
			Position position= fVisualAnnotationModel.getPosition(fRangeIndicator);
			if (position != null)
				return new Region(position.getOffset(), position.getLength());
		}

		return null;
	}

	/*
	 * @see ISourceViewer#removeRangeIndication()
	 */
	public void removeRangeIndication() {
		if (fRangeIndicator != null && fVisualAnnotationModel != null)
			fVisualAnnotationModel.removeAnnotation(fRangeIndicator);
	}

	/*
	 * @see ISourceViewer#showAnnotations(boolean)
	 */
	public void showAnnotations(boolean show) {
		boolean old= fIsVerticalRulerVisible;
		fIsVerticalRulerVisible= (show && fVerticalRuler != null);
		if (old != fIsVerticalRulerVisible) {
			if (fComposite != null && !fComposite.isDisposed())
				fComposite.layout();
			if (fIsVerticalRulerVisible) {
				ensureAnnotationHoverManagerInstalled();
			} else if (fVerticalRulerHoveringController != null) {
				fVerticalRulerHoveringController.dispose();
				fVerticalRulerHoveringController= null;
			}
		}
	}

	/**
	 * Returns the vertical ruler of this viewer.
	 *
	 * @return the vertical ruler of this viewer
	 * @since 3.0
	 */
	protected final IVerticalRuler getVerticalRuler() {
		return fVerticalRuler;
	}

	/*
	 * @see org.eclipse.jface.text.source.ISourceViewerExtension#showAnnotationsOverview(boolean)
	 * @since 2.1
	 */
	public void showAnnotationsOverview(boolean show) {
		boolean old= fIsOverviewRulerVisible;
		fIsOverviewRulerVisible= (show && fOverviewRuler != null);
		if (old != fIsOverviewRulerVisible) {
			if (fComposite != null && !fComposite.isDisposed())
				fComposite.layout();
			if (fIsOverviewRulerVisible) {
				ensureOverviewHoverManagerInstalled();
			} else if (fOverviewRulerHoveringController != null) {
				fOverviewRulerHoveringController.dispose();
				fOverviewRulerHoveringController= null;
			}
		}
	}
}
