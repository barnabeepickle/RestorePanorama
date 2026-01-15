package com.github.barnabeepickle.restorepanorama;

import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ScreenShotHelper;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.github.barnabeepickle.restorepanorama.RestorePanoramaMain.LOGGER;

@SideOnly(Side.CLIENT)
public class CustomScreenShotHelper {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");


    /**
     * Takes a Screenshot from a {@code Framebuffer}.
     * @param directory The directory the screenshots will be saved in.
     * @param name The name of the screenshot PNG.
     * @param width The {@code width} of the image to be saved.
     * @param height The {@code height} of the image to be saved.
     * @param framebuffer The {@code Framebuffer} which the image should be take from.
     */
    public static void saveScreenshot(
            File directory,
            String name,
            int width,
            int height,
            Framebuffer framebuffer
    )
    {
        try {
            BufferedImage bufferedImage = ScreenShotHelper.createScreenshot(width, height, framebuffer);

            File file = new File(directory, name).getCanonicalFile();

            ScreenshotEvent event = ForgeHooksClient.onScreenshot(bufferedImage, file);
            file = event.getScreenshotFile();
            ImageIO.write(bufferedImage, "png", file);
        } catch (Exception e) {
            LOGGER.warn("Could not save screenshot", e);
        }
    }

    /**
     * Gets the timestamp for use when naming files
     * @return String current date and time.
     */
    public static String getTimeStamp()
    {
        return DATE_FORMAT.format(new Date());
    }
}
