/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * Copyright (c) 2010-2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 391089, Bug 437543, Ongoing Maintenance
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.component.tabs;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.xml.AnnotationAccess;
import org.eclipse.e4.tools.emf.ui.internal.common.xml.EMFDocumentResourceMediator;
import org.eclipse.e4.tools.emf.ui.internal.common.xml.XMLConfiguration;
import org.eclipse.e4.tools.emf.ui.internal.common.xml.XMLPartitionScanner;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.VerticalRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class XmiTab extends Composite {

	private static final String ORG_ECLIPSE_E4_TOOLS_MODELEDITOR_FILTEREDTREE_ENABLED_XMITAB_DISABLED = "org.eclipse.e4.tools.modeleditor.filteredtree.enabled.xmitab.disabled";//$NON-NLS-1$
	private static final int VERTICAL_RULER_WIDTH = 20;

	@Inject
	private IEclipseContext context;

	@Optional
	@Inject
	private IProject project;
	@Inject
	private EMFDocumentResourceMediator emfDocumentProvider;
	@Inject
	private IResourcePool resourcePool;
	@Inject
	private IEclipsePreferences preferences;

	@Inject
	@Translation
	protected Messages Messages;

	private Text text;
	protected int offsetStart;
	private SourceViewer sourceViewer;

	@Inject
	public XmiTab(Composite parent) {
		super(parent, SWT.NONE);
		setLayout(new GridLayout(1, false));
	}

	@PostConstruct
	protected void postConstruct() {

		text = new Text(this, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		text.setMessage(Messages.XmiTab_TypeTextToSearch);
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode != SWT.CR) {
					offsetStart = 0;
				}
				offsetStart = searchAndHighlight(text.getText(), offsetStart);
			}
		});

		final AnnotationModel model = new AnnotationModel();
		VerticalRuler verticalRuler = new VerticalRuler(VERTICAL_RULER_WIDTH, new AnnotationAccess(resourcePool));
		int styles = SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION;
		sourceViewer = new SourceViewer(this, verticalRuler, styles);
		sourceViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		sourceViewer.configure(new XMLConfiguration(resourcePool));
		sourceViewer.setEditable(project != null);
		sourceViewer.getTextWidget().setFont(JFaceResources.getTextFont());

		final IDocument document = emfDocumentProvider.getDocument();
		IDocumentPartitioner partitioner = new FastPartitioner(new XMLPartitionScanner(), new String[] { XMLPartitionScanner.XML_TAG, XMLPartitionScanner.XML_COMMENT });
		partitioner.connect(document);
		document.setDocumentPartitioner(partitioner);
		sourceViewer.setDocument(document);
		verticalRuler.setModel(model);

		emfDocumentProvider.setValidationChangedCallback(new Runnable() {

			@Override
			public void run() {
				model.removeAllAnnotations();

				for (Diagnostic d : emfDocumentProvider.getErrorList()) {
					Annotation a = new Annotation("e4xmi.error", false, d.getMessage()); //$NON-NLS-1$
					int l;
					try {
						l = document.getLineOffset(d.getLine() - 1);
						model.addAnnotation(a, new Position(l));
					} catch (BadLocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});

		String property = System.getProperty(ORG_ECLIPSE_E4_TOOLS_MODELEDITOR_FILTEREDTREE_ENABLED_XMITAB_DISABLED);
		if (property != null || preferences.getBoolean("tab-form-search-show", false)) { //$NON-NLS-1$
			sourceViewer.setEditable(false);
			sourceViewer.getTextWidget().setEnabled(false);
		}
	}

	/**
	 *
	 * @param text
	 * @param startOffset
	 * @return The endOFfset, or -1 if not found
	 */
	protected int searchAndHighlight(String text, int startOffset) {
		try {
			// select the entire start tag
			IRegion region;
			region = emfDocumentProvider.findText(text, startOffset);
			if (region == null && startOffset > 0) {
				region = emfDocumentProvider.findText(text, 0);
			}
			if (region != null) {
				sourceViewer.setSelection(new TextSelection(region.getOffset(), region.getLength()), true);
				return region.getOffset() + region.getLength();
			} else {
				sourceViewer.setSelection(new TextSelection(0, 0), true);
				return -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public IEclipseContext getContext() {
		return context;
	}

	public void gotoEObject(EObject object) {
		// select the entire start tag
		IRegion region = emfDocumentProvider.findStartTag(object);
		if (region != null) {
			sourceViewer.setSelection(new TextSelection(region.getOffset(), region.getLength()), true);
		} else {
			sourceViewer.setSelection(new TextSelection(0, 0), true);
		}
	}

	public void paste() {
		sourceViewer.getTextWidget().paste();
	}

	public void copy() {
		sourceViewer.getTextWidget().copy();
	}

	public void cut() {
		sourceViewer.getTextWidget().cut();
	}
}
