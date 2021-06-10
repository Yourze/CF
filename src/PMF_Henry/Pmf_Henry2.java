//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package PMF_Henry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Random;
import PMF.DataInfo;

public class Pmf_Henry2 {
    static String trainPath = new String("src/data/200X420-1-train.txt");
    static String testPath = new String("src/data/200X420-1-test.txt");
    static String split_Sign = new String(" ");
    static int[][] train;
    static int[][] test;

    public Pmf_Henry2() {
    }

    public static void main(String[] args) throws NumberFormatException, IOException {
        initFeature();
        readTrainData(trainPath, split_Sign);
        readTestData(testPath, split_Sign);
        System.out.println("Begin Training ! ! !");

        for(int i = 0; i < DataInfo.round; ++i) {
            System.out.println("round:  " + (i + 1));
            update_one();
            F1();
        }

    }

    static void initFeature() {
        Random rand = new Random();

        int i;
        int j;
        for(i = 0; i < DataInfo.userNumber; ++i) {
            for(j = 0; j < DataInfo.featureNumber; ++j) {
                DataInfo.userFeature[i][j] = 0.01D * rand.nextDouble();
            }
        }

        for(i = 0; i < DataInfo.itemNumber; ++i) {
            for(j = 0; j < DataInfo.featureNumber; ++j) {
                DataInfo.itemFeature[i][j] = 0.01D * rand.nextDouble();
            }
        }

    }

    static void readTrainData(String trainPath, String split_Sign) throws NumberFormatException, IOException {
        File file = new File(trainPath);
        BufferedReader buffRead = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        double sum = 0.0D;
        int index = 0;

        double rating;
        for(train = new int[200][420]; buffRead.ready(); sum += rating) {
            String str = buffRead.readLine();
            String[] parts = str.split(split_Sign);
            int user = Integer.parseInt(parts[0]);
            int item = Integer.parseInt(parts[1]);
            rating = Double.parseDouble(parts[2]);
            train[user][item] = (int)rating;
            DataInfo.user_record[index] = user;
            DataInfo.item_record[index] = item;
            DataInfo.rate_record[index] = rating;
            ++index;
        }

        DataInfo.mean_rating = sum / (double)DataInfo.trainNumber;

        for(int i = 0; i < DataInfo.trainNumber; ++i) {
            double tmp = Double.valueOf(DataInfo.rate_record[i]) - DataInfo.mean_rating;
            DataInfo.rate_record[i] = tmp;
        }

        buffRead.close();
    }

    static void readTestData(String testPath, String split_Sign) throws IOException {
        File file = new File(testPath);
        BufferedReader buffRead = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        test = new int[200][420];

        while(buffRead.ready()) {
            String str = buffRead.readLine();
            String[] parts = str.split(split_Sign);
            int user = Integer.parseInt(parts[0]);
            int item = Integer.parseInt(parts[1]);
            double rate = Double.parseDouble(parts[2]);
            test[user][item] = 1;
            DataInfo.userTest.add(user);
            DataInfo.itemTest.add(item);
            DataInfo.rateTest.add(rate);
        }

        buffRead.close();
    }

    static double predict(int paraUserId, int paraItemId) {
        double tempPredict = 0.0D;

        for(int i = 0; i < DataInfo.featureNumber; ++i) {
            tempPredict += DataInfo.userFeature[paraUserId][i] * DataInfo.itemFeature[paraItemId][i];
        }

        return tempPredict;
    }

    public static void update_one() {
        for(int i = 0; i < DataInfo.trainNumber; ++i) {
            int tempUserId = Integer.valueOf(DataInfo.user_record[i]);
            int tempItemId = Integer.valueOf(DataInfo.item_record[i]);
            double tempRate = Double.valueOf(DataInfo.rate_record[i]);
            double tempVary = predict(tempUserId, tempItemId) - tempRate;

            int j;
            double tmp;
            for(j = 0; j < DataInfo.featureNumber; ++j) {
                tmp = tempVary * DataInfo.itemFeature[tempItemId][j] + DataInfo.lambda_u * DataInfo.userFeature[tempUserId][j];
                DataInfo.userFeature[tempUserId][j] -= DataInfo.alpha * tmp;
            }

            for(j = 0; j < DataInfo.featureNumber; ++j) {
                tmp = tempVary * DataInfo.userFeature[tempUserId][j] + DataInfo.lambda_v * DataInfo.itemFeature[tempItemId][j];
                DataInfo.itemFeature[tempItemId][j] -= DataInfo.alpha * tmp;
            }
        }

    }

    static double eval() {
        double rmse = 0.0D;
        Iterator userIter = DataInfo.userTest.iterator();
        Iterator ItemIter = DataInfo.itemTest.iterator();

        double rate;
        for(Iterator rateIter = DataInfo.rateTest.iterator(); userIter.hasNext() && ItemIter.hasNext() && rateIter.hasNext(); rmse += rate * rate) {
            int a = (Integer)userIter.next();
            int b = (Integer)ItemIter.next();
            double c = (Double)rateIter.next();
            rate = predict(a, b) + DataInfo.mean_rating;
            if (rate < 1.0D) {
                rate = 1.0D;
            }

            if (rate > 5.0D) {
                rate = 5.0D;
            }

            rate -= c;
        }

        return Math.sqrt(rmse / (double)DataInfo.userTest.size());
    }

    static void F1() {
        Iterator userIter = DataInfo.userTest.iterator();
        Iterator ItemIter = DataInfo.itemTest.iterator();
        Iterator rateIter = DataInfo.rateTest.iterator();
        int tempRecommend = 0;
        int tempCorrect = 0;
        int tempMiss = 0;
        int tempRated = 0;
        boolean[] tempItem = new boolean[train[0].length];

        int i;
        int j;
        for(i = 0; i < train.length; ++i) {
            for(j = 0; j < train[0].length; ++j) {
                if (train[i][j] == 0) {
                    double rate = predict(i, j) + DataInfo.mean_rating;
                    if (rate > 3.0D && test[i][j] > 0) {
                        ++tempRecommend;
                        ++tempRated;
                        ++tempCorrect;
                        tempItem[j] = true;
                    } else if (rate <= 3.0D && test[i][j] > 0) {
                        ++tempRated;
                        ++tempMiss;
                    } else if (rate > 3.0D && test[i][j] == 0) {
                        ++tempRecommend;
                        tempItem[j] = true;
                    }
                }
            }
        }

        i = 0;

        for(j = 0; j < tempItem.length; ++j) {
            if (tempItem[j]) {
                ++i;
            }
        }

        double tempPrecision = 1.0D * (double)tempCorrect / (double)tempRecommend;
        double tempRecall = 1.0D * (double)tempCorrect / (double)tempRated;
        double tempCoverage = 1.0D * (double)i / (double)tempItem.length;
        double F1 = 2.0D * tempPrecision * tempRecall / (tempPrecision + tempRecall);
        System.out.println("Precision = " + String.format("%.4f", tempPrecision) + " recall = " + String.format("%.4f", tempRecall) + " F1 = " + String.format("%.4f", F1) + " Coverage = " + String.format("%.4f", tempCoverage));
    }
}
