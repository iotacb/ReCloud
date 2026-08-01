package tower_climber;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/** Per-run unlocks, equipment, upgrade cores, and cast resource. */
public final class RunProgression {

    public enum Weapon {
        BLADE("RIFT BLADE"),
        BOW("AETHER BOW"),
        SHURIKEN("STAR SHURIKEN");

        private final String title;

        Weapon(String title) {
            this.title = title;
        }

        public String title() {
            return title;
        }
    }

    public enum Upgrade {
        BOW("AETHER BOW", "LONG-RANGE PRECISION", 1),
        SHURIKEN("STAR SHURIKEN", "RAPID PIERCING VOLLEY", 1),
        DOUBLE_JUMP("DOUBLE JUMP", "ONE EXTRA AIR JUMP", 1),
        ASTRAL_NOVA("ASTRAL NOVA", "AIR CHARGE / SCREEN ERASURE", 2),
        AETHER_RESERVOIR("AETHER RESERVOIR", "+2 MAX AETHER", 1);

        private final String title;
        private final String description;
        private final int cost;

        Upgrade(String title, String description, int cost) {
            this.title = title;
            this.description = description;
            this.cost = cost;
        }

        public String title() {
            return title;
        }

        public String description() {
            return description;
        }

        public int cost() {
            return cost;
        }
    }

    private static final int NOVA_COST = 3;

    private final EnumSet<Upgrade> unlocked = EnumSet.noneOf(Upgrade.class);
    private int upgradeCores = 1;
    private int aether;
    private Weapon equippedWeapon = Weapon.BLADE;

    public boolean unlock(Upgrade upgrade) {
        if (!canUnlock(upgrade)) {
            return false;
        }
        upgradeCores -= upgrade.cost();
        unlocked.add(upgrade);
        if (upgrade == Upgrade.BOW) {
            equippedWeapon = Weapon.BOW;
        } else if (upgrade == Upgrade.SHURIKEN) {
            equippedWeapon = Weapon.SHURIKEN;
        }
        return true;
    }

    public boolean canUnlock(Upgrade upgrade) {
        return upgrade != null
                && !unlocked.contains(upgrade)
                && upgradeCores >= upgrade.cost()
                && (upgrade != Upgrade.AETHER_RESERVOIR || has(Upgrade.ASTRAL_NOVA));
    }

    public boolean has(Upgrade upgrade) {
        return unlocked.contains(upgrade);
    }

    public void addUpgradeCore() {
        upgradeCores++;
    }

    public int upgradeCores() {
        return upgradeCores;
    }

    public int addAether(int amount) {
        int before = aether;
        aether = Math.min(maxAether(), aether + Math.max(0, amount));
        return aether - before;
    }

    public boolean spendNova() {
        if (!canCastNova()) {
            return false;
        }
        aether -= NOVA_COST;
        return true;
    }

    public boolean canCastNova() {
        return has(Upgrade.ASTRAL_NOVA) && aether >= NOVA_COST;
    }

    public int aether() {
        return aether;
    }

    public int maxAether() {
        return has(Upgrade.AETHER_RESERVOIR) ? 5 : 3;
    }

    public int novaCost() {
        return NOVA_COST;
    }

    public Weapon equippedWeapon() {
        return equippedWeapon;
    }

    public boolean equip(Weapon weapon) {
        if (!isWeaponUnlocked(weapon)) {
            return false;
        }
        equippedWeapon = weapon;
        return true;
    }

    public boolean isWeaponUnlocked(Weapon weapon) {
        return switch (weapon) {
            case BLADE -> true;
            case BOW -> has(Upgrade.BOW);
            case SHURIKEN -> has(Upgrade.SHURIKEN);
        };
    }

    public Weapon cycleWeapon(int direction) {
        List<Weapon> available = new ArrayList<>();
        for (Weapon weapon : Weapon.values()) {
            if (isWeaponUnlocked(weapon)) {
                available.add(weapon);
            }
        }
        int index = available.indexOf(equippedWeapon);
        index = Math.floorMod(index + (direction >= 0 ? 1 : -1), available.size());
        equippedWeapon = available.get(index);
        return equippedWeapon;
    }
}
