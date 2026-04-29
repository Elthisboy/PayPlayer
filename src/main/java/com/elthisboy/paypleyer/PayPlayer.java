package com.elthisboy.paypleyer;

import com.elthisboy.paypleyer.config.PayPlayerConfig;
import com.elthisboy.paypleyer.item.ChargeMoneyItem;
import com.elthisboy.paypleyer.item.GiveMoneyItem;
import com.elthisboy.paypleyer.network.MoneyPackets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PayPlayer implements ModInitializer {
    public static final String MOD_ID = "payplayer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Item CHARGE_MONEY_ITEM = new ChargeMoneyItem(new Item.Settings().maxCount(1));
    public static final Item GIVE_MONEY_ITEM   = new GiveMoneyItem(new Item.Settings().maxCount(1));

    @Override
    public void onInitialize() {
        PayPlayerConfig.load();

        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "charge_money"), CHARGE_MONEY_ITEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "give_money"),   GIVE_MONEY_ITEM);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(CHARGE_MONEY_ITEM);
            entries.add(GIVE_MONEY_ITEM);
        });

        MoneyPackets.registerServerHandlers();

        LOGGER.info("[PayPlayer] Mod inicializado.");
    }
}