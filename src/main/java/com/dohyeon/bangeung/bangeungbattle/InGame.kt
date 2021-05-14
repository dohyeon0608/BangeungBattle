package com.dohyeon.bangeung.bangeungbattle

import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import org.jetbrains.annotations.NotNull

import java.util.*

class InGame: Listener {

    var gameInfo: MutableMap<Int, MutableMap<String, Any>> = mutableMapOf()
    var playerInfo: MutableMap<UUID, MutableList<Any>> = mutableMapOf()
    private var gameCount = 0

    private lateinit var plugin: BangeungBattle

    fun setPlugin(main: BangeungBattle){
        plugin = main
    }

    private fun toComponent(s: String): Component{
        return Component.text(s)
    }

    private fun setInventory(p1: Player, p2: Player): Inventory {
        val inv = plugin.server.createInventory(null, 45, toComponent("${ChatColor.BOLD}반응속도 배틀"))

        /* 빈칸 유리판 */
        val binKan = ItemStack(Material.WHITE_STAINED_GLASS_PANE)
        val binKanMeta: ItemMeta = binKan.itemMeta
        binKanMeta.displayName(toComponent("${ChatColor.WHITE} "))
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
        p1HeadMeta.displayName(toComponent("${ChatColor.GOLD}1P: ${ChatColor.WHITE}${p1.name}"))
        p1Head.itemMeta = p1HeadMeta

        val p2Head = ItemStack(Material.PLAYER_HEAD)
        val p2HeadMeta = p2Head.itemMeta as SkullMeta
        p2HeadMeta.owningPlayer = p2
        p2HeadMeta.displayName(toComponent("${ChatColor.GOLD}2P: ${ChatColor.WHITE}${p2.name}"))
        p2Head.itemMeta = p2HeadMeta

        inv.setItem(38, p1Head)
        inv.setItem(42, p2Head)

        //활
        val bow = ItemStack(Material.BOW)
        val bowMeta = bow.itemMeta

        bowMeta.displayName(toComponent("${ChatColor.GREEN}베리어가 검으로 바뀔 때 클릭하세요!"))
        bow.itemMeta = bowMeta

        inv.setItem(20, bow)
        inv.setItem(24, bow)

        //베리어
        val barrier = ItemStack(Material.BARRIER)
        val barrierMeta = barrier.itemMeta
        barrierMeta.displayName(toComponent("${ChatColor.RED}준비하세요!"))
        barrier.itemMeta = barrierMeta

        inv.setItem(22, barrier)

        return inv
    }

    fun createGame(p1: Player, p2: Player){
        val random = Random()
        val stopTime = 20 + random.nextInt(400)

        gameInfo[gameCount] = mutableMapOf("1P" to p1.uniqueId, "2P" to p2.uniqueId, "timer" to 0, "inv" to setInventory(p1, p2), "stop" to stopTime)

        playerInfo[p1.uniqueId] = mutableListOf(-1, false, false)
        playerInfo[p2.uniqueId] = mutableListOf(-1, false, false)
        //0 = gameCount , 1 = cilcked? , 2 = quitGame

        playerInfo[p1.uniqueId]!![0] = gameCount
        playerInfo[p2.uniqueId]!![0] = gameCount

        p1.openInventory(gameInfo[gameCount]!!["inv"] as @NotNull Inventory)
        p2.openInventory(gameInfo[gameCount]!!["inv"] as @NotNull Inventory)

        inGameManager(gameCount)
        gameCount++
    }

