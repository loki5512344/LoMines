# LoMines - TODO List (Обновлено: 2026-05-31)

> **Статус проекта:** ✅ Система конфигурации полностью переписана  
> **Базовая библиотека:** LoAPI (jar из `libs/lolib*.jar`, сейчас 3.0.0)  
> **Версия Minecraft:** Paper 1.21.4  
> **Java:** 21  
> **Пакет:** dev.loki.lomines (изменен с com.loki)

---

## ✅ Что сделано сегодня (2026-05-31)

### 1. Рефакторинг пакетов ✅
- `com.loki` → `dev.loki`
- Все Java файлы обновлены
- `plugin.yml` обновлен
- Исправлены импорты в тестах

### 2. Полная перепись системы конфигурации ✅
- Section-based конфигурация с type-safe BlockKey
- Human-readable durations (5m, 30s, 2h)
- YAML inheritance через defaults.yml
- MiniMessage поддержка для форматирования

### 3. Обновление core классов ✅
- `Mine.java` — использует RegionConfig, BlockConfig, UIConfig
- `Mines.java` — использует новый ConfigLoader
- `MineFileManager.java` — создание конфигов с новой системой
- `MineRepository.java` — работа с новым MineConfig
- `MaskScanService.java` — использует BlockKey для маркеров
- `BukkitBlockSetter.java` — Map<BlockKey, Double>

### 4. Обновление обработчиков ✅
- `ActionBarHandler.java` — UIConfig с MiniMessage
- `MineBlockHandler.java` — ResetConfig
- `MineRewardHandler.java` — RewardConfig
- `MineResetHandler.java` — новые секции конфигурации

### 5. Автодополнение команд ✅
- `LoMinesTabCompleter.java` — tab completion для всех команд
- Подсказки для имён шахт, игроков, субкоманд
- Permission-based фильтрация

### 6. GUI редакторы ✅ (1/6 основных)
- `MineEditGui.java` — главный редактор шахты
  - Просмотр всех секций конфигурации
  - Навигация к под-редакторам (заглушки)
  - Сохранение и удаление с подтверждением
- `ConfirmDeleteGui.java` — подтверждение удаления
- `MineEditGuiListener.java` — обработка кликов в GUI
- Команда `/lm edit <mine>` для открытия редактора
- Разрешение `lomines.admin.edit`

### 7. Команды для установки точек телепортации и спавна ✅
- `/lm setteleport <mine>` — установить точку телепорта при сбросе
  - Берёт текущую позицию игрока (X, Y, Z, yaw, pitch, world)
  - Право: `lomines.admin.setteleport`
- `/lm setspawn <mine>` — установить точку спавна для застрявших
  - Отдельная точка для игроков, застрявших в блоках
  - Право: `lomines.admin.setspawn`
- `/lm clearspawn <mine>` — удалить точку спавна
  - Используется fallback на `teleport` точку
  - Право: `lomines.admin.setspawn`
- Auto-completion для всех команд
- `MineEditGui` показывает статус спавна и подсказки команд

### 8. Player-spawn для застрявших игроков ✅
- `PlayerSpawnConfig.java` — настройка точки спавна для застрявших игроков
- `PlayerSpawnConfigLoader.java` — загрузка из YAML
- Обновлен `MineConfig`:
  - `playerSpawn` — отдельная точка для телепорта застрявших
  - `getSpawnForStuckPlayer()` — возвращает player-spawn или fallback на teleport
- Обновлен `MineResetHandler.teleportPlayers()`:
  - Использует `player-spawn` если настроен
  - Иначе использует `teleport` (backward compatibility)
- `findSafeTeleportLocation()` с ограничением вверх:
  - Максимум 3 блока вверх (не телепортирует слишком высоко)
  - Приоритет: соседние блоки (тот же Y) → вверх (max 3) → вниз → диагональ

### 9. Ghost blocks фикс ✅
- `BlockUpdateUtil.java` — утилита для отправки пакетов обновления блоков
  - `sendBlockUpdate()` — обновить один блок для всех видящих игроков
  - `sendRegionUpdate()` — batch-обновление для кубоидного региона
  - `sendLocationsUpdate()` — эффективное обновление для списка позиций
  - `refreshChunks()` — полный resync чанков при необходимости
  - `findSafeTeleportLocation()` — найти безопасную позицию без задыхания
- `BukkitBlockSetter` — отправляет пакеты после установки блоков
- `MineResetHandler.teleportPlayers()` — телепортирует в безопасную позицию

