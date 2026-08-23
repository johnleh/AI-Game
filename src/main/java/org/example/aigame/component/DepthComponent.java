package org.example.aigame.component;

import com.almasb.fxgl.entity.component.Component;

public class DepthComponent extends Component {
    @Override
    public void onAdded() {
        updateDepth();
    }

    @Override
    public void onUpdate(double tpf) {
        updateDepth();
    }

    private void updateDepth() {
        entity.setZIndex((int) Math.round(entity.getY() * 1000.0));
    }
}

