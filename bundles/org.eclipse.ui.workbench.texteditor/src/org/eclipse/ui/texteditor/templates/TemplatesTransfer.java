/*******************************************************************************
 * Copyright (c) 2007, 2008 Dakshinamurthy Karra, IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dakshinamurthy Karra (Jalian Systems) - Templates View - https://bugs.eclipse.org/bugs/show_bug.cgi?id=69581
 *******************************************************************************/
package org.eclipse.ui.texteditor.templates;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import org.eclipse.text.templates.TemplatePersistenceData;


/**
 * Transfer type used for clipboard and drag and drop operations for templates.
 * The templates are transfered as {@link TemplatePersistenceData}.
 * <p>
 * FIXME: only works inside the same workspace.
 * </p>
 *
 * @see AbstractTemplatesPage
 * @since 3.4
 */
class TemplatesTransfer extends ByteArrayTransfer {

	private static final TemplatesTransfer INSTANCE= new TemplatesTransfer();
	private static final String TYPE_NAME= "template-transfer-format:" + System.currentTimeMillis() + ":" + INSTANCE.hashCode(); //$NON-NLS-1$ //$NON-NLS-2$
	private static final int TYPE_ID= registerType(TYPE_NAME);

	private TemplatePersistenceData[] fObject ;


	/**
	 * Returns the singleton instance of this <code>TemplateTransfer</code> class.
	 *
	 * @return the singleton template transfer instance
	 */
	public static TemplatesTransfer getInstance() {
		return INSTANCE;
	}

	@Override
	protected int[] getTypeIds() {
		return new int[] { TYPE_ID };
	}

	@Override
	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}

	@Override
	protected void javaToNative(Object object, TransferData transferData) {
		if (object == null || !(object instanceof TemplatePersistenceData[]) || !isSupportedType(transferData)) {
			fObject= null ;
			return;
		}
		fObject= (TemplatePersistenceData[]) object ;
		super.javaToNative(TYPE_NAME.getBytes(), transferData);
	}

	@Override
	protected Object nativeToJava(TransferData transferData) {
		Object result= super.nativeToJava(transferData);
		if (!(result instanceof byte[]) || !TYPE_NAME.equals(new String((byte[]) result)))
			return null ;
		return fObject ;
	}
}
