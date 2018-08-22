/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.rulers;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;

/**
 * Describes the target of a contribution to the <code>org.eclipse.ui.texteditor.rulerColumns</code>
 * extension point.
 *
 * @since 3.3
 */
public abstract class RulerColumnTarget {
	public abstract boolean matchesEditorId(String editorId);
	public abstract boolean matchesContentType(IContentType contentType);
	public abstract boolean matchesClass(Class<?> clazz);

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

	public static RulerColumnTarget createClassTarget(String className) {
		return new ClassTarget(className);
	}
}

final class AllTarget extends RulerColumnTarget {
	AllTarget() {
	}

	@Override
	public boolean matchesContentType(IContentType contentType) {
		return true;
	}

	@Override
	public boolean matchesEditorId(String editorId) {
		return true;
	}

	@Override
	public boolean matchesClass(Class<?> clazz) {
		return true;
	}

	@Override
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

	@Override
	public boolean matchesContentType(IContentType contentType) {
		return fEither.matchesContentType(contentType) || fOr.matchesContentType(contentType);
	}

	@Override
	public boolean matchesEditorId(String editorId) {
		return fEither.matchesEditorId(editorId) || fOr.matchesEditorId(editorId);
	}

	@Override
	public boolean matchesClass(Class<?> clazz) {
		return fEither.matchesClass(clazz) || fOr.matchesClass(clazz);
	}

	@Override
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

	@Override
	public boolean matchesContentType(IContentType contentType) {
		return false;
	}

	@Override
	public boolean matchesEditorId(String editorId) {
		return fEditorId.equals(editorId);
	}

	@Override
	public boolean matchesClass(Class<?> clazz) {
		return false;
	}

	@Override
	public String toString() {
		return "editorID=" + fEditorId; //$NON-NLS-1$
	}
}

final class ClassTarget extends RulerColumnTarget {
	private final String fClassName;

	ClassTarget(String className) {
		Assert.isLegal(className != null);
		fClassName= className;
	}

	@Override
	public boolean matchesContentType(IContentType contentType) {
		return false;
	}

	@Override
	public boolean matchesEditorId(String editorId) {
		return false;
	}

	@Override
	public boolean matchesClass(Class<?> clazz) {
		Assert.isLegal(clazz != null);

		do {
			if (clazz.getName().equals(fClassName))
				return true;
			clazz= clazz.getSuperclass();
		} while (clazz != null);

		return false;
	}

	@Override
	public String toString() {
		return "class=" + fClassName; //$NON-NLS-1$
	}
}

final class ContentTypeTarget extends RulerColumnTarget {
	private final IContentType fContentType;

	ContentTypeTarget(String contentTypeId) {
		Assert.isLegal(contentTypeId != null);
		fContentType= Platform.getContentTypeManager().getContentType(contentTypeId);
	}

	@Override
	public boolean matchesContentType(IContentType contentType) {
		return fContentType != null && contentType != null && contentType.isKindOf(fContentType);
	}

	@Override
	public boolean matchesEditorId(String editorId) {
		return false;
	}

	@Override
	public boolean matchesClass(Class<?> clazz) {
		return false;
	}

	@Override
	public String toString() {
		return "contentType=" + fContentType; //$NON-NLS-1$
	}
}
