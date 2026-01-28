package com.astaro.limiter;


import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.yaml.snakeyaml.scanner.ScannerException;

import java.io.File;
import java.io.IOError;
import java.util.ArrayList;
import java.util.List;

public class LimitCommands {

    private final Limiter plugin;

    public LimitCommands(Limiter plugin) {
        this.plugin = plugin;
    }

    public LiteralArgumentBuilder<CommandSourceStack> registerCommands() {
         LiteralArgumentBuilder<CommandSourceStack> command = LiteralArgumentBuilder.<CommandSourceStack>literal("limiter")
                .requires(stack -> stack.getSender().hasPermission("limiter.admin"));

        command.then(LiteralArgumentBuilder.<CommandSourceStack>literal("reload")
                .executes(ctx -> {
                    handleReload(ctx.getSource().getSender());
                    return Command.SINGLE_SUCCESS;
                }));
        command.then(LiteralArgumentBuilder.<CommandSourceStack>literal("limits")
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("main")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, Integer>argument("value", IntegerArgumentType.integer(15))
                                .executes(ctx -> {
                                    int limit = IntegerArgumentType.getInteger(ctx, "value");
                                    CommandSender sender = ctx.getSource().getSender();
                                    try {
                                        plugin.getConfig().set("main-chunk-limit", limit);
                                        plugin.saveConfig();
                                        msg(sender, "&eMain chunk limits successfully updated!");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        msg(sender, "&cError while updating main limits!");
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("neigh")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, Integer>argument("value", IntegerArgumentType.integer(5))
                                .executes(ctx -> {
                                    int limit = IntegerArgumentType.getInteger(ctx, "value");
                                    CommandSender sender = ctx.getSource().getSender();
                                    try {
                                        plugin.getConfig().set("neighbour-limit", limit);
                                        plugin.saveConfig();
                                        msg(sender, "&aNeighbour chunks limits successfully updated");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        msg(sender, "&cError while updating neighbour limits");
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
        );
        command.then(LiteralArgumentBuilder.<CommandSourceStack>literal("portals")
                .then(RequiredArgumentBuilder.<CommandSourceStack, Boolean>argument("value", BoolArgumentType.bool())
                        .executes(ctx -> {
                            boolean enabled = BoolArgumentType.getBool(ctx, "value");
                            CommandSender sender = ctx.getSource().getSender();
                            try {
                                plugin.getConfig().set("exempts.portal-enter", enabled);
                                plugin.saveConfig();
                                msg(sender,
                                        "&aPortal interactions set to " + enabled
                                );
                            } catch (Exception e) {
                                e.printStackTrace();
                                msg(sender,
                                        "&cError while changing config. Check console"
                                );
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );

        command.then(LiteralArgumentBuilder.<CommandSourceStack>literal("exempts")
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("world")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    Bukkit.getWorlds().forEach(world -> builder.suggest(world.getName()));
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    String worldName = StringArgumentType.getString(ctx, "name");
                                    CommandSender sender = ctx.getSource().getSender();

                                    List<String> worlds = new ArrayList<>(plugin.getConfig().getStringList("exempts.world"));
                                    if (worlds.contains(worldName)) {
                                        worlds.remove(worldName);
                                        msg(sender, "&eWorld &f" + worldName + " &eremoved from exempts.");
                                    } else if (Bukkit.getWorld(worldName) != null) {
                                        worlds.add(worldName);
                                        msg(sender, "&aWorld &f" + worldName + " &aadded to exempts.");
                                    } else {
                                        msg(sender, "&cWorld not found!");
                                        return 0;
                                    }

                                    plugin.getConfig().set("exempts.world", worlds);
                                    plugin.saveConfig();
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("entity")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (EntityType type : EntityType.values()) {
                                        if (type.isAlive()) builder.suggest(type.name());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    String typeInput = StringArgumentType.getString(ctx, "type").toUpperCase();
                                    CommandSender sender = ctx.getSource().getSender();

                                    try {
                                        EntityType entityType = EntityType.valueOf(typeInput);
                                        List<String> entities = new ArrayList<>(plugin.getConfig().getStringList("exempts.entity"));

                                        if (entities.contains(entityType.name())) {
                                            entities.remove(entityType.name());
                                            msg(sender, "&eEntity &f" + entityType.name() + " &eremoved from exempts.");
                                        } else {
                                            entities.add(entityType.name());
                                            msg(sender, "&aEntity &f" + entityType.name() + " &aadded to exempts.");
                                        }

                                        plugin.getConfig().set("exempts.entity", entities);
                                        plugin.saveConfig();
                                    } catch (IllegalArgumentException e) {
                                        msg(sender, "&cInvalid entity type!");
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
        );

        return command;
    }

    public void handleReload(CommandSender sender) {
        File config = new File(plugin.getDataFolder(), "config.yml");
        if (config.exists()) {
            try {
                plugin.reloadConfig();
                msg(sender, "&aSuccesfully reloaded");
            } catch (ScannerException e) {
                e.printStackTrace();
                sender.sendMessage("&cThere is a problem with your config.yml. Check console!");
            }
        } else {
            try {
                plugin.getConfig().options().copyDefaults(true);
                plugin.saveDefaultConfig();
                msg(sender, "&aYou config.yml is missing or corrupt! Creating new one!");
            } catch (IOError e) {
                msg(sender, "&cThere is a problem creating config! Check console!");
            }
        }
    }

    private void msg(CommandSender s, String text) {
        s.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(text));
    }

}

