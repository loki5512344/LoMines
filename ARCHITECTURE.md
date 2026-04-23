# ARCHITECTURE.md — LoMines (Java Rewrite)

> **Лицензия:** GNU General Public License v3.0  
> Исходный код распространяется на условиях GPL-3. Любые производные работы обязаны публиковаться под той же лицензией.  
> Полный текст: https://www.gnu.org/licenses/gpl-3.0.html

## Обзор

LoMines — Minecraft-плагин системы шахт, основанный на Paper API.  
Данный документ описывает архитектуру **Java-версии** (переписана с Kotlin).  
В качестве базовой библиотеки используется **LoAPI (lolib 2.0.1)**.

Плагин реализует:
- Управление шахтами (создание, удаление, сброс, конфигурация)
- Мультирегиональные шахты (до 10 `Cuboid`-регионов, по 5 пар точек)
- Быструю/стандартную установку блоков с поддержкой Oraxen и ItemsAdder
- Систему наград при добыче блоков
- Статистику игроков и лидерборды
- Интеграции с PlaceholderAPI и WorldGuard
- GUI-редактор шахт
- Визуализацию выделения частицами

---

## Зависимости

| Зависимость | Scope | Откуда / Зачем |
|---|---|---|
| `paper-api:1.21-R0.1-SNAPSHOT` | compileOnly | Paper API |
| `lolib-2.0.1.jar` | implementation (shadow) | LoAPI — базовая библиотека (scheduler, commands, gui, items, utils, config, metrics, placeholders, performance) |
| `placeholderapi:2.11.6` | compileOnly | PlaceholderAPI |
| `worldguard-bukkit:7.0.9` | compileOnly | WorldGuard |
| `oraxen:1.161.0` | compileOnly | Oraxen custom blocks |
| `api-itemsadder:3.6.1` | compileOnly | ItemsAdder custom blocks |
| `commons-math3:3.6.1` | compileOnly | `EnumeratedDistribution` — взвешенный рандом для блоков и наград |
| `commons-io:2.17.0` | compileOnly | `FileUtils.listFiles()` для сканирования папки mines/ |
| `junit-jupiter:5.11.3` | testImplementation | JUnit тесты |
| `kotest-runner-junit5:5.9.1` | testImplementation | Property-based тесты |

> LoAPI шейдится в JAR плагина. Relocate: `dev.lolib` → `com.loki.lomines.libs.lolib`

---

## Структура пакетов

