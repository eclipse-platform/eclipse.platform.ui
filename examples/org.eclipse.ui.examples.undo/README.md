## **Example - Undo**

### Introduction

The Undo Example adds two views to the workbench. The **Box View** is a rudimentary view that allows the user to create boxes by clicking into empty space and dragging the mouse to form a box. Boxes can be moved by selecting a box and dragging it around. The user may undo and redo any operations performed in the box view. The **Undo History View** shows the undo history maintained by the workbench operations history.

### Features demonstrated in the example

*   Creating an `IUndoableOperation` inside an action (Delete all boxes) to perform the action's work .
*   Creating an `IUndoableOperation` based on an operation implied by a gesture (Adding and moving boxes).
*   Using a local `IUndoContext` to keep undo operations local to a particular view.
*   Using the platform undo and redo action handlers to provide undo and redo in a view.
*   Providing a user preference for setting the undo limit for a particular undo context.
*   Using an `IOperationApprover` to install additional policy (Prompt before undo) in the operation history.
*   Using `IOperationHistory` protocol to show undo history for different undo contexts (Undo History View).

### Features not demonstrated

*   Since the example is focused on simple undo, the BoxView code is kept to a minimum. Therefore, it does not provide the expected graphical editor features such as selection, resize and selection handles, color and line style attributes, etc. For the same reason, advanced features in the undo framework are not shown.
*   There is no example of assigning multiple undo contexts in order to share operations between views or editors.
*   There is no example of using composite undo operations.

### Running the example

From Eclipse's **Window** menu select **Show View** > **Other...**. In the **Show View** dialog, expand **Undo Examples** and select the view named **Box View**. The box view will appear.

Likewise, from the **Window** menu select **Show View** > **Other...**. In the **Show View** dialog, expand **Undo Examples** and select the view named **Undo History View**. A view containing the undo history will appear. This view can be used alongside the Box View to watch the undo history as boxes are added or moved. It can also be used independently of the Box View to follow the undo history for any view or editor that uses the workbench operations history to track undoable operations.

### Details

#### Box View

Click in the box view and drag the mouse to create a box that follows the mouse. Clicking inside an existing box and dragging the mouse will move the box to a new location. Note the operations that appear in the **Undo** and **Redo** menus as boxes are added and moved. The box view can be cleared by selecting **Delete all boxes** from the context menu or the view's local menu and toolbar. This operation can also be undone.

#### Undo History View

The Undo History View shows the operations available for undo in all undo contexts. To view the history in a particular undo context, select **Filter on...** from the view's context menu. This will filter the undo history on a particular undo context. The view can be used to view the undo history for the Box View, for SDK text editors, and for undoable operations that affect the workspace, such as refactoring operations. Undo and redo can be performed from the Undo History View's menus. **Undo selected** will attempt to undo the selected operation in the undo history, regardless of its position in the operation history. Depending on the operation in question, this may or may not be allowed. For example, the Box View allows all add and move operations to be undone or redone even if they aren't the most recently performed operation. Text editors will prompt if an attempt is made to undo a typing operation that is not the most recent.

#### Example Preferences

Preferences are provided that affect the operation of both views. From Eclipse's **Window** menu select **Preferences** > **Undo Preferences**.

*   **Undo history limit** controls how many undoable operations are kept in the history for the Box View.
*   **Show debug labels in undo history view** controls whether the simple label shown in the undo menu is used for displaying operations, or a debug label that includes information such as the assigned undo context(s).
*   **Confirm all undo operations** controls whether prompting occurs before undoing or redoing an operation. This preference is used by the operation approver installed by the example.