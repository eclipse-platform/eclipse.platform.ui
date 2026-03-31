Image Loading in Eclipse Platform UI
=====================================

This document describes how images are loaded, cached, and managed in Eclipse Platform UI,
covering JFace image infrastructure, workbench shared images, and the existing CSS image
support. 
It is intended as a foundation for evaluating how CSS-based image exchange could be enhanced.

## Overview

Eclipse Platform UI uses a layered image infrastructure:

1. **`ImageDescriptor`** (JFace) — lightweight, stateless description of an image that can create SWT `Image` objects on demand.
2. **`ImageRegistry`** (JFace) — cache that maps symbolic string keys to `Image`/`ImageDescriptor` pairs, with automatic disposal when the `Display` is disposed.
3. **`JFaceResources`** — global singleton providing access to the shared JFace `ImageRegistry`.
4. **`WorkbenchImages` / `ISharedImages`** — workbench-level registry of platform icons.
5. **E4 CSS engine** — can apply images to SWT widgets via `background-image` CSS properties.

## Core Classes

### ImageDescriptor

**Location:** `bundles/org.eclipse.jface/src/org/eclipse/jface/resource/ImageDescriptor.java`

`ImageDescriptor` is the central abstraction.
It is a lightweight, immutable description of an image that does not hold onto any SWT resources.
An actual `Image` is created only when explicitly requested.

Key factory methods:

```java
// From a bundle-relative path (preferred)
ImageDescriptor.createFromFile(MyPlugin.class, "icons/sample.png");

// From a URL
ImageDescriptor.createFromURL(url);

// Deferred URL lookup (lazy, preferred for plugin startup)
ImageDescriptor.createFromURLSupplier(true, () -> BundleUtility.find(pluginId, path));

// Derived image with SWT flags (e.g. greyed-out disabled variant)
ImageDescriptor.createWithFlags(descriptor, SWT.IMAGE_DISABLE);
```

Key usage methods:

| Method | Description |
|--------|-------------|
| `createImage()` | Returns a new `Image`; caller must dispose it. |
| `createImage(boolean returnMissingImageOnError)` | Same, with error-image fallback option. |
| `createResource(Device)` | Returns a shared `Image` managed by a `ResourceManager`. |
| `destroyResource(Object)` | Releases a resource obtained via `createResource()`. |

#### Concrete Subclasses

| Class | Description |
|-------|-------------|
| `URLImageDescriptor` | Loads from a URL; supports HiDPI via `ImageDataProvider` / `ImageFileNameProvider`. |
| `DeferredImageDescriptor` | Wraps a `Supplier<URL>` for lazy resolution; avoids class-loading at startup. |
| `CompositeImageDescriptor` | Abstract base for composed/synthesized images (e.g. overlays). |
| `DerivedImageDescriptor` | Applies SWT transformation flags (`IMAGE_DISABLE`, `IMAGE_GRAY`, `IMAGE_COPY`) to an existing descriptor. |
| `ImageDataImageDescriptor` | Wraps an existing `Image` or `ImageData`. |
| `DecorationOverlayIcon` | Overlays decoration images on the four corners of a base image. |

### ImageRegistry

**Location:** `bundles/org.eclipse.jface/src/org/eclipse/jface/resource/ImageRegistry.java`

`ImageRegistry` maintains a `Map<String, Entry>` where each entry holds both an `ImageDescriptor` and, once created, a cached `Image`. Images are created lazily on first access and are automatically disposed when the `Display` is disposed.

Callers must **not** dispose images obtained from a registry — the registry owns them.

```java
ImageRegistry registry = JFaceResources.getImageRegistry();

// Register
registry.put("my.icon", ImageDescriptor.createFromFile(MyClass.class, "icons/icon.svg"));

// Retrieve image (creates it on first call)
Image image = registry.get("my.icon");

// Retrieve descriptor
ImageDescriptor descriptor = registry.getDescriptor("my.icon");
```

