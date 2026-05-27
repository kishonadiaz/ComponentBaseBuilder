package com.digiforce.componentbasebuilder


import android.net.Uri
import android.util.Log
import com.digiforce.componentbasebuilder.ComponentBaseBuilder.Companion.attributeTypesList
import com.digiforce.componentbasebuilder.ComponentBaseBuilder.Companion.currentid
import com.digiforce.componentbasebuilder.ComponentHelper.Companion.classCache
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.meta.spatial.core.AbstractAttribute
import com.meta.spatial.core.BooleanAttribute
import com.meta.spatial.core.ComponentBase
import com.meta.spatial.core.ComponentCompanion
import com.meta.spatial.core.Entity
import com.meta.spatial.core.EntityAttribute
import com.meta.spatial.core.FloatAttribute
import com.meta.spatial.core.IntAttribute
import com.meta.spatial.core.LongAttribute
import com.meta.spatial.core.Pose
import com.meta.spatial.core.PoseAttribute
import com.meta.spatial.core.RegisteredAttributeType
import com.meta.spatial.core.StringAttribute
import com.meta.spatial.core.URIAttribute
import com.meta.spatial.core.UUIDAttribute
import com.meta.spatial.core.Vector2
import com.meta.spatial.core.Vector2Attribute
import com.meta.spatial.core.Vector3
import com.meta.spatial.core.Vector3Attribute
import com.meta.spatial.core.Vector4
import com.meta.spatial.core.Vector4Attribute
import com.meta.spatial.toolkit.AppSystemActivity
import com.meta.spatial.toolkit.SpatialActivityManager
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * @constructor AttributeCreator used to create the AbstractAttribute
 * */
typealias AttributeCreator =
            (Int, ComponentBase, Int, Any) -> AbstractAttribute

/**
 *@property AttributeHelper this enum is used to identify and set the AbstractAttribute
 * */
enum class AttributeHelper (
    val index: Int,
    val creator: Any,
)
{

    DEFAULT(
        0,
        ::IntAttribute),
    BOOLEANATTRIBUTE(
        1,
        ::BooleanAttribute),
    INTATTRIBUTE(1,::IntAttribute),
    LONGATTRIBUTE(2, ::LongAttribute),
    FLOATATTRIBUTE(3, ::FloatAttribute),
    STRINGATTRIBUTE(4, ::StringAttribute),
    UUIDATTRIBUTE(5, ::UUIDAttribute),
    VECTOR2ATTRIBUTE(6, ::Vector2Attribute),
    VECTOR3ATTRIBUTE(7, ::Vector3Attribute),
    VECTOR4ATTRIBUTE(8,::Vector4Attribute),
    POSEATTRIBUTE(9, ::PoseAttribute),
    ENTITYATTRIBUTE(12, ::EntityAttribute),
    TIMEATTRIBUTE(13, ::LongAttribute),
    URIATTRIBUTE(14, ::URIAttribute),
    OBJECTATTRIBUTE(15,::StringAttribute)



}

/**
 * @constructor AttributeData this class is uses to create the Attribute
 * */
data class AttributeData<T>(
    var name: String,
    var key: Int,
    var component: ComponentBase,
    var attributeKey: Int,
    var value: Any?,
    var creator: (Int, ComponentBase, Int, T) -> AbstractAttribute,
    var atthelper: AttributeHelper

)


/**
 * @constructor ComponentBaseBuilder is a placeholder for the class
 * @receiver ComponentHelper
 * @property register is a function needed in the first instance of the child it is used to register the ComponentBase with Meta Spatial SDK system
 * @property initsets is a function that is needed to initial the system and need to be called in the second instance of a child
 * @property init is uses in this context to set the initLayout and the buffers for th system
 * @property checker is a debug tool function  tht you can pass your own identifier to, or it is default test
 * @property companion is an override function needed to set the ComponentCompanionHelper to this class
 * @sample ExampleComponentBase
* */
open class ComponentBaseBuilder: ComponentHelper {

    override fun typeID(): Int = id
    override var initAttribute: Int = 5

    init {
        currentid = id
        // ZERO-TO-HERO: always +4 terminators
        initLayout(this.initAttribute)
        attributeNamesList.clear()
        attributeTypeCountsList.clear()
        attributeTypesList.clear()
        attributeKeysList.clear()
        repeat(5) {
            attributeTypeCountsList.add(0)
            attributeTypesList.add(0)
//            attributeKeysList.add(counter)
//            counter++
        }
    }

