package org.eclipse.ui.editors.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.CharConversionException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;

import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.StatusTextEditor;



/**
 * The standard implementation of <code>IEncodingSupport</code>.
 */
public class DefaultEncodingSupport implements IEncodingSupport {
	
	private Preferences.IPropertyChangeListener fPropertyChangeListener;
	private StatusTextEditor fTextEditor;
	private EncodingActionGroup fEncodingActionGroup;
	
	/**
	 * Creates a new encoding support.
	 */
	public DefaultEncodingSupport() {
		super();
	}
	
	/**
	 * Initializes this encoding support.
	 */
	public void initialize(StatusTextEditor textEditor) {
		
		fTextEditor= textEditor;
		
		fPropertyChangeListener= new Preferences.IPropertyChangeListener() {
			public void propertyChange(Preferences.PropertyChangeEvent e) {
				if (ResourcesPlugin.PREF_ENCODING.equals(e.getProperty()))
					setEncoding(null, false);
			}
		};
		
		Preferences p= ResourcesPlugin.getPlugin().getPluginPreferences();
		p.addPropertyChangeListener(fPropertyChangeListener);
		
		fEncodingActionGroup= new EncodingActionGroup(fTextEditor);
		fEncodingActionGroup.update();
	}
	
	/**
	 * Disposes this encoding support.
	 */
	public void dispose() {
		Preferences p= ResourcesPlugin.getPlugin().getPluginPreferences();
		p.removePropertyChangeListener(fPropertyChangeListener);
		
		fEncodingActionGroup.dispose();
		fEncodingActionGroup= null;
		
		fTextEditor= null;
	}
	
	/**
	 * Resets this encoding support. Should be called if, e.g., the input element
	 * of the editor changed.
	 */
	public void reset() {
		fEncodingActionGroup.update();
	}
	
	/**
	 * Sets the encoding of the editor's input to the given value. If <code>overwrite</code> is
	 * <code>true</code> the value is set even if the encoding is already set.
	 */
	protected void setEncoding(String encoding, boolean overwrite) {
		
		String internal= encoding == null ? "" : encoding; //$NON-NLS-1$
		
		IDocumentProvider p= fTextEditor.getDocumentProvider();
		if (p instanceof IStorageDocumentProvider) {
			IStorageDocumentProvider provider= (IStorageDocumentProvider) p;
			String current= provider.getEncoding(fTextEditor.getEditorInput());
			
			boolean apply= (current != null && encoding == null) ? overwrite : !internal.equals(current);
			if (apply) {
				
				provider.setEncoding(fTextEditor.getEditorInput(), encoding);
				fTextEditor.doRevertToSaved();
				fTextEditor.updatePartControl(fTextEditor.getEditorInput());
				
				fEncodingActionGroup.update();
			}
		}
	}
	
	/*
	 * @see IEncodingSupport#setEncoding(String)
	 */
	public void setEncoding(String encoding) {
		setEncoding(encoding, true);
	}
	
	/*
	 * @see IEncodingSupport#getEncoding()
	 */
	public String getEncoding() {
		IDocumentProvider p= fTextEditor.getDocumentProvider();
		if (p instanceof IStorageDocumentProvider) {
			IStorageDocumentProvider provider= (IStorageDocumentProvider) p;
			return provider.getEncoding(fTextEditor.getEditorInput());
		}
		return null;
	}
	
	/*
	 * @see IEncodingSupport#getDefaultEncoding()
	 */
	public String getDefaultEncoding() {
		IDocumentProvider p= fTextEditor.getDocumentProvider();
		if (p instanceof IStorageDocumentProvider) {
			IStorageDocumentProvider provider= (IStorageDocumentProvider) p;
			return provider.getDefaultEncoding();
		}
		return null;
	}
	
	/**
	 * Returns a status header for the given status.
	 * @param status the status
	 * @return a status header for the given status.
	 */
	public String getStatusHeader(IStatus status) {
		Throwable t= status.getException();
		
		if (t instanceof CharConversionException)
			return TextEditorMessages.getString("Editor.error.unreadable_encoding.header"); //$NON-NLS-1$
		
		if (t instanceof UnsupportedEncodingException)
			return TextEditorMessages.getString("Editor.error.unsupported_encoding.header"); //$NON-NLS-1$
		
		return null;
	}
	
	/**
	 * Returns a banner for the given status
	 * @param status the status
	 * @return a banner for the given status.
	 */
	public String getStatusBanner(IStatus status) {
		Throwable t= status.getException();
		
		if (t instanceof CharConversionException)
			return TextEditorMessages.getString("Editor.error.unreadable_encoding.banner"); //$NON-NLS-1$
		
		if (t instanceof UnsupportedEncodingException)
			return TextEditorMessages.getString("Editor.error.unsupported_encoding.banner"); //$NON-NLS-1$
		
		return null;

	}
	
	/**
	 * Returns status messages for status objects indicating encoding problems or
	 * <code>null</code> otherwise.
	 */
	public String getStatusMessage(IStatus status) {
		Throwable t= status.getException();
		if (t instanceof CharConversionException || t instanceof UnsupportedEncodingException) {
			
			String encoding= getEncoding();
			if (encoding == null)
				encoding= getDefaultEncoding();
			
			if (t instanceof CharConversionException) {
				if (encoding != null)
					return MessageFormat.format(TextEditorMessages.getString("Editor.error.unreadable_encoding.message_arg"), new Object[] { encoding }); //$NON-NLS-1$
				return TextEditorMessages.getString("Editor.error.unreadable_encoding.message"); //$NON-NLS-1$
			}
			
			if (t instanceof UnsupportedEncodingException) {
				if (encoding != null)
					return MessageFormat.format(TextEditorMessages.getString("Editor.error.unsupported_encoding.message_arg"), new Object[] { encoding }); //$NON-NLS-1$
				return TextEditorMessages.getString("Editor.error.unsupported_encoding.message"); //$NON-NLS-1$
			}
		}
		
		return null;
	}
}
