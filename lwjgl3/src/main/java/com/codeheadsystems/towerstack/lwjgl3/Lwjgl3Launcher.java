package com.codeheadsystems.towerstack.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.codeheadsystems.towerstack.TowerStackGame;

/**
 * Launches Tower Stack on the desktop using the LWJGL3 backend, in a portrait window.
 *
 * <p>Runs on Linux, Windows and macOS from the one distributable jar; see
 * {@link StartOnFirstThreadHelper} for the macOS detour taken before anything else happens.
 */
public class Lwjgl3Launcher {

    public static void main(String[] args) {
        if (StartOnFirstThreadHelper.relaunchIfNeeded(Lwjgl3Launcher.class, args)) {
            return; // the game ran in the relaunched JVM
        }

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Tower Stack");
        config.setWindowedMode(480, 854);
        config.setResizable(true);
        config.useVsync(true);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new TowerStackGame(), config);
    }
}
