Eclipse4/RCP/Modeled UI/Tags
============================

Most model elements support additional annotations called "tags". In addition to tags, a key/value store can also be defined which is called "persistedState". The tags an persisted state are free-form; the model renderer may interpret certain tags or persisted state to configure how an element is to be rendered. You can also use the tags and persisted state yourself to put information in the model.

![Tagsandstate.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/400px-Tagsandstate.png)

How did we find these tags
--------------------------

Until now, these tags were only documented in the Java source code.

To find these tags, we used the Model Spy and examined the Eclipse IDE Runtime model. Then we searched the source code for the documentation and pasted it here.

This list is not complete. If you know of some more tags/state used by the rendering engine or other standard mechanisms then please add them here.

Tags and Persisted State elements
---------------------------------

Eclipse E4 Model Tags and Persisted State elements
| Type | Value | Description | Applies to | Defined by |
| --- | --- | --- | --- | --- |
| Tag | Active | This is the tag name that indicates that the model element is active. | MUIElement, MPart, ?? | [IPresentationEngine.ACTIVE](https://google.com/?q=IPresentationEngine.ACTIVE) |
| Tag | categoryTag:`name` | Puts the PlaceHolder in the category with this `name` | PlaceHolder, ?? | [ViewContentProvider.CATEGORY_TAG](https://google.com/?q=ViewContentProvider.CATEGORY_TAG) |
| State | Custom Renderer | This is a persisted state 'key' whose value is expected to be the URI of a subclass of ABstractPartRenderer that is to be used to render the element | MUIElement, ?? | [IPresentationEngine.CUSTOM\_RENDERER\_KEY](https://google.com/?q=IPresentationEngine.CUSTOM_RENDERER_KEY) |
| Tag | Draggable | This is the tag name that enables the DND support for the element. The element's tags list has to be updated with the tag in order to enable the DND processing. | Toolbar, ?? | [IPresentationEngine.NO_CLOSE](https://google.com/?q=IPresentationEngine.NO_CLOSE) |
| State | e4\_disabled\_icon\_image\_key | This key should be used to add an optional String to an element that is a URI to the elements disabled icon. This is used, for example, by Toolbar Items which, in Eclipse SDK, provide a unique icon for disabled tool items that look better than the OS default graying on the default icon. There is a strong argument to be made that this disabledIconURI actually be part of the model | ToolbarItem, ?? | [IPresentationEngine.DISABLED\_ICON\_IMAGE_KEY](https://google.com/?q=IPresentationEngine.DISABLED_ICON_IMAGE_KEY) |
| State | e4\_override\_icon\_image\_key | This key should be used to add an optional org.eclipse.swt.graphics.Image to an element's TRANSIENTDATA. If present, the image will be used to override that elements icon URI. An example is drawing the error icon on a minimized problems view stack.NOTE: This image must be checked to ensure that it hasn't been disposed of on retrieval. | ToolbarItem, ?? | [IPresentationEngine.OVERRIDE\_ICON\_IMAGE_KEY](https://google.com/?q=IPresentationEngine.OVERRIDE_ICON_IMAGE_KEY) |
| Tag | Editor | Mark the PlaceHolder or Part as an Eclipse Editor. | MPlaceHolder, MPart, ?? | [Workbench.EDITOR_TAG](https://google.com/?q=Workbench.EDITOR_TAG) |
| Tag | glue | A `TrimBar` will replace `ToolControl`s with a `"stretch"` tag with stretchable space. A ToolControl with `"glue"` will ensure its siblings are kept together on the same line. The following example will cause the `"find"` control to be placed flush right: | MTrimBar | [TrimBarLayout.GLUE](https://google.com/?q=TrimBarLayout.GLUE) |
| Tag | Horizontal | This tag can be applied to an element as a hint to the renderers that the element would prefer to be horizontal. For an MPart this could be used both as a hint to how to show the view when it's in the trim but could also be used when picking a stack to add a newly opening part to. It could also be used for example to control where the tabs appear on an MPartStack. | MPart, MPartStack, ?? | [IPresentationEngine.ORIENTATION_HORIZONTAL](https://google.com/?q=IPresentationEngine.ORIENTATION_HORIZONTAL) |
| Tag | Maximized | When added to an element's 'tags' this should cause the presentation to minimize all other presentation elements. In the default implementation, you can only apply this tag to an MPartStack or the MPlaceholder of the MArea. | MPartStack, MPlaceholder | [IPresentationEngine.MAXIMIZED](https://google.com/?q=IPresentationEngine.MAXIMIZED) |
| Tag | Minimized | When added to an element's 'tags' this should cause the presentation to move that element to the trim. In the default implementation, you can only apply this tag to an MPartStack or the MPlaceholder of the MArea. | MPartStack, MPlaceholder | [IPresentationEngine.MINIMIZED](https://google.com/?q=IPresentationEngine.MINIMIZED) |
| Tag | MinimizedByZoom | This tag should be applied to any element that had its MINIMIZED tag set due to a different element going maximized. This allows the restore operation to only restore elements that the user did not explicitly minimize. | MPartStack, MPlaceholder | [IPresentationEngine.MINIMIZED\_BY\_ZOOM](https://google.com/?q=IPresentationEngine.MINIMIZED_BY_ZOOM) |
| Tag | MinMaximizeableChildrenArea | When applied to an MArea causes it to behave like a MPartSashContainer allowing the different parts to be minimized/maximized separately. | MArea | [IPE.MIN\_MAXIMIZEABLE\_CHILDREN\_AREA\_TAG](https://google.com/?q=IPresentationEngine.MIN_MAXIMIZEABLE_CHILDREN_AREA_TAG) |
| Tag | NoClose | Parts within a part stack can be annotated with a `"NoClose"` tag to indicate that the part should not be closeable. The SWT renderer will configure the corresponding `CTabFolder` to not display an "X" to close the part. | MPartStack | [IPresentationEngine.NO_CLOSE](https://google.com/?q=IPresentationEngine.NO_CLOSE) |
| Tag | NoDetach | When applied as a tag to an MUIElement inhibits detaching the element (ie.through DnD... | MUIElement, ?? | [IPresentationEngine.NO_DETACH](https://google.com/?q=IPresentationEngine.NO_DETACH) |
| Tag | NoMove | A part can be annotated with a `"NoMove"` tag to indicate that the drag-and-drop system should not allow the user to move this part. | MPart | [IPresentationEngine.NO_MOVE](https://google.com/?q=IPresentationEngine.NO_MOVE) |
| Tag | NoRestore | When applied as a tag to an MPartDescriptor marks the part as not restorable. | MPartDescriptor | [IPresentationEngine.NO_RESTORE](https://google.com/?q=IPresentationEngine.NO_RESTORE) |
| Tag | NoTitle | Parts within a part stack can be annotated with a `"NoTitle"` tag to indicate that the part should not have a rendered title. | MPart | [IPresentationEngine.NO_TITLE](https://google.com/?q=IPresentationEngine.NO_TITLE) |
| Tag | Opaque | A tag value that indicates a menu, menu item, menu separator, or tool item is 'opaque' | Menu, MenuSeparator, DirectMenuItem, ?? | [OpaqueElementUtil.OPAQUE_TAG](https://google.com/?q=OpaqueElementUtil.OPAQUE_TAG) |
| State | persistState {false:true} | Whether the workbench should save and restore its state. Individual model elements can add it to their persisted state with the value of "false" to declare that they should not be persisted. | Toolbar, MPart, ?? | [IWorkbench.PERSIST_STATE](https://google.com/?q=IWorkbench.PERSIST_STATE) |
| State | Rendering Parent | This key can be used, if the model element does not have a parent and a parent needs to be specified for the renderer to create the widget | Toolbar, MPart, ?? | [IPresentationEngine.RENDERING\_PARENT\_KEY](https://google.com/?q=IPresentationEngine.RENDERING_PARENT_KEY) |
| Tag | shellMaximized | When applied to an MWindow causes the renderer to maximize the resulting control. | MWindow | [IPresentationEngine.WINDOW\_MAXIMIZED\_TAG](https://google.com/?q=IPresentationEngine.WINDOW_MAXIMIZED_TAG) |
| Tag | shellMinimized | When applied to an MWindow, it causes the renderer to minimize the resulting control. | MWindow | [IPresentationEngine.WINDOW\_MINIMIZED\_TAG](https://google.com/?q=IPresentationEngine.WINDOW_MINIMIZED_TAG) |
| Tag | shellTopLevel | When applied to an MWindow causes the renderer to render the resulting control as a top level window. | MWindow | [IPresentationEngine.WINDOW\_TOP\_LEVEL](https://google.com/?q=IPresentationEngine.WINDOW_TOP_LEVEL) |
| Tag | Split Horizontal | This tag can be applied to an element (usually an MPart) to indicate that the element should be split with the result being side by side. | MPart, ?? | [IPresentationEngine.SPLIT_HORIZONTAL](https://google.com/?q=IPresentationEngine.SPLIT_HORIZONTAL) |
| Tag | Split Vertical | This tag can be applied to an element (usually an MPart) to indicate that the element shouldbe split with the result being one above the other. | MPart, ?? | [IPresentationEngine.SPLIT_VERTICAL](https://google.com/?q=IPresentationEngine.SPLIT_VERTICAL) |
| Tag | Standalone | Declare the stack as containing a single 'standalone' view. These stacks will not allow either dragging the view out of the stack nor dragging other views in. | MPartStack, ?? | [IPresentationEngine.STANDALONE](https://google.com/?q=IPresentationEngine.STANDALONE) |
| Tag | stretch | A `TrimBar` will replace `ToolControl`s with a `"stretch"` tag with stretchable space. A ToolControl with `"glue"` will ensure its siblings are kept together on the same line. The following example will cause the `"find"` control to be placed flush right: | MTrimBar | [TrimBarLayout.SPACER](https://google.com/?q=TrimBarLayout.SPACER) |
| Tag | Vertical | This tag can be applied to an element as a hint to the renderers that the element would prefer to be vertical. For an MPart this could be used both as a hint to how to show the view when it's in the trim but could also be used when picking a stack to add a newly opening part to. It could also be used for example to control where the tabs appear on an MPartStack. | MPart, MPartStack, ?? | [IPresentationEngine.ORIENTATION_VERTICAL](https://google.com/?q=IPresentationEngine.ORIENTATION_VERTICAL) |
| Tag | View | Mark the PlaceHolder as an Eclipse View. | MPlaceHolder, ?? | [ViewRegistry.VIEW_TAG](https://google.com/?q=ViewRegistry.VIEW_TAG) |

