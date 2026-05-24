# Meta Spatial Kotlin Component Builder

A Kotlin-first dynamic `ComponentBase` builder for the Meta Spatial SDK.

After Meta Spatial SDK `v0.13.0`, creating custom `ComponentBase` classes became heavily XML-driven.  
This project removes the XML workflow and replaces it with a pure Kotlin API for building dynamic Meta Spatial components directly in code.

Instead of defining component structures through XML, you can now create and manage `ComponentBase` entities using Kotlin with strongly-typed attributes and dynamic runtime generation.

---

# Why This Exists

After the changes introduced in Meta Spatial SDK `v0.13.0`, the workflow for creating `ComponentBase` systems became more rigid and XML-dependent.

I personally did not want to build component systems through XML.

This library was built to:

- Remove XML dependency
- Allow dynamic runtime component creation
- Keep everything Kotlin-first
- Make Meta Spatial component workflows feel more natural for Kotlin developers
- Reduce boilerplate when creating custom entity components

---

# Features

- Dynamic `ComponentBase` creation
- Kotlin property delegation support
- Runtime attribute registration
- Automatic attribute type detection
- Strongly typed Meta Spatial attributes
- Entity-compatible component system
- No XML component definitions required
- Works with Meta Spatial SDK `v0.13.0+`

---

# Supported Attribute Types

The builder currently supports:

- `Int`
- `Long`
- `Float`
- `Boolean`
- `String`
- `UUID`
- `Uri`
- `Vector2`
- `Vector3`
- `Vector4`
- `Pose`
- `Entity`

Mapped automatically into Meta Spatial SDK attributes:

- `IntAttribute`
- `FloatAttribute`
- `PoseAttribute`
- `EntityAttribute`
- etc.

---

# Installation

## Gradle

```kotlin
repositories {
    mavenCentral()
}
```

```kotlin
dependencies {
    implementation("com.yourname:meta-spatial-component-builder:VERSION")
}
```
```install
```
[![](https://jitpack.io/v/kishonadiaz/ComponentBaseBuilder.svg)](https://jitpack.io/#kishonadiaz/ComponentBaseBuilder)

```
To intall you  jitpack 
Add it in your settings.gradle.kts at the end of repositories:
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url = uri("https://jitpack.io") }
		}
}

then in your dependency
dependencies {
	        implementation("com.github.kishonadiaz:ComponentBaseBuilder:1.0.2")
	}

```
---

# Basic Example

Here is a simple custom component built entirely in Kotlin.

```kotlin
class AvatarStats : ComponentBaseBuilder {

    init {
        currentid = id
    }

    constructor(
        entity: Entity = Entity.nullEntity(),
        health: Int,
        mana: Float
    ) : super(1) {

        this.register<AvatarStats>(componentBase = Companion)

        this.addAttribute(
            "entity",
            entity,
            this,
            AttributeHelper.ENTITYATTRIBUTE
        )

        this.addAttribute(
            "health",
            health,
            this,
            AttributeHelper.INTATTRIBUTE
        )

        this.addAttribute(
            "mana",
            mana,
            this,
            AttributeHelper.FLOATATTRIBUTE
        )

        currentid = id
    }

    //Three ways to call the data for later usage 
    var thisEntity: Entity?
        get() = this["entity"] as Entity?
        set(value) {
            this["entity"] = value!!
        }
    var health : Float by ComponentField("heath",0.0f)

    override fun typeID(): Int {
        return id
    }

    override fun companion(): ComponentCompanionHelper = Companion

    companion object : ComponentCompanionHelper {

        override val id: Int = 998877

        override val attributeNamesList = mutableListOf<String>()

        override val attributeTypeCountsList = mutableListOf<Int>()

        override val attributeTypesList = mutableListOf<Int>()

        override val attributeKeysList = mutableListOf<Int>()

        override var createDefaultInstance: () -> ComponentBase = {
            AvatarStats(Entity.nullEntity(), 100, 50f)
        }
    }
}
```

---

# Usage With Entities

Once built, the component behaves like a normal Meta Spatial component.

```kotlin

val entity = Entity.create(
    Mesh("mesh://box".toUri()),
    Transfrom(Pose()),
    AvatarStats(entity = playerEntity, health = 100, mana = 75f)
)

or
val stats = AvatarStats(
    entity = playerEntity,
    health = 100,
    mana = 75f
)

playerEntity.setComponent(stats)

or
playerEntity.setComponent(AvatarStats(entity = playerEntity, health = 100, mana = 75f))

```

Access values normally:

```kotlin
var avatarData = entity.tryGetComponenet<AvatarStats>()
var mana = avatarData?.get<Float>("mana")!!
//If created in custom Component you can
var health:Int = avatarData?.health 
var player = avatarData["playerEntity"]!!

or 
// If you called it on its own
val health: Int? = stats["health"]
val mana: Float? = stats["mana"]
```

Update values dynamically:

```kotlin
stats["health"] = 150
```

---

# Why Kotlin Instead of XML?

This project focuses on developer ergonomics.

XML component definitions can become:

- repetitive
- difficult to maintain
- harder to generate dynamically
- disconnected from gameplay code

Using Kotlin allows:

- dynamic generation
- runtime flexibility
- cleaner APIs
- less context switching
- easier debugging
- IDE autocomplete and type safety

---

# Intended Use Cases

This library works especially well for:

- runtime-generated entities
- multiplayer systems
- RPG stats systems
- inventory systems
- AI state systems
- dynamically generated world objects
- procedural systems
- gameplay-heavy Meta Spatial applications

---

# Current Status

Early development / experimental.

The API may evolve as Meta Spatial SDK changes over time.

Contributions, improvements, and testing are welcome.

---

[//]: # (# Roadmap)

[//]: # (- [ ] Better attribute DSL)

[//]: # (- [ ] Safer generic typing)

[//]: # (- [ ] Annotation-based component generation)

[//]: # (- [ ] Serialization helpers)

[//]: # (- [ ] Editor tooling)

[//]: # (- [ ] Automatic schema validation)

[//]: # (- [ ] Better debugging utilities)

[//]: # (- [ ] Compose-style builder APIs)

---

# Contributing

Pull requests are welcome.

If you find bugs, API issues, or Meta Spatial compatibility problems, open an issue.

---

# License

Apache 2.0 License

---

# Disclaimer

This project is an independent open-source utility and is not affiliated with or endorsed by Meta.