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
- [ ] **Phase 13:** GUI редакторы (0/5)
- [ ] **Phase 14:** Интеграции PlaceholderAPI, Oraxen, ItemsAdder

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

### Приоритет 3: GUI редакторы
1. [ ] `MineEditGui` — редактирование шахты
2. [ ] `BlockConfigGui` — настройка блоков
3. [ ] `RewardConfigGui` — настройка наград
4. [ ] `ResetConfigGui` — настройка таймеров
5. [ ] `TeleportConfigGui` — настройка телепорта

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
│       ├── teleport/
│       │   └── TeleportConfig.java
│       ├── ui/
│       │   └── UIConfig.java
│       ├── MineConfig.java
│       └── ConfigLoader.java
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
- Средний размер класса: **~150 строк** ✅
- Классов >200 строк: **~5%** ✅
- Дублирование кода: **<5%** ✅

---

## 🐛 Технический долг

### 🔴 Критический (нужно сделать)
- [ ] Обновить Mine.java для использования новой конфигурации
- [ ] Обновить MineFileManager.java для нового ConfigLoader

### 🟡 Средний
- [ ] Включить интеграции с Oraxen и ItemsAdder
- [ ] GUI редакторы для шахт

### 🟢 Низкий
- [ ] Добавить `/lm info <mine>` — детальная информация
- [ ] Добавить `/lm tp <mine>` — телепортация
- [ ] Добавить `/lm copy <from> <to>` — копирование

---

## 📦 Git история

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
Система конфигурации полностью переписана!* 🎉