```
com.loki.lomines/
│
├── LoMinesPlugin.java              # extends LoPlugin (LoAPI)
│
├── commands/
│   ├── LoMinesCommand.java         # @Command("lm") — корневая команда /lm
│   ├── admin/
│   │   ├── MineManagementCommands.java   # create, delete, redefine
│   │   ├── MineControlCommands.java      # reset, reload, list
│   │   └── DebugCommands.java            # testtime, checktime
│   ├── player/
│   │   ├── MineNavigationCommands.java   # wand, tp, settp
│   │   ├── MineEditorCommands.java       # editor
│   │   └── NotificationCommands.java     # messages, notify
│   ├── region/
│   │   └── WorldGuardCommands.java       # region create/delete/process
│   └── stats/
│       └── StatsCommands.java            # stats, top, stats reset
│
├── config/
│   └── impl/
│       ├── Config.java             # config.yml — глобальные настройки
│       ├── Messages.java           # messages.yml — локализация
│       └── MineConfig.java         # mines/<n>.yml — конфиг шахты
│
├── mines/
│   ├── Mine.java                   # Основной класс шахты
│   ├── Mines.java                  # Реестр всех шахт (static singleton)
│   ├── MineTicker.java             # Тик всех шахт — Scheduler из LoAPI
│   │
│   ├── handlers/
│   │   ├── MineBlockHandler.java       # Обработка BlockBreakEvent
│   │   ├── MineResetHandler.java       # Логика сброса (fill + уведомления)
│   │   ├── MineRewardHandler.java      # Выдача наград, парсинг конфига
│   │   ├── MineActionBarHandler.java   # ActionBar — ActionBar из LoAPI utils
│   │   └── MineConfigLoader.java       # Загрузка кубоидов, создание BlockSetter
│   │
│   ├── setter/
│   │   ├── BlockSetter.java                    # Абстрактный класс
│   │   ├── BukkitBlockSetter.java              # Стандартный Bukkit (setBlockData)
│   │   ├── OraxenBlockSetter.java              # Oraxen custom blocks
│   │   └── ItemsAdderBlockSetter.java          # ItemsAdder custom blocks
│   │
│   └── editor/
│       ├── base/
│       │   ├── BaseEditor.java             # Базовый GUI — InventoryGUI из LoAPI
│       │   ├── BasePaginatedEditor.java    # Пагинация — PagedGUI из LoAPI
│       │   └── EditorUtils.java
│       ├── MinesEditor.java
│       ├── MineEditor.java
│       ├── MineContentsEditor.java
│       ├── MineRewardsEditor.java
│       ├── MineRewardEditor.java
│       └── reward/
│           ├── RewardCommandsEditor.java
│           ├── RewardItemsEditor.java
│           └── RewardBlocksEditor.java
│
├── stats/
│   ├── PlayerStats.java            # Данные одного игрока (AtomicLong, ConcurrentHashMap)
│   ├── StatsManager.java           # Центральный менеджер статистики
│   ├── StatsStorage.java           # Чтение/запись stats.yml
│   └── Leaderboard.java            # Топы по total и по шахтам
│
├── selection/
│   ├── Selection.java              # До 5 пар точек (pos1..pos10), currentPair
│   ├── SelectionWand.java          # Инструмент выделения + частицы
│   └── SelectionPairGui.java       # GUI выбора пары — InventoryGUI из LoAPI
│
├── integrations/
│   ├── PlaceholderAPIIntegration.java   # extends PlaceholderExpansion
│   ├── WorldGuardIntegration.java
│   └── WGRegionManager.java
│
├── listener/
│   └── BlockListener.java          # BlockBreakEvent, PlayerInteractEvent, PlayerJoinEvent
│
├── converters/
│   └── CataMinesConverter.java     # Конвертация из CataMines
│
└── utils/
    ├── ChunkUtils.java             # sendMultiBlockChange / sendBlockChange по чанкам
    ├── FileUtils.java              # Пути к папке плагина, копирование ресурсов
    ├── NotificationManager.java    # Отключение уведомлений — ConcurrentHashSet + yaml
    └── TimeUtils.java              # Форматирование времени — TimeFormatter из LoAPI
```

---

## Использование LoAPI

### Главный класс — `LoPlugin`

`LoMinesPlugin` наследует `LoPlugin` вместо `JavaPlugin`.

```java
public class LoMinesPlugin extends LoPlugin {

    public static LoMinesPlugin INSTANCE;

    @Override
    protected void enable() {
        INSTANCE = this;

        loLogger().info("LoMines включается...");

        // Определение активных интеграций
        SimpleFeatureFlags features = SimpleFeatureFlags.create()
            .enable("placeholderapi", Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
            .enable("worldguard",     Bukkit.getPluginManager().getPlugin("WorldGuard") != null)
            .enable("oraxen",         Bukkit.getPluginManager().getPlugin("Oraxen") != null)
            .enable("itemsadder",     Bukkit.getPluginManager().getPlugin("ItemsAdder") != null)
            .enable("statistics",     Config.STATISTICS_ENABLED);

        // Команды через CommandManager из LoAPI
        CommandManager commands = new CommandManager(this);
        commands.registerTabCompleter("mine", ctx -> new ArrayList<>(Mines.getTypes().keySet()));
        commands.register(new LoMinesCommand());
        commands.register(new MineManagementCommands());
        commands.register(new MineControlCommands());
        commands.register(new DebugCommands());
        commands.register(new MineNavigationCommands());
        commands.register(new MineEditorCommands());
        commands.register(new NotificationCommands());
        commands.register(new WorldGuardCommands());
        commands.register(new StatsCommands());

        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);

        features.ifEnabled("placeholderapi", () -> new PlaceholderAPIIntegration().register());
        features.ifEnabled("worldguard",     this::initWorldGuard);

        // Метрики через LoAPI Metrics
        features.ifEnabled("statistics", () -> {
            Metrics metrics = new Metrics(this, 20058);
            metrics.addCustomChart(new Metrics.SimplePie("mines_count",
                () -> String.valueOf(Mines.getTypes().size())));
        });

        MineTicker.schedule(this);
        SelectionWand.startParticleTask(this);
        NotificationManager.init();
        StatsManager.load();
        scheduleStatsSave();
        reload();

        loLogger().info("LoMines v" + getDescription().getVersion() + " включен!");
    }

    @Override
    protected void disable() {
        SelectionWand.stopParticleTask();
        StatsManager.save();
        Scheduler.get(this).cancelAll();
        loLogger().info("LoMines выключен!");
    }

    @Override
    protected void dependencies(DependencyManager manager) {
        // Runtime-загрузка зависимостей которых нет в LoAPI
        manager.add("org.apache.commons", "commons-math3", "3.6.1");
        manager.add("commons-io", "commons-io", "2.17.0");
    }

    public void reload() {
        Config.reload();
        Mines.reload();
    }
}
```

