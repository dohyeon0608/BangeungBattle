package com.dohyeon.bangeung.bangeungbattle

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.NotNull

class BangeungBattle : JavaPlugin(), @NotNull Listener {

    private lateinit var requestManager: RequestManager
    private lateinit var inGame: InGame

    override fun onEnable() {
        logger.info("${ChatColor.BLUE}반응 속도 배틀 ON")

        requestManager = RequestManager()
        requestManager.setPlugin(this)

        inGame = InGame()
        inGame.setPlugin(this)

        server.pluginManager.registerEvents(requestManager, this)

        //reload 오류 방지
        server.onlinePlayers.forEach {
            if(it!=null) {
                requestManager.resetRequestInfo(it)
            }
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if(cmd.name == "bangeungbattle" || cmd.name == label){
            if(sender !is Player) sender.sendMessage("${ChatColor.RED}오직 플레이어만 명령어를 입력할 수 있습니다.")
            else{
                when(args[0]){
                    "request" -> requestManager.battleRequset(sender, server.getPlayer(args[1]))
                    "accept" -> requestManager.accept(sender, server.getPlayer(args[1]))
                    "deny" -> requestManager.deny(sender, server.getPlayer(args[1]))
                    "cancel" -> requestManager.cancle(sender)
                    "debug" -> sender.sendMessage("${requestManager.requsetInfo}\n${inGame.gameInfo}")
                }
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        cmd: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String>? {
        if(cmd.name == "bangeungbattle" || cmd.name == alias){
            if(args.size == 1){
                return mutableListOf("request","accept","deny","cancel","debug","testinv")
            } else if (args.size == 2){
                val onlinePlayers: MutableList<String> = mutableListOf()
                server.onlinePlayers.forEach {
                    onlinePlayers.add(it.name)
                }
                return onlinePlayers
            }
        }
        return null
    }
}