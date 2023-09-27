package util.json

import util.json.JSON.checkDouble
import util.json.JSON.toBoolean
import util.json.JSON.toDouble
import util.json.JSON.toInteger
import util.json.JSON.toLong
import util.json.JSON.toString
import util.json.JSON.typeMismatch


class JSONObject {
    private val nameValuePairs: MutableMap<String?, Any?>

    /**
     * Creates a `JSONObject` with no name/value mappings.
     */
    constructor() {
        nameValuePairs = HashMap()
    }

    /**
     * Creates a new `JSONObject` by copying all name/value mappings from
     * the given map.
     *
     * @param copyFrom a map whose keys are of type [String] and whose
     * values are of supported types.
     * @throws NullPointerException if any of the map's keys are null.
     */
    /* (accept a raw type for API compatibility) */
    constructor(copyFrom: Map<*, *>) : this() {
        for ((key1, value) in copyFrom) {
            /*
             * Deviate from the original by checking that keys are non-null and
             * of the proper type. (We still defer validating the values).
             */
            val key = key1 as String? ?: throw NullPointerException("key == null")
            nameValuePairs[key] = wrap(value)
        }
    }

    /**
     * Creates a new `JSONObject` with name/value mappings from the next
     * object in the tokener.
     *
     * @param readFrom a tokener whose nextValue() method will yield a
     * `JSONObject`.
     * @throws JSONException if the parse fails or doesn't yield a
     * `JSONObject`.
     */
    constructor(readFrom: JSONTokener) {
        /*
         * Getting the parser to populate this could get tricky. Instead, just
         * parse to temporary JSONObject and then steal the data from that.
         */
        val `object` = readFrom.nextValue()
        if (`object` is JSONObject) {
            nameValuePairs = `object`.nameValuePairs
        } else {
            throw typeMismatch(`object`, "JSONObject")
        }
    }

    /**
     * Creates a new `JSONObject` with name/value mappings from the JSON
     * string.
     *
     * @param json a JSON-encoded string containing an object.
     * @throws JSONException if the parse fails or doesn't yield a `JSONObject`.
     */
    constructor(json: String?) : this(JSONTokener(json))

    /**
     * Creates a new `JSONObject` by copying mappings for the listed names
     * from the given object. Names that aren't present in `copyFrom` will
     * be skipped.
     */
    constructor(copyFrom: JSONObject, names: Array<String?>) : this() {
        for (name in names) {
            val value = copyFrom.opt(name)
            if (value != null) {
                nameValuePairs[name] = value
            }
        }
    }

    /**
     * Returns the number of name/value mappings in this object.
     */
    fun length(): Int {
        return nameValuePairs.size
    }

    /**
     * Maps `name` to `value`, clobbering any existing name/value
     * mapping with the same name.
     *
     * @return this object.
     */
    @Throws(JSONException::class)
    fun put(name: String?, value: Boolean): JSONObject {
        nameValuePairs[checkName(name)] = value
        return this
    }

    /**
     * Maps `name` to `value`, clobbering any existing name/value
     * mapping with the same name.
     *
     * @param value a finite value. May not be [NaNs][Double.isNaN] or
     * [infinities][Double.isInfinite].
     * @return this object.
     */
    @Throws(JSONException::class)
    fun put(name: String?, value: Double): JSONObject {
        nameValuePairs[checkName(name)] = checkDouble(value)
        return this
    }

    /**
     * Maps `name` to `value`, clobbering any existing name/value
     * mapping with the same name.
     *
     * @return this object.
     */
    @Throws(JSONException::class)
    fun put(name: String?, value: Int): JSONObject {
        nameValuePairs[checkName(name)] = value
        return this
    }

    /**
     * Maps `name` to `value`, clobbering any existing name/value
     * mapping with the same name.
     *
     * @return this object.
     */
    @Throws(JSONException::class)
    fun put(name: String?, value: Long): JSONObject {
        nameValuePairs[checkName(name)] = value
        return this
    }

    /**
     * Maps `name` to `value`, clobbering any existing name/value
     * mapping with the same name. If the value is `null`, any existing
     * mapping for `name` is removed.
     *
     * @param value a [JSONObject], [JSONArray], String, Boolean,
     * Integer, Long, Double, [.NULL], or `null`. May not be
     * [NaNs][Double.isNaN] or [     infinities][Double.isInfinite].
     * @return this object.
     */
    @Throws(JSONException::class)
    fun put(name: String?, value: Any?): JSONObject {
        if (value == null) {
            nameValuePairs.remove(name)
            return this
        }
        if (value is Number) {
            // deviate from the original by checking all Numbers, not just floats & doubles
            checkDouble(value.toDouble())
        }
        nameValuePairs[checkName(name)] = value
        return this
    }

