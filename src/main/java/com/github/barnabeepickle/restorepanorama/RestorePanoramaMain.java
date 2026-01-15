package com.github.barnabeepickle.restorepanorama;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjglx.input.Keyboard;

import java.io.File;

@Mod(modid = Reference.MODID, name = Reference.MOD_NAME, version = Reference.VERSION, clientSideOnly = true)
public class RestorePanoramaMain {

    public static final Logger LOGGER = LogManager.getLogger(Reference.MODID);

    private static File PANORAMA_DIR;

    private static KeyBinding panoramaKeyBind;

    @Mod.EventHandler
    public void init(FMLPreInitializationEvent event) {
        PANORAMA_DIR = new File(Minecraft.getMinecraft().gameDir, "panoramas");

        panoramaKeyBind = new KeyBinding("key." + Reference.MODID + ".take", KeyConflictContext.IN_GAME, Keyboard.KEY_F9, "key.category." + Reference.MODID);
        ClientRegistry.registerKeyBinding(panoramaKeyBind);

        MinecraftForge.EVENT_BUS.register(ClientEventListener.class);
        LOGGER.info("Registered " + Reference.MOD_NAME + " ClientEventListener.class on the EVENT_BUS");
    }

    public static class ClientEventListener {
        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                Minecraft client = Minecraft.getMinecraft();
                while (panoramaKeyBind.isPressed()) {
                    if (client.player != null && !client.isGamePaused()) {
                        PANORAMA_DIR.mkdirs();

                        LOGGER.info("Attempting to take panorama screenshot, the game might freeze for a moment!");
                        ITextComponent feedbackMessage = takePanoramaScreenshots(client, PANORAMA_DIR);

                        if (feedbackMessage != null) {
                            client.player.sendMessage(feedbackMessage);
                        }
                    }
                }
            }
        }

        /**
         * Takes a panorama. The panorama is stored in the provided {@code dir}, it takes
         * and saves 6 screenshots of size {@code width} and {@code height},this overload
         * always straightens the player's view to {@code 0.0F}.
         * @param client An instance of {@code Minecraft}.
         * @param dir The directory where the panorama screenshots are to be saved.
         * @return a translatable piece of text dependent on the screenshot result.
         */
        public static ITextComponent takePanoramaScreenshots(Minecraft client, File dir) {
            return takePanoramaScreenshots(client, dir , true);
        }

        // This code is inspired by the "disabled"/inaccessible panorama screenshot code in later versions of Minecraft
        /**
         * Takes a panorama. The panorama is stored in the provided {@code dir}, it takes
         * and saves 6 screenshots of size {@code width} and {@code height}.
         * @param client An instance of {@code Minecraft}.
         * @param dir The directory where the panorama screenshots are to be saved.
         * @param onAxis If the player's view should be snapped straight to {@code 0.0F} or start from where they are currently looking.
         * @return a translatable piece of text dependent on the screenshot result.
         */
        @SideOnly(Side.CLIENT)
        public static ITextComponent takePanoramaScreenshots(Minecraft client, File dir, boolean onAxis) {
            // height and width that the panorama images will be rendered at (should be the same)
            int height = 4096;
            int width = 4096;

            int originalHeight = client.displayHeight;
            int originalWidth = client.displayWidth;

            Framebuffer frameBuffer = client.getFramebuffer();

            float workingYaw = client.player.rotationYaw;
            if (onAxis) {
                workingYaw = 0.0F;
            }

            float pitch = client.player.rotationPitch;
            float yaw = client.player.rotationYaw;
            float lastPitch = client.player.prevRotationPitch;
            float lastYaw = client.player.prevRotationYaw;

            float playerFov = client.getRenderManager().options.fovSetting;

            boolean originalHideGUI = client.gameSettings.hideGUI;
            client.gameSettings.hideGUI = true;

            File subDirectory = new File(dir, "panorama_" + CustomScreenShotHelper.getTimeStamp());
            subDirectory.mkdir();

            ITextComponent text;
            try {
                client.getRenderManager().options.fovSetting = 90.0F;

                client.displayHeight = height;
                client.displayWidth = width;
                frameBuffer.createBindFramebuffer(width, height);

                for (int i = 0; i < 6; i++) {
                    switch (i) {
                        case 0:
                            client.player.rotationYaw = workingYaw;
                            client.player.rotationPitch = 0.0F;
                            break;
                        case 1:
                            client.player.rotationYaw = (client.player.rotationYaw + 90.0F) % 360.0F;
                            client.player.rotationPitch = 0.0F;
                            break;
                        case 2:
                            client.player.rotationYaw = (client.player.rotationYaw + 180.0F) % 360.0F;
                            client.player.rotationPitch = 0.0F;
                            break;
                        case 3:
                            client.player.rotationYaw = (client.player.rotationYaw - 90.0F) % 360.0F;
                            client.player.rotationPitch = 0.0F;
                            break;
                        case 4:
                            client.player.rotationYaw = workingYaw;
                            client.player.rotationPitch = -90.0F;
                            break;
                        case 5:
                        default:
                            client.player.rotationYaw = workingYaw;
                            client.player.rotationPitch = 90.0F;
                    }

                    client.player.prevRotationYaw = client.player.rotationYaw;
                    client.player.prevRotationPitch = client.player.rotationPitch;

                    try {
                        Thread.sleep(10L);
                    } catch (InterruptedException ignored) {
                    }

                    client.entityRenderer.updateCameraAndRender(client.isGamePaused() ? client.renderPartialTicksPaused : client.timer.renderPartialTicks, System.nanoTime());
                    frameBuffer.framebufferRender(width, height);

                    try {
                        Thread.sleep(10L);
                    } catch (InterruptedException ignored) {
                    }

                    client.entityRenderer.updateCameraAndRender(client.isGamePaused() ? client.renderPartialTicksPaused : client.timer.renderPartialTicks, System.nanoTime());
                    frameBuffer.framebufferRender(width, height);

                    CustomScreenShotHelper.saveScreenshot(subDirectory, "panorama_" + i + ".png", 1024, 1024, frameBuffer);
                }

                ITextComponent interactText = new TextComponentString(subDirectory.getName());
                interactText.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, subDirectory.getAbsolutePath()));
                interactText.getStyle().setUnderlined(Boolean.TRUE);

                text = new TextComponentTranslation("screenshot." + Reference.MODID + ".success", interactText);

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

                client.gameSettings.hideGUI = originalHideGUI;

                client.displayHeight = originalHeight;
                client.displayWidth = originalWidth;

                frameBuffer.createBindFramebuffer(originalWidth, originalHeight);
            }

            return text;
        }
    }
}
