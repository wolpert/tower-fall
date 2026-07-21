package com.codeheadsystems.towerstack;

import com.badlogic.gdx.Game;
import com.codeheadsystems.towerstack.screens.TitleScreen;

/**
 * Application entry point. Owns the screen stack.
 *
 * <p>Boots into the title screen; from there a tap starts a run, and game over is presented
 * as a phase within the play screen so the camera can hold on the settling tower.
 */
public class TowerStackGame extends Game {

    @Override
    public void create() {
        setScreen(new TitleScreen(this));
    }
}
