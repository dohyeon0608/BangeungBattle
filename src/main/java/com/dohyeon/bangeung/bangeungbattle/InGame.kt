package com.dohyeon.bangeung.bangeungbattle

import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta

class InGame {

    private var gameInfo: MutableMap<Int, MutableMap<String, Any>> = mutableMapOf()
    private var gameCount = 0

    private lateinit var plugin: BangeungBattle

    fun setPlugin(main: BangeungBattle){
        plugin = main
        println("InGame, $plugin 으로 초기화되었습니다.")
    }

    fun toComponent(s: String): Component{
        return Component.text(s)
    }

    fun testInventory(p: Player){
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
        p1HeadMeta.owningPlayer = p
        p1HeadMeta.displayName(toComponent("${ChatColor.GOLD}1P: ${ChatColor.WHITE}${p.name}"))
        p1Head.itemMeta = p1HeadMeta

        val p2Head = ItemStack(Material.PLAYER_HEAD)
        val p2HeadMeta = p2Head.itemMeta as SkullMeta
        p2HeadMeta.owningPlayer = p
        p2HeadMeta.displayName(toComponent("${ChatColor.GOLD}2P: ${ChatColor.WHITE}${p.name}"))
        p2Head.itemMeta = p2HeadMeta

        inv.setItem(38, p1Head)
        inv.setItem(42, p2Head)

        //활
        val bow = ItemStack(Material.BOW)
        val bow_meta = bow.itemMeta

        bow_meta.displayName(toComponent("${ChatColor.GREEN}대기하세요!"))
        bow.setItemMeta(bow_meta)

        inv.setItem(20, bow)
        inv.setItem(24, bow)

        //시계
        val clockinfo = ItemStack(Material.CLOCK)
        val clockinfometa = clockinfo.itemMeta

        clockinfometa.displayName(toComponent("준비중..."))
        inv.setItem(22, ItemStack(Material.CLOCK))

        p.openInventory(inv)
    }

    fun setInventory(p1: Player, p2: Player): Inventory {
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
        val bow_meta = bow.itemMeta

        bow_meta.displayName(toComponent("${ChatColor.GREEN}대기하세요!"))
        bow.setItemMeta(bow_meta)

        inv.setItem(20, bow)
        inv.setItem(24, bow)

        //시계
        val clockinfo = ItemStack(Material.CLOCK)
        val clockinfometa = clockinfo.itemMeta

        clockinfometa.displayName(toComponent("준비중..."))
        inv.setItem(22, ItemStack(Material.CLOCK))

        return inv
    }

    private fun createGame(p1: Player, p2: Player){
        gameInfo[gameCount++] = mutableMapOf("1P" to p1.uniqueId, "2P" to p2.uniqueId, "timer" to 0, "inv" to setInventory(p1, p2))
    }

    private fun InGame(){
        val stopCode = plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, {
            for(game in gameInfo.keys){
            }
        },0L,1L)
    }
}