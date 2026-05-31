# LoMines

Advanced mine management plugin for Paper 1.21.4+

## Features

- **Multiple fill modes**: Cuboid (fill entire region) or Mask (fill marked positions only)
- **Weighted block system**: Configure different blocks with spawn chances
- **Automatic resets**: Timer-based or percentage-based triggers
- **WorldGuard integration**: Auto-create regions for new mines
- **PlaceholderAPI support**: Display mine info in other plugins
- **GUI editor**: In-game mine configuration editor
- **Player statistics**: Track blocks mined, mines reset, playtime

## Commands

### Admin Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/lm create <name>` | `lomines.admin.create` | Create a new mine |
| `/lm delete <name>` | `lomines.admin.delete` | Delete a mine |
| `/lm reset <name> [silent]` | `lomines.admin.reset` | Reset a mine manually |
| `/lm reload` | `lomines.admin.reload` | Reload all mine configs |
| `/lm list` | `lomines.admin.list` | List all mines |
| `/lm edit <mine>` | `lomines.admin.edit` | Open GUI editor for a mine |
| `/lm setteleport <mine>` | `lomines.admin.setteleport` | Set teleport location (your current position) |
| `/lm setspawn <mine>` | `lomines.admin.setspawn` | Set safe spawn location for stuck players |
| `/lm clearspawn <mine>` | `lomines.admin.setspawn` | Clear spawn location |
| `/lm maskscan <mine>` | `lomines.admin.maskscan` | Scan mask marker blocks |

### Player Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/lm stats [player]` | `lomines.stats` | View mining statistics |
| `/lm top [mine] [limit]` | `lomines.stats` | View leaderboard |

### Wand Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/lm wand` | `lomines.admin.wand` | Get the region selection wand |
| `/lm group <prefix>` | `lomines.admin.wand` | Create mines from group wand selections |

## Permissions

```yaml
lomines.admin:
  description: All admin commands
  default: op
  children:
    lomines.use: true
    lomines.admin.create: true
    lomines.admin.delete: true
    lomines.admin.reset: true
    lomines.admin.reload: true
    lomines.admin.list: true
    lomines.admin.edit: true
    lomines.admin.setteleport: true
    lomines.admin.setspawn: true
    lomines.admin.maskscan: true
    lomines.admin.wand: true
    lomines.stats: true
    lomines.stats.others: true

lomines.use:
  description: Basic usage
  default: true

lomines.stats:
  description: View own stats and leaderboards
  default: true

lomines.stats.others:
  description: View other players' stats
  default: op
```

## Placeholders (PlaceholderAPI)

### Mine Information

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `%lomines_mine_<name>_name%` | Mine name | `mymine` |
| `%lomines_mine_<name>_blocks%` | Current blocks | `8542` |
| `%lomines_mine_<name>_total%` | Total volume | `10000` |
| `%lomines_mine_<name>_percent%` | Fill percentage (1 decimal) | `85.4` |
| `%lomines_mine_<name>_percentint%` | Fill percentage (integer) | `85` |
| `%lomines_mine_<name>_world%` | World name | `world` |
| `%lomines_mine_<name>_remaining%` | Remaining blocks | `1458` |
| `%lomines_mine_<name>_resettime%` | Time since reset (mm:ss) | `03:45` |
| `%lomines_mine_<name>_resetseconds%` | Seconds since reset | `225` |

### Player Statistics

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `%lomines_player_blocksmined%` | Total blocks mined | `15234` |
| `%lomines_player_minesreset%` | Mines reset count | `45` |
| `%lomines_player_playtime%` | Formatted play time | `2h 15m` |
| `%lomines_player_rank%` | Leaderboard rank | `3` |

### Global

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `%lomines_count%` | Total number of mines | `12` |

## Configuration

### defaults.yml

Default configuration applied to all mines:

```yaml
# Reset settings
reset:
  interval: "5m"              # Reset interval (30s, 5m, 2h, 1d)
  percent-trigger: 10.0       # Trigger reset when X% of blocks mined
  percent-enabled: false      # Enable percentage trigger
  commands: []                # Commands to run on reset
  broadcast: ""               # Broadcast message on reset

# Block settings
blocks:
  fill-mode: CUBOID           # CUBOID or MASK
  mask:
    marker: "pink_concrete"   # Marker block for mask mode

# Teleport settings
teleport:
  enabled: false
  location: ""

# Safe spawn for stuck players
player-spawn:
  enabled: false
  location: ""

# UI settings
ui:
  actionbar:
    enabled: true
    format: "<green>{mine}</green> <gray>{percent}%</gray> <dark_gray>({time})"
    range: 50.0

# WorldGuard integration
worldguard:
  enabled: false
  region-template: "{mine_name}_{random_4}"
  owners: []
  members: []
  flags:
    - "passthrough=deny"
    - "build=allow"
```

## Usage Examples

### Creating a Simple Mine

```bash
# 1. Get the wand
/lm wand

# 2. Select two corners (left and right click)
# 3. Create the mine
/lm create mymine

# 4. Edit the config file or use GUI
/lm edit mymine
```

### Setting Up Teleport

```bash
# Stand where players should be teleported
/lm setteleport mymine

# Stand where STUCK players should spawn (optional, falls back to teleport location)
/lm setspawn mymine
```

### Using Mask Fill Mode

```bash
# 1. Paint positions with pink_concrete (or configured marker)
# 2. Scan the markers
/lm maskscan mymine

# Mine will now only fill at marked positions
```

### WorldGuard Auto-Regions

Enable in `plugins/LoMines/mines/_defaults.yml`:

```yaml
worldguard:
  enabled: true
  region-template: "mine_{mine_name}_{random_4}"
  flags:
    - "passthrough=deny"
    - "build=allow"
```

When you create a mine, a WorldGuard region `mine_mymine_7392` is automatically created.

## Support

- Paper 1.21.4+
- Java 21+
- Optional: PlaceholderAPI, WorldGuard

## License

MIT License
