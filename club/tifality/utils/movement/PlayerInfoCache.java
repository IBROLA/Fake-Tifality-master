package club.tifality.utils.movement;

import club.tifality.Tifality;
import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.api.annotations.Priority;
import club.tifality.manager.event.impl.player.MoveEntityEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.utils.Wrapper;

public final class PlayerInfoCache {

    private static double lastDist;
    private static double prevLastDist;
    private static double baseMoveSpeed;

    static {
        Tifality.getInstance().getEventBus().subscribe(new PlayerUpdatePositionSubscriber());
    }

    public static double getPrevLastDist() {
        return prevLastDist;
    }

    public static double getLastDist() {
        return lastDist;
    }

    public static double getBaseMoveSpeed() {
        return baseMoveSpeed;
    }

    public static double getFriction(double moveSpeed) {
        return MovementUtils.calculateFriction(moveSpeed, lastDist, baseMoveSpeed);
    }

    private static class PlayerUpdatePositionSubscriber {
        @Listener(Priority.HIGH)
        public void onMoveEntity(MoveEntityEvent event) {
            baseMoveSpeed = MovementUtils.getBaseMoveSpeed();
        }

        @Listener
        public void onUpdatePositionEvent(UpdatePositionEvent event) {
            if (event.isPre()) {
                EntityPlayerSP player = Wrapper.getPlayer();
                double xDif = player.posX - player.lastTickPosX;
                double zDif = player.posZ - player.lastTickPosZ;
                prevLastDist = lastDist;
                lastDist = StrictMath.sqrt(xDif * xDif + zDif * zDif);
            }
        }
        /*@EventLink(EventBusPriorities.HIGHEST)
        private final Listener<MoveEntityEvent> onMoveEntity = event -> {
            baseMoveSpeed = MovementUtils.getBaseMoveSpeed();
        };

        @EventLink(EventBusPriorities.HIGHEST)
        private final Listener<UpdatePositionEvent> onUpdatePositionEvent = event -> {
            if (event.isPre()) {
                EntityPlayerSP player = Wrapper.getPlayer();
                double xDif = player.posX - player.lastTickPosX;
                double zDif = player.posZ - player.lastTickPosZ;
                prevLastDist = lastDist;
                lastDist = StrictMath.sqrt(xDif * xDif + zDif * zDif);
            }
        };*/
    }

}
