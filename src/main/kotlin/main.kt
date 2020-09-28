import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.rest.util.Permission
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle

@FlowPreview
@InternalCoroutinesApi
fun main() = runBlocking {
    val client = DiscordClient.create(System.getenv("TOKEN"))
        .login().awaitSingle()

    val messages = client.on(MessageCreateEvent::class.java).asFlow()
    val adminMessages = messages.filter {
        val member = it.member.orElse(null) ?: return@filter false
        val permissions = member.basePermissions.awaitSingle()
        Permission.ADMINISTRATOR in permissions
    }
    val commandMessages = adminMessages.filter {
        it.message.content.toLowerCase().startsWith("d!deleteall")
    }
    commandMessages.collect { e ->
        val args = e.message.content.toLowerCase().removePrefix("d!deleteall ")
        val userId = Snowflake.of(args)
        val guild = e.guild.awaitSingle()
        deleteAllMessages(guild, userId, e.message.id, e.member.get(), e.message.channel.awaitSingle())
    }
}

@FlowPreview
private suspend fun deleteAllMessages(
    guild: Guild,
    userId: Snowflake,
    before: Snowflake,
    admin: Member,
    responseChannel: MessageChannel
) {
    responseChannel.createMessage("${admin.mention} started deleting").awaitFirstOrNull()
    val channels = guild.channels.ofType(TextChannel::class.java).asFlow()
    channels.flatMapMerge { channel ->
        val messages = channel.getMessagesBefore(before).asFlow()
        val toDelete = messages.filter { message ->
            message.author.map { author -> author.id == userId }.orElse(false)
        }
        val notDeleted = channel.bulkDeleteMessages(toDelete.asPublisher()).asFlow()
        notDeleted.map { message ->
            message.delete().awaitFirstOrNull()
        }
    }.collect()
    responseChannel.createMessage("${admin.mention} finished deleting").awaitFirstOrNull()
}