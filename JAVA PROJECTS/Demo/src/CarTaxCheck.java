import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
//import java.io.FileNotFoundException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
//import org.openqa.selenium.WebDriverWait;
//import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
//import org.apache.commons.io.FileUtils;
import java.util.concurrent.TimeUnit;
//import java.util.date;


/*****************************************************************************************
 * Write a test automation suite which does following.
 * 
 *  1. Reads given input file: car_input.txt
 *  2. Extracts vehicle registration numbers based on pattern(s).
 *  3. Each number extracted from input file is fed to https://cartaxcheck.co.uk/ 
 *     (Perform Free Car Check)
 *  4. Compare the output returned by https://cartaxcheck.co.uk/ with given car_output.txt
 *  5. Highlight/fail the test for any mismatches.
 * 
 *  Showcase your skills so its easier to add more input files in future. 
 *  Utilise any JVM based language with broser automation tools. 
 *  Use design patterns where appropriate
 *****************************************************************************************/

public class CarTaxCheck {

    public static int j, k;
    public static String[] AddPlate(String[] regplates, String regplatetoadd)
    {
        // increment array by 1 every time we add a new record
        String[] updatedArray = new String[regplates.length + 1]; // increase array by one more
        // remove embedded space character from plate number if present loop over char array to remove
        char[] platechars = regplatetoadd.toCharArray();
        int len = platechars.length;
        for (int i = 0; i < len; i++) {
            if (platechars[i] == ' ') {
                --len;
                System.arraycopy(platechars, i + 1, platechars, i, len - i);
                --i;
            }
        }
        // convert back to a string and add to array
        regplatetoadd = new String(platechars, 0, len);

        for(int i = 0; i < regplates.length; i++)
            updatedArray[i] = regplates[i];
        updatedArray[updatedArray.length - 1] = regplatetoadd;
        return updatedArray;
    }

    public static String[] getFiles(String folderPath)
    {
        String inputFileFullPath = "";
        File inputFilePath = new File(folderPath);
        String fileList[] = inputFilePath.list();
        Pattern inputFileCheck = Pattern.compile(".*_input.*");
        String [] inputFileFullPathList = new String[fileList.length];
        for (int i=0; i<fileList.length; i++)  
        {
            Matcher m = inputFileCheck.matcher(fileList[i]);
            boolean inputfilefound = m.find();
            if (inputfilefound) 
            {
                inputFileFullPath = folderPath + "\\"+ fileList[i];
                System.out.println(inputFileFullPath);
                inputFileFullPathList[i] = inputFileFullPath;
            }
        }
        return inputFileFullPathList;
    }

    public static void main(String[] args) throws Exception 
    {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Dell\\Drivers\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        //WebDriverWait wait = new WebDriverWait(driver, 10);
        
        // Read and Parse the Test File Extracting all Registration Plate Information
        // Add Regplates to an Array

        // List regplates = new ArrayList<String>();
        String[] regplates = {};
        // regex pattern to match purely the number plate, use matcher to extract all occurences
        Pattern regptn = Pattern.compile("(([A-Z]){2}\\d{2}\\s?([A-Z]){3})");

        //String FilePath = "C:\\Users\\Dell\\source\\repos\\JAVA PROJECTS\\data_files\\\\input_data\\car_input.txt";
        String[] InputFileList = getFiles("C:\\Users\\Dell\\source\\repos\\JAVA PROJECTS\\data_files\\input_data");
        String FilePath = "";
        System.out.println("CarInput Files Available ["+ String.join(",",InputFileList) +"]");

        for (int flcount=0; flcount<InputFileList.length; flcount++)  
        {
            FilePath = InputFileList[flcount];
            try {
                FileReader reader = new FileReader(FilePath);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    //System.out.println(line);
                    Matcher check = regptn.matcher(line);

                    while(check.find()) {
                        String plate = check.group(1);
                        regplates = AddPlate(regplates,plate);
                        //System.out.println(plate);            
                    }
                }
                reader.close();
    
            } catch (IOException e) {
                e.printStackTrace();
            }

            LinkedList<String[]> vehicles = new LinkedList<String[]>();
            String separator = ",";
            FilePath = "C:\\Users\\Dell\\source\\repos\\JAVA PROJECTS\\data_files\\output_data\\car_output.txt";
            try {
                FileReader reader = new FileReader(FilePath);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    // split the csv line by comma separation into a string array
                    String[] vehicleAttributes = line.split(separator);
                    //System.out.println("CarOutput File - Data Loaded ["+ String.join(",",vehicleAttributes) +"]");
                    vehicles.addLast(vehicleAttributes);
                }
                reader.close();
    
            } catch (IOException e) {
                e.printStackTrace();
            }

            String[][] vehicleDataArray = vehicles.toArray(new String[vehicles.size()][]);

            // Search for each Registration Number Plate entered into https://cartaxcheck.co.uk/ - scroll down to
            // capture vehicle details
            for(int i=0; i < regplates.length; i++) 
            {
                driver.get("https://cartaxcheck.co.uk/");
                WebElement vrmentryfield = driver.findElement(By.name("vrm"));
                //System.out.println(regplates[i]);  // DEBUG

                vrmentryfield.sendKeys(regplates[i]);
                vrmentryfield.sendKeys(Keys.RETURN);
                //vrmentryfield.sendKeys(Keys.F5);
                //vrmentryfield.submit();
                Thread.sleep(2000);
                String resultLoadedCheck = driver.getTitle();
                String expectedTitle = "Car Tax Check | Free Vehicle Check | "+regplates[i];
                //Assert.assertEquals(expectedTitle,resultLoadedCheck);
                System.out.println("PAGE CHECK - '"+ expectedTitle + "' <> '" + resultLoadedCheck + "' || CHECK RESULT " + expectedTitle.equals(resultLoadedCheck));
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("window.scrollBy(0,620)", "");
            
                // verify the prescence of the expected data from the caroutput.txt file
                for (j = 1; j < vehicleDataArray.length; j++)
                {
                    //System.out.println("EXPECTEDvsCHECKED|"+vehicleDataArray[j][0]+"|"+regplates[i]+"|"); // DEBUG
                    String inputreg = regplates[i];
                    String expectedreg = vehicleDataArray[j][0];
                    if (inputreg.equals(expectedreg))
                    {
                        System.out.println("CHECKING "+expectedreg+" RETRIEVED DATA AGAINST EXPECTED"); // DEBUG
                        for (k = 0; k < vehicleDataArray[j].length - 1; k++)
                        {
                            String elementDatatoFind = vehicleDataArray[j][k];
                            System.out.println("verifying prescence of : "+elementDatatoFind);
                            try{
                                driver.findElement(By.xpath("//dd[text()='"+elementDatatoFind+"']"));
                            } catch (Exception e) {
                                System.out.println(expectedreg+" EXPECTED "+vehicleDataArray[0][k]+" : "+elementDatatoFind+" >> ERROR : NOT FOUND <<");
                                //TakesScreenshot screenshot = (TakesScreenshot)driver;
                                //File source = screenshot.getScreenshotAs(OutputType.FILE);
                                //String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
                                //FileUtils.copyFile(source,new File("C:\\Users\\Dell\\source\\repos\\JAVA PROJECTS\\screenshots\\error\\carfreecheck_"+expectedreg+"_"+timeStamp+"_FAIL"));
                            }
                        }
                    }
                }
                System.out.println();
                Thread.sleep(2000);
                js.executeScript("window.scrollBy(0,-620)", "");
                vrmentryfield = driver.findElement(By.name("vrm"));
                vrmentryfield.clear();
            }
        }
    driver.quit();
    }
}