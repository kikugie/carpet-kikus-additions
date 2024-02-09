package me.kikugie.carpet;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class InventoryTransfer {
    public static void mergeInventories(Inventory source, Inventory target) {
        ArrayList<Integer> sourceSlots = getPopulatedSlots(source);
        ArrayList<Integer> targetSlots = getAvailableSlots(target);
        if (sourceSlots.isEmpty() || targetSlots.isEmpty()) return;

        int sourceIndex = 0;
        int targetIndex = 0;

        while (sourceIndex < sourceSlots.size()) {
            if (targetIndex == targetSlots.size()) {
                targetIndex = 0;
                sourceIndex++;
                continue;
            }

            int currentSourceSlot = sourceSlots.get(sourceIndex);
            int currentTargetSlot = targetSlots.get(targetIndex);

            ItemStack sourceItemStack = source.getStack(currentSourceSlot);
            ItemStack targetItemStack = target.getStack(currentTargetSlot);

            if (targetItemStack.getCount() >= targetItemStack.getMaxCount()) {
                targetIndex++;
                continue;
            }

            if (targetItemStack.isEmpty()) {
                source.setStack(currentSourceSlot, ItemStack.EMPTY);
                target.setStack(currentTargetSlot, sourceItemStack);
                sourceIndex++;
                targetIndex = 0;
                continue;
            }

            if (ItemStack.canCombine(sourceItemStack, targetItemStack)) {
                int stackSizeDiff = Math.min(sourceItemStack.getCount(), targetItemStack.getMaxCount() - targetItemStack.getCount());
                sourceItemStack.decrement(stackSizeDiff);
                targetItemStack.increment(stackSizeDiff);
                if (sourceItemStack.isEmpty()) {
                    sourceIndex++;
                    targetIndex = 0;
                }
                continue;
            }
            targetIndex++;
        }
        target.markDirty();
    }

    private static ArrayList<Integer> getAvailableSlots(Inventory inventory) {
        ArrayList<Integer> availableSlots = new ArrayList<>();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack itemStack = inventory.getStack(i);
            if (itemStack.getCount() >= itemStack.getMaxCount()) continue;
            availableSlots.add(i);
        }
        return availableSlots;
    }

    private static ArrayList<Integer> getPopulatedSlots(Inventory inventory) {
        ArrayList<Integer> populatedSlots = new ArrayList<>();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack itemStack = inventory.getStack(i);
            if (itemStack.isEmpty()) continue;
            populatedSlots.add(i);
        }
        return populatedSlots;
    }
}