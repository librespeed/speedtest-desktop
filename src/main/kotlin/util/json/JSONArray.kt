package util.json

import util.json.JSON.checkDouble
import util.json.JSON.toBoolean
import util.json.JSON.toDouble
import util.json.JSON.toInteger
import util.json.JSON.toLong
import util.json.JSON.toString
import util.json.JSON.typeMismatch
import java.lang.reflect.Array
import kotlin.math.min

class JSONArray {
    private val values: MutableList<Any?>

    /**
     * Creates a `JSONArray` with no values.
     */
    constructor() {
        values = ArrayList()
    }

    /**
     * Creates a new `JSONArray` by copying all values from the given
     * collection.
     *
     * @param copyFrom a collection whose values are of supported types.
     * Unsupported values are not permitted and will yield an array in an
     * inconsistent state.
     */
    /* Accept a raw type for API compatibility */
    constructor(copyFrom: Collection<Any>?) : this() {
        if (copyFrom != null) {
            val it = copyFrom.iterator()
            while (it.hasNext()) {
                put(JSONObject.wrap(it.next()))
            }
        }
    }

    /**
     * Creates a new `JSONArray` with values from the next array in the
     * tokener.
     *
     * @param readFrom a tokener whose nextValue() method will yield a
     * `JSONArray`.
     * @throws JSONException if the parse fails or doesn't yield a
     * `JSONArray`.
     */
    constructor(readFrom: JSONTokener) {
        /*
         * Getting the parser to populate this could get tricky. Instead, just
         * parse to temporary JSONArray and then steal the data from that.
         */
        val `object` = readFrom.nextValue()
        values = if (`object` is JSONArray) {
            `object`.values
        } else {
            throw typeMismatch(`object`, "JSONArray")
        }
    }

    /**
     * Creates a new `JSONArray` with values from the JSON string.
     *
     * @param json a JSON-encoded string containing an array.
     * @throws JSONException if the parse fails or doesn't yield a `JSONArray`.
     */
    constructor(json: String) : this(JSONTokener(json))

    /**
     * Creates a new `JSONArray` with values from the given primitive array.
     */
    constructor(array: Any) {
        if (!array.javaClass.isArray) {
            throw JSONException("Not a primitive array: " + array.javaClass)
        }
        val length = Array.getLength(array)
        values = ArrayList(length)
        for (i in 0 until length) {
            put(JSONObject.wrap(Array.get(array, i)))
        }
    }

    /**
     * Returns the number of values in this array.
     */
    fun length(): Int {
        return values.size
    }

    /**
     * Appends `value` to the end of this array.
     *
     * @return this array.
     */
    fun put(value: Boolean): JSONArray {
        values.add(value)
        return this
    }

    /**
     * Appends `value` to the end of this array.
     *
     * @param value a finite value. May not be [NaNs][Double.isNaN] or
     * [infinities][Double.isInfinite].
     * @return this array.
     */
    @Throws(JSONException::class)
    fun put(value: Double): JSONArray {
        values.add(checkDouble(value))
        return this
    }

    /**
     * Appends `value` to the end of this array.
     *
     * @return this array.
     */
    fun put(value: Int): JSONArray {
        values.add(value)
        return this
    }

    /**
     * Appends `value` to the end of this array.
     *
     * @return this array.
     */
    fun put(value: Long): JSONArray {
        values.add(value)
        return this
    }

    /**
     * Appends `value` to the end of this array.
     *
     * @param value a [JSONObject], [JSONArray], String, Boolean,
     * Integer, Long, Double, [JSONObject.NULL], or `null`. May
     * not be [NaNs][Double.isNaN] or [     infinities][Double.isInfinite]. Unsupported values are not permitted and will cause the
     * array to be in an inconsistent state.
     * @return this array.
     */
    fun put(value: Any?): JSONArray {
        values.add(value)
        return this
    }

    /**
     * Sets the value at `index` to `value`, null padding this array
     * to the required length if necessary. If a value already exists at `index`, it will be replaced.
     *
     * @return this array.
     */
    @Throws(JSONException::class)
    fun put(index: Int, value: Boolean): JSONArray {
        return put(index, value)
    }

    /**
     * Sets the value at `index` to `value`, null padding this array
     * to the required length if necessary. If a value already exists at `index`, it will be replaced.
     *
     * @param value a finite value. May not be [NaNs][Double.isNaN] or
     * [infinities][Double.isInfinite].
     * @return this array.
     */
    @Throws(JSONException::class)
    fun put(index: Int, value: Double): JSONArray {
        return put(index, value)
    }

    /**
     * Sets the value at `index` to `value`, null padding this array
     * to the required length if necessary. If a value already exists at `index`, it will be replaced.
     *
     * @return this array.
     */
    @Throws(JSONException::class)
    fun put(index: Int, value: Int): JSONArray {
        return put(index, value)
    }

