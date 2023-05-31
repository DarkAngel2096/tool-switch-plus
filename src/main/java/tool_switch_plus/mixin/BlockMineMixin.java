package tool_switch_plus.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tool_switch_plus.utils.GameUtils;

@Mixin (ClientPlayerInteractionManager.class)
public class BlockMineMixin {

    @Shadow @Final private static Logger LOGGER;

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "attackBlock",
            slice = @Slice(from = @At(value = "FIELD", ordinal = 0,
                    target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;breakingBlock:Z")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getBlockState(" +
                    "Lnet/minecraft/util/math/BlockPos;" +
                    ")Lnet/minecraft/block/BlockState;", ordinal = 0))
    private void toolSwitchOnBlockAttack(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        // get the player, world
        ClientPlayerEntity player = this.client.player;
        ClientWorld world = this.client.world;

        // check that the player and world are not null
        if (player != null && world != null) {
            // get the players current slot
            int currentSlot = player.getInventory().selectedSlot;

            // check if the active slot is right before starting to do anything else
            if (GameUtils.isToolbarSlotInRange(currentSlot, 2, 2)) {
                // get the block state of the block
                BlockState blockState = world.getBlockState(pos);

                // a container that has all kinds of details of the player
                ScreenHandler playerContainer = player.playerScreenHandler;

                // variable for slot found
                int toolSlotFound;

                // check if the block targeted is an ore block, if so, do special pick check, otherwise do normal check
                if (GameUtils.isOreBlock(blockState)) {
                    toolSlotFound = GameUtils.findPickWithName(playerContainer, "Ore");
                } else {
                    toolSlotFound = GameUtils.searchInvForEffectiveTool(playerContainer, blockState);
                }

                //LOGGER.info("Slot with best tool is: '{}', current slot is: '{}'", toolSlotFound, currentSlot);

                // if a tool slot was found, it should be swapped to, if not, do nothing
                if (toolSlotFound != -1 && toolSlotFound != currentSlot + 36) {
                    //LOGGER.info("swapping tool");
                    GameUtils.swapSlots(MinecraftClient.getInstance(), playerContainer, toolSlotFound, currentSlot);
                }
            }
        }
    }
}
