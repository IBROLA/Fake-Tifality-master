package club.tifality.module.impl.player;

import club.tifality.manager.api.annotations.Listener;
import net.minecraft.network.play.client.C03PacketPlayer;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.property.impl.EnumProperty;
import club.tifality.utils.movement.MovementUtils;
import club.tifality.utils.Wrapper;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@ModuleInfo(label = "No Fall", category = ModuleCategory.PLAYER)
public final class NoFall extends Module {

    private static final List<Double> BLOCK_HEIGHTS = Arrays.asList(0.015625, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1.0);

    private final EnumProperty<NoFallMode> noFallModeProperty = new EnumProperty<>("Mode", NoFallMode.WATCHDOG);
    @Listener
    public void onUpdatePositionEvent(UpdatePositionEvent event) {
        if (event.isPre()) {
            final double minFallDist = MovementUtils.getMinFallDist();

            if (Wrapper.getPlayer().fallDistance >= minFallDist) {
                noFallModeProperty.getValue().fallListener.onFall(event, Wrapper.getPlayer().fallDistance);
            }
        }
    }
    /*@EventLink
    public final Listener<UpdatePositionEvent> onUpdatePositionEvent = event -> {
        if (event.isPre()) {
            final double minFallDist = MovementUtils.getMinFallDist();

            if (Wrapper.getPlayer().fallDistance >= minFallDist) {
                noFallModeProperty.getValue().fallListener.onFall(event, Wrapper.getPlayer().fallDistance);
            }
        }
    };*/

    public NoFall() {
        setSuffixListener(noFallModeProperty);
    }

    private enum NoFallMode {
        EDIT((event, fallDist) -> event.setOnGround(true)),
        PACKET((event, fallDist) -> Wrapper.sendPacketDirect(new C03PacketPlayer(true))),
        WATCHDOG((event, fallDist) -> {
            if (MovementUtils.isOverVoid()) {
                return;
            }

            final int basePosY = (int) event.getPosY();

            final double baseOffset = event.getPosY() - basePosY;

            BLOCK_HEIGHTS.sort(Comparator.comparingDouble(height -> Math.abs(height - baseOffset)));

            double yDif = 2;

            int index = 0;

            double closest = 0;

            while (index < BLOCK_HEIGHTS.size() && (yDif > 0.0 || yDif <= -0.05)) {
                closest = BLOCK_HEIGHTS.get(index);

                yDif = closest - baseOffset;

                index++;
            }

            if (yDif != 2 && yDif >= -0.05 && yDif < 0.0) {
                event.setOnGround(true);
                event.setPosY(basePosY + closest);
                Wrapper.getPlayer().fallDistance = 0.0F;
            }
        });

        private final OnFall fallListener;

        NoFallMode(OnFall fallListener) {
            this.fallListener = fallListener;
        }
    }

    @FunctionalInterface
    private interface OnFall {
        void onFall(UpdatePositionEvent event, float fallDist);
    }
}