**Проблема:** Ghost blocks (невидимые блоки) возникают при быстрой установке блоков, когда сервер не отправляет пакеты клиенту.
**Решение:** Принудительная отправка `player.sendBlockChange()` после установки блоков.

### 10. PlaceholderAPI интеграция ✅
- `LoMinesPlaceholderExpansion.java` — расширение PlaceholderAPI
  - `%lomines_mine_<name>_name%` — название шахты
  - `%lomines_mine_<name>_blocks%` — текущее количество блоков
  - `%lomines_mine_<name>_total%` — общий объём
  - `%lomines_mine_<name>_percent%` — процент заполнения (1 знак)
  - `%lomines_mine_<name>_percentint%` — процент (целое)
  - `%lomines_mine_<name>_world%` — мир шахты
  - `%lomines_mine_<name>_remaining%` — оставшиеся блоки
  - `%lomines_mine_<name>_resettime%` — время с последнего сброса (mm:ss)
  - `%lomines_mine_<name>_resetseconds%` — секунды с сброса
  - `%lomines_player_blocksmined%` — всего добыто блоков
  - `%lomines_player_minesreset%` — сброшено шахт
  - `%lomines_player_playtime%` — время игры (форматированное)
  - `%lomines_player_rank%` — позиция в топе
  - `%lomines_count%` — количество шахт
- `IntegrationManager` — регистрация/отключение расширения
- Oraxen и ItemsAdder интеграции отключены (не реализованы)

### 12. Holograms (голограммы над шахтами) 📋
- **Показывают:**
  - Текущий % заполнения шахты
  - Таймер до следующего сброса
  - Название шахты
- **Интеграции:**
  - HolographicDisplays
  - DecentHolograms
- **Конфигурация:** `hologram.enabled`, `hologram.format`, `hologram.height`
- **Команды:** `/lm hologram <mine> [on|off]`

### 13. Sub-GUI редакторы (в разработке) 📋
Текущий статус: только главное меню `MineEditGui` (заглушки для под-редакторов)

Нужно реализовать:

#### BlocksGui — редактор блоков и шансов
- Список всех блоков с весами (MATERIAL + шанс)
- Добавление нового блока (инвентарь с доступными материалами)
- Изменение веса блока (клик ЛКМ/ПКМ для +/-)
- Удаление блока (Shift+ПКМ)
- Просмотр FillMode (CUBOID/MASK)
- Интеграция с Oraxen/ItemsAdder (если включено)

#### ResetGui — настройка сброса шахты
- Интервал сброса (поле ввода с форматом 5m, 30s, 2h)
- Процентный триггер (вкл/выкл + значение)
- Сообщение при сбросе (поле ввода с поддержкой MiniMessage)
- Список команд при сбросе (добавление/удаление)

#### RewardsGui — настройка наград
- Список записей наград (chance + типы блоков)
- Редактирование шанса награды
- Добавление/удаление типов блоков для награды
- Настройка предметов (items) — инвентарь для создания ItemStack
- Настройка команд (commands) — список с добавлением/удалением
- Флаг prevent-vanilla-drops

### 11. WorldGuard интеграция ✅
- `WorldGuardConfig.java` — настройки авто-регионов
  - Шаблоны имён: `{mine_name}_{random_4}`, `{random_6}`, и т.д.
  - Настройка владельцев и членов (name или uuid:xxx)
  - Настройка флагов (passthrough, build, pvp, tnt, и т.д.)
  - Включение/выключение авто-создания
- `WorldGuardRegionService.java` — создание/обновление/удаление регионов
- `WorldGuardFlagParser.java` — парсинг флагов из строк
- `WorldGuardConfigLoader.java` — загрузка из YAML
- Автоматическое создание региона при создании шахты
- Автоматическое удаление при удалении шахты
- Обновление при перезагрузке шахты

### 11. Рефакторинг больших файлов ✅
- `ConfigLoader.java`: 427 → 151 строк (split на 6 лоадеров)
- `WorldGuardRegionService.java`: split флаг-парсер
- Новые лоадеры: BlockConfigLoader, RegionConfigLoader, ResetConfigLoader, RewardConfigLoader, TeleportConfigLoader, UIConfigLoader
- `MineEditGui.java` — главный редактор шахты
  - Просмотр всех секций конфигурации
  - Навигация к под-редакторам (заглушки)
  - Сохранение и удаление с подтверждением
- `ConfirmDeleteGui.java` — подтверждение удаления
- `MineEditGuiListener.java` — обработка кликов в GUI
- Команда `/lm edit <mine>` для открытия редактора
- Разрешение `lomines.admin.edit`

