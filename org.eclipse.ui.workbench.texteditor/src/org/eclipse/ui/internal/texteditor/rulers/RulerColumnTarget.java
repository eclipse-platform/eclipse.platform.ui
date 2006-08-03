/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.rulers;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;

/**
 * Describes the target of a contribution to the <code>org.eclipse.ui.texteditor.rulerColumn</code>
 * extension point.
 * 
 * @since 3.3
 */
public abstract class RulerColumnTarget {
	public abstract boolean matchesEditorId(String editorId);
	public abstract boolean matchesContentType(IContentType contentType);
//	public abstract boolean matchesEditorInput(IEditorInput input);
//	public abstract boolean matchesEditor(IEditorReference reference);

	/* package visible */
	RulerColumnTarget() {
	}
	
	public static RulerColumnTarget createAllTarget() {
		return new AllTarget();
	}
	public static RulerColumnTarget createOrTarget(RulerColumnTarget either, RulerColumnTarget or) {
		Assert.isLegal(or != null || either != null);
		if (either == null)
			return or;
		if (or == null)
			return either;
		return new OrTarget(either, or);
	}
	public static RulerColumnTarget createContentTypeTarget(String contentTypeId) {
		return new ContentTypeTarget(contentTypeId);
	}
	public static RulerColumnTarget createEditorIdTarget(String editorId) {
		return new EditorIdTarget(editorId);
	}
}

final class AllTarget extends RulerColumnTarget {
	AllTarget() {
	}

	public boolean matchesContentType(IContentType contentType) {
		return true;
	}

	public boolean matchesEditorId(String editorId) {
		return true;
	}

	public String toString() {
		return "All"; //$NON-NLS-1$
	}
}

final class OrTarget extends RulerColumnTarget {
	private final RulerColumnTarget fEither;
	private final RulerColumnTarget fOr;

	OrTarget(RulerColumnTarget either, RulerColumnTarget or) {
		fEither= either;
		fOr= or;
		Assert.isLegal(either != null);
		Assert.isLegal(or != null);
	}

	public boolean matchesContentType(IContentType contentType) {
		return fEither.matchesContentType(contentType) || fOr.matchesContentType(contentType);
	}

	public boolean matchesEditorId(String editorId) {
		return fEither.matchesEditorId(editorId) || fOr.matchesEditorId(editorId);
	}
	
	public String toString() {
		return fEither.toString() + " || " + fOr.toString(); //$NON-NLS-1$
	}
}

final class EditorIdTarget extends RulerColumnTarget {
	private final String fEditorId;

	EditorIdTarget(String id) {
		Assert.isLegal(id != null);
		fEditorId= id;
	}

	public boolean matchesContentType(IContentType contentType) {
		return false;
	}

	public boolean matchesEditorId(String editorId) {
		return fEditorId.equals(editorId);
	}

	public String toString() {
		return "editorID="+fEditorId; //$NON-NLS-1$
	}
}

final class ContentTypeTarget extends RulerColumnTarget {
	private final IContentType fContentType;
	
	ContentTypeTarget(String contentTypeId) {
		Assert.isLegal(contentTypeId != null);
		fContentType= Platform.getContentTypeManager().getContentType(contentTypeId);
	}
	
	public boolean matchesContentType(IContentType contentType) {
		return fContentType != null && contentType != null && contentType.isKindOf(fContentType);
	}
	
	public boolean matchesEditorId(String editorId) {
		return false;
	}

	public String toString() {
		return "contentType=" + fContentType; //$NON-NLS-1$
	}
}
