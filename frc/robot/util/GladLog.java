package frc.robot.util;

import java.util.ArrayList;
import java.util.List;

import org.littletonrobotics.junction.Logger;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.hardware.TalonFX;

/**
 * Centralized current logging helper for CTRE TalonFX (Phoenix 6).
 *
 * Usage:
 *   - Call registerTalonFX(...) from subsystems (e.g. in constructors).
 *   - Call GladLog.getInstance().logAll() once per loop (robotPeriodic).
 */
public final class GladLog {

    private static final class TalonFxEntry {
        final String name;
        final TalonFX motor;
        final StatusSignal<Double> statorCurrent;
        final StatusSignal<Double> supplyCurrent;

        TalonFxEntry(String name, TalonFX motor) {
            this.name = name;
            this.motor = motor;
            this.statorCurrent = motor.getStatorCurrent();
            this.supplyCurrent = motor.getSupplyCurrent();
        }
    }

    private static final GladLog instance = new GladLog();

    public static GladLog getInstance() {
        return instance;
    }

    private final List<TalonFxEntry> talonFxEntries = new ArrayList<>();
    private boolean frequenciesConfigured = false;
    private double updateFrequencyHz = 50.0;  // default

    private GladLog() {}

    /** Set how often Phoenix will push current signals on the CAN bus. */
    public void setUpdateFrequencyHz(double hz) {
        this.updateFrequencyHz = hz;
        frequenciesConfigured = false; // force reconfigure next time
    }

    /** Register a TalonFX to be logged as Current/<name>/Stator & /Supply. */
    public void registerTalonFX(String name, TalonFX motor) {
        talonFxEntries.add(new TalonFxEntry(name, motor));
        // We’ll configure frequencies next time logAll() runs.
    }

    /** Internal: configure signal update frequencies once. */
    private void configureFrequenciesIfNeeded() {
        if (frequenciesConfigured) return;

        for (TalonFxEntry e : talonFxEntries) {
            BaseStatusSignal.setUpdateFrequencyForAll(
                    updateFrequencyHz,
                    e.statorCurrent,
                    e.supplyCurrent
            );
            e.motor.optimizeBusUtilization();
        }

        frequenciesConfigured = true;
    }

    /** Call once per loop (e.g. robotPeriodic) to log all registered motors. */
    public void logAll() {
        if (talonFxEntries.isEmpty()) return;

        configureFrequenciesIfNeeded();

        for (TalonFxEntry e : talonFxEntries) {
            // Refresh both signals in one CAN transaction:
            BaseStatusSignal.refreshAll(e.statorCurrent, e.supplyCurrent);

            double stator = e.statorCurrent.getValueAsDouble();
            double supply = e.supplyCurrent.getValueAsDouble();

            Logger.recordOutput("Current/" + e.name + "/Stator", stator);
            Logger.recordOutput("Current/" + e.name + "/Supply", supply);
        }
    }
}