`ImageRegistry` is **not thread-safe** and must be accessed from the UI thread.

### JFaceResources

**Location:** `bundles/org.eclipse.jface/src/org/eclipse/jface/resource/JFaceResources.java`

Global entry point for JFace-managed resources.
The shared `ImageRegistry` is initialized lazily and backed by a `ResourceManager` with a default cache of 300 images.

The standard icon path constant for localized icons is:

```java
JFaceResources.ICONS_PATH = "$nl$/icons/full/"
```

The `$nl$` segment is resolved by `FileLocator` to the locale-specific icon directory.

### ResourceLocator

**Location:** `bundles/org.eclipse.jface/src/org/eclipse/jface/resource/ResourceLocator.java`

Utility class for resolving bundle resources to URLs.

```java
// Resolve a bundle-relative path to an Optional<URL>
Optional<URL> url = ResourceLocator.locate("com.example.plugin", "icons/sample.svg");

// Create a descriptor directly
Optional<ImageDescriptor> desc =
    ResourceLocator.imageDescriptorFromBundle("com.example.plugin", "icons/sample.svg");
```

- Uses `platform:/plugin/<bundleId>/<path>` URIs internally.
- Supports `$nl$` for NLS path resolution via `FileLocator.find()`.
- Paths must **not** start with `.` or `/`.

### AbstractUIPlugin.imageDescriptorFromPlugin()

**Location:** `bundles/org.eclipse.ui.workbench/eclipseui/org/eclipse/ui/plugin/AbstractUIPlugin.java`

Convenience method for loading images from a plugin:

```java
ImageDescriptor desc = AbstractUIPlugin.imageDescriptorFromPlugin(
    "com.example.plugin", "icons/sample.svg");
```

Resolution order:
1. Checks the workbench's shared image registry.
2. Falls back to `ResourceLocator.imageDescriptorFromBundle()`.

## Workbench Image Infrastructure

### WorkbenchImages

**Location:** `bundles/org.eclipse.ui.workbench/eclipseui/org/eclipse/ui/internal/WorkbenchImages.java`

Central declaration point for all built-in workbench icons. Images are declared using deferred descriptors for minimal startup overhead:

```java
declareImage(
    ISharedImages.IMG_ETOOL_SAVE_EDIT,
    IWorkbenchGraphicConstants.IMG_DTOOL_SAVE_EDIT,
    "icons/full/etool16/save_edit.svg",
    true /* shared */
);
```

`WorkbenchImages` creates disabled variants automatically via `ImageDescriptor.createWithFlags(desc, SWT.IMAGE_DISABLE)`.

Icon directory convention under `$nl$/icons/full/`:

| Directory | Purpose |
|-----------|---------|
| `etool16/` | Enabled toolbar icons (16px) |
| `elcl16/` | Enabled local toolbar icons (16px) |
| `eview16/` | View icons (16px) |
| `ovr16/` | Overlay/decorator icons (16px) |
| `obj16/` | Model object icons (16px) |
| `wizban/` | Wizard banner images |
| `pref/` | Preference page icons |

SVG is now the standard image format for workbench icons.

### ISharedImages

**Location:** `bundles/org.eclipse.ui.workbench/eclipseui/org/eclipse/ui/ISharedImages.java`

Public API for accessing shared workbench images:

```java
ISharedImages shared = PlatformUI.getWorkbench().getSharedImages();
Image image = shared.getImage(ISharedImages.IMG_TOOL_COPY);
ImageDescriptor desc = shared.getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT);
```

Each icon constant has a corresponding disabled variant (e.g. `IMG_TOOL_COPY_DISABLED`).

## Image Loading Flow

The following sequence shows how an image travels from a plugin's `icons/` folder to an SWT widget:

```
Plugin's icons/ folder (e.g. icons/sample.svg)
        |
        | ResourceLocator.locate() / BundleUtility.find()
        |   → FileLocator resolves platform:/plugin/... URL
        ↓
ImageDescriptor.createFromURLSupplier(true, () -> url)
        |   → DeferredImageDescriptor (lazy, no I/O at declaration time)
        ↓
ImageRegistry.put(key, descriptor)
        |   → stored without creating the Image yet
        ↓
ImageRegistry.get(key)  [first access]
        |   → calls descriptor.createImage()
        |   → URLImageDescriptor reads the file, decodes image data
        |   → creates SWT Image on the current Display
        ↓
SWT Image
        |
        ↓
Widget.setImage(image) / Label.setImage(image) / etc.
```

For HiDPI displays, `URLImageDescriptor` uses `ImageFileNameProvider` or `ImageDataProvider`
to look for `@1.5x` / `@2x` variants alongside the base file.

## CSS Image Support

The E4 CSS engine has partial support for applying images to SWT widgets.

### Key Classes

| Class | Location | Role |
|-------|----------|------|
| `CSSSWTImageHelper` | `bundles/org.eclipse.e4.ui.css.swt/src/.../helpers/CSSSWTImageHelper.java` | Converts CSS URI values to SWT `Image` objects; stores/restores default images. |
| `CSSValueSWTImageConverterImpl` | `bundles/org.eclipse.e4.ui.css.swt/src/.../converters/CSSValueSWTImageConverterImpl.java` | `ICSSValueConverter` that converts `CSSValue` (URI type) to `Image`. |
| `CSSPropertyBackgroundSWTHandler` | `bundles/org.eclipse.e4.ui.css.swt/src/.../css2/CSSPropertyBackgroundSWTHandler.java` | CSS property handler for `background-image`; applies the resolved image to a widget. |

### Supported CSS Property

```css
/* Apply a background image to an SWT widget */
Button.myButton {
    background-image: url("platform:/plugin/com.example/icons/icon.svg");
}
```

Supported widget types: `Control`, `Button`, `ToolItem`, `CTabItem`, `TableItem`, `Shell`.

### Image Resolution via IResourcesLocatorManager

`CSSSWTImageHelper.loadImageFromURL()` delegates path resolution to `IResourcesLocatorManager`, which can chain multiple `IResourceLocator` implementations. This allows the CSS engine to resolve `platform:/plugin/...` URIs to actual image data.

### Default Image Preservation

`CSSSWTImageHelper` saves the widget's original image before CSS applies a new one:

```java
// Before CSS application
CSSSWTImageHelper.storeDefaultImage(widget, currentImage);

// When CSS is removed / theme changes
CSSSWTImageHelper.restoreDefaultImage(widget);
```

### Theme Change Events

The `IThemeEngine` publishes `THEME_CHANGED` events via OSGi `EventAdmin` when the active theme changes. The CSS engine reapplies all stylesheets at that point, which triggers fresh image resolution via the registered property handlers.

```java
@Inject @Optional
void themeChanged(@UIEventTopic(IThemeEngine.Events.THEME_CHANGED) Map<?,?> event) {
    IThemeEngine engine = (IThemeEngine) event.get(IThemeEngine.Events.THEME_ENGINE);
    // engine will re-apply CSS, including background-image properties
}
```

## Gaps and Considerations for CSS-Based Image Exchange

The following observations are relevant for evaluating enhanced CSS-based image exchange:

### What works today

- The CSS engine can override a widget's background image via `background-image: url(...)`.
- `IResourcesLocatorManager` provides an extension point for custom URI resolution (e.g. theme-aware paths).
- `CSSSWTImageHelper` already handles save/restore of original images when CSS overrides them.

### Current limitations

1. **Icon keys not CSS-addressable.** Workbench images registered under `ISharedImages` keys (e.g. `IMG_TOOL_COPY`) are not reachable via CSS selectors. CSS can only affect widget background images, not images set via `Widget.setImage()` (e.g. tool-bar buttons, label icons).

