package arena_survivor;

final class RunStats {
    int maxHealth = 100;
    float moveSpeed = 315;
    float damageMultiplier = 1;
    float attackSpeedMultiplier = 1;
    float rangeMultiplier = 1;
    float projectileSpeedMultiplier = 1;
    float armor;
    float regeneration;
    int scrap;
    int kills;

    int damage(int base) {
        return Math.max(1, Math.round(base * damageMultiplier));
    }

    int incomingDamage(int base) {
        return Math.max(1, Math.round(base * (100f / (100f + Math.max(0, armor) * 8f))));
    }
}