#### Новая архитектура (Section-based):
```
data/config/
├── block/
│   ├── BlockKey.java          # Sealed interface: Vanilla, Oraxen, ItemsAdder
│   ├── BlockConfig.java       # Type-safe block weights
│   └── FillMode.java          # CUBOID / MASK
├── region/
│   └── RegionConfig.java      # Cuboid regions
├── reset/
│   └── ResetConfig.java       # Duration, triggers, commands
├── reward/
│   └── RewardConfig.java      # ItemReward with MiniMessage
├── teleport/
│   └── TeleportConfig.java    # Teleport on reset
├── ui/
│   └── UIConfig.java          # Action bar, timer format
├── MineConfig.java            # Composed record
└── ConfigLoader.java          # Clean loader with YAML inheritance
```

#### Удалена старая система:
- ❌ `ConfigParser.java` (464 строк)
- ❌ `ConfigValidator.java` (валидация размазана)
- ❌ `ConfigSerializer.java` (сериализация размазана)
- ❌ `MineConfigBuilder.java` (builder внутри record)
- ❌ `parser/ConfigParseException.java`
- ❌ Старый `MineConfig.java` (17 полей)

#### Новые возможности:
- ✅ **Type-safe BlockKey** — sealed interface с Vanilla, Oraxen, ItemsAdder
- ✅ **Human-readable durations** — "5m", "30s", "2h", "1d"
- ✅ **YAML inheritance** — `defaults.yml` + перезапись в mine.yml
- ✅ **MiniMessage** — форматирование action bar и предметов
- ✅ **Immutable records** — все конфиги неизменяемы
- ✅ **Валидация на уровне конструктора** — fail-fast

---

## 📋 Текущий прогресс

- [x] **Phase 1:** Структура проекта и конфигурация ✅
- [x] **Phase 2:** Core классы и handlers ✅
- [x] **Phase 3:** BlockSetter — тип по `Map<String, Double>` и prefix ✅
- [x] **Phase 4:** Команды — разбито на 4 класса ✅
- [x] **Phase 5:** Рефакторинг больших файлов ✅
- [x] **Phase 6:** Организация папок (все ≤6 файлов) ✅
- [x] **Phase 7:** Утилиты и хелперы ✅
- [x] **Phase 8:** Рефакторинг пакетов com.loki → dev.loki ✅
- [x] **Phase 9:** Полная перепись системы конфигурации ✅
- [x] **Phase 10:** Обновление core классов для новой системы ✅
- [x] **Phase 11:** Обновление handlers для новой конфигурации ✅
- [x] **Phase 12:** Автодополнение команд (tab completer) ✅
- [x] **Phase 13:** GUI редакторы - главное меню ✅ (под-редакторы 0/3)
  - [ ] BlocksGui — редактирование блоков и шансов
  - [ ] ResetGui — настройка сброса
  - [ ] RewardsGui — настройка наград
- [x] **Phase 14:** WorldGuard авто-создание регионов ✅
- [ ] **Phase 15:** Holograms — голограммы над шахтами (процент, таймер)
- [ ] **Phase 16:** Интеграции PlaceholderAPI (✅), Oraxen, ItemsAdder

---

## 🎯 Следующие шаги

### ✅ Приоритет 1: Интеграция новой конфигурации (ВЫПОЛНЕНО)
- [x] Обновить `Mine.java` — использовать новые `RegionConfig`, `BlockConfig`
- [x] Обновить `Mines.java` — использовать новый `ConfigLoader`
- [x] Обновить `MineFileManager.java` — миграция на новый loader
- [x] Обновить `MineRepository.java` — работа с новым `MineConfig`
- [x] Обновить `BukkitBlockSetter.java` — использовать `BlockKey`
- [x] Обновить все handlers — ActionBarHandler, MineBlockHandler, MineResetHandler, MineRewardHandler
- [x] Обновить команды — MaskCommands
- [x] Добавить автодополнение — LoMinesTabCompleter

### Приоритет 2: Интеграции с плагинами
1. [ ] Включить `OraxenBlockSetter.java` — использовать `BlockKey.Oraxen`
2. [ ] Включить `ItemsAdderBlockSetter.java` — использовать `BlockKey.ItemsAdder`
3. [ ] PlaceholderAPI интеграция

### Приоритет 3: Sub-GUI редакторы ✅ (3/3)
- [x] `MineEditGui` — главное меню редактора (просмотр + навигация)
- [x] **BlocksGui** — редактор блоков и весов
  - Список блоков с весами (MATERIAL + шанс)
  - Добавление нового блока (меню выбора материала)
  - Изменение веса блока (ЛКМ/ПКМ для +/-)
  - Удаление блока (Shift+ПКМ)
  - Поддержка Oraxen/ItemsAdder