2. **`background-image` vs `setImage()`.** SWT distinguishes between the *background image* of a `Control` and the *content image* set via `setImage()` on `Label`, `Button`, `ToolItem`, etc. CSS `background-image` maps to the former; workbench icons use the latter.
A CSS-based solution for exchanging workbench icons would require either a new CSS property (e.g. `swt-image`) or a convention for mapping CSS declarations to `ImageRegistry` keys.

3. **`ImageDescriptor` not CSS-aware.** The `ImageDescriptor` hierarchy has no knowledge of the CSS engine or active theme. However, `URLImageDescriptor` is the concrete class that performs the actual URL-to-image resolution, making it a natural interception point — see [Approach: theme overrides inside URLImageDescriptor](#approach-theme-overrides-inside-urlimagedescriptor) below.

4. **SVG rendering.** SVG support is handled at the `URLImageDescriptor` level via the Eclipse SVG renderer bundle. CSS `url()` references to SVG files work today only if the renderer is active.

5. **No CSS property for `ImageRegistry` injection.** There is currently no mechanism to declare in a CSS stylesheet that a given `ImageRegistry` key should resolve to a different resource in a specific theme.

### Approach: Equinox Transforms Hook (iconpack example)

The `examples/org.eclipse.ui.examples.iconpack` bundle in this repository demonstrates a
fully working, framework-level approach to icon replacement that requires **no changes to
JFace or the workbench**. It is based on the
[Equinox Transforms Hook](https://github.com/eclipse-equinox/equinox/pull/732) and is the
currently recommended direction for icon pack support.

**How it works:**

The Equinox transforms hook is an OSGi framework extension that intercepts bundle resource
lookups at the OSGi level, before any JFace or SWT code is involved. When a bundle tries
to load a resource (e.g. an icon), the hook consults a set of registered transformer rules
and can transparently substitute a different resource.

An icon pack is a plain OSGi bundle that:

1. Declares the Equinox transforms extender capability in its manifest:
   ```
   Require-Capability: osgi.extender;filter:="(osgi.extender=equinox.transforms.hook)"
   Equinox-Transformer: /transform.txt
   ```
   The `Equinox-Transformer` header (enabled by
   [eclipse-equinox/equinox#732](https://github.com/eclipse-equinox/equinox/pull/732))
   points to the bundle's transformer rules file. The extender scans this header and
   registers the rules as an OSGi service automatically — no manual service registration
   is required.

2. Provides a `transform.txt` rules file. Each line has the format:
   ```
   <bundle-pattern>,<resource-pattern>,<replacement-resource>
   ```
   The `replace` transformer (built-in since
   [eclipse-equinox/equinox#731](https://github.com/eclipse-equinox/equinox/pull/731))
   is the default when the fourth field is omitted.

   Example from the iconpack sample — replace `save_edit.png` in `org.eclipse.ui` with a
   custom icon, including the HiDPI `@2x` variant:
   ```
   org\.eclipse\.ui,icons/.*/save_edit.png,/myicons/saveme.png
   org\.eclipse\.ui,icons/.*/save_edit@2x.png,/myicons/saveme@2x.png
   ```
   - Bundle and resource patterns are regular expressions.
   - The replacement path points to a resource inside the icon pack bundle itself.
   - A PNG can replace a PNG, or any other format — the caller still sees the original
     file name, so format changes should be made with care.

3. Includes the replacement icons and the rules file in `build.properties`
   (`bin.includes`).

**Running the example:**

The Equinox transforms hook must be enabled at VM startup:

```
-Dosgi.framework.extensions=org.eclipse.equinox.transforms.hook
```

Equinox Transforms Hook version `1.4.200` or later is required.

**Advantages over the `URLImageDescriptor` interception approach (eclipse-platform/eclipse.platform.ui#14):**

| | Equinox Transforms Hook | URLImageDescriptor interception |
|---|---|---|
| JFace/UI changes required | None | Yes (`URLImageDescriptor`, `JFacePreferences`) |
| Scope | All bundle resources (not just images) | `platform:/plugin/...` image URLs only |
| Pattern matching | Full regex on bundle ID and resource path | Path prefix matching only |
| Format substitution | Possible (caller sees original name) | Not addressed |
| HiDPI support | Requires explicit rules per zoom variant | Built into `URLImageDescriptor` zoom logic |
| Preference-based on/off switch | Not needed (bundle presence controls it) | Requires `CUSTOM_RESOURCE_THEME` preference |

**Known limitations and future work (from the example README):**

1. HiDPI `@2x` and `@1.5x` variants currently require a separate rule per zoom level.
   Supporting capture groups in transform instructions would fix this.
2. Bundle patterns use plain regex; simple glob/ant-style patterns and no-escape exact
   matching would be more ergonomic.
3. No tooling exists yet to enumerate a bundle's icons and generate a starter rules file.
4. No support for variable/system-property placeholders in rules (e.g. to activate a
   specific named theme at runtime).

See the full example at
`examples/org.eclipse.ui.examples.iconpack` and the contributing Equinox PRs
[eclipse-equinox/equinox#731](https://github.com/eclipse-equinox/equinox/pull/731) and
[eclipse-equinox/equinox#732](https://github.com/eclipse-equinox/equinox/pull/732).

---

### Approach: theme overrides inside URLImageDescriptor

eclipse-platform/eclipse.platform.ui#14 proposes intercepting image loading directly inside
`URLImageDescriptor.getxURL()` — the method that resolves the final URL for a given zoom
level. Because every image loaded via a `platform:/plugin/...` URL passes through this
method, a single interception point can redirect *any* plugin icon to a themed replacement
without requiring changes to callers or to `ImageRegistry`.

**Mechanism (as proposed in the PR):**

1. A new `JFacePreferences` constant `CUSTOM_RESOURCE_THEME` (key
   `"org.eclipse.jface.CUSTOM_RESOURCE_THEME"`) acts as an on/off switch for theme
   override resolution.

2. After the normal HiDPI zoom variant lookup, `getxURL()` checks whether the URL refers
   to a `platform:/plugin/...` resource. If so, it constructs an alternate URL by
   inserting a `/theme/` directory segment after the bundle ID:

   ```
   Original:  platform:/plugin/org.eclipse.ui/icons/full/etool16/save_edit.svg
   Alternate: platform:/plugin/org.eclipse.ui/theme/icons/full/etool16/save_edit.svg
   ```

3. If a file exists at the alternate URL, it is returned instead of the original. Otherwise
   the original URL is used unchanged (non-breaking fallback).

4. Theme authors ship replacements as a **plugin fragment** targeting the original plugin as
   the host. The fragment places alternate icons under `/theme/<original-icon-path>` at its
   root. The OSGi fragment host mechanism merges the fragment's classpath with the host,
   making the `/theme/` files visible to `FileLocator` lookups.

   Example fragment layout to override `org.eclipse.ui`'s `save_edit.svg`:
   ```
   my.theme.fragment/
   └── theme/
       └── icons/
           └── full/
               └── etool16/
                   └── save_edit.svg
   ```

**Why `URLImageDescriptor` is the right place:**

- All images loaded through `ImageDescriptor.createFromURL()` or
  `ImageDescriptor.createFromFile()` ultimately end up as `URLImageDescriptor` instances.
  A hook here covers workbench icons, JFace icons, and third-party plugin icons uniformly,
  regardless of whether they were registered in an `ImageRegistry` or created ad-hoc.
- No API changes are needed on `ImageRegistry`, `ISharedImages`, or any widget code.
- The `$nl$` path normalization (stripping locale segments) is already handled in the PR,
  ensuring locale-aware icon paths are matched correctly.
- Because a **restart is acceptable** (dynamic theme switching at runtime is out of scope),
  cache invalidation of `ImageRegistry` is not required. The theme override is set once
  before any image is loaded and stays fixed for the lifetime of the process.

**Feasibility assessment:**

The approach is **feasible with targeted fixes** given the restart constraint. The main
issues to resolve are:

1. **Wrong interception point for zoom == 100.** The PR modifies `getxURL()`, but that
   method is only called from `getZoomedImageSource()`, which is only reached when
   zoom != 100. The two zoom == 100 fast paths in `getImageData()` and
   `createURLImageFileNameProvider()` bypass it entirely:

   ```java
   // getImageData() — zoom==100 never calls getZoomedImageSource()
   if (zoom == 100 || canLoadAtZoom(tempURL, zoom)) {
       return getImageData(tempURL, 100, zoom);
   }

   // createURLImageFileNameProvider() — zoom==100 never calls getZoomedImageSource()
   if (zoom == 100) {
       return getFilePath(tempURL, logIOException);
   }
   ```

   The fix is to extract the theme URL substitution into a dedicated method (e.g.
   `getThemedURL(URL)`) and call it at the top of both zoom == 100 paths as well,
   before any file resolution takes place.

2. **URL parsing must operate on the raw `platform:/plugin/...` form.** The substitution
   must happen before `resolvePathVariables()` converts the URL to a `file://` path.
   The raw URL string is stored in the `url` field and is available at that point.
   The `$nl$` strip already present in the PR handles locale-aware paths correctly.

3. **Preference store access.** Rather than reading an `IPreferenceStore` from inside
   JFace core, the cleanest solution is a package-private static field on
   `URLImageDescriptor` (or `InternalPolicy`) that is written once at application startup
   by the RCP layer. This keeps JFace independent of any preference store implementation.

**Comparison with the Equinox Transforms Hook approach:**

| | Equinox Transforms Hook | URLImageDescriptor interception |
|---|---|---|
| JFace/UI changes required | None | Small, contained changes to `URLImageDescriptor` |
| Scope | All bundle resources | `platform:/plugin/...` image URLs only |
| Pattern matching | Full regex on bundle ID and path | Path prefix convention (`/theme/`) |
| Format substitution | Possible | Not addressed |
| HiDPI support | Requires one rule per zoom variant | Handled automatically by existing zoom logic |
| Framework extension required | Yes (`equinox.transforms.hook`) | No |
| Works without OSGi / plain JFace | No | Yes |
| Dynamic theme switching | Possible | Out of scope; restart required |

### Refined variant: URLImageDescriptor delegates to a strategy interface

Rather than embedding the `/theme/` path convention and URL-string parsing directly in
`URLImageDescriptor`, the rewriting logic could be delegated to a pluggable strategy.
JFace would own only the call site and the interface; all policy lives outside JFace.

**Proposed interface** (in `org.eclipse.jface.resource`):

```java
/**
 * Strategy that can rewrite an image URL before URLImageDescriptor
 * opens it. Implementations are set once at application startup.
 *
 * @since 3.x
 */
public interface IImageURLModifier {
    /**
     * Returns a (possibly different) URL to load instead of
     * {@code originalURL}, or {@code originalURL} if no substitution
     * is desired.
     */
    URL modifyURL(URL originalURL);
}
```

**Single static registration point** on `URLImageDescriptor` (or `InternalPolicy`):

```java
// Package-private — set by the RCP/OSGi layer at startup, before any image is loaded
static IImageURLModifier urlModifier;
```

**Call site** — invoked at the top of all three resolution paths, before
`resolvePathVariables()` converts the URL to a `file://` path:

```java
private URL applyModifier(URL url) {
    IImageURLModifier m = urlModifier;
    return (m != null) ? m.modifyURL(url) : url;
}
```

The three places that need this call:

| Method | Zoom | Current code |
|--------|------|-------------|
| `getImageData()` | 100 | `getImageData(tempURL, 100, zoom)` |
| `createURLImageFileNameProvider()` | 100 | `getFilePath(tempURL, logIOException)` |
| `getZoomedImageSource()` | 150 / 200 | called before `getxURL()` |

**What moves out of JFace:**

The RCP layer (or a dedicated icon-pack bundle) provides the implementation. A simple
fragment-based icon pack would look like:

```java
public class FragmentImageURLModifier implements IImageURLModifier {
    private static final String PLUGIN_PREFIX = "/plugin/";
    private static final String THEME_DIR    = "/theme";

    @Override
    public URL modifyURL(URL url) {
        String file = url.getFile();
        if (!file.contains(PLUGIN_PREFIX)) {
            return url;
        }
        // Build candidate: insert /theme/ after the bundle ID segment
        String afterPlugin = file.substring(file.indexOf(PLUGIN_PREFIX) + PLUGIN_PREFIX.length());
        String bundleId    = afterPlugin.substring(0, afterPlugin.indexOf('/'));
        String iconPath    = afterPlugin.substring(afterPlugin.indexOf('/'));
        // Strip $nl$ locale segments
        iconPath = iconPath.replace("/$nl$/", "/");
        try {
            URL candidate = new URL(url.getProtocol(), url.getHost(), url.getPort(),
                    PLUGIN_PREFIX + bundleId + THEME_DIR + iconPath);
            if (/* resource exists at candidate */ exists(candidate)) {
                return candidate;
            }
        } catch (MalformedURLException e) { /* ignore, fall through */ }
        return url;
    }
}
```

Registration at startup (e.g. in an `IStartup` or E4 lifecycle handler):

```java
URLImageDescriptor.urlModifier = new FragmentImageURLModifier();
```

**Why this design is preferable to embedding logic in `URLImageDescriptor`:**

- JFace has no knowledge of path conventions, OSGi fragments, or preference stores.
- Different RCP applications can supply different implementations (fragment-based,
  CSS-driven, database-backed, etc.) without patching JFace.
- The interface is trivially testable in isolation.
- HiDPI variants are handled for free: the modifier rewrites the base URL; the existing
  `@2x` / `@1.5x` zoom logic in `URLImageDescriptor` then appends the zoom suffix to
  the already-rewritten URL, so no duplicate rules are needed.
- Because a restart is acceptable, the modifier is set once and never changed — no
  thread synchronisation or cache invalidation is required.

### Potential approach for image exchange via CSS

A CSS-level approach is complementary to the `URLImageDescriptor` interception described
above. It would allow theme stylesheets to declare which icon set to activate, while
`URLImageDescriptor` does the actual file resolution at startup:

```css
/* Hypothetical: activate a named icon theme */
ImageRegistry {
    theme: "dark-compact";
}
```

Because a restart is acceptable, the CSS engine would only need to write the theme name
to the static field in `URLImageDescriptor` during application initialisation, before
any images are loaded. No cache invalidation or `ImageRegistry` lifecycle hooks are needed.

## Related Documentation

- [CSS.md](CSS.md) — CSS styling engine overview
- [JFace.md](JFace.md) — JFace framework overview
- [Eclipse4_RCP_FAQ.md](Eclipse4_RCP_FAQ.md) — E4 RCP FAQ including theming
- [eclipse-platform/eclipse.platform.ui#14](https://github.com/eclipse-platform/eclipse.platform.ui/pull/14) — WIP PR: Allow plugin fragments to supply alternate icons via `URLImageDescriptor`
- [eclipse-equinox/equinox#731](https://github.com/eclipse-equinox/equinox/pull/731) — Add `replace` as a default available transformer
- [eclipse-equinox/equinox#732](https://github.com/eclipse-equinox/equinox/pull/732) — Add extender that allows bundles to declare a transformer resource via `Equinox-Transformer` manifest header