    constructor():super(){
        initLayout(this.initAttribute)
    }
    constructor(initAttribute:Int=0,callback:(thiss: ComponentBaseBuilder)-> Unit = {}):super(){
        buildDynamic()
        callback(this)
        currentid = id
    }
    open fun initsets(component: ComponentBaseBuilder, initAttribute: Int= 0){
//        component.initAttribute = initAttribute + 5 // ZERO-TO-HERO: always +4 terminators
//        initLayout(component.initAttribute)



        component.companion().attributeNamesList.clear()
        component.companion().attributeTypeCountsList.clear()
        component.companion().attributeTypesList.clear()
        component.companion().attributeKeysList.clear()

        repeat(5) {
            component.companion().attributeTypeCountsList.add(0)
            component.companion().attributeTypesList.add(0)

        }
        companion().componentBaseBuilder = component
    }

    open fun checker(identifier:String="test") {
        Log.d("test"," ${attributeTypes().toMutableList()} :" +
                    " ${attributeNames().toMutableList()} : " +
                    "${attributeKeys().toMutableList()}:" +
                    "${attributeTypeCounts().toMutableList()}")
    }

    public inline fun <reified T : ComponentBase> register(componentBase: ComponentCompanion){
        val activity = SpatialActivityManager.getVrActivity<AppSystemActivity>()
        activity.componentManager.registerComponent<T>(componentBase)
    }

    override fun companion(): ComponentCompanionHelper = Companion

    fun buildDynamic(): ComponentCompanionHelper {

        val target = componentBaseBuilder?.companion()

        val targetCache =
            classCache.getOrPut(target!!::class.java) {

                target::class.memberProperties
                    .mapNotNull {
                        it as? KMutableProperty1<ComponentCompanionHelper, Any?>
                    }
                    .associateBy { it.name }
            }

        for (prop in componentBaseBuilder?.companion()!!::class.memberProperties) {

            val value = runCatching {
                prop.getter.call(componentBaseBuilder?.companion())
            }.getOrNull()

            val targetProp = targetCache[prop.name]

            runCatching {
                targetProp?.set(target, value)
            }
        }

        return target.componentBaseBuilder?.companion()!!
    }
    var propertyCache: Map<String, KMutableProperty1<ComponentCompanionHelper, Any?>>
        get() = ComponentHelper.Companion.classCache.getOrPut(this::class.java) {
            ComponentBaseBuilder.Companion.componentBaseBuilder!!::class.memberProperties
                .mapNotNull { it as? KMutableProperty1<ComponentCompanionHelper, Any?> }
                .associateBy { it.name }
        }
        set(value) {
            ComponentHelper.Companion.classCache[this::class.java] = value
        }

    fun getV(attr: String): Any? {
        return propertyCache[attr]?.get(ComponentBaseBuilder.Companion.componentBaseBuilder?.companion()!!)
    }

    fun setv(attr: String, value: Any?) {
        propertyCache[attr]?.set(ComponentBaseBuilder.Companion.componentBaseBuilder?.companion()!!, value)
    }

    companion object : ComponentCompanionHelper {

        var currentid = 0;
        var pool: MutableList<MutableMap<Int,ComponentBase>> = mutableListOf()

        override val id: Int
            get() = 1228886;



        fun <T> child(componentBase: ComponentBase): ComponentBaseBuilder {

            return componentBase as ComponentBaseBuilder
        }


        override var attributeNamesList: MutableList<String> = mutableListOf()
        override var attributeTypeCountsList: MutableList<Int> = mutableListOf()
        override var attributeTypesList: MutableList<Int> = mutableListOf()
        override var attributeKeysList: MutableList<Int> = mutableListOf()


        // ZERO-TO-HERO DYNAMIC MAPPINGS
        override fun keyToIndex(key: Int): Int {
            // Keys are sequential: 0,1,2,3... for active fields
            // Terminators get keys too but return -1
            val activeCount = attributeNamesList.size
//            log("adfadfafd  ${currentid}")
            return if (key < activeCount) key else -1
        }

        override fun keyStringToKeyIntMap(keyString: String): Int? {
            val idx = attributeNamesList.indexOf(keyString)

            return if (idx >= 0) idx else null
        }

        override var createDefaultInstance: () -> ComponentBase ={ componentBaseBuilder!! }
        override var componentBaseBuilder: ComponentBaseBuilder? = ComponentBaseBuilder()
        override var propertyCache: Map<String, KMutableProperty1<ComponentCompanionHelper, Any?>>
            get() = ComponentHelper.Companion.classCache.getOrPut(this::class.java) {
                this::class.memberProperties
                    .mapNotNull { it as? KMutableProperty1<ComponentCompanionHelper, Any?> }
                    .associateBy { it.name }
            }
            set(value) {
                ComponentHelper.Companion.classCache[this::class.java] = value
            }

        override fun get(attr: String): Any? {
            return propertyCache[attr]?.get(ComponentBaseBuilder.Companion.componentBaseBuilder?.companion()!!)
        }

        override fun set(attr: String, value: Any?) {
            propertyCache[attr]?.set(ComponentBaseBuilder.Companion.componentBaseBuilder?.companion()!!, value)
        }


    }
}

