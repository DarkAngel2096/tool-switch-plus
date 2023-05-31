package tool_switch_plus.utils;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class GameUtils {
    // variable for hard coding the minimum allowed durability
    protected static int toolMinDurability = 5;

    public static int searchInvForEffectiveTool(ScreenHandler playerContainer, BlockState state) {
        // variable to keep the best slot found, it's speed and durability left
        int slotNum = -1;
        float bestSpeed = 1f;
        int lowestDurability = 0;

        // used to keep track of current index (starting from -1 because incrementing it before doing anything else)
        int currentIndex = -1;

        // loop over the inv, checking what is found against the block given
        for (ItemStack stack : playerContainer.getStacks()) {
            currentIndex++;
            // check if the item found is not part of the "MiningToolItem" type, if so, continue
            if (!(stack.getItem() instanceof MiningToolItem)) {
                continue;
            }

            // check if the tool is named, if it is, continue
            if (stack.hasCustomName()) {
                continue;
            }

            // check if tool durability is below min durability, if so, continue
            if (isToolBelowMinDurability(stack)) {
                continue;
            }

            // get the durability left and tool speed
            int durabilityLeft = toolDurabilityLeft(stack);
            float itemSpeed = calculateToolSpeed(stack, state);

            // check if durability left less than current lowest and speed higher than current highest
            if (itemSpeed > bestSpeed || (itemSpeed == bestSpeed && durabilityLeft < lowestDurability)) {
                slotNum = currentIndex;
                bestSpeed = itemSpeed;
                lowestDurability = durabilityLeft;
            }
        }

        // return the slot number of the slot with the best item found
        return slotNum;
    }

    public static int toolDurabilityLeft(ItemStack itemStack) {
        return itemStack.getMaxDamage() - itemStack.getDamage();
    }

    public static boolean isToolBelowMinDurability(ItemStack itemStack) {
        return toolDurabilityLeft(itemStack) <= toolMinDurability;
    }

    public static float calculateToolSpeed(ItemStack itemStack, BlockState blockState) {
        // var for speed with tool for block
        float speed = itemStack.getMiningSpeedMultiplier(blockState);

        // get the efficiency enchantment on the tool
        int efficiencyLevel = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, itemStack);
        if (efficiencyLevel > 0 && itemStack.isSuitableFor(blockState)) {
            speed += efficiencyLevel * efficiencyLevel + 1;
        }

        return speed;
    }

    public static int findPickWithName(ScreenHandler playerContainer, String pickName) {
        // variables to use here
        int slotNum = -1;

        // used to keep track of current index (starting from -1 because incrementing it before doing anything else)
        int currentIndex = -1;

        // loop over the inv
        for (ItemStack stack : playerContainer.getStacks()) {
            currentIndex++;
            // check if the item found is not part of the "MiningToolItem" type, if so, continue
            if (!(stack.getItem() instanceof MiningToolItem)) {
                continue;
            }

            // check if below min durability
            if (isToolBelowMinDurability(stack)) {
                continue;
            }

            // check if the tool has the custom name specified, otherwise continue
            if (stack.hasCustomName() && stack.getName().getString().equals(pickName)) {
                slotNum = currentIndex;
            }
        }
        // return the slot number of the slot with the pick by the name
        return slotNum;
    }

    public static boolean isOreBlock(BlockState blockState) {
        return blockState.getBlock().getName().toString().toLowerCase().contains("ore");
    }

    public static boolean isToolbarSlotInRange(int currentSlot, int minSlot, int maxSlot) {
        return currentSlot >= minSlot && currentSlot <= maxSlot;
    }

    public static void swapSlots (MinecraftClient mc, ScreenHandler playerContainer, int toolSlot, int hotbarSlot) {
        // swap the slots
        mc.interactionManager.clickSlot(playerContainer.syncId, toolSlot, hotbarSlot, SlotActionType.SWAP, mc.player);

        // send a update packet to the server
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(hotbarSlot));
    }
}


