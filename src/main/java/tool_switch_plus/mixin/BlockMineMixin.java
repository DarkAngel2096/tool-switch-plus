package tool_switch_plus.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tool_switch_plus.utils.GameUtils;

@Mixin (ClientPlayerInteractionManager.class)
public class BlockMineMixin {

    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "attackBlock", at = @At("HEAD"))
    private void toolSwitchOnBlockAttack(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        // get the player
        ClientPlayerEntity player = GameUtils.getClientPlayer();

        // check if the active slot is right before starting to do anything else
        if (GameUtils.isToolbarSlotInRange(player, 2, 2)) {
            // get the block state of the block
            BlockState blockState = GameUtils.getClientWorld().getBlockState(pos);

            // variable for slot found
            int toolSlotFound;

            // check if the block targeted is an ore block, if so, do special pick check, otherwise do normal check
            if (GameUtils.isOreBlock(blockState)) {
                toolSlotFound = GameUtils.findPickWithName(player.getInventory(), "Ore");
            } else {
                toolSlotFound = GameUtils.searchInvForEffectiveTool(player.getInventory(), blockState);
            }

            LOGGER.info("Slot found: " + toolSlotFound);

            if (toolSlotFound != -1) {
                GameUtils.swapSlots(player, toolSlotFound, player.getInventory().selectedSlot);
            }

            LOGGER.info("block broken: " + blockState);
            LOGGER.info("Item in slot '{}' is best to use", toolSlotFound);
        }
    }
}