    /**
     * Equivalent to `put(name, value)` when both parameters are non-null;
     * does nothing otherwise.
     */
    @Throws(JSONException::class)
    fun putOpt(name: String?, value: Any?): JSONObject {
        return if (name == null || value == null) {
            this
        } else put(name, value)
    }

    /**
     * Appends `value` to the array already mapped to `name`. If
     * this object has no mapping for `name`, this inserts a new mapping.
     * If the mapping exists but its value is not an array, the existing
     * and new values are inserted in order into a new array which is itself
     * mapped to `name`. In aggregate, this allows values to be added to a
     * mapping one at a time.
     *
     * @param value a [JSONObject], [JSONArray], String, Boolean,
     * Integer, Long, Double, [.NULL] or null. May not be [     ][Double.isNaN] or [infinities][Double.isInfinite].
     */
    @Throws(JSONException::class)
    fun accumulate(name: String?, value: Any?): JSONObject {
        val current = nameValuePairs[checkName(name)] ?: return put(name, value)

        // check in accumulate, since array.put(Object) doesn't do any checking
        if (value is Number) {
            checkDouble(value.toDouble())
        }
        if (current is JSONArray) {
            current.put(value)
        } else {
            val array = JSONArray()
            array.put(current)
            array.put(value)
            nameValuePairs[name] = array
        }
        return this
    }

    @Throws(JSONException::class)
    fun checkName(name: String?): String {
        if (name == null) {
            throw JSONException("Names must be non-null")
        }
        return name
    }

    /**
     * Removes the named mapping if it exists; does nothing otherwise.
     *
     * @return the value previously mapped by `name`, or null if there was
     * no such mapping.
     */
    fun remove(name: String?): Any? {
        return nameValuePairs.remove(name)
    }

    /**
     * Returns true if this object has no mapping for `name` or if it has
     * a mapping whose value is [.NULL].
     */
    fun isNull(name: String?): Boolean {
        val value = nameValuePairs[name]
        return value == null || value === NULL
    }

    /**
     * Returns true if this object has a mapping for `name`. The mapping
     * may be [.NULL].
     */
    fun has(name: String?): Boolean {
        return nameValuePairs.containsKey(name)
    }

    /**
     * Returns the value mapped by `name`.
     *
     * @throws JSONException if no such mapping exists.
     */
    @Throws(JSONException::class)
    operator fun get(name: String): Any {
        return nameValuePairs[name] ?: throw JSONException("No value for $name")
    }

    /**
     * Returns the value mapped by `name`, or null if no such mapping
     * exists.
     */
    fun opt(name: String?): Any? {
        return nameValuePairs[name]
    }

    /**
     * Returns the value mapped by `name` if it exists and is a boolean or
     * can be coerced to a boolean.
     *
     * @throws JSONException if the mapping doesn't exist or cannot be coerced
     * to a boolean.
     */
    @Throws(JSONException::class)
    fun getBoolean(name: String): Boolean {
        val `object` = get(name)
        return toBoolean(`object`) ?: throw typeMismatch(name, `object`, "boolean")
    }
    /**
     * Returns the value mapped by `name` if it exists and is a boolean or
     * can be coerced to a boolean. Returns `fallback` otherwise.
     */
    /**
     * Returns the value mapped by `name` if it exists and is a boolean or
     * can be coerced to a boolean. Returns false otherwise.
     */
    @JvmOverloads
    fun optBoolean(name: String?, fallback: Boolean = false): Boolean {
        val `object` = opt(name)
        val result = toBoolean(`object`)
        return result ?: fallback
    }

    /**
     * Returns the value mapped by `name` if it exists and is a double or
     * can be coerced to a double.
     *
     * @throws JSONException if the mapping doesn't exist or cannot be coerced
     * to a double.
     */
    @Throws(JSONException::class)
    fun getDouble(name: String): Double {
        val `object` = get(name)
        return toDouble(`object`) ?: throw typeMismatch(name, `object`, "double")
    }
    /**
     * Returns the value mapped by `name` if it exists and is a double or
     * can be coerced to a double. Returns `fallback` otherwise.
     */
    /**
     * Returns the value mapped by `name` if it exists and is a double or
     * can be coerced to a double. Returns `NaN` otherwise.
     */
    @JvmOverloads
    fun optDouble(name: String?, fallback: Double = Double.NaN): Double {
        val `object` = opt(name)
        val result = toDouble(`object`)
        return result ?: fallback
    }

    /**
     * Returns the value mapped by `name` if it exists and is an int or
     * can be coerced to an int.
     *
     * @throws JSONException if the mapping doesn't exist or cannot be coerced
     * to an int.
     */
    @Throws(JSONException::class)
    fun getInt(name: String): Int {
        val `object` = get(name)
        return toInteger(`object`) ?: throw typeMismatch(name, `object`, "int")
    }
    /**
     * Returns the value mapped by `name` if it exists and is an int or
     * can be coerced to an int. Returns `fallback` otherwise.
     */
    /**
     * Returns the value mapped by `name` if it exists and is an int or
     * can be coerced to an int. Returns 0 otherwise.
     */
    @JvmOverloads
    fun optInt(name: String?, fallback: Int = 0): Int {
        val `object` = opt(name)
        val result = toInteger(`object`)
        return result ?: fallback
    }