/**
 * @constructor ComponentHelper is a abstract class used to create the ComponentBasedBuilder
 * @receiver ComponentBase is Meta Spatial SDK and is used to pass to the other children calling this abstract class
* */
public abstract class ComponentHelper : ComponentBase {
    open override fun typeID(): Int = 233
    private var nameList: MutableMap<Int, AttributeData<Any>> = mutableMapOf()
    var currentBuild: MutableList<AbstractAttribute?> = mutableListOf()
    abstract var initAttribute: Int
    var attributeMapCounter = 0;
    val listOfAttribute: MutableList<Int> = mutableListOf()
    open var attributeKeyCounter: Int=0
    val attributesMap =
        mutableMapOf<Int, (Int, ComponentBase, Int, Any) -> AbstractAttribute>()
    constructor():super()


    fun to(
        value: Any,
        id: Int,
        component: ComponentBase,
        flags: Int,
        name: String = ""
    ): AbstractAttribute
    {

        return when (value) {

            is Float ->
                FloatAttribute(id, component, flags, value)

            is Int ->
                IntAttribute(id, component, flags, value)

            is Boolean ->
                BooleanAttribute(id, component, flags, value)

            is String ->
                StringAttribute(id, component, flags, value)

            is Vector2 ->
                Vector2Attribute(id, component, flags, value)

            is Vector3 ->
                Vector3Attribute(id, component, flags, value)

            is Vector4 ->
                Vector4Attribute(id, component, flags, value)

            is Entity ->
                EntityAttribute(id, component, flags, value)

            is Pose ->
                PoseAttribute(id, component, flags, value)

            is UUID ->
                UUIDAttribute(id, component, flags, value)

            is Uri ->
                URIAttribute(id, component, flags, value)

            is Long -> {

                val lower = name.lowercase()

                val looksLikeTimeName =
                    lower.contains("time") ||
                            lower.contains("timestamp") ||
                            lower.contains("date")

                val looksLikeTimestamp =
                    value > 1_000_000_000_000L

                if (looksLikeTimeName || looksLikeTimestamp)
                    LongAttribute(id, component, flags, value)
                else
                    LongAttribute(id, component, flags, value)
            }

            else -> error("Unsupported type: ${value::class}")
        }
    }

    fun  fromValue(from: Any?,toString: Class<*>): Any?{


        return when (from) {

            is Float -> from as Float

            is Int -> from as Int

            is Boolean -> {
                if (from == true && toString is String) {
                    "true"
                }
                else if (from == false && toString is String){
                    "false"
                }
                else {
                    if (from == "true" && toString is Boolean) {
                        true
                    } else if (from == "false" && toString is Boolean) {
                        false
                    } else {
                        false
                    }
                }
            }

            is String -> {
                return (if(from == "true"){true} else if(from == "false"){false} else{
                    from as String
                }) as Any
            }

            is Vector2 -> from as Vector2

            is Vector3 -> from as Vector3

            is Vector4 -> from as Vector4

            is Entity -> from as Entity

            is Pose -> from as Pose

            is UUID -> from as UUID

            is Uri -> from as Uri

            is Long -> from as Long

            else -> from
        } as Any
    }

