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

public class Window extends Application {

	public static int rValue;
	public static Matrix inputImage;
	public static Matrix redChannel;
	public static Matrix blueChannel;
	public static Matrix greenChannel;
	public static Matrix outputRedChannel;
	public static Matrix outputBlueChannel;
	public static Matrix outputGreenChannel;
	public static Matrix outputImage;
	public boolean svdPending = false;
	boolean update = true;

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
		VBox rightright = new VBox();
		long time = System.currentTimeMillis();
		/**
		 * Create/load image to perform SVD on
		 */
		File tree = new File("C:\\Users\\piresa2\\Documents\\College Work\\Y1S2\\LinearAlgebra\\tree4.jpg");
		File face = new File(
				"C:\\Users\\piresa2\\Documents\\College Work\\Y1S2\\LinearAlgebra\\img_align_celeba\\img_align_celeba\\000212.jpg");

		inputImage = convertTo2DUsingGetRGB(ImageIO.read(tree));

		// System.out.println("" + inputImage.getRowDimension() + "," +
		// inputImage.getColumnDimension());

		time = System.currentTimeMillis();
		/*
		 * Matrix[] inputImage = new Matrix[3]; Random generator = new Random(100);
		 * 
		 * for(int i = 0;i<3;i++) { double[][] tempContent = new double[100][100];
		 * for(int j = 0;j<tempContent.length;j++) { for(int k = 0;
		 * k<tempContent[0].length;k++) { tempContent[j][k] =
		 * generator.nextDouble()*256; } } inputImage[i] = new Matrix(tempContent); }
		 */
//		inputImage = new Matrix(3,3);
//		for (int i = 0; i < inputImage.getRowDimension(); i++) {
//			for (int j = 0; j < inputImage.getColumnDimension(); j++) {
//				inputImage.set(i, j, Math.random() * 16777215);
//			}
//		}

		rValue = 0;
		outputImage = new Matrix(inputImage.getRowDimension(), inputImage.getColumnDimension());
		Slider rValSelector = new Slider(1, Math.min(inputImage.getColumnDimension(), inputImage.getRowDimension()), 2);

		Text rLabel = new Text("R Value : 2");
		Button button = new Button("Compress Image");

		rLabel.setFont(Font.font("Verdana", 20));

		// Matrix[] outputImage = new Matrix[3];
		MatrixCanvas originalImage = new MatrixCanvas(inputImage);

		MatrixCanvas redChannel = new MatrixCanvas(getChannel(inputImage, 2));
		MatrixCanvas greenChannel = new MatrixCanvas(getChannel(inputImage, 1));
		MatrixCanvas blueChannel = new MatrixCanvas(getChannel(inputImage, 0));
		MatrixCanvas resultImage = new MatrixCanvas(inputImage);
		MatrixCanvas resultRedChannel = new MatrixCanvas(redChannel.m);
		MatrixCanvas resultGreenChannel = new MatrixCanvas(greenChannel.m);
		MatrixCanvas resultBlueChannel = new MatrixCanvas(blueChannel.m);

		originalImage.drawData();
		System.out.println("1");

		redChannel.drawData();
		System.out.println("2");
		greenChannel.drawData();
		System.out.println("3");
		blueChannel.drawData();
		System.out.println("4");
		resultImage.drawData();
		System.out.println("5");
		resultRedChannel.drawData();
		System.out.println("6");
		resultGreenChannel.drawData();
		System.out.println("7");
		resultBlueChannel.drawData();
		System.out.println("8");
		new AnimationTimer() {
			@Override
			public void handle(long now) {
				if(update) {
					// System.out.println("0");
				originalImage.drawData();// System.out.println("1");

				redChannel.drawData();// System.out.println("2");
				greenChannel.drawData();// System.out.println("3");
				blueChannel.drawData();// System.out.println("4");
				resultImage.drawData();// System.out.println("5");
				resultRedChannel.drawData();// System.out.println("6");
				resultGreenChannel.drawData();// System.out.println("7");
				resultBlueChannel.drawData();// System.out.println("8");
				update = false;
				}
				

			}
		}.start();

