package com.digiforce.componentbasebuilder


import android.net.Uri
import android.util.Log
import com.digiforce.componentbasebuilder.ComponentBaseBuilder.Companion.attributeTypesList
import com.digiforce.componentbasebuilder.ComponentBaseBuilder.Companion.currentid
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
import kotlin.reflect.KProperty

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
    URIATTRIBUTE(14, ::URIAttribute)



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

    }
    constructor(initAttribute:Int=0,callback:(thiss: ComponentBaseBuilder)-> Unit = {}):super(){
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

        override var createDefaultInstance: () -> ComponentBase ={ ComponentBaseBuilder() }


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

    /**
     * This function is used to create the attribute of the Component Base and has to be called in an init{} or a constructor
     * @param name is a String that you used to name the attribute and set ro rhe component
     * @param value is used to pass in the data you want to set be it a float or string etc. , you have to match the attribute you call
     * @param component is the componentBaseBuilder you call it in usually the word this
     * @param atthelper is a call to the AttributeHelper.STRINGATTRIBUTE, etc.
     * @sample showExample
     * */
    open fun <T> addAttribute(name: String, value: T?, component: ComponentBaseBuilder, atthelper: AttributeHelper){


        @Suppress("UNCHECKED_CAST")
        component.buildAttribute<T>(
            name, value as? T?,
            component,atthelper.creator as AttributeCreator
        )

        component.listOfAttribute.add(currentid)



    }

    open operator fun <T> get(key: String): T? {
        val comp = companion() as ComponentCompanionHelper
        val index = comp.keyStringToKeyIntMap(key) ?: return null
        return getComponentDataValue(index) as? T
    }
    operator fun set(key: String, value: Any) {
        val comp = ComponentBaseBuilder.Companion as ComponentCompanionHelper
        val index = comp.keyStringToKeyIntMap(key) ?: return
        setComponentDataValue(index, value)
    }
    protected open fun <T> buildAttribute(name:String, value:Any?, component: ComponentBaseBuilder, creator: (Int, ComponentBase, Int, Any) -> AbstractAttribute, terminator: Boolean=false){
        component.addAtt<T>(currentid!!, component, creator)
        component.companion().attributeNamesList.add(name)
        component.currentBuild.add(component.attributesMap[currentid]!!(component.attributeKeyCounter,component,component.attributeKeyCounter,value!!))
        setComponentDataValue(component.attributeKeyCounter,value)

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
            else -> error("Unsupported type")
        }
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




}

open class ComponentField<T>(private val key: String, private val default: T) {
    operator fun getValue(thisRef: ComponentBaseBuilder, property: KProperty<*>): T {

        return thisRef[key]?: default
    }
    operator fun setValue(thisRef: ComponentBaseBuilder, property: KProperty<*>, value: Any) {
        thisRef[key] = value
    }
}