    /**
     * This function is used to create the attribute of the Component Base and has to be called in an init{} or a constructor
     * @param name is a String that you used to name the attribute and set ro rhe component
     * @param value is used to pass in the data you want to set be it a float or string etc. , you have to match the attribute you call
     * @param component is the componentBaseBuilder you call it in usually the word this
     * @param atthelper is a call to the AttributeHelper.STRINGATTRIBUTE, etc.
     * @sample showExample
     * */
    open fun <T> addAttribute(name: String, value: T?, component: ComponentBaseBuilder, atthelper: AttributeHelper,terminator: Boolean =false){

        var data = value
        var currentHelper = atthelper

        if (currentHelper== AttributeHelper.BOOLEANATTRIBUTE){
            currentHelper= AttributeHelper.STRINGATTRIBUTE
            if(data is Boolean){
                data = ((if(data == true){"true"}else{"false"}) as T?)
            }
            if(data is Int){
                data = ((if(data == 1){true}else{false} as T?
                        ))
            }
            if(data is java.lang.Integer){
                data = ((if(data == 1){true}else{false} as T?
                        ))
            }
        }else
        if(currentHelper == AttributeHelper.OBJECTATTRIBUTE){
            data = component.pack(name, value) as T? // pack returns String
            gsons[name] = data as String
            currentHelper = AttributeHelper.STRINGATTRIBUTE // Store as String
        }else if(currentHelper == AttributeHelper.INTATTRIBUTE){
            if(data is Boolean){
                data = ((if(data == true){1}else{0} as T?))
            }
        }

        @Suppress("UNCHECKED_CAST")
        component.buildAttribute<T>(
            name, data as? T?,
            component,currentHelper.creator as AttributeCreator,atthelper ,terminator)


        component.listOfAttribute.add(currentid)



    }

    open fun  toBoolean(data:Any) : Boolean ?{
//         log("asdfhkjahdffff ${Thread.currentThread().stackTrace[2].methodName}")


        return (when(data){
            is Int->{
                if(data == 1){
                    true
                }else{
                    false
                }
            }
            is String ->{
                if(data == "true"){
                    true
                }else if(data == "false"){
                    false
                }else{
                    false
                }
            }
            else ->{
                if(data !is Boolean){
                    Log.i("ComponentBaseBuilder","Cannot be converted")
                    return false
                }
                data
            }
        } as Boolean?)

    }



    fun pack(key: String, value: Any?) : String {
        val json = getGson().toJson(value)
        if (value == null) { this[key] = ""; return "" }
        this[key] = json// Universal adapter handles it
        return  json
    }

    inline fun <reified T> unpack(key: String): T? {
        val json = this[key] as? String
        if (json.isNullOrEmpty()) return null
        return runCatching {
            getGson().fromJson(json, T::class.java) // Uses T, not Any
        }.getOrNull()
    }