    /**
     * Returns the value mapped by `name` if it exists and is a long or
     * can be coerced to a long. Note that JSON represents numbers as doubles,
     * so this is [lossy](#lossy); use strings to transfer numbers via JSON.
     *
     * @throws JSONException if the mapping doesn't exist or cannot be coerced
     * to a long.
     */
    @Throws(JSONException::class)
    fun getLong(name: String): Long {
        val `object` = get(name)
        return toLong(`object`) ?: throw typeMismatch(name, `object`, "long")
    }
    /**
     * Returns the value mapped by `name` if it exists and is a long or
     * can be coerced to a long. Returns `fallback` otherwise. Note that JSON represents
     * numbers as doubles, so this is [lossy](#lossy); use strings to transfer
     * numbers via JSON.
     */
    /**
     * Returns the value mapped by `name` if it exists and is a long or
     * can be coerced to a long. Returns 0 otherwise. Note that JSON represents numbers as doubles,
     * so this is [lossy](#lossy); use strings to transfer numbers via JSON.
     */
    @JvmOverloads
    fun optLong(name: String?, fallback: Long = 0L): Long {
        val `object` = opt(name)
        val result = toLong(`object`)
        return result ?: fallback
    }

    /**
     * Returns the value mapped by `name` if it exists, coercing it if
     * necessary.
     *
     * @throws JSONException if no such mapping exists.
     */
    @Throws(JSONException::class)
    fun getString(name: String): String {
        val `object` = get(name)
        return toString(`object`) ?: throw typeMismatch(name, `object`, "String")
    }
    /**
     * Returns the value mapped by `name` if it exists, coercing it if
     * necessary. Returns `fallback` if no such mapping exists.
     */
    /**
     * Returns the value mapped by `name` if it exists, coercing it if
     * necessary. Returns the empty string if no such mapping exists.
     */
    @JvmOverloads
    fun optString(name: String?, fallback: String? = ""): String {
        val `object` = opt(name)
        val result = toString(`object`)
        return result ?: fallback!!
    }

    /**
     * Returns the value mapped by `name` if it exists and is a `JSONArray`.
     *
     * @throws JSONException if the mapping doesn't exist or is not a `JSONArray`.
     */
    @Throws(JSONException::class)
    fun getJSONArray(name: String): JSONArray {
        val `object` = get(name)
        return if (`object` is JSONArray) {
            `object`
        } else {
            throw typeMismatch(name, `object`, "JSONArray")
        }
    }

    /**
     * Returns the value mapped by `name` if it exists and is a `JSONArray`. Returns null otherwise.
     */
    fun optJSONArray(name: String?): JSONArray? {
        val `object` = opt(name)
        return if (`object` is JSONArray) `object` else null
    }

    /**
     * Returns the value mapped by `name` if it exists and is a `JSONObject`.
     *
     * @throws JSONException if the mapping doesn't exist or is not a `JSONObject`.
     */
    @Throws(JSONException::class)
    fun getJSONObject(name: String): JSONObject {
        val `object` = get(name)
        return if (`object` is JSONObject) {
            `object`
        } else {
            throw typeMismatch(name, `object`, "JSONObject")
        }
    }

    /**
     * Returns the value mapped by `name` if it exists and is a `JSONObject`. Returns null otherwise.
     */
    fun optJSONObject(name: String?): JSONObject? {
        val `object` = opt(name)
        return if (`object` is JSONObject) `object` else null
    }

    /**
     * Returns an array with the values corresponding to `names`. The
     * array contains null for names that aren't mapped. This method returns
     * null if `names` is either null or empty.
     */
    @Throws(JSONException::class)
    fun toJSONArray(names: JSONArray?): JSONArray? {
        val result = JSONArray()
        if (names == null) {
            return null
        }
        val length = names.length()
        if (length == 0) {
            return null
        }
        for (i in 0 until length) {
            val name = toString(names.opt(i))
            result.put(opt(name))
        }
        return result
    }

    /**
     * Returns an iterator of the `String` names in this object. The
     * returned iterator supports [remove][Iterator.remove], which will
     * remove the corresponding mapping from this object. If this object is
     * modified after the iterator is returned, the iterator's behavior is
     * undefined. The order of the keys is undefined.
     */
    /* Return a raw type for API compatibility */
    fun keys(): Iterator<*> {
        return nameValuePairs.keys.iterator()
    }

