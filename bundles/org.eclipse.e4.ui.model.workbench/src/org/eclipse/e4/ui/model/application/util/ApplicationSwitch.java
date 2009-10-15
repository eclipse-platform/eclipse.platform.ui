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
			case MApplicationPackage.UI_ITEM: {
				MUIItem uiItem = (MUIItem)theEObject;
				T1 result = caseUIItem(uiItem);
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
			case MApplicationPackage.TRIM_STRUCTURE: {
				MTrimStructure<?> trimStructure = (MTrimStructure<?>)theEObject;
				T1 result = caseTrimStructure(trimStructure);
				if (result == null) result = caseElementContainer(trimStructure);
				if (result == null) result = caseUIElement(trimStructure);
				if (result == null) result = caseApplicationElement(trimStructure);
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
				if (result == null) result = caseUIElement(application);
				if (result == null) result = caseApplicationElement(application);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.ITEM: {
				MItem item = (MItem)theEObject;
				T1 result = caseItem(item);
				if (result == null) result = caseUIElement(item);
				if (result == null) result = caseUIItem(item);
				if (result == null) result = caseContribution(item);
				if (result == null) result = caseApplicationElement(item);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.HANDLED_ITEM: {
				MHandledItem handledItem = (MHandledItem)theEObject;
				T1 result = caseHandledItem(handledItem);
				if (result == null) result = caseItem(handledItem);
				if (result == null) result = caseUIElement(handledItem);
				if (result == null) result = caseUIItem(handledItem);
				if (result == null) result = caseContribution(handledItem);
				if (result == null) result = caseApplicationElement(handledItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.MENU_ITEM: {
				MMenuItem menuItem = (MMenuItem)theEObject;
				T1 result = caseMenuItem(menuItem);
				if (result == null) result = caseItem(menuItem);
				if (result == null) result = caseMenu(menuItem);
				if (result == null) result = caseUIItem(menuItem);
				if (result == null) result = caseContribution(menuItem);
				if (result == null) result = caseElementContainer(menuItem);
				if (result == null) result = caseUIElement(menuItem);
				if (result == null) result = caseApplicationElement(menuItem);
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
				if (result == null) result = caseUIItem(toolItem);
				if (result == null) result = caseContribution(toolItem);
				if (result == null) result = caseApplicationElement(toolItem);
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
				if (result == null) result = caseUIItem(part);
				if (result == null) result = caseUIElement(part);
				if (result == null) result = caseApplicationElement(part);
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
				if (result == null) result = caseUIItem(window);
				if (result == null) result = caseElementContainer(window);
				if (result == null) result = caseContext(window);
				if (result == null) result = caseUIElement(window);
				if (result == null) result = caseApplicationElement(window);
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
				if (result == null) result = caseUIItem(view);
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
			case MApplicationPackage.EDITOR: {
				MEditor editor = (MEditor)theEObject;
				T1 result = caseEditor(editor);
				if (result == null) result = casePart(editor);
				if (result == null) result = caseInput(editor);
				if (result == null) result = caseESCElement(editor);
				if (result == null) result = caseContribution(editor);
				if (result == null) result = caseContext(editor);
				if (result == null) result = casePSCElement(editor);
				if (result == null) result = caseUIItem(editor);
				if (result == null) result = caseUIElement(editor);
				if (result == null) result = caseApplicationElement(editor);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.MULTI_EDITOR: {
				MMultiEditor multiEditor = (MMultiEditor)theEObject;
				T1 result = caseMultiEditor(multiEditor);
				if (result == null) result = caseEditor(multiEditor);
				if (result == null) result = caseElementContainer(multiEditor);
				if (result == null) result = casePart(multiEditor);
				if (result == null) result = caseInput(multiEditor);
				if (result == null) result = caseESCElement(multiEditor);
				if (result == null) result = caseContribution(multiEditor);
				if (result == null) result = caseContext(multiEditor);
				if (result == null) result = casePSCElement(multiEditor);
				if (result == null) result = caseUIItem(multiEditor);
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
				if (result == null) result = caseUIItem(perspective);
				if (result == null) result = caseElementContainer(perspective);
				if (result == null) result = caseContext(perspective);
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
				if (result == null) result = caseUIElement(perspectiveStack);
				if (result == null) result = caseApplicationElement(perspectiveStack);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MApplicationPackage.IDE_WINDOW: {
				MIDEWindow ideWindow = (MIDEWindow)theEObject;
				T1 result = caseIDEWindow(ideWindow);
				if (result == null) result = caseTrimStructure(ideWindow);
				if (result == null) result = caseUIItem(ideWindow);
				if (result == null) result = caseContext(ideWindow);
				if (result == null) result = caseElementContainer(ideWindow);
				if (result == null) result = caseUIElement(ideWindow);
				if (result == null) result = caseApplicationElement(ideWindow);
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
	 * Returns the result of interpreting the object as an instance of '<em>UI Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>UI Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseUIItem(MUIItem object) {
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
	 * Returns the result of interpreting the object as an instance of '<em>Trim Structure</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Trim Structure</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <T extends MUIElement> T1 caseTrimStructure(MTrimStructure<T> object) {
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