    open operator fun get(key:String): Any{
        return get<Any?>(key)!!
    }
    inline operator fun <reified T> get(vararg datas:Any?): T? {
        var key = datas[0] as? String ?: return null

        val comp = companion() as ComponentCompanionHelper
        val index = comp.keyStringToKeyIntMap(key) ?: return null
        var data  = getComponentDataValue(index) as? T

        if(data is String){
            if(data == "true"){
                data = true as T?
            }else if(data ==  "false"){
                data = false as T?
            }
        }
        if(objects.keys.contains(key)) return objects[key] as T?
        return data
    }
    fun checkObject(any:Any): Boolean{
        return when(any){
            is String -> false
            is Float -> false
            is Double -> false
            is Long -> false
            is UUID -> false
            is Uri -> false
            is Pose -> false
            is Entity ->false
            is Vector4 -> false
            is Vector3 -> false
            is Vector2 -> false
            is Boolean -> false
            else -> true
        }
    }
    inline operator fun <T> set(key: String, value: T?) {
        val comp = companion() as ComponentCompanionHelper
        val index = comp.keyStringToKeyIntMap(key) ?: return
        var data = value
        if(data is Boolean){

            if(data == true){
                data = "true" as T?
            }else{
                data = "false" as T?
            }
        }

//        val isSingleton = value!!::class.objectInstance != null

        if(checkObject(value!!)){
//            log("KOIUYHH ${value} ${checkObject(value)}")
            objects[key] = value
            pack(key,value)
        }else
            setComponentDataValue(index, data!!)
    }
    open fun <T> buildAttribute(name:String, value:Any?, component: ComponentBaseBuilder, creator: (Int, ComponentBase, Int, Any) -> AbstractAttribute, atthelper:AttributeHelper, terminator: Boolean=false){

        component.addAtt<T>(currentid!!, component, creator)
        component.companion().attributeNamesList.add(name)
        component.currentBuild.add(component.attributesMap[currentid]!!(component.attributeKeyCounter,component,component.attributeKeyCounter,value!!))
        component.setComponentDataValue(name,value)

        val insertPos =component.companion().attributeNamesList.size-1



        component.companion().attributeTypesList.add(insertPos, 0)

        // Count = 1 for active, but we need to slide the 0's
        // Remove one terminator 0, add 1, then add 0 back at end
        if (component.companion().attributeTypeCountsList.size >= 5) {
            component.companion().attributeTypeCountsList.removeAt(component.companion().attributeTypeCountsList.size - 1) // Remove last 0
        }
        component.companion().attributeTypeCountsList.add(insertPos, 1) // Add 1 for new field
        component.companion().attributeTypeCountsList.add(0) // Add 0 back at end to maintain 4 terms

        // Same for types - maintain 4 trailing 0s
        if (component.companion().attributeTypesList.size >= 5) {
            component.companion().attributeTypesList.removeAt(attributeTypesList.size - 1)
        }
        component.companion().attributeTypesList.add(0) // Add trailing 0 back



        component.companion().attributeKeysList.add(attributeKeyCounter)


        component.attributeMapCounter++;
        component.attributeKeyCounter++


    }
    protected open fun <T> addAtt(index:Int, component: ComponentBaseBuilder, creator: (Int, ComponentBase, Int, Any) -> AbstractAttribute){

        @Suppress("UNCHECKED_CAST")
        component.attributesMap[index] = creator as (Int, ComponentBase, Int, Any?) -> AbstractAttribute

    }
    fun attributeHelperFromValue(value: Any,name:String="",terminator: Boolean=false): AttributeHelper {
        return when (value) {
            is Float -> AttributeHelper.FLOATATTRIBUTE
            is Int -> {
                if(terminator){
                    AttributeHelper.DEFAULT
                }else
                    AttributeHelper.INTATTRIBUTE
            }
            is Boolean -> AttributeHelper.BOOLEANATTRIBUTE
            is String -> AttributeHelper.STRINGATTRIBUTE
            is Vector2 -> AttributeHelper.VECTOR2ATTRIBUTE
            is Vector3 -> AttributeHelper.VECTOR3ATTRIBUTE
            is Vector4 -> AttributeHelper.VECTOR4ATTRIBUTE
            is Entity -> AttributeHelper.ENTITYATTRIBUTE
            is Pose -> AttributeHelper.POSEATTRIBUTE
            is UUID -> AttributeHelper.UUIDATTRIBUTE
            is Uri -> AttributeHelper.URIATTRIBUTE
            is Long-> {
                val lower = name.lowercase()

                val looksLikeTimeName =
                    lower.contains("time") ||
                            lower.contains("timestamp") ||
                            lower.contains("date")

                val looksLikeTimestamp =
                    value > 1_000_000_000_000L

                if (looksLikeTimeName || looksLikeTimestamp)
                    AttributeHelper.TIMEATTRIBUTE
                else
                    AttributeHelper.LONGATTRIBUTE

            }
            else -> AttributeHelper.OBJECTATTRIBUTE
        }
    }

    companion object{
        var classCache = mutableMapOf<Class<*>, Map<String, KMutableProperty1<ComponentCompanionHelper, Any?>>>()
        var gsons: MutableMap<String, String> = mutableMapOf()
        var objects: MutableMap<String, Any?> = mutableMapOf()
        private val defaultGson: Gson = AlteraGson.default

//        private val defaultGson: Gson = AlteraGson.Builder()
//            .autoDetectPackage(false) // <- KEY FIX
//            .skipSdkPackage(false)    // <- KEY FIX
//            .skipMetaPackage(true)    // <- Keep this
//            .useAnnotations(true)
//            .build()
//        private val defaultGson: Gson = AlteraGson.Builder()
//            .autoDetectPackage(false) // <- KEY FIX
//            .skipSdkPackage(false)    // <- KEY FIX
//            .skipMetaPackage(true)    // <- Keep this
//            .useAnnotations(true)
//            .build()
        private var customGson: Gson? = null
        fun setGson(gson: Gson) { customGson = gson }
        fun getGson(): Gson = customGson ?: defaultGson
//        val gson: Gson = GsonBuilder()
//            .setExclusionStrategies(object : com.google.gson.ExclusionStrategy {
//                override fun shouldSkipField(f: FieldAttributes): Boolean {
//                    return f.declaringClass.name.startsWith("com.meta.spatial")
//                }
//                override fun shouldSkipClass(clazz: Class<*>): Boolean {
//                    return clazz.name.startsWith("com.meta.spatial")
//                }
//            })
//            .registerTypeAdapterFactory(UniversalTypeAdapterFactory()) // USE THIS
//            .create()
    }

