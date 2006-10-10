/**
 * 
 */
package org.eclipse.compare.internal.patch;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

class PatchedFileNode implements ITypedElement, IStreamContentAccessor, IEditableContent {

	byte[] bytes;
	String type;
	String name;
	private boolean editable;
	
	public PatchedFileNode(byte[] bytes, String type, String name) {
		this.bytes = bytes;
		this.type = type;
		this.name = name;
		this.editable = false;
	}

	public PatchedFileNode(byte[] bytes, String type, String name, boolean editable) {
		this.bytes = bytes;
		this.type = type;
		this.name = name;
		this.editable = editable;
	}

	public Image getImage() {
		return null;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public InputStream getContents() throws CoreException {
		return new ByteArrayInputStream(bytes);
	}

	public boolean isEditable() {
		return editable;
	}

	public ITypedElement replace(ITypedElement dest, ITypedElement src) {
		return null;
	}

	public void setContent(byte[] newContent) {
		bytes = new byte[newContent.length];
		System.arraycopy(newContent, 0, bytes, 0, newContent.length);
	}

	byte[] getBytes() {
		return bytes;
	}

}