---

### Scheduler

`MineTicker` и все задержанные/асинхронные операции используют `Scheduler` из LoAPI.  
Работает на Paper и Folia без изменений кода.

```java
// MineTicker.java — тик всех шахт
public class MineTicker {
    public static void schedule(Plugin plugin) {
        Scheduler.get(plugin).runTimer(() ->
            Mines.getTypes().forEach((name, mine) -> mine.tick()), 1L, 1L);
    }
}

// Частицы выделения каждые 10 тиков
Scheduler.get(plugin).runTimer(SelectionWand::tickParticles, 0L, 10L);

// Автосохранение статистики (async, не блокирует main thread)
Scheduler.get(this).runTimerAsync(() -> StatsManager.save(), intervalTicks, intervalTicks);

// Sync-коллбэк после завершения fill (вызывается из callback BlockSetter)
Scheduler.get(plugin).run(() -> {
    executeResetCommands();
    sendResetNotifications(silent);
    teleportPlayers();
    running.set(false);
});

// Обновление чанков с задержкой 2 тика
Scheduler.get(plugin).runLater(() -> ChunkUtils.refreshChunksForPlayers(cuboid), 2L);
```

---

### Команды — `CommandManager`

Все команды используют аннотации LoAPI: `@Command`, `@Subcommand`, `@Arg`, `@Cooldown`, `@PlayerOnly`, `@Permission`.

```java
@Command(value = "lm", aliases = {"lomines"})
public class LoMinesCommand {

    @Default
    public void onHelp(CommandSender sender) { /* help */ }

    @Subcommand(value = "reload", permission = "lomines.command.reload")
    public void onReload(CommandSender sender) {
        LoMinesPlugin.INSTANCE.reload();
        sender.sendMessage(Messages.RELOAD);
    }

    @Subcommand(value = "list", permission = "lomines.command.list")
    public void onList(CommandSender sender) {
        Mines.getTypes().forEach((name, mine) -> sender.sendMessage("- " + name));
    }
}

@Command(value = "lm")
public class MineControlCommands {

    @Subcommand(value = "reset", permission = "lomines.command.reset")
    public void onReset(CommandSender sender,
                        @Arg(value = "mine", completer = "mine") String mineName) {
        Mine mine = Mines.valueOf(mineName);
        mine.reset(false);
    }
}

@Command(value = "lm")
public class MineNavigationCommands {

    @Subcommand(value = "tp", permission = "lomines.command.teleport")
    @PlayerOnly
    @Cooldown(seconds = 3)
    public void onTp(Player player,
                     @Arg(value = "mine", completer = "mine") String mineName) {
        Mine mine = Mines.valueOf(mineName);
        player.teleport(Serializers.LOCATION.deserialize(mine.config.TELEPORT_LOCATION));
    }

    @Subcommand(value = "wand", permission = "lomines.command.wand")
    @PlayerOnly
    public void onWand(Player player) {
        player.getInventory().addItem(SelectionWand.getWand());
    }
}

@Command(value = "lm")
public class StatsCommands {

    @Subcommand(value = "stats", permission = "lomines.command.stats")
    @PlayerOnly
    public void onStats(Player player) {
        PlayerStats stats = StatsManager.getStats(player);
        player.sendMessage("Всего сломано: " + stats.getTotalBlocks());
    }

    @Subcommand(value = "top", permission = "lomines.command.top")
    public void onTop(CommandSender sender) {
        Leaderboard.getTopTotal(10).forEach(entry ->
            sender.sendMessage(entry.position() + ". " + entry.playerName() + " — " + entry.blocks()));
    }
}
```

