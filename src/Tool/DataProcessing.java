//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package Tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import weka.core.Instances;

public class DataProcessing {
    int[][] originalFormalContext;
    int[][] samplingFormalContext;
    int[][] trainingFormalContext;
    int[][] testingFormalContext;

    public DataProcessing() {
    }

    public void obtainArffFormalContext(String paraFilename, int paraNumUsers, int paraNumItems) {
        Instances data = null;

        try {
            FileReader fileReader = new FileReader(paraFilename);
            data = new Instances(fileReader);
            fileReader.close();
        } catch (Exception var8) {
            System.out.println("Cannot read the file: " + paraFilename + "\r\n" + var8);
            System.exit(0);
        }

        this.originalFormalContext = new int[paraNumUsers][paraNumItems];

        for(int i = 0; i < data.numInstances(); ++i) {
            int tempUser = (int)data.instance(i).value(0);
            int tempItem = (int)data.instance(i).value(1);
            this.originalFormalContext[tempUser][tempItem] = 1;
        }

    }

    public static int[][] toArrayByInputStreamReader(String paraName, int paraRow, int paraColumn) {
        ArrayList arrayList = new ArrayList();

        try {
            File file = new File(paraName);
            InputStreamReader input = new InputStreamReader(new FileInputStream(file));
            BufferedReader bf = new BufferedReader(input);

            String str;
            while((str = bf.readLine()) != null) {
                arrayList.add(str);
            }

            bf.close();
            input.close();
        } catch (IOException var13) {
            var13.printStackTrace();
        }

        int length = arrayList.size();
        int[][] array = new int[paraRow][paraColumn];

        for(int i = 0; i < length; ++i) {
            int tempUser = Integer.parseInt(((String)arrayList.get(i)).split("\t")[0]);
            int tempItem = Integer.parseInt(((String)arrayList.get(i)).split("\t")[1]);
            double tempRate = Double.parseDouble(((String)arrayList.get(i)).split("\t")[2]);
            if (tempUser > paraRow) {
                break;
            }

            if (tempRate == 0.0D) {
                tempRate = 0.2D;
            }

            array[tempUser - 1][tempItem - 1] = (int)(tempRate * 5.0D);
        }

        return array;
    }

    public int[][] readFCFromFile(String paraFormalContextURL) {
        try {
            FileReader fr = new FileReader(paraFormalContextURL);
            BufferedReader br = new BufferedReader(fr);
            String str1 = br.readLine();
            int row = Integer.parseInt(str1);
            str1 = br.readLine();
            int column = Integer.parseInt(str1);
            int tempI = 0;

            int[][] tempFormalContext;
            for(tempFormalContext = new int[row][]; str1 != null; ++tempI) {
                str1 = br.readLine();
                if (str1 == null) {
                    break;
                }

                tempFormalContext[tempI] = this.obtainFCRow(str1, column);
            }

            br.close();
            fr.close();
            return tempFormalContext;
        } catch (Exception var9) {
            var9.printStackTrace();
            return null;
        }
    }

    int[] obtainFCRow(String paraRow, int paraColumn) {
        int[] tempRow = new int[paraColumn];
        String tempSubString = paraRow.substring(0, paraRow.length());
        String[] tempAllElement = tempSubString.split(",");

        for(int i = 0; i < tempAllElement.length; ++i) {
            int tempElement = Integer.parseInt(tempAllElement[i]);
            tempRow[i] = tempElement;
        }

        return tempRow;
    }

