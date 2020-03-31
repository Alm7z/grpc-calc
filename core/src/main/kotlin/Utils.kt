import org.example.calculator.SimpleIntValue
import org.example.calculator.SimpleValue

fun simpleValueOf(value: Double): SimpleValue {
    return SimpleValue.newBuilder().setValue(value).build()
}

fun Double.asSimpleValue(): SimpleValue {
    return SimpleValue.newBuilder().setValue(this).build()
}

fun simpleIntValueOf(value: Int): SimpleIntValue {
    return SimpleIntValue.newBuilder().setValue(value).build()
}

fun Int.asSimpleIntValue(): SimpleIntValue {
    return SimpleIntValue.newBuilder().setValue(this).build()
}
