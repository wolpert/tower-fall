package com.codeheadsystems.towerstack;

import com.badlogic.gdx.Game;
import com.codeheadsystems.towerstack.screens.PlayScreen;

/**
 * Application entry point. Owns the screen stack.
 *
 * <p>For now it boots straight into the play loop; the title and game-over screens are
 * added in the state-screens increment (build brief §9.5).
 */
public class TowerStackGame extends Game {

    @Override
    public void create() {
        setScreen(new PlayScreen(this));
    }
}
