package lk.pixcapsoft.diamondscanner;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

public class KeyBindingHandler {
    public static KeyBinding scanKey;
    public static KeyBinding openResultsKey;
    
    // Store last scan results
    private static List<BlockPos> lastScanResults = new ArrayList<>();
    private static String lastScanType = "No scan performed yet";

    public static void register() {
        // Create category once and reuse it
        String category = "key.categories.pixcapdiamondscanner";
        
        scanKey = new KeyBinding(
                "key.pixcapdiamondscanner.scan",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                category
        );
        
        openResultsKey = new KeyBinding(
                "key.pixcapdiamondscanner.openresults",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                category
        );

        KeyBindingHelper.registerKeyBinding(scanKey);
        KeyBindingHelper.registerKeyBinding(openResultsKey);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (scanKey.wasPressed()) {
                scanForOres(client);
            }
            
            while (openResultsKey.wasPressed()) {
                openLastResults(client);
            }
        });
    }

    private static void scanForOres(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        
        // Only allow in singleplayer
        if (!client.isInSingleplayer()) {
            client.player.sendMessage(Text.literal("§cScanner only works in singleplayer worlds."), false);
            return;
        }
        
        BlockPos playerPos = client.player.getBlockPos();
        int radius = 32;
        List<BlockPos> foundPositions = new ArrayList<>();
        
        // Check if player is in the Nether
        boolean isInNether = client.world.getRegistryKey() == World.NETHER;
        
        if (isInNether) {
            client.player.sendMessage(Text.literal("§6Starting Ancient Debris scan..."), false);
            lastScanType = "Ancient Debris";
            
            // Scan for Ancient Debris in the Nether
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos scanPos = playerPos.add(x, y, z);
                        if (client.world.getBlockState(scanPos).isOf(Blocks.ANCIENT_DEBRIS)) {
                            foundPositions.add(scanPos);
                        }
                    }
                }
            }
        } else {
            client.player.sendMessage(Text.literal("§bStarting Diamond scan..."), false);
            lastScanType = "Diamonds";
            
            // Scan for Diamonds in Overworld/End
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos scanPos = playerPos.add(x, y, z);
                        if (client.world.getBlockState(scanPos).isOf(Blocks.DIAMOND_ORE) ||
                                client.world.getBlockState(scanPos).isOf(Blocks.DEEPSLATE_DIAMOND_ORE)) {
                            foundPositions.add(scanPos);
                        }
                    }
                }
            }
        }

        // Store results
        lastScanResults = foundPositions;

        // Show results in GUI
        if (foundPositions.isEmpty()) {
            client.player.sendMessage(Text.literal("§eNo " + lastScanType.toLowerCase() + " found nearby."), false);
        } else {
            client.player.sendMessage(Text.literal("§aScan complete! Found " + foundPositions.size() + " ore(s). Opening results..."), false);
            client.execute(() -> {
                client.setScreen(new DiamondResultsScreen(null, foundPositions, lastScanType));
            });
        }
    }
    
    private static void openLastResults(MinecraftClient client) {
        if (client.player == null) return;
        
        if (lastScanResults.isEmpty()) {
            client.player.sendMessage(Text.literal("§eNo previous scan results available. Press G to scan."), false);
            return;
        }
        
        client.execute(() -> {
            client.setScreen(new DiamondResultsScreen(null, lastScanResults, lastScanType));
        });
    }
}