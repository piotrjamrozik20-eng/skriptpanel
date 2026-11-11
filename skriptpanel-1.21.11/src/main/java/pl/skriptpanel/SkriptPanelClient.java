package pl.skriptpanel;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class SkriptPanelClient implements ClientModInitializer {
    public static KeyMapping openKey;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("skriptpanel", "main"));

        openKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.skriptpanel.open",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_K,
                category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new PanelScreen());
                }
            }
        });
    }
}
