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
 *******************************************************************************/
package org.eclipse.jface.text.templates.persistence;

import java.util.UUID;

import org.eclipse.jface.text.templates.Template;


/**
 * TemplatePersistenceData stores information about a template. It uniquely
 * references contributed templates via their id. Contributed templates may be
 * deleted or modified. All template may be enabled or not.
 * <p>
 * Clients may use this class, although this is not usually needed except when
 * implementing a custom template preference page or template store. This class
 * is not intended to be subclassed.
 * </p>
 *
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 * @deprecated See {@link org.eclipse.text.templates.TemplatePersistenceData}
 */
@Deprecated
public class TemplatePersistenceData extends org.eclipse.text.templates.TemplatePersistenceData {

	private final org.eclipse.text.templates.TemplatePersistenceData ref;

	/**
	 * In some cases, we must continue to respect the deprecated TemplatePresistenceData
	 * even though we are given {@link org.eclipse.text.templates.TemplatePersistenceData}.
	 *
	 * @param data The {@link org.eclipse.text.templates.TemplatePersistenceData} that will
	 * underlie this object.
	 * @since 3.14
	 */
	public TemplatePersistenceData(org.eclipse.text.templates.TemplatePersistenceData data) {
		super(data.getTemplate(), data.isEnabled(), data.getId()); // these are ignored
		this.ref= data instanceof TemplatePersistenceData ? ((TemplatePersistenceData) data).ref : data; // no double wrapping
	}

	public TemplatePersistenceData(Template template, boolean enabled) {
		this(new org.eclipse.text.templates.TemplatePersistenceData(template, enabled));
	}

	public TemplatePersistenceData(Template template, boolean enabled, String id) {
		this(new org.eclipse.text.templates.TemplatePersistenceData(template, enabled, id));
	}

	@Override
	public String getId() {
		return ref.getId();
	}

	@Override
	public boolean isDeleted() {
		return ref.isDeleted();
	}

	@Override
	public void setDeleted(boolean isDeleted) {
		ref.setDeleted(isDeleted);
	}

	@Override
	public Template getTemplate() {
		return ref.getTemplate();
	}

	@Override
	public void setTemplate(Template template) {
		ref.setTemplate(template);
	}

	@Override
	public boolean isCustom() {
		return ref.isCustom();
	}

	@Override
	public boolean isModified() {
		return ref.isModified();
	}

	@Override
	public boolean isUserAdded() {
		return ref.isUserAdded();
	}

	@Override
	public void revert() {
		ref.revert();
	}

	@Override
	public boolean isEnabled() {
		return ref.isEnabled();
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		ref.setEnabled(isEnabled);
	}

	@Override
	public boolean equals(Object other) {
		return ref.equals(other);
	}

	@Override
	public int hashCode() {
		return ref.hashCode();
	}

	@Override
	protected UUID getUniqueIdForEquals() {
		return getUniqueIdForEquals(ref);
	}

}