    void obtainTrainingAndTestingFC(int[][] originFormalContext) {
        int userNum = originFormalContext.length;
        int itemNum = originFormalContext[0].length;
        int randomTime = (int)((double)(userNum * itemNum) * 0.2D);
        this.trainingFormalContext = new int[userNum][itemNum];
        this.testingFormalContext = new int[userNum][itemNum];

        int i;
        for(i = 0; i < originFormalContext.length; ++i) {
            for(int j = 0; j < originFormalContext[0].length; ++j) {
                this.trainingFormalContext[i][j] = originFormalContext[i][j];
            }
        }

        for(i = 0; i < randomTime; ++i) {
            Random random = new Random();
            int user = random.nextInt(userNum);
            int item = random.nextInt(itemNum);
            if (originFormalContext[user][item] > 0) {
                this.trainingFormalContext[user][item] = 0;
                this.testingFormalContext[user][item] = originFormalContext[user][item];
            }
        }

    }

    void obtainTrainingAndTestingFormalContext(int[][] paraFormalContext, double paraRatio) {
        this.testingFormalContext = new int[paraFormalContext.length][];
        this.trainingFormalContext = new int[paraFormalContext.length][];
        int tempModel = 1000 / (int)(paraRatio * 1000.0D);
        int temCount = 1;

        for(int i = 0; i < paraFormalContext.length; ++i) {
            this.testingFormalContext[i] = new int[paraFormalContext[i].length];
            this.trainingFormalContext[i] = new int[paraFormalContext[i].length];

            for(int j = 0; j < paraFormalContext[i].length; ++j) {
                if (paraFormalContext[i][j] > 0) {
                    ++temCount;
                    if (temCount % tempModel == 1) {
                        this.testingFormalContext[i][j] = paraFormalContext[i][j];
                    } else {
                        this.trainingFormalContext[i][j] = paraFormalContext[i][j];
                    }
                }
            }
        }

    }

    double computeMatrixSparse(int[][] paraMatrix) {
        int tempCount = 0;

        for(int i = 0; i < paraMatrix.length; ++i) {
            for(int j = 0; j < paraMatrix[0].length; ++j) {
                if (paraMatrix[i][j] > 0) {
                    ++tempCount;
                }
            }
        }

        double sparse = (double)tempCount / (double)(paraMatrix.length * paraMatrix[0].length);
        return sparse;
    }

    int[] obtainPopularityForAttribute(int paraItemIndex) {
        int[] tempIntUserIndex = new int[this.originalFormalContext.length];
        int tempIntUserIndexCount = 0;

        for(int i = 0; i < this.originalFormalContext.length; ++i) {
            if (this.originalFormalContext[i][paraItemIndex] == 1) {
                tempIntUserIndex[tempIntUserIndexCount] = i;
                ++tempIntUserIndexCount;
            }
        }

        int[] finalIntUserIndex = new int[tempIntUserIndexCount];

        for(int i = 0; i < tempIntUserIndexCount; ++i) {
            finalIntUserIndex[i] = tempIntUserIndex[i];
        }

        return finalIntUserIndex;
    }

    int[] obtainPopularityForObject(int paraUserIndex) {
        int[] tempIntItemIndex = new int[this.originalFormalContext[0].length];
        int tempIntItemIndexCount = 0;

        for(int i = 0; i < this.originalFormalContext[0].length; ++i) {
            if (this.originalFormalContext[paraUserIndex][i] == 1) {
                tempIntItemIndex[tempIntItemIndexCount] = i;
                ++tempIntItemIndexCount;
            }
        }

        int[] finalIntItemIndex = new int[tempIntItemIndexCount];

        for(int i = 0; i < tempIntItemIndexCount; ++i) {
            finalIntItemIndex[i] = tempIntItemIndex[i];
        }

        return finalIntItemIndex;
    }

    int[] weightSortReturnIndex(double[] paraWeight) {
        int[] tempSortedIndex = new int[paraWeight.length];
        Arrays.fill(tempSortedIndex, -1);

        for(int i = 0; i < tempSortedIndex.length; ++i) {
            double tempMax = 0.0D;

            for(int j = 0; j < paraWeight.length; ++j) {
                boolean tempHasBeenUsed = false;

                for(int k = 0; k < tempSortedIndex.length; ++k) {
                    if (j == tempSortedIndex[k]) {
                        tempHasBeenUsed = true;
                        break;
                    }
                }

                if (!tempHasBeenUsed && tempMax < paraWeight[j]) {
                    tempMax = paraWeight[j];
                    tempSortedIndex[i] = j;
                }
            }
        }

        return tempSortedIndex;
    }

