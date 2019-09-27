import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Main
{
    private static int SIZE_Y = 50, SIZE_X = 50, Y = 0;
    private static final int COLUMNS = 20;
    private static final String DOMAIN = "https://www.doc-solus.fr", PATH = "C:\\Users\\Benjamin LOISON\\Desktop\\BensFolder\\DEV\\Java\\eclipse\\DocSolus decoder\\pic\\", PREFIX = "</script><a href=\"/prepa/sci/adc/bin/view.question.html?q=";

    public static void waitForPageLoaded(WebDriver driver)
    {
        ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>()
        {
            public Boolean apply(WebDriver driver)
            {
                return ((JavascriptExecutor)driver).executeScript("return document.readyState").toString().equals("complete");
            }
        };
        try
        {
            Thread.sleep(100);
            new WebDriverWait(driver, 30).until(expectation);
        }
        catch(Throwable error)
        {
            // time out
        }
    }

    private static void writeImage(String path, String data)
    {
        try(OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(new File(path + ".jpg"))))
        {
            outputStream.write(DatatypeConverter.parseBase64Binary(data.split(",")[1]));
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        long time = new Date().getTime();

        try
        {
            System.setProperty("webdriver.chrome.driver", "C:\\Users\\Benjamin LOISON\\Desktop\\BensFolder\\DEV\\Java\\Libs\\chromedriver.exe");
            WebDriver driver = new ChromeDriver();

            driver.navigate().to(new URL(DOMAIN));
            driver.manage().addCookie(new Cookie("ck_id", "YOUR_ID"));

            File file = new File("contests.txt");
            Scanner scan = new Scanner(file);
            while(scan.hasNextLine())
            {
                String contest = scan.nextLine();
                driver.navigate().to(new URL(DOMAIN + "/prepa/sci/adc/bin/view.corrige.html?q=" + contest));

                waitForPageLoaded(driver);
                
                for(String url : driver.getPageSource().split("\n"))
                {
                    if(url.startsWith(PREFIX + contest + "__q"))
                    {
                        url = url.replaceFirst("</script><a href=\"", "");
                        String niceName = url.split(">")[1].replace("</a", "").replace(".", "_");
                        url = DOMAIN + url.split("\"")[0];
                        System.out.println(url);
                        
                        driver.navigate().to(new URL(url));
                        int imageIndex = 0;

                        new File(PATH + contest + File.separatorChar).mkdir();
                        
                        Thread.sleep(3000);
                        System.out.println("here");
                        
                        Y = 0;
                        
                        for(String line : driver.getPageSource().split("tbody>")[5].replace("</tr></tbody></table></div>", "").split("</td>"))
                        {
                            line = line.replaceAll(" ", "");
                            // System.out.println(line);
                            if(line.contains("<tr>"))
                                Y++;
                            if(line.contains("src=\"data:image/jpeg;base64"))
                            {
                                String content = line.split("src=\"")[1].replace("\"></td>", "");
                                writeImage(PATH + imageIndex, content);
                                imageIndex++;
                            }
                        }

                        File initSizeFile = new File(PATH + "0.jpg");
                        BufferedImage img = ImageIO.read(initSizeFile);
                        SIZE_X = img.getTileWidth();
                        SIZE_Y = img.getTileHeight();
                        
                        BufferedImage combined = new BufferedImage(20 * SIZE_X, Y * SIZE_Y, BufferedImage.TYPE_INT_RGB);
                        Graphics g = combined.getGraphics();
                        for(int lineIndex = 0; lineIndex < Y; lineIndex++)
                            for(int columnIndex = 0; columnIndex < COLUMNS; columnIndex++)
                            {
                                String path = PATH + (columnIndex + lineIndex * COLUMNS) + ".jpg";
                                //System.out.println(path);
                                File pic = new File(path);
                                if(!pic.exists())
                                    break;
                                g.drawImage(ImageIO.read(pic), columnIndex * SIZE_X, lineIndex * SIZE_Y, null);
                                pic.delete();
                            }
                        ImageIO.write(combined, "JPG", new File(PATH + contest + File.separatorChar + niceName + ".jpg"));
                    }
                }                
            }
            scan.close();
            driver.close();
            System.out.println("Finished in " + (new Date().getTime() - time) + " ms !");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}

/*

Correction of:

Maths: CCP, Centrale, E3A, Mines, X
Physique: CCP, Centrale, Mines, X
Info: Centrale, Mines, X
Chimie: CCP, Mines

 */