---

### GUI — `InventoryGUI`

Редакторы шахт используют `InventoryGUI` из LoAPI.

```java
// BaseEditor.java
public abstract class BaseEditor {
    protected final Player player;
    protected final InventoryGUI gui;  // dev.lolib.gui.InventoryGUI

    public BaseEditor(Player player, String title, int rows) {
        this.player = player;
        this.gui = new InventoryGUI(LoMinesPlugin.INSTANCE, Component.text(title), rows);
    }

    protected void setItem(int slot, ItemStack item, Consumer<Player> onClick) {
        gui.setItem(slot, item, onClick::accept);
    }

    public void open() {
        buildItems();
        gui.open(player);
    }

    protected abstract void buildItems();
}

// MineContentsEditor.java
public class MineContentsEditor extends BaseEditor {
    public MineContentsEditor(Player player, Mine mine) {
        super(player, "Блоки: " + mine.name, 6);
    }

    @Override
    protected void buildItems() {
        setItem(49, ItemBuilder.of(Material.LIME_DYE).name("<green>Добавить").build(),
            p -> openAddBlockMenu(p));
        // ... слоты с блоками
    }
}
```

---

### ItemBuilder

```java
import dev.lolib.items.ItemBuilder;

// Кнопка сброса
ItemStack btn = ItemBuilder.of(Material.REDSTONE)
    .name("<red>Сбросить шахту")
    .lore("<gray>Заполнение: <white>" + percent + "%")
    .build();

// Заглушка
ItemStack filler = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();

// Палочка выделения
ItemStack wand = ItemBuilder.of(Material.GOLDEN_AXE)
    .name("<#00AAFF><b>Палочка выделения")
    .lore("<#00AAFF><b>ЛКМ <gray>— позиция #1")
    .lore("<#00AAFF><b>ПКМ <gray>— позиция #2")
    .lore("<#00AAFF><b>Shift+ПКМ <gray>— выбор региона")
    .persistentData(wandKey, PersistentDataType.BYTE, (byte) 0)
    .build();
```

---

### Логирование — `LoLogger`

```java
// Вместо getLogger() везде используем loLogger()
private static final LoLogger LOGGER = LoMinesPlugin.INSTANCE.loLogger();

LOGGER.info("Шахта " + name + " создана, объём: " + volume);
LOGGER.warn("Мир не найден для шахты " + name);
LOGGER.error("Не удалось создать BlockSetter", e);
if (Config.DEBUG) LOGGER.debug("Сброс занял " + took + "мс");
```

---

### Форматирование — `StringUtils` / `TimeFormatter`

```java
import dev.lolib.utils.StringUtils;
import dev.lolib.utils.TimeFormatter;

// MiniMessage строки с плейсхолдерами
String msg = StringUtils.format(mine.config.ACTION_BAR,
    "notbroken",    String.valueOf(blocks),
    "total",        String.valueOf(mine.volume),
    "percent",      String.format("%.2f", blocks / mine.volume * 100.0),
    "blocksbroken", String.format("%.2f", mine.volume - blocks),
    "time",         formatTime(remainingMs, mine.config.TIMER_FORMAT)
);

// Форматирование времени по TIMER_FORMAT
private String formatTime(long ms, int format) {
    Duration d = Duration.ofMillis(ms);
    return switch (format) {
        case 1  -> TimeFormatter.formatDigital(d);  // "01:25:35"
        case 2  -> TimeFormatter.formatShort(d);    // "1ч 25м"
        default -> TimeFormatter.formatSmart(d);    // "01ч 25м 35с"
    };
}
```

---

## Ключевые классы

### `Mine.java`

Координирует handler-ы. Никакой логики — только делегирование.

