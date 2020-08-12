package com.knoban.atlas.claims;

import com.knoban.atlas.commandsII.ACParsable;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EstateParser implements ACParsable<EstatePermission> {

    private static final List<String> suggestions = new ArrayList<>();

    public EstateParser() {
        for(EstatePermission ep : EstatePermission.values())
            suggestions.add(ep.name().toLowerCase());
    }

    @Nullable
    @Override
    public EstatePermission parse(@NotNull CommandSender sender, @NotNull String arg) {
        try {
            return EstatePermission.valueOf(arg.toUpperCase());
        } catch(IllegalArgumentException e) {
            return null;
        }
    }

    @Nullable
    @Override
    public String filter(@NotNull CommandSender sender, @NotNull EstatePermission parsed, @NotNull String filter) {
        return null;
    }

    @Nullable
    @Override
    public List<String> defaultSuggestions(@NotNull CommandSender sender) {
        return suggestions;
    }
}
