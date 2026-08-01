package tower_climber;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import de.kostari.cloud.core.physics.AABB;
import tower_climber.Enemy.Type;
import tower_climber.TowerPlatform.Kind;

public class TowerGenerator {

    private enum LayoutPattern {
        ZIGZAG,
        SWEEP,
        CENTER_CROSS,
        EDGE_PING_PONG,
        SPIRAL,
        FAN
    }

    public record EnemyContact(Enemy enemy, boolean stomped) {
    }

    public static final long SEED = 0x5EED_C10DL;

    private static final float PLATFORM_HEIGHT = 20;
    private static final float GENERATION_AHEAD = 1_250;
    private static final float CLEANUP_BEHIND = 420;
    private static final int PATTERN_LENGTH = 6;

    private final Random random;
    private final List<TowerPlatform> platforms = new ArrayList<>();
    private final List<XpOrb> xpOrbs = new ArrayList<>();
    private final List<AetherOrb> aetherOrbs = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<EnergyVent> hazards = new ArrayList<>();
    private final float towerLeft;
    private final float towerRight;
    private final long seed;
    private final String routeCode;

    private TowerPlatform highestPlatform;
    private final TowerPlatform startingPlatform;
    private int generatedLevels;
    private float horizontalDirection;
    private float pendingConvergenceX = Float.NaN;
    private LayoutPattern layoutPattern = LayoutPattern.ZIGZAG;
    private boolean debugColliders;

    public TowerGenerator(float viewportWidth, float baseY) {
        this(viewportWidth, baseY, SEED);
    }

    public TowerGenerator(float viewportWidth, float baseY, long seed) {
        this.seed = seed;
        routeCode = createRouteCode(seed);
        random = new Random(seed);
        horizontalDirection = random.nextBoolean() ? 1 : -1;
        float sideMargin = Math.max(72, viewportWidth * 0.085f);
        towerLeft = sideMargin;
        towerRight = Math.max(towerLeft + 520, viewportWidth - sideMargin);

        float floorWidth = towerRight - towerLeft;
        startingPlatform = new TowerPlatform(
                (towerLeft + towerRight) * 0.5f,
                baseY,
                floorWidth,
                72,
                0,
                Kind.START);
        platforms.add(startingPlatform);
        highestPlatform = startingPlatform;
        generateUntil(baseY - GENERATION_AHEAD);
    }

    public static long createRunSeed() {
        return ThreadLocalRandom.current().nextLong();
    }

    public static long createRunSeed(long previousSeed) {
        int previousOpening = openingSignature(previousSeed);
        long seed;
        do {
            seed = createRunSeed();
        } while (seed == previousSeed || openingSignature(seed) == previousOpening);
        return seed;
    }

    public void update(float cameraTop, float viewportHeight, Player player) {
        generateUntil(cameraTop - GENERATION_AHEAD);
        cleanupBelow(cameraTop + viewportHeight + CLEANUP_BEHIND);
        for (Enemy enemy : enemies) {
            enemy.reactTo(player);
        }
        for (XpOrb orb : xpOrbs) {
            orb.attractTo(player);
        }
        for (AetherOrb orb : aetherOrbs) {
            orb.attractTo(player);
        }
    }

