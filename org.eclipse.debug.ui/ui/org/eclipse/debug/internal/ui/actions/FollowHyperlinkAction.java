///*******************************************************************************
// * Copyright (c) 2000, 2003 IBM Corporation and others.
// * All rights reserved. This program and the accompanying materials 
// * are made available under the terms of the Common Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/cpl-v10.html
// * 
// * Contributors:
// *     IBM Corporation - initial API and implementation
// *******************************************************************************/
//package org.eclipse.debug.internal.ui.actions;
//
//
//import org.eclipse.debug.internal.ui.DebugUIPlugin;
//import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
//import org.eclipse.debug.internal.ui.views.console.ConsoleViewer;
//import org.eclipse.debug.ui.console.IConsoleHyperlink;
//import org.eclipse.jface.text.ITextSelection;
//import org.eclipse.jface.viewers.ISelectionProvider;
//import org.eclipse.ui.ISharedImages;
//import org.eclipse.ui.actions.SelectionProviderAction;
//import org.eclipse.ui.help.WorkbenchHelp;
//import org.eclipse.ui.ide.IDE;
//
///**
// * Follows a hyperlink in the console
// */
//public class FollowHyperlinkAction extends SelectionProviderAction {
//
//	/**
//	 * Constructs a follow link action
//	 */
//	public FollowHyperlinkAction(ISelectionProvider selectionProvider) {
//		super(selectionProvider, ActionMessages.getString("FollowHyperlinkAction.&Open_Link_1")); //$NON-NLS-1$
//		setToolTipText(ActionMessages.getString("FollowHyperlinkAction.Follow_the_selected_hyperlink._2")); //$NON-NLS-1$
//		ISharedImages images= DebugUIPlugin.getDefault().getWorkbench().getSharedImages();
//		setImageDescriptor(images.getImageDescriptor(IDE.SharedImages.IMG_OPEN_MARKER));
//		WorkbenchHelp.setHelp(
//			this,
//			IDebugHelpContextIds.FOLLOW_CONSOLE_HYPERLINK_ACTION);
//	}
//	
//	public IConsoleHyperlink getHyperLink() {
//		ISelectionProvider selectionProvider = getSelectionProvider();
//		if (selectionProvider instanceof ConsoleViewer) {
//			ITextSelection textSelection = (ITextSelection)selectionProvider.getSelection();
//			ConsoleViewer consoleViewer = (ConsoleViewer)selectionProvider;
//			if (textSelection != null) {
//				return consoleViewer.getHyperlink(textSelection.getOffset());
//			}
//		}
//		return null;
//	}
//
//	/**
//	 * @see org.eclipse.jface.action.IAction#run()
//	 */
//	public void run() {
//		IConsoleHyperlink link = getHyperLink();
//		if (link != null) {
//			link.linkActivated();
//		}
//	}
//
//}
