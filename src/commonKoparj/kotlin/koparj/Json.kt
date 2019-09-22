package koparj

/**
 * TODO: document
 */
interface Json<E : Json.Element<E>> {
    fun parse(raw: kotlin.String) : E

    interface Element<E>

    interface Object<S : String<E>, E: Element<E>> : Element<E>, Map<S, E> {
        fun get(key: kotlin.String)  : E?
        fun getValue(key: kotlin.String) : E
        fun getOrElse(key: kotlin.String, defaultValue: () -> E) : E
    }

    interface Array<E: Element<E>> : Element<E>, List<E>

    interface String<E: Element<E>> : Element<E> {
        val value: kotlin.String
    }

    interface Number<E: Element<E>> : Element<E> {
        val value: kotlin.Number
    }

    interface True<E: Element<E>> : Element<E> {
        val value: kotlin.Boolean
            get() = true
    }

    interface False<E: Element<E>> : Element<E> {
        val value: kotlin.Boolean
            get() = false
    }

    interface Null<E: Element<E>> : Element<E> {
        val value: kotlin.Any?
            get() = null
    }
}