    void obtainSamplingFormalContext(int paraObjectSampleNum, int paraAttributeSampleNum, int paraSamplingSteplength) {
        this.samplingFormalContext = new int[paraObjectSampleNum][paraAttributeSampleNum];
        int tempMaxItemNumber = paraSamplingSteplength * paraAttributeSampleNum;
        int tempMaxUserNumber = paraSamplingSteplength * paraObjectSampleNum;
        if (tempMaxUserNumber <= this.originalFormalContext.length && tempMaxItemNumber <= this.originalFormalContext[0].length) {
            double[] tempItemPopularity = new double[this.originalFormalContext[0].length];

            int[] tempSamplingItemset;
            for(int i = 0; i < this.originalFormalContext[0].length; ++i) {
                tempSamplingItemset = this.obtainPopularityForAttribute(i);
                tempItemPopularity[i] = (double)tempSamplingItemset.length + 0.0D;
            }

            int[] tempSortedItemPopularityIndex = this.weightSortReturnIndex(tempItemPopularity);
            tempSamplingItemset = new int[paraAttributeSampleNum];
            int tempSamplingItemsetCount = 0;

            for(int i = 0; i < tempMaxItemNumber; ++i) {
                if (i % paraSamplingSteplength == 0) {
                    tempSamplingItemset[tempSamplingItemsetCount] = tempSortedItemPopularityIndex[i];
                    ++tempSamplingItemsetCount;
                }
            }

            double[] tempUserPopularity = new double[this.originalFormalContext.length];

            int[] tempSamplingUserset;
            for(int i = 0; i < this.originalFormalContext.length; ++i) {
                tempSamplingUserset = this.obtainPopularityForObject(i);
                tempUserPopularity[i] = (double)tempSamplingUserset.length + 0.0D;
            }

            int[] tempSortedUserPopularityIndex = this.weightSortReturnIndex(tempUserPopularity);
            tempSamplingUserset = new int[paraObjectSampleNum];
            int tempSamplingUsersetCount = 0;

            int tempCountOne;
            for(tempCountOne = 0; tempCountOne < tempMaxUserNumber; ++tempCountOne) {
                if (tempCountOne % paraSamplingSteplength == 0) {
                    tempSamplingUserset[tempSamplingUsersetCount] = tempSortedUserPopularityIndex[tempCountOne];
                    ++tempSamplingUsersetCount;
                }
            }

            tempCountOne = 0;
            this.samplingFormalContext = new int[tempSamplingUserset.length][tempSamplingItemset.length];

            for(int i = 0; i < tempSamplingUserset.length; ++i) {
                for(int j = 0; j < tempSamplingItemset.length; ++j) {
                    this.samplingFormalContext[i][j] = this.originalFormalContext[tempSamplingUserset[i]][tempSamplingItemset[j]];
                    if (this.samplingFormalContext[i][j] == 1) {
                        ++tempCountOne;
                    }
                }
            }

            System.out.println("the sparise is " + (float)tempCountOne / (float)(this.samplingFormalContext.length * this.samplingFormalContext[0].length));
        } else {
            System.out.println("Invalid parameters.");
        }
    }

