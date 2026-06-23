# smartcrop.kt

![example workflow](https://github.com/jwagner/smartcrop.js/actions/workflows/tests.yml/badge.svg)

Kotlin port of [smartcrop.js](https://github.com/jwagner/smartcrop.js) — content-aware image cropping.

Finds the most visually important region of an image using edge detection, skin-tone analysis, saturation, and rule-of-thirds composition scoring.

![Example](https://29a.ch/sandbox/2014/smartcrop/example.jpg)
Image: [https://www.flickr.com/photos/endogamia/5682480447/](https://www.flickr.com/photos/endogamia/5682480447) by Leon F. Cabeiro (N. Feans), licensed under CC-BY-2.0

## Usage

```kotlin
import smartcrop.*

val input = ImgData(width, height, pixelData)
val result = SmartCrop.crop(input, Options(width = 200, height = 200))

val crop = result.topCrop
// crop.x, crop.y, crop.width, crop.height
```

**Output:**
```kotlin
Crop(x=300, y=200, width=200, height=200, score=Score(...))
```

The library operates on raw RGBA pixel data (`IntArray` with values 0–255). No platform dependencies — works on JVM, Android, Native, or JS via Kotlin Multiplatform.

## Installation

### Gradle

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.jwagner:smartcrop:2.0.5")
}
```

### Maven

```xml
<dependency>
    <groupId>com.github.jwagner</groupId>
    <artifactId>smartcrop</artifactId>
    <version>2.0.5</version>
</dependency>
```

## Algorithm Overview

1. Find edges using a Laplacian filter
1. Find regions with skin-like color
1. Find regions high in saturation
1. Boost regions as specified by options (e.g. detected faces)
1. Generate a set of candidate crops using a sliding window
1. Rank them using an importance function that centres detail and penalises edges
1. Return the highest-ranked crop

## Face Detection

Smartcrop's base algorithm is simple, fast, and generic. For better results on portraits, combine it with a face detector to create boost regions.

Example with Android's `CameraX` or ML Kit:

```kotlin
val boost = detectedFaces.map { face ->
    CropBoost(
        x = face.boundingBox.left,
        y = face.boundingBox.top,
        width = face.boundingBox.width(),
        height = face.boundingBox.height(),
        weight = 1.0
    )
}
val result = SmartCrop.crop(input, Options(width = 200, height = 200, boost = boost))
```

## API

### `SmartCrop.crop(input, options)`

Finds the best crop for `input` using `options`.

**`input: ImgData`** — RGBA pixel data. Width, height, and an `IntArray` of packed RGBA byte values (0–255 per channel, row-major order).

**`options: Options`** — see [Options](#options).

**returns:** [`CropResult`](#cropresult).

### Options

| Field | Type | Default | Description |
|---|---|---|---|
| `width` | `Int` | `0` | Desired crop width |
| `height` | `Int` | `0` | Desired crop height |
| `aspect` | `Double` | `0.0` | Set width = aspect, height = 1 (alternative to width/height) |
| `minScale` | `Double` | `1.0` | Smallest allowed crop scale; `1.0` prevents upscaling |
| `maxScale` | `Double` | `1.0` | Largest allowed crop scale |
| `scaleStep` | `Double` | `0.1` | Step between scale iterations |
| `step` | `Int` | `8` | Pixel step between crop candidates |
| `boost` | `List<CropBoost>?` | `null` | Regions to boost (e.g. faces). See [CropBoost](#cropboost) |
| `ruleOfThirds` | `Boolean` | `true` | Enable rule-of-thirds composition weighting |
| `detailWeight` | `Double` | `0.2` | Importance of edge detail in scoring |
| `skinWeight` | `Double` | `1.8` | Importance of skin tones in scoring |
| `saturationWeight` | `Double` | `0.1` | Importance of saturated regions in scoring |
| `edgeWeight` | `Double` | `-20.0` | Penalty for cropping near edges |
| `outsideImportance` | `Double` | `-0.5` | Importance weight for areas outside the crop |
| `boostWeight` | `Double` | `100.0` | Multiplier for boosted regions |
| `debug` | `Boolean` | `false` | Include full candidate list and intermediate buffers in result |

For the full list of internal tuning parameters, see `Options` in [SmartCrop.kt](src/main/kotlin/smartcrop/SmartCrop.kt).

### CropResult

```kotlin
CropResult(
    topCrop = Crop(x, y, width, height, score),
    crops = [...]?,       // all candidates (only when debug = true)
    debugOutput = ...?,    // analysis buffer (debug only)
    debugOptions = ...?,   // options snapshot (debug only)
    debugTopCrop = ...?    // unscaled copy of topCrop (debug only)
)
```

### CropBoost

```kotlin
CropBoost(
    x = 11,
    y = 20,
    width = 32,
    height = 32,
    weight = 1.0  // [0, 1]
)
```

Impact is proportional to weight × area.

## Performance

The analysis handles most images in <20 ms on a modern JVM/Android device. Large images are automatically prescaled before analysis.

## Tests

```bash
./gradlew test
```

## License

Copyright (c) 2018 Jonas Wagner — MIT License (enclosed)

Kotlin port by the smartcrop community.