		root.setPrefHeight(redChannel.m.getRowDimension() * 4);
		root.setPrefHeight(redChannel.m.getColumnDimension() * 2);

		originalImage.widthProperty().bind(root.widthProperty().divide(4));
		originalImage.heightProperty().bind(root.heightProperty().divide(3).multiply(2));
		resultImage.widthProperty().bind(originalImage.widthProperty());
		resultImage.heightProperty().bind(originalImage.heightProperty());

		blueChannel.widthProperty().bind(originalImage.widthProperty());
		redChannel.widthProperty().bind(originalImage.widthProperty());
		greenChannel.widthProperty().bind(originalImage.widthProperty());
		resultBlueChannel.widthProperty().bind(originalImage.widthProperty());
		resultRedChannel.widthProperty().bind(originalImage.widthProperty());
		resultGreenChannel.widthProperty().bind(originalImage.widthProperty());

		blueChannel.heightProperty().bind(root.heightProperty().divide(3));
		redChannel.heightProperty().bind(blueChannel.heightProperty());
		greenChannel.heightProperty().bind(blueChannel.heightProperty());
		resultBlueChannel.heightProperty().bind(blueChannel.heightProperty());
		resultRedChannel.heightProperty().bind(blueChannel.heightProperty());
		resultGreenChannel.heightProperty().bind(blueChannel.heightProperty());

		/**
		 * Build the pane-tree from the bottom most level to the root
		 */
		button.setOnAction(e -> {
			update = true;
			if (inputImage.getRowDimension() < inputImage.getColumnDimension()) {
				System.out.println("will break!");
				inputImage = inputImage.transpose();
				outputImage = new Matrix(inputImage.getRowDimension(), inputImage.getColumnDimension());
			}
			outputImage = new Matrix(inputImage.getRowDimension(), inputImage.getColumnDimension());
			for (int i = 0; i < 3; i++) {
				// inputImage = inputImage.transpose();
				Matrix IN = getChannel(inputImage, i);
				for (int z = 0; z < IN.getRowDimension(); z++) {
					for (int j = 0; j < IN.getColumnDimension(); j++) {
						IN.set(z, j, MatrixCanvas.checkRange((int) IN.get(z, j) >> i * 8));
					}
				}

				SingularValueDecomposition SVD = IN.svd();
				Matrix U = SVD.getU();

				Matrix S = SVD.getS();

				Matrix V = SVD.getV();
//				if (i == 0) {
//					System.out.println("A:");
//					printMatrix(IN);
//					System.out.println("U:");
//					printMatrix(U);
//					System.out.println("S:");
//					printMatrix(S);
//					System.out.println("Vt:");
//					printMatrix(V.transpose());
//				}

				for (int j = rValue; j < S.getRowDimension(); j++) {
					S.set(j, j, 0);
				}

				Matrix A = U.times(S).times(V.transpose());

				for (int z = 0; z < A.getRowDimension(); z++) {
					for (int j = 0; j < A.getColumnDimension(); j++) {
						A.set(z, j, MatrixCanvas.checkRange((int) A.get(z, j)) << i * 8);
					}
				}
				if (inputImage.getRowDimension() < inputImage.getColumnDimension())
					A = A.transpose();
				if (i == 0) {
					resultBlueChannel.setMatrix(A);
					System.out.println("Setting blue");
				} else if (i == 1) {
					resultGreenChannel.setMatrix(A);
					System.out.println("Setting blue2");
				} else {
					resultRedChannel.setMatrix(A);
					System.out.println("Setting blue3");
				}

				outputImage = outputImage.plus(A);
				// inputImage = inputImage.transpose();
				// outputImage = outputImage.transpose();
			}
			if (inputImage.getRowDimension() < inputImage.getColumnDimension())
				inputImage = inputImage.transpose();
			/*
			 * resultRedChannel.setMatrix(outputImage[);
			 * resultGreenChannel.setMatrix(outputImage[1]);
			 * resultBlueChannel.setMatrix(outputImage[2]);
			 */
			resultImage.setMatrix(outputImage);
			System.out.println("done");
		});

