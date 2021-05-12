package com.dohyeon.bangeung.bangeungbattle

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.*

class RequestManager {

    var requsetInfo: MutableMap<UUID, MutableMap<String, Any>> = mutableMapOf()
    //0번: 요청을 수락/거절을 했는가의 여부 (자동 거부) 1번: 플레이어가 요청 받은 사람들 목록
    private lateinit var plugin: BangeungBattle

    fun setPlugin(main: BangeungBattle){
        plugin = main
        println("RequestManager, $plugin 으로 초기화되었습니다.")
    }

    fun resetRequestInfo(p: Player){
        requsetInfo[p.uniqueId] = mutableMapOf()
        requsetInfo[p.uniqueId]!!["requestToPlayer"] = false
        requsetInfo[p.uniqueId]!!["requestFromPlayers"] = mutableListOf<UUID>()
        requsetInfo[p.uniqueId]!!["clickedButton"] = false
    }

    @Suppress("UNCHECKED_CAST")
    fun battleRequset(sendp: Player, thatp: Player?){
        try {

            if (thatp == null) {
                sendp.sendMessage("${ChatColor.RED}해당 플레이어는 존재하지 않거나 오프라인 상태입니다.")
                return
            }
            if ((requsetInfo[thatp.uniqueId]!!["requestFromPlayers"] as MutableList<UUID>).contains(sendp.uniqueId)){
                sendp.sendMessage("${ChatColor.RED}이미 해당 플레이어에게 요청했습니다.")
                return
            }

            (requsetInfo[thatp.uniqueId]!!["requestFromPlayers"] as MutableList<UUID>).add(sendp.uniqueId)

            val acceptIt = TextComponent("${ChatColor.BOLD}${ChatColor.GREEN}[ 수락 ] ")
            acceptIt.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("${ChatColor.GREEN}요청을 수락합니다."))
            acceptIt.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bangeungbattle accept ${sendp.name}")

            val denyIt = TextComponent("${ChatColor.BOLD}${ChatColor.RED} [ 거절 ]")
            denyIt.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("${ChatColor.RED}요청을 거절합니다."))
            denyIt.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bangeungbattle deny ${sendp.name}")

            sendp.sendMessage("${ChatColor.AQUA}${thatp.name}님에게 반응속도 배틀을 요청했습니다.")
            thatp.sendMessage("${ChatColor.AQUA}${sendp.name}님으로부터 반응속도 배틀 요청이 왔습니다.\n60초 후에 만료됩니다.")
            thatp.sendMessage(acceptIt, denyIt)

            autoDeny(sendp, thatp)

        } catch (e: Exception){
            sendp.sendMessage("오류 발생: $e")
        }

    }

    @Suppress("UNCHECKED_CAST")
    fun accept(sendp: Player, thatp: Player?){

        try {
            if (thatp == null) {
                sendp.sendMessage("${ChatColor.RED}해당 플레이어는 존재하지 않거나 오프라인 상태입니다.")
                return
            }
            if (!((requsetInfo[thatp.uniqueId]!!["requestFromPlayers"] as MutableList<UUID>).contains(sendp.uniqueId))) {
                sendp.sendMessage("${ChatColor.RED}당신은 해당 플레이어에게 요청을 받은 적이 없습니다!")
                return
            }
            requsetInfo[thatp.uniqueId]!!["clickedButton"] = true
            (requsetInfo[thatp.uniqueId]!!["requestFromPlayers"] as MutableList<UUID>).remove(sendp.uniqueId)

            sendp.sendMessage("${thatp.name}님이 요청을 수락했습니다.")
            thatp.sendMessage("${sendp.name}님의 요청을 수락했습니다.")

        } catch(e: Exception){
            sendp.sendMessage("오류 발생: $e")
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun deny(sendp: Player, thatp: Player?){

        try {
            if (thatp == null) {
                sendp.sendMessage("${ChatColor.RED}해당 플레이어는 존재하지 않거나 오프라인 상태입니다.")
                return
            }
            if (!((requsetInfo[thatp.uniqueId]!!["requestFromPlayers"] as MutableList<UUID>).contains(sendp.uniqueId))) {
                sendp.sendMessage("${ChatColor.RED}당신은 해당 플레이어에게 요청을 받은 적이 없습니다!")
                return
            }

            requsetInfo[thatp.uniqueId]!!["clickedButton"] = true
            (requsetInfo[thatp.uniqueId]!!["requestFromPlayers"] as MutableList<UUID>).remove(sendp.uniqueId)

            sendp.sendMessage("${thatp.name}님이 요청을 거부했습니다.")
            thatp.sendMessage("${sendp.name}님의 요청을 거부했습니다.")

        } catch(e: Exception){
            sendp.sendMessage("오류 발생: $e")
        }
    }

    private fun timeOverDeny(sendp: Player, thatp: Player?){
        if(thatp == null){
            return
        }
        sendp.sendMessage("${thatp.name}님에게 간 요청이 만료되었습니다.")
        thatp.sendMessage("${sendp.name}님의 요청이 만료되었습니다.")
        (requsetInfo[thatp.uniqueId]?.get("requestFromPlayers") as MutableList<*>).remove(sendp.uniqueId)
    }

    fun cancle(p: Player){
        requsetInfo[p.uniqueId]!!["requestToPlayer"] = false
        requsetInfo[p.uniqueId]!!["clickedButton"] = false
        (requsetInfo[p.uniqueId]!!["requestFromPlayers"] as MutableList<*>).clear()
    }

    private fun autoDeny(sendp: Player, thatp: Player){
        var t = 0
        var stopCode = 0
        stopCode = plugin.server.scheduler.scheduleSyncRepeatingTask(
            plugin,
            {
                t++
                if(t < 60){
                    if (requsetInfo[thatp.uniqueId]!!["clickedButton"] as Boolean) {
                        requsetInfo[thatp.uniqueId]!!["clickedButton"] = false
                        plugin.server.scheduler.cancelTask(stopCode)
                    }
                }
                else{
                    requsetInfo[thatp.uniqueId]!!["clickedButton"] = false
                    timeOverDeny(sendp, thatp)
                }
            },
            0L, 20L)
    }
}