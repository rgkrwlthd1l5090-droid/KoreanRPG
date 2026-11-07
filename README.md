package kr.rpg;

public class PlayerData {
    public static final String[] NAMES = {"힘", "민첩", "체력", "지능", "행운"};
    public static final int STR = 0, DEX = 1, VIT = 2, INT = 3, LUK = 4;

    public int level = 1, points = 0, chapter = 0, progress = 0;
    public long exp = 0;
    public boolean active = false;
    public int[] st = new int[5];

    /** 다음 레벨까지 필요한 경험치 (여기 공식을 바꾸면 성장 속도가 달라져요) */
    public long need() {
        return 40L + 8L * level * level;
    }
}
