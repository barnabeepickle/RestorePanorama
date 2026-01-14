package com.github.barnabeepickle.restorepanorama;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjglx.input.Keyboard;

import java.io.File;

@Mod(modid = Reference.MODID, name = Reference.MOD_NAME, version = Reference.VERSION, clientSideOnly = true)
public class RestorePanoramaMain {

    public static final Logger LOGGER = LogManager.getLogger(Reference.MODID);

    private static File PANORAMA_DIR;

    private KeyBinding panoramaKeyBind;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        PANORAMA_DIR = new File(Minecraft.getMinecraft().gameDir, "panoramas");

        panoramaKeyBind = new KeyBinding("key." + Reference.MODID + ".take", Keyboard.KEY_F9, "key." + Reference.MODID + ".category");
        ClientRegistry.registerKeyBinding(panoramaKeyBind);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft client = Minecraft.getMinecraft();
            while (panoramaKeyBind.isPressed()) {
                if (client.player != null && !client.isGamePaused()) {
                    PANORAMA_DIR.mkdirs();

                    ITextComponent feedbackMessage = this.takePanoramaScreenshots(client, PANORAMA_DIR);

                    if (feedbackMessage != null) {
                        client.player.sendMessage(feedbackMessage);
                    }
                }
            }
        }
    }

    // This code is inspired by the disabled panorama screenshot code in later versions of Minecraft
    /**
     * Takes a panorama. The panorama is stored in the provided {@code dir}, it takes
     * and saves 6 screenshots of size {@code width} and {@code height}.
     * @param client An instance of {@code Minecraft}
     * @param dir The directory where the panorama screenshots are to be saved
     * @return a translatable piece of text dependent on the screenshot result.
     */
    public ITextComponent takePanoramaScreenshots(Minecraft client, File dir) {
        // height and width that the panorama images will be rendered at (should be the same)
        int height = 4096;
        int width = 4096;
        // the height and width is down sampled later

        int originalHeight = client.displayHeight;
        int originalWidth = client.displayWidth;

        Framebuffer frameBuffer = client.getFramebuffer();

        float pitch = client.player.rotationPitch;
        float yaw = this.getPlayerYaw(client);
        float lastPitch = client.player.prevRotationPitch;
        float lastYaw = client.player.prevRotationYaw;

        float playerFov = client.getRenderManager().options.fovSetting;

        client.getRenderManager().setRenderOutlines(false);

        ITextComponent text;
        try {
            client.getRenderManager().options.fovSetting = 90.0F;

            client.displayHeight = height;
            client.displayWidth = width;
            frameBuffer.framebufferRender(width, height);

            for (int i = 0; i < 6; i++) {
                switch (i) {
                    case 0:
                        client.player.rotationYaw = yaw;
                        client.player.rotationPitch = 0.0F;
                        break;
                    case 1:
                        client.player.rotationYaw = (yaw + 90.0F) % 360.0F;
                        client.player.rotationPitch = 0.0F;
                        break;
                    case 2:
                        client.player.rotationYaw = (yaw + 180.0F) % 360.0F;
                        client.player.rotationPitch = 0.0F;
                        break;
                    case 3:
                        client.player.rotationYaw = (yaw - 90.0F) % 360.0F;
                        client.player.rotationPitch = 0.0F;
                        break;
                    case 4:
                        client.player.rotationYaw = yaw;
                        client.player.rotationPitch = 0.0F;
                        break;
                    case 5:
                    default:
                        client.player.rotationYaw = yaw;
                        client.player.rotationPitch = 0.0F;
                }

                client.player.prevRotationYaw = client.player.rotationYaw;
                client.player.prevRotationPitch = client.player.rotationPitch;

                try {
                    Thread.sleep(10L);
                } catch (InterruptedException ignored) {
                }

                ScreenShotHelper.saveScreenshot(dir, "panorama_" + i + ".png", width / 4, height / 4, frameBuffer);
                // the height and width are down sampled here
            }

            ITextComponent interactText = new TextComponentString(dir.getName());
            interactText.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, dir.getAbsolutePath()));
            interactText.getStyle().setUnderlined(Boolean.TRUE);

            return new TextComponentTranslation("screenshot." + Reference.MODID + ".success", interactText);

        } catch (Exception e) {
            LOGGER.error("Was unable to save panorama", e);
            text = new TextComponentTranslation("screenshot." + Reference.MODID + ".failure", e.getMessage());
        } finally {
            // reset everthing important back to how it was
            client.player.rotationPitch = pitch;
            client.player.rotationYaw = yaw;
            client.player.prevRotationPitch = lastPitch;
            client.player.prevRotationYaw = lastYaw;

            client.getRenderManager().options.fovSetting = playerFov;

            client.getRenderManager().setRenderOutlines(true);

            client.displayHeight = originalHeight;
            client.displayWidth = originalWidth;

            frameBuffer.framebufferRender(originalWidth, originalHeight);
        }

        return text;
    }

    private float getPlayerYaw(Minecraft client) {
        return client.player.rotationYaw;
    }
}
