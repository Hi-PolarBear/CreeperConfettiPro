package creeperconfetti.util;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Utilities for building clickable / hoverable chat messages and for simple
 * placeholder formatting.
 */
public final class MessageUtil {

    private MessageUtil() {
    }

    /**
     * Replaces {@code {0}}, {@code {1}}, ... placeholders with the given values.
     */
    public static String format(String message, Object... args) {
        if (message == null) {
            return null;
        }
        String result = message;
        for (int index = 0; index < args.length; index++) {
            result = result.replace("{" + index + "}", String.valueOf(args[index]));
        }
        return result;
    }

    /**
     * Builds a chat component from a legacy (§) string, attaching an optional
     * run-command click event and hover text.
     */
    public static BaseComponent component(String legacyText, String command, String hoverText) {
        BaseComponent[] parts = TextComponent.fromLegacyText(legacyText);
        ClickEvent clickEvent = command == null ? null : new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
        HoverEvent hoverEvent = hoverText == null ? null : new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hoverText));

        TextComponent combined = new TextComponent("");
        for (BaseComponent part : parts) {
            if (clickEvent != null) {
                part.setClickEvent(clickEvent);
            }
            if (hoverEvent != null) {
                part.setHoverEvent(hoverEvent);
            }
            combined.addExtra(part);
        }
        return combined;
    }

    /**
     * Builds a plain chat component from a legacy (§) string.
     */
    public static BaseComponent plain(String legacyText) {
        return component(legacyText, null, null);
    }

    /**
     * Sends a component to a player (clickable) or as plain text to the console.
     */
    public static void send(CommandSender sender, BaseComponent component) {
        if (sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(component);
        } else {
            sender.sendMessage(component.toPlainText());
        }
    }
}
