package PMF_Henry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Random;

import PMF.DataInfo;
import Tool.*;

/**
 * 根据Pmf代码进行改写，主题结构没有发生变化
 *
 */

public class Pmf_Henry {
	// 输入文件的userID，itemID都是从1开始
	// trainPath的格式是userID，itemID，rate
	// testPath的格式是userID，itemID，rate
	// split_Sign表示的是输入文件中三元组的分隔符号
	static String trainPath = new String("data/movielens1m-CompressedTrain0.2.txt");//("data/eachMovie-2k-CompressedTrain0.3.txt");//("data/train_vec.txt");
	static String testPath = new String("data/movielens1m-CompressedTest0.2.txt");//("data/eachMovie-2k-CompressedTest0.3.txt");//("data/probe_vec.txt");
	static String split_Sign = new String(" ");
	static int[][] train;
	static int[][] test;
	/**
	 * 
	 * @param args
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static void main(String args[]) throws NumberFormatException, IOException {
		//Step 1. Initialize the feature matrices U and V
		initFeature();
		//Step 2. read the training and testing data
		readTrainData(trainPath, split_Sign);
		readTestData(testPath, split_Sign);

		//Step 3. update and predict
		System.out.println("Begin Training ! ! !");

		for (int i = 0; i < DataInfo.round; i++) {
			System.out.println("round:  " + (i + 1));
			//Step 3.1 根据预测结果进行更新（更新用的迭代公式非常关键）
			update_one();
			//Step 3.2 根据预测结果计算RMSE
//			System.out.println("RMSE: " + eval());
			F1();
		}//of for i
		// genResult();
	}// of main

	/**
	 * 给两个矩阵填上随机值
	 */
	static void initFeature() {

		Random rand = new Random();

		for (int i = 0; i < DataInfo.userNumber; i++){
			for (int j = 0; j < DataInfo.featureNumber; j++){
				DataInfo.userFeature[i][j] = 0.01 * rand.nextDouble();
			}//of for j
		}//Of for i

		//SimpleTool.printMatrix(DataInfo.userFeature);
		for (int i = 0; i < DataInfo.itemNumber; i++){
			for (int j = 0; j < DataInfo.featureNumber; j++){
				DataInfo.itemFeature[i][j] = 0.01 * rand.nextDouble();
			}//Of for j
		}//Of for i
	}

	/**
	 * 
	 * @param trainPath
	 * @param split_Sign
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	static void readTrainData(String trainPath, String split_Sign) throws NumberFormatException, IOException {
		File file = new File(trainPath);
		BufferedReader buffRead = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

		double sum = 0;
		int index = 0;
		train = new int[6040][3952];
		while (buffRead.ready()) {
			String str = buffRead.readLine();
			String[] parts = str.split(split_Sign);

			int user = Integer.parseInt(parts[0]) - 1;//user id
			int item = Integer.parseInt(parts[1]) - 1;//item id
			double rating = Double.parseDouble(parts[2]);//rating
			
			train[user][item] = (int) rating;
			DataInfo.user_record[index] = user;
			DataInfo.item_record[index] = item;
			DataInfo.rate_record[index] = rating;

			index++;
			sum += rating;//total rating
//			System.out.println("user: " + (user+1) + "  item： " + (item+1)+ "  rating: " + rating);
		}//Of while

		DataInfo.mean_rating = sum / DataInfo.trainNumber;//average rating
		for (int i = 0; i < DataInfo.trainNumber; i++) {
			double tmp = (Double) DataInfo.rate_record[i] - DataInfo.mean_rating;
			DataInfo.rate_record[i] = tmp;//原始评分-平均分
		}//of for i
		buffRead.close();
	}//Of readTrainData

	/**
	 * 
	 * @param testPath
	 * @param split_Sign
	 * @throws IOException
	 */
	static void readTestData(String testPath, String split_Sign) throws IOException {

		File file = new File(testPath);
		BufferedReader buffRead = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

		test = new int[6040][3952];
		int user, item;
		double rate;
		while (buffRead.ready()) {
			String str = buffRead.readLine();
			String[] parts = str.split(split_Sign);

			user = Integer.parseInt(parts[0]) - 1;
			item = Integer.parseInt(parts[1]) - 1;
			rate = Double.parseDouble(parts[2]);
			
			test[user][item] = (int) rate;
			DataInfo.userTest.add(user);
			DataInfo.itemTest.add(item);
			DataInfo.rateTest.add(rate);
//			System.out.println("user: " + (user+1) + "  item： " + (item+1)+ "  rating: " + rate);
		}//Of while

		buffRead.close();
	}//Of readTestData

	/**
	 * R_{i,j} = \sigma_{l \in [0, k]} U_{i,l} * V_{l, j}
	 * @param userId
	 * @param itemId
	 * @return
	 */
	static double predict(int paraUserId, int paraItemId) {
		double tempPredict = 0;
		for (int i = 0; i < DataInfo.featureNumber; i++){
			//User的行向量和Item列向量的乘积
			//这种存储方式非常精妙
			tempPredict += DataInfo.userFeature[paraUserId][i] * DataInfo.itemFeature[paraItemId][i];
		}//of for i
		return tempPredict;
	}//Of predict

