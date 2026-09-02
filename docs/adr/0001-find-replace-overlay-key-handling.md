# 1. Find/Replace overlay: key event and editor command handling

How the Find/Replace overlay makes sure that the right commands are in effect while one of its
input fields has focus: its own, and those of the surrounding workbench, but not those of the
editor it is drawn on, whose key bindings and handlers would otherwise carry out edits on the
document rather than in the field the user is typing into. The overlay is not a workbench part,
so the framework does not separate the two on its own. This decision covers how that separation
is achieved, and how it is achieved without tying the overlay to a particular kind of editor.

## Status

Accepted.

## Context

The Find/Replace overlay shows text input fields on top of a text editor. Its `containerControl`
is a plain SWT `Composite` parented into the editor's widget tree.

Being embedded rather than living in its own `Shell` is a hard requirement: a shell has to
manually follow the target widget's move, resize and hide/show operations, always lags behind by
some milliseconds, and cannot be positioned at all under Wayland (see commit `78c9a1c60a`, which
introduced the embedded composite for these reasons). **A separate shell is therefore out of
scope**, which constrains the whole design space below.

The consequence of being embedded is the root of the problem: focus in the overlay's fields does
not change the active workbench part. The editor stays active, so its key-binding scopes and its
command handlers stay in effect and compete with the overlay for every keystroke.

### Goals

1. Standard text editing keys (Ctrl+C/V/X/Z/A, Ctrl+Backspace, word navigation, Home/End,
   arrows, Delete) must operate on the overlay's input fields, not on the editor's document.
2. The retargetable global actions (Edit > Cut/Copy/Paste/Select All) must be bound to the
   overlay's fields while they have focus, so the Edit menu and toolbar act on them.
3. Commands in the "In Windows" scope (Save, Close, Next Editor, Preferences, ...) must stay
   executable while the overlay has focus.
4. The overlay must not depend on `AbstractTextEditor` or `StatusTextEditor`, so it can later
   serve non-text editors.
5. No reflective access to private platform API.

### How Eclipse decides which handler runs

Two independent gates decide whether a handler activation wins.

**Gate 1, reachability.** `HandlerServiceImpl#lookUpHandler` resolves
`context.getActiveLeaf().get("handler::" + commandId)`, and the `HandlerSelectionFunction`
installed there walks from that leaf up to the root. An activation in a context that is not an
ancestor of the active leaf is never considered. Key-binding scopes work the same way:
`ActiveContextsFunction` unions the `localContexts` of every context from the active leaf
upwards, and that union is what `ContextManager` and the E4 `BindingService` use.

**Gate 2, expression.** Among reachable activations, only those whose `activeWhen` expression
evaluates to true participate. The winner is chosen by `HandlerActivation#compareTo` on source
priorities computed by `SourcePriorityNameMapping#computeSourcePriority` from the variables the
expression accesses. There is no part/window layering in this comparison; `getDepth()` is
always `0`. Note that `activeFocusControl` maps to `ISources.ACTIVE_MENU` (`1 << 31`), which
`compareTo` normalises into `1 << 30`, higher than any other source. That, and not any notion
of part-level versus window-level, is why an `ACTIVE_FOCUS_CONTROL`-scoped activation outranks
the editor's.

Legacy `ISources` variables are resolved by `ExpressionContext#getVariable` through
`IEclipseContext#getActive(name)`, that is, **from the active leaf upwards**, so a context
nearer the leaf can shadow them.

A key reaches the widget when the winning handler reports `isHandled() == false`:
`KeyBindingDispatcher#executeCommand` computes `commandHandled` from the handler, `press()`
returns false, `processKeyEvent` leaves `event.doit` untouched, and SWT delivers the key
natively. A command with no handler at all behaves the same way.

### Where the editor registers what

The editor's commands do not all live in one place, which is what makes the problem non-obvious:

| Registered by | Into which context | Guarded by |
|---|---|---|
| Editor's `KeyBindingService` (scopes such as `org.eclipse.ui.textEditorScope`) | the **part** context | n/a |
| Editor's own `IHandlerService.activateHandler` calls | the **part** context | `ActivePartExpression` |
| Retargetable actions (cut, copy, paste, delete, select all, undo, redo) via `IActionBars.setGlobalActionHandler` | the **window** context, because `EditorReference` constructs `EditorActionBars` with `page.getWorkbenchWindow()` as service locator | `LegacyEditorActionBarExpression`, comparing `activePartId` against the editor id |

Any approach that only detaches the part context addresses the first two rows and leaves the
third untouched. The third row holds exactly the commands users notice most.

### Context inheritance of the overlay's own scope

