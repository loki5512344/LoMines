<div align="center">

# LoMines

Multi-region mine management plugin with WorldGuard integration and player statistics for Paper servers.

![Java](https://img.shields.io/badge/Java-21+-orange?style=flat-square&logo=openjdk&logoColor=white)
![Paper](https://img.shields.io/badge/Paper-1.19.2+-blue?style=flat-square)
![Folia](https://img.shields.io/badge/Folia-supported-purple?style=flat-square)
![License](https://img.shields.io/badge/license-GPLv3-blue?style=flat-square&logo=gnu&logoColor=white)
![version](https://img.shields.io/badge/version-3.0.0-green?style=flat-square)

[English](#english) | [Русский](#russian)

</div>

---

<a name="english"></a>

## English

### Overview

LoMines is an advanced mine management plugin for Paper 1.19.2+ servers. It supports multiple fill modes, weighted block systems, automatic resets, WorldGuard integration, in-game GUI editor, and player statistics.

### Features

| Feature | Description |
|---------|-------------|
| Multiple fill modes | Cuboid (fill entire region) or Mask (fill marked positions) |
| Weighted blocks | Configure spawn chances per block type |
| Automatic resets | Timer-based or percentage-based triggers |
| WorldGuard integration | Auto-create regions for new mines |
| GUI editor | In-game mine configuration |
| Player statistics | Track blocks mined, resets, playtime |
| Leaderboards | Top players per mine or global |
| Group wand | Create multiple mines from batch selections |
| PlaceholderAPI | Display mine info in other plugins |
| Holograms | DecentHolograms and HolographicDisplays support |

### Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/lm create <name>` | `lomines.admin` | Create a new mine |
| `/lm delete <name>` | `lomines.admin` | Delete a mine |
| `/lm reset <name> [silent]` | `lomines.admin` | Reset a mine |
| `/lm reload` | `lomines.admin` | Reload configs |
| `/lm list` | `lomines.admin` | List all mines |
| `/lm edit <mine>` | `lomines.admin.edit` | Open GUI editor |
| `/lm wand` | `lomines.admin.wand` | Get selection wand |
| `/lm group <prefix>` | `lomines.admin.wand` | Batch mine creation |
| `/lm setteleport <mine>` | `lomines.admin.setteleport` | Set teleport point |
| `/lm setspawn <mine>` | `lomines.admin.setspawn` | Set safe spawn |
| `/lm maskscan <mine>` | `lomines.admin.maskscan` | Scan mask markers |
| `/lm stats [player]` | `lomines.stats` | View statistics |
| `/lm top [mine] [limit]` | `lomines.stats` | View leaderboard |

### Placeholders

| Placeholder | Description |
|-------------|-------------|
| `%lomines_mine_<name>_blocks%` | Current blocks in mine |
| `%lomines_mine_<name>_percent%` | Fill percentage |
| `%lomines_mine_<name>_remaining%` | Remaining blocks |
| `%lomines_mine_<name>_resettime%` | Time since reset |
| `%lomines_player_blocksmined%` | Total blocks mined |
| `%lomines_player_rank%` | Leaderboard rank |
| `%lomines_count%` | Total mines count |

### Dependencies

- Required: Paper 1.19.2+, Java 21+
- Optional: PlaceholderAPI, WorldGuard, DecentHolograms, HolographicDisplays, Oraxen, ItemsAdder

### Installation

1. Drop the jar into `plugins/`
2. Restart the server
3. Configure `plugins/LoMines/mines/` and `plugins/LoMines/defaults.yml`

---

<a name="russian"></a>

## Русский

### Обзор

LoMines - продвинутый плагин управления шахтами для Paper 1.19.2+ серверов. Поддерживает несколько режимов заполнения, взвешенные блоки, автоматические сбросы, интеграцию с WorldGuard, GUI-редактор и статистику игроков.

### Возможности

| Возможность | Описание |
|-------------|----------|
| Режимы заполнения | Cuboid (весь регион) или Mask (по меткам) |
| Взвешенные блоки | Настройка шансов появления для каждого блока |
| Авто-сбросы | По таймеру или по проценту добычи |
| WorldGuard | Авто-создание регионов для новых шахт |
| GUI-редактор | Настройка шахт прямо в игре |
| Статистика | Добыто блоков, сбросы, время игры |
| Лидерборды | Топ игроков по шахте или глобально |
| Групповая палка | Создание нескольких шахт за раз |
| PlaceholderAPI | Информация о шахтах в других плагинах |
| Голограммы | Поддержка DecentHolograms и HolographicDisplays |

### Команды

| Команда | Право | Описание |
|---------|-------|----------|
| `/lm create <имя>` | `lomines.admin` | Создать шахту |
| `/lm delete <имя>` | `lomines.admin` | Удалить шахту |
| `/lm reset <имя>` | `lomines.admin` | Сбросить шахту |
| `/lm edit <имя>` | `lomines.admin.edit` | Открыть GUI-редактор |
| `/lm wand` | `lomines.admin.wand` | Получить палку выделения |
| `/lm stats [игрок]` | `lomines.stats` | Статистика |
| `/lm top [шахта]` | `lomines.stats` | Лидерборд |

### Зависимости

- Обязательные: Paper 1.19.2+, Java 21+
- Опциональные: PlaceholderAPI, WorldGuard, DecentHolograms, Oraxen, ItemsAdder

### Установка

1. Положите jar в папку `plugins/`
2. Перезапустите сервер
3. Настройте `plugins/LoMines/mines/` и `plugins/LoMines/defaults.yml`

---

### Links

- [Releases](../../releases)
- [Issues](../../issues)
- [License](LICENSE)

### License

GNU General Public License v3.0