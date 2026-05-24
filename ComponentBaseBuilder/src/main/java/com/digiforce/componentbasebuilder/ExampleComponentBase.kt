package com.digiforce.componentbasebuilder

import com.meta.spatial.core.ComponentBase
import com.meta.spatial.core.Vector3

/**
 * This class is the example code on how to use ComponentBaseBuilder
 * @class ExampleComponentBase
 * @property ExampleComponentBase this class is the example code one how to use
 * ComponentBaseBuilder
 * @constructor This is how you create the constructor you must call super and pass in a integer into the super
 * @receiver this class inherits from ComponentBaseBuilder
 *
 */
class ExampleComponentBase : ComponentBaseBuilder {
    /**
     * @param any create your params as you normally would create a class contractor use params that will be relevant to your needs
     * @property register Registers this class is in and is needed
     * @property initsets is needed to make it run without it will get a ArrayOutOfBounds Error or a initLayout Error
     *
     */
    constructor(name:String="test",f1: Float= 0.0f,vector: Vector3= Vector3(0f,0f,0f)):super(0){
        /**
         * these must be called first
         */
        register<ExampleComponentBase>(Companion)
        this.initsets(this)

        /**
         * Here you must set your values you created in the Companion this care need to initialize the values of the system
         * if you don't the default parameters will be passed and won't update can can put as many attributes as you
         * make
         * */
        named = name
        floatvalue = f1
        vectorvalue = vector

        /**
         * this is where you place your addAttribute functions after the calls to the Companion variables place as many that you want to show in your Entity to use
         * */

        this.addAttribute("name",name,this, AttributeHelper.STRINGATTRIBUTE)
        this.addAttribute("f1",f1,this, AttributeHelper.FLOATATTRIBUTE)
        this.addAttribute("vector",vector,this, AttributeHelper.VECTOR3ATTRIBUTE)

        /**
         * place your currentid = id here this tells the code which current componentBase is being built
         * */
        currentid = id

    }
    /**
     * this is where you place your getters and setters here there are 3 ways to create then these 3 here are the 3 ways to create them
     * */
    var name: String
        get() = this["name"]?:""
        set(value) { this["name"] = value}

    var f1 : Float by ComponentField("f1",0.0f)

    var vector: Vector3
        get() = getComponentDataValue("vector") as Vector3
        set(value) {setComponentDataValue("vector",value)}
    /**
     * in your code where you set the componentbase @example var example = exampleEntity.tryGetComponent<ExampleComponentBase>()
     * example?.get<Any ex. Float ,String,etc.>("name in attribute ex. f1") this is the same aas the first example in this block
     * */


    /**
     *  This override is need to set the system to this Child of the ComponentBaseBuilder otherwise you will get an error something of the likes off
     *  AbstractAttribute.get() trying to call non-object or null add this if you get that in your debugger
     * */

    override fun companion(): ComponentCompanionHelper = Companion

    /**
     * this is where you need the companion object and set it to ComponentCompanionHelper this is need to make this run
     * */
    companion object: ComponentCompanionHelper{

        /**
         * you need to place these here these vaibles will be used to initialize the attributes
         * */
        var named:String = "tested"
        var floatvalue:Float = 0.0f
        var vectorvalue:Vector3 = Vector3(0f,0f,0f)

        /**
         * you have to set theses as mutableListOf to these make it look like this don't use getter and setter it won't work.
         * */

        override val attributeNamesList: MutableList<String> = mutableListOf()
        override val attributeTypeCountsList: MutableList<Int> = mutableListOf()
        override val attributeTypesList: MutableList<Int> = mutableListOf()
        override val attributeKeysList: MutableList<Int> = mutableListOf()

        /**
         * you need this will need to look like this you must pass in your current class in the lambda with the vars
         * */

        override val createDefaultInstance: () -> ComponentBase = { ExampleComponentBase(named,floatvalue,vectorvalue) }

        /**
         * set you id like this don't use Random numbers it won't show up
         * */
        override val id: Int
            get() = 1222636

    }
}