		rValSelector.valueProperty().addListener(e -> {
			rLabel.setText("R Value: " + (int) rValSelector.getValue());
			rValue = (int) rValSelector.getValue();
			// button.fire();
		});

		button.fire();
		leftleft.getChildren().add(originalImage);
		leftleft.getChildren().add(rValSelector);
		leftleft.getChildren().add(rLabel);
		leftleft.getChildren().add(button);

		leftright.getChildren().add(redChannel);
		leftright.getChildren().add(greenChannel);
		leftright.getChildren().add(blueChannel);

		rightleft.getChildren().add(resultRedChannel);
		rightleft.getChildren().add(resultGreenChannel);
		rightleft.getChildren().add(resultBlueChannel);

		rightright.getChildren().add(resultImage);

		root.getChildren().add(leftleft);
		root.getChildren().add(leftright);
		root.getChildren().add(rightleft);
		root.getChildren().add(rightright);

		primaryStage.setScene(new Scene(root));
		primaryStage.setFullScreen(true);
		primaryStage.setResizable(true);
		primaryStage.show();
		button.fire();
		System.out.println(System.currentTimeMillis() - time);
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
		System.out.println("We made it!");
		BufferedImage image;
		DecimalFormat f = new DecimalFormat("000000");
		for (int i = 0; i < n - s; i++) {
			File face = new File(
					"C:\\Users\\piresa2\\Documents\\College Work\\Y1S2\\LinearAlgebra\\img_align_celeba\\img_align_celeba\\"
							+ f.format(s + i + 1) + ".jpg");
			System.out.println(i);
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

	private static Matrix convertTo2DUsingGetRGB(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		Matrix result = new Matrix(width, height);
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				// System.out.printf("size:%s,%s: %s,%s%n", width,height,row,col);
				result.set(col, row, image.getRGB(col, row));

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
				result[i][j] = (((int) a.get(i, j) >> 8 * c) & 0xFF) << 8 * c;
				// System.out.printf("%s,%s:%s%n",i,j,result[i][j]);
			}
		}
		return new Matrix(result);
	}

	private static Matrix[] convertTo2D(BufferedImage image) {

		final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		final int width = image.getWidth();
		final int height = image.getHeight();
		final boolean hasAlphaChannel = image.getAlphaRaster() != null;
		Matrix[] result = new Matrix[width];
		result[0] = new Matrix(width, height);
		result[1] = new Matrix(width, height);
		result[2] = new Matrix(width, height);
		if (hasAlphaChannel) {
			final int pixelLength = 4;
			for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
				result[2].set(col, row, ((int) pixels[pixel + 1] & 0xff)); // blue
				result[1].set(col, row, (((int) pixels[pixel + 2] & 0xff) << 8)); // green
				result[0].set(col, row, (((int) pixels[pixel + 3] & 0xff) << 16)); // red
				col++;
				if (col == width) {
					col = 0;
					row++;
				}
			}
		} else {
			final int pixelLength = 3;
			for (int pixel = 0, row = 0, col = 0; pixel < pixels.length && row < width; pixel += pixelLength) {
				result[2].set(col, row, ((int) pixels[pixel] & 0xff)); // blue
				result[1].set(col, row, (((int) pixels[pixel + 1] & 0xff) << 8)); // green
				result[0].set(col, row, (((int) pixels[pixel + 2] & 0xff) << 16)); // red
				col++;
				if (col == width) {
					col = 0;
					row++;
				}
			}
		}

		return result;
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