    public EnemyContact resolveEnemyContact(Player player) {
        Iterator<Enemy> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            if (!enemy.isAlive() || !enemy.overlaps(player)) {
                continue;
            }
            if (enemy.canBeStompedBy(player)) {
                defeatEnemy(enemy);
                iterator.remove();
                return new EnemyContact(enemy, true);
            }
            return new EnemyContact(enemy, false);
        }
        return null;
    }

    public Enemy resolveAttack(AABB attackBounds) {
        Iterator<Enemy> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            if (!enemy.isAlive() || !enemy.bounds().overlaps(attackBounds)) {
                continue;
            }
            defeatEnemy(enemy);
            iterator.remove();
            return enemy;
        }
        return null;
    }

    public List<Enemy> resolveAttackCone(float originX, float originY,
            float directionX, float directionY, float range, float minimumDot) {
        List<Enemy> defeated = new ArrayList<>();
        Iterator<Enemy> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            if (!enemy.isAlive()) {
                continue;
            }
            float dx = enemy.transform.position.x - originX;
            float dy = enemy.transform.position.y - originY;
            float distanceSquared = dx * dx + dy * dy;
            float extendedRange = range + Math.max(enemy.bounds().width(), enemy.bounds().height()) * 0.5f;
            if (distanceSquared > extendedRange * extendedRange) {
                continue;
            }
            float distance = Math.max(0.001f, (float) Math.sqrt(distanceSquared));
            float dot = dx / distance * directionX + dy / distance * directionY;
            if (dot < minimumDot) {
                continue;
            }
            defeatEnemy(enemy);
            iterator.remove();
            defeated.add(enemy);
        }
        return defeated;
    }

    public Enemy resolveProjectile(AABB projectileBounds) {
        return resolveAttack(projectileBounds);
    }

    public List<Enemy> defeatVisible(AABB viewportBounds) {
        List<Enemy> defeated = new ArrayList<>();
        Iterator<Enemy> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            if (!enemy.isAlive() || !viewportBounds.overlaps(enemy.bounds())) {
                continue;
            }
            defeatEnemy(enemy);
            iterator.remove();
            defeated.add(enemy);
        }
        return defeated;
    }

    public EnergyVent resolveHazardContact(Player player) {
        for (EnergyVent hazard : hazards) {
            if (hazard.overlaps(player)) {
                return hazard;
            }
        }
        return null;
    }

    public int collectXp(Player player) {
        int collected = 0;
        Iterator<XpOrb> iterator = xpOrbs.iterator();
        while (iterator.hasNext()) {
            XpOrb orb = iterator.next();
            if (orb.collectIfOverlapping(player)) {
                collected += orb.value();
                iterator.remove();
            }
        }
        return collected;
    }

    public int collectAether(Player player) {
        int collected = 0;
        Iterator<AetherOrb> iterator = aetherOrbs.iterator();
        while (iterator.hasNext()) {
            AetherOrb orb = iterator.next();
            if (orb.collectIfOverlapping(player)) {
                collected += orb.value();
                iterator.remove();
            }
        }
        return collected;
    }

    public TowerPlatform getStartingPlatform() {
        return startingPlatform;
    }

    public float getTowerLeft() {
        return towerLeft;
    }

    public float getTowerRight() {
        return towerRight;
    }

    public int getGeneratedLevels() {
        return generatedLevels;
    }

    public String routeCode() {
        return routeCode;
    }

    public long seed() {
        return seed;
    }

    public String zoneName(int platformLevel) {
        return switch (zoneIndex(platformLevel)) {
            case 1 -> "REACTOR SHAFT";
            case 2 -> "VOID FOUNDRY";
            case 3 -> "STORM CROWN";
            default -> "LOWER WORKS";
        };
    }

    public int zoneIndex(int platformLevel) {
        return Math.floorMod(Math.max(0, platformLevel) / 12, 4);
    }

    public void setDebugColliders(boolean debugColliders) {
        this.debugColliders = debugColliders;
        for (TowerPlatform platform : platforms) {
            platform.setDebugCollider(debugColliders);
        }
        for (Enemy enemy : enemies) {
            enemy.setDebugCollider(debugColliders);
        }
        for (XpOrb orb : xpOrbs) {
            orb.setDebugCollider(debugColliders);
        }
        for (EnergyVent hazard : hazards) {
            hazard.setDebugCollider(debugColliders);
        }
    }

    private void generateUntil(float targetY) {
        while (highestPlatform.transform.position.y > targetY) {
            generatePlatform();
        }
    }

    private void generatePlatform() {
        generatedLevels++;
        int difficulty = 1 + generatedLevels / 12;
        int zone = zoneIndex(generatedLevels);
        int zoneStep = Math.floorMod(generatedLevels, 12);
        float convergenceX = pendingConvergenceX;
        boolean convergenceLanding = !Float.isNaN(convergenceX);
        pendingConvergenceX = Float.NaN;
        boolean restPlatform = generatedLevels % 10 == 0;
        boolean milestonePlatform = generatedLevels % 12 == 0;
        boolean boostLanding = highestPlatform.kind() == Kind.BOOST;
        boolean boostPlatform = generatedLevels >= 9 && !restPlatform && !milestonePlatform && !boostLanding
                && ((zone == 0 && zoneStep == 9)
                        || (zone == 1 && zoneStep == 4)
                        || (zone == 2 && zoneStep == 8)
                        || (zone == 3 && zoneStep == 6));
        boolean forkPlatform = generatedLevels >= 6 && !restPlatform && !milestonePlatform
                && !boostPlatform && !boostLanding && !convergenceLanding
                && ((zone == 0 && zoneStep == 6)
                        || (zone == 1 && zoneStep == 7)
                        || (zone == 2 && zoneStep == 4)
                        || (zone == 3 && zoneStep == 10));
        boolean hazardPlatform = generatedLevels >= 15 && !restPlatform && !milestonePlatform
                && !boostPlatform && !boostLanding && !forkPlatform && !convergenceLanding
                && ((zone == 1 && (zoneStep == 3 || zoneStep == 9))
                        || (zone == 2 && zoneStep == 9)
                        || (zone == 3 && (zoneStep == 1 || zoneStep == 8))
                        || (zone == 0 && generatedLevels >= 48 && zoneStep == 11));
        boolean movingPlatform = !restPlatform && !milestonePlatform && !boostPlatform && !boostLanding
                && !forkPlatform && !convergenceLanding && !hazardPlatform
                && ((zone == 1 && (zoneStep == 2 || zoneStep == 8))
                        || (zone == 3 && zoneStep == 6));
        boolean fragilePlatform = !restPlatform && !milestonePlatform && !movingPlatform
                && !boostPlatform && !boostLanding && !forkPlatform && !convergenceLanding
                && !hazardPlatform
                && ((zone == 2 && (zoneStep == 1 || zoneStep == 7))
                        || (zone == 3 && zoneStep == 3));
        int patternStep = (generatedLevels - 1) % PATTERN_LENGTH;
        if (patternStep == 0) {
            chooseNextPattern();
        }

        float widthPenalty = Math.min(36, difficulty * 4);
        float width = milestonePlatform ? 264
                : restPlatform ? 292
                : boostPlatform ? 218
                : forkPlatform ? 148
                : 180 + random.nextFloat() * 68 - widthPenalty;
        if (movingPlatform) {
            width = Math.max(width, 205);
        } else if (fragilePlatform) {
            width = Math.max(width, 180);
        } else if (boostLanding) {
            width = Math.max(width, 232);
        } else if (convergenceLanding) {
            width = Math.max(width, 252);
        } else if (hazardPlatform) {
            width = Math.max(width, 238);
        }
        width = Math.max(146, width);
        float minGap = boostLanding ? 146
                : convergenceLanding ? 90
                : milestonePlatform ? 88 : restPlatform ? 84
                : fragilePlatform ? 88 : 90 + Math.min(14, difficulty * 1.4f);
        float maxGap = boostLanding ? 166
                : convergenceLanding ? 101
                : milestonePlatform ? 98 : restPlatform ? 94
                : fragilePlatform ? 101 : 106 + Math.min(20, difficulty * 1.8f);
        float gap = minGap + random.nextFloat() * (maxGap - minGap);
        float y = highestPlatform.transform.position.y - gap;

        float minimumX = towerLeft + width * 0.5f + 22;
        float maximumX = towerRight - width * 0.5f - 22;
        float forkCenterX = Float.NaN;
        float forkOffset = 0;
        float forkDirection = 1;
        float x;
        if (forkPlatform) {
            forkOffset = Math.min(104, (towerRight - towerLeft) * 0.18f);
            float centerMinimum = towerLeft + width * 0.5f + forkOffset + 22;
            float centerMaximum = towerRight - width * 0.5f - forkOffset - 22;
            forkCenterX = clamp(highestPlatform.transform.position.x, centerMinimum, centerMaximum);
            forkDirection = random.nextBoolean() ? 1 : -1;
            x = forkCenterX + forkDirection * forkOffset;
        } else if (convergenceLanding) {
            x = clamp(convergenceX, minimumX, maximumX);
        } else {
            x = choosePlatformX(patternStep, difficulty, minimumX, maximumX,
                    restPlatform || milestonePlatform, boostLanding);
        }
        float minimumRouteOffset = boostLanding ? 96
                : restPlatform || milestonePlatform ? 58
                : 72;
        if (movingPlatform || highestPlatform.kind() == Kind.MOVING) {
            minimumRouteOffset = Math.max(minimumRouteOffset, 104);
        }
        x = avoidVerticalStacking(x, minimumX, maximumX, minimumRouteOffset);

        Kind kind = milestonePlatform ? Kind.MILESTONE
                : restPlatform ? Kind.REST
                : movingPlatform ? Kind.MOVING
                : fragilePlatform ? Kind.FRAGILE
                : boostPlatform ? Kind.BOOST
                : Kind.NORMAL;
        TowerPlatform platform = new TowerPlatform(x, y, width, PLATFORM_HEIGHT, generatedLevels, kind);
        if (movingPlatform) {
            float availableTravel = Math.min(x - minimumX, maximumX - x);
            platform.configureHorizontalMotion(Math.min(32, Math.max(0, availableTravel - 4)),
                    0.82f + random.nextFloat() * 0.36f,
                    random.nextFloat() * (float) (Math.PI * 2));
        }
        platform.setDebugCollider(debugColliders);
        platforms.add(platform);
        highestPlatform = platform;

        populatePlatform(platform, restPlatform || boostPlatform || boostLanding
                || forkPlatform || convergenceLanding || hazardPlatform, difficulty);
        if (milestonePlatform) {
            spawnRewardOrbs(platform, 5, 3 + Math.min(3, difficulty));
        } else if (restPlatform) {
            spawnRewardOrbs(platform, 3, 3);
        } else if (boostPlatform) {
            spawnRewardOrbs(platform, 3, 2 + Math.min(2, difficulty / 2));
        } else if (forkPlatform) {
            generateForkLane(platform, forkCenterX, forkOffset, forkDirection, difficulty);
            pendingConvergenceX = forkCenterX;
        } else if (hazardPlatform) {
            generateEnergyVent(platform, zone, difficulty);
        } else if (!movingPlatform && !fragilePlatform && !boostLanding && !convergenceLanding
                && generatedLevels >= 7
                && (generatedLevels % 7 == 0 || random.nextFloat() < bonusChanceForZone(zone))) {
            generateBonusLedge(platform, difficulty);
        }
    }

    private void generateEnergyVent(TowerPlatform platform, int zone, int difficulty) {
        float side = random.nextBoolean() ? 1 : -1;
        float x = platform.transform.position.x + side * platform.width() * 0.24f;
        EnergyVent vent = new EnergyVent(platform, x, zone, difficulty, random.nextFloat());
        vent.setDebugCollider(debugColliders);
        hazards.add(vent);
    }

    private void generateForkLane(TowerPlatform mainLane, float centerX, float offset,
            float mainDirection, int difficulty) {
        float x = centerX - mainDirection * offset;
        TowerPlatform riskLane = new TowerPlatform(x, mainLane.transform.position.y - 3,
                mainLane.width(), PLATFORM_HEIGHT - 2, generatedLevels, Kind.BONUS);
        riskLane.setDebugCollider(debugColliders);
        platforms.add(riskLane);
        spawnRewardOrbs(riskLane, 4, 3 + Math.min(3, difficulty / 2));

        if (generatedLevels >= 12 && random.nextFloat() < 0.68f) {
            spawnEnemy(riskLane, difficulty, chooseEnemyType());
        }
    }

    private void chooseNextPattern() {
        int segment = (generatedLevels - 1) / PATTERN_LENGTH;
        LayoutPattern next = switch (segment) {
            case 0 -> openingPattern(random.nextFloat());
            case 1 -> layoutPattern == LayoutPattern.CENTER_CROSS
                    ? LayoutPattern.ZIGZAG
                    : LayoutPattern.CENTER_CROSS;
            default -> chooseZonePattern(zoneIndex(generatedLevels));
        };
        if (segment > 1 && next == layoutPattern) {
            next = LayoutPattern.values()[(next.ordinal() + 1) % LayoutPattern.values().length];
        }
        layoutPattern = next;
    }

    private static LayoutPattern openingPattern(float roll) {
        return roll < 0.4f ? LayoutPattern.ZIGZAG
                : roll < 0.72f ? LayoutPattern.SWEEP
                : LayoutPattern.CENTER_CROSS;
    }

    private static int openingSignature(long seed) {
        Random preview = new Random(seed);
        int direction = preview.nextBoolean() ? 1 : 0;
        return openingPattern(preview.nextFloat()).ordinal() * 2 + direction;
    }

    private LayoutPattern chooseZonePattern(int zone) {
        float roll = random.nextFloat();
        return switch (zone) {
            case 1 -> roll < 0.5f ? LayoutPattern.CENTER_CROSS
                    : roll < 0.76f ? LayoutPattern.SWEEP
                    : roll < 0.9f ? LayoutPattern.SPIRAL : LayoutPattern.ZIGZAG;
            case 2 -> roll < 0.48f ? LayoutPattern.ZIGZAG
                    : roll < 0.76f ? LayoutPattern.EDGE_PING_PONG
                    : roll < 0.9f ? LayoutPattern.FAN : LayoutPattern.CENTER_CROSS;
            case 3 -> roll < 0.5f ? LayoutPattern.EDGE_PING_PONG
                    : roll < 0.72f ? LayoutPattern.SWEEP
                    : roll < 0.9f ? LayoutPattern.FAN : LayoutPattern.CENTER_CROSS;
            default -> roll < 0.5f ? LayoutPattern.ZIGZAG
                    : roll < 0.72f ? LayoutPattern.SWEEP
                    : roll < 0.88f ? LayoutPattern.SPIRAL : LayoutPattern.CENTER_CROSS;
        };
    }

    private float bonusChanceForZone(int zone) {
        return switch (zone) {
            case 1 -> 0.13f;
            case 2 -> 0.19f;
            case 3 -> 0.16f;
            default -> 0.1f;
        };
    }

    private float choosePlatformX(int patternStep, int difficulty, float minimumX, float maximumX,
            boolean restPlatform, boolean boostLanding) {
        float center = (towerLeft + towerRight) * 0.5f;
        float range = Math.max(0, maximumX - minimumX);
        float target;
        if (restPlatform) {
            target = center + (random.nextFloat() * 2 - 1) * Math.min(48, range * 0.18f);
        } else {
            target = switch (layoutPattern) {
                case ZIGZAG -> {
                    float step = 108 + random.nextFloat() * Math.min(54, 30 + difficulty * 4);
                    float value = highestPlatform.transform.position.x + horizontalDirection * step;
                    horizontalDirection *= -1;
                    yield value;
                }
                case SWEEP -> {
                    float step = 74 + random.nextFloat() * 45;
                    float value = highestPlatform.transform.position.x + horizontalDirection * step;
                    if (value < minimumX || value > maximumX) {
                        horizontalDirection *= -1;
                        value = highestPlatform.transform.position.x + horizontalDirection * step;
                    }
                    yield value;
                }
                case CENTER_CROSS -> {
                    float offset = Math.min(166, range * 0.42f);
                    yield switch (patternStep % 4) {
                        case 1 -> center + offset;
                        case 3 -> center - offset;
                        default -> center + (random.nextFloat() * 2 - 1) * 26;
                    };
                }
                case EDGE_PING_PONG -> {
                    float offset = Math.min(184, range * 0.46f);
                    yield center + ((patternStep & 1) == 0 ? -offset : offset);
                }
                case SPIRAL -> {
                    float normalizedStep = patternStep / (float) Math.max(1, PATTERN_LENGTH - 1);
                    float offset = Math.min(188, range * 0.46f) * normalizedStep;
                    yield center + horizontalDirection * offset;
                }
                case FAN -> {
                    float inner = Math.min(92, range * 0.24f);
                    float outer = Math.min(184, range * 0.46f);
                    yield switch (patternStep) {
                        case 1 -> center + inner;
                        case 2 -> center + outer;
                        case 4 -> center - inner;
                        case 5 -> center - outer;
                        default -> center;
                    };
                }
            };
        }

        float reachableStep = boostLanding
                ? 226 + Math.min(18, difficulty * 2)
                : 166 + Math.min(18, difficulty * 2.5f);
        target = clamp(target,
                highestPlatform.transform.position.x - reachableStep,
                highestPlatform.transform.position.x + reachableStep);
        return clamp(target, minimumX, maximumX);
    }

    private float avoidVerticalStacking(float target, float minimumX, float maximumX,
            float minimumOffset) {
        float previousX = highestPlatform.transform.position.x;
        if (Math.abs(target - previousX) >= minimumOffset) {
            return target;
        }

        float leftCandidate = previousX - minimumOffset;
        float rightCandidate = previousX + minimumOffset;
        boolean leftFits = leftCandidate >= minimumX;
        boolean rightFits = rightCandidate <= maximumX;
        if (leftFits && rightFits) {
            if (target < previousX - 0.5f) {
                return leftCandidate;
            }
            if (target > previousX + 0.5f) {
                return rightCandidate;
            }
            float leftRoom = previousX - minimumX;
            float rightRoom = maximumX - previousX;
            return rightRoom >= leftRoom ? rightCandidate : leftCandidate;
        }
        if (leftFits) {
            return leftCandidate;
        }
        if (rightFits) {
            return rightCandidate;
        }
        return clamp(target, minimumX, maximumX);
    }

    private void generateBonusLedge(TowerPlatform mainPlatform, int difficulty) {
        float width = Math.max(102, 132 - difficulty * 2 + random.nextFloat() * 16);
        float freeLeft = mainPlatform.left() - towerLeft;
        float freeRight = towerRight - mainPlatform.right();
        float direction = freeRight >= freeLeft ? 1 : -1;
        float x = mainPlatform.transform.position.x
                + direction * (mainPlatform.width() * 0.5f + width * 0.5f + 28);
        float minimumX = towerLeft + width * 0.5f + 18;
        float maximumX = towerRight - width * 0.5f - 18;
        if (x < minimumX || x > maximumX) {
            return;
        }

        float y = mainPlatform.transform.position.y - 28 - random.nextFloat() * 20;
        TowerPlatform bonus = new TowerPlatform(x, y, width, PLATFORM_HEIGHT - 3,
                generatedLevels, Kind.BONUS);
        bonus.setDebugCollider(debugColliders);
        platforms.add(bonus);
        spawnRewardOrbs(bonus, 3, 2 + Math.min(2, difficulty / 2));

        if (generatedLevels >= 12 && random.nextFloat() < 0.38f) {
            spawnEnemy(bonus, difficulty, chooseEnemyType());
        }
    }

    private void spawnRewardOrbs(TowerPlatform platform, int count, int value) {
        float spacing = Math.min(32, platform.width() / Math.max(3, count + 1));
        float startX = platform.transform.position.x - spacing * (count - 1) * 0.5f;
        for (int i = 0; i < count; i++) {
            XpOrb orb = XpOrb.hovering(startX + i * spacing, platform.top() - 28,
                    value, random.nextFloat() * 10);
            orb.setDebugCollider(debugColliders);
            xpOrbs.add(orb);
        }
    }

    private void populatePlatform(TowerPlatform platform, boolean restPlatform, int difficulty) {
        if (restPlatform || generatedLevels < 4) {
            return;
        }

        float enemyChance = Math.min(0.74f,
                0.25f + difficulty * 0.05f + zoneIndex(generatedLevels) * 0.018f);
        boolean guaranteedIntroduction = generatedLevels == 5 || generatedLevels == 8 || generatedLevels == 12;
        boolean guaranteedSignature = Math.floorMod(generatedLevels, 12) == 5;
        if (!guaranteedIntroduction && !guaranteedSignature && random.nextFloat() >= enemyChance) {
            return;
        }

        Type type = guaranteedSignature ? signatureEnemy(zoneIndex(generatedLevels)) : chooseEnemyType();
        spawnEnemy(platform, difficulty, type);
    }

    private Type signatureEnemy(int zone) {
        return switch (zone) {
            case 1, 3 -> Type.FLYING;
            case 2 -> Type.EVADER;
            default -> Type.BASIC;
        };
    }

    private void spawnEnemy(TowerPlatform platform, int difficulty, Type type) {
        float range = Math.max(0, platform.width() * 0.5f - 42);
        float x = platform.transform.position.x + (random.nextFloat() * 2 - 1) * range;
        Enemy enemy = new Enemy(type, platform, x, random.nextBoolean() ? 1 : -1,
                difficulty, random.nextFloat() * 10);
        enemy.setDebugCollider(debugColliders);
        enemies.add(enemy);
    }

    private Type chooseEnemyType() {
        if (generatedLevels == 5) {
            return Type.BASIC;
        }
        if (generatedLevels == 8) {
            return Type.FLYING;
        }
        if (generatedLevels == 12) {
            return Type.EVADER;
        }
        float roll = random.nextFloat();
        return switch (zoneIndex(generatedLevels)) {
            case 1 -> roll < 0.5f ? Type.FLYING : roll < 0.72f ? Type.EVADER : Type.BASIC;
            case 2 -> roll < 0.48f ? Type.EVADER : roll < 0.68f ? Type.FLYING : Type.BASIC;
            case 3 -> roll < 0.48f ? Type.FLYING : roll < 0.82f ? Type.EVADER : Type.BASIC;
            default -> roll < 0.2f && generatedLevels >= 12 ? Type.EVADER
                    : roll < 0.44f && generatedLevels >= 8 ? Type.FLYING : Type.BASIC;
        };
    }

    private void spawnXp(Enemy enemy) {
        int totalValue = switch (enemy.type()) {
            case BASIC -> 15;
            case FLYING -> 20;
            case EVADER -> 25;
        };
        int orbCount = enemy.type() == Type.BASIC ? 3 : 4;
        for (int i = 0; i < orbCount; i++) {
            float angle = (float) (-Math.PI * 0.88 + i * Math.PI * 0.76 / Math.max(1, orbCount - 1));
            float speed = 150 + random.nextFloat() * 60;
            int value = totalValue / orbCount + (i < totalValue % orbCount ? 1 : 0);
            XpOrb orb = new XpOrb(enemy.transform.position.x, enemy.transform.position.y,
                    (float) Math.cos(angle) * speed,
                    (float) Math.sin(angle) * speed,
                    value,
                    random.nextFloat() * 10);
            orb.setDebugCollider(debugColliders);
            xpOrbs.add(orb);
        }
    }

    private void spawnAether(Enemy enemy) {
        float direction = enemy.facingDirection();
        AetherOrb orb = new AetherOrb(
                enemy.transform.position.x,
                enemy.transform.position.y - 4,
                -direction * 85,
                -190,
                1,
                random.nextFloat() * 10);
        aetherOrbs.add(orb);
    }

    private void defeatEnemy(Enemy enemy) {
        spawnXp(enemy);
        spawnAether(enemy);
        enemy.kill();
    }

    private void cleanupBelow(float cleanupY) {
        removeBelow(platforms, cleanupY);
        removeBelow(xpOrbs, cleanupY);
        removeBelow(aetherOrbs, cleanupY);
        removeBelow(enemies, cleanupY);
        removeBelow(hazards, cleanupY);
    }

    private <T extends de.kostari.cloud.core.objects.GameObject> void removeBelow(List<T> objects, float cleanupY) {
        Iterator<T> iterator = objects.iterator();
        while (iterator.hasNext()) {
            T object = iterator.next();
            if (object.transform.position.y > cleanupY) {
                object.destroy();
                iterator.remove();
            }
        }
    }

    private float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static String createRouteCode(long seed) {
        long mixed = seed ^ (seed >>> 29) ^ (seed << 17);
        return String.format("%06X", mixed & 0xFF_FFFFL);
    }
}
