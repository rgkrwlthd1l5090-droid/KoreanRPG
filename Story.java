package kr.rpg;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.ThreadLocalRandom;

public class Events implements Listener {
    private final RpgPlugin pl;

    public Events(RpgPlugin pl) {
        this.pl = pl;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        PlayerData d = pl.data.get(p);
        Stats.apply(p, d);
        if (!p.hasPlayedBefore()) {
            pl.story.prologue(p);
        } else {
            p.sendMessage("§7[RPG] §e/길라잡이§7 로 이야기를 이어가 보세요. (Lv." + d.level + ")");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        pl.data.unload(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        LivingEntity v = e.getEntity();
        Player k = v.getKiller();
        if (k == null || !(v instanceof Monster)) return;
        AttributeInstance ai = v.getAttribute(Stats.MAX_HP);
        double hp = ai == null ? 20 : ai.getValue();
        Stats.giveExp(k, Math.max(5, Math.round(hp)), true);
        pl.story.onKill(k, v.getType());
        if (ThreadLocalRandom.current().nextDouble() < 0.04) {
            v.getWorld().dropItemNaturally(v.getLocation(), pl.enhance.stone(1));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        double dmg = e.getDamage();
        ThreadLocalRandom r = ThreadLocalRandom.current();

        if (e.getDamager() instanceof Player p && e.getEntity() instanceof LivingEntity) {
            PlayerData d = pl.data.get(p);
            dmg += d.st[PlayerData.STR] * 0.5;
            ItemStack hand = p.getInventory().getItemInMainHand();
            if (pl.enhance.isWeapon(hand.getType())) dmg += pl.enhance.get(hand);
            if (r.nextDouble() < Math.min(0.5, d.st[PlayerData.LUK] * 0.005)) {
                dmg *= 1.5;
                Stats.bar(p, "§c§l치명타!");
            }
        }
        if (e.getEntity() instanceof Player v) {
            PlayerData d = pl.data.get(v);
            if (r.nextDouble() < Math.min(0.3, d.st[PlayerData.DEX] * 0.003)) {
                e.setCancelled(true);
                Stats.bar(v, "§b회피!");
                return;
            }
            int sum = 0;
            for (ItemStack a : v.getInventory().getArmorContents()) {
                if (a != null && pl.enhance.isArmor(a.getType())) sum += pl.enhance.get(a);
            }
            dmg *= 1 - Math.min(0.6, sum * 0.01);
        }
        e.setDamage(dmg);
    }

    @EventHandler
    public void onGuideClick(PlayerInteractEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (!e.getRightClicked().getPersistentDataContainer().has(pl.guideKey, PersistentDataType.BYTE)) return;
        e.setCancelled(true);
        pl.gui.openGuide(e.getPlayer());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        String t = e.getView().getTitle();
        if (!(e.getWhoClicked() instanceof Player p)) return;

        if (t.equals(Gui.STAT_T)) {
            e.setCancelled(true);
            int idx = e.getRawSlot() - 11;
            if (idx < 0 || idx > 4) return;
            PlayerData d = pl.data.get(p);
            int amt = Math.min(e.isShiftClick() ? 5 : 1, d.points);
            if (amt <= 0) {
                p.sendMessage("§c남은 스탯 포인트가 없습니다. 레벨업 하면 +3씩 얻어요!");
                return;
            }
            d.points -= amt;
            d.st[idx] += amt;
            Stats.apply(p, d);
            pl.gui.fillStats(e.getInventory(), p);
        } else if (t.equals(Gui.GUIDE_T)) {
            e.setCancelled(true);
            switch (e.getRawSlot()) {
                case 11 -> { p.closeInventory(); pl.story.talk(p); }
                case 13 -> pl.gui.openStats(p);
                case 15 -> { p.closeInventory(); pl.gui.help(p); }
                default -> { }
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        String t = e.getView().getTitle();
        if (t.equals(Gui.STAT_T) || t.equals(Gui.GUIDE_T)) e.setCancelled(true);
    }
}
