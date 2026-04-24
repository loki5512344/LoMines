package com.loki.lomines.data.config;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for creating immutable MineConfig instances.
 */
public final class MineConfigBuilder {
    private List<Location> selections = new ArrayList<>();
    private Map<String, Double> blocks = new HashMap<>();
    private FillMode fillMode = FillMode.CUBOID;
    private String maskMarkerMaterial = "pink_concrete";
    private List<Location> maskPositions = new ArrayList<>();
    private List<Reward> rewards = new ArrayList<>();
    private int resetTicks = 6000;
    private double resetPercent = 10.0;
    private boolean resetOnPercentEnabled = false;
    private List<String> resetCommands = new ArrayList<>();
    private String broadcastReset = "";
    private boolean teleportOnReset = false;
    private Location teleportLocation = null;
    private boolean actionBarEnabled = true;
    private String actionBarMessage = "";
    private double actionBarRange = 50.0;
    private String timerFormat = "mm:ss";

    MineConfigBuilder() {
    }

    public MineConfigBuilder selections(List<Location> selections) {
        this.selections = new ArrayList<>(selections);
        return this;
    }

    public MineConfigBuilder blocks(Map<String, Double> blocks) {
        this.blocks = new HashMap<>(blocks);
        return this;
    }

    public MineConfigBuilder fillMode(FillMode fillMode) {
        this.fillMode = fillMode != null ? fillMode : FillMode.CUBOID;
        return this;
    }

    public MineConfigBuilder maskMarkerMaterial(String maskMarkerMaterial) {
        this.maskMarkerMaterial = maskMarkerMaterial != null ? maskMarkerMaterial : "pink_concrete";
        return this;
    }

    public MineConfigBuilder maskPositions(List<Location> maskPositions) {
        this.maskPositions = maskPositions != null ? new ArrayList<>(maskPositions) : new ArrayList<>();
        return this;
    }

    public MineConfigBuilder rewards(List<Reward> rewards) {
        this.rewards = new ArrayList<>(rewards);
        return this;
    }

    public MineConfigBuilder resetTicks(int resetTicks) {
        this.resetTicks = resetTicks;
        return this;
    }

    public MineConfigBuilder resetPercent(double resetPercent) {
        this.resetPercent = resetPercent;
        return this;
    }

    public MineConfigBuilder resetOnPercentEnabled(boolean resetOnPercentEnabled) {
        this.resetOnPercentEnabled = resetOnPercentEnabled;
        return this;
    }

    public MineConfigBuilder resetCommands(List<String> resetCommands) {
        this.resetCommands = new ArrayList<>(resetCommands);
        return this;
    }

    public MineConfigBuilder broadcastReset(String broadcastReset) {
        this.broadcastReset = broadcastReset;
        return this;
    }

    public MineConfigBuilder teleportOnReset(boolean teleportOnReset) {
        this.teleportOnReset = teleportOnReset;
        return this;
    }

    public MineConfigBuilder teleportLocation(Location teleportLocation) {
        this.teleportLocation = teleportLocation;
        return this;
    }

    public MineConfigBuilder actionBarEnabled(boolean actionBarEnabled) {
        this.actionBarEnabled = actionBarEnabled;
        return this;
    }

    public MineConfigBuilder actionBarMessage(String actionBarMessage) {
        this.actionBarMessage = actionBarMessage;
        return this;
    }

    public MineConfigBuilder actionBarRange(double actionBarRange) {
        this.actionBarRange = actionBarRange;
        return this;
    }

    public MineConfigBuilder timerFormat(String timerFormat) {
        this.timerFormat = timerFormat;
        return this;
    }

    public MineConfig build() {
        return new MineConfig(
            selections, blocks, fillMode, maskMarkerMaterial, maskPositions,
            rewards, resetTicks, resetPercent, resetOnPercentEnabled,
            resetCommands, broadcastReset, teleportOnReset, teleportLocation,
            actionBarEnabled, actionBarMessage, actionBarRange, timerFormat
        );
    }
}