    void obtainSamplingFormalContext2(int paraObjectSampleNum, int paraAttributeSampleNum, int paraSamplingSteplength) {
        this.samplingFormalContext = new int[paraObjectSampleNum][paraAttributeSampleNum];
        int tempMaxItemNumber = paraSamplingSteplength * paraAttributeSampleNum;
        int tempMaxUserNumber = paraSamplingSteplength * paraObjectSampleNum;
        if (tempMaxUserNumber <= this.originalFormalContext.length && tempMaxItemNumber <= this.originalFormalContext[0].length) {
            double[] tempItemPopularity = new double[this.originalFormalContext[0].length];

            int[] tempSamplingItemset;
            for(int i = 0; i < this.originalFormalContext[0].length; ++i) {
                tempSamplingItemset = this.obtainPopularityForAttribute(i);
                tempItemPopularity[i] = (double)tempSamplingItemset.length + 0.0D;
            }

            int[] tempSortedItemPopularityIndex = this.weightSortReturnIndex(tempItemPopularity);
            tempSamplingItemset = new int[paraAttributeSampleNum];
            int tempSamplingItemsetCount = 0;

            for(int i = 0; i < this.originalFormalContext[0].length && tempSamplingItemsetCount != 420; ++i) {
                if (i > 10 && i % 3 == 0) {
                    tempSamplingItemset[tempSamplingItemsetCount] = tempSortedItemPopularityIndex[i];
                    ++tempSamplingItemsetCount;
                }
            }

            double[] tempUserPopularity = new double[this.originalFormalContext.length];

            int[] tempSamplingUserset;
            for(int i = 0; i < this.originalFormalContext.length; ++i) {
                tempSamplingUserset = this.obtainPopularityForObject(i);
                tempUserPopularity[i] = (double)tempSamplingUserset.length + 0.0D;
            }

            int[] tempSortedUserPopularityIndex = this.weightSortReturnIndex(tempUserPopularity);
            tempSamplingUserset = new int[paraObjectSampleNum];
            int tempSamplingUsersetCount = 0;

            int tempCountOne;
            for(tempCountOne = 0; tempCountOne < 943 && tempSamplingUsersetCount != 200; ++tempCountOne) {
                if (tempCountOne % 4 == 0) {
                    tempSamplingUserset[tempSamplingUsersetCount] = tempSortedUserPopularityIndex[tempCountOne];
                    ++tempSamplingUsersetCount;
                }
            }

            tempCountOne = 0;
            this.samplingFormalContext = new int[tempSamplingUserset.length][tempSamplingItemset.length];

            for(int i = 0; i < tempSamplingUserset.length; ++i) {
                for(int j = 0; j < tempSamplingItemset.length; ++j) {
                    this.samplingFormalContext[i][j] = this.originalFormalContext[tempSamplingUserset[i]][tempSamplingItemset[j]];
                    if (this.samplingFormalContext[i][j] == 1) {
                        ++tempCountOne;
                    }
                }
            }

            System.out.println("the sparise is " + (float)tempCountOne / (float)(this.samplingFormalContext.length * this.samplingFormalContext[0].length));
        } else {
            System.out.println("Invalid parameters.");
        }
    }

    public String stringFormalContext(int[][] paraFormalContext) {
        String tempFormalContext = "";

        for(int i = 0; i < paraFormalContext.length; ++i) {
            tempFormalContext = tempFormalContext + Arrays.toString(paraFormalContext[i]) + "\r\n";
        }

        return tempFormalContext;
    }

    public String stringCompressedFormalContext(int[][] paraFormalContext) {
        String tempFormalContext = "";
        String formalContext = "";

        for(int i = 0; i < paraFormalContext.length; ++i) {
            for(int j = 0; j < paraFormalContext[0].length; ++j) {
                if (paraFormalContext[i][j] > 0) {
                    tempFormalContext = i + 1 + " " + (j + 1) + " " + paraFormalContext[i][j];
                    formalContext = formalContext + tempFormalContext + "\r\n";
                }
            }
        }

        return formalContext;
    }

