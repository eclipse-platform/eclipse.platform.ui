package org.eclipse.ui.editors.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;

/**
 * Help context ids for the encoding actions.
 */
public interface IEncodingActionsHelpContextIds {
	public static final String US_ASCII= IAbstractTextEditorHelpContextIds.PREFIX + IEncodingActionsConstants.US_ASCII + IAbstractTextEditorHelpContextIds.ACTION_POSTFIX;
	public static final String ISO_8859_1=  IAbstractTextEditorHelpContextIds.PREFIX + IEncodingActionsConstants.ISO_8859_1 + IAbstractTextEditorHelpContextIds.ACTION_POSTFIX;
	public static final String UTF_8=  IAbstractTextEditorHelpContextIds.PREFIX + IEncodingActionsConstants.UTF_8 + IAbstractTextEditorHelpContextIds.ACTION_POSTFIX;
	public static final String UTF_16BE=  IAbstractTextEditorHelpContextIds.PREFIX + IEncodingActionsConstants.UTF_16BE + IAbstractTextEditorHelpContextIds.ACTION_POSTFIX;
	public static final String UTF_16LE=  IAbstractTextEditorHelpContextIds.PREFIX + IEncodingActionsConstants.UTF_16LE + IAbstractTextEditorHelpContextIds.ACTION_POSTFIX;
	public static final String UTF_16=  IAbstractTextEditorHelpContextIds.PREFIX + IEncodingActionsConstants.UTF_16 + IAbstractTextEditorHelpContextIds.ACTION_POSTFIX;
	public static final String SYSTEM=  IAbstractTextEditorHelpContextIds.PREFIX + IEncodingActionsConstants.SYSTEM + IAbstractTextEditorHelpContextIds.ACTION_POSTFIX;
	public static final String CUSTOM=  IAbstractTextEditorHelpContextIds.PREFIX + IEncodingActionsConstants.CUSTOM + IAbstractTextEditorHelpContextIds.ACTION_POSTFIX;
}