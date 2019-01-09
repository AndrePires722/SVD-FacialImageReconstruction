package FinalProject;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Slider;
import javafx.scene.control.Button;
import javafx.scene.text.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;

import javax.imageio.ImageIO;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class FacialReconstruction extends Application {
	public static boolean T1Done=false,T2Done=false,T3Done=false;
	public static Matrix inputRed,inputGreen,inputBlue;
	public static Matrix solution;
	public static void main(String[] args) {
		try {
			launch(args);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		/**
		 * Create Window and setup containers
		 */
		
		primaryStage.setTitle("SVD Compression");
		HBox root = new HBox();
		VBox leftleft = new VBox();
		VBox leftright = new VBox();
		VBox rightleft = new VBox();
		long time = System.currentTimeMillis();

		Matrix database = loadFromDatabase(0, 100);
		Matrix databaseRed = getChannel(database, 2);
		Matrix databaseGreen = getChannel(database, 1);
		Matrix databaseBlue = getChannel(database, 0);

		time = System.currentTimeMillis();
		Matrix inputImage = loadFromDatabase(99999,100000);
		 inputRed = getChannel(inputImage, 2);
		 inputGreen = getChannel(inputImage, 1);
		inputBlue = getChannel(inputImage, 0);
		Matrix outputImage;
		Thread T1 = new Thread() {
			public void run() {
				System.out.println("Solving red...");
				// Solve Red Channel
				Matrix LS = databaseRed.solve(inputRed);
				solution = LS;
				LS = databaseRed.times(LS);

				inputRed = convertRowToFace(LS, 178, 218, 0);
				inputRed = checkRange(inputRed);
				System.out.println("Solved Red");
				T1Done = true;
			}
		};
		Thread T2 = new Thread() {
			public void run() {
				System.out.println("Solving Green...");
				// Solve Green Channel
				Matrix LS = databaseGreen.solve(inputGreen);
				LS = databaseGreen.times(LS);

				inputGreen = convertRowToFace(LS, 178, 218, 0);
				inputGreen = checkRange(inputGreen);
				System.out.println("Solved Green");
				T2Done = true;
			}
		};
		Thread T3 = new Thread() {
			public void run() {
				// Solve Blue Channel
				System.out.println("Solving Blue...");
				Matrix LS = databaseBlue.solve(inputBlue);
				LS = databaseBlue.times(LS);

				inputBlue = convertRowToFace(LS, 178, 218, 0);
				inputBlue = checkRange(inputBlue);
				System.out.println("Solved Blue");
				T3Done = true;
			}
		};
		
		T1.start();
		T2.start();
		T3.start();
		inputImage = convertRowToFace(inputImage,178,218,0);
		while(!(T1Done && T2Done && T3Done)) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
System.out.println("Wait over");
		
		
		// Combine Channels
		outputImage = new Matrix(178, 218);
		for (int i = 0; i < outputImage.getRowDimension(); i++) {
			for (int j = 0; j < outputImage.getColumnDimension(); j++) {

				outputImage.set(i, j,
						new Color((int) inputRed.get(i, j), (int) inputGreen.get(i, j), (int) inputBlue.get(i, j))
								.getRGB());

			}
		}
	
		// LS = database.solve(inputImage);
		// printMatrix(LS);
		// LS = database.times(LS);
		// System.out.println("Solving took: " + (System.currentTimeMillis() - time));
		// System.out.printf("LS's dimensions are %s,%s", LS.getRowDimension(),
		// LS.getColumnDimension());
		// inputImage = convertRowToFace(LS, 178, 218, 0);

		System.out.println("Converted successfully");
		MatrixCanvas originalImage = new MatrixCanvas(inputImage);
		MatrixCanvas redChannel = new MatrixCanvas(shift(inputRed, 2));
		MatrixCanvas greenChannel = new MatrixCanvas(shift(inputGreen, 1));
		MatrixCanvas blueChannel = new MatrixCanvas(inputBlue);
		
		int max1 = 0,max2 = 0,max3 = 0;
		for(int i = 0;i<solution.getRowDimension();i++) {
			if(Math.abs(solution.get(i,0)) > Math.abs(solution.get(max1,0))) {
				max3 = max2;
				max2 = max1;
				max1 = i;
			}else if(Math.abs(solution.get(i,0)) > Math.abs(solution.get(max2,0))) {
				max3 = max2;
				max2 = i;
			}else if(Math.abs(solution.get(i,0)) > Math.abs(solution.get(max3,0))) {
				max3 = i;
			}
		}
		printMatrix(solution);
		System.out.printf("%s,%s,%s%n", max1,max2,max3);
		
		MatrixCanvas similar1 = new MatrixCanvas(convertRowToFace(database,178,218,max1));
		MatrixCanvas similar2 = new MatrixCanvas(convertRowToFace(database,178,218,max2));
		MatrixCanvas similar3 = new MatrixCanvas(convertRowToFace(database,178,218,max3));
		
		
		MatrixCanvas output = new MatrixCanvas(outputImage);

		originalImage.drawData();
		output.drawData();
		System.out.println("5");

		new AnimationTimer() {
			@Override
			public void handle(long now) {

				// System.out.println("0");
				originalImage.drawData();// System.out.println("1");
				output.drawData();
				similar1.drawData();
				similar2.drawData();
				similar3.drawData();
//				redChannel.drawData();// System.out.println("2");
//				greenChannel.drawData();// System.out.println("3");
//				blueChannel.drawData();// System.out.println("4");
			}
		}.start();
		
		root.setPrefHeight(redChannel.m.getRowDimension() * 4);
		root.setPrefHeight(redChannel.m.getColumnDimension() * 2);

		originalImage.widthProperty().bind(root.widthProperty().divide(3));
		originalImage.heightProperty().bind(root.heightProperty().divide(3));
		output.widthProperty().bind(originalImage.widthProperty());
		output.heightProperty().bind(originalImage.heightProperty());
		
		similar1.widthProperty().bind(originalImage.widthProperty());
		similar2.widthProperty().bind(originalImage.widthProperty());
		similar3.widthProperty().bind(originalImage.widthProperty());
		
		similar1.heightProperty().bind(originalImage.heightProperty());
		similar2.heightProperty().bind(originalImage.heightProperty());
		similar3.heightProperty().bind(originalImage.heightProperty());
		blueChannel.widthProperty().bind(originalImage.widthProperty());
		redChannel.widthProperty().bind(originalImage.widthProperty());
		greenChannel.widthProperty().bind(originalImage.widthProperty());

		blueChannel.heightProperty().bind(root.heightProperty().divide(3));
		redChannel.heightProperty().bind(blueChannel.heightProperty());
		greenChannel.heightProperty().bind(blueChannel.heightProperty());

		/**
		 * Build the pane-tree from the bottom most level to the root
		 */

		leftleft.getChildren().add(originalImage);
		leftright.getChildren().add(output);
		rightleft.getChildren().add(similar1);
		rightleft.getChildren().add(similar2);
		rightleft.getChildren().add(similar3);
//		leftright.getChildren().add(redChannel);
//		leftright.getChildren().add(greenChannel);
//		leftright.getChildren().add(blueChannel);

		root.getChildren().add(leftleft);
		root.getChildren().add(leftright);
		root.getChildren().add(rightleft);

		primaryStage.setScene(new Scene(root));
		primaryStage.setFullScreen(false);
		primaryStage.setResizable(true);
		primaryStage.show();
		System.out.println(System.currentTimeMillis() - time);
	}

	private static Matrix shift(Matrix A, int c) {
		for (int i = 0; i < A.getRowDimension(); i++) {
			for (int j = 0; j < A.getColumnDimension(); j++) {
				A.set(i, j, (int) A.get(i, j) << 8 * c);
			}
		}
		return A;
	}

	private static Matrix convertRowToFace(Matrix database, int width, int height, int srcCol) {
		Matrix result = new Matrix(width, height);

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {

				int index = row * width + col;
				result.set(col, row, database.get(index, srcCol));

			}
		}

		return result;
	}

	private static Matrix loadFromDatabase(int s, int n) throws IOException {
		assert n > s;
		Matrix result = new Matrix(178 * 218, n - s);
		BufferedImage image;
		DecimalFormat f = new DecimalFormat("000000");
		for (int i = 0; i < n - s; i++) {
			File face = new File(
					"C:\\Users\\piresa2\\Documents\\College Work\\Y1S2\\LinearAlgebra\\img_align_celeba\\img_align_celeba\\"
							+ f.format(s + i + 1) + ".jpg");

			image = ImageIO.read(face);
			for (int row = 0; row < 218; row++) {
				for (int col = 0; col < 178; col++) {
					int rgb = image.getRGB(col, row);
					int index = row * 178 + col;
					// System.out.printf("%s,%s%n",index,i);
					result.set(index, i, rgb);
				}
			}
		}
		return result;
	}

	public static Matrix getChannel(Matrix a, int c) {
		/*
		 * c=0 returns blue c=1 returns green c=2 returns red
		 */
		double[][] result = new double[a.getRowDimension()][a.getColumnDimension()];
		for (int i = 0; i < a.getRowDimension(); i++) {
			for (int j = 0; j < a.getColumnDimension(); j++) {
				result[i][j] = (((int) a.get(i, j) >> 8 * c) & 0xFF);
				// System.out.printf("%s,%s:%s%n",i,j,result[i][j]);
			}
		}
		return new Matrix(result);
	}

	public static Matrix checkRange(Matrix A) {
		for (int i = 0; i < A.getRowDimension(); i++) {
			for (int j = 0; j < A.getColumnDimension(); j++) {
				if (A.get(i, j) > 255)
					System.out.printf("checkRange : %s,%s;%s%n", i, j, A.get(i, j));
				A.set(i, j, checkRange((int) A.get(i, j)));
			}
		}
		return A;
	}

	public static int checkRange(int i) {
		if (i >= 0 && i < 255) {
			return i;
		}
		if (i < 0 || i == 255)
			return 0;
		return 255;
	}

	private static void printMatrix(Matrix m) {
		double[][] ar = m.getArrayCopy();
		DecimalFormat f = new DecimalFormat("0.0000");
		DecimalFormat n = new DecimalFormat("0.000");
		for (int i = 0; i < ar.length; i++) {
			for (int j = 0; j < ar[0].length; j++) {
				if (ar[i][j] > 0)
					System.out.print(" " + f.format(ar[i][j]));
				else
					System.out.print(" " + n.format(ar[i][j]));
			}
			System.out.println();
		}
	}
}
