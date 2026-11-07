package kr.rpg;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class Gui {
    public static final String STAT_T = "§8[ §6능력치 §8]";
    public static final String GUIDE_T = "§8[ §a길라잡이 루미 §8]";
    private static final Material[] ICON = {Material.IRON_SWORD, Material.FEATHER, Material.GOLDEN_APPLE,
            Material.ENCHANTED_BOOK, Material.RABBIT_FOOT};
    private static final String[] DESC = {"공격력 +0.5", "이동속도 증가 / 회피 +0.3%", "최대 체력 +2",
            "획득 경험치 +1%", "치명타 +0.5% / 강화 확률 +0.2%"};
    private final RpgPlugin pl;

    public Gui(RpgPlugin pl) {
        this.pl = pl;
    }

    static ItemStack item(Material m, String name, String... lore) {
        ItemStack it = new ItemStack(m);
        ItemMeta mt = it.getItemMeta();
        mt.setDisplayName(name);
        mt.setLore(Arrays.asList(lore));
        it.setItemMeta(mt);
        return it;
    }

    public void openStats(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, STAT_T);
        fillStats(inv, p);
        p.openInventory(inv);
    }

    public void fillStats(Inventory inv, Player p) {
        PlayerData d = pl.data.get(p);
        inv.clear();
        for (int i = 0; i < 5; i++) {
            inv.setItem(11 + i, item(ICON[i], "§e" + PlayerData.NAMES[i] + " §f" + d.st[i],
                    "§7포인트당 " + DESC[i], "", "§a클릭: +1  §7(Shift+클릭: +5)"));
        }
        inv.setItem(22, item(Material.NETHER_STAR, "§6Lv." + d.level,
                "§7경험치 " + d.exp + " / " + d.need(), "§b남은 포인트: §f" + d.points));
    }

    public void openGuide(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, GUIDE_T);
        inv.setItem(11, item(Material.WRITTEN_BOOK, "§a이야기 나누기", "§7스토리를 이어갑니다."));
        inv.setItem(13, item(Material.EXPERIENCE_BOTTLE, "§e능력치 올리기", "§7스탯 포인트를 분배합니다."));
        inv.setItem(15, item(Material.PAPER, "§b모험 안내", "§7명령어와 성장 방법을 알려드려요."));
        p.openInventory(inv);
    }

    public void help(Player p) {
        p.sendMessage("§6━━━ 모험 안내 ━━━");
        p.sendMessage("§e/스탯 §7- 레벨업으로 얻은 포인트를 힘·민첩·체력·지능·행운에 분배");
        p.sendMessage("§e/강화 §7- 손에 든 장비를 강화석으로 +10까지 강화 (+7부터는 실패 시 하락)");
        p.sendMessage("§e/퀘스트 §7- 현재 스토리 목표 확인");
        p.sendMessage("§e/레벨 §7- 내 레벨과 경험치 확인");
        p.sendMessage("§7몬스터를 처치하면 경험치와 강화석(낮은 확률)을 얻어요.");
    }
}