```java
public class Mine {
    public final String name;
    public final MineConfig config;
    public final List<Cuboid> cuboids;   // до 5 кубоидов

    private final MineBlockHandler blockHandler;
    private final MineResetHandler resetHandler;
    private final MineRewardHandler rewardHandler;
    private final MineActionBarHandler actionBarHandler;
    private final MineConfigLoader configLoader;

    private BlockSetter placer;
    public double volume;   // суммарный объём всех кубоидов
    public double blocks;   // текущее кол-во неломаных блоков
    private long tick;      // тик с последнего сброса

    public void tick() { ... }
    public void onBlockBreak(Player player, Block block, BlockBreakEvent event) { ... }
    public void reset(boolean silent) { ... }
    public void reload(boolean reset) { ... }
    public boolean containsLocation(Location loc) { ... }    // any cuboid
    public double getDistanceToMine(Location loc) { ... }    // min dist to any cuboid
    public World getWorld() { ... }                          // world of first cuboid
}
```

### `Mines.java`

```java
public final class Mines {
    private static final Map<String, Mine> TYPES = new HashMap<>();
    // ключ = mine.name.toLowerCase()

    public static void reload() { ... }
    public static void register(Mine mine) { ... }
    public static void unregister(Mine mine) { ... }   // удаляет + файл конфига
    public static Mine valueOf(String name) { ... }    // throws IllegalArgumentException
    public static Map<String, Mine> getTypes() { ... }
}
```

### `BlockSetter.java`

```java
public abstract class BlockSetter {
    protected final World world;
    protected final EnumeratedDistribution<?> distribution;  // commons-math3

    public abstract void fill(Cuboid cuboid, IntConsumer consumer);
    // consumer.accept(blocksPlaced) — вызывается после завершения заполнения
}
```

Выбор реализации в `MineConfigLoader.createBlockSetter()`:
- ключ в `contents` содержит `oraxen:` → `OraxenBlockSetter`
- ключ содержит `itemsadder:` → `ItemsAdderBlockSetter`
- иначе → `BukkitBlockSetter` (vanilla)

### `MineResetHandler.java`

```java
public class MineResetHandler {
    private final AtomicBoolean running = new AtomicBoolean(false);

    public void reset(boolean silent, BlockSetter placer) {
        if (!running.compareAndSet(false, true)) return; // защита от двойного сброса
        // для каждого cuboid: placer.fill(cub, callback)
        // когда ВСЕ кубоиды завершены (AtomicInteger completedCuboids):
        //   Scheduler.get(plugin).run(() -> постобработка + running.set(false))
    }
}
```

### `MineActionBarHandler.java`

Каждые 10 тиков отправляет action bar игрокам в радиусе `ACTION_BAR_RANGE²`.

```java
// Отправка через ActionBar из LoAPI utils
import dev.lolib.utils.ActionBar;

ActionBar.send(player, StringUtils.format(mine.config.ACTION_BAR,
    "notbroken", ..., "total", ..., "percent", ..., "time", ...));
```

### `PlayerStats.java`

```java
public class PlayerStats {
    public final UUID uuid;
    public String playerName;
    private final AtomicLong totalBlocks = new AtomicLong(0);
    private final ConcurrentHashMap<String, AtomicLong> mineStats = new ConcurrentHashMap<>();
    // ключ = mineName.toLowerCase()

    public void incrementTotal() { ... }
    public void incrementMine(String mineName) { ... }   // computeIfAbsent → new AtomicLong
    public long getMineStat(String mineName) { ... }
    public void resetMine(String mineName) { ... }       // вычитает из total, мин 0
    public void setTotalBlocks(long count) { ... }       // только для десериализации
    public void setMineStat(String mineName, long count) { ... }
}
```

### `Selection.java`

Хранит до 5 пар точек. Ключ в `Map<Integer, Location>` = `(pair-1)*2 + (first?1:2)`.

```
pair=1: pos[1], pos[2]
pair=2: pos[3], pos[4]
...
pair=5: pos[9], pos[10]
currentPair — активная пара (переключается через SelectionPairGui)
```

---

## Поток данных при разрушении блока

```
BlockBreakEvent (MONITOR, ignoreCancelled=true)
    └── BlockListener.onBlockBreakEvent()
            └── for mine : Mines.getTypes().values()
                    if mine.containsLocation(block.location):
                        ├── mine.onBlockBreak(player, block, event)
                        │       ├── MineBlockHandler: blocks--
                        │       ├── MineRewardHandler.getRandomRewards(block)
                        │       │       └── rand <= chance/100 → reward.execute(player)
                        │       │           ├── dispatchCommand (от консоли)
                        │       │           └── player.inventory.addItem(items)
                        │       └── if blocks/volume*100 <= RESET_PERCENT
                        │           && Config.RESET_ON_PERCENT_ENABLED → mine.reset()
                        │
                        └── StatsManager.incrementStats(player, mine)
                                └── playerStats.incrementTotal() + incrementMine(mine.name)
```

