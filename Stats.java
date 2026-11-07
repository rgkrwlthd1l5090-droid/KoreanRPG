package kr.rpg;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Enhance {
    public static final int MAX = 10;
    /** +0→+1 ... +9→+10 성공 확률(%) */
    private static final int[] RATE = {100, 95, 90, 80, 70, 55, 40, 30, 20, 10};
    private static final String TAG = "§6[강화]";
    private final RpgPlugin pl;

    public Enhance(RpgPlugin pl) {
        this.pl = pl;
    }

    public boolean isWeapon(Material m) {
        String n = m.name();
        return n.endsWith("_SWORD") || n.endsWith("_AXE") || n.equals("MACE") || n.equals("TRIDENT");
    }

    public boolean isArmor(Material m) {
        String n = m.name();
        return n.endsWith("_HELMET") || n.endsWith("_CHESTPLATE") || n.endsWith("_LEGGINGS") || n.endsWith("_BOOTS");
    }

    public int get(ItemStack it) {
        if (it == null || !it.hasItemMeta()) return 0;
        Integer v = it.getItemMeta().getPersistentDataContainer().get(pl.enhKey, PersistentDataType.INTEGER);
        return v == null ? 0 : v;
    }

    public ItemStack stone(int n) {
        ItemStack it = new ItemStack(Material.AMETHYST_SHARD, n);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName("§d강화석");
        m.setLore(List.of("§7무기·방어구를 손에 들고", "§7/강화 로 사용합니다."));
        m.getPersistentDataContainer().set(pl.stoneKey, PersistentDataType.BYTE, (byte) 1);
        it.setItemMeta(m);
        return it;
    }

    private boolean isStone(ItemStack it) {
        return it != null && it.hasItemMeta()
                && it.getItemMeta().getPersistentDataContainer().has(pl.stoneKey, PersistentDataType.BYTE);
    }

    private void set(ItemStack it, int lv) {
        ItemMeta m = it.getItemMeta();
        m.getPersistentDataContainer().set(pl.enhKey, PersistentDataType.INTEGER, lv);
        List<String> lore = m.hasLore() ? new ArrayList<>(m.getLore()) : new ArrayList<>();
        lore.removeIf(s -> s.startsWith(TAG));
        if (lv > 0) lore.add(0, TAG + " +" + lv);
        m.setLore(lore);
        it.setItemMeta(m);
    }

    public void tryEnhance(Player p) {
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand.getType().isAir() || !(isWeapon(hand.getType()) || isArmor(hand.getType()))) {
            p.sendMessage("§c강화할 무기(검/도끼 등)나 방어구를 손에 들어주세요.");
            return;
        }
        int cur = get(hand);
        if (cur >= MAX) {
            p.sendMessage("§e이미 최대 강화 단계(+" + MAX + ")입니다.");
            return;
        }
        ItemStack stone = null;
        for (ItemStack it : p.getInventory().getContents()) {
            if (isStone(it)) { stone = it; break; }
        }
        if (stone == null) {
            p.sendMessage("§c강화석이 없습니다. 몬스터를 처치하면 가끔 얻을 수 있어요.");
            return;
        }
        stone.setAmount(stone.getAmount() - 1);

        PlayerData d = pl.data.get(p);
        double rate = Math.min(100, RATE[cur] + d.st[PlayerData.LUK] * 0.2);
        if (ThreadLocalRandom.current().nextDouble() * 100 < rate) {
            set(hand, cur + 1);
            p.getInventory().setItemInMainHand(hand);
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1.2f);
            p.sendMessage("§a✔ 강화 성공! §6+" + cur + " → +" + (cur + 1) + " §7(확률 " + String.format("%.1f", rate) + "%)");
            if (cur + 1 >= 7) {
                Bukkit.broadcastMessage("§6[강화] §e" + p.getName() + "§f님이 장비를 §c+" + (cur + 1) + "§f 강화에 성공했습니다!");
            }
            pl.story.onEnhance(p, cur + 1);
        } else {
            p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 0.8f);
            if (cur >= 7) {
                set(hand, cur - 1);
                p.getInventory().setItemInMainHand(hand);
                p.sendMessage("§c✘ 강화 실패... 단계가 하락했습니다. §6+" + cur + " → +" + (cur - 1));
            } else {
                p.sendMessage("§c✘ 강화 실패... 다행히 단계는 유지됩니다. §7(확률 " + String.format("%.1f", rate) + "%)");
            }
        }
    }
}
