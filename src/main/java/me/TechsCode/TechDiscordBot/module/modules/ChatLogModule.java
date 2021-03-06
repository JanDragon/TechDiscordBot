package me.TechsCode.TechDiscordBot.module.modules;

import me.TechsCode.TechDiscordBot.TechDiscordBot;
import me.TechsCode.TechDiscordBot.module.Module;
import me.TechsCode.TechDiscordBot.objects.DefinedQuery;
import me.TechsCode.TechDiscordBot.objects.Query;
import me.TechsCode.TechDiscordBot.objects.Requirement;
import me.TechsCode.TechDiscordBot.util.TechEmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.util.HashMap;

public class ChatLogModule extends Module {

    private final DefinedQuery<TextChannel> CHATLOGS_CHANNEL = new DefinedQuery<TextChannel>() {
        @Override
        protected Query<TextChannel> newQuery() {
            return bot.getChannels("chatlogs");
        }
    };

    private HashMap<String, Message> cachedMessages = new HashMap<>();

    private final DefinedQuery<Role> STAFF_ROLE = new DefinedQuery<Role>() {
        @Override
        protected Query newQuery() {
            return bot.getRoles("Staff");
        }
    };

    public ChatLogModule(TechDiscordBot bot) {
        super(bot);
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    @SubscribeEvent
    public void onMessage(GuildMessageReceivedEvent e) {
        if(e.getMember() == null) return;
        if(e.getMember().getRoles().stream().anyMatch(r -> r.getName().equals("Staff"))) return;

        cachedMessages.put(e.getMessageId(), e.getMessage());
    }

    @SubscribeEvent
    public void onMessageEdit(GuildMessageUpdateEvent e) {
        if(e.getMember() == null) return;
        if(e.getMember().getRoles().stream().anyMatch(r -> r.getName().equals("Staff"))) return;

        Message oldMessage = cachedMessages.get(e.getMessageId());
        if(oldMessage == null) return;

        new TechEmbedBuilder("Message Edited")
                .setText("Message Edited by " + e.getMember().getAsMention() + " in " + e.getMessage().getTextChannel().getAsMention() + ".")
                .addField("From", oldMessage.getContentDisplay(), false)
                .addField("To", e.getMessage().getContentDisplay(), false)
                .send(CHATLOGS_CHANNEL.query().first());

        cachedMessages.put(e.getMessageId(), e.getMessage());
    }

    @SubscribeEvent
    public void onMessageDelete(GuildMessageDeleteEvent e) {
        Message message = cachedMessages.get(e.getMessageId());
        if(message == null) return;

        if(message.getMember() == null) return;
        if(message.getMember().getRoles().stream().anyMatch(r -> r.getName().equals("Staff"))) return;

        new TechEmbedBuilder("Message Deleted")
                .setText("Message by " + message.getMember().getAsMention() + " was deleted.")
                .addField("Text Channel", e.getChannel().getAsMention(), true)
                .addField("Message", message.getContentDisplay(), true)
                .send(CHATLOGS_CHANNEL.query().first());

        cachedMessages.remove(e.getMessageId());
    }

    @Override
    public String getName() {
        return "Chat Log";
    }

    @Override
    public Requirement[] getRequirements() {
        return new Requirement[] {
                new Requirement(CHATLOGS_CHANNEL, 1, "Missing Chat Logs Channel (#chatlogs)"),
                new Requirement(STAFF_ROLE, 1, "Missing 'Staff' Role")
        };
    }
}