    /**
     * Returns an array containing the string names in this object. This method
     * returns null if this object contains no mappings.
     */
    fun names(): JSONArray? {
        return if (nameValuePairs.isEmpty()) null else JSONArray(ArrayList(nameValuePairs.keys))
    }

    /**
     * Encodes this object as a compact JSON string, such as:
     * <pre>{"query":"Pizza","locations":[94043,90210]}</pre>
     */
    override fun toString(): String {
        return try {
            val stringer = JSONStringer()
            writeTo(stringer)
            stringer.toString()
        } catch (e: JSONException) {
            ""
        }
    }

    /**
     * Encodes this object as a human readable JSON string for debugging, such
     * as:
     * <pre>
     * {
     * "query": "Pizza",
     * "locations": [
     * 94043,
     * 90210
     * ]
     * }</pre>
     *
     * @param indentSpaces the number of spaces to indent for each level of
     * nesting.
     */
    @Throws(JSONException::class)
    fun toString(indentSpaces: Int): String {
        val stringer = JSONStringer(indentSpaces)
        writeTo(stringer)
        return stringer.toString()
    }

    @Throws(JSONException::class)
    fun writeTo(stringer: JSONStringer) {
        stringer.`object`()
        for ((key, value) in nameValuePairs) {
            stringer.key(key).value(value)
        }
        stringer.endObject()
    }

    companion object {
        private const val NEGATIVE_ZERO = -0.0

        /**
         * A sentinel value used to explicitly define a name with no value. Unlike
         * `null`, names with this value:
         *
         *  * show up in the [.names] array
         *  * show up in the [.keys] iterator
         *  * return `true` for [.has]
         *  * do not throw on [.get]
         *  * are included in the encoded JSON string.
         *
         *
         *
         * This value violates the general contract of [Object.equals] by
         * returning true when compared to `null`. Its [.toString]
         * method returns "null".
         */
        val NULL: Any = object : Any() {
            override fun equals(o: Any?): Boolean {
                return o === this || o == null // API specifies this broken equals implementation
            }

            override fun toString(): String {
                return "null"
            }
        }

        /**
         * Encodes the number as a JSON string.
         *
         * @param number a finite value. May not be [NaNs][Double.isNaN] or
         * [infinities][Double.isInfinite].
         */
        @Throws(JSONException::class)
        fun numberToString(number: Number?): String {
            if (number == null) {
                throw JSONException("Number must be non-null")
            }
            val doubleValue = number.toDouble()
            checkDouble(doubleValue)

            // the original returns "-0" instead of "-0.0" for negative zero
            if (number == NEGATIVE_ZERO) {
                return "-0"
            }
            val longValue = number.toLong()
            return if (doubleValue == longValue.toDouble()) {
                longValue.toString()
            } else number.toString()
        }

        /**
         * Encodes `data` as a JSON string. This applies quotes and any
         * necessary character escaping.
         *
         * @param data the string to encode. Null will be interpreted as an empty
         * string.
         */
        fun quote(data: String?): String {
            return if (data == null) {
                "\"\""
            } else try {
                val stringer = JSONStringer()
                stringer.open(JSONStringer.Scope.NULL, "")
                stringer.value(data)
                stringer.close(JSONStringer.Scope.NULL, JSONStringer.Scope.NULL, "")
                stringer.toString()
            } catch (e: JSONException) {
                throw AssertionError()
            }
        }

        /**
         * Wraps the given object if necessary.
         *
         *
         * If the object is null or , returns [.NULL].
         * If the object is a `JSONArray` or `JSONObject`, no wrapping is necessary.
         * If the object is `NULL`, no wrapping is necessary.
         * If the object is an array or `Collection`, returns an equivalent `JSONArray`.
         * If the object is a `Map`, returns an equivalent `JSONObject`.
         * If the object is a primitive wrapper type or `String`, returns the object.
         * Otherwise if the object is from a `java` package, returns the result of `toString`.
         * If wrapping fails, returns null.
         */
        fun wrap(o: Any?): Any? {
            if (o == null) {
                return NULL
            }
            if (o is JSONArray || o is JSONObject) {
                return o
            }
            if (o == NULL) {
                return o
            }
            try {
                if (o is Collection<*>) {
                    return JSONArray(o as Collection<Any>?)
                } else if (o.javaClass.isArray) {
                    return JSONArray(o)
                }
                if (o is Map<*, *>) {
                    return JSONObject(o)
                }
                if (o is Boolean ||
                    o is Byte ||
                    o is Char ||
                    o is Double ||
                    o is Float ||
                    o is Int ||
                    o is Long ||
                    o is Short ||
                    o is String
                ) {
                    return o
                }
                if (o.javaClass.getPackage().name.startsWith("java.")) {
                    return o.toString()
                }
            } catch (ignored: Exception) {
            }
            return null
        }
    }
}

