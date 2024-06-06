package de.kostari.cloud.demo.scripts;

import de.kostari.cloud.core.Clogger;
import de.kostari.cloud.core.components.Component;
import de.kostari.cloud.core.utils.input.Controllings;

public class PlayerController extends Component {

    private float PLAYER_SPEED = 10;

    @Override
    public void update() {
        Controllings.moveWithWASD(PLAYER_SPEED, gameObject);
        Clogger.log(222);
        super.update();
    }

}
