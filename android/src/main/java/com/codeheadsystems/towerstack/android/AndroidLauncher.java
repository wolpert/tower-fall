package com.codeheadsystems.towerstack.android;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.codeheadsystems.towerstack.TowerStackGame;

/**
 * Android entry point. Hosts the shared {@link TowerStackGame} in a libGDX
 * {@link AndroidApplication}. Touch input maps straight onto the game's single "drop" action,
 * so no platform-specific input handling is needed.
 */
public class AndroidLauncher extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true; // hide the system bars for a clean, full-screen play area
        initialize(new TowerStackGame(), config);
    }
}
