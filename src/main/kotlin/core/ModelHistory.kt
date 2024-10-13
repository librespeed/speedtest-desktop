package core

data class ModelHistory(
    var id : Int = 0,
    var netAdapter : String,
    var ping : Double,
    var jitter : Double,
    var download : Double,
    var upload : Double,
    var ispInfo : String,
    var testPoint : String,
    var date : Long
)