---

## Поток сброса шахты

```
mine.reset(silent)
    ├── tick = 0
    ├── actionBarHandler.reset()
    ├── if resetHandler.isRunning() → return  (защита от двойного сброса)
    ├── mine.blocks = mine.volume              (немедленно, до завершения fill)
    └── resetHandler.reset(silent, placer)
            ├── running.compareAndSet(false, true)
            ├── for each cuboid:
            │       placer.fill(cuboid, placed -> {
            │           if (++completed >= total):
            │               Scheduler.get(plugin).run(() -> {
            │                   executeResetCommands()
            │                   sendResetNotifications(silent)
            │                   teleportPlayers()
            │                   running.set(false)
            │               })
            │       })
            └── после fill → ChunkUtils.refreshChunksDelayed(cuboid, 2L)
```

---

## Поток загрузки плагина

```
LoMinesPlugin.enable()  [extends LoPlugin]
    ├── LoPlugin.onLoad() → dependencies(manager)   // commons-math3, commons-io
    ├── loLogger().info(...)
    ├── SimpleFeatureFlags → определение активных интеграций
    ├── CommandManager(this)
    │       .registerTabCompleter("mine", ...)
    │       .register(9 классов команд)
    ├── Bukkit.registerEvents(new BlockListener(), this)
    ├── features.ifEnabled("placeholderapi") → register()
    ├── features.ifEnabled("statistics") → new Metrics(this, 20058)
    ├── MineTicker.schedule(this)             // Scheduler.runTimer(1L, 1L)
    ├── SelectionWand.startParticleTask(this)  // Scheduler.runTimer(0L, 10L)
    ├── NotificationManager.init()
    ├── StatsManager.load()
    ├── scheduleStatsSave()                   // Scheduler.runTimerAsync(...)
    └── reload()
            ├── Config.reload()
            └── Mines.reload()
```

---

## Жизненный цикл Mine-объекта

```
new Mine(file, reset=true)
    ├── Mines.register(this)
    └── reload(reset=true)
            ├── config.reload()
            ├── cuboids = configLoader.loadCuboids()
            ├── volume  = configLoader.calculateVolume(cuboids)
            ├── blocks  = volume
            ├── rewardHandler.loadRewards()
            ├── placer  = configLoader.createBlockSetter(world)
            └── if reset: mine.reset()

Mine.tick() [каждый тик]
    ├── tick++
    ├── if tick >= RESET_TICKS → reset(false); tick = 0
    └── actionBarHandler.tick(tick, blocks)

Mines.unregister(mine)
    ├── TYPES.remove(mine.name)
    └── mine.config.file.delete()
```

---

## Конфигурация шахты (MineConfig)

Файл: `plugins/LoMines/mines/<n>.yml`

| Поле | Ключ в yaml | Тип | Описание |
|---|---|---|---|
| `DISPLAY_NAME` | `display-name` | String | MiniMessage имя |
| `CONTENTS` | `contents` | Map | блок → вес (нормализует EnumeratedDistribution) |
| `SELECTION_CORNER_1..10` | `selection.1..10` | String | `"world;x;y;z;yaw;pitch"` |
| `RESET_TICKS` | `reset.ticks` | long | тики до автосброса (20 = 1 сек) |
| `RESET_PERCENT` | `reset.percent` | double | % для автосброса по проценту |
| `BROADCAST_RESET` | `broadcast-reset` | int | -2=откл, -1=все, 0=мир, N=радиус |
| `TELEPORT_ON_RESET` | `teleport-on-reset` | int | 0=верхний блок, 1=settp, иное=откл |
| `TELEPORT_LOCATION` | `teleport-location` | String | `"world;x;y;z;yaw;pitch"` |
| `ACTION_BAR_ENABLED` | `actionbar.enabled` | boolean | показывать action bar |
| `ACTION_BAR_RANGE` | `actionbar.range` | int | радиус (проверяется как range²) |
| `TIMER_FORMAT` | `timer-format` | int | 1=digital, 2=short, 3=full |
| ~~`SETTER`~~ | ~~`setter`~~ | ~~String~~ | ~~Удалено (только Bukkit API)~~ |
| `RANDOM_REWARDS` | `random-rewards` | List\<Map\> | список наград |
| `RESET_COMMANDS` | `reset-commands` | List\<String\> | команды после сброса |