	/**
	 * 所有基于矩阵分解的方法核心变化就在update_one上
	 */
	public static void update_one() {
		for (int i = 0; i < DataInfo.trainNumber; i++) {

			int tempUserId = (Integer) DataInfo.user_record[i];
			int tempItemId = (Integer) DataInfo.item_record[i];
			double tempRate = (Double) DataInfo.rate_record[i];

			double tempVary = predict(tempUserId, tempItemId) - tempRate;//残差

			for (int j = 0; j < DataInfo.featureNumber; j++) {
				double tmp = tempVary * DataInfo.itemFeature[tempItemId][j] + DataInfo.lambda_u * DataInfo.userFeature[tempUserId][j];
				DataInfo.userFeature[tempUserId][j] = DataInfo.userFeature[tempUserId][j] - DataInfo.alpha * tmp;
			}//of for j

			for (int j = 0; j < DataInfo.featureNumber; j++) {
				double tmp = tempVary * DataInfo.userFeature[tempUserId][j] + DataInfo.lambda_v * DataInfo.itemFeature[tempItemId][j];
				DataInfo.itemFeature[tempItemId][j] = DataInfo.itemFeature[tempItemId][j] - DataInfo.alpha * tmp;
			}//of for j
		}//of for i
	}//Of update_one

	/**
	 * Compute the RMSE
	 * @return
	 */
	static double eval() {
		double rmse = 0;
		@SuppressWarnings("rawtypes")
		Iterator userIter = DataInfo.userTest.iterator();
		@SuppressWarnings("rawtypes")
		Iterator ItemIter = DataInfo.itemTest.iterator();
		@SuppressWarnings("rawtypes")
		Iterator rateIter = DataInfo.rateTest.iterator();

		while (userIter.hasNext() && ItemIter.hasNext() && rateIter.hasNext()) {
			int a = (Integer) userIter.next();
			int b = (Integer) ItemIter.next();
			double c = (Double) rateIter.next();

			double rate = predict(a, b) + DataInfo.mean_rating;

			if (rate < 1)
				rate = 1;
			if (rate > 5)
				rate = 5;

			rate = rate - c;
			rmse += rate * rate;

			/*
			 * if(c == 0) { double x = -Math.log10(1-p); if(Double.isNaN(x)){
			 * System.err.println("real:  " + c + "    predict: " + p); } loss
			 * -= Math.log10(1-p); } else { double x = -Math.log10(p);
			 * if(Double.isNaN(x)){ System.err.println("real:  " + c +
			 * "    predict: " + p); } loss -= Math.log10(p); }
			 */
		}//Of while

		return Math.sqrt(rmse / DataInfo.userTest.size());
	}//Of eval
	
	/**
	 * Compute the RMSE
	 * @return
	 */
	static void F1() {
		@SuppressWarnings("rawtypes")
		Iterator userIter = DataInfo.userTest.iterator();
		@SuppressWarnings("rawtypes")
		Iterator ItemIter = DataInfo.itemTest.iterator();
		@SuppressWarnings("rawtypes")
		Iterator rateIter = DataInfo.rateTest.iterator();
		int tempRecommend = 0;
		int tempCorrect = 0;
		int tempMiss = 0;
		int tempRated = 0;
		
		for (int i = 0; i < train.length; i++) {
			for (int j = 0; j < train[0].length; j++) {
				if(train[i][j]==0) {
					double rate = predict(i, j) + DataInfo.mean_rating;
					if(rate >3 && test[i][j] > 0) {
						tempRecommend++;
						tempRated++;
						tempCorrect++;
					}else if(rate <= 3 && test[i][j] > 0) {
						tempRated++;
						tempMiss++;
					}else if(rate > 3 && test[i][j] == 0) {
						tempRecommend++;
					}
				}
			}
		}

//		while (userIter.hasNext() && ItemIter.hasNext() && rateIter.hasNext()) {
//			int a = (Integer) userIter.next();
//			int b = (Integer) ItemIter.next();
//			double c = (Double) rateIter.next();
//
//			double rate = predict(a, b) + DataInfo.mean_rating;
//
//			if(rate > 3 && c > 3) {
//				tempRecommend++;
//				tempRated++;
//				tempCorrect++;
//			}else if(rate <= 3 && c > 3) {
//				tempRated++;
//				tempMiss++;
//			}else if(rate > 3 && c <= 3) {
//				tempRecommend++;
//			}
//		}//Of while

		double tempPrecision = 1.0 * tempCorrect / tempRecommend;
		double tempRecall = 1.0 * tempCorrect / tempRated;
		double F1 = (2 * tempPrecision * tempRecall) / (tempPrecision + tempRecall);
		System.out.println("Precision = " + tempPrecision
				 + " recall = " + tempRecall
				 + " F1 = " + F1);
	}//Of F1

	/*
	 * static void genResult() throws IOException { BufferedReader reader =new
	 * BufferedReader(new
	 * FileReader("/home/starry/DataSet/competions/track/sat_write/test_cf.csv")
	 * ); BufferedWriter writer = new BufferedWriter(new
	 * FileWriter("/home/starry/DataSet/competions/track/sat_write/result.csv"))
	 * ;
	 * 
	 * ArrayList<Integer> user = new ArrayList<Integer>(); ArrayList<Integer>
	 * item = new ArrayList<Integer>();
	 * 
	 * while(reader.ready()) { String str = reader.readLine(); String[] parts =
	 * str.split(split_sign); int a = Integer.parseInt(parts[0]); int b =
	 * Integer.parseInt(parts[1]); user.add(a); item.add(b); } reader.close();
	 * 
	 * @SuppressWarnings("rawtypes") Iterator userIter = user.iterator();
	 * 
	 * @SuppressWarnings("rawtypes") Iterator ItemIter = item.iterator();
	 * 
	 * while(userIter.hasNext() && ItemIter.hasNext()) { int a = (Integer)
	 * userIter.next(); int b = (Integer) ItemIter.next(); double p = predict(a,
	 * b); writer.write(a + "," + b + "," + p + "\n"); } writer.flush();
	 * writer.close(); }
	 */
}
