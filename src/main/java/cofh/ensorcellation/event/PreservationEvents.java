package cofh.ensorcellation.event;

import cofh.ensorcellation.enchantment.override.MendingEnchantmentAlt;
import cofh.ensorcellation.init.EnsorcConfig;
import cofh.lib.util.helpers.XpHelper;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static cofh.lib.util.Utils.getItemEnchantmentLevel;
import static cofh.lib.util.constants.Constants.ID_ENSORCELLATION;
import static net.minecraft.enchantment.Enchantments.MENDING;

@Mod.EventBusSubscriber(modid = ID_ENSORCELLATION)
public class PreservationEvents {

    private PreservationEvents() {

    }

    @SubscribeEvent
    public static void handleAnvilRepairEvent(AnvilRepairEvent event) {

        if (event.isCanceled()) {
            return;
        }
        if (!EnsorcConfig.enableMendingOverride) {
            return;
        }
        ItemStack left = event.getItemInput();
        ItemStack output = event.getItemResult();

        if (getItemEnchantmentLevel(MENDING, left) <= 0) {
            return;
        }
        if (output.getDamageValue() < left.getDamageValue()) {
            event.setBreakChance(MendingEnchantmentAlt.anvilDamage);
        }
    }

    @SubscribeEvent
    public static void handleAnvilUpdateEvent(AnvilUpdateEvent event) {

        if (event.isCanceled()) {
            return;
        }
        if (!EnsorcConfig.enableMendingOverride) {
            return;
        }
        ItemStack left = event.getLeft();

        if (getItemEnchantmentLevel(MENDING, left) <= 0) {
            return;
        }
        ItemStack right = event.getRight();
        ItemStack output = left.copy();

        if (output.isDamageableItem() && output.getItem().isValidRepairItem(left, right)) {
            int damageLeft = Math.min(output.getDamageValue(), output.getMaxDamage() / 4);
            if (damageLeft <= 0) {
                return;
            }
            int matCost;
            for (matCost = 0; damageLeft > 0 && matCost < right.getCount(); ++matCost) {
                int durability = output.getDamageValue() - damageLeft;
                output.setDamageValue(durability);
                damageLeft = Math.min(output.getDamageValue(), output.getMaxDamage() / 4);
            }
            event.setMaterialCost(matCost);
            // event.setCost(0);
            event.setOutput(output);
        } else if (output.isDamageableItem()) {
            int damageLeft = left.getMaxDamage() - left.getDamageValue();
            int damageRight = right.getMaxDamage() - right.getDamageValue();
            int damageRepair = damageLeft + damageRight + output.getMaxDamage() * 12 / 100;
            int damageOutput = output.getMaxDamage() - damageRepair;

            if (damageOutput < 0) {
                damageOutput = 0;
            }
            if (damageOutput < output.getDamageValue()) {
                output.setDamageValue(damageOutput);
            }
            // event.setCost(0);
            event.setOutput(output);
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void handlePickupXpEvent(PlayerXpEvent.PickupXp event) {

        if (event.isCanceled()) {
            return;
        }
        if (!EnsorcConfig.enableMendingOverride) {
            return;
        }
        PlayerEntity player = event.getPlayer();
        ExperienceOrbEntity orb = event.getOrb();

        player.takeXpDelay = 2;
        player.take(orb, 1);

        XpHelper.attemptStoreXP(player, orb);
        if (orb.value > 0) {
            player.giveExperiencePoints(orb.value);
        }
        orb.remove();
        event.setCanceled(true);
    }

}