    abstract override fun companion(): ComponentCompanionHelper

    fun showExample(component: ComponentBaseBuilder){
        /**
         * @sample addAttribute("sample","sample data string,float,entity, etc. ",component, AttributeHelper.STRINGATTRIBUTE)
         */

    }

}

public  interface ComponentCompanionHelper : ComponentCompanion{


    abstract val attributeNamesList: MutableList<String>
    abstract val attributeTypeCountsList: MutableList<Int>

    abstract val attributeTypesList: MutableList<Int>

    abstract val attributeKeysList: MutableList<Int>



    var nameList: Array<String>
        get() = attributeNamesList.toTypedArray()
        set(value){}
    var typeArray: IntArray
        get() = attributeTypesList.toIntArray()
        set(value){}
    var keysArray: IntArray
        get() = attributeKeysList.toIntArray()
        set(value) {}
    var TypeCountsArray: IntArray
        get() = attributeTypeCountsList.toIntArray()
        set(value){}
    override fun attributeNames() = nameList
    override fun attributeTypeCounts() = TypeCountsArray
    override fun attributeTypes()      = typeArray
    override fun attributeKeys()        = keysArray
    override fun enumClassesMap()      = mutableMapOf<Int, Class<out Enum<*>>>()
    override fun attributeMetaData()   = mutableMapOf<Int, Pair<RegisteredAttributeType, String>>()

    // DYNAMIC KEY MAPPING
    override fun keyToIndex(key: Int): Int {
        val activeCount = attributeNamesList.size
        return if (key < activeCount) key else -1
    }

    override fun keyStringToKeyIntMap(keyString: String): Int? {
        val idx = attributeNamesList.indexOf(keyString)

        return if (idx >= 0) idx else -1
    }

    abstract override val createDefaultInstance: () -> ComponentBase

    var componentBaseBuilder: ComponentBaseBuilder?
    var propertyCache: Map<String, KMutableProperty1<ComponentCompanionHelper, Any?>>
        get() = classCache.getOrPut(ComponentCompanionHelper::class.java) {
            componentBaseBuilder?.companion()!!::class.memberProperties
                .mapNotNull { it as? KMutableProperty1<ComponentCompanionHelper, Any?> }
                .associateBy { it.name }
        }
        set(value) {
            classCache[componentBaseBuilder!!::class.java] = value
        }

    fun get(attr: String): Any? {
        return componentBaseBuilder?.propertyCache[attr]?.get(componentBaseBuilder?.companion()!!)
    }

    fun set(attr: String, value: Any?) {
        componentBaseBuilder?.propertyCache[attr]?.set(componentBaseBuilder?.companion()!!, value)
    }


}

open class ComponentField<T>(private val key: String, private val default: T) {
    operator fun getValue(thisRef: ComponentBaseBuilder, property: KProperty<*>): T {

        return (thisRef[key] as? T?: default) as T
    }
    operator fun setValue(thisRef: ComponentBaseBuilder, property: KProperty<*>, value: Any) {
        thisRef[key] = value
    }
}
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.LOCAL_VARIABLE ,AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.EXPRESSION)
annotation class AlteraIgnore

@AlteraIgnore
class UniversalReflectionAdapter(private val gson: Gson, private val type: Class<*>) : TypeAdapter<Any>() {

    override fun write(out: JsonWriter, value: Any?) {
        if (value == null) { out.nullValue(); return }

        out.beginObject()
        out.name("@type").value(value::class.java.name)

        for (prop in value::class.memberProperties) {
            try {
                if (prop.name == "Companion" || prop.name == "propertyCache") continue
                prop.isAccessible = true
                val propValue = prop.getter.call(value)?: continue
                out.name(prop.name)
                gson.toJson(propValue, propValue.javaClass, out)
            } catch (e: Exception) { continue }
        }
        out.endObject()
    }