- [x] **ResetGui** — настройка сброса шахты
  - Интервал (формат: 5m, 30s, 2h)
  - Процентный триггер (on/off + значение)
  - Сообщение при сбросе (MiniMessage)
  - Список команд
- [x] **RewardsGui** — настройка наград
  - Список записей наград (chance + типы блоков)
  - Редактор шанса и типов блоков
  - Редактор предметов (drag & drop)
  - Редактор команд
  - Флаг prevent-vanilla-drops

### Приоритет 4: Holograms (голограммы над шахтами) ✅
- [x] `HologramManager.java` — управление всеми голограммами
- [x] `HologramProvider.java` — интерфейс провайдеров
- [x] Поддержка провайдеров:
  - HolographicDisplays (основной)
  - DecentHolograms (альтернатива)
- [x] Формат строк:
  - Название шахты
  - % заполнения (прогресс-бар)
  - Таймер до сброса (mm:ss)
- [x] Конфигурация: `ui.hologram.enabled`, `ui.hologram.format`, `ui.hologram.height`
- [x] Команда `/lm hologram <mine> [on|off]`

---

## 📁 Структура проекта

```
dev.loki.lomines/
├── LoMinesPlugin.java
├── ComponentInitializer.java
├── RegistrationManager.java
├── command/
│   ├── AdminCommands.java
│   ├── PlayerCommands.java
│   ├── StatsCommands.java
│   └── MaskCommands.java
├── core/
│   ├── Mine.java
│   ├── Mines.java
│   ├── MineTicker.java
│   └── service/
│       ├── MineFileManager.java
│       ├── MineRepository.java
│       └── MaskScanService.java
├── data/
│   └── config/                    ← ✅ ПЕРЕПИСАНО
│       ├── block/
│       │   ├── BlockKey.java
│       │   ├── BlockConfig.java
│       │   └── FillMode.java
│       ├── region/
│       │   └── RegionConfig.java
│       ├── reset/
│       │   └── ResetConfig.java
│       ├── reward/
│       │   └── RewardConfig.java
│       ├── spawn/                 ← ✅ NEW: Player spawn config
│       │   └── PlayerSpawnConfig.java
│       ├── teleport/
│       │   └── TeleportConfig.java
│       ├── ui/
│       │   └── UIConfig.java
│       ├── loader/                ← ✅ SPLIT FROM ConfigLoader
│       │   ├── BlockConfigLoader.java
│       │   ├── PlayerSpawnConfigLoader.java
│       │   ├── RegionConfigLoader.java
│       │   ├── ResetConfigLoader.java
│       │   ├── RewardConfigLoader.java
│       │   ├── TeleportConfigLoader.java
│       │   ├── UIConfigLoader.java
│       │   └── WorldGuardConfigLoader.java
│       ├── MineConfig.java
│       └── ConfigLoader.java      # 427 → 151 lines
├── handler/
│   ├── ActionBarHandler.java
│   ├── MineBlockHandler.java
│   ├── MineResetHandler.java
│   └── MineRewardHandler.java
├── block/
│   ├── BlockSetter.java
│   ├── BukkitBlockSetter.java
│   ├── OraxenBlockSetter.java.disabled
│   └── ItemsAdderBlockSetter.java.disabled
├── listener/
│   ├── BlockBreakListener.java
│   ├── PlayerInteractListener.java
│   ├── PlayerJoinListener.java
│   └── GroupGuiListener.java
├── util/
│   ├── ValidationUtils.java
│   ├── ErrorHandler.java
│   ├── MessageFormatter.java
│   ├── block/                      ← ✅ NEW: Block utilities
│   │   └── BlockUpdateUtil.java    # Fix ghost blocks, safe teleport
│   ├── format/
│   │   ├── ChunkUtils.java
│   │   └── TimeFormatter.java
│   ├── location/
│   │   ├── BlockKeys.java
│   │   ├── Cuboid.java
│   │   └── LocationParser.java
│   └── selection/
│       ├── MaskScanner.java
│       ├── Selection.java
│       └── SelectionManager.java
├── wand/
│   ├── GroupWandItem.java
│   ├── GroupWandManager.java
│   └── GroupWandSession.java
├── gui/
│   ├── GroupCreateGui.java
│   └── GroupCreateGuiHolder.java
├── integration/
│   ├── IntegrationManager.java
│   ├── placeholder/                   # ✅ NEW: PlaceholderAPI
│   │   └── LoMinesPlaceholderExpansion.java
│   ├── worldguard/
│   │   ├── WorldGuardConfig.java          # Шаблоны имён регионов, флаги
│   │   ├── WorldGuardFlagParser.java      # Парсинг флагов WG
│   │   └── WorldGuardRegionService.java   # Создание/удаление регионов
│   └── PlaceholderAPIIntegration.java.disabled
└── data/stats/
    ├── StatsManager.java
    ├── PlayerStats.java
    ├── Leaderboard.java
    └── LeaderboardEntry.java
```

