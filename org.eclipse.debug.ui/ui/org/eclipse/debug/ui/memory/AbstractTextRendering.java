/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
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
package org.eclipse.debug.ui.memory;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Abstract implementation of a rendering that translates memory into
 * text, displayed in a table.
 * <p>
 * Clients should subclass from this class if they wish to provide a table
 * text rendering with a specific code page.
 * </p>
 * @since 3.1
 */
abstract public class AbstractTextRendering extends AbstractTableRendering {

	private String fCodePage;

	/**
	 * Constructs a text rendering of the specified type.
	 *
	 * @param renderingId memory rendering type identifier
	 */
	public AbstractTextRendering(String renderingId)
	{
		super(renderingId);
	}

	/**
	 * Constructs a text rendering of the specified type on the given
	 * code page.
	 *
	 * @param renderingId memory rendering type identifier
	 * @param codePage the name of a supported
     *  {@link java.nio.charset.Charset </code>charset<code>}, for
     *  example <code>CP1252</code>
	 */
	public AbstractTextRendering(String renderingId, String codePage)
	{
		super(renderingId);
		fCodePage = codePage;
	}

	/**
	 * Sets the code page for this rendering.  This does not cause
	 * the rendering to be updated with the new code page.  Clients need
	 * to update the rendering manually when the code page is changed.
	 *
	 * @param codePage the name of a supported
	 * {@link java.nio.charset.Charset </code>charset<code>}, for
     *  example <code>CP1252</code>
	 */
	public void setCodePage(String codePage)
	{
		fCodePage = codePage;
	}

	/**
	 * Returns the current code page used by this rendering.  Returns null
	 * if not set.
	 * @return  Returns the current code page used by this rendering.  Returns null
	 * if not set.
	 */
	public String getCodePage()
	{
		return fCodePage;
	}

	@Override
	public String getString(String dataType, BigInteger address,  MemoryByte[] data) {
		try {
			String paddedStr = DebugUIPlugin.getDefault().getPreferenceStore().getString(IDebugUIConstants.PREF_PADDED_STR);
			if(fCodePage == null) {
				return IInternalDebugCoreConstants.EMPTY_STRING;
			}

			boolean[] invalid = new boolean[data.length];
			boolean hasInvalid = false;
			byte byteArray[] = new byte[data.length];
			for (int i=0; i<data.length; i++)
			{
				if (!data[i].isReadable())
				{
					invalid[i] = true;
					hasInvalid = true;
				}
				byteArray[i] = data[i].getValue();
			}

			if (hasInvalid)
			{
				StringBuffer strBuf = new StringBuffer();
				for (int i=0; i<data.length; i++)
				{
					if (invalid[i]) {
						strBuf.append(paddedStr);
					} else {
						strBuf.append(new String(new byte[]{byteArray[i]}, fCodePage));
					}
				}
				return strBuf.toString();
			}

			return new String(byteArray, fCodePage);

		} catch (UnsupportedEncodingException e) {
			return "-- error --"; //$NON-NLS-1$
		}
	}

	@Override
	public byte[] getBytes(String dataType, BigInteger address, MemoryByte[] currentValues, String data) {
		try {

			if (fCodePage == null) {
				return new byte[0];
			}

			byte[] bytes =  data.getBytes(fCodePage);
			return bytes;

		} catch (UnsupportedEncodingException e) {
			return new byte[0];
		}
	}
}