`ContextSet` resolves parent contexts when building the set a binding lookup runs against. If
the overlay's own key-binding context declares `parentId="org.eclipse.ui.textEditorScope"`, the
editor scope is pulled back in **even when the editor's part context has been taken off the
active chain**, and Ctrl+Delete still resolves to `deleteNextWord`. Giving that context a parent
outside the editor scope is therefore a precondition for the chosen alternative, not cosmetic
tidying towards goal 4.

## Decision

While one of the overlay's input fields has focus, the overlay activates an `IEclipseContext` of
its own below the window context and publishes its own id as the active part id. That takes the
editor's key binding scopes and part-level handlers off the active context chain, and makes the
expression guarding its retargetable actions evaluate to false, so no editor handler is left in
the command resolution path and no individual command has to be suppressed. The overlay's own key
binding scopes are activated in that same context, which also gives them their lifetime, and its
shared scope is given a parent outside `org.eclipse.ui.textEditorScope`.

### Alternatives

**Suppress the editor's commands individually.** The editor stays the active part, and for every
command whose key an input field needs, its handler is neutralised or replaced while the field
has focus, so that the key falls through to the widget. This leaves the workbench's view of the
world untouched, since the editor remains the active part throughout. It needs a set of affected
command ids, which is either maintained by hand, and then misses commands contributed by other
plugins, or derived from the current key bindings, and then unbounded. Suppression works per
command id, so a command bound to both a native and a non-native key loses both. It also only
takes the keys away from the editor: nothing binds them to the input field, so cut, copy and
paste stay unbound and the menu entries appear enabled while doing nothing, leaving goal 2 unmet.

**Make the overlay a workbench element of its own.** Give the overlay its own shell, or model it
as a real part and activate it, and the framework's own activation takes the editor's contexts
and handlers off the active chain. This needs no per-command work at all, and is how content
assist, Quick Access and dialogs already avoid the problem. A shell is excluded by the embedding
requirement stated in the context. A real part changes the part service's notion of the active
part, so part listeners, the selection service, Outline, link-with-editor and the editor's tab
all react, and it needs a home in the application model that a floating overlay does not
naturally have.

**Give the overlay a context of its own (adopted).** Create an `IEclipseContext` for the overlay
and activate it while an input field has focus, without the overlay becoming a part as far as the
part service is concerned. This addresses both gates at once and needs no command set: the
editor's part-level registrations become unreachable, and its window-level ones evaluate to
false. The platform's own default handlers for cut, copy, paste and select all then act on the
focused field, which meets goal 2 without registering anything, while window-scoped commands stay
reachable. Its cost is that it relies on a divergence between the active-leaf chain and
`EPartService`'s notion of the active part which E4 does not promise to preserve, that the
`MPart` it carries exists only in a context and not in the application model, and that shadowing
`activePartId` also switches off contributions keyed on that variable against the host editor.

### Observed behaviour

With the search field focused in a text editor. The columns are variants described in the
appendix.

| | Non-handling overrides | Own context, window | Own context, application | **Adopted** |
|---|---|---|---|---|
| `textEditorScope` active | yes | no | no | **no** |
| Ctrl+Delete resolves | yes | no | no | **no** |
| `edit.undo` / `edit.delete` handler | non-handling override | editor | none | **none** |
| `edit.copy` / `edit.selectAll` handler | non-handling override | editor | platform default | **platform default** |
| `deleteNextWord` handler | non-handling override | none | none | **none** |
| Save / Close / Next Editor / Preferences | ok | ok | ok | **ok** |
| `IWorkbenchWindow` from leaf | ok | ok | **null** | **ok** |
| `ISources.activePart` / `activeEditor` | editor | editor | **undefined** / editor | **editor** |
| Overlay's own bindings and handlers | ok | ok | ok | **ok** |

## Consequences

- The overlay does not reference `AbstractTextEditor`, and nothing in the key handling depends on
  the editor type. Goal 4 is met for the command infrastructure, though `FindReplaceOverlay`
  still has two `instanceof StatusTextEditor` checks (target control, colours) and
  `FindReplaceAction#shouldUseOverlay()` still gates the overlay on `StatusTextEditor`, so the
  overlay remains text-editor-only until those are addressed separately.
- Cut, copy, paste and select all work on the overlay's fields through the platform's default
  handlers, including from the Edit menu.
- Undo, delete and the word-wise editing and navigation commands resolve to no handler while the
  overlay has focus, so the native `Text` widget handles those keys.
- Multi-page editors need no handling of their own, and the mechanism that used to provide it is
  removed rather than replaced. `LegacyEditorActionBarExpression` compares `activePartId` against
  the editor id it was constructed with and does nothing else, so shadowing that variable with the
  overlay's id makes it false whichever editor id it carries and whichever of a multi-page editor's
  two registration paths put the handler there. The gap that made the contributor variant fail, and
  that `DeactivateGlobalActionHandlers` was introduced to close, therefore cannot arise here. This
  follows from the expression's implementation; no test pins it, since the existing find/replace
  tests do not exercise multi-page editors.
