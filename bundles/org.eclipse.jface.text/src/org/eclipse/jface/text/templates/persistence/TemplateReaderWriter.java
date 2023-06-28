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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ResourceBundle;


/**
 * Serializes templates as character or byte stream and reads the same format
 * back.
 * <p>
 * Clients may instantiate this class, it is not intended to be
 * subclassed.</p>
 *
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 * @deprecated See {@link org.eclipse.text.templates.TemplateReaderWriter}
 */
@Deprecated
public class TemplateReaderWriter extends org.eclipse.text.templates.TemplateReaderWriter {

	public TemplateReaderWriter() {
	}

	@Override
	public TemplatePersistenceData[] read(Reader reader) throws IOException {
		org.eclipse.text.templates.TemplatePersistenceData[] list= super.read(reader);
		TemplatePersistenceData[] result= new TemplatePersistenceData[list.length];
		for (int i= 0; i < list.length; i++) {
			result[i]= new TemplatePersistenceData(list[i]);
		}
		return result;
	}

	@Override
	public TemplatePersistenceData readSingle(Reader reader, String id) throws IOException {
		return new org.eclipse.jface.text.templates.persistence.TemplatePersistenceData(super.readSingle(reader, id));
	}

	@Override
	public TemplatePersistenceData[] read(Reader reader, ResourceBundle bundle) throws IOException {
		org.eclipse.text.templates.TemplatePersistenceData[] list= super.read(reader, bundle);
		TemplatePersistenceData[] result= new TemplatePersistenceData[list.length];
		for (int i= 0; i < list.length; i++) {
			result[i]= new TemplatePersistenceData(list[i]);
		}
		return result;
	}

	@Override
	public TemplatePersistenceData[] read(InputStream stream, ResourceBundle bundle) throws IOException {
		org.eclipse.text.templates.TemplatePersistenceData[] list= super.read(stream, bundle);
		TemplatePersistenceData[] result= new TemplatePersistenceData[list.length];
		for (int i= 0; i < list.length; i++) {
			result[i]= new TemplatePersistenceData(list[i]);
		}
		return result;
	}

	public void save(TemplatePersistenceData[] templates, OutputStream stream) throws IOException {
		super.save(templates, stream);
	}

	public void save(TemplatePersistenceData[] templates, Writer writer) throws IOException {
		super.save(templates, writer);
	}

}

