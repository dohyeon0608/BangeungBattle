package com.dohyeon.bangeung.bangeungbattle

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.NotNull
import java.util.*

class BangeungBattle : JavaPlugin(), @NotNull Listener {

    private var requsetInfo: MutableMap<UUID, MutableMap<String, Any>> = mutableMapOf()
    //0번: 요청을 수락/거절을 했는가의 여부 (자동 거부) 1번: 플레이어가 요청 받은 사람들 목록

    private var requestTo: MutableMap<UUID, Boolean> = mutableMapOf() //버튼(수락/거부)을 눌렀는가의 여부, 미사용 예정
    private var requestMoklok: MutableMap<UUID, MutableList<UUID>> = mutableMapOf() //플레이어가 요청 받은 사람들 목록, 미사용 예정

    private var gamelist: MutableMap<Int, MutableMap<String, UUID>> = mutableMapOf()
    private var gametimer: MutableMap<Int, Int> = mutableMapOf()
    private var gameinv: MutableMap<Any, Any> = mutableMapOf()
    private var noCount = 0

    override fun onEnable() {
        logger.info("${ChatColor.BLUE}반응 속도 배틀 ON")
        server.pluginManager.registerEvents(this, this)

        //reload 오류 방지
        server.onlinePlayers.forEach{
            resetRequestTo(it)
            resetRequestMoklok(it)
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    private fun resetRequestTo(p: Player) {
        if (!(requestTo.containsKey(p.uniqueId))) {
            requestTo[p.uniqueId] = false
        }
    }

    private fun resetRequestMoklok(p: Player){
        if(!(requestMoklok.containsKey(p.uniqueId))){
            requestMoklok[p.uniqueId] = mutableListOf()
        }
    }

    private fun resetRequestInfo(p: Player){
        requsetInfo[p.uniqueId]!!.put("requestToPlayer", false)
        requsetInfo[p.uniqueId]!!.put("requestFromPlayers", mutableListOf<UUID>())
        requsetInfo[p.uniqueId]!!.put("clickedButton", false)
    }

    @Suppress("UNCHECKED_CAST")
    private fun battleRequset(sendp: Player, thatp: Player?){
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

            val acceptIt = TextComponent("${ChatColor.BOLD}${ChatColor.GREEN}[ 수락 ]")
            acceptIt.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("${ChatColor.GREEN}요청을 수락합니다."))
            acceptIt.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bangeungbattle accept ${sendp.name}")

            val denyIt = TextComponent("${ChatColor.BOLD}${ChatColor.RED}[ 거절 ]")
            denyIt.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("${ChatColor.RED}요청을 거절합니다."))
            denyIt.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bangeungbattle deny ${sendp.name}")

            sendp.sendMessage("${ChatColor.AQUA}${thatp.name}님에게 반응속도 배틀을 요청했습니다.")
            thatp.sendMessage("${ChatColor.AQUA}${sendp.name}님으로부터 반응속도 배틀 요청이 왔습니다.\n60초 후에 만료됩니다.\n${acceptIt}   ${denyIt}")

            autoDeny(sendp, thatp)

        } catch (e: Exception){
            sendp.sendMessage("오류 발생: $e")
        }

    }

    @Suppress("UNCHECKED_CAST")
    private fun accept(sendp: Player, thatp: Player?){

        val NOT_HAVE_ID = requestMoklok[thatp?.uniqueId]?.contains(sendp.uniqueId) == false

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
    private fun deny(sendp: Player, thatp: Player?){

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
        (requsetInfo[thatp.uniqueId]?.get("requestFromPlayers") as MutableList<UUID>).remove(sendp.uniqueId)
    }

    private fun cancle(p: Player){
        requsetInfo[p.uniqueId]!!["clickedButton"] = false
        (requsetInfo[p.uniqueId]!!["requestFromPlayers"] as MutableList<UUID>).clear()
    }

    private fun autoDeny(sendp: Player, thatp: Player){
        var t = 0
        var stopCode = 0
        stopCode = server.scheduler.scheduleSyncRepeatingTask(
            this,
            {
                t++
                if(t < 60){
                    if (requsetInfo[thatp.uniqueId]!!["clickedButton"] as Boolean) {
                        requsetInfo[thatp.uniqueId]!!["clickedButton"] = false
                        server.scheduler.cancelTask(stopCode)
                    }
                }
                else{
                    requsetInfo[thatp.uniqueId]!!["clickedButton"] = false
                    timeOverDeny(sendp, thatp)
                }
            },
        0L, 20L)
    }

    fun testInventory(p: Player): Inventory {
        try {
            val inv: Inventory = server.createInventory(null, 45, "${ChatColor.BOLD}반응속도 배틀 [${noCount}번 게임]")

            //빈칸 유리판
            val binKan = ItemStack(Material.WHITE_STAINED_GLASS_PANE)
            val binKanMeta: ItemMeta = binKan.itemMeta
            binKanMeta.setDisplayName("${ChatColor.WHITE} ")
            binKan.itemMeta = binKanMeta

            for (i in 0..8) {
                inv.setItem(i, binKan)
            }
            for (i in 1..3) {
                for (j in 9 * i until 9 * (i + 1) step 4) {
                    inv.setItem(j, binKan)
                }
            }
            for (i in 36..44) {
                inv.setItem(i, binKan)
            }

            //플레이어 머리
            val p1Head = ItemStack(Material.PLAYER_HEAD)
            val p1HeadMeta = p1Head.itemMeta as SkullMeta
            p1HeadMeta.owningPlayer = p
            p1HeadMeta.setDisplayName("${ChatColor.GOLD}1P: ${ChatColor.WHITE}${p.name}")
            p1Head.itemMeta = p1HeadMeta

            val p2Head = ItemStack(Material.PLAYER_HEAD)
            val p2HeadMeta = p2Head.itemMeta as SkullMeta
            p2HeadMeta.owningPlayer = p
            p2HeadMeta.setDisplayName("${ChatColor.GOLD}2P: ${ChatColor.WHITE}${p.name}")
            p2Head.itemMeta = p2HeadMeta

            inv.setItem(38, p1Head)
            inv.setItem(42, p2Head)

            //활
            inv.setItem(20, ItemStack(Material.BOW))
            inv.setItem(24, ItemStack(Material.BOW))

            //시계
            inv.setItem(22, ItemStack(Material.CLOCK))

            return inv
        } catch(e: Exception){
            p.sendMessage("${ChatColor.RED}오류 발생: $e")
            return p.inventory
        }
    }

    fun setInventory(p1: Player, p2: Player): Inventory {
        val inv = server.createInventory(null, 45, "${ChatColor.BOLD}반응속도 배틀")

        /* 빈칸 유리판 */
        val binKan = ItemStack(Material.WHITE_STAINED_GLASS_PANE)
        val binKanMeta: ItemMeta = binKan.itemMeta
        binKanMeta.setDisplayName("${ChatColor.WHITE} ")
        binKan.itemMeta = binKanMeta

        for (i in 0..8) {
            inv.setItem(i, binKan)
        }
        for (i in 1..3) {
            for (j in 9 * i until 9 * (i + 1) step 4) {
                inv.setItem(j, binKan)
            }
        }
        for (i in 36..44) {
            inv.setItem(i, binKan)
        }

        //플레이어 머리
        val p1Head = ItemStack(Material.PLAYER_HEAD)
        val p1HeadMeta = p1Head.itemMeta as SkullMeta
        p1HeadMeta.owningPlayer = p1
        p1HeadMeta.setDisplayName("${ChatColor.GOLD}1P: ${ChatColor.WHITE}${p1.name}")
        p1Head.itemMeta = p1HeadMeta

        val p2Head = ItemStack(Material.PLAYER_HEAD)
        val p2HeadMeta = p2Head.itemMeta as SkullMeta
        p2HeadMeta.owningPlayer = p2
        p2HeadMeta.setDisplayName("${ChatColor.GOLD}2P: ${ChatColor.WHITE}${p2.name}")
        p2Head.itemMeta = p2HeadMeta

        inv.setItem(38, p1Head)
        inv.setItem(42, p2Head)

        //활
        val bow = ItemStack(Material.BOW)
        val bow_meta = bow.itemMeta

        bow_meta.setDisplayName("${ChatColor.GREEN}대기하세요!")
        bow.setItemMeta(bow_meta)

        inv.setItem(20, bow)
        inv.setItem(24, bow)

        //시계
        val clockinfo = ItemStack(Material.CLOCK)
        val clockinfometa = clockinfo.itemMeta

        clockinfometa.setDisplayName("준비중 . . .")
        inv.setItem(22, ItemStack(Material.CLOCK))

        return inv
    }

    @EventHandler
    fun InGameInventory(e: InventoryClickEvent){

        if(e.view.title == "${ChatColor.BOLD}반응속도 배틀"){
            e.isCancelled = true
        }
    }

    @EventHandler
    fun InGameDontCloseInventory(e: InventoryCloseEvent){
        if(e.view.title == "${ChatColor.BOLD}반응속도 배틀"){
            e.player.openInventory
        }
    }

    fun InGameManager(gameNum: Int){
        var stopCode: Int = 0
        stopCode = server.scheduler.scheduleSyncRepeatingTask(this, Runnable {
            (gametimer[gameNum]!! + 1).also { gametimer[gameNum] = it }


            var P1: Player = gamelist[gameNum]?.get("1P")?.let { server.getPlayer(it) }!!
            var P2: Player = gamelist[gameNum]?.get("2P")?.let { server.getPlayer(it) }!!

            val clockinfo = ItemStack(Material.CLOCK)
            var clockinfometa = clockinfo.itemMeta

            fun setClockName(name: String){
                clockinfometa.setDisplayName("${ChatColor.YELLOW}${String}")
                clockinfo.setItemMeta(clockinfometa)
            }

            fun editClock(){
                P1.openInventory.setItem(22, clockinfo)
                P2.openInventory.setItem(22,clockinfo)
            }

            if(gameNum == 20){
                setClockName("3초 후 시작")
                editClock()
            } else if(gameNum == 40){
                setClockName("2초 후 시작")
                editClock()
            } else if(gameNum == 60){
                setClockName("1초 후 시작")
                editClock()
            } else if(gameNum in 100..199){
                setClockName("테스트")
                editClock()
            } else if(gameNum > 200){
                P1.sendMessage("종료")
                P2.sendMessage("종료")
                P1.closeInventory()
                P2.closeInventory()
                gamelist.remove(gameNum)
                gametimer.remove(gameNum)
                server.scheduler.cancelTask(stopCode)

            }
        },0L,1L)
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if(cmd.name == "bangeungbattle" || cmd.name == label){
            if(sender !is Player) sender.sendMessage("${ChatColor.RED}오직 플레이어만 명령어를 입력할 수 있습니다.")
            else{
                when(args[0]){
                    "request" -> battleRequset(sender, server.getPlayer(args[1]))
                    "accept" -> accept(sender, server.getPlayer(args[1]))
                    "deny" -> deny(sender, server.getPlayer(args[1]))
                    "cancel" -> cancle(sender)
                    "debug" -> sender.sendMessage("$requestTo \n$requestMoklok")
                    "testinv" -> sender.openInventory(testInventory(sender))
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

    @EventHandler
    fun onJoinEvent(e: PlayerJoinEvent){
        resetRequestInfo(e.player)
    }
}