    public void writeToFile(int[][] paraFormalContext, String paraWriteURL, int paraI) {
        String tempFormalContext = "";
        switch(paraI) {
            case 1:
                tempFormalContext = this.stringFormalContext(paraFormalContext);
                break;
            case 2:
                tempFormalContext = this.stringCompressedFormalContext(paraFormalContext);
        }

        File myFilePath = new File(paraWriteURL);

        try {
            if (!myFilePath.exists()) {
                myFilePath.createNewFile();
            }

            FileWriter resultFile = new FileWriter(myFilePath);
            resultFile.write(tempFormalContext);
            resultFile.flush();
            resultFile.close();
        } catch (Exception var8) {
            var8.printStackTrace();
        }

    }

    public int[][] readArffFile(String paraArffFilename, int paraNumUsers, int paraNumItems) {
        Instances data = null;

        try {
            FileReader fileReader = new FileReader(paraArffFilename);
            data = new Instances(fileReader);
            fileReader.close();
        } catch (Exception var9) {
            System.out.println("Cannot read the file: " + paraArffFilename + "\r\n" + var9);
            System.exit(0);
        }

        int[][] array = new int[paraNumUsers][paraNumItems];

        for(int i = 0; i < data.numInstances(); ++i) {
            int tempUser = (int)data.instance(i).value(0);
            int tempItem = (int)data.instance(i).value(1);
            array[tempUser][tempItem] = (int)data.instance(i).value(2);
        }

        return array;
    }

    int[][] readDat() {
        int userNumber = 6040;
        int itemNumber = 3952;
        String tempReaderfile = "data/ratings.dat";
        File tempFile = null;
        BufferedReader tempBufReader = null;
        String tempString = null;
//        int tempRating = false;
//        int tempUserIndex = false;
//        int tempItemIndex = false;
        String[] tempStrArray = null;
        tempFile = new File(tempReaderfile);
        if (!tempFile.exists()) {
            System.out.println("file not found");
            return null;
        } else {
            try {
                tempBufReader = new BufferedReader(new FileReader(tempFile));

                int[][] rating;
                int tempRating;
                int tempUserIndex;
                int tempItemIndex;
                for(rating = new int[userNumber][itemNumber]; (tempString = tempBufReader.readLine()) != null; rating[tempUserIndex - 1][tempItemIndex - 1] = tempRating) {
                    tempStrArray = tempString.split("::");
                    tempUserIndex = Integer.parseInt(tempStrArray[0]);
                    tempItemIndex = Integer.parseInt(tempStrArray[1]);
                    tempRating = Integer.parseInt(tempStrArray[2]);
                }

                tempBufReader.close();
                return rating;
            } catch (Exception var12) {
                var12.printStackTrace();
                return null;
            }
        }
    }

    public static void test1() {
        DataProcessing da = new DataProcessing();
        da.obtainArffFormalContext("data/ratings.arff", 943, 1682);
        da.obtainSamplingFormalContext2(200, 420, 2);
        System.out.println("origin sparse : " + da.computeMatrixSparse(da.samplingFormalContext));
        da.obtainTrainingAndTestingFC(da.samplingFormalContext);
        String tempTrainingFormalContextURL = "data/200x420Tr-s0.0915.txt";
        da.writeToFile(da.trainingFormalContext, tempTrainingFormalContextURL, 1);
        System.out.println("1");
        String tempTestingFormalContextURL = "data/200x420Te-s0.0915.txt";
        da.writeToFile(da.testingFormalContext, tempTestingFormalContextURL, 1);
    }

    public static void test2() {
        DataProcessing da = new DataProcessing();
        String tempTrainingURL = "QIQI/douban_train_matrix_01.txt";
        String tempTestingURL = "QIQI/douban_test_matrix_01.txt";
        String tempTrainCompressURL = "QIQI/douban_train_compress_01.txt";
        String tempTestCompressURL = "QIQI/douban_test_compress_01.txt";
        int[][] train = da.readFCFromFile(tempTrainingURL);
        da.writeToFile(train, tempTrainCompressURL, 2);
        int[][] test = da.readFCFromFile(tempTestingURL);
        da.writeToFile(test, tempTestCompressURL, 2);
    }

    public static void main(String[] args) {
        test2();
    }
}