    /**
     * Sets the value at `index` to `value`, null padding this array
     * to the required length if necessary. If a value already exists at `index`, it will be replaced.
     *
     * @return this array.
     */
    @Throws(JSONException::class)
    fun put(index: Int, value: Long): JSONArray {
        return put(index, value)
    }

    /**
     * Sets the value at `index` to `value`, null padding this array
     * to the required length if necessary. If a value already exists at `index`, it will be replaced.
     *
     * @param value a [JSONObject], [JSONArray], String, Boolean,
     * Integer, Long, Double, [JSONObject.NULL], or `null`. May
     * not be [NaNs][Double.isNaN] or [     infinities][Double.isInfinite].
     * @return this array.
     */
    @Throws(JSONException::class)
    fun put(index: Int, value: Any?): JSONArray {
        if (value is Number) {
            // deviate from the original by checking all Numbers, not just floats & doubles
            checkDouble(value.toDouble())
        }
        while (values.size <= index) {
            values.add(null)
        }
        values[index] = value
        return this
    }

    /**
     * Returns true if this array has no value at `index`, or if its value
     * is the `null` reference or [JSONObject.NULL].
     */
    fun isNull(index: Int): Boolean {
        val value = opt(index)
        return value == null || value === JSONObject.NULL
    }

    /**
     * Returns the value at `index`.
     *
     * @throws JSONException if this array has no value at `index`, or if
     * that value is the `null` reference. This method returns
     * normally if the value is `JSONObject#NULL`.
     */
    @Throws(JSONException::class)
    operator fun get(index: Int): Any {
        return try {
            val value = values[index] ?: throw JSONException("Value at $index is null.")
            value
        } catch (e: IndexOutOfBoundsException) {
            throw JSONException("Index " + index + " out of range [0.." + values.size + ")")
        }
    }

    /**
     * Returns the value at `index`, or null if the array has no value
     * at `index`.
     */
    fun opt(index: Int): Any? {
        return if (index < 0 || index >= values.size) {
            null
        } else values[index]
    }

    /**
     * Removes and returns the value at `index`, or null if the array has no value
     * at `index`.
     */
    fun remove(index: Int): Any? {
        return if (index < 0 || index >= values.size) {
            null
        } else values.removeAt(index)
    }

    /**
     * Returns the value at `index` if it exists and is a boolean or can
     * be coerced to a boolean.
     *
     * @throws JSONException if the value at `index` doesn't exist or
     * cannot be coerced to a boolean.
     */
    @Throws(JSONException::class)
    fun getBoolean(index: Int): Boolean {
        val `object` = get(index)
        return toBoolean(`object`) ?: throw typeMismatch(index, `object`, "boolean")
    }
    /**
     * Returns the value at `index` if it exists and is a boolean or can
     * be coerced to a boolean. Returns `fallback` otherwise.
     */
    /**
     * Returns the value at `index` if it exists and is a boolean or can
     * be coerced to a boolean. Returns false otherwise.
     */
    @JvmOverloads
    fun optBoolean(index: Int, fallback: Boolean = false): Boolean {
        val `object` = opt(index)
        val result = toBoolean(`object`)
        return result ?: fallback
    }

    /**
     * Returns the value at `index` if it exists and is a double or can
     * be coerced to a double.
     *
     * @throws JSONException if the value at `index` doesn't exist or
     * cannot be coerced to a double.
     */
    @Throws(JSONException::class)
    fun getDouble(index: Int): Double {
        val `object` = get(index)
        return toDouble(`object`) ?: throw typeMismatch(index, `object`, "double")
    }
    /**
     * Returns the value at `index` if it exists and is a double or can
     * be coerced to a double. Returns `fallback` otherwise.
     */
    /**
     * Returns the value at `index` if it exists and is a double or can
     * be coerced to a double. Returns `NaN` otherwise.
     */
    @JvmOverloads
    fun optDouble(index: Int, fallback: Double = Double.NaN): Double {
        val `object` = opt(index)
        val result = toDouble(`object`)
        return result ?: fallback
    }

    /**
     * Returns the value at `index` if it exists and is an int or
     * can be coerced to an int.
     *
     * @throws JSONException if the value at `index` doesn't exist or
     * cannot be coerced to a int.
     */
    @Throws(JSONException::class)
    fun getInt(index: Int): Int {
        val `object` = get(index)
        return toInteger(`object`) ?: throw typeMismatch(index, `object`, "int")
    }
    /**
     * Returns the value at `index` if it exists and is an int or
     * can be coerced to an int. Returns `fallback` otherwise.
     */
    /**
     * Returns the value at `index` if it exists and is an int or
     * can be coerced to an int. Returns 0 otherwise.
     */
    @JvmOverloads
    fun optInt(index: Int, fallback: Int = 0): Int {
        val `object` = opt(index)
        val result = toInteger(`object`)
        return result ?: fallback
    }