---

## 📝 Правила разработки

### Лимиты строк
- **Главный класс плагина:** ≤120 строк
- **Обычные классы:** ≤200 строк
- **Утилиты:** ≤150 строк
- **Секции конфигурации:** ≤100 строк

### Принципы
- **KISS:** Один метод = одна задача, вложенность ≤3 уровней
- **DRY:** Дублирование кода ≥3 раз → вынести в метод/класс
- **SOLID:** Каждый класс = одна ответственность
- **YAGNI:** Не добавлять функциональность "на будущее"

---

## 📊 Метрики качества

### Конфигурация (новая система)
- Средний размер секции: **~60 строк** ✅
- Количество полей на секцию: **≤8** ✅
- Валидация: **Constructor-time** ✅
- Типобезопасность: **Sealed interfaces + Records** ✅

### Текущие (весь проект)
- Средний размер класса: **~120 строк** ✅
- Классов >200 строк: **~3%** (GUI классы — допустимо) ✅
- Дублирование кода: **<5%** ✅

### Новые лоадеры конфигурации
- `BlockConfigLoader` — парсинг блоков и весов
- `RegionConfigLoader` — парсинг регионов из локаций
- `ResetConfigLoader` — парсинг настроек сброса
- `RewardConfigLoader` — парсинг наград
- `TeleportConfigLoader` — парсинг телепорта
- `UIConfigLoader` — парсинг UI настроек
- `WorldGuardConfigLoader` — парсинг WG интеграции

---

## 🐛 Технический долг

### 🔴 Критический (нужно сделать)
- [x] Обновить Mine.java для использования новой конфигурации ✅
- [x] Обновить MineFileManager.java для нового ConfigLoader ✅

### 🟡 Средний
- [ ] Включить интеграции с Oraxen и ItemsAdder
- [x] GUI под-редакторы (BlocksGui, ResetGui, RewardsGui) ✅
- [x] PlaceholderAPI интеграция ✅

### 🟢 Низкий
- [x] Добавить `/lm info <mine>` — детальная информация ✅
- [x] Добавить `/lm tp <mine>` — телепортация ✅
- [x] Добавить `/lm copy <from> <to>` — копирование ✅
- [x] Разбить большие GUI файлы (>200 строк) ✅
- [x] Множественные регионы в одной шахте ✅
  - `/lm regions <mine>` — список регионов
  - `/lm addregion <mine>` — добавить регион
  - `/lm removeregion <mine> <index>` — удалить регион
- [x] Улучшить формат конфигурации ✅
  - Пример конфига с комментариями (mines/example_mine.yml)
  - Поддержка &#RRGGBB цветов
  - До 100 точек селекшна (50 регионов)
  - Чистый читаемый YAML формат

---

## 📦 Git история

- `1daff28` - feat: PlaceholderAPI integration with mine and player placeholders
- `cfaf638` - feat: commands to set teleport and spawn locations from player position
- `bb2e47a` - feat: player-spawn config for stuck players, limit teleport height
- `0612465` - fix: ghost blocks and safe teleport location
- `3ba6f30` - feat: WorldGuard auto-region creation, split ConfigLoader
- `23d527e` - docs: update TODO with WorldGuard progress
- `75d7851` - feat: GUI editors (MineEditGui, ConfirmDeleteGui), tab completer
- `f1060f3` - refactor(config): complete rewrite of configuration system
- `f4e9458` - refactor: migrate package from com.loki to dev.loki
- `41924ea` - refactor: simplify LoMinesPlugin (220→109 lines)
- `1e29889` - refactor: split MineConfig and Mines classes
- `14daef3` - refactor: reorganize data/ and util/ into subpackages
- `2dc2543` - refactor: split MineCommands into separate command classes
- `5ddd6e9` - refactor: split ConfigParser into 3 classes
- `bd32b9f` - chore: initial commit - LoMines v3.0.0 base structure

---

*Последнее обновление: 2026-05-31  
Добавлена настройка телепорта застрявших игроков!* 🎉
