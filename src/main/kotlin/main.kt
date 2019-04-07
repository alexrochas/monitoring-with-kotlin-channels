import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.consumeEach

enum class MessageType { ERROR, SUCCESS }
data class Message(val type: MessageType, val body: String)
data class HealthStatus(val status: String)

val msgBuffer = mutableListOf<Message>()

fun healthStatus(): HealthStatus {
    msgBuffer.take(5).forEach {
        println(it)
        if (it.type == MessageType.ERROR) {
            return HealthStatus("RED")
        }
    }

    return HealthStatus("GREEN")
}

object ReactiveChannel {
    @ExperimentalCoroutinesApi
    fun BroadcastChannel<Message>.emit(message: Message) {
        this.offer(message)
    }

    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    fun BroadcastChannel<Message>.subscribe(function: (Message) -> Boolean) {
        CoroutineScope(Dispatchers.Unconfined).launch {
            consumeEach {
                function(it)
            }
        }
    }
}

@ExperimentalCoroutinesApi
fun main() {
    val broadcast = BroadcastChannel<Message>(100)
    with(ReactiveChannel) {
        broadcast.subscribe { msg: Message ->
            msgBuffer.add(msg)
        }
        broadcast.emit(Message(MessageType.SUCCESS, "Something good"))
        broadcast.emit(Message(MessageType.SUCCESS, "Something good"))
        broadcast.emit(Message(MessageType.ERROR, "Something bad"))
        broadcast.emit(Message(MessageType.ERROR, "Something bad"))
        broadcast.emit(Message(MessageType.ERROR, "Something bad"))
        broadcast.emit(Message(MessageType.ERROR, "Something bad"))
        broadcast.emit(Message(MessageType.SUCCESS, "Something good"))
        broadcast.emit(Message(MessageType.SUCCESS, "Something good"))
    }
    println(healthStatus())
    broadcast.close()
}

