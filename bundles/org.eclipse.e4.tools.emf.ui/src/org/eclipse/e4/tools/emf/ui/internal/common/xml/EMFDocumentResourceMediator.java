/*******************************************************************************
 * Contributors:
 * Steven Spungin <steven@spungin.tv> - Bug 431735, Bug 391089
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.xml;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.E;
import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.xml.sax.InputSource;

public class EMFDocumentResourceMediator {
	private IModelResource modelResource;
	private Document document;
	private boolean updateFromEMF;
	private List<Diagnostic> errorList = new ArrayList<Diagnostic>();
	private Runnable documentValidationChanged;

	public EMFDocumentResourceMediator(final IModelResource modelResource) {
		this.modelResource = modelResource;
		document = new Document();
		document.addDocumentListener(new IDocumentListener() {

			@Override
			public void documentChanged(DocumentEvent event) {
				if (updateFromEMF) {
					return;
				}

				final String doc = document.get();
				final E4XMIResource res = new E4XMIResource();
				try {
					res.load(new InputSource(new StringReader(doc)), null);
					modelResource.replaceRoot(res.getContents().get(0));
					errorList.clear();
					if (documentValidationChanged != null) {
						documentValidationChanged.run();
					}
				} catch (final IOException e) {
					errorList = res.getErrors();
					if (documentValidationChanged != null) {
						documentValidationChanged.run();
					}

				}
			}

			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {

			}
		});
		updateFromEMF();
	}

	public void setValidationChangedCallback(Runnable runnable) {
		documentValidationChanged = runnable;
	}

	public List<Diagnostic> getErrorList() {
		return Collections.unmodifiableList(errorList);
	}

	public void updateFromEMF() {
		try {
			updateFromEMF = true;
			document.set(toXMI((EObject) modelResource.getRoot().get(0)));
		} finally {
			updateFromEMF = false;
		}
	}

	public Document getDocument() {
		return document;
	}

	private String toXMI(EObject root) {
		final E4XMIResource resource = (E4XMIResource) root.eResource();
		// resource.getContents().add(EcoreUtil.copy(root));
		final StringWriter writer = new StringWriter();
		try {
			resource.save(writer, null);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return writer.toString();
	}

	/**
	 * @param object
	 * @return The region for the start tag of the EObject, or null if not
	 *         found.
	 */
	public IRegion findStartTag(EObject object) {
		if (object == null) {
			return null;
		}
		final E4XMIResource root = (E4XMIResource) ((EObject) modelResource.getRoot().get(0)).eResource();
		final String xmiId = root.getID(object);

		final FindReplaceDocumentAdapter find = new FindReplaceDocumentAdapter(document);
		IRegion region;
		try {
			// TODO This will not work if the element has '<' or '>' in an
			// attribute value
			region = find.find(0, "<.*?" + xmiId + ".*?>", true, true, false, true); //$NON-NLS-1$ //$NON-NLS-2$
			return region;
		} catch (final BadLocationException e) {
			return null;
		}
	}

	/**
	 * 
	 * @return The region for the start of the text, or null if not found or the
	 *         text is empty.
	 */
	public IRegion findText(String text, int startOffset) {
		if (E.isEmpty(text)) {
			return null;
		}

		final FindReplaceDocumentAdapter find = new FindReplaceDocumentAdapter(document);
		IRegion region;
		try {
			region = find.find(startOffset, text, true, true, false, false);
			return region;
		} catch (final BadLocationException e) {
			return null;
		}
	}

}