    /**
     * Returns the value at `index` if it exists and is a long or
     * can be coerced to a long.
     *
     * @throws JSONException if the value at `index` doesn't exist or
     * cannot be coerced to a long.
     */
    @Throws(JSONException::class)
    fun getLong(index: Int): Long {
        val `object` = get(index)
        return toLong(`object`) ?: throw typeMismatch(index, `object`, "long")
    }
    /**
     * Returns the value at `index` if it exists and is a long or
     * can be coerced to a long. Returns `fallback` otherwise.
     */
    /**
     * Returns the value at `index` if it exists and is a long or
     * can be coerced to a long. Returns 0 otherwise.
     */
    @JvmOverloads
    fun optLong(index: Int, fallback: Long = 0L): Long {
        val `object` = opt(index)
        val result = toLong(`object`)
        return result ?: fallback
    }

    /**
     * Returns the value at `index` if it exists, coercing it if
     * necessary.
     *
     * @throws JSONException if no such value exists.
     */
    @Throws(JSONException::class)
    fun getString(index: Int): String {
        val `object` = get(index)
        return toString(`object`) ?: throw typeMismatch(index, `object`, "String")
    }
    /**
     * Returns the value at `index` if it exists, coercing it if
     * necessary. Returns `fallback` if no such value exists.
     */
    /**
     * Returns the value at `index` if it exists, coercing it if
     * necessary. Returns the empty string if no such value exists.
     */
    @JvmOverloads
    fun optString(index: Int, fallback: String? = ""): String {
        val `object` = opt(index)
        val result = toString(`object`)
        return result ?: fallback!!
    }

    /**
     * Returns the value at `index` if it exists and is a `JSONArray`.
     *
     * @throws JSONException if the value doesn't exist or is not a `JSONArray`.
     */
    @Throws(JSONException::class)
    fun getJSONArray(index: Int): JSONArray {
        val `object` = get(index)
        return if (`object` is JSONArray) {
            `object`
        } else {
            throw typeMismatch(index, `object`, "JSONArray")
        }
    }

    /**
     * Returns the value at `index` if it exists and is a `JSONArray`. Returns null otherwise.
     */
    fun optJSONArray(index: Int): JSONArray? {
        val `object` = opt(index)
        return if (`object` is JSONArray) `object` else null
    }

    /**
     * Returns the value at `index` if it exists and is a `JSONObject`.
     *
     * @throws JSONException if the value doesn't exist or is not a `JSONObject`.
     */
    @Throws(JSONException::class)
    fun getJSONObject(index: Int): JSONObject {
        val `object` = get(index)
        return if (`object` is JSONObject) {
            `object`
        } else {
            throw typeMismatch(index, `object`, "JSONObject")
        }
    }

    /**
     * Returns the value at `index` if it exists and is a `JSONObject`. Returns null otherwise.
     */
    fun optJSONObject(index: Int): JSONObject? {
        val `object` = opt(index)
        return if (`object` is JSONObject) `object` else null
    }

    /**
     * Returns a new object whose values are the values in this array, and whose
     * names are the values in `names`. Names and values are paired up by
     * index from 0 through to the shorter array's length. Names that are not
     * strings will be coerced to strings. This method returns null if either
     * array is empty.
     */
    @Throws(JSONException::class)
    fun toJSONObject(names: JSONArray): JSONObject? {
        val result = JSONObject()
        val length = min(names.length().toDouble(), values.size.toDouble()).toInt()
        if (length == 0) {
            return null
        }
        for (i in 0 until length) {
            val name = toString(names.opt(i))
            result.put(name, opt(i))
        }
        return result
    }

    /**
     * Returns a new string by alternating this array's values with `separator`. This array's string values are quoted and have their special
     * characters escaped. For example, the array containing the strings '12"
     * pizza', 'taco' and 'soda' joined on '+' returns this:
     * <pre>"12\" pizza"+"taco"+"soda"</pre>
     */
    @Throws(JSONException::class)
    fun join(separator: String?): String {
        val stringer = JSONStringer()
        stringer.open(JSONStringer.Scope.NULL, "")
        var i = 0
        val size = values.size
        while (i < size) {
            if (i > 0) {
                stringer.out.append(separator)
            }
            stringer.value(values[i])
            i++
        }
        stringer.close(JSONStringer.Scope.NULL, JSONStringer.Scope.NULL, "")
        return stringer.out.toString()
    }

    override fun toString(): String {
        return try {
            val stringer = JSONStringer()
            writeTo(stringer)
            stringer.toString()
        } catch (e: JSONException) {
            ""
        }
    }

    @Throws(JSONException::class)
    fun toString(indentSpaces: Int): String {
        val stringer = JSONStringer(indentSpaces)
        writeTo(stringer)
        return stringer.toString()
    }

    @Throws(JSONException::class)
    fun writeTo(stringer: JSONStringer) {
        stringer.array()
        for (value in values) {
            stringer.value(value)
        }
        stringer.endArray()
    }

    override fun equals(other: Any?): Boolean {
        return other is JSONArray && other.values == values
    }

    override fun hashCode(): Int {
        // diverge from the original, which doesn't implement hashCode
        return values.hashCode()
    }
}