    private fun inGameManager(gameNumber: Int){
        var timer: Int
        var p1: Player?
        var p2: Player?
        var p1Click: Boolean
        var p2Click: Boolean
        var p1Quit: Boolean
        var p2Quit: Boolean
        var inv: Inventory
        var stopCode = 0
        val stopTime = gameInfo[gameNumber]!!["stop"] as Int

        var winMessage: String
        var loseMessage: String
        var drawMessage: String

        stopCode = plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, {
            p1 = plugin.server.getPlayer(gameInfo[gameNumber]?.get("1P") as @NotNull UUID)
            p2 = plugin.server.getPlayer(gameInfo[gameNumber]?.get("2P") as @NotNull UUID)

            p1Click = playerInfo[p1?.uniqueId]?.get(1) as Boolean? ?: true
            p2Click = playerInfo[p2?.uniqueId]?.get(1) as Boolean? ?: true

            p1Quit = playerInfo[p1?.uniqueId]?.get(2) as Boolean? ?: true
            p2Quit = playerInfo[p1?.uniqueId]?.get(2) as Boolean? ?: true

            inv = gameInfo[gameNumber]?.get("inv") as Inventory

            p1?.updateInventory()
            p2?.updateInventory()

            winMessage = "${ChatColor.AQUA}${p1?.name} vs ${p2?.name}:\n승리하였습니다."
            loseMessage = "${ChatColor.RED}${p1?.name} vs ${p2?.name}:\n패배하였습니다."
            drawMessage = "${ChatColor.GRAY}${p1?.name} vs ${p2?.name}:\n무승부입니다."

            fun stop() {
                playerInfo.remove(p1?.uniqueId)
                playerInfo.remove(p2?.uniqueId)
                p1?.performCommand("bb cancel")
                p2?.performCommand("bb cancel")
                plugin.server.scheduler.cancelTask(stopCode)
            }

            timer = gameInfo[gameNumber]?.get("timer") as Int
            gameInfo[gameNumber]?.set("timer", timer+1)

            p1?.updateInventory()
            p2?.updateInventory()

            if (p1?.openInventory?.topInventory != inv) {
                p1?.openInventory(inv)
            }
            if (p2?.openInventory?.topInventory != inv) {
                p2?.openInventory(inv)
            }

            if (timer > stopTime) {
                val sword = ItemStack(Material.DIAMOND_SWORD)
                val swordMeta = sword.itemMeta
                swordMeta.displayName(toComponent("${ChatColor.RED}쏘세요!!"))
                sword.itemMeta = swordMeta
                (gameInfo[gameNumber]?.get("inv") as Inventory).setItem(22, sword)
            }

            if (p1Click && p2Click) {
                (gameInfo[gameNumber]?.get("inv") as Inventory).setItem(20, ItemStack(Material.BONE))
                (gameInfo[gameNumber]?.get("inv") as Inventory).setItem(24, ItemStack(Material.BONE))
                p1?.sendMessage(drawMessage)
                p2?.sendMessage(drawMessage)
                p1?.closeInventory()
                p2?.closeInventory()
                stop()
            }
            if (p1Click || p2Quit) {
                if(p2==null) p2?.sendMessage("${ChatColor.RED}상대방이 게임을 나가서 부전승 처리 되었습니다.")
                (gameInfo[gameNumber]?.get("inv") as Inventory).setItem(24, ItemStack(Material.BONE))
                p1?.sendMessage(winMessage)
                p2?.sendMessage(loseMessage)
                p1?.closeInventory()
                p2?.closeInventory()
                stop()
            }
            if (p2Click || p1Quit) {
                if(p2==null) p2?.sendMessage("${ChatColor.RED}상대방이 게임을 나가서 부전승 처리 되었습니다.")
                (gameInfo[gameNumber]?.get("inv") as Inventory).setItem(20, ItemStack(Material.BONE))
                p2?.sendMessage(winMessage)
                p1?.sendMessage(loseMessage)
                p1?.closeInventory()
                p2?.closeInventory()
                stop()
            }
        },0L,1L)
    }

    private fun clickBow(p: Player){
        when(p.uniqueId){
            gameInfo[playerInfo[p.uniqueId]!![0]]!!["1P"] -> {
                playerInfo[p.uniqueId]!![1] = true
            }
            gameInfo[playerInfo[p.uniqueId]!![0]]!!["2P"] -> {
                playerInfo[p.uniqueId]!![1] = true
            }
        }
    }

    @EventHandler
    fun dontClickItem(e: InventoryClickEvent){
        val p = (e.whoClicked as Player)
        if(playerInfo.containsKey(p.uniqueId)){
            val stopTime = gameInfo[playerInfo[p.uniqueId]!![0]]!!["stop"] as Int
            if(gameInfo[playerInfo[p.uniqueId]!![0]]!!["timer"] as Int > stopTime){
                if(e.currentItem?.type == Material.BOW){
                    clickBow(p)
                }
            }
            e.isCancelled = true
        }
    }

    @EventHandler
    fun dontQuitGame(e: PlayerQuitEvent){
        if(playerInfo.containsKey(e.player.uniqueId)){
            playerInfo[e.player.uniqueId]!![2] = true
        }
    }
}