### Структура блока награды

```yaml
- chance: 0.01
  prevent-drops: false
  blocks:
    - "diamond_ore"
  items:
    - type: diamond
      amount: 1
      name: "<gold>Редкий алмаз"
  commands:
    - "eco give <player> 500"
```

---

## Интеграции

### PlaceholderAPI

| Плейсхолдер | Возвращает |
|---|---|
| `%lomines_percent_<mine>%` | `blocks/volume*100` (2 знака) |
| `%lomines_notbroken_<mine>%` | `mine.blocks` |
| `%lomines_blocksbroken_<mine>%` | `volume - blocks` |
| `%lomines_total_<mine>%` | `mine.volume` |
| `%lomines_resettime_<mine>%` | время до сброса |
| `%lomines_stats_total%` | `playerStats.totalBlocks` |
| `%lomines_stats_<mine>%` | `playerStats.getMineStat(mine)` |
| `%lomines_top_<pos>_name%` | имя в общем топе |
| `%lomines_top_<pos>_count%` | блоков в общем топе |
| `%lomines_top_<mine>_<pos>_name%` | имя в топе по шахте |
| `%lomines_top_<mine>_<pos>_count%` | блоков в топе по шахте |

### WorldGuard

`WGRegionManager` создаёт/удаляет WG-регион при операциях с шахтой.  
Настройки: `Config.WORLDGUARD_ENABLED`, `WORLDGUARD_AUTO_CREATE_REGIONS`, `WORLDGUARD_REGION_PRIORITY`.

---

## Система статистики

```
StatsManager
├── ConcurrentHashMap<UUID, PlayerStats> stats
├── load()     ← enable()
├── save()     ← по таймеру Scheduler.runTimerAsync + disable()
└── incrementStats(Player, Mine)
        ├── if !Config.STATISTICS_ENABLED → return
        └── stats.computeIfAbsent(uuid) → incrementTotal() + incrementMine(mine.name)

Leaderboard (stateless)
├── getTopTotal(limit)        → сортировка по totalBlocks desc
├── getTopByMine(mine, limit) → фильтр + сортировка по getMineStat(mine) desc
├── getPosition(uuid)         → 1-based, -1 если нет
└── getPositionByMine(uuid, mine)
```

---

## Принципы

**SOLID:**
- `Mine` — координатор без логики, делегирует в handler-ы (SRP)
- `BlockSetter` — новые реализации добавляются наследованием (OCP)
- Все `BlockSetter` взаимозаменяемы в `MineResetHandler` (LSP)
- Handler-классы зависят только от нужного (ISP)
- `Mine` зависит от абстракции `BlockSetter`, не от конкретного NMS (DIP)

**Ограничения на код:**
- Максимум 200 строк на файл (кроме сгенерированных/библиотечных)
- Асинхронность — только через `Scheduler.get(plugin)`, никогда `new Thread()`
- Bukkit API — только в main thread (кроме async-save статистики)
- Изменяемое static состояние — только в `Mines.TYPES` и `StatsManager.stats`

---

## Известные проблемы

| # | Проблема | Файл | Приоритет |
|---|---|---|---|
| 1 | `Leaderboard.getTopTotal()` — O(n log n) без кэша | `Leaderboard.java` | средний |
| 2 | `mine.blocks = mine.volume` выставляется до завершения fill — ложный процент-reset во время сброса | `Mine.java` | средний |
| 3 | `MineResetHandler.reset()` принимает `BlockSetter` параметром — лишняя связность | `MineResetHandler.java` | низкий |
| 4 | Нет тестов на `MineResetHandler`, `MineBlockHandler`, `BlockListener`, `Mines` | `test/` | высокий |
| 5 | `ChunkUtils` определяет Paper через `Class.forName` — хрупко | `ChunkUtils.java` | низкий |
| 6 | `SelectionWand.drawLine` не ограничивает частицы по Y — спам для высоких шахт | `SelectionWand.java` | низкий |