    override fun read(reader: JsonReader): Any? {
        val jsonObj = gson.fromJson<JsonObject>(reader, JsonObject::class.java)
        val className = jsonObj.get("@type")?.asString ?: type.name // Fallback to known type

        return runCatching {
            val kClass = Class.forName(className).kotlin
            val instance = kClass.createInstance()

            jsonObj.entrySet().forEach { (propName, jsonElement) ->
                if (propName == "@type") return@forEach
                val prop = kClass.memberProperties.find { it.name == propName }
                        as? KMutableProperty1<Any, Any?>
                prop?.isAccessible = true
                val propType = prop?.returnType?.classifier as? KClass<*>
                val propValue = gson.fromJson(jsonElement, propType?.java)
                prop?.set(instance, propValue)
            }
            instance
        }.getOrNull()
    }
}

@AlteraIgnore
class UniversalTypeAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        var rawType = type.rawType



        if (rawType.isPrimitive ||
            rawType == String::class.java ||
            rawType == java.lang.Boolean::class.java ||
            rawType == java.lang.Integer::class.java ||
            rawType == java.lang.Long::class.java ||
            rawType == java.lang.Float::class.java ||
            rawType == java.lang.Double::class.java ||
            rawType == java.lang.Object::class.java||
            List::class.java.isAssignableFrom(rawType) ||
            Map::class.java.isAssignableFrom(rawType)) {
            return null
        }

        if (rawType.name.startsWith("com.meta.spatial")) {
            return null
        }



        @Suppress("UNCHECKED_CAST")
        // PASS THE TYPE TO THE ADAPTER
        return UniversalReflectionAdapter(gson, rawType) as TypeAdapter<T>
    }
}

// ADAPTER NOW TAKES Class<*> SO IT KNOWS T
object AlteraGson {
    // HARDCODE YOUR JITPACK PACKAGE - NEVER AUTO-DETECT
    public var SDK_PACKAGE: String = "com.digiforce.componentbasebuilder" // <- CHANGE THIS TO YOUR REAL JITPACK ID

    class Builder {
        private var skipSdkPackage = true
        private var skipMetaPackage = true
        private var useAnnotations = true
        private var autoDetectPackage = false // <- DEFAULT FALSE FOR LIBRARIES
        private var customSkipPackages = mutableListOf<String>()

        fun skipSdkPackage(skip: Boolean) = apply { this.skipSdkPackage = skip }
        fun skipMetaPackage(skip: Boolean) = apply { this.skipMetaPackage = skip }
        fun useAnnotations(use: Boolean) = apply { this.useAnnotations = use }
        fun autoDetectPackage(auto: Boolean) = apply { this.autoDetectPackage = auto }
        fun addSkipPackage(pkg: String) = apply { customSkipPackages.add(pkg) }

        fun build(): Gson {
            // NEVER use auto-detect in the default build
            val activeSdkPackage = SDK_PACKAGE // <- Always use hardcoded

            return GsonBuilder()
                .setExclusionStrategies(object:com.google.gson.ExclusionStrategy {
                    override fun shouldSkipField(f: FieldAttributes): Boolean {
                        val declaring = f.declaringClass.name
                        if (useAnnotations && f.getAnnotation(AlteraIgnore::class.java) != null) return true
                        if (skipSdkPackage && declaring.startsWith(activeSdkPackage)) return true
                        if (skipMetaPackage && declaring.startsWith("com.meta.spatial")) return true
                        return customSkipPackages.any { declaring.startsWith(it) }
                    }
                    override fun shouldSkipClass(clazz: Class<*>): Boolean {
                        val name = clazz.name
                        if (useAnnotations && clazz.isAnnotationPresent(AlteraIgnore::class.java)) return true
                        if (skipSdkPackage && name.startsWith(activeSdkPackage)) return true
                        if (skipMetaPackage && name.startsWith("com.meta.spatial")) return true
                        return customSkipPackages.any { name.startsWith(it) }
                    }
                })
                .registerTypeAdapterFactory(UniversalTypeAdapterFactory())
                .create()
        }
    }

    // THIS IS YOUR JITPACK DEFAULT - SAFE + GREEDY
    val default: Gson by lazy {
        Builder()
            .autoDetectPackage(false) // <- NEVER auto-detect
            .skipSdkPackage(true)     // <- Skip your library only
            .skipMetaPackage(true)    // <- Skip Meta
            .useAnnotations(true)     // <- Support @AlteraIgnore
            .build()
    }
}