- `org.eclipse.ui.workbench.texteditor` gains a dependency on the E4 context and model bundles.
- Per-focus imperative state is limited to activating and deactivating that one context plus
  switching which of the two per-field scopes is activated inside it. The overlay's shared scope
  is activated once, because scopes activated in the overlay's context are active exactly while
  that context is the active leaf.
- Handing the active leaf back when an input field loses focus must happen only while the overlay
  still holds that leaf. A click activates the part under the mouse before the focus leaves the
  field, so on that path the new part already owns the leaf and has to keep it. Handing it to the
  editor anyway raises the editor's key-binding scope beside the one the new part brings, and while
  both are up every stroke the two scopes share is an unresolvable binding conflict. Observed by
  clicking from the overlay into the console, which reports conflicting zoom bindings; it is
  reported from `PartServiceImpl#activate`, when the context updates deferred for the part switch
  are flushed. Whether it surfaces depends on how the other part holds its scope: a scope that
  rides the active-leaf chain drops as the leaf moves, which is why an ordinary view does not show
  it. Clicking into the console without the overlay involved does not produce it either.

## Appendix: variants and dead ends

### Suppressing the editor's commands

**Reflection into `AbstractTextEditor#setActionActivation`, plus nulling the action bars.** On
focus gained, call the private `setActionActivation(false)` reflectively, and for multi-page
editors additionally null out the `IActionBars` global action handlers for
cut/copy/paste/delete/select-all/find. Reverse both on focus lost. Addresses both registration
sites, but uses reflective private API against goal 5, is hard-coded to `AbstractTextEditor` and
therefore a no-op for any other editor against goal 4, and covers only the six global action ids,
not word navigation or undo.

**Non-handling handler overrides.** Collect the commands bound to the ~40 key sequences that SWT
`Text` handles natively and, for each, activate a handler at the target part's `IHandlerService`
that returns `isHandled() == false`, scoped by an expression on `ACTIVE_FOCUS_CONTROL`, whose
rogue-bit priority outranks the editor's activations. Needs no reflection and is not tied to an
editor class, but each `activateHandler` also sets `handler::<cmdId>` in the part context through
`EHandlerService`, and `deactivateHandlers` only clears the `legacy::handler::` list, so that
entry outlives the overlay and shadows any pure-E4 handler for that command for the rest of the
editor's life. Deriving the command set from `BindingManager#getBindings()` returns every
declared binding across all schemes, platforms and locales, may return `null`, and is a snapshot
that ignores later changes in Preferences > Keys; a fixed list avoids that but misses third-party
commands.

**`IEditorActionBarContributor#setActiveEditor(null)` on focus.** Makes the contributor call
`IActionBars.setGlobalActionHandler(id, null)` for each action it manages, removing the
window-level activations, restored on focus lost. Public API, and correct for single-page
editors, but `MultiPageEditorSite#getActionBarContributor()` returns `null` by specification and
the fallback via the outer editor does not reliably cover handlers the inner editor registered.
Addresses only the global action bar subset.

The reason the fallback does not suffice is that a multi-page editor's global action handlers are
registered twice over the same shared `SubActionBars`. The platform calls
`outerContributor.setActiveEditor(outerEditor)` when the multi-page editor becomes active, and
then focusing an inner `AbstractTextEditor` page runs its `setActionActivation(true)`, whose
private `setActiveEditor` resolves the very same outer contributor through
`MultiPageEditorSite#getMultiPageEditor()` and calls it again with the *inner* editor, overwriting
the first registration. Asking that contributor to forget its active editor clears only what it
itself tracks, which need not match what the inner editor put there, so a generic
`MultiPageEditorActionBarContributor` leaves the inner editor's paste handler active. That the two
registration sites really are distinct is on record: commit `69f6e45a` had to add
`DeactivateGlobalActionHandlers` for multi-page editors *in addition to* the reflection call of
the previous variant, which would have been unnecessary had addressing the contributor been
enough.

**Explicit pass-through handlers.** Register real handlers calling the corresponding `Text`
method. Expresses intent directly and avoids the enablement problem, but SWT `Text` exposes only
`cut()`, `copy()`, `paste()` and `selectAll()`, so most of the ~30 affected operations cannot be
implemented this way, let alone correctly across platforms and locale-sensitive word boundaries.

### Making the overlay a workbench element of its own

**Own `Shell`.** `ShellActivationListener` gives an unmodelled shell a child context of the
application context, activates it, and activates `org.eclipse.ui.contexts.dialog`. Complete
isolation for free, but reintroduces exactly what the embedded composite exists to avoid.

