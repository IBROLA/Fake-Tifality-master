package club.tifality.module.impl.movement;

import club.tifality.manager.api.annotations.Listener;
import net.minecraft.potion.Potion;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.property.Property;
import club.tifality.utils.Rotation;
import club.tifality.utils.RotationUtils;
import club.tifality.utils.movement.MovementUtils;

@ModuleInfo(label="Sprint", category= ModuleCategory.MOVEMENT)
public final class Sprint extends Module {
    public final Property<Boolean> allDirectionsValue = new Property<>("Omni", true);
    public final Property<Boolean> blindnessValue = new Property<>("Blindness", true);
    public final Property<Boolean> foodValue = new Property<>("Food", true);
    public final Property<Boolean> checkServerSide = new Property<>("CheckServerSide", false);
    public final Property<Boolean> checkServerSideGround = new Property<>("CheckServerSideOnlyGround", false);

    @Listener
    public void onUpdate(final UpdatePositionEvent event) {
        if (!MovementUtils.isMoving() || mc.thePlayer.isSneaking() || (this.blindnessValue.get() && mc.thePlayer.isPotionActive(Potion.blindness)) || (this.foodValue.get() && mc.thePlayer.getFoodStats().getFoodLevel() <= 6.0f && !mc.thePlayer.capabilities.allowFlying) || (this.checkServerSide.get() && (mc.thePlayer.onGround || !this.checkServerSideGround.get()) && !this.allDirectionsValue.get() && RotationUtils.targetRotation != null && RotationUtils.getRotationDifference(new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)) > 30.0)) {
            mc.thePlayer.setSprinting(false);
            return;
        }
        if (this.allDirectionsValue.get() || mc.thePlayer.movementInput.moveForward >= 0.8f) {
            mc.thePlayer.setSprinting(true);
        }
    }
}

