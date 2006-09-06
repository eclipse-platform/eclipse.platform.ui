package org.eclipse.compare.internal;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.compare.*;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * An {@link IEditorInput} that is used as the document key.
 * 
 */
public abstract class AbstractTypedElementEditorInput extends PlatformObject implements IEditorInput {
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	public boolean exists() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		Image image = null;
		if (getTypedElement() != null)
			image = getTypedElement().getImage();
		if (image == null)
			return null;
		return ImageDescriptor.createFromImage(image);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName() {
		if (getTypedElement() != null)
			return getTypedElement().getName();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	public IPersistableElement getPersistable() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		return getName();
	}
	
	public abstract ITypedElement getTypedElement();
	
	public String getEncoding() {
		String encoding = getStreamEncoding(getTypedElement());
		if (encoding != null)
			return encoding;
		return null;
	}
	
	public InputStream getContents() throws CoreException {
		ITypedElement element = getTypedElement();
		if (element instanceof IStreamContentAccessor) {
			IStreamContentAccessor accessor = (IStreamContentAccessor) element;
			return accessor.getContents();
		}
		return null;
	}
	
	public static String getStreamEncoding(Object o) {
		if (o instanceof IEncodedStreamContentAccessor) {
			try {
				return ((IEncodedStreamContentAccessor)o).getCharset();
			} catch (CoreException e) {
				// silently ignored
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof AbstractTypedElementEditorInput) {
			AbstractTypedElementEditorInput other = (AbstractTypedElementEditorInput) obj;
			return (other.getTypedElement().equals(getTypedElement()));
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getTypedElement().hashCode();
	}
	
	protected void doSave(IDocument document, IProgressMonitor monitor) throws CoreException {
		// TODO: Code is copied from ContentMergeViewer but will only be usable by TextMergeViewers.
		// We should generalize the code and put it in a place that is accessible from both code paths);
		byte[] bytes;
		try {
			String encoding = getEncoding();
			if (encoding == null)
				encoding = ResourcesPlugin.getEncoding();
			bytes = document.get().getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			CompareUIPlugin.log(e);
			bytes = document.get().getBytes();
		}
		ITypedElement typedElement= getTypedElement();
		if (typedElement instanceof IEditableContent)
			((IEditableContent)typedElement).setContent(bytes);		
	}
	
}