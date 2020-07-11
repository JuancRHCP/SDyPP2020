package edu.unlu.sdypp.ej4.client;

import edu.unlu.sdypp.ej4.util.ImageUtils;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Client {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(Client.class);
    private final String ip;
    private final int port;

    public Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
        try {
            List<ImageIcon> pictures = getImagesInFolder(new File("images"));
            long globalTime = System.currentTimeMillis();
            for (ImageIcon img : pictures) {
                long partialTime = System.currentTimeMillis();
                Date date = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-ms");
                new File("sobel").mkdir();
                File file = new File("sobel" + File.separator + dateFormat.format(date) + ".png");
                LOGGER.info("{} --> {}", img.getDescription(), file.getName());

                DrawBorders task = new DrawBorders(img);
                ImageIcon processedImage = getImageBorders(task);
                LOGGER.info("Finished SOBEL processing ({}ms) for: {}",
                        System.currentTimeMillis() - partialTime, img.getDescription());

                Iterator writers = ImageIO.getImageWritersByFormatName("png");
                ImageWriter writer = (ImageWriter) writers.next();
                if (writer == null) {
                    throw new RuntimeException("PNG not supported?!");
                }
                try {
                    if (processedImage == null) {
                        LOGGER.error("Error getting result for image {}", img.getDescription());
                    } else {
                        ImageOutputStream out = ImageIO.createImageOutputStream(file);
                        writer.setOutput(out);
//                writer.write(toBufferedImage(processedImage.getImage()));
                        writer.write(ImageUtils.imageIconToBufferedImage(processedImage));
                        out.close(); // close flushes buffer
                    }
                } catch (IOException e) {
                    LOGGER.error("Error saving file: ", e);
                }
            }
            LOGGER.info("Total time: {}ms", System.currentTimeMillis() - globalTime);
        } catch (ConnectException e) {
            LOGGER.error("Error trying to connect to server: {}:{}. Exiting...", this.ip, this.port);
            System.exit(1);
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("Unexpected error. Exiting...", e);
            System.exit(1);
        }
    }


    private ImageIcon getImageBorders(DrawBorders drawBorders) throws IOException, ClassNotFoundException {
        Socket socket = new Socket(this.ip, this.port);
        LOGGER.info("Connected to server");
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
        os.writeObject(drawBorders);

        Object response = inputStream.readObject();
        ImageIcon result = (ImageIcon) response;
        socket.close();
        return result;
    }


    private List<ImageIcon> getImagesInFolder(File folder) {
        List<ImageIcon> result = new ArrayList<>();
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isFile()) {
                if (fileEntry.getName().toLowerCase().endsWith("jpg") || fileEntry.getName().toLowerCase().endsWith("png")) {
                    try {
                        result.add(new ImageIcon(fileEntry.getPath()));
                    } catch (Exception e) {
                        LOGGER.error("Error trying to load image: {}", fileEntry.getPath());
                    }
                }
            }
        }
        LOGGER.info("Images found: {}", result);
        return result;
    }

}
