//#
//# Author https://fazziclay.ru/ | https://github.com/fazziclay/
//#

package ru.fazziclay.opendiscordauth;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import ru.fazziclay.opendiscordauth.cogs.LoginManager;
import ru.fazziclay.opendiscordauth.cogs.UpdateChecker;
import ru.fazziclay.opendiscordauth.objects.Account;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static ru.fazziclay.opendiscordauth.cogs.Config.*;
import static ru.fazziclay.opendiscordauth.cogs.FileUtil.*;
import static ru.fazziclay.opendiscordauth.cogs.LoginManager.accountsJson;
import static ru.fazziclay.opendiscordauth.cogs.UpdateChecker.THIS_VERSION_NAME;
import static ru.fazziclay.opendiscordauth.cogs.UpdateChecker.THIS_VERSION_TAG;


public class Main extends JavaPlugin {
    // Переменные
    public static JDA bot;                    //Пременная бота дискорд
    public static FileConfiguration config;   //Переменная конфигурации config.yml


    @Override // Рыбка - При старте плагина
    public void onEnable() {
        getLogger().info("#########################");
        getLogger().info("## Website:§b https://github.com/fazziclay/OpenDiscordAuth/");
        getLogger().info("## Author:§b 'https://github.com/fazziclay/");
        getLogger().info("## ");
        getLogger().info("## Current version: ("+THIS_VERSION_NAME+") (#"+THIS_VERSION_TAG+")");
        getLogger().info("## ");
        getLogger().info("## §a(Starting...)");
        getLogger().info("## ");

        try {
            loadConfig();                                                           // Загрузка конфигурации
            loadBot();                                                              // Заргузка бота
            loadAccounts();                                                         // Загрузка аккаунтов
            Bukkit.getPluginManager().registerEvents(new Events(), this);     // Регистрация класса для обработки событий

            if (CONFIG_BUNGEECORD_ENABLE) {
                getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            }
        } catch (Exception e) {
            getLogger().info("## ");
            getLogger().info("## §c(Started Error)");
            getLogger().info("## ");
            getLogger().info("## ERROR: " + e.toString());
            getLogger().info("## ");
            Bukkit.getPluginManager().disablePlugins();
            return;
        }


        getLogger().info("## ");
        getLogger().info("## §a(Started!)");
        getLogger().info("## ");
        getLogger().info("#########################");

        loadUpdateChecker();
    }

    @Override // Рыбка - При выключении плагина
    public void onDisable() {
        getLogger().info("#########################");
        getLogger().info("## ");
        getLogger().info("## §c(Stopping...)");

        // Отсоеденить всех незахогиненых игроков от сервера.
        for (String i : LoginManager.noLoginList) {
            Player player = Bukkit.getPlayer(UUID.fromString(i));
            if (!(player==null) && player.isOnline()) {
                player.kickPlayer(CONFIG_MESSAGE_KICK_PLUGIN_DISABLED);
            }
        }
        getLogger().info("## Kicked all no login players.");

        // Выключит бота.
        try {
            getLogger().info("## Bot stopped.");
            bot.shutdownNow();
        } catch (Exception ignored) {}

        getLogger().info("## §c(Stopped!)");
        getLogger().info("## ");
        getLogger().info("#########################");
    }


    private void loadConfig() { // Загрузка конфига
        getConfig().options().copyDefaults(true);
        saveConfig();
        config = getConfig();
    }

    private void loadAccounts() throws JSONException { // Загрузка аккаунтов
        if (!isFile(CONFIG_ACCOUNTS_FILE_PATH)) {
            writeFile(CONFIG_ACCOUNTS_FILE_PATH, "[]");
        }
        accountsJson = new JSONArray(readFile(CONFIG_ACCOUNTS_FILE_PATH));

        int i = 0;
        while (i < accountsJson.length()) {
            String id       = accountsJson.getJSONObject(i).getString("id");
            String discord  = accountsJson.getJSONObject(i).getString("discord");
            String nickname = accountsJson.getJSONObject(i).getString("nickname");

            Account account = new Account(id, discord, nickname);
            LoginManager.accounts.add(account);

            i++;
        }
    }

    private void loadBot() { // Загрузка бота
        try {
            bot = JDABuilder.createDefault(CONFIG_BOT_TOKEN)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_PRESENCES)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .addEventListeners(new DiscordBot())
                    .build();

            bot.awaitReady();

            getLogger().info("## Bot §a'" + bot.getSelfUser().getName() + "#" + bot.getSelfUser().getDiscriminator() + "'§r started!");

        } catch (Exception e) {
            getLogger().info("##");
            getLogger().info("## §c[ERROR] " + e.toString());
            getLogger().info("##");
        }
    }

    private void loadUpdateChecker() { // Загрузка Апдейт чекера
        if (!CONFIG_UPDATE_CHECKER) {
            return;
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                UpdateChecker updateChecker = new UpdateChecker();

                if (updateChecker.isError) {
                    return;
                }

                if (updateChecker.isLast == 0) {
                    getLogger().info("## OpenDiscordAuth New Version! Update please!");
                    getLogger().info("## ");
                    getLogger().info("## Current version: (" + THIS_VERSION_NAME + ") (#" + THIS_VERSION_TAG + ")");
                    getLogger().info("## Last version:    (" + updateChecker.version_name + ") (#" + updateChecker.version_tag + ")");
                    getLogger().info("## Download page:§b " + updateChecker.version_link);
                }
            }
        }, 5 * 1000L);
    }
}
