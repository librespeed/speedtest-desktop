package util.json

internal object JSON {
    /**
     * Returns the input if it is a JSON-permissible value; throws otherwise.
     */
    @Throws(JSONException::class)
    fun checkDouble(d: Double): Double {
        if (java.lang.Double.isInfinite(d) || java.lang.Double.isNaN(d)) {
            throw JSONException("Forbidden numeric value: $d")
        }
        return d
    }

    fun toBoolean(value: Any?): Boolean? {
        if (value is Boolean) {
            return value
        } else if (value is String) {
            if ("true".equals(value, ignoreCase = true)) {
                return true
            } else if ("false".equals(value, ignoreCase = true)) {
                return false
            }
        }
        return null
    }

    fun toDouble(value: Any?): Double? {
        when (value) {
            is Double -> {
                return value
            }

            is Number -> {
                return value.toDouble()
            }

            is String -> {
                try {
                    return value.toDouble()
                } catch (ignored: NumberFormatException) {
                }
            }
        }
        return null
    }

    fun toInteger(value: Any?): Int? {
        when (value) {
            is Int -> {
                return value
            }

            is Number -> {
                return value.toInt()
            }

            is String -> {
                try {
                    return value.toDouble().toInt()
                } catch (ignored: NumberFormatException) {
                }
            }
        }
        return null
    }

    fun toLong(value: Any?): Long? {
        when (value) {
            is Long -> {
                return value
            }

            is Number -> {
                return value.toLong()
            }

            is String -> {
                try {
                    return value.toDouble().toLong()
                } catch (ignored: NumberFormatException) {
                }
            }
        }
        return null
    }

    fun toString(value: Any?): String? {
        if (value is String) {
            return value
        } else if (value != null) {
            return value.toString()
        }
        return null
    }

    @Throws(JSONException::class)
    fun typeMismatch(
        indexOrName: Any, actual: Any?,
        requiredType: String
    ): JSONException {
        if (actual == null) {
            throw JSONException("Value at $indexOrName is null.")
        } else {
            throw JSONException(
                "Value " + actual + " at " + indexOrName
                        + " of type " + actual.javaClass.getName()
                        + " cannot be converted to " + requiredType
            )
        }
    }

    @Throws(JSONException::class)
    fun typeMismatch(actual: Any?, requiredType: String): JSONException {
        if (actual == null) {
            throw JSONException("Value is null.")
        } else {
            throw JSONException(
                ("Value " + actual
                        + " of type " + actual.javaClass.getName()
                        + " cannot be converted to " + requiredType)
            )
        }
    }
}

