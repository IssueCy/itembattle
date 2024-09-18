package de.merix.itembattle;

import de.merix.itembattle.commands.StartCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class Itembattle extends JavaPlugin {

    @Override
    public void onEnable() {

        //commands
        getCommand("start").setExecutor(new StartCommand(this));

    }

    @Override
    public void onDisable() {

    }
}
