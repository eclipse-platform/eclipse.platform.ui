/**
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.util;

import java.util.List;

import org.eclipse.e4.ui.model.application.*;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see org.eclipse.e4.ui.model.application.MApplicationPackage
 * @generated
 */
public class ApplicationSwitch<T1> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static MApplicationPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ApplicationSwitch() {
		if (modelPackage == null) {
			modelPackage = MApplicationPackage.eINSTANCE;
		}
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	public T1 doSwitch(EObject theEObject) {
		return doSwitch(theEObject.eClass(), theEObject);
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	protected T1 doSwitch(EClass theEClass, EObject theEObject) {
		if (theEClass.eContainer() == modelPackage) {
			return doSwitch(theEClass.getClassifierID(), theEObject);
		}
		else {
			List<EClass> eSuperTypes = theEClass.getESuperTypes();
			return
				eSuperTypes.isEmpty() ?
					defaultCase(theEObject) :
					doSwitch(eSuperTypes.get(0), theEObject);
		}
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	protected T1 doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case MApplicationPackage.VABSTRACT_V: {
				MV____________Abstract_____________V v____________Abstract_____________V = (MV____________Abstract_____________V)theEObject;
				T1 result = caseV____________Abstract_____________V(v____________Abstract_____________V);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.APPLICATION_ELEMENT: {
				MApplicationElement applicationElement = (MApplicationElement)theEObject;
				T1 result = caseApplicationElement(applicationElement);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.CONTRIBUTION: {
				MContribution contribution = (MContribution)theEObject;
				T1 result = caseContribution(contribution);
				if (result == null) result = caseApplicationElement(contribution);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.COMMAND: {
				MCommand command = (MCommand)theEObject;
				T1 result = caseCommand(command);
				if (result == null) result = caseApplicationElement(command);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.COMMAND_PARAMETER: {
				MCommandParameter commandParameter = (MCommandParameter)theEObject;
				T1 result = caseCommandParameter(commandParameter);
				if (result == null) result = caseApplicationElement(commandParameter);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.DIRTYABLE: {
				MDirtyable dirtyable = (MDirtyable)theEObject;
				T1 result = caseDirtyable(dirtyable);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.HANDLER: {
				MHandler handler = (MHandler)theEObject;
				T1 result = caseHandler(handler);
				if (result == null) result = caseContribution(handler);
				if (result == null) result = caseApplicationElement(handler);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.HANDLER_CONTAINER: {
				MHandlerContainer handlerContainer = (MHandlerContainer)theEObject;
				T1 result = caseHandlerContainer(handlerContainer);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.INPUT: {
				MInput input = (MInput)theEObject;
				T1 result = caseInput(input);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.PARAMETER: {
				MParameter parameter = (MParameter)theEObject;
				T1 result = caseParameter(parameter);
				if (result == null) result = caseApplicationElement(parameter);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.UI_LABEL: {
				MUILabel uiLabel = (MUILabel)theEObject;
				T1 result = caseUILabel(uiLabel);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.UI_ELEMENT: {
				MUIElement uiElement = (MUIElement)theEObject;
				T1 result = caseUIElement(uiElement);
				if (result == null) result = caseApplicationElement(uiElement);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.CONTEXT: {
				MContext context = (MContext)theEObject;
				T1 result = caseContext(context);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.KEY_SEQUENCE: {
				MKeySequence keySequence = (MKeySequence)theEObject;
				T1 result = caseKeySequence(keySequence);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.VTESTING_V: {
				MV_________Testing__________V v_________Testing__________V = (MV_________Testing__________V)theEObject;
				T1 result = caseV_________Testing__________V(v_________Testing__________V);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.TEST_HARNESS: {
				MTestHarness testHarness = (MTestHarness)theEObject;
				T1 result = caseTestHarness(testHarness);
				if (result == null) result = caseCommand(testHarness);
				if (result == null) result = caseContext(testHarness);
				if (result == null) result = caseContribution(testHarness);
				if (result == null) result = caseElementContainer(testHarness);
				if (result == null) result = caseParameter(testHarness);
				if (result == null) result = caseInput(testHarness);
				if (result == null) result = caseItem(testHarness);
				if (result == null) result = caseDirtyable(testHarness);
				if (result == null) result = caseUIElement(testHarness);
				if (result == null) result = caseUILabel(testHarness);
				if (result == null) result = caseApplicationElement(testHarness);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.VABSTRACT_CONTAINERS_V: {
				MV_________AbstractContainers__________V v_________AbstractContainers__________V = (MV_________AbstractContainers__________V)theEObject;
				T1 result = caseV_________AbstractContainers__________V(v_________AbstractContainers__________V);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.ELEMENT_CONTAINER: {
				MElementContainer<?> elementContainer = (MElementContainer<?>)theEObject;
				T1 result = caseElementContainer(elementContainer);
				if (result == null) result = caseUIElement(elementContainer);
				if (result == null) result = caseApplicationElement(elementContainer);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.GENERIC_TILE: {
				MGenericTile<?> genericTile = (MGenericTile<?>)theEObject;
				T1 result = caseGenericTile(genericTile);
				if (result == null) result = caseElementContainer(genericTile);
				if (result == null) result = caseUIElement(genericTile);
				if (result == null) result = caseApplicationElement(genericTile);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.TRIM_CONTAINER: {
				MTrimContainer<?> trimContainer = (MTrimContainer<?>)theEObject;
				T1 result = caseTrimContainer(trimContainer);
				if (result == null) result = caseElementContainer(trimContainer);
				if (result == null) result = caseUIElement(trimContainer);
				if (result == null) result = caseApplicationElement(trimContainer);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.VRCP_V: {
				MV______________RCP_______________V v______________RCP_______________V = (MV______________RCP_______________V)theEObject;
				T1 result = caseV______________RCP_______________V(v______________RCP_______________V);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.APPLICATION: {
				MApplication application = (MApplication)theEObject;
				T1 result = caseApplication(application);
				if (result == null) result = caseContext(application);
				if (result == null) result = caseElementContainer(application);
				if (result == null) result = caseHandlerContainer(application);
				if (result == null) result = caseBindingContainer(application);
				if (result == null) result = caseUIElement(application);
				if (result == null) result = caseApplicationElement(application);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.ITEM: {
				MItem item = (MItem)theEObject;
				T1 result = caseItem(item);
				if (result == null) result = caseUIElement(item);
				if (result == null) result = caseUILabel(item);
				if (result == null) result = caseApplicationElement(item);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.HANDLED_ITEM: {
				MHandledItem handledItem = (MHandledItem)theEObject;
				T1 result = caseHandledItem(handledItem);
				if (result == null) result = caseItem(handledItem);
				if (result == null) result = caseUIElement(handledItem);
				if (result == null) result = caseUILabel(handledItem);
				if (result == null) result = caseApplicationElement(handledItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.MENU_ITEM: {
				MMenuItem menuItem = (MMenuItem)theEObject;
				T1 result = caseMenuItem(menuItem);
				if (result == null) result = caseMenu(menuItem);
				if (result == null) result = caseItem(menuItem);
				if (result == null) result = caseElementContainer(menuItem);
				if (result == null) result = caseUILabel(menuItem);
				if (result == null) result = caseUIElement(menuItem);
				if (result == null) result = caseApplicationElement(menuItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.HANDLED_MENU_ITEM: {
				MHandledMenuItem handledMenuItem = (MHandledMenuItem)theEObject;
				T1 result = caseHandledMenuItem(handledMenuItem);
				if (result == null) result = caseMenuItem(handledMenuItem);
				if (result == null) result = caseHandledItem(handledMenuItem);
				if (result == null) result = caseMenu(handledMenuItem);
				if (result == null) result = caseItem(handledMenuItem);
				if (result == null) result = caseElementContainer(handledMenuItem);
				if (result == null) result = caseUILabel(handledMenuItem);
				if (result == null) result = caseUIElement(handledMenuItem);
				if (result == null) result = caseApplicationElement(handledMenuItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.DIRECT_MENU_ITEM: {
				MDirectMenuItem directMenuItem = (MDirectMenuItem)theEObject;
				T1 result = caseDirectMenuItem(directMenuItem);
				if (result == null) result = caseContribution(directMenuItem);
				if (result == null) result = caseMenuItem(directMenuItem);
				if (result == null) result = caseMenu(directMenuItem);
				if (result == null) result = caseItem(directMenuItem);
				if (result == null) result = caseElementContainer(directMenuItem);
				if (result == null) result = caseUILabel(directMenuItem);
				if (result == null) result = caseApplicationElement(directMenuItem);
				if (result == null) result = caseUIElement(directMenuItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.MENU: {
				MMenu menu = (MMenu)theEObject;
				T1 result = caseMenu(menu);
				if (result == null) result = caseElementContainer(menu);
				if (result == null) result = caseUIElement(menu);
				if (result == null) result = caseApplicationElement(menu);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.TOOL_ITEM: {
				MToolItem toolItem = (MToolItem)theEObject;
				T1 result = caseToolItem(toolItem);
				if (result == null) result = caseItem(toolItem);
				if (result == null) result = caseElementContainer(toolItem);
				if (result == null) result = caseUIElement(toolItem);
				if (result == null) result = caseUILabel(toolItem);
				if (result == null) result = caseApplicationElement(toolItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.HANDLED_TOOL_ITEM: {
				MHandledToolItem handledToolItem = (MHandledToolItem)theEObject;
				T1 result = caseHandledToolItem(handledToolItem);
				if (result == null) result = caseHandledItem(handledToolItem);
				if (result == null) result = caseToolItem(handledToolItem);
				if (result == null) result = caseElementContainer(handledToolItem);
				if (result == null) result = caseItem(handledToolItem);
				if (result == null) result = caseUIElement(handledToolItem);
				if (result == null) result = caseApplicationElement(handledToolItem);
				if (result == null) result = caseUILabel(handledToolItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.DIRECT_TOOL_ITEM: {
				MDirectToolItem directToolItem = (MDirectToolItem)theEObject;
				T1 result = caseDirectToolItem(directToolItem);
				if (result == null) result = caseContribution(directToolItem);
				if (result == null) result = caseToolItem(directToolItem);
				if (result == null) result = caseItem(directToolItem);
				if (result == null) result = caseElementContainer(directToolItem);
				if (result == null) result = caseUIElement(directToolItem);
				if (result == null) result = caseUILabel(directToolItem);
				if (result == null) result = caseApplicationElement(directToolItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.TOOL_BAR: {
				MToolBar toolBar = (MToolBar)theEObject;
				T1 result = caseToolBar(toolBar);
				if (result == null) result = caseElementContainer(toolBar);
				if (result == null) result = caseUIElement(toolBar);
				if (result == null) result = caseApplicationElement(toolBar);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.PSC_ELEMENT: {
				MPSCElement pscElement = (MPSCElement)theEObject;
				T1 result = casePSCElement(pscElement);
				if (result == null) result = caseUIElement(pscElement);
				if (result == null) result = caseApplicationElement(pscElement);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.PART: {
				MPart part = (MPart)theEObject;
				T1 result = casePart(part);
				if (result == null) result = caseContribution(part);
				if (result == null) result = caseContext(part);
				if (result == null) result = casePSCElement(part);
				if (result == null) result = caseUILabel(part);
				if (result == null) result = caseHandlerContainer(part);
				if (result == null) result = caseBindingContainer(part);
				if (result == null) result = caseUIElement(part);
				if (result == null) result = caseApplicationElement(part);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.SAVEABLE_PART: {
				MSaveablePart saveablePart = (MSaveablePart)theEObject;
				T1 result = caseSaveablePart(saveablePart);
				if (result == null) result = casePart(saveablePart);
				if (result == null) result = caseDirtyable(saveablePart);
				if (result == null) result = caseContribution(saveablePart);
				if (result == null) result = caseContext(saveablePart);
				if (result == null) result = casePSCElement(saveablePart);
				if (result == null) result = caseUILabel(saveablePart);
				if (result == null) result = caseHandlerContainer(saveablePart);
				if (result == null) result = caseBindingContainer(saveablePart);
				if (result == null) result = caseUIElement(saveablePart);
				if (result == null) result = caseApplicationElement(saveablePart);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.PART_STACK: {
				MPartStack partStack = (MPartStack)theEObject;
				T1 result = casePartStack(partStack);
				if (result == null) result = caseElementContainer(partStack);
				if (result == null) result = casePSCElement(partStack);
				if (result == null) result = caseUIElement(partStack);
				if (result == null) result = caseApplicationElement(partStack);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.PART_SASH_CONTAINER: {
				MPartSashContainer partSashContainer = (MPartSashContainer)theEObject;
				T1 result = casePartSashContainer(partSashContainer);
				if (result == null) result = caseGenericTile(partSashContainer);
				if (result == null) result = casePSCElement(partSashContainer);
				if (result == null) result = caseElementContainer(partSashContainer);
				if (result == null) result = caseUIElement(partSashContainer);
				if (result == null) result = caseApplicationElement(partSashContainer);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.WINDOW: {
				MWindow window = (MWindow)theEObject;
				T1 result = caseWindow(window);
				if (result == null) result = caseUILabel(window);
				if (result == null) result = caseElementContainer(window);
				if (result == null) result = caseContext(window);
				if (result == null) result = caseHandlerContainer(window);
				if (result == null) result = caseBindingContainer(window);
				if (result == null) result = caseUIElement(window);
				if (result == null) result = caseApplicationElement(window);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.WINDOW_TRIM: {
				MWindowTrim windowTrim = (MWindowTrim)theEObject;
				T1 result = caseWindowTrim(windowTrim);
				if (result == null) result = caseTrimContainer(windowTrim);
				if (result == null) result = casePSCElement(windowTrim);
				if (result == null) result = caseElementContainer(windowTrim);
				if (result == null) result = caseUIElement(windowTrim);
				if (result == null) result = caseApplicationElement(windowTrim);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.KEY_BINDING: {
				MKeyBinding keyBinding = (MKeyBinding)theEObject;
				T1 result = caseKeyBinding(keyBinding);
				if (result == null) result = caseKeySequence(keyBinding);
				if (result == null) result = caseApplicationElement(keyBinding);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.BINDING_CONTAINER: {
				MBindingContainer bindingContainer = (MBindingContainer)theEObject;
				T1 result = caseBindingContainer(bindingContainer);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.VIDE_V: {
				MV______________IDE_______________V v______________IDE_______________V = (MV______________IDE_______________V)theEObject;
				T1 result = caseV______________IDE_______________V(v______________IDE_______________V);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.VSC_ELEMENT: {
				MVSCElement vscElement = (MVSCElement)theEObject;
				T1 result = caseVSCElement(vscElement);
				if (result == null) result = caseUIElement(vscElement);
				if (result == null) result = caseApplicationElement(vscElement);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.VIEW: {
				MView view = (MView)theEObject;
				T1 result = caseView(view);
				if (result == null) result = casePart(view);
				if (result == null) result = caseVSCElement(view);
				if (result == null) result = caseContribution(view);
				if (result == null) result = caseContext(view);
				if (result == null) result = casePSCElement(view);
				if (result == null) result = caseUILabel(view);
				if (result == null) result = caseHandlerContainer(view);
				if (result == null) result = caseBindingContainer(view);
				if (result == null) result = caseUIElement(view);
				if (result == null) result = caseApplicationElement(view);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.VIEW_STACK: {
				MViewStack viewStack = (MViewStack)theEObject;
				T1 result = caseViewStack(viewStack);
				if (result == null) result = caseElementContainer(viewStack);
				if (result == null) result = caseVSCElement(viewStack);
				if (result == null) result = caseUIElement(viewStack);
				if (result == null) result = caseApplicationElement(viewStack);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.EDITOR: {
				MEditor editor = (MEditor)theEObject;
				T1 result = caseEditor(editor);
				if (result == null) result = caseInput(editor);
				if (result == null) result = caseESCElement(editor);
				if (result == null) result = caseSaveablePart(editor);
				if (result == null) result = casePart(editor);
				if (result == null) result = caseDirtyable(editor);
				if (result == null) result = caseContribution(editor);
				if (result == null) result = caseContext(editor);
				if (result == null) result = casePSCElement(editor);
				if (result == null) result = caseUILabel(editor);
				if (result == null) result = caseHandlerContainer(editor);
				if (result == null) result = caseBindingContainer(editor);
				if (result == null) result = caseUIElement(editor);
				if (result == null) result = caseApplicationElement(editor);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.VIEW_SASH_CONTAINER: {
				MViewSashContainer viewSashContainer = (MViewSashContainer)theEObject;
				T1 result = caseViewSashContainer(viewSashContainer);
				if (result == null) result = caseGenericTile(viewSashContainer);
				if (result == null) result = caseVSCElement(viewSashContainer);
				if (result == null) result = casePSCElement(viewSashContainer);
				if (result == null) result = caseElementContainer(viewSashContainer);
				if (result == null) result = caseUIElement(viewSashContainer);
				if (result == null) result = caseApplicationElement(viewSashContainer);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.MULTI_EDITOR: {
				MMultiEditor multiEditor = (MMultiEditor)theEObject;
				T1 result = caseMultiEditor(multiEditor);
				if (result == null) result = caseSaveablePart(multiEditor);
				if (result == null) result = caseElementContainer(multiEditor);
				if (result == null) result = casePart(multiEditor);
				if (result == null) result = caseDirtyable(multiEditor);
				if (result == null) result = caseContribution(multiEditor);
				if (result == null) result = caseContext(multiEditor);
				if (result == null) result = casePSCElement(multiEditor);
				if (result == null) result = caseUILabel(multiEditor);
				if (result == null) result = caseHandlerContainer(multiEditor);
				if (result == null) result = caseBindingContainer(multiEditor);
				if (result == null) result = caseUIElement(multiEditor);
				if (result == null) result = caseApplicationElement(multiEditor);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.ESC_ELEMENT: {
				MESCElement escElement = (MESCElement)theEObject;
				T1 result = caseESCElement(escElement);
				if (result == null) result = caseUIElement(escElement);
				if (result == null) result = caseApplicationElement(escElement);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.EDITOR_STACK: {
				MEditorStack editorStack = (MEditorStack)theEObject;
				T1 result = caseEditorStack(editorStack);
				if (result == null) result = caseElementContainer(editorStack);
				if (result == null) result = caseESCElement(editorStack);
				if (result == null) result = caseUIElement(editorStack);
				if (result == null) result = caseApplicationElement(editorStack);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.EDITOR_SASH_CONTAINER: {
				MEditorSashContainer editorSashContainer = (MEditorSashContainer)theEObject;
				T1 result = caseEditorSashContainer(editorSashContainer);
				if (result == null) result = caseGenericTile(editorSashContainer);
				if (result == null) result = caseESCElement(editorSashContainer);
				if (result == null) result = caseVSCElement(editorSashContainer);
				if (result == null) result = caseElementContainer(editorSashContainer);
				if (result == null) result = caseUIElement(editorSashContainer);
				if (result == null) result = caseApplicationElement(editorSashContainer);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.PERSPECTIVE: {
				MPerspective perspective = (MPerspective)theEObject;
				T1 result = casePerspective(perspective);
				if (result == null) result = caseUILabel(perspective);
				if (result == null) result = caseElementContainer(perspective);
				if (result == null) result = caseContext(perspective);
				if (result == null) result = casePSCElement(perspective);
				if (result == null) result = caseUIElement(perspective);
				if (result == null) result = caseApplicationElement(perspective);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.PERSPECTIVE_STACK: {
				MPerspectiveStack perspectiveStack = (MPerspectiveStack)theEObject;
				T1 result = casePerspectiveStack(perspectiveStack);
				if (result == null) result = caseElementContainer(perspectiveStack);
				if (result == null) result = caseVSCElement(perspectiveStack);
				if (result == null) result = casePSCElement(perspectiveStack);
				if (result == null) result = caseUIElement(perspectiveStack);
				if (result == null) result = caseApplicationElement(perspectiveStack);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.IDE_WINDOW: {
				MIDEWindow ideWindow = (MIDEWindow)theEObject;
				T1 result = caseIDEWindow(ideWindow);
				if (result == null) result = caseUILabel(ideWindow);
				if (result == null) result = caseContext(ideWindow);
				if (result == null) result = caseHandlerContainer(ideWindow);
				if (result == null) result = caseBindingContainer(ideWindow);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>VAbstract V</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>VAbstract V</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseV____________Abstract_____________V(MV____________Abstract_____________V object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Element</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Element</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseApplicationElement(MApplicationElement object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Contribution</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Contribution</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseContribution(MContribution object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Command</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Command</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseCommand(MCommand object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Command Parameter</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Command Parameter</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseCommandParameter(MCommandParameter object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Dirtyable</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Dirtyable</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseDirtyable(MDirtyable object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Handler</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Handler</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseHandler(MHandler object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Handler Container</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Handler Container</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseHandlerContainer(MHandlerContainer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Input</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Input</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseInput(MInput object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Parameter</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Parameter</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseParameter(MParameter object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>UI Label</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>UI Label</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseUILabel(MUILabel object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>UI Element</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>UI Element</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseUIElement(MUIElement object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Context</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Context</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseContext(MContext object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Key Sequence</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Key Sequence</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseKeySequence(MKeySequence object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>VTesting V</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>VTesting V</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseV_________Testing__________V(MV_________Testing__________V object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Test Harness</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Test Harness</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseTestHarness(MTestHarness object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>VAbstract Containers V</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>VAbstract Containers V</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseV_________AbstractContainers__________V(MV_________AbstractContainers__________V object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Element Container</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Element Container</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <T extends MUIElement> T1 caseElementContainer(MElementContainer<T> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Generic Tile</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Generic Tile</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <T extends MUIElement> T1 caseGenericTile(MGenericTile<T> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Trim Container</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Trim Container</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <T extends MUIElement> T1 caseTrimContainer(MTrimContainer<T> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>VRCP V</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>VRCP V</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseV______________RCP_______________V(MV______________RCP_______________V object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Application</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Application</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseApplication(MApplication object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseItem(MItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Handled Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Handled Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseHandledItem(MHandledItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Menu Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Menu Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseMenuItem(MMenuItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Handled Menu Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Handled Menu Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseHandledMenuItem(MHandledMenuItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Direct Menu Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Direct Menu Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseDirectMenuItem(MDirectMenuItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Menu</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Menu</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseMenu(MMenu object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Tool Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Tool Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseToolItem(MToolItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Handled Tool Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Handled Tool Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseHandledToolItem(MHandledToolItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Direct Tool Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Direct Tool Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseDirectToolItem(MDirectToolItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Tool Bar</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Tool Bar</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseToolBar(MToolBar object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>PSC Element</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>PSC Element</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 casePSCElement(MPSCElement object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Part</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Part</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 casePart(MPart object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Saveable Part</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Saveable Part</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseSaveablePart(MSaveablePart object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Part Stack</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Part Stack</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 casePartStack(MPartStack object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Part Sash Container</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Part Sash Container</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 casePartSashContainer(MPartSashContainer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Window</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Window</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseWindow(MWindow object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Window Trim</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Window Trim</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseWindowTrim(MWindowTrim object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Key Binding</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Key Binding</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseKeyBinding(MKeyBinding object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Binding Container</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Binding Container</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseBindingContainer(MBindingContainer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>VIDE V</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>VIDE V</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseV______________IDE_______________V(MV______________IDE_______________V object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>VSC Element</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>VSC Element</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseVSCElement(MVSCElement object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>View</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>View</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseView(MView object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>View Stack</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>View Stack</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseViewStack(MViewStack object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Editor</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Editor</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseEditor(MEditor object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>View Sash Container</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>View Sash Container</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseViewSashContainer(MViewSashContainer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Multi Editor</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Multi Editor</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseMultiEditor(MMultiEditor object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>ESC Element</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>ESC Element</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseESCElement(MESCElement object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Editor Stack</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Editor Stack</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseEditorStack(MEditorStack object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Editor Sash Container</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Editor Sash Container</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseEditorSashContainer(MEditorSashContainer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Perspective</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Perspective</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 casePerspective(MPerspective object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Perspective Stack</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Perspective Stack</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 casePerspectiveStack(MPerspectiveStack object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IDE Window</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IDE Window</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIDEWindow(MIDEWindow object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	public T1 defaultCase(EObject object) {
		return null;
	}

} //ApplicationSwitch
