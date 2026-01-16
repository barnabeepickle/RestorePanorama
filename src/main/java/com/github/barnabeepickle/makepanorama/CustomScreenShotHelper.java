package com.github.barnabeepickle.makepanorama;

import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ScreenShotHelper;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.github.barnabeepickle.makepanorama.MakePanoramaMod.LOGGER;

@SideOnly(Side.CLIENT)
public class CustomScreenShotHelper {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    /**
     * Takes a Screenshot from a {@code Framebuffer}. Override.
     * @param directory The directory the screenshots will be saved in.
     * @param name The name of the screenshot PNG.
     * @param framebuffer The {@code Framebuffer} which the image should be take from.
     * @param downscaleFactor An integer amount the final image should be downscaled by.
     */
    public static void saveScreenshot(
            File directory,
            String name,
            Framebuffer framebuffer,
            int downscaleFactor
    ) {
        saveScreenshot(directory, name, framebuffer.framebufferWidth, framebuffer.framebufferHeight, framebuffer, 1);
    }

    /**
     * Takes a Screenshot from a {@code Framebuffer}, with no downscale. Override.
     * @param directory The directory the screenshots will be saved in.
     * @param name The name of the screenshot PNG.
     * @param framebuffer The {@code Framebuffer} which the image should be take from.
     */
    public static void saveScreenshot(
            File directory,
            String name,
            Framebuffer framebuffer
    ) {
        saveScreenshot(directory, name, framebuffer.framebufferWidth, framebuffer.framebufferHeight, framebuffer, 1);
    }

    /**
     * Takes a Screenshot from a {@code Framebuffer}, with no downscale. Override.
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
    ) {
        saveScreenshot(directory, name, width, height, framebuffer, 1);
    }

    /**
     * Takes a Screenshot from a {@code Framebuffer}.
     * @param directory The directory the screenshots will be saved in.
     * @param name The name of the screenshot PNG.
     * @param width The {@code width} of the image to be saved.
     * @param height The {@code height} of the image to be saved.
     * @param framebuffer The {@code Framebuffer} which the image should be take from.
     * @param downscaleFactor An integer amount the final image should be downscaled by.
     */
    public static void saveScreenshot(
            File directory,
            String name,
            int width,
            int height,
            Framebuffer framebuffer,
            int downscaleFactor
    )
    {
        // Effectively clamps the downscaleFactor to atleast 1
        if (downscaleFactor <= 1) {
            downscaleFactor = 1;
        }
        try {
            // Use ScreenShotHelper.createScreenshot get a BufferedImage of the screen
            BufferedImage bufferedImage = ScreenShotHelper.createScreenshot(width, height, framebuffer);

            // Create a new File for the image
            File file = new File(directory, name).getCanonicalFile();
            // get the file
            ScreenshotEvent event = ForgeHooksClient.onScreenshot(bufferedImage, file);
            file = event.getScreenshotFile();

            // We check to see if we have a downscaleFactor we can act on
            BufferedImage finalImage;
            if (downscaleFactor != 1) {
                // Use .getScaledInstance to scale the image by the downscaleFactor
                Image scaledImage = bufferedImage.getScaledInstance(width / downscaleFactor, height / downscaleFactor, Image.SCALE_SMOOTH);

                // ImageIO.write needs something with a super class of RenderedImage and the easiest way to do that is
                // to convert back to a BufferedImage so we make a make new one then use Graphics2D to convert
                finalImage = new BufferedImage(width / downscaleFactor, height / downscaleFactor, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = finalImage.createGraphics();
                g2d.drawImage(scaledImage, 0, 0, null);
                g2d.dispose();
            } else {
                finalImage = bufferedImage;
            }

            // Finally we write the BufferedImage to the file
            ImageIO.write(finalImage, "png", file);
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