**Real `MPart` activated through `EPartService`.** The threshold is
`PartServiceImpl#isInContainer`, true once the part is found by a `PRESENTATION`-scoped model
search or tagged as a hosted element in the window's shared elements. Crossing it has two
observable effects: `PartServiceImpl` activates the part, and since it carries no compatibility
wrapper `IWorkbenchPage#getActivePart()` then reports `null` while the overlay has focus and part
deactivation is broadcast for the editor; and placing the part in the editor area additionally
renders it, which takes focus away from the input field altogether.

### Giving the overlay a context of its own

**Parented on the window context.** Removes `textEditorScope` and the editor's part-level
handlers with no per-command work, and does not disturb the part service. Insufficient on its
own: the retargetable actions are registered at the window handler service, the window context
remains an ancestor, and `activePartId` still resolves to the editor, so undo, copy, delete and
select all keep resolving to the editor's handlers.

**Parented on the application context.** The topology `ShellActivationListener` gives a dialog
shell, which additionally takes the window context off the chain. Full isolation, and the
platform's default handlers surface as described in the decision, but window-scoped services stop
being injectable (`IWorkbenchWindow` resolves to `null`) and the legacy `activePart` variable
becomes undefined, so `HandlerUtil#getActivePart` and any `activeWhen` keyed on the active part
go false, putting goal 3 at risk.

**Parented on the window context, shadowing `activePartId` (adopted).** Setting
`ISources.ACTIVE_PART_ID_NAME` to the overlay's own id in that context makes
`LegacyEditorActionBarExpression` evaluate to false, since legacy variables resolve from the
active leaf upwards. The editor's window-level activations stay reachable but stop participating,
so gate 2 does the work that detaching the window context does in the previous variant, without
its cost. Only the id is shadowed, not `ACTIVE_PART_NAME`, so anything needing a part still sees
the editor. The context activation is also stable under ordinary interaction: clicking into the
fields, moving focus to an overlay toolbar button and back, and switching between editor and
overlay by mouse all leave the overlay's context as the active leaf, despite the `SWT.Activate`
listener `ContributedPartRenderer` installs on the editor's composite.

The `MPart` carried by the context is what lets `ActivePartLookupFunction` resolve an active part
for it. Without it that lookup yields `null`, and `PartServiceImpl` answers a null active part by
firing part deactivation and clearing the active selection. With it, the same code path finds a
part outside the application model and returns early, leaving the part service untouched.

### Context manipulations that do not remove the editor from resolution

- **`EContextService#deactivateContext("org.eclipse.ui.textEditorScope")` on the part's
  context** removes the id from that context's `localContexts`, but the editor's
  `KeyBindingService` owns and re-establishes those activations.
- **`IContextService#deactivateContext` (3.x)** needs the `IContextActivation` token, which
  belongs to the editor's `KeyBindingService`.
- **`ContextManager#setActiveContextIds`** is not exposed through `IContextService`; reaching it
  means casting to the internal implementation, which goal 5 excludes.
- **`IContextService#activateContext(id, falseExpression)`** cannot help, because context
  activations are additive.
- **A child `IEclipseContext` of the *part* context** leaves the part context an ancestor, so
  `ActiveContextsFunction` still finds `textEditorScope`. Only a context that is not a descendant
  of the part context helps.

### The focus service

`IFocusService#addFocusTracker(control, id)` makes `ACTIVE_FOCUS_CONTROL` and
`ACTIVE_FOCUS_CONTROL_ID` reflect the overlay's fields when they have focus. It is the documented
way to attach `WidgetMethodHandler`-based cut/copy/paste to a widget outside the part lifecycle,
and it is the source whose priority lets the suppression variants outrank the editor. It changes
nothing on its own, and `FocusControlSourceProvider` clears the variables on focus lost of a
tracked control, so anything keyed on them is inactive while an overlay toolbar button has focus.
For the adopted alternative it is redundant, because `activePartId` already changes at exactly
the same moments and additionally identifies *which* overlay is focused.

## Related

- `FindReplaceOverlayCommandSupport`, `FindReplaceOverlayContextSupport`
- Commit `78c9a1c60a`, which replaced the shell-based overlay with the embedded composite
- Commit `69f6e45a` and issue
  <https://github.com/eclipse-platform/eclipse.platform.ui/issues/2509>, the multi-page editor
  paste defect that motivated `DeactivateGlobalActionHandlers`
- Issue <https://github.com/eclipse-platform/eclipse.platform.ui/issues/1912>
- Issue <https://github.com/eclipse-platform/eclipse.platform.ui/issues/2093>, the workbench-part
  proposal
